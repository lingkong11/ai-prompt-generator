<template>
  <div class="app-container">
    <!-- Header -->
    <el-header class="app-header">
      <div class="logo" @click="activeTab = 'generate'">
        <el-icon :size="28"><MagicStick /></el-icon>
        <span>{{ t.app.title }}</span>
      </div>

      <div class="nav-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="t.app.tabHome" name="home" />
          <el-tab-pane :label="t.app.tabGenerate" name="generate" />
          <el-tab-pane :label="t.app.tabTemplates" name="templates" />
          <el-tab-pane :label="t.app.tabOpenClaw" name="openclaw" />
          <el-tab-pane :label="t.app.tabFavorites" name="favorites" />
          <el-tab-pane :label="t.app.tabPricing" name="pricing" />
        </el-tabs>
      </div>

      <!-- Settings: Language + Theme -->
      <div class="header-settings">
        <el-dropdown trigger="click" @command="appStore.setLocale">
          <el-button text size="small">
            <el-icon><Globe /></el-icon> {{ appStore.localeNames[appStore.locale] }}
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="(name, key) in appStore.localeNames" :key="key" :command="key">
                {{ name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-dropdown trigger="click" @command="appStore.setTheme">
          <el-button text size="small">
            <el-icon><Brush /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="(name, key) in appStore.themeNames" :key="key" :command="key">
                {{ name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- User -->
      <div v-if="authStore.isLoggedIn" class="user-area">
        <el-dropdown @command="handleUserCommand">
          <span class="user-trigger">
            <el-avatar :size="32">{{ authStore.user?.nickname?.charAt(0) || authStore.user?.username?.charAt(0) }}</el-avatar>
            <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">{{ t.app.profile }}</el-dropdown-item>
              <el-dropdown-item command="my-prompts">{{ t.app.myPrompts }}</el-dropdown-item>
              <el-dropdown-item command="logout" divided>{{ t.app.logout }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div v-else class="user-area">
        <el-button text @click="authStore.openLoginModal()">{{ t.app.login }}</el-button>
        <el-button type="primary" @click="authStore.openRegisterModal()">{{ t.app.register }}</el-button>
      </div>
    </el-header>

    <!-- Main -->
    <el-main class="app-main">

      <!-- Home -->
      <div v-if="activeTab === 'home'" class="page-section home-page">
        <HeroSection @start="activeTab = 'generate'" @view-templates="activeTab = 'templates'" />
        <FeaturesSection />
        <StepsSection />
        
        <!-- FAQ -->
        <div class="faq-section">
          <h2 class="section-title">{{ t.faq.title }}</h2>
          <el-collapse>
            <el-collapse-item :title="t.faq.q1" name="1">
              <p>{{ t.faq.a1 }}</p>
            </el-collapse-item>
            <el-collapse-item :title="t.faq.q2" name="2">
              <p>{{ t.faq.a2 }}</p>
            </el-collapse-item>
            <el-collapse-item :title="t.faq.q3" name="3">
              <p>{{ t.faq.a3 }}</p>
            </el-collapse-item>
            <el-collapse-item :title="t.faq.q4" name="4">
              <p>{{ t.faq.a4 }}</p>
            </el-collapse-item>
            <el-collapse-item :title="t.faq.q5" name="5">
              <p>{{ t.faq.a5 }}</p>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>

      <!-- Generate -->
      <div v-if="activeTab === 'generate'" class="page-section">
        <el-row :gutter="24">
          <el-col :xs="24" :lg="12">
            <el-card class="input-card">
              <template #header><span>{{ t.gen.inputTitle }}</span></template>
              <el-form label-width="90px">
                <el-form-item :label="t.gen.goalLabel">
                  <el-input v-model="genForm.goal" type="textarea" :rows="4" :placeholder="t.gen.goalPlaceholder" />
                </el-form-item>
                <el-form-item :label="t.gen.typeLabel">
                  <el-select v-model="genForm.type">
                    <el-option :label="t.type.general" value="general" />
                    <el-option :label="t.type.writing" value="writing" />
                    <el-option :label="t.type.coding" value="coding" />
                    <el-option :label="t.type.analysis" value="analysis" />
                    <el-option :label="t.type.agent" value="agent" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="t.gen.styleLabel">
                  <el-select v-model="genForm.style">
                    <el-option :label="t.style.professional" value="专业" />
                    <el-option :label="t.style.concise" value="简洁" />
                    <el-option :label="t.style.humorous" value="风趣" />
                    <el-option :label="t.style.technical" value="技术" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="t.gen.languageLabel">
                  <el-radio-group v-model="genForm.language">
                    <el-radio label="zh">中文</el-radio>
                    <el-radio label="en">English</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="genLoading" @click="handleGenerate">
                    <el-icon><MagicStick /></el-icon> {{ t.gen.generateBtn }}
                  </el-button>
                  <el-button @click="genForm.goal = ''; genResult = ''">{{ t.gen.resetBtn }}</el-button>
                </el-form-item>
              </el-form>
              <el-alert v-if="!authStore.isLoggedIn" type="info" :closable="false" style="margin-top: 12px">
                {{ t.gen.guestTip }}
              </el-alert>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card class="output-card">
              <template #header>
                <div class="flex-between">
                  <span>{{ t.gen.resultTitle }}</span>
                  <div v-if="genResult" class="btn-group">
                    <el-button size="small" type="primary" @click="handleSave">
                      <el-icon><DocumentAdd /></el-icon> {{ t.gen.saveBtn }}
                    </el-button>
                    <el-button size="small" @click="copyToClipboard(genResult)">
                      <el-icon><CopyDocument /></el-icon> {{ t.gen.copyBtn }}
                    </el-button>
                  </div>
                </div>
              </template>
              <div v-if="genResult" class="result-box"><pre>{{ genResult }}</pre></div>
              <el-empty v-else :description="t.gen.emptyResult" />
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Templates -->
      <div v-if="activeTab === 'templates'" class="page-section">
        <el-card>
          <template #header><span>{{ t.tpl.title }}</span></template>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :lg="8" v-for="tpl in templateList" :key="tpl.name">
              <el-card class="tpl-card" shadow="hover" @click="applyTemplate(tpl)">
                <div class="tpl-icon">{{ categoryIcon(tpl.category) }}</div>
                <div class="tpl-name">{{ tpl.name }}</div>
                <div class="tpl-desc">{{ tpl.description }}</div>
                <div class="tpl-tags">
                  <el-tag v-for="tag in (tpl.tags||'').split(',')" :key="tag" size="small">{{ tag }}</el-tag>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </div>

      <!-- OpenClaw -->
      <div v-if="activeTab === 'openclaw'" class="page-section">
        <el-alert :title="t.oc.alertTitle" type="info" :closable="false" style="margin-bottom: 20px">
          {{ t.oc.alertDesc }}
        </el-alert>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :lg="8" v-for="(item, idx) in t.oc.stages" :key="idx">
            <el-card class="tpl-card" shadow="hover" @click="applyOpenClawStage(item, idx)">
              <div class="tpl-icon">{{ stageIcons[idx] }}</div>
              <div class="tpl-name">{{ item.title }}</div>
              <div class="tpl-desc">{{ item.desc }}</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- Favorites -->
      <div v-if="activeTab === 'favorites'" class="page-section">
        <el-card>
          <template #header>
            <div class="flex-between">
              <span>{{ t.fav.title }} <el-tag size="small" type="warning">{{ favoriteList.length }}</el-tag></span>
              <el-input v-model="favSearch" :placeholder="t.fav.search" size="small" style="width:160px" clearable />
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
                <el-button size="small" @click="viewDetail(fav)">{{ t.fav.view }}</el-button>
                <el-button size="small" @click="copyToClipboard(fav.content)">{{ t.fav.copy }}</el-button>
                <el-button size="small" type="danger" @click="handleUnfavorite(fav)">{{ t.fav.delete }}</el-button>
              </div>
            </el-card>
          </div>
          <el-empty v-else :description="t.fav.empty" />
        </el-card>
      </div>

      <!-- Pricing -->
      <div v-if="activeTab === 'pricing'" class="page-section">
        <PricingView />
      </div>

      <!-- My Prompts -->
      <div v-if="activeTab === 'my-prompts'" class="page-section">
        <el-card>
          <template #header>
            <div class="flex-between">
              <span>{{ t.mp.title }} <el-tag size="small">{{ myPromptList.length }}</el-tag></span>
              <el-input v-model="mySearch" :placeholder="t.mp.search" size="small" style="width:160px" clearable />
            </div>
          </template>
          <el-table :data="filteredMyPrompts" stripe v-if="filteredMyPrompts.length > 0">
            <el-table-column prop="title" :label="t.mp.colTitle" min-width="150" />
            <el-table-column prop="type" :label="t.mp.colType" width="100">
              <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="useCount" :label="t.mp.colUseCount" width="60" />
            <el-table-column prop="isFavorite" :label="t.mp.colFavorite" width="60">
              <template #default="{ row }">
                <el-icon v-if="row.isFavorite" color="#f56c6c"><StarFilled /></el-icon>
                <el-icon v-else @click="toggleFav(row)"><Star /></el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" :label="t.mp.colCreated" width="160">
              <template #default="{ row }">{{ fmtDate(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column :label="t.mp.colActions" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="viewDetail(row)">{{ t.fav.view }}</el-button>
                <el-button size="small" type="danger" @click="delPrompt(row)">{{ t.mp.confirmDelete }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :description="t.mp.empty" />
        </el-card>
      </div>
    </el-main>

    <!-- Footer -->
    <el-footer class="app-footer">
      <div class="footer-content">
        <div class="footer-left">{{ t.footer.copyright }}</div>
        <div class="footer-center">{{ t.footer.aiPowered }}</div>
        <div class="footer-right">{{ t.footer.poweredBy }}</div>
      </div>
    </el-footer>

    <!-- Save Dialog -->
    <el-dialog v-model="saveDialogVisible" :title="t.save.dialogTitle" width="500px">
      <el-form label-width="80px">
        <el-form-item :label="t.save.titleLabel">
          <el-input v-model="saveForm.title" :placeholder="t.save.titlePlaceholder" />
        </el-form-item>
        <el-form-item :label="t.save.typeLabel">
          <el-select v-model="saveForm.type">
            <el-option :label="t.type.general" value="general" />
            <el-option :label="t.type.writing" value="writing" />
            <el-option :label="t.type.coding" value="coding" />
            <el-option :label="t.type.analysis" value="analysis" />
            <el-option :label="t.type.agent" value="agent" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t.save.contentLabel">
          <el-input v-model="saveForm.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDialogVisible = false">{{ t.save.cancelBtn }}</el-button>
        <el-button type="primary" :loading="saveLoading" @click="handleConfirmSave">{{ t.save.confirmBtn }}</el-button>
      </template>
    </el-dialog>

    <!-- Profile Dialog -->
    <el-dialog v-model="profileDialogVisible" :title="t.prof.title" width="450px">
      <el-form label-width="80px">
        <el-form-item :label="t.prof.avatarLabel">
          <el-avatar :size="80" style="margin-bottom:8px">{{ profileForm.nickname?.charAt(0) || profileForm.username?.charAt(0) }}</el-avatar>
          <el-input v-model="profileForm.avatar" :placeholder="t.prof.avatarPlaceholder" />
        </el-form-item>
        <el-form-item :label="t.prof.usernameLabel"><el-input v-model="profileForm.username" disabled /></el-form-item>
        <el-form-item :label="t.prof.emailLabel"><el-input v-model="profileForm.email" disabled /></el-form-item>
        <el-form-item :label="t.prof.nicknameLabel"><el-input v-model="profileForm.nickname" :placeholder="t.prof.nicknamePlaceholder" maxlength="20" show-word-limit /></el-form-item>
        <el-form-item :label="t.prof.createdLabel"><el-text>{{ profileForm.createdAt }}</el-text></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">{{ t.prof.cancelBtn }}</el-button>
        <el-button type="primary" :loading="profileLoading" @click="handleUpdateProfile">{{ t.prof.saveBtn }}</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" :title="detailData?.title || ''" width="600px">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item :label="t.detail.typeLabel"><el-tag>{{ detailData.type }}</el-tag></el-descriptions-item>
        <el-descriptions-item :label="t.detail.createdLabel">{{ fmtDate(detailData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item :label="t.detail.useCountLabel">{{ detailData.useCount || 0 }}</el-descriptions-item>
        <el-descriptions-item :label="t.detail.contentLabel">
          <pre class="detail-content">{{ detailData.content }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="copyToClipboard(detailData?.content)">{{ t.detail.copyBtn }}</el-button>
        <el-button @click="detailDialogVisible = false">{{ t.detail.closeBtn }}</el-button>
      </template>
    </el-dialog>

    <!-- Auth Modal -->
    <AuthModal />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { generatePrompt, getFavorites, getTemplates, createPrompt, toggleFavorite, getPrompts, deletePrompt } from '@/api/prompts'
import AuthModal from '@/components/AuthModal.vue'
import PricingView from '@/views/PricingView.vue'
import HeroSection from '@/components/home/HeroSection.vue'
import FeaturesSection from '@/components/home/FeaturesSection.vue'
import StepsSection from '@/components/home/StepsSection.vue'

const authStore = useAuthStore()
const appStore = useAppStore()
const t = computed(() => appStore.t)
const activeTab = ref('home')

const stageIcons = ['📋','🎨','⚙️','💻','🧪','🔍','📚','🚀','📊']

// Generate
const genLoading = ref(false), genResult = ref('')
const genForm = reactive({ goal: '', type: 'general', style: '专业', language: 'zh' })

const handleGenerate = () => {
  if (!genForm.goal) { ElMessage.warning(t.value.gen.inputGoal); return }
  authStore.requireAuth(async () => {
    genLoading.value = true
    try {
      const res = await generatePrompt(genForm)
      genResult.value = res.data.data.prompt
      ElMessage.success(t.value.gen.genSuccess)
    } catch (e: any) { ElMessage.error(e.response?.data?.message || t.value.gen.genFail) }
    finally { genLoading.value = false }
  })
}

const handleSave = () => {
  if (!genResult.value) return
  authStore.requireAuth(() => {
    saveForm.content = genResult.value
    saveForm.title = genForm.goal.substring(0, 50) || 'Untitled'
    saveForm.type = genForm.type
    saveDialogVisible.value = true
  })
}

// Templates
const templateList = ref<any[]>([])
const loadTemplates = async () => {
  try { const res = await getTemplates(); templateList.value = res.data.data?.templates || [] }
  catch { templateList.value = [] }
}
const categoryIcon = (cat: string) => ({ openclaw:'🤖', writing:'📝', coding:'💻', analysis:'📊', general:'🎯' }[cat] || '📄')
const applyTemplate = (tpl: any) => {
  genForm.type = tpl.category; activeTab.value = 'generate'
  ElMessage.success(t.value.gen.selectedTemplate.replace('{name}', tpl.name))
}

// OpenClaw
const applyOpenClawStage = (item: any) => {
  genForm.type = 'agent'; genForm.goal = item.goal; activeTab.value = 'generate'
  ElMessage.success(t.value.gen.selectedStage.replace('{title}', item.title))
}

// Favorites
const favoriteList = ref<any[]>([]), favSearch = ref('')
const filteredFavorites = computed(() => {
  if (!favSearch.value) return favoriteList.value
  const kw = favSearch.value.toLowerCase()
  return favoriteList.value.filter(f => f.title?.toLowerCase().includes(kw) || f.content?.toLowerCase().includes(kw))
})
const loadFavorites = () => {
  authStore.requireAuth(async () => {
    try { const res = await getFavorites(); favoriteList.value = res.data.data?.prompts || [] }
    catch { favoriteList.value = [] }
  })
}
const handleUnfavorite = async (row: any) => {
  await ElMessageBox.confirm(t.value.fav.confirmDelete, '', { type: 'warning' })
  try { await toggleFavorite(row.id); ElMessage.success(t.value.fav.deleteSuccess); loadFavorites() }
  catch (e: any) { ElMessage.error(e.response?.data?.message || t.value.fav.deleteFail) }
}

// My Prompts
const myPromptList = ref<any[]>([]), mySearch = ref('')
const filteredMyPrompts = computed(() => {
  if (!mySearch.value) return myPromptList.value
  const kw = mySearch.value.toLowerCase()
  return myPromptList.value.filter(p => p.title?.toLowerCase().includes(kw) || p.content?.toLowerCase().includes(kw))
})
const loadMyPrompts = () => {
  authStore.requireAuth(async () => {
    try { const res = await getPrompts({ size: 100 }); myPromptList.value = res.data.data?.prompts || [] }
    catch { myPromptList.value = [] }
  })
}
const toggleFav = async (row: any) => {
  try { await toggleFavorite(row.id); row.isFavorite = !row.isFavorite; ElMessage.success(row.isFavorite ? t.value.mp.favSuccess : t.value.mp.unfavSuccess) }
  catch (e: any) { ElMessage.error(e.response?.data?.message || '') }
}
const delPrompt = async (row: any) => {
  await ElMessageBox.confirm(t.value.mp.confirmDelete, '', { type: 'warning' })
  try { await deletePrompt(row.id); ElMessage.success(t.value.mp.deleteSuccess); loadMyPrompts() }
  catch (e: any) { ElMessage.error(e.response?.data?.message || t.value.mp.deleteFail) }
}

// Save Dialog
const saveDialogVisible = ref(false), saveLoading = ref(false)
const saveForm = reactive({ title: '', type: 'general', content: '' })
const handleConfirmSave = async () => {
  if (!saveForm.title || !saveForm.content) { ElMessage.warning(t.value.save.fillRequired); return }
  saveLoading.value = true
  try { await createPrompt(saveForm); ElMessage.success(t.value.gen.saved); saveDialogVisible.value = false; loadMyPrompts() }
  catch (e: any) { ElMessage.error(e.response?.data?.message || t.value.gen.saveFail) }
  finally { saveLoading.value = false }
}

// Profile
const profileDialogVisible = ref(false), profileLoading = ref(false)
const profileForm = reactive({ username: '', email: '', nickname: '', avatar: '', createdAt: '' })
const openProfileDialog = () => {
  const u = authStore.user; if (!u) return
  profileForm.username = u.username || ''; profileForm.email = u.email || ''
  profileForm.nickname = u.nickname || ''; profileForm.avatar = u.avatar || ''
  profileForm.createdAt = u.createdAt ? fmtDate(u.createdAt) : ''
  profileDialogVisible.value = true
}
const handleUpdateProfile = async () => {
  profileLoading.value = true
  try { await authStore.updateUserProfile({ nickname: profileForm.nickname, avatar: profileForm.avatar }); ElMessage.success(t.value.prof.updateSuccess); profileDialogVisible.value = false }
  catch (e: any) { ElMessage.error(e.response?.data?.message || t.value.prof.updateFail) }
  finally { profileLoading.value = false }
}

// Detail
const detailDialogVisible = ref(false), detailData = ref<any>(null)
const viewDetail = (item: any) => { detailData.value = item; detailDialogVisible.value = true }

// Utils
const copyToClipboard = (text?: string) => { if (text) { navigator.clipboard.writeText(text); ElMessage.success(t.value.gen.copied) } }
const fmtDate = (d: string) => d ? d.replace('T', ' ').substring(0, 19) : ''

const handleUserCommand = (cmd: string) => {
  if (cmd === 'logout') { authStore.logout(); activeTab.value = 'generate' }
  if (cmd === 'profile') openProfileDialog()
  if (cmd === 'my-prompts') { activeTab.value = 'my-prompts'; loadMyPrompts() }
}

// Init
authStore.init(); loadTemplates()
watch(activeTab, (tab) => {
  if (tab === 'favorites') loadFavorites()
  if (tab === 'my-prompts') loadMyPrompts()
  if ((tab === 'favorites' || tab === 'my-prompts') && !authStore.isLoggedIn) authStore.openLoginModal()
})
</script>

<style>
/* ========== Theme Variables ========== */
.theme-light {
  --bg-gradient: linear-gradient(135deg, #e8eaf6 0%, #c5cae9 100%);
  --header-bg: rgba(255,255,255,0.98);
  --footer-bg: rgba(255,255,255,0.9);
  --footer-text: #666;
  --footer-border: #e0e0e0;
  --card-bg: #ffffff;
  --text-primary: #1a1a2e;
  --text-secondary: #555;
}
.theme-dark {
  --bg-gradient: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  --header-bg: rgba(30,30,50,0.95);
  --footer-bg: rgba(20,20,40,0.9);
  --footer-text: #aaa;
  --footer-border: #333;
  --card-bg: #222244;
  --text-primary: #e0e0e0;
  --text-secondary: #aaa;
}
.theme-dark .el-card { --el-card-bg-color: #222244; --el-border-color: #333; }
.theme-dark .el-header { border-bottom-color: #333 !important; }
.theme-dark .el-input__wrapper,
.theme-dark .el-select .el-input__wrapper,
.theme-dark .el-textarea__inner { background-color: #2a2a4a; box-shadow: 0 0 0 1px #444 inset; }
.theme-dark .el-table { --el-table-bg-color: transparent; --el-table-tr-bg-color: #222244; --el-table-header-bg-color: #2a2a4a; }
.theme-dark .el-dialog { --el-dialog-bg-color: #222244; }
.theme-dark .el-descriptions { --el-descriptions-table-border: #444; }
.theme-dark .el-tabs__item { color: #aaa; }
.theme-dark .el-tabs__item.is-active { color: #90caf9; }
.theme-dark .el-form-item__label { color: #ccc; }
.theme-dark .el-dropdown-menu { background-color: #222244; border-color: #444; }
.theme-dark .el-dropdown-menu__item { color: #ccc; }
.theme-dark .el-empty__description p { color: #888; }
.theme-dark .el-alert { --el-alert-bg-color: #2a2a4a; }
.theme-dark .el-radio__label { color: #ccc; }
.theme-dark .el-message-box { background-color: #222244; }
.theme-dark .el-pagination { --el-pagination-bg-color: #222244; }

.theme-purple {
  --bg-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --header-bg: rgba(255,255,255,0.95);
  --footer-bg: rgba(255,255,255,0.9);
  --footer-text: #666;
  --footer-border: #e0e0e0;
  --card-bg: #ffffff;
  --text-primary: #1a1a2e;
  --text-secondary: #555;
}
</style>

<style scoped>
.app-container { height: 100vh; background: var(--bg-gradient); display: flex; flex-direction: column; }
.app-header { background: var(--header-bg); display: flex; align-items: center; padding: 0 24px; gap: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); z-index: 10; }
.logo { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: bold; color: #667eea; cursor: pointer; flex-shrink: 0; }
.nav-tabs { flex: 1; }
.header-settings { display: flex; gap: 4px; flex-shrink: 0; }
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
.tpl-desc { font-size: 12px; color: var(--text-secondary); text-align: center; margin-bottom: 12px; }
.tpl-tags { display: flex; justify-content: center; gap: 4px; flex-wrap: wrap; }
.fav-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.fav-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.fav-date { font-size: 12px; color: var(--text-secondary); }
.fav-title { font-weight: bold; margin-bottom: 8px; }
.fav-content { font-size: 12px; color: var(--text-secondary); line-height: 1.5; margin-bottom: 12px; max-height: 80px; overflow: hidden; }
.fav-actions { display: flex; gap: 8px; }
.detail-content { white-space: pre-wrap; word-wrap: break-word; background: #f5f7fa; padding: 12px; border-radius: 4px; font-size: 13px; max-height: 300px; overflow-y: auto; }
/* Home Page */
.home-page { padding: 0 !important; background: transparent; }
.home-page > * { margin-bottom: 0; }
.faq-section { max-width: 800px; margin: 60px auto 0; padding: 0 40px; }
.faq-section .section-title { font-size: 32px; font-weight: 700; text-align: center; margin-bottom: 32px; color: var(--text-primary); }
.faq-section :deep(.el-collapse) { border: none; }
.faq-section :deep(.el-collapse-item) { margin-bottom: 12px; }
.faq-section :deep(.el-collapse-item__header) { 
  font-size: 16px; 
  font-weight: 600; 
  padding: 20px 24px; 
  border-radius: 12px;
  background: var(--card-bg);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}
.faq-section :deep(.el-collapse-item__header:hover) { 
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}
.faq-section :deep(.el-collapse-item__wrap) { 
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
  background: var(--card-bg);
  margin-top: -8px;
  padding-top: 8px;
}
.faq-section :deep(.el-collapse-item__content) { 
  padding: 16px 24px 24px; 
  font-size: 15px; 
  color: var(--text-secondary); 
  line-height: 1.6; 
}
/* Footer */
.app-footer { padding: 0; flex-shrink: 0; }
.footer-content { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; background: var(--footer-bg); border-top: 1px solid var(--footer-border); color: var(--footer-text); font-size: 13px; }
.footer-left { flex-shrink: 0; }
.footer-center { flex-shrink: 0; }
.footer-right { flex-shrink: 0; }
</style>
