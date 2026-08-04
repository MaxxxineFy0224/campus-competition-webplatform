<template>
  <div class="ai-match-page">
    <div class="container">
      <!-- 顶部标题 -->
      <div class="header">
        <div class="header-avatar">
          <div class="avatar-ring">
            <div class="avatar-inner">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2a3 3 0 0 0-3 3v1a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
                <path d="M5 15a7 7 0 0 1 14 0v1a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-1Z" />
                <path d="M8 18v2a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-2" />
              </svg>
            </div>
          </div>
          <div class="header-text">
            <h1 class="title">AI 竞赛助手</h1>
            <p class="subtitle">告诉我你的情况，AI 为你推荐最适合的竞赛和队伍</p>
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-wrapper" ref="chatWrapper">
        <div class="chat-messages" ref="chatMessages">
          <!-- 欢迎消息 -->
          <div class="message-row ai-row">
            <div class="msg-avatar">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2a3 3 0 0 0-3 3v1a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
                <path d="M5 15a7 7 0 0 1 14 0v1a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-1Z" />
              </svg>
            </div>
            <div class="msg-content">
              <div class="msg-bubble ai-bubble">
                <p>你好！我是你的 AI 竞赛助手 🎯</p>
                <p>我可以帮你：</p>
                <ul>
                  <li>根据你的专业和兴趣推荐竞赛</li>
                  <li>分析竞赛的难度和含金量</li>
                  <li>帮你找到合适的队友</li>
                  <li>给出备赛建议和时间规划</li>
                </ul>
                <p>告诉我你的情况，比如：</p>
              </div>
            </div>
          </div>

          <!-- 快捷问题 -->
          <div class="quick-questions">
            <button
              v-for="q in quickQuestions"
              :key="q.text"
              class="quick-btn"
              :disabled="isLoading"
              @click="handleQuickQuestion(q.text)"
            >
              {{ q.icon }} {{ q.text }}
            </button>
          </div>

          <!-- 对话消息 -->
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="message-row"
            :class="msg.role === 'user' ? 'user-row' : 'ai-row'"
          >
            <div v-if="msg.role === 'assistant'" class="msg-avatar">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2a3 3 0 0 0-3 3v1a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
                <path d="M5 15a7 7 0 0 1 14 0v1a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-1Z" />
              </svg>
            </div>
            <div class="msg-content">
              <div
                class="msg-bubble"
                :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'"
              >
                <div
                  v-if="msg.role === 'assistant'"
                  class="ai-text"
                  v-html="renderMarkdown(msg.content)"
                ></div>
                <p v-else class="user-text">{{ msg.content }}</p>
              </div>
            </div>
          </div>

          <!-- 加载指示器 -->
          <div v-if="isLoading" class="message-row ai-row">
            <div class="msg-avatar">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2a3 3 0 0 0-3 3v1a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z" />
                <path d="M5 15a7 7 0 0 1 14 0v1a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-1Z" />
              </svg>
            </div>
            <div class="msg-content">
              <div class="msg-bubble ai-bubble">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-wrapper">
          <textarea
            ref="inputRef"
            v-model="inputText"
            class="input-field"
            placeholder="描述你的情况，如：我大三计算机专业，擅长Python和前端开发..."
            rows="1"
            @keydown.enter.exact="handleSend"
            @input="autoResize"
          ></textarea>
          <button
            class="send-btn"
            :class="{ active: inputText.trim() }"
            :disabled="!inputText.trim() || isLoading"
            @click="handleSend"
          >
            <svg v-if="!isLoading" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
            <svg v-else class="spinner" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
            </svg>
          </button>
        </div>
        <p class="input-hint">按 Enter 发送 · Shift+Enter 换行</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useChat } from '../composables/useChat'

const { messages, isLoading, sendMessage, clearMessages } = useChat()

const inputText = ref('')
const inputRef = ref(null)
const chatMessages = ref(null)
const chatWrapper = ref(null)

const quickQuestions = [
  { icon: '💻', text: '我计算机专业，推荐什么竞赛？' },
  { icon: '🔢', text: '数学建模竞赛难吗？' },
  { icon: '👥', text: '怎么找到靠谱的队友？' },
  { icon: '📅', text: '大二适合参加什么竞赛？' },
]

/** 自动滚动到底部 */
async function scrollToBottom() {
  await nextTick()
  if (chatWrapper.value) {
    chatWrapper.value.scrollTop = chatWrapper.value.scrollHeight
  }
}

/** 发送消息 */
async function handleSend(e) {
  if (e) e.preventDefault()
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  inputText.value = ''
  resetInputHeight()
  await sendMessage(text)
  scrollToBottom()
}

/** 快捷问题 */
async function handleQuickQuestion(text) {
  inputText.value = text
  resetInputHeight()
  await sendMessage(text)
  scrollToBottom()
}

