<template>
  <a-config-provider
    :theme="themeConfig"
    :render-empty="renderEmpty"
  >
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
import { computed, h, onMounted } from 'vue'
import type { ConfigProviderProps } from 'ant-design-vue'
import { theme } from 'ant-design-vue'
import { InboxOutlined } from '@ant-design/icons-vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { UI_EMPTY_TEXTS } from '@/constants/uiTexts'

const appConfigStore = useAppConfigStore()

onMounted(() => {
  appConfigStore.loadConfig()
})

const themeConfig = computed(() => ({
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: '#1B3B5F',
    colorSuccess: '#2D6A4F',
    colorWarning: '#9C6B1A',
    colorError: '#A61E2D',
    colorInfo: '#2D5A8E',
    colorBgBase: '#F5F7FA',
    colorTextBase: '#102A43',
    colorBorder: '#E8ECEF',
    colorSplit: '#E8ECEF',
    borderRadius: 4,
    wireframe: false,
    motionDurationSlow: '0.24s',
    motionDurationMid: '0.2s',
    fontSize: 14,
    controlHeight: 40,
    fontFamily: '"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei UI", "Microsoft YaHei", "Noto Sans SC", sans-serif',
    fontFamilyCode: '"SFMono-Regular", "JetBrains Mono", "Consolas", monospace',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)',
    boxShadowSecondary: '0 10px 26px rgba(16, 42, 67, 0.08)',
    controlOutline: 'rgba(27, 59, 95, 0.16)',
  },
  components: {
    Layout: {
      bodyBg: '#F5F7FA',
      headerBg: '#ffffff',
      siderBg: '#001529',
      triggerBg: '#001529',
    },
    Button: {
      borderRadius: 4,
      controlHeight: 40,
      fontWeight: 600,
      primaryShadow: 'none',
    },
    Card: {
      borderRadiusLG: 12,
      headerBg: 'transparent',
      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)',
    },
    Input: {
      borderRadius: 4,
      controlHeight: 40,
      hoverBorderColor: '#1B3B5F',
      activeBorderColor: '#1B3B5F',
    },
    Select: {
      borderRadius: 4,
      controlHeight: 40,
    },
    Table: {
      borderColor: '#E8ECEF',
      headerBg: '#FAFBFC',
      headerColor: '#102A43',
      headerSplitColor: '#E8ECEF',
      rowHoverBg: 'rgba(27, 59, 95, 0.04)',
    },
    Menu: {
      borderRadius: 6,
      itemBorderRadius: 4,
      itemHeight: 44,
      itemMarginInline: 8,
      itemMarginBlock: 4,
      darkItemBg: 'transparent',
      darkSubMenuItemBg: 'transparent',
      darkItemHoverBg: 'rgba(255, 255, 255, 0.06)',
      darkItemSelectedBg: 'rgba(59, 130, 246, 0.16)',
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
      itemColor: '#627D98',
      itemSelectedColor: '#1B3B5F',
      inkBarColor: '#1B3B5F',
    },
  },
}) as ConfigProviderProps['theme'])

const renderEmpty: ConfigProviderProps['renderEmpty'] = componentName =>
  h('div', { class: 'app-empty-state' }, [
    h('div', { class: 'app-empty-state__icon' }, [h(InboxOutlined)]),
    h('strong', { class: 'app-empty-state__title' }, UI_EMPTY_TEXTS.noDataTitle),
    h(
      'p',
      { class: 'app-empty-state__hint' },
      componentName === 'Table' ? UI_EMPTY_TEXTS.noTableDataHint : UI_EMPTY_TEXTS.noDataHint,
    ),
  ])
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

:deep(.app-empty-state) {
  display: grid;
  justify-items: center;
  gap: 10px;
  padding: 32px 20px;
  border-radius: 18px;
  border: 1px dashed rgba(27, 59, 95, 0.18);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(245, 247, 250, 0.92));
}

:deep(.app-empty-state__icon) {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: rgba(27, 59, 95, 0.08);
  color: #1b3b5f;
  font-size: 24px;
}

:deep(.app-empty-state__title) {
  color: #102a43;
  font-size: 15px;
  font-weight: 600;
}

:deep(.app-empty-state__hint) {
  margin: 0;
  max-width: 280px;
  color: #627d98;
  font-size: 13px;
  line-height: 1.7;
  text-align: center;
}
</style>
