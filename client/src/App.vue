<template>
  <div class="app-container">
    <!-- 未登录：跳转登录页 -->
    <div v-if="!authStore.isLoggedIn" class="auth-loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <!-- 已登录：主框架 -->
    <template v-else>
      <!-- 顶部导航 -->
      <el-header class="app-header">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="28"><MagicStick /></el-icon>
          <span>AI Prompt Generator</span>
        </div>

        <div class="nav-tabs">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="🔮 提示词生成" name="generate" />
            <el-tab-pane label="📁 提示词管理" name="prompts" />
            <el-tab-pane label="📂 分类管理" name="categories" />
            <el-tab-pane label="⭐ 收藏" name="favorites" />
            <el-tab-pane label="🤖 OpenClaw" name="openclaw" />
          </el-tabs>
        </div>

        <div class="user-area">
          <el-dropdown @command="handleUserCommand">
            <span class="user-trigger">
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

      <!-- 页面内容 -->
      <el-main class="app-main">
        <HomeView v-if="activeTab !== 'openclaw'" :active-tab="activeTab" />
        <OpenClawView v-else />
      </el-main>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import HomeView from '@/views/HomeView.vue'
import OpenClawView from '@/views/OpenClawView.vue'

const router = useRouter()
const authStore = useAuthStore()
const activeTab = ref('generate')

const handleTabChange = (tab: string) => {
  // 切换到OpenClaw时路由跳转
  if (tab === 'openclaw') {
    router.push('/openclaw')
  }
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

// 监听路由变化同步tab
router.afterEach((to) => {
  if (to.path === '/openclaw') {
    activeTab.value = 'openclaw'
  } else if (to.path === '/') {
    const tab = (to.query.tab as string) || 'generate'
    activeTab.value = tab
  }
})

onMounted(() => {
  authStore.init()
  if (!authStore.isLoggedIn) {
    router.push('/login')
  }
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
}

.auth-loading {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: white;
  font-size: 18px;
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #667eea;
  cursor: pointer;
  flex-shrink: 0;
}

.nav-tabs {
  flex: 1;
}

.user-area {
  flex-shrink: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.app-main {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
  background: transparent;
}
</style>
