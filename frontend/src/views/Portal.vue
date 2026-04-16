<template>
  <div class="portal-page">
    <header class="portal-header">
      <div class="portal-header__inner section-shell">
        <div class="portal-brand">
          <div class="portal-brand__logo">
            <img
              v-if="logoUrl"
              :src="logoUrl"
              alt="律所标识"
              class="portal-brand__image"
            >
            <BankOutlined v-else />
          </div>
          <div class="portal-brand__copy">
            <p class="portal-brand__name">{{ lawFirmName }}</p>
            <p class="portal-brand__meta">{{ portalLabel }}</p>
          </div>
        </div>

        <button
          type="button"
          class="portal-login"
          :title="staffEntryTitle"
          :aria-label="staffEntryTitle"
          @click="goToAdminLogin"
        >
          <LoginOutlined />
        </button>
      </div>
    </header>

    <main class="portal-content">
      <section class="portal-hero section-shell fade-in">
        <div class="portal-hero__layout">
          <div class="portal-hero__copy">
            <p
              v-if="heroEyebrow"
              class="portal-hero__eyebrow"
            >
              {{ heroEyebrow }}
            </p>

            <p class="portal-hero__firm">{{ lawFirmName }}</p>

            <p
              v-if="heroSlogan"
              class="portal-hero__slogan"
            >
              {{ heroSlogan }}
            </p>

            <h1 class="portal-hero__title">
              客户服务系统
            </h1>

            <p class="portal-hero__lead">
              本系统用于向正式委托客户展示并提供受控的数字化协作能力，用于查阅授权文书、跟进事项进展、接收重要通知并与承办律师团队保持专业联络。
            </p>

            <div class="portal-hero__actions">
              <a
                href="#portal-access"
                class="hero-action hero-action--primary"
              >
                查看访问说明
              </a>
              <a
                href="#portal-features"
                class="hero-action hero-action--secondary"
              >
                查看系统功能
              </a>
            </div>
          </div>

          <aside class="portal-hero__panel">
            <div class="portal-panel">
              <p class="portal-panel__kicker">核心能力</p>
              <div class="portal-panel__list">
                <article
                  v-for="item in featuredSummary"
                  :key="item.label"
                  class="portal-panel__item"
                >
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </article>
              </div>
            </div>
          </aside>
        </div>
      </section>

      <section
        id="portal-features"
        class="portal-features section-shell"
      >
        <div class="portal-section-head">
          <p class="portal-section-head__eyebrow">系统功能</p>
          <h2>围绕案件协作建立统一入口</h2>
          <p>页面只展示与客户协作直接相关的能力，不做复杂导航，不引入后台感，只保留最需要被理解和信任的功能表达。</p>
        </div>

        <div class="portal-feature-grid">
          <article class="portal-feature portal-feature--lead">
            <div class="portal-feature__icon">
              <DeploymentUnitOutlined />
            </div>
            <p class="portal-feature__kicker">统一协作主界面</p>
            <h3>从案件动态到材料流转，维持单一清晰的数字化工作边界</h3>
            <p>
              客户进入系统后，面对的是围绕单个委托事项组织起来的协作环境，而不是分散的功能入口。所有进展、文件、通知与补充材料都在同一边界内完成流转。
            </p>
          </article>

          <article
            v-for="feature in features"
            :key="feature.title"
            class="portal-feature"
          >
            <div class="portal-feature__icon">
              <component :is="feature.icon" />
            </div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.description }}</p>
          </article>
        </div>
      </section>

      <section
        id="portal-access"
        class="portal-access section-shell"
      >
        <div class="portal-access__frame">
          <div class="portal-access__copy">
            <p class="portal-section-head__eyebrow">门户访问说明</p>
            <h2>本门户仅面向正式委托客户开放</h2>
            <p class="portal-access__lead">
              门户不是公开注册产品，而是律师服务流程的一部分。访问范围、资料可见性与协作权限，均以具体委托事项和授权边界为准。
            </p>

            <div class="portal-access__rules">
              <article
                v-for="rule in accessRules"
                :key="rule.title"
                class="portal-access__rule"
              >
                <strong>{{ rule.title }}</strong>
                <p>{{ rule.description }}</p>
              </article>
            </div>

            <p
              v-if="accessNotice"
              class="portal-access__notice"
            >
              {{ accessNotice }}
            </p>
          </div>

          <aside class="portal-access__side">
            <div class="portal-security-card">
              <div class="portal-security-card__head">
                <SafetyCertificateOutlined />
                <span>受控访问边界</span>
              </div>

              <div class="portal-security-card__body">
                <article
                  v-for="point in securityPoints"
                  :key="point.label"
                  class="portal-security-card__row"
                >
                  <span>{{ point.label }}</span>
                  <strong>{{ point.value }}</strong>
                </article>
              </div>
            </div>
          </aside>
        </div>
      </section>

      <footer class="portal-footer section-shell">
        <p class="portal-footer__copy">{{ copyrightText }}</p>
        <p
          v-if="icpText"
          class="portal-footer__icp"
        >
          {{ icpText }}
        </p>
      </footer>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  BankOutlined,
  BellOutlined,
  DeploymentUnitOutlined,
  FileSearchOutlined,
  FileTextOutlined,
  FolderOpenOutlined,
  LoginOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { APP_SLOGAN, APP_SHORT_NAME, APP_SHORT_NAME_EN, COPYRIGHT_TEXT } from '@/config/app'

