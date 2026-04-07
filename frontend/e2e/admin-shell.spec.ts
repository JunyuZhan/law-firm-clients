import { expect, test } from '@playwright/test'
import { mockAdminSession, mockAdminShellApis, mockMatterList, mockPublicConfig } from './fixtures'

test.describe('Admin shell smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)
    await mockAdminSession(page)
    await mockAdminShellApis(page)
    await mockMatterList(page)
  })

  test('renders the admin layout and matter workspace', async ({ page }) => {
    await page.goto('/admin/matters')

    await expect(page).toHaveURL(/\/admin\/matters$/)
    await expect(page.getByText('项目列表').first()).toBeVisible()
    await expect(page.getByText('项目数据表')).toBeVisible()
    await expect(page.locator('.header .page-title')).toHaveText('项目列表')
    await expect(page.getByText('测试管理员')).toBeVisible()
  })

  test('keeps the matter workspace usable on mobile', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/admin/matters')

    await expect(page).toHaveURL(/\/admin\/matters$/)
    await expect(page.getByText('项目列表').first()).toBeVisible()
    await expect(page.getByText('项目数据表')).toBeVisible()
  })
})
