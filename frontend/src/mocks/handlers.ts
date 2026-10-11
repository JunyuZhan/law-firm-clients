/**
 * MSW 请求处理器 —— 体验 Demo 的 Mock 后端。
 *
 * 覆盖接口（与 src/api/* 一一对应）：
 *  认证：/api/admin/auth/{captcha,login,me,logout,change-password,init/check,init/setup}
 *  项目：/api/matter/list, /api/matter/detail/:id, /api/matter/revoke
 *  门户：/portal/api/matters, /portal/api/matter/:id, /portal/api/notifications
 *  通知：/api/notification/{history,statistics,send}
 *  文件：/api/client/files, /api/client/files/upload, /api/client/files/:id/download|preview, DELETE
 *  统计：/api/admin/dashboard/stats, /api/admin/file/statistics, /api/admin/letter-verification/statistics, /api/admin/access/statistics
 *  配置：/api/admin/config*, /api/public/config*（portal/brand/icp）
 *  密钥：/api/admin/api-keys*
 *  模板：/api/admin/notification-templates*
 *  函件：/letter/verification/*, /api/admin/letter-verification/*
 *  系统：/api/admin/system/*, /api/admin/files/public/upload, /api/admin/fix/password
 *
 * 登录规则：任意非空账号密码均可登录（推荐 demo / 123456）；验证码任意 4-6 位。
 */
import { http, HttpResponse } from 'msw'
import * as D from './data'

const ok = <T,>(data: T, message = 'success') =>
  HttpResponse.json({ code: 200, message, data, success: true })

const fail = (message: string, code = 400, status = 200) =>
  HttpResponse.json({ code, message, data: null, success: false }, { status })

/** 从 query + formData + body 中解析出 token（兼容不同调用方式） */
function readToken(request: Request, body?: Record<string, unknown>): string | undefined {
  const url = new URL(request.url)
  return (
    url.searchParams.get('token') ||
    (body?.token as string | undefined) ||
    request.headers.get('Authorization')?.replace(/^Bearer\s+/i, '') ||
    undefined
  )
}

function pageResult<T>(records: T[], page = 1, pageSize = 10) {
  const total = records.length
  const start = (page - 1) * pageSize
  return {
    records: records.slice(start, start + pageSize),
    total,
    page,
    pageSize,
    pages: Math.max(1, Math.ceil(total / pageSize)),
  }
}

