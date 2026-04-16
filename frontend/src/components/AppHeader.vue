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
          <div
            v-if="title"
            class="title-section"
          >
            <div
              v-if="variant === 'portal'"
              class="welcome-text"
            >
              {{ lawFirmName }}
            </div>
            <div
              v-else-if="welcomeText"
              class="welcome-text"
            >
              {{ welcomeText }}
            </div>
            <h1 class="page-title">
              {{ title }}
            </h1>
            <p
              v-if="variant !== 'default'"
              class="page-caption"
            >
              {{ variant === 'portal' ? portalSubtitle : detailSubtitle }}
            </p>
          </div>
        </slot>
      </div>

      <div class="header-right">
        <slot name="right">
          <a-button
            v-if="showAdminButton"
            type="default"
            class="header-btn admin-link"
            :title="'管理员入口'"
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
const portalSubtitle = computed(() => '客户服务系统')
const detailSubtitle = computed(() => '项目进展与文件')
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
  color: #fff;
}

.header-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(var(--backdrop-blur));
  border-bottom: 1px solid var(--border-color);
  pointer-events: none;
}

.app-header.portal .header-backdrop {
  background: rgba(246, 244, 239, 0.84);
  border-bottom: 1px solid rgba(16, 42, 67, 0.08);
}

.header-content {
  position: relative;
  min-height: 64px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
  align-items: center;
  gap: 18px;
  padding: 16px 0;
}

.app-header.has-welcome .header-content {
  min-height: 82px;
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
  gap: 18px;
  min-width: 0;
  max-width: min(100%, 640px);
}

.logo-icon {
  width: 62px;
  height: 62px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  flex-shrink: 0;
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.app-header.portal .logo-text h1,
.app-header.portal .welcome-text,
.app-header.portal .page-title,
.app-header.portal .page-caption {
  color: #102a43;
}

.app-header.portal .logo-system-label {
  color: #627d98;
}

.app-header.portal .header-btn {
  border-color: rgba(16, 42, 67, 0.08);
  background: rgba(255, 255, 255, 0.7);
  color: #102a43;
}

.app-header.portal .header-btn:hover {
  border-color: rgba(16, 42, 67, 0.16);
  background: rgba(255, 255, 255, 0.96);
}

.logo-text {
  min-width: 0;
  flex: 1;
}

.logo-text h1 {
  margin: 0;
  font-size: 18px;
  line-height: 1.3;
  color: var(--lex-primary);
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.logo-system-label {
  margin: 4px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-secondary);
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.title-section {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.welcome-text {
  color: var(--lex-accent-strong);
  font-size: 12px;
  line-height: 1.5;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-weight: 700;
}

.page-title {
  margin: 0;
  font-size: 20px;
  line-height: 1.3;
  color: var(--lex-primary);
}

.page-caption {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.header-btn {
  min-width: 40px;
  min-height: 40px;
  padding-inline: 12px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--lex-bg-muted) !important;
  color: var(--lex-primary-soft) !important;
}

.header-btn:hover {
  background: var(--lex-surface-strong) !important;
  color: var(--lex-primary) !important;
  transform: translateY(-1px);
}

.back-btn-text {
  margin-left: 4px;
}

@media (max-width: 768px) {
  .header-content {
    min-height: 56px;
    gap: 12px;
    padding: 12px 0;
  }

  .app-header.has-welcome .header-content {
    min-height: 72px;
  }

  .logo-icon {
    width: 52px;
    height: 52px;
  }

  .logo-text h1 {
    font-size: 16px;
  }

  .logo-system-label {
    margin-top: 2px;
    font-size: 11px;
    line-height: 1.5;
  }

  .title-section {
    gap: 2px;
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
    letter-spacing: 0.1em;
  }

  .page-title {
    font-size: 18px;
  }

  .page-caption {
    font-size: 12px;
    line-height: 1.5;
  }

  .header-btn {
    min-width: 38px;
    min-height: 38px;
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
