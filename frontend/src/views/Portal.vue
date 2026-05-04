<template>
  <div class="portal-page dark-saas-theme">
    <!-- 背景光效与网格 -->
    <div class="hero-grid"></div>
    <div class="hero-glow hero-glow-a"></div>
    <div class="hero-glow hero-glow-b"></div>

    <!-- 极简导航栏 -->
    <header class="portal-header fade-in-down">
      <div class="portal-header__inner">
        <div class="portal-brand">
          <div class="portal-brand__logo" :class="{ 'portal-brand__logo--fallback': !logoUrl }">
            <img v-if="logoUrl" :src="logoUrl" alt="律所标识" width="28" height="28" class="portal-brand__image">
            <BankOutlined v-else />
          </div>
          <strong class="portal-brand__name">{{ lawFirmName || ADMIN_LOGIN_TEXTS.headings.fallbackLawFirm }}</strong>
        </div>

        <div class="portal-header__meta">
          <button
            v-if="staffEntryLabel"
            class="staff-entry-btn"
            :title="staffEntryLabel"
            @click="goToAdminLogin"
          >
            <!-- 与原版 LoginOutlined 同为「入门」语义，线条改为直角/方头，整体偏方 -->
            <svg
              class="staff-entry-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              aria-hidden="true"
            >
              <rect
                x="4"
                y="5"
                width="10"
                height="14"
                stroke="currentColor"
                stroke-width="1.5"
                fill="none"
              />
              <path
                d="M20.5 12H15.5M18.25 9.5L15.5 12l2.75 2.5"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="square"
                stroke-linejoin="miter"
                fill="none"
              />
            </svg>
          </button>
        </div>
      </div>
    </header>

    <main id="main-content">
      <!-- 英雄区：干净、居中、专注 -->
      <section class="portal-hero">
        <div class="hero-content fade-in-up">
          <div class="hero-badge">{{ heroEyebrow }}</div>
          <h1 class="hero-title">{{ portalSystemName || UI_TEXTS.loginTitle }}</h1>
          <p class="hero-subtitle">
            {{ accessNotice }}
          </p>
        </div>
      </section>

      <!-- 核心能力：极简卡片网格 -->
      <section id="collaboration" class="portal-features">
        <div class="section-header fade-in-up delay-1">
          <h2 class="section-title">核心能力</h2>
          <p class="section-desc">围绕真实案件为您设计的协作体验，剔除冗余信息，结构清晰可读。</p>
        </div>

        <div class="features-grid">
          <article
            v-for="(item, index) in capabilitySections"
            :key="item.title"
            class="feature-card fade-in-up"
            :style="{ animationDelay: `${(index + 2) * 100}ms` }"
          >
            <div class="feature-icon-wrapper">
              <component :is="item.icon" class="feature-icon" aria-hidden="true" />
            </div>
            <div class="feature-label">{{ item.label }}</div>
            <h3 class="feature-title">{{ item.title }}</h3>
            <p class="feature-desc">{{ item.description }}</p>
          </article>
        </div>
      </section>

      <!-- 访问流程：清晰的步骤条 -->
      <section class="portal-workflow">
        <div class="section-header fade-in-up">
          <h2 class="section-title">服务流程</h2>
        </div>
        
        <div class="workflow-steps">
          <div class="workflow-step fade-in-up" v-for="(item, index) in accessSteps" :key="item.title" :style="{ animationDelay: `${index * 150}ms` }">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <h3 class="step-title">{{ item.title }}</h3>
              <p class="step-desc">{{ item.description }}</p>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 底部版权 -->
    <footer class="portal-footer">
      <div class="footer-inner">
        <div class="footer-left">
          <span class="copyright">{{ copyrightText }}</span>
          <span v-if="icpText" class="divider">|</span>
          <span class="icp" v-if="icpText">
            <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer" class="footer-link">
              {{ icpText }}
            </a>
          </span>
        </div>
        <div class="footer-right">
          <a v-if="lawFirmWebsite" :href="lawFirmWebsite" target="_blank" rel="noopener noreferrer" class="footer-link">
            访问官网 ↗
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Component } from 'vue'
import { useRouter } from 'vue-router'
import {
  BankOutlined,
  BellOutlined,
  CloudUploadOutlined,
  FileSearchOutlined,
  FolderOpenOutlined,
} from '@ant-design/icons-vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { COPYRIGHT_TEXT } from '@/config/app'
import { UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_LOGIN_TEXTS } from '@/constants/adminTexts'

interface PortalStep {
  title: string
  description: string
}

