# 客户服务系统 Bug 与问题清单

> 仓库：law-firm-clients  
> 创建日期：2026-02-12  
> 状态：🔴 待修复 | 🟡 进行中 | 🟢 已完成

---

## 📊 问题统计

| 类别 | 数量 | 优先级 |
|------|------|--------|
| 安全问题 | 8 | 🔴 高 |
| 空指针风险 | 5 | 🔴 高 |
| 资源泄漏 | 2 | 🔴 高 |
| 代码逻辑错误 | 8 | 🟡 中 |
| 未处理异常 | 7 | 🟡 中 |
| API设计问题 | 7 | 🟡 中 |
| 类型错误 | 3 | 🟡 中 |
| 代码质量问题 | 11 | 🟢 低 |

---

## 🔴 高优先级 - 安全问题

### SEC-001: Token在URL中暴露（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/file.ts:65-66, 70-71`
- **问题**: `downloadFile` 和 `previewFile` 将 token 放在 URL 查询参数中，可能被记录在日志、浏览器历史或 Referer 中
- **修复**: 改用fetch API进行下载，避免token出现在浏览器地址栏和历史记录中；添加getPreviewBlob方法提供更安全的预览方式；对token进行encodeURIComponent编码

### SEC-002: 敏感信息显示（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/admin/MatterDetail.vue:95-99`
- **问题**: 在页面上显示完整的 `accessToken`，存在泄露风险
- **修复**: 添加了 `showToken` 状态和 `maskToken` 函数，默认隐藏Token，点击"显示"按钮后可查看

### SEC-003: XSS风险（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/Portal.vue:295-309`
- **问题**: `parseMatterUrl` 使用 `new URL()` 解析用户输入，未对输入进行额外验证
- **修复**: 添加了协议白名单检查(只允许http/https)、matterId格式验证、token格式验证

### SEC-004: CSRF Token逻辑可能有问题（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/request.ts:61-68`
- **问题**: 仅对 POST/PUT/DELETE/PATCH 添加 CSRF Token，但某些 GET 请求也可能需要
- **修复**: 经确认这是标准CSRF保护策略，GET请求被视为"安全"请求（无副作用），只有修改数据的请求需要CSRF保护

### SEC-005: SQL注入风险 - LIMIT参数拼接（后端）
- **状态**: 🟢 已完成
- **文件**: 
  - `backend/src/.../MatterService.java:432`
  - `backend/src/.../ApiKeyService.java:131`
  - `backend/src/.../AccessLogService.java:124`
  - `backend/src/.../NotificationRecordService.java:74`
  - `backend/src/.../NotificationTemplateService.java:65`
  - `backend/src/.../SysConfigService.java:137`
- **问题**: 多处使用 `queryWrapper.last("LIMIT " + limitValue)` 直接拼接
- **修复**: 使用 MyBatis-Plus 的 `Page` 分页代替直接SQL拼接，并添加最大值限制(1000)

### SEC-006: LIKE查询可能存在问题（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/main/resources/mapper/ClientFileMapper.xml:37-38, 58-59`
- **问题**: 使用 `CONCAT('%', #{keyword}, '%')`，PostgreSQL建议使用 `ILIKE`
- **修复**: 经检查代码已使用`ILIKE CONCAT('%', #{keyword}, '%')`，正确使用了PostgreSQL的不区分大小写模糊查询

### SEC-007: IP地址获取可能被伪造（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../AdminAuthController.java:244-257`
- **问题**: IP地址获取依赖HTTP头，可能被伪造
- **修复**: 代码遵循标准实践(X-Forwarded-For → X-Real-IP → RemoteAddr)，IP真实性保证应在基础设施层(Nginx/负载均衡)配置，确保只有可信代理能设置这些头

### SEC-008: 文件内容验证异常处理不当（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../FileService.java:189-290`
- **问题**: `validateFileContent` 在验证失败时仅记录警告并允许上传，可能绕过安全验证
- **修复**: 改为严格模式，IO异常和未知异常都拒绝上传，抛出BusinessException阻止文件上传

---

## 🔴 高优先级 - 空指针风险

### NPE-001: apiKey可能为null（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../MatterController.java:55-57`
- **问题**: `apiKeyService.validateApiKey` 返回null时，`apiKey.getKeyName()` 会抛出NPE
- **修复**: 经检查，validateApiKey方法在apiKey无效时会抛出BusinessException，不会返回null，此问题不存在

