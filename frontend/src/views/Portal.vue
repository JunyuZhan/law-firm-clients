<template>
  <div class="portal-page">
    <div class="portal-shell fade-in">
      <header class="portal-header">
        <div class="section-shell portal-header__inner">
          <div class="portal-brand">
            <div
              class="portal-brand__logo"
              :class="{ 'portal-brand__logo--fallback': !logoUrl }"
            >
              <img
                v-if="logoUrl"
                :src="logoUrl"
                alt="律所标识"
                class="portal-brand__image"
              >
              <BankOutlined v-else />
            </div>
            <div class="portal-brand__text">
              <p class="portal-brand__system">
                {{ portalSystemName }}
              </p>
              <h1 class="portal-brand__name">
                {{ lawFirmName }}
              </h1>
            </div>
          </div>

          <a-button
            v-if="staffEntryLabel"
            type="default"
            class="portal-header__action"
            @click="goToAdminLogin"
          >
            <template #icon>
              <LoginOutlined />
            </template>
            {{ staffEntryLabel }}
          </a-button>
        </div>
      </header>

      <main
        id="main-content"
        class="portal-main"
      >
        <section class="portal-hero section-shell">
          <div class="portal-hero__copy">
            <span class="eyebrow portal-hero__eyebrow">{{ heroEyebrow }}</span>
            <h2 class="portal-hero__title">
              <span class="portal-hero__title-line">{{ lawFirmName }}</span>
              <span class="portal-hero__title-line">客户服务入口</span>
            </h2>
            <p class="portal-hero__subtitle">
              {{ appSlogan }}
            </p>
            <p class="portal-hero__description">
              面向客户提供案件进展、文件查阅、通知提醒与补充材料协作的统一入口。
            </p>

            <div
              v-if="accessNotice"
              class="portal-hero__notice surface-card"
            >
              <SafetyCertificateOutlined class="portal-hero__notice-icon" />
              <p>{{ accessNotice }}</p>
            </div>

            <div class="portal-hero__actions">
              <a-button
                v-if="staffEntryLabel"
                type="primary"
                size="large"
                class="pro-button-primary portal-primary-action"
                @click="goToAdminLogin"
              >
                <template #icon>
                  <LoginOutlined />
                </template>
                {{ staffEntryLabel }}
              </a-button>

              <a
                v-if="lawFirmWebsite"
                :href="lawFirmWebsite"
                target="_blank"
                rel="noopener noreferrer"
                class="portal-secondary-link"
              >
                访问律所官网
                <ArrowRightOutlined />
              </a>
            </div>
          </div>

          <aside class="portal-hero__panel surface-card">
            <div class="portal-panel__head">
              <span class="portal-panel__eyebrow">客户使用方式</span>
              <h3 class="portal-panel__title">
                通过专属链接进入对应事项
              </h3>
              <p class="portal-panel__description">
                无需公开注册，访问范围与可见内容以承办律师发送的受控链接为准。
              </p>
            </div>

            <ol class="portal-step-list">
              <li
                v-for="(item, index) in accessSteps"
                :key="item.title"
                class="portal-step"
              >
                <span class="portal-step__index">{{ String(index + 1).padStart(2, '0') }}</span>
                <div class="portal-step__body">
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.description }}</p>
                </div>
              </li>
            </ol>
          </aside>
        </section>

        <section
          id="portal-services"
          class="portal-section section-shell"
        >
          <div class="page-intro portal-section-intro">
            <div>
              <span class="eyebrow">服务能力</span>
              <h2 class="intro-title portal-section-intro__title">
                围绕法律协作的核心入口
              </h2>
              <p class="intro-text">
                聚焦客户在事项推进中的高频动作，保留必要信息密度，避免展示型堆砌。
              </p>
            </div>
          </div>

          <div class="portal-card-grid">
            <article
              v-for="item in collaborationCards"
              :key="item.title"
              class="portal-card"
            >
              <div class="portal-card__icon">
                <component :is="item.icon" />
              </div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </section>

        <section
          id="portal-trust"
          class="portal-section section-shell"
        >
          <div class="portal-trust-panel">
            <div class="portal-trust-panel__head">
              <span class="portal-trust-badge">
                <SafetyCertificateOutlined />
                访问边界与资料保护
              </span>
              <h2>以清晰、克制、可追溯的方式承接客户协作</h2>
              <p>
                在保证阅读体验的前提下，明确访问范围、资料流转与提醒方式，让协作过程保持专业秩序。
              </p>
            </div>

            <div class="portal-trust-grid">
              <article
                v-for="item in trustCards"
                :key="item.title"
                class="portal-trust-card"
              >
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
              </article>
            </div>
          </div>
        </section>
      </main>

      <footer class="portal-footer">
        <div class="section-shell portal-footer__inner">
          <div class="portal-footer__meta">
            <span>{{ copyrightText }}</span>
            <span v-if="icpText">{{ icpText }}</span>
          </div>
          <div class="portal-footer__links">
            <a href="#portal-services">服务说明</a>
            <a href="#portal-trust">访问边界</a>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRightOutlined,
  BankOutlined,
  BellOutlined,
  CloudUploadOutlined,
  FileSearchOutlined,
  FolderOpenOutlined,
  LoginOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons-vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { COPYRIGHT_TEXT } from '@/config/app'

