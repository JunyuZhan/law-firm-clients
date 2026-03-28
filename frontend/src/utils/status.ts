/**
 * 状态颜色和文本工具函数
 * 统一处理各种状态的颜色和显示文本
 */

// 事项状态颜色映射
const MATTER_STATUS_COLORS: Record<string, string> = {
  ACTIVE: 'green',
  EXPIRED: 'orange',
  REVOKED: 'red',
  IN_PROGRESS: 'blue',
  COMPLETED: 'green',
  PENDING: 'blue',
}

// 事项状态文本映射
const MATTER_STATUS_TEXTS: Record<string, string> = {
  ACTIVE: '活跃',
  EXPIRED: '已过期',
  REVOKED: '已撤销',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  PENDING: '待处理',
}

// 通知状态颜色映射
const NOTIFICATION_STATUS_COLORS: Record<string, string> = {
  PENDING: 'blue',
  SUCCESS: 'green',
  FAILED: 'red',
}

// 通知状态文本映射
const NOTIFICATION_STATUS_TEXTS: Record<string, string> = {
  PENDING: '待发送',
  SUCCESS: '成功',
  FAILED: '失败',
}

// 任务状态颜色映射
const TASK_STATUS_COLORS: Record<string, string> = {
  PENDING: 'gold',
  IN_PROGRESS: 'processing',
  COMPLETED: 'success',
  OVERDUE: 'error',
  FAILED: 'red',
}

// 任务状态文本映射
const TASK_STATUS_TEXTS: Record<string, string> = {
  PENDING: '待处理',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  OVERDUE: '已逾期',
  FAILED: '失败',
}

/**
 * 获取事项状态颜色
 */
export function getMatterStatusColor(status: string): string {
  return MATTER_STATUS_COLORS[status] || 'default'
}

/**
 * 获取事项状态文本
 */
export function getMatterStatusText(status: string): string {
  return MATTER_STATUS_TEXTS[status] || status
}

/**
 * 获取通知状态颜色
 */
export function getNotificationStatusColor(status: string): string {
  return NOTIFICATION_STATUS_COLORS[status] || 'default'
}

/**
 * 获取通知状态文本
 */
export function getNotificationStatusText(status: string): string {
  return NOTIFICATION_STATUS_TEXTS[status] || status
}

/**
 * 获取任务状态颜色
 */
export function getTaskStatusColor(status: string): string {
  return TASK_STATUS_COLORS[status] || 'default'
}

/**
 * 获取任务状态文本
 */
export function getTaskStatusText(status: string): string {
  return TASK_STATUS_TEXTS[status] || status
}

/**
 * 通用状态颜色获取函数（向后兼容）
 * @param status 状态值
 * @param type 状态类型 - 'matter' | 'notification' | 'task'
 */
export function getStatusColor(status: string, type: 'matter' | 'notification' | 'task' = 'matter'): string {
  switch (type) {
    case 'notification':
      return getNotificationStatusColor(status)
    case 'task':
      return getTaskStatusColor(status)
    default:
      return getMatterStatusColor(status)
  }
}

/**
 * 通用状态文本获取函数
 * @param status 状态值
 * @param type 状态类型 - 'matter' | 'notification' | 'task'
 */
export function getStatusText(status: string, type: 'matter' | 'notification' | 'task' = 'matter'): string {
  switch (type) {
    case 'notification':
      return getNotificationStatusText(status)
    case 'task':
      return getTaskStatusText(status)
    default:
      return getMatterStatusText(status)
  }
}
