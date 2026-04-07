# Phase 3 技术设计文档
> 版本: v1.0 | 日期: 2026-04-07 | 负责人: 马可行
> Git分支: phase3-openclaw | 目标交付: 2026-04-12

---

## 一、Phase 3 目标

### 1.1 核心目标

| 目标 | 说明 | 竞品对比 |
|------|------|----------|
| **OpenClaw专用Tab** | 14个Agent提示词模板，竞品绝对空白 | 🔴 无竞品有 |
| **模板库Tab** | 独立Tab，分类浏览+搜索（Phase 0原有功能恢复） | 🟡 基础 |
| **提示词编辑** | 完整CRUD，补充Update功能 | ⚠️ 缺失 |
| **Markdown渲染** | 代码高亮+复制按钮 | 🟡 基础 |
| **AI模型切换** | DeepSeek / OpenAI 多引擎 | 🟡 基础 |
| **评分系统** | 1-5星评分反馈 | 🟡 基础 |

### 1.2 验收标准

- [ ] OpenClaw专用Tab显示14个模板，可预览/使用/收藏
- [ ] 模板库Tab独立运行，分类浏览+搜索
- [ ] 提示词完整Update（编辑标题/内容/类型/标签）
- [ ] 生成结果Markdown渲染（代码高亮）
- [ ] AI模型选择下拉框
- [ ] 14/14现有测试继续通过
- [ ] Phase 3 新增测试 10/10 通过

---

## 二、后端改动

### 2.1 新增实体

#### OpenClawPrompt（专属提示词模板）

```java
@Entity
@Table(name = "openclaw_prompts")
public class OpenClawPrompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 唯一标识符，如 "requirement_analysis_agent"
    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;          // 显示名称

    private String icon;           // emoji图标
    private String category;       // 分类：agent/workflow/tool
    private String stage;         // 所属阶段：1-9 或 tool
    private String content;       // 提示词正文
    private String description;    // 简短描述
    private Integer useCount;     // 使用次数
    private Double rating;        // 平均评分
    private Integer ratingCount;   // 评分人数
    private Boolean featured;      // 是否推荐

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

#### PromptRating（评分记录）

```java
@Entity
@Table(name = "prompt_ratings")
public class PromptRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long promptId;

    @Column(nullable = false)
    private Long userId;

    private Integer rating;        // 1-5分

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

#### PromptVersion（版本历史）

```java
@Entity
@Table(name = "prompt_versions")
public class PromptVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long promptId;

    @Column(columnDefinition = "TEXT")
    private String content;        // 快照内容

    private Integer version;        // 版本号 1,2,3...

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### 2.2 新增Repository

```java
// OpenClaw提示词模板库
public interface OpenClawPromptRepository extends JpaRepository<OpenClawPrompt, Long> {
    List<OpenClawPrompt> findAllByOrderByUseCountDesc();
    List<OpenClawPrompt> findByCategory(String category);
    Optional<OpenClawPrompt> findByCode(String code);
}

// 评分
public interface PromptRatingRepository extends JpaRepository<PromptRating, Long> {
    Optional<PromptRating> findByPromptIdAndUserId(Long promptId, Long userId);
    Double getAverageRatingByPromptId(Long promptId);
}

// 版本历史
public interface PromptVersionRepository extends JpaRepository<PromptVersion, Long> {
    List<PromptVersion> findByPromptIdOrderByVersionDesc(Long promptId);
    @Query("SELECT MAX(pv.version) FROM PromptVersion pv WHERE pv.promptId = :promptId")
    Integer findMaxVersionByPromptId(@Param("promptId") Long promptId);
}
```

### 2.3 新增Service

#### OpenClawPromptService

```java
public interface OpenClawPromptService {
    List<OpenClawPrompt> getAllPrompts();
    List<OpenClawPrompt> getByCategory(String category);
    OpenClawPrompt getByCode(String code);
    void usePrompt(String code);  // 增加使用次数
    void ratePrompt(Long promptId, Integer rating, Long userId);
}
```

#### PromptService 改动

新增方法：
```java
public interface PromptService {
    // 现有方法...