interface FeatureItem {
  title: string
  description: string
  icon: object
}

const router = useRouter()
const appConfigStore = useAppConfigStore()

const features: FeatureItem[] = [
  {
    title: '案件 / 事项进展查看',
    description: '实时查看关键节点、当前状态与阶段性推进情况，减少沟通断层造成的信息偏差。',
    icon: FileSearchOutlined,
  },
  {
    title: '授权文件查阅',
    description: '对已获授权的法律文书、意见文件与证据材料进行在线预览、查阅与下载。',
    icon: FolderOpenOutlined,
  },
  {
    title: '通知与提醒接收',
    description: '围绕重要时间点、文件更新与协作动作接收定向提醒，保持事务节奏同步。',
    icon: BellOutlined,
  },
  {
    title: '客户协作联络',
    description: '围绕同一委托事项与承办律师团队进行反馈、补充与确认，保留完整上下文。',
    icon: TeamOutlined,
  },
  {
    title: '材料提交 / 上传',
    description: '将补充资料、扫描件及电子证据在受控边界内递交并纳入统一资料流转。',
    icon: UploadOutlined,
  },
  {
    title: '专属门户访问',
    description: '每位客户只在已授权范围内查看属于自身事项的数据与文档，不暴露无关信息。',
    icon: FileTextOutlined,
  },
]

const featuredSummary = [
  { label: '案件动态', value: '按事项持续更新' },
  { label: '授权文书', value: '集中查阅与下载' },
  { label: '协作方式', value: '与承办律师定向联络' },
]

const accessRules = [
  {
    title: '仅对正式委托客户开放',
    description: '门户不面向公众开放，不提供公开注册入口，访问资格以实际委托关系为前提。',
  },
  {
    title: '按授权边界展示资料',
    description: '可见内容仅限当前事项所需的进展、文件和通知，避免信息超范围暴露。',
  },
  {
    title: '协作行为保持留痕',
    description: '重要资料提交、查看与通知反馈围绕统一流程组织，便于后续核对与持续跟进。',
  },
]

const securityPoints = [
  { label: '开放对象', value: '正式委托客户' },
  { label: '访问方式', value: '授权后进入受控空间' },
  { label: '权限边界', value: '按事项与角色限定' },
]

const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || '律师事务所')
const heroEyebrow = computed(() => appConfigStore.portalEyebrowEn?.trim() || APP_SHORT_NAME_EN || '')
const heroSlogan = computed(() => appConfigStore.appSlogan?.trim() || APP_SLOGAN)
const portalLabel = computed(() => appConfigStore.appShortName?.trim() || APP_SHORT_NAME || '客户服务系统')
const accessNotice = computed(() => appConfigStore.portalAccessNotice?.trim() || '')
const icpText = computed(() => appConfigStore.icpLicense?.trim() || '')
const logoUrl = computed(() => appConfigStore.logoUrl?.trim() || '')
const staffEntryTitle = computed(() => appConfigStore.staffEntryLabel?.trim() || '登录入口')

const currentYear = new Date().getFullYear()

const copyrightText = computed(() => {
  const configuredLines = appConfigStore.copyright
    ?.split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean) ?? []

  if (configuredLines.length > 0) {
    if (configuredLines.length === 1) {
      return configuredLines[0]
    }

    return configuredLines.join(' ')
  }

  const fallbackName = lawFirmName.value || COPYRIGHT_TEXT
  return `© ${currentYear} ${fallbackName} 版权所有`
})

function goToAdminLogin() {
  router.push('/admin/login')
}
</script>

