# API密钥生成与对接流程说明

> 文档版本：v1.0  
> 创建日期：2026-02-02  
> 适用系统：客户服务系统 + 律所管理系统

## 📋 概述

本文档说明**客户服务系统**和**律所管理系统**之间的API密钥生成、配置和对接流程。

**重要说明**：
- 客服系统的**基础URL**可以通过**系统配置管理页面**配置（`/admin/system-config`）
- 配置键：`system.base-url`
- 如果未在系统配置中设置，则使用配置文件 `application.yml` 中的默认值

---

## 🔑 核心概念

### 两个系统的角色

1. **客户服务系统（client-service）**
   - **角色**：API服务提供方
   - **职责**：生成API密钥，提供API接口供律所系统调用
   - **端口**：8081

2. **律所管理系统（law-firm backend）**
   - **角色**：API服务调用方
   - **职责**：配置API密钥，调用客户服务系统的API接口
   - **端口**：8080

### API密钥的作用

- **认证**：律所系统调用客户服务系统API时的身份凭证
- **授权**：验证调用方是否有权限访问API
- **安全**：防止未授权访问

---

## 🔄 完整对接流程

### 第一步：客户服务系统生成API密钥

**操作位置**：客户服务系统管理后台 → API密钥管理

**操作步骤**：

1. 登录客户服务系统管理后台（`http://localhost:3000/admin`）
2. 进入 **API密钥管理** 页面
3. 点击 **创建API密钥** 按钮
4. 填写表单：
   - **密钥名称**：例如 "律所系统生产环境密钥"
   - **律所名称**：例如 "XX律师事务所"
   - **过期时间**（可选）：设置密钥有效期
5. 点击 **创建**
6. **重要**：系统会生成一个32位的API密钥，**请立即复制保存**（只显示一次）

**生成的API密钥示例**：
```
test-api-key-12345678901234567890
```

**注意事项**：
- API密钥创建后只显示一次，请妥善保管
- 如果忘记密钥，需要重新创建
- 密钥可以设置过期时间，过期后需要重新生成

---

### 第二步：律所管理系统配置API地址和密钥

**操作位置**：律所管理系统 → 系统管理 → 外部系统集成

**重要说明**：
- ⚠️ **需要同时配置两个字段**：
  1. **API地址（URL）**：客户服务系统的API基础地址
  2. **API密钥（Key）**：第一步生成的API密钥

**操作步骤**：

1. 登录律所管理系统管理后台
2. 进入 **系统管理** → **外部系统集成**
3. 点击 **新增配置** 或找到 **客户服务系统** 配置项
4. 填写配置信息：
   - **集成编码**：`CLIENT_SERVICE`（创建时必填）
   - **集成名称**：客户服务系统（创建时必填）
   - **集成类型**：`CLIENT_SERVICE`（创建时必填）
   - **API地址**：`http://localhost:8081/api`（**必填**，客户服务系统的API基础地址）
   - **API密钥**：填写第一步生成的API密钥（**必填**，用于认证）
   - **认证方式**：`API_KEY`（默认值）
   - **描述**：可选，填写配置说明
5. 点击 **测试连接** 验证配置是否正确
6. 点击 **启用** 启用该配置

**配置示例**：
```yaml
集成编码: CLIENT_SERVICE
集成名称: 客户服务系统
集成类型: CLIENT_SERVICE
API地址: http://localhost:8081/api          # ⚠️ 必填：API基础地址
API密钥: test-api-key-12345678901234567890  # ⚠️ 必填：认证密钥
认证方式: API_KEY
描述: 客户服务系统对接配置
```

**API地址说明**：
- **格式**：完整的HTTP/HTTPS URL，包含协议、域名/IP和端口
- **示例**：
  - 开发环境：`http://localhost:8081/api`
  - 生产环境：`https://client-service.example.com/api`
- **用途**：律所系统调用时会拼接具体接口路径，如：
  - 完整URL = `{apiUrl}/matter/receive`
  - 示例 = `http://localhost:8081/api/matter/receive`

**注意事项**：
- **API地址和API密钥都是必填项**，缺一不可
- API密钥在律所系统中以**加密方式存储**（AES-256-CBC）
- 前端显示时会自动脱敏（前4位 + "****" + 后4位）
- 只有管理员可以配置和查看API密钥
- 启用配置前会验证API地址和密钥是否都已配置

---

### 第三步：律所系统调用客户服务系统API

**调用流程**：

