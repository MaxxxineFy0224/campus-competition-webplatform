<template>
  <div class="team-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>
    <div class="page-container">
      <!-- 顶部标题区域 -->
      <div class="page-header">
        <div class="header-inner">
          <div class="header-decoration"></div>
          <h1 class="page-title">组队广场</h1>
        </div>
        <p class="page-subtitle">找到志同道合的队友，一起冲击竞赛奖项</p>
      </div>

      <!-- 分类标签 -->
      <div class="tabs-wrapper">
        <button
          v-for="tab in CATEGORY_TABS"
          :key="tab"
          class="tab-btn"
          :class="{ active: category === tab }"
          @click="category = tab"
        >
          {{ tab }}
        </button>
      </div>

      <!-- 统计信息 -->
      <div class="count-info">
        <span class="count-label">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
          共 {{ filtered.length }} 个组队帖
        </span>
        <span v-if="category !== '全部'" class="count-tag">「{{ category }}」</span>
      </div>

      <!-- 卡片列表 -->
      <div v-if="filtered.length > 0" class="responsive-grid">
        <RouterLink
          v-for="post in filtered"
          :key="post.id"
          :to="`/team/${post.id}`"
          class="team-card"
          :class="{ expired: getDeadline(post) && isExpired(post) }"
        >
          <!-- 过期标记 -->
          <div v-if="getDeadline(post) && isExpired(post)" class="expired-badge">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <path d="M12 8v4M12 16h.01" />
            </svg>
            已过期
          </div>

          <!-- 卡片头部：作者信息 -->
          <div class="card-header-section">
            <div class="author-row">
              <div class="author-avatar" :style="getAvatarStyle(post)">{{ post.author.charAt(0) }}</div>
              <div class="author-meta">
                <div class="author-name">{{ post.author }}</div>
                <div class="post-time">{{ formatTime(post.createdAt) }}</div>
              </div>
            </div>
          </div>

          <!-- 卡片主体 -->
          <div class="card-body-section">
            <div class="competition-name">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
              {{ post.competitionTitle }}
            </div>
            <h3 class="post-title">{{ post.title }}</h3>
            <div class="leader-badge">
              <span class="tag tag-leader">👑 队长</span>
            </div>
            <div class="skills-row">
              <span
                v-for="(s, i) in post.skills.slice(0, 3)"
                :key="s"
                class="tag tag-skill"
                :class="'skill-' + (i % 3)"
              >{{ s }}</span>
              <span v-if="post.skills.length > 3" class="tag tag-more">+{{ post.skills.length - 3 }}</span>
            </div>
            <div v-if="getDeadline(post)" class="deadline-info" :class="{ expired: isExpired(post) }">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 6v6l4 2" />
              </svg>
              组队截止：{{ getDeadline(post) }}
            </div>
          </div>

          <!-- 卡片底部 -->
          <div class="card-footer-section">
            <span class="view-detail">
              查看详情
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m9 18 6-6-6-6" />
              </svg>
            </span>
          </div>
        </RouterLink>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            <line x1="2" y1="2" x2="22" y2="22" />
          </svg>
        </div>
        <p class="empty-text">暂无组队帖</p>
        <p class="empty-subtext">还没有人发布组队信息，快来发布第一条吧</p>
        <RouterLink to="/publish" class="btn btn-primary">+ 发布组队</RouterLink>
      </div>

      <!-- FAB 按钮 -->
      <RouterLink to="/publish" class="fab-btn" title="发布组队">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 5v14M5 12h14" />
        </svg>
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTeamPosts, getCompetitions } from '../utils/storage'

const CATEGORY_TABS = ['全部', '学科类', '创新创业类', '文体类']
const CATEGORY_MAP = {
  学科类: ['数学建模', '编程算法', '电子设计', '机器人', '语言文学', '信息安全', '智能硬件', '计算机设计'],
  创新创业类: ['创新创业'],
  文体类: ['设计传媒'],
}

const GRADIENT_COLORS = [
  ['#667eea', '#764ba2'],
  ['#f093fb', '#f5576c'],
  ['#4facfe', '#00f2fe'],
  ['#43e97b', '#38f9d7'],
  ['#fa709a', '#fee140'],
  ['#a18cd1', '#fbc2eb'],
]

function getPostTab(category) {
  for (const [tab, cats] of Object.entries(CATEGORY_MAP)) {
    if (cats.includes(category)) return tab
  }
  return '学科类'
}

function formatTime(dateStr) {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  const mins = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function isExpired(deadlineStr) {
  if (!deadlineStr) return false
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineStr)
  deadline.setHours(23, 59, 59, 999)
  return deadline < now
}

const posts = ref([])
const competitions = ref([])
const category = ref('全部')

onMounted(() => {
  posts.value = getTeamPosts()
  competitions.value = getCompetitions()
})

const compMap = computed(() => {
  const map = {}
  competitions.value.forEach((c) => { map[c.id] = c })
  return map
})

const filtered = computed(() => {
  let list = [...posts.value]
  if (category.value !== '全部') {
    list = list.filter((p) => {
      const comp = compMap.value[p.competitionId]
      if (!comp) return false
      return getPostTab(comp.category) === category.value
    })
  }
  list.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  return list
})

function getDeadline(post) {
  const comp = compMap.value[post.competitionId]
  return comp ? comp.deadline : null
}

function getAvatarStyle(post) {
  const idx = post.author.length % GRADIENT_COLORS.length
  const [c1, c2] = GRADIENT_COLORS[idx]
  return {
    background: `linear-gradient(135deg, ${c1}, ${c2})`,
  }
}
</script>

