/**
 * useAuth — 全局认证状态管理（单例模式）
 *
 * 所有组件共享同一份认证状态。使用 reactive 全局单例确保跨组件响应式。
 *
 * 使用方式：
 *   const { isLoggedIn, user, showLogin, login, logout } = useAuth()
 */
import { computed, reactive } from 'vue'
import { api } from '../utils/api'
import {
  login as storageLogin,
  register as storageRegister,
  logout as storageLogout,
  getUser,
  saveUser,
} from '../utils/storage'

/* ================================================================
 * 全局响应式状态（单例）
 * ================================================================ */
const globalState = reactive({
  isLoggedIn: api.isLogin(),
  user: getUser(),
  showLoginModal: false,
})

let loginModalCallback = null

/* ================================================================
 * useAuth
 * ================================================================ */
export function useAuth() {

  /** 显示登录弹窗，可传入登录成功后的回调 */
  function showLogin(callback) {
    globalState.showLoginModal = true
    loginModalCallback = callback || null
  }

  /** 隐藏登录弹窗 */
  function hideLogin() {
    globalState.showLoginModal = false
    loginModalCallback = null
  }

  /** 登录 */
  async function login(name, password) {
    const data = await storageLogin(name, password)
    globalState.isLoggedIn = true
    globalState.user = getUser()
    return data
  }

  /** 注册 */
  async function register(formData) {
    const data = await storageRegister(formData)
    globalState.isLoggedIn = true
    globalState.user = getUser()
    return data
  }

  /** 退出登录 */
  function logout() {
    storageLogout()
    globalState.isLoggedIn = false
    globalState.user = null
    loginModalCallback = null
  }

  /** 登录成功后调用（由 LoginModal 触发） */
  async function onLoginSuccess() {
    globalState.isLoggedIn = true
    globalState.user = getUser()
    globalState.showLoginModal = false
    const cb = loginModalCallback
    loginModalCallback = null
    if (typeof cb === 'function') {
      await cb()
    }
  }

  /** 从后端刷新用户信息 */
  async function refreshUser() {
    if (!globalState.isLoggedIn) return
    try {
      const me = await api.getMe()
      if (me) {
        saveUser(me)
        globalState.user = me
      }
    } catch { /* 静默 */ }
  }

  return {
    isLoggedIn: computed(() => globalState.isLoggedIn),
    user: computed(() => globalState.user),
    showLoginModal: computed(() => globalState.showLoginModal),
    showLogin,
    hideLogin,
    login,
    register,
    logout,
    onLoginSuccess,
    refreshUser,
  }
}