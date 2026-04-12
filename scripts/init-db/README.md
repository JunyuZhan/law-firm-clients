# 数据库初始化脚本说明

## 脚本列表

1. **01-schema.sql** - 数据库表结构
   - 创建所有核心表与索引
   - 开发期基线结构统一收敛到此文件
   - 不再依赖单独的增量迁移脚本

2. **02-test-data.sql** - 测试数据（可选）
   - 插入测试API密钥
   - 插入测试项目数据
   - 更新测试配置

## 使用方法

### 1. 创建数据库

```bash
createdb client_service
```

### 2. 执行初始化脚本

```bash
# 执行表结构脚本
psql -d client_service -f scripts/init-db/01-schema.sql

# 执行测试数据脚本（可选）
psql -d client_service -f scripts/init-db/02-test-data.sql
```

### 3. 验证

```bash
# 检查表是否创建成功
psql -d client_service -c "\dt"

# 检查测试数据
psql -d client_service -c "SELECT * FROM api_key;"
psql -d client_service -c "SELECT id, client_name, status FROM client_matter;"
```

## 测试API密钥

- **API密钥**：`test-api-key-12345678901234567890`
- **律所名称**：测试律师事务所

## 测试项目访问

- **项目ID**：`CS1706860800000123456`
- **访问令牌**：`test-access-token-12345678901234567890123456789012`
- **访问链接**：`http://localhost:8081/portal/api/matter/CS1706860800000123456?token=test-access-token-12345678901234567890123456789012`
