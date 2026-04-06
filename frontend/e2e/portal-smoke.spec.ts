import { expect, test } from '@playwright/test'
import { mockPublicConfig } from './fixtures'

test.describe('Portal smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)
  })

  test('renders the portal entry with brand copy', async ({ page }) => {
    await page.goto('/portal')

    await expect(page.locator('.main-title')).toContainText('专业事项')
    await expect(page.locator('.main-title')).toContainText('客户入口')
    await expect(page.getByText('项目访问入口')).toBeVisible()
    await expect(page.getByText('单链接访问')).toBeVisible()
    await expect(page.getByText('安全令牌验证')).toBeVisible()
  })

  test('stays readable on a narrow viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portal')

    await expect(page.locator('.main-title')).toContainText('专业事项')
    await expect(page.locator('.main-title')).toContainText('客户入口')
    await expect(page.getByText('项目访问入口')).toBeVisible()
    await expect(page.getByRole('button', { name: '进入项目' })).toBeVisible()
  })
})
