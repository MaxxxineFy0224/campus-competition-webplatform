<script setup>
import { RouterView, useRouter } from 'vue-router'
import { watch, ref, onMounted, onUnmounted } from 'vue'
import Navbar from './components/Navbar.vue'
import Toast from './components/Toast.vue'
import Modal from './components/Modal.vue'
import { initMockData } from './data/mockData'

initMockData()

const router = useRouter()
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
    <main class="main-content">
      <RouterView :key="routeKey" v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
    <Toast />
    <Modal />
    <footer class="app-footer">
      <div class="footer-wave">
        <svg viewBox="0 0 1440 60" preserveAspectRatio="none">
          <path d="M0,30 C360,60 720,0 1080,30 C1260,45 1350,30 1440,30 L1440,60 L0,60 Z" fill="var(--color-bg, #f5f7fa)"/>
        </svg>
      </div>
      <div class="footer-inner">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="footer-logo-wrapper">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9z" />
                <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9z" />
                <path d="M4 22h16" />
                <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
                <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
                <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
              </svg>
              <span class="footer-logo-text">竞队</span>
            </div>
            <p class="footer-desc">校园竞赛组队平台 — 汇聚全品类竞赛信息，AI 智能匹配优质队友，让你的竞赛之路不再孤单。</p>
            <div class="footer-social">
              <span class="social-dot"></span>
              <span class="social-dot" style="background: #722ed1; animation-delay: 0.3s;"></span>
              <span class="social-dot" style="background: #52c41a; animation-delay: 0.6s;"></span>
            </div>
          </div>
          <div class="footer-links">
            <h4 class="footer-heading">快速导航</h4>
            <RouterLink to="/" class="footer-link">竞赛首页</RouterLink>
            <RouterLink to="/team" class="footer-link">组队广场</RouterLink>
            <RouterLink to="/publish" class="footer-link">发布组队</RouterLink>
            <RouterLink to="/ai-match" class="footer-link">AI 智能匹配</RouterLink>
          </div>
          <div class="footer-links">
            <h4 class="footer-heading">功能服务</h4>
            <RouterLink to="/mine" class="footer-link">个人中心</RouterLink>
            <RouterLink to="/team" class="footer-link">浏览队伍</RouterLink>
            <RouterLink to="/ai-match" class="footer-link">智能推荐</RouterLink>
            <span class="footer-link disabled-link">更多功能</span>
          </div>
          <div class="footer-contact">
            <h4 class="footer-heading">联系我们</h4>
            <div class="contact-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
                <polyline points="22,6 12,13 2,6" />
              </svg>
              <span>contact@jingdui.edu</span>
            </div>
            <div class="contact-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <span>微信: jingdui_app</span>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <div class="footer-divider"></div>
          <div class="footer-bottom-inner">
            <span>©2026 竞队 · 校园竞赛组队平台</span>
            <span class="footer-credit">Built with ❤️ for students</span>
          </div>
        </div>
      </div>
    </footer>

    <!-- 滚动进度条 -->
    <div class="scroll-progress" :style="{ width: scrollProgress + '%' }"></div>

    <!-- 回到顶部按钮 -->
    <Transition name="back-to-top">
      <button
        v-if="showBackToTop"
        class="back-to-top-btn"
        @click="scrollToTop"
        title="回到顶部"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="m18 15-6-6-6 6" />
        </svg>
      </button>
    </Transition>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 4px;
  animation: contentFadeIn 0.4s ease;
}

@keyframes contentFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.page-enter-active {
  animation: pageIn 0.3s ease;
}
.page-leave-active {
  animation: pageOut 0.18s ease;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pageOut {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-6px); }
}

.app-footer {
  margin-top: 60px;
  background: linear-gradient(180deg, #f5f7fa 0%, #eef1f5 100%);
  border-top: 1px solid rgba(0, 0, 0, 0.04);
  position: relative;
}

.footer-wave {
  position: absolute;
  top: -1px;
  left: 0;
  right: 0;
  height: 60px;
  overflow: hidden;
  pointer-events: none;
}

.footer-wave svg {
  width: 100%;
  height: 100%;
}

.footer-inner {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
  padding: 0 clamp(16px, 3vw, 48px);
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr repeat(3, 1fr);
  gap: clamp(24px, 3vw, 48px);
  padding: 48px 0 32px;
}

.footer-logo-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #722ed1;
}

.footer-logo-text {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, #1677ff, #722ed1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.footer-desc {
  font-size: 13px;
  color: #888;
  line-height: 1.7;
  margin: 0 0 16px;
  max-width: min(320px, 100%);
}

.footer-social {
  display: flex;
  gap: 8px;
}

.social-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1677ff;
  animation: socialPulse 2s ease-in-out infinite;
}

@keyframes socialPulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.3); }
}

.footer-heading {
  font-size: 14px;
  font-weight: 700;
  color: #333;
  margin: 0 0 16px;
  position: relative;
  padding-bottom: 8px;
}

.footer-heading::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 24px;
  height: 2px;
  background: linear-gradient(90deg, #1677ff, #722ed1);
  border-radius: 1px;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-link {
  font-size: 13px;
  color: #888;
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;
  background: none;
  border: none;
  text-align: left;
  padding: 0;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  width: fit-content;
}

.footer-link:hover {
  color: #722ed1;
  transform: translateX(4px);
}

.disabled-link {
  color: #ccc !important;
  cursor: default !important;
  transform: none !important;
}

.footer-contact {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #888;
}

.contact-item svg {
  flex-shrink: 0;
  color: #722ed1;
}

.footer-bottom {
  padding: 20px 0;
}

.footer-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0,0,0,0.06), transparent);
  margin-bottom: 20px;
}

.footer-bottom-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  font-size: 12px;
  color: #bbb;
}

.footer-credit {
  color: #ccc;
}

@media (max-width: 900px) {
  .footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: clamp(20px, 3vw, 32px);
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}

@media (max-width: 480px) {
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 32px 0 24px;
  }

  .footer-inner {
    padding: 0 clamp(12px, 4vw, 16px);
  }

  .footer-bottom-inner {
    flex-direction: column;
    text-align: center;
  }
}

/* ===== 滚动进度条 ===== */
.scroll-progress {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  background: linear-gradient(90deg, #1677ff, #722ed1);
  z-index: 9999;
  transition: width 0.1s linear;
  pointer-events: none;
}

/* ===== 回到顶部按钮 ===== */
.back-to-top-btn {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  color: #555;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
}

.back-to-top-btn:hover {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
  transform: translateY(-3px);
  box-shadow: 0 6px 24px rgba(22, 119, 255, 0.3);
}

.back-to-top-btn:active {
  transform: translateY(-1px);
}

.back-to-top-enter-active {
  animation: backIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-to-top-leave-active {
  animation: backOut 0.2s ease forwards;
}

@keyframes backIn {
  from { opacity: 0; transform: scale(0.5) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

@keyframes backOut {
  from { opacity: 1; transform: scale(1) translateY(0); }
  to { opacity: 0; transform: scale(0.5) translateY(12px); }
}

@media (max-width: 640px) {
  .back-to-top-btn {
    bottom: 20px;
    right: 20px;
    width: 40px;
    height: 40px;
  }
}
</style>