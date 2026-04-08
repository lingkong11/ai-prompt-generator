# AI Prompt Generator - Sprint 2 UI 设计规范
## 定价模块视觉设计文档

**版本**: v1.0  
**日期**: 2026-04-08  
**设计工具**: Figma / 代码实现  
**状态**: 设计定稿

---

## 一、设计原则

1. **清晰对比**：三个套餐一目了然，差异化突出
2. **引导转化**：Pro 套餐视觉权重最高，引导用户升级
3. **品牌一致**：与现有紫色主题系统保持统一
4. **移动优先**：响应式设计，3列 → 1列

---

## 二、色彩系统

### 2.1 套餐主题色

```css
/* Free 套餐 */
--pricing-free-bg: #ffffff;
--pricing-free-border: #e5e7eb;
--pricing-free-text: #374151;
--pricing-free-badge: #9ca3af;

/* Pro 套餐（视觉焦点）*/
--pricing-pro-bg-start: #667eea;
--pricing-pro-bg-end: #764ba2;
--pricing-pro-text: #ffffff;
--pricing-pro-border: transparent;
--pricing-pro-badge: #fbbf24;  /* 金色推荐徽章 */
--pricing-pro-badge-text: #1f2937;

/* Team 套餐 */
--pricing-team-bg: #1f2937;
--pricing-team-border: #fbbf24;
--pricing-team-text: #ffffff;
--pricing-team-badge: #fbbf24;
```

### 2.2 辅助色

```css
--pricing-save-green: #10b981;
--pricing-save-bg: #d1fae5;
--pricing-check: #667eea;
--pricing-x: #9ca3af;
--pricing-popular-bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
```

---

## 三、Typography

```css
/* Hero 标题 */
font-size: 48px;
font-weight: 700;
line-height: 1.2;
color: var(--text-primary);

/* 套餐名称 */
font-size: 24px;
font-weight: 700;

/* 价格数字 */
font-size: 56px;
font-weight: 800;
line-height: 1;

/* 价格单位 */
font-size: 18px;
font-weight: 500;
opacity: 0.8;

/* 功能列表 */
font-size: 15px;
line-height: 1.6;
```

---

## 四、Spacing 系统

```css
--pricing-section-gap: 80px;    /* 大区块间距 */
--pricing-card-gap: 24px;         /* 卡片间距 */
--pricing-card-padding: 32px;    /* 卡片内边距 */
--pricing-feature-gap: 16px;     /* 功能项间距 */
--pricing-radius: 16px;           /* 卡片圆角 */
```

---

## 五、套餐卡片详细规格

### 5.1 Free 套餐

```
┌──────────────────────────────────┐
│                                  │  padding: 32px
│         [无徽章]                 │
│                                  │
│           Free                   │  font-size: 24px, font-weight: 700
│           ¥0                    │  font-size: 56px, font-weight: 800
│         永久免费                 │  font-size: 14px, opacity: 0.7
│                                  │
│  ─────────────────────────────  │  divider
│                                  │
│    ✓ 每日 20 次生成             │
│    ✓ 收藏 50 条提示词           │
│    ✓ 基础模板库                 │
│    ✗ 导出功能                   │
│    ✗ 优先支持                   │
│                                  │
│       [当前方案]                 │  outline 按钮，禁用状态
│                                  │
└──────────────────────────────────┘

背景: #ffffff
边框: 1px solid #e5e7eb
圆角: 16px
阴影: none（平铺卡片风格）
```

### 5.2 Pro 套餐（推荐）

```
┌──────────────────────────────────┐
│  ★ 最受欢迎                       │  金色背景徽章，固定顶部
├──────────────────────────────────┤
│                                  │
│         ⭐ 推荐                  │
│                                  │
│           Pro                   │
│           ¥29                    │
│         /月                      │
│                                  │
│   ¥279/年 · 节省 ¥69             │  原价 ¥348，绿色字体
│                                  │
│  ─────────────────────────────  │
│                                  │
│    ✓ 无限次数生成               │
│    ✓ 无限收藏                   │
│    ✓ 完整模板库                 │
│    ✓ 导出功能 (PDF/TXT)         │
│    ✓ 优先客服支持               │
│                                  │
│     [立即升级]                   │  渐变按钮，高度 48px
│                                  │
└──────────────────────────────────┘

背景: linear-gradient(135deg, #667eea 0%, #764ba2 100%)
边框: none
圆角: 16px
阴影: 0 20px 40px rgba(102, 126, 234, 0.3)
transform: scale(1.05)  ← 视觉放大
position: relative
z-index: 2
```

### 5.3 Team 套餐

```
┌──────────────────────────────────┐
│                                  │
│         🏢 团队                  │
│                                  │
│           Team                  │
│           ¥99                    │
│         /月                      │
│                                  │
│   ¥899/年 · 节省 ¥289            │
│                                  │
│  ─────────────────────────────  │
│                                  │
│    ✓ 无限次数生成               │
│    ✓ 无限收藏                   │
│    ✓ 完整模板库 + 自定义模板    │
│    ✓ 导出功能                   │
│    ✓ API 访问                   │
│    ✓ 5 人团队协作               │
│    ✓ 专属客户经理               │
│                                  │
│     [立即升级]                   │  深色按钮，金色边框
│                                  │
└──────────────────────────────────┘

背景: #1f2937 (dark)
边框: 2px solid #fbbf24
圆角: 16px
阴影: 0 20px 40px rgba(0, 0, 0, 0.2)
```