1. **律所系统**在项目详情页点击"推送数据到客户服务系统"
2. **律所系统**从配置中读取API密钥（自动解密）
3. **律所系统**构建HTTP请求：
   ```http
   POST http://localhost:8081/api/matter/receive
   Authorization: Bearer test-api-key-12345678901234567890
   Content-Type: application/json
   
   {
     "clientId": 123,
     "clientName": "张三",
     "matterData": {...},
     "scopes": ["MATTER_INFO", "MATTER_PROGRESS"],
     "validDays": 30
   }
   ```
4. **客户服务系统**验证API密钥：
   - 检查密钥是否存在
   - 检查密钥是否启用
   - 检查密钥是否过期
5. **客户服务系统**处理请求并返回响应：
   ```json
   {
     "success": true,
     "code": "200",
     "data": {
       "id": "CS1706860800000123456",
       "accessUrl": "http://localhost:8081/portal/matter/CS1706860800000123456?token=xxx"
     }
   }
   ```

---

## 🎯 关键点总结

### 1. 谁生成API密钥？

**答案**：**客户服务系统生成API密钥**

- 客户服务系统提供API密钥管理功能
- 管理员在客户服务系统管理后台创建API密钥
- 生成的密钥提供给律所系统使用

### 2. 谁配置API地址和密钥？

**答案**：**律所管理系统配置API地址和密钥**

- 律所系统管理员在外部集成配置中填写：
  - **API地址**：客户服务系统的API基础地址（如：`http://localhost:8081/api`）
  - **API密钥**：从客户服务系统获取的认证密钥
- API密钥以加密方式存储在律所系统数据库中
- 调用API时自动解密并使用
- **重要**：API地址和API密钥都是必填项，缺一不可

### 3. 对接流程

```
┌─────────────────────┐
│  客户服务系统        │
│  (API提供方)        │
└─────────────────────┘
         │
         │ 1. 生成API密钥
         │
         ▼
┌─────────────────────┐
│  管理员              │
│  (复制API密钥)      │
└─────────────────────┘
         │
         │ 2. 配置API密钥
         │
         ▼
┌─────────────────────┐
│  律所管理系统        │
│  (API调用方)        │
└─────────────────────┘
         │
         │ 3. 调用API
         │    (携带API密钥)
         │
         ▼
┌─────────────────────┐
│  客户服务系统        │
│  (验证API密钥)      │
└─────────────────────┘
```

---

## 📝 实际操作示例

### 示例1：在客户服务系统中创建API密钥

**使用管理后台界面**：

1. 访问：`http://localhost:3000/admin/api-keys`
2. 点击"创建API密钥"
3. 填写：
   - 密钥名称：`律所系统生产环境`
   - 律所名称：`XX律师事务所`
   - 过期时间：`2027-12-31`
4. 点击创建
5. 复制生成的密钥：`prod-api-key-abcdefghijklmnopqrstuvwxyz123456`

**使用API接口**（可选）：

```bash
curl -X POST "http://localhost:8081/api/admin/api-keys" \
  -H "Authorization: Bearer {已有API密钥}" \
  -H "Content-Type: application/json" \
  -d '{
    "keyName": "律所系统生产环境",
    "lawFirmName": "XX律师事务所",
    "expiresAt": "2027-12-31T23:59:59"
  }'
```

### 示例2：在律所系统中配置API地址和密钥

1. 访问：`http://localhost:8080/system/external-integration`
2. 点击"新增配置"
3. 填写配置表单：
   - **集成编码**：`CLIENT_SERVICE`（必填）
   - **集成名称**：`客户服务系统`（必填）
   - **集成类型**：`CLIENT_SERVICE`（必填）
   - **API地址**：`http://localhost:8081/api`（**必填**，客户服务系统的API基础地址）
   - **API密钥**：`prod-api-key-abcdefghijklmnopqrstuvwxyz123456`（**必填**，从客服系统获取）
   - **认证方式**：`API_KEY`（默认值）
   - **描述**：`客户服务系统对接配置`（可选）
4. 点击"测试连接"验证配置是否正确
5. 如果测试成功，点击"启用"

**前端表单字段说明**：
- **API地址**：输入框，必填，格式如 `http://localhost:8081/api`
- **API Key**：密码输入框，用于API_KEY或BEARER_TOKEN认证方式
- **API Secret**：密码输入框，可选，部分API需要

### 示例3：律所系统调用客户服务系统API

**代码示例**（Java）：

