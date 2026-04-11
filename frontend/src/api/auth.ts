import request from './request'

export interface LoginRequest {
  username: string
  password: string
  captchaId?: string
  captchaText?: string
}

export interface CaptchaResponse {
  captchaId: string
  captchaImage: string
}

export interface UserInfo {
  id: number
  username: string
  realName?: string
  email?: string
  lastLoginAt?: string
  superAdmin?: boolean
}

export interface LoginResponse {
  token: string
  user: UserInfo
  csrfToken?: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

// 新版本API响应格式（包含CSRF Token）
export interface CurrentUserResponse {
  user: UserInfo
  csrfToken: string
}

// 类型守卫：判断是否为新版本API响应
export function isCurrentUserResponse(response: UserInfo | CurrentUserResponse): response is CurrentUserResponse {
  return 'user' in response && 'csrfToken' in response
}

/**
 * 获取验证码
 */
export function getCaptcha(captchaId?: string): Promise<CaptchaResponse> {
  const url = captchaId
    ? `/api/admin/auth/captcha?captchaId=${captchaId}`
    : '/api/admin/auth/captcha'
  return request.get(url).then(res => res.data as CaptchaResponse)
}

/**
 * 管理员登录
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return request.post('/api/admin/auth/login', data).then(res => res.data as LoginResponse)
}

/**
 * 获取当前用户信息（返回用户信息和CSRF Token）
 * 使用 isCurrentUserResponse 类型守卫来判断响应格式
 */
export function getCurrentUser(): Promise<UserInfo | CurrentUserResponse> {
  return request.get('/api/admin/auth/me').then(res => res.data as UserInfo | CurrentUserResponse)
}

/**
 * 登出
 */
export function logout(): Promise<void> {
  return request.post('/api/admin/auth/logout').then(() => undefined)
}

/**
 * 修改密码
 */
export function changePassword(data: ChangePasswordRequest): Promise<void> {
  return request.post('/api/admin/auth/change-password', data).then(() => undefined)
}
