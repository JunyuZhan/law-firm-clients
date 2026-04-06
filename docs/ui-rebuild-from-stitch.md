# Stitch UI 重构清单

## 目标

把 Stitch 导出的 Lex Elite 视觉稿，落到当前 `frontend/` 的 `Vue 3 + TypeScript + Ant Design Vue` 项目中，优先复用现有路由、数据接口和状态管理，不做无关业务扩展。

## 已有素材

- Stitch 下载目录：`/Users/apple/Documents/Project/law-firm-clients/.stitch-downloads/10915524383963336703`
- 设计系统：
  - `design-system.json`
  - `design-system.md`
- 页面产物：
  - `screen.html`
  - `screen.json`
  - 部分页面包含 `screenshot.png`

## 当前前端结构

### Portal

- 首页：[frontend/src/views/Portal.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/Portal.vue)
- 项目详情：[frontend/src/views/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/MatterDetail.vue)
- 文件中心：[frontend/src/views/ClientFileList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientFileList.vue)
- 消息中心：[frontend/src/views/ClientNotifications.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientNotifications.vue)
- 个人中心：[frontend/src/views/ClientProfile.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientProfile.vue)
- 帮助中心：[frontend/src/views/HelpCenter.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/HelpCenter.vue)
- 函件验证：[frontend/src/views/verify/LetterVerify.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/verify/LetterVerify.vue)

### Dashboard

- 总布局：[frontend/src/views/admin/AdminLayout.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminLayout.vue)
- 登录页：[frontend/src/views/admin/Login.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/Login.vue)
- 项目列表：[frontend/src/views/admin/MatterList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterList.vue)
- 项目详情：[frontend/src/views/admin/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterDetail.vue)
- 文件管理：[frontend/src/views/admin/FileManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/FileManagement.vue)
- 通知记录：[frontend/src/views/admin/NotificationHistory.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationHistory.vue)
- 通知模板：[frontend/src/views/admin/NotificationTemplateManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationTemplateManagement.vue)
- 通知配置：[frontend/src/views/admin/NotificationSettings.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationSettings.vue)
- 系统配置：[frontend/src/views/admin/SystemConfig.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemConfig.vue)
- 系统信息：[frontend/src/views/admin/SystemInfo.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemInfo.vue)
- 系统维护：[frontend/src/views/admin/SystemMaintenance.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemMaintenance.vue)
- API Key 管理：[frontend/src/views/admin/ApiKeyManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/ApiKeyManagement.vue)
- 验证记录：[frontend/src/views/admin/LetterVerificationList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/LetterVerificationList.vue)
- 管理员个人中心：[frontend/src/views/admin/AdminProfile.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminProfile.vue)

## Stitch 页面到现有文件的映射

### 文档与系统层

- `Lex Elite PRD 设计基准文档`
  - 用途：业务和视觉基准参考
  - 落地方式：不直接转代码，只作为信息架构与文案风格约束
- `Design System`
  - 来源：`design-system.json` + `design-system.md`
  - 首要落点：
    - [frontend/src/App.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/App.vue)
    - [frontend/src/style.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/style.css)
    - [frontend/src/styles/theme.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/styles/theme.css)
- `Lex Elite UI 结构规范 (开发导向)`
  - 用途：页面模块分层参考
  - 落地方式：拆成页面模板和共享组件，不直接照抄 HTML

### Portal

- `Portal 首页 - 链接录入与品牌展示`
  - 目标文件：[frontend/src/views/Portal.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/Portal.vue)
  - 关联共享组件：[frontend/src/components/AppHeader.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/components/AppHeader.vue)
- `Portal 项目详情 - 进度追踪与文件快照`
  - 目标文件：[frontend/src/views/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/MatterDetail.vue)
- `Portal 消息中心 - 通知列表`
  - 目标文件：[frontend/src/views/ClientNotifications.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientNotifications.vue)
- `Portal 文件中心 - 资料库管理`
  - 目标文件：[frontend/src/views/ClientFileList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientFileList.vue)
