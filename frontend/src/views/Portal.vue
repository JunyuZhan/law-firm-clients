<template>
  <div class="portal-container">
    <a-layout class="portal-layout">
      <AppHeader
        variant="portal"
        :show-mobile-menu="isMobile"
        :show-admin-button="true"
        @menu-click="openMobileDrawer"
        @admin-click="goToAdmin"
      />

      <a-layout-content
        id="main-content"
        class="content"
        tabindex="-1"
      >
        <div class="hero-section">
          <div class="hero-content section-shell fade-in">
            <section class="hero-panel">
              <div class="hero-copy hero-surface">
                <div class="hero-headline-group">
                  <p
                    v-if="heroValueItems.length"
                    class="hero-value-line"
                  >
                    {{ heroValueItems.join(' · ') }}
                  </p>
                  <a-typography-title
                    :level="1"
                    class="main-title"
                  >
                    输入完整访问链接
                  </a-typography-title>
                </div>
                <a-typography-paragraph class="main-subtitle">
                  直接粘贴律所发送的完整访问链接，系统会自动识别项目编号与访问 token。
                </a-typography-paragraph>

                <div class="input-wrapper access-input-panel">
                  <span class="input-label">完整访问链接</span>
                  <a-input-search
                    v-model:value="matterUrl"
                    class="matter-search"
                    placeholder="请输入完整的项目访问链接，例如：https://example.com/matter/123?token=xxx"
                    enter-button="进入项目"
                    size="large"
                    :loading="loading"
                    @search="handleAccess"
                    @input="checkClipboardAndUpdateButton"
                  >
                    <template #prefix>
                      <LinkOutlined class="search-prefix" />
                    </template>
                  </a-input-search>
                  <div
                    v-if="showPasteButton"
                    class="paste-tip"
                  >
                    <a-button
                      type="link"
                      size="small"
                      :disabled="!canPaste"
                      @mousedown.prevent
                      @click="handlePaste"
                    >
                      {{ pasteButtonText }}
                    </a-button>
                  </div>
                </div>

                <div class="portal-help-row">
                  <p class="portal-help-copy">
                    链接失效或打不开页面时，请联系承办律师重新发送。
                  </p>
                  <a-button type="link" class="hero-secondary-action" @click="router.push('/help')">
                    查看帮助说明
                  </a-button>
                </div>
              </div>
            </section>
          </div>
        </div>
      </a-layout-content>

      <a-layout-footer class="footer">
        <div class="footer-content section-shell">
          <div class="footer-meta">
            <p>
              <a
                v-if="lawFirmWebsite"
                :href="lawFirmWebsite"
                target="_blank"
                rel="noopener noreferrer"
                class="law-firm-link"
              >
                {{ lawFirmName }}
              </a>
              <span v-else>{{ lawFirmName }}</span>
            </p>
            <p>如有疑问，请优先联系承办律师或事务所工作人员。</p>
          </div>

          <div class="footer-bottom">
            <p class="footer-copy">
              © 2026 {{ lawFirmName }}
            </p>
            <p
              v-if="icpLicense"
              class="icp-info"
            >
              <a
                href="https://beian.miit.gov.cn"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ icpLicense }}
              </a>
            </p>
          </div>
        </div>
      </a-layout-footer>

      <MobileDrawer
        v-model:open="mobileDrawerOpen"
        :user-name="drawerUserName"
        @menu-click="handleDrawerMenuClick"
      />
      <MobileBottomNav />
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { LinkOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileDrawer from '@/components/MobileDrawer.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { usePortalVisitorStore } from '@/stores/portalVisitor'
import logger from '@/utils/logger'

const router = useRouter()
const route = useRoute()
const portalVisitorStore = usePortalVisitorStore()
const matterUrl = ref('')
const loading = ref(false)
const mobileDrawerOpen = ref(false)
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)
const showPasteButton = ref(false)
const canPaste = ref(false)
const pasteButtonText = ref('粘贴')

