# 移动端优化组件使用指南

本指南介绍了新增的移动端优化组件及其使用方法。

## 组件列表

### 1. MobileDrawer - 移动端抽屉菜单

移动端专用的侧边栏菜单，支持从右侧滑出。

**功能特性：**
- 响应式设计，仅在移动端显示
- 支持菜单项高亮和徽章提示
- 内置用户信息展示
- 优雅的动画效果

**使用示例：**

```vue
<template>
  <div>
    <!-- 触发按钮 -->
    <a-button @click="drawerOpen = true">
      <template #icon>
        <MenuOutlined />
      </template>
    </a-button>

    <!-- 移动端抽屉 -->
    <MobileDrawer
      v-model:open="drawerOpen"
      user-name="张三"
      user-avatar="/avatar.jpg"
      :unread-count="5"
      @menu-click="handleMenuClick"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MobileDrawer from '@/components/MobileDrawer.vue'

const drawerOpen = ref(false)

function handleMenuClick(key: string) {
  console.log('点击菜单项:', key)
  // 路由跳转逻辑
}
</script>
```

**Props:**
| 属性 | 类型 | 默认值 | 说明 |
|------|------|---------|------|
| open | Boolean | false | 控制抽屉显示/隐藏 |
| userName | String | '' | 用户名称；为空时优先使用 `portalVisitor` 中的最近访客资料 |
| userAvatar | String | '' | 用户头像 URL |
| unreadCount | Number | 0 | 未读消息数量 |

**Events:**
| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:open | (value: boolean) | 抽屉显示状态变化 |
| menu-click | (key: string) | 菜单项点击 |

---

### 2. PullRefresh - 下拉刷新组件

移动端下拉刷新功能，支持触摸手势操作。

**功能特性：**
- 阻尼效果，提升手感
- 自定义触发阈值
- 加载状态指示
- 防止重复刷新

**使用示例：**

```vue
<template>
  <PullRefresh
    :loading="refreshing"
    @refresh="handleRefresh"
    ref="pullRefreshRef"
  >
    <div class="content">
      <!-- 你的内容 -->
      <div v-for="item in list" :key="item.id">
        {{ item.title }}
      </div>
    </div>
  </PullRefresh>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PullRefresh from '@/components/PullRefresh.vue'
import { message } from 'ant-design-vue'

const pullRefreshRef = ref()
const refreshing = ref(false)
const list = ref([])

async function handleRefresh() {
  refreshing.value = true
  try {
    await fetchData()
    message.success('刷新成功')
  } catch (error) {
    message.error('刷新失败')
  } finally {
    refreshing.value = false
    pullRefreshRef.value?.reset()
  }
}
</script>
```

**Props:**
| 属性 | 类型 | 默认值 | 说明 |
|------|------|---------|------|
| threshold | Number | 60 | 触发刷新的阈值（像素） |
| maxDistance | Number | 120 | 最大拉动距离（像素） |
| loading | Boolean | false | 是否正在刷新 |

**Events:**
| 事件名 | 参数 | 说明 |
|--------|------|------|
| refresh | - | 触发刷新 |

**Expose 方法：**
| 方法名 | 参数 | 说明 |
|--------|------|------|
| reset | - | 重置组件状态 |

---

### 3. MobileBottomNav - 移动端底部导航栏

移动端底部固定导航栏，类似 App 的底部 Tab 切换。

**功能特性：**
- 固定在底部，便于拇指操作
- 自动高亮当前页面
- 支持徽章提示
- 安全区域适配（iPhone X+）

**使用示例：**

```vue
<template>
  <div class="page">
    <!-- 页面内容 -->
    <div class="content">
      <!-- 你的页面内容 -->
    </div>

    <!-- 底部导航栏 -->
    <MobileBottomNav />
  </div>
</template>

<script setup lang="ts">
import MobileBottomNav from '@/components/MobileBottomNav.vue'
</script>

<style scoped>
.content {
  padding-bottom: 60px; /* 为底部导航栏留出空间 */
}
</style>
```

**配置：**

组件内部预配置了以下导航项，可以根据实际需求修改：

