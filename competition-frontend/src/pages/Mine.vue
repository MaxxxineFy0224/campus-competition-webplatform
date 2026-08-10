<template>
  <div class="page-mine">
    <div class="container">
      <!-- 用户资料卡 -->
      <div class="profile-card">
        <div class="profile-bg"></div>
        <div class="profile-content">
          <!-- 查看模式 -->
          <div v-if="!editMode" class="profile-info">
            <div class="avatar">{{ userName.charAt(0).toUpperCase() }}</div>
            <div class="profile-meta">
              <h2 class="user-name">{{ userName }}</h2>
              <p class="user-stats">
                发布 {{ myPosts.length }} 条组队帖
                <template v-if="userProfile?.school"> · {{ userProfile?.school }}</template>
              </p>
              <div v-if="userProfile?.major || userProfile?.grade" class="user-tags">
                <span v-if="userProfile?.major" class="tag">{{ userProfile?.major }}</span>
                <span v-if="userProfile?.grade" class="tag">{{ userProfile?.grade }}</span>
              </div>
            </div>
            <div class="profile-actions">
              <button class="btn-edit" @click="enterEditMode">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>
                编辑
              </button>
              <button class="btn-logout" @click="handleLogout">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" y1="12" x2="9" y2="12" /></svg>
                退出
              </button>
            </div>
          </div>

          <!-- 编辑模式 -->
          <div v-else class="profile-edit">
            <h2 class="edit-title">编辑个人资料</h2>
            <div class="edit-form">
              <div class="form-field">
                <label class="field-label">昵称</label>
                <input v-model="editForm.name" placeholder="请输入昵称 *" class="field-input" />
              </div>
              <div class="form-field">
                <label class="field-label">学校</label>
                <input v-model="editForm.school" placeholder="请输入学校" class="field-input" />
              </div>
              <div class="form-field">
                <label class="field-label">专业</label>
                <input v-model="editForm.major" placeholder="请输入专业" class="field-input" />
              </div>
              <div class="form-field">
                <label class="field-label">年级</label>
                <input v-model="editForm.grade" placeholder="如：大三" class="field-input" />
              </div>
            </div>
            <div class="edit-actions">
              <button class="btn-save" @click="handleSaveProfile">保存</button>
              <button class="btn-cancel" @click="cancelEdit">取消</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 主内容卡片 -->
      <div class="main-card">
        <div class="tabs-primary">
          <button v-for="tab in mainTabs" :key="tab.key" :class="['tab-primary-btn', { active: mainTab === tab.key }]" @click="mainTab = tab.key">
            <span class="tab-icon">{{ tab.icon }}</span> {{ tab.label }}
          </button>
        </div>

        <!-- 我的发布 -->
        <div v-if="mainTab === 'publish'" class="tab-content">
          <div v-if="myPosts.length > 0" class="list-items">
            <ListItem v-for="post in myPosts" :key="post.id" :post="post" :to="`/team/${post.id}`" @action="handleDelete(post)" action-label="删除" action-danger="true" />
          </div>
          <EmptyState v-else icon="📝" text="你还没有发布过组队帖" :action="{ label: '去发布', to: '/publish' }" />
        </div>

        <!-- 我的收藏 -->
        <div v-else class="tab-content">
          <div class="tabs-secondary">
            <button v-for="t in favTabs" :key="t.key" :class="['tab-secondary-btn', { active: favSubTab === t.key }]" @click="favSubTab = t.key">
              {{ t.label }} <span class="tab-badge">{{ t.count }}</span>
            </button>
          </div>
          <div v-if="favSubTab === 'comp'" class="sub-tab-content">
            <div v-if="favCompetitions.length > 0" class="list-items">
              <ListItem v-for="comp in favCompetitions" :key="comp.id" :comp="comp" :to="`/competition/${comp.id}`" @action="handleUnfavorite(comp.id, 'competition')" action-label="取消收藏" action-danger="true" />
            </div>
            <EmptyState v-else icon="⭐" text="还没有收藏竞赛" />
          </div>
          <div v-else class="sub-tab-content">
            <div v-if="favPosts.length > 0" class="list-items">
              <ListItem v-for="post in favPosts" :key="post.id" :post="post" :to="`/team/${post.id}`" @action="handleUnfavorite(post.id, 'team')" action-label="取消收藏" action-danger="true" />
            </div>
            <EmptyState v-else icon="🤝" text="还没有收藏组队帖" />
          </div>
        </div>
      </div>

      <!-- 快捷操作栏 -->
      <div class="quick-actions">
        <RouterLink to="/publish" class="action-btn primary">发布组队</RouterLink>
        <RouterLink to="/ai-match" class="action-btn ai">AI 智能匹配</RouterLink>
        <RouterLink to="/team" class="action-btn outline">组队广场</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import {
  getUser, saveUser, getFavorites, getCompetitions, getTeamPosts,
  saveTeamPosts, toggleFavorite, updateUserApi,
  syncAllData
} from '../utils/storage'
import { api } from '../utils/api'
import { useToast } from '../composables/useToast'
import { useModal } from '../composables/useModal'
import ListItem from '../components/ListItem.vue'
import EmptyState from '../components/EmptyState.vue'

