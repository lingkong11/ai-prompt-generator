import axios from 'axios'

const API_BASE = '/api'

// 设置全局超时（防止请求永远挂起）
axios.defaults.timeout = 10000

// 获取本地存储的token
export const getToken = () => localStorage.getItem('prompt_token')

// 设置token
export const setToken = (token: string) => {
  localStorage.setItem('prompt_token', token)
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

// 清除token
export const clearToken = () => {
  localStorage.removeItem('prompt_token')
  delete axios.defaults.headers.common['Authorization']
}

// 初始化axios拦截器
export const initAxios = () => {
  const token = getToken()
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
  }
}

// 注册
export const register = (data: { username: string; email: string; password: string; nickname?: string }) => {
  return axios.post(`${API_BASE}/auth/register`, data)
}

// 登录
export const login = (data: { username: string; password: string }) => {
  return axios.post(`${API_BASE}/auth/login`, data)
}

// 获取当前用户
export const getCurrentUser = () => {
  return axios.get(`${API_BASE}/auth/me`)
}

// 退出登录
export const logout = () => {
  clearToken()
}
