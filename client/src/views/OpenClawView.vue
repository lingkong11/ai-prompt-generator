<template>
  <div class="openclaw-container">
    <div class="header">
      <h1>OpenClaw Agent Templates</h1>
      <p class="subtitle">14 specialized agents for AI-powered software engineering workflows</p>
    </div>

    <div class="controls">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select v-model="selectedCategory" placeholder="Filter by Category" clearable @change="filterPrompts">
            <el-option label="All Categories" value="" />
            <el-option label="Agent (9 Stages)" value="agent" />
            <el-option label="Tool" value="tool" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-select v-model="selectedStage" placeholder="Filter by Stage" clearable @change="filterPrompts">
            <el-option label="All Stages" value="" />
            <el-option v-for="stage in stages" :key="stage" :label="getStageLabel(stage)" :value="stage" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="Search templates..."
            clearable
            @input="filterPrompts"
            @clear="filterPrompts"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
      </el-row>
    </div>

    <div class="stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="Total Templates" :value="prompts.length" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="Total Uses" :value="totalUses" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="Featured" :value="featuredCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="Avg Rating" :value="averageRating" :precision="1" />
        </el-col>
      </el-row>
    </div>

    <div class="prompts-grid">
      <el-row :gutter="20">
        <el-col
          v-for="prompt in filteredPrompts"
          :key="prompt.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          class="prompt-card-col"
        >
          <el-card class="prompt-card" :class="{ featured: prompt.featured }" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="prompt-icon">{{ prompt.icon }}</div>
                <div>
                  <h3>{{ prompt.name }}</h3>
                  <div class="prompt-meta">
                    <el-tag :type="prompt.category === 'agent' ? 'primary' : 'success'" size="small">
                      {{ prompt.category }}
                    </el-tag>
                    <el-tag v-if="prompt.stage !== 'tool'" type="info" size="small">
                      Stage {{ prompt.stage }}
                    </el-tag>
                    <el-tag v-else type="warning" size="small">
                      Tool
                    </el-tag>
                  </div>
                </div>
              </div>
            </template>

            <div class="prompt-content">
              <p class="description">{{ prompt.description }}</p>
              
              <div class="prompt-stats">
                <div class="stat-item">
                  <el-icon><View /></el-icon>
                  <span>{{ prompt.useCount }} uses</span>
                </div>
                <div class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ prompt.rating }} ({{ prompt.ratingCount }})</span>
                </div>
              </div>

              <div class="prompt-actions">
                <el-button type="primary" size="small" @click="usePrompt(prompt.code)">
                  <el-icon><MagicStick /></el-icon>
                  Use Template
                </el-button>
                <el-button type="info" size="small" @click="viewDetails(prompt.code)">
                  <el-icon><View /></el-icon>
                  Details
                </el-button>
              </div>
            </div>

            <div v-if="prompt.featured" class="featured-badge">
              <el-icon><Star /></el-icon>
              Featured
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="filteredPrompts.length === 0" class="empty-state">
      <el-empty description="No templates found matching your criteria" />
    </div>

    <!-- 模板详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="selectedPrompt?.name"
      width="600px"
    >
      <div v-if="selectedPrompt" class="prompt-detail">
        <div class="detail-header">
          <div class="detail-icon">{{ selectedPrompt.icon }}</div>
          <div>
            <h2>{{ selectedPrompt.name }}</h2>
            <div class="detail-meta">
              <el-tag :type="selectedPrompt.category === 'agent' ? 'primary' : 'success'">
                {{ selectedPrompt.category }}
              </el-tag>
              <el-tag v-if="selectedPrompt.stage !== 'tool'" type="info">
                Stage {{ selectedPrompt.stage }}
              </el-tag>
              <el-tag v-else type="warning">
                Tool
              </el-tag>
              <el-tag v-if="selectedPrompt.featured" type="danger">
                <el-icon><Star /></el-icon>
                Featured
              </el-tag>
            </div>
          </div>
        </div>

        <div class="detail-content">
          <h3>Description</h3>
          <p>{{ selectedPrompt.description }}</p>

          <h3>Template Content</h3>
          <el-input
            v-model="selectedPrompt.content"
            type="textarea"
            :rows="6"
            readonly
          />

          <div class="detail-stats">
            <el-statistic title="Uses" :value="selectedPrompt.useCount" />
            <el-statistic title="Rating" :value="selectedPrompt.rating" :precision="1" />
            <el-statistic title="Ratings Count" :value="selectedPrompt.ratingCount" />
            <el-statistic title="Created" :value="formatDate(selectedPrompt.createdAt)" />
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">Close</el-button>
        <el-button type="primary" @click="usePrompt(selectedPrompt?.code || '')">
          Use This Template
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Search,
  View,
  Star,
  MagicStick
} from '@element-plus/icons-vue'
import {
  getOpenClawPrompts,
  getOpenClawPrompt,
  useOpenClawPrompt,
  getOpenClawCategories,
  getOpenClawStages
} from '@/api/openclaw'
import type { OpenClawPrompt } from '@/types/openclaw'
import { ElMessage, ElMessageBox } from 'element-plus'

