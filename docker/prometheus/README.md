# Prometheus 监控配置

本文档说明如何配置和使用 Prometheus 监控客户服务系统。

## 📊 监控指标

### 应用指标

- **HTTP请求指标**：
  - `http_server_requests_seconds`：HTTP请求耗时
  - `http_server_requests_seconds_count`：HTTP请求总数
  - `http_server_requests_seconds_sum`：HTTP请求总耗时

- **自定义业务指标**（通过 `@Timed` 注解）：
  - `api.matter.receive`：接收项目数据接口耗时
  - `api.matter.getById`：获取项目详情接口耗时
  - `api.portal.matter.detail`：客户门户获取项目详情接口耗时
  - `api.file.upload`：文件上传接口耗时
  - `api.file.list`：获取文件列表接口耗时
  - `api.file.download`：文件下载接口耗时

- **JVM指标**：
  - `jvm_memory_used_bytes`：JVM内存使用
  - `jvm_memory_max_bytes`：JVM最大内存
  - `jvm_gc_pause_seconds`：GC暂停时间
  - `jvm_threads_live`：活跃线程数

- **数据库连接池指标**：
  - `hikari_connections_active`：活跃连接数
  - `hikari_connections_idle`：空闲连接数
  - `hikari_connections_pending`：等待连接数

## 🚀 快速开始

### 1. 启动应用

确保应用已启动并暴露了 `/actuator/prometheus` 端点：

```bash
# 检查健康状态
curl http://localhost:8081/actuator/health

# 查看Prometheus指标
curl http://localhost:8081/actuator/prometheus
```

### 2. 配置Prometheus

编辑 `prometheus.yml` 文件，确保 `client-service-backend` 的 `targets` 配置正确：

```yaml
- job_name: 'client-service-backend'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['client-service:8081']  # 根据实际部署情况修改
```

### 3. 启动Prometheus

使用Docker Compose启动Prometheus：

```yaml
# docker-compose.yml 中添加
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: client-service-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
```

或直接运行：

```bash
docker run -d \
  --name client-service-prometheus \
  -p 9090:9090 \
  -v $(pwd)/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:latest
```

### 4. 访问Prometheus UI

打开浏览器访问：`http://localhost:9090`

## 📈 常用查询示例

### 查询接口平均响应时间

```promql
rate(api_matter_receive_seconds_sum[5m]) / rate(api_matter_receive_seconds_count[5m])
```

### 查询接口QPS

```promql
rate(api_matter_receive_seconds_count[5m])
```

### 查询JVM内存使用率

```promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

### 查询HTTP错误率

```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) * 100
```

## 🔔 告警规则示例

创建 `alerts.yml` 文件：

```yaml
groups:
  - name: client-service-alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "高错误率告警"
          description: "错误率超过5%"

      - alert: SlowAPI
        expr: rate(api_matter_receive_seconds_sum[5m]) / rate(api_matter_receive_seconds_count[5m]) > 3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "接口响应慢"
          description: "接收项目数据接口平均响应时间超过3秒"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "内存使用率过高"
          description: "JVM堆内存使用率超过90%"
```

在 `prometheus.yml` 中添加告警规则：

```yaml
rule_files:
  - 'alerts.yml'
```

## 📊 Grafana集成

### 1. 添加Prometheus数据源

在Grafana中添加Prometheus数据源：
- URL: `http://prometheus:9090`
- Access: Server (default)

### 2. 导入仪表板

可以使用以下预定义的仪表板模板：
- Spring Boot 2.1 Statistics (ID: 11378)
- JVM (Micrometer) (ID: 4701)

或创建自定义仪表板，监控以下指标：
- 接口响应时间
- 接口QPS
- 错误率
- JVM内存和GC
- 数据库连接池状态

## 🔧 配置说明

### application.yml 配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true
```

### 添加自定义指标

在Service层使用 `MeterRegistry`：

```java
@Autowired
private MeterRegistry meterRegistry;

public void someMethod() {
    // 计数器
    meterRegistry.counter("business.matter.created").increment();
    
    // 计时器
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
        // 业务逻辑
    } finally {
        sample.stop(meterRegistry.timer("business.matter.process"));
    }
    
    // 仪表（Gauge）
    meterRegistry.gauge("business.matter.active", matterCount);
}
```

## 📝 注意事项

1. **性能影响**：Prometheus指标收集对性能影响很小，但建议在生产环境中合理配置 `scrape_interval`
2. **数据保留**：默认保留15天，可通过 `--storage.tsdb.retention.time` 参数调整
3. **安全**：生产环境建议通过Nginx反向代理添加认证，避免直接暴露Prometheus端点
4. **网络**：确保Prometheus能够访问应用的 `/actuator/prometheus` 端点

## 🔗 相关文档

- [Prometheus官方文档](https://prometheus.io/docs/)
- [Spring Boot Actuator文档](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer文档](https://micrometer.io/docs)
