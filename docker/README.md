# Docker 部署指南

## ⚠️ 生产环境部署前检查清单

> **重要提醒**：部署前请务必完成以下检查，否则存在安全风险！

- [ ] 修改 `.env` 中的数据库密码（`POSTGRES_PASSWORD`）
- [ ] 配置强 JWT 密钥（`CLIENT_SERVICE_JWT_SECRET`，建议 64 位以上随机字符串）
- [ ] 配置正确的系统访问地址（`BASE_URL`）
- [ ] 部署后立即登录修改默认管理员密码（默认：`admin/admin123`）
- [ ] 如使用 MinIO，修改默认密钥（`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`）
- [ ] 配置律所系统回调地址或 API 密钥（如需要）
- [ ] 配置 HTTPS（生产环境强烈建议）
- [ ] 关闭 Swagger UI（`SWAGGER_ENABLED=false`）

---

## 📋 目录

- [生产环境部署前检查清单](#️-生产环境部署前检查清单)
- [快速开始](#快速开始)
- [环境变量配置](#环境变量配置)
- [构建和运行](#构建和运行)
- [数据持久化](#数据持久化)
- [健康检查](#健康检查)
- [常见问题](#常见问题)

---

## 🚀 快速开始

### 1. 准备环境变量

```bash
cd docker
cp .env.example .env
# 编辑 .env 文件，修改配置值
```

### 2. 构建应用

```bash
# 在项目根目录执行
cd backend
mvn clean package -DskipTests
```

### 3. 启动服务

```bash
cd docker
docker-compose up -d
```

### 4. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看应用日志
docker-compose logs -f app

# 查看数据库日志
docker-compose logs -f postgres
```

### 5. 停止服务

```bash
docker-compose down
```

---

## ⚙️ 环境变量配置

主要环境变量说明：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `POSTGRES_PASSWORD` | PostgreSQL密码 | `postgres` |
| `BASE_URL` | 应用基础URL | `http://localhost:8081` |
| `SPRING_PROFILES_ACTIVE` | Spring Profile | `prod` |
| `CLIENT_SERVICE_FILE_STORAGE_LOCAL_PATH` | 文件存储路径 | `/data/client-service/files` |

详细配置请参考 `.env.example` 文件。

---

## 🔨 构建和运行

### 构建Docker镜像

```bash
# 在项目根目录执行
docker build -f docker/Dockerfile -t client-service:latest .
```

### 使用docker-compose

```bash
# 启动所有服务
docker-compose up -d

# 启动特定服务
docker-compose up -d postgres redis

# 重新构建并启动
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 停止服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器和卷（注意：会删除数据）
docker-compose down -v
```

---

## 💾 数据持久化

Docker Compose配置了以下数据卷：

- `postgres_data`: PostgreSQL数据
- `redis_data`: Redis数据
- `file_storage`: 应用文件存储
- `app_logs`: 应用日志

数据卷位置：
- Linux: `/var/lib/docker/volumes/`
- macOS/Windows: Docker Desktop管理

### 备份数据

```bash
# 备份PostgreSQL数据
docker-compose exec postgres pg_dump -U postgres client_service > backup.sql

# 备份文件存储
docker cp client-backend:/data/client-service/files ./backup/files
```

### 恢复数据

```bash
# 恢复PostgreSQL数据
docker-compose exec -T postgres psql -U postgres client_service < backup.sql
```

---

## 🏥 健康检查

所有服务都配置了健康检查：

- **PostgreSQL**: `pg_isready`
- **Redis**: `redis-cli ping`
- **应用**: `GET /api/health`

查看健康状态：

```bash
docker-compose ps
```

---

## 🔧 常见问题

### 1. 端口冲突

如果端口被占用，修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "8082:8081"  # 改为8082
```

### 2. 数据库连接失败

检查：
- PostgreSQL容器是否正常运行：`docker-compose ps`
- 数据库密码是否正确：检查 `.env` 文件
- 网络连接：确保服务在同一网络中

### 3. 文件权限问题

```bash
# 修复文件存储目录权限
docker-compose exec app chmod -R 755 /data/client-service/files
```

### 4. 查看应用日志

```bash
# 实时查看日志
docker-compose logs -f app

# 查看最近100行日志
docker-compose logs --tail=100 app
```

### 5. 进入容器调试

```bash
# 进入应用容器
docker-compose exec app sh

# 进入数据库容器
docker-compose exec postgres psql -U postgres client_service
```

---

## 📝 生产环境建议

1. **修改默认密码**：修改 `.env` 中的 `POSTGRES_PASSWORD`
2. **使用HTTPS**：配置SSL证书，修改 `BASE_URL` 为HTTPS地址
3. **限制资源**：在 `docker-compose.yml` 中添加资源限制
4. **配置日志轮转**：配置日志收集和轮转
5. **监控告警**：配置Prometheus和Grafana监控

---

**最后更新**：2026-02-02
