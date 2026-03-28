import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { 
  getItem, 
  getJSON, 
  setItem, 
  setJSON, 
  removeItem, 
  removeItems, 
  clear, 
  isAvailable 
} from '../storage'

// Mock logger to avoid console output
vi.mock('../logger', () => ({
  default: {
    warn: vi.fn(),
    error: vi.fn(),
    debug: vi.fn(),
    info: vi.fn(),
  },
  logger: {
    warn: vi.fn(),
    error: vi.fn(),
    debug: vi.fn(),
    info: vi.fn(),
  }
}))

describe('storage utilities', () => {
  let mockLocalStorage: { [key: string]: string }
  let originalLocalStorage: Storage

  beforeEach(() => {
    // Save original localStorage
    originalLocalStorage = global.localStorage
    
    // Create mock localStorage
    mockLocalStorage = {}
    const mockStorage: Storage = {
      getItem: vi.fn((key: string) => mockLocalStorage[key] ?? null),
      setItem: vi.fn((key: string, value: string) => {
        mockLocalStorage[key] = value
      }),
      removeItem: vi.fn((key: string) => {
        delete mockLocalStorage[key]
      }),
      clear: vi.fn(() => {
        mockLocalStorage = {}
      }),
      key: vi.fn(() => null),
      length: 0,
    }
    
    // Replace global localStorage with mock
    Object.defineProperty(global, 'localStorage', {
      value: mockStorage,
      writable: true,
    })
  })

  afterEach(() => {
    // Restore original localStorage
    Object.defineProperty(global, 'localStorage', {
      value: originalLocalStorage,
      writable: true,
    })
    vi.clearAllMocks()
  })

  describe('getItem', () => {
    it('returns stored value when key exists', () => {
      mockLocalStorage['testKey'] = 'testValue'
      expect(getItem('testKey')).toBe('testValue')
    })

    it('returns default value when key does not exist', () => {
      expect(getItem('nonExistentKey', 'default')).toBe('default')
      expect(getItem('nonExistentKey')).toBe(null)
    })

    it('returns default value when localStorage throws error', () => {
      // Mock localStorage.getItem to throw error
      const mockGetItem = vi.spyOn(global.localStorage, 'getItem')
      mockGetItem.mockImplementation(() => {
        throw new Error('Storage error')
      })
      
      expect(getItem('testKey', 'default')).toBe('default')
      expect(getItem('testKey')).toBe(null)
    })
  })

  describe('getJSON', () => {
    it('returns parsed JSON when key exists', () => {
      const data = { foo: 'bar', num: 123 }
      mockLocalStorage['testKey'] = JSON.stringify(data)
      expect(getJSON('testKey')).toEqual(data)
    })

    it('returns default value when key does not exist', () => {
      expect(getJSON('nonExistentKey', { default: true })).toEqual({ default: true })
      expect(getJSON('nonExistentKey')).toBe(null)
    })

    it('returns default value when JSON parsing fails', () => {
      mockLocalStorage['invalidJSON'] = '{ invalid json'
      expect(getJSON('invalidJSON', { default: true })).toEqual({ default: true })
      expect(getJSON('invalidJSON')).toBe(null)
    })

    it('returns default value when localStorage throws error', () => {
      const mockGetItem = vi.spyOn(global.localStorage, 'getItem')
      mockGetItem.mockImplementation(() => {
        throw new Error('Storage error')
      })
      
      expect(getJSON('testKey', { default: true })).toEqual({ default: true })
    })
  })

  describe('setItem', () => {
    it('returns true when storage succeeds', () => {
      expect(setItem('testKey', 'testValue')).toBe(true)
      expect(global.localStorage.setItem).toHaveBeenCalledWith('testKey', 'testValue')
    })

    it('returns false when storage fails', () => {
      const mockSetItem = vi.spyOn(global.localStorage, 'setItem')
      mockSetItem.mockImplementation(() => {
        throw new Error('Storage error')
      })
      
      expect(setItem('testKey', 'testValue')).toBe(false)
    })
  })

  describe('setJSON', () => {
    it('returns true when storage succeeds', () => {
      const data = { foo: 'bar' }
      expect(setJSON('testKey', data)).toBe(true)
      expect(global.localStorage.setItem).toHaveBeenCalledWith('testKey', JSON.stringify(data))
    })

    it('returns false when storage fails', () => {
      const mockSetItem = vi.spyOn(global.localStorage, 'setItem')
      mockSetItem.mockImplementation(() => {
        throw new Error('Storage error')
      })
      
      expect(setJSON('testKey', { foo: 'bar' })).toBe(false)
    })
  })

  describe('removeItem', () => {
    it('returns true when removal succeeds', () => {
      mockLocalStorage['testKey'] = 'value'
      expect(removeItem('testKey')).toBe(true)
      expect(global.localStorage.removeItem).toHaveBeenCalledWith('testKey')
    })

    it('returns false when removal fails', () => {
      const mockRemoveItem = vi.spyOn(global.localStorage, 'removeItem')
      mockRemoveItem.mockImplementation(() => {
        throw new Error('Storage error')
      })
      
      expect(removeItem('testKey')).toBe(false)
    })
  })

  describe('removeItems', () => {
    it('returns count of successfully removed items', () => {
      mockLocalStorage['key1'] = 'value1'
      mockLocalStorage['key2'] = 'value2'
      mockLocalStorage['key3'] = 'value3'
      
      // Mock removeItem to fail for key2
      const originalRemoveItem = global.localStorage.removeItem
      const mockRemoveItem = vi.spyOn(global.localStorage, 'removeItem')
      mockRemoveItem.mockImplementation((key: string) => {
        if (key === 'key2') {
          throw new Error('Failed')
        }
        originalRemoveItem(key)
      })
      
      expect(removeItems(['key1', 'key2', 'key3'])).toBe(2)
    })
  })

  describe('clear', () => {
    it('returns true when clear succeeds', () => {
      mockLocalStorage['key1'] = 'value1'
      expect(clear()).toBe(true)
      expect(global.localStorage.clear).toHaveBeenCalled()
    })

    it('returns false when clear fails', () => {
      const mockClear = vi.spyOn(global.localStorage, 'clear')
      mockClear.mockImplementation(() => {
        throw new Error('Storage error')
      })
      
      expect(clear()).toBe(false)
    })
  })

  describe('isAvailable', () => {
    it('returns true when localStorage is available', () => {
      expect(isAvailable()).toBe(true)
    })

    it('returns false when localStorage is not available', () => {
      // Temporarily make localStorage throw error
      const mockSetItem = vi.spyOn(global.localStorage, 'setItem')
      mockSetItem.mockImplementation(() => {
        throw new Error('Storage disabled')
      })
      
      expect(isAvailable()).toBe(false)
    })
  })
})