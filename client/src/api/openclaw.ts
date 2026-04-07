// OpenClaw 专用 API (Mock 版本)
import axios from 'axios'

const API_BASE = '/api'

// Mock 数据 - 14个OpenClaw模板
const MOCK_PROMPTS = [
  {
    id: 1,
    code: 'requirement_analysis_agent',
    name: 'Requirement Analysis Agent',
    icon: 'A',
    category: 'agent',
    stage: '1',
    description: 'Converts vague ideas into structured PRD documents',
    content: 'OpenClaw Agent Template: requirement_analysis_agent',
    useCount: 42,
    rating: 4.8,
    ratingCount: 15,
    featured: true,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 2,
    code: 'tech_design_agent',
    name: 'Tech Design Agent',
    icon: 'T',
    category: 'agent',
    stage: '2',
    description: 'Transforms PRD into executable technical design',
    content: 'OpenClaw Agent Template: tech_design_agent',
    useCount: 38,
    rating: 4.7,
    ratingCount: 12,
    featured: true,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 3,
    code: 'task_decompose_agent',
    name: 'Task Decompose Agent',
    icon: 'D',
    category: 'agent',
    stage: '3',
    description: 'Decomposes design into trackable task cards',
    content: 'OpenClaw Agent Template: task_decompose_agent',
    useCount: 35,
    rating: 4.6,
    ratingCount: 10,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 4,
    code: 'code_dev_agent',
    name: 'Code Development Agent',
    icon: 'C',
    category: 'agent',
    stage: '4',
    description: 'Full-stack engineer outputs production code',
    content: 'OpenClaw Agent Template: code_dev_agent',
    useCount: 56,
    rating: 4.9,
    ratingCount: 18,
    featured: true,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 5,
    code: 'unit_test_agent',
    name: 'Unit Test Agent',
    icon: 'U',
    category: 'agent',
    stage: '5',
    description: 'QA engineer outputs test cases',
    content: 'OpenClaw Agent Template: unit_test_agent',
    useCount: 29,
    rating: 4.5,
    ratingCount: 8,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 6,
    code: 'code_review_agent',
    name: 'Code Review Agent',
    icon: 'R',
    category: 'agent',
    stage: '6',
    description: 'Senior reviewer outputs review comments',
    content: 'OpenClaw Agent Template: code_review_agent',
    useCount: 47,
    rating: 4.8,
    ratingCount: 14,
    featured: true,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 7,
    code: 'doc_output_agent',
    name: 'Doc Output Agent',
    icon: 'O',
    category: 'agent',
    stage: '7',
    description: 'Tech doc engineer outputs manuals',
    content: 'OpenClaw Agent Template: doc_output_agent',
    useCount: 31,
    rating: 4.6,
    ratingCount: 9,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 8,
    code: 'deploy_agent',
    name: 'Deploy Agent',
    icon: 'P',
    category: 'agent',
    stage: '8',
    description: 'DevOps outputs deployment plan',
    content: 'OpenClaw Agent Template: deploy_agent',
    useCount: 33,
    rating: 4.7,
    ratingCount: 11,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 9,
    code: 'retro_agent',
    name: 'Retro Agent',
    icon: 'S',
    category: 'agent',
    stage: '9',
    description: 'PM outputs project retrospective',
    content: 'OpenClaw Agent Template: retro_agent',
    useCount: 40,
    rating: 4.8,
    ratingCount: 13,
    featured: true,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 10,
    code: 'git_commit_agent',
    name: 'Git Commit Agent',
    icon: 'G',
    category: 'tool',
    stage: 'tool',
    description: 'Conventional commits output',
    content: 'OpenClaw Agent Template: git_commit_agent',
    useCount: 68,
    rating: 4.9,
    ratingCount: 21,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 11,
    code: 'readme_agent',
    name: 'README Agent',
    icon: 'M',
    category: 'tool',
    stage: 'tool',
    description: 'Professional README generation',
    content: 'OpenClaw Agent Template: readme_agent',
    useCount: 72,
    rating: 4.9,
    ratingCount: 23,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 12,
    code: 'api_doc_agent',
    name: 'API Doc Agent',
    icon: 'K',
    category: 'tool',
    stage: 'tool',
    description: 'OpenAPI 3.0 spec output',
    content: 'OpenClaw Agent Template: api_doc_agent',
    useCount: 58,
    rating: 4.8,
    ratingCount: 17,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 13,
    code: 'test_plan_agent',
    name: 'Test Plan Agent',
    icon: 'E',
    category: 'tool',
    stage: 'tool',
    description: 'Complete test plan generation',
    content: 'OpenClaw Agent Template: test_plan_agent',
    useCount: 45,
    rating: 4.7,
    ratingCount: 14,
    featured: false,
    createdAt: '2026-04-07T12:00:00Z'
  },
  {
    id: 14,
    code: 'debug_agent',
    name: 'Debug Agent',
    icon: 'B',
    category: 'tool',
    stage: 'tool',
    description: 'Bug location and fix output',
    content: 'OpenClaw Agent Template: debug_agent',
    useCount: 51,
    rating: 4.8,
    ratingCount: 16,
    featured: true,
    createdAt: '2026-04-07T12:00:00Z'
  }
]

// Mock 延迟函数
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms))

// 获取所有OpenClaw模板
export const getOpenClawPrompts = async (params?: {
  category?: string
  stage?: string
}) => {
  await delay(300) // 模拟网络延迟
  
  let prompts = MOCK_PROMPTS
  
  if (params?.category) {
    prompts = prompts.filter(p => p.category === params.category)
  }
  
  if (params?.stage) {
    prompts = prompts.filter(p => p.stage === params.stage)
  }
  
  // 按使用次数降序排序
  prompts.sort((a, b) => b.useCount - a.useCount)
  
  return {
    data: {
      prompts,
      total: prompts.length,
      categories: ['agent', 'tool'],
      stages: ['1', '2', '3', '4', '5', '6', '7', '8', '9', 'tool']
    }
  }
}

// 获取单个模板
export const getOpenClawPrompt = async (code: string) => {
  await delay(200)
  
  const prompt = MOCK_PROMPTS.find(p => p.code === code)
  
  if (!prompt) {
    throw new Error(`Template not found: ${code}`)
  }
  
  return { data: prompt }
}

// 使用模板（增加使用次数）
export const useOpenClawPrompt = async (code: string) => {
  await delay(200)
  
  const prompt = MOCK_PROMPTS.find(p => p.code === code)
  
  if (!prompt) {
    throw new Error(`Template not found: ${code}`)
  }
  
  // 模拟增加使用次数
  prompt.useCount += 1
  
  return {
    data: {
      success: true,
      code,
      name: prompt.name,
      useCount: prompt.useCount,
      content: prompt.content
    }
  }
}

// 获取分类列表
export const getOpenClawCategories = async () => {
  await delay(100)
  return { data: ['agent', 'tool'] }
}

// 获取阶段列表
export const getOpenClawStages = async () => {
  await delay(100)
  return { data: ['1', '2', '3', '4', '5', '6', '7', '8', '9', 'tool'] }
}

// 健康检查
export const getOpenClawHealth = async () => {
  await delay(100)
  return {
    data: {
      status: 'UP',
      service: 'OpenClaw Prompt API (Mock)',
      timestamp: Date.now()
    }
  }
}
