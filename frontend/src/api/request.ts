import axios, { type AxiosInstance, type AxiosResponse, type AxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// API响应接口
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  success: boolean
}

/**
 * 管理后台API路径模式配置
 * 集中管理需要JWT认证的API路径，便于维护
 */
const ADMIN_API_PATTERNS = [
  '/api/admin/',                        // 所有管理后台API
  '/api/matter/list',                   // 项目列表（管理后台）
  '/api/matter/detail/',                // 项目详情（管理后台）
  '/api/matter/revoke',                 // 撤销项目（管理后台）
  '/api/notification/history',          // 通知历史（管理后台）
  '/api/notification/send',             // 发送通知（管理后台）
  '/api/notification/statistics',       // 通知统计（管理后台）
]

/**
 * 无需认证的API路径模式
 */
const NO_AUTH_API_PATTERNS = [
  '/api/admin/auth/login',              // 登录接口
  '/api/admin/auth/captcha',            // 验证码接口
  '/api/admin/api-keys/init',           // 初始化接口
  '/letter/verification/verify',        // 函件验证公开接口
  '/letter/verification/info/',         // 函件验证信息公开接口
]

/**
 * 检查URL是否匹配模式列表
 */
function matchesPatterns(url: string | undefined, patterns: string[]): boolean {
  if (!url) return false
  return patterns.some(pattern => url.includes(pattern))
}

// 扩展 AxiosRequestConfig，添加 params 类型支持
export interface RequestConfig extends AxiosRequestConfig {
  params?: Record<string, unknown>
  suppressErrorMessage?: boolean
}

// 创建axios实例
const axiosInstance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
axiosInstance.interceptors.request.use(
  (config) => {
    // 使用配置化的模式匹配检查API类型
    const isAdminApi = matchesPatterns(config.url, ADMIN_API_PATTERNS)
    
    if (isAdminApi) {
      // 检查是否为无需认证的API
      const isNoAuthApi = matchesPatterns(config.url, NO_AUTH_API_PATTERNS)
      
      if (!isNoAuthApi) {
        // 管理后台API需要JWT Token认证
        const authStore = useAuthStore()
        const token = authStore.getToken()
        
        if (token) {
          config.headers = config.headers || {}
          config.headers['Authorization'] = `Bearer ${token}`
          
          // 对于 POST/PUT/DELETE/PATCH 请求，添加 CSRF Token
          const method = config.method?.toUpperCase()
          if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {
            const csrfToken = authStore.getCsrfToken()
            if (csrfToken) {
              config.headers['X-CSRF-Token'] = csrfToken
            }
          }
        } else {
          // 如果没有Token，返回错误提示
          message.error('请先登录')
          router.push('/admin/login')
          return Promise.reject(new Error('未登录'))
        }
      }
    } else {
      // 客户门户API：token通过query参数传递，不需要设置Authorization header
      // 后端API通过@RequestParam接收token
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
axiosInstance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 检查success字段，如果为false则视为错误
    if (!res.success) {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // 兼容code字段（code为200或"200"表示成功）
    const code = typeof res.code === 'string' ? parseInt(res.code, 10) : res.code
    if (code !== 200 && code !== 0) {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // 直接返回 response.data，这样API函数返回的就是 ApiResponse<T>
    return response
  },
  (error) => {
    const status = error.response?.status
    const suppressErrorMessage = Boolean(error.config?.suppressErrorMessage)

    // 处理401未授权错误（Token无效或过期）
    if (status === 401) {
      const authStore = useAuthStore()
      // 只有在非登录页面时才清除认证信息
      const isLoginPage = window.location.pathname === '/admin/login'
      if (!isLoginPage) {
        authStore.clearAuth()
        if (!suppressErrorMessage) {
          message.error('登录已过期，请重新登录')
        }
        // 如果是管理后台页面，跳转到登录页
        if (window.location.pathname.startsWith('/admin')) {
          router.push('/admin/login')
        }
      }
      return Promise.reject(error)
    }

    // 处理403权限不足
    if (status === 403) {
      const errorCode = error.response?.data?.code
      const errorMsg = error.response?.data?.message
      
      // 检查是否是 CSRF Token 相关错误
      if (errorCode === 'CSRF_TOKEN_MISSING' || errorCode === 'CSRF_TOKEN_INVALID') {
        const authStore = useAuthStore()
        const isLoginPage = window.location.pathname === '/admin/login'
        if (!isLoginPage) {
          authStore.clearAuth()
          if (!suppressErrorMessage) {
            message.error(errorMsg || '安全令牌已失效，请重新登录')
          }
          if (window.location.pathname.startsWith('/admin')) {
            router.push('/admin/login')
          }
        }
        return Promise.reject(error)
      }
      
      if (!suppressErrorMessage) {
        message.error('权限不足，无法执行此操作')
      }
      return Promise.reject(error)
    }

    // 处理500服务器内部错误（不暴露后端详情）
    if (status && status >= 500) {
      if (!suppressErrorMessage) {
        message.error('服务器繁忙，请稍后重试')
      }
      return Promise.reject(error)
    }

    // 处理429请求过多
    if (status === 429) {
      if (!suppressErrorMessage) {
        message.warning('操作过于频繁，请稍后再试')
      }
      return Promise.reject(error)
    }

    // 其他错误：使用通用提示，不暴露后端内部信息
    const errorMessage = error.response?.data?.message || error.message || '网络错误'
    if (!suppressErrorMessage) {
      message.error(errorMessage)
    }
    return Promise.reject(error)
  }
)

// 包装请求方法，提供正确的类型
const request = {
  get<T = unknown>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.get<ApiResponse<T>>(url, config).then(response => response.data)
  },
  post<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.post<ApiResponse<T>>(url, data, config).then(response => response.data)
  },
  put<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.put<ApiResponse<T>>(url, data, config).then(response => response.data)
  },
  delete<T = unknown>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.delete<ApiResponse<T>>(url, config).then(response => response.data)
  },
  patch<T = unknown>(url: string, data?: unknown, config?: RequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.patch<ApiResponse<T>>(url, data, config).then(response => response.data)
  },
}

export default request
