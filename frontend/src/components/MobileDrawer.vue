<template>
  <a-drawer
    v-model:open="visible"
    placement="right"
    :width="300"
    :closable="false"
    class="mobile-drawer"
    @close="handleClose"
  >
    <template #title>
      <div class="drawer-header">
        <div class="drawer-logo-wrap">
          <img
            :src="logoUrl"
            alt="Logo"
            class="drawer-logo"
          >
        </div>
        <div class="drawer-title">
          <strong>{{ appTitle }}</strong>
          <span>客户门户</span>
        </div>
      </div>
    </template>

    <div class="drawer-content">
      <div class="drawer-user">
        <a-avatar
          :size="40"
          :src="userAvatar"
        >
          {{ displayUserName.charAt(0) }}
        </a-avatar>
        <div class="user-copy">
          <strong>{{ displayUserName }}</strong>
          <span>{{ userRoleLabel }}</span>
        </div>
      </div>

      <div class="drawer-section">
        <div class="drawer-section__title">
          导航
        </div>
        <a-menu
          v-model:selected-keys="selectedKeys"
          mode="inline"
          class="drawer-menu"
          @click="handleMenuClick"
        >
          <a-menu-item key="home">
            <template #icon>
              <HomeOutlined />
            </template>
            首页
          </a-menu-item>
          <a-menu-item key="files">
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
          </a-menu-item>
          <a-menu-item key="help">
            <template #icon>
              <QuestionCircleOutlined />
            </template>
            帮助中心
          </a-menu-item>
          <a-menu-item key="profile">
            <template #icon>
              <SettingOutlined />
            </template>
            个人中心
          </a-menu-item>
        </a-menu>
      </div>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppConfigStore } from '@/stores/appConfig'
import { usePortalVisitorStore } from '@/stores/portalVisitor'
import {
  HomeOutlined,
  FolderOutlined,
  BellOutlined,
  QuestionCircleOutlined,
  SettingOutlined,
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
  if (path === '/' || path === '/portal' || path.startsWith('/matter/')) return ['home']
  if (path.startsWith('/files')) return ['files']
  if (path.startsWith('/notifications')) return ['notification']
  if (path.startsWith('/help')) return ['help']
  if (path.startsWith('/profile')) return ['profile']
  return ['home']
})

function handleMenuClick({ key }: { key: string }) {
  emit('menu-click', key)

  const routes: Record<string, string> = {
    home: '/portal',
    files: '/files',
    notification: '/notifications',
    help: '/help',
    profile: '/profile',
  }

  if (routes[key]) {
    router.push(routes[key])
  }

  handleClose()
}

function handleClose() {
  visible.value = false
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drawer-logo-wrap {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  padding: 6px;
  border: 1px solid var(--border-color);
  background: #ffffff;
}

.drawer-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.drawer-title {
  display: grid;
  gap: 2px;
}

.drawer-title strong {
  font-size: 15px;
  color: var(--text-primary);
}

.drawer-title span {
  color: var(--text-tertiary);
  font-size: 12px;
}

.drawer-content {
  display: grid;
  gap: 16px;
}

.drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  background: #fafafa;
  border: 1px solid var(--border-color-light);
}

.drawer-section {
  display: grid;
  gap: 8px;
}

.drawer-section__title {
  color: var(--text-tertiary);
  font-size: 12px;
}

.user-copy {
  display: grid;
  gap: 2px;
}

.user-copy strong {
  color: var(--text-primary);
}

.user-copy span {
  color: var(--text-secondary);
  font-size: 12px;
}

.drawer-menu {
  border: 1px solid var(--border-color-light);
  border-radius: 6px;
  background: #ffffff;
}

.drawer-menu :deep(.ant-menu-item) {
  border-radius: 4px;
  margin-inline: 4px;
  margin-block: 4px;
  height: 40px;
  line-height: 40px;
}

.drawer-menu :deep(.ant-menu-item-selected) {
  background: #e6f4ff;
  color: var(--primary-color);
}
</style>