<style scoped>
.portal-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(27, 59, 95, 0.1), transparent 28%),
    linear-gradient(180deg, #f6f7f9 0%, #eef1f5 52%, #f7f9fc 100%);
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 40;
  border-bottom: 1px solid rgba(27, 59, 95, 0.08);
  background: rgba(247, 249, 252, 0.84);
  backdrop-filter: blur(18px);
}

.portal-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding-top: 14px;
  padding-bottom: 14px;
}

.portal-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.portal-brand__logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: 1px solid rgba(27, 59, 95, 0.12);
  background: rgba(255, 255, 255, 0.92);
  color: var(--lex-primary);
  box-shadow: 0 10px 24px rgba(16, 42, 67, 0.06);
}

.portal-brand__image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.portal-brand__copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.portal-brand__name,
.portal-brand__meta {
  margin: 0;
}

.portal-brand__name {
  color: #102a43;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.portal-brand__meta {
  color: #627d98;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.portal-login {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: 1px solid rgba(27, 59, 95, 0.12);
  background: rgba(255, 255, 255, 0.94);
  color: #1b3b5f;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.portal-login:hover {
  transform: translateY(-1px);
  border-color: rgba(27, 59, 95, 0.22);
  box-shadow: 0 12px 24px rgba(16, 42, 67, 0.08);
}

.portal-login:focus-visible,
.hero-action:focus-visible {
  outline: 2px solid rgba(27, 59, 95, 0.3);
  outline-offset: 2px;
}

.portal-content {
  padding-bottom: 40px;
}

.portal-hero,
.portal-features,
.portal-access,
.portal-footer {
  padding-top: 88px;
  padding-bottom: 88px;
}

.portal-hero {
  padding-top: 92px;
}

.portal-hero__layout,
.portal-access__frame {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 420px);
  gap: 48px;
  align-items: start;
}

.portal-hero__copy {
  max-width: 760px;
}

.portal-hero__eyebrow,
.portal-section-head__eyebrow {
  margin: 0 0 16px;
  color: #486581;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.26em;
  text-transform: uppercase;
}

.portal-hero__firm {
  margin: 0 0 14px;
  color: #1b3b5f;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.portal-hero__slogan {
  margin: 0;
  color: #102a43;
  font-family: 'EB Garamond', 'STSong', 'Songti SC', serif;
  font-size: clamp(56px, 8vw, 112px);
  font-weight: 600;
  line-height: 0.94;
  letter-spacing: -0.06em;
}

.portal-hero__title {
  margin: 22px 0 0;
  color: #243b53;
  font-size: clamp(26px, 3vw, 38px);
  font-weight: 600;
  letter-spacing: -0.03em;
}

.portal-hero__lead,
.portal-section-head p,
.portal-feature p,
.portal-access__lead,
.portal-access__rule p,
.portal-access__notice,
.portal-footer__copy,
.portal-footer__icp {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.9;
}

.portal-hero__lead {
  margin-top: 28px;
  max-width: 700px;
  font-size: 17px;
}

.portal-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 36px;
}

.hero-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 52px;
  padding: 0 24px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-decoration: none;
  text-transform: uppercase;
  transition: transform 0.2s ease, background-color 0.2s ease, border-color 0.2s ease;
}

.hero-action:hover {
  transform: translateY(-1px);
}

.hero-action--primary {
  background: #1b3b5f;
  color: #fff;
}

.hero-action--secondary {
  border: 1px solid rgba(16, 42, 67, 0.12);
  background: rgba(255, 255, 255, 0.82);
  color: #102a43;
}

.portal-panel,
.portal-access__frame,
.portal-security-card,
.portal-feature {
  border: 1px solid rgba(16, 42, 67, 0.08);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 40px rgba(16, 42, 67, 0.06);
}

.portal-panel {
  padding: 28px;
}

.portal-panel__kicker,
.portal-feature__kicker {
  margin: 0 0 18px;
  color: #627d98;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.portal-panel__list {
  display: grid;
  gap: 20px;
}

.portal-panel__item {
  display: grid;
  gap: 8px;
  padding-top: 18px;
  border-top: 1px solid rgba(16, 42, 67, 0.08);
}

.portal-panel__item:first-child {
  padding-top: 0;
  border-top: none;
}

