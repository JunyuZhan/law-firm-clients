<template>
  <div class="matter-detail-container">
    <a-spin :spinning="loading">
      <div
        v-if="matterDetail"
        class="detail-grid"
      >
        <section class="detail-hero">
          <div class="hero-top">
            <a-button
              class="back-btn"
              @click="goBack"
            >
              <template #icon>
                <ArrowLeftOutlined />
              </template>
              <span class="back-btn-text">{{ UI_TEXTS.backToList }}</span>
            </a-button>
            <a-tag :color="getStatusColor(matterDetail.status)">
              {{ getStatusName(matterDetail.status) }}
            </a-tag>
          </div>

          <div class="hero-copy">
            <h2 class="editorial-title hero-title">
              {{ matterDetail.clientName || ADMIN_MATTER_DETAIL_TEXTS.fallbackTitle }}
            </h2>
            <p class="hero-text">
              {{ ADMIN_MATTER_DETAIL_TEXTS.heroText }}
            </p>
          </div>

          <div class="hero-metrics">
            <div class="metric-card">
              <span class="metric-label">{{ ADMIN_MATTER_DETAIL_TEXTS.fields.matterId }}</span>
              <strong>{{ matterDetail.id }}</strong>
            </div>
            <div class="metric-card">
              <span class="metric-label">{{ ADMIN_MATTER_DETAIL_TEXTS.fields.lawFirmMatterId }}</span>
              <strong>{{ matterDetail.lawFirmMatterId }}</strong>
            </div>
            <div class="metric-card">
              <span class="metric-label">{{ ADMIN_MATTER_DETAIL_TEXTS.fields.expiresAt }}</span>
              <strong :class="{ expired: isExpired(matterDetail.expiresAt) }">{{ formatDate(matterDetail.expiresAt) }}</strong>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h3>{{ ADMIN_MATTER_DETAIL_TEXTS.sections.basicInfo }}</h3>
          <a-descriptions
            :column="{ xs: 1, sm: 2 }"
            bordered
          >
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.matterId">
              {{ matterDetail.id }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.lawFirmMatterId">
              {{ matterDetail.lawFirmMatterId }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.clientId">
              {{ matterDetail.clientId }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.clientName">
              {{ matterDetail.clientName }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.status">
              <a-tag :color="getStatusColor(matterDetail.status)">
                {{ getStatusName(matterDetail.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.validDays">
              {{ matterDetail.validDays }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.expiresAt">
              <span :class="{ expired: isExpired(matterDetail.expiresAt) }">
                {{ formatDate(matterDetail.expiresAt) }}
              </span>
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.scopes">
              {{ matterDetail.scopes }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.createdAt">
              {{ formatDate(matterDetail.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.updatedAt">
              {{ formatDate(matterDetail.updatedAt) }}
            </a-descriptions-item>
          </a-descriptions>
        </section>

        <section class="detail-section">
          <h3>{{ ADMIN_MATTER_DETAIL_TEXTS.sections.accessInfo }}</h3>
          <a-descriptions
            :column="1"
            bordered
          >
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.accessUrl">
              <div class="access-url-wrapper">
                <a
                  :href="matterDetail.accessUrl"
                  target="_blank"
                  class="access-url"
                >
                  {{ matterDetail.accessUrl }}
                </a>
                <a-button
                  type="link"
                  size="small"
                  class="copy-btn"
                  @click="copyToClipboard(matterDetail.accessUrl)"
                >
                  {{ ADMIN_MATTER_DETAIL_TEXTS.actions.copy }}
                </a-button>
              </div>
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_MATTER_DETAIL_TEXTS.fields.accessToken">
              <div class="token-display">
                <a-typography-text :copyable="{ text: matterDetail.accessToken }">
                  {{ showToken ? matterDetail.accessToken : maskToken(matterDetail.accessToken) }}
                </a-typography-text>
                <a-button
                  type="link"
                  size="small"
                  @click="showToken = !showToken"
                >
                  {{ showToken ? ADMIN_MATTER_DETAIL_TEXTS.actions.hide : ADMIN_MATTER_DETAIL_TEXTS.actions.show }}
                </a-button>
              </div>
            </a-descriptions-item>
          </a-descriptions>
        </section>

        <section class="detail-section">
          <h3>{{ ADMIN_MATTER_DETAIL_TEXTS.sections.matterData }}</h3>
          <pre class="matter-data-pre">{{ formatMatterData(matterDetail.matterData) }}</pre>
        </section>

        <section class="detail-actions">
          <a-space
            class="action-buttons"
            :size="16"
          >
            <a-button
              v-if="matterDetail.status === 'ACTIVE'"
              type="primary"
              danger
              @click="handleRevoke"
            >
              {{ ADMIN_MATTER_DETAIL_TEXTS.actions.revoke }}
            </a-button>
            <a-button @click="goBack">
              {{ UI_TEXTS.backToList }}
            </a-button>
          </a-space>
        </section>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { getMatterDetail, revokeMatter, type MatterDetailInfo } from '@/api/matter'
import { formatDate, isExpired } from '@/utils/date'
import { getMatterStatusColor, getMatterStatusText } from '@/utils/status'
import { UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_MATTER_DETAIL_TEXTS } from '@/constants/adminTexts'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const matterDetail = ref<MatterDetailInfo | null>(null)
const showToken = ref(false)

function maskToken(token: string): string {
  if (!token || token.length <= 10) return '••••••••••'
  return token.slice(0, 6) + '••••••••' + token.slice(-4)
}

async function loadData() {
  const id = route.params.id as string
  if (!id || !/^[\w-]+$/.test(id)) {
    message.error(ADMIN_MATTER_DETAIL_TEXTS.feedback.invalidId)
    goBack()
    return
  }

  loading.value = true
  try {
    const res = await getMatterDetail(id)
    matterDetail.value = res.data
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_MATTER_DETAIL_TEXTS.feedback.loadFailed
    message.error(errorMessage)
    goBack()
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/admin/matters')
}

async function handleRevoke() {
  if (!matterDetail.value) return

  try {
    await revokeMatter(matterDetail.value.id)
    message.success(UI_FEEDBACK_TEXTS.matterRevoked)
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_MATTER_DETAIL_TEXTS.feedback.revokeFailed
    message.error(errorMessage)
  }
}

function copyToClipboard(text: string) {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      message.success(UI_FEEDBACK_TEXTS.copySuccess)
    }).catch(() => {
      fallbackCopy(text)
    })
  } else {
    fallbackCopy(text)
  }
}

function fallbackCopy(text: string) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    message.success(UI_FEEDBACK_TEXTS.copySuccess)
  } catch {
    message.error(UI_FEEDBACK_TEXTS.copyFailed)
  }
  document.body.removeChild(textarea)
}

const getStatusName = getMatterStatusText
const getStatusColor = getMatterStatusColor

function formatMatterData(data?: Record<string, unknown>): string {
  if (!data) return ADMIN_MATTER_DETAIL_TEXTS.feedback.emptyData
  return JSON.stringify(data, null, 2)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.matter-detail-container {
  padding: 0;
}

.detail-grid {
  display: grid;
  gap: 18px;
}

.detail-hero,
.detail-section,
.detail-actions {
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(12px);
}

.detail-hero {
  padding: 24px;
}

.hero-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn-text {
  margin-left: 4px;
}

.hero-title {
  margin: 12px 0 10px;
  font-size: clamp(30px, 4vw, 44px);
  color: var(--primary-color-dark);
  line-height: 1.02;
}

.hero-text {
  margin: 0;
  max-width: 760px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 22px;
}

.metric-card {
  display: grid;
  gap: 6px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid var(--border-color-light);
}

.metric-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-tertiary);
}

.metric-card strong {
  color: var(--primary-color-dark);
  word-break: break-all;
}

.detail-section {
  padding: 20px;
}

.detail-section h3 {
  margin: 0 0 16px;
  color: var(--primary-color-dark);
  font-size: 22px;
}

.detail-section :deep(.ant-descriptions-bordered .ant-descriptions-item-label) {
  background: rgba(255, 255, 255, 0.72);
}

.access-url-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.access-url {
  flex: 1;
  word-break: break-all;
  min-width: 0;
}

.copy-btn {
  flex-shrink: 0;
  white-space: nowrap;
}

.token-display {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.token-display :deep(.ant-typography) {
  font-family: var(--font-mono);
  word-break: break-all;
}

.matter-data-pre {
  margin: 0;
  background: rgba(255, 255, 255, 0.84);
  padding: 18px;
  border-radius: 18px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.7;
  border: 1px solid var(--border-color-light);
}

.detail-actions {
  padding: 20px;
}

.expired {
  color: var(--error-color);
}

@media (max-width: 768px) {
  .detail-hero,
  .detail-section,
  .detail-actions {
    padding: 16px;
    border-radius: 20px;
  }

  .hero-top {
    flex-direction: column;
  }

  .back-btn-text {
    display: none;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
    width: 100%;
  }

  .action-buttons :deep(.ant-space-item),
  .action-buttons :deep(.ant-btn) {
    width: 100%;
  }
}
</style>
