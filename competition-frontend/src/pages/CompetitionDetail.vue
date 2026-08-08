<template>
  <div class="detail-page">
    <!-- ===== 渐变 Banner 区域 ===== -->
    <div class="banner">
      <div class="banner-bg" />
      <div class="banner-content">
        <button class="back-btn" @click="router.back()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="m15 18-6-6 6-6" />
          </svg>
          返回
        </button>
        <div class="banner-tags">
          <span class="tag tag-category">{{ comp?.category }}</span>
          <span class="tag" :class="statusClass">{{ statusLabel }}</span>
          <span class="tag tag-level">{{ comp?.level }}</span>
        </div>
        <h1 class="banner-title">{{ comp?.title }}</h1>
      </div>
    </div>

    <!-- ===== 主体内容 ===== -->
    <div class="body-wrapper">
      <!-- 信息卡片 -->
      <div class="card info-card">
        <div class="info-grid">
          <InfoCell icon="🏛️" label="主办方" :value="comp?.organizer" />
          <InfoCell icon="📅" label="报名截止" :value="comp?.deadline" />
          <InfoCell icon="👥" label="队伍规模" :value="comp?.teamSize" />
          <div class="link-cell" @click="handleCopyLink">
            <span class="link-icon">🔗</span>
            <div>
              <div class="link-label">官网链接</div>
              <div class="link-value">点击复制</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 竞赛介绍 -->
      <div class="card desc-card">
        <h3 class="section-title">
          <span class="section-icon">📄</span>
          竞赛介绍
        </h3>
        <div class="desc-wrapper">
          <p ref="descRef" class="desc-text" :class="{ expanded: descExpanded }">{{ comp?.description }}</p>
          <button v-if="descOverflows" class="expand-btn" @click="descExpanded = !descExpanded">
            {{ descExpanded ? '收起' : '展开全文' }}
            <svg
              width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
              stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"
              class="expand-arrow" :class="{ rotated: descExpanded }"
            >
              <path d="m6 9 6 6 6-6" />
            </svg>
          </button>
        </div>
        <div class="tags-section" v-if="comp?.tags?.length">
          <span v-for="tag in comp?.tags" :key="tag" class="tag tag-item">{{ tag }}</span>
        </div>
      </div>

      <!-- 相关组队帖 -->
      <div class="posts-section">
        <h3 class="section-title">
          <span class="section-icon">📋</span>
          该赛事组队帖
          <span v-if="relatedPosts.length > 0" class="post-count">({{ relatedPosts.length }})</span>
        </h3>
        <div v-if="relatedPosts.length > 0" class="post-list">
          <RouterLink
            v-for="post in relatedPosts"
            :key="post.id"
            :to="`/team/${post.id}`"
            class="card post-card"
          >
            <div class="post-info">
              <div class="post-title">{{ post.title }}</div>
              <div class="post-meta">
                <span class="meta-item">👤 {{ post.author }}</span>
                <span class="meta-item">👥 {{ post.currentCount }}/{{ post.currentCount + post.needCount }}人</span>
                <span class="meta-item" :class="post.status === '招募中' ? 'recruiting' : 'closed'">{{ post.status }}</span>
              </div>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="post-arrow">
              <path d="m9 18 6-6-6-6" />
            </svg>
          </RouterLink>
        </div>
        <div v-else class="empty-state">
          <div class="empty-icon">📋</div>
          <p class="empty-text">暂无组队帖，来做第一个发起人吧</p>
          <RouterLink to="/publish" class="btn btn-primary btn-sm">+ 发布组队</RouterLink>
        </div>
      </div>

      <!-- 底部占位，防止被固定栏遮挡 -->
      <div class="bottom-spacer" />
    </div>

    <!-- ===== 底部固定操作栏 ===== -->
    <div class="bottom-bar">
      <div class="bottom-bar-inner">
        <button class="fav-btn" :class="{ favorited: isFav }" @click="handleToggleFav">
          <HeartIcon :filled="isFav" />
          <span>{{ isFav ? '已收藏' : '收藏' }}</span>
        </button>
        <button
          class="btn btn-primary action-btn"
          :class="{ disabled: statusExpired }"
          :disabled="statusExpired"
          @click="handleCopyLink"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
          </svg>
          {{ statusExpired ? '已截止报名' : '前往报名' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCompetitionById, getFavorites, toggleFavorite, getTeamPosts } from '../utils/storage'
import { useToast } from '../composables/useToast'
import { useAuth } from '../composables/useAuth'
import HeartIcon from '../components/HeartIcon.vue'
import InfoCell from '../components/InfoCell.vue'

const route = useRoute()
const router = useRouter()
const { toast } = useToast()

const comp = ref(null)
const favorites = ref([])
const relatedPosts = ref([])
const descExpanded = ref(false)
const descOverflows = ref(false)
const descRef = ref(null)

function computeStatus(deadlineStr) {
  const now = new Date(); now.setHours(0, 0, 0, 0)
  const deadline = new Date(deadlineStr); deadline.setHours(23, 59, 59, 999)
  const diff = (deadline - now) / (1000 * 60 * 60 * 24)
  if (diff < 0) return { label: '已截止', cls: 'status-expired', expired: true }
  if (diff <= 7) return { label: '即将截止', cls: 'status-urgent', expired: false }
  return { label: '报名中', cls: 'status-open', expired: false }
}

onMounted(() => {
  comp.value = getCompetitionById(route.params.id)
  favorites.value = getFavorites()
  const allPosts = getTeamPosts()
  relatedPosts.value = allPosts.filter((p) => p.competitionId === route.params.id)

  nextTick(() => {
    if (descRef.value) {
      const el = descRef.value
      el.style.webkitLineClamp = 'unset'
      const fullHeight = el.scrollHeight
      el.style.webkitLineClamp = '3'
      const lineHeight = parseFloat(getComputedStyle(el).lineHeight)
      descOverflows.value = fullHeight > lineHeight * 3 + 2
    }
  })
})

const statusInfo = computed(() => comp.value ? computeStatus(comp.value.deadline) : { label: '', cls: '', expired: false })
const statusLabel = computed(() => statusInfo.value.label)
const statusClass = computed(() => statusInfo.value.cls)
const statusExpired = computed(() => statusInfo.value.expired)

const isFav = computed(() =>
  favorites.value.some((f) => f.id === route.params.id && f.type === 'competition')
)

function handleToggleFav() {
  const { isLoggedIn, showLogin } = useAuth()
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  const updated = toggleFavorite(route.params.id, 'competition')
  favorites.value = updated
  const nowFav = updated.some((f) => f.id === route.params.id && f.type === 'competition')
  toast.success(nowFav ? '已收藏' : '已取消收藏')
}

function handleCopyLink() {
  const link = comp.value?.website || `https://www.baidu.com/s?wd=${encodeURIComponent(comp.value?.title || '')}`
  navigator.clipboard.writeText(link).then(() => {
    toast.success('官网链接已复制')
  }).catch(() => {
    toast.info(`请搜索：${comp.value?.title || ''}`)
  })
}
</script>

<style scoped>
/* ========== 页面容器 ========== */
.detail-page {
  width: 100%;
  min-height: 100vh;
  background: var(--color-bg, #f5f7fa);
}

/* ========== Banner ========== */
.banner {
  position: relative;
  overflow: hidden;
  padding: 32px 24px 48px;
}

.banner-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 40%, #722ed1 100%);
  z-index: 0;
}

.banner-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 60% at 10% 20%, rgba(255,255,255,0.15) 0%, transparent 60%),
    radial-gradient(ellipse 60% 50% at 90% 80%, rgba(255,255,255,0.08) 0%, transparent 60%);
  pointer-events: none;
}

