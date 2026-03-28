import request from './request'
import type { ApiResponse } from './request'

// 项目列表接口
export interface MatterListItem {
  id: string
  lawFirmMatterId: number
  clientId: number
  clientName: string
  status: string  // ACTIVE, EXPIRED, REVOKED
  validDays: number
  expiresAt?: string
  accessUrl: string
  createdAt: string
  updatedAt: string
}

// 获取项目列表
export function getMatterList(params: {
  clientId?: number
  status?: string
  startTime?: string
  endTime?: string
  limit?: number
}): Promise<ApiResponse<MatterListItem[]>> {
  return request.get('/api/matter/list', { params })
}

// 项目数据（来自律所系统的动态数据）
// 索引签名支持扩展，但常见字段有明确类型定义
export interface MatterData {
  [key: string]: unknown
  // 基本信息
  matterName?: string
  matterNo?: string
  matterType?: string
  matterTypeName?: string
  status?: string
  statusName?: string
  createDate?: string
  // 进度信息
  currentStage?: string
  currentStageName?: string
  progress?: number
  lastUpdateTime?: string
  // 负责人信息
  leadLawyerName?: string
  leadLawyerContact?: string
  lawyers?: Array<{ name?: string; title?: string; contact?: string }>
  lawyerList?: Array<{ name?: string; title?: string; contact?: string }>
  teamMembers?: Array<{ name?: string; title?: string; contact?: string }>
  // 任务相关
  completedTaskCount?: number
  totalTaskCount?: number
  pendingTasks?: Array<{ title?: string; dueDate?: string; priority?: string; status?: string }>
  // 截止日期
  deadlines?: Array<{ title?: string; type?: string; isOverdue?: boolean; deadline?: string }>
  // 费用相关
  contractAmount?: number
  receivedAmount?: number
  pendingAmount?: number
  // 文档相关
  documentCount?: number
  documents?: Array<{ name?: string; uploadTime?: string; type?: string }>
}

// 项目详情接口
export interface MatterDetailInfo {
  id: string
  lawFirmMatterId: number
  clientId: number
  clientName: string
  matterData?: MatterData
  scopes: string
  status: string
  validDays: number
  expiresAt?: string
  accessToken: string
  accessUrl: string
  createdAt: string
  updatedAt: string
}

// 获取项目详情（管理后台）
export function getMatterDetail(id: string): Promise<ApiResponse<MatterDetailInfo>> {
  return request.get(`/api/matter/detail/${id}`)
}

// 客户门户项目详情接口
export interface ClientMatterDetail {
  id: string
  lawFirmMatterId: number
  clientId: number
  clientName: string
  matterData?: MatterData
  accessToken: string
  accessUrl: string
  status: string
  expiresAt?: string
  createdAt: string
}

// 获取项目详情（客户门户）
export function getClientMatterDetail(matterId: string, token: string): Promise<ApiResponse<ClientMatterDetail>> {
  return request.get(`/portal/api/matter/${matterId}`, {
    params: { token },
  })
}

// 撤销项目访问
export function revokeMatter(matterId: string): Promise<ApiResponse<void>> {
  return request.post('/api/matter/revoke', null, {
    params: { matterId },
  })
}
