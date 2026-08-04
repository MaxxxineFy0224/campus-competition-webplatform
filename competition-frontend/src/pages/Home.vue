<template>
  <div class="page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1"></div>
        <div class="hero-shape hero-shape-2"></div>
        <div class="hero-shape hero-shape-3"></div>
        <div class="hero-shape hero-shape-4"></div>
        <div class="hero-grid"></div>
      </div>
      <div class="page-container hero-content">
        <div class="hero-text">
          <div class="hero-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9z" />
              <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9z" />
              <path d="M4 22h16" />
              <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
              <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
              <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
            </svg>
            校园竞赛平台
          </div>
          <h1 class="hero-title">
            发现竞赛，<br><span class="gradient-text">组队同行</span>
          </h1>
          <p class="hero-subtitle">汇聚全品类竞赛信息，AI 智能匹配优质队友，让你的竞赛之路不再孤单</p>
          <div class="hero-actions">
            <RouterLink to="/team" class="btn btn-primary btn-lg hero-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
              去组队广场
            </RouterLink>
            <RouterLink to="/ai-match" class="btn btn-ai btn-lg hero-btn">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2a3 3 0 0 0-3 3v1a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
                <path d="M5 15a7 7 0 0 1 14 0v1a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-1Z" />
                <path d="M8 18v2a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-2" />
              </svg>
              AI 智能匹配
            </RouterLink>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-card">
            <div class="stat-icon stat-icon-1">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
            </div>
            <span class="stat-number">12</span>
            <span class="stat-label">竞赛项目</span>
          </div>
          <div class="stat-card">
            <div class="stat-icon stat-icon-2">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </div>
            <span class="stat-number">10</span>
            <span class="stat-label">活跃队伍</span>
          </div>
          <div class="stat-card">
            <div class="stat-icon stat-icon-3">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7" />
                <rect x="14" y="3" width="7" height="7" />
                <rect x="14" y="14" width="7" height="7" />
                <rect x="3" y="14" width="7" height="7" />
              </svg>
            </div>
            <span class="stat-number">5</span>
            <span class="stat-label">竞赛类别</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 搜索和筛选 -->
    <div class="page-container" style="padding-bottom: 48px">
      <div class="filter-bar">
        <div class="search-wrapper">
          <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <input
            type="text"
            class="search-input"
            v-model="search"
            placeholder="搜索竞赛名称..."
          />
          <button v-if="search" class="search-clear" @click="search = ''">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div class="category-tabs">
          <button
            v-for="tab in CATEGORY_TABS"
            :key="tab"
            :class="['tab-btn', { active: category === tab }]"
            @click="category = tab"
          >
            {{ tab }}
          </button>
        </div>
      </div>

      <div class="result-meta">
        共 <strong>{{ filtered.length }}</strong> 个竞赛
        <span v-if="search" class="meta-tag">搜索「{{ search }}」</span>
        <span v-if="category !== '全部'" class="meta-tag">分类「{{ category }}」</span>
      </div>

      <!-- 竞赛卡片网格 -->
      <div v-if="filtered.length > 0" class="responsive-grid">
        <RouterLink
          v-for="comp in filtered"
          :key="comp.id"
          :to="`/competition/${comp.id}`"
          class="competition-card"
        >
          <div class="card-banner" :style="{ background: getCategoryGradient(comp.category) }">
            <div class="card-banner-overlay"></div>
            <div class="card-banner-icon">{{ getCategoryIcon(comp.category) }}</div>
          </div>
          <div class="card-top">
            <div class="card-tags">
              <span class="tag card-tag-category">{{ comp.category }}</span>
              <span
                class="tag card-tag-status"
                :class="getStatus(comp.deadline).cls"
              >
                {{ getStatus(comp.deadline).label }}
              </span>
            </div>
            <button
              class="fav-btn"
              @click.prevent.stop="handleToggleFav(comp.id, $event)"
              :title="isFav(comp.id) ? '取消收藏' : '收藏'"
            >
              <HeartIcon :filled="isFav(comp.id)" />
            </button>
          </div>

          <div class="card-body">
            <h3 class="card-title">{{ comp.title }}</h3>
            <div class="card-meta">
              <span class="meta-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2" /><line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" /></svg>
                截止 {{ comp.deadline }}
              </span>
              <span class="meta-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>
                {{ comp.teamSize }}
              </span>
            </div>
          </div>

          <div class="card-footer">
            <span class="card-organizer">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" /></svg>
              {{ comp.organizer }}
            </span>
            <span class="card-level">{{ comp.level }}</span>
          </div>
        </RouterLink>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state-card">
        <div class="empty-icon">🔍</div>
        <h3 class="empty-title">未找到相关竞赛</h3>
        <p class="empty-desc">换个关键词或分类试试</p>
        <button class="btn btn-outline" @click="resetFilter">重置筛选</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCompetitions, getFavorites, toggleFavorite } from '../utils/storage'
