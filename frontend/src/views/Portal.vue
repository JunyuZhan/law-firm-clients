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

      <a-layout-content class="content">
        <section class="portal-hero section-shell">
          <div class="hero-grid fade-in">
            <div class="hero-copy glass-panel">
              <div class="eyebrow">
                客户访问工作台
              </div>
              <h1 class="editorial-title hero-title">
                {{ appSlogan?.replace(/[、,，]/g, ' · ') || '精确、安全、透明的客户门户' }}
              </h1>
              <p class="hero-subtitle">
                集中查看项目、文件与进展节点。界面更安静，信息更直接，保持事务所品牌感与产品效率感之间的平衡。
              </p>

              <div class="hero-trust-strip">
                <span
                  v-for="signal in trustSignals"
                  :key="signal"
                  class="hero-trust-chip"
                >
                  {{ signal }}
                </span>
              </div>

              <div class="hero-points">
                <div class="hero-point">
                  <span class="hero-point-label">项目视图</span>
                  <span class="hero-point-value">关键摘要、进展与关联文件放在同一信息面板中。</span>
                </div>
                <div class="hero-point">
                  <span class="hero-point-label">安全交付</span>
                  <span class="hero-point-value">通过专属链接和访问令牌控制访问范围与时效。</span>
                </div>
                <div class="hero-point">
                  <span class="hero-point-label">沟通效率</span>
                  <span class="hero-point-value">减少跳转与理解成本，让客户更快进入具体项目。</span>
                </div>
              </div>
            </div>

            <div class="hero-access glass-panel">
              <div class="access-card-top">
                <div class="access-badge">
                  <LinkOutlined class="access-badge-icon" />
                </div>
                <div>
                  <h2>输入访问链接</h2>
                  <p>粘贴完整项目链接，系统将自动识别项目 ID 与访问令牌。</p>
                </div>
              </div>

              <div class="access-inputs">
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

              <div class="access-note">
                <span>如果您是事务所管理员，请从右上角进入后台。</span>
              </div>

              <div class="access-metrics">
                <div
                  v-for="metric in portalStats"
                  :key="metric.label"
                  class="metric-tile"
                >
                  <span class="metric-value">{{ metric.value }}</span>
                  <span class="metric-label">{{ metric.label }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="feature-shell section-shell fade-in">
          <div class="feature-head">
            <div>
              <div class="eyebrow">
                门户概览
              </div>
              <h2 class="editorial-title feature-title">
                统一的信息层级，而不是分散的功能堆叠
              </h2>
            </div>
            <p class="feature-summary">
              首页只负责三件事：快速进入项目、解释客户能做什么、建立对系统稳定性和专业性的信任。
            </p>
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
      </a-layout-content>

      <a-layout-footer class="footer">
        <div class="footer-content section-shell">
          <a-row :gutter="[24, 24]">
            <a-col
              :xs="24"
              :md="8"
            >
              <div class="footer-section">
                <span class="footer-kicker">机构</span>
                <a-typography-title :level="4">
                  关于我们
                </a-typography-title>
                <a-typography-paragraph>
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
                </a-typography-paragraph>
              </div>
            </a-col>
            <a-col
              :xs="24"
              :md="8"
            >
              <div class="footer-section">
                <span class="footer-kicker">支持</span>
                <a-typography-title :level="4">
                  联系方式
                </a-typography-title>
                <a-typography-paragraph>
                  如有疑问，请联系您的专属律师
                </a-typography-paragraph>
              </div>
            </a-col>
            <a-col
              :xs="24"
              :md="8"
            >
              <div class="footer-section">
                <span class="footer-kicker">合规</span>
                <a-typography-title :level="4">
                  安全保障
                </a-typography-title>
                <a-typography-paragraph>
                  采用银行级加密技术，保障信息安全
                </a-typography-paragraph>
              </div>
            </a-col>
          </a-row>

          <a-divider class="footer-divider" />

          <div class="footer-bottom">
            <a-typography-paragraph class="footer-copy">
              © 2026 {{ lawFirmName }}
            </a-typography-paragraph>
            <a-typography-paragraph
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
            </a-typography-paragraph>
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
const icpLicense = computed(() => appConfigStore.icpLicense)
const lawFirmName = computed(() => appConfigStore.lawFirmName)
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)
const isMobile = computed(() => windowWidth.value <= 768)
const drawerUserName = computed(() => portalVisitorStore.displayName || lawFirmName.value)

