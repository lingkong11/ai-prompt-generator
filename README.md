# AI Prompt Generator

> AI 提示词生成器 — 支持 DeepSeek 大模型 + 本地模板双引擎

## 功能特性

- 🔮 **提示词生成**：输入目标描述，自动生成高质量 Prompt
- 🤖 **AI 大模型**：接入 DeepSeek API，支持 OpenAI 兼容协议
- 📝 **本地模板**：无需 API Key 也可使用内置模板生成
- 📁 **提示词管理**：保存、搜索、分类、收藏
- 🔒 **用户认证**：JWT 无状态认证，数据按用户隔离
- 🌐 **中英双语**：支持中文和英文输出

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Element Plus + Pinia |
| 后端 | Spring Boot 3.2 + Spring Security + JPA |
| 数据库 | H2（开发）/ PostgreSQL（生产） |
| AI | DeepSeek API（OpenAI 兼容协议） |

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- Maven 3.8+

### 后端启动

```bash
cd server

# 配置 AI API（可选，不配置则使用本地模板）
# 方式一：环境变量
set DP_AI_API_KEY=sk-your-api-key

# 方式二：application.yml
# ai:
#   api:
#     key: sk-your-api-key
#     url: https://api.deepseek.com/v1/chat/completions

mvn spring-boot:run
```

后端默认运行在 `http://localhost:8082`

### 前端启动

```bash
cd client
npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`，API 请求自动代理到后端。

### 运行测试

```bash
# 启动后端后执行
python verify_utf8.py
```

预期输出 14/14 全部通过。

## 配置说明

### application.yml 关键配置

```yaml
# AI 大模型配置
ai:
  provider: deepseek          # 提供商标识（可扩展 openai、claude 等）
  api:
    url: https://api.deepseek.com/v1/chat/completions
    key: ${DP_AI_API_KEY:}    # 优先读环境变量
  model: deepseek-chat
  api:
    timeout: 30000

# JWT 配置
jwt:
  secret: your-production-secret-key-at-least-256-bits
  expiration: 86400000        # 24 小时（毫秒）
```

## API 概览

| 接口 | 方法 | 说明 | 认证 |
|------|------|------|------|
| `/api/auth/register` | POST | 注册 | 否 |
| `/api/auth/login` | POST | 登录 | 否 |
| `/api/auth/me` | GET | 当前用户 | 是 |
| `/api/prompt/generate` | POST | 生成提示词 | 否 |
| `/api/prompt/templates` | GET | 预置模板 | 否 |
| `/api/prompt/health` | GET | 健康检查 | 否 |
| `/api/prompts` | GET/POST | 提示词列表/创建 | 是 |
| `/api/prompts/{id}` | GET/PUT/DELETE | 提示词详情/更新/删除 | 是 |
| `/api/prompts/{id}/favorite` | POST | 切换收藏 | 是 |
| `/api/categories` | GET/POST | 分类列表/创建 | 是 |

所有接口统一返回格式：

```json
{
  "code": 0,
  "message": "ok",
  "data": { ... },
  "timestamp": "2026-04-07 22:30:00"
}
```

## 项目结构

详见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

## 扩展指南

### 新增 AI 提供商

1. 在 `service/ai/` 下新建类实现 `AiProvider` 接口
2. 添加 `@Service` 和 `@ConditionalOnProperty(name = "ai.provider", havingValue = "your-provider")`
3. 修改配置 `ai.provider=your-provider`

示例：

```java
@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiProvider implements AiProvider {
    // 实现 generate(), test(), isConfigured(), providerName()
}
```

## 文档

- [测试报告](docs/TEST_REPORT.md)
- [架构设计](docs/ARCHITECTURE.md)
