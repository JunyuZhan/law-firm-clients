<template>
  <header
    class="app-header"
    :class="[variant, variant === 'detail' ? 'has-welcome' : '', variant === 'portal' && !title ? 'portal-no-title' : '']"
  >
    <div class="header-backdrop" />
    <div class="header-content section-shell">
      <div class="header-left">
        <slot name="left">
          <div
            v-if="variant === 'portal' && !title"
            class="portal-left-group"
          >
            <div class="logo-section">
              <div class="logo-icon">
                <img
                  :src="logoUrl"
                  alt="Logo"
                  class="logo-image"
                  width="24"
                  height="24"
                >
              </div>
              <div class="logo-text">
                <h1>{{ lawFirmName }}</h1>
                <p
                  v-if="title"
                  class="logo-system-label"
                >
                  {{ portalSystemLabel }}
                </p>
              </div>
            </div>
          </div>

          <a-button
            v-else-if="showBack"
            type="default"
            class="header-btn back-btn"
            :aria-label="showBackText ? undefined : backText"
            @click="$emit('back')"
          >
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            <span
              v-if="showBackText"
              class="back-btn-text"
            >{{ backText }}</span>
          </a-button>
        </slot>

        <a-button
          v-if="variant === 'portal' && !title && showBack"
          type="default"
          class="header-btn back-btn"
          :aria-label="showBackText ? undefined : backText"
          @click="$emit('back')"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          <span
            v-if="showBackText"
            class="back-btn-text"
          >{{ backText }}</span>
        </a-button>
      </div>

      <div class="header-center">
        <slot name="center">
          <hgroup
            v-if="title"
            class="title-section"
          >
            <p
              v-if="variant === 'portal'"
              class="welcome-text"
            >
              {{ portalSystemLabel }}
            </p>
            <p
              v-else-if="welcomeText"
              class="welcome-text"
            >
              {{ welcomeText }}
            </p>
            <h1 class="page-title">
              {{ title }}
            </h1>
            <p
              v-if="variant !== 'default'"
              class="page-caption"
            >
              {{ variant === 'portal' ? portalSubtitle : detailSubtitle }}
            </p>
          </hgroup>
        </slot>
      </div>

      <div class="header-right">
        <slot name="right">
          <a-button
            v-if="showAdminButton"
            type="default"
            class="header-btn admin-link"
            :title="'管理员入口'"
            aria-label="管理员入口"
            @click="$emit('admin-click')"
          >
            <template #icon>
              <SettingOutlined />
            </template>
          </a-button>

          <a-button
            v-if="showMobileMenu"
            type="default"
            class="header-btn mobile-menu-btn"
            aria-label="打开菜单"
            @click="$emit('menu-click')"
          >
            <template #icon>
              <MenuOutlined />
            </template>
          </a-button>
        </slot>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowLeftOutlined, MenuOutlined, SettingOutlined } from '@ant-design/icons-vue'
import { useAppConfigStore } from '@/stores/appConfig'

interface Props {
  variant?: 'portal' | 'detail' | 'default'
  title?: string
  welcomeText?: string
  showBack?: boolean
  showBackText?: boolean
  backText?: string
  showMobileMenu?: boolean
  showAdminButton?: boolean
}

withDefaults(defineProps<Props>(), {
  variant: 'default',
  title: '',
  welcomeText: '',
  showBack: false,
  showBackText: false,
  backText: '返回',
  showMobileMenu: false,
  showAdminButton: false,
})

defineEmits<{
  (e: 'back'): void
  (e: 'menu-click'): void
  (e: 'admin-click'): void
}>()

const appConfigStore = useAppConfigStore()
const logoUrl = computed(() => appConfigStore.logoUrl)
const lawFirmName = computed(() => appConfigStore.lawFirmName || appConfigStore.displayName || '律师事务所')
const portalSystemLabel = computed(() => appConfigStore.appShortName || '客户服务系统')
const portalSubtitle = computed(() => '仅展示当前授权范围内的客户协作内容')
const detailSubtitle = computed(() => '围绕事项进展、文件与提醒继续协作')
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
  background: transparent;
}

.app-header.portal {
  color: #1f2937;
}

.header-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #f0f0f0;
  pointer-events: none;
}

.app-header.portal .header-backdrop {
  background: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  position: relative;
  min-height: 64px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  gap: 16px;
  padding: 12px max(24px, env(safe-area-inset-right)) 12px max(24px, env(safe-area-inset-left));
}

.app-header.has-welcome .header-content {
  min-height: 72px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.header-left {
  justify-content: flex-start;
}

.header-right {
  justify-content: flex-end;
}

.header-center {
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.app-header.portal .header-center {
  justify-content: flex-start;
}

.app-header.portal-no-title .header-content {
  grid-template-columns: minmax(0, 1fr) auto;
}

.app-header.portal-no-title .header-center {
  display: none;
}

.portal-left-group {
  display: flex;
  align-items: center;
  min-width: 0;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  max-width: min(100%, 640px);
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #d9d9d9;
}

.logo-image {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.app-header.portal .logo-text h1,
.app-header.portal .welcome-text,
.app-header.portal .page-title,
.app-header.portal .page-caption {
  color: #1f2937;
}

.app-header.portal .logo-system-label {
  color: #6b7280;
}

.app-header.portal .header-btn {
  border-color: #d9d9d9;
  background: #ffffff;
  color: #1f2937;
}

.app-header.portal .header-btn:hover {
  border-color: #1677ff;
  color: #1677ff;
}

.logo-text {
  min-width: 0;
  flex: 1;
}

.logo-text h1 {
  margin: 0;
  font-size: 16px;
  line-height: 1.3;
  font-weight: 600;
  color: #1f2937;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.logo-system-label {
  margin: 2px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #6b7280;
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.title-section {
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.welcome-text {
  margin: 0;
  color: #1677ff;
  font-size: 12px;
  line-height: 1.5;
  font-weight: 500;
}

.page-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.3;
  font-weight: 600;
  color: #1f2937;
}

.page-caption {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.header-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
  background: #ffffff !important;
  color: #4b5563 !important;
}

.header-btn:hover,
.header-btn:focus-visible {
  border-color: #1677ff;
  color: #1677ff !important;
  outline: 2px solid rgba(22, 119, 255, 0.2);
  outline-offset: 2px;
}

.back-btn-text {
  margin-left: 4px;
}

@media (max-width: 768px) {
  .header-content {
    min-height: 56px;
    gap: 8px;
    padding: 8px 16px;
  }

  .app-header.has-welcome .header-content {
    min-height: 64px;
  }

  .logo-icon {
    width: 32px;
    height: 32px;
    border-radius: 6px;
  }
  
  .logo-image {
    width: 20px;
    height: 20px;
  }

  .logo-text h1 {
    font-size: 14px;
  }

  .logo-system-label {
    font-size: 11px;
  }

  .title-section {
    gap: 0;
  }

  .welcome-text,
  .page-caption {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .welcome-text {
    font-size: 10px;
  }

  .page-title {
    font-size: 16px;
  }

  .page-caption {
    font-size: 11px;
  }

  .header-btn {
    min-width: 32px;
    height: 32px;
    padding: 0 8px;
  }

  .back-btn-text,
  .admin-link-text {
    display: none;
  }

  .admin-link,
  .mobile-menu-btn {
    display: inline-flex !important;
  }
}
</style>
