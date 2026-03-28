<template>
  <a-layout class="admin-layout">
    <div
      v-if="isMobile && !collapsed"
      class="mobile-overlay"
      @click="collapsed = true"
    />
    <a-layout-sider
      v-model:collapsed="collapsed"
      :width="240"
      theme="dark"
      class="sider"
      :class="{ 'mobile-sidebar': isMobile }"
    >
      <div class="logo">
        <div
          v-if="!collapsed"
          class="logo-content"
        >
          <div class="logo-icon">
            <img
              :src="logoUrl"
              alt="Logo"
              class="logo-image"
            >
          </div>
          <div class="logo-text">
            <span class="logo-kicker">管理工作台</span>
            <a-typography-title :level="4">
              {{ appShortName }}
            </a-typography-title>
            <a-typography-text>{{ appShortNameEn || appShortName }}</a-typography-text>
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
      <div
        v-if="!collapsed"
        class="sider-footer"
      >
        <span class="sider-footer-label">统一管理空间</span>
        <strong>{{ appShortName || '客户服务系统' }}</strong>
        <p>项目、文件、通知与系统配置在同一控制台内完成。</p>
      </div>
    </a-layout-sider>
    <a-layout class="main-layout">
      <div
        v-if="updateInfo.hasUpdate && !updateInfo.dismissed"
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
            <span class="header-eyebrow">管理后台</span>
            <div class="header-title-row">
              <span class="page-title">{{ currentPageTitle }}</span>
              <span class="header-caption">统一查看项目、通知、文件与系统配置</span>
              <span class="header-status-pill">{{ headerStatusLabel }}</span>
            </div>
          </div>

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
      </a-layout-header>
      <a-layout-content class="content">
        <div class="content-wrapper fade-in">
          <router-view />
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

const appShortName = computed(() => appConfigStore.appShortName)
const appShortNameEn = computed(() => appConfigStore.appShortNameEn)
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

const headerStatusLabel = computed(() => {
  if (route.path.includes('/maintenance')) {
    return '系统维护'
  }
  if (route.path.includes('/config') || route.path.includes('/api-keys')) {
    return '受保护配置'
  }
  return '安全会话'
})

const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)
const isMobile = computed(() => windowWidth.value <= 768)

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

  checkForUpdates()

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

const menuItems: MenuProps['items'] = [
  {
    key: '/admin/matters',
    icon: () => h(AppstoreOutlined),
    label: '项目列表',
    title: '项目列表',
  },
  {
    key: '/admin/files',
    icon: () => h(FolderOutlined),
    label: '文件管理',
    title: '文件管理',
  },
  {
    key: '/admin/letter-verifications',
    icon: () => h(SafetyCertificateOutlined),
    label: '函件验证',
    title: '函件验证',
  },
  {
    key: '/admin/notifications',
    icon: () => h(BellOutlined),
    label: '通知记录',
    title: '通知记录',
  },
  {
    key: '/admin/notification-templates',
    icon: () => h(FileSyncOutlined),
    label: '通知模板',
    title: '通知模板',
  },
  {
    key: '/admin/notification-settings',
    icon: () => h(SettingFilled),
    label: '通知配置',
    title: '通知配置',
  },
  {
    key: '/admin/api-keys',
    icon: () => h(ApiOutlined),
    label: 'API密钥管理',
    title: 'API密钥管理',
  },
  {
    key: '/admin/config',
    icon: () => h(SettingFilled),
    label: '系统配置',
    title: '系统配置',
  },
  {
    key: '/admin/maintenance',
    icon: () => h(ToolOutlined),
    label: '系统维护',
    title: '系统维护',
  },
]

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
  background: var(--bg-secondary);
}

.fade-in {
  animation: fadeIn 0.3s ease-out;
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
  background:
    radial-gradient(circle at top right, rgba(63, 109, 153, 0.14), transparent 26%),
    radial-gradient(circle at bottom left, rgba(201, 164, 76, 0.12), transparent 18%),
    var(--bg-secondary);
}

.update-banner {
  background: linear-gradient(90deg, rgba(24, 58, 90, 0.96) 0%, rgba(49, 92, 130, 0.94) 100%);
  color: #fff;
  padding: 12px 28px;
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(196, 160, 92, 0.16);
}

