# 客户服务门户前端

客户服务系统的前端应用，提供门户首页、项目详情、文件中心、消息通知、个人中心、帮助中心以及管理后台功能。

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - 类型安全的JavaScript超集
- **Vite** - 下一代前端构建工具
- **Ant Design Vue** - 企业级UI组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - 状态管理库
- **Axios** - HTTP客户端

## 快速开始

### 安装依赖

```bash
npm install
# 或
pnpm install
# 或
yarn install
```

### 开发

```bash
npm run dev
```

访问 http://localhost:3000

### 构建

```bash
npm run build
```

构建产物在 `dist/` 目录

### 预览

```bash
npm run preview
```

## 项目结构

```
frontend/
├── src/
│   ├── api/           # API接口定义
│   ├── router/        # 路由配置
│   ├── stores/        # Pinia状态管理
│   │   ├── auth.ts
│   │   ├── appConfig.ts
│   │   └── portalVisitor.ts  # 前台访客资料缓存
│   ├── views/         # 页面组件
│   │   ├── admin/     # 管理后台页面
│   │   │   ├── AdminLayout.vue          # 管理后台布局
│   │   │   ├── MatterList.vue           # 项目列表
│   │   │   ├── MatterDetail.vue         # 项目详情
│   │   │   ├── NotificationHistory.vue  # 通知记录
│   │   │   ├── ApiKeyManagement.vue     # API密钥管理
│   │   │   ├── NotificationTemplateManagement.vue  # 通知模板管理
│   │   │   └── SystemConfig.vue         # 系统配置
│   │   ├── Portal.vue                   # 客户门户首页
│   │   ├── MatterDetail.vue             # 客户项目详情
│   │   ├── ClientFileList.vue           # 文件中心
│   │   ├── ClientNotifications.vue      # 消息通知
│   │   ├── ClientProfile.vue            # 个人中心
│   │   └── HelpCenter.vue               # 帮助中心
│   ├── App.vue        # 根组件
│   └── main.ts        # 入口文件
├── index.html         # HTML模板
├── vite.config.ts     # Vite配置
├── tsconfig.json      # TypeScript配置
├── package.json       # 项目配置
└── .env.example       # 环境变量示例
```

## 功能特性

### 客户门户功能
- ✅ 门户首页（访问链接输入）
- ✅ 项目详情查看
- ✅ 文件上传
- ✅ 文件下载
- ✅ 文件列表
- ✅ 文件删除
- ✅ 文件预览（图片、PDF、文本文件）
- ✅ 文件中心、消息通知、个人中心、帮助中心
- ✅ 移动端抽屉菜单和底部导航
- ✅ 前台访客资料缓存（最近访问项目）
- ✅ 响应式设计

### 管理后台功能
- ✅ 项目列表管理
- ✅ 项目详情查看（管理后台）
- ✅ 通知记录查询
- ✅ API密钥管理
- ✅ 通知模板管理
- ✅ 系统配置管理
- ✅ JWT 登录认证机制
- ✅ 统一的管理后台布局
- ✅ 路由守卫保护

## 环境变量

创建 `.env` 文件（可选）：

```env
# API基础URL（必填）
VITE_API_BASE_URL=http://localhost:8081

# 应用名称配置（可选，不配置则使用默认值）
VITE_APP_NAME=律师事务所客户服务系统  # 系统完整名称（用于页面标题、页脚版权信息等）
VITE_APP_SHORT_NAME=律所客服系统      # 系统简称（用于管理后台侧边栏等）
VITE_APP_NAME_EN=Professional Legal Service Portal  # 系统英文完整名称（可选）
VITE_APP_SHORT_NAME_EN=Professional Service         # 系统英文简称（可选）
VITE_PORTAL_TITLE=客户服务门户         # 客户服务门户标题（当前首页优先使用系统完整名称作为标题语义）
VITE_APP_SLOGAN=专业 · 安全 · 高效    # 系统标语（首页显示）
VITE_LOGO_URL=/logo.png              # Logo图片路径（完整展开时显示）
VITE_LOGO_COLLAPSED_URL=/logo.png    # Logo图片路径（侧边栏折叠时显示）
VITE_ICP_LICENSE=京ICP备12345678号-1  # ICP备案号（可选，不配置则不显示）
```

**说明**：
- 所有环境变量都是可选的，不配置时会使用默认值
- **版权信息会自动使用 `VITE_APP_NAME` 的值，无需单独配置**
- **ICP备案号可选配置，不配置则不显示备案信息**
- 修改环境变量后需要重启开发服务器才能生效
- 环境变量以 `VITE_` 开头才能在客户端代码中访问
- Logo图片应放置在 `public` 目录下，路径以 `/` 开头

## 开发说明

### API请求

所有API请求通过 `src/api/request.ts` 统一封装，自动处理token认证。

### 路由

**客户门户路由：**
- `/portal` - 门户首页（输入访问链接）
- `/matter/:id` - 项目详情页
- `/matters` - 我的项目
- `/files` - 文件中心
- `/notifications` - 消息通知
- `/profile` - 个人中心
- `/help` - 帮助中心

**管理后台路由：**
- `/admin/login` - 管理员登录
- `/admin/matters` - 项目列表
- `/admin/matters/:id` - 项目详情（管理后台）
- `/admin/notifications` - 通知记录
- `/admin/api-keys` - API密钥管理
- `/admin/notification-templates` - 通知模板管理
- `/admin/config` - 系统配置

### 状态管理

使用Pinia进行状态管理：
- `stores/auth.ts` - 管理后台认证状态
- `stores/appConfig.ts` - 系统品牌和门户配置
- `stores/portalVisitor.ts` - 前台访客资料缓存

### API认证

- **客户门户API**：通过URL参数传递token（`?token=xxx`）
- **管理后台API**：通过Authorization header传递Bearer Token（JWT）
  - 登录入口为 `/admin/login`
  - 路由守卫会自动检查后台登录态

## 部署

### 静态部署

构建后，将 `dist/` 目录部署到任何静态文件服务器（Nginx、Apache等）。

### Docker部署

可以配合后端Docker配置，将前端构建产物复制到Nginx容器中。

---

**最后更新**：2026-03-29
