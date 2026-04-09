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
        <section class="hero-section section-shell fade-in">
          <div class="hero-grid">
            <div class="hero-main hero-surface">
              <p class="hero-eyebrow">
                {{ lawFirmName }}
              </p>
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
                {{ heroTitle }}
              </a-typography-title>
              <a-typography-paragraph class="main-subtitle">
                {{ heroSubtitle }}
              </a-typography-paragraph>

              <div class="hero-actions">
                <a-button
                  type="primary"
                  size="large"
                  @click="router.push('/help')"
                >
                  帮助中心
                </a-button>
                <a-button
                  size="large"
                  @click="router.push('/verify/letter')"
                >
                  函件验证
                </a-button>
                <a
                  v-if="lawFirmWebsite"
                  :href="lawFirmWebsite"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="hero-link"
                >
                  律所官网
                </a>
              </div>
            </div>

            <aside class="hero-side hero-surface hero-surface--soft">
              <strong class="hero-side-title">访问说明</strong>
              <ol class="hero-steps">
                <li>用律师发来的链接直接进入项目。</li>
                <li>链接失效时，请让律师重发，或打开「帮助中心」。</li>
                <li>仍无法打开时，在下方粘贴完整链接。</li>
              </ol>
            </aside>
          </div>
        </section>

        <section class="section-shell fallback-section">
          <a-collapse ghost>
            <a-collapse-panel
              key="manual-access"
              header="链接打不开？粘贴完整访问链接"
            >
              <div class="fallback-panel">
                <p class="fallback-copy">
                  粘贴律师提供的完整链接（含项目编号与 token），点击「继续访问」即可跳转。
                </p>

                <div class="input-wrapper access-input-panel">
                  <label
                    for="portal-access-url"
                    class="input-label"
                  >完整访问链接</label>
                  <a-input-search
                    id="portal-access-url"
                    v-model:value="matterUrl"
                    class="matter-search"
                    placeholder="请输入完整的项目访问链接，例如：https://example.com/matter/123?token=xxx"
                    enter-button="继续访问"
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
              </div>
            </a-collapse-panel>
          </a-collapse>
        </section>
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
            <p>请通过承办律师获取访问链接；其他问题见帮助中心。</p>
          </div>

          <div class="footer-bottom">
            <p class="footer-copy">
              {{ footerCopyright }}
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
    .map((item: string) => item.trim())
    .filter(Boolean)
    .slice(0, 4)
})
const icpLicense = computed(() => appConfigStore.icpLicense)
const lawFirmName = computed(() => appConfigStore.displayName || '律师事务所')
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)
const footerCopyright = computed(() => appConfigStore.copyright || `© ${new Date().getFullYear()} ${lawFirmName.value}`)
const isMobile = computed(() => windowWidth.value <= 768)
const drawerUserName = computed(() => portalVisitorStore.displayName || lawFirmName.value)
const heroTitle = computed(() => appConfigStore.appShortName || '客户门户')
const heroSubtitle = computed(
  () =>
    appSlogan.value || '通过律师发送的链接进入项目；可在此验证函件或查看帮助。',
)

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
  display: grid;
  gap: 20px;
  padding: 0 0 24px;
}

.hero-section {
  padding-top: 24px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.9fr);
  gap: 16px;
}

.hero-surface,
.fallback-section :deep(.ant-collapse-item) {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
}

.hero-surface {
  padding: 28px;
}

.hero-surface--soft {
  background: var(--lex-bg);
}

.hero-main {
  display: grid;
  gap: 18px;
}

.hero-eyebrow,
.hero-value-line {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  letter-spacing: 0.06em;
  font-weight: 600;
}

.hero-eyebrow {
  color: var(--text-tertiary);
}

.hero-value-line {
  color: var(--lex-primary-soft);
}

.main-title {
  margin: 0 !important;
  color: var(--lex-primary) !important;
  font-family: var(--font-heading);
  font-weight: 600 !important;
  font-size: clamp(32px, 4.8vw, 52px) !important;
  line-height: 1.12 !important;
}

.main-subtitle,
.fallback-copy,
.footer-meta p,
.hero-steps {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.65;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.hero-link,
.support-link,
.law-firm-link {
  color: var(--lex-primary-soft);
  transition: color 0.2s ease;
}

.hero-link:hover,
.law-firm-link:hover {
  color: var(--lex-primary);
}

.hero-side {
  display: grid;
  align-content: start;
  gap: 10px;
}

.hero-side-title {
  margin: 0;
  font-size: 17px;
  line-height: 1.4;
  color: var(--text-primary);
}

.hero-steps {
  padding-left: 18px;
}

.fallback-section {
  display: grid;
  gap: 16px;
}

.fallback-section :deep(.ant-collapse-header) {
  font-weight: 600;
  color: var(--lex-primary);
}

.fallback-section :deep(.ant-collapse-content-box) {
  padding-top: 0 !important;
}

.fallback-panel {
  display: grid;
  gap: 16px;
}

.input-wrapper {
  position: relative;
}

.access-input-panel {
  display: grid;
  gap: 10px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--lex-bg-muted);
  border: 1px solid var(--border-color-light);
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

.footer {
  background: var(--lex-surface);
  color: var(--text-secondary);
  padding: 20px 0 28px;
  border-top: 1px solid var(--border-color-light);
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
  .content {
    gap: 16px;
    padding-bottom: calc(24px + var(--mobile-nav-height));
  }

  .hero-section {
    padding-top: 16px;
  }

  .hero-grid,
  .footer-content {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-surface,
  .access-input-panel {
    padding: 16px;
  }

  .main-title {
    font-size: 30px !important;
  }

  .paste-tip {
    right: 0;
    top: calc(100% + 4px);
  }

  .matter-search :deep(.ant-btn) {
    min-width: 108px;
  }
}
</style>
