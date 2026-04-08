import { createRouter, createWebHistory } from 'vue-router'
import App from '@/App.vue'
import PricingView from '@/views/PricingView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: App
    },
    {
      path: '/pricing',
      name: 'Pricing',
      component: PricingView
    },
  ]
})

export default router
