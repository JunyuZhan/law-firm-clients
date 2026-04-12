# 生产环境数据库初始化指南

## 自动初始化机制

PostgreSQL Docker容器会在**首次启动时**（数据目录为空）自动执行 `/docker-entrypoint-initdb.d` 目录下的SQL脚本。

### 执行顺序

脚本按照**文件名排序**执行：
1. `01-schema.sql` - 创建表结构
2. `02-init-data.sql` - 插入初始化数据（生产环境）

当前仓库仍处于开发阶段，表结构以 `01-schema.sql` 为唯一基线来源，不依赖额外增量迁移脚本。

### 重要限制

⚠️ **自动初始化仅在首次启动时执行**：
- ✅ 如果数据目录为空，脚本会自动执行
- ❌ 如果数据目录已存在（数据卷已初始化），脚本**不会再次执行**
- 🔒 这是PostgreSQL官方镜像的保护机制，防止意外覆盖现有数据

## 生产环境部署步骤

### 1. 首次部署（自动初始化）

```bash
# 1. 确保数据卷为空（首次部署）
cd docker

# 2. 检查是否有现有数据卷
docker volume ls | grep client-postgres

# 3. 如果有，删除数据卷（谨慎操作！）
docker compose down -v

# 4. 启动PostgreSQL容器（自动执行初始化脚本）
docker compose up -d postgres

# 5. 等待初始化完成（查看日志）
docker compose logs -f postgres

# 6. 验证初始化
docker exec -it client-postgres psql -U postgres -d client_service -c "\dt"
docker exec -it client-postgres psql -U postgres -d client_service -c "SELECT username, real_name FROM admin_user;"
```

### 2. 确保使用正确的初始化脚本

**生产环境**应该使用 `02-init-data.sql`（不包含测试数据）。

**方法一：临时重命名测试脚本（推荐）**

```bash
cd scripts/init-db

# 重命名测试脚本，避免自动执行
mv 02-test-data.sql 02-test-data.sql.bak

# 确保生产环境脚本存在
ls -la 02-init-data.sql
```

**方法二：使用符号链接**

```bash
cd scripts/init-db

# 创建符号链接，指向生产环境脚本
ln -sf 02-init-data.sql 02-init-data-active.sql

# 在 docker-compose.yml 中只挂载需要的脚本
# volumes:
#   - ../scripts/init-db/01-schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
#   - ../scripts/init-db/02-init-data.sql:/docker-entrypoint-initdb.d/02-init-data.sql
```

### 3. 验证初始化结果

```bash
# 检查表结构
docker exec -it client-postgres psql -U postgres -d client_service -c "\dt"

# 检查管理员用户（应该只有 admin，没有 test）
docker exec -it client-postgres psql -U postgres -d client_service -c "SELECT username, real_name, enabled FROM admin_user;"

# 检查API密钥（生产环境应该为空，需要通过管理后台创建）
docker exec -it client-postgres psql -U postgres -d client_service -c "SELECT key_name, api_key FROM api_key;"

# 检查系统配置
docker exec -it client-postgres psql -U postgres -d client_service -c "SELECT config_key, config_value FROM sys_config LIMIT 10;"
```

## 默认账号信息

### 管理员账号

- **用户名**：`admin`
- **密码**：`admin123`
- **真实姓名**：系统管理员

⚠️ **重要**：首次登录后，请立即修改默认密码！

### API密钥

生产环境**不包含**默认API密钥，需要通过管理后台创建：
1. 使用管理员账号登录 `/admin`
2. 进入「API密钥管理」页面
3. 创建新的API密钥用于系统集成

## 常见问题

### Q: 如果数据卷已存在，如何重新初始化？

**方法一：删除数据卷重新初始化（谨慎！会丢失所有数据）**

```bash
docker compose down -v
docker compose up -d postgres
```

**方法二：手动执行初始化脚本**

```bash
cd scripts/init-db
./init-database.sh --drop
```

### Q: 如何确认初始化脚本已执行？

检查PostgreSQL容器日志：

```bash
docker compose logs postgres | grep -i "init\|executing\|running"
```

应该能看到类似输出：
```
/docker-entrypoint-initdb.d/01-schema.sql
/docker-entrypoint-initdb.d/02-init-data.sql
```

### Q: 生产环境可以使用测试数据脚本吗？

**不推荐**。`02-test-data.sql` 包含：
- 测试API密钥（不安全）
- 测试管理员账号（test/admin123）
- 测试项目数据

生产环境应该使用 `02-init-data.sql`，只包含系统运行必需的基础数据。

## 安全建议

1. ✅ **首次部署后立即修改默认密码**
2. ✅ **删除或重命名测试数据脚本**（避免误执行）
3. ✅ **定期备份数据库**
4. ✅ **使用强密码策略**
5. ✅ **限制数据库访问权限**
