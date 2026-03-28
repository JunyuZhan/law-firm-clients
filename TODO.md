# 客户服务系统 - 函件验证功能开发任务

## 背景

律所管理系统已实现电子函件盖章功能，生成的 PDF 文件中包含二维码。扫描二维码后需要跳转到客户服务系统进行真伪验证。

管理系统会将验证数据推送到客服系统的 `/letter/verification/receive` 接口。

## 推送数据结构

管理系统推送的 `LetterVerificationPushDTO` 包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| letterId | Long | 函件申请ID（内部关联用） |
| applicationNo | String | 函件申请编号（对外展示） |
| verificationCode | String | 验证码（用于验证） |
| letterType | String | 函件类型编码 |
| letterTypeName | String | 函件类型名称 |
| targetUnit | String | 接收单位 |
| lawyerNames | String | 出函律师姓名（多人逗号分隔） |
| firmName | String | 律师事务所名称 |
| matterName | String | 关联项目名称（脱敏） |
| approvedAt | LocalDateTime | 审批时间 |
| printedAt | LocalDateTime | 打印/盖章时间 |
| validUntil | LocalDateTime | 有效期截止时间 |
| remark | String | 备注 |

---

## 一、数据库设计

### 任务 1.1：创建函件验证数据表

**文件**: `scripts/init-db/06-letter-verification.sql`

```sql
CREATE TABLE IF NOT EXISTS public.letter_verification (
    id BIGSERIAL PRIMARY KEY,
    letter_id BIGINT NOT NULL,                       -- 律所系统函件ID
    application_no VARCHAR(50) NOT NULL UNIQUE,      -- 函件申请编号
    verification_code VARCHAR(100) NOT NULL,         -- 验证码
    letter_type VARCHAR(50),                         -- 函件类型编码
    letter_type_name VARCHAR(100),                   -- 函件类型名称
    target_unit VARCHAR(200),                        -- 接收单位
    lawyer_names VARCHAR(500),                       -- 出函律师姓名
    firm_name VARCHAR(200),                          -- 律师事务所名称
    matter_name VARCHAR(200),                        -- 关联项目名称（脱敏）
    approved_at TIMESTAMP,                           -- 审批时间
    printed_at TIMESTAMP,                            -- 打印/盖章时间
    valid_until TIMESTAMP,                           -- 有效期截止时间
    remark TEXT,                                     -- 备注
    verify_count INTEGER DEFAULT 0,                  -- 验证次数
    last_verify_at TIMESTAMP,                        -- 最后验证时间
    last_verify_ip VARCHAR(50),                      -- 最后验证IP
    status VARCHAR(20) DEFAULT 'ACTIVE',             -- 状态：ACTIVE/EXPIRED/REVOKED
    deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_letter_verification_code ON letter_verification(verification_code);
CREATE INDEX idx_letter_verification_letter_id ON letter_verification(letter_id);
CREATE INDEX idx_letter_verification_status ON letter_verification(status);
```

**状态**: [x] 已完成

---

## 二、后端开发

### 任务 2.1：创建实体类

**文件**: `backend/src/main/java/com/clientservice/domain/entity/LetterVerification.java`

**状态**: [x] 已完成

---

### 任务 2.2：创建 Mapper

**文件**: `backend/src/main/java/com/clientservice/infrastructure/persistence/mapper/LetterVerificationMapper.java`

**状态**: [x] 已完成

---

### 任务 2.3：创建接收推送 DTO

**文件**: `backend/src/main/java/com/clientservice/application/dto/LetterVerificationReceiveDTO.java`

复制管理系统的 `LetterVerificationPushDTO` 字段结构。

**状态**: [x] 已完成

---

### 任务 2.4：创建验证结果 DTO

**文件**: `backend/src/main/java/com/clientservice/application/dto/LetterVerificationResultDTO.java`

用于返回验证结果给前端展示：
- 是否有效
- 函件信息（编号、类型、律所、律师、接收单位）
- 审批/打印时间
- 验证次数
- 状态提示

**状态**: [x] 已完成

---

### 任务 2.5：创建服务类

**文件**: `backend/src/main/java/com/clientservice/application/service/LetterVerificationService.java`

方法：
1. `receiveVerificationData(DTO)` - 接收并存储验证数据
2. `verifyLetter(applicationNo, code)` - 验证函件真伪
3. `getVerificationInfo(applicationNo)` - 获取验证信息（用于页面展示）
4. `revokeVerification(letterId)` - 撤销验证（管理端调用）

**状态**: [x] 已完成

---

### 任务 2.6：创建控制器

**文件**: `backend/src/main/java/com/clientservice/interfaces/rest/LetterVerificationController.java`

接口：
1. `POST /letter/verification/receive` - 接收推送数据（需 API Key 认证）
2. `GET /letter/verification/verify` - 验证函件（公开接口）
3. `GET /letter/verification/info/{applicationNo}` - 获取验证信息（公开接口）
4. `DELETE /letter/verification/{letterId}` - 撤销验证（管理端接口）

