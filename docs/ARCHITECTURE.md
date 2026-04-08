# AI Prompt Generator — 技术设计文档（v2.0 重构版）

> 版本：2.0 | 更新日期：2026-04-08
> 作者：马可行（AI 重构工程 Agent）

---

## 1. 项目概述

AI Prompt Generator 是一个基于 Spring Boot + Vue3 的全栈提示词管理平台，提供：
- AI 驱动的提示词生成（DeepSeek API + 本地模板降级）
- 提示词的 CRUD、收藏、搜索、分类管理
- 14 个 OpenClaw 专用 Agent 模板展示
- JWT 无状态认证 + 用户数据隔离

## 2. 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.0 / Spring Security / Spring Data JPA |
| 数据库 | PostgreSQL（持久化） |
| 认证 | JWT（jjwt 0.12.3）+ BCrypt 密码哈希 |
| AI 集成 | DeepSeek API（OpenAI 兼容协议） |
| 前端框架 | Vue 3 + TypeScript + Element Plus |
| 构建工具 | Vite（前端）/ Maven（后端） |

## 3. 系统架构

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Vue 3 SPA │────▶│  Spring Boot │────▶│ PostgreSQL  │
│  Element+   │ API │   :8082      │ JPA │   :5432     │
│  TypeScript │◀────│              │◀────│             │
└─────────────┘     └──────┬───────┘     └─────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  DeepSeek    │
                    │  API (v1)    │
                    └──────────────┘
```

### 3.1 分层架构

```
Controller 层     — 参数校验（@Valid）、HTTP 适配、统一 ApiResult 响应
    ↓
Service 层        — 业务逻辑、事务管理（@Transactional）、Entity→DTO 转换
    ↓
Repository 层     — Spring Data JPA 接口、JPQL 查询
    ↓
Entity 层          — JPA 实体映射、生命周期回调（@PrePersist/@PreUpdate）
```

### 3.2 设计模式

| 模式 | 应用场景 |
|------|----------|
| **策略模式** | `AiProvider` 接口 — 新增 AI 提供商只需添加实现类 + `@ConditionalOnProperty` |
| **DTO 模式** | `DtoConverter` — Controller 层不直接序列化 Entity，防止懒加载异常和敏感字段泄漏 |
| **模板方法** | `PromptService.generateLocalPrompt()` — 按 type 分派到具体生成方法 |
| **过滤器链** | `JwtAuthenticationFilter` — Spring Security 过滤器链中的 JWT 认证 |

## 4. 核心模块设计

### 4.1 认证模块

```
请求 → JwtAuthenticationFilter → 提取 Bearer Token
     → JwtUtils.validateToken() → 签名+有效期校验
     → UserRepository.findByUsername() → 查库获取 User Entity
     → 写入 SecurityContextHolder → Controller 通过 getPrincipal() 获取用户
```

- **密码安全**：BCrypt 哈希存储，`@JsonIgnore` 防止 Hash 泄漏
- **无状态**：不使用 Session，JWT Token 承载认证信息
- **Token 有效期**：24 小时（可配置）

### 4.2 提示词生成模块

```
用户输入 (goal/type/style/language)
    ↓
PromptService.generatePrompt()
    ↓
AiProvider.isConfigured()? ──YES──→ DeepSeek API 调用
    ↓ NO                                      ↓
本地模板降级生成 ←─── 失败 ←────────────── 成功返回
    ↓
返回生成结果
```

- **降级策略**：AI 调用失败时自动切换到本地模板引擎，保证功能可用
- **本地模板**：支持 writing / coding / analysis / agent / general 五种类型
- **调用统计**：AtomicInteger 线程安全计数（totalCalls / successCalls / fallbackCalls）

### 4.3 DTO 数据流

```
Entity (JPA) → DtoConverter.toPromptDTO() → PromptDTO (Controller 返回)
```

- **PromptDTO**：包含 id / title / content / type / categoryId / categoryName 等展示字段
- **不包含**：User Entity（避免序列化循环）、password hash
- **优势**：API 接口稳定（Entity 字段变更不影响对外契约）、懒加载安全

## 5. API 接口清单

### 5.1 公开接口（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/prompt/health` | 健康检查 |
| GET | `/api/prompt/templates` | 预置模板列表 |
| POST | `/api/prompt/generate` | 生成提示词 |
| POST | `/api/prompt/test` | 测试提示词 |
| GET | `/api/prompt/stats` | 服务统计 |

### 5.2 认证接口（需 Bearer Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/me` | 当前用户信息 |
| GET | `/api/prompts` | 分页查询提示词 |
| GET | `/api/prompts/{id}` | 单条提示词详情 |
| POST | `/api/prompts` | 创建提示词 |
| PUT | `/api/prompts/{id}` | 更新提示词 |
| DELETE | `/api/prompts/{id}` | 删除提示词 |
| POST | `/api/prompts/{id}/favorite` | 切换收藏 |
| GET | `/api/prompts/favorites` | 收藏列表 |
| POST | `/api/prompts/{id}/use` | 递增使用次数 |
| GET | `/api/categories` | 分类列表 |
| POST | `/api/categories` | 创建分类 |
| PUT | `/api/categories/{id}` | 更新分类 |
| DELETE | `/api/categories/{id}` | 删除分类 |

