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
          <div class="hero-panel">
            <div class="hero-copy">
              <p
                v-if="heroEyebrow"
                class="hero-eyebrow"
              >
                {{ heroEyebrow }}
              </p>
              <h1 class="hero-title">
                {{ heroTitle }}
              </h1>
              <p class="hero-lead">
                {{ heroLead }}
              </p>

              <div class="hero-actions">
                <a
                  href="#instructions"
                  class="hero-primary"
                >
                  访问机制说明
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
          </div>
        </div>
      </section>

      <section
        id="capabilities"
        class="portal-section section-shell"
      >
        <div class="section-frame">
          <div class="section-head">
            <p class="section-kicker">服务能力</p>
            <h2>客户协作与资料管理</h2>
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

              <div class="instruction-list">
                <article class="instruction-item">
                  <span>01</span>
                  <div>
                    <h3>定向准入制</h3>
                    <p>本门户仅向已建立正式委托关系的特定客户开放，不设公开注册入口，以确保相关资料保持必要的隔离性。</p>
                  </div>
                </article>

                <article class="instruction-item">
                  <span>02</span>
                  <div>
                    <h3>基于受控链接的访问机制</h3>
                    <p>访问权限由承办律师根据案件保密要求进行管理，客户通过预留的联络渠道获取对应的访问凭证。</p>
                  </div>
                </article>

                <article class="instruction-item">
                  <span>03</span>
                  <div>
                    <h3>权限动态管理</h3>
                    <p>访问范围严格限定于当前委托事项，并依据协作进度及合规要求进行持续审核与管理。</p>
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
            <p>我们高度重视法律事务的私密性。本门户的设计与运行逻辑均遵循律师职业道德规范及行业数据保护要求。</p>
            <p>通过访问控制与协作流程管理，确保数字化案卷与线上沟通记录始终处于必要边界之内。维护客户的信息安全与利益，是服务过程中的基本前提。</p>
          </div>
          <div class="commitment-support">
            <p class="support-label">业务支持</p>
            <p>如在访问过程中遇到问题或需调整协作权限，请通过预留联络方式直接联系您的承办律师团队。</p>
          </div>
        </div>
      </section>

      <footer
        class="portal-footer section-shell"
      >
        <div class="footer-meta">
          <p class="footer-brand">
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
  const configured = appConfigStore.copyright?.trim()
  if (configured) return configured

  const fallbackName = lawFirmName.value || COPYRIGHT_TEXT
  return `© ${currentYear} ${fallbackName} 版权所有`
})

const icpText = computed(() => appConfigStore.icpLicense?.trim() || '')

const heroTitle = computed(() => {
  const shortName = appShortName.value
  if (!shortName) return `${lawFirmName.value} 客户协作门户`

  const normalizedLawFirmName = lawFirmName.value.replace(/\s+/g, '').toLowerCase()
  const normalizedShortName = shortName.replace(/\s+/g, '').toLowerCase()

  if (
    normalizedShortName === normalizedLawFirmName ||
    normalizedShortName.includes(normalizedLawFirmName) ||
    normalizedLawFirmName.includes(normalizedShortName)
  ) {
    return `${lawFirmName.value} 客户协作门户`
  }

  return `${lawFirmName.value} ${shortName}`
})

const heroLead = computed(() => {
  if (slogan.value) {
    return `${slogan.value}。本门户旨在为委托客户提供受控的数字化协作环境，用于查阅经授权的法律文书、追踪事务进展，并与承办律师团队保持高效专业的联络。`
  }
  return '本门户旨在为委托客户提供受控的数字化协作环境，用于查阅经授权的法律文书、追踪事务进展，并与承办律师团队保持高效专业的联络。'
})
</script>

<style scoped>
.portal-page {
  min-height: 100vh;
  background: #f7f5f1;
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
  padding-top: 72px;
  padding-bottom: 72px;
}

.portal-hero {
  padding-top: 104px;
  padding-bottom: 120px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
  background: transparent;
}

.hero-layout {
  display: block;
}

.hero-panel {
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.hero-copy {
  max-width: 840px;
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

.hero-title {
  margin: 0;
  color: var(--text-primary);
  font-size: clamp(48px, 6vw, 76px);
  font-weight: 600;
  line-height: 1.18;
  letter-spacing: -0.03em;
}

.hero-lead,
.capability-card p,
.instruction-item p,
.commitment-copy p,
.commitment-support p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.85;
}

.hero-lead {
  max-width: 700px;
  margin-top: 32px;
  font-size: 19px;
  line-height: 2;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  margin-top: 44px;
}

.hero-primary,
.hero-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 52px;
  padding: 0 28px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
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

.portal-section {
  background: transparent;
  border-bottom: 1px solid rgba(148, 163, 184, 0.14);
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
}

.section-head h2,
.instruction-copy h2,
.commitment h2 {
  margin: 0;
  color: var(--text-primary);
  font-size: 34px;
  font-weight: 700;
  line-height: 1.2;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 28px 48px;
}

.capability-card {
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
}

.capability-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-bottom: 28px;
  border-radius: 4px;
  background: var(--lex-primary-container, rgba(30, 64, 175, 0.1));
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
  font-size: 24px;
  line-height: 1.3;
}

.instruction-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr);
  gap: 88px;
  align-items: start;
}

.instruction-list {
  display: grid;
  gap: 30px;
  margin-top: 36px;
}

.instruction-note {
  margin: 40px 0 0;
  padding: 0 0 0 18px;
  border-left: 2px solid rgba(30, 64, 175, 0.3);
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
  border: 1px solid var(--lex-primary);
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
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.72);
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
  border: 1px solid rgba(148, 163, 184, 0.18);
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
  background: var(--lex-surface-dark);
  color: #fff;
}

.commitment-frame {
  display: grid;
  justify-items: center;
  text-align: center;
  padding: 96px 0;
  border: 0;
  border-radius: 0;
  background: transparent;
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
  .capability-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .instruction-layout {
    grid-template-columns: 1fr;
    gap: 40px;
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
    font-size: clamp(34px, 10vw, 52px);
  }

  .hero-lead,
  .commitment-copy p,
  .commitment-support p {
    font-size: 16px;
  }

  .capability-grid {
    grid-template-columns: 1fr;
    gap: 32px;
  }

  .mock-card {
    padding: 24px;
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
