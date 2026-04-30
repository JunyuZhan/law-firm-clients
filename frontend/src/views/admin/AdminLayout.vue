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
                      {{ UI_TEXTS.logout }}
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
          <PageContainer class="content-wrapper">
            <router-view />
          </PageContainer>
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
import PageContainer from '@/components/PageContainer.vue'
import { UI_ACTION_TEXTS, UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_LAYOUT_TEXTS } from '@/constants/adminTexts'

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
const expandedSidebarWidth = computed(() => 220)
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
    okText: UI_ACTION_TEXTS.continueUsing,
    cancelText: UI_ACTION_TEXTS.logoutNow,
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

  message.warning(UI_FEEDBACK_TEXTS.autoLogout)
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
      title: `v${updateInfo.value.latestVersion || ADMIN_LAYOUT_TEXTS.update.unknownVersion}${ADMIN_LAYOUT_TEXTS.update.releaseNotesTitleSuffix}`,
      content: updateInfo.value.releaseNotes,
      width: 600,
    })
  } else {
    message.info(ADMIN_LAYOUT_TEXTS.update.emptyReleaseNotes)
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
    message.warning(ADMIN_LAYOUT_TEXTS.update.missingVersion)
    return
  }
  try {
    await request.post(`/api/admin/system/version/ignore?version=${updateInfo.value.latestVersion}`)
    updateInfo.value.hasUpdate = false
    message.success(ADMIN_LAYOUT_TEXTS.update.ignoredVersion)
  } catch {
    message.error(UI_FEEDBACK_TEXTS.operationFailed)
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
    label: UI_TEXTS.matterManagement,
    title: UI_TEXTS.matterManagement,
    superAdminOnly: true,
  },
  {
    key: '/admin/files',
    icon: () => h(FolderOutlined),
    label: ADMIN_LAYOUT_TEXTS.menu.fileManagement,
    title: ADMIN_LAYOUT_TEXTS.menu.fileManagement,
    superAdminOnly: true,
  },
  {
    key: '/admin/setup',
    icon: () => h(CompassOutlined),
    label: ADMIN_LAYOUT_TEXTS.menu.initialSetup,
    title: ADMIN_LAYOUT_TEXTS.menu.initialSetup,
    superAdminOnly: true,
  },
  {
    key: '/admin/system-info',
    icon: () => h(InfoCircleOutlined),
    label: ADMIN_LAYOUT_TEXTS.menu.systemInfo,
    title: ADMIN_LAYOUT_TEXTS.menu.systemInfo,
    superAdminOnly: true,
  },
  {
    key: '/admin/letter-verifications',
    icon: () => h(SafetyCertificateOutlined),
    label: ADMIN_LAYOUT_TEXTS.menu.letterVerification,
    title: ADMIN_LAYOUT_TEXTS.menu.letterVerification,
    superAdminOnly: true,
  },
  {
    key: '/admin/notifications',
    icon: () => h(BellOutlined),
    label: UI_TEXTS.progressNoticeCenter,
    title: UI_TEXTS.progressNoticeCenter,
    superAdminOnly: true,
  },
  {
    key: '/admin/notification-templates',
    icon: () => h(FileSyncOutlined),
    label: ADMIN_LAYOUT_TEXTS.menu.notificationTemplates,
    title: ADMIN_LAYOUT_TEXTS.menu.notificationTemplates,
    superAdminOnly: true,
  },
  {
    key: '/admin/notification-settings',
    icon: () => h(SettingFilled),
    label: UI_TEXTS.autoRetryStrategy,
    title: UI_TEXTS.autoRetryStrategy,
    superAdminOnly: true,
  },
  {
    key: '/admin/api-keys',
    icon: () => h(ApiOutlined),
    label: ADMIN_LAYOUT_TEXTS.menu.apiKeyManagement,
    title: ADMIN_LAYOUT_TEXTS.menu.apiKeyManagement,
    superAdminOnly: true,
  },
  {
    key: '/admin/config',
    icon: () => h(SettingFilled),
    label: ADMIN_LAYOUT_TEXTS.menu.systemConfig,
    title: ADMIN_LAYOUT_TEXTS.menu.systemConfig,
    superAdminOnly: true,
  },
  {
    key: '/admin/maintenance',
    icon: () => h(ToolOutlined),
    label: ADMIN_LAYOUT_TEXTS.menu.systemMaintenance,
    title: ADMIN_LAYOUT_TEXTS.menu.systemMaintenance,
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
    message.success(UI_FEEDBACK_TEXTS.logoutSuccess)
    router.push('/admin/login')
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_LAYOUT_TEXTS.feedback.logoutFailed
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
  min-height: 100vh;
}

.update-banner {
  margin: 80px 24px 0;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #bae0ff;
  background: #e6f4ff;
}

.update-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.update-icon {
  font-size: 16px;
  color: #1677ff;
}

.update-text {
  flex: 1;
  font-size: 14px;
  color: #1f2937;
}

.current-version {
  color: #6b7280;
  font-size: 12px;
}

.sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  height: 100vh;
  z-index: 100;
  box-shadow: 2px 0 8px 0 rgba(29,35,41,.05);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 0 16px;
  background: #001529;
}

.logo-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.logo-brand-mark {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-image, .logo-image-collapsed {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.logo-text strong {
  display: block;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.logo-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-menu {
  flex: 1;
  border-right: none;
}

.main-layout {
  margin-left: var(--sidebar-width);
  transition: margin-left 0.2s;
  min-height: 100vh;
}

.header {
  padding: 0;
  position: fixed;
  top: 0;
  left: var(--sidebar-width);
  right: 0;
  z-index: 99;
  height: 64px;
  line-height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  transition: left 0.2s;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 24px 0 0;
}

.header-left-cluster {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
  height: 64px;
  border-radius: 0;
}

.collapse-btn:hover {
  color: #1677ff;
  background: rgba(0,0,0,.025);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  align-items: center;
}

.header-btn {
  height: 48px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5563;
}

.header-btn:hover {
  background: rgba(0,0,0,.025);
}

.content {
  padding: 88px 24px 24px;
  min-height: 100vh;
}

.content-wrapper {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  min-height: calc(100vh - 112px);
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 99;
}

@media (max-width: 768px) {
  .main-layout {
    margin-left: 0 !important;
  }

  .sider {
    transform: translateX(-100%);
    transition: transform 0.3s;
  }

  .sider.mobile-sidebar:not(.ant-layout-sider-collapsed) {
    transform: translateX(0);
  }

  .header {
    left: 0;
  }

  .content {
    padding: 76px 12px 12px;
  }

  .content-wrapper {
    padding: 16px;
    min-height: calc(100vh - 88px);
  }
}
</style>
