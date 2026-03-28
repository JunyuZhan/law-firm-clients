/**
 * 路由集成测试
 * 验证 /portal/matter/:id 路由是否能够正确匹配和访问
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

// 使用与实际路由配置相同的结构
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/portal',
  },
  {
    // 将更具体的路由放在前面，确保优先匹配（与实际配置一致）
    path: '/portal/matter/:id',
    name: 'PortalMatterDetail',
    component: {
      template: '<div>MatterDetail Component</div>',
      setup() {
        return {}
      }
    },
  },
  {
    path: '/portal',
    name: 'Portal',
    component: {
      template: '<div>Portal Component</div>',
      setup() {
        return {}
      }
    },
  },
  {
    path: '/matter/:id',
    name: 'MatterDetail',
    component: {
      template: '<div>MatterDetail Component</div>',
      setup() {
        return {}
      }
    },
  },
]

describe('路由集成测试 - /portal/matter/:id 直接访问', () => {
  let router: ReturnType<typeof createRouter>

  beforeEach(() => {
    router = createRouter({
      history: createWebHistory(),
      routes,
    })
  })

  it('应该能够匹配 /portal/matter/:id 路由', async () => {
    const testMatterId = 'CS1234567890123456789'
    const testToken = 'test-token-12345678901234567890123456789012'
    const testPath = `/portal/matter/${testMatterId}?token=${testToken}`

    await router.push(testPath)

    expect(router.currentRoute.value.path).toBe(`/portal/matter/${testMatterId}`)
    expect(router.currentRoute.value.name).toBe('PortalMatterDetail')
    expect(router.currentRoute.value.params.id).toBe(testMatterId)
    expect(router.currentRoute.value.query.token).toBe(testToken)
  })

  it('应该优先匹配 /portal/matter/:id 而不是 /portal', async () => {
    const testMatterId = 'CS123'
    const testPath = `/portal/matter/${testMatterId}`

    await router.push(testPath)

    // 验证匹配的是 PortalMatterDetail 而不是 Portal
    expect(router.currentRoute.value.name).toBe('PortalMatterDetail')
    expect(router.currentRoute.value.name).not.toBe('Portal')
    expect(router.currentRoute.value.params.id).toBe(testMatterId)
  })

  it('应该正确提取 URL 参数', async () => {
    const testMatterId = 'CS1706860800000123456'
    const testToken = 'abc123def456'
    const testPath = `/portal/matter/${testMatterId}?token=${testToken}`

    await router.push(testPath)

    const route = router.currentRoute.value
    expect(route.params.id).toBe(testMatterId)
    expect(route.query.token).toBe(testToken)
  })

  it('应该能够处理不同的 matterId 格式', async () => {
    const testCases = [
      'CS123',
      'CS1234567890123456789',
      'CS1706860800000123456',
      'matter-123',
    ]

    for (const matterId of testCases) {
      await router.push(`/portal/matter/${matterId}?token=test`)
      expect(router.currentRoute.value.params.id).toBe(matterId)
      expect(router.currentRoute.value.name).toBe('PortalMatterDetail')
    }
  })

  it('应该能够处理不同的 token 格式', async () => {
    const testCases = [
      'simple-token',
      'test-token-12345678901234567890123456789012',
      'abc123def456',
      'token-with-special-chars-!@#$%',
    ]

    for (const token of testCases) {
      const encodedToken = encodeURIComponent(token)
      await router.push(`/portal/matter/CS123?token=${encodedToken}`)
      expect(router.currentRoute.value.query.token).toBe(token)
    }
  })

  it('访问 /portal 时不应该匹配到 /portal/matter/:id', async () => {
    await router.push('/portal')

    expect(router.currentRoute.value.name).toBe('Portal')
    expect(router.currentRoute.value.name).not.toBe('PortalMatterDetail')
    expect(router.currentRoute.value.path).toBe('/portal')
  })

  it('应该能够处理完整的访问链接格式', async () => {
    // 模拟后端生成的链接格式
    const baseUrl = 'http://localhost:8081'
    const matterId = 'CS1706860800000123456'
    const token = 'test-token-12345678901234567890123456789012'
    const fullUrl = `${baseUrl}/portal/matter/${matterId}?token=${token}`

    // 提取路径和查询参数
    const url = new URL(fullUrl)
    const path = url.pathname
    const queryToken = url.searchParams.get('token')

    await router.push(`${path}?token=${queryToken}`)

    expect(router.currentRoute.value.path).toBe(path)
    expect(router.currentRoute.value.params.id).toBe(matterId)
    expect(router.currentRoute.value.query.token).toBe(token)
  })
})

describe('路由顺序验证', () => {
  it('验证路由定义顺序：/portal/matter/:id 应该在 /portal 之前', () => {
    const portalMatterIndex = routes.findIndex(r => r.path === '/portal/matter/:id')
    const portalIndex = routes.findIndex(r => r.path === '/portal')

    expect(portalMatterIndex).toBeLessThan(portalIndex)
    expect(portalMatterIndex).toBeGreaterThan(-1)
    expect(portalIndex).toBeGreaterThan(-1)
  })
})
