# Playwright 验收用例草案

## 说明

当前仓库尚未接入 Playwright，本文件先提供一版可直接转换为 E2E 用例的草案。

目标：

- 把 [docs/ui-rebuild-acceptance-checklist.md](/Users/apple/Documents/Project/law-firm-clients/docs/ui-rebuild-acceptance-checklist.md) 拆成可执行场景
- 先覆盖高价值页面和高风险交互
- 避免在没有稳定测试夹具前，直接写出大量依赖真实数据的脆弱脚本

建议后续目录：

- `frontend/e2e/admin-shell.spec.ts`
- `frontend/e2e/matter-ops.spec.ts`
- `frontend/e2e/file-ops.spec.ts`
- `frontend/e2e/notification-ops.spec.ts`
- `frontend/e2e/system-ops.spec.ts`
- `frontend/e2e/portal-flow.spec.ts`

## 前置假设

- 前端运行地址：`http://localhost:5173`
- 后端与测试数据可用
- 已准备一个可登录的管理员账号
- 已准备至少一条项目、一条文件、一条通知、一条验证记录和一组系统配置数据

## 推荐夹具

- `adminUser`
  - 有后台完整权限
- `matterSeed`
  - 至少包含 ACTIVE / EXPIRED / REVOKED 三类项目
- `fileSeed`
  - 至少包含 ACTIVE / DELETED 两类文件
- `notificationSeed`
  - 至少包含 SUCCESS / FAILED / PENDING 三类记录
- `verificationSeed`
  - 至少包含 ACTIVE、已过期、REVOKED 三类函件

## 公共步骤草案

### `loginAsAdmin`

1. 打开 `/admin/login`
2. 输入管理员账号密码
3. 点击登录
4. 断言进入后台首页或默认工作台

### `assertDashboardHeader`

1. 断言页面存在主标题
2. 断言页面存在副标题或说明文案
3. 断言右上角主操作按钮存在

### `assertResponsiveSingleColumn`

1. 设置视口为 `390 x 844`
2. 断言顶部按钮区未溢出
3. 断言筛选区改为单列
4. 断言主要卡片区为单列堆叠

## 用例分组

### 1. 后台骨架与主题

#### `admin-shell.spec.ts`

`describe('Admin shell')`

- `it('登录后展示统一后台骨架')`
  - 登录后台
  - 断言左侧导航存在
  - 断言顶部区域存在当前页面标题
  - 断言页面容器未出现明显横向溢出

- `it('关键后台页使用统一标题与说明区')`
  - 依次进入：
    - `/admin/matters`
    - `/admin/files`
    - `/admin/config`
    - `/admin/maintenance`
  - 断言每页都存在标题、说明、操作区

- `it('后台骨架在移动端仍可浏览')`
  - 登录后台
  - 切换到移动端视口
  - 断言导航、标题区和内容区仍可访问

### 2. 项目治理

#### `matter-ops.spec.ts`

`describe('Matter operations')`

- `it('项目列表可按状态筛选并展示统计卡')`
  - 打开 `/admin/matters`
  - 断言顶部统计卡显示
  - 选择状态筛选
  - 点击查询
  - 断言表格仅展示对应状态项目

- `it('项目列表可进入项目详情')`
  - 打开 `/admin/matters`
  - 点击第一条记录的“查看”
  - 断言进入 `/admin/matters/:id`

- `it('有效项目可执行撤销')`
  - 打开 `/admin/matters`
  - 对 ACTIVE 项目点击“撤销”
  - 确认弹窗
  - 断言成功提示出现

### 3. 文件治理

#### `file-ops.spec.ts`

`describe('File operations')`

- `it('文件管理页展示统计卡、spotlight 和表格摘要')`
  - 打开 `/admin/files`
  - 断言统计卡可见
  - 断言 spotlight 区可见
  - 断言表格摘要显示当前结果数与已选择数

- `it('文件管理页支持筛选和详情查看')`
  - 输入关键字或状态
  - 触发搜索
  - 点击“详情”
  - 断言详情弹窗字段完整

- `it('文件管理页支持批量选择')`
  - 勾选至少两条记录
  - 断言批量删除按钮变为可点击

### 4. 通知治理

#### `notification-ops.spec.ts`

`describe('Notification operations')`

