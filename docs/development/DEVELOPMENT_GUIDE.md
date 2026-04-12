# 客户服务系统开发指南

> 文档版本：v1.0  
> 创建日期：2026-02-02  
> 适用系统：客户服务系统

## 📋 目录

- [架构设计](#架构设计)
- [代码规范](#代码规范)
- [开发流程](#开发流程)
- [API设计规范](#api设计规范)
- [前端开发规范](#前端开发规范)
- [数据库规范](#数据库规范)
- [测试规范](#测试规范)

---

## 🏗️ 架构设计

### 分层架构（DDD四层模型）

系统采用领域驱动设计（DDD）四层架构：

```
com.clientservice/
├── interfaces/          # 接口层（Controller）
│   └── rest/           # REST控制器
├── application/        # 应用层（Service、DTO、Task）
│   ├── service/        # 业务服务
│   ├── dto/            # 数据传输对象
│   └── task/           # 定时任务
├── domain/              # 领域层（Entity）
│   └── entity/         # 领域实体
└── infrastructure/      # 基础设施层
    ├── config/         # 配置类
    ├── persistence/    # 持久层（Mapper）
    ├── storage/        # 存储策略
    └── notification/   # 通知客户端
```

### 各层职责

#### 1. 接口层（Interfaces）
- **职责**：处理HTTP请求，参数验证，响应封装
- **规范**：
  - 使用`@RestController`注解
  - 统一使用`Result<T>`响应格式
  - 所有管理接口需要后台登录认证（JWT）
  - 使用Swagger注解完善API文档

#### 2. 应用层（Application）
- **职责**：业务逻辑编排，事务管理
- **规范**：
  - Service类使用`@Service`注解
  - 事务方法使用`@Transactional`注解
  - 使用DTO进行数据传输
  - 异常统一抛出`BusinessException`

#### 3. 领域层（Domain）
- **职责**：领域实体定义
- **规范**：
  - 实体类继承`BaseEntity`
  - 使用MyBatis-Plus注解
  - 定义业务常量（如状态常量）

#### 4. 基础设施层（Infrastructure）
- **职责**：技术实现细节
- **规范**：
  - Mapper接口继承`BaseMapper`
  - 配置类使用`@Configuration`注解
  - 外部服务客户端使用`@Component`注解

---

## 📝 代码规范

### Java代码规范

#### 1. 命名规范
- **类名**：大驼峰（PascalCase），如`MatterService`
- **方法名**：小驼峰（camelCase），如`getMatterList`
- **常量**：全大写下划线分隔，如`STATUS_ACTIVE`
- **包名**：全小写，如`com.clientservice.application.service`

#### 2. 注解使用
```java
// Service层
@Slf4j
@Service
@RequiredArgsConstructor
public class MatterService {
    // ...
}

// Controller层
@Slf4j
@Tag(name = "项目管理", description = "项目访问管理接口")
@RestController
@RequestMapping("/api/matter")
@RequiredArgsConstructor
public class MatterController {
    // ...
}
```

#### 3. 异常处理
```java
// ✅ 正确：抛出业务异常
if (matter == null) {
    throw new BusinessException("404", "项目不存在");
}

// ❌ 错误：捕获异常后返回错误串
try {
    // ...
} catch (Exception e) {
    return "错误信息";  // 不要这样做
}
```

#### 4. 日志记录
```java
// ✅ 正确：使用SLF4J日志
log.info("接收项目数据成功: matterId={}, clientId={}", matterId, clientId);
log.error("邮件发送失败: matterId={}, recipient={}", matterId, recipient, e);

// ❌ 错误：使用System.out.println
System.out.println("接收项目数据成功");  // 不要这样做
```

### TypeScript代码规范

#### 1. 命名规范
- **接口/类型**：大驼峰，如`MatterListItem`
- **函数**：小驼峰，如`getMatterList`
- **常量**：全大写下划线分隔，如`API_BASE_URL`
- **文件**：kebab-case，如`matter-list.vue`

#### 2. 类型定义
```typescript
// ✅ 正确：定义明确的类型
export interface MatterListItem {
  id: string
  clientId: number
  clientName: string
  status: string
}

// ❌ 错误：使用any
function getData(): any {  // 不要这样做
  // ...
}
```

#### 3. 组件规范
```vue
<script setup lang="ts">
// ✅ 正确：使用Composition API
import { ref, onMounted } from 'vue'
import { getMatterList, type MatterListItem } from '@/api/matter'

const loading = ref(false)
const dataSource = ref<MatterListItem[]>([])

async function loadData() {
  loading.value = true
  try {
    const res = await getMatterList({})
    dataSource.value = res.data || []
  } catch (error: any) {
    message.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
```

---

## 🔄 开发流程

### 新增功能开发流程

1. **数据库设计**
   - 创建表结构（必须包含`deleted`、`created_at`、`updated_at`字段）
   - 编写数据库迁移脚本（`scripts/migration/V*.sql`）

2. **实体类（Entity）**
   - 继承`BaseEntity`
   - 添加MyBatis-Plus注解
   - 定义业务常量

3. **Mapper层**
   - 创建Mapper接口，继承`BaseMapper`
   - 如有复杂查询，创建XML映射文件

4. **DTO层**
   - 创建请求DTO（如`MatterReceiveRequest`）
   - 创建响应DTO（如`MatterListDTO`）

5. **Service层**
   - 创建Service类
   - 实现业务逻辑
   - 添加事务注解（如需要）

6. **Controller层**
   - 创建Controller类
   - 添加后台登录认证（管理接口）
   - 添加Swagger注解

7. **前端API客户端**
   - 在`frontend/src/api/`创建API文件
   - 定义TypeScript接口
   - 实现API调用函数

8. **前端页面**
   - 在`frontend/src/views/`创建页面组件
   - 使用Ant Design Vue组件
   - 添加路由配置

9. **测试**
   - 编写单元测试
   - 编写集成测试（如需要）

10. **文档更新**
    - 更新`docs/development/TODO.md`
    - 更新`README.md`（如需要）
    - 更新API文档（如需要）

---

## 🌐 API设计规范

### RESTful API设计

#### 1. URL设计
```
GET    /api/matter/list          # 获取列表
GET    /api/matter/detail/{id}   # 获取详情
POST   /api/matter/receive       # 创建资源
PUT    /api/admin/config/{id}    # 更新资源
DELETE /api/admin/config/{id}    # 删除资源
```

#### 2. 请求头
```http
Content-Type: application/json
Authorization: Bearer {API密钥}
```

#### 3. 响应格式
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1706860800000
}
```

#### 4. 错误响应
```json
{
  "success": false,
  "code": "404",
  "message": "项目不存在",
  "data": null,
  "timestamp": 1706860800000
}
```

#### 5. HTTP状态码
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未认证（API密钥无效）
- `403` - 无权限（项目已过期）
- `404` - 资源不存在
- `500` - 服务器错误

---

## 💻 前端开发规范

### 1. 组件结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup lang="ts">
// 导入
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'

// 响应式数据
const loading = ref(false)
const dataSource = ref<Type[]>([])

// 方法
async function loadData() {
  // ...
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 样式 */
</style>
```

### 2. API调用规范
```typescript
// ✅ 正确：使用统一的API客户端
import { getMatterList, type MatterListItem } from '@/api/matter'

async function loadData() {
  try {
    const res = await getMatterList({})
    dataSource.value = res.data || []
  } catch (error: any) {
    message.error(error.message || '加载失败')
  }
}

// ❌ 错误：直接使用axios
import axios from 'axios'
axios.get('/api/matter/list')  // 不要这样做
```

### 3. 表单验证
```vue
<a-form
  :model="formData"
  :rules="rules"
  @finish="handleSubmit"
>
  <a-form-item label="配置键" name="configKey">
    <a-input v-model:value="formData.configKey" />
  </a-form-item>
</a-form>
```

### 4. 表格使用
```vue
<a-table
  :columns="columns"
  :data-source="dataSource"
  :loading="loading"
  :pagination="pagination"
  row-key="id"
  @change="handleTableChange"
>
  <template #bodyCell="{ column, record }">
    <!-- 自定义列内容 -->
  </template>
</a-table>
```

---

## 🗄️ 数据库规范

### 1. 表设计规范
- 所有表必须包含以下字段：
  - `id` - 主键
  - `created_at` - 创建时间
  - `updated_at` - 更新时间
  - `deleted` - 逻辑删除标志（布尔类型，默认false）

### 2. 命名规范
- **表名**：小写下划线分隔，如`client_matter`
- **字段名**：小写下划线分隔，如`client_id`
- **索引名**：`idx_表名_字段名`，如`idx_notification_next_retry_at`

### 3. 迁移脚本规范
```sql
-- =====================================================
-- 数据库迁移脚本：添加通知重试字段
-- =====================================================
-- 版本: V2
-- 日期: 2026-02-02
-- 描述: 为notification_record表添加重试相关字段
-- =====================================================

-- 添加字段
ALTER TABLE public.notification_record
    ADD COLUMN IF NOT EXISTS retry_count INTEGER DEFAULT 0;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_notification_next_retry_at 
    ON public.notification_record(next_retry_at) 
    WHERE status = 'FAILED' 
      AND next_retry_at IS NOT NULL;

-- 添加注释
COMMENT ON COLUMN public.notification_record.retry_count IS '重试次数';
```

---

## 🧪 测试规范

### 1. 单元测试
- 测试类命名：`{ClassName}Test`
- 测试方法命名：`{methodName}_Should{ExpectedBehavior}`
- 使用JUnit 5和Mockito

```java
@ExtendWith(MockitoExtension.class)
class MatterServiceTest {
    
    @Mock
    private ClientMatterMapper matterMapper;
    
    @InjectMocks
    private MatterService matterService;
    
    @Test
    @DisplayName("获取项目列表应该成功")
    void getMatterList_ShouldSuccess() {
        // Given
        // When
        // Then
    }
}
```

### 2. 集成测试
- 使用`@SpringBootTest`注解
- 使用`@AutoConfigureMockMvc`进行HTTP测试
- 使用测试数据库

```java
@SpringBootTest
@AutoConfigureMockMvc
class MatterControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetMatterList() throws Exception {
        mockMvc.perform(get("/api/matter/list")
                .header("Authorization", "Bearer test-api-key"))
                .andExpect(status().isOk());
    }
}
```

---

## 🔐 安全规范

### 1. API密钥认证
- 所有管理接口必须验证API密钥
- API密钥通过`Authorization: Bearer {key}`传递
- 使用`ApiKeyService.validateApiKey()`验证

### 2. 访问令牌
- 客户门户使用访问令牌验证
- 令牌包含在URL参数中：`?token={accessToken}`
- 验证令牌有效性和项目归属

### 3. 数据安全
- 敏感数据（如API密钥）部分隐藏显示
- 文件访问验证项目归属
- 使用逻辑删除，不物理删除数据

---

## 📚 文档规范

### 1. 代码注释
```java
/**
 * 获取项目列表（管理后台使用）
 *
 * @param clientId 客户ID（可选）
 * @param status 状态（可选）
 * @param limit 限制数量（可选，默认100）
 * @return 项目列表
 */
public List<MatterListDTO> getMatterList(...) {
    // ...
}
```

### 2. API文档
- 使用Swagger注解完善API文档
- 添加`@Operation`描述接口功能
- 添加`@Parameter`描述参数

### 3. README更新
- 新增功能后更新`README.md`
- 更新`docs/development/TODO.md`标记完成状态
- 重要变更更新`CHANGELOG.md`（如有）

---

## 🚀 部署规范

### 1. 环境配置
- 使用`application.yml`进行配置
- 敏感信息使用环境变量
- 支持多环境配置（dev、test、prod）

### 2. Docker部署
- 使用Docker Compose编排服务
- 数据卷持久化
- 健康检查配置

### 3. 数据库迁移
- 迁移脚本放在`scripts/migration/`目录
- 命名规范：`V{版本号}__{描述}.sql`
- 迁移脚本必须可重复执行（使用`IF NOT EXISTS`）

---

## ✅ 代码审查清单

开发完成后，请检查以下事项：

- [ ] 代码符合命名规范
- [ ] 异常处理完善
- [ ] 日志记录完整
- [ ] API文档完整（Swagger注解）
- [ ] 单元测试覆盖
- [ ] 数据库迁移脚本可重复执行
- [ ] 前端类型定义完整
- [ ] 错误处理用户友好
- [ ] 安全性检查（API密钥验证、权限控制）
- [ ] 文档更新（README、TODO）

---

## 📖 参考文档

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Vue 3官方文档](https://vuejs.org/)
- [Ant Design Vue文档](https://antdv.com/)
- [MyBatis-Plus文档](https://baomidou.com/)
- [项目API文档](./API.md)
- [运维手册](./OPERATIONS_MANUAL.md)

---

**最后更新**：2026-02-02
