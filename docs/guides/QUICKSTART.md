# 快速启动指南

> 最后更新：2026-03-29

## 🚀 快速开始（5分钟）

### 方式一：使用Docker Compose（推荐）

```bash
# 1. 进入docker目录
cd docker

# 2. 配置环境变量（可选）
cp .env.example .env
# 编辑 .env 文件修改配置

# 3. 构建并启动
cd ../..
./deploy.sh start

# 4. 查看服务状态
./deploy.sh status

# 5. 查看日志
./deploy.sh logs app
```

### 方式二：本地开发环境

#### 1. 准备数据库

```bash
# 创建数据库
createdb client_service

# 初始化数据库
psql -d client_service -f scripts/init-db/01-schema.sql
psql -d client_service -f scripts/init-db/02-test-data.sql
```

#### 2. 启动Redis（可选）

```bash
# 使用Docker启动Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 或使用本地Redis
redis-server
```

#### 3. 配置应用

编辑 `backend/src/main/resources/application.yml`：
- 数据库连接信息
- Redis连接信息（如需要）
- 邮件服务器配置（如需要）

#### 4. 启动应用

```bash
cd backend
mvn spring-boot:run
```

#### 5. 验证

```bash
# 健康检查
curl http://localhost:8081/api/health

# 访问Swagger文档
open http://localhost:8081/swagger-ui.html
```

---

## 📝 测试API

### 1. 接收项目数据

```bash
curl -X POST http://localhost:8081/api/matter/receive \
  -H "Authorization: Bearer test-api-key-12345678901234567890" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 2001,
    "clientName": "测试客户",
    "matterData": {
      "matterId": 1001,
      "matterName": "测试项目",
      "phone": "13800138000",
      "email": "client@example.com"
    },
    "scopes": ["MATTER_INFO", "MATTER_PROGRESS"],
    "validDays": 30
  }'
```

### 2. 访问客户门户

```bash
curl "http://localhost:8081/portal/api/matter/CS1706860800000123456?token=test-access-token-12345678901234567890123456789012"
```

说明：
- 门户首页地址通常为 `http://localhost/portal`
- 当前前台页面包含：首页、我的项目、文件中心、消息通知、个人中心、帮助中心

### 3. 上传文件

```bash
curl -X POST http://localhost:8081/api/client/files/upload \
  -F "matterId=CS1706860800000123456" \
  -F "clientId=2001" \
  -F "token=test-access-token-12345678901234567890123456789012" \
  -F "file=@test.pdf"
```

---

## 🔧 常用命令

### Docker Compose命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose stop

# 重启服务
docker-compose restart app

# 查看日志
docker-compose logs -f app

# 查看状态
docker-compose ps

# 进入容器
docker-compose exec app sh
```

### 部署脚本命令

```bash
# 启动服务
./scripts/deploy.sh start

# 停止服务
./scripts/deploy.sh stop

# 重启服务
./scripts/deploy.sh restart

# 查看状态
./scripts/deploy.sh status

# 查看日志
./scripts/deploy.sh logs app

# 备份数据
./scripts/backup.sh
```

---

## 🐛 故障排查

### 应用无法启动

1. 检查端口是否被占用：`lsof -i :8081`
2. 检查数据库连接：查看应用日志
3. 检查环境变量：确认 `.env` 文件配置正确

### 数据库连接失败

1. 检查PostgreSQL是否运行：`docker-compose ps postgres`
2. 检查数据库密码：确认 `.env` 中的 `POSTGRES_PASSWORD`
3. 检查网络连接：确认服务在同一网络中

### 文件上传失败

1. 检查文件存储目录权限：`chmod 755 /data/client-service/files`
2. 检查文件大小：默认最大10MB
3. 查看应用日志：`docker-compose logs app`

---

## 📚 更多文档

- [API接口文档](./API.md)
- [系统设计文档](./docs/CLIENT_SERVICE_SYSTEM_DESIGN.md)
- [Docker部署指南](./docker/README.md)
- [开发任务清单](../development/TODO.md)

---

**快速启动完成！** 🎉
