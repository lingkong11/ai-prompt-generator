<template>
  <div class="app-container">
    <!-- 头部 -->
    <el-header class="header">
      <div class="logo" @click="router.push('/')">
        <el-icon :size="28"><MagicStick /></el-icon>
        <span>AI Prompt Generator</span>
      </div>
      <div class="header-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="🔮 提示词生成" name="generate" />
          <el-tab-pane label="📁 提示词管理" name="prompts" />
          <el-tab-pane label="📂 分类管理" name="categories" />
          <el-tab-pane label="⭐ 收藏" name="favorites" />
        </el-tabs>
      </div>
      <div class="user-info">
        <el-dropdown @command="handleUserCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :icon="UserFilled" />
            <span class="username">{{ authStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主内容：HomeView -->
    <el-main class="main">
      <HomeView />
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import HomeView from '@/views/HomeView.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 根据query参数确定当前Tab
const tabNames = ['generate', 'prompts', 'categories', 'favorites']
const activeTab = computed(() => {
  const tab = (route.query.tab as string) || 'generate'
  return tabNames.includes(tab) ? tab : 'generate'
})

const handleTabChange = (tab: string) => {
  router.push({ path: '/', query: { tab } })
}

const handleUserCommand = (cmd: string) => {
  if (cmd === 'logout') {
    authStore.logout()
    router.push('/login')
  }
  if (cmd === 'profile') {
    ElMessage.info('个人资料功能开发中...')
  }
}
</script>

<style scoped>
.app-container { height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; flex-direction: column; }

.header { background: rgba(255,255,255,0.95); display: flex; align-items: center; padding: 0 24px; gap: 24px; }

.logo { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: bold; color: #667eea; cursor: pointer; flex-shrink: 0; }

.header-tabs { flex: 1; }

.user-info { flex-shrink: 0; }

.user-dropdown { display: flex; align-items: center; gap: 8px; cursor: pointer; }

.username { font-size: 14px; }

.main { padding: 24px; flex: 1; overflow-y: auto; background: transparent; }
</style>
