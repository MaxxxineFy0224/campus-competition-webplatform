import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../pages/Home.vue') },
  { path: '/competition/:id', component: () => import('../pages/CompetitionDetail.vue') },
  { path: '/team', component: () => import('../pages/Team.vue') },
  { path: '/team/:id', component: () => import('../pages/TeamDetail.vue') },
  { path: '/publish', component: () => import('../pages/Publish.vue'), meta: { requiresAuth: true } },
  { path: '/ai-match', component: () => import('../pages/AiMatch.vue'), meta: { requiresAuth: true } },
  { path: '/mine', component: () => import('../pages/Mine.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 路由守卫：访问需登录页面时，未登录则弹出登录弹窗
 *
 * 游客仍可浏览竞赛首页(/)、竞赛详情(/competition/:id)、组队广场(/team)、组队帖详情(/team/:id)
 * 需要登录的页面：/publish、/ai-match、/mine
 */
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    // 动态导入 useAuth，避免循环依赖
    import('../composables/useAuth').then(({ useAuth }) => {
      const { isLoggedIn, showLogin } = useAuth()
      if (!isLoggedIn.value) {
        showLogin(() => {
          // 登录成功后重新导航到目标页面
          next()
        })
        // 不执行 next()，等待登录回调
      } else {
        next()
      }
    })
  } else {
    next()
  }
})

export default router