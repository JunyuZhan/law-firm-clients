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
              <strong class="portal-brand__name">{{ lawFirmName }}</strong>
            </div>
          </div>

          <div class="portal-header__meta">
            <span class="portal-header__note">
              仅限通过律师发送的专属链接进入事项
            </span>

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
        </div>
      </header>

      <main
        id="main-content"
        class="portal-main"
      >
        <section class="section-shell portal-hero">
          <div class="portal-hero__copy">
            <span class="eyebrow portal-hero__eyebrow">{{ heroEyebrow }}</span>
            <h1 class="portal-hero__title">
              <span>{{ lawFirmName }}</span>
              <span>客户案件协作入口</span>
            </h1>

            <p class="portal-hero__subtitle">
              客户可在此查看案件进展、接收提醒、查阅文件并补充材料。该空间为受控访问环境，展示范围以律师授权为准。
            </p>

            <div class="portal-hero__highlights">
              <span
                v-for="item in collaborationHighlights"
                :key="item"
                class="portal-highlight"
              >
                {{ item }}
              </span>
            </div>

            <div
              v-if="accessNotice"
              class="portal-hero__notice"
            >
              <SafetyCertificateOutlined class="portal-hero__notice-icon" />
              <p>{{ accessNotice }}</p>
            </div>

            <div class="portal-hero__actions">
              <a
                class="portal-primary-link"
                href="#access-guide"
              >
                查看访问方式
                <ArrowRightOutlined />
              </a>

              <a
                class="portal-secondary-link"
                href="#collaboration"
              >
                了解协作能力
              </a>

              <a
                v-if="lawFirmWebsite"
                :href="lawFirmWebsite"
                target="_blank"
                rel="noopener noreferrer"
                class="portal-tertiary-link"
              >
                访问律所官网
              </a>
            </div>
          </div>

          <aside
            id="access-guide"
            class="portal-access"
          >
            <div class="portal-access__top">
              <span class="portal-access__eyebrow">受控访问方式</span>
              <h2>通过专属链接进入对应事项</h2>
              <p>
                无需公开注册账号。客户收到承办律师发送的专属链接后，可直接进入被授权的事项空间，查看当前可见的资料与动态。
              </p>
            </div>

            <ol class="portal-access__steps">
              <li
                v-for="(item, index) in accessSteps"
                :key="item.title"
                class="portal-access__step"
              >
                <span class="portal-access__index">{{ String(index + 1).padStart(2, '0') }}</span>
                <div class="portal-access__body">
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.description }}</p>
                </div>
              </li>
            </ol>

            <div class="portal-access__rules">
              <article
                v-for="item in accessRules"
                :key="item.title"
                class="portal-access__rule"
              >
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </article>
            </div>
          </aside>
        </section>

        <section
          id="collaboration"
          class="section-shell portal-collaboration"
        >
          <div class="portal-section-head">
            <span class="eyebrow">协作能力</span>
            <h2>围绕实际案件协作设计，而不是展示型首页</h2>
            <p>
              入口仅承接客户在案件推进过程中的高频动作，减少无关信息，保持访问结构稳定、清晰、可追踪。
            </p>
          </div>

          <div class="portal-capability-list">
            <article
              v-for="(item, index) in capabilitySections"
              :key="item.title"
              class="portal-capability"
              :class="{ 'portal-capability--reverse': index % 2 === 1 }"
            >
              <div class="portal-capability__marker">
                <div class="portal-capability__icon">
                  <component :is="item.icon" />
                </div>
                <span class="portal-capability__line" />
              </div>

              <div class="portal-capability__content">
                <span class="portal-capability__label">{{ item.label }}</span>
                <h3>{{ item.title }}</h3>
                <p class="portal-capability__summary">
                  {{ item.description }}
                </p>

                <div class="portal-capability__details">
                  <p
                    v-for="detail in item.details"
                    :key="detail"
                  >
                    {{ detail }}
                  </p>
                </div>
              </div>
            </article>
          </div>
        </section>

        <section
          id="portal-boundary"
          class="section-shell portal-boundary"
        >
          <div class="portal-boundary__intro">
            <span class="portal-boundary__badge">
              <SafetyCertificateOutlined />
              访问边界与隐私说明
            </span>
            <h2>该系统是客户受控访问空间，不是公开门户站点</h2>
            <p>
              页面只承接客户与事项协作相关的必要信息。访问边界、资料流转和可见范围由律师授权控制，减少信息外溢与无效跳转。
            </p>
          </div>

          <div class="portal-boundary__grid">
            <article
              v-for="item in boundaryCards"
              :key="item.title"
              class="portal-boundary__card"
            >
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </article>
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
            <a href="#access-guide">访问方式</a>
            <a href="#collaboration">协作能力</a>
            <a href="#portal-boundary">访问边界</a>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Component } from 'vue'
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

interface PortalStep {
  title: string
  description: string
}

