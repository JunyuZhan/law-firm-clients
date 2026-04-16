<template>
  <div class="portal-page">
    <AppHeader variant="portal" />

    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="portal-hero section-shell fade-in">
        <div class="hero-layout">
          <div class="hero-copy">
            <p
              v-if="heroEyebrow"
              class="hero-eyebrow"
            >
              {{ heroEyebrow }}
            </p>
            <p class="hero-brand">
              {{ lawFirmName }}
            </p>
            <p
              v-if="heroMotto"
              class="hero-motto"
            >
              {{ heroMotto }}
            </p>
            <h1 class="hero-title">
              {{ heroTitle }}
            </h1>
            <p class="hero-lead">
              {{ heroLead }}
            </p>

            <div class="hero-trust">
              <article class="trust-item">
                <span>访问范围</span>
                <strong>按案件授权开放</strong>
              </article>
              <article class="trust-item">
                <span>资料治理</span>
                <strong>文书与文件统一管理</strong>
              </article>
              <article class="trust-item">
                <span>协作方式</span>
                <strong>与承办律师团队定向联络</strong>
              </article>
            </div>

            <div class="hero-actions">
              <a
                href="#instructions"
                class="hero-primary"
              >
                查阅访问规则
              </a>
              <a
                :href="contactHref"
                :target="lawFirmWebsite ? '_blank' : undefined"
                :rel="lawFirmWebsite ? 'noopener' : undefined"
                class="hero-secondary"
              >
                联络承办律师
              </a>
            </div>
          </div>

          <aside class="hero-aside">
            <div class="hero-note-card">
              <p class="note-kicker">受控访问说明</p>
              <div class="hero-note-grid">
                <article>
                  <span>授权对象</span>
                  <strong>仅限受托客户与经确认协作成员</strong>
                </article>
                <article>
                  <span>资料范围</span>
                  <strong>仅展示已授权的案件进展、文书与文件材料</strong>
                </article>
                <article>
                  <span>联络机制</span>
                  <strong>由承办律师团队统一管理访问凭证与协作边界</strong>
                </article>
              </div>
            </div>
          </aside>
        </div>
      </section>

      <section
        id="capabilities"
        class="portal-section section-shell"
      >
        <div class="section-frame">
          <div class="section-head">
            <p class="section-kicker">服务能力</p>
            <h2>客户协作与资料治理</h2>
            <p>围绕案件动态、授权文书与关键信息通知建立统一协作界面，减少分散沟通带来的理解偏差。</p>
          </div>

          <div class="capability-grid">
            <article class="capability-card">
              <div class="capability-icon">
                <FileTextOutlined />
              </div>
              <h3>事务进度查阅</h3>
              <p>系统化展示委托事项的关键节点与当前状态，帮助客户及时掌握事务进展情况及后续预定计划。</p>
            </article>

            <article class="capability-card">
              <div class="capability-icon">
                <FolderOpenOutlined />
              </div>
              <h3>电子档案库</h3>
              <p>集中存储法律意见书、合同文本及证据材料等数字化档案，基于严格权限控制实现有序存取。</p>
            </article>

            <article class="capability-card">
              <div class="capability-icon">
                <NotificationOutlined />
              </div>
              <h3>关键节点通知</h3>
              <p>针对重要节点及法律时效提供定向信息反馈，确保客户在第一时间获悉事务的实质性更新。</p>
            </article>

            <article class="capability-card">
              <div class="capability-icon">
                <MessageOutlined />
              </div>
              <h3>专业协作空间</h3>
              <p>整合意见反馈、文书校阅等功能，减少信息碎片化，为法律问题研讨提供统一协作环境。</p>
            </article>
          </div>
        </div>
      </section>

      <section
        id="instructions"
        class="portal-section portal-section--plain section-shell"
      >
        <div class="section-frame section-frame--plain">
          <div class="instruction-layout">
            <div class="instruction-copy">
              <p class="section-kicker">管理说明</p>
              <h2>受控访问机制说明</h2>
              <p class="instruction-intro">门户不追求开放注册，而是强调按案件、按授权、按协作关系开放。所有信息展示均服从于律师服务流程与资料边界。</p>

              <div class="instruction-list">
                <article class="instruction-item">
                  <span>01</span>
                  <div>
                    <h3>定向准入制</h3>
                    <p>本门户仅面向已建立正式委托关系的客户开放，不设公开注册入口。</p>
                  </div>
                </article>

                <article class="instruction-item">
                  <span>02</span>
                  <div>
                    <h3>基于受控链接的访问机制</h3>
                    <p>访问权限由承办律师依据案件保密要求进行管理，客户通过预留联络渠道获取访问凭证。</p>
                  </div>
                </article>

                <article class="instruction-item">
                  <span>03</span>
                  <div>
                    <h3>权限动态管理</h3>
                    <p>访问范围限定于当前委托事项，并随协作进度与合规要求动态调整。</p>
                  </div>
                </article>
              </div>

              <p
                v-if="accessNotice"
                class="instruction-note"
              >
                {{ accessNotice }}
              </p>
            </div>

            <aside class="instruction-panel">
              <div class="mock-card">
                <div class="mock-head">
                  <span class="mock-dot" />
                  <span class="mock-line" />
                </div>
                <div class="mock-body">
                  <div class="mock-field" />
                  <div class="mock-field" />
                  <div class="mock-lock">
                    <LockOutlined />
                    <span />
                  </div>
                </div>
              </div>
            </aside>
          </div>
        </div>
      </section>

      <section
        id="contact"
        class="commitment section-shell"
      >
        <div class="commitment-frame">
          <SafetyCertificateOutlined class="commitment-icon" />
          <h2>专业隐私承诺</h2>
          <div class="commitment-copy">
            <p>本门户的设计与运行遵循律师职业伦理与行业数据保护要求，所有访问均建立在授权边界与协作关系之上。</p>
            <p>维护客户信息安全与事务私密性，是数字化协作的基本前提，而非附加承诺。</p>
          </div>
          <div class="commitment-support">
            <p class="support-label">业务支持</p>
            <p>如需调整访问权限或补充协作资料，请直接联系您的承办律师团队。</p>
          </div>
        </div>
      </section>

      <footer
        class="portal-footer section-shell"
      >
        <div class="footer-meta">
          <p
            v-if="showFooterBrand"
            class="footer-brand"
          >
            {{ lawFirmName }}
          </p>
          <p class="footer-copy">
            {{ copyrightText }}
          </p>
          <p
            v-if="icpText"
            class="footer-icp"
          >
            {{ icpText }}
          </p>
        </div>
      </footer>
    </a-layout-content>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  FileTextOutlined,
  FolderOpenOutlined,
  LockOutlined,
  MessageOutlined,
  NotificationOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { APP_SLOGAN, COPYRIGHT_TEXT } from '@/config/app'

