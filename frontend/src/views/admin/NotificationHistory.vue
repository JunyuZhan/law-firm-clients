<template>
  <div class="notification-history-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.intro }}
        </p>
      </div>
      <a-button @click="loadData">
        <template #icon>
          <ReloadOutlined />
        </template>
        {{ UI_TEXTS.refresh }}数据
      </a-button>
    </section>

    <section class="stats-grid">
      <article class="stats-card">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.stats.total }}</span>
        <strong>{{ statistics.total }}</strong>
      </article>
      <article class="stats-card success">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.stats.success }}</span>
        <strong>{{ statistics.success }}</strong>
      </article>
      <article class="stats-card danger">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.stats.failed }}</span>
        <strong>{{ statistics.failed }}</strong>
      </article>
      <article class="stats-card info">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.stats.pending }}</span>
        <strong>{{ statistics.pending }}</strong>
      </article>
    </section>

    <section class="channel-grid">
      <article class="channel-card">
        <div class="channel-title">
          {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.sms }}
        </div>
        <div class="channel-total">
          {{ statisticsData?.byType?.SMS?.total || statisticsData?.sms?.total || 0 }}
        </div>
        <p>{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.success }} {{ statisticsData?.byType?.SMS?.success || statisticsData?.sms?.success || 0 }} / {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.failed }} {{ statisticsData?.byType?.SMS?.failed || statisticsData?.sms?.failed || 0 }}</p>
      </article>
      <article class="channel-card">
        <div class="channel-title">
          {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.wechat }}
        </div>
        <div class="channel-total">
          {{ statisticsData?.byType?.WECHAT?.total || statisticsData?.wechat?.total || 0 }}
        </div>
        <p>{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.success }} {{ statisticsData?.byType?.WECHAT?.success || statisticsData?.wechat?.success || 0 }} / {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.failed }} {{ statisticsData?.byType?.WECHAT?.failed || statisticsData?.wechat?.failed || 0 }}</p>
      </article>
      <article class="channel-card">
        <div class="channel-title">
          {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.email }}
        </div>
        <div class="channel-total">
          {{ statisticsData?.byType?.EMAIL?.total || statisticsData?.email?.total || 0 }}
        </div>
        <p>{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.success }} {{ statisticsData?.byType?.EMAIL?.success || statisticsData?.email?.success || 0 }} / {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.channelSummary.failed }} {{ statisticsData?.byType?.EMAIL?.failed || statisticsData?.email?.failed || 0 }}</p>
      </article>
    </section>

    <section class="filter-panel">
      <div class="panel-head dashboard-panel-head">
        <h3>{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.title }}</h3>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="notification-filter-form"
        @finish="handleSearch"
      >
        <a-form-item :label="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.matterIdLabel">
          <a-input
            v-model:value="searchForm.matterId"
            :placeholder="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.matterIdPlaceholder"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.clientIdLabel">
          <a-input-number
            v-model:value="searchForm.clientId"
            :placeholder="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.clientIdPlaceholder"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.typeLabel">
          <a-select
            v-model:value="searchForm.notificationType"
            :placeholder="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.typePlaceholder"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="SMS">
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.typeOptions.sms }}
            </a-select-option>
            <a-select-option value="WECHAT">
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.typeOptions.wechat }}
            </a-select-option>
            <a-select-option value="EMAIL">
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.typeOptions.email }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.statusLabel">
          <a-select
            v-model:value="searchForm.status"
            :placeholder="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.statusPlaceholder"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="PENDING">
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.statusOptions.pending }}
            </a-select-option>
            <a-select-option value="SUCCESS">
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.statusOptions.success }}
            </a-select-option>
            <a-select-option value="FAILED">
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.statusOptions.failed }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.dateRangeLabel">
          <a-range-picker
            v-model:value="dateRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateRangeChange"
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
        <h3>{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.tableTitle }}</h3>
      </div>

      <div class="table-summary dashboard-table-summary">
        <span>{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.totalPrefix }}{{ dataSource.length }}{{ ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.totalSuffix }}</span>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading || statisticsLoading"
        :pagination="pagination"
        :scroll="{ x: 1100 }"
        row-key="id"
        size="small"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'notificationType'">
            <a-tag :color="getTypeColor(record.notificationType)">
              {{ getTypeName(record.notificationType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusName(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'recipient'">
            <span>{{ maskRecipient(record.recipient, record.notificationType) }}</span>
          </template>
          <template v-else-if="column.key === 'retryInfo'">
            <span v-if="record.retryCount !== undefined && record.retryCount > 0">
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.retry.label }} {{ record.retryCount }}/{{ record.maxRetries || 3 }}
              <a-tag
                v-if="record.nextRetryAt"
                color="orange"
                class="retry-tag"
              >
                {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.retry.nextRetry }}: {{ formatDate(record.nextRetryAt) }}
              </a-tag>
            </span>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button
              v-if="record.status === 'FAILED' && (record.retryCount || 0) < (record.maxRetries || 3)"
              type="link"
              size="small"
              @click="handleRetry(record)"
            >
              {{ ADMIN_NOTIFICATION_HISTORY_TEXTS.retry.button }}
            </a-button>
          </template>
        </template>
      </a-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { getNotificationHistory, getNotificationStatistics, sendNotification, type NotificationHistory, type NotificationStatistics } from '@/api/notification'
import { formatDate } from '@/utils/date'
import { getNotificationStatusColor, getNotificationStatusText } from '@/utils/status'
import type { Dayjs } from 'dayjs'
import logger from '@/utils/logger'
import { UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_NOTIFICATION_HISTORY_TEXTS } from '@/constants/adminTexts'

const loading = ref(false)
const statisticsLoading = ref(false)
const dataSource = ref<NotificationHistory[]>([])
const statisticsData = ref<NotificationStatistics | null>(null)
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

const searchForm = ref({
  matterId: '',
  clientId: undefined as number | undefined,
  notificationType: '',
  status: '',
  startTime: '',
  endTime: '',
  limit: 100,
})

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `${ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.totalPrefix}${total}${ADMIN_NOTIFICATION_HISTORY_TEXTS.filter.totalSuffix}`,
})

