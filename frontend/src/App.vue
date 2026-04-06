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
    colorPrimary: '#002140',
    colorSuccess: '#3f6f4d',
    colorWarning: '#a26c17',
    colorError: '#b44537',
    colorInfo: '#123154',
    colorBgBase: '#f3f0e8',
    colorTextBase: '#16120d',
    colorBorder: 'rgba(0, 9, 24, 0.14)',
    colorSplit: 'rgba(0, 9, 24, 0.08)',
    borderRadius: 6,
    wireframe: false,
    motionDurationSlow: '0.24s',
    motionDurationMid: '0.2s',
    fontFamily: '"Inter", "PingFang SC", "Microsoft YaHei", sans-serif',
    fontFamilyCode: '"SF Mono", "Consolas", monospace',
    boxShadow: '0 12px 32px rgba(0, 9, 24, 0.06)',
    boxShadowSecondary: '0 18px 48px rgba(0, 9, 24, 0.08)',
    controlOutline: 'rgba(179, 138, 61, 0.18)',
  },
  components: {
    Layout: {
      bodyBg: 'transparent',
      headerBg: 'transparent',
      siderBg: '#000918',
      triggerBg: '#000918',
    },
    Button: {
      borderRadius: 4,
      controlHeight: 42,
      fontWeight: 600,
      primaryShadow: 'none',
    },
    Card: {
      borderRadiusLG: 8,
      headerBg: 'transparent',
      boxShadow: '0 12px 32px rgba(0, 9, 24, 0.06)',
    },
    Input: {
      borderRadius: 4,
      controlHeight: 46,
      hoverBorderColor: '#b38a3d',
      activeBorderColor: '#b38a3d',
    },
    Select: {
      borderRadius: 4,
      controlHeight: 46,
    },
    Table: {
      borderColor: 'rgba(0, 9, 24, 0.08)',
      headerBg: 'rgba(0, 9, 24, 0.04)',
      headerColor: '#000918',
      rowHoverBg: 'rgba(179, 138, 61, 0.08)',
    },
    Menu: {
      borderRadius: 8,
      itemBorderRadius: 6,
      itemHeight: 48,
      itemMarginInline: 8,
      itemMarginBlock: 6,
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemHoverBg: 'rgba(255, 255, 255, 0.06)',
      darkItemSelectedBg: 'rgba(179, 138, 61, 0.14)',
    },
    Drawer: {
      colorBgElevated: 'rgba(252, 251, 248, 0.98)',
    },
    Modal: {
      borderRadiusLG: 8,
    },
    Tag: {
      borderRadiusSM: 999,
    },
    Tabs: {
      itemColor: '#7a7367',
      itemSelectedColor: '#000918',
      inkBarColor: '#b38a3d',
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
  border-radius: 6px;
  background: rgba(252, 251, 248, 0.96);
  color: var(--lex-primary);
  border: 1px solid var(--border-color-light);
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
  background: radial-gradient(circle, rgba(0, 33, 64, 0.16) 0%, rgba(0, 33, 64, 0) 72%);
}

.app-frame__glow--accent {
  left: -80px;
  bottom: 8vh;
  width: 340px;
  height: 340px;
  background: radial-gradient(circle, rgba(179, 138, 61, 0.18) 0%, rgba(179, 138, 61, 0) 72%);
}
</style>
