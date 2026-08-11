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
      <h1 class="page-title">申请管理</h1>
      <div class="header-spacer" />
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
        <span v-if="tab.key === 'pending' && pendingCount" class="tab-badge">{{ pendingCount }}</span>
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <span class="loading-spinner" />
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredList.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <h3 class="empty-title">{{ emptyText }}</h3>
    </div>

    <!-- 申请列表 -->
    <div v-else class="list-container">
      <div
        v-for="app in filteredList"
        :key="app.id"
        class="app-card"
        :class="{ 'card-approved': app.status === 1, 'card-rejected': app.status === 2 }"
      >
        <div class="card-top">
          <!-- 申请人信息 -->
          <div class="applicant-info">
            <div class="avatar">{{ (app.applicantName || '?')[0] }}</div>
            <div class="info-text">
              <div class="name">{{ app.applicantName || '匿名用户' }}</div>
              <div class="meta">
                <span v-if="app.applicantSchool">{{ app.applicantSchool }}</span>
                <span v-if="app.applicantMajor">{{ app.applicantMajor }}</span>
              </div>
            </div>
          </div>

          <!-- 状态标签 -->
          <span :class="['status-tag', statusClass(app.status)]">
            {{ statusText(app.status) }}
          </span>
        </div>

        <!-- 申请留言 -->
        <div v-if="app.message" class="app-message">
          <span class="message-label">申请留言</span>
          <p class="message-text">{{ app.message }}</p>
        </div>

        <!-- 拒绝理由 -->
        <div v-if="app.status === 2 && app.rejectReason" class="reject-reason">
          <span class="message-label">拒绝理由</span>
          <p class="reject-text">{{ app.rejectReason }}</p>
        </div>

        <!-- 申请时间 -->
        <div class="card-footer">
          <span class="time">{{ formatTime(app.createdAt) }}</span>

          <!-- 操作按钮（仅待审核状态显示） -->
          <div v-if="app.status === 0" class="actions">
            <button class="btn btn-reject" @click="openReject(app)" :disabled="actionLoading === app.id">
              拒绝
            </button>
            <button class="btn btn-approve" @click="handleApprove(app)" :disabled="actionLoading === app.id">
              <span v-if="actionLoading === app.id" class="btn-loading" />
              {{ actionLoading === app.id ? '处理中...' : '通过' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 拒绝理由弹窗 -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showReject" class="modal-overlay" @click.self="showReject = false">
          <div class="reject-modal">
            <h3>拒绝申请</h3>
            <textarea
              v-model="rejectReason"
              class="reject-input"
              placeholder="填写拒绝理由（选填）"
              maxlength="200"
              rows="3"
            />
            <div class="btn-group">
              <button class="btn btn-cancel" @click="showReject = false">取消</button>
              <button class="btn btn-confirm" @click="handleReject" :disabled="rejectLoading">
                {{ rejectLoading ? '处理中...' : '确认拒绝' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../utils/api'
import { useToast } from '../composables/useToast'

const route = useRoute()
const router = useRouter()
const { toast } = useToast()

const teamPostId = computed(() => route.params.teamPostId)

const loading = ref(false)
const applications = ref([])
const activeTab = ref('all')
const actionLoading = ref(null)

const showReject = ref(false)
const rejectReason = ref('')
const rejectTarget = ref(null)
const rejectLoading = ref(false)

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待审核' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '已拒绝' },
]

const pendingCount = computed(() =>
  applications.value.filter((a) => a.status === 0).length
)

const filteredList = computed(() => {
  if (activeTab.value === 'all') return applications.value
  const map = { pending: 0, approved: 1, rejected: 2 }
  return applications.value.filter((a) => a.status === map[activeTab.value])
})

const emptyText = computed(() => {
  const map = {
    all: '暂无申请记录',
    pending: '没有待审核的申请',
    approved: '暂无已通过的申请',
    rejected: '暂无已拒绝的申请',
  }
  return map[activeTab.value] || '暂无数据'
})

function statusClass(status) {
  return { 0: 'tag-pending', 1: 'tag-approved', 2: 'tag-rejected' }[status] || ''
}

function statusText(status) {
  return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[status] || '未知'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

async function fetchApplications() {
  loading.value = true
  try {
    const data = await api.getTeamApplications(Number(teamPostId.value))
    applications.value = Array.isArray(data) ? data : (data?.records || [])
  } catch (e) {
    toast.error('加载申请列表失败')
    console.warn('[API] 申请列表加载失败:', e.message)
  } finally {
    loading.value = false
  }
}

async function handleApprove(app) {
  actionLoading.value = app.id
  try {
    await api.approveApplication(app.id)
    app.status = 1
    toast.success('已通过申请')
  } catch (e) {
    toast.error(e.message || '操作失败')
  } finally {
    actionLoading.value = null
  }
}

function openReject(app) {
  rejectTarget.value = app
  rejectReason.value = ''
  showReject.value = true
}

async function handleReject() {
  if (!rejectTarget.value) return
  rejectLoading.value = true
  try {
    await api.rejectApplication(rejectTarget.value.id, rejectReason.value.trim())
    rejectTarget.value.status = 2
    rejectTarget.value.rejectReason = rejectReason.value.trim()
    showReject.value = false
    toast.success('已拒绝申请')
  } catch (e) {
    toast.error(e.message || '操作失败')
  } finally {
    rejectLoading.value = false
  }
}

onMounted(fetchApplications)
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

.header-spacer {
  width: 60px;
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

/* ===== Tab 切换 ===== */
.tab-bar {
  display: flex;
  gap: 4px;
  padding: 8px 0 16px;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 16px;
}

.tab-btn {
  flex: 1;
  padding: 8px 0;
  border: none;
  border-radius: 10px;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: #888;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  position: relative;
}

.tab-btn.active {
  background: #fff;
  color: #1677ff;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
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

/* ===== 申请卡片 ===== */
.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.app-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.card-approved {
  border-left: 3px solid #52c41a;
}

.card-rejected {
  border-left: 3px solid #ff4d4f;
  opacity: 0.75;
}

.card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}

.info-text {
  min-width: 0;
}

.name {
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
}

.meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.status-tag {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.tag-pending {
  background: #fff7e6;
  color: #d48806;
}

.tag-approved {
  background: #f6ffed;
  color: #52c41a;
}

.tag-rejected {
  background: #f5f5f5;
  color: #999;
}

/* ===== 留言区域 ===== */
.app-message {
  margin-top: 14px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.message-label {
  font-size: 12px;
  color: #aaa;
  font-weight: 500;
}

.message-text {
  margin: 6px 0 0;
  font-size: 14px;
  color: #444;
  line-height: 1.6;
  white-space: pre-wrap;
}

.reject-reason {
  margin-top: 14px;
  padding: 12px;
  background: #fff2f0;
  border-radius: 8px;
}

.reject-text {
  margin: 6px 0 0;
  font-size: 14px;
  color: #cf1322;
  line-height: 1.6;
}

/* ===== 卡片底部 ===== */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.time {
  font-size: 12px;
  color: #bbb;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 7px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-approve {
  background: linear-gradient(135deg, #1677ff, #722ed1);
  color: #fff;
}
.btn-approve:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
}

.btn-reject {
  background: #f0f0f0;
  color: #666;
}
.btn-reject:hover:not(:disabled) {
  background: #e0e0e0;
}

.btn-loading {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

/* ===== 拒绝弹窗 ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  padding: 16px;
}

.reject-modal {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border-radius: 16px;
  padding: 24px 20px 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.reject-modal h3 {
  font-size: 17px;
  font-weight: 700;
  color: #1f1f1f;
  margin: 0 0 16px;
  text-align: center;
}

.reject-input {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  background: #fafafa;
  resize: vertical;
  font-family: inherit;
  line-height: 1.6;
  box-sizing: border-box;
  transition: all 0.2s;
}
.reject-input:focus {
  outline: none;
  border-color: #ff4d4f;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.1);
}

.btn-group {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.btn-group .btn {
  flex: 1;
  padding: 10px 0;
  justify-content: center;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}
.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-confirm {
  background: #ff4d4f;
  color: #fff;
}
.btn-confirm:hover:not(:disabled) {
  background: #ff7875;
}

/* 过渡动画 */
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.2s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
</style>