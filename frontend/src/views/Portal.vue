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
              <a-typography-title
                :level="1"
                class="main-title"
              >
                {{ portalSystemLabel }}
              </a-typography-title>
              <a-typography-paragraph class="main-subtitle">
                {{ portalDescription }}
              </a-typography-paragraph>
              <p class="hero-note">
                {{ portalAccessNote }}
              </p>

              <div class="hero-actions">
                <a-button
                  type="primary"
                  size="large"
                  @click="router.push('/help')"
                >
                  了解系统
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
            </div>

            <aside class="hero-side hero-surface hero-surface--soft">
              <strong class="hero-side-title">访问提示</strong>
              <ul class="hero-points">
                <li>门户页用于展示律所与客户服务系统信息。</li>
                <li>进入具体项目，请使用承办律师发送的专属链接。</li>
                <li>如链接异常，可前往帮助中心查看说明。</li>
              </ul>
            </aside>
          </div>
        </section>

        <section class="section-shell intro-section">
          <div class="intro-grid">
            <article class="intro-card">
              <h2 class="intro-title">系统定位</h2>
              <p class="intro-copy">
                这是律所面向客户提供的数字化服务系统，用于承载项目协作、文件往来与服务沟通。
              </p>
            </article>
            <article class="intro-card">
              <h2 class="intro-title">访问方式</h2>
              <p class="intro-copy">
                门户页用于说明系统与访问方式；具体项目数据仅在授权后的专属链接中展示。
              </p>
            </article>
          </div>
        </section>

        <section class="section-shell feature-section">
          <div class="feature-head">
            <div>
              <p class="feature-kicker">系统能力</p>
              <h2 class="feature-heading">客户服务系统提供的核心支持</h2>
            </div>
            <p class="feature-summary">
              以下能力会在具体项目中按授权提供，门户页仅作能力说明。
            </p>
          </div>

          <div class="feature-grid">
            <article
              v-for="feature in featureCards"
              :key="feature.title"
              class="feature-card"
            >
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-copy">{{ feature.description }}</p>
            </article>
          </div>
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
            <p>本页面用于展示律所客户服务系统，具体项目内容需通过专属链接访问。</p>
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
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import MobileDrawer from '@/components/MobileDrawer.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { usePortalVisitorStore } from '@/stores/portalVisitor'

const router = useRouter()
const portalVisitorStore = usePortalVisitorStore()
const mobileDrawerOpen = ref(false)
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)

const appConfigStore = useAppConfigStore()
const icpLicense = computed(() => appConfigStore.icpLicense)
const lawFirmName = computed(() => appConfigStore.lawFirmName || appConfigStore.displayName || '律师事务所')
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)
const footerCopyright = computed(() => appConfigStore.copyright || `© ${new Date().getFullYear()} ${lawFirmName.value}`)
const isMobile = computed(() => windowWidth.value <= 768)
const drawerUserName = computed(() => portalVisitorStore.displayName || lawFirmName.value)
const portalSystemLabel = computed(() => appConfigStore.appShortName || '客户服务系统')
const portalDescription = computed(
  () => appConfigStore.appSlogan || '面向客户提供项目协作、资料交付与服务沟通的数字化服务系统。',
)
const portalAccessNote = computed(
  () => '如需进入具体项目，请使用承办律师发送的专属链接；门户页主要用于说明系统与访问方式。',
)

const featureCards = [
  {
    title: '项目协作',
    description: '围绕具体法律服务事项承载客户与律师之间的协作流程与项目信息展示。',
  },
  {
    title: '文件往来',
    description: '支持围绕项目进行文件收发、整理与受控查看，提升资料交付效率。',
  },
  {
    title: '进度沟通',
    description: '用于承载项目进展说明与服务过程中的关键节点沟通，帮助客户理解事项推进情况。',
  },
  {
    title: '受控访问',
    description: '具体项目页面通过专属链接进行访问控制，确保不同客户仅查看与自身相关的内容。',
  },
]

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
  gap: 24px;
  padding: 0 0 32px;
}

.hero-section {
  padding-top: 28px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(300px, 0.9fr);
  gap: 16px;
}

.hero-surface,
.intro-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
}

.hero-surface {
  padding: 32px;
}

.hero-surface--soft {
  background: color-mix(in srgb, var(--lex-bg) 82%, white);
}

.hero-main,
.hero-side,
.intro-card {
  display: grid;
  gap: 16px;
}

.hero-eyebrow {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  letter-spacing: 0.1em;
  font-weight: 700;
  color: var(--lex-primary-soft);
  text-transform: uppercase;
}

.main-title {
  margin: 0 !important;
  max-width: 10ch;
  color: var(--lex-primary) !important;
  font-family: var(--font-heading);
  font-weight: 600 !important;
  font-size: clamp(40px, 6vw, 68px) !important;
  line-height: 1.02 !important;
  letter-spacing: -0.03em;
}

.main-subtitle,
.hero-note,
.footer-meta p,
.hero-points,
.intro-copy {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.72;
}

.hero-note {
  padding-left: 14px;
  border-left: 3px solid color-mix(in srgb, var(--lex-accent) 40%, transparent);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
}

.hero-actions :deep(.ant-btn) {
  min-height: 44px;
  padding-inline: 18px;
  border-radius: 999px;
}

.hero-link,
.law-firm-link {
  color: var(--lex-primary-soft);
  transition: color 0.2s ease;
}

.hero-link:hover,
.law-firm-link:hover {
  color: var(--lex-primary);
}

.hero-side {
  align-content: start;
}

.hero-side-title,
.intro-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
  color: var(--text-primary);
}

.hero-points {
  padding-left: 18px;
}

.intro-section,
.feature-section {
  display: grid;
}

.intro-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.intro-card,
.feature-card {
  padding: 24px;
}

.feature-head {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}

.feature-kicker {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.6;
  letter-spacing: 0.08em;
  font-weight: 700;
  color: var(--lex-primary-soft);
  text-transform: uppercase;
}

.feature-heading,
.feature-title {
  margin: 0;
  color: var(--lex-primary);
}

.feature-heading {
  font-size: 28px;
  line-height: 1.25;
}

.feature-summary,
.feature-copy {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.feature-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
  display: grid;
  gap: 10px;
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
  .intro-grid,
  .feature-grid,
  .footer-content {
    grid-template-columns: 1fr;
    display: grid;
  }

  .feature-head {
    align-items: start;
    margin-bottom: 12px;
  }

  .hero-surface,
  .intro-card,
  .feature-card {
    padding: 18px;
  }

  .feature-heading {
    font-size: 24px;
  }

  .main-title {
    max-width: none;
    font-size: 34px !important;
    line-height: 1.08 !important;
  }
}
</style>
