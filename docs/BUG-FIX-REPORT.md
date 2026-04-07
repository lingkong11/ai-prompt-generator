# AI Prompt Generator - Bug修复报告

**修复时间**: 2026-04-07 08:23  
**修复人员**: 马可行（AI Agent）  
**问题编号**: BUG-001  
**Git Commit**: fe5c69e

---

## 📋 问题描述

### 问题现象
- 用户注册成功后，使用相同账号密码登录返回401 Unauthorized
- 错误信息：`{"error":"登录失败: null"}`
- 根本原因：`java.lang.NullPointerException`

### 错误堆栈
```
java.lang.NullPointerException: null
    at java.base/java.util.Objects.requireNonNull(Objects.java:208)
    at java.base/java.util.ImmutableCollections$MapN.<init>(ImmutableCollections.java:1186)
    at java.base/java.util.Map.of(Map.java:1445)
    at com.ai.prompt.controller.AuthController.login(AuthController.java:128)
```

---

## 🔍 根因分析

### 问题定位
1. 登录接口在验证密码成功后，构建响应Map时使用了`Map.of()`
2. `Map.of()`是Java 10引入的不可变Map工厂方法，**不允许任何key或value为null**
3. 当`user.getAvatar()`返回null时，触发NPE

### 影响范围
| 接口 | 影响 |
|------|------|
| POST /api/auth/login | ✅ 登录失败 |
| GET /api/auth/me | ✅ 可能失败（待验证） |
| 其他接口 | ⚠️ 未测试 |

---

## ✅ 修复方案

### 修复前代码
```java
String token = jwtUtils.generateToken(request.getUsername());

Map<String, Object> response = new HashMap<>();
response.put("timestamp", timestamp);
response.put("status", "success");
response.put("token", token);
response.put("user", Map.of(  // ⚠️ 问题：Map.of()不允许null值
    "id", user.getId(),
    "username", user.getUsername(),
    "email", user.getEmail(),
    "nickname", user.getNickname(),
    "avatar", user.getAvatar()  // ⚠️ avatar可能为null
));
```

### 修复后代码
```java
String token = jwtUtils.generateToken(request.getUsername());

Map<String, Object> userMap = new HashMap<>();
userMap.put("id", user.getId());
userMap.put("username", user.getUsername());
userMap.put("email", user.getEmail());
userMap.put("nickname", user.getNickname());
userMap.put("avatar", user.getAvatar());  // ✅ HashMap允许null值

Map<String, Object> response = new HashMap<>();
response.put("timestamp", timestamp);
response.put("status", "success");
response.put("token", token);
response.put("user", userMap);
```

### 修复位置
- `AuthController.java`: `login()`方法
- `AuthController.java`: `getCurrentUser()`方法

---

## 🧪 修复验证

### 测试用例

| 测试步骤 | 预期结果 | 实际结果 |
|----------|----------|----------|
| 1. 用户注册 | 返回token | ✅ 通过 |
| 2. 用户登录 | 返回token + user | ✅ 通过 |
| 3. 创建提示词 | 返回prompt对象 | ✅ 通过 |

### 测试日志
```
[INFO] [时间戳:2026-04-07 08:22:57.774] [阶段:Controller] [任务:密码验证] - matches:true
[INFO] [时间戳:2026-04-07 08:22:57.774] [阶段:Controller] [任务:登录成功] - username:testuser2
```

### 响应示例
```json
{
    "user": {
        "nickname": "Test User",
        "id": 1,
        "avatar": null,
        "email": "test2@example.com",
        "username": "testuser2"
    },
    "timestamp": "2026-04-07 08:22:57.774",
    "status": "success",
    "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

---

## 📊 影响评估

### 风险评估
| 风险项 | 风险等级 | 缓解措施 |
|--------|----------|----------|
| 回归风险 | 🟢 低 | 仅修改2处，构建HashMap替代Map.of() |
| 性能影响 | 🟢 无 | HashMap性能与Map.of()相当 |
| 兼容性 | 🟢 无 | 不影响API契约 |

### 修复范围
- 修改文件：1个（AuthController.java）
- 修改行数：+15/-14
- 测试用例：3个

---

## 📝 经验教训

### 技术要点
1. **Java不可变集合**：`Map.of()`、`List.of()`、`Set.of()`不允许null值
2. **替代方案**：使用`HashMap`构建可包含null值的Map
3. **最佳实践**：对外API返回数据时，避免使用不可变集合工厂方法

### 调试技巧
1. 异常堆栈中的`Objects.requireNonNull`是判断null值的关键
2. 添加详细日志（timestamp、task、action）有助于快速定位问题
3. H2内存数据库每次启动会清空数据，需要注意测试数据状态

---

## 🔗 相关文档

- [Phase 1开发报告](./PHASE1-REPORT.md)
- [API测试报告](./API-TEST-REPORT.md)

---

**报告生成**: 马可行（AI Agent）  
**修复时间**: 2026-04-07 08:23 GMT+8  
**Git Commit**: fe5c69e