const { toast } = useToast()
const { confirm } = useModal()
const router = useRouter()
const { isLoggedIn, user, logout, showLogin } = useAuth()

/* ---- 用户资料 ---- */
const userProfile = ref(null)
const editMode = ref(false)
const editForm = reactive({ name: '', school: '', major: '', grade: '' })

const userName = computed(() => userProfile.value?.name || user.value?.name || '用户')

async function loadUserData() {
  try {
    if (isLoggedIn.value) {
      const me = await api.getMe()
      if (me) {
        userProfile.value = me
        editForm.name = me.name || ''
        editForm.school = me.school || ''
        editForm.major = me.major || ''
        editForm.grade = me.grade || ''
      }
    }
  } catch { /* ignore */ }
  // 先从 localStorage 加载（立即渲染）
  favorites.value = getFavorites()
  competitions.value = getCompetitions()
  teamPosts.value = getTeamPosts()
  // 再从后端异步同步（刷新数据）
  if (isLoggedIn.value) {
    syncAllData().then(() => {
      favorites.value = getFavorites()
      competitions.value = getCompetitions()
      teamPosts.value = getTeamPosts()
    }).catch(() => {})
  }
}

onMounted(loadUserData)

function handleLogout() {
  logout()
  toast.success('已退出登录')
  router.push('/')
}

function enterEditMode() {
  const p = userProfile.value
  editForm.name = p?.name || user.value?.name || ''
  editForm.school = p?.school || ''
  editForm.major = p?.major || ''
  editForm.grade = p?.grade || ''
  editMode.value = true
}

async function handleSaveProfile() {
  if (!editForm.name.trim()) { toast.error('请输入昵称'); return }
  try {
    if (isLoggedIn.value && userProfile.value?.id) {
      const updated = await updateUserApi(userProfile.value.id, {
        name: editForm.name.trim(),
        school: editForm.school.trim(),
        major: editForm.major.trim(),
        grade: editForm.grade.trim(),
      })
      // 用后端返回的最新数据更新本地 — 合并而非覆盖
      if (updated) {
        saveUser(updated)
        userProfile.value = updated
      }
    }
    // 合并更新本地 user（仅覆盖编辑过的字段，保留 id/avatar/bio/skills 等）
    const merged = { ...userProfile.value, name: editForm.name.trim(), school: editForm.school.trim(), major: editForm.major.trim(), grade: editForm.grade.trim() }
    saveUser(merged)
    userProfile.value = merged
    editMode.value = false
    toast.success('个人信息已更新')
  } catch (e) {
    toast.error('保存失败: ' + (e.message || '未知错误'))
  }
}

function cancelEdit() {
  editMode.value = false
  const p = userProfile.value
  editForm.name = p?.name || ''
  editForm.school = p?.school || ''
  editForm.major = p?.major || ''
  editForm.grade = p?.grade || ''
}

/* ---- 数据 ---- */
const favorites = ref([])
const competitions = ref([])
const teamPosts = ref([])
const mainTab = ref('publish')
const favSubTab = ref('comp')

const mainTabs = [
  { key: 'publish', label: '我的发布', icon: '📋' },
  { key: 'favorite', label: '我的收藏', icon: '⭐' },
]

function refresh() {
  favorites.value = getFavorites()
  competitions.value = getCompetitions()
  teamPosts.value = getTeamPosts()
}

