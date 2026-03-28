# 管理员密码管理指南

> 文档版本：v1.0  
> 创建日期：2026-02-03

## 📋 概述

本文档说明如何设置和管理客户服务系统的管理员密码，包括默认密码、密码修改和密码修复方法。

---

## 🔐 默认管理员账号

### 默认账号信息

- **用户名**：`admin`
- **密码**：`admin123`
- **状态**：已启用

### 密码设置方式

管理员密码通过以下方式设置：

1. **数据库初始化脚本**（`scripts/init-db/02-init-data.sql`）
   - 使用BCrypt加密后的密码哈希值
   - 默认密码：`admin123`
   - BCrypt哈希：`$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

2. **应用启动时自动创建**（`StartupListener.initDefaultAdminUser()`）
   - 如果数据库中不存在admin用户，自动创建
   - 默认密码：`admin123`
   - 使用BCrypt加密

⚠️ **重要**：首次登录后，请立即修改默认密码！

---

## 🔑 密码修改方法

### 方法一：通过管理后台修改（推荐）

1. 登录管理后台：`http://localhost:3000/admin/login`
2. 点击右上角用户名下拉菜单
3. 选择"修改密码"
4. 输入旧密码和新密码
5. 新密码要求：
   - 至少8位
   - 包含字母和数字

**API接口**：`POST /api/admin/auth/change-password`

**请求体**：
```json
{
  "oldPassword": "admin123",
  "newPassword": "NewPassword123"
}
```

### 方法二：通过密码修复接口（仅开发环境）

**适用场景**：
- 忘记密码
- 密码被锁定
- 紧急重置密码

**接口**：`POST /api/admin/fix/password`

**参数**：
- `username`：用户名（默认：admin）
- `password`：新密码（默认：admin123）
- `captchaId`：验证码ID（必填）
- `captchaText`：验证码文本（必填）

**使用步骤**：

1. **获取验证码**：
   ```bash
   curl http://localhost:8081/api/admin/auth/captcha
   ```
   返回：
   ```json
   {
     "success": true,
     "code": "200",
     "data": {
       "captchaId": "uuid-xxxx-xxxx",
       "captchaImage": "data:image/png;base64,..."
     }
   }
   ```

2. **修复密码**：
   ```bash
   curl -X POST "http://localhost:8081/api/admin/fix/password?username=admin&password=NewPassword123&captchaId=uuid-xxxx-xxxx&captchaText=ABCD"
   ```

**安全限制**：
- ✅ 仅开发环境可用（生产环境自动禁用）
- ✅ 需要验证码验证
- ✅ 记录IP地址和操作日志
- ✅ 验证密码强度

### 方法三：直接修改数据库（不推荐）

**适用场景**：无法通过其他方式修改密码时

**步骤**：

1. **生成BCrypt密码哈希**：
   
   使用 `PasswordHashGenerator` 工具类：
   ```bash
   cd backend
   mvn exec:java -Dexec.mainClass="com.clientservice.common.util.PasswordHashGenerator" -Dexec.args="YourNewPassword123"
   ```
   
   或使用Java代码：
   ```java
   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
   String hash = encoder.encode("YourNewPassword123");
   System.out.println(hash);
   ```

2. **更新数据库**：
   ```sql
   UPDATE admin_user 
   SET password_hash = '$2a$10$...生成的BCrypt哈希值...',
       failed_login_count = 0,
       locked_until = NULL,
       updated_at = CURRENT_TIMESTAMP
   WHERE username = 'admin';
   ```

---

## 🔒 密码安全要求

### 密码强度要求

- ✅ 至少8位字符
- ✅ 包含字母（大小写均可）
- ✅ 包含数字
- ✅ 建议包含特殊字符（可选）

### 密码存储

- ✅ 使用BCrypt加密存储
- ✅ 每次加密结果不同（使用随机salt）
- ✅ 密码哈希存储在 `admin_user.password_hash` 字段

---

## 🛠️ 密码修复接口说明

### 接口信息

- **路径**：`POST /api/admin/fix/password`
- **环境**：仅开发环境（`@Profile("dev")`）
- **认证**：无需认证（但需要验证码）

### 安全措施

1. **环境限制**：仅在开发环境启用，生产环境自动禁用
2. **验证码验证**：必须提供正确的验证码
3. **密码强度验证**：新密码必须符合强度要求
4. **操作日志**：记录IP地址和操作时间
5. **账户状态重置**：自动清除失败次数和锁定状态

### 使用示例

**完整流程**：

```bash
# 1. 获取验证码
CAPTCHA_RESPONSE=$(curl -s http://localhost:8081/api/admin/auth/captcha)
CAPTCHA_ID=$(echo $CAPTCHA_RESPONSE | jq -r '.data.captchaId')
CAPTCHA_IMAGE=$(echo $CAPTCHA_RESPONSE | jq -r '.data.captchaImage')

# 2. 查看验证码图片（Base64）
echo $CAPTCHA_IMAGE | base64 -d > captcha.png
# 打开 captcha.png 查看验证码

# 3. 修复密码（替换 CAPTCHA_TEXT 为实际验证码）
curl -X POST "http://localhost:8081/api/admin/fix/password?username=admin&password=NewPassword123&captchaId=$CAPTCHA_ID&captchaText=ABCD"
```

---

## 📝 常见问题

### Q1: 忘记密码怎么办？

**开发环境**：
1. 使用密码修复接口（需要验证码）
2. 或直接修改数据库

**生产环境**：
1. 通过数据库管理员直接修改数据库
2. 或联系系统管理员重置密码

### Q2: 账户被锁定怎么办？

密码修复接口会自动：
- 清除登录失败次数（`failed_login_count = 0`）
- 解除账户锁定（`locked_until = NULL`）

### Q3: 如何生成BCrypt密码哈希？

**方法一**：使用工具类
```bash
mvn exec:java -Dexec.mainClass="com.clientservice.common.util.PasswordHashGenerator" -Dexec.args="YourPassword"
```

**方法二**：使用在线工具
- 访问：https://bcrypt-generator.com/
- 输入密码，生成BCrypt哈希

**方法三**：使用Java代码
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("YourPassword");
```

### Q4: 生产环境可以使用密码修复接口吗？

**不可以**。密码修复接口仅在开发环境可用（`@Profile("dev")`），生产环境会自动禁用。

生产环境如需重置密码，请：
1. 通过数据库管理员直接修改数据库
2. 或联系系统管理员

### Q5: 如何验证密码是否正确？

使用 `PasswordUtil.matches()` 方法：
```java
passwordUtil.matches("明文密码", "BCrypt哈希值")
```

---

## 🔍 相关文件

- **密码修复接口**：`AdminPasswordFixController.java`
- **密码工具类**：`PasswordUtil.java`
- **密码哈希生成工具**：`PasswordHashGenerator.java`
- **数据库初始化脚本**：`scripts/init-db/02-init-data.sql`
- **启动监听器**：`StartupListener.java`

---

## ⚠️ 安全建议

1. **首次登录后立即修改默认密码**
2. **使用强密码**（至少12位，包含大小写字母、数字、特殊字符）
3. **定期更换密码**（建议每3-6个月）
4. **不要共享管理员账号**
5. **生产环境禁用密码修复接口**（已自动实现）
6. **记录所有密码修改操作**（已实现日志记录）

---

**最后更新**：2026-02-03
