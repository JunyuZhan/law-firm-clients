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

// Lex Elite 色板：主色深藏青 #1a2b4a，点缀金 #c9a961。
// 与 src/styles/lex-elite.css 的 --le-* token 保持一致。
const themeConfig = computed(() => ({
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: '#1a2b4a',
    colorSuccess: '#2d6a4f',
    colorWarning: '#9c6b1a',
    colorError: '#a61e2d',
    colorInfo: '#2f4a75',
    colorBgBase: '#f8f6f1',
    colorTextBase: '#1a2330',
    borderRadius: 4,
    wireframe: false,
    fontSize: 14,
    fontFamily: 'Helvetica Neue, Arial, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
  },
  components: {
    Layout: {
      bodyBg: '#f8f6f1',
      headerBg: '#ffffff',
      siderBg: '#0d1830',
    },
    Card: {
      borderRadiusLG: 8,
    },
    Menu: {
      darkItemBg: '#0d1830',
      darkSubMenuItemBg: '#0a1326',
      darkItemSelectedBg: 'rgba(201, 169, 97, 0.16)',
      darkItemSelectedColor: '#e0c485',
      darkItemColor: 'rgba(224, 229, 240, 0.78)',
      darkItemHoverColor: '#ffffff',
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
}

.skip-link {
  position: fixed;
  left: 16px;
  top: -48px;
  z-index: 2000;
  padding: 10px 14px;
  border-radius: 8px;
  background: #ffffff;
  color: #1a2b4a;
  border: 1px solid #e4dfd2;
  text-decoration: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: top 0.3s;
}

.skip-link:focus {
  top: 16px;
}

:deep(.app-empty-state) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 0;
}

:deep(.app-empty-state__icon) {
  font-size: 48px;
  color: #1a2b4a;
  opacity: 0.2;
  margin-bottom: 16px;
}

:deep(.app-empty-state__title) {
  color: #1a2330;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

:deep(.app-empty-state__hint) {
  color: #6b7280;
  font-size: 14px;
}
</style>