interface PortalRule {
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

const collaborationHighlights = [
  '案件进展',
  '文件协作',
  '通知提醒',
  '补充材料',
]

const accessSteps: PortalStep[] = [
  {
    title: '接收专属事项链接',
    description: '承办律师按事项范围发送专属访问链接，仅对应当前客户可进入的案件空间。',
  },
  {
    title: '直接进入被授权内容',
    description: '客户通过链接进入后，可查看当前被授权的案件进展、文件与通知，无需公开注册。',
  },
  {
    title: '按要求继续协作',
    description: '如需补充材料或确认信息，可在事项空间内继续提交，避免分散在多个沟通渠道。',
  },
]

const accessRules: PortalRule[] = [
  {
    title: '无需公开注册',
    description: '客户不需要单独创建公开账号，进入方式以专属链接和授权范围为准。',
  },
  {
    title: '范围按授权控制',
    description: '不同事项、文件与提醒内容的可见范围由承办律师控制，不做公开暴露。',
  },
]

const capabilitySections: CapabilitySection[] = [
  {
    label: '01 / 案件进度',
    title: '以事项推进为主线查看当前状态',
    description: '客户可以查看当前阶段、关键节点和最近更新，减少反复询问案件是否有新进展。',
    details: [
      '围绕事项推进展示必要状态，而不是泛化的统计概览。',
      '关键信息按照案件上下文组织，阅读路径更接近真实协作。',
    ],
    icon: FileSearchOutlined,
  },
  {
    label: '02 / 文件协作',
    title: '在同一入口查阅律师已授权的文件资料',
    description: '文件查看、下载与后续补充放在同一协作空间内，避免资料散落在邮件、微信和临时网盘中。',
    details: [
      '仅展示当前事项下已授权的文件内容。',
      '文件流转与案件进度保持上下文关联，降低理解成本。',
    ],
    icon: FolderOpenOutlined,
  },
  {
    label: '03 / 通知提醒',
    title: '用明确提醒承接状态变化与时间节点',
    description: '当事项有进展、需要补件或接近关键节点时，客户可在统一入口接收提醒，不再依赖零散通知。',
    details: [
      '提醒内容围绕案件动作组织，而不是泛消息流。',
      '重点强调是否需要客户响应，减少信息噪音。',
    ],
    icon: BellOutlined,
  },
  {
    label: '04 / 补充材料',
    title: '按事项要求继续上传补充文件',
    description: '当律师需要进一步材料时，客户可以在当前事项上下文中直接补充，减少口头往返与版本混乱。',
    details: [
      '上传动作保留在案件语境中，方便双方持续对齐。',
      '后续查看、下载与补充行为保持在同一受控空间里。',
    ],
    icon: CloudUploadOutlined,
  },
]

const boundaryCards = [
  {
    title: '访问权限由律师授权控制',
    description: '客户可见的事项、文件与通知范围以律师设置为准，不提供公开浏览入口。',
  },
  {
    title: '文件操作可追溯',
    description: '查看、下载、上传等协作动作保留访问轨迹，用于支持有序、可回溯的资料流转。',
  },
  {
    title: '仅承接必要协作信息',
    description: '页面不承担营销展示任务，优先确保客户能快速找到当前事项所需的进展、提醒和材料。',
  },
]

const lawFirmName = computed(() => appConfigStore.lawFirmName?.trim() || appConfigStore.displayName?.trim() || '律师事务所')
const portalSystemName = computed(() => appConfigStore.appShortName?.trim() || '客户服务系统')
const heroEyebrow = computed(() => appConfigStore.portalEyebrowEn?.trim() || appConfigStore.appShortNameEn?.trim() || 'Client Service Portal')
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
  min-height: 100vh;
  background-color: #f0f2f5;
  color: #1f2937;
}

.portal-shell {
  min-height: 100vh;
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 20;
  border-bottom: 1px solid #e5e7eb;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
}

.portal-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 64px;
  padding: 0 24px;
}

.portal-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.portal-brand__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #1677ff;
  flex-shrink: 0;
  font-size: 20px;
}

.portal-brand__logo--fallback {
  background: #f9fafb;
}

