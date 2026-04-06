<template>
  <nav class="mobile-bottom-nav">
    <button
      v-for="item in navItems"
      :key="item.key"
      type="button"
      class="nav-item"
      :class="{ active: activeKey === item.key }"
      @click="handleClick(item)"
    >
      <component
        :is="item.icon"
        class="nav-icon"
      />
      <span class="nav-label">{{ item.label }}</span>
      <span
        v-if="(item.badge ?? 0) > 0"
        class="nav-badge"
      >
        {{ (item.badge ?? 0) > 99 ? '99+' : item.badge }}
      </span>
    </button>
  </nav>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeOutlined,
  FolderOutlined,
  BellOutlined,
  QuestionCircleOutlined,
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
  { key: 'home', label: '首页', icon: HomeOutlined, path: '/portal' },
  { key: 'files', label: '文件', icon: FolderOutlined, path: '/files' },
  { key: 'notifications', label: '消息', icon: BellOutlined, path: '/notifications', badge: 0 },
  { key: 'help', label: '帮助', icon: QuestionCircleOutlined, path: '/help' },
  { key: 'profile', label: '我的', icon: UserOutlined, path: '/profile' },
])

const activeKey = computed(() => {
  const path = route.path
  if (path === '/' || path === '/portal' || path.startsWith('/matter/')) return 'home'
  if (path.startsWith('/files')) return 'files'
  if (path.startsWith('/notifications')) return 'notifications'
  if (path.startsWith('/help')) return 'help'
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
  left: 0;
  right: 0;
  bottom: 0;
  display: none;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 0;
  padding: 8px 8px calc(8px + env(safe-area-inset-bottom, 0px));
  border-top: 1px solid rgba(0, 9, 24, 0.08);
  background: rgba(252, 251, 248, 0.94);
  backdrop-filter: blur(18px);
  box-shadow: 0 -10px 30px rgba(0, 9, 24, 0.08);
  z-index: 1000;
}

.nav-item {
  position: relative;
  display: grid;
  justify-items: center;
  gap: 2px;
  padding: 8px 4px 6px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: var(--text-tertiary);
}

.nav-item.active {
  color: var(--primary-color);
  background: rgba(0, 33, 64, 0.06);
}

.nav-icon {
  font-size: 18px;
}

.nav-label {
  font-size: 11px;
  line-height: 1.2;
}

.nav-badge {
  position: absolute;
  top: 2px;
  right: 14px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: var(--error-color);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  line-height: 16px;
}

@media (max-width: 768px) {
  .mobile-bottom-nav {
    display: grid;
  }
}
</style>