interface PortalCard {
  title: string
  description: string
  icon: object
}

interface PortalStep {
  title: string
  description: string
}

const router = useRouter()
const appConfigStore = useAppConfigStore()

const collaborationCards: PortalCard[] = [
  {
    title: '案件进展查看',
    description: '围绕关键节点展示事项状态，帮助客户及时了解当前推进情况。',
    icon: FileSearchOutlined,
  },
  {
    title: '授权文件查阅',
    description: '集中查看已授权资料与正式文件，减少信息分散和重复沟通。',
    icon: FolderOpenOutlined,
  },
  {
    title: '通知提醒接收',
    description: '在进度变化、材料补充或关键时间临近时收到明确提醒。',
    icon: BellOutlined,
  },
  {
    title: '补充材料提交',
    description: '按事项要求上传补充文件，让资料流转更直接、更可追溯。',
    icon: CloudUploadOutlined,
  },
]

const accessSteps: PortalStep[] = [
  {
    title: '接收专属链接',
    description: '承办律师会按事项范围向客户发送受控访问链接。',
  },
  {
    title: '进入事项与文件',
    description: '通过链接查看进展、资料与通知，无需额外创建公开账号。',
  },
  {
    title: '补充协作材料',
    description: '在需要时上传补充材料，保持沟通链路集中且有序。',
  },
]

const trustCards = [
  {
    title: '授权范围清晰',
    description: '只展示当前客户被授权查看的事项、文件与提醒，降低信息外溢风险。',
  },
  {
    title: '资料流转克制',
    description: '以必要信息为主，不额外堆砌展示内容，让访问过程更聚焦。',
  },
  {
    title: '协作节奏统一',
    description: '把查看进度、接收提醒与补充材料放回同一入口，减少来回切换。',
  },
]

const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || '律师事务所')
const portalSystemName = computed(() => appConfigStore.appShortName?.trim() || '客户服务系统')
const heroEyebrow = computed(() => appConfigStore.portalEyebrowEn?.trim() || appConfigStore.appShortNameEn?.trim() || 'Client Service Portal')
const appSlogan = computed(() => appConfigStore.appSlogan?.trim() || '专业 · 诚信 · 高效')
const accessNotice = computed(() => appConfigStore.portalAccessNotice?.trim() || '')
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite?.trim() || '')
const icpText = computed(() => appConfigStore.icpLicense?.trim() || '')
const logoUrl = computed(() => appConfigStore.logoUrl?.trim() || '')
const staffEntryLabel = computed(() => appConfigStore.staffEntryLabel?.trim() || '工作人员入口')

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
.portal-page {
  --portal-accent: #b88a2b;
  --portal-accent-soft: rgba(184, 138, 43, 0.14);
  --portal-ink: #0f172a;
  --portal-ink-soft: #334155;
  --portal-border: rgba(15, 23, 42, 0.08);
  --portal-surface: rgba(255, 255, 255, 0.94);
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(15, 23, 42, 0.08), transparent 30%),
    radial-gradient(circle at top right, rgba(184, 138, 43, 0.09), transparent 20%),
    linear-gradient(180deg, #fafaf9 0%, #f8fafc 42%, #f1f5f9 100%);
}

