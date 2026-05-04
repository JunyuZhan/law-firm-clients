# Docker 部署指南 (Zero-Config)

随着架构升级为 **Zero-Config (零配置启动)** 和 **全栈单体镜像**，手动部署变得极其简单。您不再需要运行繁杂的部署脚本或手动拼凑配置。

## ⚠️ 生产环境部署前检查清单

> **重要提醒**：部署前请务必完成以下检查，否则存在安全风险！

- [ ] 确保目标服务器已安装 **Docker** 和 **Docker Compose (V2)**。
- [ ] 确保目标服务器能访问镜像仓库（如 `192.168.50.5:5050`）。
- [ ] 修改 `.env` 中的数据库密码（`POSTGRES_PASSWORD`）
- [ ] 配置律所系统回调地址或 API 密钥（如需要）
- [ ] 配置 HTTPS（生产环境强烈建议）

---

## 🚀 手动部署步骤 (只需三步)

### 1. 准备配置和 Compose 文件
在目标服务器的部署目录（如 `/opt/law-firm-clients`）下，准备 `docker-compose.yml` 文件。

你可以直接使用项目中的精简版配置：
```bash
# 在部署目录执行
cat << 'EOF' > docker-compose.yml
version: '3.8'

services:
  nginx:
    image: 192.168.50.5:5050/albert/law-firm-clients:v1.0.11 # 替换为最新版本
    container_name: law-firm-clients-nginx
    restart: always
    ports:
      - "8080:80"
    command: ["nginx", "-g", "daemon off;"]
    depends_on:
      - backend

  backend:
    image: 192.168.50.5:5050/albert/law-firm-clients:v1.0.11 # 替换为最新版本
    container_name: law-firm-clients-backend
    restart: always
    command: ["java", "-jar", "app.jar"]
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/client_service
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:15-alpine
    container_name: law-firm-clients-postgres
    restart: always
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-postgres123}
      - POSTGRES_DB=client_service
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: law-firm-clients-redis
    restart: always
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
EOF
```

### 2. 一键启动

```bash
docker compose up -d
```

### 3. 初始化系统

1. 打开浏览器访问 `http://<服务器IP>:8080`。
2. 系统会自动检测到无初始管理员账号，并引导您进入 **“初始化管理员密码”** 页面。
3. 设置密码后，即可登录使用。

---

## 💾 数据持久化

Docker Compose配置了以下数据卷：

- `postgres_data`: PostgreSQL数据
- `redis_data`: Redis数据

### 备份数据

```bash
# 备份PostgreSQL数据
docker compose exec postgres pg_dump -U postgres client_service > backup.sql
```

### 恢复数据

```bash
# 恢复PostgreSQL数据
docker compose exec -T postgres psql -U postgres client_service < backup.sql
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
