import { expect, test } from '@playwright/test'
import { mockPublicConfig } from './fixtures'

test.describe('Portal smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)
  })

  test('renders public portal copy from config', async ({ page }) => {
    await page.goto('/portal')

    await expect(page.locator('.logo-text').getByRole('heading', { level: 1 })).toContainText('汉科律师事务所')
    await expect(page.locator('.logo-system-label')).toContainText('汉科客户服务')
    await expect(page.locator('.portal-slogan')).toContainText('专业事项，一个清晰的客户入口')
    await expect(page.locator('.portal-eyebrow')).toContainText('Hanke Client Portal')
    await expect(page.locator('.portal-note')).toContainText('具体项目内容需通过承办律师向您发送的专属链接访问。')
    await expect(page.getByRole('link', { name: '工作人员入口' })).toBeVisible()
  })

  test('stays readable on a narrow viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portal')

    await expect(page.locator('.logo-text').getByRole('heading', { level: 1 })).toContainText('汉科律师事务所')
    await expect(page.locator('.logo-system-label')).toContainText('汉科客户服务')
    await expect(page.locator('.portal-note')).toContainText('具体项目内容需通过承办律师向您发送的专属链接访问。')
  })
})
