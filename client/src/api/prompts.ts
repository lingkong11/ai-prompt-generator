import axios from 'axios'

const API_BASE = '/api'

// 创建提示词
export const createPrompt = (data: {
  title: string
  content: string
  type?: string
  style?: string
  language?: string
  goal?: string
  tags?: string
  categoryId?: number
}) => {
  return axios.post(`${API_BASE}/prompts`, data)
}

// 获取提示词列表
export const getPrompts = (params?: {
  page?: number
  size?: number
  type?: string
  keyword?: string
}) => {
  return axios.get(`${API_BASE}/prompts`, { params })
}

// 获取单个提示词
export const getPrompt = (id: number) => {
  return axios.get(`${API_BASE}/prompts/${id}`)
}

// 更新提示词
export const updatePrompt = (id: number, data: any) => {
  return axios.put(`${API_BASE}/prompts/${id}`, data)
}

// 删除提示词
export const deletePrompt = (id: number) => {
  return axios.delete(`${API_BASE}/prompts/${id}`)
}

// 切换收藏
export const toggleFavorite = (id: number) => {
  return axios.post(`${API_BASE}/prompts/${id}/favorite`)
}

// 获取收藏列表
export const getFavorites = () => {
  return axios.get(`${API_BASE}/prompts/favorites`)
}

// 增加使用次数
export const incrementUseCount = (id: number) => {
  return axios.post(`${API_BASE}/prompts/${id}/use`)
}

// 生成提示词（旧接口，Phase 0）
export const generatePrompt = (data: {
  goal: string
  type: string
  style?: string
  language?: string
}) => {
  return axios.post(`${API_BASE}/prompt/generate`, data)
}

// 获取模板
export const getTemplates = () => {
  return axios.get(`${API_BASE}/prompt/templates`)
}

// 获取分类列表
export const getCategories = () => {
  return axios.get(`${API_BASE}/categories`)
}

// 创建分类
export const createCategory = (data: { name: string; icon?: string; description?: string }) => {
  return axios.post(`${API_BASE}/categories`, data)
}

// 更新分类
export const updateCategory = (id: number, data: any) => {
  return axios.put(`${API_BASE}/categories/${id}`, data)
}

// 删除分类
export const deleteCategory = (id: number) => {
  return axios.delete(`${API_BASE}/categories/${id}`)
}
