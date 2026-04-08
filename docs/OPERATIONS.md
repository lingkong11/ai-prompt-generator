# AI Prompt Generator - Sprint 2 定价模块
## 操作手册

**版本**: v1.0  
**日期**: 2026-04-08  
**适用版本**: v1.2.0+

---

## 一、功能概述

Sprint 2 定价模块实现了完整的付费订阅体系，包括：

- **三档套餐**: 免费版(Free) / 专业版(Pro) / 团队版(Team)
- **配额系统**: 生成次数限制、收藏数量限制
- **定价页面**: 套餐展示、功能对比、FAQ
- **订阅管理**: 升级、降级、取消订阅

---

## 二、套餐体系

| 套餐 | 价格 | 生成配额 | 收藏配额 | 特色功能 |
|------|------|----------|----------|----------|
| **Free** | ¥0 | 20次/日 | 50条 | 基础模板 |
| **Pro** | ¥29/月 | 无限 | 无限 | 导出、优先支持 |
| **Team** | ¥99/月 | 无限 | 无限 | API、团队协作 |

**年付优惠**: 8折 (Pro 省¥69/年, Team 省¥289/年)

---

## 三、用户操作指南

### 3.1 查看定价
1. 访问首页 `http://localhost:3001`
2. 点击导航栏"定价"或访问 `/pricing`
3. 查看三档套餐对比

### 3.2 升级套餐
1. 登录账号
2. 进入定价页 `/pricing`
3. 选择 Pro 或 Team 套餐
4. 选择月付/年付
5. 点击"立即升级"
6. 确认升级（当前为 Mock，实际需支付）

### 3.3 查看当前套餐
1. 登录后点击头像
2. 选择"个人资料"
3. 查看"当前套餐"信息

### 3.4 取消订阅
1. 进入个人资料页
2. 点击"管理订阅"
3. 点击"取消订阅"
4. 确认取消

**注意**: 取消后仍可使用到当期结束

---

## 四、管理员操作

### 4.1 查看套餐配置
```sql
SELECT * FROM subscription_plans;
```

### 4.2 修改套餐价格
```sql
UPDATE subscription_plans 
SET price_monthly = 39.00, price_yearly = 379.00 
WHERE code = 'PRO';
```

### 4.3 查看用户订阅
```sql
SELECT u.username, p.name, s.status, s.expires_at
FROM user_subscriptions s
JOIN users u ON s.user_id = u.id
JOIN subscription_plans p ON s.plan_id = p.id;
```

### 4.4 查看配额使用
```sql
SELECT u.username, q.usage_date, q.generate_count, q.favorite_count
FROM quota_usage q
JOIN users u ON q.user_id = u.id
WHERE q.usage_date = CURRENT_DATE;
```

---

## 五、常见问题

### Q1: 免费用户配额何时重置？
**A**: 每日 00:00 (服务器时区) 自动重置

### Q2: 升级后多久生效？
**A**: 立即生效，刷新页面即可看到新配额

### Q3: 可以降级吗？
**A**: 可以，降级在当前计费周期结束后生效

### Q4: 如何申请退款？
**A**: 购买后7天内联系客服申请全额退款

### Q5: 企业定制如何联系？
**A**: 定价页底部点击"联系销售"填写表单

---

## 六、故障排查

### 问题1: 配额未正确扣除
**检查**:
```sql
SELECT * FROM quota_usage WHERE user_id = ? AND usage_date = CURRENT_DATE;
```

### 问题2: 订阅状态不正确
**检查**:
```sql
SELECT * FROM user_subscriptions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1;
```

### 问题3: 套餐未显示
**检查**:
```sql
SELECT * FROM subscription_plans WHERE is_active = true;
```

---

## 七、更新日志

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.2.0 | 2026-04-08 | Sprint 2 定价模块上线 |

---

**文档维护**: 马可行  
**最后更新**: 2026-04-08
