# 日志收集配置文档

## 概述

本文档描述了如何配置和使用日志收集系统（ELK/Loki）来收集和分析客户服务系统的日志。

## 日志格式

系统支持两种日志格式：

1. **普通格式**：用于开发和测试环境，便于阅读
2. **JSON格式**：用于生产环境，便于日志收集系统解析

## 配置说明

### 1. Logback配置

系统使用 `logback-spring.xml` 配置文件，支持以下功能：

- **控制台输出**：开发环境使用普通格式，生产环境使用JSON格式
- **文件输出**：同时输出普通格式和JSON格式日志文件
- **日志轮转**：按天轮转，保留30天，总大小限制1GB
- **环境区分**：根据Spring Profile自动切换日志格式

### 2. 日志级别配置

```yaml
logging:
  level:
    root: INFO
    com.clientservice: DEBUG  # 开发环境
    # com.clientservice: INFO  # 生产环境
```

### 3. 环境变量配置

可以通过环境变量控制日志行为：

- `LOG_PATH`: 日志文件路径（默认：logs）
- `LOG_FILE`: 日志文件名（默认：client-service）
- `LOKI_URL`: Loki服务地址（可选，默认：http://localhost:3100/loki/api/v1/push）
- `spring.profiles.active`: Spring环境（dev/test/prod），如需使用Loki Appender，需要同时启用loki profile，例如：`prod,loki`

## ELK Stack配置

### 1. 使用Filebeat收集日志

#### 安装Filebeat

```bash
# Ubuntu/Debian
wget https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-8.11.0-amd64.deb
sudo dpkg -i filebeat-8.11.0-amd64.deb

# CentOS/RHEL
wget https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-8.11.0-x86_64.rpm
sudo rpm -vi filebeat-8.11.0-x86_64.rpm
```

#### 配置Filebeat

编辑 `/etc/filebeat/filebeat.yml`:

```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /path/to/client-service/logs/client-service-json-*.log
  json.keys_under_root: true
  json.add_error_key: true
  json.message_key: message

output.elasticsearch:
  hosts: ["localhost:9200"]
  indices:
    - index: "client-service-%{+yyyy.MM.dd}"

setup.template.name: "client-service"
setup.template.pattern: "client-service-*"
```

#### 启动Filebeat

```bash
sudo systemctl enable filebeat
sudo systemctl start filebeat
```

### 2. 使用Logstash收集日志（可选）

#### 安装Logstash

```bash
# Ubuntu/Debian
wget https://artifacts.elastic.co/downloads/logstash/logstash-8.11.0-amd64.deb
sudo dpkg -i logstash-8.11.0-amd64.deb

# CentOS/RHEL
wget https://artifacts.elastic.co/downloads/logstash/logstash-8.11.0-x86_64.rpm
sudo rpm -vi logstash-8.11.0-x86_64.rpm
```

#### 配置Logstash

创建 `/etc/logstash/conf.d/client-service.conf`:

```ruby
input {
  file {
    path => "/path/to/client-service/logs/client-service-json-*.log"
    codec => json
    type => "client-service"
    start_position => "beginning"
  }
}

filter {
  if [type] == "client-service" {
    date {
      match => [ "timestamp", "ISO8601" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "client-service-%{+YYYY.MM.dd}"
  }
}
```

#### 启动Logstash

```bash
sudo systemctl enable logstash
sudo systemctl start logstash
```

## Loki配置

### 1. 使用Docker Compose部署Loki

创建 `docker-compose-loki.yml`:

```yaml
version: '3.8'

services:
  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml
    volumes:
      - loki-data:/loki

  promtail:
    image: grafana/promtail:latest
    volumes:
      - /path/to/client-service/logs:/var/log/client-service:ro
      - ./promtail-config.yml:/etc/promtail/config.yml
    command: -config.file=/etc/promtail/config.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana

volumes:
  loki-data:
  grafana-data:
```

### 2. Promtail配置

创建 `promtail-config.yml`:

```yaml
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: client-service
    static_configs:
      - targets:
          - localhost
        labels:
          job: client-service
          __path__: /var/log/client-service/client-service-json-*.log
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            logger: logger_name
            message: message
            thread: thread_name
            application: application
            environment: environment
      - labels:
          level:
          application:
          environment:
      - timestamp:
          source: timestamp
          format: RFC3339
```

### 3. 启动Loki Stack

```bash
docker-compose -f docker-compose-loki.yml up -d
```

### 4. 配置应用直接发送到Loki

如果使用Loki Appender（需要启用loki profile）：

```bash
# 设置环境变量
export LOKI_URL=http://localhost:3100/loki/api/v1/push
export SPRING_PROFILES_ACTIVE=prod,loki

# 启动应用
java -jar client-service.jar
```

## 日志查询示例

### Elasticsearch查询（Kibana）

```
# 查询错误日志
level:ERROR AND application:client-service

# 查询特定时间段的日志
@timestamp:[2026-02-01 TO 2026-02-02] AND application:client-service

# 查询特定项目的访问日志
message:*matterId* AND level:INFO
```

### Loki查询（Grafana）

```
# 查询错误日志
{application="client-service", level="ERROR"}

# 查询特定时间段的日志
{application="client-service"} | json | timestamp >= 2026-02-01T00:00:00Z

# 查询包含特定关键词的日志
{application="client-service"} |= "matterId"
```

## 日志字段说明

JSON格式日志包含以下字段：

- `timestamp`: 日志时间戳（ISO8601格式）
- `level`: 日志级别（DEBUG/INFO/WARN/ERROR）
- `logger_name`: 日志记录器名称
- `message`: 日志消息
- `thread_name`: 线程名称
- `application`: 应用名称（client-service）
- `environment`: 环境（dev/test/prod）
- `stack_trace`: 异常堆栈（如果有）

## 最佳实践

1. **生产环境使用JSON格式**
   - 便于日志收集系统解析
   - 支持结构化查询

2. **合理设置日志级别**
   - 生产环境使用INFO级别
   - 开发环境使用DEBUG级别

3. **日志轮转配置**
   - 按天轮转，避免单个文件过大
   - 设置保留天数，控制磁盘使用

4. **监控日志收集**
   - 监控日志收集系统状态
   - 设置告警规则

5. **日志安全**
   - 避免在日志中输出敏感信息（密码、密钥等）
   - 使用MDC添加上下文信息

## 故障排查

### 日志文件未生成

1. 检查日志目录权限
2. 检查磁盘空间
3. 检查logback配置

### 日志收集失败

1. 检查日志收集服务状态
2. 检查网络连接
3. 检查配置文件格式

### 日志格式错误

1. 检查logstash-logback-encoder版本
2. 检查logback配置
3. 查看日志收集系统错误日志

## 参考资源

- [Logback官方文档](http://logback.qos.ch/)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
- [Loki官方文档](https://grafana.com/docs/loki/latest/)
- [ELK Stack官方文档](https://www.elastic.co/guide/index.html)
