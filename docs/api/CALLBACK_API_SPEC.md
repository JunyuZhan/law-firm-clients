# 回调接口规范文档

> 文档版本：v1.0  
> 创建日期：2026-02-02  
> 适用系统：客户服务系统 → 管理系统

## 📋 概述

本文档定义客户服务系统回调管理系统的接口规范。当客户访问项目详情或下载文件时，客户服务系统会异步回调管理系统，通知访问和下载行为。

## 🔐 认证方式

**建议**：管理系统可以实现API密钥认证，验证回调来源。

**认证方式**：
- 客户服务系统在回调时携带API密钥（Bearer Token）
- 管理系统验证API密钥的有效性
- 如果验证失败，返回401状态码

**注意**：当前客户服务系统回调时**未携带API密钥**，管理系统可以根据需要添加认证逻辑。

## 📡 回调接口

### 1. 访问日志回调接口

**接口地址**：`POST {管理系统地址}/api/open/client/access-log`

**请求头**：
```
Content-Type: application/json
Authorization: Bearer {api-key}  // 可选，建议添加
```

**请求体**：
```json
{
  "matterId": 456,                    // 律所系统的项目ID（Long类型）
  "clientId": 123,                    // 客户ID（Long类型）
  "accessTime": "2026-02-02T10:00:00", // 访问时间（ISO 8601格式）
  "ipAddress": "192.168.1.100",       // IP地址（String类型）
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", // 用户代理（String类型）
  "eventType": "ACCESS"               // 事件类型：ACCESS（固定值）
}
```

**字段说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| matterId | Long | 是 | 律所系统的项目ID（不是客户服务系统的项目ID） |
| clientId | Long | 是 | 客户ID |
| accessTime | String | 是 | 访问时间（ISO 8601格式：yyyy-MM-ddTHH:mm:ss） |
| ipAddress | String | 否 | 客户端IP地址 |
| userAgent | String | 否 | 用户代理字符串 |
| eventType | String | 是 | 事件类型，固定值："ACCESS" |

**响应格式**：
```json
{
  "success": true,
  "code": "200",
  "message": "处理成功",
  "data": null,
  "timestamp": 1706860800000
}
```

**HTTP状态码**：
- `200`：处理成功
- `400`：请求参数错误
- `401`：认证失败（如果实现了API密钥认证）
- `500`：服务器错误

---

### 2. 下载日志回调接口

**接口地址**：`POST {管理系统地址}/open/client/download-log`

**请求头**：
```
Content-Type: application/json
Authorization: Bearer {api-key}  // 可选，建议添加
```

**请求体**：
```json
{
  "matterId": 456,                    // 律所系统的项目ID（Long类型）
  "clientId": 123,                    // 客户ID（Long类型）
  "fileId": "CS1234567890123456789",  // 文件ID（客户服务系统的文件ID）
  "fileName": "判决书.pdf",            // 文件名（String类型）
  "downloadTime": "2026-02-02T10:00:00", // 下载时间（ISO 8601格式）
  "ipAddress": "192.168.1.100",       // IP地址（String类型）
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", // 用户代理（String类型）
  "eventType": "DOWNLOAD"             // 事件类型：DOWNLOAD（固定值）
}
```

**字段说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| matterId | Long | 是 | 律所系统的项目ID（不是客户服务系统的项目ID） |
| clientId | Long | 是 | 客户ID |
| fileId | String | 是 | 文件ID（客户服务系统的文件ID，格式：CS + 时间戳 + 随机数） |
| fileName | String | 否 | 文件名 |
| downloadTime | String | 是 | 下载时间（ISO 8601格式：yyyy-MM-ddTHH:mm:ss） |
| ipAddress | String | 否 | 客户端IP地址 |
| userAgent | String | 否 | 用户代理字符串 |
| eventType | String | 是 | 事件类型，固定值："DOWNLOAD" |

