import { createRouter, createWebHistory } from 'vue-router'
import { initAxios } from '@/api/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue')
    },
    {
      // 所有其他路径都由App.vue处理（认证检查+布局）
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

// 初始化axios
initAxios()

export default router
