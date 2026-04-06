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
          <div class="hero-content section-shell">
            <section class="hero-panel fade-in">
              <div class="hero-copy">
                <div class="hero-kicker-row">
                  <div class="hero-kicker">
                    {{ lawFirmName }}
                  </div>
                  <span class="hero-badge">Client Matter Portal</span>
                </div>
                <div class="hero-headline-group">
                  <div
                    v-if="heroValueItems.length"
                    class="hero-value-line"
                  >
                    <span
                      v-for="item in heroValueItems"
                      :key="item"
                    >
                      {{ item }}
                    </span>
                  </div>
                  <a-typography-title
                    :level="1"
                    class="main-title"
                  >
                    客户项目入口
                  </a-typography-title>
                </div>
                <a-typography-paragraph class="main-subtitle">
                  粘贴律所发送的完整访问链接后，即可直接进入项目详情、文件与通知入口。
                </a-typography-paragraph>
                <div class="hero-actions">
                  <a-button type="primary" size="large" class="hero-primary-action" @click="focusAccessInput">
                    粘贴并进入项目
                  </a-button>
                  <a-button size="large" @click="router.push('/help')">
                    访问帮助中心
                  </a-button>
                </div>

                <div class="hero-evidence-strip">
                  <div class="evidence-item">
                    <span>进入方式</span>
                    <strong>完整链接直达</strong>
                  </div>
                  <div class="evidence-item">
                    <span>访问凭证</span>
                    <strong>系统自动识别 token</strong>
                  </div>
                  <div class="evidence-item">
                    <span>进入后</span>
                    <strong>查看项目、文件与通知</strong>
                  </div>
                </div>
              </div>

              <div class="hero-aside">
                <a-card
                  class="access-card"
                  bordered
                >
                  <template #title>
                    <div class="access-card-head">
                      <div>
                        <span class="eyebrow">Client Access</span>
                        <h3>输入完整访问链接</h3>
                      </div>
                      <span class="access-card-status">安全识别</span>
                    </div>
                  </template>

                  <div class="access-card-body">
                    <p class="access-description">
                      系统会自动提取项目编号与访问令牌，并带您进入对应项目。
                    </p>

                    <div class="access-flow">
                      <div class="flow-step">
                        <strong>1</strong>
                        <span>复制律所发送的完整访问链接</span>
                      </div>
                      <div class="flow-step">
                        <strong>2</strong>
                        <span>粘贴后自动识别项目编号与 token</span>
                      </div>
                      <div class="flow-step">
                        <strong>3</strong>
                        <span>直接进入项目详情、文件与通知入口</span>
                      </div>
                    </div>

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

                    <ul class="access-hints">
                      <li>请直接粘贴律所发送的完整访问链接。</li>
                      <li>如链接失效，请联系承办律师重新发送。</li>
                      <li>管理员请从右上角进入后台工作台。</li>
                    </ul>
                  </div>
                </a-card>

                <div class="assurance-panel">
                  <div class="assurance-copy">
                    <span class="assurance-kicker">Need Help</span>
                    <h3>链接失效或页面无法进入？</h3>
                    <p>请先确认链接完整，再查看帮助中心；如仍无法访问，请联系承办律师重新发送。</p>
                  </div>
                  <div class="assurance-actions">
                    <a-button block @click="router.push('/help')">
                      查看访问说明
                    </a-button>
                    <a-button type="primary" block @click="focusAccessInput">
                      回到输入区
                    </a-button>
                  </div>
                </div>
              </div>
            </section>

            <section
              class="features-section fade-in"
              style="animation-delay: 0.12s"
            >
              <div class="section-header">
                <div>
                  <span class="eyebrow">Core Capabilities</span>
                  <a-typography-title :level="2">
                    在一个入口里完成访问、查看与协作
                  </a-typography-title>
                </div>
                <a-typography-text type="secondary">
                  先完成安全进入，再逐步展开项目进度、文件快照与通知信息。
                </a-typography-text>
              </div>
              <div class="feature-grid">
                <article
                  v-for="feature in features"
                  :key="feature.title"
                  class="feature-card"
                >
                  <div class="feature-icon-box">
                    <component :is="feature.icon" />
                  </div>
                  <h3>{{ feature.title }}</h3>
                  <p>{{ feature.description }}</p>
                </article>
              </div>
            </section>
          </div>
        </div>
      </a-layout-content>

      <a-layout-footer class="footer">
        <div class="footer-content section-shell">
          <div class="footer-grid">
            <section class="footer-section">
              <span class="footer-kicker">机构</span>
              <h4>关于我们</h4>
              <p>
                <a
                  v-if="lawFirmWebsite"
                  :href="lawFirmWebsite"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="law-firm-link"
                >
                  {{ lawFirmName }} 官网
                </a>
                <span v-else>{{ lawFirmName }}</span>
              </p>
            </section>
            <section class="footer-section">
              <span class="footer-kicker">支持</span>
              <h4>联系路径</h4>
              <p>如有疑问，请优先联系承办律师或事务所工作人员。</p>
            </section>
            <section class="footer-section">
              <span class="footer-kicker">合规</span>
              <h4>访问安全</h4>
              <p>访问链接、令牌校验与验证能力共同保障文件与项目信息安全。</p>
            </section>
          </div>

          <a-divider class="footer-divider" />

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
import {
  LinkOutlined,
  FileTextOutlined,
  FolderOutlined,
  SafetyOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  LockOutlined,
} from '@ant-design/icons-vue'
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
const lawFirmName = computed(() => appConfigStore.lawFirmName)
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)
const isMobile = computed(() => windowWidth.value <= 768)
const drawerUserName = computed(() => portalVisitorStore.displayName || lawFirmName.value)

