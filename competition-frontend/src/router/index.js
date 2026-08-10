import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../pages/Welcome.vue') },
  { path: '/home', component: () => import('../pages/Home.vue'), meta: { requiresAuth: true } },
  { path: '/competition/:id', component: () => import('../pages/CompetitionDetail.vue'), meta: { requiresAuth: true } },
  { path: '/team', component: () => import('../pages/Team.vue'), meta: { requiresAuth: true } },
  { path: '/team/:id', component: () => import('../pages/TeamDetail.vue'), meta: { requiresAuth: true } },
  { path: '/publish', component: () => import('../pages/Publish.vue'), meta: { requiresAuth: true } },
  { path: '/ai-match', component: () => import('../pages/AiMatch.vue'), meta: { requiresAuth: true } },
  { path: '/mine', component: () => import('../pages/Mine.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 路由守卫：所有页面（除欢迎页 / 外）都需要登录
 * 未登录用户访问任何页面都会重定向到欢迎页
 */
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    import('../composables/useAuth').then(({ useAuth }) => {
      const { isLoggedIn } = useAuth()
      if (!isLoggedIn.value) {
        next('/')
      } else {
        next()
      }
    })
  } else {
    next()
  }
})

export default router