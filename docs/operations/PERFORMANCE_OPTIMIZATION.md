# 性能优化文档

## 概述

本文档描述了客户服务系统的性能优化措施，包括缓存策略和数据库优化建议。

## 缓存策略

### 1. Redis缓存配置

系统使用Redis作为主要缓存存储，配置了以下缓存区域：

- **apiKey**: API密钥缓存，过期时间1小时
- **sysConfig**: 系统配置缓存，过期时间2小时
- **matter**: 项目数据缓存，过期时间10分钟
- **notificationTemplate**: 通知模板缓存，过期时间1小时

### 2. 缓存实现

#### API密钥服务 (`ApiKeyService`)

- `validateApiKey()`: 缓存API密钥验证结果，减少数据库查询
- `updateApiKey()`: 更新时清除相关缓存
- `deleteApiKey()`: 删除时清除相关缓存

#### 系统配置服务 (`SysConfigService`)

- `getConfigValue()`: 缓存配置值，避免频繁查询数据库
- `saveConfig()`: 保存时清除对应配置的缓存
- `updateConfig()`: 更新时清除所有配置缓存
- `deleteConfig()`: 删除时清除所有配置缓存

#### 项目服务 (`MatterService`)

- `getMatterByToken()`: 缓存项目数据（按token），过期时间10分钟
- `getMatterById()`: 缓存项目数据（按ID），过期时间10分钟
- `revokeMatter()`: 撤销时清除对应项目的缓存

#### 通知模板服务 (`NotificationTemplateService`)

- `getTemplateList()`: 缓存模板列表查询结果
- `getTemplateById()`: 缓存单个模板数据
- `getEnabledTemplatesByTypeAndProvider()`: 缓存启用的模板查询（通知服务常用）
- `createTemplate()`: 创建时清除所有模板缓存
- `updateTemplate()`: 更新时清除所有模板缓存
- `deleteTemplate()`: 删除时清除所有模板缓存

### 3. 缓存失效策略

- **写操作清除缓存**: 所有更新、创建、删除操作都会清除相关缓存
- **过期时间**: 根据数据更新频率设置不同的过期时间
- **事务支持**: 缓存管理器支持事务，确保数据一致性

## 数据库优化

### 1. 索引建议

#### client_matter 表

```sql
-- 访问令牌索引（用于快速查找项目）
CREATE INDEX IF NOT EXISTS idx_client_matter_access_token ON client_matter(access_token) WHERE deleted = 0;

-- 律所项目ID索引（用于检查重复）
CREATE INDEX IF NOT EXISTS idx_client_matter_law_firm_matter_id ON client_matter(law_firm_matter_id) WHERE deleted = 0;

-- 状态索引（用于查询和定时任务）
CREATE INDEX IF NOT EXISTS idx_client_matter_status ON client_matter(status) WHERE deleted = 0;

-- 过期时间索引（用于定时任务）
CREATE INDEX IF NOT EXISTS idx_client_matter_expires_at ON client_matter(expires_at) WHERE deleted = 0 AND status = 'ACTIVE';
```

#### api_key 表

```sql
-- API密钥索引（用于验证）
CREATE INDEX IF NOT EXISTS idx_api_key_api_key ON api_key(api_key) WHERE deleted = 0 AND enabled = true;

-- 律所名称索引（用于查询）
CREATE INDEX IF NOT EXISTS idx_api_key_law_firm_name ON api_key(law_firm_name) WHERE deleted = 0;
```

#### sys_config 表

```sql
-- 配置键索引（用于快速查找配置）
CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_config_config_key ON sys_config(config_key) WHERE deleted = 0;
```

#### notification_template 表

```sql
-- 模板类型和提供商索引（用于查询启用的模板）
CREATE INDEX IF NOT EXISTS idx_notification_template_type_provider ON notification_template(template_type, provider, enabled) WHERE deleted = 0 AND enabled = true;

-- 模板代码和提供商索引（用于检查重复）
CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_template_code_provider ON notification_template(template_code, provider) WHERE deleted = 0 AND template_code IS NOT NULL;
```

#### client_file 表

```sql
-- 项目ID索引（用于查询项目文件列表）
CREATE INDEX IF NOT EXISTS idx_client_file_matter_id ON client_file(matter_id) WHERE deleted = 0;

-- 状态索引（用于查询）
CREATE INDEX IF NOT EXISTS idx_client_file_status ON client_file(status) WHERE deleted = 0;
```

#### notification_record 表

```sql
-- 项目ID索引（用于查询通知记录）
CREATE INDEX IF NOT EXISTS idx_notification_record_matter_id ON notification_record(matter_id);

-- 状态索引（用于查询和重试）
CREATE INDEX IF NOT EXISTS idx_notification_record_status ON notification_record(status) WHERE status IN ('PENDING', 'FAILED');

-- 创建时间索引（用于查询和清理）
CREATE INDEX IF NOT EXISTS idx_notification_record_created_at ON notification_record(created_at);
```

### 2. 查询优化

#### 使用分页查询

对于列表查询，始终使用 `LIMIT` 限制返回数量：

```java
queryWrapper.last("LIMIT " + limitValue);
```

#### 避免N+1查询

使用 MyBatis-Plus 的批量查询方法，避免循环查询数据库。

#### 使用连接池

配置了 HikariCP 连接池：
- `maximum-pool-size: 20`
- `minimum-idle: 5`
- `connection-timeout: 30000`

### 3. 数据库配置优化

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # 最大连接数
      minimum-idle: 5             # 最小空闲连接数
      connection-timeout: 30000   # 连接超时时间（毫秒）
      idle-timeout: 600000        # 空闲连接超时时间（毫秒）
      max-lifetime: 1800000       # 连接最大生存时间（毫秒）
```

## 性能监控

### 1. Prometheus指标

系统已集成 Prometheus 监控，可以监控：
- API响应时间
- 缓存命中率
- 数据库查询时间
- 系统资源使用情况

### 2. 日志记录

关键操作都记录了日志，包括：
- 缓存命中/未命中
- 数据库查询时间
- 性能瓶颈点

## 最佳实践

1. **缓存使用原则**
   - 只缓存读多写少的数据
   - 设置合理的过期时间
   - 及时清除相关缓存

2. **数据库查询优化**
   - 使用索引加速查询
   - 避免全表扫描
   - 使用分页查询

3. **连接池配置**
   - 根据实际负载调整连接池大小
   - 监控连接池使用情况
   - 避免连接泄漏

4. **监控和调优**
   - 定期检查慢查询日志
   - 监控缓存命中率
   - 根据实际情况调整缓存策略

## 未来优化方向

1. **二级缓存**
   - 考虑使用 MyBatis-Plus 的二级缓存
   - 减少数据库查询

2. **异步处理**
   - 更多操作改为异步处理
   - 提高系统响应速度

3. **读写分离**
   - 配置数据库主从复制
   - 读操作使用从库

4. **分库分表**
   - 当数据量增大时考虑分库分表
   - 提高数据库性能