/** 自动调整输入框高度 */
function autoResize() {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function resetInputHeight() {
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
}

/** 监听新消息，自动滚动 */
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

/** 监听加载状态变化 */
watch(isLoading, (loading) => {
  if (!loading) scrollToBottom()
})

/** 渲染 Markdown（简单处理器） */
function renderMarkdown(text) {
  if (!text) return ''
  let html = text
    // 转义 HTML
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // **粗体**
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // 换行转 <br/>
    .replace(/\n/g, '<br/>')
    // 数字列表
    .replace(/(\d+)\.\s+/g, '<span class="list-num">$1.</span> ')
    // 无序列表项
    .replace(/•\s+/g, '<span class="list-dot">•</span> ')
  return html
}
</script>

<style scoped>
/* ===== 页面布局 ===== */
.ai-match-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(160deg, #f8f6ff 0%, #f0ebff 50%, #f5f0ff 100%);
}

.container {
  width: 100%;
  max-width: 100%;
  padding: 0 clamp(16px, 3vw, 48px);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
}

/* ===== 顶部标题 ===== */
.header {
  padding: 24px 0 16px;
  flex-shrink: 0;
}

.header-avatar {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-ring {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.3);
}

.avatar-inner {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7c3aed;
}

.header-text {
  min-width: 0;
}

.title {
  font-size: 22px;
  font-weight: 800;
  color: #1a1a2e;
  margin: 0 0 4px;
  letter-spacing: -0.3px;
}

.subtitle {
  font-size: 14px;
  color: #8b8fa3;
  margin: 0;
  line-height: 1.4;
}

/* ===== 聊天区域 ===== */
.chat-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 0 16px;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
}

.chat-wrapper::-webkit-scrollbar {
  width: 4px;
}

.chat-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.chat-wrapper::-webkit-scrollbar-thumb {
  background: #d0c8e0;
  border-radius: 4px;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

/* ===== 消息行 ===== */
.message-row {
  display: flex;
  gap: 10px;
  padding: 6px 0;
  animation: msgFadeIn 0.3s ease-out;
}

@keyframes msgFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.user-row {
  justify-content: flex-end;
}

.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ede9fe, #ddd6fe);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #7c3aed;
  margin-top: 4px;
}

.msg-content {
  max-width: min(85%, 600px);
  min-width: 0;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
}

.user-bubble {
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-bubble {
  background: #fff;
  color: #333;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-bottom-left-radius: 4px;
}

.user-text {
  margin: 0;
  white-space: pre-wrap;
}

/* ===== AI 消息富文本 ===== */
.ai-text {
  margin: 0;
}

.ai-text :deep(p) {
  margin: 0 0 8px;
}

.ai-text :deep(p:last-child) {
  margin-bottom: 0;
}

.ai-text :deep(strong) {
  color: #7c3aed;
  font-weight: 700;
}

.ai-text :deep(br) {
  display: block;
  content: '';
  margin: 4px 0;
}

.ai-text :deep(.list-num) {
  font-weight: 700;
  color: #7c3aed;
  margin-right: 4px;
}

.ai-text :deep(.list-dot) {
  color: #7c3aed;
  margin-right: 4px;
}

.ai-text :deep(ul) {
  margin: 8px 0;
  padding-left: 20px;
  list-style: none;
}

.ai-text :deep(li) {
  position: relative;
  padding-left: 16px;
  margin-bottom: 4px;
}

.ai-text :deep(li::before) {
  content: '•';
  position: absolute;
  left: 0;
  color: #7c3aed;
  font-weight: 700;
}

/* ===== 快捷问题 ===== */
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 0 8px 44px;
}

.quick-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  border-radius: 20px;
  border: 1px solid rgba(124, 58, 237, 0.15);
  background: rgba(255, 255, 255, 0.8);
  color: #555;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  white-space: nowrap;
}

.quick-btn:hover:not(:disabled) {
  background: #fff;
  border-color: #7c3aed;
  color: #7c3aed;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.1);
}

.quick-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== 打字指示器 ===== */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 4px 0;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d0c8e0;
  animation: typingBounce 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); background: #d0c8e0; }
  30% { transform: translateY(-8px); background: #7c3aed; }
}

/* ===== 输入区域 ===== */
.input-area {
  flex-shrink: 0;
  padding: 12px 0 20px;
  background: linear-gradient(0deg, #f8f6ff 0%, transparent 100%);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: #fff;
  border-radius: 16px;
  padding: 6px 6px 6px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(124, 58, 237, 0.08);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: rgba(124, 58, 237, 0.3);
  box-shadow: 0 2px 16px rgba(124, 58, 237, 0.1);
}

.input-field {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  font-family: inherit;
  color: #333;
  resize: none;
  padding: 8px 0;
  line-height: 1.5;
  max-height: 160px;
  background: transparent;
}

.input-field::placeholder {
  color: #bbb;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #e5e0f0;
  color: #b0a8c0;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.send-btn.active {
  background: linear-gradient(135deg, #7c3aed, #6d28d9);
  color: #fff;
  box-shadow: 0 2px 8px rgba(124, 58, 237, 0.3);
}

.send-btn.active:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(124, 58, 237, 0.4);
}

.send-btn:disabled {
  cursor: not-allowed;
}

.spinner {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.input-hint {
  font-size: 11px;
  color: #ccc;
  text-align: center;
  margin: 6px 0 0;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
  .container {
    padding: 0 16px;
  }

  .header {
    padding: 16px 0 12px;
  }

  .avatar-ring {
    width: 48px;
    height: 48px;
  }

  .avatar-inner {
    width: 38px;
    height: 38px;
  }

  .avatar-inner svg {
    width: 22px;
    height: 22px;
  }

  .title {
    font-size: 20px;
  }

  .subtitle {
    font-size: 13px;
  }

  .msg-content {
    max-width: 90%;
  }

  .msg-bubble {
    padding: 10px 14px;
    font-size: 13px;
  }

  .quick-questions {
    padding-left: 44px;
    gap: 6px;
  }

  .quick-btn {
    font-size: 12px;
    padding: 6px 12px;
  }

  .input-area {
    padding: 8px 0 16px;
  }

  .input-wrapper {
    padding: 4px 4px 4px 12px;
  }

  .input-field {
    font-size: 13px;
    padding: 6px 0;
  }

  .send-btn {
    width: 36px;
    height: 36px;
  }

  .send-btn svg {
    width: 18px;
    height: 18px;
  }
}
</style>