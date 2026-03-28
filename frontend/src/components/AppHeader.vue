<template>
  <a-layout-header
    class="app-header"
    :class="[variant, variant === 'detail' ? 'has-welcome' : '']"
  >
    <div class="header-content">
      <!-- 左侧：返回按钮或Logo -->
      <div class="header-left">
        <slot name="left">
          <!-- 返回按钮（默认） -->
          <a-button
            v-if="showBack"
            type="text"
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
      </div>

      <!-- 中间：标题或Logo区域 -->
      <div class="header-center">
        <slot name="center">
          <!-- Logo（默认） -->
          <div
            v-if="variant === 'portal'"
            class="logo-section"
          >
            <div class="logo-icon-wrapper">
              <div class="logo-icon">
                <img
                  :src="logoUrl"
                  alt="Logo"
                  class="logo-image"
                >
              </div>
            </div>
            <div class="logo-text">
              <h1>{{ lawFirmName }}</h1>
              <p>客户服务系统</p>
            </div>
          </div>

          <!-- 页面标题（默认） -->
          <div
            v-else-if="title"
            class="title-section"
          >
            <div
              v-if="welcomeText"
              class="welcome-text"
            >
              {{ welcomeText }}
            </div>
            <h1 class="page-title">
              {{ title }}
            </h1>
          </div>
        </slot>
      </div>

      <!-- 右侧：操作按钮 -->
      <div class="header-right">
        <slot name="right">
          <!-- 移动端菜单按钮 -->
          <a-button
            v-if="showMobileMenu"
            type="text"
            class="header-btn mobile-menu-btn"
            @click="$emit('menu-click')"
          >
            <template #icon>
              <MenuOutlined />
            </template>
          </a-button>

          <!-- 管理员入口按钮 -->
          <a-button
            v-if="showAdminButton"
            type="text"
            class="header-btn admin-link"
            :title="'管理员入口'"
            @click="$emit('admin-click')"
          >
            <template #icon>
              <SettingOutlined />
            </template>
          </a-button>
        </slot>
      </div>
    </div>
  </a-layout-header>
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

defineProps<Props>()

defineEmits<{
  (e: 'back'): void
  (e: 'menu-click'): void
  (e: 'admin-click'): void
}>()

// 从全局配置 store 获取配置
const appConfigStore = useAppConfigStore()
const logoUrl = computed(() => appConfigStore.logoUrl)
const lawFirmName = computed(() => appConfigStore.lawFirmName)
</script>

<style scoped>
.app-header {
  background:
    linear-gradient(135deg, rgba(12, 27, 43, 0.98) 0%, rgba(18, 44, 68, 0.96) 44%, rgba(34, 74, 110, 0.94) 100%) !important;
  padding: 0;
  box-shadow: 0 18px 48px rgba(10, 20, 30, 0.16);
  position: relative;
  z-index: 100;
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(196, 160, 92, 0.18);
  min-height: 94px;
  height: auto;
  line-height: normal !important;
  overflow: hidden;
}

.app-header::before,
.app-header::after {
  content: '';
  position: absolute;
  pointer-events: none;
}

.app-header::before {
  inset: 0;
  background:
    radial-gradient(circle at 14% 18%, rgba(196, 160, 92, 0.12), transparent 24%),
    radial-gradient(circle at 86% 0%, rgba(98, 137, 173, 0.16), transparent 26%);
}

.app-header::after {
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, rgba(196, 160, 92, 0) 0%, rgba(196, 160, 92, 0.5) 50%, rgba(196, 160, 92, 0) 100%);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px;
  min-height: 94px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  position: relative;
  z-index: 1;
}

/* 左侧区域 */
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

/* 中间区域 */
.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 0;
}

/* 右侧区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* Logo 样式（Portal variant） */
.logo-section {
  display: flex;
  align-items: center;
  gap: 18px;
}

.logo-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.03));
  border-radius: 16px;
  transition: all 0.3s ease;
  padding: 8px;
  border: 1px solid rgba(196, 160, 92, 0.24);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08), 0 0 0 1px rgba(255, 255, 255, 0.04);
}

.logo-icon:hover {
  transform: translateY(-1px);
  border-color: var(--accent-color);
  box-shadow: 0 12px 22px rgba(0, 0, 0, 0.18);
  background: linear-gradient(180deg, rgba(196, 160, 92, 0.1), rgba(255, 255, 255, 0.03));
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

.logo-text {
  display: flex !important;
  flex-direction: column !important;
  justify-content: center !important;
  gap: 4px !important;
}

.logo-text h1 {
  font-size: 19px !important;
  font-weight: 600 !important;
  color: #fff !important;
  margin: 0 !important;
  line-height: 1.2 !important;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logo-text p {
  font-size: 12px !important;
  color: rgba(255, 255, 255, 0.72) !important;
  margin: 0 !important;
  font-weight: 500 !important;
  line-height: 1.4 !important;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 标题样式（Detail variant） */
.title-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.welcome-text {
  color: rgba(255, 255, 255, 0.74);
  font-size: 13px;
  font-weight: 400;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
  gap: 2px;
  letter-spacing: 0.04em;
}

/* Detail variant 时的header高度优化 */
.app-header.has-welcome {
  min-height: 120px;
}

.page-title {
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  line-height: 1.2;
}

/* Header 按钮通用样式 */
.header-btn {
  color: rgba(255, 255, 255, 0.88) !important;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  padding: 9px;
  min-width: 42px;
  min-height: 42px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
}

.header-btn:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(196, 160, 92, 0.24);
}

.back-btn {
  flex-shrink: 0;
}

.back-btn-text {
  margin-left: 4px;
  white-space: nowrap;
}

.mobile-menu-btn {
  font-size: 20px;
  padding: 8px;
  border-radius: 8px;
}

.admin-link {
  padding: 8px;
  border-radius: 8px;
}

/* 响应式 - 移动端 */
@media (max-width: 768px) {
  .app-header {
    min-height: 70px;
  }

  .app-header.has-welcome {
    min-height: 100px;
  }

  .header-content {
    padding: 0 16px;
    min-height: 70px;
  }

  .logo-section {
    gap: 12px;
  }

  .logo-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
  }

  .logo-text h1 {
    font-size: 17px;
  }

  .logo-text p {
    font-size: 10px;
  }

  .title-section {
    gap: 2px;
  }

  .welcome-text {
    font-size: 12px;
    line-height: 1.4;
  }

  .page-title {
    font-size: 18px;
  }

  .back-btn-text {
    display: none;
  }

  .admin-link {
    display: none;
  }

  .mobile-menu-btn {
    display: inline-flex !important;
    color: #fff !important;
  }
}

/* 小屏手机额外优化 */
@media (max-width: 480px) {
  .header-content {
    padding: 0 12px;
    min-height: 70px;
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .app-header.has-welcome {
    min-height: 90px;
  }

  .logo-icon {
    width: 36px;
    height: 36px;
    padding: 6px;
  }

  .logo-text h1 {
    font-size: 16px;
  }

  .welcome-text {
    font-size: 11px;
    line-height: 1.3;
  }

  .page-title {
    font-size: 16px;
  }

  .back-btn-text {
    display: none;
  }

  .admin-link {
    display: none;
  }

  .mobile-menu-btn {
    display: inline-flex !important;
    color: #fff !important;
  }
}
</style>
