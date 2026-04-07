import { createRouter, createWebHistory } from 'vue-router'
import { initAxios } from '@/api/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/App.vue'),
      children: [
        { path: '', name: 'Generate', component: () => import('@/views/HomeView.vue') },
        { path: '/openclaw', name: 'OpenClaw', component: () => import('@/views/OpenClawView.vue') }
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue')
    }
  ]
})

initAxios()
export default router
