<template>
  <div class="app-container">
    <!-- 头部 -->
    <el-header class="header">
      <div class="logo">
        <el-icon :size="28"><MagicStick /></el-icon>
        <span>AI Prompt Generator</span>
      </div>
      <div class="header-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="🔮 提示词生成" name="generate" />
          <el-tab-pane label="📁 模板库" name="templates" />
          <el-tab-pane label="🤖 OpenClaw专用" name="openclaw" />
          <el-tab-pane label="⭐ 收藏" name="favorites" />
        </el-tabs>
      </div>
    </el-header>
    
    <!-- 主内容 -->
    <el-main class="main">
      <!-- 提示词生成页面 -->
      <div v-if="activeTab === 'generate'" class="generate-page">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-card class="input-card">
              <template #header>
                <span>📝 输入你的目标</span>
              </template>
              <el-form label-width="80px">
                <el-form-item label="目标描述">
                  <el-input
                    v-model="form.goal"
                    type="textarea"
                    :rows="4"
                    placeholder="例如：写一篇关于AI技术发展的文章"
                  />
                </el-form-item>
                <el-form-item label="提示词类型">
                  <el-select v-model="form.type" placeholder="选择类型">
                    <el-option label="通用" value="general" />
                    <el-option label="写作" value="writing" />
                    <el-option label="编程" value="coding" />
                    <el-option label="分析" value="analysis" />
                    <el-option label="🤖 Agent工作流" value="agent" />
                  </el-select>
                </el-form-item>
                <el-form-item label="风格">
                  <el-select v-model="form.style" placeholder="选择风格">
                    <el-option label="专业" value="专业" />
                    <el-option label="简洁" value="简洁" />
                    <el-option label="风趣" value="风趣" />
                    <el-option label="技术" value="技术" />
                  </el-select>
                </el-form-item>
                <el-form-item label="输出语言">
                  <el-radio-group v-model="form.language">
                    <el-radio label="zh">中文</el-radio>
                    <el-radio label="en">English</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="generatePrompt" :loading="loading">
                    <el-icon><MagicStick /></el-icon>
                    生成提示词
                  </el-button>
                  <el-button @click="resetForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="output-card">
              <template #header>
                <span>✨ 生成结果</span>
              </template>
              <div v-if="result" class="result-content">
                <pre>{{ result }}</pre>
                <div class="result-actions">
                  <el-button type="primary" @click="copyResult">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                  <el-button @click="favoriteResult">
                    <el-icon><Star /></el-icon>
                    收藏
                  </el-button>
                </div>
              </div>
              <el-empty v-else description="点击生成按钮获取提示词" />
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 模板库页面 -->
      <div v-if="activeTab === 'templates'" class="templates-page">
        <el-row :gutter="20">
          <el-col :span="8" v-for="tpl in templates" :key="tpl.name">
            <el-card class="template-card" shadow="hover" @click="useTemplate(tpl)">
              <div class="template-icon">{{ getCategoryIcon(tpl.category) }}</div>
              <div class="template-name">{{ tpl.name }}</div>
              <div class="template-desc">{{ tpl.description }}</div>
              <div class="template-tags">
                <el-tag v-for="tag in tpl.tags.split(',')" :key="tag" size="small">
                  {{ tag }}
                </el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- OpenClaw专用页面 -->
      <div v-if="activeTab === 'openclaw'" class="openclaw-page">
        <el-alert
          title="🤖 OpenClaw AI Agent 专用提示词"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          这是专为您（马可行）设计的九阶段工作流提示词，支持完整的项目开发闭环。
        </el-alert>
        
        <el-row :gutter="20">
          <el-col :span="8" v-for="(item, index) in openclawTemplates" :key="index">
            <el-card class="openclaw-card" shadow="hover" @click="useOpenClawTemplate(item)">
              <div class="oc-icon">{{ item.icon }}</div>
              <div class="oc-title">{{ item.title }}</div>
              <div class="oc-desc">{{ item.description }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 收藏页面 -->
      <div v-if="activeTab === 'favorites'" class="favorites-page">
        <el-empty v-if="favorites.length === 0" description="暂无收藏" />
        <el-card v-else v-for="fav in favorites" :key="fav.id" class="fav-card">
          <div class="fav-content">
            <pre>{{ fav.content }}</pre>
            <div class="fav-actions">
              <el-button size="small" @click="copyFavorite(fav)">复制</el-button>
              <el-button size="small" type="danger" @click="removeFavorite(fav)">删除</el-button>
            </div>
          </div>
        </el-card>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const activeTab = ref('generate')
const loading = ref(false)
const result = ref('')
const favorites = ref<any[]>([])

const form = reactive({
  goal: '',
  type: 'general',
  style: '专业',
  language: 'zh'
})

const templates = ref([
  { name: 'OpenClaw九阶段工作流', category: 'openclaw', description: 'AI Agent标准九阶段开发流程', tags: 'openclaw,workflow,九阶段' },
  { name: '文章写作助手', category: 'writing', description: '专业文章写作提示词', tags: '写作,文章' },
  { name: '代码开发助手', category: 'coding', description: '编程问题解决提示词', tags: '编程,代码' },
  { name: '数据分析助手', category: 'analysis', description: '数据分析和报告提示词', tags: '分析,数据' },
  { name: '翻译助手', category: 'general', description: '多语言翻译提示词', tags: '翻译,语言' },
])

const openclawTemplates = [
  { icon: '📋', title: '需求分析', description: '用户需求分析提示词' },
  { icon: '🎨', title: '技术设计', description: '技术方案设计提示词' },
  { icon: '💻', title: '代码开发', description: '代码编写实现提示词' },
  { icon: '🧪', title: '功能测试', description: '测试用例与执行提示词' },
  { icon: '📊', title: '项目管理', description: '进度与风险追踪提示词' },
  { icon: '📚', title: '文档输出', description: '技术文档生成提示词' },
  { icon: '🚀', title: '项目交付', description: '交付与部署提示词' },
]

const generatePrompt = async () => {
  if (!form.goal) {
    ElMessage.warning('请输入目标描述')
    return
  }
  
  loading.value = true
  try {
    const response = await axios.post('http://localhost:8081/api/prompt/generate', form)
    result.value = response.data.prompt
    ElMessage.success('提示词生成成功')
  } catch (error: any) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.goal = ''
  result.value = ''
}

const copyResult = () => {
  navigator.clipboard.writeText(result.value)
  ElMessage.success('已复制到剪贴板')
}

const favoriteResult = () => {
  if (!result.value) return
  favorites.value.push({
    id: Date.now(),
    content: result.value,
    type: form.type
  })
  ElMessage.success('已收藏')
}

const useTemplate = (tpl: any) => {
  form.type = tpl.category
  activeTab.value = 'generate'
}

const useOpenClawTemplate = (item: any) => {
  form.type = 'agent'
  form.goal = item.title + '任务'
  activeTab.value = 'generate'
}

const copyFavorite = (fav: any) => {
  navigator.clipboard.writeText(fav.content)
  ElMessage.success('已复制')
}

const removeFavorite = (fav: any) => {
  favorites.value = favorites.value.filter(f => f.id !== fav.id)
}

const handleTabChange = (tab: string) => {
  console.log('Tab changed:', tab)
}

onMounted(() => {
  // 加载模板
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255,255,255,0.95);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

.main {
  padding: 24px;
  background: transparent;
}

.input-card, .output-card {
  height: 100%;
  min-height: 400px;
}

.result-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  max-height: 350px;
  overflow-y: auto;
}

.result-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.template-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.template-card:hover {
  transform: translateY(-4px);
}

.template-icon {
  font-size: 32px;
  text-align: center;
  margin-bottom: 12px;
}

.template-name {
  font-weight: bold;
  text-align: center;
  margin-bottom: 8px;
}

.template-desc {
  font-size: 12px;
  color: #666;
  text-align: center;
  margin-bottom: 12px;
}

.template-tags {
  display: flex;
  justify-content: center;
  gap: 4px;
}

.openclaw-card {
  margin-bottom: 20px;
  cursor: pointer;
  text-align: center;
  padding: 20px;
}

.oc-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.oc-title {
  font-weight: bold;
  margin-bottom: 8px;
}

.oc-desc {
  font-size: 12px;
  color: #666;
}

.fav-card {
  margin-bottom: 16px;
}

.fav-content pre {
  white-space: pre-wrap;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.fav-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
</style>