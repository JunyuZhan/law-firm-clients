<template>
  <a-layout
    class="admin-layout"
    :style="layoutStyle"
  >
    <div
      v-if="isMobile && !collapsed"
      class="mobile-overlay"
      @click="collapsed = true"
    />
    <a-layout-sider
      v-model:collapsed="collapsed"
      :width="expandedSidebarWidth"
      theme="dark"
      class="sider"
      :class="{ 'mobile-sidebar': isMobile }"
    >
      <div class="logo">
        <div
          v-if="!collapsed"
          class="logo-content"
        >
          <div class="logo-brand-mark">
            <img
              :src="logoUrl"
              alt="Logo"
              class="logo-image"
            >
          </div>
          <div class="logo-text">
            <strong>{{ appShortName || '客户服务系统' }}</strong>
          </div>
        </div>
        <div
          v-else
          class="logo-collapsed"
        >
          <img
            :src="logoCollapsedUrl"
            alt="Logo"
            class="logo-image-collapsed"
          >
        </div>
      </div>
      <a-menu
        v-model:selected-keys="selectedKeys"
        theme="dark"
        mode="inline"
        :items="menuItems"
        class="admin-menu"
        @click="handleMenuClick"
      />
    </a-layout-sider>
    <a-layout class="main-layout">
      <div
        v-if="isSuperAdmin && updateInfo.hasUpdate && !updateInfo.dismissed"
        class="update-banner"
      >
        <div class="update-content">
          <UpCircleOutlined class="update-icon" />
          <span class="update-text">
            发现新版本 <strong>v{{ updateInfo.latestVersion }}</strong>
            <span class="current-version">（当前 v{{ updateInfo.currentVersion }}）</span>
          </span>
          <a-space>
            <a-button
              size="small"
              type="primary"
              ghost
              @click="viewReleaseNotes"
            >
              查看更新
            </a-button>
            <a-button
              size="small"
              type="primary"
              @click="goToMaintenance"
            >
              前往升级
            </a-button>
            <a-button
              size="small"
              type="text"
              @click="dismissUpdate"
            >
              稍后提醒
            </a-button>
            <a-button
              size="small"
              type="text"
              @click="ignoreVersion"
            >
              忽略此版本
            </a-button>
          </a-space>
        </div>
      </div>

      <a-layout-header class="header">
        <div class="header-content">
          <div class="header-left-cluster">
            <a-button
              type="text"
              class="collapse-btn"
              @click="collapsed = !collapsed"
            >
              <template #icon>
                <MenuUnfoldOutlined v-if="collapsed" />
                <MenuFoldOutlined v-else />
              </template>
            </a-button>
            <div class="header-title-block">
              <span class="page-title">{{ currentPageTitle }}</span>
            </div>
          </div>

          <div class="header-side header-side-right">
            <a-space
              class="header-actions"
              size="middle"
            >
              <a-badge :count="0">
                <a-button
                  type="text"
                  class="header-btn"
                >
                  <template #icon>
                    <BellOutlined />
                  </template>
                </a-button>
              </a-badge>
              <a-dropdown>
                <a-button
                  type="text"
                  class="header-btn"
                >
                  <template #icon>
                    <UserOutlined />
                  </template>
                  <span class="header-btn-text">{{ authStore.getCurrentUserInfo()?.realName || authStore.getCurrentUserInfo()?.username || '管理员' }}</span>
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="router.push('/admin/profile')">
                      <template #icon>
                        <UserOutlined />
                      </template>
                      个人中心
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="handleLogout">
                      <template #icon>
                        <LogoutOutlined />
                      </template>
                      退出登录
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </div>
        </div>
      </a-layout-header>
      <a-layout-content
        id="main-content"
        class="content"
        tabindex="-1"
      >
        <div class="content-shell fade-in">
          <div class="content-wrapper">
            <router-view />
          </div>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch, computed, h, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  UserOutlined,
  LogoutOutlined,
  AppstoreOutlined,
  BellOutlined,
  ApiOutlined,
  SettingFilled,
  FileSyncOutlined,
  FolderOutlined,
  ToolOutlined,
  UpCircleOutlined,
  SafetyCertificateOutlined,
  InfoCircleOutlined,
  CompassOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { Modal } from 'ant-design-vue'
