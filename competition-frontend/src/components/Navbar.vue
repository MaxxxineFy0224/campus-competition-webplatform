<template>
  <nav :class="['navbar', { scrolled: isScrolled }]">
    <div class="navbar-inner">
      <RouterLink to="/" class="navbar-logo">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9z" />
          <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9z" />
          <path d="M4 22h16" />
          <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
          <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
          <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
        </svg>
        竞队
      </RouterLink>

      <button
        class="navbar-menu-btn"
        @click="menuOpen = !menuOpen"
        :aria-label="menuOpen ? '关闭菜单' : '打开菜单'"
      >
        <svg v-if="!menuOpen" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M3 12h18M3 6h18M3 18h18" />
        </svg>
        <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M18 6 6 18M6 6l12 12" />
        </svg>
      </button>

      <div :class="['navbar-links', { open: menuOpen }]">
        <RouterLink
          v-for="item in NAV_ITEMS"
          :key="item.path"
          :to="item.path"
          :class="['navbar-link', { active: isActive(item.path) }]"
          @click="menuOpen = false"
        >
          <component :is="item.icon" class="nav-link-icon" />
          {{ item.label }}
        </RouterLink>

        <!-- 用户区域 -->
        <div class="navbar-user">
          <template v-if="isLoggedIn && user">
            <div class="user-avatar">{{ user.name?.charAt(0)?.toUpperCase() || 'U' }}</div>
            <span class="user-name">{{ user.name }}</span>
            <button class="btn-logout" @click="handleLogout" title="退出登录">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
            </button>
          </template>
          <template v-else>
            <button class="btn-login" @click="handleLogin">登录</button>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onUnmounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const route = useRoute()
const router = useRouter()
const { isLoggedIn, user, showLogin, logout } = useAuth()
const menuOpen = ref(false)
const isScrolled = ref(false)

// 路由切换时关闭菜单
import { watch } from 'vue'
watch(() => route.path, () => { menuOpen.value = false })

// ---- SVG Icons ----
const HomeIcon = h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }, [
  h('path', { d: 'm3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z' }),
  h('polyline', { points: '9 22 9 12 15 12 15 22' }),
])
const TeamIcon = h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }, [
  h('path', { d: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: 9, cy: 7, r: 4 }),
  h('path', { d: 'M23 21v-2a4 4 0 0 0-3-3.87' }),
  h('path', { d: 'M16 3.13a4 4 0 0 1 0 7.75' }),
])
const PublishIcon = h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }, [
  h('path', { d: 'M12 5v14' }), h('path', { d: 'M5 12h14' }),
])
const AiIcon = h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }, [
  h('path', { d: 'M12 2a3 3 0 0 0-3 3v1a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z' }),
  h('path', { d: 'M5 15a7 7 0 0 1 14 0v1a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-1Z' }),
  h('path', { d: 'M8 18v2a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-2' }),
])
const UserIcon = h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: 12, cy: 7, r: 4 }),
])

const NAV_ITEMS = [
  { path: '/', label: '竞赛首页', icon: HomeIcon },
  { path: '/team', label: '组队广场', icon: TeamIcon },
  { path: '/publish', label: '发布组队', icon: PublishIcon },
  { path: '/ai-match', label: 'AI匹配', icon: AiIcon },
  { path: '/mine', label: '个人中心', icon: UserIcon },
]

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function handleLogin() {
  showLogin(() => {
    router.push('/')
  })
}

function handleLogout() {
  logout()
  if (route.meta.requiresAuth) {
    router.push('/')
  }
}

function handleScroll() {
  isScrolled.value = window.scrollY > 10
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 500;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}
.navbar.scrolled {
  border-bottom-color: var(--border);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 clamp(16px, 3vw, 48px);
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 800;
  color: var(--text);
  flex-shrink: 0;
}
.navbar-logo svg {
  color: var(--primary);
}

.navbar-menu-btn {
  display: none;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px;
}

.navbar-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.navbar-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all 0.2s;
  white-space: nowrap;
}
.navbar-link:hover {
  background: rgba(102, 126, 234, 0.08);
  color: var(--primary);
}
.navbar-link.active {
  background: rgba(102, 126, 234, 0.12);
  color: var(--primary);
  font-weight: 600;
}

/* ---- 用户区域 ---- */
.navbar-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
  padding-left: 12px;
  border-left: 1px solid var(--border);
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-logout {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
}
.btn-logout:hover {
  background: #fee2e2;
  color: #e74c3c;
}

.btn-login {
  padding: 7px 18px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-login:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* ---- 响应式 ---- */
@media (max-width: 768px) {
  .navbar-menu-btn {
    display: flex;
  }

  .navbar-links {
    display: none;
    position: fixed;
    top: 64px;
    left: 0;
    right: 0;
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(12px);
    flex-direction: column;
    padding: 12px;
    border-bottom: 1px solid var(--border);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }
  .navbar-links.open {
    display: flex;
  }

  .navbar-link {
    width: 100%;
    padding: 12px 16px;
  }

  .navbar-user {
    width: 100%;
    margin-left: 0;
    padding-left: 0;
    border-left: none;
    padding: 8px 16px;
    justify-content: space-between;
  }
}

.nav-link-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
</style>