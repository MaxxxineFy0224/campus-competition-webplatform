<template>
  <div class="page-container detail-page">
    <!-- 顶部渐变区 -->
    <div class="gradient-header">
      <div class="gradient-bg" />
      <div class="header-content">
        <button class="back-btn" @click="router.back()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="m15 18-6-6 6-6" />
          </svg>
          返回
        </button>
        <h1 class="header-title">组队详情</h1>
      </div>
    </div>

    <div class="detail-container">
      <!-- 过期提示 -->
      <div v-if="expired" class="expired-banner">
        <span class="expired-icon">⏰</span>
        <span>该组队帖已过期</span>
      </div>

      <!-- 帖子内容 -->
      <div v-if="post" class="detail-card">
        <!-- 收藏按钮 -->
        <button class="fav-btn" @click="handleToggleFav" :title="isFav ? '取消收藏' : '收藏'">
          <HeartIcon :filled="isFav" />
        </button>

        <!-- 标题区域 -->
        <div class="post-header">
          <div class="post-tags">
            <span class="tag tag-role">
              {{ post.role === '队员' ? '🙋 队员' : '👑 队长' }}
            </span>
            <span :class="['tag', expired ? 'tag-expired' : 'tag-recruiting']">
              {{ expired ? '已过期' : (post.status || '招募中') }}
            </span>
          </div>
          <h1 class="post-comp-title">{{ post.competitionTitle }}</h1>
          <h2 class="post-title">{{ post.title }}</h2>
        </div>

        <!-- 作者信息 -->
        <div class="section">
          <div class="author-row">
            <div class="avatar">{{ post.author.charAt(0) }}</div>
            <div class="author-info">
              <div class="author-name">{{ post.author }}</div>
              <div class="post-time">{{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
        </div>

        <!-- 技能标签 -->
        <div class="section">
          <h3 class="section-title">🎯 需要技能</h3>
          <div class="skills-row">
            <span v-for="s in post.skills" :key="s" class="tag tag-skill">{{ s }}</span>
          </div>
        </div>

        <!-- 组队截止时间 -->
        <div class="section">
          <h3 class="section-title">⏰ 组队截止时间</h3>
          <p class="section-value">
            {{ post.teamDeadline || comp?.deadline || '未设置' }}
            <span v-if="expired" class="expired-label">（已过期）</span>
          </p>
        </div>

        <!-- 队伍介绍 -->
        <div class="section">
          <h3 class="section-title">📄 队伍介绍</h3>
          <p class="desc-text">{{ post.description }}</p>
        </div>

        <!-- 联系方式 -->
        <div class="section">
          <h3 class="section-title">📬 联系方式</h3>
          <div class="contact-box">
            <div class="contact-row">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="contact-icon">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <span class="contact-text">{{ copied ? post.contact : maskContact(post.contact) }}</span>
            </div>
            <button
              class="btn btn-primary copy-btn"
              :disabled="expired"
              @click="handleCopyContact"
            >
              {{ copied ? '✓ 已复制' : '📋 复制联系方式' }}
            </button>
          </div>
        </div>

        <!-- 安全提示 -->
        <p class="safety-tip">🔒 同校同学请文明沟通，注意保护个人信息</p>
      </div>

      <!-- 404 状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <h2 class="empty-title">该组队帖不存在</h2>
        <p class="empty-desc">帖子可能已被删除或链接地址有误</p>
        <RouterLink to="/team" class="btn btn-primary btn-lg">返回组队广场</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTeamPostById, getCompetitionById, getFavorites, toggleFavorite } from '../utils/storage'
import { useToast } from '../composables/useToast'
import { useAuth } from '../composables/useAuth'
import HeartIcon from '../components/HeartIcon.vue'

const route = useRoute()
const router = useRouter()
const { toast } = useToast()

const post = ref(null)
const comp = ref(null)
const favorites = ref([])
const copied = ref(false)
const expired = ref(false)

function isExpiredByDeadline(p, c) {
  const deadlineStr = p.teamDeadline || c?.deadline
  if (!deadlineStr) return false
  const now = new Date(); now.setHours(0, 0, 0, 0)
  const d = new Date(deadlineStr); d.setHours(23, 59, 59, 999)
  return d < now
}

function maskContact(contact) {
  if (!contact) return '***'
  const idx = Math.max(contact.indexOf('：'), contact.indexOf(':'))
  if (idx > -1) return contact.slice(0, idx + 1) + ' ***'
  const atIdx = contact.indexOf('@')
  if (atIdx > 2) return contact.slice(0, 1) + '***' + contact.slice(atIdx)
  if (contact.length <= 2) return '***'
  return contact.slice(0, 1) + '***' + contact.slice(-1)
}

function formatTime(dateStr) {
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  const p = getTeamPostById(route.params.id)
  post.value = p
  if (p) {
    const c = getCompetitionById(p.competitionId)
    comp.value = c
    expired.value = isExpiredByDeadline(p, c)
  }
  favorites.value = getFavorites()
})

