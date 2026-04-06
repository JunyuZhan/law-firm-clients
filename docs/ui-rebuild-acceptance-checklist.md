# UI 重构验收清单

## 目的

用于验收本轮基于 Stitch 规划文档落地的 Portal / Dashboard UI 重构，重点确认：

- 视觉层级是否统一
- 关键业务流是否可正常完成
- 移动端和窄屏下是否出现明显布局回归
- 本轮抽出的共享样式是否在各页面表现一致

## 当前完成范围

### 全局与骨架

- [frontend/src/App.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/App.vue)
- [frontend/src/style.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/style.css)
- [frontend/src/styles/theme.css](/Users/apple/Documents/Project/law-firm-clients/frontend/src/styles/theme.css)
- [frontend/src/components/AppHeader.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/components/AppHeader.vue)
- [frontend/src/views/admin/AdminLayout.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminLayout.vue)

### Portal 页面

- [frontend/src/views/Portal.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/Portal.vue)
- [frontend/src/views/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/MatterDetail.vue)
- [frontend/src/views/ClientFileList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientFileList.vue)
- [frontend/src/views/ClientNotifications.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientNotifications.vue)
- [frontend/src/views/verify/LetterVerify.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/verify/LetterVerify.vue)

### Dashboard 页面

- [frontend/src/views/admin/MatterList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterList.vue)
- [frontend/src/views/admin/FileManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/FileManagement.vue)
- [frontend/src/views/admin/NotificationHistory.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationHistory.vue)
- [frontend/src/views/admin/NotificationTemplateManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationTemplateManagement.vue)
- [frontend/src/views/admin/LetterVerificationList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/LetterVerificationList.vue)
- [frontend/src/views/admin/SystemConfig.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemConfig.vue)
- [frontend/src/views/admin/NotificationSettings.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationSettings.vue)
- [frontend/src/views/admin/SystemMaintenance.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemMaintenance.vue)
- [frontend/src/views/admin/ApiKeyManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/ApiKeyManagement.vue)
- [frontend/src/views/admin/AdminProfile.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminProfile.vue)

## 通用视觉验收

- [ ] 页面头部采用统一的标题、副标题和右侧操作区层级。
- [ ] 顶部统计卡在同一页面内的高度、字号和间距一致。
- [ ] Spotlight 区、Guide 区、Table Summary 区在不同后台页里视觉风格一致。
- [ ] 表格标题区、说明区和筛选区的留白节奏一致。
- [ ] 卡片、抽屉、弹窗的圆角和阴影没有出现明显混杂。
- [ ] 主按钮、默认按钮、危险按钮的层级区分清晰，没有样式串色。
- [ ] 英文 kicker、中文标题、正文说明之间的层级稳定，没有字号跳变。
- [ ] 长文本、长链接、长文件名都不会把卡片或表格撑坏。

## Dashboard 关键业务验收

### 项目与文件

- [ ] [frontend/src/views/admin/MatterList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterList.vue) 的筛选、查看、撤销操作可正常触发。
- [ ] [frontend/src/views/admin/FileManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/FileManagement.vue) 的批量选择、批量删除、过期清理按钮状态正确。
- [ ] 文件详情弹窗信息完整，长文件名仍可读。

### 通知治理

- [ ] [frontend/src/views/admin/NotificationHistory.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationHistory.vue) 的失败率、可重试数量和渠道统计显示正确。
- [ ] [frontend/src/views/admin/NotificationTemplateManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationTemplateManagement.vue) 的模板筛选、启停、编辑、删除流程正常。
- [ ] [frontend/src/views/admin/NotificationSettings.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationSettings.vue) 的邮件、短信、微信三套配置在启用/关闭切换时不会出现布局错乱。

### 系统与运维

- [ ] [frontend/src/views/admin/SystemConfig.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemConfig.vue) 的品牌、门户、系统配置和高级配置页签切换正常。
- [ ] [frontend/src/views/admin/SystemMaintenance.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemMaintenance.vue) 的系统状态、升级、备份、日志、命令行区域顺序清晰且不拥挤。
- [ ] [frontend/src/views/admin/ApiKeyManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/ApiKeyManagement.vue) 的新增、编辑、启停、删除和成功弹窗展示正常。
- [ ] [frontend/src/views/admin/AdminProfile.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminProfile.vue) 的密码修改表单、退出登录操作和资料卡布局正常。

### 验证与审计

- [ ] [frontend/src/views/admin/LetterVerificationList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/LetterVerificationList.vue) 的统计展开、筛选、详情抽屉和撤销操作正常。
- [ ] 抽屉中的时间、状态、备注等信息在窄屏下不会挤压重叠。

## Portal 关键业务验收

- [ ] [frontend/src/views/Portal.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/Portal.vue) 的访问入口、说明和品牌信息层级清晰。
- [ ] [frontend/src/views/MatterDetail.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/MatterDetail.vue) 的概览、进度、文件和动态区域顺序自然。
- [ ] [frontend/src/views/ClientFileList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientFileList.vue) 的文件卡片、搜索和下载入口可正常使用。
- [ ] [frontend/src/views/ClientNotifications.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/ClientNotifications.vue) 的通知列表与状态标签可正确展示。
- [ ] [frontend/src/views/verify/LetterVerify.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/verify/LetterVerify.vue) 的输入、验证反馈和结果展示正常。

## 移动端 / 窄屏验收

- [ ] 宽度 768px 左右时，后台页的 Spotlight、Guide、表格说明区都能自然折成单列。
- [ ] 宽度 390px 左右时，页面头部按钮区不会溢出屏幕。
- [ ] 筛选表单会自动变成单列，输入框和下拉框宽度正确。
- [ ] 抽屉、弹窗和 Descriptions 组件在移动端仍可阅读。
- [ ] 表格横向滚动时，固定列和内容背景没有出现明显断层。

## 回归建议顺序

1. 先验 [frontend/src/views/admin/AdminLayout.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/AdminLayout.vue)、[frontend/src/views/admin/MatterList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/MatterList.vue)、[frontend/src/views/admin/SystemConfig.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemConfig.vue)，确认后台骨架与主题稳定。
2. 再验 [frontend/src/views/admin/FileManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/FileManagement.vue)、[frontend/src/views/admin/NotificationHistory.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationHistory.vue)、[frontend/src/views/admin/LetterVerificationList.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/LetterVerificationList.vue)，确认高信息密度页没有回归。
3. 最后验 [frontend/src/views/admin/SystemMaintenance.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/SystemMaintenance.vue)、[frontend/src/views/admin/NotificationSettings.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/NotificationSettings.vue)、[frontend/src/views/admin/ApiKeyManagement.vue](/Users/apple/Documents/Project/law-firm-clients/frontend/src/views/admin/ApiKeyManagement.vue) 这类长页面和配置页。

## 自动验证记录

- 已执行：`pnpm typecheck`
- 已执行：`pnpm build`

这份清单默认用于人工视觉回归；如果后续补 E2E，可把本文件拆成 Playwright 用例分组。