const appConfigStore = useAppConfigStore()

const slogan = computed(() => appConfigStore.appSlogan?.trim() || APP_SLOGAN)
const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || '律师事务所')
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite?.trim() || '')
const accessNotice = computed(() => appConfigStore.portalAccessNotice?.trim() || '')
const contactHref = computed(() => lawFirmWebsite.value || '#contact')
const currentYear = new Date().getFullYear()
const appShortName = computed(() => appConfigStore.appShortName?.trim() || '')

const heroEyebrow = computed(() => {
  const custom = appConfigStore.portalEyebrowEn?.trim()
  return custom || ''
})

const copyrightText = computed(() => {
  const configuredLines = appConfigStore.copyright
    ?.split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean) ?? []

  if (configuredLines.length > 0) {
    if (configuredLines.length === 1) {
      return configuredLines[0]
    }

    const organization = lawFirmName.value.trim()
    const dedupedLines = configuredLines.filter((line, index) => !(index === 0 && line === organization))
    return dedupedLines.join(' ')
  }

  const fallbackName = lawFirmName.value || COPYRIGHT_TEXT
  return `© ${currentYear} ${fallbackName} 版权所有`
})

const icpText = computed(() => appConfigStore.icpLicense?.trim() || '')
const showFooterBrand = computed(() => {
  const organization = lawFirmName.value.trim()
  if (!organization) return false

  const normalizedCopyright = copyrightText.value.replace(/\s+/g, '').toLowerCase()
  const normalizedOrganization = organization.replace(/\s+/g, '').toLowerCase()

  return !normalizedCopyright.includes(normalizedOrganization)
})

const heroTitle = computed(() => {
  const shortName = appShortName.value
  if (!shortName) return '客户协作门户'

  const normalizedLawFirmName = lawFirmName.value.replace(/\s+/g, '').toLowerCase()
  const normalizedShortName = shortName.replace(/\s+/g, '').toLowerCase()

  if (
    normalizedShortName === normalizedLawFirmName ||
    normalizedShortName.includes(normalizedLawFirmName) ||
    normalizedLawFirmName.includes(normalizedShortName)
  ) {
    return '客户协作门户'
  }

  return shortName
})