- `Portal 函件验证 - 安全核查与即时反馈`
  - 目标文件：[frontend/src/views/verify/LetterVerify.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/verify/LetterVerify.vue)

### Dashboard

- `Dashboard 登录页 - 律所管理入口`
  - 目标文件：[frontend/src/views/admin/Login.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/Login.vue)
- `Dashboard 项目列表 - 案件全周期概览`
  - 目标文件：[frontend/src/views/admin/MatterList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterList.vue)
- `Dashboard 项目详情 - 进度配置与信息维护`
  - 目标文件：[frontend/src/views/admin/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterDetail.vue)
- `Dashboard 通知模板 - 文案配置与分发管理`
  - 目标文件：[frontend/src/views/admin/NotificationTemplateManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationTemplateManagement.vue)
- `Dashboard 文件管理 - 律所全域资料库`
  - 目标文件：[frontend/src/views/admin/FileManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/FileManagement.vue)
- `Dashboard 验证记录 - 数字化核查审计台账`
  - 目标文件：[frontend/src/views/admin/LetterVerificationList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/LetterVerificationList.vue)
- `Dashboard 系统配置 - 安全设置与权限审计`
  - 主目标文件：[frontend/src/views/admin/SystemConfig.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemConfig.vue)
  - 次级拆分目标：
    - [frontend/src/views/admin/SystemInfo.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemInfo.vue)
    - [frontend/src/views/admin/ApiKeyManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/ApiKeyManagement.vue)
    - [frontend/src/views/admin/NotificationSettings.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationSettings.vue)

## 共享基础层改造

### 1. 全局主题

优先统一这三处：

- [frontend/src/App.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/App.vue)
  - 更新 Ant Design Vue token
  - 将 Portal / Dashboard 的品牌色和圆角统一
- [frontend/src/style.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/style.css)
  - 定义全局背景、文字层级、阴影、面板基色
- [frontend/src/styles/theme.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/styles/theme.css)
  - 补充 Lex Elite 语义变量，例如：
    - `--lex-bg`
    - `--lex-surface`
    - `--lex-primary`
    - `--lex-accent`
    - `--lex-muted`
    - `--lex-panel-shadow`

### 2. Portal 共享组件

优先抽象：

- `PortalTopNav`
  - 基于 [frontend/src/components/AppHeader.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/components/AppHeader.vue)
  - 支持首页、详情、列表页三种模式
- `PortalSectionCard`
  - 用于首页能力卡片、详情侧栏块、消息中心面板
- `PortalStatusTag`
  - 统一“进行中 / 待处理 / 已完成 / 已过期 / 验证成功 / 验证失败”
- `PortalMetaList`
  - 用于详情页概览、文件属性、验证结果摘要

### 3. Dashboard 共享组件

优先抽象：

- `AdminPageHeader`
  - 替代各后台页面重复的标题、副标题、右侧操作区
- `AdminStatCards`
  - 后台列表和配置页顶部统计卡
- `AdminFilterBar`
  - 统一搜索、状态筛选、日期筛选、重置/查询按钮
- `AdminDataTableShell`
  - 表头说明、表格、分页、空状态
- `AuditTimeline`
  - 文件审计和验证记录可复用

## 页面级重构建议

### 第一批：先做品牌观感和骨架

1. [frontend/src/App.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/App.vue)
   - 对齐 Stitch 设计系统色板和圆角
2. [frontend/src/components/AppHeader.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/components/AppHeader.vue)
   - 建立 Portal 统一顶栏
3. [frontend/src/views/admin/AdminLayout.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminLayout.vue)
   - 建立 Dashboard 统一左侧栏、顶栏、页面容器

原因：

- 这三处决定 80% 的第一眼视觉
- 后续页面套入时工作量最低
- 先改骨架，后改单页，能避免重复返工

### 第二批：优先做最常用页面

1. [frontend/src/views/Portal.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/Portal.vue)
   - 保留“完整访问链接录入”核心交互
   - 按 Stitch 首页重组版面，不要引入短验证码逻辑
