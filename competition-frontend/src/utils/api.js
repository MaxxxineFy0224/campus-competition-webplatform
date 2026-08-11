/**
 * 后端 API 请求封装（支持 JWT 登录）
 *
 * 后端返回：{ "code": 200, "message": "success", "data": ... }
 *
 * Token 管理：
 *   - 登录后 token 存入 localStorage('auth_token')
 *   - 所有请求自动携带 Authorization: Bearer <token>
 */

const BASE_URL = '/api';

/* ---- Token 管理 ---- */

const TOKEN_KEY = 'auth_token';

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

/* ---- 通用请求 ---- */

async function request(method, path, body) {
  const url = `${BASE_URL}${path}`;
  const options = {
    method,
    headers: { 'Content-Type': 'application/json' },
  };

  const token = getToken();
  if (token) {
    options.headers['Authorization'] = `Bearer ${token}`;
  }
  if (body) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(url, options);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);

  const result = await response.json();
  if (result.code !== 200) throw new Error(result.message || '请求失败');

  return result.data;
}

/* ---- SSE 流式请求 ---- */

async function streamRequest(path, body, onChunk) {
  const url = `${BASE_URL}${path}`;
  const options = {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  };
  const token = getToken();
  if (token) options.headers['Authorization'] = `Bearer ${token}`;

  const response = await fetch(url, options);
  if (!response.ok) throw new Error(`HTTP ${response.status}`);

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    const chunk = decoder.decode(value, { stream: true });
    const lines = chunk.split('\n');
    for (const line of lines) {
      if (line.startsWith('data:')) {
        try {
          const parsed = JSON.parse(line.slice(5).trim());
          if (onChunk) onChunk(parsed);
        } catch { /* skip */ }
      }
    }
  }
}

/* ---- 导出 ---- */

export const api = {
  /* ---- 基础方法 ---- */
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body),
  put: (path, body) => request('PUT', path, body),
  delete: (path) => request('DELETE', path),
  stream: (path, body, onChunk) => streamRequest(path, body, onChunk),

  /* ---- Token ---- */
  getToken,
  setToken,
  clearToken,
  isLogin: () => !!getToken(),

  /* ---- 认证 ---- */
  login: (name, password) => api.post('/auth/login', { name, password }),
  register: (data) => api.post('/auth/register', data),
  changePassword: (oldPassword, newPassword) =>
    api.put('/auth/password', { oldPassword, newPassword }),

  /* ---- 用户 ---- */
  getMe: () => api.get('/users/me'),
  getUser: (id) => api.get(`/users/${id}`),
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  getUserPosts: (id) => api.get(`/users/${id}/posts`),
  getUserFavorites: (id, type = 'competition') =>
    api.get(`/users/${id}/favorites?type=${type}`),

  /* ---- 竞赛 ---- */
  getCompetitions: (params = {}) => {
    const qs = new URLSearchParams(params).toString();
    return api.get(`/competitions${qs ? '?' + qs : ''}`);
  },
  getCompetitionById: (id) => api.get(`/competitions/${id}`),
  toggleCompetitionFavorite: (id) => api.post(`/competitions/${id}/favorite`),
  getFavoriteCompetitions: (page = 1, size = 50) =>
    api.get(`/competitions/favorites?page=${page}&size=${size}`),

  /* ---- 组队帖 ---- */
  getTeamPosts: (params = {}) => {
    const qs = new URLSearchParams(params).toString();
    return api.get(`/team-posts${qs ? '?' + qs : ''}`);
  },
  getTeamPostById: (id) => api.get(`/team-posts/${id}`),
  createTeamPost: (data) => api.post('/team-posts', data),
  deleteTeamPost: (id) => api.delete(`/team-posts/${id}`),
  toggleTeamPostFavorite: (id) => api.post(`/team-posts/${id}/favorite`),
  getFavoriteTeamPosts: (page = 1, size = 50) =>
    api.get(`/team-posts/favorites?page=${page}&size=${size}`),

  /* ---- 组队申请 ---- */
  applyTeamPost: (teamPostId, message) =>
    api.post('/team-applications', { teamPostId, message }),
  getTeamApplications: (teamPostId) =>
    api.get(`/team-applications?teamPostId=${teamPostId}`),
  approveApplication: (id) =>
    api.put(`/team-applications/${id}/approve`),
  rejectApplication: (id, reason) =>
    api.put(`/team-applications/${id}/reject`, { reason }),
  getMyApplications: () =>
    api.get('/team-applications/my'),

  /* ---- 消息通知 ---- */
  getNotifications: (type = '', page = 1, size = 20) => {
    const qs = new URLSearchParams({ page: String(page), size: String(size) });
    if (type) qs.set('type', type);
    return api.get(`/notifications?${qs.toString()}`);
  },
  getUnreadNotificationCount: () =>
    api.get('/notifications/unread-count'),
  markNotificationRead: (id) =>
    api.put(`/notifications/${id}/read`),
  markAllNotificationsRead: () =>
    api.put('/notifications/read-all'),

  /* ---- 评论 ---- */
  getComments: (teamPostId, page = 1, size = 20) =>
    api.get(`/comments?teamPostId=${teamPostId}&page=${page}&size=${size}`),
  createComment: (teamPostId, content, parentId) =>
    api.post('/comments', { teamPostId, content, parentId }),
  deleteComment: (id) =>
    api.delete(`/comments/${id}`),
  likeComment: (id) =>
    api.post(`/comments/${id}/like`),

  /* ---- AI 聊天 ---- */
  chatStream: (message, onChunk) =>
    api.stream('/ai/stream/chat', { message }, onChunk),
  chat: (message) =>
    api.post('/ai-match/chat', { message }),
};

export default api;