const heroLead = computed(() => {
  return '本门户用于查阅经授权的法律文书、跟进案件动态，并在统一边界内完成与承办律师团队的协作联络。'
})

const heroMotto = computed(() => slogan.value || '')
</script>

<style scoped>
.portal-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(27, 59, 95, 0.06), transparent 28%),
    linear-gradient(180deg, #f6f4ef 0%, #f8f7f3 50%, #f2eee7 100%);
}

.content {
  display: grid;
  gap: 0;
  padding-bottom: 40px;
}

.portal-hero,
.portal-section,
.commitment,
.portal-footer {
  padding-top: 88px;
  padding-bottom: 88px;
}

.portal-hero {
  padding-top: 108px;
  padding-bottom: 120px;
  border-bottom: 1px solid rgba(16, 42, 67, 0.08);
  background: transparent;
}

.hero-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 380px);
  gap: 72px;
  align-items: start;
}

.hero-copy {
  max-width: 760px;
}

.hero-eyebrow,
.section-kicker,
.support-label {
  margin: 0 0 16px;
  color: var(--lex-primary-soft);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.hero-brand {
  margin: 0 0 18px;
  color: #1b3b5f;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 0;
  color: var(--text-primary);
  font-family: 'EB Garamond', 'PingFang SC', serif;
  font-size: clamp(28px, 3vw, 40px);
  font-weight: 600;
  line-height: 1.08;
  letter-spacing: -0.02em;
}

.hero-motto {
  margin: 0 0 18px;
  color: #102a43;
  font-family: 'EB Garamond', 'PingFang SC', serif;
  font-size: clamp(60px, 8vw, 118px);
  font-weight: 600;
  line-height: 0.94;
  letter-spacing: -0.05em;
}

.hero-lead,
.section-head p,
.capability-card p,
.instruction-intro,
.instruction-item p,
.commitment-copy p,
.commitment-support p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.85;
}

.hero-lead {
  max-width: 700px;
  margin-top: 28px;
  font-size: 18px;
  line-height: 1.95;
}

.hero-trust {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  max-width: 760px;
  margin-top: 30px;
}

.trust-item {
  display: grid;
  gap: 8px;
  padding-top: 18px;
  border-top: 1px solid rgba(16, 42, 67, 0.1);
}

.trust-item span {
  color: #829ab1;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.trust-item strong {
  color: #102a43;
  font-size: 15px;
  line-height: 1.7;
  font-weight: 600;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 36px;
}

.hero-primary,
.hero-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 52px;
  padding: 0 28px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-decoration: none;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
  text-transform: uppercase;
}

.hero-primary {
  background: var(--lex-primary);
  color: #fff;
}

.hero-secondary {
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  background: #fff;
}

.hero-primary:hover,
.hero-secondary:hover {
  transform: translateY(-1px);
}

.hero-aside {
  display: flex;
  justify-content: flex-end;
}

.hero-note-card {
  width: min(100%, 380px);
  padding: 28px;
  border: 1px solid rgba(16, 42, 67, 0.08);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 16px 30px rgba(16, 42, 67, 0.06);
}

.note-kicker {
  margin: 0 0 24px;
  color: #486581;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-note-grid {
  display: grid;
  gap: 18px;
}

.hero-note-grid article {
  display: grid;
  gap: 6px;
  padding-top: 18px;
  border-top: 1px solid rgba(16, 42, 67, 0.08);
}

.hero-note-grid article:first-child {
  padding-top: 0;
  border-top: 0;
}

.hero-note-grid span {
  color: #829ab1;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-note-grid strong {
  color: #102a43;
  font-size: 16px;
  line-height: 1.7;
  font-weight: 600;
}

.portal-section {
  background: transparent;
  border-bottom: 1px solid rgba(16, 42, 67, 0.08);
}

.portal-section--plain {
  background: transparent;
}

.section-frame {
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.section-frame--plain {
  background: transparent;
}

.section-head {
  margin-bottom: 56px;
  max-width: 720px;
  display: grid;
  gap: 16px;
}

.section-head h2,
.instruction-copy h2,
.commitment h2 {
  margin: 0;
  color: var(--text-primary);
  font-family: 'EB Garamond', 'PingFang SC', serif;
  font-size: 42px;
  font-weight: 600;
  line-height: 1.05;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 28px;
}

.capability-card {
  padding: 30px;
  border: 1px solid rgba(16, 42, 67, 0.08);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 16px 28px rgba(16, 42, 67, 0.04);
}

.capability-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-bottom: 22px;
  border-radius: 4px;
  background: rgba(27, 59, 95, 0.08);
  color: var(--lex-primary);
  font-size: 24px;
}

