import { describe, expect, it } from 'vitest'
import {
  getPortalMatterStatusText,
  getPortalMatterStatusTone,
  getPortalNotificationStatusText,
  resolvePortalAccessErrorState,
} from '../portalState'

describe('portalState utilities', () => {
  describe('resolvePortalAccessErrorState', () => {
    it('returns invalid-token for recognized token failure messages', () => {
      expect(resolvePortalAccessErrorState(new Error('访问令牌已过期'))).toBe('invalid-token')
      expect(resolvePortalAccessErrorState(new Error('当前链接无效'))).toBe('invalid-token')
      expect(resolvePortalAccessErrorState(new Error('事项不存在'))).toBe('invalid-token')
      expect(resolvePortalAccessErrorState(new Error('访问权限已撤销'))).toBe('invalid-token')
    })

    it('falls back to missing-token for other errors', () => {
      expect(resolvePortalAccessErrorState(new Error('网络异常'))).toBe('missing-token')
      expect(resolvePortalAccessErrorState('unknown')).toBe('missing-token')
    })
  })

  describe('getPortalMatterStatusText', () => {
    it('returns portal-specific matter labels', () => {
      expect(getPortalMatterStatusText('ACTIVE')).toBe('有效')
      expect(getPortalMatterStatusText('EXPIRED')).toBe('已过期')
      expect(getPortalMatterStatusText('REVOKED')).toBe('已撤销')
    })

    it('returns the raw status for unknown values', () => {
      expect(getPortalMatterStatusText('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('getPortalMatterStatusTone', () => {
    it('returns tone mapping for known portal matter statuses', () => {
      expect(getPortalMatterStatusTone('ACTIVE')).toBe('success')
      expect(getPortalMatterStatusTone('EXPIRED')).toBe('warning')
      expect(getPortalMatterStatusTone('REVOKED')).toBe('danger')
    })

    it('returns primary for unknown values', () => {
      expect(getPortalMatterStatusTone('UNKNOWN')).toBe('primary')
    })
  })

  describe('getPortalNotificationStatusText', () => {
    it('returns portal notification labels', () => {
      expect(getPortalNotificationStatusText('SUCCESS')).toBe('发送成功')
      expect(getPortalNotificationStatusText('FAILED')).toBe('发送失败')
      expect(getPortalNotificationStatusText('PENDING')).toBe('发送中')
    })

    it('returns the raw status for unknown values', () => {
      expect(getPortalNotificationStatusText('UNKNOWN')).toBe('UNKNOWN')
    })
  })
})