const isFav = computed(() =>
  favorites.value.some((f) => f.id === route.params.id && f.type === 'team')
)

function handleToggleFav() {
  const { isLoggedIn, showLogin } = useAuth()
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  const updated = toggleFavorite(route.params.id, 'team')
  favorites.value = updated
  const nowFav = updated.some((f) => f.id === route.params.id && f.type === 'team')
  toast.success(nowFav ? '已收藏' : '已取消收藏')
}

async function handleCopyContact() {
  if (expired.value) return
  try {
    await navigator.clipboard.writeText(post.value.contact)
    copied.value = true
    toast.success('联系方式已复制')
    setTimeout(() => { copied.value = false }, 2000)
  } catch { toast.error('复制失败，请手动记录') }
}
</script>

<style scoped>
.detail-page {
  padding-bottom: 60px;
}

/* ===== 顶部渐变区 ===== */
.gradient-header {
  position: relative;
  margin: 0 -24px;
  padding: 0 24px;
  overflow: hidden;
}

.gradient-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1677ff 0%, #722ed1 50%, #1677ff 100%);
  opacity: 0.08;
  border-radius: 0 0 32px 32px;
}

.header-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0 32px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text, #1f1f1f);
  letter-spacing: 0.5px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--color-text-secondary, #555);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: var(--radius-md, 10px);
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: rgba(22, 119, 255, 0.08);
  color: var(--color-primary, #1677ff);
}

/* ===== 容器 ===== */
.detail-container {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 clamp(16px, 3vw, 48px);
}

/* ===== 过期提示横幅 ===== */
.expired-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--color-gold-bg, #fffbe6);
  border: 1px solid #ffe58f;
  border-radius: var(--radius-md, 10px);
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #ad6800;
  animation: slideDown 0.3s ease;
}

