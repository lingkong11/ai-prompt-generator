<template>
  <div class="pricing-card" :class="[`plan-${plan.code.toLowerCase()}`, { popular: plan.isPopular }]">
    <!-- 推荐徽章 -->
    <div v-if="plan.isPopular" class="popular-badge">
      <el-icon><StarFilled /></el-icon>
      {{ t.pricing.popular }}
    </div>

    <div class="card-header">
      <h3 class="plan-name">{{ plan.name }}</h3>
      <div class="plan-price">
        <span class="currency">¥</span>
        <span class="amount">{{ displayPrice }}</span>
        <span class="period">/{{ billingCycle === 'YEARLY' ? t.pricing.yearly : t.pricing.monthly }}</span>
      </div>
      <div v-if="yearlySavings > 0 && billingCycle === 'YEARLY'" class="savings-tag">
        {{ t.pricing.save.replace('{amount}', yearlySavings.toString()) }}
      </div>
    </div>

    <el-divider />

    <ul class="feature-list">
      <li v-for="feature in planFeatures" :key="feature.key" :class="{ disabled: !feature.included }">
        <el-icon v-if="feature.included" class="check-icon"><Check /></el-icon>
        <el-icon v-else class="cross-icon"><Close /></el-icon>
        <span>{{ feature.label }}</span>
      </li>
    </ul>

    <el-button
      :type="buttonType"
      :class="['upgrade-btn', { current: isCurrent }]"
      :disabled="isCurrent"
      :loading="loading"
      @click="handleUpgrade"
    >
      {{ buttonText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { StarFilled, Check, Close } from '@element-plus/icons-vue'
import type { Plan, BillingCycle } from '@/stores/subscription'

const props = defineProps<{
  plan: Plan
  billingCycle: BillingCycle
  isCurrent: boolean
  loading: boolean
  t: any
}>()

const emit = defineEmits<{
  upgrade: [planCode: string]
}>()

const displayPrice = computed(() => {
  return props.billingCycle === 'YEARLY' 
    ? Math.round(props.plan.priceYearly / 12)
    : props.plan.priceMonthly
})

const yearlySavings = computed(() => {
  return Math.round((props.plan.priceMonthly * 12) - props.plan.priceYearly)
})

const buttonType = computed(() => {
  if (props.plan.code === 'PRO') return 'primary'
  if (props.plan.code === 'TEAM') return 'warning'
  return 'default'
})

const buttonText = computed(() => {
  if (props.isCurrent) return props.t.pricing.currentPlan
  if (props.plan.code === 'FREE') return props.t.pricing.freePlan
  return props.t.pricing.upgrade
})

const planFeatures = computed(() => {
  const t = props.t.pricing.features
  return [
    { key: 'generate', label: props.plan.generateLimit === -1 ? t.unlimitedGenerate : t.dailyGenerate.replace('{count}', props.plan.generateLimit), included: true },
    { key: 'favorite', label: props.plan.favoriteLimit === -1 ? t.unlimitedFavorite : t.favoriteLimit.replace('{count}', props.plan.favoriteLimit), included: true },
    { key: 'templates', label: props.plan.code === 'FREE' ? t.basicTemplates : t.allTemplates, included: true },
    { key: 'export', label: t.exportFeature, included: props.plan.code !== 'FREE' },
    { key: 'support', label: t.prioritySupport, included: props.plan.code !== 'FREE' },
    { key: 'api', label: t.apiAccess, included: props.plan.code === 'TEAM' },
  ]
})

function handleUpgrade() {
  emit('upgrade', props.plan.code)
}
</script>

<style scoped>
.pricing-card {
  position: relative;
  padding: 32px;
  border-radius: 16px;
  background: var(--card-bg, #fff);
  border: 2px solid var(--card-border, #e5e7eb);
  transition: all 0.3s ease;
}

.pricing-card.popular {
  transform: scale(1.05);
  z-index: 2;
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
}

.popular-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: #1f2937;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.4);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(251, 191, 36, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(251, 191, 36, 0); }
}

/* Free Plan */
.plan-free {
  --card-bg: #ffffff;
  --card-border: #e5e7eb;
}

/* Pro Plan */
.plan-pro {
  --card-bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --card-border: transparent;
  color: white;
}

.plan-pro :deep(.el-divider) {
  border-color: rgba(255, 255, 255, 0.2);
}

/* Team Plan */
.plan-team {
  --card-bg: #1f2937;
  --card-border: #fbbf24;
  color: white;
}

.plan-team :deep(.el-divider) {
  border-color: rgba(255, 255, 255, 0.2);
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
}

.plan-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
}

.plan-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.currency {
  font-size: 24px;
  font-weight: 600;
}

.amount {
  font-size: 56px;
  font-weight: 800;
  line-height: 1;
}

.period {
  font-size: 16px;
  opacity: 0.8;
}

.savings-tag {
  margin-top: 8px;
  font-size: 13px;
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0 0 24px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  font-size: 15px;
}

.feature-list li.disabled {
  opacity: 0.5;
}

.check-icon {
  color: #10b981;
  font-size: 18px;
}

.cross-icon {
  color: #9ca3af;
  font-size: 18px;
}

.plan-pro .check-icon,
.plan-team .check-icon {
  color: #86efac;
}

.upgrade-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.upgrade-btn.current {
  opacity: 0.6;
}

@media (max-width: 768px) {
  .pricing-card.popular {
    transform: none;
  }
}
</style>
