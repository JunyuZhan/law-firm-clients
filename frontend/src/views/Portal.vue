<template>
  <div class="portal-page">
    <AppHeader variant="portal" />

    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="portal-hero section-shell fade-in">
        <p
          v-if="cardEyebrow"
          class="portal-eyebrow"
        >
          {{ cardEyebrow }}
        </p>

        <div class="hero-head">
          <div>
            <h1 class="portal-title">
              {{ heroTitle }}
            </h1>
            <p class="portal-slogan">
              {{ heroLead }}
            </p>
          </div>

          <div class="access-badge">
            <span class="access-badge__label">Access</span>
            <strong>Private Link Only</strong>
            <p>客户通过律师发送的专属链接进入对应项目。</p>
          </div>
        </div>

        <div class="portal-grid">
          <article class="info-card">
            <span class="info-card__label">系统说明</span>
            <p>
              这是一个面向客户的协作系统，用于查看项目进展、接收通知以及获取与项目相关的资料。
            </p>
          </article>

          <article class="info-card">
            <span class="info-card__label">访问方式</span>
            <p v-if="accessNoticeDisplay">
              {{ accessNoticeDisplay }}
            </p>
            <p v-else>
              门户首页不提供公开登录。客户收到带有 Token 的专属链接后，可直接访问对应项目页面。
            </p>
          </article>

          <article class="info-card info-card--steps">
            <span class="info-card__label">访问流程</span>
            <ol class="access-steps">
              <li>律师发送项目专属链接</li>
              <li>系统校验链接中的 Token</li>
              <li>客户直接进入对应项目页面</li>
            </ol>
          </article>
        </div>
      </section>

      <footer
        v-if="footerText || staffEntryLabel || lawFirmName"
        class="portal-footer section-shell"
      >
        <p
          v-if="lawFirmName"
          class="footer-brand"
        >
          {{ lawFirmName }}
        </p>
        <p
          v-if="footerText"
          class="footer-meta"
        >
          {{ footerText }}
        </p>
        <p
          v-if="staffEntryLabel"
          class="portal-staff-line"
        >
          <router-link
            to="/admin/login"
            class="portal-staff-link"
          >
            {{ staffEntryLabel }}
          </router-link>
        </p>
      </footer>
    </a-layout-content>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { APP_SLOGAN } from '@/config/app'

const appConfigStore = useAppConfigStore()

const slogan = computed(() => appConfigStore.appSlogan?.trim() || APP_SLOGAN)
const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || '')

const cardEyebrow = computed(() => {
  const custom = appConfigStore.portalEyebrowEn?.trim()
  if (custom) return custom
  return appConfigStore.appShortNameEn?.trim() || 'Private Client Portal'
})

const accessNoticeDisplay = computed(() => appConfigStore.portalAccessNotice?.trim() || '')
const staffEntryLabel = computed(() => appConfigStore.staffEntryLabel?.trim() || '')

const footerText = computed(() => {
  const icp = appConfigStore.icpLicense?.trim()
  const copy = appConfigStore.copyright?.trim()
  if (icp && copy) return `${copy} · ${icp}`
  return icp || copy || ''
})

const heroTitle = computed(() => lawFirmName.value || appConfigStore.displayName?.trim() || '客户协作门户')

const heroLead = computed(() => {
  if (slogan.value) return slogan.value
  return '客户通过专属链接访问具体项目，查看进展、资料与通知。'
})
</script>

<style scoped>
.portal-page {
  min-height: 100vh;
  background: transparent;
}

.content {
  display: grid;
  gap: 16px;
  padding-bottom: 32px;
}

.portal-hero {
  display: grid;
  gap: 24px;
  padding: 28px;
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.96));
  box-shadow: var(--shadow-sm);
}

.portal-eyebrow,
.info-card__label,
.access-badge__label {
  margin: 0;
  color: var(--lex-accent-strong);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-head {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(240px, 320px);
  gap: 20px;
  align-items: start;
}

.portal-title {
  margin: 0;
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1.08;
  color: var(--lex-primary);
  font-family: var(--font-heading);
}

.portal-slogan {
  max-width: 44rem;
  margin: 14px 0 0;
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.8;
}

.access-badge {
  display: grid;
  gap: 8px;
  padding: 18px;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(30, 64, 175, 0.12);
  background: var(--lex-bg);
}

.access-badge strong {
  color: var(--text-primary);
  font-size: 20px;
  line-height: 1.2;
}

.access-badge p,
.info-card p,
.footer-meta,
.portal-staff-line {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.portal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.info-card {
  display: grid;
  gap: 12px;
  min-height: 100%;
  padding: 18px;
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-md);
  background: var(--lex-surface-strong);
}

.info-card--steps {
  background: linear-gradient(180deg, var(--lex-surface-strong) 0%, var(--lex-bg) 100%);
}

.access-steps {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.9;
}

.portal-footer {
  display: grid;
  gap: 6px;
  padding-top: 6px;
}

.portal-footer p {
  text-align: center;
}

.footer-brand {
  margin: 0;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 600;
}

.footer-meta {
  font-size: 12px;
}

.portal-staff-line {
  font-size: 12px;
}

.portal-staff-link {
  color: var(--text-tertiary);
  text-decoration: none;
}

.portal-staff-link:hover {
  color: var(--lex-primary-soft);
  text-decoration: underline;
}

@media (max-width: 960px) {
  .hero-head,
  .portal-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .portal-hero {
    padding: 20px;
  }

  .portal-slogan {
    font-size: 15px;
  }
}
</style>