const appConfigStore = useAppConfigStore()
const appSlogan = computed(() => appConfigStore.appSlogan)
const heroValueItems = computed(() => {
  if (!appSlogan.value) return []
  return appSlogan.value
    .split(/[·、,，|｜/]/)
    .map(item => item.trim())
    .filter(Boolean)
    .slice(0, 4)
})
const icpLicense = computed(() => appConfigStore.icpLicense)
const lawFirmName = computed(() => appConfigStore.displayName)
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)
const isMobile = computed(() => windowWidth.value <= 768)
const drawerUserName = computed(() => portalVisitorStore.displayName || lawFirmName.value)

function parseMatterUrl(url: string): { matterId: string; token: string } | null {
  try {
    if (!url || typeof url !== 'string') return null

    const urlObj = new URL(url)
    if (!['http:', 'https:'].includes(urlObj.protocol)) return null

    const pathParts = urlObj.pathname.split('/')
    const matterId = pathParts[pathParts.length - 1]
    const token = urlObj.searchParams.get('token') || ''

    if (!matterId || !/^[\w-]+$/.test(matterId)) return null
    if (!token || !/^[\w.-]+$/.test(token)) return null

    return { matterId, token }
  } catch {
    return null
  }
}

async function checkClipboardAndUpdateButton() {
  if (matterUrl.value.trim()) {
    canPaste.value = false
    pasteButtonText.value = '请复制链接后再粘贴'
    showPasteButton.value = false
    return
  }

  if (!window.isSecureContext || !navigator.clipboard) {
    canPaste.value = false
    pasteButtonText.value = '当前环境不支持剪贴板'
    return
  }

  try {
    const text = await navigator.clipboard.readText()
    if (text && text.trim()) {
      canPaste.value = true
      pasteButtonText.value = '粘贴链接'
      showPasteButton.value = true
    } else {
      canPaste.value = false
      showPasteButton.value = false
    }
  } catch (error) {
    logger.debug('剪贴板读取失败:', error)
    canPaste.value = false
    showPasteButton.value = false
  }
}

async function handlePaste() {
  if (!canPaste.value) return

  try {
    const text = await navigator.clipboard.readText()
    if (text && text.trim()) {
      matterUrl.value = text.trim()
      message.success('已粘贴链接')
      showPasteButton.value = false
    } else {
      message.warning('剪贴板中没有内容')
    }
  } catch {
    message.error('无法读取剪贴板内容，请手动粘贴')
  }
}