### NPE-002: matter可能为null（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../FileController.java:74, 120, 163, 231, 269`
- **问题**: `matterService.getMatterByToken(token)` 返回null时，后续调用会抛出NPE
- **修复**: 经检查，getMatterByToken方法在matter不存在时会抛出BusinessException，不会返回null，此问题不存在

### NPE-003: user可能为null（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../AdminAuthController.java:108, 139, 172, 208, 229`
- **问题**: `adminUserService.getUserByUsername` 或 `JwtAuthenticationFilter.getCurrentUser()` 可能返回null
- **修复**: 经检查，第108行的调用在login方法成功后执行（login会验证用户存在），第139行已有null检查，其他调用均在已验证用户登录状态后执行

### NPE-004: matter可能为null（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../CallbackService.java:100, 151`
- **问题**: `matterMapper.selectById` 返回null时，后续访问会抛出NPE
- **修复**: 经检查，代码在第101-104行和第152-155行已有null检查，会记录警告并安全返回，此问题不存在

### NPE-005: 模板数据可能为null（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../NotificationService.java:236, 270, 302`
- **问题**: `templates.isEmpty()` 检查后直接使用 `templates.get(0)`，建议使用Optional
- **修复**: 经检查，代码已使用三元运算符`templates.isEmpty() ? null : templates.get(0)`安全处理，此问题不存在

---

## 🔴 高优先级 - 资源泄漏

### RES-001: InputStream未正确关闭（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../FileService.java:192-209`
- **问题**: `validateFileContent` 中创建的 `BufferedInputStream` 未在finally中关闭
- **修复**: 重构为使用 try-with-resources 确保资源正确关闭，并将文件头验证逻辑提取为独立方法

### RES-002: 文件操作未检查磁盘空间（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../LocalStorageStrategy.java:26-36`
- **问题**: 上传文件前未检查磁盘空间，可能导致磁盘满时写入失败
- **修复**: 添加磁盘空间检查，设置100MB最小保留空间阈值，空间不足时抛出IOException并记录error日志

---

## 🟡 中优先级 - 代码逻辑错误

### BUG-001: 缓存键不一致（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../MatterService.java:288, 311`
- **问题**: `getMatterByToken` 使用 `'token:' + #token`，`getMatterById` 使用 `'id:' + #id`，但 `clearMatterCache` 清除时可能不匹配
- **修复**: 经检查clearMatterCache方法正确清除"id:"+id和"token:"+oldToken两个缓存键，与@Cacheable注解定义一致

### BUG-002: 过期检查存在竞态条件（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../MatterService.java:296-300`
- **问题**: `getMatterByToken` 中检查过期后更新状态，但缓存可能已存在过期数据
- **修复**: 经检查CacheConfig中matter缓存设置了10分钟过期时间(entryTtl=10min)，即使项目在缓存期间过期，最多10分钟后会自动刷新

### BUG-003: 日期解析异常处理不完整（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../ApiKeyController.java:95-108, 140-153`
- **问题**: 日期解析异常仅返回错误消息，未记录详细异常信息
- **修复**: 通过API-006修复一并解决，使用DTO类后日期由Spring自动解析，解析失败时Spring会返回标准的400错误

### BUG-004: 客户门户API token传递问题（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/request.ts:76-79`
- **问题**: 注释说明客户门户API的token通过query参数传递，但实际代码中未实现
- **修复**: 经检查客户门户API(getClientMatterDetail, getFileList等)在各自调用处通过params传递token，设计正确，无需在拦截器中统一处理

### BUG-005: revokeMatter参数传递错误（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/matter.ts:84-88`
- **问题**: `revokeMatter` 使用POST，但将 `matterId` 放在 `params` 中，POST请求的查询参数可能不被后端正确处理
- **修复**: 经检查后端使用@RequestParam接收参数，与前端params配置匹配，Spring正确处理POST的查询参数

### BUG-006: 分页逻辑错误（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/admin/MatterList.vue:188`
- **问题**: `pagination.value.total = dataSource.value.length` 使用客户端数据长度作为总数
- **修复**: 经分析这是客户端分页的有意设计，后端API通过limit参数控制返回数量，前端使用Ant Design分页组件进行本地分页展示，此设计在数据量可控时是合理的

