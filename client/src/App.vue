<template>
  <div class="app-container">
    <!-- Header -->
    <el-header class="app-header">
      <div class="logo" @click="activeTab = 'generate'">
        <el-icon :size="28"><MagicStick /></el-icon>
        <span>AI Prompt Generator</span>
      </div>

      <div class="nav-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="🔮 提示词生成" name="generate" />
          <el-tab-pane label="📁 模板库" name="templates" />
          <el-tab-pane label="🤖 OpenClaw专用" name="openclaw" />
          <el-tab-pane label="⭐ 收藏" name="favorites" />
        </el-tabs>
      </div>

      <!-- 已登录 -->
      <div v-if="authStore.isLoggedIn" class="user-area">
        <el-dropdown @command="handleUserCommand">
          <span class="user-trigger">
            <el-avatar :size="32">{{ authStore.user?.nickname?.charAt(0) || authStore.user?.username?.charAt(0) }}</el-avatar>
            <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="my-prompts">我的提示词</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <!-- 未登录 -->
      <div v-else class="user-area">
        <el-button text @click="authStore.openLoginModal()">登录</el-button>
        <el-button type="primary" @click="authStore.openRegisterModal()">注册</el-button>
      </div>
    </el-header>

    <!-- Main -->
    <el-main class="app-main">

      <!-- 生成 -->
      <div v-if="activeTab === 'generate'" class="page-section">
        <el-row :gutter="24">
          <el-col :xs="24" :lg="12">
            <el-card class="input-card">
              <template #header><span>📝 输入你的目标</span></template>
              <el-form label-width="90px">
                <el-form-item label="目标描述">
                  <el-input v-model="genForm.goal" type="textarea" :rows="4" placeholder="例如：写一篇关于AI技术发展的文章" />
                </el-form-item>
                <el-form-item label="提示词类型">
                  <el-select v-model="genForm.type">
                    <el-option label="通用" value="general" />
                    <el-option label="写作" value="writing" />
                    <el-option label="编程" value="coding" />
                    <el-option label="分析" value="analysis" />
                    <el-option label="🤖 Agent工作流" value="agent" />
                  </el-select>
                </el-form-item>
                <el-form-item label="风格">
                  <el-select v-model="genForm.style">
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
                  <el-button type="primary" :loading="genLoading" @click="handleGenerate">
                    <el-icon><MagicStick /></el-icon> 生成提示词
                  </el-button>
                  <el-button @click="genForm.goal = ''; genResult = ''">重置</el-button>
                </el-form-item>
              </el-form>
              <el-alert v-if="!authStore.isLoggedIn" type="info" :closable="false" style="margin-top: 12px">
                💡 注册后可保存、收藏、管理你的提示词
              </el-alert>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card class="output-card">
              <template #header>
                <div class="flex-between">
                  <span>✨ 生成结果</span>
                  <div v-if="genResult" class="btn-group">
                    <el-button size="small" type="primary" @click="handleSave">
                      <el-icon><DocumentAdd /></el-icon> 保存
                    </el-button>
                    <el-button size="small" @click="copyToClipboard(genResult)">
                      <el-icon><CopyDocument /></el-icon> 复制
                    </el-button>
                  </div>
                </div>
              </template>
              <div v-if="genResult" class="result-box"><pre>{{ genResult }}</pre></div>
              <el-empty v-else description="点击生成按钮获取提示词" />
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 模板库 -->
      <div v-if="activeTab === 'templates'" class="page-section">
        <el-card>
          <template #header><span>📁 预置模板库</span></template>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8" v-for="tpl in templateList" :key="tpl.name">
              <el-card class="tpl-card" shadow="hover" @click="applyTemplate(tpl)">
                <div class="tpl-icon">{{ categoryIcon(tpl.category) }}</div>
                <div class="tpl-name">{{ tpl.name }}</div>
                <div class="tpl-desc">{{ tpl.description }}</div>
                <div class="tpl-tags"><el-tag v-for="tag in (tpl.tags||'').split(',')" :key="tag" size="small">{{ tag }}</el-tag></div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </div>

      <!-- OpenClaw -->
      <div v-if="activeTab === 'openclaw'" class="page-section">
        <el-alert title="🤖 OpenClaw AI Agent 九阶段专用提示词" type="info" :closable="false" style="margin-bottom: 20px">
          自主型AI软件工程Agent标准工作流，覆盖需求分析到项目交付的完整闭环。
        </el-alert>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :lg="8" v-for="(item, idx) in openClawStages" :key="idx">
            <el-card class="tpl-card" shadow="hover" @click="applyOpenClawStage(item)">
              <div class="tpl-icon">{{ item.icon }}</div>
              <div class="tpl-name">{{ item.title }}</div>
              <div class="tpl-desc">{{ item.description }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 收藏 - 增强版 -->
      <div v-if="activeTab === 'favorites'" class="page-section">
        <el-card>
          <template #header>
            <div class="flex-between">
              <span>⭐ 我的收藏 <el-tag size="small" type="warning">{{ favoriteList.length }}</el-tag></span>
              <el-input v-model="favSearch" placeholder="搜索..." size="small" style="width:160px" clearable @input="filterFavorites" />
            </div>
          </template>
          <div v-if="filteredFavorites.length > 0" class="fav-grid">
            <el-card v-for="fav in filteredFavorites" :key="fav.id" class="fav-card" shadow="hover">
              <div class="fav-header">
                <el-tag size="small">{{ fav.type }}</el-tag>
                <span class="fav-date">{{ fmtDate(fav.createdAt) }}</span>
              </div>
              <div class="fav-title">{{ fav.title }}</div>
              <div class="fav-content">{{ fav.content?.substring(0, 120) }}...</div>
              <div class="fav-actions">
                <el-button size="small" @click="viewFavDetail(fav)">查看</el-button>
                <el-button size="small" @click="copyToClipboard(fav.content)">复制</el-button>
                <el-button size="small" type="danger" @click="handleUnfavorite(fav)">删除</el-button>
              </div>
            </el-card>
          </div>
          <el-empty v-else description="暂无收藏，生成提示词后点击保存即可收藏" />
        </el-card>
      </div>

      <!-- 我的提示词 -->
      <div v-if="activeTab === 'my-prompts'" class="page-section">
        <el-card>
          <template #header>
            <div class="flex-between">
              <span>📋 我的提示词 <el-tag size="small">{{ myPromptList.length }}</el-tag></span>
              <el-input v-model="mySearch" placeholder="搜索..." size="small" style="width:160px" clearable @input="filterMyPrompts" />
            </div>
          </template>
          <el-table :data="filteredMyPrompts" stripe v-if="filteredMyPrompts.length > 0">
            <el-table-column prop="title" label="标题" min-width="150" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="useCount" label="使用" width="60" />
            <el-table-column prop="isFavorite" label="收藏" width="60">
              <template #default="{ row }">
                <el-icon v-if="row.isFavorite" color="#f56c6c"><StarFilled /></el-icon>
                <el-icon v-else @click="toggleFav(row)"><Star /></el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="160">
              <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="viewPrompt(row)">查看</el-button>
                <el-button size="small" type="danger" @click="delPrompt(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无提示词，去生成一个吧" />
        </el-card>
      </div>
    </el-main>

    <!-- 保存对话框 -->
    <el-dialog v-model="saveDialogVisible" title="保存提示词" width="500px">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="saveForm.title" placeholder="提示词标题" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="saveForm.type">
            <el-option label="通用" value="general" />
            <el-option label="写作" value="writing" />
            <el-option label="编程" value="coding" />
            <el-option label="分析" value="analysis" />
            <el-option label="Agent工作流" value="agent" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="saveForm.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleConfirmSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 个人资料对话框 -->
    <el-dialog v-model="profileDialogVisible" title="个人资料" width="450px">
      <el-form label-width="80px">
        <el-form-item label="头像">
          <el-avatar :size="80" style="margin-bottom:8px">{{ profileForm.nickname?.charAt(0) || profileForm.username?.charAt(0) }}</el-avatar>
          <el-input v-model="profileForm.avatar" placeholder="头像URL（可选）" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="你的昵称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-text>{{ profileForm.createdAt }}</el-text>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="detailData?.title || '详情'" width="600px">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item label="类型"><el-tag>{{ detailData.type }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ fmtDate(detailData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ detailData.useCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <pre class="detail-content">{{ detailData.content }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="copyToClipboard(detailData?.content)">复制内容</el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 认证弹窗 -->
    <AuthModal />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { generatePrompt, getFavorites, getTemplates, createPrompt, toggleFavorite, getPrompts, deletePrompt } from '@/api/prompts'
import AuthModal from '@/components/AuthModal.vue'

const authStore = useAuthStore()
const activeTab = ref('generate')

// ========== 生成 ==========
const genLoading = ref(false), genResult = ref('')
const genForm = reactive({ goal: '', type: 'general', style: '专业', language: 'zh' })

const handleGenerate = () => {
  if (!genForm.goal) { ElMessage.warning('请输入目标描述'); return }
  authStore.requireAuth(async () => {
    genLoading.value = true
    try {
      const res = await generatePrompt(genForm)
      genResult.value = res.data.data.prompt
      ElMessage.success('生成成功')
    } catch (e: any) { ElMessage.error(e.response?.data?.message || '生成失败') }
    finally { genLoading.value = false }
  })
}

const handleSave = () => {
  if (!genResult.value) return
  authStore.requireAuth(() => {
    saveForm.content = genResult.value
    saveForm.title = genForm.goal.substring(0, 50) || '未命名提示词'
    saveForm.type = genForm.type
    saveDialogVisible.value = true
  })
}

// ========== 模板库 ==========
const templateList = ref<any[]>([])
const loadTemplates = async () => {
  try {
    const res = await getTemplates()
    templateList.value = res.data.data?.templates || []
  } catch { templateList.value = [] }
}
const categoryIcon = (cat: string) => ({ openclaw: '🤖', writing: '📝', coding: '💻', analysis: '📊', general: '🎯' }[cat] || '📄')
const applyTemplate = (tpl: any) => { genForm.type = tpl.category; activeTab.value = 'generate'; ElMessage.success(`已选择模板：${tpl.name}`) }

// ========== OpenClaw ==========
const openClawStages = [
  { icon: '📋', title: '需求分析', description: '将模糊想法转化为结构化PRD文档', goal: '需求分析：将用户需求转化为可执行的需求规格说明书' },
  { icon: '🎨', title: '技术设计', description: '将PRD转化为可执行的技术方案', goal: '技术设计：输出系统架构、数据库设计、API接口规范' },
  { icon: '⚙️', title: '任务分解', description: '将设计拆解为可追踪的任务卡片', goal: '任务分解：将技术方案拆解为具体的开发任务' },
  { icon: '💻', title: '代码开发', description: '全栈工程师输出生产级代码', goal: '代码开发：按照设计方案编写完整可运行的代码' },
  { icon: '🧪', title: '功能测试', description: 'QA工程师输出测试用例与报告', goal: '功能测试：编写并执行测试用例，输出测试报告' },
  { icon: '🔍', title: '代码审查', description: '资深Reviewer输出审查意见', goal: '代码审查：审查代码质量、安全性和最佳实践' },
  { icon: '📚', title: '文档输出', description: '技术文档工程师输出手册', goal: '文档输出：编写操作手册、运维手册和技术方案' },
  { icon: '🚀', title: '部署上线', description: 'DevOps输出部署方案', goal: '部署上线：制定部署计划并执行生产环境发布' },
  { icon: '📊', title: '项目复盘', description: 'PM输出项目回顾与改进建议', goal: '项目复盘：总结项目经验教训，输出改进计划' },
]
const applyOpenClawStage = (item: any) => { genForm.type = 'agent'; genForm.goal = item.goal; activeTab.value = 'generate'; ElMessage.success(`已选择 ${item.title}，目标已填入`) }

// ========== 收藏 ==========
const favoriteList = ref<any[]>([])
const favSearch = ref('')
const filteredFavorites = computed(() => {
  if (!favSearch.value) return favoriteList.value
  const kw = favSearch.value.toLowerCase()
  return favoriteList.value.filter(f => f.title?.toLowerCase().includes(kw) || f.content?.toLowerCase().includes(kw))
})

const loadFavorites = () => {
  authStore.requireAuth(async () => {
    try {
      const res = await getFavorites()
      favoriteList.value = res.data.data?.prompts || []
    } catch { favoriteList.value = [] }
  })
}
const filterFavorites = () => {} // computed 处理

const handleUnfavorite = async (row: any) => {
  await ElMessageBox.confirm('确定取消收藏？', '提示', { type: 'warning' })
  try {
    await toggleFavorite(row.id)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '操作失败') }
}

