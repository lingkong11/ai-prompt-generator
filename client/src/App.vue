<template>
  <div class="app-container">
    <!-- 澶撮儴 -->
    <el-header class="header">
      <div class="logo" @click="router.push('/')">
        <el-icon :size="28"><MagicStick /></el-icon>
        <span>AI Prompt Generator</span>
      </div>
      <div class="header-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="馃敭 鎻愮ず璇嶇敓鎴? name="generate" />
          <el-tab-pane label="馃搧 鎻愮ず璇嶇鐞? name="prompts" />
          <el-tab-pane label="馃搨 鍒嗙被绠＄悊" name="categories" />
          <el-tab-pane label="猸?鏀惰棌" name="favorites" />
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
              <el-dropdown-item command="profile">涓汉璧勬枡</el-dropdown-item>
              <el-dropdown-item command="logout" divided>閫€鍑虹櫥褰?/el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 涓诲唴瀹癸細HomeView -->
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

// 鏍规嵁query鍙傛暟纭畾褰撳墠Tab
const tabNames = ['generate', 'prompts', 'categories', 'favorites', 'openclaw']
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
    ElMessage.info('涓汉璧勬枡鍔熻兘寮€鍙戜腑...')
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