export const handlers = [
  // ================= 认证 =================
  http.get('/api/admin/auth/captcha', () =>
    ok({
      captchaId: 'demo-captcha-' + Date.now(),
      // 1x1 透明 PNG，避免依赖网络图片
      captchaImage:
        'data:image/svg+xml;base64,' +
        btoa(
          '<svg xmlns="http://www.w3.org/2000/svg" width="120" height="40">' +
            '<rect width="120" height="40" fill="#1a2b4a"/>' +
            '<text x="60" y="27" font-size="20" fill="#c9a961" text-anchor="middle" font-family="serif" letter-spacing="4">DEMO</text>' +
          '</svg>'
        ),
    })
  ),

  http.post('/api/admin/auth/login', async ({ request }) => {
    const body = (await request.json().catch(() => ({}))) as Record<string, string>
    const username = (body.username || '').trim()
    const password = (body.password || '').trim()
    if (!username || !password) {
      return fail('请输入用户名和密码')
    }
    const user = { ...D.DEMO_USER, username, realName: username === 'demo' ? D.DEMO_USER.realName : `${username}（演示）` }
    return ok({ token: D.DEMO_TOKEN, csrfToken: D.DEMO_CSRF, user }, '登录成功')
  }),

  http.get('/api/admin/auth/me', () =>
    ok({ user: D.DEMO_USER, csrfToken: D.DEMO_CSRF })
  ),

  http.post('/api/admin/auth/logout', () => ok(null, '已退出登录')),
  http.post('/api/admin/auth/change-password', () => ok(null, '密码修改成功')),
  http.post('/api/admin/fix/password', () => ok(null, '密码已重置')),
  http.get('/api/admin/auth/init/check', () => ok(false)), // Demo 已初始化，直接进入登录
  http.post('/api/admin/auth/init/setup', () => ok(null, '初始化成功')),

  // ================= 项目 =================
  http.get('/api/matter/list', () => ok(D.MATTERS)),
  http.get('/api/matter/detail/:id', ({ params }) => ok(D.matterDetail(String(params.id)))),
  http.post('/api/matter/revoke', () => ok(null, '项目已撤销')),

  // ================= 客户门户 =================
  http.get('/portal/api/matters', () => ok(D.MATTERS)),
  http.get('/portal/api/matter/:id', ({ params }) => ok(D.matterDetail(String(params.id)))),
  http.get('/portal/api/notifications', () => ok(D.NOTIFICATIONS)),

  // ================= 通知 =================
  http.get('/api/notification/history', () => ok(D.NOTIFICATIONS)),
  http.post('/api/notification/send', () => ok(null, '通知已发送（演示）')),
  http.get('/api/notification/statistics', () => ok(D.NOTIFICATION_STATS)),

  // ================= 文件 =================
  http.get('/api/client/files', () => ok(D.FILES)),
  http.post('/api/client/files/upload', async ({ request }) => {
    const form = await request.formData().catch(() => null)
    const file = form?.get('file')
    const name = file instanceof File ? file.name : '演示上传文件.pdf'
    return ok(
      {
        id: 'F' + Date.now(),
        matterId: String(form?.get('matterId') || 'M2026001'),
        clientId: Number(form?.get('clientId') || 5001),
        fileName: name,
        fileSize: file instanceof File ? file.size : 1024,
        fileType: 'application/octet-stream',
        fileCategory: String(form?.get('fileCategory') || 'OTHER'),
        description: String(form?.get('description') || '（演示）客户上传文件'),
        fileSource: 'CLIENT_UPLOAD',
        uploadedAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
        status: 'AVAILABLE',
      },
      '上传成功（演示）'
    )
  }),
  http.get('/api/client/files/:id/download', () => {
    const content = '这是汇衡律师事务所客户服务 Demo 的演示文件内容。\n'
    return new HttpResponse(content, {
      status: 200,
      headers: {
        'Content-Type': 'application/octet-stream',
        'Content-Disposition': `attachment; filename="demo-file.txt"`,
      },
    })
  }),
  http.get('/api/client/files/:id/preview', () =>
    new HttpResponse('<html><body style="font-family:sans-serif;padding:24px"><h3>演示文件预览</h3><p>当前为 Demo Mock 内容。</p></body></html>', {
      status: 200,
      headers: { 'Content-Type': 'text/html; charset=utf-8' },
    })
  ),
  http.delete('/api/client/files/:id', () => ok(null, '文件已删除（演示）')),

  // ================= 统计 / Dashboard =================
  http.get('/api/admin/dashboard/stats', () => ok(D.DASHBOARD_STATS)),
  http.get('/api/admin/file/statistics', () =>
    ok({ total: D.FILES.length, totalSize: 6_082_048, categoryStats: { CONTRACT: 1, REPORT: 1, EVIDENCE: 1, DOCUMENT: 1 } })
  ),
  http.get('/api/admin/letter-verification/statistics', () =>
    ok({ total: 15, active: 12, expired: 2, revoked: 1, todayVerifyCount: 4 })
  ),
  http.get('/api/admin/access/statistics', () =>
    ok({
      total: 1280,
      today: 37,
      uniqueVisitors: 86,
      topPages: [
        { path: '/portal', count: 420 },
        { path: '/matters', count: 260 },
        { path: '/files', count: 180 },
      ],
    })
  ),

  // ================= 系统配置 =================
  http.get('/api/admin/config', () => ok(D.CONFIGS)),
  http.get('/api/admin/config/:id', ({ params }) => {
    const c = D.CONFIGS.find(x => String(x.id) === String(params.id)) || D.CONFIGS[0]
    return ok(c)
  }),
  http.post('/api/admin/config', () => ok(D.CONFIGS[0], '保存成功（演示）')),
  http.put('/api/admin/config/:id', ({ params }) => {
    const c = D.CONFIGS.find(x => String(x.id) === String(params.id)) || D.CONFIGS[0]
    return ok(c, '更新成功（演示）')
  }),
  http.delete('/api/admin/config/:id', () => ok(null, '删除成功（演示）')),
  http.post('/api/admin/files/public/upload', () => ok({ url: '/demo-upload.png', path: 'demo/demo-upload.png' }, '上传成功（演示）')),

  // ================= 公开配置 =================
  http.get('/api/public/config', () => ok({}) ),
  http.get('/api/public/config/icp', () => ok(D.PORTAL_CONFIG.icpLicense)),
  http.get('/api/public/config/portal', () => ok(D.PORTAL_CONFIG)),
  http.get('/api/public/config/brand', () => ok(D.BRAND_CONFIG)),

  // ================= API 密钥 =================
  http.get('/api/admin/api-keys', () => ok(D.API_KEYS)),
  http.get('/api/admin/api-keys/:id', ({ params }) => {
    const k = D.API_KEYS.find(x => String(x.id) === String(params.id)) || D.API_KEYS[0]
    return ok(k)
  }),
  http.post('/api/admin/api-keys/init', () => ok(D.API_KEYS[0], '初始化成功（演示）')),
  http.post('/api/admin/api-keys', () => ok(D.API_KEYS[0], '创建成功（演示）')),
  http.put('/api/admin/api-keys/:id', ({ params }) => {
    const k = D.API_KEYS.find(x => String(x.id) === String(params.id)) || D.API_KEYS[0]
    return ok(k, '更新成功（演示）')
  }),
  http.delete('/api/admin/api-keys/:id', () => ok(null, '删除成功（演示）')),

  // ================= 通知模板 =================
  http.get('/api/admin/notification-templates', () => ok(D.TEMPLATES)),
  http.get('/api/admin/notification-templates/:id', ({ params }) => {
    const t = D.TEMPLATES.find(x => String(x.id) === String(params.id)) || D.TEMPLATES[0]
    return ok(t)
  }),
  http.post('/api/admin/notification-templates', () => ok(D.TEMPLATES[0], '创建成功（演示）')),
  http.put('/api/admin/notification-templates/:id', ({ params }) => {
    const t = D.TEMPLATES.find(x => String(x.id) === String(params.id)) || D.TEMPLATES[0]
    return ok(t, '更新成功（演示）')
  }),
  http.delete('/api/admin/notification-templates/:id', () => ok(null, '删除成功（演示）')),

  // ================= 函件验证 =================
  http.get('/letter/verification/verify', () => ok(D.VERIFY_RESULT)),
  http.get('/letter/verification/info/:no', () => ok(D.VERIFY_RESULT)),
  http.get('/api/admin/letter-verification/list', ({ request }) => {
    const url = new URL(request.url)
    const page = Number(url.searchParams.get('page') || 1)
    const pageSize = Number(url.searchParams.get('pageSize') || 10)
    return ok(pageResult(D.VERIFICATION_RECORDS, page, pageSize))
  }),
  http.get('/api/admin/letter-verification/:id', ({ params }) => {
    const r = D.VERIFICATION_RECORDS.find(x => String(x.id) === String(params.id)) || D.VERIFICATION_RECORDS[0]
    return ok(r)
  }),
  http.delete('/api/admin/letter-verification/:id', () => ok(null, '已撤销（演示）')),

  // ================= 系统信息 =================
  http.get('/api/admin/system/runtime-info', () => ok(D.SYSTEM_RUNTIME_INFO)),
  http.get('/api/admin/system/dependency-status', () => ok(D.DEPENDENCY_STATUS)),
]
