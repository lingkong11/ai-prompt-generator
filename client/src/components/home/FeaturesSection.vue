<template>
  <div class="features-section">
    <h2 class="section-title">{{ t.features.title }}</h2>
    <p class="section-subtitle">{{ t.features.subtitle }}</p>
    
    <div class="features-grid">
      <div v-for="feature in features" :key="feature.key" class="feature-card">
        <div class="feature-icon" :style="{ background: feature.color }">
          <component :is="feature.icon" />
        </div>
        <h3 class="feature-title">{{ feature.title }}</h3>
        <p class="feature-desc">{{ feature.desc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { useAppStore } from '@/stores/app'
import { MagicStick, Document, Cpu, Collection, Star, Connection } from '@element-plus/icons-vue'

const appStore = useAppStore()
const t = computed(() => appStore.t)

const features = computed(() => [
  {
    key: 'generate',
    icon: MagicStick,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    title: t.value.features.f1Title,
    desc: t.value.features.f1Desc
  },
  {
    key: 'templates',
    icon: Document,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    title: t.value.features.f2Title,
    desc: t.value.features.f2Desc
  },
  {
    key: 'ai',
    icon: Cpu,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    title: t.value.features.f3Title,
    desc: t.value.features.f3Desc
  },
  {
    key: 'openclaw',
    icon: Collection,
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    title: t.value.features.f4Title,
    desc: t.value.features.f4Desc
  },
  {
    key: 'save',
    icon: Star,
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    title: t.value.features.f5Title,
    desc: t.value.features.f5Desc
  },
  {
    key: 'multi',
    icon: Connection,
    color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    title: t.value.features.f6Title,
    desc: t.value.features.f6Desc
  }
])
</script>

<style scoped>
.features-section {
  padding: 80px 40px;
  max-width: 1200px;
  margin: 0 auto;
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

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.feature-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 28px;
  color: white;
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.feature-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .section-title {
    font-size: 28px;
  }
}
</style>
