# AI Prompt Generator - Sprint 2 定价模块
## 运维手册

**版本**: v1.0  
**日期**: 2026-04-08  
**适用环境**: 生产环境

---

## 一、系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Vue 3 前端    │────▶│  Spring Boot    │────▶│   PostgreSQL    │
│   (port 3001)   │     │   (port 8082)   │     │   (port 5432)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │
         │                       │
    ┌────▼────┐            ┌────▼────┐
    │  Nginx  │            │  Redis  │
    │(生产)   │            │(可选)   │
    └─────────┘            └─────────┘
```

---

## 二、部署步骤

### 2.1 环境要求
- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.9+

### 2.2 后端部署
```bash
# 1. 编译打包
cd server
mvn clean package -DskipTests

# 2. 启动服务
java -jar target/prompt-generator-1.0.0.jar

# 3. 验证健康
curl http://localhost:8082/api/prompt/health
```

### 2.3 前端部署
```bash
# 1. 安装依赖
cd client
npm install

# 2. 构建生产包
npm run build

# 3. 部署到 Nginx
cp -r dist/* /var/www/html/
```

### 2.4 数据库初始化
```sql
-- 套餐数据自动初始化 (DataInitializer)
-- 无需手动插入
```

---

## 三、配置说明

### 3.1 后端配置 (application.yml)
```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/prompt-generator
    username: postgres
    password: ${POSTGRES_PASSWORD}

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

### 3.2 前端配置 (vite.config.ts)
```typescript
server: {
  port: 3001,
  proxy: {
    '/api': {
      target: 'http://localhost:8082',
      changeOrigin: true
    }
  }
}
```

---

## 四、监控指标

### 4.1 关键指标
| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| 订阅转化率 | 付费用户/总用户 | < 5% |
| 配额使用率 | 已用/上限 | > 80% |
| API 响应时间 | P99 延迟 | > 500ms |
| 错误率 | 5xx 比例 | > 1% |

### 4.2 监控 SQL
```sql
-- 今日订阅转化
SELECT 
  COUNT(CASE WHEN plan_id > 1 THEN 1 END) * 100.0 / COUNT(*) as conversion_rate
FROM user_subscriptions
WHERE created_at::date = CURRENT_DATE;

-- 配额使用 TOP 10
SELECT user_id, generate_count, favorite_count
FROM quota_usage
WHERE usage_date = CURRENT_DATE
ORDER BY generate_count DESC
LIMIT 10;
```

---

## 五、备份策略

### 5.1 数据库备份
```bash
# 每日全量备份
pg_dump -U postgres prompt-generator > backup_$(date +%Y%m%d).sql

# 保留30天
find /backup -name "backup_*.sql" -mtime +30 -delete
```

### 5.2 关键表备份
- `subscription_plans` - 套餐配置
- `user_subscriptions` - 用户订阅
- `quota_usage` - 配额使用记录

---

## 六、故障处理

### 6.1 后端无法启动
**排查步骤**:
1. 检查 Java 版本: `java -version` (需 17+)
2. 检查端口占用: `netstat -tlnp | grep 8082`
3. 检查数据库连接: `psql -U postgres -d prompt-generator`
4. 查看日志: `tail -f logs/spring.log`

### 6.2 前端无法访问
**排查步骤**:
1. 检查 Nginx 状态: `systemctl status nginx`
2. 检查静态文件: `ls /var/www/html/`
3. 检查 API 代理: `curl http://localhost:8082/api/prompt/health`

### 6.3 配额异常
**排查步骤**:
```sql
-- 查看用户配额
SELECT * FROM quota_usage WHERE user_id = ?;

-- 重置配额（谨慎使用）
UPDATE quota_usage SET generate_count = 0, favorite_count = 0 
WHERE user_id = ? AND usage_date = CURRENT_DATE;
```

---

## 七、安全加固

### 7.1 JWT 密钥
```bash
# 生成强密钥
openssl rand -base64 64
```

### 7.2 数据库安全
```sql
-- 创建专用用户
CREATE USER prompt_app WITH PASSWORD 'strong_password';
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO prompt_app;
```

### 7.3 API 限流
```yaml
# application.yml
spring:
  mvc:
    async:
      request-timeout: 30000
```

---

## 八、升级指南

### 8.1 从 v1.1.x 升级到 v1.2.0
```bash
# 1. 备份数据库
pg_dump -U postgres prompt-generator > backup_v1.1.sql

# 2. 拉取新代码
git pull origin main

# 3. 数据库迁移（自动）
# JPA ddl-auto=update 会自动创建新表

# 4. 重启服务
systemctl restart prompt-generator
```

---

## 九、联系方式

- **技术支持**: support@example.com
- **紧急联系**: +86-xxx-xxxx-xxxx
- **文档维护**: 马可行

---

**最后更新**: 2026-04-08