const statistics = computed(() => {
  if (statisticsData.value) {
    return {
      total: statisticsData.value.total || 0,
      success: statisticsData.value.success || 0,
      failed: statisticsData.value.failed || 0,
      pending: statisticsData.value.pending || 0,
    }
  }

  const stats = {
    total: dataSource.value.length,
    success: 0,
    failed: 0,
    pending: 0,
  }

  dataSource.value.forEach(item => {
    if (item.status === 'SUCCESS') stats.success++
    else if (item.status === 'FAILED') stats.failed++
    else if (item.status === 'PENDING') stats.pending++
  })

  return stats
})
const columns = [
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.id, key: 'id', dataIndex: 'id', width: 72, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.matterId, key: 'matterId', dataIndex: 'matterId', ellipsis: true, width: 150, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.notificationType, key: 'notificationType', width: 100, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.recipient, key: 'recipient', ellipsis: true, width: 140, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.status, key: 'status', width: 92, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.sentAt, key: 'sentAt', dataIndex: 'sentAt', width: 160, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.createdAt, key: 'createdAt', dataIndex: 'createdAt', width: 160, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.retryInfo, key: 'retryInfo', width: 180, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.errorMessage, key: 'errorMessage', dataIndex: 'errorMessage', ellipsis: true, width: 180, align: 'center' },
  { title: ADMIN_NOTIFICATION_HISTORY_TEXTS.table.action, key: 'action', width: 88, align: 'center' },
]

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (searchForm.value.matterId) params.matterId = searchForm.value.matterId
    if (searchForm.value.clientId) params.clientId = searchForm.value.clientId
    if (searchForm.value.notificationType) params.notificationType = searchForm.value.notificationType
    if (searchForm.value.status) params.status = searchForm.value.status
    if (searchForm.value.startTime) params.startTime = searchForm.value.startTime
    if (searchForm.value.endTime) params.endTime = searchForm.value.endTime
    if (searchForm.value.limit) params.limit = searchForm.value.limit

    const res = await getNotificationHistory(params)
    dataSource.value = res.data || []
    pagination.value.total = dataSource.value.length
    await loadStatistics()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_NOTIFICATION_HISTORY_TEXTS.loadFailed
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  statisticsLoading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (searchForm.value.matterId) params.matterId = searchForm.value.matterId
    if (searchForm.value.clientId) params.clientId = searchForm.value.clientId
    if (searchForm.value.startTime) params.startTime = searchForm.value.startTime
    if (searchForm.value.endTime) params.endTime = searchForm.value.endTime

    const res = await getNotificationStatistics(params)
    statisticsData.value = res.data || null
  } catch (error: unknown) {
    logger.warn(ADMIN_NOTIFICATION_HISTORY_TEXTS.feedback.statsLoadFailed, error)
  } finally {
    statisticsLoading.value = false
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  searchForm.value = {
    matterId: '',
    clientId: undefined,
    notificationType: '',
    status: '',
    startTime: '',
    endTime: '',
    limit: 100,
  }
  dateRange.value = null
  handleSearch()
}