interface CapabilitySection {
  label: string
  title: string
  description: string
  details: string[]
  icon: Component
}

const router = useRouter()
const appConfigStore = useAppConfigStore()

const accessSteps: PortalStep[] = [
  {
    title: '接收专属事项链接',
    description: '承办律师按事项范围向您发送专属访问链接，您凭链接进入与本人对应的案件空间。',
  },
  {
    title: '直接进入被授权内容',
    description: '您通过链接进入后，可查看当前被授权的案件进展、文件与通知，无需公开注册。',
  },
  {
    title: '按要求继续协作',
    description: '如需补充材料或确认信息，您可在事项空间内继续提交，避免分散在多个沟通渠道。',
  },
]

const capabilitySections: CapabilitySection[] = [
  {
    label: '01 / 案件进度',
    title: '以事项推进为主线查看当前状态',
    description: '您可以查看当前阶段、关键节点和最近更新，减少反复询问案件是否有新进展。',
    details: [
      '围绕事项推进展示必要状态，而不是泛化的统计概览。',
      '关键信息按照案件上下文组织，阅读路径更接近真实协作。',
    ],
    icon: FileSearchOutlined,
  },
  {
    label: '02 / 文件协作',
    title: '在同一入口查阅律师已授权的文件资料',
    description: '您可在同一协作空间内完成查阅、下载与后续补充，避免资料散落在邮件、微信和临时网盘中。',
    details: [
      '仅展示当前事项下已授权的文件内容。',
      '文件流转与案件进度保持上下文关联，降低理解成本。',
    ],
    icon: FolderOpenOutlined,
  },
  {
    label: '03 / 通知提醒',
    title: '用明确提醒承接状态变化与时间节点',
    description: '当事项有进展、需要补件或接近关键节点时，您可在统一入口接收提醒，不必依赖零散通知。',
    details: [
      '提醒内容围绕案件动作组织，而不是泛消息流。',
      '重点标明是否需要您响应，减少信息噪音。',
    ],
    icon: BellOutlined,
  },
  {
    label: '04 / 补充材料',
    title: '按事项要求继续上传补充文件',
    description: '当律师需要进一步材料时，您可在当前事项上下文中直接补充，减少口头往返与版本混乱。',
    details: [
      '上传动作保留在案件语境中，方便双方持续对齐。',
      '后续查看、下载与补充行为保持在同一受控空间里。',
    ],
    icon: CloudUploadOutlined,
  },
]

const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || ADMIN_LOGIN_TEXTS.headings.fallbackLawFirm)
const portalSystemName = computed(() => appConfigStore.appShortName?.trim() || UI_TEXTS.loginTitle)
const heroEyebrow = computed(() => appConfigStore.portalEyebrowEn?.trim() || appConfigStore.appShortNameEn?.trim() || '专属协作通道')
const accessNotice = computed(() => appConfigStore.portalAccessNotice?.trim() || '安全、私密的案件协作环境。您可在此了解进展、查阅核心文件并补充材料；所有访问均需承办律师单独授权。')
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite?.trim() || '')
const icpText = computed(() => appConfigStore.icpLicense?.trim() || '')
const logoUrl = computed(() => appConfigStore.logoUrl?.trim() || '')
const staffEntryLabel = computed(() => appConfigStore.staffEntryLabel?.trim() || '系统管理入口')

const currentYear = new Date().getFullYear()

const copyrightText = computed(() => {
  const configuredLines = appConfigStore.copyright
    ?.split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean) ?? []

  if (configuredLines.length > 0) {
    return configuredLines.join(' ')
  }

  return `© ${currentYear} ${lawFirmName.value || COPYRIGHT_TEXT} 版权所有`
})

function goToAdminLogin() {
  router.push('/admin/login')
}
</script>

<style scoped>
/* 底色原为 #111827 偏「纯黑」，对外门户略沉；改为 slate-800 系略提亮，仍保持专业深色气质 */
.dark-saas-theme {
  --portal-bg: #1e293b;
  --portal-bg-deep: #172033;
  --portal-muted: rgba(255, 255, 255, 0.68);
  --portal-surface: rgba(255, 255, 255, 0.065);
  --portal-border: rgba(255, 255, 255, 0.08);

  background-color: var(--portal-bg);
  color: #ffffff;
  font-family: "Inter", "Helvetica Neue", Helvetica, -apple-system, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow-x: hidden;
}

