# 系统配置管理指南

> 文档版本：v1.1  
> 更新日期：2026-03-29

## 📋 概述

系统配置（`sys_config`）是客户服务系统的核心配置管理模块，用于存储和管理系统的各种运行时配置。配置存储在数据库中，支持动态修改，无需重启服务即可生效。

## 🗂️ 配置结构

### 数据库表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键，自增 |
| `config_key` | VARCHAR(200) | 配置键（唯一标识） |
| `config_value` | TEXT | 配置值 |
| `config_type` | VARCHAR(20) | 配置类型：STRING、NUMBER、BOOLEAN、JSON |
| `description` | VARCHAR(500) | 配置描述 |
| `created_at` | TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | 更新时间 |
| `deleted` | BOOLEAN | 是否删除（逻辑删除） |

### 配置类型说明

- **STRING**：字符串类型，用于文本配置
- **NUMBER**：数字类型，用于数值配置
- **BOOLEAN**：布尔类型，值为 `true` 或 `false`
- **JSON**：JSON格式，用于复杂配置对象

## 📝 系统配置列表

### 1. 系统基础配置

| 配置键 | 默认值 | 类型 | 说明 |
|--------|--------|------|------|
| `system.name` | `客户服务系统` | STRING | 系统名称 |
| `system.version` | `1.0.0` | STRING | 系统版本号 |
| `system.base-url` | `http://localhost:8081` | STRING | 系统基础URL（用于生成客户访问链接） |

**`system.base-url` 配置说明**：
- 格式：`协议://域名或IP:端口`（可包含路径前缀）
- 示例：
  - `http://localhost:8081`（开发环境）
  - `https://client-service.example.com`（生产环境）
  - `http://localhost:8081/v1`（如果API网关有版本路径）
- 用途：生成客户访问链接时使用，如：`{base-url}/portal/matter/{matterId}?token={token}`

### 1.1 品牌与门户展示配置

当前管理后台的“系统配置”页面已拆分出“品牌配置”和“门户配置”两个可视化区域，常用字段如下：

| 配置键 | 用途 |
|--------|------|
| `system.app-name` | 浏览器标题、页脚版权等完整系统名称 |
| `system.app-short-name` | 管理后台侧边栏主标题 |
| `system.app-short-name-en` | 管理后台侧边栏可选英文副标题 |
| `system.logo-url` | 门户和后台共用 Logo |
| `system.law-firm-name` | 门户头部与页脚显示的律所名称 |
| `system.law-firm-website` | 帮助中心和页脚“官网”跳转链接 |
| `system.app-slogan` | 门户首页主标语、帮助中心提示语 |
| `system.icp-license` | 页脚备案号 |
| `system.copyright` | 页脚版权文案 |

说明：
- `system.app-short-name-en` 为可选字段，当前界面以中文为主、英文为辅
- 帮助中心、移动端抽屉和门户首页都会复用上述品牌字段

### 2. 通知服务配置

#### 2.1 邮件通知

**⚠️ 重要提示**：所有通知配置应在后台"系统配置"中管理，而非在代码或环境变量中配置。

| 配置键 | 默认值 | 类型 | 说明 |
|--------|--------|------|------|
| `notification.email.enabled` | `false` | BOOLEAN | 是否启用邮件通知（**应在后台配置**） |
| `client-service.notification.email.from` | `noreply@example.com` | STRING | 发件人邮箱（**应在后台配置**） |
| `client-service.notification.email.from-name` | `客户服务系统` | STRING | 发件人名称（**应在后台配置**） |

**SMTP 服务器配置说明**：
- SMTP 服务器配置（host, port, username, password）可以在后台"通知配置"页面中管理
- 配置保存后立即生效，无需重启服务
- 如果使用环境变量 `SPRING_MAIL_*` 配置，则环境变量优先级更高
- 后台配置路径：管理后台 → 通知配置 → 邮件通知 → SMTP 服务器配置

**SMTP 配置项**（应在后台配置）：
- `spring.mail.host`：SMTP 服务器地址（如：smtp.example.com）
- `spring.mail.port`：SMTP 端口（如：587）
- `spring.mail.username`：SMTP 用户名（通常是邮箱地址）
- `spring.mail.password`：SMTP 密码（或授权码）

#### 2.2 短信通知

