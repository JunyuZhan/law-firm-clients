import type { Page } from '@playwright/test'

type ApiEnvelope<T> = {
  code: number
  success: boolean
  message: string
  data: T
}

function ok<T>(data: T): ApiEnvelope<T> {
  return {
    code: 200,
    success: true,
    message: 'ok',
    data,
  }
}

export async function mockPublicConfig(page: Page) {
  await page.route('**/api/public/config/portal', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok({
        lawFirmName: '汉科律师事务所',
        lawFirmWebsite: 'https://example.com',
        appSlogan: '专业事项，一个清晰的客户入口',
        icpLicense: '沪ICP备12345678号',
        copyright: '© 2026 汉科律师事务所',
        logoUrl: '',
        portalEyebrowEn: 'Hanke Client Portal',
        portalAccessNotice: '具体项目内容需通过承办律师向您发送的专属链接访问。',
        staffEntryLabel: '工作人员入口',
      })),
    })
  })

  await page.route('**/api/public/config/brand', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok({
        appName: '汉科客户服务系统',
        appShortName: '汉科客户服务',
        appShortNameEn: 'Hanke Clients',
        logoUrl: '',
      })),
    })
  })
}

export async function mockAdminSession(page: Page) {
  await page.addInitScript(() => {
    sessionStorage.setItem('admin_token', 'playwright-token')
    sessionStorage.setItem('admin_csrf_token', 'playwright-csrf')
    sessionStorage.setItem('admin_user', JSON.stringify({
      id: 1,
      username: 'admin',
      realName: '测试管理员',
      email: 'admin@example.com',
    }))
  })
}

export async function mockAdminShellApis(page: Page) {
  await page.route('**/api/admin/auth/me', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok({
        user: {
          id: 1,
          username: 'admin',
          realName: '测试管理员',
          email: 'admin@example.com',
        },
        csrfToken: 'playwright-csrf',
      })),
    })
  })

  await page.route('**/api/admin/system/version/check', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok({
        currentVersion: '1.0.0',
        latestVersion: '1.0.0',
        hasUpdate: false,
        releaseNotes: '',
        releaseUrl: '',
      })),
    })
  })
}

export async function mockMatterList(page: Page) {
  await page.route('**/api/matter/list**', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok([
        {
          id: 'MAT-001',
          lawFirmMatterId: 1001,
          clientId: 501,
          clientName: '张三',
          status: 'ACTIVE',
          validDays: 30,
          expiresAt: '2099-12-31T12:00:00.000Z',
          accessUrl: 'https://example.com/portal/matter/MAT-001?token=abc123',
          createdAt: '2026-04-01T12:00:00.000Z',
          updatedAt: '2026-04-01T12:00:00.000Z',
        },
        {
          id: 'MAT-002',
          lawFirmMatterId: 1002,
          clientId: 502,
          clientName: '李四',
          status: 'EXPIRED',
          validDays: 15,
          expiresAt: '2026-03-01T12:00:00.000Z',
          accessUrl: 'https://example.com/portal/matter/MAT-002?token=expired',
          createdAt: '2026-03-01T12:00:00.000Z',
          updatedAt: '2026-03-01T12:00:00.000Z',
        },
      ])),
    })
  })
}