### 5.3 统一响应格式

```json
{
  "code": 0,
  "message": "ok",
  "data": { /* 业务数据 */ },
  "timestamp": "2026-04-08 08:54:00"
}
```

## 6. 数据库设计

### 6.1 用户表 (users)

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT |
| username | VARCHAR | UNIQUE, NOT NULL |
| email | VARCHAR | UNIQUE, NOT NULL |
| password | VARCHAR | NOT NULL (BCrypt Hash) |
| nickname | VARCHAR(20) | |
| avatar | VARCHAR(500) | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 6.2 提示词表 (prompts)

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT |
| title | VARCHAR | NOT NULL |
| content | TEXT | |
| type | VARCHAR(50) | |
| style | VARCHAR(50) | |
| language | VARCHAR(10) | |
| goal | VARCHAR(100) | |
| user_id | BIGINT | FK → users.id |
| category_id | BIGINT | FK → categories.id (nullable) |
| tags | VARCHAR(200) | |
| is_favorite | BOOLEAN | DEFAULT false |
| is_template | BOOLEAN | DEFAULT false |
| use_count | INT | DEFAULT 0 |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### 6.3 分类表 (categories)

| 字段 | 类型 | 约束 |
|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT |
| name | VARCHAR(100) | NOT NULL |
| description | VARCHAR(200) | |
| icon | VARCHAR(50) | |
| parent_id | BIGINT | FK → categories.id (nullable) |
| sort_order | INT | DEFAULT 0 |
| user_id | BIGINT | FK → users.id |
| created_at | TIMESTAMP | |

## 7. 安全设计

| 措施 | 说明 |
|------|------|
| BCrypt 密码哈希 | 强度因子 10，`@JsonIgnore` 防止 Hash 泄漏 |
| JWT 签名 | HMAC-SHA，通过 `jwt.secret` 配置 |
| CORS 策略 | 开发阶段全放开，生产环境需收紧 `allowedOrigins` |
| CSRF 禁用 | 前后端分离 + JWT 无状态，CSRF 无意义 |
| SQL 注入防护 | JPA 参数化查询 + `@Query` 占位符 |
| XSS 防护 | 前端 Element Plus 默认转义 + Vue 模板自动转义 |
| 数据隔离 | 所有 CRUD 接口按 `user.id` 过滤 |

## 8. 前端架构

```
App.vue (布局壳)
├── Header (Logo + Tab 导航 + 用户下拉)
└── Main
    ├── HomeView.vue (提示词生成/管理/分类/收藏)
    │   ├── 生成面板 (表单 + 结果)
    │   ├── 管理列表 (分页表格 + 搜索)
    │   ├── 分类管理 (列表 + 创建)
    │   └── 收藏列表
    └── OpenClawView.vue (14 Agent 模板展示)
```

### 8.1 状态管理

- **authStore (Pinia)**：Token 持久化（localStorage）、用户信息缓存、登录/注册/退出
- **组件状态**：各页面使用 `ref/reactive` 管理局部状态

### 8.2 HTTP 层

- `auth.ts`：axios 实例 + 全局 Token 注入 + 401 自动跳转
- `prompts.ts`：提示词 & 分类 API 封装
- `openclaw.ts`：OpenClaw 模板 Mock 数据（待接入后端）

## 9. 部署说明

### 9.1 后端启动

```bash
# 编译（需 JDK 17）
set JAVA_HOME=C:\Program Files\Java\jdk-17
cd server
mvn clean package -DskipTests

# 启动
java -jar target/prompt-generator-1.0.0.jar
# 服务端口: 8082
```

### 9.2 前端启动

```bash
cd client
npm install
npm run dev
# 开发端口: 3001，自动代理 /api → localhost:8082
```

### 9.3 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `POSTGRES_PASSWORD` | PostgreSQL 密码 | (空) |
| `DP_AI_API_KEY` | DeepSeek API Key | (空) |
| `jwt.secret` | JWT 签名密钥 | `ai-prompt-generator-jwt-secret-key-...` |

## 10. 已知限制与后续优化

| 项目 | 说明 |
|------|------|
| OpenClaw 模板 | 当前为前端 Mock 数据，待接入后端 API |
| 批量删除 | 分类管理页面缺少批量操作 |
| 密码找回 | 未实现密码重置流程 |
| 国际化 | 前端文案硬编码中文，未做 i18n |
| 单元测试 | 缺少 Service 层单元测试 |
| 生产安全 | JWT Secret 需替换为高强度随机值 |
