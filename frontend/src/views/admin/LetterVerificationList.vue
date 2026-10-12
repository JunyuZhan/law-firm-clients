<template>
  <div class="letter-verification-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_LETTER_VERIFICATION_TEXTS.intro }}
        </p>
      </div>
      <a-space>
        <a-button @click="loadStatistics">
          <template #icon>
            <BarChartOutlined />
          </template>
          {{ showStatistics ? UI_TEXTS.collapseStatistics : UI_TEXTS.viewStatistics }}
        </a-button>
        <a-button @click="loadData">
          <template #icon>
            <ReloadOutlined />
          </template>
          {{ UI_TEXTS.refresh }}
        </a-button>
      </a-space>
    </section>

    <section
      v-if="showStatistics"
      class="stats-grid"
    >
      <article class="stats-card">
        <span class="stats-label">{{ ADMIN_LETTER_VERIFICATION_TEXTS.stats.total }}</span>
        <strong>{{ statistics.total }}</strong>
      </article>
      <article class="stats-card success">
        <span class="stats-label">{{ ADMIN_LETTER_VERIFICATION_TEXTS.stats.active }}</span>
        <strong>{{ statistics.active }}</strong>
      </article>
      <article class="stats-card warning">
        <span class="stats-label">{{ ADMIN_LETTER_VERIFICATION_TEXTS.stats.expired }}</span>
        <strong>{{ statistics.expired }}</strong>
      </article>
      <article class="stats-card danger">
        <span class="stats-label">{{ ADMIN_LETTER_VERIFICATION_TEXTS.stats.revoked }}</span>
        <strong>{{ statistics.revoked }}</strong>
      </article>
    </section>

    <section class="filter-panel">
      <div class="panel-head dashboard-panel-head">
        <h3>{{ ADMIN_LETTER_VERIFICATION_TEXTS.filter.title }}</h3>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="verification-filter-form"
        @finish="handleSearch"
      >
        <a-form-item :label="ADMIN_LETTER_VERIFICATION_TEXTS.filter.statusLabel">
          <a-select
            v-model:value="searchForm.status"
            :placeholder="ADMIN_LETTER_VERIFICATION_TEXTS.filter.statusPlaceholder"
            allow-clear
            style="width: 140px"
          >
            <a-select-option value="ACTIVE">
              {{ ADMIN_LETTER_VERIFICATION_TEXTS.statusOptions.active }}
            </a-select-option>
            <a-select-option value="REVOKED">
              {{ ADMIN_LETTER_VERIFICATION_TEXTS.statusOptions.revoked }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_LETTER_VERIFICATION_TEXTS.filter.keywordLabel">
          <a-input
            v-model:value="searchForm.keyword"
            :placeholder="ADMIN_LETTER_VERIFICATION_TEXTS.filter.keywordPlaceholder"
            allow-clear
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item class="filter-actions">
          <a-space>
            <a-button
              type="primary"
              html-type="submit"
            >
              {{ UI_TEXTS.search }}
            </a-button>
            <a-button @click="handleReset">
              {{ UI_TEXTS.reset }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <section class="table-panel">
      <div class="panel-head panel-head--table dashboard-panel-head dashboard-panel-head--table">
        <h3>{{ ADMIN_LETTER_VERIFICATION_TEXTS.table.title }}</h3>
      </div>

      <div class="table-summary dashboard-table-summary">
        <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.filter.totalPrefix }}{{ dataSource.length }}{{ ADMIN_LETTER_VERIFICATION_TEXTS.filter.totalSuffix }}</span>
        <span v-if="revocableCount">{{ ADMIN_LETTER_VERIFICATION_TEXTS.filter.revocablePrefix }}{{ revocableCount }}{{ ADMIN_LETTER_VERIFICATION_TEXTS.filter.revocableSuffix }}</span>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1120 }"
        row-key="id"
        size="small"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record)">
              {{ getStatusName(record) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'validUntil'">
            <span :class="{ expired: isExpired(record.validUntil) }">
              {{ formatDateTime(record.validUntil) }}
            </span>
          </template>
          <template v-else-if="column.key === 'verifyCount'">
            <span class="count-pill">{{ record.verifyCount }}</span>
          </template>
          <template v-else-if="column.key === 'lastVerifyAt'">
            <span
              v-if="record.lastVerifyAt"
              class="verify-time"
            >
              {{ formatDateTime(record.lastVerifyAt) }}
              <a-tooltip
                v-if="record.lastVerifyIp"
                :title="ADMIN_LETTER_VERIFICATION_TEXTS.table.ipPrefix + record.lastVerifyIp"
              >
                <InfoCircleOutlined class="verify-tip" />
              </a-tooltip>
            </span>
            <span
              v-else
              class="muted-text"
            >-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleView(record)"
              >
                {{ ADMIN_LETTER_VERIFICATION_TEXTS.actions.detail }}
              </a-button>
              <a-popconfirm
                v-if="record.status === 'ACTIVE' && !isExpired(record.validUntil)"
                :title="UI_CONFIRM_TEXTS.revokeVerification"
                @confirm="handleRevoke(record)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                >
                  {{ ADMIN_LETTER_VERIFICATION_TEXTS.actions.invalidate }}
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <a-drawer
      v-model:open="detailVisible"
      :width="520"
      placement="right"
      class="verification-drawer"
    >
      <template #title>
        <div class="drawer-title-block">
          <div class="drawer-title">
            {{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.title }}
          </div>
        </div>
      </template>

      <div
        v-if="currentRecord"
        class="detail-stack"
      >
        <section class="detail-hero">
          <div>
            <div class="detail-label">
              {{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.applicationNo }}
            </div>
            <h3>{{ currentRecord.applicationNo }}</h3>
          </div>
          <a-tag :color="getStatusColor(currentRecord)">
            {{ getStatusName(currentRecord) }}
          </a-tag>
        </section>

        <section class="detail-panel">
          <div class="detail-grid">
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.letterType }}</span>
              <strong>{{ currentRecord.letterTypeName || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.firmName }}</span>
              <strong>{{ currentRecord.firmName || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.lawyerNames }}</span>
              <strong>{{ currentRecord.lawyerNames || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.targetUnit }}</span>
              <strong>{{ currentRecord.targetUnit || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.matterName }}</span>
              <strong>{{ currentRecord.matterName || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.verifyCount }}</span>
              <strong>{{ currentRecord.verifyCount }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.approvedAt }}</span>
              <strong>{{ formatDateTime(currentRecord.approvedAt) }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.printedAt }}</span>
              <strong>{{ formatDateTime(currentRecord.printedAt) }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.validUntil }}</span>
              <strong :class="{ expired: isExpired(currentRecord.validUntil) }">
                {{ formatDateTime(currentRecord.validUntil) }}
                {{ isExpired(currentRecord.validUntil) ? ADMIN_LETTER_VERIFICATION_TEXTS.drawer.expiredSuffix : '' }}
              </strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.lastVerifyAt }}</span>
              <strong>{{ formatDateTime(currentRecord.lastVerifyAt) || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.lastVerifyIp }}</span>
              <strong>{{ currentRecord.lastVerifyIp || '-' }}</strong>
            </div>
            <div class="detail-item full">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.remark }}</span>
              <strong>{{ currentRecord.remark || '-' }}</strong>
            </div>
            <div class="detail-item full">
              <span>{{ ADMIN_LETTER_VERIFICATION_TEXTS.drawer.createdAt }}</span>
              <strong>{{ formatDateTime(currentRecord.createdAt) }}</strong>
            </div>
          </div>
        </section>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined, BarChartOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import { getVerificationList, revokeVerification, getVerificationStatistics } from '@/api/letter-verification'
