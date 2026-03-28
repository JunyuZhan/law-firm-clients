import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      // 只代理API请求，不代理前端路由
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        ws: true, // 启用WebSocket代理
      },
      // 代理后端API路径（/portal/api/...）
      '/portal/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path, // 保持路径不变
        ws: true, // 启用WebSocket代理
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    // 生产环境移除 console 和 debugger，防止信息泄露
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    // 提高警告阈值：Ant Design Vue 统一打包约 1.2MB，但 gzip 压缩后仅 370KB
    // 统一打包可以避免循环依赖导致的运行时错误
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        manualChunks(id) {
          // 将 node_modules 中的依赖分割
          if (id.includes('node_modules')) {
            // Vue 核心库
            if (id.includes('vue') && !id.includes('ant-design-vue') && !id.includes('vue-router') && !id.includes('pinia')) {
              return 'vue-core'
            }
            // 路由和状态管理
            if (id.includes('vue-router') || id.includes('pinia')) {
              return 'vue-vendor'
            }
            // Ant Design Vue - 统一打包（避免循环依赖导致的运行时错误）
            if (id.includes('ant-design-vue')) {
              // 样式文件不参与 JS chunk 分割
              if (id.includes('.css')) {
                return undefined
              }
              // 图标库单独打包（图标库通常较大且独立）
              if (id.includes('@ant-design/icons-vue')) {
                return 'antd-icons'
              }
              // 所有 Ant Design Vue 组件统一打包（避免循环依赖）
              return 'antd-vendor'
            }
            // 工具库
            if (id.includes('axios') || id.includes('dayjs')) {
              return 'utils-vendor'
            }
            // 其他第三方库
            return 'vendor'
          }
        },
      },
    },
  },
})
