# 全仓代码评审台账

- 评审日期：2026-04-17
- 评审范围：`backend`、关键配置、核心接口测试
- 评审方式：静态代码审查 + 测试样例抽查
- 当前结论：首轮已发现 5 个高优先级问题，其中 2 个为直接安全风险，2 个为业务删除语义失效，1 个为运维能力缺陷

## 问题清单

| ID | 严重级别 | 位置 | 问题 | 影响 |
| --- | --- | --- | --- | --- |
| CR-001 | P0 | `backend/src/main/java/com/clientservice/interfaces/rest/FileController.java` | 文件详情接口缺少项目令牌校验，任意人只要知道 `fileId` 就能读取元数据 | 造成文件名、类型、描述、存储路径、所属项目等敏感信息泄露 |
| CR-002 | P1 | `backend/src/main/java/com/clientservice/application/service/FileService.java` `backend/src/main/resources/mapper/ClientFileMapper.xml` | 删除文件仅改 `status=DELETED`，但列表/详情查询默认不过滤已删除状态 | 已删除文件仍会继续出现在客户侧列表与详情接口中，删除语义失效 |
| CR-003 | P1 | `backend/src/main/java/com/clientservice/infrastructure/config/CorsConfig.java` | CORS 配置无条件放开 `*` 且允许携带凭证 | 任意站点都可跨域发起带凭证请求，放大后台接口被跨站调用的风险 |
| CR-004 | P1 | `backend/src/main/java/com/clientservice/common/util/UrlGenerator.java` | 在 `system.base-url=auto` 时直接信任 `Host` / `X-Forwarded-*` 请求头生成访问链接 | 可被构造恶意域名，导致门户访问链接、下载链接、通知链接被投毒 |
| CR-005 | P2 | `backend/src/main/java/com/clientservice/interfaces/rest/SystemMaintenanceController.java` | 备份 SQL 导出逻辑直接 `toString()` 非字符串字段，生成的 SQL 对时间/JSON 等类型不可靠 | 备份文件可能“看起来成功、恢复时失败”，存在运维恢复风险 |

## 详细记录

### CR-001 文件详情接口存在对象级越权

- 严重级别：P0
- 位置：
  - `backend/src/main/java/com/clientservice/interfaces/rest/FileController.java:171`
  - `backend/src/main/java/com/clientservice/application/service/FileService.java:413`
  - `backend/src/test/java/com/clientservice/interfaces/rest/FileControllerTest.java:223`
- 问题说明：
  - `GET /api/client/files/{fileId}` 直接调用 `fileService.getFileById(fileId)`。
  - 该接口没有校验 `matterId`、`token`，也没有校验文件是否属于当前访问项目。
  - 当前测试还显式把“无 token 直接成功”当作正确行为固化了下来。
- 风险影响：
  - 任何知道或撞到 `fileId` 的调用方都能读取文件元数据。
  - 返回 DTO 中还包含 `storagePath`，会额外暴露内部存储结构。
- 修复方案：
  - 将该接口改成与下载/预览一致，强制要求 `matterId` + `token`。
  - 校验 `matterService.getMatterByToken(token)` 与 `file.matterId` 一致后再返回。
  - 对外 DTO 去掉 `storagePath`、`fileHash` 等内部字段，至少客户侧接口不要返回。
  - 补一组鉴权测试：无 token、错误 token、跨项目 fileId 都应失败。
- 建议验证：
  - 使用其他项目 token 访问目标 `fileId`，应返回 403。
  - 不带 token 访问，应该返回 401/403，而不是 200。

### CR-002 删除文件后仍会在客户列表与详情中出现

- 严重级别：P1
- 位置：
  - `backend/src/main/java/com/clientservice/application/service/FileService.java:400`
  - `backend/src/main/java/com/clientservice/application/service/FileService.java:413`
  - `backend/src/main/java/com/clientservice/application/service/FileService.java:552`
  - `backend/src/main/resources/mapper/ClientFileMapper.xml:6`
- 问题说明：
  - 删除逻辑只把 `status` 改成 `DELETED`，没有设置逻辑删除位。
  - `getFileById` 仅判断 `deleted`，不判断 `status`。
  - `selectByMatterId` 默认只过滤 `deleted=false`，调用方未传 `status` 时会把 `DELETED` 文件也查出来。
- 风险影响：
  - 客户删除文件后，在文件列表里仍可能看到该文件。
  - 详情接口也仍可查到“已删除”文件元数据，产生业务混乱。
  - 后续重复上传幂等、统计、清理逻辑会和业务预期持续偏离。
- 修复方案：
  - 明确删除语义，二选一并统一：
  - 方案 A：删除时同时写 `deleted=true`，所有对外查询维持 `deleted=false`。
  - 方案 B：保留逻辑删除位不变，但所有客户侧查询默认强制 `status='ACTIVE'`。
  - `getFileById` / `getFileEntity` 也应校验 `status=ACTIVE`，至少客户侧路径必须如此。
  - 为“删除后列表不可见、详情不可见、重复上传可重新创建/不可见”补回归测试。
