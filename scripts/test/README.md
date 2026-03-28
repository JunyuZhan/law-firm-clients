# 客户服务系统 - 测试脚本

## 📋 目录结构

```
scripts/test/
├── api-test.sh          # API测试脚本（主要功能测试）
├── run-all-tests.sh     # 测试运行入口脚本
└── README.md            # 本文档
```

## 🚀 快速开始

### 1. 确保后端服务运行

```bash
# 方式一：使用Maven（开发环境）
cd backend
mvn spring-boot:run

# 方式二：使用Docker
cd docker
docker compose up -d app
```

### 2. 运行测试

```bash
# 进入项目根目录
cd /path/to/project

# 快速测试（推荐）
./scripts/test/run-all-tests.sh quick

# 或直接运行API测试
./scripts/test/api-test.sh
```

## 📝 测试内容

### API测试 (`api-test.sh`)

测试以下功能：

1. ✅ **健康检查** - 验证服务是否运行
2. ✅ **管理员登录** - JWT认证测试
3. ✅ **获取当前用户信息** - 验证Token有效性
4. ✅ **API密钥列表** - 管理后台API密钥查询
5. ✅ **项目列表** - 项目管理功能
6. ✅ **通知历史** - 通知记录查询
7. ✅ **系统配置列表** - 系统配置管理
8. ✅ **管理员登出** - 登出功能
9. ⏭️ **接收项目数据** - API密钥认证测试（需要有效的API密钥）

## 🎯 测试选项

### 使用 `run-all-tests.sh`

```bash
# 快速测试（API基础测试，推荐）
./scripts/test/run-all-tests.sh quick

# 单元测试（Maven）
./scripts/test/run-all-tests.sh unit

# 集成测试（Maven）
./scripts/test/run-all-tests.sh integration

# API测试
./scripts/test/run-all-tests.sh api

# 完整测试套件（所有测试）
./scripts/test/run-all-tests.sh full

# 显示帮助
./scripts/test/run-all-tests.sh help
```

### 直接运行测试脚本

```bash
# API测试
./scripts/test/api-test.sh

# 单元测试（使用原有的test.sh）
./scripts/test.sh unit

# 集成测试
./scripts/test.sh integration

# 所有测试
./scripts/test.sh all
```

## 📊 测试结果

测试脚本会显示：

- ✅ **通过** - 测试成功
- ❌ **失败** - 测试失败（会显示错误信息）
- ⏭️ **跳过** - 测试被跳过（通常是前置条件不满足）

### 示例输出

```
==========================================
客户服务系统 - API测试
==========================================
检查后端服务状态...
✓ 后端服务运行正常

=== 健康检查测试 ===
✓ 健康检查: PASS

=== 管理员登录测试 ===
✓ 管理员登录: PASS
  Token: eyJhbGciOiJIUzM4NCJ9...

=== 获取当前用户信息测试 ===
✓ 获取当前用户信息: PASS

...

==========================================
测试总结
==========================================
总测试数: 9
通过: 8
失败: 0
跳过: 1

所有测试通过！
注意: 有 1 个测试被跳过（这是正常的）
```

## ⚙️ 配置

### 默认配置

- **后端地址**: `http://localhost:8081`
- **管理员账号**: `admin` / `admin123`

### 修改配置

编辑 `api-test.sh` 文件，修改以下变量：

```bash
BASE_URL="http://localhost:8081/api"  # 后端API地址
```

## 🔧 故障排除

### 问题：后端服务未运行

**错误**: `✗ 后端服务未运行 (HTTP XXX)`

**解决**:
```bash
# 启动后端服务
cd backend
mvn spring-boot:run
```

### 问题：登录失败

**错误**: `✗ 管理员登录: FAIL`

**解决**:
1. 检查数据库是否已初始化
2. 确认管理员账号密码是否正确（默认：`admin` / `admin123`）
3. 检查后端日志查看详细错误

### 问题：API密钥测试失败

**错误**: `✗ 接收项目数据: FAIL - API密钥无效`

**说明**: 这是正常的，如果测试环境中没有配置API密钥，该测试会被跳过。

**解决**（如果需要测试）:
1. 登录管理后台
2. 创建API密钥
3. 重新运行测试

## 📚 相关文档

- [开发指南](../docs/DEVELOPMENT_GUIDE.md)
- [API文档](../API.md)
- [快速启动指南](../QUICKSTART.md)

## 🤝 贡献

添加新测试：

1. 在 `api-test.sh` 中添加新的测试函数
2. 在 `main()` 函数中调用新测试
3. 更新本文档

测试函数模板：

```bash
test_new_feature() {
    echo -e "\n${BLUE}=== 新功能测试 ===${NC}"
    
    if [ -z "$ADMIN_TOKEN" ]; then
        print_result "新功能" "SKIP" "需要先登录"
        return
    fi
    
    local response=$(send_request "GET" "$BASE_URL/new/endpoint" "" \
        "Authorization: Bearer $ADMIN_TOKEN")
    local body=$(echo "$response" | sed '$d')
    local code=$(echo "$response" | tail -1)
    
    if [ "$code" = "200" ]; then
        print_result "新功能" "PASS"
    else
        print_result "新功能" "FAIL" "HTTP $code"
    fi
}
```
