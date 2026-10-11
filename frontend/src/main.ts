import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd, { message } from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import Vant from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import logger from './utils/logger'
import './style.css'
import './styles/lex-elite-demo.css'

async function bootstrap() {
  // 体验 Demo：启用 MSW Mock 后端（GitHub Pages 静态部署，无真实后端）
  if (import.meta.env.VITE_DEMO_MODE === 'true') {
    const { startMockWorker } = await import('./mocks/browser')
    await startMockWorker()
  }

  const app = createApp(App)

// 全局错误处理器：捕获组件渲染和生命周期中未处理的异常
app.config.errorHandler = (err, _instance, info) => {
  if (import.meta.env.DEV) {
    logger.error(`[全局错误] ${info}:`, err)
    message.error(`页面出错: ${err instanceof Error ? err.message : '未知错误'}`)
  } else {
    // 生产环境：不输出详细错误到控制台，仅提示用户
    message.error('操作异常，请稍后重试')
  }
}

// 捕获未处理的 Promise 异常
window.addEventListener('unhandledrejection', (event) => {
  if (import.meta.env.DEV) {
    logger.error('[未处理的Promise异常]', event.reason)
  }
  event.preventDefault()
})

  app.use(createPinia())
  app.use(router)
  app.use(Antd)
  app.use(Vant)

  app.mount('#app')
}

bootstrap()