- 建议验证：
  - 删除后调用列表接口，不应再返回目标文件。
  - 删除后调用详情接口，应返回 404 或 403。

### CR-003 CORS 允许任意来源携带凭证

- 严重级别：P1
- 位置：
  - `backend/src/main/java/com/clientservice/infrastructure/config/CorsConfig.java:55`
- 问题说明：
  - 配置项声明了 `cors.allowed-origins`，但实际没有使用。
  - 当前实现固定 `addAllowedOriginPattern("*")`，同时 `setAllowCredentials(true)`。
  - 这意味着浏览器会把任意 `Origin` 回显为可访问来源。
- 风险影响：
  - 任何第三方站点都能直接跨域调用后台接口。
  - 一旦前端把 JWT 放到浏览器可读位置，或后续改为 Cookie，会显著放大跨站风险面。
- 修复方案：
  - 仅允许配置中的白名单域名。
  - 非开发环境禁止 `*`；开发环境单独放开 `localhost` / `127.0.0.1`。
  - 若必须开放公共接口，按路径拆分 CORS 策略，不要对 `/**` 一刀切。
- 建议验证：
  - 使用非白名单 `Origin` 访问后台接口，响应头中不应出现允许该域的 CORS 头。

### CR-004 自动生成访问链接时信任 Host / X-Forwarded 头

- 严重级别：P1
- 位置：
  - `backend/src/main/java/com/clientservice/common/util/UrlGenerator.java:35`
  - `backend/src/main/java/com/clientservice/common/util/UrlGenerator.java:74`
- 问题说明：
  - 当 `system.base-url` 为空或为 `auto` 时，系统会直接从请求头构造对外链接。
  - `Host`、`X-Forwarded-Proto`、`X-Forwarded-Host` 都未校验来源与格式。
  - 该方法会被项目接收、下载链接生成等流程复用。
- 风险影响：
  - 攻击者可通过伪造 Host 头拿到带恶意域名的访问链接。
  - 这些链接一旦写入数据库、通知短信/邮件或回包给上游系统，会形成钓鱼入口。
- 修复方案：
  - 生产环境强制使用配置化 `system.base-url`，不要从外部请求头推断。
  - 如必须支持反向代理动态推断，只信任网关层注入的固定头，并校验域名白名单。
  - 对 `host` 做格式校验，拒绝包含路径、空格、逗号、多值头的情况。
- 建议验证：
  - 带伪造 `Host` / `X-Forwarded-Host` 请求时，生成结果应仍为系统配置中的正式域名。

### CR-005 数据库备份文件可生成但不保证可恢复

- 严重级别：P2
- 位置：
  - `backend/src/main/java/com/clientservice/interfaces/rest/SystemMaintenanceController.java:41`
  - `backend/src/main/java/com/clientservice/interfaces/rest/SystemMaintenanceController.java:143`
  - `backend/src/main/java/com/clientservice/interfaces/rest/SystemMaintenanceController.java:379`
  - `backend/src/test/java/com/clientservice/interfaces/rest/SystemMaintenanceControllerTest.java:103`
- 问题说明：
  - `exportTableData` 只对 `String` / `Boolean` 做了特殊处理，其余类型直接 `toString()` 拼到 SQL 里。
  - 对 `timestamp`、`json/jsonb`、带特殊字符的数据库对象值，这种 SQL 序列化方式并不可靠。
  - 测试只断言“接口返回成功”和“文件名存在”，没有验证导出 SQL 可执行。
  - 同时，存储路径读取使用的是 `file.storage.path`，与实际项目配置 `client-service.file.storage.local.path` 不一致，系统状态页可能长期显示错误目录。
- 风险影响：
  - 运维拿到备份文件后，只有在恢复时才会暴露问题，恢复链路不可靠。
  - 状态页展示的存储路径可能误导排障。
- 修复方案：
  - 不要手写 SQL 备份；优先调用 `pg_dump` 或导出结构化 JSON/CSV 并明确恢复工具链。
  - 如果短期必须保留 Java 导出，至少按 JDBC 类型分别正确转义，并补“导出后可回灌”的集成测试。
  - 修正配置键为 `client-service.file.storage.local.path`。
- 建议验证：
  - 自动化测试中生成备份后，在临时数据库执行恢复并做行数校验。

## 建议修复顺序

1. 先修 `CR-001` 和 `CR-002`，这两项直接影响客户侧数据边界。
2. 再修 `CR-003` 和 `CR-004`，收紧后台与外链生成的安全面。
3. 最后修 `CR-005`，补齐运维恢复链路与配置一致性。

## 建议补测清单

- `FileController` 客户侧详情接口的鉴权测试
- 文件删除后的客户列表/详情回归测试
- CORS 白名单测试
- `UrlGenerator` 对伪造代理头的防御测试
- 备份生成后可恢复的集成测试
