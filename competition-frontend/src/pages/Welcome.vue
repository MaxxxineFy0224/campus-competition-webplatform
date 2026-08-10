<template>
  <div class="welcome-page">
    <!-- 背景装饰 -->
    <div class="welcome-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="welcome-container">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9z" />
              <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9z" />
              <path d="M4 22h16" />
              <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
              <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
              <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
            </svg>
            <span>竞队</span>
          </div>
          <h1 class="brand-title">发现竞赛，<br><span class="gradient-text">组队同行</span></h1>
          <p class="brand-desc">汇聚全品类竞赛信息，AI 智能匹配优质队友，让你的竞赛之路不再孤单</p>
          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 2L2 7l10 5 10-5-10-5z" />
                  <path d="M2 17l10 5 10-5" />
                  <path d="M2 12l10 5 10-5" />
                </svg>
              </div>
              <span>全品类竞赛信息</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
              </div>
              <span>AI 智能匹配队友</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="3" width="7" height="7" />
                  <rect x="14" y="3" width="7" height="7" />
                  <rect x="14" y="14" width="7" height="7" />
                  <rect x="3" y="14" width="7" height="7" />
                </svg>
              </div>
              <span>一站式组队平台</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h2 class="form-title">{{ isRegisterMode ? '创建账号' : '欢迎回来' }}</h2>
            <p class="form-subtitle">{{ isRegisterMode ? '注册后即可发布组队、AI匹配、收藏' : '登录后继续你的竞赛之旅' }}</p>
          </div>

          <form class="form-body" @submit.prevent="handleSubmit">
            <!-- 登录模式 -->
            <template v-if="!isRegisterMode">
              <div class="field">
                <label class="field-label">昵称</label>
                <input
                  v-model="loginForm.name"
                  class="field-input"
                  placeholder="输入你的昵称"
                  maxlength="50"
                  required
                  autocomplete="username"
                />
              </div>
              <div class="field">
                <label class="field-label">密码</label>
                <input
                  v-model="loginForm.password"
                  type="password"
                  class="field-input"
                  placeholder="输入密码"
                  required
                  autocomplete="current-password"
                />
              </div>
            </template>

            <!-- 注册模式 -->
            <template v-else>
              <div class="field">
                <label class="field-label">昵称</label>
                <input
                  v-model="registerForm.name"
                  class="field-input"
                  placeholder="给自己取个名字"
                  maxlength="50"
                  required
                  autocomplete="username"
                />
              </div>
              <div class="field">
                <label class="field-label">密码</label>
                <input
                  v-model="registerForm.password"
                  type="password"
                  class="field-input"
                  placeholder="至少6位密码"
                  minlength="6"
                  required
                  autocomplete="new-password"
                />
              </div>
              <div class="field-row">
                <div class="field">
                  <label class="field-label">学校</label>
                  <input
                    v-model="registerForm.school"
                    class="field-input"
                    placeholder="选填"
                    maxlength="50"
                    autocomplete="organization"
                  />
                </div>
                <div class="field">
                  <label class="field-label">专业</label>
                  <input
                    v-model="registerForm.major"
                    class="field-input"
                    placeholder="选填"
                    maxlength="50"
                  />
                </div>
              </div>
              <div class="field">
                <label class="field-label">年级</label>
                <input
                  v-model="registerForm.grade"
                  class="field-input"
                  placeholder="选填，如：大三"
                  maxlength="20"
                />
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
          <div class="form-footer">
            <span>{{ isRegisterMode ? '已有账号？' : '没有账号？' }}</span>
            <button class="btn-switch" @click="toggleMode">
              {{ isRegisterMode ? '去登录' : '立即注册' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { login, register, onLoginSuccess } = useAuth()

const isRegisterMode = ref(false)
const loading = ref(false)
const errorMsg = ref('')

const loginForm = reactive({ name: '', password: '' })
const registerForm = reactive({ name: '', password: '', school: '', major: '', grade: '' })

function toggleMode() {
  isRegisterMode.value = !isRegisterMode.value
  errorMsg.value = ''
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
    router.push('/home')
  } catch (e) {
    errorMsg.value = e.message || (isRegisterMode.value ? '注册失败，请重试' : '登录失败，请检查昵称和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  padding: 24px;
}

/* ===== 背景装饰 ===== */
.welcome-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.15;
}

.bg-shape-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #667eea 0%, transparent 70%);
  top: -200px;
  right: -100px;
  animation: float 20s ease-in-out infinite;
}

.bg-shape-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #764ba2 0%, transparent 70%);
  bottom: -150px;
  left: -100px;
  animation: float 25s ease-in-out infinite reverse;
}

.bg-shape-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #43e97b 0%, transparent 70%);
  top: 40%;
  left: 10%;
  opacity: 0.08;
  animation: float 18s ease-in-out infinite 3s;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

/* ===== 容器 ===== */
.welcome-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* ===== 左侧品牌区 ===== */
.brand-section {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 30% 50%, rgba(102, 126, 234, 0.1) 0%, transparent 60%);
}

.brand-content {
  position: relative;
  z-index: 1;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 32px;
}
.brand-logo svg {
  color: #667eea;
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  line-height: 1.2;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.brand-title .gradient-text {
  background: linear-gradient(135deg, #667eea, #a78bfa, #43e97b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.7;
  margin-bottom: 36px;
  max-width: 360px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
  font-weight: 500;
}

.feature-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(102, 126, 234, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  flex-shrink: 0;
}

/* ===== 右侧表单区 ===== */
.form-section {
  width: 420px;
  padding: 48px 40px;
  display: flex;
  align-items: center;
}

.form-card {
  width: 100%;
}

.form-header {
  margin-bottom: 28px;
}

.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
}

.form-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
}

.field-input {
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  font-size: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.06);
  transition: all 0.2s;
  box-sizing: border-box;
  outline: none;
}
.field-input:focus {
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}
.field-input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.field-row {
  display: flex;
  gap: 12px;
}

.form-error {
  color: #ff6b6b;
  font-size: 13px;
  margin: 0;
  text-align: center;
  padding: 8px 12px;
  background: rgba(255, 107, 107, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(255, 107, 107, 0.15);
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
  margin-top: 4px;
}
.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}
.btn-submit:disabled {
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

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.btn-switch {
  background: none;
  border: none;
  color: #667eea;
  font-weight: 600;
  cursor: pointer;
  font-size: 14px;
  padding: 0 0 0 4px;
  transition: color 0.2s;
}
.btn-switch:hover {
  color: #a78bfa;
  text-decoration: underline;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .welcome-page {
    padding: 16px;
    align-items: flex-start;
    padding-top: 40px;
  }

  .welcome-container {
    flex-direction: column;
    min-height: auto;
    border-radius: 16px;
  }

  .brand-section {
    padding: 32px 24px 24px;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-desc {
    margin-bottom: 24px;
  }

  .brand-features {
    display: none;
  }

  .form-section {
    width: 100%;
    padding: 24px 24px 32px;
  }

  .form-title {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .welcome-page {
    padding: 12px;
  }

  .brand-section {
    padding: 24px 20px 20px;
  }

  .form-section {
    padding: 20px 20px 28px;
  }

  .field-row {
    flex-direction: column;
    gap: 18px;
  }
}
</style>