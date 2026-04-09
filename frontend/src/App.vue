<template>
  <a-config-provider :theme="themeConfig">
    <div class="app-frame">
      <a
        href="#main-content"
        class="skip-link"
      >
        跳到主要内容
      </a>
      <div class="app-frame__glow app-frame__glow--primary" />
      <div class="app-frame__glow app-frame__glow--accent" />
      <router-view />
    </div>
  </a-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import type { ConfigProviderProps } from 'ant-design-vue'
import { theme } from 'ant-design-vue'
import { useAppConfigStore } from '@/stores/appConfig'

const appConfigStore = useAppConfigStore()

onMounted(() => {
  appConfigStore.loadConfig()
})

const themeConfig = computed(() => ({
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: '#1e40af',
    colorSuccess: '#15803d',
    colorWarning: '#ca8a04',
    colorError: '#dc2626',
    colorInfo: '#3b82f6',
    colorBgBase: '#f8fafc',
    colorTextBase: '#0f172a',
    colorBorder: '#e2e8f0',
    colorSplit: '#f1f5f9',
    borderRadius: 8,
    wireframe: false,
    motionDurationSlow: '0.24s',
    motionDurationMid: '0.2s',
    fontFamily: '"Fira Sans", "PingFang SC", "Microsoft YaHei", sans-serif',
    fontFamilyCode: '"Fira Code", "SFMono-Regular", "Consolas", monospace',
    boxShadow: '0 1px 2px rgba(15, 23, 42, 0.05), 0 4px 12px rgba(15, 23, 42, 0.06)',
    boxShadowSecondary: '0 8px 24px rgba(15, 23, 42, 0.08)',
    controlOutline: 'rgba(30, 64, 175, 0.15)',
  },
  components: {
    Layout: {
      bodyBg: 'transparent',
      headerBg: 'transparent',
      siderBg: '#0f172a',
      triggerBg: '#0f172a',
    },
    Button: {
      borderRadius: 6,
      controlHeight: 40,
      fontWeight: 600,
      primaryShadow: 'none',
    },
    Card: {
      borderRadiusLG: 10,
      headerBg: 'transparent',
      boxShadow: '0 1px 2px rgba(15, 23, 42, 0.05), 0 4px 12px rgba(15, 23, 42, 0.06)',
    },
    Input: {
      borderRadius: 6,
      controlHeight: 40,
      hoverBorderColor: '#1e40af',
      activeBorderColor: '#1e40af',
    },
    Select: {
      borderRadius: 6,
      controlHeight: 40,
    },
    Table: {
      borderColor: '#f1f5f9',
      headerBg: '#f8fafc',
      headerColor: '#1e3a8a',
      rowHoverBg: 'rgba(30, 64, 175, 0.06)',
    },
    Menu: {
      borderRadius: 8,
      itemBorderRadius: 6,
      itemHeight: 44,
      itemMarginInline: 8,
      itemMarginBlock: 4,
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemHoverBg: 'rgba(255, 255, 255, 0.06)',
      darkItemSelectedBg: 'rgba(59, 130, 246, 0.18)',
    },
    Drawer: {
      colorBgElevated: '#ffffff',
    },
    Modal: {
      borderRadiusLG: 12,
    },
    Tag: {
      borderRadiusSM: 999,
    },
    Tabs: {
      itemColor: '#64748b',
      itemSelectedColor: '#1e3a8a',
      inkBarColor: '#1e40af',
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

.skip-link {
  position: fixed;
  left: 16px;
  top: -48px;
  z-index: 2000;
  padding: 10px 14px;
  border-radius: 8px;
  background: var(--lex-surface-strong);
  color: var(--lex-primary-soft);
  border: 1px solid var(--border-color);
  text-decoration: none;
  box-shadow: var(--shadow-sm);
}

.skip-link:focus {
  top: 16px;
}

.app-frame__glow {
  position: fixed;
  pointer-events: none;
  filter: blur(56px);
  opacity: 0.85;
  z-index: 0;
}

.app-frame__glow--primary {
  top: -100px;
  right: -40px;
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, rgba(30, 64, 175, 0.14) 0%, rgba(30, 64, 175, 0) 72%);
}

.app-frame__glow--accent {
  left: -80px;
  bottom: 8vh;
  width: 340px;
  height: 340px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.12) 0%, rgba(245, 158, 11, 0) 72%);
}
</style>
