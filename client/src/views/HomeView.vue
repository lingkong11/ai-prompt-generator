<template>
  <div class="home-page">
    <!-- 提示词生成页面 -->
    <div v-if="activeTab === 'generate'" class="generate-page">
      <el-row :gutter="24">
        <el-col :xs="24" :lg="12">
          <el-card class="input-card">
            <template #header>
              <span>📝 输入你的目标</span>
            </template>
            <el-form label-width="90px">
              <el-form-item label="目标描述">
                <el-input v-model="genForm.goal" type="textarea" :rows="4" placeholder="例如：写一篇关于AI技术发展的文章" />
              </el-form-item>
              <el-form-item label="提示词类型">
                <el-select v-model="genForm.type" placeholder="选择类型">
                  <el-option label="通用" value="general" />
                  <el-option label="写作" value="writing" />
                  <el-option label="编程" value="coding" />
                  <el-option label="分析" value="analysis" />
                  <el-option label="Agent工作流" value="agent" />
                </el-select>
              </el-form-item>
              <el-form-item label="风格">
                <el-select v-model="genForm.style" placeholder="选择风格">
                  <el-option label="专业" value="专业" />
                  <el-option label="简洁" value="简洁" />
                  <el-option label="风趣" value="风趣" />
                  <el-option label="技术" value="技术" />
                </el-select>
              </el-form-item>
              <el-form-item label="输出语言">
                <el-radio-group v-model="genForm.language">
                  <el-radio label="zh">中文</el-radio>
                  <el-radio label="en">English</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="generatePrompt" :loading="genLoading">
                  <el-icon><MagicStick /></el-icon>
                  生成提示词
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <el-col :xs="24" :lg="12">
          <el-card class="output-card">
            <template #header>
              <div class="output-header">
                <span>✨ 生成结果</span>
                <div v-if="result" class="output-actions">
                  <el-button size="small" type="primary" @click="savePrompt">
                    <el-icon><DocumentAdd /></el-icon> 保存
                  </el-button>
                  <el-button size="small" @click="copyResult">
                    <el-icon><CopyDocument /></el-icon> 复制
                  </el-button>
                </div>
              </div>
            </template>
            <div v-if="result" class="result-content">
              <pre>{{ result }}</pre>
            </div>
            <el-empty v-else description="点击生成按钮获取提示词" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 提示词管理页面 -->
    <div v-if="activeTab === 'prompts'" class="prompts-page">
      <el-card>
        <template #header>
          <div class="card-header-toolbar">
            <span>📁 我的提示词</span>
            <el-input v-model="searchKeyword" placeholder="搜索提示词..." style="width: 200px" clearable @change="loadPrompts">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </template>
        <el-table :data="prompts" stripe>
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isFavorite" label="收藏" width="80">
            <template #default="{ row }">
              <el-icon v-if="row.isFavorite" color="#f56c6c" @click="toggleFav(row)"><StarFilled /></el-icon>
              <el-icon v-else @click="toggleFav(row)"><Star /></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="useCount" label="使用次数" width="100" />
          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewPrompt(row)">查看</el-button>
              <el-button size="small" type="danger" @click="delPrompt(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-if="totalPages > 1" layout="prev, pager, next" :total="totalElements" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadPrompts" style="margin-top: 16px" />
      </el-card>
    </div>

    <!-- 分类管理页面 -->
    <div v-if="activeTab === 'categories'" class="categories-page">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>📂 分类列表</span>
            </template>
            <el-table :data="categories" stripe>
              <el-table-column prop="name" label="名称" />
              <el-table-column prop="icon" label="图标" width="80" />
              <el-table-column prop="description" label="描述" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" type="danger" @click="delCategory(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>➕ 新建分类</span>
            </template>
            <el-form label-width="80px">
              <el-form-item label="名称">
                <el-input v-model="catForm.name" placeholder="分类名称" />
              </el-form-item>
              <el-form-item label="图标">
                <el-input v-model="catForm.icon" placeholder="如: 📚" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input v-model="catForm.description" type="textarea" :rows="2" placeholder="分类描述" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="addCategory">创建</el-button>
                <el-button @click="Object.assign(catForm, { name: '', icon: '', description: '' })">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 收藏页面 -->
    <div v-if="activeTab === 'favorites'" class="favorites-page">
      <el-card>
        <template #header>
          <span>⭐ 我的收藏</span>
        </template>
        <el-table :data="favorites" stripe>
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="内容" min-width="200">
            <template #default="{ row }">
              <div class="content-preview">{{ row.content?.substring(0, 100) }}...</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="copyContent(row.content)">复制</el-button>
              <el-button size="small" type="danger" @click="toggleFav(row)">取消收藏</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="favorites.length === 0" description="暂无收藏" />
      </el-card>
    </div>

    <!-- 保存提示词对话框 -->
    <el-dialog v-model="saveDialogVisible" title="保存提示词" width="500px">
      <el-form label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="saveForm.title" placeholder="提示词标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="saveForm.type">
            <el-option label="通用" value="general" />
            <el-option label="写作" value="writing" />
            <el-option label="编程" value="coding" />
            <el-option label="分析" value="analysis" />
            <el-option label="Agent工作流" value="agent" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="saveForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看提示词对话框 -->
    <el-dialog v-model="viewDialogVisible" title="提示词详情" width="600px">
      <div v-if="viewPromptData" class="prompt-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">{{ viewPromptData.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag size="small">{{ viewPromptData.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收藏">
            <el-icon v-if="viewPromptData.isFavorite" color="#f56c6c"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
          </el-descriptions-item>
          <el-descriptions-item label="使用次数">{{ viewPromptData.useCount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(viewPromptData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="内容">
            <pre class="content-pre">{{ viewPromptData.content }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="copyContent(viewPromptData?.content)">复制内容</el-button>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { generatePrompt, getPrompts, createPrompt, deletePrompt, toggleFavorite, getFavorites, getCategories, createCategory, deleteCategory } from '@/api/prompts'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 根据路由确定当前Tab
const tabRouteMap: Record<string, string> = {
  'generate': 'generate',
  'prompts': 'prompts',
  'categories': 'categories',
  'favorites': 'favorites'
}
const routeToTab = (path: string) => {
  const segment = path.split('/').pop() || 'generate'
  return tabRouteMap[segment] || 'generate'
}
const activeTab = ref(routeToTab(route.fullPath))

watch(() => route.fullPath, (path) => {
  activeTab.value = routeToTab(path)
})

watch(activeTab, (tab) => {
  router.push('/' + tab)
})

// 生成相关
const genLoading = ref(false)
const result = ref('')
const genForm = reactive({ goal: '', type: 'general', style: '专业', language: 'zh' })

// 提示词管理
const prompts = ref<any[]>([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const totalPages = ref(0)

// 收藏
const favorites = ref<any[]>([])

// 分类
const categories = ref<any[]>([])
const catForm = reactive({ name: '', icon: '', description: '' })

// 保存对话框
const saveDialogVisible = ref(false)
const saveForm = reactive({ title: '', type: 'general', content: '' })

// 查看对话框
const viewDialogVisible = ref(false)
const viewPromptData = ref<any>(null)

const generatePromptFn = async () => {
  if (!genForm.goal) { ElMessage.warning('请输入目标描述'); return }
  genLoading.value = true
  try {
    const res = await generatePrompt(genForm)
    result.value = res.data.prompt
    ElMessage.success('生成成功')
  } catch (e: any) { ElMessage.error(e.response?.data?.error || e.message || '生成失败') }
  finally { genLoading.value = false }
}

const resetForm = () => { genForm.goal = ''; result.value = '' }
const copyResult = () => { navigator.clipboard.writeText(result.value); ElMessage.success('已复制') }

const savePrompt = () => {
  saveForm.content = result.value
  saveForm.title = genForm.goal.substring(0, 50)
  saveDialogVisible.value = true
}

const confirmSave = async () => {
  if (!saveForm.title || !saveForm.content) { ElMessage.warning('请填写标题和内容'); return }
  try {
    await createPrompt(saveForm)
    ElMessage.success('保存成功')
    saveDialogVisible.value = false
    loadPrompts()
  } catch (e: any) { ElMessage.error(e.response?.data?.error || '保存失败') }
}

const loadPrompts = async () => {
  try {
    const res = await getPrompts({ page: currentPage.value - 1, size: pageSize.value, keyword: searchKeyword.value || undefined })
    prompts.value = res.data.prompts || []
    totalElements.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch (e) { prompts.value = [] }
}

const loadFavorites = async () => {
  try {
    const res = await getFavorites()
    favorites.value = res.data.prompts || []
  } catch (e) { favorites.value = [] }
}

const loadCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) { categories.value = [] }
}

const toggleFav = async (row: any) => {
  try {
    await toggleFavorite(row.id)
    row.isFavorite = !row.isFavorite
    ElMessage.success(row.isFavorite ? '已收藏' : '已取消收藏')
    loadPrompts()
    if (activeTab.value === 'favorites') loadFavorites()
  } catch (e: any) { ElMessage.error(e.response?.data?.error || '操作失败') }
}

const delPrompt = async (row: any) => {
  await ElMessageBox.confirm('确定删除该提示词？', '提示', { type: 'warning' })
  try {
    await deletePrompt(row.id)
    ElMessage.success('删除成功')
    loadPrompts()
  } catch (e: any) { ElMessage.error(e.response?.data?.error || '删除失败') }
}

const viewPrompt = (row: any) => { viewPromptData.value = row; viewDialogVisible.value = true }
const copyContent = (content: string) => { navigator.clipboard.writeText(content); ElMessage.success('已复制') }

const addCategory = async () => {
  if (!catForm.name) { ElMessage.warning('请输入分类名称'); return }
  try {
    await createCategory(catForm)
    ElMessage.success('创建成功')
    Object.assign(catForm, { name: '', icon: '', description: '' })
    loadCategories()
  } catch (e: any) { ElMessage.error(e.response?.data?.error || '创建失败') }
}

const delCategory = async (row: any) => {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch (e: any) { ElMessage.error(e.response?.data?.error || '删除失败') }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}

// 初始化加载
if (activeTab.value === 'prompts') loadPrompts()
if (activeTab.value === 'favorites') loadFavorites()
if (activeTab.value === 'categories') loadCategories()
</script>

<style scoped>
.home-page { height: 100%; }

.generate-page, .prompts-page, .categories-page, .favorites-page { height: 100%; }

.input-card, .output-card { height: 100%; min-height: 420px; }

.output-header { display: flex; justify-content: space-between; align-items: center; }

.output-actions { display: flex; gap: 8px; }

.result-content pre { white-space: pre-wrap; word-wrap: break-word; background: #f5f7fa; padding: 16px; border-radius: 8px; max-height: 360px; overflow-y: auto; font-size: 13px; }

.card-header-toolbar { display: flex; justify-content: space-between; align-items: center; }

.content-preview { font-size: 12px; color: #666; max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.content-pre { white-space: pre-wrap; word-wrap: break-word; background: #f5f7fa; padding: 12px; border-radius: 4px; font-size: 13px; }
</style>