.portal-panel__item span,
.portal-security-card__row span {
  color: #829ab1;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.portal-panel__item strong,
.portal-security-card__row strong {
  color: #102a43;
  font-size: 15px;
  line-height: 1.7;
  font-weight: 600;
}

.portal-section-head {
  max-width: 760px;
  margin-bottom: 32px;
}

.portal-section-head h2,
.portal-access__copy h2 {
  margin: 0;
  color: var(--text-primary);
  font-family: 'EB Garamond', 'STSong', 'Songti SC', serif;
  font-size: clamp(34px, 4vw, 46px);
  font-weight: 600;
  letter-spacing: -0.03em;
}

.portal-section-head p {
  margin-top: 16px;
  font-size: 16px;
}

.portal-feature-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 18px;
}

.portal-feature {
  display: grid;
  gap: 16px;
  grid-column: span 4;
  padding: 26px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.portal-feature:hover {
  transform: translateY(-2px);
  border-color: rgba(27, 59, 95, 0.14);
  box-shadow: 0 22px 42px rgba(16, 42, 67, 0.08);
}

.portal-feature--lead {
  grid-column: span 6;
  grid-row: span 2;
  align-content: start;
  padding: 34px;
  background:
    linear-gradient(180deg, rgba(27, 59, 95, 0.08), rgba(255, 255, 255, 0.92)),
    rgba(255, 255, 255, 0.92);
}

.portal-feature__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  background: rgba(27, 59, 95, 0.08);
  color: #1b3b5f;
  font-size: 20px;
}

.portal-feature h3 {
  margin: 0;
  color: #102a43;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.5;
}

.portal-feature--lead h3 {
  font-size: 30px;
  line-height: 1.25;
  font-family: 'EB Garamond', 'STSong', 'Songti SC', serif;
}

.portal-feature p {
  font-size: 14px;
}

.portal-access__frame {
  padding: 34px;
}

.portal-access__lead {
  margin-top: 18px;
  font-size: 16px;
}

.portal-access__rules {
  display: grid;
  gap: 16px;
  margin-top: 28px;
}

.portal-access__rule {
  display: grid;
  gap: 8px;
  padding: 18px 0 0;
  border-top: 1px solid rgba(16, 42, 67, 0.08);
}

.portal-access__rule:first-child {
  padding-top: 0;
  border-top: none;
}

.portal-access__rule strong {
  color: #102a43;
  font-size: 16px;
  font-weight: 600;
}

.portal-access__notice {
  margin-top: 28px;
  padding: 20px 22px;
  border-left: 3px solid rgba(27, 59, 95, 0.22);
  background: rgba(27, 59, 95, 0.04);
}

.portal-security-card {
  padding: 26px;
}

.portal-security-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  color: #1b3b5f;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.portal-security-card__body {
  display: grid;
  gap: 18px;
}

.portal-security-card__row {
  display: grid;
  gap: 8px;
  padding-top: 18px;
  border-top: 1px solid rgba(16, 42, 67, 0.08);
}

.portal-security-card__row:first-child {
  padding-top: 0;
  border-top: none;
}

.portal-footer {
  display: grid;
  justify-items: center;
  gap: 6px;
  padding-top: 56px;
  padding-bottom: 56px;
  text-align: center;
}

.portal-footer__copy,
.portal-footer__icp {
  font-size: 12px;
}

@media (max-width: 1100px) {
  .portal-hero__layout,
  .portal-access__frame {
    grid-template-columns: 1fr;
  }

  .portal-feature--lead,
  .portal-feature {
    grid-column: span 6;
  }
}

@media (max-width: 768px) {
  .portal-header__inner {
    min-height: 64px;
    padding-top: 10px;
    padding-bottom: 10px;
  }

  .portal-brand__logo {
    width: 38px;
    height: 38px;
  }

  .portal-brand__name {
    font-size: 14px;
  }

  .portal-brand__meta,
  .portal-hero__eyebrow,
  .portal-section-head__eyebrow {
    letter-spacing: 0.18em;
  }

  .portal-hero,
  .portal-features,
  .portal-access,
  .portal-footer {
    padding-top: 56px;
    padding-bottom: 56px;
  }

  .portal-hero {
    padding-top: 60px;
  }

  .portal-hero__title {
    margin-top: 16px;
  }

  .portal-hero__lead {
    margin-top: 22px;
    font-size: 15px;
  }

  .portal-feature-grid {
    grid-template-columns: 1fr;
  }

  .portal-feature,
  .portal-feature--lead {
    grid-column: auto;
    grid-row: auto;
    padding: 24px;
  }

  .portal-feature h3,
  .portal-feature--lead h3 {
    font-size: 22px;
  }

  .portal-access__frame,
  .portal-panel,
  .portal-security-card {
    padding: 22px;
  }

  .hero-action {
    width: 100%;
  }
}
</style>
