import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import OpenClawView from '@/views/OpenClawView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'Register',
      component: RegisterView
    },
    {
      path: '/',
      name: 'Home',
      component: HomeView
    },
    {
      path: '/openclaw',
      name: 'OpenClaw',
      component: OpenClawView
    }
  ]
})

export default router