// ========== 我的提示词 ==========
const myPromptList = ref<any[]>([])
const mySearch = ref('')
const filteredMyPrompts = computed(() => {
  if (!mySearch.value) return myPromptList.value
  const kw = mySearch.value.toLowerCase()
  return myPromptList.value.filter(p => p.title?.toLowerCase().includes(kw) || p.content?.toLowerCase().includes(kw))
})

const loadMyPrompts = () => {
  authStore.requireAuth(async () => {
    try {
      const res = await getPrompts({ size: 100 })
      myPromptList.value = res.data.data?.prompts || []
    } catch { myPromptList.value = [] }
  })
}
const filterMyPrompts = () => {}

const toggleFav = async (row: any) => {
  try {
    await toggleFavorite(row.id)
    row.isFavorite = !row.isFavorite
    ElMessage.success(row.isFavorite ? '已收藏' : '已取消收藏')
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '操作失败') }
}

const delPrompt = async (row: any) => {
  await ElMessageBox.confirm('确定删除该提示词？', '提示', { type: 'warning' })
  try {
    await deletePrompt(row.id)
    ElMessage.success('删除成功')
    loadMyPrompts()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '删除失败') }
}

// ========== 保存对话框 ==========
const saveDialogVisible = ref(false), saveLoading = ref(false)
const saveForm = reactive({ title: '', type: 'general', content: '' })
const handleConfirmSave = async () => {
  if (!saveForm.title || !saveForm.content) { ElMessage.warning('请填写标题和内容'); return }
  saveLoading.value = true
  try {
    await createPrompt(saveForm)
    ElMessage.success('保存成功')
    saveDialogVisible.value = false
    loadMyPrompts()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '保存失败') }
  finally { saveLoading.value = false }
}

