import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getCaptcha,
  login,
  getCurrentUser,
  logout,
  changePassword,
  isCurrentUserResponse,
  type LoginRequest,
  type UserInfo,
  type CurrentUserResponse,
} from '../auth'

// Mock request module
vi.mock('../request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  }
}))

import request from '../request'

describe('auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('isCurrentUserResponse', () => {
    it('returns true for CurrentUserResponse', () => {
      const response: CurrentUserResponse = {
        user: { id: 1, username: 'test' },
        csrfToken: 'csrf',
      }
      expect(isCurrentUserResponse(response)).toBe(true)
    })

    it('returns false for UserInfo', () => {
      const userInfo: UserInfo = { id: 1, username: 'test' }
      expect(isCurrentUserResponse(userInfo)).toBe(false)
    })
  })

  describe('getCaptcha', () => {
    it('calls request.get with captchaId when provided', async () => {
      const mockData = { captchaId: 'id', captchaImage: 'base64' }
      const mockApiResponse = { code: 200, message: 'success', data: mockData, success: true }
      vi.mocked(request.get).mockResolvedValue(mockApiResponse)

      const result = await getCaptcha('test-id')

      expect(request.get).toHaveBeenCalledWith('/api/admin/auth/captcha?captchaId=test-id')
      expect(result).toEqual(mockData)
    })

    it('calls request.get without captchaId when not provided', async () => {
      const mockData = { captchaId: 'new-id', captchaImage: 'base64' }
      const mockApiResponse = { code: 200, message: 'success', data: mockData, success: true }
      vi.mocked(request.get).mockResolvedValue(mockApiResponse)

      const result = await getCaptcha()

      expect(request.get).toHaveBeenCalledWith('/api/admin/auth/captcha')
      expect(result).toEqual(mockData)
    })
  })

  describe('login', () => {
    it('calls request.post with login data', async () => {
      const loginData: LoginRequest = {
        username: 'user',
        password: 'pass',
        captchaId: 'cap-id',
        captchaText: 'cap-text',
      }
      const mockData = {
        token: 'token',
        user: { id: 1, username: 'user' },
        csrfToken: 'csrf',
      }
      const mockApiResponse = { code: 200, message: 'success', data: mockData, success: true }
      vi.mocked(request.post).mockResolvedValue(mockApiResponse)

      const result = await login(loginData)

      expect(request.post).toHaveBeenCalledWith('/api/admin/auth/login', loginData)
      expect(result).toEqual(mockData)
    })
  })

  describe('getCurrentUser', () => {
    it('calls request.get and returns response', async () => {
      const mockData = { id: 1, username: 'user' }
      const mockApiResponse = { code: 200, message: 'success', data: mockData, success: true }
      vi.mocked(request.get).mockResolvedValue(mockApiResponse)

      const result = await getCurrentUser()

      expect(request.get).toHaveBeenCalledWith('/api/admin/auth/me')
      expect(result).toEqual(mockData)
    })
  })

  describe('logout', () => {
    it('calls request.post to logout', async () => {
      const mockApiResponse = { code: 200, message: 'success', data: null, success: true }
      vi.mocked(request.post).mockResolvedValue(mockApiResponse)

      await logout()

      expect(request.post).toHaveBeenCalledWith('/api/admin/auth/logout')
    })
  })

  describe('changePassword', () => {
    it('calls request.post with password data', async () => {
      const passwordData = {
        oldPassword: 'old',
        newPassword: 'new',
      }
      const mockApiResponse = { code: 200, message: 'success', data: null, success: true }
      vi.mocked(request.post).mockResolvedValue(mockApiResponse)

      await changePassword(passwordData)

      expect(request.post).toHaveBeenCalledWith('/api/admin/auth/change-password', passwordData)
    })
  })
})