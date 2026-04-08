import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getPlans, getCurrentSubscription, getQuotaStatus, upgradePlan, cancelSubscription } from '@/api/subscription'

export type PlanCode = 'FREE' | 'PRO' | 'TEAM'
export type BillingCycle = 'MONTHLY' | 'YEARLY'

export interface Plan {
  id: number
  code: PlanCode
  name: string
  priceMonthly: number
  priceYearly: number
  generateLimit: number
  favoriteLimit: number
  features: string[]
  isPopular?: boolean
}

export interface Subscription {
  id: number
  planId: number
  planName: string
  planCode: PlanCode
  status: string
  billingCycle: BillingCycle
  expiresAt: string
  autoRenew: boolean
  isValid: boolean
}

export interface QuotaStatus {
  generateLimit: number
  generateUsed: number
  generateRemaining: number
  favoriteLimit: number
  favoriteUsed: number
  favoriteRemaining: number
  canGenerate: boolean
  canFavorite: boolean
}

export const useSubscriptionStore = defineStore('subscription', () => {
  // State
  const plans = ref<Plan[]>([])
  const currentSubscription = ref<Subscription | null>(null)
  const quotaStatus = ref<QuotaStatus | null>(null)
  const billingCycle = ref<BillingCycle>('MONTHLY')
  const loading = ref(false)

  // Getters
  const currentPlan = computed(() => {
    return plans.value.find(p => p.code === currentSubscription.value?.planCode)
  })

  const isPro = computed(() => currentSubscription.value?.planCode === 'PRO')
  const isTeam = computed(() => currentSubscription.value?.planCode === 'TEAM')
  const isFree = computed(() => !currentSubscription.value || currentSubscription.value.planCode === 'FREE')

  const yearlySavings = computed(() => {
    return plans.value.map(plan => ({
      code: plan.code,
      savings: (plan.priceMonthly * 12) - plan.priceYearly
    }))
  })

  // Actions
  async function fetchPlans() {
    try {
      const res = await getPlans()
      plans.value = res.data.data || []
    } catch (e) {
      console.error('获取套餐列表失败:', e)
    }
  }

  async function fetchCurrentSubscription() {
    try {
      const res = await getCurrentSubscription()
      currentSubscription.value = res.data.data
    } catch (e) {
      console.error('获取当前订阅失败:', e)
    }
  }

  async function fetchQuotaStatus() {
    try {
      const res = await getQuotaStatus()
      quotaStatus.value = res.data.data
    } catch (e) {
      console.error('获取配额状态失败:', e)
    }
  }

  async function upgrade(planCode: PlanCode, cycle: BillingCycle = 'MONTHLY') {
    loading.value = true
    try {
      const res = await upgradePlan(planCode, cycle)
      currentSubscription.value = res.data.data
      await fetchQuotaStatus()
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function cancel() {
    loading.value = true
    try {
      await cancelSubscription()
      await fetchCurrentSubscription()
    } finally {
      loading.value = false
    }
  }

  function setBillingCycle(cycle: BillingCycle) {
    billingCycle.value = cycle
  }

  // Initialize
  async function init(isLoggedIn: boolean = false) {
    await fetchPlans() // 公开接口，游客也可访问
    if (isLoggedIn) {
      await fetchCurrentSubscription()
      await fetchQuotaStatus()
    }
  }

  return {
    plans,
    currentSubscription,
    quotaStatus,
    billingCycle,
    loading,
    currentPlan,
    isPro,
    isTeam,
    isFree,
    yearlySavings,
    fetchPlans,
    fetchCurrentSubscription,
    fetchQuotaStatus,
    upgrade,
    cancel,
    setBillingCycle,
    init
  }
})
