import request from './request'
import type { ApiResponse } from './request'

/**
 * 函件验证结果接口
 */
export interface LetterVerificationResult {
  valid: boolean
  verifyStatus: 'VALID' | 'INVALID' | 'EXPIRED' | 'REVOKED' | 'NOT_FOUND'
  statusMessage: string
  applicationNo?: string
  letterTypeName?: string
  firmName?: string
  lawyerNames?: string
  targetUnit?: string
  matterName?: string
  approvedAt?: string
  printedAt?: string
  validUntil?: string
  verifyCount?: number
  remark?: string
}

/**
 * 函件验证记录接口（管理后台）
 */
export interface LetterVerificationRecord {
  id: number
  letterId: number
  applicationNo: string
  verificationCode: string
  letterType?: string
  letterTypeName?: string
  targetUnit?: string
  lawyerNames?: string
  firmName?: string
  matterName?: string
  approvedAt?: string
  printedAt?: string
  validUntil?: string
  remark?: string
  verifyCount: number
  lastVerifyAt?: string
  lastVerifyIp?: string
  status: string
  createdAt: string
  updatedAt: string
}

/**
 * 分页查询结果接口
 */
export interface VerificationListResponse {
  records: LetterVerificationRecord[]
  total: number
  page: number
  pageSize: number
  pages: number
}

/**
 * 统计数据接口
 */
export interface VerificationStatistics {
  total: number
  active: number
  expired: number
  revoked: number
}

// ========== 公开接口 ==========

/**
 * 验证函件真伪
 * @param applicationNo 函件申请编号
 * @param code 验证码
 */
export function verifyLetter(applicationNo: string, code: string): Promise<ApiResponse<LetterVerificationResult>> {
  return request.get('/letter/verification/verify', {
    params: { no: applicationNo, code }
  })
}

/**
 * 获取函件验证信息
 * @param applicationNo 函件申请编号
 */
export function getVerificationInfo(applicationNo: string): Promise<ApiResponse<LetterVerificationResult>> {
  return request.get(`/letter/verification/info/${applicationNo}`)
}

// ========== 管理后台接口 ==========

/**
 * 分页查询验证记录
 */
export function getVerificationList(params: {
  page?: number
  pageSize?: number
  status?: string
  keyword?: string
}): Promise<ApiResponse<VerificationListResponse>> {
  return request.get('/api/admin/letter-verification/list', { params })
}

/**
 * 获取验证详情
 */
export function getVerificationDetail(id: number): Promise<ApiResponse<LetterVerificationRecord>> {
  return request.get(`/api/admin/letter-verification/${id}`)
}

/**
 * 撤销验证
 */
export function revokeVerification(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/admin/letter-verification/${id}`)
}

/**
 * 获取验证统计
 */
export function getVerificationStatistics(): Promise<ApiResponse<VerificationStatistics>> {
  return request.get('/api/admin/letter-verification/statistics')
}