**⚠️ 重要提示**：所有短信通知配置应在后台"系统配置"中管理，而非在代码或环境变量中配置。

| 配置键 | 默认值 | 类型 | 说明 |
|--------|--------|------|------|
| `notification.sms.enabled` | `false` | BOOLEAN | 是否启用短信通知（**应在后台配置**） |
| `client-service.notification.sms.provider` | `aliyun` | STRING | 短信服务商：`aliyun`（阿里云）或 `tencent`（腾讯云）（**应在后台配置**） |

**阿里云短信配置**（**应在后台配置**）：
- `client-service.notification.sms.aliyun.access-key-id`：AccessKey ID
- `client-service.notification.sms.aliyun.access-key-secret`：AccessKey Secret
- `client-service.notification.sms.aliyun.sign-name`：短信签名
- `client-service.notification.sms.aliyun.template-code`：短信模板代码
- `client-service.notification.sms.aliyun.endpoint`：API端点（默认：`dysmsapi.aliyuncs.com`）

**腾讯云短信配置**（**应在后台配置**）：
- `client-service.notification.sms.tencent.secret-id`：Secret ID
- `client-service.notification.sms.tencent.secret-key`：Secret Key
- `client-service.notification.sms.tencent.app-id`：应用ID
- `client-service.notification.sms.tencent.sign-name`：短信签名
- `client-service.notification.sms.tencent.template-id`：短信模板ID
- `client-service.notification.sms.tencent.region`：地域（默认：`ap-beijing`）

#### 2.3 微信通知

**⚠️ 重要提示**：所有微信通知配置应在后台"系统配置"中管理，而非在代码或环境变量中配置。

| 配置键 | 默认值 | 类型 | 说明 |
|--------|--------|------|------|
| `notification.wechat.enabled` | `false` | BOOLEAN | 是否启用微信通知（**应在后台配置**） |
| `notification.wechat.template-id` | `` | STRING | 微信模板ID（**应在后台配置**） |
| `client-service.notification.wechat.app-id` | `` | STRING | 微信公众号AppID（**应在后台配置**） |
| `client-service.notification.wechat.app-secret` | `` | STRING | 微信公众号AppSecret（**应在后台配置**） |

### 3. 文件管理配置

| 配置键 | 默认值 | 类型 | 说明 |
|--------|--------|------|------|
| `file.max-size` | `10485760` | NUMBER | 最大文件大小（字节），默认10MB |
| `file.storage.type` | `local` | STRING | 文件存储类型：`local`（本地）、`minio`、`oss` |

### 4. API配置

| 配置键 | 默认值 | 类型 | 说明 |
|--------|--------|------|------|
| `api.token-expire-days` | `30` | NUMBER | Token默认有效期（天） |
| `api.token-length` | `32` | NUMBER | Token长度 |

### 5. 回调配置

| 配置键 | 默认值 | 类型 | 说明 |
|--------|--------|------|------|
| `callback.law-firm-url` | `` | STRING | 律所系统回调地址（从配置文件读取，可覆盖） |

## 🛠️ 管理方式

### 方式1：前端管理界面（推荐）

**访问路径**：管理后台 → 系统管理 → 系统配置

**功能**：
- ✅ 查看所有配置列表
- ✅ 按配置键或类型搜索
- ✅ 新增配置
- ✅ 编辑配置
- ✅ 删除配置（逻辑删除）
- ✅ 配置值格式验证（URL、JSON等）

**操作步骤**：
1. 登录管理后台
2. 进入「系统管理」→「系统配置」
3. 点击「新增配置」或「编辑」按钮
4. 填写配置信息：
   - **配置键**：如 `system.base-url`
   - **配置值**：如 `https://client-service.example.com`
   - **配置类型**：选择 STRING、NUMBER、BOOLEAN 或 JSON
   - **描述**：配置说明（可选）
5. 点击「确定」保存

### 方式2：后端API接口

**基础URL**：`/api/admin/config`

**接口列表**：

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/admin/config` | 获取配置列表 | JWT Token |
| GET | `/api/admin/config/{id}` | 获取配置详情 | JWT Token |
| POST | `/api/admin/config` | 创建或更新配置 | JWT Token |
| PUT | `/api/admin/config/{id}` | 更新配置 | JWT Token |
| DELETE | `/api/admin/config/{id}` | 删除配置 | JWT Token |

**请求示例**：

```bash
# 获取配置列表
curl -X GET "http://localhost:8081/api/admin/config?configKey=system&limit=10" \
  -H "Authorization: Bearer {token}"

