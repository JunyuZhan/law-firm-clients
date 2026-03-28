<template>
  <a-layout class="admin-layout">
    <!-- 移动端遮罩层 -->
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
      <div class="logo gradient-bg-primary">
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
            <a-typography-title
              :level="4"
              style="color: white; margin: 0"
            >
              {{ appShortName }}
            </a-typography-title>
            <a-typography-text style="color: rgba(255, 255, 255, 0.65); font-size: 12px">
              {{ appShortNameEn }}
            </a-typography-text>
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
      <!-- 版本更新提示横幅 -->
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
          <div class="header-title">
            <span class="page-title">{{ currentPageTitle }}</span>
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

// 从全局配置 store 获取配置
const appShortName = computed(() => appConfigStore.appShortName)
const appShortNameEn = computed(() => appConfigStore.appShortNameEn)
const logoUrl = computed(() => appConfigStore.logoUrl)
const logoCollapsedUrl = ref(LOGO_COLLAPSED_URL)  // 折叠Logo暂用默认值

const collapsed = ref(false)
const selectedKeys = ref<string[]>([])

// 版本更新信息
const updateInfo = ref({
  hasUpdate: false,
  currentVersion: '',
  latestVersion: '',
  releaseNotes: '',
  releaseUrl: '',
  dismissed: false,
})

// 当前页面标题
const currentPageTitle = computed(() => {
  const matched = route.matched.find(r => r.meta?.title)
  return matched?.meta?.title as string || '管理后台'
})

// 窗口宽度（响应式）
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)

// 检测是否为移动端
const isMobile = computed(() => windowWidth.value <= 768)

// 监听窗口大小变化
let resizeHandler: (() => void) | null = null

// 无活动自动登出相关
const IDLE_TIMEOUT = 30 * 60 * 1000 // 30分钟无活动自动登出
const WARNING_TIME = 5 * 60 * 1000 // 提前5分钟警告
const RESET_INTERVAL = 5000 // 5秒内不重复重置计时器（避免频繁重置）
let idleTimer: ReturnType<typeof setTimeout> | null = null
let warningTimer: ReturnType<typeof setTimeout> | null = null
let lastResetTime = 0 // 上次重置计时器的时间
let warningModal: ReturnType<typeof Modal.warning> | null = null
let isPageVisible = true

// 重置无活动计时器
function resetIdleTimer() {
  const now = Date.now()
  
  // 如果距离上次重置时间太短，跳过（避免频繁重置）
  if (now - lastResetTime < RESET_INTERVAL) {
    return
  }
  
  lastResetTime = now
  
  // 清除现有计时器
  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }
  
  // 关闭警告弹窗
  if (warningModal) {
    warningModal.destroy()
    warningModal = null
  }
  
  // 如果页面不可见，不启动计时器
  if (!isPageVisible) {
    return
  }
  
  // 设置警告计时器（提前5分钟警告）
  warningTimer = setTimeout(() => {
    showIdleWarning()
  }, IDLE_TIMEOUT - WARNING_TIME)
  
  // 设置自动登出计时器
  idleTimer = setTimeout(() => {
    handleAutoLogout()
  }, IDLE_TIMEOUT)
}

// 显示无活动警告
function showIdleWarning() {
  if (warningModal) {
    return // 已经显示警告
  }
  
  const remainingMinutes = Math.ceil(WARNING_TIME / 60000)
  const idleMinutes = Math.floor((IDLE_TIMEOUT - WARNING_TIME) / 60000)
  warningModal = Modal.warning({
    title: '会话即将过期',
    content: `您已 ${idleMinutes} 分钟无操作，系统将在 ${remainingMinutes} 分钟后自动登出。请点击"继续使用"保持登录状态。`,
    okText: '继续使用',
    cancelText: '立即登出',
    onOk: () => {
      // resetIdleTimer 内部会关闭弹窗并重置 warningModal
      resetIdleTimer()
    },
    onCancel: () => {
      // handleAutoLogout 内部会关闭弹窗并重置 warningModal
      handleAutoLogout()
    },
  })
}

// 自动登出
function handleAutoLogout() {
  // 关闭警告弹窗
  if (warningModal) {
    warningModal.destroy()
    warningModal = null
  }
  
  // 清除计时器
  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }
  
  // 登出
  message.warning('由于长时间无操作，系统已自动登出')
  authStore.logout()
  router.push('/admin/login')
}

// 处理用户活动
function handleUserActivity() {
  // 重置计时器（函数内部会检查是否频繁重置）
  resetIdleTimer()
}