const features = [
  {
    icon: FileTextOutlined,
    title: '项目详情',
    description: '实时查看项目基本信息、承办律师、案件进展等详细信息',
  },
  {
    icon: FolderOutlined,
    title: '文件管理',
    description: '安全上传、下载、预览项目相关文件，支持多种文件格式',
  },
  {
    icon: TeamOutlined,
    title: '律师团队',
    description: '了解承办律师信息，随时与专业团队保持沟通',
  },
  {
    icon: LockOutlined,
    title: '安全可靠',
    description: '采用银行级加密技术，多重安全验证，保障您的信息安全',
  },
  {
    icon: ClockCircleOutlined,
    title: '实时更新',
    description: '项目信息实时同步，第一时间获取最新进展和文件',
  },
  {
    icon: SafetyOutlined,
    title: '隐私保护',
    description: '严格遵守法律法规，保护客户隐私，信息仅限授权访问',
  },
]

const trustSignals = [
  '访问令牌校验',
  '事务进度透明',
  '文档统一归档',
]

const portalStats = [
  { value: '24/7', label: '项目链接可访问' },
  { value: 'TLS', label: '传输全程加密' },
  { value: '1-Link', label: '单链接直达项目' },
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
    pasteButtonText.value = '请复制链接，再输入'
    showPasteButton.value = false
    return
  }

  if (!window.isSecureContext || !navigator.clipboard) {
    canPaste.value = false
    pasteButtonText.value = '请复制链接，再输入'
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

async function handleAccess() {
  if (!matterUrl.value) {
    message.warning('请输入项目访问链接')
    return
  }

  const parsed = parseMatterUrl(matterUrl.value)
  if (!parsed) {
    message.error('链接格式不正确，请检查链接是否包含项目ID和token')
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
  background:
    radial-gradient(circle at 12% 16%, rgba(196, 160, 92, 0.18), transparent 24%),
    radial-gradient(circle at 88% 8%, rgba(63, 109, 153, 0.18), transparent 28%),
    linear-gradient(180deg, #f7f2e8 0%, #eee6da 100%);
}

.portal-layout {
  min-height: 100vh;
}

.content {
  flex: 1;
  padding: 42px 0 64px;
}

.portal-hero {
  padding-top: 16px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 0.8fr);
  gap: 28px;
  align-items: stretch;
}

.hero-copy,
.hero-access {
  border-radius: var(--radius-xl);
  padding: 34px;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 520px;
  position: relative;
  overflow: hidden;
}

.hero-copy::after {
  content: '';
  position: absolute;
  inset: auto -12% -24% auto;
  width: 260px;
  height: 260px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(196, 160, 92, 0.14) 0%, rgba(196, 160, 92, 0) 72%);
  pointer-events: none;
}

.hero-title {
  margin: 20px 0 18px;
  font-size: clamp(42px, 6vw, 78px);
  color: var(--primary-color-dark);
  max-width: 10ch;
}

.hero-subtitle {
  max-width: 620px;
  margin: 0 0 28px;
  font-size: 17px;
  line-height: 1.85;
  color: var(--text-secondary);
}

.hero-trust-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 0 0 28px;
}

.hero-trust-chip {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(16, 38, 61, 0.05);
  border: 1px solid rgba(16, 38, 61, 0.08);
  color: var(--primary-color);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.hero-points {
  display: grid;
  gap: 14px;
}

.hero-point {
  display: grid;
  gap: 6px;
  padding: 18px 20px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--border-color);
}

.hero-point-label {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-tertiary);
}

.hero-point-value {
  font-size: 15px;
  color: var(--text-primary);
}

.hero-access {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 24px;
  position: relative;
  overflow: hidden;
}

