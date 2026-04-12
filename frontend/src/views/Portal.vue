<template>
  <div class="portal-page">
    <AppHeader variant="portal" />

    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="portal-hero section-shell fade-in">
        <div class="hero-copy">
          <p
            v-if="cardEyebrow"
            class="hero-eyebrow"
          >
            {{ cardEyebrow }}
          </p>
          <h1 class="hero-title">
            面向客户的专属案件协作门户
          </h1>
          <p class="hero-lead">
            {{ heroLead }}
          </p>

          <div class="hero-signals">
            <span class="signal-pill">
              非公开访问
            </span>
            <span class="signal-pill">
              专属 Token 验证
            </span>
            <span class="signal-pill">
              文件与进度同步
            </span>
          </div>
        </div>

        <aside class="hero-aside">
          <div class="hero-note-card">
            <span class="note-label">访问方式</span>
            <p class="note-title">
              请通过律师发送的专属链接进入项目页面
            </p>
            <p
              v-if="accessNoticeDisplay"
              class="note-copy"
            >
              {{ accessNoticeDisplay }}
            </p>
            <p
              v-else
              class="note-copy"
            >
              门户首页用于说明系统能力与访问方式，不提供公开登录入口。客户收到专属链接后，可直接查看对应项目。
            </p>
          </div>
        </aside>
      </section>

      <section class="capability-grid">
        <article class="capability-card section-shell">
          <FileTextOutlined class="capability-icon" />
          <h2>项目进展查看</h2>
          <p>围绕单个项目查看状态、节点、更新时间与关键说明，减少重复沟通。</p>
        </article>

        <article class="capability-card section-shell">
          <FolderOpenOutlined class="capability-icon" />
          <h2>文件集中交付</h2>
          <p>在统一空间内查看、上传和下载项目资料，保留清晰的交付轨迹。</p>
        </article>

        <article class="capability-card section-shell">
          <NotificationOutlined class="capability-icon" />
          <h2>通知及时同步</h2>
          <p>重要更新通过门户、短信、邮件或微信同步触达，减少信息遗漏。</p>
        </article>
      </section>

      <section class="portal-band section-shell">
        <div class="band-copy">
          <p class="band-label">Private Access</p>
          <h2>这是一个受控访问的客户协作界面，而不是公开业务页面。</h2>
        </div>
        <div class="band-points">
          <div class="band-point">
            <span>01</span>
            <p>客户收到律师发送的项目专属链接</p>
          </div>
          <div class="band-point">
            <span>02</span>
            <p>系统通过链接中的 Token 校验访问权限</p>
          </div>
          <div class="band-point">
            <span>03</span>
            <p>客户直接进入对应项目，而非先登录门户首页</p>
          </div>
        </div>
      </section>

      <section class="actions-grid">
        <router-link
          to="/help"
          class="action-card section-shell"
        >
          <div class="action-head">
            <QuestionCircleOutlined class="action-icon" />
            <span>帮助中心</span>
          </div>
          <p>查看访问说明、常见问题与异常处理方式。</p>
        </router-link>

        <router-link
          to="/verify/letter"
          class="action-card section-shell"
        >
          <div class="action-head">
            <SafetyCertificateOutlined class="action-icon" />
            <span>函件验真</span>
          </div>
          <p>核验函件信息与签发结果，获得即时反馈。</p>
        </router-link>
      </section>

      <footer
        v-if="footerText || staffEntryLabel"
        class="portal-footer"
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
import {
  FileTextOutlined,
  FolderOpenOutlined,
  NotificationOutlined,
  QuestionCircleOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { APP_SLOGAN } from '@/config/app'

const appConfigStore = useAppConfigStore()

const slogan = computed(() => appConfigStore.appSlogan?.trim() || APP_SLOGAN)
const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || '')

const cardEyebrow = computed(() => {
  const custom = appConfigStore.portalEyebrowEn?.trim()
  if (custom) return custom
  return appConfigStore.appShortNameEn?.trim() || 'Private Client Access'
})

const accessNoticeDisplay = computed(() => appConfigStore.portalAccessNotice?.trim() || '')

const staffEntryLabel = computed(() => appConfigStore.staffEntryLabel?.trim() || '')

const footerText = computed(() => {
  const icp = appConfigStore.icpLicense?.trim()
  const copy = appConfigStore.copyright?.trim()
  if (icp && copy) return `${copy} · ${icp}`
  return icp || copy || ''
})

const heroLead = computed(() => {
  if (slogan.value) {
    return `${slogan.value}。客户通常通过律师发送的专属链接直接进入对应项目，在同一界面完成进度查看、资料获取与通知接收。`
  }
  return '客户通常通过律师发送的专属链接直接进入对应项目，在同一界面完成进度查看、资料获取与通知接收。'
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=EB+Garamond:wght@500;600;700&family=Lato:wght@400;500;700&display=swap');

.portal-page {
  position: relative;
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(30, 58, 138, 0.12), transparent 32%),
    radial-gradient(circle at 85% 18%, rgba(180, 83, 9, 0.12), transparent 24%),
    linear-gradient(180deg, #f8fafc 0%, #f3f6fb 46%, #eef2f8 100%);
}

.portal-page::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(rgba(255, 255, 255, 0.32), rgba(255, 255, 255, 0.32)),
    repeating-linear-gradient(
      90deg,
      transparent 0,
      transparent 72px,
      rgba(15, 23, 42, 0.02) 72px,
      rgba(15, 23, 42, 0.02) 73px
    );
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.42), transparent 85%);
}

.content {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 24px;
  padding-bottom: 36px;
}