async function handleDelete(post) {
  const ok = await confirm(`确认删除「${post.title}」？\n\n删除后无法恢复。`, '删除组队帖')
  if (!ok) return
  try {
    await api.deleteTeamPost(parseInt(post.id, 10))
  } catch (e) {
    toast.error('删除失败: ' + (e.message || '未知错误'))
    return
  }
  const updated = teamPosts.value.filter(p => p.id !== post.id)
  saveTeamPosts(updated)
  teamPosts.value = updated
  refresh()
  toast.success('已删除')
}

function handleUnfavorite(id, type) {
  toggleFavorite(id, type)
  refresh()
  toast.success('已取消收藏')
}

const myPosts = computed(() =>
  userProfile.value || user.value
    ? teamPosts.value.filter(p => p.author === (userProfile.value?.name || user.value?.name))
    : []
)

const favCompetitions = computed(() =>
  competitions.value.filter(c => favorites.value.some(f => f.id === c.id && f.type === 'competition'))
)
const favPosts = computed(() =>
  teamPosts.value.filter(p => favorites.value.some(f => f.id === p.id && f.type === 'team'))
)
const favTabs = computed(() => [
  { key: 'comp', label: '竞赛收藏', count: favCompetitions.value.length },
  { key: 'team', label: '组队帖收藏', count: favPosts.value.length },
])
</script>