---

## 六、月付/年付切换

```
┌──────────────────────────────┐
│   月付   ○──●──○  年付      │  ← 年付8折标签
│         省 20%              │
└──────────────────────────────┘

切换样式:
- 轨道: 40px × 24px
- 圆点: 20px
- 切换动画: 0.3s ease
- 月付: 灰色背景
- 年付: 紫色背景 (#667eea)
```

---

## 七、功能对比表格

```
┌────────────────────────────────────────────────────────────┐
│ 功能               │   Free   │   Pro   │   Team           │
├────────────────────────────────────────────────────────────┤
│ 每日生成次数        │   20次   │   ∞     │   ∞             │
│ 收藏上限           │   50条   │   ∞     │   ∞             │
│ 模板库            │   基础   │   全部   │   全部+自定义    │
│ 导出格式          │   -      │   3种   │   全部          │
│ API 访问          │   -      │   -     │   ✓             │
│ 团队成员          │   1人    │   1人    │   5人           │
│ 优先支持          │   -      │   ✓     │   ✓             │
│ 专属客户经理       │   -      │   -     │   ✓             │
└────────────────────────────────────────────────────────────┘

表格样式:
- 表头: 紫色背景 (#667eea)，白色文字
- Free 列: 浅灰背景
- Pro 列: 浅紫背景 (highlight)
- Team 列: 深灰背景
- ✓ 绿色 (#10b981)
- ✗ 灰色 (#9ca3af)
- ∞ 紫色 (#667eea)
```

---

## 八、FAQ 区域

```
常见问题

  ▼ 如何取消订阅？                              ← 展开状态
    您可以随时在个人资料页面取消订阅...
    取消后您仍可使用到当期结束。

  ▶ 免费版有广告吗？
  ▶ 年付可以退款吗？
  ▶ 如何获取发票？
  ▶ 支持哪些支付方式？
```

FAQ 手风琴:
- 默认全部收起
- 点击展开/收起，动画 0.3s
- 展开项: 紫色左边框
- 问题: 16px 加粗
- 答案: 14px 常规，灰色

---

## 九、页面整体布局

```
Viewport Width: 1280px (max-width)

┌─────────────────────────────────────────────────────────┐
│                                                         │
│    Hero 区域 (padding: 120px 0 80px)                    │
│    "Simple, transparent pricing"                        │
│    "Choose the plan that fits your needs"              │
│    月付/年付切换                                         │
│                                                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│    Pricing Cards (display: flex, gap: 24px)            │
│    [Free]    [Pro ⭐]    [Team]                        │
│    300px     330px      300px                         │
│    align-items: center                                 │
│                                                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│    Feature Comparison Table (max-width: 900px)         │
│                                                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│    FAQ Section (max-width: 700px)                      │
│                                                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│    Enterprise CTA                                       │
│    "需要更大规模？联系我们定制方案"                       │
│    [联系销售]                                           │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  Footer                                                 │
└─────────────────────────────────────────────────────────┘
```

---

## 十、响应式断点

| 断点 | 布局变化 |
|------|----------|
| < 768px | 卡片垂直堆叠，Pro 不放大 |
| 768-1024px | 卡片等宽，Pro 略微放大 |
| > 1024px | 完整 3 列布局，Pro scale(1.05) |

---

## 十一、动效规范

### 11.1 卡片入场动画

```css
/* 卡片依次入场，间隔 100ms */
.pricing-card {
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.5s ease forwards;
}

.pricing-card:nth-child(1) { animation-delay: 0ms; }
.pricing-card:nth-child(2) { animation-delay: 100ms; }
.pricing-card:nth-child(3) { animation-delay: 200ms; }

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### 11.2 按钮悬停

```css
.btn-primary {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.2s ease;
}

.btn-primary:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 30px rgba(102, 126, 234, 0.4);
}
```

### 11.3 推荐卡片脉冲

```css
/* Pro 卡片左上角推荐徽章微微发光 */
.pro-badge {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(251, 191, 36, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(251, 191, 36, 0); }
}
```

---

## 十二、组件清单

| 组件 | 文件 | 描述 |
|------|------|------|
| PricingView | views/PricingView.vue | 定价页主容器 |
| PricingCard | components/pricing/PricingCard.vue | 套餐卡片 |
| PricingToggle | components/pricing/PricingToggle.vue | 月付/年付切换 |
| FeatureTable | components/pricing/FeatureTable.vue | 功能对比表 |
| PricingFAQ | components/pricing/PricingFAQ.vue | FAQ 手风琴 |
| QuotaAlert | components/pricing/QuotaAlert.vue | 配额超限弹窗 |

---

**设计完成**: 2026-04-08  
**下一步**: Sprint 2 开发排期与任务拆解