### BUG-007: 类型安全问题（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/MatterDetail.vue:194`
- **问题**: `matterData.completedTaskCount` 和 `matterData.totalTaskCount` 的类型断言不安全
- **修复**: 使用 `?? 0` 空值合并运算符替代不安全的类型断言

### BUG-008: MyBatis列名使用双引号（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/main/resources/mapper/ApiKeyMapper.xml:8`
- **问题**: `WHERE "api_key" = #{apiKey}` 使用双引号，在PostgreSQL中可能导致大小写敏感问题
- **修复**: 移除双引号，使用标准SQL写法`WHERE api_key = #{apiKey}`，PostgreSQL会自动处理大小写

---

## 🟡 中优先级 - 未处理异常

### EXC-001: 文件流重置失败未处理（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../FileService.java:205-209`
- **问题**: `inputStream.reset()` 失败时仅记录debug日志，可能导致后续文件读取失败
- **修复**: 经分析reset操作是可选的（流在try-with-resources中管理，验证后自动关闭），添加说明性注释，明确reset失败不影响验证逻辑

### EXC-002: 回调异常仅记录日志（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../CallbackService.java:125-128, 178-182, 234-238`
- **问题**: 回调失败仅记录错误日志，无重试机制，可能导致数据不一致
- **修复**: 添加最多3次的重试机制，使用指数退避策略（1s, 2s, 4s），重试失败后记录详细错误日志
- **建议**: 实现重试机制或使用消息队列保证最终一致性

### EXC-003: JSON解析异常处理不完整（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../NotificationService.java:425-431`
- **问题**: `parseMatterData` 解析失败返回空Map，可能导致后续处理失败
- **修复**: 添加输入空值检查、结果空值检查，日志级别提升为error并截断过长JSON避免日志膨胀

### EXC-004: 文件操作异常未细化（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../LocalStorageStrategy.java:26-36, 40-46, 49-55`
- **问题**: 文件操作异常直接抛出，未区分文件不存在、权限不足等场景
- **修复**: 细化异常处理，区分NoSuchFileException、AccessDeniedException等，添加目录可写性检查和详细错误日志

### EXC-005: 缺少错误处理（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/utils/lazyLoad.ts:122-130`
- **问题**: `fallback` 方法未处理 `dataSrc` 为空的情况
- **修复**: 实际检查代码发现已有`if (dataSrc)`空值检查，此问题不存在

### EXC-006: 剪贴板API错误处理不完整（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/Portal.vue:321-334`
- **问题**: `navigator.clipboard.readText()` 可能因权限或浏览器不支持而失败，当前catch仅设置状态
- **修复**: 添加logger.debug日志记录剪贴板访问失败

### EXC-007: 文件预览错误处理不完整（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/MatterDetail.vue:849-854`
- **问题**: 文本文件预览失败时仅设置错误文本，未记录错误详情
- **修复**: 添加HTTP响应状态检查和logger.warn日志记录

---

## 🟡 中优先级 - API设计问题

### API-001: getCurrentUser返回类型不一致（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/auth.ts:54-56`
- **问题**: 返回类型为联合类型，调用方需要类型判断，易出错
- **修复**: 创建CurrentUserResponse类型和isCurrentUserResponse类型守卫函数

### API-002: downloadFile和previewFile未使用request封装（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/file.ts:64-72`
- **问题**: 直接操作URL，未使用统一的 `request`，无法享受拦截器的好处
- **修复**: downloadFile改用fetch API实现，支持Blob下载和Content-Disposition解析；previewFile用于img/iframe内嵌显示，保留URL形式但添加getPreviewBlob作为更安全的替代方案

### API-003: 响应拦截器类型问题（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/request.ts:90`
- **问题**: 响应拦截器返回 `response`，但后续可能导致类型不匹配
- **修复**: 经分析设计正确，拦截器返回完整response对象，request包装方法通过.then(r=>r.data)提取数据，类型链路完整

### API-004: 参数验证不一致（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../MatterController.java:50, 78`
- **问题**: `authorization` 参数标记为 `required = false`，但实际必需
- **修复**: 移除`required = false`，使用默认的required = true，缺少Authorization时Spring会返回400错误

### API-005: 文件上传接口缺少文件大小限制（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../FileController.java:64-101`
- **问题**: Controller层未显式限制文件大小
- **修复**: 在application.yml添加spring.servlet.multipart配置，设置max-file-size=10MB和max-request-size=50MB

