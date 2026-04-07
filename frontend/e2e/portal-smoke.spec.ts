import { expect, test } from '@playwright/test'
import { mockPublicConfig } from './fixtures'

test.describe('Portal smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)
  })

  test('renders the portal entry with slogan and centered hero copy', async ({ page }) => {
    await page.goto('/portal')

    await expect(page.locator('.logo-text h1')).toContainText('汉科律师事务所')
    await expect(page.locator('.logo-slogan')).toContainText('专业事项，一个清晰的客户入口')
    await expect(page.locator('.main-title')).toContainText('客户项目入口')
    await expect(page.locator('.hero-slogan')).toContainText('专业事项，一个清晰的客户入口')
    await expect(page.getByRole('button', { name: '进入项目' })).toBeVisible()
  })

  test('stays readable on a narrow viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portal')

    await expect(page.locator('.logo-text h1')).toContainText('汉科律师事务所')
    await expect(page.locator('.logo-slogan')).toContainText('专业事项，一个清晰的客户入口')
    await expect(page.locator('.main-title')).toContainText('客户项目入口')
    await expect(page.locator('.hero-slogan')).toContainText('专业事项，一个清晰的客户入口')
    await expect(page.getByRole('button', { name: '进入项目' })).toBeVisible()
  })
})