const features = [
  {
    icon: FileTextOutlined,
    title: '项目详情',
    description: '进入后先查看项目概况、承办律师与当前处理状态。',
  },
  {
    icon: FolderOutlined,
    title: '文件中心',
    description: '统一查看、预览和下载当前项目材料，不再分散在多个入口。',
  },
  {
    icon: ClockCircleOutlined,
    title: '最新通知',
    description: '同步查看最近动态、提醒与关键状态更新。',
  },
  {
    icon: LockOutlined,
    title: '受控访问',
    description: '通过项目链接与访问令牌识别身份，减少误入和重复验证。',
  },
  {
    icon: TeamOutlined,
    title: '帮助支持',
    description: '遇到访问异常时，可先查看说明，再联系承办律师处理。',
  },
  {
    icon: SafetyOutlined,
    title: '隐私保护',
    description: '访问与文件分发遵循授权范围，尽量避免信息外泄。',
  },
]

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
  padding: 36px 0 64px;
}

.hero-content {
  display: grid;
  gap: 28px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(360px, 0.92fr);
  gap: 28px;
  align-items: start;
}

.hero-copy {
  display: grid;
  gap: 18px;
  align-content: start;
  padding: 12px 0 0;
}

.hero-kicker-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.hero-kicker {
  color: var(--lex-accent-strong);
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  font-weight: 700;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(0, 33, 64, 0.06);
  border: 1px solid rgba(0, 33, 64, 0.08);
  color: var(--lex-primary-soft);
  font-size: 12px;
  font-weight: 600;
}

.hero-headline-group {
  display: grid;
  gap: 16px;
}

.hero-value-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.hero-value-line span {
  position: relative;
  display: inline-flex;
  align-items: center;
  color: var(--lex-accent-strong);
  font-family: var(--font-sans);
  font-size: 13px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  font-weight: 700;
}

.hero-value-line span:not(:last-child)::after {
  content: '';
  width: 18px;
  height: 1px;
  margin-left: 10px;
  background: rgba(179, 138, 61, 0.35);
}

.main-title {
  margin: 0 !important;
  color: var(--lex-primary) !important;
  font-family: var(--font-heading);
  font-weight: 600 !important;
  font-size: clamp(36px, 5.2vw, 58px) !important;
  line-height: 1.08 !important;
  letter-spacing: 0.01em;
}

.main-subtitle {
  margin: 0 !important;
  max-width: 42ch;
  font-family: var(--font-sans);
  font-size: 18px;
  color: var(--text-secondary);
  line-height: 1.85;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-primary-action {
  min-width: 150px;
}

.hero-evidence-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.evidence-item {
  display: grid;
  gap: 4px;
  padding: 14px 16px;
  border-radius: 8px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.72) 0%, rgba(252, 251, 248, 0.88) 100%);
  border: 1px solid rgba(0, 9, 24, 0.06);
}

.evidence-item span {
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.5;
}