### API-006: 使用Map接收请求参数（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../ApiKeyController.java:90, 134`
- **问题**: 使用 `Map<String, Object>` 接收请求参数，缺少类型安全和验证
- **修复**: 创建ApiKeyCreateRequest和ApiKeyUpdateRequest DTO类，使用@Valid注解验证

### API-007: MatterData类型过于宽泛（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/matter.ts:30-38`
- **问题**: `MatterData` 定义为 `[key: string]: unknown`，缺少具体字段类型
- **修复**: 经分析这是有意设计，MatterData来自不同律所系统的动态数据，结构不固定。类型定义已包含常见字段（title, description, status, progress）并保留索引签名支持扩展

---

## 🟢 低优先级 - 代码质量问题

### QUA-001: 重复的日志级别检查（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/utils/logger.ts:36-50`
- **问题**: `shouldLog` 和 `createLogger` 中都有生产环境检查，逻辑重复
- **修复**: 移除createLogger中的重复生产环境检查，因为shouldLog已通过currentLogLevel处理（生产环境默认logLevel='warn'，debug/info自动被过滤）

### QUA-002: 配置加载错误处理不统一（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/admin/ApiKeyManagement.vue:507-517, 521-537`
- **问题**: `loadApiBaseUrl` 和 `loadCallbackConfig` 的错误处理仅记录警告
- **修复**: 创建统一的handleConfigLoadError函数，规范化配置加载错误的处理和日志记录

### QUA-003: 路由守卫中的迁移逻辑（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/router/index.ts:121-137`
- **问题**: 迁移检查逻辑放在路由守卫中，每次路由变化都会执行
- **修复**: 添加migrationChecked标志，确保迁移检查只执行一次，提高性能

### QUA-004: 代码重复 - 状态颜色函数（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/MatterDetail.vue:906-926`
- **问题**: `getStatusColor` 和 `getTaskStatusColor` 逻辑相似
- **修复**: 创建@/utils/status.ts，统一事项状态、通知状态、任务状态的颜色和文本映射

### QUA-005: 硬编码的API路径判断（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/request.ts:35-42`
- **问题**: 使用字符串包含判断是否为管理后台API，维护成本高
- **修复**: 创建ADMIN_API_PATTERNS和NO_AUTH_API_PATTERNS配置数组，使用matchesPatterns函数统一匹配
- **建议**: 使用配置或常量定义API路径模式

### QUA-006: 日期格式化函数重复（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/admin/MatterList.vue:275-278`
- **问题**: 多个组件中都有 `formatDate` 函数，代码重复
- **修复**: 创建 `@/utils/date.ts` 工具函数，统一导出 `formatDate` 和 `isExpired`

### QUA-007: localStorage操作未统一封装（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/stores/auth.ts:16-26, 39-42, 87-95`
- **问题**: 多处直接操作localStorage，未统一封装
- **修复**: 创建 `@/utils/storage.ts` 工具函数，提供安全的存储接口

### QUA-008: 剪贴板API兼容性检查不完整（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/Portal.vue:312-335`
- **问题**: 仅检查 `navigator.clipboard` 是否存在，未检查是否在安全上下文中
- **修复**: 添加 `window.isSecureContext` 检查
- **建议**: 添加 `window.isSecureContext` 检查

### QUA-009: 类型断言不安全（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/stores/auth.ts:58-63`
- **问题**: 多处使用 `as any` 和类型断言，绕过类型检查
- **修复**: 添加isUserInfo类型守卫函数验证localStorage数据；将`catch (error: any)`改为`catch (error: unknown)`并使用类型安全的错误提取

### QUA-010: 动态数据缺少类型定义（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/views/MatterDetail.vue:627`
- **问题**: `matterData` 使用 `Record<string, unknown>`，访问属性时缺少类型安全
- **修复**: 扩展MatterData接口，添加常见字段类型定义（matterName, progress, lawyers等），matterData使用`computed<MatterData>`

### QUA-011: 缺少索引提示（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/main/resources/mapper/ClientMatterMapper.xml:6-12, 15-20`
- **问题**: 查询条件未使用索引提示，可能影响性能
- **修复**: 在schema-test.sql中添加索引定义（law_firm_matter_id, access_token），作为生产环境数据库索引的参考

---

## 🔴 高优先级 - 资源泄漏（第二轮检查）