.banner-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 clamp(16px, 3vw, 48px);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 9999px;
  transition: all 0.25s ease;
  margin-bottom: 20px;
}

.back-btn:hover {
  background: rgba(255,255,255,0.25);
  transform: translateX(-2px);
}

.banner-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.banner-tags .tag {
  padding: 4px 14px;
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  line-height: 1.5;
}

.tag-category {
  background: rgba(255,255,255,0.2);
  color: #fff;
  backdrop-filter: blur(4px);
}

.tag-level {
  background: rgba(255,255,255,0.15);
  color: rgba(255,255,255,0.85);
  backdrop-filter: blur(4px);
}

.status-open {
  background: #f6ffed;
  color: #52c41a;
}

.status-urgent {
  background: #fff1f0;
  color: #ff4d4f;
}

.status-expired {
  background: #f5f5f5;
  color: #999;
}

.banner-title {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.35;
  color: #fff;
  text-shadow: 0 2px 16px rgba(0,0,0,0.15);
  margin: 0;
}

/* ========== 主体 ========== */
.body-wrapper {
  width: 100%;
  max-width: 100%;
  margin: -24px auto 0;
  padding: 0 clamp(16px, 3vw, 48px);
  position: relative;
  z-index: 2;
}

/* ========== 信息卡片 ========== */
.info-card {
  padding: 24px 28px;
  margin-bottom: 20px;
  border-radius: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 180px), 1fr));
  gap: clamp(8px, 1.5vw, 16px);
}

.link-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  cursor: pointer;
  transition: opacity 0.2s;
  border-radius: 8px;
  padding-left: 8px;
  margin-left: -8px;
}

.link-cell:hover {
  opacity: 0.7;
}

.link-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.link-label {
  font-size: 12px;
  color: var(--color-text-muted, #aaa);
  margin-bottom: 2px;
}

.link-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary, #1677ff);
}

