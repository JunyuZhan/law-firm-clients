import request from './request'
import type { ApiResponse } from './request'

// 通知模板接口
export interface NotificationTemplateInfo {
  id: number
  templateName: string
  templateType: string  // SMS, WECHAT, EMAIL
  templateCode: string
  templateContent?: string
  templateVariables?: string
  provider?: string  // aliyun, tencent, wechat
  signName?: string
  enabled: boolean
  description?: string
  createdAt: string
  updatedAt: string
}

// 创建模板请求
export interface CreateTemplateRequest {
  templateName: string
  templateType: string
  templateCode?: string
  templateContent?: string
  templateVariables?: string
  provider?: string
  signName?: string
  enabled?: boolean
  description?: string
}

// 更新模板请求
export interface UpdateTemplateRequest {
  templateName?: string
  templateContent?: string
  templateVariables?: string
  enabled?: boolean
  description?: string
}

// 获取模板列表
export function getTemplateList(params: {
  templateType?: string
  provider?: string
  enabled?: boolean
  limit?: number
}): Promise<ApiResponse<NotificationTemplateInfo[]>> {
  return request.get('/api/admin/notification-templates', { params })
}

// 获取模板详情
export function getTemplateById(id: number): Promise<ApiResponse<NotificationTemplateInfo>> {
  return request.get(`/api/admin/notification-templates/${id}`)
}

// 创建模板
export function createTemplate(data: CreateTemplateRequest): Promise<ApiResponse<NotificationTemplateInfo>> {
  return request.post('/api/admin/notification-templates', data)
}

// 更新模板
export function updateTemplate(id: number, data: UpdateTemplateRequest): Promise<ApiResponse<NotificationTemplateInfo>> {
  return request.put(`/api/admin/notification-templates/${id}`, data)
}

// 删除模板
export function deleteTemplate(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/admin/notification-templates/${id}`)
}