import { useToast } from '../composables/useToast'
import HeartIcon from '../components/HeartIcon.vue'

const CATEGORY_TABS = ['全部', '学科竞赛', '创新创业', '文体活动']
const CATEGORY_MAP = {
  学科竞赛: ['数学建模', '编程算法', '电子设计', '机器人', '语言文学', '信息安全', '智能硬件', '计算机设计'],
  创新创业: ['创新创业'],
  文体活动: ['设计传媒'],
}

const CATEGORY_GRADIENTS = {
  '数学建模': 'linear-gradient(135deg, #667eea, #764ba2)',
  '编程算法': 'linear-gradient(135deg, #4facfe, #00f2fe)',
  '电子设计': 'linear-gradient(135deg, #f093fb, #f5576c)',
  '机器人': 'linear-gradient(135deg, #43e97b, #38f9d7)',
  '语言文学': 'linear-gradient(135deg, #fa709a, #fee140)',
  '信息安全': 'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  '智能硬件': 'linear-gradient(135deg, #13547a, #80d0c7)',
  '计算机设计': 'linear-gradient(135deg, #0c3483, #a2b6df)',
  '创新创业': 'linear-gradient(135deg, #ff5858, #f09819)',
  '设计传媒': 'linear-gradient(135deg, #c471f5, #fa71cd)',
}

const CATEGORY_ICONS = {
  '数学建模': '📐',
  '编程算法': '💻',
  '电子设计': '🔌',
  '机器人': '🤖',
  '语言文学': '📖',
  '信息安全': '🔒',
  '智能硬件': '📱',
  '计算机设计': '🖥️',
  '创新创业': '💡',
  '设计传媒': '🎨',
}

function mapToTab(category) {
  for (const [tab, cats] of Object.entries(CATEGORY_MAP)) {
    if (cats.includes(category)) return tab
  }
  return '学科竞赛'
}

function getCategoryGradient(category) {
  return CATEGORY_GRADIENTS[category] || 'linear-gradient(135deg, #667eea, #764ba2)'
}

function getCategoryIcon(category) {
  return CATEGORY_ICONS[category] || '🏆'
}

const statusMap = {
  open: { cls: 'status-open', label: '报名中' },
  urgent: { cls: 'status-urgent', label: '即将截止' },
  expired: { cls: 'status-expired', label: '已截止' },
}

function getStatus(deadlineStr) {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineStr)
  deadline.setHours(23, 59, 59, 999)
  const diff = (deadline - now) / (1000 * 60 * 60 * 24)
  if (diff < 0) return statusMap.expired
  if (diff <= 7) return statusMap.urgent
  return statusMap.open
}

const competitions = ref([])
const favorites = ref([])
const search = ref('')
const category = ref('全部')
const { toast } = useToast()

onMounted(() => {
  competitions.value = getCompetitions()
  favorites.value = getFavorites()
})

function isFav(id) {
  return favorites.value.some((f) => f.id === id && f.type === 'competition')
}

function handleToggleFav(id, e) {
  e.preventDefault()
  e.stopPropagation()
  const updated = toggleFavorite(id, 'competition')
  favorites.value = updated
  const nowFav = updated.some((f) => f.id === id && f.type === 'competition')
  toast.success(nowFav ? '已收藏' : '已取消收藏')
}

function resetFilter() {
  search.value = ''
  category.value = '全部'
}

const filtered = computed(() => {
  let list = [...competitions.value]
  if (category.value !== '全部') {
    list = list.filter((c) => mapToTab(c.category) === category.value)
  }
  if (search.value.trim()) {
    const kw = search.value.trim().toLowerCase()
    list = list.filter((c) => c.title.toLowerCase().includes(kw))
  }
  list.sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
  return list
})
</script>

<style scoped>
.page {
  min-height: calc(100vh - 60px);
}

/* ===== Hero Section ===== */
.hero-section {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 50%, #722ed1 100%);
  padding: 60px 0 48px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.hero-shape-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #fff 0%, transparent 70%);
  top: -200px;
  right: -100px;
  opacity: 0.08;
}

.hero-shape-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, #fff 0%, transparent 70%);
  bottom: -120px;
  left: -80px;
  opacity: 0.06;
}

.hero-shape-3 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  top: 20%;
  left: 55%;
}

.hero-shape-4 {
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  bottom: 30%;
  right: 25%;
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  pointer-events: none;
}

