<template>
  <div class="portal-page">
    <a-layout class="portal-shell fade-in">
      <a-layout-header class="portal-header">
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

        <a-button
          type="text"
          class="portal-login"
          :title="staffEntryTitle"
          :aria-label="staffEntryTitle"
          @click="goToAdminLogin"
        >
          <template #icon>
            <LoginOutlined />
          </template>
        </a-button>
      </a-layout-header>

      <a-layout-content>
        <section class="portal-section portal-section--hero">
          <a-row
            :gutter="[56, 32]"
            align="middle"
          >
            <a-col
              :xs="24"
              :lg="12"
            >
              <div class="portal-hero__copy">
                <a-typography-title
                  :level="1"
                  class="portal-hero__title"
                >
                  {{ lawFirmName }} 客户服务系统
                </a-typography-title>

                <a-typography-text class="portal-hero__eyebrow">
                  {{ heroEyebrow }}
                </a-typography-text>

                <a-space
                  direction="vertical"
                  :size="18"
                  class="portal-hero__feature-list"
                >
                  <article
                    v-for="item in heroHighlights"
                    :key="item.title"
                    class="hero-highlight"
                  >
                    <div class="hero-highlight__icon">
                      <component :is="item.icon" />
                    </div>
                    <div class="hero-highlight__body">
                      <h2>{{ item.title }}</h2>
                      <p>{{ item.description }}</p>
                    </div>
                  </article>
                </a-space>

                <a-typography-paragraph
                  v-if="accessNotice"
                  class="portal-hero__notice"
                >
                  {{ accessNotice }}
                </a-typography-paragraph>
              </div>
            </a-col>

            <a-col
              :xs="0"
              :lg="12"
            >
              <a-card
                :bordered="false"
                class="portal-hero__card"
                :body-style="{ padding: '0px' }"
              >
                <img
                  :src="heroImageUrl"
                  alt="律师事务所接待办公空间"
                  class="portal-hero__image"
                >
              </a-card>
            </a-col>
          </a-row>
        </section>

        <section
          id="portal-features"
          class="portal-section portal-section--tools"
        >
          <div class="portal-section-head">
            <a-typography-title
              :level="2"
              class="portal-section-head__title"
            >
              数字化法律工具集
            </a-typography-title>
            <span class="portal-section-head__line" />
          </div>

          <a-row
            :gutter="[1, 1]"
            class="portal-tools__grid"
          >
            <a-col
              v-for="feature in featureCards"
              :key="feature.title"
              :xs="24"
              :sm="12"
              :lg="6"
            >
              <a-card
                :bordered="false"
                class="portal-tool-card"
              >
                <div class="portal-tool-card__icon">
                  <component :is="feature.icon" />
                </div>
                <a-typography-title
                  :level="4"
                  class="portal-tool-card__title"
                >
                  {{ feature.title }}
                </a-typography-title>
                <a-typography-paragraph class="portal-tool-card__description">
                  {{ feature.description }}
                </a-typography-paragraph>
              </a-card>
            </a-col>
          </a-row>
        </section>

        <section class="portal-section portal-section--security">
          <div class="portal-security">
            <div class="portal-security__badge">
              <SafetyCertificateOutlined />
              <span>Security Protocol Active</span>
            </div>

            <a-typography-title
              :level="2"
              class="portal-security__title"
            >
              金融级安全与数据隐私保护
            </a-typography-title>

            <a-row
              :gutter="[26, 26]"
              class="portal-security__grid"
            >
              <a-col
                v-for="item in securityCards"
                :key="item.title"
                :xs="24"
                :md="8"
              >
                <div class="portal-security__item">
                  <a-typography-title
                    :level="4"
                    class="portal-security__item-title"
                  >
                    {{ item.title }}
                  </a-typography-title>
                  <a-typography-paragraph class="portal-security__item-description">
                    {{ item.description }}
                  </a-typography-paragraph>
                </div>
              </a-col>
            </a-row>
          </div>
        </section>
      </a-layout-content>

      <a-layout-footer class="portal-footer">
        <div class="portal-footer__meta">
          <span>{{ copyrightText }}</span>
          <span v-if="icpText">{{ icpText }}</span>
        </div>

        <a-space
          :size="16"
          class="portal-footer__links"
        >
          <a
            href="#portal-features"
            aria-label="查看服务条款"
          >服务条款</a>
          <a
            href="#portal-features"
            aria-label="查看隐私政策"
          >隐私政策</a>
        </a-space>
      </a-layout-footer>
    </a-layout>
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
  overflow: hidden;
  background: #f8fafc;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.24);
}

