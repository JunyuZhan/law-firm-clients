import request from './request'
import type { ApiResponse } from './request'

// 通知历史接口
export interface NotificationHistory {
  id: number
  matterId: string
  clientId: number
  notificationType: string  // SMS, WECHAT, EMAIL
  recipient: string
  content: string
  status: string  // PENDING, SUCCESS, FAILED
  errorMessage?: string
  sentAt?: string
  createdAt: string
  retryCount?: number
  maxRetries?: number
  nextRetryAt?: string
  lastRetryAt?: string
}

// 通知统计接口
export interface NotificationStatistics {
  total: number
  success: number
  failed: number
  pending: number
  byType?: {
    SMS?: { total: number; success: number; failed: number; pending: number }
    WECHAT?: { total: number; success: number; failed: number; pending: number }
    EMAIL?: { total: number; success: number; failed: number; pending: number }
  }
  sms?: { total: number; success: number; failed: number; pending: number }
  wechat?: { total: number; success: number; failed: number; pending: number }
  email?: { total: number; success: number; failed: number; pending: number }
}

// 获取通知历史
export function getNotificationHistory(params: {
  matterId?: string
  clientId?: number
  notificationType?: string
  status?: string
  startTime?: string
  endTime?: string
  limit?: number
}): Promise<ApiResponse<NotificationHistory[]>> {
  return request.get('/api/notification/history', { params })
}

// 手动发送通知
export function sendNotification(matterId: string): Promise<ApiResponse<void>> {
  return request.post('/api/notification/send', null, {
    params: { matterId },
  })
}

// 获取通知统计
export function getNotificationStatistics(params: {
  matterId?: string
  clientId?: number
  startTime?: string
  endTime?: string
}): Promise<ApiResponse<NotificationStatistics>> {
  return request.get('/api/notification/statistics', { params })
}
