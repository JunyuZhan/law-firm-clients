import { expect, test } from '@playwright/test'
import { mockPublicConfig } from './fixtures'

test.describe('Portal smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockPublicConfig(page)
  })

  test('renders public portal copy from config', async ({ page }) => {
    await page.goto('/portal')

    await expect(page.getByRole('heading', { level: 1, name: '汉科律师事务所' })).toBeVisible()
    await expect(page.getByText('汉科客户服务')).toBeVisible()
    await expect(page.getByText('Hanke Client Portal')).toBeVisible()
    await expect(page.getByRole('heading', { level: 2, name: /汉科律师事务所\s*客户服务入口/ })).toBeVisible()
    await expect(page.getByText('专业事项，一个清晰的客户入口')).toBeVisible()
    await expect(page.getByText('具体项目内容需通过承办律师向您发送的专属链接访问。')).toBeVisible()
    await expect(page.getByRole('button', { name: '工作人员入口' }).first()).toBeVisible()
  })

  test('stays readable on a narrow viewport', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portal')

    await expect(page.getByRole('heading', { level: 1, name: '汉科律师事务所' })).toBeVisible()
    await expect(page.getByRole('heading', { level: 2, name: /汉科律师事务所\s*客户服务入口/ })).toBeVisible()
    await expect(page.getByText('具体项目内容需通过承办律师向您发送的专属链接访问。')).toBeVisible()
    await expect(page.getByRole('button', { name: '工作人员入口' }).first()).toBeVisible()
  })
})
