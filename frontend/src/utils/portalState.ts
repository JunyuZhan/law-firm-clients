export type PortalAccessErrorState = 'none' | 'missing-token' | 'invalid-token'

const INVALID_TOKEN_HINTS = ['过期', '无效', '不存在', '撤销']

export function resolvePortalAccessErrorState(error: unknown): PortalAccessErrorState {
  const errorMessage = error instanceof Error ? error.message : ''
  return INVALID_TOKEN_HINTS.some(keyword => errorMessage.includes(keyword))
    ? 'invalid-token'
    : 'missing-token'
}

export function getPortalMatterStatusText(status: string): string {
  const textMap: Record<string, string> = {
    ACTIVE: '有效',
    EXPIRED: '已过期',
    REVOKED: '已撤销',
  }
  return textMap[status] || status
}

export function getPortalMatterStatusTone(status: string): 'primary' | 'success' | 'warning' | 'danger' {
  const toneMap: Record<string, 'primary' | 'success' | 'warning' | 'danger'> = {
    ACTIVE: 'success',
    EXPIRED: 'warning',
    REVOKED: 'danger',
  }
  return toneMap[status] || 'primary'
}

export function getPortalNotificationStatusText(status: string): string {
  const textMap: Record<string, string> = {
    SUCCESS: '发送成功',
    FAILED: '发送失败',
    PENDING: '发送中',
  }
  return textMap[status] || status
}