<style scoped>
.team-page {
  padding-top: 32px;
  padding-bottom: 96px;
  min-height: 100vh;
  background: var(--color-bg, #f5f7fa);
  position: relative;
  overflow: hidden;
}

.team-page .page-container {
  position: relative;
  z-index: 1;
}

/* ===== 背景装饰 ===== */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
}

.bg-circle-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(22, 119, 255, 0.04) 0%, transparent 70%);
  top: -100px;
  right: -100px;
  animation: bgFloat 8s ease-in-out infinite;
}

.bg-circle-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(114, 46, 209, 0.03) 0%, transparent 70%);
  bottom: 10%;
  left: -80px;
  animation: bgFloat 10s ease-in-out infinite reverse;
}

.bg-circle-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(22, 119, 255, 0.03) 0%, transparent 70%);
  top: 40%;
  left: 50%;
  animation: bgFloat 12s ease-in-out infinite 2s;
}

@keyframes bgFloat {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(20px, -20px); }
}

/* ===== 页面标题 ===== */
.page-header {
  margin-bottom: 28px;
  position: relative;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.header-decoration {
  width: 4px;
  height: 32px;
  border-radius: 2px;
  background: linear-gradient(180deg, #1677ff, #722ed1);
  flex-shrink: 0;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #1677ff, #722ed1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.page-subtitle {
  color: #888;
  font-size: 14px;
  line-height: 1.6;
  margin-left: 16px;
  padding-left: 16px;
  border-left: 2px solid #e8e8e8;
}

/* ===== 分类标签 ===== */
.tabs-wrapper {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.tab-btn {
  padding: 8px 24px;
  border-radius: 9999px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 0.5px;
  background: #f0f0f0;
  color: #666;
}

.tab-btn:hover {
  background: #e6f4ff;
  color: #1677ff;
}

.tab-btn.active {
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

/* ===== 统计信息 ===== */
.count-info {
  font-size: 13px;
  color: #999;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.count-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.count-tag {
  color: #1677ff;
  font-weight: 600;
}

/* ===== 组队卡片 ===== */
.team-card {
  display: flex;
  flex-direction: column;
  padding: 20px;
  position: relative;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.04);
  text-decoration: none;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  color: inherit;
}

.team-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.1), 0 4px 12px rgba(22, 119, 255, 0.08);
  border-color: rgba(22, 119, 255, 0.12);
}

.team-card:active {
  transform: translateY(-2px);
}

.team-card.expired {
  opacity: 0.55;
}

.team-card:hover .view-detail svg {
  transform: translateX(3px);
}

/* ===== 过期标记 ===== */
.expired-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #999;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 9999px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

/* ===== 卡片头部 ===== */
.card-header-section {
  margin-bottom: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s ease;
}

.team-card:hover .author-avatar {
  transform: scale(1.08);
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f1f1f;
  line-height: 1.4;
}

.post-time {
  font-size: 12px;
  color: #bbb;
  line-height: 1.4;
}

/* ===== 卡片主体 ===== */
.card-body-section {
  flex: 1;
  margin-bottom: 12px;
}

.competition-name {
  font-size: 12px;
  color: #1677ff;
  font-weight: 500;
  margin-bottom: 6px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  color: #1f1f1f;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.leader-badge {
  margin-bottom: 10px;
}

.tag-leader {
  background: linear-gradient(135deg, #fff7e6, #fff1cc);
  color: #d48806;
  font-size: 11px;
  font-weight: 500;
  border: 1px solid rgba(212, 136, 6, 0.15);
}

.skills-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.tag-skill {
  font-size: 11px;
  font-weight: 500;
  padding: 3px 10px;
}

.tag-skill.skill-0 {
  background: linear-gradient(135deg, #f9f0ff, #ede0ff);
  color: #722ed1;
  border: 1px solid rgba(114, 46, 209, 0.12);
}

.tag-skill.skill-1 {
  background: linear-gradient(135deg, #e6f7ff, #bae7ff);
  color: #1677ff;
  border: 1px solid rgba(22, 119, 255, 0.12);
}

.tag-skill.skill-2 {
  background: linear-gradient(135deg, #fff7e6, #ffe7ba);
  color: #d48806;
  border: 1px solid rgba(212, 136, 6, 0.12);
}

.tag-more {
  background: linear-gradient(135deg, #f5f5f5, #fafafa);
  color: #999;
  font-size: 11px;
  border: 1px solid #f0f0f0;
}

.deadline-info {
  font-size: 12px;
  color: #888;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.deadline-info.expired {
  color: #ccc;
}

/* ===== 卡片底部 ===== */
.card-footer-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  display: flex;
  justify-content: center;
}

.view-detail {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  color: #1677ff;
  transition: gap 0.2s ease;
}

.team-card:hover .view-detail {
  gap: 8px;
}

.view-detail svg {
  transition: transform 0.2s ease;
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 80px 24px;
}

.empty-icon {
  margin-bottom: 20px;
  opacity: 0.5;
  color: #d0d0d0;
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
  color: #bbb;
  margin-bottom: 28px;
}

/* ===== FAB 按钮 ===== */
.fab-btn {
  position: fixed;
  bottom: 28px;
  right: 28px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 24px rgba(22, 119, 255, 0.4);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  z-index: 60;
  text-decoration: none;
}

.fab-btn:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 8px 32px rgba(22, 119, 255, 0.5);
}

.fab-btn:active {
  transform: scale(0.95);
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .team-page {
    padding-top: 24px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-subtitle {
    font-size: 13px;
  }

  .team-card {
    padding: 16px;
  }

  .fab-btn {
    bottom: 20px;
    right: 20px;
    width: 48px;
    height: 48px;
  }

  .fab-btn svg {
    width: 24px;
    height: 24px;
  }
}
</style>