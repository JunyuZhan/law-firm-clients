<template>
  <a-config-provider :theme="themeConfig">
    <div class="app-frame">
      <div class="app-frame__glow app-frame__glow--primary" />
      <div class="app-frame__glow app-frame__glow--accent" />
      <router-view />
    </div>
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
const themeConfig = computed(() => ({
  token: {
    colorPrimary: '#183a5a',
    colorSuccess: '#2f8f63',
    colorWarning: '#b7791f',
    colorError: '#c84f4f',
    colorInfo: '#2f6f9f',
    colorBgBase: '#f5f2eb',
    colorTextBase: '#152435',
    colorBorder: 'rgba(21, 36, 53, 0.12)',
    colorSplit: 'rgba(21, 36, 53, 0.08)',
    borderRadius: 14,
    wireframe: false,
    motionDurationSlow: '0.28s',
    motionDurationMid: '0.2s',
    fontFamily: '"Source Han Sans SC", "PingFang SC", "Microsoft YaHei", "Helvetica Neue", sans-serif',
    fontFamilyCode: '"SF Mono", "JetBrains Mono", "Consolas", monospace',
    boxShadow: '0 18px 44px rgba(13, 24, 38, 0.08)',
    boxShadowSecondary: '0 8px 28px rgba(13, 24, 38, 0.08)',
  },
  components: {
    Layout: {
      bodyBg: 'transparent',
      headerBg: 'transparent',
      siderBg: 'transparent',
    },
    Button: {
      borderRadius: 14,
      controlHeight: 44,
      fontWeight: 500,
      primaryShadow: 'none',
    },
    Card: {
      borderRadiusLG: 24,
      headerBg: 'transparent',
      boxShadow: '0 18px 44px rgba(13, 24, 38, 0.08)',
    },
    Input: {
      borderRadius: 14,
      controlHeight: 46,
      hoverBorderColor: '#4d6987',
      activeBorderColor: '#183a5a',
    },
    Select: {
      borderRadius: 14,
      controlHeight: 46,
    },
    Table: {
      borderColor: 'rgba(21, 36, 53, 0.08)',
      headerBg: 'rgba(24, 58, 90, 0.04)',
      headerColor: '#183a5a',
      rowHoverBg: 'rgba(196, 160, 92, 0.08)',
    },
    Menu: {
      borderRadius: 14,
      itemBorderRadius: 14,
      itemHeight: 48,
      itemMarginInline: 8,
      itemMarginBlock: 6,
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemHoverBg: 'rgba(255, 255, 255, 0.08)',
      darkItemSelectedBg: 'rgba(255, 255, 255, 0.12)',
    },
    Drawer: {
      colorBgElevated: 'rgba(248, 244, 236, 0.98)',
    },
    Modal: {
      borderRadiusLG: 22,
    },
    Tag: {
      borderRadiusSM: 999,
    },
    Tabs: {
      itemColor: '#516273',
      itemSelectedColor: '#183a5a',
      inkBarColor: '#c4a05c',
    },
  },
}) as ConfigProviderProps['theme'])
</script>

<style scoped>
.app-frame {
  position: relative;
  min-height: 100vh;
  overflow: clip;
}

.app-frame__glow {
  position: fixed;
  inset: auto;
  pointer-events: none;
  filter: blur(48px);
  opacity: 0.65;
  z-index: 0;
}

.app-frame__glow--primary {
  top: -120px;
  right: -80px;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(36, 82, 123, 0.18) 0%, rgba(36, 82, 123, 0) 72%);
}

.app-frame__glow--accent {
  left: -80px;
  bottom: 4vh;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(196, 160, 92, 0.16) 0%, rgba(196, 160, 92, 0) 72%);
}
</style>
