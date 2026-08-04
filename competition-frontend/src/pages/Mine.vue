<template>
  <div class="page-mine">
    <div class="container">
      <!-- 顶部用户资料卡 -->
      <div class="profile-card">
        <div class="profile-bg"></div>
        <div class="profile-content">
          <!-- 未登录 / 无用户 -->
          <div v-if="!user && !editMode" class="profile-empty">
            <div class="avatar-placeholder">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <h2 class="welcome-title">欢迎来到竞队</h2>
            <p class="welcome-desc">创建你的个人档案，开启竞赛组队之旅</p>
            <button class="btn-create" @click="editMode = true">创建档案</button>
          </div>

          <!-- 查看模式 -->
          <div v-else-if="user && !editMode" class="profile-info">
            <div class="avatar">{{ user.name.charAt(0).toUpperCase() }}</div>
            <div class="profile-meta">
              <h2 class="user-name">{{ user.name }}</h2>
              <p class="user-stats">
                发布 {{ myPosts.length }} 条组队帖
                <template v-if="user.school"> · {{ user.school }}</template>
              </p>
              <div v-if="user.major || user.grade" class="user-tags">
                <span v-if="user.major" class="tag">{{ user.major }}</span>
                <span v-if="user.grade" class="tag">{{ user.grade }}</span>
              </div>
            </div>
            <button class="btn-edit" @click="editMode = true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
              编辑资料
            </button>
          </div>

          <!-- 编辑模式 -->
          <div v-else class="profile-edit">
            <h2 class="edit-title">编辑个人资料</h2>
            <div class="edit-form">
              <div class="form-field">
                <label class="field-label">昵称</label>
                <input v-model="form.name" placeholder="请输入昵称 *" class="field-input" />
              </div>
              <div class="form-field">
                <label class="field-label">学校</label>
                <input v-model="form.school" placeholder="请输入学校" class="field-input" />
              </div>
              <div class="form-field">
                <label class="field-label">专业</label>
                <input v-model="form.major" placeholder="请输入专业" class="field-input" />
              </div>
              <div class="form-field">
                <label class="field-label">年级</label>
                <input v-model="form.grade" placeholder="如：大三" class="field-input" />
              </div>
            </div>
            <div class="edit-actions">
              <button class="btn-save" @click="handleSave">保存</button>
              <button class="btn-cancel" @click="cancelEdit">取消</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 主内容卡片 -->
      <div class="main-card">
        <!-- 主 Tab 栏 -->
        <div class="tabs-primary">
          <button
            v-for="tab in mainTabs"
            :key="tab.key"
            class="tab-primary-btn"
            :class="{ active: mainTab === tab.key }"
            @click="mainTab = tab.key"
          >
            <span class="tab-icon">{{ tab.icon }}</span>
            {{ tab.label }}
          </button>
        </div>

        <!-- 我的发布 -->
        <div v-if="mainTab === 'publish'" class="tab-content">
          <div v-if="myPosts.length > 0" class="list-items">
            <ListItem
              v-for="post in myPosts"
              :key="post.id"
              :post="post"
              :to="`/team/${post.id}`"
              @action="handleDelete(post)"
              action-label="删除"
              action-danger="true"
            />
          </div>
          <EmptyState
            v-else
            icon="📝"
            :text="user ? '你还没有发布过组队帖' : '创建档案后开始发布组队帖'"
            :action="user ? { label: '去发布', to: '/publish' } : null"
          />
        </div>

        <!-- 我的收藏 -->
        <div v-else class="tab-content">
          <!-- 收藏子 Tab -->
          <div class="tabs-secondary">
            <button
              v-for="t in favTabs"
              :key="t.key"
              class="tab-secondary-btn"
              :class="{ active: favSubTab === t.key }"
              @click="favSubTab = t.key"
            >
              {{ t.label }}
              <span class="tab-badge">{{ t.count }}</span>
            </button>
          </div>

          <!-- 竞赛收藏 -->
          <div v-if="favSubTab === 'comp'" class="sub-tab-content">
            <div v-if="favCompetitions.length > 0" class="list-items">
              <ListItem
                v-for="comp in favCompetitions"
                :key="comp.id"
                :comp="comp"
                :to="`/competition/${comp.id}`"
                @action="handleUnfavorite(comp.id, 'competition')"
                action-label="取消收藏"
                action-danger="true"
              />
            </div>
            <EmptyState v-else icon="⭐" text="还没有收藏竞赛" />
          </div>

          <!-- 组队帖收藏 -->
          <div v-else class="sub-tab-content">
            <div v-if="favPosts.length > 0" class="list-items">
              <ListItem
                v-for="post in favPosts"
                :key="post.id"
                :post="post"
                :to="`/team/${post.id}`"
                @action="handleUnfavorite(post.id, 'team')"
                action-label="取消收藏"
                action-danger="true"
              />
            </div>
            <EmptyState v-else icon="🤝" text="还没有收藏组队帖" />
          </div>
        </div>
      </div>

      <!-- 快捷操作栏 -->
      <div class="quick-actions">
        <RouterLink to="/publish" class="action-btn primary">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 5v14M5 12h14" />
          </svg>
          发布组队
        </RouterLink>
        <RouterLink to="/ai-match" class="action-btn ai">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2a3 3 0 0 0-3 3v1a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
            <path d="M5 15a7 7 0 0 1 14 0v1a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-1Z" />
            <path d="M8 18v2a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-2" />
          </svg>
          AI 智能匹配
        </RouterLink>
        <RouterLink to="/team" class="action-btn outline">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
          组队广场
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  getUser, saveUser, getFavorites, getCompetitions, getTeamPosts,
  saveTeamPosts, toggleFavorite,
} from '../utils/storage'
import { useToast } from '../composables/useToast'
import { useModal } from '../composables/useModal'
import ListItem from '../components/ListItem.vue'
import EmptyState from '../components/EmptyState.vue'

