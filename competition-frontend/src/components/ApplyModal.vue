<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="handleCancel">
        <div class="modal-card">
          <!-- 关闭按钮 -->
          <button class="modal-close" @click="handleCancel" aria-label="关闭">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>

          <!-- 头部 -->
          <div class="modal-header">
            <div class="modal-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <line x1="19" y1="8" x2="19" y2="14"/>
                <line x1="22" y1="11" x2="16" y2="11"/>
              </svg>
            </div>
            <h2 class="modal-title">申请加入</h2>
            <p class="modal-subtitle">{{ postTitle }}</p>
          </div>

          <!-- 表单 -->
          <form class="modal-form" @submit.prevent="handleSubmit">
            <div class="form-group">
              <label class="form-label">申请留言</label>
              <textarea
                v-model="message"
                class="form-textarea"
                placeholder="介绍一下自己，让队长更快了解你（选填）"
                maxlength="500"
                rows="4"
              />
              <span class="form-count">{{ message.length }} / 500</span>
            </div>

            <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>

            <div class="btn-group">
              <button type="button" class="btn btn-cancel" @click="handleCancel">取消</button>
              <button type="submit" class="btn btn-confirm" :disabled="loading">
                <span v-if="loading" class="btn-loading" />
                {{ loading ? '提交中...' : '提交申请' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { api } from '../utils/api'
import { useToast } from '../composables/useToast'
import { useAuth } from '../composables/useAuth'

const props = defineProps({
  visible: { type: Boolean, default: false },
  teamPostId: { type: [String, Number], required: true },
  postTitle: { type: String, default: '' },
})

const emit = defineEmits(['close', 'applied'])

const { toast } = useToast()
const { showLogin } = useAuth()

const message = ref('')
const loading = ref(false)
const errorMsg = ref('')

watch(() => props.visible, (val) => {
  if (val) {
    message.value = ''
    errorMsg.value = ''
  }
})

function handleCancel() {
  emit('close')
}

async function handleSubmit() {
  errorMsg.value = ''
  loading.value = true
  try {
    await api.applyTeamPost(Number(props.teamPostId), message.value.trim())
    toast.success('申请已提交，等待队长审核')
    emit('applied')
    emit('close')
  } catch (e) {
    if (e.message?.includes('401')) {
      showLogin()
    } else {
      errorMsg.value = e.message || '提交失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
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

.modal-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 20px;
  padding: 32px 28px 28px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}
.modal-close:hover {
  background: #e0e0e0;
  color: #333;
}

.modal-header {
  text-align: center;
  margin-bottom: 24px;
}
.modal-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  color: #fff;
  margin-bottom: 14px;
}
.modal-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
}
.modal-subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
}
.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #444;
}
.form-textarea {
  width: 100%;
  padding: 12px 14px;
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
.form-textarea:focus {
  outline: none;
  border-color: #1677ff;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.12);
}
.form-textarea::placeholder {
  color: #bbb;
}

.form-count {
  font-size: 12px;
  color: #aaa;
  text-align: right;
}

.form-error {
  color: #e74c3c;
  font-size: 13px;
  margin: 8px 0 0;
  text-align: center;
}

.btn-group {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn {
  flex: 1;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}
.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-confirm {
  background: linear-gradient(135deg, #1677ff, #722ed1);
  color: #fff;
}
.btn-confirm:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(22, 119, 255, 0.35);
}
.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-loading {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 过渡动画 */
.modal-enter-active, .modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-active .modal-card,
.modal-leave-active .modal-card {
  transition: transform 0.25s ease;
}
.modal-enter-from, .modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal-card {
  transform: scale(0.95) translateY(10px);
}
.modal-leave-to .modal-card {
  transform: scale(0.95) translateY(10px);
}
</style>