**状态**: [x] 已完成

---

### 任务 2.7：配置安全放行

**文件**: `backend/src/main/java/com/clientservice/infrastructure/filter/JwtAuthenticationFilter.java`

放行以下公开接口：
- `/letter/verification/verify`
- `/letter/verification/info/**`

**状态**: [x] 已完成

---

### 任务 2.8：单元测试

**文件**: `backend/src/test/java/com/clientservice/application/service/LetterVerificationServiceTest.java`

测试用例：
- 接收验证数据成功
- 验证函件成功（有效）
- 验证函件失败（已过期）
- 验证函件失败（已撤销）
- 验证函件失败（验证码错误）
- 获取验证信息

**状态**: [x] 已完成

---

## 三、前端开发

### 任务 3.1：创建验证页面

**文件**: `frontend/src/views/verify/LetterVerify.vue`

功能：
- 输入函件编号和验证码进行验证
- 或通过 URL 参数自动验证（二维码扫描场景）
- 展示验证结果（成功/失败）
- 成功时展示函件详细信息

**状态**: [x] 已完成

---

### 任务 3.2：创建验证结果页面

**文件**: `frontend/src/views/verify/LetterVerifyResult.vue`

展示内容：
- 验证状态（有效/无效/已过期）
- 函件编号
- 函件类型
- 出函律所
- 出函律师
- 接收单位
- 审批时间
- 盖章时间
- 有效期
- 验证次数

**状态**: [x] 已完成（合并到 LetterVerify.vue 中）

---

### 任务 3.3：配置路由

**文件**: `frontend/src/router/index.ts`

新增路由：
- `/verify/letter` - 验证页面（包含结果展示）

**状态**: [x] 已完成

---

### 任务 3.4：创建 API

**文件**: `frontend/src/api/letter-verification.ts`

API 函数：
- `verifyLetter(applicationNo, code)` - 验证函件
- `getVerificationInfo(applicationNo)` - 获取验证信息

**状态**: [x] 已完成

---

## 四、管理后台（可选）

### 任务 4.1：验证记录列表页面

**文件**: `frontend/src/views/admin/LetterVerificationList.vue`

功能：
- 查看所有验证记录
- 筛选（状态、关键词搜索）
- 查看验证详情
- 撤销验证
- 统计数据展示

**后端接口**:
- `GET /api/admin/letter-verification/list` - 分页查询
- `GET /api/admin/letter-verification/{id}` - 获取详情
- `DELETE /api/admin/letter-verification/{id}` - 撤销验证
- `GET /api/admin/letter-verification/statistics` - 获取统计

**相关文件**:
- 后端控制器: `backend/src/main/java/com/clientservice/interfaces/rest/AdminLetterVerificationController.java`
- 前端 API: `frontend/src/api/letter-verification.ts`
- 路由配置: `frontend/src/router/index.ts` - `/admin/letter-verifications`
- 侧边栏菜单: `frontend/src/views/admin/AdminLayout.vue`

**状态**: [x] 已完成

---

## 五、集成测试

### 任务 5.1：端到端测试

**文件**: `backend/src/test/java/com/clientservice/e2e/LetterVerificationE2ETest.java`

测试流程：
1. 模拟管理系统推送验证数据
2. 模拟用户扫描二维码验证
3. 验证结果正确展示

**状态**: [ ] 待完成（可选功能）

---

## 实现顺序建议

1. 数据库表创建（任务 1.1）
2. 后端实体和 Mapper（任务 2.1-2.2）
3. 后端 DTO（任务 2.3-2.4）
4. 后端服务和控制器（任务 2.5-2.6）
5. 安全配置（任务 2.7）
6. 前端验证页面和路由（任务 3.1-3.4）
7. 单元测试（任务 2.8）
8. 集成测试（任务 5.1）
9. 管理后台（任务 4.1，可选）

---

## 注意事项

1. **API Key 认证**: 接收推送接口需要验证 API Key，与现有 `ApiKeyService` 集成
2. **验证码安全**: 验证码使用 HMAC-SHA256 生成，确保不可伪造
3. **有效期检查**: 验证时需检查是否已过期
4. **验证次数记录**: 每次验证需记录 IP 和时间，用于审计
5. **响应式设计**: 验证页面需适配移动端（二维码扫描场景）

---

## 二维码 URL 格式

管理系统生成的二维码 URL 格式：
```
{客服系统地址}/verify/letter?no={申请编号}&code={验证码}
```

示例：
```
https://service.lawfirm.com/verify/letter?no=HF2024001&code=abc123...
```

验证接口同时支持两种参数名：
- `no` + `code`: 二维码扫描场景（推荐）
- `applicationNo` + `code`: 兼容旧版本
