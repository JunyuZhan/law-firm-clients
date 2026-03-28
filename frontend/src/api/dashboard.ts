import request from './request'
import type { ApiResponse } from './request'

// Dashboard 统计数据
export interface DashboardStats {
  matterCount: number
  activeMatterCount: number
  expiredMatterCount: number
  notificationCount: number
  notificationSuccessCount: number
  notificationFailedCount: number
  fileCount: number
  totalFileSize: number
  verificationCount: number
  activeVerificationCount: number
  apiKeyCount: number
  accessLogCount: number
  todayAccessCount: number
}

// 通知统计
export interface NotificationStats {
  total: number
  success: number
  failed: number
  pending: number
  sms: { total: number; success: number; failed: number }
  wechat: { total: number; success: number; failed: number }
  email: { total: number; success: number; failed: number }
}

// 文件统计
export interface FileStats {
  total: number
  totalSize: number
  categoryStats: Record<string, number>
}

// 验证统计
export interface VerificationStats {
  total: number
  active: number
  expired: number
  revoked: number
  todayVerifyCount: number
}

// 获取 Dashboard 统计数据
export function getDashboardStats(): Promise<ApiResponse<DashboardStats>> {
  return request.get('/api/admin/dashboard/stats')
}

// 获取通知统计
export function getNotificationStats(params?: {
  startTime?: string
  endTime?: string
}): Promise<ApiResponse<NotificationStats>> {
  return request.get('/api/notification/statistics', { params })
}

// 获取文件统计
export function getFileStats(): Promise<ApiResponse<FileStats>> {
  return request.get('/api/admin/file/statistics')
}

// 获取验证统计
export function getVerificationStats(): Promise<ApiResponse<VerificationStats>> {
  return request.get('/api/admin/letter-verification/statistics')
}

// 获取访问统计
export function getAccessStats(params?: {
  startTime?: string
  endTime?: string
}): Promise<ApiResponse<{
  total: number
  today: number
  uniqueVisitors: number
  topPages: Array<{ path: string; count: number }>
}>> {
  return request.get('/api/admin/access/statistics', { params })
}