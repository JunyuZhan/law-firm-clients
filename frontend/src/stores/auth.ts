import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getCurrentUser, logout as logoutApi, isCurrentUserResponse } from '@/api/auth'
import type { UserInfo } from '@/api/auth'
import logger from '@/utils/logger'

const TOKEN_STORAGE_KEY = 'admin_token'
const USER_STORAGE_KEY = 'admin_user'
const CSRF_TOKEN_STORAGE_KEY = 'admin_csrf_token'
const OLD_API_KEY_STORAGE_KEY = 'admin_api_key' // 旧的API密钥存储键

// 使用 sessionStorage 替代 localStorage，关闭标签页后自动清除认证信息
// 降低 XSS 攻击窃取 Token 的持久化风险
const storage = sessionStorage

export const useAuthStore = defineStore('auth', () => {
  // 注意：迁移检查在路由守卫中进行，因为需要显示用户提示
  // 这里只负责清除旧的API密钥（在clearAuth时）

  // 状态（优先从 sessionStorage 读取，兼容从 localStorage 迁移）
  const token = ref<string | null>(storage.getItem(TOKEN_STORAGE_KEY) || localStorage.getItem(TOKEN_STORAGE_KEY))
  const csrfToken = ref<string | null>(storage.getItem(CSRF_TOKEN_STORAGE_KEY) || localStorage.getItem(CSRF_TOKEN_STORAGE_KEY))
  // 类型守卫：验证对象是否符合UserInfo结构
  const isUserInfo = (obj: unknown): obj is UserInfo => {
    if (typeof obj !== 'object' || obj === null) return false
    const user = obj as Record<string, unknown>
    return typeof user.id === 'number' && typeof user.username === 'string'
  }

  const getUserFromStorage = (): UserInfo | null => {
    try {
      const userStr = storage.getItem(USER_STORAGE_KEY) || localStorage.getItem(USER_STORAGE_KEY)
      if (!userStr) return null
      const parsed: unknown = JSON.parse(userStr)
      return isUserInfo(parsed) ? parsed : null
    } catch {
      return null
    }
  }
  const user = ref<UserInfo | null>(getUserFromStorage())

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)

  // 方法
  async function loginAction(username: string, password: string, captchaId?: string, captchaText?: string): Promise<void> {
    try {
      const response = await login({ username, password, captchaId, captchaText })
      token.value = response.token
      user.value = response.user
      if (response.csrfToken) {
        csrfToken.value = response.csrfToken
        storage.setItem(CSRF_TOKEN_STORAGE_KEY, response.csrfToken)
      }
      storage.setItem(TOKEN_STORAGE_KEY, response.token)
      storage.setItem(USER_STORAGE_KEY, JSON.stringify(response.user))
    } catch (error: unknown) {
      // 安全地提取错误信息
      let errorMessage = '登录失败'
      if (error instanceof Error) {
        errorMessage = error.message
      }
      // 处理Axios错误响应
      if (typeof error === 'object' && error !== null) {
        const axiosError = error as { response?: { data?: { message?: string } } }
        if (axiosError.response?.data?.message) {
          errorMessage = axiosError.response.data.message
        }
      }
      throw new Error(errorMessage)
    }
  }

  async function fetchCurrentUser(): Promise<void> {
    if (!token.value) {
      return
    }
    try {
      const response = await getCurrentUser()
      // 使用类型守卫区分新旧版本API响应
      if (isCurrentUserResponse(response)) {
        // 新版本API（返回包含csrfToken的对象）
        user.value = response.user
        if (response.csrfToken) {
          csrfToken.value = response.csrfToken
          storage.setItem(CSRF_TOKEN_STORAGE_KEY, response.csrfToken)
        }
        storage.setItem(USER_STORAGE_KEY, JSON.stringify(response.user))
      } else {
        // 旧版本API（只返回UserInfo）
        user.value = response
        storage.setItem(USER_STORAGE_KEY, JSON.stringify(response))
      }
    } catch (error) {
      // Token可能已过期，清除本地存储
      logger.warn('获取当前用户信息失败，Token可能已过期:', error)
      clearAuth()
    }
  }

  async function logout(): Promise<void> {
    try {
      if (token.value) {
        await logoutApi()
      }
    } catch (error) {
      // 即使登出API失败，也清除本地存储
      logger.warn('登出API调用失败，已清除本地存储:', error)
    } finally {
      clearAuth()
    }
  }

  function clearAuth(): void {
    token.value = null
    user.value = null
    csrfToken.value = null
    storage.removeItem(TOKEN_STORAGE_KEY)
    storage.removeItem(USER_STORAGE_KEY)
    storage.removeItem(CSRF_TOKEN_STORAGE_KEY)
    // 同时清除旧存储中的数据（兼容迁移）
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    localStorage.removeItem(USER_STORAGE_KEY)
    localStorage.removeItem(CSRF_TOKEN_STORAGE_KEY)
    localStorage.removeItem(OLD_API_KEY_STORAGE_KEY)
  }

  function getToken(): string | null {
    return token.value
  }

  function getCsrfToken(): string | null {
    return csrfToken.value
  }

  function getCurrentUserInfo(): UserInfo | null {
    return user.value
  }

  return {
    token,
    user,
    csrfToken,
    isAuthenticated,
    login: loginAction,
    fetchCurrentUser,
    logout,
    clearAuth,
    getToken,
    getCsrfToken,
    getCurrentUserInfo,
  }
})