.portal-shell {
  min-height: 100vh;
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 20;
  border-bottom: 1px solid var(--portal-border);
  background: rgba(250, 250, 249, 0.92);
  backdrop-filter: blur(var(--backdrop-blur));
}

.portal-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  min-height: 74px;
}

.portal-brand {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.portal-brand__logo {
  display: grid;
  place-items: center;
  width: 50px;
  height: 50px;
  flex-shrink: 0;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(15, 23, 42, 0.07);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
  color: var(--portal-accent);
  font-size: 22px;
}

.portal-brand__logo--fallback {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.94));
}

.portal-brand__image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.portal-brand__text {
  min-width: 0;
}

.portal-brand__system,
.portal-panel__eyebrow {
  margin: 0;
  color: var(--portal-accent);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.portal-brand__name {
  margin: 4px 0 0;
  color: var(--portal-ink);
  font-size: 22px;
  line-height: 1.3;
}

.portal-header__action {
  min-height: 44px;
  padding-inline: 18px;
  border-radius: 999px;
  border-color: rgba(184, 138, 43, 0.28);
  background: rgba(255, 255, 255, 0.86);
  color: var(--portal-ink);
  font-weight: 600;
}

.portal-header__action:hover,
.portal-header__action:focus {
  border-color: rgba(184, 138, 43, 0.48);
  color: var(--portal-ink);
  background: #ffffff;
}

.portal-main {
  display: grid;
  gap: 28px;
  padding: 36px 0 52px;
}

.portal-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.84fr);
  gap: 28px;
  align-items: stretch;
}

.portal-hero__copy {
  padding: 18px 0 8px;
}

.portal-hero__eyebrow {
  font-size: 11px;
  color: var(--portal-accent);
}

.portal-hero__title {
  margin: 18px 0 0;
  max-width: 12em;
  color: var(--portal-ink);
  font-size: clamp(34px, 4.4vw, 54px);
  line-height: 1.06;
  letter-spacing: -0.045em;
}

.portal-hero__title-line {
  display: block;
}

.portal-hero__subtitle {
  margin: 18px 0 0;
  max-width: 18em;
  color: var(--portal-ink-soft);
  font-size: clamp(20px, 1.9vw, 24px);
  font-weight: 600;
  line-height: 1.45;
}

.portal-hero__description {
  max-width: 700px;
  margin: 16px 0 0;
  color: #475569;
  font-size: 16px;
  line-height: 1.8;
}

.portal-hero__notice {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-top: 24px;
  padding: 18px 20px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-left: 3px solid var(--portal-accent);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: none;
}

.portal-hero__notice-icon {
  margin-top: 2px;
  color: var(--portal-accent);
  font-size: 18px;
}

.portal-hero__notice p,
.portal-panel__description,
.portal-card p,
.portal-trust-card p,
.portal-footer__meta span,
.portal-footer__links a {
  margin: 0;
  color: #475569;
  line-height: 1.75;
}

.portal-hero__actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 28px;
}

.portal-primary-action {
  height: 46px !important;
  padding-inline: 22px !important;
  border-radius: 999px !important;
  font-weight: 700 !important;
}

.portal-secondary-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--portal-accent);
  font-weight: 600;
  text-decoration: none;
}

.portal-secondary-link:hover {
  color: #8f6a1d;
}

.portal-hero__panel {
  position: relative;
  padding: 28px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.95));
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.06);
}

.portal-hero__panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 28px;
  right: 28px;
  height: 1px;
  background: linear-gradient(90deg, var(--portal-accent), rgba(184, 138, 43, 0));
}

.portal-panel__head {
  display: grid;
  gap: 10px;
}

.portal-panel__title,
.portal-card h3,
.portal-trust-card h3 {
  margin: 0;
  color: var(--portal-ink);
  line-height: 1.35;
}

.portal-panel__title {
  font-size: 27px;
  letter-spacing: -0.03em;
}

.portal-step-list {
  display: grid;
  gap: 18px;
  margin: 24px 0 0;
  padding: 0;
  list-style: none;
}