.update-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.update-icon {
  font-size: 20px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.update-text {
  flex: 1;
  font-size: 14px;
}

.update-text strong {
  font-size: 15px;
}

.current-version {
  opacity: 0.8;
  font-size: 12px;
}

.update-banner :deep(.ant-btn) {
  border-color: rgba(255, 255, 255, 0.5);
  color: #fff;
}

.update-banner :deep(.ant-btn:hover) {
  border-color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.update-banner :deep(.ant-btn-primary) {
  background: rgba(255, 255, 255, 0.2);
}

.update-banner :deep(.ant-btn-primary:hover) {
  background: rgba(255, 255, 255, 0.3);
}

.sider {
  background: linear-gradient(180deg, rgba(10, 24, 37, 0.98) 0%, rgba(16, 39, 61, 0.98) 100%) !important;
  box-shadow: none;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  height: 100vh;
  width: 240px;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 100;
  transition: width 0.2s;
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

.sider::-webkit-scrollbar {
  width: 6px;
}

.sider::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.sider::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.sider::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.logo {
  min-height: 108px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(15, 35, 53, 0.92) 0%, rgba(17, 41, 63, 0.72) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.logo::after {
  content: '';
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, rgba(196, 160, 92, 0), rgba(196, 160, 92, 0.44), rgba(196, 160, 92, 0));
}

.logo-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 18px;
  width: 100%;
}

.logo-image {
  width: 48px;
  height: 48px;
  object-fit: contain;
  display: block;
}

.logo-image-collapsed {
  width: 32px;
  height: 32px;
  object-fit: contain;
  display: block;
}

.logo-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.logo-kicker {
  display: inline-block;
  margin-bottom: 6px;
  font-size: 10px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.42);
}

.logo-text :deep(h4) {
  color: #fff !important;
  margin: 0 !important;
  line-height: 1.25 !important;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logo-text :deep(.ant-typography) {
  color: rgba(255, 255, 255, 0.62) !important;
  font-size: 12px;
}

.logo-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.admin-menu {
  flex: 1;
  border-right: none;
  padding: 18px 10px 20px;
  background: transparent !important;
}

.admin-menu :deep(.ant-menu-item) {
  margin: 4px 6px;
  border-radius: 16px;
  height: 50px;
  line-height: 50px;
  transition: all 0.2s ease;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.78) !important;
  font-weight: 500;
}

.admin-menu :deep(.ant-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  transform: translateX(2px);
}

.admin-menu :deep(.ant-menu-item-selected) {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0.08)) !important;
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #fff !important;
}

.admin-menu :deep(.ant-menu-item-selected::after) {
  display: none;
}

.admin-menu :deep(.ant-menu-item-selected .anticon) {
  color: var(--accent-color) !important;
}

.sider-footer {
  margin: 10px 18px 20px;
  padding: 18px 16px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.04));
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.8);
}

.sider-footer-label {
  display: inline-block;
  margin-bottom: 6px;
  color: rgba(255, 255, 255, 0.48);
  font-size: 10px;
  letter-spacing: 0.18em;
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
  line-height: 1.6;
}

.main-layout {
  margin-left: 240px;
  transition: margin-left 0.2s;
  min-height: 100vh;
  background: transparent !important;
}

.admin-layout .sider.ant-layout-sider-collapsed ~ .main-layout {
  margin-left: 80px;
}

.header {
  background: transparent !important;
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 99;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 92px;
  width: min(var(--shell-max-width), calc(100vw - 240px - 2 * var(--shell-gutter)));
  margin: 0 auto;
  padding: 18px 0 10px;
}

.collapse-btn {
  color: var(--text-primary);
  font-size: 18px;
  transition: all 0.2s ease;
  cursor: pointer;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78) !important;
  border: 1px solid var(--border-color);
  backdrop-filter: blur(16px);
}

.collapse-btn:hover {
  color: var(--primary-color);
  background: rgba(255, 255, 255, 0.92) !important;
}

.header-title-block {
  flex: 1;
}

.header-eyebrow {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--text-tertiary);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
}

.header-title-row {
  display: flex;
  align-items: baseline;
  gap: 14px;
  flex-wrap: wrap;
}

.page-title {
  font-size: 30px;
  font-weight: 600;
  color: var(--primary-color-dark);
  line-height: 1.1;
}

.header-caption {
  color: var(--text-secondary);
  font-size: 14px;
}

.header-status-pill {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(196, 160, 92, 0.12);
  border: 1px solid rgba(196, 160, 92, 0.18);
  color: var(--accent-color-deep);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.header-actions {
  margin-left: auto;
}

.header-btn {
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.76) !important;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  cursor: pointer;
  min-height: 46px;
  backdrop-filter: blur(14px);
}

.header-btn:hover {
  color: var(--primary-color);
  background: rgba(255, 255, 255, 0.92) !important;
}

.header-actions :deep(.ant-badge) {
  display: flex;
  align-items: center;
}

.header-actions :deep(.ant-badge .ant-badge-count) {
  box-shadow: none;
}

.header-btn-text {
  font-size: 14px;
}

.content {
  padding: 0 0 24px;
  min-height: calc(100vh - 120px);
}

.content-wrapper {
  width: min(var(--shell-max-width), calc(100vw - 240px - 2 * var(--shell-gutter)));
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--border-color);
  border-radius: 26px;
  padding: 24px;
  min-height: calc(100vh - 160px);
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(22px);
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
}

.mobile-sidebar {
  z-index: 1000;
}

@media (max-width: 1024px) {
  .sider {
    width: 200px !important;
  }

  .main-layout {
    margin-left: 200px;
  }

  .admin-layout :deep(.ant-layout-sider-collapsed) ~ .main-layout {
    margin-left: 80px;
  }

  .header-content,
  .content-wrapper {
    width: min(var(--shell-max-width), calc(100vw - 200px - 2 * 20px));
  }
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

  .header-content {
    width: min(var(--shell-max-width), calc(100vw - 2 * 16px));
    padding: 14px 0 8px;
    min-height: 76px;
  }

  .header-caption,
  .header-btn-text {
    display: none;
  }

  .page-title {
    font-size: 20px;
  }

  .content {
    padding-bottom: 20px;
  }

  .content-wrapper {
    width: min(var(--shell-max-width), calc(100vw - 2 * 16px));
    border-radius: 20px;
    padding: 16px;
    min-height: calc(100vh - 128px);
  }

  .update-banner {
    padding: 10px 16px;
  }
}
</style>
