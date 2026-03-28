# 前端路由集成测试

## 测试目的

验证 `/portal/matter/:id` 路由是否能够直接访问，无需通过 Portal 页面输入链接。

## 测试方法

### 方法1：单元测试（推荐）

运行 Vitest 单元测试：

```bash
cd frontend

# 安装测试依赖（首次运行）
npm install

# 运行测试
npm test

# 或运行测试并查看UI
npm run test:ui

# 或运行一次测试（CI模式）
npm run test:run
```

### 方法2：手动集成测试

运行集成测试脚本：

```bash
cd frontend

# 确保前端服务运行
npm run dev

# 在另一个终端运行测试脚本
./test/route-direct-access.test.sh
```

### 方法3：浏览器手动测试

1. 启动前端服务：`npm run dev`
2. 获取一个真实的项目访问链接（从后端API或管理后台）
3. 在浏览器中直接访问该链接，例如：
   ```
   http://localhost:3000/portal/matter/CS123?token=abc123
   ```
4. 检查：
   - ✅ 是否直接显示项目详情页（不是 Portal 首页）
   - ✅ 打开浏览器控制台（F12），查看是否有路由匹配日志
   - ✅ 项目数据是否正确加载

## 测试用例

### 1. 路由匹配测试
- ✅ `/portal/matter/:id` 应该优先匹配，而不是 `/portal`
- ✅ 路由参数 `id` 应该正确提取
- ✅ 查询参数 `token` 应该正确提取

### 2. 参数提取测试
- ✅ 支持不同的 matterId 格式
- ✅ 支持不同的 token 格式
- ✅ URL 编码/解码正确处理

### 3. 路由顺序测试
- ✅ `/portal/matter/:id` 应该在 `/portal` 之前定义
- ✅ 确保更具体的路由优先匹配

## 预期结果

所有测试应该通过，并且：

1. **路由匹配正确**：访问 `/portal/matter/CS123?token=abc` 时，应该匹配到 `PortalMatterDetail` 路由
2. **参数提取正确**：`route.params.id` 和 `route.query.token` 应该正确获取
3. **组件加载正确**：`MatterDetail` 组件应该正确加载并显示

## 调试

如果测试失败，检查：

1. **路由配置**：确认 `router/index.ts` 中 `/portal/matter/:id` 在 `/portal` 之前
2. **组件参数**：确认 `MatterDetail.vue` 中正确使用 `route.params.id` 和 `route.query.token`
3. **浏览器控制台**：查看是否有 JavaScript 错误
4. **网络请求**：确认 API 调用是否成功

## 测试文件说明

- `router-integration.test.ts` - Vitest 单元测试
- `route-direct-access.test.sh` - Shell 集成测试脚本
- `setup.ts` - Vitest 测试环境配置
