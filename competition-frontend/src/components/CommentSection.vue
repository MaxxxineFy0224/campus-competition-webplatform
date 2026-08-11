<template>
  <div class="comment-section">
    <!-- 标题 -->
    <div class="section-header">
      <h3 class="section-title">评论</h3>
      <span class="comment-count">{{ totalCount }} 条评论</span>
    </div>

    <!-- 发表评论 -->
    <div class="comment-input-area">
      <div class="avatar">{{ userAvatar }}</div>
      <div class="input-box">
        <textarea
          v-model="newComment"
          class="comment-textarea"
          :placeholder="replyTo ? `回复 @${replyTo.userName}：` : '写下你的想法...'"
          rows="2"
          maxlength="500"
          @keydown.enter.ctrl="handleSubmit"
        />
        <div class="input-footer">
          <span v-if="replyTo" class="reply-hint">
            回复 @{{ replyTo.userName }}
            <button class="cancel-reply" @click="cancelReply">取消</button>
          </span>
          <span v-else />
          <button
            class="submit-btn"
            :disabled="!newComment.trim() || submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <span class="loading-spinner" />
      <span>加载评论...</span>
    </div>

    <!-- 评论列表 -->
    <div v-else-if="comments.length === 0" class="empty-comments">
      <p>暂无评论，来发表第一条吧</p>
    </div>

    <div v-else class="comment-list">
      <div
        v-for="item in comments"
        :key="item.id"
        class="comment-item"
        :class="{ 'is-reply': item.parentId }"
      >
        <div class="avatar">{{ (item.userName || '?')[0] }}</div>
        <div class="comment-body">
          <div class="comment-top">
            <span class="comment-name">{{ item.userName || '匿名用户' }}</span>
            <span class="comment-time">{{ formatTime(item.createdAt) }}</span>
          </div>

          <!-- 回复目标 -->
          <div v-if="item.parentId && item.parentUserName" class="reply-target">
            回复 <span class="target-name">@{{ item.parentUserName }}</span>
          </div>

          <p class="comment-content">{{ item.content }}</p>

          <div class="comment-actions">
            <button
              :class="['action-btn', { liked: item.liked }]"
              @click="handleLike(item)"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" :fill="item.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z" />
                <path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
              </svg>
              {{ item.likeCount || 0 }}
            </button>
            <button class="action-btn" @click="handleReply(item)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m3 17 1 1a16 16 0 0 0 14-4" />
                <path d="M8 21v-3a12 12 0 0 1 9-5" />
                <path d="m21 8-5-5 5-5" />
              </svg>
              回复
            </button>
            <button
              v-if="item.userId === currentUserId"
              class="action-btn delete-btn"
              @click="handleDelete(item)"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
      <span>加载更多评论</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../utils/api'
import { useAuth } from '../composables/useAuth'
import { useToast } from '../composables/useToast'

const props = defineProps({
  teamPostId: { type: [String, Number], required: true },
})

const { user, isLoggedIn, showLogin } = useAuth()
const { toast } = useToast()

const comments = ref([])
const newComment = ref('')
const replyTo = ref(null)
const loading = ref(false)
const submitting = ref(false)
const page = ref(1)
const hasMore = ref(true)
const totalCount = ref(0)

const currentUserId = computed(() => user.value?.id)
const userAvatar = computed(() => (user.value?.name || '?')[0])

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