**响应格式**：
```json
{
  "success": true,
  "code": "200",
  "message": "处理成功",
  "data": null,
  "timestamp": 1706860800000
}
```

**HTTP状态码**：
- `200`：处理成功
- `400`：请求参数错误
- `401`：认证失败（如果实现了API密钥认证）
- `500`：服务器错误

---

## 🔄 回调流程

### 访问日志回调流程

```
1. 客户打开访问链接
   ↓
2. PortalController.getMatterDetail()
   ↓
3. AccessLogService.recordAccess()
   ├── 记录访问日志到数据库
   └── 异步调用 CallbackService.callbackAccessLog()
       ↓
4. CallbackService.callbackAccessLog()
   ├── 查询项目信息（获取lawFirmMatterId）
   ├── 构建回调数据
   └── 发送HTTP POST请求到管理系统
        POST {管理系统地址}/api/open/client/access-log
       ↓
5. 管理系统接收回调
   ├── 验证API密钥（如果实现了认证）
   ├── 保存访问日志
   └── 返回成功响应
```

### 下载日志回调流程

```
1. 客户下载文件
   ↓
2. FileController.downloadFile()
   ├── 记录下载日志（FileService.recordFileDownload）
   │   ├── DownloadLogService.recordDownload()
   │   │   ├── 记录下载日志到数据库
   │   │   └── 异步调用 CallbackService.callbackDownloadLog()
   │   └── 如果是推送文件且首次下载，更新firstDownloadedAt
   └── 返回文件资源
       ↓
3. CallbackService.callbackDownloadLog()
   ├── 查询项目信息（获取lawFirmMatterId）
   ├── 构建回调数据
   └── 发送HTTP POST请求到管理系统
       POST {管理系统地址}/open/client/download-log
       ↓
4. 管理系统接收回调
   ├── 验证API密钥（如果实现了认证）
   ├── 保存下载日志
   └── 返回成功响应
```

---

## ⚙️ 配置说明

### 客户服务系统配置

#### 1. 配置文件（application.yml）

```yaml
client-service:
  # 回调配置
  callback:
    enabled: true  # 是否启用回调（默认启用）
    law-firm-url: http://localhost:8080  # 管理系统回调地址
```

#### 2. 系统配置（sys_config表）

**配置键**：`callback.law-firm-url`

**配置值**：管理系统的完整地址（如：`http://localhost:8080`）

**优先级**：系统配置 > 配置文件默认值

**说明**：
- 如果系统配置中设置了值，优先使用系统配置
- 如果未配置，则使用配置文件中的默认值
- 如果都未配置，回调功能会自动跳过（不影响主流程）

---

## 🛡️ 错误处理

### 客户服务系统端

1. **回调失败不影响主流程**
   - 访问日志记录失败不影响客户访问
   - 下载日志记录失败不影响文件下载
   - 回调失败只记录日志，不抛出异常

2. **回调重试机制**（可选）
   - 当前实现：回调失败只记录日志
   - 建议：可以实现重试机制（如：失败后重试3次，每次间隔5秒）

3. **回调超时处理**
   - RestTemplate默认超时时间：30秒
   - 超时后记录错误日志，不影响主流程

### 管理系统端

1. **幂等性处理**
   - 建议：根据matterId + clientId + accessTime或downloadTime去重
   - 避免重复处理相同的回调请求

2. **异常处理**
   - 返回标准错误响应格式
   - 记录错误日志
   - 不要抛出异常（避免影响客户服务系统）

---

## 📊 数据示例

### 访问日志回调示例

**请求**：
```bash
POST http://localhost:8080/api/open/client/access-log
Content-Type: application/json

{
  "matterId": 456,
  "clientId": 123,
  "accessTime": "2026-02-02T10:30:00",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
  "eventType": "ACCESS"
}
```

**响应**：
```json
{
  "success": true,
  "code": "200",
  "message": "处理成功",
  "data": null,
  "timestamp": 1706860800000
}
```