import request from '@/api/request'
import type { MenuProps } from 'ant-design-vue'
import { LOGO_COLLAPSED_URL } from '@/config/app'
import { useAppConfigStore } from '@/stores/appConfig'
import logger from '@/utils/logger'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const appConfigStore = useAppConfigStore()
const isSuperAdmin = computed(() => !!authStore.getCurrentUserInfo()?.superAdmin)

const appShortName = computed(() => appConfigStore.appShortName)
const logoUrl = computed(() => appConfigStore.logoUrl)
const logoCollapsedUrl = ref(LOGO_COLLAPSED_URL)

const collapsed = ref(false)
const selectedKeys = ref<string[]>([])

const updateInfo = ref({
  hasUpdate: false,
  currentVersion: '',
  latestVersion: '',
  releaseNotes: '',
  releaseUrl: '',
  dismissed: false,
})

const currentPageTitle = computed(() => {
  const matched = route.matched.find(r => r.meta?.title)
  return matched?.meta?.title as string || '管理后台'
})

const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)
const isMobile = computed(() => windowWidth.value <= 768)
const expandedSidebarWidth = computed(() => (windowWidth.value <= 1024 ? 200 : 240))
const currentSidebarWidth = computed(() => {
  if (isMobile.value) {
    return 0
  }
  return collapsed.value ? 80 : expandedSidebarWidth.value
})
const layoutStyle = computed(() => ({
  '--sidebar-width': `${currentSidebarWidth.value}px`,
  '--sidebar-expanded-width': `${expandedSidebarWidth.value}px`,
}))

let resizeHandler: (() => void) | null = null

const IDLE_TIMEOUT = 30 * 60 * 1000
const WARNING_TIME = 5 * 60 * 1000
const RESET_INTERVAL = 5000
let idleTimer: ReturnType<typeof setTimeout> | null = null
let warningTimer: ReturnType<typeof setTimeout> | null = null
let lastResetTime = 0
let warningModal: ReturnType<typeof Modal.warning> | null = null
let isPageVisible = true

function resetIdleTimer() {
  const now = Date.now()

  if (now - lastResetTime < RESET_INTERVAL) {
    return
  }

  lastResetTime = now

  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }

  if (warningModal) {
    warningModal.destroy()
    warningModal = null
  }

  if (!isPageVisible) {
    return
  }

  warningTimer = setTimeout(() => {
    showIdleWarning()
  }, IDLE_TIMEOUT - WARNING_TIME)

  idleTimer = setTimeout(() => {
    handleAutoLogout()
  }, IDLE_TIMEOUT)
}

function showIdleWarning() {
  if (warningModal) {
    return
  }

  const remainingMinutes = Math.ceil(WARNING_TIME / 60000)
  const idleMinutes = Math.floor((IDLE_TIMEOUT - WARNING_TIME) / 60000)
  warningModal = Modal.warning({
    title: '会话即将过期',
    content: `您已 ${idleMinutes} 分钟无操作，系统将在 ${remainingMinutes} 分钟后自动登出。请点击"继续使用"保持登录状态。`,
    okText: '继续使用',
    cancelText: '立即登出',
    onOk: () => {
      resetIdleTimer()
    },
    onCancel: () => {
      handleAutoLogout()
    },
  })
}

function handleAutoLogout() {
  if (warningModal) {
    warningModal.destroy()
    warningModal = null
  }

  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }

  message.warning('由于长时间无操作，系统已自动登出')
  authStore.logout()
  router.push('/admin/login')
}

function handleUserActivity() {
  resetIdleTimer()
}

function handleVisibilityChange() {
  isPageVisible = !document.hidden
  if (isPageVisible) {
    if (idleTimer) {
      clearTimeout(idleTimer)
      idleTimer = null
    }
    if (warningTimer) {
      clearTimeout(warningTimer)
      warningTimer = null
    }
    if (warningModal) {
      warningModal.destroy()
      warningModal = null
    }
    lastResetTime = 0
    resetIdleTimer()
  } else {
    if (idleTimer) {
      clearTimeout(idleTimer)
      idleTimer = null
    }
    if (warningTimer) {
      clearTimeout(warningTimer)
      warningTimer = null
    }
    if (warningModal) {
      warningModal.destroy()
      warningModal = null
    }
  }
}