| Key | 标签 | 图标 | 路径 |
|-----|------|------|------|
| home | 首页 | HomeOutlined | /portal |
| matter | 我的项目 | FileTextOutlined | /matters |
| files | 文件中心 | FolderOutlined | /files |
| notifications | 消息通知 | BellOutlined | /notifications |
| profile | 个人中心 | UserOutlined | /profile |

**自定义导航项：**

编辑 `MobileBottomNav.vue` 的 `navItems` 计算属性：

```typescript
const navItems = computed<NavItem[]>(() => [
  {
    key: 'custom',
    label: '自定义',
    icon: CustomIcon,
    path: '/custom',
    badge: 10, // 可选徽章数量
  },
  // ...
])
```

---

## 在 Portal 页面集成

已将以下组件集成到 `Portal.vue`：

1. **MobileDrawer** - 添加移动端菜单按钮和抽屉
2. **MobileBottomNav** - 提供固定底部导航
3. **响应式样式** - 添加了 768px 和 480px 断点的适配

### Portal.vue 中的关键修改

**1. 通过 AppHeader 暴露菜单按钮**

```vue
<AppHeader
  variant="portal"
  :show-mobile-menu="isMobile"
  @menu-click="openMobileDrawer"
/>
```

**2. 添加 MobileDrawer 组件**

```vue
<MobileDrawer
  v-model:open="mobileMenuVisible"
  :user-name="drawerUserName"
  unread-count="0"
  @menu-click="handleMenuClick"
/>
```

**3. 移动端样式优化**

- 响应式字体大小
- 触摸友好的按钮尺寸
- 表格横向滚动
- 模态框适配小屏幕

---

## 最佳实践

### 1. 响应式断点

推荐使用的断点：

```css
/* 平板 */
@media (max-width: 768px) { }

/* 手机 */
@media (max-width: 480px) { }

/* 大屏 */
@media (min-width: 769px) { }
```

### 2. 触摸目标尺寸

遵循 iOS 人机界面指南：
- 最小触摸目标：44px × 44px
- 按钮间距：至少 8px

### 3. 防止 iOS 自动缩放

```css
input {
  font-size: 16px; /* iOS 不会自动缩放 16px+ 的输入框 */
}
```

### 4. 安全区域适配

iPhone X+ 需要适配安全区域：

```css
.bottom-nav {
  padding-bottom: env(safe-area-inset-bottom, 8px);
}
```

### 5. 性能优化

- 使用 `will-change` 提示浏览器优化
- 避免过度的重绘和回流
- 使用 CSS 动画替代 JavaScript 动画

---

## 测试建议

### 测试设备

1. **iPhone**
   - iPhone SE（小屏手机）
   - iPhone 12/13/14（标准手机）
   - iPhone 12/13/14 Pro Max（大屏手机）

2. **Android**
   - 小屏手机（≤ 360px）
   - 中屏手机（360-414px）
   - 大屏手机（≥ 414px）

### 测试内容

- ✅ 触摸交互是否流畅
- ✅ 按钮是否容易点击
- ✅ 文字是否清晰可读
- ✅ 滚动是否流畅
- ✅ 横屏显示是否正常
- ✅ iOS 安全区域是否正确

---

## 已知问题

### 1. 浏览器兼容性

- iOS Safari: 完全支持
- Chrome Mobile: 完全支持
- 微信浏览器: 完全支持
- UC 浏览器: 基本支持（部分 CSS3 动画可能不流畅）

### 2. 性能考虑

- 下拉刷新使用 `transform` 而非 `top`，性能更好
- 动画使用 GPU 加速（`transform`, `opacity`）
- 避免在滚动时触发重排

---

## 后续优化计划

1. **懒加载组件**
   - 使用 Intersection Observer API
   - 图片懒加载
   - 路由懒加载

2. **图片优化**
   - WebP 格式支持
   - 响应式图片
   - CDN 加速

3. **PWA 支持**
   - Service Worker
   - 离线访问
   - 添加到主屏幕

4. **手势优化**
   - 左右滑动切换
   - 长按菜单
   - 双击返回顶部

---

## 参考资源

- [iOS 人机界面指南](https://developer.apple.com/design/human-interface-guidelines/)
- [Material Design 移动端指南](https://material.io/design/platform-guidance/android-platform.html)
- [Web 移动端最佳实践](https://web.dev/mobile/)