function focusAccessInput() {
  const input = document.querySelector('.matter-search input') as HTMLInputElement | null
  input?.focus()
  input?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

async function handleAccess() {
  if (!matterUrl.value) {
    message.warning('请输入项目访问链接')
    focusAccessInput()
    return
  }

  const parsed = parseMatterUrl(matterUrl.value)
  if (!parsed) {
    message.error('链接格式不正确，请检查链接是否包含项目 ID 和 token')
    return
  }

  loading.value = true
  try {
    await router.push({
      name: 'MatterDetail',
      params: { id: parsed.matterId },
      query: { token: parsed.token },
    })
  } catch {
    message.error('访问失败，请检查链接是否正确')
  } finally {
    loading.value = false
  }
}

function goToAdmin() {
  router.push({ name: 'AdminLogin' }).catch(() => {
    window.location.href = '/admin/login'
  })
}

function openMobileDrawer() {
  mobileDrawerOpen.value = true
}

function handleDrawerMenuClick(key: string) {
  if (key === 'portal') {
    mobileDrawerOpen.value = false
  }
}

function updateWindowWidth() {
  windowWidth.value = window.innerWidth
  if (!isMobile.value) {
    mobileDrawerOpen.value = false
  }
}

onMounted(() => {
  updateWindowWidth()
  window.addEventListener('resize', updateWindowWidth)
  const urlParam = route.query.url as string
  if (urlParam) {
    const parsed = parseMatterUrl(urlParam)
    if (parsed) {
      router.push({
        name: 'MatterDetail',
        params: { id: parsed.matterId },
        query: { token: parsed.token },
      })
    }
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', updateWindowWidth)
})
</script>

<style scoped>
.portal-container {
  min-height: 100vh;
  background: transparent;
}

.portal-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  overflow-x: clip;
}

.content {
  flex: 1;
  padding: 0;
}

.hero-section {
  position: relative;
  padding: 24px 0 48px;
}

.hero-content {
  display: block;
}

.hero-panel {
  width: 100%;
  max-width: 760px;
}

.hero-copy {
  display: grid;
  gap: 18px;
  align-content: start;
}

.hero-surface {
  padding: 28px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(0, 9, 24, 0.06);
  box-shadow: var(--shadow-sm);
}

.hero-headline-group {
  display: grid;
  gap: 10px;
}

.hero-value-line {
  margin: 0;
  color: var(--lex-accent-strong);
  font-size: 13px;
  line-height: 1.6;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 600;
}

.main-title {
  margin: 0 !important;
  color: var(--lex-primary) !important;
  font-family: var(--font-heading);
  font-weight: 600 !important;
  font-size: clamp(32px, 4.4vw, 48px) !important;
  line-height: 1.12 !important;
  letter-spacing: 0.01em;
}

.main-subtitle {
  margin: 0 !important;
  max-width: 46ch;
  font-family: var(--font-sans);
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.75;
}

.hero-secondary-action {
  padding-inline: 0;
  height: auto;
}

.input-wrapper {
  position: relative;
}

.access-input-panel {
  display: grid;
  gap: 10px;
  padding: 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(0, 9, 24, 0.05);
}

.input-label {
  color: var(--lex-primary);
  font-size: 13px;
  font-weight: 600;
}

.matter-search :deep(.ant-input-group-addon) {
  padding: 0 !important;
  background: transparent !important;
  border: none !important;
}

.matter-search :deep(.ant-input-affix-wrapper),
.matter-search :deep(.ant-input),
.matter-search :deep(.ant-btn) {
  height: 50px;
}

.matter-search :deep(.ant-btn) {
  border-radius: 0 4px 4px 0 !important;
  min-width: 124px;
}

.matter-search :deep(.ant-input-affix-wrapper) {
  border-radius: 4px 0 0 4px !important;
}

.search-prefix {
  color: var(--lex-accent-strong);
}

.paste-tip {
  position: absolute;
  right: 126px;
  top: calc(100% + 6px);
  z-index: 10;
}

.access-hints {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.8;
}

.portal-help-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 4px;
}

.portal-help-copy {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.75;
}

.footer {
  background: rgba(249, 247, 242, 0.66);
  color: var(--text-secondary);
  padding: 20px 0 28px;
  border-top: 1px solid rgba(0, 9, 24, 0.05);
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.footer-meta {
  display: grid;
  gap: 4px;
}

.footer-meta p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.law-firm-link {
  color: var(--lex-primary-soft);
  transition: color 0.3s;
}

.law-firm-link:hover {
  color: var(--accent-color-deep);
}

.footer-bottom {
  text-align: center;
  padding-top: 8px;
}

.footer-copy,
.icp-info {
  margin: 0;
  color: var(--text-tertiary);
}

.icp-info a {
  color: var(--text-tertiary);
}

.icp-info a:hover {
  color: var(--text-secondary);
}

.fade-in {
  animation: fadeIn 0.32s linear forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 16px 0 32px;
  }

  .hero-surface {
    padding: 20px 16px;
  }

  .main-title {
    font-size: 30px !important;
    max-width: none;
  }

  .main-subtitle {
    font-size: 15px;
  }

  .portal-help-row,
  .footer-content {
    display: grid;
  }

  .paste-tip {
    right: 0;
    top: calc(100% + 4px);
  }

  .matter-search :deep(.ant-btn) {
    min-width: 108px;
  }

  .access-input-panel {
    padding: 14px;
  }

  .footer {
    padding-bottom: calc(28px + var(--mobile-nav-height));
  }
}
</style>
