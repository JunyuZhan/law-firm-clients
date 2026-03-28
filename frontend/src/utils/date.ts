/**
 * 日期工具函数
 */
import dayjs from 'dayjs'

/**
 * 格式化日期时间
 * @param date 日期字符串
 * @param format 格式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期字符串，如果输入为空则返回 '-'
 */
export function formatDate(date?: string, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!date) return '-'
  return dayjs(date).format(format)
}

/**
 * 格式化日期（不含时间）
 * @param date 日期字符串
 * @returns 格式化后的日期字符串
 */
export function formatDateOnly(date?: string): string {
  return formatDate(date, 'YYYY-MM-DD')
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
  
  if (diffMinutes >= 0 && diffMinutes < 60) {
    return diffMinutes === 0 ? '刚刚' : `${diffMinutes}分钟后`
  } else if (diffMinutes < 0 && diffMinutes > -60) {
    return `${Math.abs(diffMinutes)}分钟前`
  } else if (diffHours >= 0 && diffHours < 24) {
    return `${diffHours}小时后`
  } else if (diffHours < 0 && diffHours > -24) {
    return `${Math.abs(diffHours)}小时前`
  } else if (diffDays >= 0 && diffDays < 30) {
    return `${diffDays}天后`
  } else if (diffDays < 0 && diffDays > -30) {
    return `${Math.abs(diffDays)}天前`
  } else {
    return formatDate(date, 'YYYY-MM-DD')
  }
}
