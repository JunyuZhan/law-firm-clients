<template>
  <a-drawer
    v-model:open="visible"
    placement="right"
    :width="280"
    :closable="false"
    class="mobile-drawer"
    @close="handleClose"
  >
    <template #title>
      <div class="drawer-header">
        <img
          :src="logoUrl"
          alt="Logo"
          class="drawer-logo"
        >
        <div class="drawer-title">
          <span class="drawer-app-name">{{ appTitle }}</span>
        </div>
      </div>
    </template>

    <div class="drawer-content">
      <a-menu
        v-model:selected-keys="selectedKeys"
        mode="inline"
        :theme="menuTheme"
        class="drawer-menu"
        @click="handleMenuClick"
      >
        <a-menu-item key="home">
          <template #icon>
            <HomeOutlined />
          </template>
          首页
        </a-menu-item>

        <a-menu-item key="matter">
          <template #icon>
            <FileTextOutlined />
          </template>
          我的项目
        </a-menu-item>

        <a-menu-item key="file">
          <template #icon>
            <FolderOutlined />
          </template>
          文件中心
        </a-menu-item>

        <a-menu-item key="notification">
          <template #icon>
            <BellOutlined />
          </template>
          消息通知
          <a-badge
            :count="unreadCount"
            :offset="[10, 0]"
          />
        </a-menu-item>

        <a-menu-item key="help">
          <template #icon>
            <QuestionCircleOutlined />
          </template>
          帮助中心
        </a-menu-item>

        <a-menu-divider />

        <a-menu-item key="profile">
          <template #icon>
            <SettingOutlined />
          </template>
          个人中心
        </a-menu-item>

        <a-menu-item key="portal">
          <template #icon>
            <LogoutOutlined />
          </template>
          返回门户
        </a-menu-item>
      </a-menu>

      <div class="drawer-footer">
        <div class="user-info">
          <a-avatar
            :size="40"
            :src="userAvatar"
          >
            {{ displayUserName.charAt(0) }}
          </a-avatar>
          <div class="user-details">
            <div class="user-name">
              {{ displayUserName }}
            </div>
            <div class="user-role">
              {{ userRoleLabel }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppConfigStore } from '@/stores/appConfig'
import { usePortalVisitorStore } from '@/stores/portalVisitor'
import {
  HomeOutlined,
  FileTextOutlined,
  FolderOutlined,
  BellOutlined,
  QuestionCircleOutlined,
  SettingOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'

interface Props {
  open?: boolean
  userName?: string
  userAvatar?: string
  unreadCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  userName: '',
  userAvatar: '',
  unreadCount: 0,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'menu-click', key: string): void
}>()

const router = useRouter()
const route = useRoute()
const appConfigStore = useAppConfigStore()
const portalVisitorStore = usePortalVisitorStore()
const menuTheme = ref<'light' | 'dark'>('dark')

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const logoUrl = computed(() => appConfigStore.logoUrl)
const appTitle = computed(() => appConfigStore.appShortName || appConfigStore.appName || '客户服务系统')
const displayUserName = computed(() => props.userName || portalVisitorStore.displayName || '访客')
const userRoleLabel = computed(() => {
  if (portalVisitorStore.hasProfile) return '客户门户用户'
  if (props.userName) return '门户导航'
  return '访客'
})
const selectedKeys = computed(() => {
  const path = route.path
  if (path === '/' || path === '/portal') return ['home']
  if (path.startsWith('/matters') || path.startsWith('/matter/')) return ['matter']
  if (path.startsWith('/files')) return ['file']
  if (path.startsWith('/notifications')) return ['notification']
  if (path.startsWith('/help')) return ['help']
  if (path.startsWith('/profile')) return ['profile']
  return ['home']
})

const handleMenuClick = ({ key }: { key: string }) => {
  emit('menu-click', key)
  
  const routes: Record<string, string> = {
    home: '/portal',
    matter: '/matters',
    file: '/files',
    notification: '/notifications',
    help: '/help',
    profile: '/profile',
    portal: '/portal',
  }
  
  if (routes[key]) {
    router.push(routes[key])
  }
  
  handleClose()
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0;
}

.drawer-logo {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.drawer-title {
  flex: 1;
}

.drawer-app-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.drawer-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
}

.drawer-menu {
  flex: 1;
  border-right: none;
}

.drawer-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

:deep(.ant-drawer-header) {
  background: linear-gradient(135deg, #0f2438 0%, #1a3a5c 50%, #2c5282 100%);
  padding: 16px 24px;
}

:deep(.ant-drawer-title) {
  padding: 0;
}

:deep(.ant-drawer-body) {
  padding: 0;
  display: flex;
  flex-direction: column;
}

:deep(.ant-menu-item) {
  margin: 4px 8px;
  border-radius: 6px;
}

:deep(.ant-menu-item-selected) {
  background: rgba(212, 175, 55, 0.2);
}

:deep(.ant-menu-divider) {
  margin: 12px 0;
  background: rgba(255, 255, 255, 0.1);
}
</style>