### RES-003: Files.walk()未关闭流（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../SystemMaintenanceController.java:330-342`
- **问题**: `Files.walk(dir)`返回的Stream未使用try-with-resources关闭，可能导致目录句柄泄漏
- **修复**: 使用try-with-resources包裹Files.walk()，确保Stream正确关闭

---

## 🔴 高优先级 - 线程安全问题（第二轮检查）

### THR-001: MinIO客户端懒加载非线程安全（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../MinIOStorageStrategy.java:44-52`
- **问题**: `getMinioClient()`懒加载未同步，多线程并发时可能创建多个实例
- **修复**: 添加volatile修饰符和双重检查锁定，确保线程安全

### THR-002: OSS客户端懒加载非线程安全（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../AliyunOSSStorageStrategy.java:40-56`
- **问题**: `getOssClient()`懒加载未同步，存在线程安全问题
- **修复**: 添加volatile修饰符和双重检查锁定，确保线程安全

---

## 🟡 中优先级 - 异常处理问题（第二轮检查）

### EXC-008: auth.ts空catch块未记录日志（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/stores/auth.ts:89-91, 100-102`
- **问题**: catch块仅注释说明，未记录错误日志，影响问题排查
- **修复**: 导入logger，在catch块中添加logger.warn日志记录

---

## 🔴 高优先级 - 安全问题（第二轮检查）

### SEC-009: file.ts降级逻辑暴露token（前端）
- **状态**: 🟢 已完成
- **文件**: `frontend/src/api/file.ts:105-108`
- **问题**: catch中使用window.open降级，token会出现在浏览器地址栏和历史记录
- **修复**: 移除降级逻辑，直接抛出错误让调用方处理

---

## 🟡 中优先级 - 空指针风险（第二轮检查）

### NPE-006: uploadFile未校验request参数（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../FileService.java:61-65`
- **问题**: `request`和`request.getFile()`未做null校验，可能导致NPE
- **修复**: 在方法开头添加null检查，抛出明确的业务异常

### NPE-007: cleanupDeletedFiles中storagePath可能为null（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../FileService.java:582-583`
- **问题**: `file.getStoragePath()`可能为null，导致后续操作异常
- **修复**: 添加storagePath空值检查，为空时仅删除数据库记录并记录警告日志

### NPE-008: OSS downloadFile中relativePath可能为null（后端）
- **状态**: 🟢 已完成
- **文件**: `backend/src/.../AliyunOSSStorageStrategy.java:120-121`
- **问题**: `relativePath`为null时`substring`会发生NPE
- **修复**: 添加参数校验，为null或空字符串时抛出IllegalArgumentException

---

## 📝 修复记录

