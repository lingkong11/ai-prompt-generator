<template>
  <div class="hero-section">
    <div class="hero-content">
      <h1 class="hero-title">
        <span class="gradient-text">{{ t.hero.title }}</span>
      </h1>
      <p class="hero-subtitle">{{ t.hero.subtitle }}</p>
      <p class="hero-desc">{{ t.hero.desc }}</p>
      
      <div class="hero-actions">
        <el-button type="primary" size="large" class="cta-btn" @click="$emit('start')">
          <el-icon><MagicStick /></el-icon>
          {{ t.hero.startNow }}
        </el-button>
        <el-button size="large" @click="$emit('view-templates')">
          <el-icon><Folder /></el-icon>
          {{ t.hero.viewTemplates }}
        </el-button>
      </div>
      
      <div class="hero-badges">
        <el-tag type="success" effect="dark">{{ t.hero.badgeFree }}</el-tag>
        <el-tag type="warning" effect="dark">{{ t.hero.badgeAI }}</el-tag>
        <el-tag effect="dark">{{ t.hero.badgeMultiModel }}</el-tag>
      </div>
    </div>
    
    <div class="hero-visual">
      <div class="floating-card card-1">
        <el-icon><ChatDotRound /></el-icon>
        <span>ChatGPT</span>
      </div>
      <div class="floating-card card-2">
        <el-icon><Cpu /></el-icon>
        <span>Claude</span>
      </div>
      <div class="floating-card card-3">
        <el-icon><SetUp /></el-icon>
        <span>Gemini</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { MagicStick, Folder, ChatDotRound, Cpu, SetUp } from '@element-plus/icons-vue'

const appStore = useAppStore()
const t = computed(() => appStore.t)

defineEmits<{
  start: []
  'view-templates': []
}>()
</script>

<style scoped>
.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60px 40px;
  max-width: 1200px;
  margin: 0 auto;
  gap: 40px;
}

.hero-content {
  flex: 1;
  max-width: 600px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 20px;
}

.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 24px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  font-weight: 500;
}

.hero-desc {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.cta-btn {
  padding: 12px 32px;
  font-size: 18px;
  font-weight: 600;
}

.hero-badges {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-visual {
  flex: 1;
  position: relative;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-card {
  position: absolute;
  background: var(--card-bg);
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  animation: float 3s ease-in-out infinite;
}

.card-1 {
  top: 20px;
  left: 20px;
  animation-delay: 0s;
}

.card-2 {
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  animation-delay: 1s;
}

.card-3 {
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding: 40px 20px;
  }
  
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 18px;
  }
  
  .hero-actions {
    flex-direction: column;
  }
  
  .hero-visual {
    display: none;
  }
}
</style>
