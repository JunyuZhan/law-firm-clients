import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import logger from '../logger'

type ConsoleMethod = (...args: unknown[]) => void

describe('logger', () => {
  let originalConsoleDebug: ConsoleMethod
  let originalConsoleInfo: ConsoleMethod
  let originalConsoleWarn: ConsoleMethod
  let originalConsoleError: ConsoleMethod

  beforeEach(() => {
    // Save original console methods
    originalConsoleDebug = globalThis.console.debug.bind(globalThis.console)
    originalConsoleInfo = globalThis.console.info.bind(globalThis.console)
    originalConsoleWarn = globalThis.console.warn.bind(globalThis.console)
    originalConsoleError = globalThis.console.error.bind(globalThis.console)
    
    // Mock console methods
    globalThis.console.debug = vi.fn()
    globalThis.console.info = vi.fn()
    globalThis.console.warn = vi.fn()
    globalThis.console.error = vi.fn()
  })

  afterEach(() => {
    // Restore original console methods
    globalThis.console.debug = originalConsoleDebug
    globalThis.console.info = originalConsoleInfo
    globalThis.console.warn = originalConsoleWarn
    globalThis.console.error = originalConsoleError
    vi.clearAllMocks()
  })

  it('has all log level methods', () => {
    expect(typeof logger.debug).toBe('function')
    expect(typeof logger.info).toBe('function')
    expect(typeof logger.warn).toBe('function')
    expect(typeof logger.error).toBe('function')
  })

  it('calls console.debug for debug level', () => {
    logger.debug('debug message', { extra: 'data' })
    expect(globalThis.console.debug).toHaveBeenCalledWith('debug message', { extra: 'data' })
  })

  it('calls console.info for info level', () => {
    logger.info('info message')
    expect(globalThis.console.info).toHaveBeenCalledWith('info message')
  })

  it('calls console.warn for warn level', () => {
    logger.warn('warn message')
    expect(globalThis.console.warn).toHaveBeenCalledWith('warn message')
  })

  it('calls console.error for error level', () => {
    logger.error('error message')
    expect(globalThis.console.error).toHaveBeenCalledWith('error message')
  })

  it('handles multiple arguments', () => {
    logger.debug('arg1', 'arg2', 3)
    expect(globalThis.console.debug).toHaveBeenCalledWith('arg1', 'arg2', 3)
  })
})