.portal-step {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 16px;
  align-items: start;
  padding-top: 18px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.portal-step:first-child {
  padding-top: 0;
  border-top: 0;
}

.portal-step__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 38px;
  min-height: 38px;
  border-radius: 999px;
  border: 1px solid rgba(184, 138, 43, 0.32);
  background: rgba(184, 138, 43, 0.08);
  color: var(--portal-accent);
  font-size: 13px;
  font-weight: 700;
}

.portal-step__body h4 {
  margin: 0;
  color: var(--portal-ink);
  font-size: 16px;
}

.portal-step__body p {
  margin: 6px 0 0;
  color: #475569;
  line-height: 1.75;
}

.portal-section {
  display: grid;
  gap: 18px;
}

.portal-section-intro {
  align-items: end;
}

.portal-section-intro__title {
  margin-top: 10px;
}

.portal-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.portal-card {
  padding: 24px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.portal-card:hover {
  border-color: rgba(184, 138, 43, 0.26);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.06);
  transform: translateY(-1px);
}

.portal-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  margin-bottom: 18px;
  border-radius: 14px;
  background: var(--portal-accent-soft);
  color: var(--portal-accent);
  font-size: 18px;
}

.portal-card h3 {
  font-size: 18px;
}

.portal-card p {
  margin-top: 10px;
  font-size: 14px;
}

.portal-trust-panel {
  padding: 30px;
  border-radius: 24px;
  border-top: 1px solid rgba(184, 138, 43, 0.46);
  background: linear-gradient(180deg, #0f172a 0%, #162033 100%);
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.14);
}

.portal-trust-panel__head {
  display: grid;
  gap: 12px;
  max-width: 780px;
}

.portal-trust-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid rgba(184, 138, 43, 0.24);
  background: rgba(184, 138, 43, 0.1);
  color: #f5deb1;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.portal-trust-panel__head h2,
.portal-trust-card h3 {
  color: #f8fafc;
}

.portal-trust-panel__head h2 {
  margin: 0;
  font-size: clamp(28px, 3vw, 38px);
  line-height: 1.18;
  letter-spacing: -0.03em;
}

.portal-trust-panel__head p,
.portal-trust-card p {
  color: rgba(226, 232, 240, 0.82);
}

.portal-trust-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.portal-trust-card {
  padding: 22px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.portal-trust-card h3 {
  font-size: 18px;
}

.portal-trust-card p {
  margin-top: 10px;
}

.portal-footer {
  border-top: 1px solid var(--portal-border);
  background: rgba(250, 250, 249, 0.72);
}

.portal-footer__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding-block: 18px 26px;
}

.portal-footer__meta,
.portal-footer__links {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.portal-footer__links a {
  color: #64748b;
  text-decoration: none;
}

.portal-footer__links a:hover {
  color: var(--portal-ink);
}

@media (max-width: 1080px) {
  .portal-hero,
  .portal-trust-grid {
    grid-template-columns: 1fr;
  }

  .portal-card-grid {
    grid-template-columns: 1fr;
  }

  .portal-hero__copy {
    padding-bottom: 0;
  }
}

@media (max-width: 768px) {
  .portal-header__inner {
    min-height: 64px;
  }

  .portal-brand__logo {
    width: 46px;
    height: 46px;
    border-radius: 14px;
  }

  .portal-brand__name {
    font-size: 18px;
  }

  .portal-brand__system,
  .portal-panel__eyebrow,
  .portal-hero__eyebrow {
    letter-spacing: 0.1em;
  }

  .portal-main {
    gap: 24px;
    padding-top: 24px;
  }

  .portal-hero__title {
    max-width: none;
  }

  .portal-hero__panel,
  .portal-card,
  .portal-trust-panel,
  .portal-trust-card {
    padding: 20px;
  }

  .portal-footer__inner {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 560px) {
  .portal-header__inner,
  .portal-hero__actions {
    align-items: stretch;
    flex-direction: column;
  }

  .portal-header__action,
  .portal-primary-action,
  .portal-secondary-link {
    justify-content: center;
    width: 100%;
  }

  .portal-secondary-link {
    padding: 0 12px;
  }

  .portal-hero__notice {
    padding: 18px;
  }

  .portal-step {
    gap: 12px;
  }
}
</style>
