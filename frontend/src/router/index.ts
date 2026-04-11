import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'
import { useAppConfigStore } from '@/stores/appConfig'
import { APP_NAME } from '@/config/app'
import logger from '@/utils/logger'

// 迁移检查标志（确保只执行一次）
let migrationChecked = false

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/portal',
  },
  {
    // 将更具体的路由放在前面，确保优先匹配
    path: '/portal/matter/:id',
    name: 'PortalMatterDetail',
    component: () => import('@/views/MatterDetail.vue'),
  },
  {
    path: '/portal',
    name: 'Portal',
    component: () => import('@/views/Portal.vue'),
  },
  {
    path: '/matter/:id',
    name: 'MatterDetail',
    component: () => import('@/views/MatterDetail.vue'),
  },
  {
    path: '/matters',
    name: 'ClientMatterList',
    component: () => import('@/views/ClientMatterList.vue'),
    meta: { title: '我的项目' },
  },
  {
    path: '/files',
    name: 'ClientFileList',
    component: () => import('@/views/ClientFileList.vue'),
    meta: { title: '文件中心' },
  },
  {
    path: '/notifications',
    name: 'ClientNotifications',
    component: () => import('@/views/ClientNotifications.vue'),
    meta: { title: '消息通知' },
  },
  {
    path: '/profile',
    name: 'ClientProfile',
    component: () => import('@/views/ClientProfile.vue'),
    meta: { title: '个人中心' },
  },
  {
    path: '/help',
    name: 'ClientHelp',
    component: () => import('@/views/HelpCenter.vue'),
    meta: { title: '帮助中心' },
  },
  {
    path: '/verify/letter',
    name: 'LetterVerify',
    component: () => import('@/views/verify/LetterVerify.vue'),
    meta: { title: '函件真伪验证' },
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录' },
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'setup',
        name: 'InitialSetup',
        component: () => import('@/views/admin/InitialSetup.vue'),
        meta: { title: '首次初始化', adminOnly: true },
      },
      {
        path: 'system-info',
        name: 'SystemInfo',
        component: () => import('@/views/admin/SystemInfo.vue'),
        meta: { title: '系统信息', adminOnly: true },
      },
      {
        path: 'notifications',
        name: 'NotificationHistory',
        component: () => import('@/views/admin/NotificationHistory.vue'),
        meta: { title: '通知记录', adminOnly: true },
      },
      {
        path: 'matters',
        name: 'MatterList',
        component: () => import('@/views/admin/MatterList.vue'),
        meta: { title: '项目列表', adminOnly: true },
      },
      {
        path: 'matters/:id',
        name: 'AdminMatterDetail',
        component: () => import('@/views/admin/MatterDetail.vue'),
        meta: { title: '项目详情', adminOnly: true },
      },
      {
        path: 'api-keys',
        name: 'ApiKeyManagement',
        component: () => import('@/views/admin/ApiKeyManagement.vue'),
        meta: { title: 'API密钥管理', adminOnly: true },
      },
      {
        path: 'config',
        name: 'SystemConfig',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: { title: '系统配置', adminOnly: true },
      },
      {
        path: 'notification-templates',
        name: 'NotificationTemplateManagement',
        component: () => import('@/views/admin/NotificationTemplateManagement.vue'),
        meta: { title: '通知模板管理', adminOnly: true },
      },
      {
        path: 'notification-settings',
        name: 'NotificationSettings',
        component: () => import('@/views/admin/NotificationSettings.vue'),
        meta: { title: '通知配置', adminOnly: true },
      },
      {
        path: 'files',
        name: 'FileManagement',
        component: () => import('@/views/admin/FileManagement.vue'),
        meta: { title: '文件管理', adminOnly: true },
      },
      {
        path: 'maintenance',
        name: 'SystemMaintenance',
        component: () => import('@/views/admin/SystemMaintenance.vue'),
        meta: { title: '系统维护', adminOnly: true },
      },
      {
        path: 'letter-verifications',
        name: 'LetterVerificationList',
        component: () => import('@/views/admin/LetterVerificationList.vue'),
        meta: { title: '函件验证', adminOnly: true },
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/AdminProfile.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: '',
        redirect: '/admin/notifications',
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  // 调试信息：验证路由匹配
  if (to.path.startsWith('/portal/matter/')) {
    logger.debug('[Router] 匹配到项目详情路由:', {
      path: to.path,
      params: to.params,
      query: to.query,
      matched: to.matched.map(r => ({ path: r.path, name: r.name }))
    })
  }

  const authStore = useAuthStore()
  
  // 迁移检查：如果存在旧的API密钥，清除它并提示用户（仅执行一次，提高性能）
  if (!migrationChecked && to.path.startsWith('/admin')) {
    migrationChecked = true
    const OLD_API_KEY_STORAGE_KEY = 'admin_api_key'
    const oldApiKey = localStorage.getItem(OLD_API_KEY_STORAGE_KEY)
    
    if (oldApiKey) {
      // 清除旧的API密钥
      localStorage.removeItem(OLD_API_KEY_STORAGE_KEY)
      // 如果用户未登录，提示需要重新登录
      if (!authStore.isAuthenticated) {
        message.info('系统已升级，请使用用户名密码登录管理后台', 5)
      } else {
        message.info('系统已升级为用户名密码登录方式', 3)
      }
    }
  }
  
  // 设置页面标题（优先使用后台配置的系统名称）
  const appConfigStore = useAppConfigStore()
  const appName = appConfigStore.appName || APP_NAME
  const pageTitle = to.matched.find(r => r.meta?.title)?.meta?.title as string
  if (pageTitle) {
    document.title = `${pageTitle} - ${appName}`
  } else if (to.path === '/portal' || to.path === '/') {
    document.title = appName  // 门户首页使用系统完整名称
  } else if (to.path.startsWith('/admin')) {
    document.title = `管理后台 - ${appName}`
  } else {
    document.title = appName
  }
  
  // 检查是否需要认证：meta 标记 或 /admin 路径（双重保障）
  const needsAuth = to.matched.some((record) => record.meta.requiresAuth) ||
    (to.path.startsWith('/admin') && to.path !== '/admin/login')
  if (needsAuth) {
    if (!authStore.isAuthenticated) {
      message.warning('请先登录')
      next('/admin/login')
      return
    }
  }

  if (to.matched.some((record) => record.meta.adminOnly) && authStore.isAuthenticated && !authStore.getCurrentUserInfo()) {
    await authStore.fetchCurrentUser()
  }

  if (to.matched.some((record) => record.meta.adminOnly) && !authStore.getCurrentUserInfo()?.superAdmin) {
    message.warning('该功能仅超级管理员可访问')
    next('/admin/notifications')
    return
  }
  
  // 如果已登录，访问登录页时跳转到管理后台首页
  if (to.path === '/admin/login' && authStore.isAuthenticated) {
    next(authStore.getCurrentUserInfo()?.superAdmin ? '/admin/matters' : '/admin/notifications')
    return
  }
  
  next()
})

export default router