// 页面可见性变化处理
function handleVisibilityChange() {
  isPageVisible = !document.hidden
  if (isPageVisible) {
    // 页面变为可见，强制重置计时器（忽略 RESET_INTERVAL 限制）
    // 清除现有计时器
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
    // 强制重置（跳过 RESET_INTERVAL 检查）
    lastResetTime = 0
    resetIdleTimer()
  } else {
    // 页面变为隐藏，清除计时器（但不重置最后活动时间）
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
    // 更新窗口宽度（触发 isMobile 重新计算）
    windowWidth.value = window.innerWidth
    
    // 窗口大小变化时，如果从移动端切换到桌面端，自动展开侧边栏
    if (window.innerWidth > 768) {
      collapsed.value = false
    } else {
      // 切换到移动端时，默认收起
      collapsed.value = true
    }
  }
  window.addEventListener('resize', resizeHandler)
  // 移动端默认收起侧边栏
  if (isMobile.value) {
    collapsed.value = true
  }
  
  // 如果store中没有用户信息，尝试从API获取
  if (authStore.isAuthenticated && !authStore.getCurrentUserInfo()) {
    authStore.fetchCurrentUser().catch(() => {
      // 如果获取失败，可能是Token已过期，清除认证信息
      authStore.clearAuth()
      router.push('/admin/login')
    })
  }
  
  // 检查版本更新
  checkForUpdates()
  
  // 初始化无活动自动登出（仅在已登录时）
  if (authStore.isAuthenticated) {
    // 监听用户活动事件
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click']
    events.forEach(event => {
      document.addEventListener(event, handleUserActivity, { passive: true })
    })
    
    // 监听页面可见性变化
    document.addEventListener('visibilitychange', handleVisibilityChange)
    
    // 启动无活动计时器
    resetIdleTimer()
  }
})