.capability-icon--dark {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.capability-card h3 {
  margin: 0 0 12px;
  color: var(--text-primary);
  font-size: 22px;
  line-height: 1.3;
}

.instruction-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(320px, 0.92fr);
  gap: 72px;
  align-items: start;
}

.instruction-copy {
  display: grid;
  gap: 18px;
}

.instruction-list {
  display: grid;
  gap: 30px;
  margin-top: 18px;
}

.instruction-note {
  margin: 40px 0 0;
  padding: 0 0 0 18px;
  border-left: 2px solid rgba(27, 59, 95, 0.26);
  background: transparent;
  color: var(--text-secondary);
  line-height: 1.8;
}

.instruction-item {
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr);
  gap: 24px;
}

.instruction-item span {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(27, 59, 95, 0.3);
  color: var(--lex-primary);
  font-size: 12px;
  font-weight: 700;
}

.instruction-item h3 {
  margin: 0 0 8px;
  color: var(--text-primary);
  font-size: 18px;
}

.instruction-panel {
  display: flex;
  justify-content: center;
}

.mock-card {
  width: min(100%, 360px);
  padding: 36px;
  border: 1px solid rgba(16, 42, 67, 0.08);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 20px 36px rgba(16, 42, 67, 0.06);
}

.mock-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 32px;
}

.mock-dot {
  width: 8px;
  height: 8px;
  background: var(--lex-primary);
}

.mock-line {
  width: 96px;
  height: 6px;
  background: rgba(148, 163, 184, 0.3);
}

.mock-body {
  display: grid;
  gap: 18px;
}

.mock-field {
  height: 36px;
  background: #fff;
  border: 1px solid rgba(16, 42, 67, 0.08);
}

.mock-lock {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 44px;
  padding: 0 16px;
  border: 1px solid rgba(30, 64, 175, 0.18);
  background: rgba(255, 255, 255, 0.92);
  color: var(--lex-primary);
}

.mock-lock span {
  display: block;
  width: 72px;
  height: 6px;
  background: rgba(30, 64, 175, 0.18);
}

.commitment {
  background: transparent;
  color: var(--text-primary);
}

.commitment-frame {
  display: grid;
  justify-items: center;
  text-align: center;
  padding: 72px 64px;
  border: 1px solid rgba(16, 42, 67, 0.08);
  background:
    linear-gradient(180deg, rgba(27, 59, 95, 0.94) 0%, rgba(16, 42, 67, 0.96) 100%);
  box-shadow: 0 28px 48px rgba(16, 42, 67, 0.12);
}

.commitment-icon {
  margin-bottom: 18px;
  font-size: 48px;
  color: rgba(255, 255, 255, 0.4);
}

.commitment h2 {
  color: #fff;
}

.commitment-copy {
  max-width: 820px;
  margin-top: 28px;
  display: grid;
  gap: 22px;
}

.commitment-copy p,
.commitment-support p {
  color: rgba(226, 232, 240, 0.84);
  font-size: 18px;
}

.commitment-support {
  max-width: 820px;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.support-label {
  color: rgba(226, 232, 240, 0.44);
}

.portal-footer {
  display: grid;
  justify-items: center;
  text-align: center;
  background: transparent;
}

.footer-meta {
  display: grid;
  gap: 8px;
}

.footer-brand,
.footer-copy,
.footer-icp {
  margin: 0;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  line-height: 1.8;
}

.footer-brand {
  color: var(--text-primary);
  font-weight: 600;
}

.footer-copy,
.footer-icp {
  letter-spacing: 0.04em;
}

@media (max-width: 1200px) {
  .hero-layout,
  .instruction-layout {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .hero-aside {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .portal-hero,
  .portal-section,
  .commitment,
  .portal-footer {
    padding-top: 48px;
    padding-bottom: 48px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-motto {
    font-size: clamp(48px, 16vw, 72px);
  }

  .hero-lead,
  .trust-item strong,
  .section-head p,
  .instruction-intro,
  .commitment-copy p,
  .commitment-support p {
    font-size: 16px;
  }

  .hero-trust {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .capability-grid {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .mock-card {
    padding: 24px;
  }

  .commitment-frame {
    padding: 48px 24px;
  }

  .hero-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-primary,
  .hero-secondary {
    width: 100%;
  }

  .instruction-item {
    grid-template-columns: 1fr;
  }

  .instruction-note {
    margin-top: 24px;
    padding: 16px;
  }
}
</style>