// ========== 个人资料对话框 ==========
const profileDialogVisible = ref(false), profileLoading = ref(false)
const profileForm = reactive({ username: '', email: '', nickname: '', avatar: '', createdAt: '' })

const openProfileDialog = () => {
  const u = authStore.user
  if (!u) return
  profileForm.username = u.username || ''
  profileForm.email = u.email || ''
  profileForm.nickname = u.nickname || ''
  profileForm.avatar = u.avatar || ''
  profileForm.createdAt = u.createdAt ? fmtDate(u.createdAt) : ''
  profileDialogVisible.value = true
}

const handleUpdateProfile = async () => {
  profileLoading.value = true
  try {
    await authStore.updateUserProfile({ nickname: profileForm.nickname, avatar: profileForm.avatar })
    ElMessage.success('资料更新成功')
    profileDialogVisible.value = false
  } catch (e: any) { ElMessage.error(e.response?.data?.message || '更新失败') }
  finally { profileLoading.value = false }
}

// ========== 详情对话框 ==========
const detailDialogVisible = ref(false), detailData = ref<any>(null)
const viewFavDetail = (fav: any) => { detailData.value = fav; detailDialogVisible.value = true }
const viewPrompt = (p: any) => { detailData.value = p; detailDialogVisible.value = true }