import type { LetterVerificationRecord } from '@/api/letter-verification'
import { UI_CONFIRM_TEXTS, UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_LETTER_VERIFICATION_TEXTS } from '@/constants/adminTexts'

const loading = ref(false)
const dataSource = ref<LetterVerificationRecord[]>([])
const detailVisible = ref(false)
const currentRecord = ref<LetterVerificationRecord | null>(null)
const showStatistics = ref(false)

const statistics = reactive({
  total: 0,
  active: 0,
  expired: 0,
  revoked: 0,
})
const revocableCount = computed(() => dataSource.value.filter(record => record.status === 'ACTIVE' && !isExpired(record.validUntil)).length)

const searchForm = ref({
  status: undefined as string | undefined,
  keyword: '',
})

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `${ADMIN_LETTER_VERIFICATION_TEXTS.filter.totalPrefix}${total}${ADMIN_LETTER_VERIFICATION_TEXTS.filter.totalSuffix}`,
})

const columns = [
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.applicationNo, key: 'applicationNo', dataIndex: 'applicationNo', width: 140, align: 'center' },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.letterTypeName, key: 'letterTypeName', dataIndex: 'letterTypeName', width: 96, align: 'center', ellipsis: true },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.firmName, key: 'firmName', dataIndex: 'firmName', width: 140, align: 'center', ellipsis: true },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.lawyerNames, key: 'lawyerNames', dataIndex: 'lawyerNames', width: 110, align: 'center', ellipsis: true },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.targetUnit, key: 'targetUnit', dataIndex: 'targetUnit', width: 140, align: 'center', ellipsis: true },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.status, key: 'status', width: 84, align: 'center' },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.validUntil, key: 'validUntil', width: 150, align: 'center' },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.verifyCount, key: 'verifyCount', width: 92, align: 'center' },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.lastVerifyAt, key: 'lastVerifyAt', width: 160, align: 'center' },
  { title: ADMIN_LETTER_VERIFICATION_TEXTS.table.action, key: 'action', width: 108, align: 'center' },
]

