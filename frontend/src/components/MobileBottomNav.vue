<template>
  <div class="mobile-bottom-nav">
    <div
      v-for="item in navItems"
      :key="item.key"
      class="nav-item"
      :class="{ active: activeKey === item.key }"
      @click="handleClick(item)"
    >
      <component
        :is="item.icon"
        class="nav-icon"
      />
      <span class="nav-label">{{ item.label }}</span>
      <div
        v-if="(item.badge ?? 0) > 0"
        class="nav-badge"
      >
        {{ item.badge ?? 0 > 99 ? '99+' : item.badge }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeOutlined,
  FileTextOutlined,
  FolderOutlined,
  BellOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'

interface NavItem {
  key: string
  label: string
  icon: Component
  path: string
  badge?: number
}

const router = useRouter()
const route = useRoute()

const navItems = computed<NavItem[]>(() => [
  {
    key: 'home',
    label: '首页',
    icon: HomeOutlined,
    path: '/',
  },
  {
    key: 'matter',
    label: '我的项目',
    icon: FileTextOutlined,
    path: '/matters',
  },
  {
    key: 'files',
    label: '文件中心',
    icon: FolderOutlined,
    path: '/files',
  },
  {
    key: 'notifications',
    label: '消息通知',
    icon: BellOutlined,
    path: '/notifications',
    badge: 0,
  },
  {
    key: 'profile',
    label: '个人中心',
    icon: UserOutlined,
    path: '/profile',
  },
])

const activeKey = computed(() => {
  const path = route.path
  if (path === '/' || path.startsWith('/matter/')) return 'home'
  if (path.startsWith('/matters')) return 'matter'
  if (path.startsWith('/files')) return 'files'
  if (path.startsWith('/notifications')) return 'notifications'
  if (path.startsWith('/profile')) return 'profile'
  return 'home'
})

function handleClick(item: NavItem) {
  if (activeKey.value === item.key) {
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }
  router.push(item.path)
}
</script>

<style scoped>
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 8px 0;
  padding-bottom: env(safe-area-inset-bottom, 8px);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.98) 100%);
  backdrop-filter: blur(20px);
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  border-top: 1px solid rgba(212, 175, 55, 0.2);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px 12px;
  min-width: 60px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin: 0 4px;
}

.nav-item:active {
  background: rgba(212, 175, 55, 0.15);
}

.nav-item.active {
  color: var(--primary-color);
}

.nav-item:not(.active) {
  color: #666;
}

.nav-icon {
  font-size: 20px;
  margin-bottom: 4px;
  transition: transform 0.2s ease;
}

.nav-item.active .nav-icon {
  transform: translateY(-2px);
}

.nav-label {
  font-size: 11px;
  font-weight: 500;
  line-height: 1.2;
}

.nav-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  background: var(--error-color, #ff4d4f);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  line-height: 16px;
  text-align: center;
  border-radius: 8px;
  animation: badgePulse 0.3s ease;
}

@keyframes badgePulse {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 移动端设备优化 */
@media (max-width: 768px) {
  .mobile-bottom-nav {
    display: flex;
  }
}

/* 桌面端隐藏 */
@media (min-width: 769px) {
  .mobile-bottom-nav {
    display: none;
  }
}
</style>
