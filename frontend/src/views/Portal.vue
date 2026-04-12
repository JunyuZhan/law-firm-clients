<template>
  <div class="portal-page">
    <AppHeader variant="portal" />

    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="portal-stage section-shell fade-in">
        <div class="stage-grid">
          <div class="stage-copy">
            <p
              v-if="cardEyebrow"
              class="stage-eyebrow"
            >
              {{ cardEyebrow }}
            </p>

            <div class="title-block">
              <p class="title-prefix">
                Private Client System
              </p>
              <h1 class="stage-title">
                {{ heroTitle }}
              </h1>
            </div>

            <p class="stage-lead">
              {{ heroLead }}
            </p>

            <div class="stage-note">
              <span class="stage-note__label">Access Notice</span>
              <p v-if="accessNoticeDisplay">
                {{ accessNoticeDisplay }}
              </p>
              <p v-else>
                门户页仅用于说明系统与访问方式，不提供公开登录。客户收到律师发送的带 Token 专属链接后，直接进入对应项目页面。
              </p>
            </div>
          </div>

          <aside class="stage-panel">
            <div class="panel-surface">
              <div class="panel-head">
                <span class="panel-kicker">Controlled Access</span>
                <strong>Private Link Only</strong>
              </div>

              <ol class="panel-steps">
                <li>
                  <span>01</span>
                  <div>
                    <strong>律师发送专属链接</strong>
                    <p>每位客户通过对应项目的私有链接访问，不经过公开入口。</p>
                  </div>
                </li>
                <li>
                  <span>02</span>
                  <div>
                    <strong>系统完成 Token 校验</strong>
                    <p>链接中的验证信息用于识别访问范围与有效期。</p>
                  </div>
                </li>
                <li>
                  <span>03</span>
                  <div>
                    <strong>直接进入项目协作页面</strong>
                    <p>客户查看项目进展、资料与通知，不需要先在门户页登录。</p>
                  </div>
                </li>
              </ol>
            </div>
          </aside>
        </div>
      </section>

      <footer
        v-if="footerText || staffEntryLabel || lawFirmName"
        class="portal-footer section-shell"
      >
        <div class="footer-rule" />
        <div class="footer-meta">
          <p
            v-if="lawFirmName"
            class="footer-brand"
          >
            {{ lawFirmName }}
          </p>
          <p
            v-if="footerText"
            class="footer-copy"
          >
            {{ footerText }}
          </p>
        </div>
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
  if (slogan.value) {
    return `${slogan.value}。客户通过律师发送的专属链接进入对应项目，在同一界面查看进展、接收通知并获取相关资料。`
  }
  return '客户通过律师发送的专属链接进入对应项目，在同一界面查看进展、接收通知并获取相关资料。'
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap');

.portal-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 14% 18%, rgba(202, 138, 4, 0.08), transparent 26%),
    radial-gradient(circle at 88% 20%, rgba(30, 64, 175, 0.1), transparent 30%),
    linear-gradient(180deg, #fcfbf8 0%, #f8fafc 48%, #eef2f7 100%);
}

.content {
  display: grid;
  gap: 18px;
  padding-bottom: 40px;
}

.portal-stage {
  position: relative;
  overflow: hidden;
  padding: 36px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(249, 250, 251, 0.92)),
    var(--lex-surface-strong);
  box-shadow:
    0 20px 60px rgba(15, 23, 42, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.portal-stage::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(90deg, transparent 0, transparent calc(100% - 1px), rgba(15, 23, 42, 0.05) calc(100% - 1px)),
    linear-gradient(180deg, transparent 0, transparent calc(100% - 1px), rgba(15, 23, 42, 0.05) calc(100% - 1px));
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.45), transparent 88%);
}

.stage-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 430px);
  gap: 36px;
  align-items: stretch;
}

.stage-copy {
  display: grid;
  align-content: start;
  gap: 28px;
  min-width: 0;
}

.stage-eyebrow,
.title-prefix,
.stage-note__label,
.panel-kicker {
  margin: 0;
  color: var(--lex-accent-strong);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.title-block {
  display: grid;
  gap: 12px;
}

.stage-title {
  max-width: 11ch;
  margin: 0;
  color: #0f172a;
  font-family: 'Playfair Display', Georgia, serif;
  font-size: clamp(44px, 6vw, 78px);
  line-height: 0.96;
  letter-spacing: -0.04em;
}

.stage-lead,
.stage-note p,
.panel-steps p,
.footer-copy,
.portal-staff-line {
  margin: 0;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.stage-lead {
  max-width: 42rem;
  color: var(--text-secondary);
  font-size: 17px;
  line-height: 1.95;
}

.stage-note {
  display: grid;
  gap: 12px;
  max-width: 44rem;
  padding: 22px 24px;
  border-left: 2px solid rgba(202, 138, 4, 0.42);
  background: rgba(255, 255, 255, 0.58);
}

.stage-note p {
  color: var(--text-secondary);
  line-height: 1.9;
}

.stage-panel {
  display: flex;
}

.panel-surface {
  width: 100%;
  display: grid;
  gap: 26px;
  padding: 26px;
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.98) 0%, rgba(30, 41, 59, 0.98) 100%);
  color: #f8fafc;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.18);
}

.panel-head {
  display: grid;
  gap: 10px;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.panel-head strong {
  font-size: 28px;
  line-height: 1.05;
  font-weight: 600;
}

.panel-steps {
  display: grid;
  gap: 18px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.panel-steps li {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 14px;
  align-items: start;
}

.panel-steps span {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 999px;
  background: rgba(202, 138, 4, 0.12);
  color: #f8fafc;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.panel-steps strong {
  display: block;
  margin-bottom: 6px;
  font-size: 15px;
  line-height: 1.5;
}

.panel-steps p {
  color: rgba(226, 232, 240, 0.78);
  font-size: 13px;
  line-height: 1.8;
}

.portal-footer {
  display: grid;
  gap: 12px;
  padding-top: 2px;
}

.footer-rule {
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(15, 23, 42, 0.16) 18%, rgba(15, 23, 42, 0.16) 82%, transparent 100%);
}

.footer-meta {
  display: grid;
  gap: 6px;
  justify-items: center;
}

.footer-brand {
  margin: 0;
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.footer-copy {
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.7;
  text-align: center;
}

.portal-staff-line {
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.7;
  text-align: center;
}

.portal-staff-link {
  color: inherit;
  text-decoration: none;
}

.portal-staff-link:hover {
  color: var(--lex-primary-soft);
}

@media (max-width: 1024px) {
  .stage-grid {
    grid-template-columns: 1fr;
  }

  .stage-title {
    max-width: none;
  }
}

@media (max-width: 768px) {
  .portal-stage {
    padding: 22px;
    border-radius: 14px;
  }

  .stage-grid {
    gap: 24px;
  }

  .stage-copy {
    gap: 22px;
  }

  .stage-lead {
    font-size: 15px;
  }

  .panel-surface {
    padding: 20px;
  }
}
</style>