function handleDateRangeChange(dates: [Dayjs, Dayjs] | null) {
  if (dates) {
    searchForm.value.startTime = dates[0].toISOString()
    searchForm.value.endTime = dates[1].toISOString()
  } else {
    searchForm.value.startTime = ''
    searchForm.value.endTime = ''
  }
}

function handleTableChange(pag: TablePaginationConfig) {
  if (pag.current) pagination.value.current = pag.current
  if (pag.pageSize) pagination.value.pageSize = pag.pageSize
}

async function handleRetry(record: NotificationHistory) {
  if (!record?.matterId) {
    message.error(ADMIN_NOTIFICATION_HISTORY_TEXTS.retry.missingMatter)
    return
  }
  try {
    await sendNotification(record.matterId)
    message.success(ADMIN_NOTIFICATION_HISTORY_TEXTS.retry.success)
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_NOTIFICATION_HISTORY_TEXTS.retry.failed
    message.error(errorMessage)
  }
}

function getTypeName(type: string): string {
  const map: Record<string, string> = {
    SMS: ADMIN_NOTIFICATION_HISTORY_TEXTS.typeOptions.sms,
    WECHAT: ADMIN_NOTIFICATION_HISTORY_TEXTS.typeOptions.wechat,
    EMAIL: ADMIN_NOTIFICATION_HISTORY_TEXTS.typeOptions.email,
  }
  return map[type] || type
}

function getTypeColor(type: string): string {
  const map: Record<string, string> = {
    SMS: 'blue',
    WECHAT: 'green',
    EMAIL: 'orange',
  }
  return map[type] || 'default'
}

const getStatusName = getNotificationStatusText
const getStatusColor = getNotificationStatusColor

function maskRecipient(recipient: string, type: string): string {
  if (!recipient) return '-'

  if (type === 'SMS' && recipient.length === 11) {
    return recipient.substring(0, 3) + '****' + recipient.substring(7)
  }

  if (type === 'EMAIL') {
    const [local, domain] = recipient.split('@')
    if (local && domain) {
      return local.substring(0, 2) + '***@' + domain
    }
  }

  return recipient
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notification-history-container {
  display: grid;
  gap: 18px;
}

.stats-card.success strong {
  color: var(--success-color);
}

.stats-card.danger strong {
  color: var(--error-color);
}

.stats-card.info strong {
  color: var(--primary-color);
}

.notification-filter-form {
  display: flex;
  gap: 12px 8px;
}

.notification-filter-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  margin-left: auto;
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.channel-card {
  padding: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  box-shadow: var(--shadow-sm);
}

.channel-title {
  display: block;
  margin-bottom: 8px;
  color: var(--text-tertiary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.channel-total {
  display: block;
  color: var(--text-primary);
  font-size: 28px;
  line-height: 1.1;
}

.channel-card p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.retry-tag {
  margin-left: 8px;
}

@media (max-width: 992px) {
  .channel-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .notification-filter-form {
    display: grid;
  }

  .notification-filter-form :deep(.ant-form-item) {
    width: 100%;
  }

  .notification-filter-form :deep(.ant-input),
  .notification-filter-form :deep(.ant-input-number),
  .notification-filter-form :deep(.ant-select),
  .notification-filter-form :deep(.ant-picker) {
    width: 100% !important;
  }

  .filter-actions {
    margin-left: 0;
  }

  .channel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
