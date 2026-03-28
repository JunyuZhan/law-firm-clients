/**
 * 统一的日志工具
 * 替代 console.* 方法，支持日志级别控制和生产环境禁用
 */

type LogLevel = 'debug' | 'info' | 'warn' | 'error'

interface Logger {
  debug: (...args: unknown[]) => void
  info: (...args: unknown[]) => void
  warn: (...args: unknown[]) => void
  error: (...args: unknown[]) => void
}

// 获取当前日志级别（从环境变量或默认值）
const getLogLevel = (): LogLevel => {
  const envLevel = import.meta.env.VITE_LOG_LEVEL?.toLowerCase()
  if (envLevel && ['debug', 'info', 'warn', 'error'].includes(envLevel)) {
    return envLevel as LogLevel
  }
  // 生产环境默认仅输出 error（配合 terser drop_console 双重防护）
  return import.meta.env.PROD ? 'error' : 'debug'
}

const currentLogLevel = getLogLevel()

// 日志级别优先级
const logLevelPriority: Record<LogLevel, number> = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3,
}

// 检查是否应该输出日志
const shouldLog = (level: LogLevel): boolean => {
  return logLevelPriority[level] >= logLevelPriority[currentLogLevel]
}

// 创建日志函数
// 注意：shouldLog已通过currentLogLevel处理生产环境逻辑
// 生产环境默认logLevel='warn'，debug/info级别自动被过滤，无需重复检查
const createLogger = (level: LogLevel): (...args: unknown[]) => void => {
  return (...args: unknown[]) => {
    if (!shouldLog(level)) {
      return
    }
    
    // 使用原生 console 方法
    const consoleRef = globalThis.console
    const consoleMethod = consoleRef[level] || consoleRef.log
    consoleMethod(...args)
  }
}

// 导出日志工具
export const logger: Logger = {
  debug: createLogger('debug'),
  info: createLogger('info'),
  warn: createLogger('warn'),
  error: createLogger('error'),
}

// 默认导出
export default logger
