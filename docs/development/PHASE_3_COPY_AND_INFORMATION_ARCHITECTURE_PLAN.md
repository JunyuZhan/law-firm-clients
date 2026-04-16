# 律所客户服务系统 Phase 3 实施计划

## 目标

在 Phase 2 已完成视觉、容器、反馈口径统一的基础上，继续推进：

- 模块级文案字典化
- 说明文案域模型化
- 后台复杂页面的信息架构优化

本阶段不调整后端接口，不引入微前端，不重写业务状态管理。

## 优先级

### P1. 模块级文案字典化

首批模块：

- `SystemConfig`
- `NotificationSettings`
- `SystemMaintenance`

实施方式：

- 保留 `uiTexts.ts` 承载跨模块通用词。
- 新增 `adminTexts.ts` 承载后台模块级说明文案、placeholder、helper text、模块标题。
- 后续逐步拆分出：
  - `portalTexts.ts`
  - `maintenanceTexts.ts`
  - `notificationTexts.ts`

### P2. 说明文案域模型化

优先收拢以下类型：

- `field-hint`
- `helper-text`
- `alert.message`
- `alert.description`
- 模块引导说明

建议按页面批次推进：

1. `SystemConfig`
2. `NotificationSettings`
3. `SystemMaintenance`
4. `NotificationTemplateManagement`
5. `ApiKeyManagement`

### P3. 后台信息架构优化

优先考虑的页面：

- `SystemConfig`
- `SystemMaintenance`
- `NotificationTemplateManagement`

目标：

- 将长表单拆为更明确的步骤区块
- 将高频操作与低频配置分层
- 将说明信息从“散落的段落”升级为“可扫描的二级面板”

## 当前已落地

- 已新增 `frontend/src/constants/adminTexts.ts`
- `SystemConfig.vue` 已开始接入模块级说明文案与 placeholder
- `NotificationSettings.vue` 已开始接入模块级说明文案与 placeholder
- `SystemMaintenance.vue` 已开始接入模块级说明文案与说明性标题
- `NotificationTemplateManagement.vue` 已开始接入模块级说明文案、模板说明和示例提示
- `ApiKeyManagement.vue` 已开始接入模块级说明文案、引导提示与表单校验文本
- `NotificationHistory.vue` 已开始接入模块级筛选文案、重试反馈与状态说明
- `Login.vue` 已开始接入登录、验证码和成功提示的模块级文案
- `InitialSetup.vue` 已开始接入品牌初始化文案、占位符与交付路径说明
- `MatterDetail.vue` 已开始接入案件详情说明、字段标签与失败反馈文案
- `AdminLayout.vue` 已开始接入版本提醒、菜单标签与退出失败提示的模块级文案
- `MatterList.vue` 已开始接入案件列表统计、筛选、表头与失败反馈文案
- `LetterVerificationList.vue` 已开始接入函件验证列表、统计、详情抽屉与状态文案
- `AdminProfile.vue` 已开始接入账户资料、安全设置与密码校验文案
- `FileManagement.vue` 已开始接入文件管理筛选、详情弹窗、批量操作与清理文案
- `NotificationSettings.vue` 已继续接入渠道状态、保存反馈与核心表单标签文案
- `NotificationTemplateManagement.vue` 已继续接入筛选、表格、表单标签与启停反馈文案
- `SystemConfig.vue` 已继续接入页签、字段标签、表格标题与保存状态文案

## 本轮新增收口范围

- `SystemMaintenance.vue`
  - 备份确认按钮、备份结果反馈、升级提示、日志加载失败与导出失败提示已并入模块字典
  - 状态面板标签、备份表头、日志筛选标签已并入模块字典
- `InitialSetup.vue`
  - 初始化入口说明、品牌占位文案、状态条与后续动作建议已并入模块字典
- `MatterDetail.vue`
  - 案件详情标题、字段标签、访问动作与异常提示已并入模块字典
- `AdminLayout.vue`
  - 版本更新提示与后台菜单的后台域文案已开始脱离散落硬编码
- `MatterList.vue`
  - 列表统计、筛选项、状态选项、表头与异常提示已并入模块字典
- `LetterVerificationList.vue`
  - 统计卡、筛选区、记录列表、详情抽屉与状态口径已并入模块字典
- `AdminProfile.vue`
  - 个人资料、安全设置、密码表单 placeholder 与校验文案已并入模块字典
- `FileManagement.vue`
  - 文件筛选、分类名称、详情弹窗、批量删除确认与清理反馈已并入模块字典
- `NotificationSettings.vue`
  - 通道开关状态、保存成功提示、接入状态与核心字段标签已并入模块字典
- `NotificationTemplateManagement.vue`
  - 筛选标题、表头、弹窗标签、启停反馈与校验警告已并入模块字典
- `SystemConfig.vue`
  - 页签命名、配置标签、布尔值口径、弹窗标签与校验提示已并入模块字典

## 剩余热点扫描

当前仍值得优先继续收口的后台页面：

- `NotificationHistory.vue`
  - 已完成大部分筛选与表头收口，仍可继续细化渠道摘要与个别统计文案
- `NotificationSettings.vue`
  - 仍可继续细化配置描述字段和 description 常量的域建模
- `NotificationTemplateManagement.vue`
  - 仍可继续细化示例内容与格式提示的域模型拆分
- `SystemConfig.vue`
  - 仍可继续收口 `showTotal`、描述映射及少量基础描述常量

## 验收标准

- 模块说明文案不再继续堆入全局 `uiTexts.ts`
- 首批复杂页面的说明文案具备域级归属
- 新增页面说明文案可直接复用既有模块常量
- `pnpm -C frontend typecheck` 通过
- `pnpm -C frontend build` 通过

## 暂不处理

- 微前端拆分
- 多入口架构调整
- 多语言能力接入
- 后台大规模路由重构

## 预期收益

- 文案维护边界更清晰
- 后续术语升级成本更低
- 复杂后台页的信息层级更稳定
- 为未来行业化定制和多语言支持预留更合理的扩展点
