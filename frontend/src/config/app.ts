/**
 * 应用配置 - Fallback 默认值
 * 
 * 配置优先级：后台配置 > 环境变量 > 代码默认值
 * 
 * 注意：这些值仅作为 fallback，实际配置请在【后台管理 -> 系统配置】页面设置
 * 前端启动时会从后台 API 获取配置并覆盖这些默认值（见 stores/appConfig.ts）
 */
import logger from '@/utils/logger'

// 系统名称
export const APP_NAME = import.meta.env.VITE_APP_NAME || '律师事务所客户服务系统'

// 系统简称
export const APP_SHORT_NAME = import.meta.env.VITE_APP_SHORT_NAME || '律所客服系统'

// 系统英文名称
export const APP_NAME_EN = import.meta.env.VITE_APP_NAME_EN || 'Guizhou Weidi Law Firm Client Service System'

// 系统英文简称
export const APP_SHORT_NAME_EN = import.meta.env.VITE_APP_SHORT_NAME_EN || 'Professional Service'

// 客户服务门户标题（如果未配置，则使用系统名称）
export const PORTAL_TITLE = import.meta.env.VITE_PORTAL_TITLE || APP_NAME

// 版权信息（使用系统完整名称）
export const COPYRIGHT_TEXT = APP_NAME

// 系统标语
export const APP_SLOGAN = import.meta.env.VITE_APP_SLOGAN || '专业 · 诚信 · 高效'

// 律师事务所名称（用于蓝色背景条和版权信息）
export const LAW_FIRM_NAME = import.meta.env.VITE_LAW_FIRM_NAME || (() => {
  const name = APP_NAME
  // 如果包含"客户服务系统"，则提取前面的部分
  if (name.includes('客户服务系统')) {
    return name.replace('客户服务系统', '').trim()
  }
  return name
})()

// 律师事务所官网链接（用于"关于我们"部分的链接）
// 必须包含协议前缀（http:// 或 https://），由部署人员根据实际情况填写
export const LAW_FIRM_WEBSITE = (() => {
  const url = import.meta.env.VITE_LAW_FIRM_WEBSITE || ''
  if (!url || url.trim() === '') return ''
  const trimmedUrl = url.trim()
  // 如果已经有 http:// 或 https://，直接返回
  if (trimmedUrl.startsWith('http://') || trimmedUrl.startsWith('https://')) {
    return trimmedUrl
  }
  // 如果没有协议前缀，返回空字符串（不显示链接）
  // 部署人员需要在配置中明确指定 http:// 或 https://
  logger.warn('VITE_LAW_FIRM_WEBSITE 必须包含协议前缀（http:// 或 https://），当前配置将被忽略:', trimmedUrl)
  return ''
})()

// Logo配置
export const LOGO_URL = import.meta.env.VITE_LOGO_URL || '/logo.png'
export const LOGO_COLLAPSED_URL = import.meta.env.VITE_LOGO_COLLAPSED_URL || '/logo.png'

// ICP备案号（可选，不配置则不显示）
export const ICP_LICENSE = import.meta.env.VITE_ICP_LICENSE || ''