.portal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: auto;
  padding: 22px 28px;
  line-height: normal;
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
  color: #5b6f91;
}

.portal-section {
  padding: 0 28px;
}

.portal-section--hero {
  padding-top: 48px;
  padding-bottom: 64px;
}

.portal-hero__copy {
  max-width: 680px;
}

.portal-hero__title:deep(.ant-typography),
.portal-hero__title {
  margin-bottom: 0;
  color: var(--lex-primary);
  font-size: clamp(28px, 4vw, 58px);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.03em;
}

.portal-hero__eyebrow {
  display: block;
  margin-top: 14px;
  color: #5d7090;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.portal-hero__feature-list {
  width: 100%;
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
.portal-tool-card__title,
.portal-security__item-title {
  margin: 0;
  color: #0f2749;
  font-weight: 700;
  line-height: 1.35;
}

.hero-highlight h2 {
  font-size: 26px;
}

.hero-highlight p,
.portal-tool-card__description,
.portal-security__item-description,
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
  margin-bottom: 0;
  max-width: 650px;
  font-size: 14px;
}

.portal-hero__card {
  overflow: hidden;
  background: #d9dee6;
}

.portal-hero__image {
  display: block;
  width: 100%;
  aspect-ratio: 1.18 / 0.9;
  object-fit: cover;
}

.portal-section--tools {
  padding-top: 54px;
  padding-bottom: 56px;
  background: #f2f4f7;
}

.portal-section-head {
  margin-bottom: 28px;
}

.portal-section-head__title,
.portal-security__title {
  margin-bottom: 0;
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
  background: #d8e0ea;
}

.portal-tool-card {
  min-height: 220px;
  height: 100%;
  border-radius: 0;
  background: #fff;
}

.portal-tool-card :deep(.ant-card-body) {
  height: 100%;
  padding: 24px 22px;
}

.portal-tool-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: var(--lex-primary);
  font-size: 18px;
}

.portal-tool-card__title {
  font-size: 18px;
}

.portal-tool-card__description {
  margin-top: 14px;
  margin-bottom: 0;
  font-size: 14px;
}

.portal-section--security {
  padding-top: 48px;
  padding-bottom: 54px;
  background: #123a7a;
}

.portal-security {
  display: grid;
  justify-items: center;
  gap: 22px;
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

.portal-security__title {
  color: #fff;
  font-size: 32px;
}

.portal-security__grid {
  width: min(100%, 980px);
}

.portal-security__item-title,
.portal-security__item-description {
  color: #fff;
}

.portal-security__item-title {
  font-size: 18px;
}

.portal-security__item-description {
  margin-top: 10px;
  margin-bottom: 0;
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

.portal-footer__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.portal-footer__links {
  display: inline-flex;
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

@media (max-width: 991px) {
  .portal-tool-card__title {
    font-size: 16px;
  }

  .portal-tool-card__description {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .portal-page {
    padding: 0;
  }

  .portal-section,
  .portal-header,
  .portal-footer {
    padding-left: 18px;
    padding-right: 18px;
  }

  .portal-section--hero {
    padding-top: 24px;
    padding-bottom: 40px;
  }

  .hero-highlight h2 {
    font-size: 18px;
  }

  .portal-section-head__title,
  .portal-security__title {
    font-size: 24px;
  }

  .portal-tool-card {
    min-height: 180px;
  }

  .portal-tool-card :deep(.ant-card-body) {
    padding: 20px 18px;
  }

  .portal-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