const { toast } = useToast()
const { confirm } = useModal()

const user = ref(null)
const favorites = ref([])
const competitions = ref([])
const teamPosts = ref([])
const mainTab = ref('publish')
const favSubTab = ref('comp')
const editMode = ref(false)
const form = reactive({ name: '', school: '', major: '', grade: '' })

const mainTabs = [
  { key: 'publish', label: '我的发布', icon: '📋' },
  { key: 'favorite', label: '我的收藏', icon: '⭐' },
]

onMounted(() => {
  const u = getUser()
  user.value = u
  if (u) {
    form.name = u.name
    form.school = u.school || ''
    form.major = u.major || ''
    form.grade = u.grade || ''
  }
  favorites.value = getFavorites()
  competitions.value = getCompetitions()
  teamPosts.value = getTeamPosts()
})

function refresh() {
  favorites.value = getFavorites()
  competitions.value = getCompetitions()
  teamPosts.value = getTeamPosts()
}

function handleSave() {
  if (!form.name.trim()) {
    toast.error('请输入昵称')
    return
  }
  const newUser = {
    name: form.name.trim(),
    school: form.school.trim(),
    major: form.major.trim(),
    grade: form.grade.trim(),
    createdAt: user.value?.createdAt || new Date().toISOString(),
  }
  saveUser(newUser)
  user.value = newUser
  editMode.value = false
  toast.success('个人信息已更新')
}

function cancelEdit() {
  editMode.value = false
  if (user.value) {
    form.name = user.value.name
    form.school = user.value.school || ''
    form.major = user.value.major || ''
    form.grade = user.value.grade || ''
  }
}

async function handleDelete(post) {
  const confirmed = await confirm(`确认删除「${post.title}」？\n\n删除后无法恢复。`, '删除组队帖')
  if (!confirmed) return
  const updatedPosts = teamPosts.value.filter((p) => p.id !== post.id)
  saveTeamPosts(updatedPosts)
  teamPosts.value = updatedPosts
  if (favorites.value.some((f) => f.id === post.id && f.type === 'team')) {
    toggleFavorite(post.id, 'team')
  }
  refresh()
  toast.success('已删除')
}

function handleUnfavorite(id, type) {
  toggleFavorite(id, type)
  refresh()
  toast.success('已取消收藏')
}

const myPosts = computed(() =>
  user.value ? teamPosts.value.filter((p) => p.author === user.value.name) : []
)

const favCompetitions = computed(() =>
  competitions.value.filter((c) =>
    favorites.value.some((f) => f.id === c.id && f.type === 'competition')
  )
)

const favPosts = computed(() =>
  teamPosts.value.filter((p) =>
    favorites.value.some((f) => f.id === p.id && f.type === 'team')
  )
)

const favTabs = computed(() => [
  { key: 'comp', label: '竞赛收藏', count: favCompetitions.value.length },
  { key: 'team', label: '组队帖收藏', count: favPosts.value.length },
])
</script>

<style scoped>
/* ===== 页面容器 ===== */
.page-mine {
  min-height: 100vh;
  padding: 24px 16px 48px;
  background: linear-gradient(180deg, #f0f5ff 0%, #f5f7fa 100%);
}

.container {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 clamp(16px, 3vw, 48px);
}

/* ===== 用户资料卡 ===== */
.profile-card {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 4px 24px rgba(22, 119, 255, 0.18);
}

.profile-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 50%, #69b1ff 100%);
  z-index: 0;
}

.profile-content {
  position: relative;
  z-index: 1;
  padding: 32px 28px;
  color: #fff;
}

/* 空状态 */
.profile-empty {
  text-align: center;
  padding: 8px 0;
}

.avatar-placeholder {
  width: 64px;
  height: 64px;
  margin: 0 auto 14px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 6px;
  color: #fff;
}

.welcome-desc {
  font-size: 14px;
  opacity: 0.85;
  margin: 0 0 18px;
  line-height: 1.5;
}

