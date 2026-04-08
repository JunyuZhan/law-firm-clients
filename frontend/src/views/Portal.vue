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
                  查看帮助说明
                </a-button>
                <a-button
                  size="large"
                  @click="router.push('/verify/letter')"
                >
                  函件真伪验证
                </a-button>
                <a
                  v-if="lawFirmWebsite"
                  :href="lawFirmWebsite"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="hero-link"
                >
                  访问律所官网
                </a>
              </div>

              <div class="hero-metrics">
                <article
                  v-for="item in heroMetrics"
                  :key="item.label"
                  class="metric-card"
                >
                  <span class="metric-label">{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                  <p>{{ item.description }}</p>
                </article>
              </div>
            </div>

            <aside class="hero-side hero-surface hero-surface--soft">
              <div class="hero-side-label">
                Access Guide
              </div>
              <strong>{{ accessGuideTitle }}</strong>
              <ol class="hero-steps">
                <li>收到承办律师发送的专属访问链接后，直接打开即可查看项目信息。</li>
                <li>如链接失效、过期或无法打开，请联系承办律师重新发送。</li>
                <li>只有在无法直接打开链接时，才使用下方的辅助入口继续访问。</li>
              </ol>
            </aside>
          </div>
        </section>

        <section class="section-shell info-section">
          <div class="section-head">
            <div>
              <div class="eyebrow">
                Service Overview
              </div>
              <h2>客户访问应当直接、可信、清晰</h2>
            </div>
            <p>门户页负责说明访问方式、展示服务入口，并提供异常情况下的自助帮助。</p>
          </div>

          <div class="journey-card">
            <div class="journey-copy">
              <span class="panel-kicker">Client Journey</span>
              <h3>从收到链接到进入项目，流程应当尽量少一步</h3>
              <p>客户不需要理解 token，也不需要先到门户页再手动粘贴链接。首页更适合承担品牌展示、路径说明和帮助承接。</p>
            </div>
            <ol class="journey-steps">
              <li>律师发送专属访问链接</li>
              <li>客户直接打开链接进入项目</li>
              <li>若链接异常，再使用帮助或辅助入口</li>
            </ol>
          </div>

          <div class="feature-grid">
            <article
              v-for="item in featureCards"
              :key="item.title"
              class="feature-card"
            >
              <span class="feature-label">{{ item.label }}</span>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </section>

        <section class="section-shell support-section">
          <div class="support-grid">
            <article class="support-card support-card--primary">
              <span class="panel-kicker">How it works</span>
              <h3>标准访问方式</h3>
              <p>系统默认以“律师推送专属链接，客户直接打开”为主流程，不要求客户手动输入 token 或重复粘贴链接。</p>
              <ul class="support-list">
                <li>直接点击消息中的访问链接</li>
                <li>进入项目详情页查看进展与材料</li>
                <li>如遇异常，先看帮助中心，再联系承办律师</li>
              </ul>
            </article>

            <article class="support-card">
              <span class="panel-kicker">Support</span>
              <h3>需要人工协助时</h3>
              <p>如仍无法访问，请优先联系承办律师；如需公开联系方式，可通过官网进一步查询。</p>
              <div class="support-actions">
                <a-button @click="router.push('/help')">进入帮助中心</a-button>
                <a
                  v-if="lawFirmWebsite"
                  :href="lawFirmWebsite"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="support-link"
                >
                  前往 {{ lawFirmName }} 官网
                </a>
              </div>
            </article>
          </div>
        </section>

        <section class="section-shell fallback-section">
          <a-collapse ghost>
            <a-collapse-panel
              key="manual-access"
              header="无法直接打开律师发送的链接？可使用辅助入口继续访问"
            >
              <div class="fallback-panel">
                <p class="fallback-copy">
                  请粘贴律师发送的完整访问链接，系统会自动识别项目编号与访问 token，并跳转到对应页面。
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
            <p>如有疑问，请优先联系承办律师或查看帮助中心中的访问说明。</p>
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
    .map(item => item.trim())
    .filter(Boolean)
    .slice(0, 4)
})
const icpLicense = computed(() => appConfigStore.icpLicense)
const lawFirmName = computed(() => appConfigStore.displayName || '律师事务所')
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)
const footerCopyright = computed(() => appConfigStore.copyright || `© ${new Date().getFullYear()} ${lawFirmName.value}`)
const isMobile = computed(() => windowWidth.value <= 768)
const drawerUserName = computed(() => portalVisitorStore.displayName || lawFirmName.value)
const heroTitle = computed(() => '客户服务与项目访问入口')
const heroSubtitle = computed(() => '客户通常通过承办律师发送的专属访问链接直接进入项目页面。门户首页主要用于展示服务说明、帮助入口与异常情况下的辅助访问方式。')
const accessGuideTitle = computed(() => appSlogan.value || '安全访问、进度同步、材料查看')
const heroMetrics = computed(() => [
  {
    label: '主流程',
    value: '直接打开链接',
    description: '收到律师发送的访问链接后，无需重复粘贴或输入 token。',
  },
  {
    label: '帮助入口',
    value: '帮助说明 / 验证',
    description: '遇到访问异常时，先查看帮助中心或进行函件真伪验证。',
  },
  {
    label: '辅助方式',
    value: '粘贴完整链接',
    description: '仅在无法直接打开链接时，使用辅助入口继续访问。',
  },
])

