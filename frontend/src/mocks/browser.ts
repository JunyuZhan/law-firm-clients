/**
 * MSW 浏览器 Worker —— 仅用于体验 Demo（GitHub Pages 静态部署）。
 *
 * 该文件只在 `VITE_DEMO_MODE=true` 时由 main.ts 动态加载，
 * 用于拦截所有 /api 与 /portal/api、/letter/verification 请求并返回假数据，
 * 使前端在没有任何真实后端的情况下也能完整跑通体验。
 */
import { setupWorker } from 'msw/browser'
import { handlers } from './handlers'

export const worker = setupWorker(...handlers)

/** 启动 Mock 服务（等待 Service Worker 就绪后再放行渲染，避免首屏请求漏拦） */
export async function startMockWorker(): Promise<void> {
  await worker.start({
    onUnhandledRequest: 'bypass',
    quiet: true,
    serviceWorker: {
      // 子路径部署（/law-firm-clients/）时 service worker 需要正确的 scope
      url: `${import.meta.env.BASE_URL}mockServiceWorker.js`,
    },
  })
  // eslint-disable-next-line no-console
  console.info('[demo] MSW mock 后端已启动：任意账号密码均可登录（推荐 demo/123456）')
}