.evidence-item strong {
  color: var(--lex-primary);
  font-size: 15px;
  line-height: 1.55;
}

.hero-aside {
  display: grid;
  gap: 18px;
}

.access-card {
  height: 100%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(250, 248, 244, 0.94) 100%);
  border: 1px solid rgba(0, 9, 24, 0.06);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.08);
}

.access-card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.access-card-head h3 {
  margin: 0;
  font-size: 24px;
  color: var(--lex-primary);
  font-family: var(--font-heading);
}

.access-card-status {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(179, 138, 61, 0.1);
  border: 1px solid rgba(179, 138, 61, 0.14);
  color: var(--accent-color-deep);
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.access-card-body {
  display: grid;
  gap: 16px;
}

.access-description {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.access-flow {
  display: grid;
  gap: 10px;
}

.flow-step {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 9, 24, 0.05);
}

.flow-step strong {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(0, 33, 64, 0.08);
  color: var(--lex-primary-soft);
  font-size: 13px;
}

.flow-step span {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 13px;
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

.features-section {
  display: grid;
  gap: 20px;
}

.section-header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
}

.section-header :deep(.ant-typography) {
  margin-bottom: 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.feature-card {
  display: grid;
  gap: 16px;
  min-height: 220px;
  padding: 22px;
  border-radius: 8px;
  background: rgba(252, 251, 248, 0.78);
  border: 1px solid rgba(0, 9, 24, 0.05);
  box-shadow: var(--shadow-xs);
}

.feature-icon-box {
  width: 44px;
  height: 44px;
  background: rgba(0, 33, 64, 0.06);
  border: 1px solid rgba(0, 33, 64, 0.06);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--lex-primary-soft);
}

.feature-card h3 {
  margin: 0;
  color: var(--lex-primary);
  font-size: 18px;
}

.feature-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.assurance-panel {
  display: grid;
  gap: 18px;
  padding: 24px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(0, 9, 24, 0.98) 0%, rgba(0, 33, 64, 0.96) 100%);
  color: rgba(255, 255, 255, 0.88);
  box-shadow: var(--shadow-md);
}

.assurance-copy {
  display: grid;
  gap: 10px;
}

.assurance-kicker {
  color: rgba(179, 138, 61, 0.94);
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  font-weight: 700;
}

.assurance-copy h3 {
  margin: 0;
  color: #fff;
  font-size: 24px;
  line-height: 1.25;
}

.assurance-copy p {
  margin: 0;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.72);
}

.assurance-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.footer {
  background: rgba(249, 247, 242, 0.66);
  color: var(--text-secondary);
  padding: 32px 0 40px;
  border-top: 1px solid rgba(0, 9, 24, 0.05);
}

.footer-content {
  display: grid;
  gap: 18px;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.footer-section {
  display: grid;
  gap: 6px;
}

.footer-kicker {
  color: var(--lex-accent-strong);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-weight: 700;
}

.footer-section h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--lex-primary);
}

.footer-section p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.footer-divider {
  margin: 16px 0 !important;
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

@media (max-width: 1100px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .hero-copy {
    padding-top: 0;
  }

  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .section-header {
    display: grid;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 18px 0 40px;
  }

  .main-title {
    font-size: 34px !important;
    max-width: none;
  }

  .main-subtitle {
    font-size: 15px;
  }

  .hero-value-line {
    gap: 8px;
  }

  .hero-value-line span {
    font-size: 11px;
    letter-spacing: 0.14em;
  }

  .hero-value-line span:not(:last-child)::after {
    width: 12px;
    margin-left: 8px;
  }

  .hero-panel,
  .feature-grid,
  .hero-evidence-strip,
  .footer-grid,
  .assurance-actions {
    grid-template-columns: 1fr;
  }

  .hero-actions,
  .section-header,
  .access-card-head {
    display: grid;
  }

  .paste-tip {
    right: 0;
    top: calc(100% + 4px);
  }

  .matter-search :deep(.ant-btn) {
    min-width: 108px;
  }

  .hero-primary-action,
  .hero-actions :deep(.ant-btn) {
    width: 100%;
  }

  .access-input-panel,
  .assurance-panel {
    padding: 16px;
  }

  .footer {
    padding-bottom: calc(40px + var(--mobile-nav-height));
  }

  .footer-section {
    text-align: center;
  }
}
</style>
