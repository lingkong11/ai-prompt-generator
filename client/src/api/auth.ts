/**
 * axios 实例 & 全局拦截器配置
 *
 * <p>所有 API 请求通过此模块创建的 axios 实例发出。
 * 自动附加 Authorization 头，401 时自动清除登录态并跳转登录页。</p>
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'

const API_BASE = '/api'

/**
 * 创建专用 axios 实例，隔离全局默认配置
 */
const http = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
})

// ========== Token 管理 ==========

export const getToken = () => localStorage.getItem('prompt_token')

export const setToken = (token: string) => {
  localStorage.setItem('prompt_token', token)
  http.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

export const clearToken = () => {
  localStorage.removeItem('prompt_token')
  delete http.defaults.headers.common['Authorization']
}

/** 初始化时恢复 Token（应用启动时调用一次） */
export const initAxios = () => {
  const token = getToken()
  if (token) {
    http.defaults.headers.common['Authorization'] = `Bearer ${token}`
  }
}

// ========== 响应拦截器（延迟导入 router 避免循环依赖） ==========

let _routerPush: ((path: string) => void) | null = null

/**
 * 由 main.ts 调用一次，注入 router 实例的 push 方法
 */
export const setRouter = (push: (path: string) => void) => {
  _routerPush = push
}

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const status = error.response.status

      if (status === 401) {
        clearToken()
        _routerPush?.('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        ElMessage.error('无权限访问')
      } else if (status >= 500) {
        ElMessage.error('服务器异常，请稍后重试')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络')
    } else {
      ElMessage.error('网络异常')
    }

    return Promise.reject(error)
  },
)

// ========== API 方法 ==========

/** 用户注册 */
export const register = (data: {
  username: string
  email: string
  password: string
  nickname?: string
}) => http.post('/auth/register', data)

/** 用户登录 */
export const login = (data: { username: string; password: string }) =>
  http.post('/auth/login', data)

/** 获取当前登录用户信息 */
export const getCurrentUser = () => http.get('/auth/me')

/** 更新个人资料（昵称 / 头像） */
export const updateProfile = (data: { nickname?: string; avatar?: string }) =>
  http.put('/auth/profile', data)

/** 退出登录（纯前端清理） */
export const logout = () => {
  clearToken()
}

export default http