.portal-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(320px, 0.9fr);
  gap: 24px;
  padding: 32px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.97), rgba(248, 250, 252, 0.92)),
    var(--lex-surface-strong);
  border: 1px solid rgba(148, 163, 184, 0.22);
  box-shadow:
    0 24px 60px rgba(15, 23, 42, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.75);
}

.hero-copy {
  min-width: 0;
}

.hero-eyebrow,
.band-label,
.note-label {
  margin: 0 0 14px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: var(--lex-accent-strong);
}

.hero-title {
  max-width: 12ch;
  margin: 0;
  color: #0f172a;
  font-family: 'EB Garamond', Georgia, serif;
  font-size: clamp(40px, 5vw, 68px);
  font-weight: 600;
  line-height: 0.98;
  letter-spacing: -0.03em;
}

.hero-lead,
.note-copy,
.capability-card p,
.band-point p,
.action-card p,
.footer-meta {
  font-family: 'Lato', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.hero-lead {
  max-width: 62ch;
  margin: 24px 0 0;
  font-size: 16px;
  line-height: 1.9;
  color: var(--text-secondary);
}

.hero-signals {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.signal-pill {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid rgba(30, 58, 138, 0.12);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  color: #1e3a8a;
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.hero-aside {
  display: flex;
  align-items: stretch;
}

.hero-note-card {
  position: relative;
  width: 100%;
  padding: 24px;
  border-radius: 24px;
  background:
    linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(15, 23, 42, 0.88)),
    var(--lex-surface-dark);
  color: rgba(248, 250, 252, 0.92);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.24);
  overflow: hidden;
}

.hero-note-card::after {
  content: '';
  position: absolute;
  inset: auto -36px -36px auto;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.32), transparent 68%);
}

.note-title {
  position: relative;
  z-index: 1;
  margin: 0 0 14px;
  color: #f8fafc;
  font-family: 'EB Garamond', Georgia, serif;
  font-size: 30px;
  line-height: 1.08;
}

.note-copy {
  position: relative;
  z-index: 1;
  margin: 0;
  font-size: 14px;
  line-height: 1.9;
  color: rgba(226, 232, 240, 0.88);
}

.capability-grid,
.actions-grid {
  display: grid;
  gap: 16px;
}

.capability-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.capability-card {
  padding: 24px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(16px);
  transition:
    transform 220ms ease,
    box-shadow 220ms ease,
    border-color 220ms ease;
}

.capability-card:hover {
  transform: translateY(-2px);
  border-color: rgba(30, 58, 138, 0.2);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);
}

.capability-icon,
.action-icon {
  color: var(--lex-primary-soft);
  font-size: 20px;
}

.capability-card h2,
.band-copy h2,
.action-head span,
.footer-brand {
  font-family: 'EB Garamond', Georgia, serif;
}

.capability-card h2 {
  margin: 18px 0 10px;
  color: var(--lex-primary);
  font-size: 27px;
  font-weight: 600;
  line-height: 1.1;
}

.capability-card p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.85;
}

.portal-band {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 1.2fr);
  gap: 22px;
  padding: 26px 28px;
  background:
    linear-gradient(120deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.76)),
    var(--lex-surface-strong);
}

.band-copy h2 {
  max-width: 18ch;
  margin: 0;
  color: #0f172a;
  font-size: clamp(28px, 3.2vw, 42px);
  line-height: 1.05;
  font-weight: 600;
}

.band-points {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.band-point {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(255, 255, 255, 0.7);
}

.band-point span {
  display: inline-flex;
  margin-bottom: 12px;
  color: var(--lex-accent-strong);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.band-point p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.75;
}

.actions-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.action-card {
  display: block;
  padding: 22px 24px;
  color: inherit;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.86);
  transition:
    transform 220ms ease,
    box-shadow 220ms ease,
    border-color 220ms ease;
}

.action-card:hover {
  transform: translateY(-2px);
  border-color: rgba(180, 83, 9, 0.2);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.08);
}

.action-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.action-head span {
  color: var(--lex-primary);
  font-size: 28px;
  line-height: 1;
}

.action-card p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;
}

.portal-footer {
  display: grid;
  gap: 6px;
  justify-items: center;
  padding: 12px 0 4px;
}

.footer-brand {
  color: var(--lex-primary);
  font-size: 19px;
}

.footer-meta,
.portal-staff-line {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.7;
  text-align: center;
}

.portal-staff-link {
  color: inherit;
  text-decoration: none;
  transition: color 180ms ease;
}

.portal-staff-link:hover {
  color: var(--lex-primary-soft);
  text-decoration: underline;
}

@media (max-width: 1024px) {
  .portal-hero,
  .portal-band,
  .capability-grid,
  .actions-grid {
    grid-template-columns: 1fr;
  }

  .band-points {
    grid-template-columns: 1fr;
  }

  .hero-title {
    max-width: none;
  }
}

@media (max-width: 768px) {
  .content {
    gap: 18px;
  }

  .portal-hero,
  .portal-band,
  .capability-card,
  .action-card {
    padding: 20px;
  }

  .hero-title {
    font-size: 40px;
    line-height: 1.02;
  }

  .hero-lead {
    font-size: 15px;
    line-height: 1.8;
  }

  .note-title {
    font-size: 26px;
  }

  .capability-card h2,
  .action-head span {
    font-size: 24px;
  }

  .signal-pill {
    min-height: 34px;
    font-size: 12px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .capability-card,
  .action-card,
  .portal-staff-link {
    transition: none;
  }

  .capability-card:hover,
  .action-card:hover {
    transform: none;
  }
}
</style>
