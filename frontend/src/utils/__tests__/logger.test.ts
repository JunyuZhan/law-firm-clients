import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import logger from '../logger'

describe('logger', () => {
  let originalConsoleDebug: any
  let originalConsoleInfo: any
  let originalConsoleWarn: any
  let originalConsoleError: any

  beforeEach(() => {
    // Save original console methods
    originalConsoleDebug = console.debug
    originalConsoleInfo = console.info
    originalConsoleWarn = console.warn
    originalConsoleError = console.error
    
    // Mock console methods
    console.debug = vi.fn()
    console.info = vi.fn()
    console.warn = vi.fn()
    console.error = vi.fn()
  })

  afterEach(() => {
    // Restore original console methods
    console.debug = originalConsoleDebug
    console.info = originalConsoleInfo
    console.warn = originalConsoleWarn
    console.error = originalConsoleError
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
    expect(console.debug).toHaveBeenCalledWith('debug message', { extra: 'data' })
  })

  it('calls console.info for info level', () => {
    logger.info('info message')
    expect(console.info).toHaveBeenCalledWith('info message')
  })

  it('calls console.warn for warn level', () => {
    logger.warn('warn message')
    expect(console.warn).toHaveBeenCalledWith('warn message')
  })

  it('calls console.error for error level', () => {
    logger.error('error message')
    expect(console.error).toHaveBeenCalledWith('error message')
  })

  it('handles multiple arguments', () => {
    logger.debug('arg1', 'arg2', 3)
    expect(console.debug).toHaveBeenCalledWith('arg1', 'arg2', 3)
  })
})