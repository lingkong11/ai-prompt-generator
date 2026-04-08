<template>
  <div class="pricing-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <h1 class="hero-title">{{ t.pricing.title }}</h1>
      <p class="hero-subtitle">{{ t.pricing.subtitle }}</p>
      
      <BillingToggle v-model="billingCycle" :t="t" />
    </section>

    <!-- Pricing Cards -->
    <section class="pricing-cards">
      <PricingCard
        v-for="plan in subscriptionStore.plans"
        :key="plan.code"
        :plan="plan"
        :billing-cycle="billingCycle"
        :is-current="subscriptionStore.currentPlan?.code === plan.code"
        :loading="subscriptionStore.loading"
        :t="t"
        @upgrade="handleUpgrade"
      />
    </section>

    <!-- FAQ Section -->
    <PricingFAQ :t="t" />

    <!-- Enterprise CTA -->
    <section class="enterprise-cta">
      <h2>{{ t.pricing.enterpriseTitle }}</h2>
      <p>{{ t.pricing.enterpriseDesc }}</p>
      <el-button type="primary" size="large" @click="showEnterpriseDialog = true">
        {{ t.pricing.contactSales }}
      </el-button>
    </section>

    <!-- Upgrade Dialog -->
    <el-dialog
      v-model="showUpgradeDialog"
      :title="t.pricing.upgradeTitle"
      width="440px"
    >
      <div class="upgrade-confirm">
        <p>{{ t.pricing.upgradeConfirm.replace('{plan}', selectedPlan?.name || '') }}</p>
        <p class="upgrade-price">
          ¥{{ billingCycle === 'YEARLY' ? selectedPlan?.priceYearly : selectedPlan?.priceMonthly }}
          /{{ billingCycle === 'YEARLY' ? t.pricing.year : t.pricing.month }}
        </p>
        <p class="upgrade-note">{{ t.pricing.upgradeNote }}</p>
      </div>
      <template #footer>
        <el-button @click="showUpgradeDialog = false">{{ t.common.cancel }}</el-button>
        <el-button type="primary" :loading="subscriptionStore.loading" @click="confirmUpgrade">
          {{ t.pricing.confirmUpgrade }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Enterprise Dialog -->
    <el-dialog
      v-model="showEnterpriseDialog"
      :title="t.pricing.enterpriseTitle"
      width="500px"
    >
      <p class="enterprise-desc">{{ t.pricing.enterpriseFormDesc }}</p>
      <el-form :model="enterpriseForm" label-position="top">
        <el-form-item :label="t.pricing.companyName">
          <el-input v-model="enterpriseForm.company" />
        </el-form-item>
        <el-form-item :label="t.pricing.contactEmail">
          <el-input v-model="enterpriseForm.email" />
        </el-form-item>
        <el-form-item :label="t.pricing.teamSize">
          <el-input-number v-model="enterpriseForm.teamSize" :min="1" />
        </el-form-item>
        <el-form-item :label="t.pricing.requirements">
          <el-input v-model="enterpriseForm.requirements" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEnterpriseDialog = false">{{ t.common.cancel }}</el-button>
        <el-button type="primary" @click="submitEnterprise">{{ t.pricing.submit }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/stores/app'
import { useSubscriptionStore, type BillingCycle, type Plan } from '@/stores/subscription'
import PricingCard from '@/components/pricing/PricingCard.vue'
import BillingToggle from '@/components/pricing/BillingToggle.vue'
import PricingFAQ from '@/components/pricing/PricingFAQ.vue'

const appStore = useAppStore()
const subscriptionStore = useSubscriptionStore()

const t = computed(() => appStore.t)
const billingCycle = ref<BillingCycle>('MONTHLY')

const showUpgradeDialog = ref(false)
const showEnterpriseDialog = ref(false)
const selectedPlan = ref<Plan | null>(null)

const enterpriseForm = ref({
  company: '',
  email: '',
  teamSize: 10,
  requirements: ''
})

onMounted(() => {
  subscriptionStore.init()
})

function handleUpgrade(planCode: string) {
  const plan = subscriptionStore.plans.find(p => p.code === planCode)
  if (!plan) return
  
  if (planCode === 'FREE') {
    ElMessage.info(t.value.pricing.alreadyFree)
    return
  }
  
  selectedPlan.value = plan
  showUpgradeDialog.value = true
}

async function confirmUpgrade() {
  if (!selectedPlan.value) return
  
  try {
    await subscriptionStore.upgrade(selectedPlan.value.code, billingCycle.value)
    ElMessage.success(t.value.pricing.upgradeSuccess)
    showUpgradeDialog.value = false
  } catch (e: any) {
    ElMessage.error(e.message || t.value.pricing.upgradeFailed)
  }
}

function submitEnterprise() {
  ElMessage.success(t.value.pricing.submitSuccess)
  showEnterpriseDialog.value = false
  enterpriseForm.value = { company: '', email: '', teamSize: 10, requirements: '' }
}
</script>

<style scoped>
.pricing-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 80px;
}

.hero-section {
  text-align: center;
  padding: 60px 0 40px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 20px;
  color: #6b7280;
  margin-bottom: 40px;
}

.pricing-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  align-items: center;
  margin-bottom: 80px;
}

@media (max-width: 1024px) {
  .pricing-cards {
    grid-template-columns: 1fr;
    max-width: 400px;
    margin: 0 auto 80px;
  }
}

.enterprise-cta {
  text-align: center;
  padding: 60px 40px;
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  border-radius: 24px;
  margin-top: 80px;
}

.enterprise-cta h2 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.enterprise-cta p {
  color: #6b7280;
  margin-bottom: 24px;
}

.upgrade-confirm {
  text-align: center;
}

.upgrade-price {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
  margin: 16px 0;
}

.upgrade-note {
  font-size: 13px;
  color: #9ca3af;
}

.enterprise-desc {
  margin-bottom: 20px;
  color: #6b7280;
}
</style>
