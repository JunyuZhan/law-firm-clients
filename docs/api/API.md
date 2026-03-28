# 客户服务系统 API 接口文档

> 最后更新：2026-02-02  
> API版本：v1.0.0  
> Base URL: `http://localhost:8081`

## 📋 目录

- [认证方式](#认证方式)
- [项目数据接口](#项目数据接口)
- [客户门户接口](#客户门户接口)
- [文件管理接口](#文件管理接口)
- [通知管理接口](#通知管理接口)
- [访问日志接口](#访问日志接口)
- [错误码说明](#错误码说明)

---

## 🔐 认证方式

### API密钥认证（供律所系统调用）

所有需要API密钥的接口，需要在请求头中携带：

```
Authorization: Bearer {api-key}
```

### Token认证（供客户访问）

客户门户相关接口，需要在URL参数中携带：

```
?token={access-token}
```

---

## 📦 项目数据接口

### 1. 接收项目数据

**接口地址**：`POST /api/matter/receive`

**认证方式**：API密钥

**请求头**：
```
Authorization: Bearer {api-key}
Content-Type: application/json
```

**请求体**：
```json
{
  "clientId": 2001,
  "clientName": "测试客户",
  "matterData": {
    "matterId": 1001,
    "matterName": "测试项目",
    "matterNo": "M20260202001",
    "status": "IN_PROGRESS",
    "phone": "13800138000",
    "email": "client@example.com"
  },
  "scopes": ["MATTER_INFO", "MATTER_PROGRESS"],
  "validDays": 30
}
```

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "id": "CS1706860800000123456",
    "accessUrl": "http://localhost:8081/portal/api/matter/CS1706860800000123456?token=xxx"
  },
  "timestamp": 1706860800000
}
```

### 2. 获取项目详情（内部API）

**接口地址**：`GET /api/matter/{id}`

**认证方式**：API密钥

**请求头**：
```
Authorization: Bearer {api-key}
```

**路径参数**：
- `id`：项目ID

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "id": "CS1706860800000123456",
    "lawFirmMatterId": 1001,
    "clientId": 2001,
    "clientName": "测试客户",
    "matterData": "{...}",
    "accessToken": "xxx",
    "accessUrl": "http://localhost:8081/portal/api/matter/CS1706860800000123456?token=xxx",
    "status": "ACTIVE",
    "expiresAt": "2026-03-04T00:00:00"
  },
  "timestamp": 1706860800000
}
```

### 3. 撤销项目访问

**接口地址**：`POST /api/matter/revoke`

**认证方式**：API密钥

**请求头**：
```
Authorization: Bearer {api-key}
```

**请求参数**：
- `matterId`：项目ID（Query参数）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": null,
  "timestamp": 1706860800000
}
```

---

## 🌐 客户门户接口

### 1. 获取项目详情（客户门户）

**接口地址**：`GET /portal/api/matter/{id}`

**认证方式**：Token

**请求参数**：
- `id`：项目ID（路径参数）
- `token`：访问令牌（Query参数，必填）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "id": "CS1706860800000123456",
    "lawFirmMatterId": 1001,
    "clientId": 2001,
    "clientName": "测试客户",
    "matterData": "{...}",
    "accessToken": "xxx",
    "accessUrl": "http://localhost:8081/portal/api/matter/CS1706860800000123456?token=xxx",
    "status": "ACTIVE",
    "expiresAt": "2026-03-04T00:00:00"
  },
  "timestamp": 1706860800000
}
```

---

## 📁 文件管理接口

### 1. 上传文件

**接口地址**：`POST /api/client/files/upload`

**认证方式**：Token

**请求参数**：
- `matterId`：项目ID（必填）
- `clientId`：客户ID（必填）
- `token`：访问令牌（必填）
- `file`：文件（必填，multipart/form-data）
- `fileCategory`：文件类别（可选：EVIDENCE、CONTRACT、ID_CARD、OTHER）
- `description`：文件描述（可选）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "id": "CS1706860800000123457",
    "matterId": "CS1706860800000123456",
    "clientId": 2001,
    "fileName": "document.pdf",
    "fileSize": 102400,
    "fileType": "application/pdf",
    "fileCategory": "EVIDENCE",
    "description": "证据材料",
    "storagePath": "matters/CS1706860800000123456/CS1706860800000123457.pdf",
    "uploadedAt": "2026-02-02T10:00:00",
    "status": "ACTIVE"
  },
  "timestamp": 1706860800000
}
```

### 2. 获取文件列表

**接口地址**：`GET /api/client/files`

**认证方式**：Token

**请求参数**：
- `matterId`：项目ID（必填）
- `token`：访问令牌（必填）
- `status`：状态（可选：ACTIVE、DELETED）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": [
    {
      "id": "CS1706860800000123457",
      "matterId": "CS1706860800000123456",
      "fileName": "document.pdf",
      "fileSize": 102400,
      "fileType": "application/pdf",
      "fileCategory": "EVIDENCE",
      "uploadedAt": "2026-02-02T10:00:00",
      "status": "ACTIVE"
    }
  ],
  "timestamp": 1706860800000
}
```

### 3. 获取文件详情

**接口地址**：`GET /api/client/files/{fileId}`

**认证方式**：无需认证（公开接口）

**路径参数**：
- `fileId`：文件ID

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "id": "CS1706860800000123457",
    "matterId": "CS1706860800000123456",
    "fileName": "document.pdf",
    "fileSize": 102400,
    "fileType": "application/pdf",
    "fileCategory": "EVIDENCE",
    "uploadedAt": "2026-02-02T10:00:00",
    "status": "ACTIVE"
  },
  "timestamp": 1706860800000
}
```

### 4. 下载文件

**接口地址**：`GET /api/client/files/{fileId}/download`

**认证方式**：Token

**请求参数**：
- `fileId`：文件ID（路径参数）
- `matterId`：项目ID（Query参数，必填）
- `token`：访问令牌（Query参数，必填）

**响应**：文件流（Content-Type: application/octet-stream）

### 5. 删除文件

**接口地址**：`DELETE /api/client/files/{fileId}`

**认证方式**：Token

**请求参数**：
- `fileId`：文件ID（路径参数）
- `matterId`：项目ID（Query参数，必填）
- `token`：访问令牌（Query参数，必填）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": null,
  "timestamp": 1706860800000
}
```