async function loadData() {
  loading.value = true
  try {
    const res = await getVerificationList({
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
      status: searchForm.value.status,
      keyword: searchForm.value.keyword || undefined,
    })
    dataSource.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_LETTER_VERIFICATION_TEXTS.feedback.loadFailed
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  try {
    const res = await getVerificationStatistics()
    statistics.total = res.data.total || 0
    statistics.active = res.data.active || 0
    statistics.expired = res.data.expired || 0
    statistics.revoked = res.data.revoked || 0
    showStatistics.value = !showStatistics.value
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_LETTER_VERIFICATION_TEXTS.feedback.loadStatsFailed
    message.error(errorMessage)
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  searchForm.value = {
    status: undefined,
    keyword: '',
  }
  handleSearch()
}

function handleTableChange(pag: TablePaginationConfig) {
  if (pag.current) pagination.value.current = pag.current
  if (pag.pageSize) pagination.value.pageSize = pag.pageSize
  loadData()
}

function handleView(record: LetterVerificationRecord) {
  currentRecord.value = record
  detailVisible.value = true
}

async function handleRevoke(record: LetterVerificationRecord) {
  try {
    await revokeVerification(record.id)
    message.success(UI_FEEDBACK_TEXTS.verificationRevoked)
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_LETTER_VERIFICATION_TEXTS.feedback.revokeFailed
    message.error(errorMessage)
  }
}

function getStatusName(record: LetterVerificationRecord): string {
  if (record.status === 'REVOKED') return ADMIN_LETTER_VERIFICATION_TEXTS.statusOptions.revoked
  if (record.status === 'ACTIVE' && isExpired(record.validUntil)) return ADMIN_LETTER_VERIFICATION_TEXTS.statusOptions.expired
  if (record.status === 'ACTIVE') return ADMIN_LETTER_VERIFICATION_TEXTS.statusOptions.active
  return ADMIN_LETTER_VERIFICATION_TEXTS.statusOptions.unknown
}

function getStatusColor(record: LetterVerificationRecord): string {
  if (record.status === 'REVOKED') return 'error'
  if (record.status === 'ACTIVE' && isExpired(record.validUntil)) return 'warning'
  if (record.status === 'ACTIVE') return 'success'
  return 'default'
}

function isExpired(dateStr: string | undefined): boolean {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

function formatDateTime(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.letter-verification-container {
  display: grid;
  gap: 18px;
}

.guide-card,
.detail-hero,
.detail-panel {
  background: var(--lex-surface-strong);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}

.guide-card {
  display: grid;
  gap: 18px;
  padding: 20px;
}

.panel-kicker {
  display: inline-block;
  color: var(--lex-accent-strong);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-weight: 700;
}

.rule-grid {
  display: grid;
  gap: 12px;
}

.rule-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--border-color-light);
}

.rule-item span {
  color: var(--text-tertiary);
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.rule-item strong {
  color: var(--text-primary);
  line-height: 1.7;
}

.guide-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.85;
}

.verification-filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 0;
}

.filter-actions {
  margin-inline-start: auto;
}

.expired {
  color: var(--error-color);
  font-weight: 600;
}

.count-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--success-color) 16%, transparent);
  color: var(--success-color);
  font-weight: 700;
}

.verify-time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.verify-tip,
.muted-text {
  color: var(--text-tertiary);
}

.detail-stack {
  display: grid;
  gap: 16px;
}

.detail-hero,
.detail-panel {
  padding: 20px;
}

.detail-label,
.drawer-kicker {
  color: var(--text-tertiary);
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.drawer-title {
  margin-top: 6px;
  font-size: 22px;
  font-family: var(--font-heading);
}

.detail-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.detail-hero h3 {
  margin: 10px 0 0;
  font-size: 28px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.detail-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.detail-item span {
  color: var(--text-secondary);
  font-size: 12px;
}

.detail-item strong {
  color: var(--text-primary);
  font-weight: 600;
  line-height: 1.6;
}

.detail-item.full {
  grid-column: 1 / -1;
}

@media (max-width: 768px) {
  .verification-filter-form {
    display: block;
  }

  .verification-filter-form :deep(.ant-form-item) {
    width: 100%;
    margin-right: 0;
    margin-bottom: 12px;
  }

  .verification-filter-form :deep(.ant-select),
  .verification-filter-form :deep(.ant-input) {
    width: 100% !important;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-hero {
    flex-direction: column;
  }
}
</style>
