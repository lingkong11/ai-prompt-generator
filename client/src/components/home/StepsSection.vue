<template>
  <div class="steps-section">
    <h2 class="section-title">{{ t.steps.title }}</h2>
    <p class="section-subtitle">{{ t.steps.subtitle }}</p>
    
    <div class="steps-container">
      <div v-for="(step, index) in steps" :key="index" class="step-item">
        <div class="step-number">{{ index + 1 }}</div>
        <div class="step-icon">
          <component :is="step.icon" />
        </div>
        <h3 class="step-title">{{ step.title }}</h3>
        <p class="step-desc">{{ step.desc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { Edit, MagicStick, Star, ArrowRight } from '@element-plus/icons-vue'

const appStore = useAppStore()
const t = computed(() => appStore.t)

const steps = computed(() => [
  {
    icon: Edit,
    title: t.value.steps.s1Title,
    desc: t.value.steps.s1Desc
  },
  {
    icon: MagicStick,
    title: t.value.steps.s2Title,
    desc: t.value.steps.s2Desc
  },
  {
    icon: Star,
    title: t.value.steps.s3Title,
    desc: t.value.steps.s3Desc
  },
  {
    icon: ArrowRight,
    title: t.value.steps.s4Title,
    desc: t.value.steps.s4Desc
  }
])
</script>

<style scoped>
.steps-section {
  padding: 80px 40px;
  max-width: 1200px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  margin: 40px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.section-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  text-align: center;
  margin-bottom: 48px;
}

.steps-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
}

.step-item {
  flex: 1;
  text-align: center;
  position: relative;
}

.step-number {
  position: absolute;
  top: -10px;
  right: calc(50% - 40px);
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  z-index: 2;
}

.step-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 32px;
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.step-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .steps-container {
    flex-direction: column;
    align-items: center;
  }
  
  .step-item {
    max-width: 300px;
  }
}
</style>