### 6. 文件删除回调（供律所系统调用）

**接口地址**：`POST /api/matter/files/delete`

**认证方式**：API密钥

**请求头**：
```
Authorization: Bearer {api-key}
```

**请求参数**：
- `fileId`：外部文件ID（Query参数，必填）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": null,
  "timestamp": 1706860800000
}
```

---

## 🔔 通知管理接口

### 1. 手动发送通知

**接口地址**：`POST /api/notification/send`

**认证方式**：API密钥

**请求头**：
```
Authorization: Bearer {api-key}
```

**请求参数**：
- `matterId`：项目ID（Query参数，必填）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": null,
  "timestamp": 1706860800000
}
```

### 2. 获取通知历史

**接口地址**：`GET /api/notification/history`

**认证方式**：API密钥

**请求头**：
```
Authorization: Bearer {api-key}
```

**请求参数**（全部可选）：
- `matterId`：项目ID
- `clientId`：客户ID
- `notificationType`：通知类型（SMS、WECHAT、EMAIL）
- `status`：状态（PENDING、SUCCESS、FAILED）
- `startTime`：开始时间（ISO格式：2026-02-01T00:00:00）
- `endTime`：结束时间（ISO格式：2026-02-02T23:59:59）
- `limit`：限制数量（默认100）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "matterId": "CS1706860800000123456",
      "clientId": 2001,
      "notificationType": "EMAIL",
      "recipient": "client@example.com",
      "content": "<html>...</html>",
      "status": "SUCCESS",
      "errorMessage": null,
      "sentAt": "2026-02-02T10:00:00",
      "createdAt": "2026-02-02T10:00:00"
    }
  ],
  "timestamp": 1706860800000
}
```

---

## 📊 访问日志接口

### 1. 获取访问历史

**接口地址**：`GET /api/access-logs`

**认证方式**：Token

**请求参数**：
- `matterId`：项目ID（必填）
- `token`：访问令牌（必填）
- `startTime`：开始时间（可选，ISO格式）
- `endTime`：结束时间（可选，ISO格式）
- `limit`：限制数量（可选，默认100）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "matterId": "CS1706860800000123456",
      "clientId": 2001,
      "accessToken": "xxx",
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "accessTime": "2026-02-02T10:00:00",
      "createdAt": "2026-02-02T10:00:00"
    }
  ],
  "timestamp": 1706860800000
}
```

### 2. 获取访问统计

**接口地址**：`GET /api/access-logs/statistics`

**认证方式**：Token

**请求参数**：
- `matterId`：项目ID（必填）
- `token`：访问令牌（必填）
- `startTime`：开始时间（可选，ISO格式）
- `endTime`：结束时间（可选，ISO格式）

**响应示例**：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": {
    "totalCount": 150,
    "uniqueIpCount": 25
  },
  "timestamp": 1706860800000
}
```

---

## ❌ 错误码说明

### 成功响应

- `200`：操作成功

### 客户端错误

- `400`：请求参数错误
- `401`：认证失败（API密钥无效或Token无效）
- `403`：访问被拒绝（项目已过期、已撤销等）
- `404`：资源不存在

### 服务器错误

- `500`：服务器内部错误

### 错误响应示例

```json
{
  "success": false,
  "code": "401",
  "message": "API密钥无效",
  "data": null,
  "timestamp": 1706860800000
}
```

---

## 📝 注意事项

1. **API密钥**：所有需要API密钥的接口，必须在请求头中携带`Authorization: Bearer {api-key}`
2. **Token认证**：客户门户接口需要在URL参数中携带`token={access-token}`
3. **时间格式**：所有时间字段使用ISO 8601格式：`yyyy-MM-ddTHH:mm:ss`
4. **文件上传**：使用`multipart/form-data`格式，最大文件大小10MB
5. **分页**：列表查询接口默认返回100条记录，可通过`limit`参数调整
6. **HTTPS**：生产环境建议使用HTTPS协议

---

## 🔗 相关文档

- [系统设计文档](./docs/CLIENT_SERVICE_SYSTEM_DESIGN.md)
- [对接指南](../docs/CUSTOMER_SERVICE_INTEGRATION_GUIDE.md)
- [Swagger UI](http://localhost:8081/swagger-ui.html)（开发环境）

---

**API文档版本**：v1.0.0  
**最后更新**：2026-02-02
