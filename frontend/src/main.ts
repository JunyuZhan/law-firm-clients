import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd, { message } from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

// 全局错误处理器：捕获组件渲染和生命周期中未处理的异常
app.config.errorHandler = (err, _instance, info) => {
  if (import.meta.env.DEV) {
    console.error(`[全局错误] ${info}:`, err)
    message.error(`页面出错: ${err instanceof Error ? err.message : '未知错误'}`)
  } else {
    // 生产环境：不输出详细错误到控制台，仅提示用户
    message.error('操作异常，请稍后重试')
  }
}

// 捕获未处理的 Promise 异常
window.addEventListener('unhandledrejection', (event) => {
  if (import.meta.env.DEV) {
    console.error('[未处理的Promise异常]', event.reason)
  }
  event.preventDefault()
})

app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')