// 检查版本更新
async function checkForUpdates() {
  // 检查是否已经在本次会话中关闭了提示
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

// 查看更新内容
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

// 前往系统维护页面
function goToMaintenance() {
  router.push('/admin/maintenance')
}

// 稍后提醒（本次会话不再显示）
function dismissUpdate() {
  updateInfo.value.dismissed = true
  sessionStorage.setItem('update_dismissed', 'true')
}

// 忽略此版本
async function ignoreVersion() {
  if (!updateInfo.value.latestVersion) {
    message.warning('无法获取版本号')
    return
  }
  try {
    await request.post(`/api/admin/system/version/ignore?version=${updateInfo.value.latestVersion}`)
    updateInfo.value.hasUpdate = false
    message.success('已忽略此版本，下次有新版本时会再次提醒')
  } catch (e) {
    message.error('操作失败')
  }
}

onUnmounted(() => {
  // 清理事件监听器
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  // 停止 watch
  if (stopWatch) {
    stopWatch()
  }
  
  // 清除无活动计时器
  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
  if (warningTimer) {
    clearTimeout(warningTimer)
    warningTimer = null
  }
  
  // 关闭警告弹窗
  if (warningModal) {
    warningModal.destroy()
    warningModal = null
  }
  
  // 移除用户活动监听器
  const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click']
  events.forEach(event => {
    document.removeEventListener(event, handleUserActivity)
  })
  
  // 移除页面可见性监听器
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

// 菜单项（按功能分组排序）
const menuItems: MenuProps['items'] = [
  // === 业务功能 ===
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
  // === 通知配置 ===
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
  // === 系统配置 ===
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

// 监听路由变化，更新选中菜单
const stopWatch = watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  },
  { immediate: true }
)

// 菜单点击处理
function handleMenuClick({ key }: { key: string }) {
  router.push(key as string)
}

// 登出
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
/* 全局样式 - 确保 CSS 变量和工具类可用 */
.admin-layout {
  min-height: 100vh;
  background: var(--bg-secondary);
}
</style>

<style scoped>

/* 版本更新横幅 */
.update-banner {
  background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%);
  color: #fff;
  padding: 10px 24px;
  position: sticky;
  top: 0;
  z-index: 1000;
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

/* 侧边栏样式 */
.sider {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
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
}

/* 侧边栏收起时的宽度 */
.sider.ant-layout-sider-collapsed {
  width: 80px;
}

.sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 侧边栏滚动条样式优化 */
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
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* Logo hover效果 - 轻微高亮 */
.logo:hover {
  opacity: 0.95;
}

.logo-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  width: 100%;
}

.logo-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-image {
  width: 40px;
  height: 40px;
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

.logo-text h2 {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logo-text p {
  color: rgba(255, 255, 255, 0.7);
  font-size: 10px;
  margin: 2px 0 0 0;
  letter-spacing: 0.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logo-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.admin-menu {
  flex: 1;
  border-right: none;
  padding: 8px 0;
}

.admin-menu :deep(.ant-menu-item) {
  margin: 4px 8px;
  border-radius: var(--radius-sm);
  height: 44px;
  line-height: 44px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.admin-menu :deep(.ant-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
}

.admin-menu :deep(.ant-menu-item-selected) {
  background: rgba(255, 255, 255, 0.15);
  border-left: 3px solid var(--primary-color-light);
}

.admin-menu :deep(.ant-menu-item-selected::after) {
  display: none;
}

/* 头部样式 */
.header {
  background: #fff;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 99;
}

.header-content {
  display: flex;
  align-items: center;
  height: 72px;
  padding: 0 24px;
  max-width: 100%;
}

.collapse-btn {
  color: var(--text-primary);
  font-size: 18px;
  margin-right: 16px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.collapse-btn:hover {
  color: var(--primary-color);
  background: var(--bg-tertiary);
}

.header-title {
  flex: 1;
  margin-left: 8px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  position: relative;
  padding-left: 16px;
}

.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  border-radius: 2px;
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
  background: transparent !important;
  cursor: pointer;
}

.header-btn:hover {
  color: var(--primary-color);
  background: var(--bg-tertiary) !important;
}

/* 铃铛徽章样式 */
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

/* 主布局区域（右侧内容区） */
.main-layout {
  margin-left: 240px;
  transition: margin-left 0.2s;
  min-height: 100vh;
}

/* 当侧边栏收起时，调整主布局的左边距 */
.admin-layout .sider.ant-layout-sider-collapsed ~ .main-layout {
  margin-left: 80px;
}

/* 内容区域 */
.content {
  margin: 24px;
  min-height: calc(100vh - 120px);
}

.content-wrapper {
  background: transparent;
}

/* 响应式设计 - 平板 */
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
  
  .content {
    margin: 20px;
  }
}

/* 响应式设计 - 移动端 */
@media (max-width: 768px) {
  .admin-layout {
    position: relative;
  }
  
  .main-layout {
    margin-left: 0 !important;
  }
  
  .sider {
    position: fixed !important;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    height: 100vh;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .sider.mobile-sidebar:not(.ant-layout-sider-collapsed) {
    transform: translateX(0) !important;
  }
  
  .sider.mobile-sidebar.ant-layout-sider-collapsed {
    transform: translateX(-100%) !important;
  }
  
  .mobile-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.45);
    z-index: 999;
    transition: opacity 0.3s ease;
  }
  
  .logo {
    height: 64px;
  }
  
  .logo-content {
    padding: 0 12px;
  }
  
  .logo-icon {
    width: 32px;
    height: 32px;
  }
  
  .logo-image {
    width: 32px;
    height: 32px;
  }
  
  .logo-text h2 {
    font-size: 14px;
  }
  
  .logo-text p {
    font-size: 9px;
  }
  
  .admin-menu :deep(.ant-menu-item) {
    height: 40px;
    line-height: 40px;
    margin: 2px 4px;
    font-size: 14px;
  }
  
  .header {
    position: sticky;
    top: 0;
    z-index: 999;
  }
  
  .header-content {
    padding: 0 12px;
    height: 64px;
  }
  
  .collapse-btn {
    font-size: 20px;
    margin-right: 12px;
    padding: 8px;
  }
  
  .page-title {
    font-size: 16px;
    padding-left: 12px;
  }
  
  .page-title::before {
    width: 3px;
    height: 16px;
  }
  
  .header-actions {
    gap: 4px;
  }
  
  .header-btn {
    padding: 8px;
  }
  
  .header-btn-text {
    display: none;
  }
  
  .content {
    margin: 12px;
    padding: 16px;
    min-height: calc(100vh - 88px);
  }
}

/* 小屏手机优化 */
@media (max-width: 480px) {
  .header-content {
    padding: 0 8px;
    height: 56px;
  }
  
  .page-title {
    font-size: 14px;
    padding-left: 8px;
  }
  
  .content {
    margin: 8px;
    padding: 12px;
  }
  
  .logo-text h2 {
    font-size: 13px;
  }
  
  .logo-text p {
    font-size: 9px;
  }
  
  .admin-menu :deep(.ant-menu-item) {
    height: 36px;
    line-height: 36px;
    font-size: 13px;
  }
}
</style>

<style>
/* 全局样式 - Logo 渐变背景 */
.logo.gradient-bg-primary {
  background: linear-gradient(135deg, var(--primary-color-dark) 0%, var(--primary-color) 50%, var(--primary-color-light) 100%) !important;
}

/* 全局样式 - 平滑过渡 */
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