// 状态
const prompts = ref<OpenClawPrompt[]>([])
const filteredPrompts = ref<OpenClawPrompt[]>([])
const categories = ref<string[]>([])
const stages = ref<string[]>([])
const selectedCategory = ref('')
const selectedStage = ref('')
const searchQuery = ref('')
const detailDialogVisible = ref(false)
const selectedPrompt = ref<OpenClawPrompt | null>(null)

// 计算属性
const totalUses = computed(() => {
  return prompts.value.reduce((sum, prompt) => sum + prompt.useCount, 0)
})

const featuredCount = computed(() => {
  return prompts.value.filter(p => p.featured).length
})

const averageRating = computed(() => {
  const total = prompts.value.reduce((sum, prompt) => sum + prompt.rating, 0)
  return prompts.value.length > 0 ? total / prompts.value.length : 0
})

// 方法
const loadPrompts = async () => {
  try {
    const response = await getOpenClawPrompts()
    prompts.value = response.data.prompts
    filteredPrompts.value = prompts.value
  } catch (error) {
    console.error('Failed to load prompts:', error)
    ElMessage.error('Failed to load templates')
  }
}

const loadCategories = async () => {
  try {
    const response = await getOpenClawCategories()
    categories.value = response.data
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadStages = async () => {
  try {
    const response = await getOpenClawStages()
    stages.value = response.data
  } catch (error) {
    console.error('Failed to load stages:', error)
  }
}

const filterPrompts = () => {
  let filtered = prompts.value

  if (selectedCategory.value) {
    filtered = filtered.filter(p => p.category === selectedCategory.value)
  }

  if (selectedStage.value) {
    filtered = filtered.filter(p => p.stage === selectedStage.value)
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(p =>
      p.name.toLowerCase().includes(query) ||
      p.description.toLowerCase().includes(query) ||
      p.code.toLowerCase().includes(query)
    )
  }

  // 按使用次数排序
  filtered.sort((a, b) => b.useCount - a.useCount)
  filteredPrompts.value = filtered
}

const getStageLabel = (stage: string) => {
  if (stage === 'tool') return 'Tool'
  return `Stage ${stage}`
}

const usePrompt = async (code: string) => {
  try {
    const response = await useOpenClawPrompt(code)
    ElMessage.success(`Used "${response.data.name}" (Total uses: ${response.data.useCount})`)
    
    // 更新本地数据
    const prompt = prompts.value.find(p => p.code === code)
    if (prompt) {
      prompt.useCount = response.data.useCount
      filterPrompts()
    }
  } catch (error) {
    console.error('Failed to use prompt:', error)
    ElMessage.error('Failed to use template')
  }
}

const viewDetails = async (code: string) => {
  try {
    const response = await getOpenClawPrompt(code)
    selectedPrompt.value = response.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('Failed to load prompt details:', error)
    ElMessage.error('Failed to load template details')
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

// 生命周期
onMounted(() => {
  loadPrompts()
  loadCategories()
  loadStages()
})
</script>

<style scoped>
.openclaw-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  margin-bottom: 10px;
  color: #333;
}

.subtitle {
  color: #666;
  font-size: 16px;
}

.controls {
  margin-bottom: 30px;
}

.stats {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.prompts-grid {
  margin-bottom: 30px;
}

.prompt-card-col {
  margin-bottom: 20px;
}

.prompt-card {
  height: 100%;
  position: relative;
  transition: transform 0.3s ease;
}

.prompt-card:hover {
  transform: translateY(-5px);
}

.prompt-card.featured {
  border: 2px solid #409eff;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.prompt-icon {
  width: 40px;
  height: 40px;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.prompt-meta {
  display: flex;
  gap: 5px;
  margin-top: 5px;
}

.prompt-content {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.description {
  margin-bottom: 15px;
  color: #555;
  line-height: 1.5;
}

.prompt-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
}

.prompt-actions {
  display: flex;
  gap: 10px;
}

.featured-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  background-color: #ff6b6b;
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.empty-state {
  text-align: center;
  padding: 50px 0;
}

.prompt-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.detail-icon {
  width: 60px;
  height: 60px;
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

.detail-meta {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.detail-content h3 {
  margin: 20px 0 10px;
  color: #333;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}
</style>