// ========== 通用 ==========
const copyToClipboard = (text?: string) => { if (text) { navigator.clipboard.writeText(text); ElMessage.success('已复制') } }
const fmtDate = (d: string) => d ? d.replace('T', ' ').substring(0, 19) : ''

const handleUserCommand = (cmd: string) => {
  if (cmd === 'logout') { authStore.logout(); activeTab.value = 'generate' }
  if (cmd === 'profile') openProfileDialog()
  if (cmd === 'my-prompts') { activeTab.value = 'my-prompts'; loadMyPrompts() }
}

// ========== 初始化 ==========
authStore.init()
loadTemplates()

watch(activeTab, (tab) => {
  if (tab === 'favorites') loadFavorites()
  if (tab === 'my-prompts') loadMyPrompts()
  if (tab === 'favorites' && !authStore.isLoggedIn) authStore.openLoginModal()
  if (tab === 'my-prompts' && !authStore.isLoggedIn) authStore.openLoginModal()
})
</script>

<style scoped>
.app-container { height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; flex-direction: column; }
.app-header { background: rgba(255,255,255,0.95); display: flex; align-items: center; padding: 0 24px; gap: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); z-index: 10; }
.logo { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: bold; color: #667eea; cursor: pointer; flex-shrink: 0; }
.nav-tabs { flex: 1; }
.user-area { flex-shrink: 0; display: flex; gap: 8px; }
.user-trigger { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; }
.app-main { padding: 24px; flex: 1; overflow-y: auto; background: transparent; }
.input-card, .output-card { min-height: 420px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.btn-group { display: flex; gap: 8px; }
.result-box pre { white-space: pre-wrap; word-wrap: break-word; background: #f5f7fa; padding: 16px; border-radius: 8px; max-height: 360px; overflow-y: auto; font-size: 13px; line-height: 1.6; }
.tpl-card { margin-bottom: 20px; cursor: pointer; transition: transform 0.3s; }
.tpl-card:hover { transform: translateY(-4px); }
.tpl-icon { font-size: 32px; text-align: center; margin-bottom: 12px; }
.tpl-name { font-weight: bold; text-align: center; margin-bottom: 8px; }
.tpl-desc { font-size: 12px; color: #666; text-align: center; margin-bottom: 12px; }
.tpl-tags { display: flex; justify-content: center; gap: 4px; flex-wrap: wrap; }
.count-badge { margin-left: 8px; }
.fav-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.fav-card { }
.fav-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.fav-date { font-size: 12px; color: #999; }
.fav-title { font-weight: bold; margin-bottom: 8px; }
.fav-content { font-size: 12px; color: #666; line-height: 1.5; margin-bottom: 12px; max-height: 80px; overflow: hidden; }
.fav-actions { display: flex; gap: 8px; }
.detail-content { white-space: pre-wrap; word-wrap: break-word; background: #f5f7fa; padding: 12px; border-radius: 4px; font-size: 13px; max-height: 300px; overflow-y: auto; }
</style>
