import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { formatDate, formatDateOnly, formatTimeOnly, isExpired, getRelativeTime } from '../date'

describe('date utilities', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    // Set fixed date: 2024-01-15T12:00:00Z
    vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('formatDate', () => {
    it('returns default format when no format specified', () => {
      // Use date string without timezone to avoid timezone conversion
      expect(formatDate('2024-01-01 10:30:00')).toBe('2024-01-01 10:30:00')
    })

    it('returns custom format when specified', () => {
      expect(formatDate('2024-01-01 10:30:00', 'YYYY-MM-DD')).toBe('2024-01-01')
    })

    it('returns dash for empty date', () => {
      expect(formatDate()).toBe('-')
      expect(formatDate('')).toBe('-')
      expect(formatDate(null as any)).toBe('-')
    })
  })

  describe('formatDateOnly', () => {
    it('returns date only format', () => {
      expect(formatDateOnly('2024-01-01 10:30:00')).toBe('2024-01-01')
    })
  })

  describe('formatTimeOnly', () => {
    it('returns time only format', () => {
      expect(formatTimeOnly('2024-01-01 10:30:00')).toBe('10:30:00')
    })
  })

  describe('isExpired', () => {
    it('returns false when expiresAt is empty', () => {
      expect(isExpired()).toBe(false)
      expect(isExpired('')).toBe(false)
    })

    it('returns true when date is before now', () => {
      // expiresAt is 2024-01-10, now is 2024-01-15
      expect(isExpired('2024-01-10 00:00:00')).toBe(true)
    })

    it('returns false when date is after now', () => {
      // expiresAt is 2024-01-20, now is 2024-01-15
      expect(isExpired('2024-01-20 00:00:00')).toBe(false)
    })
  })

  describe('getRelativeTime', () => {
    it('returns dash for empty date', () => {
      expect(getRelativeTime()).toBe('-')
      expect(getRelativeTime('')).toBe('-')
    })

    it('returns "刚刚" for same minute', () => {
      // 0 minutes difference
      vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
      expect(getRelativeTime('2024-01-15T12:00:00Z')).toBe('刚刚')
    })

    it('returns minutes later for future within hour', () => {
      // 30 minutes later
      expect(getRelativeTime('2024-01-15T12:30:00Z')).toBe('30分钟后')
    })

    it('returns minutes ago for past within hour', () => {
      // 30 minutes ago
      expect(getRelativeTime('2024-01-15T11:30:00Z')).toBe('30分钟前')
    })

    it('returns hours later for future within day', () => {
      // 5 hours later
      expect(getRelativeTime('2024-01-15T17:00:00Z')).toBe('5小时后')
    })

    it('returns hours ago for past within day', () => {
      // 5 hours ago
      expect(getRelativeTime('2024-01-15T07:00:00Z')).toBe('5小时前')
    })

    it('returns days later for future within month', () => {
      // 3 days later
      expect(getRelativeTime('2024-01-18T12:00:00Z')).toBe('3天后')
    })

    it('returns days ago for past within month', () => {
      // 3 days ago
      expect(getRelativeTime('2024-01-12T12:00:00Z')).toBe('3天前')
    })

    it('returns formatted date for far future', () => {
      // 40 days later -> beyond month threshold
      expect(getRelativeTime('2024-02-24T12:00:00Z')).toBe('2024-02-24')
    })

    it('returns formatted date for far past', () => {
      // 40 days ago
      expect(getRelativeTime('2023-12-06T12:00:00Z')).toBe('2023-12-06')
    })
  })
})