.hero-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.hero-text {
  flex: 1;
  min-width: 0;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 20px;
  font-size: 13px;
  color: rgba(255,255,255,0.9);
  margin-bottom: 20px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  line-height: 1.15;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.hero-title .gradient-text {
  background: linear-gradient(135deg, #fff, #bae0ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 16px;
  color: rgba(255,255,255,0.78);
  line-height: 1.7;
  margin-bottom: 30px;
  max-width: min(500px, 100%);
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-btn {
  gap: 8px;
  padding: 14px 28px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-btn:hover {
  transform: translateY(-2px);
}

.hero-stats {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}

.stat-card {
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 16px;
  padding: 20px 24px;
  text-align: center;
  min-width: 110px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,0.12) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.35s ease;
  pointer-events: none;
}

.stat-card:hover {
  background: rgba(255,255,255,0.14);
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:active {
  transform: translateY(-1px);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

.stat-icon-1 {
  background: rgba(255, 214, 0, 0.2);
  color: #ffd600;
}

.stat-icon-2 {
  background: rgba(0, 230, 118, 0.2);
  color: #00e676;
}

.stat-icon-3 {
  background: rgba(41, 182, 246, 0.2);
  color: #29b6f6;
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  letter-spacing: -1px;
}

.stat-label {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
  font-weight: 500;
}

/* ===== 筛选栏 ===== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: -28px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  position: relative;
  z-index: 10;
}

.search-wrapper {
  flex: 1;
  max-width: min(480px, 100%);
  position: relative;
}

.search-clear {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #bbb;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-clear:hover {
  color: #666;
}

.category-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 8px 22px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  border: 1.5px solid #e5e4e7;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
  color: #666;
}

.tab-btn:hover {
  border-color: #1677ff;
  color: #1677ff;
  background: #f0f5ff;
}

.tab-btn.active {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
}

/* ===== 结果信息 ===== */
.result-meta {
  font-size: 14px;
  color: #888;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.result-meta strong {
  color: #333;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  background: #f0f5ff;
  color: #1677ff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* ===== 竞赛卡片 ===== */
.competition-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  border: 1px solid rgba(0,0,0,0.04);
  overflow: hidden;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.competition-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.12), 0 4px 12px rgba(22, 119, 255, 0.08);
  border-color: rgba(22, 119, 255, 0.15);
}

.competition-card:active {
  transform: translateY(-2px);
}

/* 卡片横幅 */
.card-banner {
  height: 80px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.card-banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 40%, rgba(0,0,0,0.15) 100%);
}

.card-banner-icon {
  font-size: 32px;
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.2));
  z-index: 1;
}

/* 卡片顶部 */
.card-top {
  padding: 14px 16px 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.card-tag-category {
  background: #f0f5ff;
  color: #1677ff;
  font-weight: 500;
}

.card-tag-status {
  font-weight: 600;
}

.status-open {
  background: #f6ffed;
  color: #52c41a;
}

.status-urgent {
  background: #fff2f0;
  color: #ff4d4f;
  animation: pulse-bg 2s ease-in-out infinite;
}

.status-expired {
  background: #f5f5f5;
  color: #bbb;
}

@keyframes pulse-bg {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.fav-btn {
  background: rgba(255,255,255,0.9);
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 6px;
  cursor: pointer;
  line-height: 1;
  transition: all 0.2s ease;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.fav-btn:hover {
  background: #fff1f0;
  border-color: #ffccc7;
  transform: scale(1.1);
}

/* 卡片主体 */
.card-body {
  flex: 1;
  padding: 10px 16px;
}

.card-title {
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

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #888;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-top: 1px solid #f5f5f5;
  gap: 8px;
  margin-top: auto;
}

.card-organizer {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #aaa;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-level {
  font-size: 12px;
  font-weight: 500;
  color: #1677ff;
  background: #f0f5ff;
  padding: 2px 10px;
  border-radius: 10px;
  flex-shrink: 0;
}

/* ===== 空状态卡片 ===== */
.empty-state-card {
  text-align: center;
  padding: 72px 24px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f0f0f0;
}

.empty-icon {
  font-size: 56px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .hero-section {
    padding: 40px 0 36px;
  }

  .hero-content {
    flex-direction: column;
    text-align: center;
  }

  .hero-subtitle {
    max-width: 100%;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-stats {
    width: 100%;
    justify-content: center;
  }

  .stat-card {
    min-width: 90px;
    padding: 14px 16px;
  }

  .stat-icon {
    width: 32px;
    height: 32px;
    border-radius: 10px;
    margin-bottom: 4px;
  }

  .stat-icon svg {
    width: 16px;
    height: 16px;
  }

  .stat-number {
    font-size: 24px;
  }

  .filter-bar {
    margin-top: -20px;
  }

  .search-wrapper {
    max-width: 100% !important;
  }

  .card-banner {
    height: 64px;
  }

  .card-banner-icon {
    font-size: 26px;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-actions .btn {
    width: 100%;
  }

  .hero-stats {
    gap: 10px;
  }

  .stat-card {
    min-width: 78px;
    padding: 12px 14px;
  }

  .stat-number {
    font-size: 22px;
  }

  .card-banner {
    height: 56px;
  }
}
</style>