onMounted(() => {
  resizeHandler = () => {
    windowWidth.value = window.innerWidth

    if (window.innerWidth > 768) {
      collapsed.value = false
    } else {
      collapsed.value = true
    }
  }
  window.addEventListener('resize', resizeHandler)
  if (isMobile.value) {
    collapsed.value = true
  }

  if (authStore.isAuthenticated && !authStore.getCurrentUserInfo()) {
    authStore.fetchCurrentUser().catch(() => {
      authStore.clearAuth()
      router.push('/admin/login')
    })
  }

  if (isSuperAdmin.value) {
    checkForUpdates()
  }

  if (authStore.isAuthenticated) {
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click']
    events.forEach(event => {
      document.addEventListener(event, handleUserActivity, { passive: true })
    })

    document.addEventListener('visibilitychange', handleVisibilityChange)
    resetIdleTimer()
  }
})

async function checkForUpdates() {
  const dismissed = sessionStorage.getItem('update_dismissed')
  if (dismissed) {
    updateInfo.value.dismissed = true
    return
  }

  try {
    const res = await request.get('/api/admin/system/version/check')
    if (res.success && res.data) {
      const data = res.data as {
        currentVersion?: string
        latestVersion?: string
        hasUpdate?: boolean
        releaseNotes?: string
        releaseUrl?: string
      }
      updateInfo.value.currentVersion = data.currentVersion || ''
      updateInfo.value.latestVersion = data.latestVersion || ''
      updateInfo.value.hasUpdate = data.hasUpdate || false
      updateInfo.value.releaseNotes = data.releaseNotes || ''
      updateInfo.value.releaseUrl = data.releaseUrl || ''
    }
  } catch (e) {
    logger.warn('版本检查失败', e)
  }
}

function viewReleaseNotes() {
  if (updateInfo.value.releaseUrl) {
    window.open(updateInfo.value.releaseUrl, '_blank')
  } else if (updateInfo.value.releaseNotes) {
    Modal.info({
      title: `v${updateInfo.value.latestVersion || '未知版本'} 更新内容`,
      content: updateInfo.value.releaseNotes,
      width: 600,
    })
  } else {
    message.info('暂无更新说明')
  }
}

function goToMaintenance() {
  router.push('/admin/maintenance')
}

function dismissUpdate() {
  updateInfo.value.dismissed = true
  sessionStorage.setItem('update_dismissed', 'true')
}

async function ignoreVersion() {
  if (!updateInfo.value.latestVersion) {
    message.warning('无法获取版本号')
    return
  }
  try {
    await request.post(`/api/admin/system/version/ignore?version=${updateInfo.value.latestVersion}`)
    updateInfo.value.hasUpdate = false
    message.success('已忽略此版本，下次有新版本时会再次提醒')
  } catch {
    message.error('操作失败')
  }
}

onUnmounted(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  if (stopWatch) {
    stopWatch()
  }

  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }

  if (warningModal) {
    warningModal.destroy()
    warningModal = null
  }

  const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click']
  events.forEach(event => {
    document.removeEventListener(event, handleUserActivity)
  })

  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