# 创建/更新配置
curl -X POST "http://localhost:8081/api/admin/config" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "configKey": "system.base-url",
    "configValue": "https://client-service.example.com",
    "configType": "STRING",
    "description": "系统基础URL"
  }'

# 更新配置
curl -X PUT "http://localhost:8081/api/admin/config/{id}" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "configValue": "https://new-url.example.com",
    "description": "更新后的描述"
  }'
```

### 方式3：直接数据库操作（不推荐）

**适用场景**：紧急修复、批量导入等

**注意事项**：
- ⚠️ 需要手动清除Redis缓存（配置有2小时缓存）
- ⚠️ 需要确保配置键格式正确
- ⚠️ 建议使用前端界面或API接口

## 🔄 配置更新机制

### 缓存机制

- **缓存类型**：Redis（如果启用）或内存缓存
- **缓存时间**：2小时
- **缓存键格式**：`sysConfig::{configKey}`

### 配置更新事件

当配置更新时，系统会发布 `ConfigUpdateEvent` 事件，监听器会自动：
- 清除相关服务的缓存（如短信客户端、微信AccessToken等）
- 记录配置更新日志

### 配置优先级

1. **数据库配置**（最高优先级）
2. **配置文件**（`application.yml`）- 作为默认值
3. **代码默认值**（最低优先级）

## 📖 使用示例

### 示例1：获取系统基础URL

```java
@Autowired
private SysConfigService sysConfigService;

String baseUrl = sysConfigService.getConfigValue(
    "system.base-url", 
    "http://localhost:8081"  // 默认值
);
```

### 示例2：检查通知是否启用

```java
boolean emailEnabled = sysConfigService.getBooleanConfig(
    "notification.email.enabled", 
    false  // 默认值
);
```

### 示例3：获取数值配置

```java
int maxFileSize = sysConfigService.getIntConfig(
    "file.max-size", 
    10485760  // 默认值：10MB
);
```

## ⚠️ 注意事项

1. **配置键命名规范**：
   - 使用小写字母和点号（`.`）分隔
   - 格式：`模块.子模块.配置项`
   - 示例：`notification.sms.enabled`、`client-service.notification.sms.aliyun.access-key-id`

2. **配置值格式**：
   - STRING：普通文本
   - NUMBER：数字字符串（如 `"10485760"`）
   - BOOLEAN：`"true"` 或 `"false"`（字符串）
   - JSON：有效的JSON字符串

3. **敏感信息**：
   - API密钥、密码等敏感配置建议存储在数据库中，而不是配置文件中
   - 生产环境请定期更换敏感配置

4. **配置更新**：
   - 修改配置后，相关服务的缓存会自动清除
   - 某些配置（如短信服务商切换）可能需要重启服务才能完全生效

5. **配置验证**：
   - 前端界面会对URL格式、JSON格式等进行验证
   - 建议使用前端界面进行配置管理，避免格式错误

## 🔍 常见问题

### Q1: 配置修改后不生效？

**A**: 
1. 检查Redis缓存是否已清除（配置有2小时缓存）
2. 某些配置（如短信服务商）可能需要重启服务
3. 检查配置键是否正确

### Q2: 如何批量导入配置？

**A**: 
1. 使用SQL脚本直接插入数据库（需要清除缓存）
2. 使用API接口批量创建
3. 使用前端界面逐个添加

### Q3: 配置删除后如何恢复？

**A**: 
- 系统使用逻辑删除，配置记录仍在数据库中
- 可以通过数据库直接恢复（设置 `deleted = false`）
- 或通过API重新创建相同配置键的配置

### Q4: 配置键冲突怎么办？

**A**: 
- `saveConfig` 方法会自动检测配置键是否存在
- 如果存在，会更新现有配置；如果不存在，会创建新配置
- 配置键是唯一标识，不能重复

## 📚 相关文档

- [API接口文档](./API_KEY_INTEGRATION_GUIDE.md)
- [通知服务配置](./NOTIFICATION_TEMPLATE_GUIDE.md)
- [系统部署文档](./OPERATIONS_MANUAL.md)
