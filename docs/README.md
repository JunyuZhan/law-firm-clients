# 客户服务系统文档

## 目录结构

```
docs/
├── api/                    # API 文档
│   ├── API.md              # API 接口完整文档
│   ├── CALLBACK_API_SPEC.md        # 回调 API 规范
│   └── API_KEY_INTEGRATION_GUIDE.md # API Key 集成指南
├── guides/                 # 使用指南
│   ├── QUICKSTART.md               # 快速启动指南
│   ├── USER_MANUAL.md              # 用户手册
│   ├── ADMIN_LOGIN_GUIDE.md        # 管理员登录指南
│   ├── ADMIN_PASSWORD_MANAGEMENT.md # 管理员密码管理
│   ├── CREATE_API_KEY_GUIDE.md     # API Key 创建指南
│   ├── SYSTEM_CONFIG_GUIDE.md      # 系统配置指南
│   ├── NOTIFICATION_TEMPLATE_GUIDE.md # 通知模板指南
│   └── CUSTOMER_SERVICE_INTEGRATION_GUIDE.md # 对接集成指南
├── operations/             # 运维文档
│   ├── OPERATIONS_MANUAL.md        # 运维手册
│   ├── SYSTEM_CONFIGURATION_GUIDE.md # 系统配置完整指南（整合版）
│   ├── LOGGING_SETUP.md            # 日志配置
│   ├── PERFORMANCE_OPTIMIZATION.md # 性能优化
│   ├── VIRUS_SCAN_SETUP.md         # 病毒扫描配置
│   └── FILE_LIFECYCLE_MANAGEMENT.md # 文件生命周期管理
└── development/            # 开发文档
    ├── DEVELOPMENT_GUIDE.md        # 开发指南
    └── IMPLEMENTATION_SUMMARY.md   # 实现总结
```

## 快速导航

### 新手入门
1. [快速启动指南](guides/QUICKSTART.md) - 5 分钟快速部署
2. [用户手册](guides/USER_MANUAL.md) - 功能使用说明
3. [管理员登录指南](guides/ADMIN_LOGIN_GUIDE.md) - 管理后台登录

### API 开发
1. [API 接口文档](api/API.md) - 完整 API 参考
2. [回调 API 规范](api/CALLBACK_API_SPEC.md) - 律所系统回调规范
3. [API Key 集成指南](api/API_KEY_INTEGRATION_GUIDE.md) - API 认证集成

### 系统配置
1. [系统配置完整指南](operations/SYSTEM_CONFIGURATION_GUIDE.md) - 系统对接配置（快速配置+详细说明）
2. [系统配置指南](guides/SYSTEM_CONFIG_GUIDE.md) - 配置项说明
3. [通知模板指南](guides/NOTIFICATION_TEMPLATE_GUIDE.md) - 配置通知模板
4. [对接集成指南](guides/CUSTOMER_SERVICE_INTEGRATION_GUIDE.md) - 与律所系统对接

### 运维部署
1. [运维手册](operations/OPERATIONS_MANUAL.md) - 日常运维操作
2. [系统配置完整指南](operations/SYSTEM_CONFIGURATION_GUIDE.md) - 系统对接配置
3. [日志配置](operations/LOGGING_SETUP.md) - ELK/Loki 日志收集
4. [性能优化](operations/PERFORMANCE_OPTIMIZATION.md) - 性能调优指南

### 开发参考
1. [开发指南](development/DEVELOPMENT_GUIDE.md) - 本地开发环境搭建
2. [实现总结](development/IMPLEMENTATION_SUMMARY.md) - 功能实现说明
