# 体验 Demo（GitHub Pages）

前端体验 Demo：**无需真实后端**，使用 MSW 在浏览器内拦截 `/api` 请求返回 Mock 数据。
用于产品演示、客户体验与内部评审。

## 体验账号

- 任意 **用户名 + 密码** 均可登录（推荐 `demo` / `123456`）
- 验证码：任意 4–6 位字符（Demo 不校验）
- 登录后为「超级管理员」身份，可浏览全部管理后台菜单

## 本地运行（Demo 模式）

```bash
cd frontend
pnpm install
VITE_DEMO_MODE=true pnpm dev --mode demo
# 打开 http://localhost:3000
```

## 本地构建（Demo 模式）

```bash
cd frontend
VITE_DEMO_MODE=true pnpm build --mode demo
pnpm preview   # 预览 dist
```

产物 `frontend/dist` 可直接静态托管（Demo 使用 hash 路由，刷新不 404）。

## 部署到 GitHub Pages

推送到 `demo` 分支后，`.github/workflows/deploy-demo.yml` 会自动构建并发布到 Pages。

首次需在仓库 **Settings → Pages → Build and deployment → Source** 选择 **GitHub Actions**。

Demo 地址：`https://<owner>.github.io/law-firm-clients/`

## Mock 覆盖接口

- 认证：登录 / 当前用户 / 登出 / 改密 / 验证码 / 初始化检查
- 项目：列表 / 详情 / 撤销
- 门户：门户项目列表 / 详情 / 通知
- 通知：历史 / 统计 / 发送
- 文件：列表 / 上传 / 下载 / 预览 / 删除
- 统计：Dashboard / 文件 / 函件验证 / 访问
- 配置：系统配置 / 公开配置（门户 / 品牌 / ICP）
- 密钥：API 密钥列表 / 初始化 / 增删改
- 模板：通知模板列表 / 增删改
- 函件：真伪验证 / 验证记录列表
- 系统：运行信息 / 依赖状态

详细实现见 `frontend/src/mocks/`。