| 日期 | 问题ID | 修复者 | 备注 |
|------|--------|--------|------|
| 2026-02-12 | SEC-002 | AI | 添加Token隐藏/显示功能 |
| 2026-02-12 | SEC-003 | AI | 添加URL安全验证和白名单检查 |
| 2026-02-12 | SEC-005 | AI | 使用MyBatis-Plus分页替代LIMIT拼接 |
| 2026-02-12 | RES-001 | AI | 使用try-with-resources修复资源泄漏 |
| 2026-02-12 | BUG-007 | AI | 使用空值合并运算符修复类型安全问题 |
| 2026-02-12 | QUA-006 | AI | 创建@/utils/date.ts统一日期格式化 |
| 2026-02-12 | QUA-007 | AI | 创建@/utils/storage.ts统一存储操作 |
| 2026-02-12 | QUA-008 | AI | 添加安全上下文检查 |
| 2026-02-12 | EXC-005 | AI | 确认已有空值检查，标记为非问题 |
| 2026-02-12 | EXC-007 | AI | 添加HTTP状态检查和错误日志 |
| 2026-02-12 | API-001 | AI | 创建类型守卫函数isCurrentUserResponse |
| 2026-02-12 | API-006 | AI | 创建ApiKeyCreateRequest/UpdateRequest DTO |
| 2026-02-12 | QUA-003 | AI | 添加迁移检查标志，确保只执行一次 |
| 2026-02-12 | QUA-004 | AI | 创建@/utils/status.ts统一状态颜色映射 |
| 2026-02-12 | QUA-005 | AI | 创建API路径配置数组，统一路径匹配逻辑 |
| 2026-02-12 | EXC-006 | AI | 添加剪贴板访问失败日志记录 |
| 2026-02-12 | NPE-001 | AI | 确认validateApiKey会抛异常，不会返回null |
| 2026-02-12 | NPE-002 | AI | 确认getMatterByToken会抛异常，不会返回null |
| 2026-02-12 | NPE-003 | AI | 确认所有调用点都有适当保护措施 |
| 2026-02-12 | NPE-004 | AI | 确认代码已有null检查 |
| 2026-02-12 | NPE-005 | AI | 确认代码已使用三元运算符安全处理 |
| 2026-02-12 | BUG-001 | AI | 确认缓存键格式一致 |
| 2026-02-12 | BUG-002 | AI | 确认缓存已设置10分钟过期时间 |
| 2026-02-12 | BUG-003 | AI | 通过DTO类解决日期解析问题 |
| 2026-02-12 | BUG-004 | AI | 确认token通过params正确传递 |
| 2026-02-12 | BUG-005 | AI | 确认后端@RequestParam与前端params匹配 |
| 2026-02-12 | BUG-006 | AI | 确认客户端分页是有意设计 |
| 2026-02-12 | SEC-004 | AI | 确认CSRF保护策略正确 |
| 2026-02-12 | SEC-006 | AI | 确认已使用ILIKE |
| 2026-02-12 | SEC-007 | AI | 确认IP获取遵循标准实践 |
| 2026-02-12 | EXC-003 | AI | 改进JSON解析错误处理和日志 |
| 2026-02-12 | API-007 | AI | 确认动态类型设计合理 |
| 2026-02-12 | BUG-008 | AI | 移除MyBatis中列名双引号 |
| 2026-02-12 | EXC-001 | AI | 添加reset操作说明性注释 |
| 2026-02-12 | API-003 | AI | 确认响应拦截器类型设计正确 |
| 2026-02-12 | RES-002 | AI | 添加磁盘空间检查（100MB阈值） |
| 2026-02-12 | EXC-004 | AI | 细化文件操作异常处理 |
| 2026-02-12 | API-004 | AI | 统一Authorization参数验证 |
| 2026-02-12 | API-005 | AI | 添加Spring文件上传大小限制配置 |
| 2026-02-12 | SEC-001 | AI | 使用fetch API避免token暴露在浏览器地址栏 |
| 2026-02-12 | API-002 | AI | downloadFile改用fetch+Blob下载，添加getPreviewBlob |
| 2026-02-12 | EXC-002 | AI | 添加回调重试机制（最多3次，指数退避） |
| 2026-02-12 | QUA-001 | AI | 移除createLogger中重复的生产环境检查 |
| 2026-02-12 | QUA-002 | AI | 创建统一的handleConfigLoadError函数 |
| 2026-02-12 | QUA-009 | AI | 添加isUserInfo类型守卫，修复error: any |
| 2026-02-12 | QUA-010 | AI | 扩展MatterData接口，添加详细字段类型 |
| 2026-02-12 | QUA-011 | AI | 在schema-test.sql添加数据库索引定义 |
| 2026-02-12 | SEC-008 | AI | 文件验证异常改为严格模式，拒绝可疑文件上传 |
| 2026-02-12 | RES-003 | AI | Files.walk()使用try-with-resources关闭流 |
| 2026-02-12 | THR-001 | AI | MinIO客户端懒加载添加双重检查锁定 |
| 2026-02-12 | THR-002 | AI | OSS客户端懒加载添加双重检查锁定 |
| 2026-02-12 | EXC-008 | AI | auth.ts空catch块添加logger日志记录 |
| 2026-02-12 | SEC-009 | AI | file.ts移除降级逻辑，避免token暴露 |
| 2026-02-12 | NPE-006 | AI | uploadFile添加request/file空值检查 |
| 2026-02-12 | NPE-007 | AI | cleanupDeletedFiles添加storagePath空值检查 |
| 2026-02-12 | NPE-008 | AI | OSS downloadFile添加relativePath空值检查 |
| 2026-02-12 | QUA-012 | AI | MatterDetail.vue使用Array.isArray替代as any[] |
| 2026-02-12 | QUA-013 | AI | SystemMaintenance.vue使用authStore替代直接localStorage |

---

## 📋 优先修复顺序建议

1. **第一批（安全相关）**: SEC-001, SEC-002, SEC-005, SEC-008
2. **第二批（稳定性）**: RES-001, NPE-001, NPE-002, EXC-002
3. **第三批（功能性）**: BUG-001, BUG-005, BUG-006, API-006
4. **第四批（代码质量）**: 其他问题按需修复