.hero-access::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 4px;
  background: linear-gradient(90deg, var(--accent-color) 0%, rgba(196, 160, 92, 0) 100%);
}

.access-card-top {
  display: flex;
  gap: 18px;
  align-items: center;
}

.access-card-top h2 {
  margin: 0 0 8px;
  color: var(--primary-color-dark);
  font-size: 28px;
}

.access-card-top p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.access-badge {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: linear-gradient(145deg, rgba(37, 77, 119, 0.12), rgba(201, 164, 76, 0.18));
  border: 1px solid rgba(37, 77, 119, 0.1);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.access-badge-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.access-inputs {
  display: grid;
  gap: 10px;
}

.matter-search :deep(.ant-input-group-addon) {
  padding: 0 !important;
  background: transparent !important;
  border: none !important;
}

.matter-search :deep(.ant-btn) {
  height: 56px;
  min-width: 126px;
  border-radius: 16px !important;
}

.matter-search :deep(.ant-input-affix-wrapper),
.matter-search :deep(.ant-input) {
  height: 56px;
}

.search-prefix {
  color: var(--text-tertiary);
}

.paste-tip {
  min-height: 24px;
}

.access-note {
  padding-top: 4px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.access-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.metric-tile {
  display: grid;
  gap: 4px;
  padding: 16px 14px;
  border-radius: 18px;
  background: rgba(16, 38, 61, 0.04);
  border: 1px solid rgba(16, 38, 61, 0.08);
}

.metric-value {
  font-family: var(--font-heading);
  font-size: 26px;
  line-height: 1;
  color: var(--primary-color-dark);
}

.metric-label {
  color: var(--text-secondary);
  font-size: 12px;
}

.feature-shell {
  padding-top: 26px;
}

.feature-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 420px);
  gap: 24px;
  align-items: end;
  margin-bottom: 28px;
}

.feature-title {
  margin: 14px 0 0;
  font-size: clamp(30px, 4vw, 44px);
  color: var(--primary-color-dark);
}

.feature-summary {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 26px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
  border-color: rgba(37, 77, 119, 0.2);
}

.feature-icon-box {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(145deg, rgba(37, 77, 119, 0.14), rgba(201, 164, 76, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
  font-size: 22px;
}

.feature-card h3 {
  margin: 0;
  font-size: 20px;
  color: var(--primary-color-dark);
}

.feature-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.footer {
  background: linear-gradient(180deg, rgba(10, 24, 37, 0.96) 0%, rgba(16, 34, 53, 0.99) 100%);
  padding: 40px 0 24px;
  border-top: 1px solid rgba(196, 160, 92, 0.12);
}

.footer-content {
  color: rgba(255, 255, 255, 0.82);
}

.footer-section {
  padding: 16px 0;
}

.footer-kicker {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.45);
}

.footer-section :deep(h4) {
  color: #fff !important;
  margin-bottom: 12px !important;
}

.footer-section :deep(.ant-typography) {
  color: rgba(255, 255, 255, 0.7) !important;
}

.footer-divider {
  border-color: rgba(255, 255, 255, 0.12) !important;
  margin: 12px 0 18px !important;
}

.footer-bottom {
  text-align: center;
}

.footer-copy,
.icp-info {
  color: rgba(255, 255, 255, 0.46) !important;
  margin-bottom: 4px !important;
}

.law-firm-link,
.icp-info a {
  color: inherit;
  text-decoration: none;
}

.law-firm-link:hover,
.icp-info a:hover {
  color: #fff;
}

@media (max-width: 992px) {
  .content {
    padding-top: 24px;
  }

  .hero-grid,
  .feature-head,
  .feature-grid {
    grid-template-columns: 1fr;
  }

  .access-metrics {
    grid-template-columns: 1fr;
  }

  .hero-copy {
    min-height: auto;
  }
}

@media (max-width: 768px) {
  .hero-copy,
  .hero-access,
  .feature-card {
    padding: 22px;
  }

  .access-card-top {
    align-items: flex-start;
  }

  .portal-layout {
    padding-bottom: 70px;
  }
}
</style>