2. [frontend/src/views/admin/MatterList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterList.vue)
   - 升级为后台主工作台风格
   - 强化统计卡、筛选区、表格层次
3. [frontend/src/views/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/MatterDetail.vue)
   - 把当前已有的数据块重组为“概览 + 时间轴/进度 + 文件快照 + 最近动态”

### 第三批：按业务分组推进

#### Portal 内容页

- [frontend/src/views/ClientFileList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientFileList.vue)
- [frontend/src/views/ClientNotifications.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientNotifications.vue)
- [frontend/src/views/verify/LetterVerify.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/verify/LetterVerify.vue)

#### Dashboard 内容页

- [frontend/src/views/admin/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterDetail.vue)
- [frontend/src/views/admin/FileManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/FileManagement.vue)
- [frontend/src/views/admin/NotificationTemplateManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationTemplateManagement.vue)
- [frontend/src/views/admin/LetterVerificationList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/LetterVerificationList.vue)
- [frontend/src/views/admin/SystemConfig.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemConfig.vue)

## 实施策略

### 策略 1：只借结构，不照搬 HTML

Stitch 导出的 `screen.html` 适合作为：

- 模块顺序参考
- 文案层级参考
- 视觉分区参考

不适合作为：

- 直接复制进 Vue SFC
- 直接替代 Ant Design Vue 组件
- 直接当成生产代码

### 策略 2：优先保留现有业务逻辑

不建议动这些层：

- 路由：[frontend/src/router/index.ts](/Users/apple/Documents/Project/law-firm-clients/frontend/src/router/index.ts)
- API：`frontend/src/api/*`
- Pinia store：`frontend/src/stores/*`

建议只重构：

- 模板结构
- 样式体系
- 组件拆分
- 页面信息层级

### 策略 3：Portal 和 Dashboard 分开处理

不要混用同一套页面容器。

- Portal：更柔和、留白更大、强调“安全访问 / 当前进度 / 最近文件 / 最近通知”
- Dashboard：信息密度更高、突出筛选、操作、审计、系统控制

## 风险点

### 1. Stitch 文案偏概念化

设计系统里仍有一些不适合落地的叙事词，例如：

- Sovereign Ledger
- 区块链式表述
- 夸张安全概念

落地时应替换成项目真实文案：

- 安全访问
- 权限控制
- 操作留痕
- 文件受控分发
- 函件真伪核验

### 2. 部分页面对现有业务字段有额外想象

例如：

- 审计台账字段可能比现在接口多
- 后台详情中的团队成员、权限块未必已有接口

处理方式：

- 先使用静态占位模块
- 与现有接口字段对齐
- 不为视觉稿新增虚假业务

### 3. 文件和验证页面容易过度设计

这两个页面最容易被视觉稿带偏。建议始终优先：

- 可读性
- 搜索和筛选
- 状态清晰
- 操作路径短

## 推荐开工顺序

1. 全局主题和样式变量
2. Portal 顶栏 + Dashboard Layout
3. Portal 首页
4. Dashboard 项目列表
5. Portal 项目详情
6. Dashboard 项目详情
7. 文件中心双端
8. 消息 / 通知模板 / 验证记录 / 系统配置

## 下一步建议

如果继续动手实现，推荐按下面的批次提任务：

### 批次 A

- 改 [frontend/src/App.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/App.vue)
- 改 [frontend/src/style.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/style.css)
- 改 [frontend/src/styles/theme.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/styles/theme.css)
- 改 [frontend/src/components/AppHeader.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/components/AppHeader.vue)
- 改 [frontend/src/views/admin/AdminLayout.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminLayout.vue)

### 批次 B

- 改 [frontend/src/views/Portal.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/Portal.vue)
- 改 [frontend/src/views/admin/MatterList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterList.vue)

### 批次 C

- 改 [frontend/src/views/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/MatterDetail.vue)
- 改 [frontend/src/views/admin/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterDetail.vue)

