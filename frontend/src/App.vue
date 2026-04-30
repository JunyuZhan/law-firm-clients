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

const themeConfig = computed(() => ({
  algorithm: theme.defaultAlgorithm,
  token: {
    colorPrimary: '#1677ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#f5222d',
    colorInfo: '#1677ff',
    colorBgBase: '#f0f2f5',
    colorTextBase: '#1f2937',
    borderRadius: 6,
    wireframe: false,
    fontSize: 14,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji"',
  },
  components: {
    Layout: {
      bodyBg: '#f0f2f5',
      headerBg: '#ffffff',
    },
    Card: {
      borderRadiusLG: 8,
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
  color: #1677ff;
  border: 1px solid #d9d9d9;
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
  color: #1677ff;
  opacity: 0.2;
  margin-bottom: 16px;
}

:deep(.app-empty-state__title) {
  color: #1f2937;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

:deep(.app-empty-state__hint) {
  color: #6b7280;
  font-size: 14px;
}
</style>
