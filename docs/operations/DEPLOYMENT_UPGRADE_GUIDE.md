# 部署与升级手册

本文档面向服务器运维管理员，用于规范客户服务系统的首次部署、版本升级、回滚和交付留痕。

## 1. 适用范围

- 单机测试环境
- 单机正式环境
- 面向多个律师事务所客户的标准化交付环境

系统原则：

- 业务管理员负责系统配置、通知模板、API Key、文件管理和日常后台操作。
- 运维管理员负责程序升级、镜像替换、容器重启、数据库迁移、异常回滚和部署目录维护。

## 2. 目录规范（与当前生产 myu 一致）

**一体化部署（当前服务器用法）：** 将整个 Git 仓库放在 **`/root/law-firm-clients`**，与仓库同级的 **`docker/`** 内放 `docker-compose.yml`、`.env` 及持久化卷配置；项目根目录的 **`./deploy.sh`** 负责备份、拉代码、构建与重启。

```bash
cd /root/law-firm-clients
git pull origin main   # 或目标分支/标签
./deploy.sh --upgrade  # 或 ./deploy.sh --quick（见 deploy.sh --help）
```

说明：

- **`/root/law-firm`** 若仅有零散目录且无完整 Git 仓库，**不要**当作本系统的部署根目录；请以 **`/root/law-firm-clients`** 为准。
- **分离式布局（可选）：** 若将来拆成「只读源码 + 独立运行目录」，可采用源码目录 `/root/src/law-firm-clients`、运行目录 `/opt/law-firm-clients`，由运维自行同步 Compose 与 `.env`；与一体化二选一即可，勿混用两套路径。

## 3. 推荐部署方式

推荐采用 Docker Compose。

推荐原因：

- 对当前规模最直接
- 便于版本化镜像管理
- 便于升级与回滚
- 便于后续复制到多个客户环境

## 4. 首次部署流程

### 4.1 准备源码（一体化示例）

```bash
cd /root
git clone git@github.com:JunyuZhan/law-firm-clients.git law-firm-clients
cd law-firm-clients
git checkout <release-tag>   # 或保持 main
```

### 4.2 准备运行配置

在 **`law-firm-clients/docker/`** 下准备：

- `docker-compose.yml`（仓库已带）
- `.env`（首次执行 `./deploy.sh --init` 可生成模板并补齐）
- 持久化目录（由 Compose volumes 定义）
- 反向代理（若前置 Nginx/网关，在宿主机或独立容器中配置）
- 版本元数据：`APP_VERSION`、`APP_COMMIT_SHA`、`APP_BUILD_TIME`（按团队规范写入 `.env` 或镜像标签）

### 4.3 初始化数据库

建议按当前项目初始化脚本顺序执行：

- `scripts/init-db/01-schema.sql`
- `scripts/init-db/02-init-data.sql`
- 其他增量脚本按版本要求补齐

### 4.4 启动服务

推荐使用仓库根目录脚本（会自动进入 `docker/`、检查依赖与配置）：

```bash
cd /root/law-firm-clients
chmod +x deploy.sh
./deploy.sh --init    # 首次：生成配置并启动
# 日常仅启动：./deploy.sh
```

若手动操作 Compose，须在 **`docker/`** 目录执行：

```bash
cd /root/law-firm-clients/docker
docker compose up -d --build
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

### 5.2 升级执行（一体化目录）

1. `cd /root/law-firm-clients`
2. 执行 **`./deploy.sh --upgrade`**（内含备份、`git pull`、重建镜像、`docker compose up -d`），或先 `git pull` 再 **`./deploy.sh --quick`**
3. 若使用固定镜像标签发布，先在 `docker/docker-compose.yml` 或 `.env` 中更新标签后再执行上述脚本
4. 执行部署后冒烟测试

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