const rawMenuItems = [
  {
    key: '/admin/matters',
    icon: () => h(AppstoreOutlined),
    label: '项目列表',
    title: '项目列表',
    superAdminOnly: true,
  },
  {
    key: '/admin/files',
    icon: () => h(FolderOutlined),
    label: '文件管理',
    title: '文件管理',
    superAdminOnly: true,
  },
  {
    key: '/admin/setup',
    icon: () => h(CompassOutlined),
    label: '首次初始化',
    title: '首次初始化',
    superAdminOnly: true,
  },
  {
    key: '/admin/system-info',
    icon: () => h(InfoCircleOutlined),
    label: '系统信息',
    title: '系统信息',
    superAdminOnly: true,
  },
  {
    key: '/admin/letter-verifications',
    icon: () => h(SafetyCertificateOutlined),
    label: '函件验证',
    title: '函件验证',
    superAdminOnly: true,
  },
  {
    key: '/admin/notifications',
    icon: () => h(BellOutlined),
    label: '通知记录',
    title: '通知记录',
    superAdminOnly: true,
  },
  {
    key: '/admin/notification-templates',
    icon: () => h(FileSyncOutlined),
    label: '通知模板',
    title: '通知模板',
    superAdminOnly: true,
  },
  {
    key: '/admin/notification-settings',
    icon: () => h(SettingFilled),
    label: '通知配置',
    title: '通知配置',
    superAdminOnly: true,
  },
  {
    key: '/admin/api-keys',
    icon: () => h(ApiOutlined),
    label: 'API密钥管理',
    title: 'API密钥管理',
    superAdminOnly: true,
  },
  {
    key: '/admin/config',
    icon: () => h(SettingFilled),
    label: '系统配置',
    title: '系统配置',
    superAdminOnly: true,
  },
  {
    key: '/admin/maintenance',
    icon: () => h(ToolOutlined),
    label: '系统维护',
    title: '系统维护',
    superAdminOnly: true,
  },
]

const menuItems = computed<MenuProps['items']>(() => rawMenuItems.filter((item) => {
  return !item.superAdminOnly || isSuperAdmin.value
}))

const stopWatch = watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  },
  { immediate: true },
)

function handleMenuClick({ key }: { key: string }) {
  router.push(key as string)
}

async function handleLogout() {
  try {
    await authStore.logout()
    message.success('已退出登录')
    router.push('/admin/login')
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '退出登录失败'
    message.error(errorMessage)
  }
}
</script>

<style>
.admin-layout {
  min-height: 100vh;
  background: transparent;
}

.fade-in {
  animation: fadeIn 0.24s linear;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

<style scoped>
.admin-layout {
  position: relative;
  background: transparent;
}

.update-banner {
  margin: calc(var(--header-height) + 16px) 24px 0;
  padding: 14px 18px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(30, 64, 175, 0.18);
  background: rgba(239, 246, 255, 0.95);
  box-shadow: var(--shadow-xs);
}

.update-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.update-icon {
  font-size: 16px;
  color: var(--accent-color-deep);
}

.update-text {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
}

.update-text strong {
  font-size: 14px;
}

.current-version {
  color: var(--text-secondary);
  font-size: 12px;
}

.update-banner :deep(.ant-btn) {
  border-color: rgba(15, 23, 42, 0.12);
  color: var(--text-primary);
}

.update-banner :deep(.ant-btn:hover) {
  border-color: var(--border-color);
  background: var(--lex-surface-strong);
}

.update-banner :deep(.ant-btn-primary) {
  color: #fff;
}

.sider {
  background: linear-gradient(180deg, var(--lex-surface-dark) 0%, var(--lex-surface-dark-muted) 100%) !important;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  height: 100vh;
  width: var(--sidebar-expanded-width);
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 100;
  transition: width 0.24s ease;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.sider.ant-layout-sider-collapsed {
  width: 80px;
}

.sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.logo {
  min-height: 78px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  overflow: hidden;
}

.logo-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 16px 14px;
  width: 100%;
}

.logo-brand-mark {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(245, 158, 11, 0.12));
  border: 1px solid rgba(255, 255, 255, 0.12);
  flex-shrink: 0;
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.logo-image-collapsed {
  width: 24px;
  height: 24px;
  object-fit: contain;
  display: block;
}

.logo-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.logo-text strong {
  display: block;
  color: #fff;
  font-size: 16px;
  line-height: 1.2;
}

.logo-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px 16px;
}

.admin-menu {
  flex: 1;
  border-right: none;
  padding: 12px 8px 10px;
  background: transparent !important;
}

.admin-menu :deep(.ant-menu-item) {
  margin: 4px 2px;
  border-radius: 6px;
  height: 42px;
  line-height: 42px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.78) !important;
  font-weight: 500;
}

.admin-menu :deep(.ant-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
}

.admin-menu :deep(.ant-menu-item-selected) {
  background: rgba(59, 130, 246, 0.22) !important;
  border: 0;
  color: #fff !important;
  box-shadow: inset 0 0 0 1px rgba(147, 197, 253, 0.35);
}

