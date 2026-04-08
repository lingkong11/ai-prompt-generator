<template>
  <el-dialog
    v-model="authStore.showAuthModal"
    :title="authStore.authModalMode === 'login' ? '登录后即可生成提示词' : '注册账号，解锁全部功能'"
    width="440px"
    :close-on-click-modal="false"
    @close="authStore.closeAuthModal()"
    class="auth-modal"
  >
    <!-- 标题区 -->
    <div class="modal-header">
      <el-icon :size="40" color="#667eea"><MagicStick /></el-icon>
      <h3>AI Prompt Generator</h3>
      <p>注册账号，解锁保存、收藏、管理等全部功能</p>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeMode" @tab-change="switchMode">
      <el-tab-pane label="登录" name="login" />
      <el-tab-pane label="注册" name="register" />
    </el-tabs>

    <!-- 登录表单 -->
    <el-form v-if="activeMode === 'login'" ref="loginFormRef" :model="loginForm" :rules="loginRules" @keyup.enter="handleLogin">
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" clearable />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 注册表单 -->
    <el-form v-else ref="regFormRef" :model="regForm" :rules="regRules" @keyup.enter="handleRegister">
      <el-form-item prop="username">
        <el-input v-model="regForm.username" placeholder="用户名" prefix-icon="User" clearable />
      </el-form-item>
      <el-form-item prop="email">
        <el-input v-model="regForm.email" placeholder="邮箱" prefix-icon="Message" clearable />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="regForm.password" type="password" placeholder="密码（6位以上）" prefix-icon="Lock" show-password />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="regForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
          注册
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 底部提示 -->
    <div class="modal-footer">
      <el-divider>或者</el-divider>
      <p>游客也可浏览模板库和 OpenClaw 专区</p>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const activeMode = ref<'login' | 'register'>('login')
const loading = ref(false)
const loginFormRef = ref()
const regFormRef = ref()

// 同步 store 的 mode
watch(() => authStore.authModalMode, (mode) => {
  activeMode.value = mode
}, { immediate: true })

const switchMode = (mode: string) => {
  activeMode.value = mode as 'login' | 'register'
}

// 登录表单
const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.loginUser(loginForm)
    ElMessage.success('登录成功，欢迎回来！')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 注册表单
const regForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })
const regRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名3-20个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email' as const, message: '请输入有效邮箱', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== regForm.password) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

const handleRegister = async () => {
  const valid = await regFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.registerUser({
      username: regForm.username,
      email: regForm.email,
      password: regForm.password,
    })
    ElMessage.success('注册成功，已自动登录！')
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-header {
  text-align: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 12px 0 4px;
  font-size: 20px;
  color: #667eea;
}

.modal-header p {
  color: #999;
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  height: 42px;
  font-size: 16px;
}

.modal-footer {
  text-align: center;
}

.modal-footer p {
  color: #999;
  font-size: 12px;
}
</style>
