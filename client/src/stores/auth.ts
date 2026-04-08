import { defineStore } from 'pinia'
import { ref } from 'vue'
import { register, login, getCurrentUser, clearToken, setToken } from '@/api/auth'

/**
 * 认证状态管理
 *
 * <p>管理登录态、Token 持久化、用户信息缓存。
 * 后端返回统一格式 ApiResult：{ code, message, data }，data 中包含业务数据。</p>
 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('prompt_token'))
  const user = ref<any>(null)
  const isLoggedIn = ref(false)

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
      // ApiResult: { code: 0, data: { id, username, ... } }
      user.value = res.data.data
      isLoggedIn.value = true
    } catch {
      logout()
    }
  }

  /** 注册并自动登录 */
  const registerUser = async (data: { username: string; email: string; password: string; nickname?: string }) => {
    const res = await register(data)
    // ApiResult: { code: 0, data: { token, user } }
    const payload = res.data.data
    token.value = payload.token
    user.value = payload.user
    setToken(payload.token)
    isLoggedIn.value = true
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
    return payload
  }

  /** 退出登录 */
  const logout = () => {
    token.value = null
    user.value = null
    isLoggedIn.value = false
    clearToken()
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
  }
})
