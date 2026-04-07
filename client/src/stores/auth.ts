import { defineStore } from 'pinia'
import { ref } from 'vue'
import { register, login, getCurrentUser, clearToken, setToken } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('prompt_token'))
  const user = ref<any>(null)
  const isLoggedIn = ref(false)

  // 初始化登录状态
  const init = () => {
    const savedToken = localStorage.getItem('prompt_token')
    if (savedToken) {
      token.value = savedToken
      isLoggedIn.value = true
      fetchCurrentUser()
    }
  }

  // 获取当前用户
  const fetchCurrentUser = async () => {
    try {
      const res = await getCurrentUser()
      user.value = res.data
      isLoggedIn.value = true
    } catch (e) {
      logout()
    }
  }

  // 注册
  const registerUser = async (data: { username: string; email: string; password: string; nickname?: string }) => {
    const res = await register(data)
    const { token: newToken, user: newUser } = res.data
    token.value = newToken
    user.value = newUser
    setToken(newToken)
    isLoggedIn.value = true
    return res.data
  }

  // 登录
  const loginUser = async (data: { username: string; password: string }) => {
    const res = await login(data)
    const { token: newToken, user: newUser } = res.data
    token.value = newToken
    user.value = newUser
    setToken(newToken)
    isLoggedIn.value = true
    return res.data
  }

  // 退出
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
    logout
  }
})