<style scoped>
.page-mine { min-height: 100vh; padding: 24px 16px 48px; background: linear-gradient(180deg, #f0f5ff 0%, #f5f7fa 100%); }
.container { width: 100%; max-width: 100%; margin: 0 auto; padding: 0 clamp(16px, 3vw, 48px); }

/* ===== 用户资料卡 ===== */
.profile-card { position: relative; border-radius: 18px; overflow: hidden; margin-bottom: 20px; box-shadow: 0 4px 24px rgba(22,119,255,0.18); }
.profile-bg { position: absolute; inset: 0; background: linear-gradient(135deg, #1677ff 0%, #4096ff 50%, #69b1ff 100%); z-index: 0; }
.profile-content { position: relative; z-index: 1; padding: 28px 24px; color: #fff; }
.profile-info { display: flex; align-items: flex-start; gap: 14px; flex-wrap: wrap; }
.avatar { width: 56px; height: 56px; min-width: 56px; border-radius: 50%; background: rgba(255,255,255,0.25); display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: 700; backdrop-filter: blur(4px); box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.profile-meta { flex: 1; min-width: 0; }
.user-name { font-size: 20px; font-weight: 700; margin: 0 0 4px; line-height: 1.3; }
.user-stats { font-size: 13px; opacity: 0.85; margin: 0 0 6px; }
.user-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.tag { display: inline-block; padding: 2px 10px; border-radius: 20px; background: rgba(255,255,255,0.2); font-size: 12px; backdrop-filter: blur(4px); }
.profile-actions { display: flex; flex-direction: column; gap: 8px; flex-shrink: 0; }
.btn-edit, .btn-logout { display: inline-flex; align-items: center; gap: 5px; background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: #fff; padding: 7px 14px; border-radius: 10px; cursor: pointer; font-size: 13px; font-weight: 500; transition: all 0.25s; backdrop-filter: blur(4px); white-space: nowrap; }
.btn-edit:hover, .btn-logout:hover { background: rgba(255,255,255,0.3); transform: translateY(-1px); }
.btn-logout { background: rgba(255,77,79,0.15); border-color: rgba(255,77,79,0.3); }
.btn-logout:hover { background: rgba(255,77,79,0.3); }

.profile-edit { padding: 4px 0; }
.edit-title { font-size: 18px; font-weight: 700; margin: 0 0 14px; }
.edit-form { display: flex; flex-direction: column; gap: 10px; }
.form-field { display: flex; flex-direction: column; gap: 4px; }
.field-label { font-size: 12px; opacity: 0.8; padding-left: 2px; }
.field-input { width: 100%; padding: 9px 12px; border: 1px solid rgba(255,255,255,0.3); border-radius: 10px; font-size: 14px; outline: none; background: rgba(255,255,255,0.15); color: #fff; font-family: inherit; transition: all 0.25s; box-sizing: border-box; }
.field-input::placeholder { color: rgba(255,255,255,0.5); }
.field-input:focus { border-color: rgba(255,255,255,0.6); background: rgba(255,255,255,0.22); box-shadow: 0 0 0 3px rgba(255,255,255,0.1); }
.edit-actions { display: flex; gap: 10px; margin-top: 14px; }
.btn-save { background: #fff; border: none; color: #1677ff; padding: 8px 22px; border-radius: 10px; cursor: pointer; font-size: 14px; font-weight: 600; transition: all 0.25s; }
.btn-save:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(0,0,0,0.12); }
.btn-cancel { background: rgba(255,255,255,0.12); border: 1px solid rgba(255,255,255,0.3); color: #fff; padding: 8px 18px; border-radius: 10px; cursor: pointer; font-size: 14px; transition: all 0.25s; }
.btn-cancel:hover { background: rgba(255,255,255,0.2); }

/* ===== 主内容卡片 ===== */
.main-card { background: #fff; border-radius: 16px; overflow: hidden; margin-bottom: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); border: 1px solid rgba(0,0,0,0.04); }
.tabs-primary { display: flex; border-bottom: 1px solid #f0f0f0; padding: 0 20px; }
.tab-primary-btn { flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px; background: none; border: none; font-size: 15px; padding: 16px 0; cursor: pointer; transition: all 0.25s; color: #999; border-bottom: 2px solid transparent; margin-bottom: -1px; }
.tab-primary-btn:hover { color: #1677ff; background: rgba(22,119,255,0.03); }
.tab-primary-btn.active { color: #1677ff; font-weight: 600; border-bottom-color: #1677ff; }
.tab-icon { font-size: 16px; }
.tab-content { padding: 4px 0; }
.tabs-secondary { display: flex; gap: 0; padding: 0 20px; border-bottom: 1px solid #f5f5f5; }
.tab-secondary-btn { display: flex; align-items: center; gap: 6px; font-size: 14px; padding: 14px 20px; cursor: pointer; transition: all 0.25s; background: none; border: none; color: #888; border-bottom: 2px solid transparent; margin-bottom: -1px; }
.tab-secondary-btn:hover { color: #1677ff; background: rgba(22,119,255,0.03); }
.tab-secondary-btn.active { color: #1677ff; font-weight: 600; border-bottom-color: #1677ff; }
.tab-badge { display: inline-flex; align-items: center; justify-content: center; min-width: 20px; height: 20px; padding: 0 6px; border-radius: 10px; background: #f0f5ff; color: #1677ff; font-size: 11px; font-weight: 600; }
.tab-secondary-btn.active .tab-badge { background: #1677ff; color: #fff; }
.sub-tab-content { padding: 4px 0; }
.list-items { display: flex; flex-direction: column; }
.quick-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.action-btn { flex: 1; min-width: 120px; display: flex; align-items: center; justify-content: center; gap: 6px; padding: 12px 16px; border-radius: 12px; font-size: 14px; font-weight: 500; text-decoration: none; transition: all 0.25s; white-space: nowrap; }
.action-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.action-btn.primary { background: linear-gradient(135deg, #1677ff, #4096ff); color: #fff; box-shadow: 0 2px 10px rgba(22,119,255,0.2); }
.action-btn.ai { background: linear-gradient(135deg, #722ed1, #b37feb); color: #fff; box-shadow: 0 2px 10px rgba(114,46,209,0.2); }
.action-btn.outline { background: #fff; color: #555; border: 1px solid #e8e8e8; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.action-btn.outline:hover { border-color: #1677ff; color: #1677ff; }

@media (max-width: 640px) {
  .page-mine { padding: 16px 12px 40px; }
  .profile-content { padding: 20px 16px; }
  .profile-info { flex-wrap: wrap; }
  .avatar { width: 48px; height: 48px; min-width: 48px; font-size: 20px; }
  .user-name { font-size: 18px; }
  .profile-actions { flex-direction: row; width: 100%; margin-top: 0; }
  .btn-edit, .btn-logout { flex: 1; justify-content: center; }
  .tabs-primary { padding: 0 12px; }
  .tab-primary-btn { font-size: 14px; padding: 14px 0; }
  .tabs-secondary { padding: 0 12px; overflow-x: auto; }
  .tab-secondary-btn { padding: 12px 14px; font-size: 13px; }
  .quick-actions { flex-direction: column; gap: 10px; }
  .action-btn { min-width: auto; }
}
</style>