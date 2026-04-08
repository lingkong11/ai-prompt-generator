/**
 * 提示词 & 分类 API
 *
 * <p>使用 auth.ts 导出的 http 实例，自动携带 Token 和错误拦截。
 * 所有方法返回 Promise<AxiosResponse>，调用方通过 try/catch 处理异常。</p>
 */

import http from '@/api/auth'

// ========== 提示词 CRUD ==========

export const createPrompt = (data: {
  title: string
  content: string
  type?: string
  style?: string
  language?: string
  goal?: string
  tags?: string
  categoryId?: number
}) => http.post('/prompts', data)

export const getPrompts = (params?: {
  page?: number
  size?: number
  type?: string
  keyword?: string
}) => http.get('/prompts', { params })

export const getPrompt = (id: number) => http.get(`/prompts/${id}`)

export const updatePrompt = (id: number, data: any) => http.put(`/prompts/${id}`, data)

export const deletePrompt = (id: number) => http.delete(`/prompts/${id}`)

export const toggleFavorite = (id: number) => http.post(`/prompts/${id}/favorite`)

export const getFavorites = () => http.get('/prompts/favorites')

export const incrementUseCount = (id: number) => http.post(`/prompts/${id}/use`)

// ========== Prompt 生成 ==========

export const generatePrompt = (data: {
  goal: string
  type: string
  style?: string
  language?: string
}) => http.post('/prompt/generate', data)

export const getTemplates = () => http.get('/prompt/templates')

// ========== 分类 CRUD ==========

export const getCategories = () => http.get('/categories')

export const createCategory = (data: {
  name: string
  icon?: string
  description?: string
}) => http.post('/categories', data)

export const updateCategory = (id: number, data: any) => http.put(`/categories/${id}`, data)

export const deleteCategory = (id: number) => http.delete(`/categories/${id}`)
