<template>
  <div class="page-container">
    <!-- 顶部导航 -->
    <div class="page-header">
      <button class="back-btn" @click="router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6" />
        </svg>
        返回
      </button>
      <h1 class="page-title">消息中心</h1>
      <button
        class="mark-all-btn"
        :disabled="!hasUnread"
        @click="handleMarkAllRead"
      >
        全部已读
      </button>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-scroll">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <span v-if="tab.key === 'unread' && unreadCount" class="tab-badge">{{ unreadCount }}</span>
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <span class="loading-spinner" />
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredList.length === 0" class="empty-state">
      <div class="empty-icon">🔔</div>
      <h3 class="empty-title">{{ emptyText }}</h3>
    </div>

    <!-- 通知列表 -->
    <div v-else class="list-container">
      <div
        v-for="item in filteredList"
        :key="item.id"
        :class="['notif-card', { unread: item.isRead === 0 }]"
        @click="handleClick(item)"
      >
        <div class="card-left">
          <div :class="['type-icon', iconClass(item.type)]">
            {{ iconText(item.type) }}
          </div>
        </div>
        <div class="card-body">
          <div class="card-top-row">
            <span class="card-title">{{ item.title }}</span>
            <span class="card-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <p class="card-content">{{ item.content }}</p>
        </div>
        <div v-if="item.isRead === 0" class="unread-dot" />
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
      <span>加载更多</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../utils/api'
import { useToast } from '../composables/useToast'

const router = useRouter()
const { toast } = useToast()

const loading = ref(false)
const notifications = ref([])
const activeTab = ref('all')
const page = ref(1)
const hasMore = ref(true)

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' },
  { key: 'system', label: '系统通知' },
  { key: 'application', label: '组队申请' },
  { key: 'comment', label: '评论' },
]

const unreadCount = computed(() =>
  notifications.value.filter((n) => n.isRead === 0).length
)

const hasUnread = computed(() => unreadCount.value > 0)

const filteredList = computed(() => {
  if (activeTab.value === 'all') return notifications.value
  if (activeTab.value === 'unread') return notifications.value.filter((n) => n.isRead === 0)
  return notifications.value.filter((n) => n.type === activeTab.value)
})

const emptyText = computed(() => {
  const map = {
    all: '暂无消息',
    unread: '没有未读消息',
    system: '暂无系统通知',
    application: '暂无组队申请通知',
    comment: '暂无评论通知',
  }
  return map[activeTab.value] || '暂无数据'
})

function iconClass(type) {
  return {
    system: 'icon-system',
    application: 'icon-application',
    comment: 'icon-comment',
  }[type] || 'icon-system'
}

function iconText(type) {
  return {
    system: '📢',
    application: '👥',
    comment: '💬',
  }[type] || '📢'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60 * 1000) return '刚刚'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)} 小时前`
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

async function fetchNotifications(pageNum = 1) {
  loading.value = true
  try {
    const type = ['system', 'application', 'comment'].includes(activeTab.value)
      ? activeTab.value : ''
    const data = await api.getNotifications(type, pageNum)
    const list = Array.isArray(data) ? data : (data?.records || [])
    if (pageNum === 1) {
      notifications.value = list
    } else {
      notifications.value.push(...list)
    }
    hasMore.value = list.length >= 20
  } catch (e) {
    console.warn('[API] 通知加载失败:', e.message)
  } finally {
    loading.value = false
  }
}

async function handleClick(item) {
  if (item.isRead === 0) {
    try {
      await api.markNotificationRead(item.id)
      item.isRead = 1
    } catch { /* 静默 */ }
  }
  // 如果有关联业务 ID，跳转
  if (item.relatedId && item.relatedType === 'team_application') {
    router.push(`/applications/${item.relatedId}`)
  }
}

async function handleMarkAllRead() {
  try {
    await api.markAllNotificationsRead()
    notifications.value.forEach((n) => { n.isRead = 1 })
    toast.success('已全部标记为已读')
  } catch (e) {
    toast.error('操作失败')
  }
}

function loadMore() {
  page.value++
  fetchNotifications(page.value)
}

onMounted(() => fetchNotifications())
</script>

<style scoped>
/* ===== 页面头部 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0 8px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f1f1f;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #555;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 10px;
  transition: all 0.2s;
}
.back-btn:hover {
  background: rgba(22, 119, 255, 0.08);
  color: #1677ff;
}

.mark-all-btn {
  background: none;
  border: 1px solid #e0e0e0;
  color: #555;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.mark-all-btn:hover:not(:disabled) {
  border-color: #1677ff;
  color: #1677ff;
}
.mark-all-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* ===== Tab 滚动 ===== */
.tab-scroll {
  display: flex;
  gap: 4px;
  padding: 0 0 14px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.tab-scroll::-webkit-scrollbar {
  display: none;
}

.tab-btn {
  flex-shrink: 0;
  padding: 7px 16px;
  border: none;
  border-radius: 20px;
  background: #f5f5f5;
  font-size: 13px;
  font-weight: 500;
  color: #888;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 5px;
  position: relative;
}

.tab-btn.active {
  background: linear-gradient(135deg, #1677ff, #722ed1);
  color: #fff;
  font-weight: 600;
}

.tab-badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

/* ===== 加载/空状态 ===== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 24px;
  color: #aaa;
  gap: 12px;
  font-size: 14px;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e0e0e0;
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 24px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-title {
  font-size: 15px;
  color: #aaa;
  font-weight: 500;
}

/* ===== 通知列表 ===== */
.list-container {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.notif-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
  position: relative;
}

.notif-card:hover {
  background: #fafafa;
}

.notif-card.unread {
  background: #f0f5ff;
}

.card-left {
  flex-shrink: 0;
  padding-top: 2px;
}

.type-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.icon-system {
  background: #f0f5ff;
}

.icon-application {
  background: #f6ffed;
}

.icon-comment {
  background: #fff7e6;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-top-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f1f1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-time {
  font-size: 12px;
  color: #bbb;
  flex-shrink: 0;
}

.card-content {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.unread-dot {
  position: absolute;
  top: 18px;
  right: 16px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff4d4f;
}

/* ===== 加载更多 ===== */
.load-more {
  text-align: center;
  padding: 16px 0;
  font-size: 13px;
  color: #1677ff;
  cursor: pointer;
  font-weight: 500;
}
.load-more:hover {
  text-decoration: underline;
}
</style>