### 下载日志回调示例

**请求**：
```bash
POST http://localhost:8080/open/client/download-log
Content-Type: application/json

{
  "matterId": 456,
  "clientId": 123,
  "fileId": "CS1706860800000123457",
  "fileName": "判决书.pdf",
  "downloadTime": "2026-02-02T10:35:00",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
  "eventType": "DOWNLOAD"
}
```

**响应**：
```json
{
  "success": true,
  "code": "200",
  "message": "处理成功",
  "data": null,
  "timestamp": 1706860800000
}
```

---

## 🔍 调试和排查

### 客户服务系统端日志

**访问日志回调**：
```
DEBUG - 访问日志回调成功: matterId=CS123, clientId=123
ERROR - 访问日志回调失败: matterId=CS123
```

**下载日志回调**：
```
DEBUG - 下载日志回调成功: matterId=CS123, fileId=CS456
ERROR - 下载日志回调失败: matterId=CS123, fileId=CS456
```

### 管理系统端建议日志

**接收访问日志回调**：
```
INFO - 收到访问日志回调: matterId=456, clientId=123, accessTime=2026-02-02T10:30:00
ERROR - 访问日志回调处理失败: matterId=456, error=xxx
```

**接收下载日志回调**：
```
INFO - 收到下载日志回调: matterId=456, fileId=CS1234567890123456789, fileName=判决书.pdf
ERROR - 下载日志回调处理失败: matterId=456, fileId=CS1234567890123456789, error=xxx
```

---

## ⚠️ 注意事项

1. **回调是异步的**
   - 客户服务系统异步发送回调请求
   - 回调失败不影响主流程
   - 管理系统应该快速响应（建议<1秒）

2. **回调可能重复**
   - 网络重试可能导致重复回调
   - 管理系统应该实现幂等性处理

3. **回调地址配置**
   - 确保管理系统地址正确
   - 确保网络可达
   - 建议使用HTTPS（生产环境）

4. **时间格式**
   - 使用ISO 8601格式：`yyyy-MM-ddTHH:mm:ss`
   - 时区：使用系统默认时区（建议使用UTC或GMT+8）

5. **matterId说明**
   - 回调中的`matterId`是律所系统的项目ID（Long类型）
   - 不是客户服务系统的项目ID（String类型，格式：CS + 时间戳 + 随机数）
   - 客户服务系统会自动转换

---

## 📝 管理系统实现示例

### Java Spring Boot示例

```java
@RestController
@RequestMapping("/open/client")
@RequiredArgsConstructor
public class ClientServiceCallbackController {

    private final ClientAccessLogService accessLogService;
    private final ClientDownloadLogService downloadLogService;

    /**
     * 接收访问日志回调
     */
    @PostMapping("/access-log")
    public Result<Void> receiveAccessLog(
            @RequestBody AccessLogCallbackRequest request) {
        try {
            // 保存访问日志
            accessLogService.saveAccessLog(request);
            return Result.success();
        } catch (Exception e) {
            log.error("处理访问日志回调失败: matterId={}", request.getMatterId(), e);
            return Result.error("处理失败");
        }
    }

    /**
     * 接收下载日志回调
     */
    @PostMapping("/download-log")
    public Result<Void> receiveDownloadLog(
            @RequestBody DownloadLogCallbackRequest request) {
        try {
            // 保存下载日志
            downloadLogService.saveDownloadLog(request);
            return Result.success();
        } catch (Exception e) {
            log.error("处理下载日志回调失败: matterId={}, fileId={}", 
                    request.getMatterId(), request.getFileId(), e);
            return Result.error("处理失败");
        }
    }
}
```

---

## 📚 相关文档

- [TODO设计评估报告](./TODO_DESIGN_REVIEW.md)
- [实现回顾总结](./IMPLEMENTATION_REVIEW.md)
- [客户服务系统集成指南](./CUSTOMER_SERVICE_INTEGRATION_GUIDE.md)
