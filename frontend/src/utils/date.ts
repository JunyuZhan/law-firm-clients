/**
 * 日期工具函数
 */
import dayjs from 'dayjs'

/**
 * 格式化日期时间
 * @param date 日期字符串
 * @param format 格式 (仅用于降级或特定格式需求)
 * @returns 格式化后的日期字符串，如果输入为空则返回 '-'
 */
export function formatDate(date?: string, format?: string): string {
  if (!date) return '-'
  
  if (!format || format === 'YYYY-MM-DD HH:mm:ss') {
    return new Intl.DateTimeFormat('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).format(new Date(date)).replace(/\//g, '-')
  }
  
  return dayjs(date).format(format)
}

/**
 * 格式化日期（不含时间）
 * @param date 日期字符串
 * @returns 格式化后的日期字符串
 */
export function formatDateOnly(date?: string): string {
  if (!date) return '-'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(date)).replace(/\//g, '-')
}

/**
 * 格式化时间（不含日期）
 * @param date 日期字符串
 * @returns 格式化后的时间字符串
 */
export function formatTimeOnly(date?: string): string {
  return formatDate(date, 'HH:mm:ss')
}

/**
 * 判断日期是否已过期
 * @param expiresAt 过期时间字符串
 * @returns 是否已过期
 */
export function isExpired(expiresAt?: string): boolean {
  if (!expiresAt) return false
  return dayjs(expiresAt).isBefore(dayjs())
}

/**
 * 计算相对时间（如：3天前，2小时后）
 * @param date 日期字符串
 * @returns 相对时间描述
 */
export function getRelativeTime(date?: string): string {
  if (!date) return '-'
  
  const now = dayjs()
  const target = dayjs(date)
  const diffMinutes = target.diff(now, 'minute')
  const diffHours = target.diff(now, 'hour')
  const diffDays = target.diff(now, 'day')
  
  const rtf = new Intl.RelativeTimeFormat('zh-CN', { numeric: 'auto' })
  
  if (diffMinutes >= 0 && diffMinutes < 60) {
    return diffMinutes === 0 ? '刚刚' : rtf.format(diffMinutes, 'minute')
  } else if (diffMinutes < 0 && diffMinutes > -60) {
    return rtf.format(diffMinutes, 'minute')
  } else if (diffHours >= 0 && diffHours < 24) {
    return rtf.format(diffHours, 'hour')
  } else if (diffHours < 0 && diffHours > -24) {
    return rtf.format(diffHours, 'hour')
  } else if (diffDays >= 0 && diffDays < 30) {
    return rtf.format(diffDays, 'day')
  } else if (diffDays < 0 && diffDays > -30) {
    return rtf.format(diffDays, 'day')
  } else {
    return formatDate(date, 'YYYY-MM-DD')
  }
}
