/**
 * 数据访问层 —— localStorage + 后端 API 双写
 *
 * 同步读取 localStorage（页面立即渲染），异步从后端刷新数据。
 * 写操作（收藏/发布/更新用户）同时提交到后端。
 *
 * 认证：api.js 自动从 localStorage('auth_token') 读取 token 并附带在请求头。
 */

import { api } from './api.js';

const KEY_COMPETITIONS = 'competitions'
const KEY_TEAM_POSTS = 'team_posts'
const KEY_USER = 'user'
const KEY_FAVORITES = 'favorites'

/* ================================================================
 * localStorage 同步读写
 * ================================================================ */
function get(key, fallback) {
  try { const v = localStorage.getItem(key); return v ? JSON.parse(v) : fallback }
  catch { return fallback }
}
function set(key, value) { localStorage.setItem(key, JSON.stringify(value)) }

export function getCompetitions() { return get(KEY_COMPETITIONS, []) }
export function getCompetitionById(id) { return getCompetitions().find(c => c.id === id) }
export function saveCompetitions(list) { set(KEY_COMPETITIONS, list) }

export function getTeamPosts() { return get(KEY_TEAM_POSTS, []) }
export function getTeamPostById(id) { return getTeamPosts().find(p => p.id === id) }
export function saveTeamPosts(list) { set(KEY_TEAM_POSTS, list) }

export function getUser() { return get(KEY_USER, null) }
export function saveUser(user) { set(KEY_USER, user) }

export function getFavorites() { return get(KEY_FAVORITES, []) }

export function toggleFavorite(id, type) {
  const favs = getFavorites()
  const idx = favs.findIndex(f => f.id === id && f.type === type)
  if (idx > -1) favs.splice(idx, 1)
  else favs.push({ id, type })
  set(KEY_FAVORITES, favs)
  return favs
}

/* ================================================================
 * 格式映射：后端 → 前端
 * ================================================================ */
function mapComp(c) {
  if (!c || c.teamSize) return c // 已是前端格式
  return {
    ...c, id: String(c.id),
    teamSize: c.maxTeamSize
      ? (c.minTeamSize === c.maxTeamSize ? `${c.minTeamSize}人` : `${c.minTeamSize}-${c.maxTeamSize}人`)
      : '不限',
    image: c.imageUrl || '', date: c.eventDate || c.deadline,
    tags: [c.category, c.level].filter(Boolean),
    status: c.statusText || c.status,
  }
}

function mapPost(p) {
  if (!p || p.skills) return p
  return {
    ...p, id: String(p.id), competitionId: String(p.competitionId),
    author: p.authorName || '匿名用户',
    skills: p.requiredSkills ? p.requiredSkills.split(',').map(s => s.trim()).filter(Boolean) : [],
    status: p.status === 0 ? '招募中' : '已过期',
    teamDeadline: p.deadline,
  }
}

/* ================================================================
 * 异步同步函数（页面 onMounted 中调用）
 * ================================================================ */

export async function syncCompetitions() {
  try {
    const data = await api.getCompetitions({ size: '50' })
    if (data?.records) { saveCompetitions(data.records.map(mapComp)); console.log('[API] 竞赛已同步') }
  } catch (e) { console.warn('[API] 竞赛同步失败:', e.message) }
}

export async function syncTeamPosts() {
  try {
    const data = await api.getTeamPosts({ size: '50' })
    if (data?.records) { saveTeamPosts(data.records.map(mapPost)); console.log('[API] 组队帖已同步') }
  } catch (e) { console.warn('[API] 组队帖同步失败:', e.message) }
}

export async function syncUser() {
  if (!api.isLogin()) return
  try {
    const data = await api.getMe()
    if (data) { saveUser(data); console.log('[API] 用户已同步:', data.name) }
  } catch (e) { console.warn('[API] 用户同步失败:', e.message) }
}

export async function syncFavorites() {
  if (!api.isLogin()) return
  try {
    const [compFavs, teamFavs] = await Promise.all([
      api.getFavoriteCompetitions().catch(() => ({ records: [] })),
      api.getFavoriteTeamPosts().catch(() => ({ records: [] })),
    ])
    const favs = []
    if (compFavs?.records) compFavs.records.forEach(c => favs.push({ id: String(c.id), type: 'competition' }))
    if (teamFavs?.records) teamFavs.records.forEach(p => favs.push({ id: String(p.id), type: 'team' }))
    set(KEY_FAVORITES, favs)
    console.log('[API] 收藏已同步:', favs.length, '条')
  } catch (e) { console.warn('[API] 收藏同步失败:', e.message) }
}

export async function syncAllData() {
  await Promise.allSettled([syncCompetitions(), syncTeamPosts(), syncUser(), syncFavorites()])
}

/** 登录并同步 */
export async function login(name, password) {
  const data = await api.login(name, password)
  if (data.token) {
    api.setToken(data.token)
    saveUser({ id: String(data.userId), name: data.userName })
    await syncAllData()
  }
  return data
}

/** 注册并同步 */
export async function register(formData) {
  const data = await api.register(formData)
  if (data.token) {
    api.setToken(data.token)
    saveUser({ id: String(data.userId), name: data.userName })
    await syncAllData()
  }
  return data
}

/** 退出登录 */
export function logout() {
  api.clearToken()
  localStorage.removeItem(KEY_USER)
  localStorage.removeItem(KEY_FAVORITES)
}

/** 发布组队帖 */
export async function publishTeamPost(formData) {
  return api.createTeamPost({
    competitionId: parseInt(formData.competitionId, 10) || 1,
    title: formData.title,
    description: formData.description,
    requiredSkills: Array.isArray(formData.skills) ? formData.skills.join(',') : formData.skills,
    contact: formData.contact,
    deadline: formData.teamDeadline,
    needCount: formData.needCount || 1,
  })
}

/** 更新用户 */
export async function updateUserApi(userId, data) {
  return api.updateUser(userId, data)
}
