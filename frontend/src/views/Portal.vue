<template>
  <div class="portal-page">
    <div class="portal-shell fade-in">
      <header class="portal-header">
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
          <span class="portal-brand__name">{{ lawFirmName }}</span>
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
      </header>

      <main>
        <section class="portal-hero">
          <div class="portal-hero__copy">
            <h1 class="portal-hero__title">{{ lawFirmName }} 客户服务系统</h1>
            <p class="portal-hero__eyebrow">{{ heroEyebrow }}</p>

            <div class="portal-hero__feature-list">
              <article
                v-for="item in heroHighlights"
                :key="item.title"
                class="hero-highlight"
              >
                <div class="hero-highlight__icon">
                  <component :is="item.icon" />
                </div>
                <div>
                  <h2>{{ item.title }}</h2>
                  <p>{{ item.description }}</p>
                </div>
              </article>
            </div>

            <p
              v-if="accessNotice"
              class="portal-hero__notice"
            >
              {{ accessNotice }}
            </p>
          </div>

          <div class="portal-hero__visual">
            <div class="portal-hero__image-frame">
              <img
                :src="heroImageUrl"
                alt="律师事务所接待办公空间"
                class="portal-hero__image"
              >
            </div>
          </div>
        </section>

        <section
          id="portal-features"
          class="portal-tools"
        >
          <div class="portal-section-head">
            <h2>数字化法律工具集</h2>
            <span class="portal-section-head__line" />
          </div>

          <div class="portal-tools__grid">
            <article
              v-for="feature in featureCards"
              :key="feature.title"
              class="portal-tool-card"
            >
              <div class="portal-tool-card__icon">
                <component :is="feature.icon" />
              </div>
              <h3>{{ feature.title }}</h3>
              <p>{{ feature.description }}</p>
            </article>
          </div>
        </section>

        <section class="portal-security">
          <div class="portal-security__badge">
            <SafetyCertificateOutlined />
            <span>Security Protocol Active</span>
          </div>

          <h2>金融级安全与数据隐私保护</h2>

          <div class="portal-security__grid">
            <article
              v-for="item in securityCards"
              :key="item.title"
              class="portal-security__item"
            >
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </section>
      </main>

      <footer class="portal-footer">
        <div class="portal-footer__meta">
          <span>{{ copyrightText }}</span>
          <span v-if="icpText">{{ icpText }}</span>
        </div>

        <div class="portal-footer__links">
          <a href="#portal-features">服务条款</a>
          <a href="#portal-features">隐私政策</a>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  BankOutlined,
  BellOutlined,
  CheckCircleOutlined,
  CloudUploadOutlined,
  FileSearchOutlined,
  FolderOpenOutlined,
  LoginOutlined,
  SafetyCertificateOutlined,
  TeamOutlined,
  TabletOutlined,
  UserSwitchOutlined,
} from '@ant-design/icons-vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { COPYRIGHT_TEXT } from '@/config/app'

interface PortalCard {
  title: string
  description: string
  icon: object
}

const router = useRouter()
const appConfigStore = useAppConfigStore()

const heroHighlights: PortalCard[] = [
  {
    title: '查看案件进展',
    description: '实时追踪法律事务处理状态与关键节点。',
    icon: CheckCircleOutlined,
  },
  {
    title: '查阅授权文件',
    description: '集中管理各类法律授权书与官方存证文件。',
    icon: FolderOpenOutlined,
  },
  {
    title: '接收通知提醒',
    description: '针对关键庭审时间与文书期限的主动式触达。',
    icon: BellOutlined,
  },
  {
    title: '上传补充材料',
    description: '安全高效地传输案件所需证据与补充性文书。',
    icon: CloudUploadOutlined,
  },
  {
    title: '与承办律师协作',
    description: '直接、专业的数字化沟通界面，确保法律意见对齐。',
    icon: TeamOutlined,
  },
]

const featureCards: PortalCard[] = [
  {
    title: '案件 / 事项进展查看',
    description: '提供全流程透明化的案件进度管理，确保客户对每一项法律流程具有知情权。',
    icon: FileSearchOutlined,
  },
  {
    title: '授权文件查阅',
    description: '云端数字化存证，支持随时查阅与下载经授权的各类法律契约与正式文件。',
    icon: FolderOpenOutlined,
  },
  {
    title: '通知与提醒接收',
    description: '关键时效自动预警系统，避免法律期限错失带来的潜在风险。',
    icon: BellOutlined,
  },
  {
    title: '客户协作联络',
    description: '构建律师与客户的高效即时沟通链路，实现跨地域、跨时区的法律协作。',
    icon: TeamOutlined,
  },
  {
    title: '材料提交 / 文件上传',
    description: '端到端加密的传输通道，支持各类电子证据与扫描件的快速云端流转。',
    icon: CloudUploadOutlined,
  },
  {
    title: '专属门户访问',
    description: '为客户建立专属的受控访问空间，体现法律服务的专业性与边界感。',
    icon: CheckCircleOutlined,
  },
  {
    title: '受控访问与权限边界',
    description: '严密的 RBAC 权限管理体系，严格限定信息的知悉范围，确保职业秘密不泄露。',
    icon: UserSwitchOutlined,
  },
  {
    title: '多端访问体验',
    description: '自适应多端接入能力，确保在移动端与 PC 端均能获得一致的严谨交互体验。',
    icon: TabletOutlined,
  },
]

