import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../pages/Home.vue') },
  { path: '/competition/:id', component: () => import('../pages/CompetitionDetail.vue') },
  { path: '/team', component: () => import('../pages/Team.vue') },
  { path: '/team/:id', component: () => import('../pages/TeamDetail.vue') },
  { path: '/publish', component: () => import('../pages/Publish.vue') },
  { path: '/ai-match', component: () => import('../pages/AiMatch.vue') },
  { path: '/mine', component: () => import('../pages/Mine.vue') },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
