import { expect, test } from '@playwright/test'
import { mockPublicConfig } from './fixtures'

test.describe('Admin login smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)

    await page.route('**/api/admin/auth/captcha**', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 200,
          success: true,
          message: 'ok',
          data: {
            captchaId: 'playwright-captcha',
            captchaImage: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Wn6zkAAAAAASUVORK5CYII=',
          },
        }),
      })
    })
  })

  test('renders the focused admin login form', async ({ page }) => {
    await page.goto('/admin/login')

    await expect(page.locator('.brand-label').first()).toHaveText('汉科律师事务所')
    await expect(page.getByRole('heading', { level: 1, name: '汉科律师事务所管理后台' })).toBeVisible()
    await expect(page.getByPlaceholder('请输入用户名')).toBeVisible()
    await expect(page.getByPlaceholder('请输入密码')).toBeVisible()
    await expect(page.getByPlaceholder('请输入验证码')).toBeVisible()
    await expect(page.getByRole('button', { name: '进入管理门户' })).toBeVisible()
    await expect(page.getByAltText('验证码')).toBeVisible()
  })

  test('keeps the login screen usable on mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/admin/login')

    await expect(page.getByRole('heading', { level: 1, name: '汉科律师事务所管理后台' })).toBeVisible()
    await expect(page.getByPlaceholder('请输入用户名')).toBeVisible()
    await expect(page.getByPlaceholder('请输入验证码')).toBeVisible()
    await expect(page.getByRole('button', { name: '进入管理门户' })).toBeVisible()
  })
})