const securityCards = [
  {
    title: '授权访问',
    description: '仅限经律所严格认证的当事人或授权代理人登录访问。',
  },
  {
    title: '数据加密',
    description: '所有传输及流转数据均通过受控机制处理，满足法律资料协作的审慎要求。',
  },
  {
    title: '权限边界',
    description: '严格执行最小知悉原则，案件关联方仅可见其获准的信息。',
  },
]

const heroImageUrl = 'https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=1200&q=80'

const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || '律师事务所')
const heroEyebrow = computed(() => appConfigStore.portalEyebrowEn?.trim() || 'DIGITAL COUNSEL: INTEGRATED CLIENT SERVICE PORTAL')
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
  padding: 6px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.08), transparent 24%),
    radial-gradient(circle at bottom right, rgba(255, 255, 255, 0.06), transparent 20%),
    #181b22;
}

.portal-shell {
  width: min(100%, 1450px);
  margin: 0 auto;
  border: 1px solid rgba(15, 23, 42, 0.18);
  border-radius: 18px;
  overflow: hidden;
  background: #f8fafc;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.24);
}

.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 28px;
  background: #f8fafc;
}

.portal-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.portal-brand__logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  color: var(--lex-primary);
}

.portal-brand__image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.portal-brand__name {
  color: var(--lex-primary);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.portal-login {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  color: #5b6f91;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}

.portal-login:hover {
  color: var(--lex-primary);
  transform: scale(1.04);
}

.portal-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(400px, 0.98fr);
  gap: 56px;
  align-items: center;
  padding: 48px 28px 64px;
}

.portal-hero__title {
  margin: 0;
  color: var(--lex-primary);
  font-size: clamp(36px, 3.6vw, 58px);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.03em;
}

.portal-hero__eyebrow {
  margin: 14px 0 0;
  color: #5d7090;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.portal-hero__feature-list {
  display: grid;
  gap: 18px;
  margin-top: 34px;
}

.hero-highlight {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.hero-highlight__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--lex-primary);
  font-size: 16px;
  transform: translateY(2px);
}

.hero-highlight h2,
.portal-tool-card h3,
.portal-security__item h3 {
  margin: 0;
  color: #0f2749;
  font-size: 23px;
  font-weight: 700;
  line-height: 1.35;
}

.hero-highlight h2 {
  font-size: 26px;
}

.hero-highlight p,
.portal-tool-card p,
.portal-security__item p,
.portal-hero__notice,
.portal-footer span,
.portal-footer a {
  margin: 4px 0 0;
  color: #58697f;
  font-size: 16px;
  line-height: 1.7;
}

.portal-hero__notice {
  margin-top: 28px;
  max-width: 650px;
  font-size: 14px;
}

.portal-hero__visual {
  display: flex;
  justify-content: flex-end;
}

.portal-hero__image-frame {
  width: 100%;
  aspect-ratio: 1.18 / 0.9;
  overflow: hidden;
  background: #d9dee6;
}

.portal-hero__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.portal-tools {
  padding: 54px 28px 56px;
  background: #f2f4f7;
}

.portal-section-head {
  margin-bottom: 28px;
}

.portal-section-head h2 {
  margin: 0;
  color: var(--lex-primary);
  font-size: 38px;
  font-weight: 800;
  line-height: 1.2;
}

.portal-section-head__line {
  display: block;
  width: 36px;
  height: 3px;
  margin-top: 12px;
  background: var(--lex-primary);
}

.portal-tools__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border-top: 1px solid #d8e0ea;
  border-left: 1px solid #d8e0ea;
  background: #fff;
}

.portal-tool-card {
  min-height: 220px;
  padding: 24px 22px;
  border-right: 1px solid #d8e0ea;
  border-bottom: 1px solid #d8e0ea;
  background: #fff;
}

.portal-tool-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--lex-primary);
  font-size: 18px;
}

.portal-tool-card h3 {
  font-size: 18px;
}

.portal-tool-card p {
  margin-top: 14px;
  font-size: 14px;
}

.portal-security {
  display: grid;
  justify-items: center;
  gap: 22px;
  padding: 48px 28px 54px;
  background: #123a7a;
  text-align: center;
}

.portal-security__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #8eb2ef;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.portal-security h2 {
  margin: 0;
  color: #ffffff;
  font-size: 32px;
  font-weight: 800;
}

.portal-security__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 26px;
  width: min(100%, 980px);
}

.portal-security__item h3,
.portal-security__item p {
  color: #ffffff;
}

.portal-security__item h3 {
  font-size: 18px;
}

.portal-security__item p {
  margin-top: 10px;
  color: rgba(233, 241, 255, 0.74);
  font-size: 14px;
}

.portal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px 28px 30px;
  background: #f8fafc;
}

.portal-footer__meta,
.portal-footer__links {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.portal-footer span,
.portal-footer a {
  margin: 0;
  font-size: 12px;
  text-decoration: none;
}

.portal-footer a:hover {
  color: var(--lex-primary);
}

@media (max-width: 1180px) {
  .portal-hero {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .portal-hero__visual {
    justify-content: flex-start;
  }

  .portal-tools__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .portal-page {
    padding: 0;
  }

  .portal-shell {
    border-radius: 0;
    border: none;
  }

  .portal-header,
  .portal-hero,
  .portal-tools,
  .portal-security,
  .portal-footer {
    padding-left: 18px;
    padding-right: 18px;
  }

  .portal-hero {
    padding-top: 24px;
    padding-bottom: 40px;
  }

  .portal-tools__grid,
  .portal-security__grid {
    grid-template-columns: 1fr;
  }

  .portal-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-highlight h2 {
    font-size: 19px;
  }

  .portal-section-head h2,
  .portal-security h2 {
    font-size: 28px;
  }
}
</style>
