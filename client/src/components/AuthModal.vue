<template>
  <el-dialog
    v-model="authStore.showAuthModal"
    :title="authStore.authModalMode === 'login' ? t.auth.loginTitle : t.auth.registerTitle"
    width="440px"
    :close-on-click-modal="false"
    @close="authStore.closeAuthModal()"
    class="auth-modal"
  >
    <div class="modal-header">
      <el-icon :size="40" color="#667eea"><MagicStick /></el-icon>
      <h3>{{ t.app.title }}</h3>
      <p>{{ t.auth.subtitle }}</p>
    </div>

    <el-tabs v-model="activeMode" @tab-change="switchMode">
      <el-tab-pane :label="t.auth.loginTab" name="login" />
      <el-tab-pane :label="t.auth.registerTab" name="register" />
    </el-tabs>

    <!-- Login -->
    <el-form v-if="activeMode === 'login'" ref="loginFormRef" :model="loginForm" :rules="loginRules" @keyup.enter="handleLogin">
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" :placeholder="t.auth.usernamePlaceholder" prefix-icon="User" clearable />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" :placeholder="t.auth.passwordPlaceholder" prefix-icon="Lock" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">{{ t.auth.loginBtn }}</el-button>
      </el-form-item>
    </el-form>

    <!-- Register -->
    <el-form v-else ref="regFormRef" :model="regForm" :rules="regRules" @keyup.enter="handleRegister">
      <el-form-item prop="username">
        <el-input v-model="regForm.username" :placeholder="t.auth.usernamePlaceholder" prefix-icon="User" clearable />
      </el-form-item>
      <el-form-item prop="email">
        <el-input v-model="regForm.email" :placeholder="t.auth.emailPlaceholder" prefix-icon="Message" clearable />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="regForm.password" type="password" :placeholder="t.auth.passwordPlaceholder" prefix-icon="Lock" show-password />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="regForm.confirmPassword" type="password" :placeholder="t.auth.confirmPasswordPlaceholder" prefix-icon="Lock" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">{{ t.auth.registerBtn }}</el-button>
      </el-form-item>
    </el-form>

    <div class="modal-footer">
      <el-divider>{{ t.auth.or }}</el-divider>
      <p>{{ t.auth.guestTip }}</p>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'

const authStore = useAuthStore()
const appStore = useAppStore()
const t = computed(() => appStore.t)
const activeMode = ref<'login' | 'register'>('login')
const loading = ref(false)
const loginFormRef = ref()
const regFormRef = ref()

watch(() => authStore.authModalMode, (mode) => { activeMode.value = mode }, { immediate: true })
const switchMode = (mode: string) => { activeMode.value = mode as 'login' | 'register' }

const loginForm = reactive({ username: '', password: '' })
const loginRules = computed(() => ({
  username: [{ required: true, message: t.value.auth.usernameRequired, trigger: 'blur' }],
  password: [{ required: true, message: t.value.auth.passwordRequired, trigger: 'blur' }],
}))

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try { await authStore.loginUser(loginForm); ElMessage.success(t.value.auth.loginSuccess) }
  catch (e: any) { ElMessage.error(e.response?.data?.message || t.value.auth.loginFail) }
  finally { loading.value = false }
}

const regForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })
const regRules = computed(() => ({
  username: [
    { required: true, message: t.value.auth.usernameRequired, trigger: 'blur' },
    { min: 3, max: 20, message: t.value.auth.usernameRule, trigger: 'blur' },
  ],
  email: [
    { required: true, message: t.value.auth.emailRequired, trigger: 'blur' },
    { type: 'email' as const, message: t.value.auth.emailInvalid, trigger: 'blur' },
  ],
  password: [
    { required: true, message: t.value.auth.passwordRequired, trigger: 'blur' },
    { min: 6, message: t.value.auth.passwordMin, trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: t.value.auth.passwordRequired, trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== regForm.password) callback(new Error(t.value.auth.passwordMismatch))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}))

const handleRegister = async () => {
  const valid = await regFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try { await authStore.registerUser({ username: regForm.username, email: regForm.email, password: regForm.password }); ElMessage.success(t.value.auth.registerSuccess) }
  catch (e: any) { ElMessage.error(e.response?.data?.message || t.value.auth.registerFail) }
  finally { loading.value = false }
}
</script>

<style scoped>
.modal-header { text-align: center; margin-bottom: 20px; }
.modal-header h3 { margin: 12px 0 4px; font-size: 20px; color: #667eea; }
.modal-header p { color: #999; font-size: 13px; }
.submit-btn { width: 100%; height: 42px; font-size: 16px; }
.modal-footer { text-align: center; }
.modal-footer p { color: #999; font-size: 12px; }
</style>
