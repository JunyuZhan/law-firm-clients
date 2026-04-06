import request from './request'
import type { ApiResponse } from './request'

// 系统配置接口
export interface SysConfigInfo {
  id: number
  configKey: string
  configValue: string
  configType: string  // STRING, NUMBER, BOOLEAN, JSON
  description?: string
  createdAt: string
  updatedAt: string
}

// 创建/更新配置请求
export interface SaveConfigRequest {
  configKey: string
  configValue: string
  configType?: string
  description?: string
}

// 更新配置请求
export interface UpdateConfigRequest {
  configValue?: string
  configType?: string
  description?: string
}

// 获取配置列表
export function getConfigList(params: {
  configKey?: string
  configType?: string
  limit?: number
}): Promise<ApiResponse<SysConfigInfo[]>> {
  return request.get('/api/admin/config', { params })
}

// 获取配置详情
export function getConfigById(id: number): Promise<ApiResponse<SysConfigInfo>> {
  return request.get(`/api/admin/config/${id}`)
}

// 创建或更新配置
export function saveConfig(data: SaveConfigRequest): Promise<ApiResponse<SysConfigInfo>> {
  return request.post('/api/admin/config', data)
}

// 更新配置
export function updateConfig(id: number, data: UpdateConfigRequest): Promise<ApiResponse<SysConfigInfo>> {
  return request.put(`/api/admin/config/${id}`, data)
}

// 删除配置
export function deleteConfig(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/admin/config/${id}`)
}

// ==================== 公开配置接口（无需认证） ====================

/**
 * 获取公开配置（无需认证）
 * @param keys 配置键列表，多个用逗号分隔（可选）
 * @returns 配置键值对
 */
export function getPublicConfig(keys?: string): Promise<ApiResponse<Record<string, string>>> {
  const params = keys ? { keys } : {}
  return request.get('/api/public/config', { params })
}

/**
 * 获取 ICP 备案号（无需认证）
 * @returns ICP 备案号
 */
export function getIcpLicense(): Promise<ApiResponse<string>> {
  return request.get('/api/public/config/icp')
}

/**
 * 门户配置响应
 */
export interface PortalConfig {
  lawFirmName: string      // 律所名称
  lawFirmWebsite: string   // 律所官网
  appSlogan: string        // 首页标语
  icpLicense: string       // ICP备案号
  copyright: string        // 版权信息
  logoUrl: string          // Logo地址
}

/**
 * 获取门户页面配置（无需认证）
 * @returns 门户配置
 */
export function getPortalConfig(): Promise<ApiResponse<PortalConfig>> {
  return request.get('/api/public/config/portal')
}

/**
 * 系统品牌配置响应
 */
export interface BrandConfig {
  appName: string          // 系统名称（浏览器标题）
  appShortName: string     // 系统简称（管理后台侧边栏）
  appShortNameEn: string   // 系统英文简称
  logoUrl: string          // Logo地址
}

/**
 * 获取系统品牌配置（无需认证）
 * @returns 品牌配置
 */
export function getBrandConfig(): Promise<ApiResponse<BrandConfig>> {
  return request.get('/api/public/config/brand')
}

export interface SystemRuntimeInfo {
  applicationName: string
  productVersion: string
  backendVersion: string
  commitSha: string
  buildTime: string
  recommendedMode: string
  serverTime: string
}

export interface DependencyStatusItem {
  key: string
  label: string
  status: string
  details: Record<string, string | number | boolean>
}

export interface SystemDependencyStatus {
  overallStatus: string
  items: DependencyStatusItem[]
}

export function getSystemRuntimeInfo(): Promise<ApiResponse<SystemRuntimeInfo>> {
  return request.get('/api/admin/system/runtime-info')
}

export function getSystemDependencyStatus(): Promise<ApiResponse<SystemDependencyStatus>> {
  return request.get('/api/admin/system/dependency-status')
}