```java
// 1. 获取客户服务系统配置（包含API地址和密钥）
ExternalIntegration integration = 
    externalIntegrationAppService.getFirstEnabledIntegrationByType("CLIENT_SERVICE");

// 2. 构建请求头（包含API密钥）
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
// 注意：integration.getApiKey() 返回的是解密后的密钥
headers.set("Authorization", "Bearer " + integration.getApiKey());

// 3. 构建请求体
Map<String, Object> requestBody = new HashMap<>();
requestBody.put("clientId", 123);
requestBody.put("clientName", "张三");
requestBody.put("matterData", matterData);
requestBody.put("scopes", Arrays.asList("MATTER_INFO", "MATTER_PROGRESS"));
requestBody.put("validDays", 30);

// 4. 发送请求
// 注意：使用 integration.getApiUrl() 获取配置的API地址
// 完整URL = apiUrl + "/matter/receive"
// 例如：http://localhost:8081/api + /matter/receive = http://localhost:8081/api/matter/receive
HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
    integration.getApiUrl() + "/matter/receive",  // ⚠️ 使用配置的API地址
    HttpMethod.POST,
    entity,
    new ParameterizedTypeReference<Map<String, Object>>() {}
);
```

**关键点**：
- `integration.getApiUrl()`：获取配置的API基础地址（如：`http://localhost:8081/api`）
- `integration.getApiKey()`：获取解密后的API密钥（用于认证）
- 完整API URL = `{apiUrl}/matter/receive`

---

## 🔒 安全说明

### API密钥存储安全

1. **客户服务系统**：
   - API密钥以明文存储在数据库中（用于验证）
   - 管理后台显示时会脱敏（前4位 + "****" + 后4位）

2. **律所管理系统**：
   - API密钥以**加密方式存储**（AES-256-CBC）
   - 前端显示时会脱敏
   - 调用API时自动解密

### API密钥使用安全

1. **传输安全**：
   - 生产环境建议使用HTTPS
   - API密钥通过HTTP Header传输（`Authorization: Bearer {key}`）

2. **验证机制**：
   - 每次API调用都会验证密钥
   - 检查密钥是否存在、是否启用、是否过期
   - 记录最后使用时间

3. **权限控制**：
   - 只有管理员可以创建和管理API密钥
   - 只有管理员可以配置外部集成

---

## ❓ 常见问题

### Q1: 客服系统和管理系统，哪个生成密钥？

**A**: **客服系统生成密钥**，提供给管理系统使用。

### Q2: 管理系统如何配置？

**A**: 
1. 客服系统管理员在管理后台创建API密钥
2. 复制生成的密钥和API地址
3. 在管理系统的外部集成配置中填写：
   - **API地址**：客户服务系统的API基础地址（如：`http://localhost:8081/api`）
   - **API密钥**：从客服系统复制的密钥
4. 测试连接并启用配置

**注意**：API地址和API密钥都是必填项，缺一不可

### Q3: 一个律所可以配置多个API密钥吗？

**A**: 可以。客服系统可以为不同的律所或不同的环境（开发/测试/生产）创建不同的API密钥。

### Q4: API密钥忘记怎么办？

**A**: 
- 在客服系统中重新创建新的API密钥
- 在管理系统中更新配置，填写新的密钥
- 旧的密钥可以禁用或删除

### Q5: 管理系统需要配置哪些字段？

**A**: 
管理系统需要配置以下字段：
- **API地址**（必填）：客户服务系统的API基础地址，如 `http://localhost:8081/api`
- **API密钥**（必填）：从客服系统获取的认证密钥
- **认证方式**：通常选择 `API_KEY` 或 `BEARER_TOKEN`
- **集成编码**（创建时必填）：`CLIENT_SERVICE`
- **集成名称**（创建时必填）：如"客户服务系统"
- **集成类型**（创建时必填）：`CLIENT_SERVICE`

**重要**：API地址和API密钥都是必填项，缺一不可。启用配置前系统会验证这两个字段是否都已填写。

### Q5: 如何测试API密钥是否有效？

**A**: 
- 在管理系统的外部集成配置页面点击"测试连接"
- 或者在客服系统管理后台使用该密钥调用API接口

### Q6: API密钥会过期吗？

**A**: 
- 创建时可以设置过期时间
- 如果设置了过期时间，过期后需要重新生成
- 如果没有设置过期时间，密钥永久有效（除非手动禁用）

---

## 📚 相关文档

- [客户服务系统API文档](./API.md)
- [对接指南](./CUSTOMER_SERVICE_INTEGRATION_GUIDE.md)
- [系统设计文档](./CLIENT_SERVICE_SYSTEM_DESIGN.md)

---

**文档版本**：v1.0  
**最后更新**：2026-02-02