.expired-icon {
  font-size: 18px;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 主卡片 ===== */
.detail-card {
  background: #fff;
  border-radius: var(--radius-xl, 18px);
  box-shadow: var(--shadow-md, 0 4px 12px rgba(0,0,0,0.08));
  padding: 32px;
  position: relative;
  border: 1px solid rgba(0,0,0,0.04);
  animation: fadeInUp 0.4s ease;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== 收藏按钮 ===== */
.fav-btn {
  position: absolute;
  top: 24px;
  right: 24px;
  background: var(--color-bg-hover, #fafafa);
  border: 1px solid var(--color-border-light, #f0f0f0);
  border-radius: var(--radius-md, 10px);
  padding: 10px;
  cursor: pointer;
  line-height: 1;
  z-index: 2;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fav-btn:hover {
  background: #fff1f0;
  border-color: #ffccc7;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.15);
}

.fav-btn:active {
  transform: scale(0.95);
}

/* ===== 帖子标题区域 ===== */
.post-header {
  margin-bottom: 24px;
  padding-right: 48px;
}

.post-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 14px;
  border-radius: var(--radius-full, 9999px);
  font-size: 12px;
  font-weight: 500;
  line-height: 1.6;
}

.tag-role {
  background: #fff7e6;
  color: #d48806;
}

.tag-recruiting {
  background: #f6ffed;
  color: #52c41a;
}

.tag-expired {
  background: #f5f5f5;
  color: #999;
}

.tag-skill {
  background: var(--color-ai-bg, #f9f0ff);
  color: var(--color-ai, #722ed1);
  font-size: 13px;
  font-weight: 500;
  padding: 5px 14px;
}

.post-comp-title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--color-text, #1f1f1f);
  margin-bottom: 8px;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  line-height: 1.5;
  color: var(--color-text-secondary, #555);
  margin: 0;
}

/* ===== 区块分割 ===== */
.section {
  padding: 18px 0;
  border-bottom: 1px solid var(--color-border-light, #f0f0f0);
}

.section:last-of-type {
  border-bottom: none;
  padding-bottom: 0;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--color-text, #1f1f1f);
}

.section-value {
  font-size: 15px;
  color: var(--color-text, #1f1f1f);
  font-weight: 500;
  margin: 0;
}

.expired-label {
  color: #ff4d4f;
  font-size: 13px;
  margin-left: 10px;
}

/* ===== 作者信息 ===== */
.author-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text, #1f1f1f);
}

.post-time {
  font-size: 13px;
  color: var(--color-text-muted, #aaa);
}

/* ===== 技能标签 ===== */
.skills-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* ===== 队伍介绍 ===== */
.desc-text {
  font-size: 15px;
  color: var(--color-text, #1f1f1f);
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}

/* ===== 联系方式 ===== */
.contact-box {
  padding: 20px;
  background: linear-gradient(135deg, #f0f5ff, #e6f4ff);
  border-radius: var(--radius-lg, 14px);
  border: 1px solid rgba(22, 119, 255, 0.08);
}

.contact-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.contact-icon {
  color: var(--color-primary, #1677ff);
  flex-shrink: 0;
}

.contact-text {
  font-size: 15px;
  color: var(--color-text, #1f1f1f);
  font-weight: 500;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
  letter-spacing: 0.5px;
}

.copy-btn {
  width: 100%;
  padding: 12px 28px;
  font-size: 14px;
  font-weight: 600;
  border-radius: var(--radius-md, 10px);
}

.copy-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

/* ===== 安全提示 ===== */
.safety-tip {
  font-size: 12px;
  color: var(--color-text-tertiary, #888);
  text-align: center;
  margin: 20px 0 0;
  padding: 12px;
  background: #fafafa;
  border-radius: var(--radius-md, 10px);
  border: 1px dashed var(--color-border-light, #f0f0f0);
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 80px 24px 48px;
  animation: fadeInUp 0.4s ease;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text, #1f1f1f);
  margin-bottom: 8px;
}

.empty-desc {
  color: var(--color-text-tertiary, #888);
  font-size: 14px;
  margin-bottom: 28px;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .gradient-header {
    margin: 0 -16px;
    padding: 0 16px;
  }

  .gradient-bg {
    border-radius: 0 0 24px 24px;
  }

  .header-content {
    padding: 16px 0 24px;
  }

  .header-title {
    font-size: 16px;
  }

  .detail-card {
    padding: 24px 20px;
    border-radius: var(--radius-lg, 14px);
  }

  .post-comp-title {
    font-size: 19px;
  }

  .post-title {
    font-size: 15px;
  }

  .post-header {
    padding-right: 44px;
  }

  .fav-btn {
    top: 20px;
    right: 20px;
    padding: 8px;
  }

  .contact-box {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .detail-card {
    padding: 20px 16px;
  }

  .post-comp-title {
    font-size: 17px;
  }

  .post-title {
    font-size: 14px;
  }

  .section {
    padding: 14px 0;
  }

  .avatar {
    width: 38px;
    height: 38px;
    font-size: 16px;
  }

  .author-name {
    font-size: 14px;
  }

  .contact-text {
    font-size: 14px;
    word-break: break-all;
  }

  .empty-state {
    padding: 60px 16px 40px;
  }

  .empty-icon {
    font-size: 52px;
  }
}
</style>