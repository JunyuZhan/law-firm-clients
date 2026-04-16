# 律所客户服务系统：专业化 UI/UX 改造计划

## 目标

在不调整后端接口和核心业务逻辑的前提下，通过统一的设计 token、页面容器、移动端交互和文案字典，完成一轮低侵入但有明显感知提升的前端改造。

## 改造原则

- 不改后端接口
- 不重写权限或状态管理
- 优先改“全局收益最高”的部分
- 先收敛样式与文案，再逐步替换交互形态

## 本轮范围

- 全局 token 与 CSS Variables 收敛
- 登录页独立品牌入口改造
- 后台主布局与统一内容容器
- 门户列表/文件操作改为更接近移动端原生体验
- 核心高频文案抽离到常量文件

## 交付项

- `frontend/src/constants/uiTexts.ts`
- `frontend/src/components/PageContainer.vue`
- 登录页重构
- 后台菜单与高频按钮文案升级
- 门户项目卡片 + 底部弹层
- 文件中心 ActionSheet 交互

## 当前进度

- Phase 1 已完成：全局 token、登录页、后台主布局、`PageContainer`、门户项目列表与文件中心的第一轮视觉升级已经落地。
- Phase 2 已完成本轮核心范围：门户“消息通知 / 个人中心 / 帮助中心”已切换为统一的移动端卡片与 Vant 交互风格。
- Phase 2 已继续推进后台文案中心化：`MatterList`、`NotificationHistory` 及多张管理页的高频操作文案已接入 `uiTexts.ts`。
- 工程校验已通过：`pnpm -C frontend typecheck` 与 `pnpm -C frontend build` 均已通过。

## 下一步建议

- 继续扩大 `uiTexts.ts` 的覆盖范围，把高频提示语、空态文案和 placeholder 逐步中心化。
- 将剩余后台业务页逐步收敛到统一的 `PageContainer + Card` 结构，减少页面观感差异。
- 进一步用 Ant Design Vue 的组件级 token 替代零散样式覆盖，压缩样式复杂度。
- 按门户现有模式继续改造个人中心的下级流程页，保持移动端触感一致。

## 验收

- `pnpm typecheck` 通过
- 登录页、后台、门户视觉风格统一
- 门户关键路径具备移动端弹层交互
- 高频术语可以从常量文件集中维护