/* ========== 描述卡片 ========== */
.desc-card {
  padding: 28px;
  margin-bottom: 20px;
  border-radius: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text, #1f1f1f);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  font-size: 20px;
  line-height: 1;
}

.desc-wrapper {
  margin-bottom: 4px;
}

.desc-text {
  color: var(--color-text-secondary, #555);
  line-height: 1.8;
  font-size: 15px;
  margin: 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  -webkit-line-clamp: 3;
  transition: all 0.35s ease;
}

.desc-text.expanded {
  -webkit-line-clamp: unset;
}

.expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--color-primary, #1677ff);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  padding: 8px 0 0;
  transition: opacity 0.2s;
}

.expand-btn:hover {
  opacity: 0.8;
}

.expand-arrow {
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.expand-arrow.rotated {
  transform: rotate(180deg);
}

.tags-section {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--color-border-light, #f0f0f0);
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-item {
  background: var(--color-primary-bg, #e6f4ff);
  color: var(--color-primary, #1677ff);
  font-size: 13px;
  padding: 4px 14px;
  border-radius: 9999px;
  font-weight: 500;
}

/* ========== 组队帖区域 ========== */
.posts-section {
  margin-bottom: 20px;
}

.post-count {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-tertiary, #888);
  margin-left: 4px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.post-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  text-decoration: none;
  color: inherit;
  border-radius: 14px;
  transition: all 0.25s ease;
}

.post-card:hover {
  transform: translateX(4px);
  border-color: rgba(22, 119, 255, 0.15);
}

.post-info {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
  line-height: 1.4;
  color: var(--color-text, #1f1f1f);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-meta {
  font-size: 13px;
  color: var(--color-text-tertiary, #888);
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.meta-item.recruiting {
  color: var(--color-primary, #1677ff);
  font-weight: 500;
}

.meta-item.closed {
  color: var(--color-text-tertiary, #888);
}

.post-arrow {
  flex-shrink: 0;
  transition: transform 0.2s;
}

.post-card:hover .post-arrow {
  transform: translateX(2px);
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  background: #fff;
  border-radius: 16px;
  border: 1px dashed rgba(0,0,0,0.08);
}

.empty-icon {
  font-size: 36px;
  margin-bottom: 10px;
  opacity: 0.35;
}

.empty-text {
  color: var(--color-text-tertiary, #888);
  font-size: 14px;
  margin-bottom: 16px;
}

/* ========== 底部占位 ========== */
.bottom-spacer {
  height: 100px;
}

/* ========== 底部固定操作栏 ========== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-top: 1px solid rgba(0,0,0,0.06);
  padding: 12px 0;
  padding-bottom: max(12px, env(safe-area-inset-bottom));
  box-shadow: 0 -4px 24px rgba(0,0,0,0.06);
  z-index: 50;
}

.bottom-bar-inner {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 clamp(16px, 3vw, 48px);
  display: flex;
  align-items: center;
  gap: 12px;
}

.fav-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--color-bg-hover, #fafafa);
  border: 1.5px solid var(--color-border, #e5e4e7);
  border-radius: 12px;
  padding: 12px 20px;
  cursor: pointer;
  color: var(--color-text-tertiary, #555);
  font-size: 14px;
  font-weight: 600;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.fav-btn:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
}

.fav-btn.favorited {
  border-color: #ff4d4f;
  color: #ff4d4f;
  background: #fff1f0;
}

.action-btn {
  flex: 1;
  padding: 14px 0;
  font-size: 16px;
  font-weight: 700;
  border-radius: 12px;
  letter-spacing: 0.5px;
  border: none;
  cursor: pointer;
  transition: all 0.25s ease;
}

.action-btn.disabled {
  opacity: 0.45;
  cursor: not-allowed;
  background: #d9d9d9;
  box-shadow: none;
}

.action-btn.disabled:hover {
  transform: none;
  box-shadow: none;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .banner {
    padding: 24px 16px 40px;
  }

  .banner-title {
    font-size: 22px;
  }

  .body-wrapper {
    padding: 0 16px;
  }

  .info-card {
    padding: 20px;
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 4px;
  }

  .desc-card {
    padding: 20px;
  }

  .section-title {
    font-size: 16px;
  }

  .desc-text {
    font-size: 14px;
  }

  .bottom-bar-inner {
    padding: 0 16px;
    gap: 10px;
  }

  .fav-btn {
    padding: 10px 14px;
    font-size: 13px;
  }

  .action-btn {
    font-size: 14px;
    padding: 12px 0;
  }

  .post-card {
    padding: 14px 16px;
  }

  .post-title {
    font-size: 14px;
  }

  .post-meta {
    font-size: 12px;
    gap: 10px;
  }

  .bottom-spacer {
    height: 90px;
  }
}

@media (max-width: 400px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>