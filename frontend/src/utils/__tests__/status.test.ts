import { describe, it, expect } from 'vitest'
import {
  getMatterStatusColor,
  getMatterStatusText,
  getNotificationStatusColor,
  getNotificationStatusText,
  getTaskStatusColor,
  getTaskStatusText,
  getStatusColor,
  getStatusText,
} from '../status'

describe('status utilities', () => {
  describe('getMatterStatusColor', () => {
    it('returns correct color for known matter status', () => {
      expect(getMatterStatusColor('ACTIVE')).toBe('green')
      expect(getMatterStatusColor('EXPIRED')).toBe('orange')
      expect(getMatterStatusColor('REVOKED')).toBe('red')
      expect(getMatterStatusColor('IN_PROGRESS')).toBe('blue')
      expect(getMatterStatusColor('COMPLETED')).toBe('green')
      expect(getMatterStatusColor('PENDING')).toBe('blue')
    })

    it('returns default color for unknown matter status', () => {
      expect(getMatterStatusColor('UNKNOWN')).toBe('default')
    })
  })

  describe('getMatterStatusText', () => {
    it('returns correct text for known matter status', () => {
      expect(getMatterStatusText('ACTIVE')).toBe('活跃')
      expect(getMatterStatusText('EXPIRED')).toBe('已过期')
      expect(getMatterStatusText('REVOKED')).toBe('已撤销')
      expect(getMatterStatusText('IN_PROGRESS')).toBe('进行中')
      expect(getMatterStatusText('COMPLETED')).toBe('已完成')
      expect(getMatterStatusText('PENDING')).toBe('待处理')
    })

    it('returns original status for unknown matter status', () => {
      expect(getMatterStatusText('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getNotificationStatusColor', () => {
    it('returns correct color for known notification status', () => {
      expect(getNotificationStatusColor('PENDING')).toBe('blue')
      expect(getNotificationStatusColor('SUCCESS')).toBe('green')
      expect(getNotificationStatusColor('FAILED')).toBe('red')
    })

    it('returns default color for unknown notification status', () => {
      expect(getNotificationStatusColor('UNKNOWN')).toBe('default')
    })
  })

  describe('getNotificationStatusText', () => {
    it('returns correct text for known notification status', () => {
      expect(getNotificationStatusText('PENDING')).toBe('待发送')
      expect(getNotificationStatusText('SUCCESS')).toBe('成功')
      expect(getNotificationStatusText('FAILED')).toBe('失败')
    })

    it('returns original status for unknown notification status', () => {
      expect(getNotificationStatusText('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getTaskStatusColor', () => {
    it('returns correct color for known task status', () => {
      expect(getTaskStatusColor('PENDING')).toBe('gold')
      expect(getTaskStatusColor('IN_PROGRESS')).toBe('processing')
      expect(getTaskStatusColor('COMPLETED')).toBe('success')
      expect(getTaskStatusColor('OVERDUE')).toBe('error')
      expect(getTaskStatusColor('FAILED')).toBe('red')
    })

    it('returns default color for unknown task status', () => {
      expect(getTaskStatusColor('UNKNOWN')).toBe('default')
    })
  })

  describe('getTaskStatusText', () => {
    it('returns correct text for known task status', () => {
      expect(getTaskStatusText('PENDING')).toBe('待处理')
      expect(getTaskStatusText('IN_PROGRESS')).toBe('进行中')
      expect(getTaskStatusText('COMPLETED')).toBe('已完成')
      expect(getTaskStatusText('OVERDUE')).toBe('已逾期')
      expect(getTaskStatusText('FAILED')).toBe('失败')
    })

    it('returns original status for unknown task status', () => {
      expect(getTaskStatusText('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getStatusColor', () => {
    it('returns matter status color by default', () => {
      expect(getStatusColor('ACTIVE')).toBe('green')
      expect(getStatusColor('UNKNOWN')).toBe('default')
    })

    it('returns notification status color when type is notification', () => {
      expect(getStatusColor('PENDING', 'notification')).toBe('blue')
      expect(getStatusColor('SUCCESS', 'notification')).toBe('green')
    })

    it('returns task status color when type is task', () => {
      expect(getStatusColor('PENDING', 'task')).toBe('gold')
      expect(getStatusColor('IN_PROGRESS', 'task')).toBe('processing')
    })
  })

  describe('getStatusText', () => {
    it('returns matter status text by default', () => {
      expect(getStatusText('ACTIVE')).toBe('活跃')
      expect(getStatusText('UNKNOWN')).toBe('UNKNOWN')
    })

    it('returns notification status text when type is notification', () => {
      expect(getStatusText('PENDING', 'notification')).toBe('待发送')
      expect(getStatusText('SUCCESS', 'notification')).toBe('成功')
    })

    it('returns task status text when type is task', () => {
      expect(getStatusText('PENDING', 'task')).toBe('待处理')
      expect(getStatusText('IN_PROGRESS', 'task')).toBe('进行中')
    })
  })
})