    // Phase 3新增
    Prompt updatePrompt(Long id, PromptUpdateRequest request, Long userId);
    List<PromptVersion> getVersionHistory(Long promptId, Long userId);
    String renderMarkdown(String content);  // Markdown渲染
    void toggleFavorite(Long promptId, Long userId);  // 已实现
}
```

### 2.4 新增Controller

#### OpenClawPromptController

```
GET  /api/openclaw/prompts              # 获取所有模板（分页+分类过滤）
GET  /api/openclaw/prompts/{code}       # 按code获取单个模板
POST /api/openclaw/prompts/{code}/use  # 标记使用+增加计数
POST /api/openclaw/prompts/{code}/rate  # 评分
```

#### TemplateController（模板库）

```
GET  /api/templates                     # 获取所有模板（分页）
GET  /api/templates/categories           # 获取分类列表
GET  /api/templates/search?q=keyword     # 搜索模板
POST /api/templates/{id}/favorite        # 收藏模板
```

### 2.5 数据初始化

启动时自动插入14个OpenClaw提示词模板（使用@PostConstruct或CommandLineRunner）：

```java
@Bean
public CommandLineRunner initOpenClawPrompts(OpenClawPromptRepository repo) {
    return args -> {
        if (repo.count() == 0) {
            // 初始化14个模板
            repo.save(createPrompt("requirement_analysis_agent", "需求分析Agent", ...));
            repo.save(createPrompt("tech_design_agent", "技术设计Agent", ...));
            // ... 共14个
        }
    };
}
```

---

## 三、前端改动

### 3.1 新增路由

```typescript
// router/index.ts
{
  path: '/',
  component: () => import('@/App.vue'),
  children: [
    { path: '', redirect: '/generate' },
    { path: 'generate', name: 'Generate', component: () => import('@/views/HomeView.vue') },
    { path: 'templates', name: 'Templates', component: () => import('@/views/TemplatesView.vue') },  // 新增
    { path: 'openclaw', name: 'OpenClaw', component: () => import('@/views/OpenClawView.vue') },    // 新增
    { path: 'prompts', name: 'Prompts', component: () => import('@/views/HomeView.vue') },
    { path: 'categories', name: 'Categories', component: () => import('@/views/HomeView.vue') },
    { path: 'favorites', name: 'Favorites', component: () => import('@/views/HomeView.vue') },
  ]
}
```

### 3.2 新增API封装

```typescript
// api/openclaw.ts
import axios from 'axios'

export const getOpenClawPrompts = (params?: { category?: string }) =>
  axios.get('/api/openclaw/prompts', { params })

export const getOpenClawPrompt = (code: string) =>
  axios.get(`/api/openclaw/prompts/${code}`)

export const useOpenClawPrompt = (code: string) =>
  axios.post(`/api/openclaw/prompts/${code}/use`)

export const ratePrompt = (promptId: number, rating: number) =>
  axios.post(`/api/openclaw/prompts/${promptId}/rate`, { rating })

// api/templates.ts
export const getTemplates = (params?: { page?: number; size?: number }) =>
  axios.get('/api/templates', { params })

export const getTemplateCategories = () =>
  axios.get('/api/templates/categories')

export const searchTemplates = (q: string) =>
  axios.get('/api/templates/search', { params: { q } })
