# 日志收集快速开始

## 使用Loki收集日志

### 1. 启动Loki Stack

```bash
cd docker
docker-compose -f docker-compose-loki.yml up -d
```

### 2. 访问Grafana

- URL: http://localhost:3000
- 用户名: admin
- 密码: admin（默认，可通过环境变量 `GRAFANA_PASSWORD` 修改）

### 3. 配置应用日志

应用会自动输出JSON格式日志到 `logs/client-service-json.log`，Promtail会自动收集这些日志。

### 4. 在Grafana中查询日志

打开Grafana，进入Explore页面，选择Loki数据源，使用LogQL查询：

```
{application="client-service"}
```

## 使用ELK Stack收集日志

### 1. 安装和配置Filebeat

参考 `docs/LOGGING_SETUP.md` 中的Filebeat配置部分。

### 2. 启动Elasticsearch和Kibana

```bash
# 使用Docker Compose启动ELK Stack
docker-compose -f docker-compose-elk.yml up -d
```

### 3. 在Kibana中查询日志

- URL: http://localhost:5601
- 创建索引模式: `client-service-*`
- 查询日志: 使用Kibana的Discover功能

## 直接发送日志到Loki（可选）

如果不想使用Promtail，可以配置应用直接发送日志到Loki：

```bash
# 注意：需要同时启用prod和loki两个profile
export LOKI_URL=http://localhost:3100/loki/api/v1/push
export SPRING_PROFILES_ACTIVE=prod,loki
java -jar client-service.jar
```

## 日志文件位置

- 普通格式: `logs/client-service.log`
- JSON格式: `logs/client-service-json.log`
- 日志轮转: `logs/client-service-json-2026-02-01.log`

## 环境变量

- `LOG_PATH`: 日志文件路径（默认：logs）
- `LOG_FILE`: 日志文件名（默认：client-service）
- `LOKI_URL`: Loki服务地址（可选）
- `SPRING_PROFILES_ACTIVE`: Spring环境（dev/test/prod）
