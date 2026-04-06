# AI Prompt Generator

> AI提示词生成器 - 支持OpenClaw专用提示词

## ✨ 核心亮点

### 1. OpenClaw专用提示词（核心差异化）
专为AI Agent设计的九阶段工作流提示词：
- 📋 需求分析提示词
- 🎨 技术设计提示词  
- 💻 代码开发提示词
- 🧪 功能测试提示词
- 📊 项目管理提示词
- 📚 文档输出提示词
- 🚀 项目交付提示词

### 2. 智能提示词生成
输入目标，自动生成优化提示词：
- 支持多种类型：写作、编程、分析、Agent
- 可选风格：专业、简洁、风趣、技术
- 中英文支持

### 3. 预置模板库
- 5+ 预置模板
- 分类管理
- 一键使用

## 🚀 快速开始

### 后端启动
```bash
cd server
mvn spring-boot:run
# 访问: http://localhost:8081
```

### 前端启动
```bash
cd client
npm install
npm run dev
# 访问: http://localhost:3001
```

## 📁 项目结构

```
ai-prompt-generator/
├── server/                 # Spring Boot后端
│   └── src/main/java/com/ai/prompt/
│       ├── PromptGeneratorApplication.java
│       ├── controller/
│       │   └── PromptController.java
│       ├── service/
│       │   └── PromptService.java
│       └── entity/
│           └── PromptTemplate.java
├── client/                  # Vue 3前端
│   └── src/
│       ├── App.vue
│       ├── main.ts
│       └── router/
├── docs/
│   └── SPEC.md             # 需求规格文档
└── README.md
```

## 🎯 使用场景

### 场景1：生成标准提示词
1. 输入目标描述
2. 选择类型（通用/写作/编程/分析）
3. 选择风格和语言
4. 点击生成

### 场景2：使用OpenClaw专用提示词
1. 切换到"OpenClaw专用"标签
2. 选择工作流阶段（需求分析/技术设计/代码开发...）
3. 自动填充到生成器
4. 复制使用

### 场景3：使用模板
1. 切换到"模板库"标签
2. 点击模板卡片
3. 自动跳转到生成页面并应用模板

## 🔧 配置说明

### AI API（可选）
在 `application.yml` 中配置：
```yaml
ai:
  api:
    url: https://api.deepseek.com/v1/chat/completions
    key: ${AI_API_KEY:}  # 填入你的API Key
  model: deepseek-chat
```

不配置时使用本地模板生成。

## 📄 文档

- [需求规格文档](docs/SPEC.md)

## 🎨 界面预览

- 提示词生成页面：输入目标，选择类型，生成优化提示词
- 模板库页面：浏览预置模板，一键使用
- OpenClaw专用页面：九阶段工作流专用提示词
- 收藏页面：管理和使用收藏的提示词

## License

MIT
