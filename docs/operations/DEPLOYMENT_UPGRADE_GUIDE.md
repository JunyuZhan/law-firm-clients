# 部署与升级手册

本文档面向服务器运维管理员，用于规范客户服务系统的首次部署、版本升级、回滚和交付留痕。

## 1. 适用范围

- 单机测试环境
- 单机正式环境
- 面向多个律师事务所客户的标准化交付环境

系统原则：

- 业务管理员负责系统配置、通知模板、API Key、文件管理和日常后台操作。
- 运维管理员负责程序升级、镜像替换、容器重启、数据库迁移、异常回滚和部署目录维护。

## 2. 推荐目录规范

- 源码目录：`/root/src/law-firm-clients`
- 部署目录：`/opt/law-firm-clients`

要求：

- `/root/src` 只放源码、构建脚本和版本标签信息
- `/opt/law-firm-clients` 只放部署清单、环境变量、持久化目录和运维台账

## 3. 推荐部署方式

推荐采用 Docker Compose。

推荐原因：

- 对当前规模最直接
- 便于版本化镜像管理
- 便于升级与回滚
- 便于后续复制到多个客户环境

## 4. 首次部署流程

### 4.1 准备源码

```bash
cd /root/src
git clone <your-repository-url> law-firm-clients
cd law-firm-clients
git checkout <release-tag>
```

### 4.2 准备部署目录

```bash
mkdir -p /opt/law-firm-clients
```

需要准备：

- Compose 文件
- `.env` 文件
- 持久化目录
- 反向代理配置
- 版本元数据：`APP_VERSION`、`APP_COMMIT_SHA`、`APP_BUILD_TIME`

### 4.3 初始化数据库

建议按当前项目初始化脚本顺序执行：

- `scripts/init-db/01-schema.sql`
- `scripts/init-db/02-init-data.sql`
- 其他增量脚本按版本要求补齐

### 4.4 启动服务

```bash
docker compose pull
docker compose up -d
```

### 4.5 首次部署后必须验证

- 管理后台可登录
- 客户门户首页可访问
- 项目访问链接可打开
- 文件列表和下载链路正常
- 通知页面可访问
- 函件验证页可访问

## 5. 标准升级流程

### 5.1 升级前

必须先完成：

- 数据库备份
- 文件目录备份
- 系统配置备份
- 当前版本号、镜像标签、提交号留痕

### 5.2 升级执行

1. 在源码目录拉取目标版本代码
2. 确认目标镜像标签，例如 `v1.0.1`
3. 在部署目录更新 Compose 中的镜像标签与版本元数据
4. 执行 `docker compose pull`
5. 执行 `docker compose up -d`
6. 执行部署后冒烟测试

建议：

- `APP_VERSION` 用于后台系统信息页显示产品版本
- `APP_COMMIT_SHA` 用于显示对应提交号
- `APP_BUILD_TIME` 用于显示构建时间

## 6. 标准回滚流程

适用于升级后出现严重故障、无法在短时间内修复的情况。

1. 确认上一稳定版本标签
2. 将 Compose 清单切回上一稳定镜像标签
3. 执行 `docker compose up -d`
4. 如涉及数据库破坏性变更，按备份恢复流程恢复数据库和文件
5. 重新执行冒烟测试并记录回滚结论

## 7. 发布留痕要求

每次首次部署、升级、回滚都应至少记录：

- 客户名称
- 环境名称
- 部署版本
- 镜像标签
- Git 提交号
- 执行时间
- 执行人
- 升级前备份编号
- 冒烟测试结果
- 是否回滚
