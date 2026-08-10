<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { watch, ref, onMounted, onUnmounted } from 'vue'
import Navbar from './components/Navbar.vue'
import LoginModal from './components/LoginModal.vue'
import Toast from './components/Toast.vue'
import Modal from './components/Modal.vue'
import { initMockData } from './data/mockData'
import { syncAllData } from './utils/storage'

initMockData()
// 尝试从后端同步数据（失败则静默降级到 localStorage mock）
syncAllData()

const router = useRouter()
const route = useRoute()
const routeKey = ref(0)
const showBackToTop = ref(false)
const scrollProgress = ref(0)

watch(() => router.currentRoute.value.path, () => {
  routeKey.value += 1
  window.scrollTo({ top: 0, behavior: 'smooth' })
})

function handleScroll() {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  showBackToTop.value = scrollTop > 300
  scrollProgress.value = docHeight > 0 ? Math.min((scrollTop / docHeight) * 100, 100) : 0
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="app-layout">
    <Navbar />
    <main :class="['main-content', { 'has-navbar': route.path !== '/' }]">
      <RouterView :key="routeKey" v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>

    <!-- 返回顶部 -->
    <Transition name="fade">
      <button v-if="showBackToTop" class="back-to-top" @click="scrollToTop" aria-label="返回顶部">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="m18 15-6-6-6 6"/>
        </svg>
      </button>
    </Transition>

    <!-- 进度条 -->
    <div class="scroll-progress" :style="{ width: scrollProgress + '%' }"></div>

    <!-- 全局组件 -->
    <Toast />
    <Modal />
    <LoginModal />
  </div>
</template>

<style>
/* ================================================================
 * 全局样式重置
 * ================================================================ */
*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

:root {
  --primary: #667eea;
  --primary-dark: #5a6fd6;
  --primary-light: #8b9cf7;
  --bg: #f5f7fa;
  --card-bg: #ffffff;
  --text: #1a1a2e;
  --text-secondary: #666;
  --text-muted: #999;
  --border: #e8ecf1;
  --radius: 12px;
  --shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-hover: 0 8px 30px rgba(0, 0, 0, 0.1);
  --transition: 0.25s ease;
}

html {
  scroll-behavior: smooth;
  -webkit-font-smoothing: antialiased;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: var(--bg);
  color: var(--text);
  line-height: 1.6;
  min-height: 100vh;
}

a {
  color: inherit;
  text-decoration: none;
}

button {
  font-family: inherit;
}

img {
  max-width: 100%;
  height: auto;
}

/* ================================================================
 * 布局
 * ================================================================ */
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.main-content.has-navbar {
  padding-top: 64px;
}

/* ================================================================
 * 页面过渡动画
 * ================================================================ */
.page-enter-active, .page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ================================================================
 * 返回顶部
 * ================================================================ */
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 100;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: var(--card-bg);
  color: var(--text-secondary);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}
.back-to-top:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
  color: var(--primary);
}

/* ================================================================
 * 滚动进度条
 * ================================================================ */
.scroll-progress {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--primary-light));
  z-index: 1000;
  transition: width 0.1s linear;
}

/* ================================================================
 * 通用过渡
 * ================================================================ */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>