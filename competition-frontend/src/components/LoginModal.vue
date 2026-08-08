<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="showLoginModal" class="modal-overlay" @click.self="handleOverlayClick">
        <div class="modal-card">
          <!-- 关闭按钮 -->
          <button class="modal-close" @click="hideLogin" aria-label="关闭">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>

          <!-- 头部 -->
          <div class="modal-header">
            <div class="modal-logo">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9z" />
                <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9z" />
                <path d="M4 22h16" />
                <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
                <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
                <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
              </svg>
            </div>
            <h2 class="modal-title">{{ isRegisterMode ? '创建账号' : '欢迎回来' }}</h2>
            <p class="modal-subtitle">{{ isRegisterMode ? '注册后即可发布组队、AI匹配、收藏' : '登录后继续你的竞赛之旅' }}</p>
          </div>

          <!-- 表单 -->
          <form class="modal-form" @submit.prevent="handleSubmit">
            <!-- 登录模式 -->
            <template v-if="!isRegisterMode">
              <div class="form-group">
                <label class="form-label">昵称</label>
                <input
                  v-model="loginForm.name"
                  class="form-input"
                  placeholder="输入你的昵称"
                  maxlength="50"
                  required
                  autocomplete="username"
                />
              </div>
              <div class="form-group">
                <label class="form-label">密码</label>
                <input
                  v-model="loginForm.password"
                  type="password"
                  class="form-input"
                  placeholder="输入密码"
                  required
                  autocomplete="current-password"
                />
              </div>
            </template>

            <!-- 注册模式 -->
            <template v-else>
              <div class="form-group">
                <label class="form-label">昵称</label>
                <input
                  v-model="registerForm.name"
                  class="form-input"
                  placeholder="给自己取个名字"
                  maxlength="50"
                  required
                  autocomplete="username"
                />
              </div>
              <div class="form-group">
                <label class="form-label">密码</label>
                <input
                  v-model="registerForm.password"
                  type="password"
                  class="form-input"
                  placeholder="至少6位密码"
                  minlength="6"
                  required
                  autocomplete="new-password"
                />
              </div>
              <div class="form-group">
                <label class="form-label">学校</label>
                <input
                  v-model="registerForm.school"
                  class="form-input"
                  placeholder="输入你的学校（选填）"
                  maxlength="50"
                  autocomplete="organization"
                />
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">专业</label>
                  <input
                    v-model="registerForm.major"
                    class="form-input"
                    placeholder="专业（选填）"
                    maxlength="50"
                  />
                </div>
                <div class="form-group">
                  <label class="form-label">年级</label>
                  <input
                    v-model="registerForm.grade"
                    class="form-input"
                    placeholder="年级（选填）"
                    maxlength="20"
                  />
                </div>
              </div>
            </template>

            <!-- 错误提示 -->
            <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>

            <!-- 提交按钮 -->
            <button type="submit" class="btn-submit" :disabled="loading">
              <span v-if="loading" class="btn-loading"></span>
              {{ loading ? '处理中...' : (isRegisterMode ? '注册' : '登录') }}
            </button>
          </form>

          <!-- 底部切换 -->
          <div class="modal-footer">
            <span>{{ isRegisterMode ? '已有账号？' : '没有账号？' }}</span>
            <button class="btn-switch" @click="toggleMode">
              {{ isRegisterMode ? '去登录' : '立即注册' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useAuth } from '../composables/useAuth'

const { showLoginModal, hideLogin, login, register, onLoginSuccess } = useAuth()

const isRegisterMode = ref(false)
const loading = ref(false)
const errorMsg = ref('')

const loginForm = reactive({ name: '', password: '' })
const registerForm = reactive({ name: '', password: '', school: '', major: '', grade: '' })

// 弹窗关闭时重置表单
watch(showLoginModal, (val) => {
  if (!val) {
    loginForm.name = ''
    loginForm.password = ''
    registerForm.name = ''
    registerForm.password = ''
    registerForm.school = ''
    registerForm.major = ''
    registerForm.grade = ''
    errorMsg.value = ''
  }
})

function toggleMode() {
  isRegisterMode.value = !isRegisterMode.value
  errorMsg.value = ''
}

function handleOverlayClick() {
  hideLogin()
}

async function handleSubmit() {
  errorMsg.value = ''
  loading.value = true
  try {
    if (isRegisterMode.value) {
      const { name, password, school, major, grade } = registerForm
      if (!name.trim()) { errorMsg.value = '请输入昵称'; loading.value = false; return }
      if (!password || password.length < 6) { errorMsg.value = '密码至少6位'; loading.value = false; return }
      await register({ name: name.trim(), password, school: school.trim(), major: major.trim(), grade: grade.trim() })
    } else {
      const { name, password } = loginForm
      if (!name.trim()) { errorMsg.value = '请输入昵称'; loading.value = false; return }
      if (!password) { errorMsg.value = '请输入密码'; loading.value = false; return }
      await login(name.trim(), password)
    }
    await onLoginSuccess()
  } catch (e) {
    errorMsg.value = e.message || (isRegisterMode.value ? '注册失败' : '登录失败')
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
  max-width: 420px;
  background: #fff;
  border-radius: 20px;
  padding: 32px 28px 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
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
.modal-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  margin-bottom: 12px;
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
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #444;
}
.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1.5px solid #e0e0e0;
  border-radius: 10px;
  font-size: 14px;
  color: #333;
  background: #fafafa;
  transition: all 0.2s;
  box-sizing: border-box;
}
.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
}
.form-input::placeholder {
  color: #bbb;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-error {
  color: #e74c3c;
  font-size: 13px;
  margin: 0;
  text-align: center;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
}
.btn-submit:disabled {
  opacity: 0.7;
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

.modal-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #888;
}
.btn-switch {
  background: none;
  border: none;
  color: #667eea;
  font-weight: 600;
  cursor: pointer;
  font-size: 14px;
  padding: 0 0 0 4px;
}
.btn-switch:hover {
  text-decoration: underline;
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