const featureCards = computed(() => [
  {
    label: 'Secure Access',
    title: '安全访问项目动态',
    description: '通过专属访问链接进入项目详情页，在受控范围内查看当前进展、关键节点与相关说明。',
  },
  {
    label: 'File Delivery',
    title: '集中查看项目材料',
    description: '文件与通知围绕项目统一呈现，减少在聊天记录、邮件与多个页面之间来回切换。',
  },
  {
    label: 'Support Guidance',
    title: '先自助排查，再联系律师',
    description: '门户页与帮助中心负责说明标准访问方式，以及遇到失效链接时的处理路径。',
  },
])

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

.hero-grid,
.support-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.9fr);
  gap: 16px;
}

.hero-surface,
.feature-card,
.support-card,
.journey-card,
.fallback-section :deep(.ant-collapse-item) {
  border-radius: 8px;
  border: 1px solid rgba(0, 9, 24, 0.06);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: var(--shadow-sm);
}

.journey-card {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(280px, 0.9fr);
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(248, 244, 237, 0.76), rgba(255, 255, 255, 0.92));
}

.journey-copy {
  display: grid;
  gap: 10px;
}

.journey-copy h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 22px;
  line-height: 1.35;
}

.journey-copy p,
.journey-steps {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.journey-steps {
  padding-left: 18px;
}

.journey-steps li + li {
  margin-top: 8px;
}

.hero-surface {
  padding: 28px;
}

.hero-surface--soft,
.support-card--primary {
  background: rgba(248, 244, 237, 0.78);
}

.hero-main {
  display: grid;
  gap: 18px;
}

.hero-eyebrow,
.hero-value-line,
.eyebrow,
.panel-kicker,
.hero-side-label,
.feature-label,
.metric-label {
  margin: 0;
  color: var(--lex-accent-strong);
  font-size: 12px;
  line-height: 1.6;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
}

.hero-eyebrow {
  color: var(--text-tertiary);
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
.section-head p,
.feature-card p,
.support-card p,
.fallback-copy,
.footer-meta p,
.hero-steps,
.support-list {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.hero-actions,
.support-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  padding: 14px 16px;
  border-radius: 8px;
  background: rgba(248, 244, 237, 0.72);
  border: 1px solid rgba(0, 9, 24, 0.05);
}

.metric-card strong {
  display: block;
  margin: 6px 0 8px;
  color: var(--text-primary);
  font-size: 16px;
  line-height: 1.4;
}

.metric-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.hero-link,
.support-link,
.law-firm-link {
  color: var(--lex-primary-soft);
  transition: color 0.2s ease;
}

.hero-link:hover,
.support-link:hover,
.law-firm-link:hover {
  color: var(--accent-color-deep);
}

.hero-side {
  display: grid;
  align-content: start;
  gap: 10px;
}

.hero-side strong,
.feature-card h3,
.support-card h3,
.section-head h2 {
  margin: 0;
  color: var(--text-primary);
}

.hero-side strong {
  font-size: 20px;
  line-height: 1.4;
}

.hero-steps,
.support-list {
  padding-left: 18px;
}

.info-section,
.support-section,
.fallback-section {
  display: grid;
  gap: 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
}

.section-head h2 {
  margin-top: 6px;
  font-size: 24px;
  line-height: 1.3;
}

.section-head p {
  max-width: 420px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.feature-card,
.support-card {
  padding: 20px;
}

.feature-card h3,
.support-card h3 {
  margin: 8px 0 10px;
  font-size: 18px;
  line-height: 1.4;
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
  .support-grid,
  .feature-grid,
  .hero-metrics,
  .journey-card,
  .footer-content {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-surface,
  .feature-card,
  .support-card,
  .journey-card,
  .access-input-panel {
    padding: 16px;
  }

  .section-head {
    display: grid;
    align-items: start;
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