.admin-menu :deep(.ant-menu-item-selected::after) {
  display: none;
}

.admin-menu :deep(.ant-menu-item-selected .anticon) {
  color: #fff !important;
}

.sider-footer {
  margin: 12px;
  padding: 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.8);
}

.sider-footer-label {
  display: inline-block;
  margin-bottom: 6px;
  color: rgba(255, 255, 255, 0.48);
  font-size: 10px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.sider-footer strong {
  display: block;
  margin-bottom: 6px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.sider-footer p {
  margin: 0;
  color: rgba(255, 255, 255, 0.64);
  font-size: 12px;
  line-height: 1.7;
}

.main-layout {
  margin-left: var(--sidebar-width);
  transition: margin-left 0.24s ease;
  min-height: 100vh;
  background: transparent !important;
}

.header {
  background: rgba(255, 255, 255, 0.88) !important;
  backdrop-filter: blur(var(--backdrop-blur));
  padding: 0;
  position: fixed;
  top: 0;
  left: var(--sidebar-width);
  right: 0;
  z-index: 99;
  border-bottom: 1px solid var(--border-color);
  transition: left 0.24s ease;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: var(--header-height);
  width: 100%;
  padding: 0 24px;
}

.header-side {
  display: flex;
  align-items: center;
  min-width: 0;
}

.header-left-cluster {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.header-side-right {
  justify-content: flex-end;
}

.collapse-btn {
  color: var(--lex-primary-soft);
  font-size: 16px;
  cursor: pointer;
  width: 42px;
  height: 42px;
  border-radius: 999px;
  background: var(--lex-bg-muted) !important;
  border: 1px solid var(--border-color);
}

.collapse-btn:hover {
  color: var(--lex-primary);
  background: var(--lex-surface-strong) !important;
}

.header-title-block {
  min-width: 0;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.page-title {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 24px;
  font-weight: 700;
  color: var(--lex-primary);
  line-height: 1.3;
  font-family: var(--font-heading);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-btn {
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  background: var(--lex-bg-muted) !important;
  border: 1px solid var(--border-color);
  border-radius: 999px;
  cursor: pointer;
}

.header-btn:hover {
  color: var(--lex-primary-soft);
  background: var(--lex-surface-strong) !important;
}

.header-actions :deep(.ant-badge) {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}

.header-actions :deep(.ant-badge .ant-badge-count) {
  box-shadow: none;
}

.header-actions :deep(.ant-badge .ant-badge-dot),
.header-actions :deep(.ant-badge .ant-badge-count) {
  transform: translate(2px, -2px);
}

.header-actions :deep(.ant-btn > .anticon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.header-btn-text {
  font-size: 14px;
}

.content {
  padding: calc(var(--header-height) + 16px) 24px 24px;
  min-height: 100vh;
}

.content-shell {
  width: 100%;
  margin: 0;
  min-height: calc(100vh - var(--header-height) - 40px);
}

.content-wrapper {
  background: var(--lex-surface-strong);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
  min-height: calc(100vh - var(--header-height) - 40px);
  box-shadow: var(--shadow-sm);
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.45);
  z-index: 999;
}

.mobile-sidebar {
  z-index: 1000;
}

@media (max-width: 768px) {
  .main-layout {
    margin-left: 0 !important;
  }

  .sider {
    position: fixed !important;
    left: 0;
    top: 0;
    bottom: 0;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .sider.mobile-sidebar:not(.ant-layout-sider-collapsed) {
    transform: translateX(0) !important;
  }

  .sider.mobile-sidebar.ant-layout-sider-collapsed {
    transform: translateX(-100%) !important;
  }

  .header {
    left: 0;
  }

  .header-content {
    min-height: 64px;
    padding: 0 16px;
    align-items: center;
  }

  .header-btn-text {
    display: none;
  }

  .page-title {
    font-size: 20px;
  }

  .content {
    padding: calc(var(--header-height) + 12px) 16px 16px;
  }

  .content-wrapper {
    padding: 16px;
    min-height: calc(100vh - var(--header-height) - 28px);
  }

  .update-banner {
    margin: calc(var(--header-height) + 12px) 16px 0;
    padding: 12px 14px;
    border-radius: 8px;
  }
}
</style>