- `it('通知记录页展示失败率、可重试数量和渠道分布')`
  - 打开 `/admin/notifications/history`
  - 断言顶部统计卡和 spotlight 区可见
  - 断言短信、微信、邮件渠道卡片可见

- `it('通知记录页支持按状态和渠道筛选')`
  - 选择通知类型和状态
  - 查询
  - 断言表格结果更新

- `it('失败记录可触发重试')`
  - 找到 FAILED 且允许重试的记录
  - 点击“重试”
  - 断言成功提示或列表刷新

- `it('通知模板页支持启停、编辑和删除')`
  - 打开 `/admin/templates`
  - 断言 guide 区和统计卡可见
  - 切换启停状态
  - 打开编辑弹窗
  - 关闭后执行删除

- `it('通知配置页支持邮件、短信、微信三通道配置')`
  - 打开 `/admin/settings/notifications`
  - 分别切换邮件、短信、微信开关
  - 断言各自表单区域展开/收起正常

### 5. 配置与系统治理

#### `system-ops.spec.ts`

`describe('System operations')`

- `it('系统配置页可切换品牌、门户、系统和高级配置页签')`
  - 打开 `/admin/config`
  - 依次点击四个页签
  - 断言对应区域展示

- `it('系统配置页的品牌与门户字段可编辑')`
  - 修改一个输入框
  - 触发 blur 保存
  - 断言成功提示或保存状态变化

- `it('API Key 管理页支持新增与编辑')`
  - 打开 `/admin/api-keys`
  - 点击“新增密钥”
  - 填写表单并提交
  - 断言成功弹窗展示 Key / Secret

- `it('管理员个人中心支持密码修改流程')`
  - 打开 `/admin/profile`
  - 填写密码修改表单
  - 提交
  - 断言成功提示或跳转重新登录

- `it('系统维护页展示系统状态、升级、备份和审计区域')`
  - 打开 `/admin/maintenance`
  - 断言运维概览、guide 区、升级区、备份区、日志区存在

### 6. 验证与审计

#### `verification-ops.spec.ts`

`describe('Verification operations')`

- `it('验证记录页支持统计展开与收起')`
  - 打开 `/admin/verifications`
  - 点击“查看统计”
  - 断言统计卡出现
  - 再次点击并断言收起

- `it('验证记录页支持筛选和详情抽屉')`
  - 输入关键词或状态
  - 点击查询
  - 点击“详情”
  - 断言抽屉展开，字段完整

- `it('有效记录支持撤销')`
  - 对 ACTIVE 且未过期记录点击“撤销”
  - 确认弹窗
  - 断言成功提示出现

### 7. Portal 关键流

#### `portal-flow.spec.ts`

`describe('Portal flows')`

- `it('Portal 首页展示品牌与访问入口')`
  - 打开 `/portal`
  - 断言品牌区、入口区、说明区存在

- `it('项目详情页展示概览、进度、文件和动态')`
  - 打开一个有效项目详情链接
  - 断言四类内容区域存在

- `it('文件中心支持查看与下载入口')`
  - 打开 Portal 文件中心
  - 断言文件列表可见
  - 断言下载按钮存在

- `it('消息中心展示通知列表与状态')`
  - 打开 Portal 消息中心
  - 断言通知卡片或列表可见

- `it('函件验证页展示输入、提交和结果反馈')`
  - 打开验证页
  - 输入测试编号
  - 提交后断言结果区出现

## 移动端专项

建议为以下页面各补一条移动端 smoke：

- `/admin/matters`
- `/admin/files`
- `/admin/config`
- `/admin/maintenance`
- `/admin/api-keys`
- `/admin/verifications`
- `/portal`

每条 smoke 至少断言：

- 页面头部按钮未溢出
- spotlight / guide 区为单列
- 筛选区控件宽度正确
- 表格可横向滚动但不破版

## 实施建议

1. 先补登录夹具和基础导航 smoke。
2. 再补不依赖复杂写操作的只读验收用例。
3. 最后补新增、编辑、删除、撤销等写操作场景。
4. 对强依赖数据状态的用例，优先配固定 seed 或 API mock。

## 当前不建议立即自动化的部分

- 系统升级真实操作
- 备份恢复真实执行
- 命令行运维指令
- 依赖第三方通道的真实短信、邮件、微信发送

这些更适合保留为人工验收或受控沙箱验收。
