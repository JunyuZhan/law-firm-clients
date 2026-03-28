# Grafana 仪表板配置指南

## 概述

本文档说明如何配置Grafana仪表板来监控客户服务系统。

## 快速开始

### 1. 启动Grafana

使用Docker Compose启动Grafana：

```yaml
# docker-compose.yml 中添加
services:
  grafana:
    image: grafana/grafana:latest
    container_name: client-service-grafana
    ports:
      - "3001:3000"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./prometheus/grafana-dashboard.json:/etc/grafana/provisioning/dashboards/dashboard.json
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_INSTALL_PLUGINS=
    networks:
      - client-service-network

volumes:
  grafana-data:
```

或直接运行：

```bash
docker run -d \
  --name client-service-grafana \
  -p 3001:3000 \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  grafana/grafana:latest
```

### 2. 添加Prometheus数据源

1. 登录Grafana：`http://localhost:3001`（默认用户名/密码：admin/admin）
2. 进入 **Configuration** > **Data Sources**
3. 点击 **Add data source**
4. 选择 **Prometheus**
5. 配置：
   - **URL**: `http://prometheus:9090`（Docker网络内）或 `http://localhost:9090`（本地）
   - **Access**: Server (default)
6. 点击 **Save & Test**

### 3. 导入仪表板

#### 方法1：使用预定义模板

1. 进入 **Dashboards** > **Import**
2. 输入以下仪表板ID：
   - **Spring Boot 2.1 Statistics**: `11378`
   - **JVM (Micrometer)**: `4701`
3. 选择Prometheus数据源
4. 点击 **Import**

#### 方法2：导入自定义仪表板

1. 进入 **Dashboards** > **Import**
2. 点击 **Upload JSON file**
3. 选择 `grafana-dashboard.json` 文件
4. 选择Prometheus数据源
5. 点击 **Import**

#### 方法3：手动创建

参考 `grafana-dashboard.json` 中的面板配置，手动创建仪表板。

## 仪表板面板说明

### 1. 服务状态
- **指标**: `up{job="client-service-backend"}`
- **说明**: 显示服务是否在线（1=在线，0=离线）

### 2. API请求量 (QPS)
- **指标**: `rate(http_server_requests_seconds_count[5m])`
- **说明**: 显示各API接口的请求速率

### 3. API响应时间
- **指标**: `rate(api_*_seconds_sum[5m]) / rate(api_*_seconds_count[5m])`
- **说明**: 显示关键API接口的平均响应时间

### 4. 错误率
- **指标**: `rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(...)`
- **说明**: 显示4xx和5xx错误率

### 5. JVM内存使用
- **指标**: `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}`
- **说明**: 显示堆内存使用率

### 6. 数据库连接池
- **指标**: `hikari_connections_*`
- **说明**: 显示活跃、空闲、等待的连接数

### 7. GC暂停时间
- **指标**: `rate(jvm_gc_pause_seconds_sum[5m])`
- **说明**: 显示GC暂停时间

### 8. 线程数
- **指标**: `jvm_threads_live_threads`, `jvm_threads_daemon_threads`
- **说明**: 显示活跃线程和守护线程数

## 告警配置

### 1. 在Grafana中配置告警

1. 进入仪表板，选择要监控的面板
2. 点击面板标题 > **Edit**
3. 进入 **Alert** 标签页
4. 点击 **Create Alert**
5. 配置告警规则和通知渠道

### 2. 使用Prometheus Alertmanager

1. 启动Alertmanager（参考Prometheus文档）
2. 在 `prometheus.yml` 中配置 `alerting` 部分
3. 告警规则已在 `alerts.yml` 中定义

## 常用查询

### 查询接口平均响应时间

```promql
rate(api_matter_receive_seconds_sum[5m]) / rate(api_matter_receive_seconds_count[5m])
```

### 查询错误率

```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) * 100
```

### 查询内存使用率

```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

## 注意事项

1. **数据源连接**：确保Grafana能够访问Prometheus
2. **时间范围**：根据实际需求调整查询时间范围
3. **刷新间隔**：建议设置为30秒或1分钟
4. **告警通知**：配置邮件、Slack等通知渠道
5. **权限控制**：生产环境建议配置用户权限

## 相关文档

- [Grafana官方文档](https://grafana.com/docs/)
- [Prometheus查询语言](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Spring Boot Actuator指标](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics)
