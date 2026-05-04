# 🚀 手动部署指南 (Manual Deployment)

随着架构升级为 **Zero-Config (零配置启动)** 和 **全栈单体镜像**，手动部署变得极其简单。您不再需要运行繁杂的 `deploy.sh` 脚本或手动配置环境。

## 部署前提
- 目标服务器已安装 **Docker** 和 **Docker Compose (V2)**。
- 目标服务器能访问镜像仓库（如 NAS 上的 `192.168.50.5:5050`）。

## 部署步骤

### 1. 准备配置文件
在目标服务器的部署目录（如 `/opt/law-firm-clients`）下，创建一个 `docker-compose.yml` 文件。

```yaml
version: '3.8'

services:
  # =====================================================
  # 1. 前端服务 (Nginx)
  # =====================================================
  nginx:
    image: 192.168.50.5:5050/albert/law-firm-clients:v1.0.11 # 替换为最新版本
    container_name: law-firm-clients-nginx
    restart: always
    ports:
      - "8080:80"
    command: ["nginx", "-g", "daemon off;"]
    depends_on:
      - backend

  # =====================================================
  # 2. 后端服务 (Spring Boot)
  # =====================================================
  backend:
    image: 192.168.50.5:5050/albert/law-firm-clients:v1.0.11 # 替换为最新版本
    container_name: law-firm-clients-backend
    restart: always
    command: ["java", "-jar", "app.jar"]
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/client_service
      # - POSTGRES_PASSWORD=您的数据库密码 # 可选，默认为 postgres123
      # - CLIENT_SERVICE_JWT_SECRET=您的长JWT密钥 # 可选，系统会自动生成默认的安全密钥
    depends_on:
      - postgres
      - redis

  # =====================================================
  # 3. 数据库 (PostgreSQL)
  # =====================================================
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

  # =====================================================
  # 4. 缓存 (Redis)
  # =====================================================
  redis:
    image: redis:7-alpine
    container_name: law-firm-clients-redis
    restart: always
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### 2. 一键启动
在 `docker-compose.yml` 所在目录执行：

```bash
docker compose up -d
```

### 3. 初始化系统
1. 打开浏览器访问 `http://<服务器IP>:8080`。
2. 系统会自动检测到无初始管理员账号，并引导您进入 **“初始化管理员密码”** 页面。
3. 设置密码后，即可登录使用。

---

> **💡 为什么删除了旧的 `deploy.sh`？**
> 旧脚本包含大量的环境拼接、本地代码编译、密码随机生成和目录挂载逻辑。现在，所有的环境依赖（如初始化 SQL）都已经打入 Java 镜像，密码设置转移到了前端 UI。部署流程彻底回归 Docker 标准，只需要一个精简的 Compose 文件即可。
