<template>
  <a-config-provider :theme="themeConfig">
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import type { ConfigProviderProps } from 'ant-design-vue'
import { useAppConfigStore } from '@/stores/appConfig'

// 初始化加载全局应用配置
const appConfigStore = useAppConfigStore()
onMounted(() => {
  appConfigStore.loadConfig()
})

// Ant Design Vue 主题配置
const themeConfig = computed<ConfigProviderProps['theme']>(() => ({
  token: {
    // 主色调
    colorPrimary: '#1a3a5c',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    colorInfo: '#1890ff',
    
    // 圆角
    borderRadius: 8,
    
    // 字体
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", "PingFang SC", "Microsoft YaHei", sans-serif',
    
    // 阴影
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.06)',
  },
  components: {
    Button: {
      borderRadius: 8,
      controlHeight: 40,
    },
    Card: {
      borderRadius: 8,
      boxShadow: '0 2px 4px rgba(0, 0, 0, 0.06)',
    },
    Input: {
      borderRadius: 8,
      controlHeight: 40,
    },
    Menu: {
      borderRadius: 4,
    },
  },
}))
</script>

<style scoped>
</style>
