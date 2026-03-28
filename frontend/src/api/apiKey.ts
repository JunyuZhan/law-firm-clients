import request from './request'
import type { ApiResponse } from './request'

// API密钥接口
export interface ApiKeyInfo {
  id: number
  keyName: string
  apiKey: string  // 已部分隐藏
  apiSecret?: string  // 已部分隐藏
  lawFirmName?: string  // 已废弃，不再使用
  enabled: boolean
  expiresAt?: string
  lastUsedAt?: string
  createdAt: string
  updatedAt: string
}

// 创建API密钥请求
export interface CreateApiKeyRequest {
  keyName: string
  expiresAt?: string
}

// 更新API密钥请求
export interface UpdateApiKeyRequest {
  keyName?: string
  enabled?: boolean
  expiresAt?: string
}

// 获取API密钥列表
export function getApiKeyList(params: {
  enabled?: boolean
  limit?: number
}): Promise<ApiResponse<ApiKeyInfo[]>> {
  return request.get('/api/admin/api-keys', { params })
}

// 获取API密钥详情
export function getApiKeyById(id: number): Promise<ApiResponse<ApiKeyInfo>> {
  return request.get(`/api/admin/api-keys/${id}`)
}

// 初始化API密钥请求（可选，通常为空对象）
export interface InitApiKeyRequest {
  // 当前版本不需要参数，保留接口以便将来扩展
}

// 初始化API密钥（首次使用，无需认证，一键生成）
export function initApiKey(data?: InitApiKeyRequest): Promise<ApiResponse<ApiKeyInfo>> {
  return request.post('/api/admin/api-keys/init', data || {})
}

// 创建API密钥
export function createApiKey(data: CreateApiKeyRequest): Promise<ApiResponse<ApiKeyInfo>> {
  return request.post('/api/admin/api-keys', data)
}

// 更新API密钥
export function updateApiKey(id: number, data: UpdateApiKeyRequest): Promise<ApiResponse<ApiKeyInfo>> {
  return request.put(`/api/admin/api-keys/${id}`, data)
}

// 删除API密钥
export function deleteApiKey(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/admin/api-keys/${id}`)
}
