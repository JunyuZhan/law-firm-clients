<template>
  <div class="notification-history-container">
    <section class="page-intro">
      <div>
        <div class="eyebrow">
          通知管理
        </div>
        <h2 class="editorial-title intro-title">
          通知记录
        </h2>
        <p class="intro-text">
          查看短信、微信与邮件发送状态，定位失败原因，并按项目维度触发重试。
        </p>
      </div>
      <a-button @click="loadData">
        <template #icon>
          <ReloadOutlined />
        </template>
        刷新
      </a-button>
    </section>

    <section class="filter-panel">
      <div class="panel-head">
        <div>
          <span class="panel-kicker">筛选与概览</span>
          <h3>发送态势</h3>
        </div>
        <p>按项目、渠道和状态定位异常，再决定是否触发重试。</p>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="notification-filter-form"
        @finish="handleSearch"
      >
        <a-form-item label="项目ID">
          <a-input
            v-model:value="searchForm.matterId"
            placeholder="请输入项目ID"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="客户ID">
          <a-input-number
            v-model:value="searchForm.clientId"
            placeholder="请输入客户ID"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item label="通知类型">
          <a-select
            v-model:value="searchForm.notificationType"
            placeholder="请选择通知类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="SMS">
              短信
            </a-select-option>
            <a-select-option value="WECHAT">
              微信
            </a-select-option>
            <a-select-option value="EMAIL">
              邮件
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="PENDING">
              待发送
            </a-select-option>
            <a-select-option value="SUCCESS">
              成功
            </a-select-option>
            <a-select-option value="FAILED">
              失败
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="时间范围">
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
              查询
            </a-button>
            <a-button @click="handleReset">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">总数</span>
        <strong>{{ statistics.total }}</strong>
        <p>当前筛选范围内的全部通知记录。</p>
      </div>
      <div class="stats-card success">
        <span class="stats-label">成功</span>
        <strong>{{ statistics.success }}</strong>
        <p>已完成发送并返回成功结果。</p>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">失败</span>
        <strong>{{ statistics.failed }}</strong>
        <p>优先关注失败原因与重试队列。</p>
      </div>
      <div class="stats-card info">
        <span class="stats-label">待发送</span>
        <strong>{{ statistics.pending }}</strong>
        <p>仍在等待异步发送或回执确认。</p>
      </div>
    </section>

    <section class="channel-grid">
      <article class="channel-card">
        <div class="channel-title">
          短信
        </div>
        <div class="channel-total">
          {{ statisticsData?.byType?.SMS?.total || statisticsData?.sms?.total || 0 }}
        </div>
        <p>成功 {{ statisticsData?.byType?.SMS?.success || statisticsData?.sms?.success || 0 }} / 失败 {{ statisticsData?.byType?.SMS?.failed || statisticsData?.sms?.failed || 0 }}</p>
      </article>
      <article class="channel-card">
        <div class="channel-title">
          微信
        </div>
        <div class="channel-total">
          {{ statisticsData?.byType?.WECHAT?.total || statisticsData?.wechat?.total || 0 }}
        </div>
        <p>成功 {{ statisticsData?.byType?.WECHAT?.success || statisticsData?.wechat?.success || 0 }} / 失败 {{ statisticsData?.byType?.WECHAT?.failed || statisticsData?.wechat?.failed || 0 }}</p>
      </article>
      <article class="channel-card">
        <div class="channel-title">
          邮件
        </div>
        <div class="channel-total">
          {{ statisticsData?.byType?.EMAIL?.total || statisticsData?.email?.total || 0 }}
        </div>
        <p>成功 {{ statisticsData?.byType?.EMAIL?.success || statisticsData?.email?.success || 0 }} / 失败 {{ statisticsData?.byType?.EMAIL?.failed || statisticsData?.email?.failed || 0 }}</p>
      </article>
    </section>

    <section class="table-panel">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading || statisticsLoading"
        :pagination="pagination"
        :scroll="{ x: 'max-content' }"
        row-key="id"
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
              重试 {{ record.retryCount }}/{{ record.maxRetries || 3 }}
              <a-tag
                v-if="record.nextRetryAt"
                color="orange"
                class="retry-tag"
              >
                下次重试: {{ formatDate(record.nextRetryAt) }}
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
              重试
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
  showTotal: (total: number) => `共 ${total} 条`,
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
  { title: 'ID', key: 'id', dataIndex: 'id', width: 80, align: 'center' },
  { title: '项目ID', key: 'matterId', dataIndex: 'matterId', ellipsis: true, width: 180, align: 'center' },
  { title: '通知类型', key: 'notificationType', width: 110, align: 'center' },
  { title: '接收人', key: 'recipient', ellipsis: true, width: 150, align: 'center' },
  { title: '状态', key: 'status', width: 100, align: 'center' },
  { title: '发送时间', key: 'sentAt', dataIndex: 'sentAt', width: 180, align: 'center' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt', width: 180, align: 'center' },
  { title: '重试信息', key: 'retryInfo', width: 220, align: 'center' },
  { title: '错误信息', key: 'errorMessage', dataIndex: 'errorMessage', ellipsis: true, width: 200, align: 'center' },
  { title: '操作', key: 'action', width: 100, align: 'center', fixed: 'right' },
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
    const errorMessage = error instanceof Error ? error.message : '加载通知记录失败'
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
    logger.warn('加载统计信息失败:', error)
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
    message.error('无法获取项目ID')
    return
  }
  try {
    await sendNotification(record.matterId)
    message.success('已触发重试发送')
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '重试发送失败'
    message.error(errorMessage)
  }
}

function getTypeName(type: string): string {
  const map: Record<string, string> = {
    SMS: '短信',
    WECHAT: '微信',
    EMAIL: '邮件',
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

.channel-card {
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(12px);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-head h3 {
  margin: 6px 0 0;
  font-size: 22px;
  color: var(--primary-color-dark);
}

.panel-head p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.panel-kicker {
  display: inline-block;
  color: var(--text-tertiary);
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
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
  gap: 16px;
}

.channel-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.channel-card {
  padding: 18px;
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
  color: var(--primary-color-dark);
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
  .channel-card {
    padding: 16px;
    border-radius: 20px;
  }

  .panel-head {
    display: grid;
  }

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