```

### 3.3 新增页面

#### OpenClawView.vue（核心差异化）

```vue
<template>
  <div class="openclaw-page">
    <!-- 九阶段侧边栏 -->
    <el-aside width="200px" class="stage-sidebar">
      <el-menu :default-active="activeStage" @select="selectStage">
        <el-menu-item index="all">
          <span>🤖 全部</span>
        </el-menu-item>
        <el-divider>九阶段</el-divider>
        <el-menu-item v-for="stage in stages" :key="stage.num" :index="String(stage.num)">
          <span>{{ stage.icon }} {{ stage.name }}</span>
        </el-menu-item>
        <el-divider>工具集</el-divider>
        <el-menu-item v-for="tool in tools" :key="tool.code" :index="tool.code">
          <span>🔧 {{ tool.name }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容 -->
    <el-main class="prompt-content">
      <!-- 顶部说明 -->
      <div class="page-header">
        <h2>🤖 OpenClaw 专用提示词</h2>
        <p>9阶段闭环Agent工作流 + 5个开发工具集，专为软件工程场景设计</p>
      </div>

      <!-- 提示词卡片网格 -->
      <div class="prompt-grid">
        <el-card v-for="prompt in filteredPrompts" :key="prompt.id" class="prompt-card">
          <template #header>
            <div class="card-header">
              <span class="prompt-name">{{ prompt.icon }} {{ prompt.name }}</span>
              <el-tag size="small" :type="getStageType(prompt.stage)">{{ getStageLabel(prompt.stage) }}</el-tag>
            </div>
          </template>

          <div class="prompt-description">{{ prompt.description }}</div>

          <!-- 使用统计 -->
          <div class="prompt-stats">
            <span>使用 {{ prompt.useCount || 0 }} 次</span>
            <span>⭐ {{ (prompt.rating || 0).toFixed(1) }}</span>
          </div>

          <template #footer>
            <div class="card-actions">
              <el-button type="primary" size="small" @click="usePrompt(prompt)">
                🚀 使用
              </el-button>
              <el-button size="small" @click="previewPrompt(prompt)">
                👁️ 预览
              </el-button>
              <el-button size="small" @click="copyPrompt(prompt)">
                📋 复制
              </el-button>
            </div>
          </template>
        </el-card>
      </div>
    </el-main>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="提示词预览" width="700px">
      <div class="markdown-content" v-html="renderedContent"></div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="usePrompt(previewingPrompt)">🚀 使用</el-button>
      </template>
    </el-dialog>

    <!-- 评分对话框 -->
    <el-dialog v-model="ratingVisible" title="评分" width="400px">
      <div class="rating-panel">
        <p>这个提示词对你有帮助吗？</p>
        <el-rate v-model="newRating" show-text :texts="['很差', '较差', '一般', '较好', '很好']" />
      </div>
      <template #footer>
        <el-button @click="ratingVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRating">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
```

#### TemplatesView.vue（模板库）

```vue
<template>
  <div class="templates-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索模板..."
        prefix-icon="Search"
        clearable
        size="large"
        @input="debouncedSearch"
      />
    </div>

    <!-- 分类标签 -->
    <div class="category-tags">
      <el-tag
        v-for="cat in categories"
        :key="cat"
        :type="activeCategory === cat ? 'primary' : 'info'"
        @click="selectCategory(cat)"
        class="category-tag"
      >
        {{ cat }}
      </el-tag>
    </div>

    <!-- 模板网格 -->
    <div class="template-grid">
      <el-card v-for="tpl in templates" :key="tpl.id" class="template-card">
        <!-- 内容 -->
      </el-card>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="totalPages > 1"
      layout="prev, pager, next"
      :total="total"
      :page-size="20"
      v-model:current-page="currentPage"
      @current-change="loadTemplates"
    />
  </div>
</template>
```

### 3.4 HomeView.vue 改动

新增Tab支持（generate/prompts/categories/favorites已有，新增）：
- 路由 query 参数 `?tab=openclaw` → 显示 OpenClawView
- 路由 query 参数 `?tab=templates` → 显示 TemplatesView

```typescript
const tabNames = ['generate', 'prompts', 'categories', 'favorites', 'openclaw', 'templates']
```

---

## 四、Markdown渲染方案

使用 `marked` + `highlight.js`：

```typescript
// utils/markdown.ts
import { marked } from 'marked'
import hljs from 'highlight.js'

// 配置 marked
marked.setOptions({
  highlight: (code: string, lang: string) => {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

export const renderMarkdown = (content: string): string => {
  return marked.parse(content) as string
}
```

安装依赖：
```bash
npm install marked highlight.js
```

---

## 五、AI模型切换方案

### 5.1 后端模型抽象

```java
public interface LLMProvider {
    String generate(String prompt, GenerateRequest request);
    String providerName();
}

@Service
public class DeepSeekProvider implements LLMProvider {
    @Override
    public String generate(String prompt, GenerateRequest request) {
        // 调用DeepSeek API
    }
    @Override
    public String providerName() { return "DeepSeek"; }
}

@Service
public class OpenAIProvider implements LLMProvider {
    @Override
    public String generate(String prompt, GenerateRequest request) {
        // 调用OpenAI API
    }
    @Override
    public String providerName() { return "OpenAI"; }
}
```

### 5.2 动态选择

```java
@Service
@RequiredArgsConstructor
public class LLMService {
    private final DeepSeekProvider deepSeek;
    private final OpenAIProvider openAI;

    public String generate(String prompt, String provider) {
        return switch (provider) {
            case "openai" -> openAI.generate(prompt, request);
            case "deepseek", default -> deepSeek.generate(prompt, request);
        };
    }

    public List<ModelInfo> getAvailableModels() {
        return List.of(
            new ModelInfo("deepseek", "DeepSeek V3", deepSeek.isConfigured()),
            new ModelInfo("openai", "GPT-4o", openAI.isConfigured())
        );
    }
}
```

### 5.3 前端模型选择

```vue
<el-select v-model="selectedModel" placeholder="选择AI模型">
  <el-option
    v-for="m in availableModels"
    :key="m.id"
    :label="m.name + (m.configured ? '' : ' (未配置)')"
    :value="m.id"
    :disabled="!m.configured"
  />
</el-select>
```

---

## 六、数据库迁移

### 6.1 新增表

```sql
-- OpenClaw专属提示词模板
CREATE TABLE openclaw_prompts (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(10),
    category VARCHAR(50),
    stage VARCHAR(20),
    content TEXT NOT NULL,
    description VARCHAR(500),
    use_count INT DEFAULT 0,
    rating DECIMAL(3,2),
    rating_count INT DEFAULT 0,
    featured BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 提示词评分
CREATE TABLE prompt_ratings (
    id BIGSERIAL PRIMARY KEY,
    prompt_id BIGINT NOT NULL REFERENCES prompts(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(prompt_id, user_id)
);

-- 提示词版本历史
CREATE TABLE prompt_versions (
    id BIGSERIAL PRIMARY KEY,
    prompt_id BIGINT NOT NULL REFERENCES prompts(id),
    content TEXT NOT NULL,
    version INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ALTER TABLE prompts ADD COLUMN description TEXT;
-- ALTER TABLE prompts ADD COLUMN language VARCHAR(10) DEFAULT 'zh';
```

---

## 七、测试计划

### 7.1 新增测试用例

| 编号 | 测试项 | 方法 | 预期 |
|------|--------|------|------|
| T15 | 获取OpenClaw模板列表 | GET /api/openclaw/prompts | 返回14个模板 |
| T16 | 按分类获取模板 | GET /api/openclaw/prompts?category=agent | 返回对应分类 |
| T17 | OpenClaw模板使用计数 | POST /api/openclaw/prompts/{code}/use | useCount+1 |
| T18 | 模板评分 | POST /api/openclaw/prompts/{id}/rate | 评分成功 |
| T19 | 提示词编辑 | PUT /api/prompts/{id} | 内容更新 |
| T20 | 版本历史 | GET /api/prompts/{id}/versions | 返回版本列表 |
| T21 | 模板库列表 | GET /api/templates | 分页返回模板 |
| T22 | 模板搜索 | GET /api/templates/search?q=coding | 返回搜索结果 |
| T23 | 获取可用模型 | GET /api/models | 返回模型列表 |
| T24 | 模型切换生成 | POST /api/prompt/generate (model=openai) | 正常生成 |

### 7.2 回归测试

所有Phase 1/2的14项测试继续通过（T1-T14）。

---

## 八、文件变更清单

### 后端新增
```
server/src/main/java/com/ai/prompt/
├── entity/
│   ├── OpenClawPrompt.java      [新增]
│   ├── PromptRating.java       [新增]
│   └── PromptVersion.java       [新增]
├── repository/
│   ├── OpenClawPromptRepository.java [新增]
│   ├── PromptRatingRepository.java   [新增]
│   └── PromptVersionRepository.java   [新增]
├── service/
│   ├── OpenClawPromptService.java     [新增]
│   └── impl/
│       └── OpenClawPromptServiceImpl.java [新增]
├── controller/
│   ├── OpenClawPromptController.java   [新增]
│   └── TemplateController.java        [新增]
└── dto/
    ├── OpenClawPromptResponse.java    [新增]
    ├── PromptRatingRequest.java        [新增]
    └── ModelInfo.java                  [新增]
```

### 前端新增
```
client/src/
├── api/
│   ├── openclaw.ts           [新增]
│   └── templates.ts          [新增]
├── views/
│   ├── OpenClawView.vue      [新增 - 核心差异化]
│   └── TemplatesView.vue      [新增]
└── utils/
    └── markdown.ts           [新增]
```

### 后端修改
```
server/src/main/java/com/ai/prompt/
├── entity/Prompt.java         [新增version字段]
├── repository/PromptRepository.java [新增update方法]
├── service/PromptService.java [新增updatePrompt方法]
├── controller/PromptManageController.java [新增PUT端点]
└── resources/application.yml  [新增OpenAI配置项]
```

### 前端修改
```
client/src/
├── App.vue         [增加openclaw/templates Tab]
├── HomeView.vue    [增加Tab路由支持]
└── router/index.ts [增加openclaw/templates路由]
```

---

## 九、排期（6天）

| 天 | 任务 | 交付 |
|----|------|------|
| Day 1 (04-07) | 技术方案设计+14个提示词内容 | 本文档 |
| Day 2 (04-08) | OpenClaw实体+Repository+Service | 后端基础 |
| Day 3 (04-09) | OpenClaw Controller + 前端OpenClawView | 前后端集成 |
| Day 4 (04-10) | 提示词Update + Markdown渲染 + 版本历史 | 编辑功能 |
| Day 5 (04-11) | AI模型切换 + 模板库Tab + 评分系统 | 模型+评分 |
| Day 6 (04-12) | 集成测试 + 文档 + Git合并 | Phase 3完成 |

---

**文档状态**: ✅ 设计完成  
**下一步**: 马可行开始Day 2开发  
**Git分支**: phase3-openclaw