/* --- 背景效果 (源自 Login.vue) --- */
.hero-grid {
  pointer-events: none;
  position: fixed;
  inset: 0;
  opacity: 0.05;
  background-image:
    linear-gradient(rgba(255, 255, 255, 1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 1) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 0;
}

.hero-glow {
  pointer-events: none;
  position: fixed;
  border-radius: 999px;
  filter: blur(64px);
  z-index: 0;
}

.hero-glow-a {
  top: 15%;
  right: 15%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
}

.hero-glow-b {
  bottom: 15%;
  left: 15%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.05);
}

/* 导航栏：暗色毛玻璃 */
.portal-header {
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 50;
  background: rgba(30, 41, 59, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.portal-header__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.portal-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.portal-brand__logo {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}

.portal-brand__logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.portal-brand__name {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: rgba(255, 255, 255, 0.95);
}

.staff-entry-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: color 0.2s ease, background 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.staff-entry-btn:hover,
.staff-entry-btn:focus-visible {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.staff-entry-btn:focus-visible {
  outline: 2px solid rgba(255, 255, 255, 0.5);
  outline-offset: 2px;
}

.staff-entry-icon {
  display: block;
  width: 1em;
  height: 1em;
  flex-shrink: 0;
}

/* 主内容区 */
#main-content {
  flex: 1;
  padding-top: 80px; /* offset header */
  position: relative;
  z-index: 10;
}

/* 英雄区 */
.portal-hero {
  padding: 120px 24px 80px;
  text-align: center;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-badge {
  display: inline-block;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.05em;
  margin-bottom: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
}

.hero-title {
  font-size: clamp(36px, 5vw, 56px);
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.02em;
  color: #ffffff;
  margin-bottom: 24px;
}

.hero-subtitle {
  font-size: 18px;
  line-height: 1.6;
  color: var(--portal-muted);
  max-width: 600px;
  margin: 0 auto;
}

/* 核心能力区 */
.portal-features {
  padding: 80px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #ffffff;
  margin-bottom: 16px;
}

.section-desc {
  font-size: 16px;
  color: var(--portal-muted);
  max-width: 600px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.feature-card {
  background: var(--portal-surface);
  border: 1px solid var(--portal-border);
  border-radius: 16px;
  padding: 32px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, border-color 0.2s ease;
  box-shadow: 0 4px 24px -1px rgba(0, 0, 0, 0.18);
}

.feature-card:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.09);
  border-color: rgba(255, 255, 255, 0.14);
  box-shadow: 0 10px 32px -3px rgba(0, 0, 0, 0.3);
}

.feature-icon-wrapper {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: #ffffff;
}

.feature-icon {
  font-size: 24px;
}

.feature-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.05em;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 8px;
  text-transform: uppercase;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 12px;
}

.feature-desc {
  font-size: 14px;
  line-height: 1.6;
  color: var(--portal-muted);
  margin: 0;
}

/* 服务流程区 */
.portal-workflow {
  padding: 80px 24px 120px;
  max-width: 1200px;
  margin: 0 auto;
}

.workflow-steps {
  display: flex;
  flex-direction: column;
  gap: 32px;
  position: relative;
}

.workflow-steps::before {
  content: '';
  position: absolute;
  top: 24px;
  bottom: 24px;
  left: 24px;
  width: 2px;
  background: rgba(255, 255, 255, 0.1);
  z-index: 0;
}

@media (max-width: 640px) {
  .workflow-steps::before {
    left: 20px;
  }
}

.workflow-step {
  display: flex;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 48px;
  height: 48px;
  background: var(--portal-bg-deep);
  border: 2px solid rgba(255, 255, 255, 0.22);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .step-number {
    width: 40px;
    height: 40px;
    font-size: 14px;
  }
}

.step-content {
  padding-top: 10px;
}

.step-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8px;
}

.step-desc {
  font-size: 15px;
  line-height: 1.6;
  color: var(--portal-muted);
  margin: 0;
}

/* 底部版权 */
.portal-footer {
  background: rgba(23, 32, 51, 0.88);
  border-top: 1px solid rgba(255, 255, 255, 0.07);
  padding: 32px 24px;
  position: relative;
  z-index: 10;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

@media (max-width: 640px) {
  .footer-inner {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.divider {
  color: rgba(255, 255, 255, 0.1);
}

.footer-link {
  color: rgba(255, 255, 255, 0.4);
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: rgba(255, 255, 255, 0.8);
}

/* 动画 */
.fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.fade-in-down {
  animation: fadeInDown 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (prefers-reduced-motion: reduce) {
  .fade-in-up,
  .fade-in-down {
    animation: none !important;
    opacity: 1 !important;
    transform: none !important;
  }
}
</style>
