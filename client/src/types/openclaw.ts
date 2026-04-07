// OpenClaw 类型定义
export interface OpenClawPrompt {
  id: number
  code: string
  name: string
  icon: string
  category: string
  stage: string
  description: string
  content: string
  useCount: number
  rating: number
  ratingCount: number
  featured: boolean
  createdAt: string
}

export interface OpenClawPromptsResponse {
  prompts: OpenClawPrompt[]
  total: number
  categories: string[]
  stages: string[]
}

export interface UsePromptResponse {
  success: boolean
  code: string
  name: string
  useCount: number
  content: string
}

export interface HealthResponse {
  status: string
  service: string
  timestamp: number
}