.btn-create {
  background: #fff;
  border: none;
  color: #1677ff;
  padding: 10px 32px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.btn-create:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
}

/* 查看模式 */
.profile-info {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.avatar {
  width: 60px;
  height: 60px;
  min-width: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.profile-meta {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px;
  color: #fff;
  line-height: 1.3;
}

.user-stats {
  font-size: 13px;
  opacity: 0.85;
  margin: 0 0 8px;
  line-height: 1.4;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 12px;
  backdrop-filter: blur(4px);
}

.btn-edit {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 8px 18px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
  transition: all 0.25s ease;
  white-space: nowrap;
  backdrop-filter: blur(4px);
}

.btn-edit:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

/* 编辑模式 */
.profile-edit {
  padding: 4px 0;
}

.edit-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 16px;
  color: #fff;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field-label {
  font-size: 12px;
  opacity: 0.8;
  padding-left: 2px;
}

.field-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-family: inherit;
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.field-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.field-input:focus {
  border-color: rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.22);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.1);
}

.edit-actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
}

.btn-save {
  background: #fff;
  border: none;
  color: #1677ff;
  padding: 9px 26px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.btn-save:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 9px 20px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.25s ease;
}

.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* ===== 主内容卡片 ===== */
.main-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

/* 主 Tab */
.tabs-primary {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
}

.tab-primary-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: none;
  border: none;
  font-size: 15px;
  padding: 16px 0;
  cursor: pointer;
  transition: all 0.25s ease;
  color: #999;
  font-weight: 400;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  position: relative;
}

.tab-primary-btn:hover {
  color: #1677ff;
  background: rgba(22, 119, 255, 0.03);
}

.tab-primary-btn.active {
  color: #1677ff;
  font-weight: 600;
  border-bottom-color: #1677ff;
}

.tab-icon {
  font-size: 16px;
}

/* Tab 内容区 */
.tab-content {
  padding: 4px 0;
}

/* 收藏子 Tab */
.tabs-secondary {
  display: flex;
  gap: 0;
  padding: 0 20px;
  border-bottom: 1px solid #f5f5f5;
}

.tab-secondary-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  padding: 14px 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: none;
  border: none;
  color: #888;
  font-weight: 400;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  white-space: nowrap;
}

.tab-secondary-btn:hover {
  color: #1677ff;
  background: rgba(22, 119, 255, 0.03);
}

.tab-secondary-btn.active {
  color: #1677ff;
  font-weight: 600;
  border-bottom-color: #1677ff;
}

.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: #f0f5ff;
  color: #1677ff;
  font-size: 11px;
  font-weight: 600;
}

.tab-secondary-btn.active .tab-badge {
  background: #1677ff;
  color: #fff;
}

.sub-tab-content {
  padding: 4px 0;
}

/* ===== 列表项容器 ===== */
.list-items {
  display: flex;
  flex-direction: column;
}

/* ===== 快捷操作栏 ===== */
.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.25s ease;
  cursor: pointer;
  white-space: nowrap;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.action-btn:active {
  transform: translateY(0);
}

.action-btn.primary {
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  box-shadow: 0 2px 10px rgba(22, 119, 255, 0.2);
}

.action-btn.ai {
  background: linear-gradient(135deg, #722ed1, #b37feb);
  color: #fff;
  box-shadow: 0 2px 10px rgba(114, 46, 209, 0.2);
}

.action-btn.outline {
  background: #fff;
  color: #555;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-btn.outline:hover {
  border-color: #1677ff;
  color: #1677ff;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .page-mine {
    padding: 16px 12px 40px;
  }

  .profile-content {
    padding: 24px 18px;
  }

  .profile-info {
    flex-wrap: wrap;
  }

  .avatar {
    width: 52px;
    height: 52px;
    min-width: 52px;
    font-size: 22px;
  }

  .user-name {
    font-size: 20px;
  }

  .btn-edit {
    width: 100%;
    text-align: center;
    justify-content: center;
    margin-top: -8px;
  }

  .tabs-primary {
    padding: 0 12px;
  }

  .tab-primary-btn {
    font-size: 14px;
    padding: 14px 0;
  }

  .tabs-secondary {
    padding: 0 12px;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .tab-secondary-btn {
    padding: 12px 14px;
    font-size: 13px;
  }

  .quick-actions {
    flex-direction: column;
    gap: 10px;
  }

  .action-btn {
    min-width: auto;
  }

  .edit-form {
    gap: 10px;
  }

  .field-input {
    padding: 9px 12px;
    font-size: 13px;
  }
}

@media (max-width: 400px) {
  .profile-content {
    padding: 18px 14px;
  }

  .avatar {
    width: 46px;
    height: 46px;
    min-width: 46px;
    font-size: 20px;
  }

  .user-name {
    font-size: 18px;
  }

  .tab-primary-btn {
    font-size: 13px;
    padding: 12px 0;
  }
}
</style>