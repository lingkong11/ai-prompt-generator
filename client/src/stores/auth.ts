import { defineStore } from 'pinia'
import { ref } from 'vue'
import { register, login, getCurrentUser, clearToken, setToken } from '@/api/auth'

/**
 * 认证状态管理
 *
 * 管理登录态、Token 持久化、用户信息缓存、游客引导弹窗。
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('prompt_token'))
  const user = ref<any>(null)
  const isLoggedIn = ref(false)

  // ========== 游客引导弹窗状态 ==========
  const showAuthModal = ref(false)
  const authModalMode = ref<'login' | 'register'>('login')
  const pendingAction = ref<(() => void) | null>(null)

  /** 应用启动时恢复登录态 */
  const init = () => {
    const savedToken = localStorage.getItem('prompt_token')
    if (savedToken) {
      token.value = savedToken
      isLoggedIn.value = true
      fetchCurrentUser()
    }
  }

  /** 从后端拉取当前用户信息 */
  const fetchCurrentUser = async () => {
    try {
      const res = await getCurrentUser()
      user.value = res.data.data
      isLoggedIn.value = true
    } catch {
      logout()
    }
  }

  /** 注册并自动登录 */
  const registerUser = async (data: { username: string; email: string; password: string; nickname?: string }) => {
    const res = await register(data)
    const payload = res.data.data
    token.value = payload.token
    user.value = payload.user
    setToken(payload.token)
    isLoggedIn.value = true
    onAuthSuccess()
    return payload
  }

  /** 登录 */
  const loginUser = async (data: { username: string; password: string }) => {
    const res = await login(data)
    const payload = res.data.data
    token.value = payload.token
    user.value = payload.user
    setToken(payload.token)
    isLoggedIn.value = true
    onAuthSuccess()
    return payload
  }

  /** 退出登录 */
  const logout = () => {
    token.value = null
    user.value = null
    isLoggedIn.value = false
    clearToken()
  }

  // ========== 游客引导方法 ==========

  /**
   * 需要登录才能执行的操作
   * @param action 登录成功后要执行的回调
   */
  const requireAuth = (action: () => void) => {
    if (isLoggedIn.value) {
      action()
    } else {
      pendingAction.value = action
      authModalMode.value = 'login'
      showAuthModal.value = true
    }
  }

  /** 打开登录弹窗 */
  const openLoginModal = () => {
    authModalMode.value = 'login'
    showAuthModal.value = true
  }

  /** 打开注册弹窗 */
  const openRegisterModal = () => {
    authModalMode.value = 'register'
    showAuthModal.value = true
  }

  /** 关闭认证弹窗 */
  const closeAuthModal = () => {
    showAuthModal.value = false
    pendingAction.value = null
  }

  /** 认证成功回调 - 执行待执行的操作 */
  const onAuthSuccess = () => {
    showAuthModal.value = false
    if (pendingAction.value) {
      const action = pendingAction.value
      pendingAction.value = null
      // 延迟执行，确保状态更新完成
      setTimeout(action, 100)
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    init,
    fetchCurrentUser,
    registerUser,
    loginUser,
    logout,
    // 游客引导
    showAuthModal,
    authModalMode,
    requireAuth,
    openLoginModal,
    openRegisterModal,
    closeAuthModal,
  }
})
