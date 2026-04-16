# 系统配置完整指南

> 创建日期：2026-02-04  
> 适用系统：客户服务系统 + 律所管理系统

## 📋 目录

- [快速配置（5分钟）](#快速配置5分钟)
- [详细配置说明](#详细配置说明)
- [端口映射说明](#端口映射说明)
- [客户访问链接配置](#客户访问链接配置)
- [配置验证](#配置验证)
- [常见问题](#常见问题)

---

## ⚡ 快速配置（5分钟）

### 核心原则

**每个系统填写对方的URL**

### 配置对照表

| 配置系统 | 配置项 | 填写对方的URL | 示例 |
|---------|--------|--------------|------|
| **客户服务系统** | 回调地址 | 律所系统的URL | `http://law-firm.example.com/api` 或 `https://law-firm.example.com/api` |
| **律所管理系统** | API地址 | 客服系统的URL | `http://client.example.com:8080/api` 或 `https://client.example.com:8080/api` |

**✅ 支持域名和IP：** 两种方式都可以，推荐使用域名（生产环境建议使用HTTPS）

---

### 步骤1：客户服务系统配置（回调）

**位置：** 客户服务系统管理后台 → **API密钥管理** → 右侧「回调配置」

| 配置项 | 填写值 | 说明 |
|--------|--------|------|
| 启用回调 | ✅ 开启 | - |
| 律所系统地址 | `http://{律所系统域名或IP}/api` | **不写端口**（默认80/443） |
| 回调 API 密钥 | `f57a4f4602342821840ad396dada66f6` | 双方约定，使用随机生成的字母数字组合 |

**🔑 生成随机密钥：**
```bash
openssl rand -hex 16
# 输出示例：f57a4f4602342821840ad396dada66f6
```

---

### 步骤2：律所管理系统配置（推送）

**位置：** 律所管理系统管理后台 → **系统管理** → **外部系统集成** → 客户服务系统

| 配置项 | 填写值 | 说明 |
|--------|--------|------|
| 集成编码 | `CLIENT_SERVICE` | 固定值 |
| API地址 | `http://{客服系统域名或IP}:8080/api` | **必须写端口8080** |
| API密钥 | 从客服系统获取 | 见下方「如何获取API密钥」 |
| 是否启用 | ✅ 是 | - |

**🔑 如何获取API密钥：**
1. 登录客服系统管理后台
2. 进入：**API密钥管理**
3. 查看：启用状态的密钥
4. 复制：密钥值

---

## 📍 端口映射说明

### 快速对照表

| 系统 | 宿主机端口 | 访问地址示例 | 说明 |
|------|-----------|------------|------|
| **客户服务系统** | **8080** | `http://client.example.com:8080` | 必须指定端口8080 |
| **律所管理系统** | **80** | `http://law-firm.example.com` | 80是默认端口，可以不写 |

### 记忆技巧

- **客服系统 = 8080**（8-0-8-0，必须写端口）
- **律所系统 = 80**（默认端口，可以不写）

### 配置填写

**律所管理系统 → 客户服务系统（推送）：**
```
API地址：http://{客服系统域名或IP}:8080/api
```

**客户服务系统 → 律所管理系统（回调）：**
```
回调地址：http://{律所系统域名或IP}/api
```

---

## 📝 详细配置说明

### 第一部分：客户服务系统配置

#### 配置位置
**客户服务系统管理后台** → **系统配置管理** (`/admin/system-config`)

#### 需要配置的3个配置项

##### 1. `callback.enabled` - 是否启用回调

**值：** `true`（启用）或 `false`（禁用）

##### 2. `callback.law-firm-url` - 律所系统API地址

**填写格式：**
```
http://{律所系统域名或IP}/api
或
https://{律所系统域名或IP}/api
```

**示例：**
- 内网部署（IP）：`http://192.168.1.100/api`
- 生产环境（HTTPS）：`https://law-firm.example.com/api`

**⚠️ 常见错误：**
- ❌ `http://law-firm.example.com` （缺少 `/api`）
- ✅ `http://law-firm.example.com/api` （正确）

##### 3. `callback.api-key` - 回调API密钥

**生成随机密钥：**
```bash
openssl rand -hex 16
# 输出示例：f57a4f4602342821840ad396dada66f6
```

**示例：**
- ✅ `f57a4f4602342821840ad396dada66f6`（随机生成，推荐）
- ❌ `13208576800`（不推荐，纯数字不够安全）

---

### 第二部分：律所管理系统配置

#### 配置位置
**律所管理系统管理后台** → **系统管理** → **外部系统集成**

#### 需要配置的字段

##### 1. 集成编码（integrationCode）
**固定值：** `CLIENT_SERVICE`

##### 2. API地址（apiUrl）

**填写格式：**
```
http://{客服系统域名或IP}:8080/api
或
https://{客服系统域名或IP}:8080/api
```

**示例：**
- 内网部署（IP）：`http://192.168.1.100:8080/api`
- 生产环境（HTTPS）：`https://client.example.com:8080/api`

**⚠️ 常见错误：**
- ❌ `http://client.example.com:8080` （缺少 `/api`）
- ✅ `http://client.example.com:8080/api` （正确）

##### 3. API密钥（apiKey）

**获取方法：**
1. 登录客服系统管理后台
2. 进入「API密钥管理」页面
3. 查看启用状态的密钥
4. 复制密钥值

##### 4. 是否启用（enabled）
**值：** 勾选「启用」

---

## 🔗 客户访问链接配置

### 配置项：`system.base-url`

**用途：** 生成客户访问链接的基础URL  
**配置位置：** 客户服务系统 → 系统配置管理

### 访问链接格式

```
{base-url}/portal/matter/{matterId}?token={token}
```

**示例：**
- 如果 `system.base-url = http://client.example.com:8080`
- 生成的链接：`http://client.example.com:8080/portal/matter/CS123?token=xxx`

### 配置要求

**格式：** `协议://域名或IP:端口`

**示例：**
- 内网部署：`http://192.168.1.100:8080`
- 域名访问：`http://client.example.com:8080` 或 `https://client.example.com`
- 本地开发：`http://localhost:8080`

**⚠️ 重要：**
- **必须包含端口**（除非是默认端口80/443）
- 客服系统使用8080端口，配置时必须包含 `:8080`

### 配置方法

**方法1：通过管理界面配置（推荐）**
1. 登录客户服务系统管理后台
2. 进入：**系统配置管理**
3. 搜索或添加配置项：`system.base-url`
4. 填写配置值：`http://{客服系统域名或IP}:8080`
5. 点击保存

**方法2：通过数据库直接配置**
```bash
docker exec client-service-postgres psql -U postgres -d client_service -c \
  "UPDATE sys_config SET config_value = 'http://{客服系统域名或IP}:8080' WHERE config_key = 'system.base-url';"
```

### 基础URL获取逻辑

优先级顺序：
1. **系统配置**（`sys_config` 表中的 `system.base-url`）
2. **动态获取**（从HTTP请求中获取，如果配置为空或"auto"）
3. **默认值**（配置文件中的值，默认 `http://localhost:8081`）

---

## ✅ 配置验证

### 验证客户服务系统配置

```bash
docker exec client-service-postgres psql -U postgres -d client_service -c \
  "SELECT config_key, config_value FROM sys_config WHERE config_key LIKE 'callback%' OR config_key = 'system.base-url';"
```

**应该看到：**
```
callback.enabled      = true
callback.law-firm-url = http://{律所系统域名或IP}/api
callback.api-key      = f57a4f4602342821840ad396dada66f6
system.base-url       = http://{客服系统域名或IP}:8080
```

### 验证律所管理系统配置

```bash
docker exec law-firm-postgres psql -U law_admin -d law_firm -c \
  "SELECT integration_code, api_url, enabled FROM sys_external_integration WHERE integration_code = 'CLIENT_SERVICE';"
```

**应该看到：**
```
integration_code = CLIENT_SERVICE
api_url          = http://{客服系统域名或IP}:8080/api
enabled          = true
```

### 测试推送功能

1. 律所系统 → 项目详情 → 推送数据
2. 查看推送记录，状态应为「成功」

### 测试回调功能

1. 客服系统 → 上传文件
2. 律所系统 → 客户文件列表，应能看到新文件

---

## ❓ 常见问题

### Q1: 如何知道律所系统的IP地址？

**A:** 
1. 查看服务器IP：`ifconfig` 或 `ip addr`
2. 查看容器网络：`docker network inspect bridge`
3. 查看nginx配置：`docker exec law-firm-frontend cat /etc/nginx/nginx.conf`

### Q2: 如何知道客服系统的端口？

**A:**
1. 查看容器端口映射：`docker ps | grep client`
2. 查看nginx配置：`docker exec client-service-nginx cat /etc/nginx/nginx.conf`

### Q3: API密钥在哪里查看？

**A:**
- **客户服务系统**：管理后台 → API密钥管理
- **律所管理系统**：管理后台 → 系统管理 → 外部系统集成

### Q4: 为什么生成的链接是 `localhost:8080`？

**A:** 
- 可能原因：配置值为空或"auto"，系统从HTTP请求中动态获取
- **解决方法**：配置 `system.base-url` 为完整URL，包含端口8080

### Q5: 配置后还是不工作怎么办？

**A:**
1. 检查配置值是否正确（特别是URL是否包含`/api`和端口）
2. 检查服务是否启用
3. 查看日志：`docker logs client-service-backend --tail 100`
4. 测试网络连通性：`curl http://目标地址/api/actuator/health`

---

## 📋 配置检查清单

### 客户服务系统
- [ ] `callback.enabled` = `true`
- [ ] `callback.law-firm-url` = `http://{律所系统域名或IP}/api`（不写端口）
- [ ] `callback.api-key` = 已填写（随机生成）
- [ ] `system.base-url` = `http://{客服系统域名或IP}:8080`（包含端口）

### 律所管理系统
- [ ] 集成编码 = `CLIENT_SERVICE`
- [ ] API地址 = `http://{客服系统域名或IP}:8080/api`（包含端口8080和`/api`）
- [ ] API密钥 = 已填写（从客服系统获取）
- [ ] 已启用 = 是

---

## 📚 相关文档

- **API对接文档：** `../api/API_KEY_INTEGRATION_GUIDE.md`
- **运维手册：** `OPERATIONS_MANUAL.md`
- **用户手册：** `../guides/USER_MANUAL.md`