async function fetchComments(pageNum = 1) {
  loading.value = true
  try {
    const data = await api.getComments(Number(props.teamPostId), pageNum)
    const list = Array.isArray(data) ? data : (data?.records || [])
    if (pageNum === 1) {
      comments.value = list
    } else {
      comments.value.push(...list)
    }
    totalCount.value = data?.total || comments.value.length
    hasMore.value = list.length >= 20
  } catch (e) {
    console.warn('[API] 评论加载失败:', e.message)
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  const content = newComment.value.trim()
  if (!content) return

  submitting.value = true
  try {
    const parentId = replyTo.value?.id || null
    const created = await api.createComment(Number(props.teamPostId), content, parentId)
    // 乐观更新
    comments.value.unshift({
      ...created,
      id: created?.id || Date.now(),
      userName: user.value?.name || '我',
      userId: currentUserId.value,
      parentId,
      parentUserName: replyTo.value?.userName || null,
      liked: false,
      likeCount: 0,
      createdAt: new Date().toISOString(),
    })
    totalCount.value++
    newComment.value = ''
    replyTo.value = null
    toast.success('评论已发送')
  } catch (e) {
    toast.error(e.message || '发送失败')
  } finally {
    submitting.value = false
  }
}

function handleReply(item) {
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  replyTo.value = { id: item.id, userName: item.userName }
  // 聚焦输入框
  const textarea = document.querySelector('.comment-textarea')
  if (textarea) textarea.focus()
}

function cancelReply() {
  replyTo.value = null
}

async function handleLike(item) {
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  try {
    await api.likeComment(item.id)
    item.liked = !item.liked
    item.likeCount = (item.likeCount || 0) + (item.liked ? 1 : -1)
  } catch {
    toast.error('操作失败')
  }
}

async function handleDelete(item) {
  if (!confirm('确定删除这条评论？')) return
  try {
    await api.deleteComment(item.id)
    const idx = comments.value.findIndex((c) => c.id === item.id)
    if (idx > -1) {
      comments.value.splice(idx, 1)
      totalCount.value = Math.max(0, totalCount.value - 1)
    }
    toast.success('已删除')
  } catch {
    toast.error('删除失败')
  }
}

function loadMore() {
  page.value++
  fetchComments(page.value)
}

onMounted(() => fetchComments())
</script>

<style scoped>
.comment-section {
  padding: 0 0 24px;
}

/* ===== 标题 ===== */
.section-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 17px;
  font-weight: 700;
  color: #1f1f1f;
  margin: 0;
}

.comment-count {
  font-size: 13px;
  color: #999;
}

/* ===== 发表评论 ===== */
.comment-input-area {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}

.input-box {
  flex: 1;
  min-width: 0;
}

.comment-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  background: #fafafa;
  resize: none;
  font-family: inherit;
  line-height: 1.5;
  box-sizing: border-box;
  transition: all 0.2s;
}
.comment-textarea:focus {
  outline: none;
  border-color: #1677ff;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.08);
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.reply-hint {
  font-size: 12px;
  color: #1677ff;
}

.cancel-reply {
  background: none;
  border: none;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  margin-left: 8px;
}
.cancel-reply:hover {
  color: #ff4d4f;
}

.submit-btn {
  padding: 6px 18px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #1677ff, #722ed1);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
}
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== 加载状态 ===== */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 0;
  color: #aaa;
  font-size: 13px;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #e0e0e0;
  border-top-color: #1677ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-comments {
  text-align: center;
  padding: 32px 0;
  color: #bbb;
  font-size: 14px;
}

/* ===== 评论列表 ===== */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-item.is-reply {
  margin-left: 44px;
  padding-left: 12px;
  border-left: 2px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-top {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #bbb;
}

.reply-target {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.target-name {
  color: #1677ff;
}

.comment-content {
  font-size: 14px;
  color: #444;
  line-height: 1.6;
  margin: 0 0 8px;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  padding: 2px 0;
  transition: color 0.15s;
}
.action-btn:hover {
  color: #1677ff;
}
.action-btn.liked {
  color: #1677ff;
}

.delete-btn:hover {
  color: #ff4d4f;
}

/* ===== 加载更多 ===== */
.load-more {
  text-align: center;
  padding: 16px 0 0;
  font-size: 13px;
  color: #1677ff;
  cursor: pointer;
  font-weight: 500;
}
.load-more:hover {
  text-decoration: underline;
}
</style>