.portal-brand__image {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.portal-brand__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.portal-brand__system {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
}

.portal-brand__name {
  color: #111827;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.3;
}

.portal-header__meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.portal-header__note {
  color: #6b7280;
  font-size: 13px;
}

.portal-header__action {
  min-height: 36px;
  padding: 0 16px;
  border-radius: 6px;
  border: 1px solid #d9d9d9;
  background: #ffffff;
  color: #1f2937;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.portal-header__action:hover {
  border-color: #1677ff;
  color: #1677ff;
}

.portal-main {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding: 40px 24px 64px;
  max-width: 1200px;
  margin: 0 auto;
}

.portal-hero {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32px;
}

@media (min-width: 1024px) {
  .portal-hero {
    grid-template-columns: 1.2fr 0.8fr;
  }
}

.portal-hero__copy {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.portal-hero__eyebrow {
  color: #1677ff;
  font-weight: 600;
  font-size: 14px;
}

.portal-hero__title {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  color: #111827;
  font-size: 40px;
  line-height: 1.2;
  font-weight: 700;
}

@media (min-width: 768px) {
  .portal-hero__title {
    font-size: 48px;
  }
}

.portal-hero__subtitle {
  margin: 0;
  color: #4b5563;
  font-size: 16px;
  line-height: 1.6;
  max-width: 600px;
}

.portal-hero__highlights {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.portal-highlight {
  padding: 6px 12px;
  border-radius: 6px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
}

.portal-hero__notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
}

.portal-hero__notice-icon {
  margin-top: 2px;
  color: #faad14;
  font-size: 16px;
}

.portal-hero__notice p {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.portal-hero__actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 8px;
}

.portal-primary-link,
.portal-secondary-link,
.portal-tertiary-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 40px;
  padding: 0 20px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s;
}

.portal-primary-link {
  background: #1677ff;
  color: #ffffff;
  border: 1px solid #1677ff;
}

.portal-primary-link:hover {
  background: #4096ff;
  border-color: #4096ff;
  color: #ffffff;
}

.portal-secondary-link {
  background: #ffffff;
  border: 1px solid #d9d9d9;
  color: #1f2937;
}

.portal-secondary-link:hover,
.portal-tertiary-link:hover {
  color: #1677ff;
  border-color: #1677ff;
}

.portal-tertiary-link {
  color: #6b7280;
}

.portal-access {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 32px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.portal-access__top {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.portal-access__eyebrow {
  color: #1677ff;
  font-size: 13px;
  font-weight: 600;
}

.portal-access__top h2 {
  margin: 0;
  color: #111827;
  font-size: 24px;
  font-weight: 600;
}

.portal-access__top p {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.portal-access__steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.portal-access__step {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.portal-access__index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e6f4ff;
  color: #1677ff;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.portal-access__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.portal-access__body strong {
  color: #1f2937;
  font-size: 15px;
  font-weight: 500;
}

.portal-access__body p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

.portal-access__rules {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.portal-access__rule {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.portal-access__rule strong {
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
}

.portal-access__rule p {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

.portal-section-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 32px;
  text-align: center;
  align-items: center;
}

.portal-section-head .eyebrow {
  color: #1677ff;
  font-size: 14px;
  font-weight: 600;
}

.portal-section-head h2 {
  margin: 0;
  color: #111827;
  font-size: 28px;
  font-weight: 600;
}

.portal-section-head p {
  margin: 0;
  color: #4b5563;
  font-size: 16px;
  max-width: 600px;
}

.portal-collaboration {
  padding-top: 32px;
}

.portal-capability-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

@media (min-width: 768px) {
  .portal-capability-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

.portal-capability {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

.portal-capability__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: #e6f4ff;
  color: #1677ff;
  font-size: 24px;
}

.portal-capability__content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.portal-capability__label {
  color: #6b7280;
  font-size: 12px;
  font-weight: 600;
}

.portal-capability__content h3 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 600;
}

.portal-capability__summary {
  margin: 0;
  color: #4b5563;
  font-size: 15px;
  line-height: 1.6;
}

.portal-capability__details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.portal-capability__details p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  padding-left: 16px;
  position: relative;
}

.portal-capability__details p::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #9ca3af;
}

.portal-boundary {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding: 40px;
  border-radius: 16px;
  background: #111827;
  color: #ffffff;
}

.portal-boundary__intro {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 600px;
}

.portal-boundary__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #93c5fd;
  font-size: 12px;
  font-weight: 600;
}

.portal-boundary__intro h2 {
  margin: 0;
  color: #ffffff;
  font-size: 28px;
  font-weight: 600;
}

.portal-boundary__intro p {
  margin: 0;
  color: #9ca3af;
  font-size: 16px;
  line-height: 1.6;
}

.portal-boundary__grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

@media (min-width: 768px) {
  .portal-boundary__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.portal-boundary__card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 24px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.portal-boundary__card h3 {
  margin: 0;
  color: #ffffff;
  font-size: 18px;
  font-weight: 500;
}

.portal-boundary__card p {
  margin: 0;
  color: #9ca3af;
  font-size: 14px;
  line-height: 1.6;
}

.portal-footer {
  border-top: 1px solid #e5e7eb;
  background: #ffffff;
}

.portal-footer__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.portal-footer__meta,
.portal-footer__links {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.portal-footer__meta span,
.portal-footer__links a {
  color: #6b7280;
  font-size: 14px;
  text-decoration: none;
}

.portal-footer__links a:hover {
  color: #111827;
}

@media (max-width: 768px) {
  .portal-header__inner {
    padding: 0 16px;
  }
  
  .portal-main {
    padding: 24px 16px 40px;
  }
  
  .portal-boundary {
    padding: 24px;
  }
  
  .portal-footer__inner {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
