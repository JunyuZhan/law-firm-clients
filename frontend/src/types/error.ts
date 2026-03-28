/**
 * 统一的错误类型定义
 */

// API 错误响应
export interface ApiError {
  code: number | string
  message: string
  success: boolean
  data?: unknown
}

// 网络错误
export interface NetworkError extends Error {
  status?: number
  response?: {
    data?: ApiError
    status: number
    statusText: string
  }
}

// 业务错误
export interface BusinessError extends Error {
  code: number | string
  message: string
}

// 错误类型判断辅助函数
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'code' in error &&
    'message' in error &&
    'success' in error
  )
}

export function isNetworkError(error: unknown): error is NetworkError {
  return (
    error instanceof Error &&
    'status' in error &&
    'response' in error
  )
}

export function isBusinessError(error: unknown): error is BusinessError {
  return (
    error instanceof Error &&
    'code' in error &&
    'message' in error
  )
}
