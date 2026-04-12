import { expect, test } from '@playwright/test'
import { mockPublicConfig } from './fixtures'

test.describe('Portal smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)
  })

  test('renders the portal as a focused entry gateway', async ({ page }) => {
    await page.goto('/portal')

    await expect(page.locator('.logo-text').getByRole('heading', { level: 1 })).toContainText('汉科律师事务所')
    await expect(page.locator('.logo-system-label')).toContainText('汉科客户服务')
    await expect(page.getByRole('heading', { level: 1, name: '汉科客户服务' })).toBeVisible()
    await expect(page.locator('.main-subtitle')).toContainText('专业事项，一个清晰的客户入口')
    await expect(page.locator('.footer-meta')).toContainText('具体项目内容需通过专属链接访问')

    await page.getByRole('button', { name: '了解系统' }).click()
    await expect(page).toHaveURL(/\/help$/)
    await expect(page.getByRole('heading', { level: 1, name: '帮助中心' })).toBeVisible()
    await page.getByRole('button', { name: /如何进入项目/ }).click()
    await expect(page.getByText('请直接打开承办律师发送的完整专属链接')).toBeVisible()
  })

  test('stays readable on a narrow viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portal')

    await expect(page.locator('.logo-text').getByRole('heading', { level: 1 })).toContainText('汉科律师事务所')
    await expect(page.getByRole('heading', { level: 1, name: '汉科客户服务' })).toBeVisible()
    await expect(page.locator('.main-subtitle')).toContainText('专业事项，一个清晰的客户入口')
    await expect(page.locator('.footer-meta')).toContainText('具体项目内容需通过专属链接访问')

    await page.getByRole('button', { name: '了解系统' }).click()
    await expect(page).toHaveURL(/\/help$/)
    await expect(page.getByRole('heading', { level: 1, name: '帮助中心' })).toBeVisible()
  })
})
