# 前后端链接生成与路由匹配集成测试结果

## 测试时间
2026-02-04

## 测试方法
运行前后端集成测试脚本：`node test/backend-frontend-integration.test.js`

## 测试结果

### ✅ 通过的检查项

1. **后端链接生成格式正确**
   - ✓ 格式：`{baseUrl}/portal/matter/{matterId}?token={token}`
   - ✓ 包含路径 `/portal/matter/` 和查询参数 `token`

2. **前端路由配置正确**
   - ✓ 有 `/portal/matter/:id` 路由
   - ✓ 路由顺序正确（`/portal/matter/:id` 在 `/portal` 之前）

3. **链接格式匹配**
   - ✓ 后端生成的路径格式匹配前端路由
   - ✓ MatterId 可以正确提取
   - ✓ Token 可以正确提取

4. **组件参数提取正确**
   - ✓ 使用 `route.params.id` 提取项目ID
   - ✓ 使用 `route.query.token` 提取token

### ⚠️ 发现的问题

**前后端端口不一致**

- 后端默认端口：`8081`
- 前端开发服务器端口：`3000`

**影响：**
- 如果后端生成的链接是 `http://localhost:8081/portal/matter/...`
- 但前端运行在 `http://localhost:3000`
- 直接访问链接会访问到后端端口，而不是前端

**解决方案：**

1. **开发环境**：
   - 使用代理配置（vite.config.ts 中已配置）
   - 或确保后端生成的 baseUrl 指向前端端口

2. **生产环境**：
   - 前后端应使用相同的域名和端口
   - 或使用反向代理（Nginx）统一入口

## 完整链接访问流程验证

### 1. 后端生成链接
```
http://localhost:8081/portal/matter/CS1234567890123456789?token=test-token-12345678901234567890123456789012
```

### 2. 用户点击链接，浏览器访问
- 路径：`/portal/matter/CS1234567890123456789`
- 查询参数：`token=test-token-12345678901234567890123456789012`

### 3. Vue Router 匹配路由
- 匹配路由：`/portal/matter/:id`
- 提取参数：`id=CS1234567890123456789`
- 提取查询：`token=test-token-12345678901234567890123456789012`

### 4. MatterDetail 组件加载
- `matterId = route.params.id = CS1234567890123456789`
- `token = route.query.token = test-token-12345678901234567890123456789012`

### 5. 组件调用 API
```
GET /portal/api/matter/CS1234567890123456789?token=test-token-12345678901234567890123456789012
```

## 代码验证

### 后端链接生成（UrlGenerator.java）

```java
public String generateAccessUrl(final String matterId, final String token) {
    return String.format("%s/portal/matter/%s?token=%s", baseUrl, matterId, token);
}
```

**生成的链接格式：**
```
{baseUrl}/portal/matter/{matterId}?token={token}
```

示例：
```
http://localhost:8081/portal/matter/CS123?token=abc123
```

### 前端路由配置（router/index.ts）

```typescript
{
  path: '/portal/matter/:id',  // 匹配路径部分
  name: 'PortalMatterDetail',
  component: () => import('@/views/MatterDetail.vue'),
}
```

### 组件参数提取（MatterDetail.vue）

```typescript
const matterId = computed(() => route.params.id as string)  // 从路径参数提取
const token = computed(() => route.query.token as string)     // 从查询参数提取
```

## 结论

✅ **链接格式与路由匹配正确**

后端生成的链接格式与前端路由配置完全匹配，理论上可以直接访问。

⚠️ **需要注意端口配置**

开发环境中前后端端口不一致，需要：
- 使用代理配置，或
- 确保 baseUrl 配置正确

生产环境中应使用相同的域名和端口，或通过反向代理统一入口。

## 建议

1. **开发环境**：
   - 检查 `system.base-url` 配置是否指向前端开发服务器（`http://localhost:3000`）
   - 或使用 Vite 代理配置（已配置）

2. **生产环境**：
   - 确保 `system.base-url` 配置为生产域名
   - 使用 Nginx 等反向代理统一入口

3. **测试验证**：
   - 实际运行时测试链接访问
   - 验证端口和域名配置是否正确
