import { expect, test } from '@playwright/test'
import { mockPublicConfig } from './fixtures'

test.describe('Portal smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)
  })

  test('renders the portal as a focused entry gateway', async ({ page }) => {
    await page.goto('/portal')

    await expect(page.locator('.logo-text h1')).toContainText('汉科客户服务系统')
    await expect(page.locator('.main-title')).toContainText('汉科客户服务')
    await expect(page.locator('.hero-value-line')).toContainText('专业事项')
    await expect(page.locator('.footer-meta')).toContainText('请通过承办律师获取访问链接')
    await expect(page.getByRole('button', { name: '帮助中心' })).toBeVisible()
    await page.getByText('链接打不开？粘贴完整访问链接').click()
    await expect(page.getByRole('button', { name: '继续访问' })).toBeVisible()
  })

  test('stays readable on a narrow viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portal')

    await expect(page.locator('.logo-text h1')).toContainText('汉科客户服务系统')
    await expect(page.locator('.main-title')).toContainText('汉科客户服务')
    await expect(page.locator('.footer-meta')).toContainText('请通过承办律师获取访问链接')
    await expect(page.getByRole('button', { name: '帮助中心' })).toBeVisible()
    await page.getByText('链接打不开？粘贴完整访问链接').click()
    await expect(page.getByRole('button', { name: '继续访问' })).toBeVisible()
  })
})
