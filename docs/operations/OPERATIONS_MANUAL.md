# 客户服务系统运维手册

> 版本：1.1.0  
> 最后更新：2026-03-29

## 📋 目录

- [系统概述](#系统概述)
- [环境要求](#环境要求)
- [部署指南](#部署指南)
- [日常运维](#日常运维)
- [监控和日志](#监控和日志)
- [故障排查](#故障排查)
- [备份和恢复](#备份和恢复)
- [性能优化](#性能优化)
- [安全加固](#安全加固)

---

## 🎯 系统概述

客户服务系统是一个独立的微服务，与律所管理系统对接，提供客户通知和门户访问功能。

### 核心功能

- 接收律所管理系统推送的项目数据
- 多渠道客户通知（短信、微信、邮件）
- 客户Web门户（门户首页、项目详情、文件中心、消息通知、个人中心、帮助中心）
- 客户文件上传和管理
- 访问日志记录

### 技术栈

- **后端**：Spring Boot 3.2.x, Java 21
- **数据库**：PostgreSQL 15.x
- **缓存**：Redis 7.x
- **前端**：Vue 3 + TypeScript + Vite

---

## 💻 环境要求

### 服务器配置

**最低配置**：
- CPU: 2核
- 内存: 4GB
- 磁盘: 20GB（不含文件存储）

**推荐配置**：
- CPU: 4核
- 内存: 8GB
- 磁盘: 100GB（含文件存储）

### 软件环境

- **操作系统**：Linux (CentOS 7+, Ubuntu 20.04+)
- **Java**：JDK 21+
- **数据库**：PostgreSQL 15+
- **缓存**：Redis 7+
- **容器**：Docker 20.10+, Docker Compose 2.0+

---

## 🚀 部署指南

### 方式一：Docker Compose部署（推荐）

#### 1. 准备环境

```bash
# 安装Docker和Docker Compose
curl -fsSL https://get.docker.com | sh
sudo systemctl start docker
sudo systemctl enable docker

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### 2. 配置环境变量

```bash
cd docker
cp .env.example .env
# 编辑 .env 文件，修改配置值
```

#### 3. 构建和启动

```bash
# 在项目根目录执行
cd /path/to/project
./deploy.sh start
```

#### 4. 验证部署

```bash
# 检查服务状态
./scripts/deploy.sh status

# 检查健康状态
curl http://localhost:8081/api/health

# 查看日志
./scripts/deploy.sh logs app
```

### 方式二：传统部署

#### 1. 准备数据库

```bash
# 创建数据库
createdb client_service

# 执行初始化脚本
psql -d client_service -f scripts/init-db/01-schema.sql
psql -d client_service -f scripts/init-db/02-test-data.sql
```

#### 2. 配置应用

编辑 `backend/src/main/resources/application.yml`：
- 数据库连接信息
- Redis连接信息
- 邮件服务器配置
- 文件存储路径

#### 3. 构建应用

```bash
cd backend
mvn clean package -DskipTests
```

#### 4. 启动应用

```bash
java -jar target/client-service-1.0.0.jar
```

---

## 🔧 日常运维

### 服务管理

#### 启动服务

```bash
# Docker方式
./scripts/deploy.sh start

# 传统方式
systemctl start client-service
# 或
nohup java -jar client-service.jar > app.log 2>&1 &
```

#### 停止服务

```bash
# Docker方式
./scripts/deploy.sh stop

# 传统方式
systemctl stop client-service
# 或
kill $(cat client-service.pid)
```

#### 重启服务

```bash
# Docker方式
./scripts/deploy.sh restart

# 传统方式
systemctl restart client-service
```

#### 查看状态

```bash
# Docker方式
./scripts/deploy.sh status

# 传统方式
systemctl status client-service
```

### 日志管理

#### 查看应用日志

```bash
# Docker方式
./scripts/deploy.sh logs app

# 传统方式
tail -f /var/log/client-service/application.log
```

#### 日志位置

- **Docker方式**：`docker-compose logs`
- **传统方式**：`/var/log/client-service/` 或 `logs/` 目录

#### 日志级别配置

编辑 `application.yml`：

```yaml
logging:
  level:
    root: INFO
    com.clientservice: DEBUG  # 开发环境可设置为DEBUG
```

### 数据库维护

#### 连接数据库

```bash
# Docker方式
docker-compose exec postgres psql -U postgres client_service

# 传统方式
psql -h localhost -U postgres -d client_service
```

#### 查看表大小

```sql
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

#### 清理过期数据

系统已配置定时任务自动清理：
- 过期项目：每天凌晨2点
- 访问日志：每周日凌晨3点（保留90天）

手动清理：

```sql
-- 清理过期访问日志（保留90天）
DELETE FROM access_log 
WHERE access_time < NOW() - INTERVAL '90 days';
```

---

## 📊 监控和日志

### 健康检查

系统提供以下健康检查端点：

- `GET /api/health` - 基础健康检查
- `GET /api/health/ready` - 就绪检查
- `GET /api/health/live` - 存活检查

### 监控指标

建议监控以下指标：

1. **应用指标**
   - JVM内存使用率
   - CPU使用率
   - 线程数
   - GC频率和时间

2. **业务指标**
   - API请求量（QPS）
   - API响应时间
   - 错误率
   - 文件上传成功率

3. **基础设施指标**
   - 数据库连接数
   - Redis连接数
   - 磁盘使用率
   - 网络流量

### 日志收集

建议使用以下工具收集日志：

- **ELK Stack**（Elasticsearch + Logstash + Kibana）
- **Loki + Grafana**
- **Fluentd**

### 告警规则

建议配置以下告警：

1. **服务不可用**
   - 健康检查失败超过3次
   - 响应时间超过5秒

2. **资源告警**
   - CPU使用率 > 80%
   - 内存使用率 > 85%
   - 磁盘使用率 > 90%

3. **业务告警**
   - API错误率 > 5%
   - 文件上传失败率 > 10%

---

## 🔍 故障排查

### 常见问题

#### 1. 应用无法启动

**症状**：服务启动失败

**排查步骤**：
1. 检查日志：`./scripts/deploy.sh logs app`
2. 检查端口是否被占用：`lsof -i :8081`
3. 检查数据库连接：确认数据库服务正常
4. 检查配置文件：确认 `application.yml` 配置正确

**解决方案**：
- 修改端口或停止占用端口的进程
- 检查数据库连接配置
- 检查环境变量配置

#### 2. 数据库连接失败

**症状**：应用启动后无法连接数据库

**排查步骤**：
1. 检查数据库服务状态：`docker-compose ps postgres`
2. 测试数据库连接：`psql -h localhost -U postgres -d client_service`
3. 检查数据库密码：确认 `.env` 中的 `POSTGRES_PASSWORD`

**解决方案**：
- 重启数据库服务
- 检查数据库密码配置
- 检查网络连接

#### 3. Redis连接失败

**症状**：缓存功能异常

**排查步骤**：
1. 检查Redis服务状态：`docker-compose ps redis`
2. 测试Redis连接：`redis-cli ping`
3. 检查Redis配置：确认 `application.yml` 中的Redis配置

**解决方案**：
- 重启Redis服务
- 检查Redis密码配置
- 检查网络连接

#### 4. 文件上传失败

**症状**：文件上传接口返回错误

**排查步骤**：
1. 检查文件存储目录权限：`ls -la /data/client-service/files`
2. 检查磁盘空间：`df -h`
3. 检查文件大小限制：确认 `application.yml` 中的 `client-service.file.max-size`

**解决方案**：
- 修复文件存储目录权限：`chmod 755 /data/client-service/files`
- 清理磁盘空间
- 调整文件大小限制

#### 5. 通知发送失败

**症状**：通知记录状态为FAILED

**排查步骤**：
1. 查看通知记录：`SELECT * FROM notification_record WHERE status = 'FAILED'`
2. 检查邮件服务器配置
3. 检查短信/微信SDK配置

**解决方案**：
- 检查邮件服务器配置和网络连接
- 检查短信/微信SDK配置和余额
- 查看详细错误日志

---

## 💾 备份和恢复

### 数据备份

#### 自动备份

系统提供备份脚本：

```bash
./scripts/backup.sh
```

备份内容包括：
- 数据库数据（SQL文件）
- 文件存储目录
- 配置文件（.env）

备份文件保存在 `backups/` 目录，格式：`client-service-backup-YYYYMMDD_HHMMSS.tar.gz`

#### 手动备份

**数据库备份**：

```bash
# Docker方式
docker-compose exec postgres pg_dump -U postgres client_service > backup.sql

# 传统方式
pg_dump -h localhost -U postgres client_service > backup.sql
```

**文件存储备份**：

```bash
# Docker方式
docker cp client-service-backend:/data/client-service/files ./backup/files

# 传统方式
tar -czf files-backup.tar.gz /data/client-service/files
```

### 数据恢复

#### 恢复数据库

```bash
# Docker方式
docker-compose exec -T postgres psql -U postgres client_service < backup.sql

# 传统方式
psql -h localhost -U postgres client_service < backup.sql
```

#### 恢复文件存储

```bash
# Docker方式
docker cp ./backup/files client-service-backend:/data/client-service/files

# 传统方式
tar -xzf files-backup.tar.gz -C /
```

### 备份策略

建议备份策略：

- **数据库**：每天全量备份，保留7天
- **文件存储**：每天增量备份，每周全量备份，保留30天
- **配置文件**：每次部署前备份

---

## ⚡ 性能优化

### 数据库优化

1. **索引优化**
   - 确保常用查询字段有索引
   - 定期分析慢查询

2. **连接池配置**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
   ```

3. **查询优化**
   - 避免全表扫描
   - 使用分页查询
   - 合理使用缓存

### Redis优化

1. **内存管理**
   - 设置最大内存限制
   - 配置淘汰策略

2. **连接池配置**
   ```yaml
   spring:
     data:
       redis:
         lettuce:
           pool:
             max-active: 8
             max-idle: 8
   ```

### 应用优化

1. **JVM参数**
   ```bash
   -Xms2g -Xmx4g
   -XX:+UseG1GC
   -XX:MaxGCPauseMillis=200
   ```

2. **异步处理**
   - 通知发送使用异步处理
   - 访问日志记录使用异步处理

3. **文件存储**
   - 大文件考虑使用对象存储（MinIO/OSS）
   - 定期清理临时文件

---

## 🔒 安全加固

### 1. API密钥管理

- 定期轮换API密钥
- 使用强密码策略
- 限制API密钥权限

### 2. 数据库安全

- 使用强密码
- 限制数据库访问IP
- 定期更新数据库版本

### 3. 网络安全

- 使用HTTPS
- 配置防火墙规则
- 限制管理端口访问

### 4. 文件安全

- 文件类型验证
- 文件大小限制
- 病毒扫描（可选）

### 5. 日志安全

- 避免记录敏感信息
- 日志文件权限控制
- 定期清理日志

---

## 📞 技术支持

如遇到问题，请：

1. 查看日志文件
2. 查阅本文档
3. 联系技术支持团队

---

**最后更新**：2026-02-02
