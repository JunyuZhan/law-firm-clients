# 客户服务系统

独立的客户服务系统，与律所管理系统对接，提供客户通知和门户访问功能。

## 功能特性

### 核心功能
- ✅ 接收律所管理系统推送的项目数据
- ✅ 多渠道客户通知（短信、微信、邮件）
- ✅ 客户Web门户（查看项目进度、文件管理、文件预览）
- ✅ 客户文件上传和管理（支持本地存储和MinIO）
- ✅ 访问日志记录
- ✅ 通知重试机制（指数退避策略）

### 管理后台
- ✅ 项目列表和详情管理
- ✅ 通知记录查询和统计
- ✅ API密钥管理
- ✅ 系统配置管理
- ✅ 通知模板管理（短信/微信/邮件模板）

### 高级功能
- ✅ 多种存储方案（本地、MinIO、阿里云OSS）
- ✅ 文件病毒扫描（ClamAV集成）
- ✅ 性能优化（Redis缓存、数据库索引优化）
- ✅ 日志收集（ELK/Loki支持）

## 技术栈

**后端**：
- Spring Boot 3.2.x
- Java 21
- MyBatis-Plus 3.5.x
- PostgreSQL 15.x
- Redis 7.x

**前端**：
- Vue 3 + Vite
- Ant Design Vue 4.x
- TypeScript
- Pinia

## 项目结构

```
.
├── deploy.sh         # 一键部署脚本
├── backend/          # 后端项目 (Spring Boot)
├── frontend/         # 前端项目 (Vue 3)
├── docker/           # Docker 配置
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── nginx/
├── scripts/          # 脚本
│   ├── init-db/      # 数据库初始化
│   ├── migration/    # 数据库迁移
│   └── test/         # 测试脚本
└── docs/             # 文档
    ├── api/          # API 文档
    ├── guides/       # 使用指南
    ├── operations/   # 运维文档
    └── development/  # 开发文档
```

## 快速开始

### 🚀 一键部署（推荐）

```bash
# 1. 首次克隆
git clone https://github.com/JunyuZhan/law-firm-clients.git law-firm-clients
cd law-firm-clients

# 2. 首次部署（自动生成配置文件）
./deploy.sh --init

# 3. 后续启动
./deploy.sh

# 查看状态
./deploy.sh --status

# 查看日志
./deploy.sh --logs
```

### 升级更新

```bash
cd /path/to/project
git pull origin main
./deploy.sh --quick
```

部署完成后：
- 客户门户：http://localhost/
- 管理后台：http://localhost/admin

更多部署选项请运行 `./deploy.sh --help`

### 开发环境

#### 环境要求

- JDK 21+
- Node.js 20+
- PostgreSQL 15+
- Redis 7+

#### 数据库初始化

```bash
# 创建数据库
createdb client_service

# 执行初始化脚本
psql -d client_service -f scripts/init-db/01-schema.sql
```

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 运行测试

```bash
# 运行所有测试
./scripts/test.sh all

# 只运行单元测试
./scripts/test.sh unit

# 只运行集成测试
./scripts/test.sh integration
```

## 📚 文档

详见 [文档索引](./docs/README.md)

### API 文档
- [API 接口文档](./docs/api/API.md)
- [回调 API 规范](./docs/api/CALLBACK_API_SPEC.md)
- [API Key 集成指南](./docs/api/API_KEY_INTEGRATION_GUIDE.md)

### 使用指南
- [快速启动指南](./docs/guides/QUICKSTART.md)
- [用户手册](./docs/guides/USER_MANUAL.md)
- [系统配置指南](./docs/guides/SYSTEM_CONFIG_GUIDE.md)
- [对接集成指南](./docs/guides/CUSTOMER_SERVICE_INTEGRATION_GUIDE.md)

### 运维文档
- [运维手册](./docs/operations/OPERATIONS_MANUAL.md)
- [性能优化指南](./docs/operations/PERFORMANCE_OPTIMIZATION.md)
- [日志配置](./docs/operations/LOGGING_SETUP.md)
- [Docker 部署指南](./docker/README.md)

### 开发文档
- [开发指南](./docs/development/DEVELOPMENT_GUIDE.md)
- [实现总结](./docs/development/IMPLEMENTATION_SUMMARY.md)
