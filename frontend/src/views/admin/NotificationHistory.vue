<template>
  <div class="notification-history-container">
    <a-card>
      <template #title>
        <span>通知记录列表</span>
      </template>
      <template #extra>
        <a-space>
          <a-button @click="loadData">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </template>

      <!-- 搜索表单 -->
      <a-form
        :model="searchForm"
        layout="inline"
        style="margin-bottom: 16px"
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
        <a-form-item>
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

      <!-- 统计信息 -->
      <a-spin :spinning="statisticsLoading">
        <a-row
          :gutter="16"
          style="margin-bottom: 16px"
        >
          <a-col
            :xs="12"
            :sm="6"
          >
            <a-statistic
              title="总数"
              :value="statistics.total"
            />
          </a-col>
          <a-col
            :xs="12"
            :sm="6"
          >
            <a-statistic
              title="成功"
              :value="statistics.success"
              :value-style="{ color: '#3f8600' }"
            />
          </a-col>
          <a-col
            :xs="12"
            :sm="6"
          >
            <a-statistic
              title="失败"
              :value="statistics.failed"
              :value-style="{ color: '#cf1322' }"
            />
          </a-col>
          <a-col
            :xs="12"
            :sm="6"
          >
            <a-statistic
              title="待发送"
              :value="statistics.pending"
              :value-style="{ color: '#1890ff' }"
            />
          </a-col>
        </a-row>
        
        <!-- 按类型统计 -->
        <a-row
          v-if="statisticsData && (statisticsData.byType || statisticsData.sms)"
          :gutter="16"
          style="margin-bottom: 16px"
        >
          <a-col
            :xs="24"
            :sm="8"
          >
            <a-card size="small">
              <a-statistic
                title="短信"
                :value="(statisticsData.byType?.SMS?.total || statisticsData.sms?.total || 0)"
              />
              <div style="margin-top: 8px">
                <span style="color: #3f8600">成功: {{ statisticsData.byType?.SMS?.success || statisticsData.sms?.success || 0 }}</span>
                <span style="margin-left: 16px; color: #cf1322">失败: {{ statisticsData.byType?.SMS?.failed || statisticsData.sms?.failed || 0 }}</span>
              </div>
            </a-card>
          </a-col>
          <a-col
            :xs="24"
            :sm="8"
          >
            <a-card size="small">
              <a-statistic
                title="微信"
                :value="(statisticsData.byType?.WECHAT?.total || statisticsData.wechat?.total || 0)"
              />
              <div style="margin-top: 8px">
                <span style="color: #3f8600">成功: {{ statisticsData.byType?.WECHAT?.success || statisticsData.wechat?.success || 0 }}</span>
                <span style="margin-left: 16px; color: #cf1322">失败: {{ statisticsData.byType?.WECHAT?.failed || statisticsData.wechat?.failed || 0 }}</span>
              </div>
            </a-card>
          </a-col>
          <a-col
            :xs="24"
            :sm="8"
          >
            <a-card size="small">
              <a-statistic
                title="邮件"
                :value="(statisticsData.byType?.EMAIL?.total || statisticsData.email?.total || 0)"
              />
              <div style="margin-top: 8px">
                <span style="color: #3f8600">成功: {{ statisticsData.byType?.EMAIL?.success || statisticsData.email?.success || 0 }}</span>
                <span style="margin-left: 16px; color: #cf1322">失败: {{ statisticsData.byType?.EMAIL?.failed || statisticsData.email?.failed || 0 }}</span>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-spin>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
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
                style="margin-left: 8px"
              >
                下次重试: {{ formatDate(record.nextRetryAt) }}
              </a-tag>
            </span>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button
              v-if="record.status === 'FAILED' && record.retryCount < (record.maxRetries || 3)"
              type="link"
              size="small"
              @click="handleRetry(record)"
            >
              重试
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
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

// 统计信息（优先使用后端统计，否则使用前端计算）
const statistics = computed(() => {
  if (statisticsData.value) {
    return {
      total: statisticsData.value.total || 0,
      success: statisticsData.value.success || 0,
      failed: statisticsData.value.failed || 0,
      pending: statisticsData.value.pending || 0,
    }
  }
  
  // 前端计算（作为后备）
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

// 表格列定义
const columns = [
  { title: 'ID', key: 'id', dataIndex: 'id', width: 80, align: 'center' },
  { title: '项目ID', key: 'matterId', dataIndex: 'matterId', ellipsis: true, width: 180, align: 'center' },
  { title: '通知类型', key: 'notificationType', width: 110, align: 'center' },
  { title: '接收人', key: 'recipient', ellipsis: true, width: 150, align: 'center' },
  { title: '状态', key: 'status', width: 100, align: 'center' },
  { title: '发送时间', key: 'sentAt', dataIndex: 'sentAt', width: 180, align: 'center' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt', width: 180, align: 'center' },
  { title: '重试信息', key: 'retryInfo', width: 200, align: 'center' },
  { title: '错误信息', key: 'errorMessage', dataIndex: 'errorMessage', ellipsis: true, width: 200, align: 'center' },
  { title: '操作', key: 'action', width: 100, align: 'center', fixed: 'right' },
]

// 加载数据
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
    
    // 加载统计信息
    await loadStatistics()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '加载通知记录失败'
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 加载统计信息
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

// 搜索
function handleSearch() {
  pagination.value.current = 1
  loadData()
}

// 重置
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

// 日期范围变化
function handleDateRangeChange(dates: [Dayjs, Dayjs] | null) {
  if (dates) {
    searchForm.value.startTime = dates[0].toISOString()
    searchForm.value.endTime = dates[1].toISOString()
  } else {
    searchForm.value.startTime = ''
    searchForm.value.endTime = ''
  }
}

// 表格变化
function handleTableChange(pag: TablePaginationConfig) {
  if (pag.current) pagination.value.current = pag.current
  if (pag.pageSize) pagination.value.pageSize = pag.pageSize
}

// 重试发送
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

// 获取类型名称
function getTypeName(type: string): string {
  const map: Record<string, string> = {
    SMS: '短信',
    WECHAT: '微信',
    EMAIL: '邮件',
  }
  return map[type] || type
}

// 获取类型颜色
function getTypeColor(type: string): string {
  const map: Record<string, string> = {
    SMS: 'blue',
    WECHAT: 'green',
    EMAIL: 'orange',
  }
  return map[type] || 'default'
}

// 使用统一的状态工具函数
const getStatusName = getNotificationStatusText
const getStatusColor = getNotificationStatusColor

// 脱敏显示接收人
function maskRecipient(recipient: string, type: string): string {
  if (!recipient) return '-'
  
  if (type === 'SMS') {
    // 手机号：138****5678
    if (recipient.length === 11) {
      return recipient.substring(0, 3) + '****' + recipient.substring(7)
    }
  } else if (type === 'EMAIL') {
    // 邮箱：wa***@example.com
    const [local, domain] = recipient.split('@')
    if (local && domain) {
      const masked = local.substring(0, 2) + '***'
      return masked + '@' + domain
    }
  }
  
  return recipient
}

// formatDate 已从 @/utils/date 导入

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notification-history-container {
  padding: 0;
}

.notification-history-container :deep(.ant-card) {
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.notification-history-container :deep(.ant-card-head) {
  padding: 16px 20px;
}

.notification-history-container :deep(.ant-card-body) {
  padding: 20px;
}

.notification-history-container :deep(.ant-form) {
  margin-bottom: 16px;
}

.notification-history-container :deep(.ant-table) {
  font-size: 14px;
}

/* 固定列背景色 - 最强覆盖 */
.notification-history-container :deep(.ant-table-cell-fix-right),
.notification-history-container :deep(.ant-table-cell-fix-left),
.notification-history-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-right),
.notification-history-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-left),
.notification-history-container :deep(td.ant-table-cell-fix-right),
.notification-history-container :deep(td.ant-table-cell-fix-left),
.notification-history-container :deep([class*="ant-table-cell-fix-right"]),
.notification-history-container :deep([class*="ant-table-cell-fix-left"]) {
  background: #fff !important;
  background-color: #fff !important;
  background-image: none !important;
}

.notification-history-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-right),
.notification-history-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-left),
.notification-history-container :deep(th.ant-table-cell-fix-right),
.notification-history-container :deep(th.ant-table-cell-fix-left) {
  background: #fafafa !important;
  background-color: #fafafa !important;
  background-image: none !important;
}

.notification-history-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-right),
.notification-history-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-left),
.notification-history-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-right"]),
.notification-history-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-left"]),
.notification-history-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
.notification-history-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
.notification-history-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-right),
.notification-history-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-left),
.notification-history-container :deep(tr:hover td.ant-table-cell-fix-right),
.notification-history-container :deep(tr:hover td.ant-table-cell-fix-left),
.notification-history-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-right"]),
.notification-history-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-left"]),
.notification-history-container :deep(tr:hover [class*="ant-table-cell-fix-right"]),
.notification-history-container :deep(tr:hover [class*="ant-table-cell-fix-left"]) {
  background: var(--accent-color-lighter, #fffbf0) !important;
  background-color: var(--accent-color-lighter, #fffbf0) !important;
  background-image: none !important;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .notification-history-container {
    padding: 0;
  }
  
  .notification-history-container :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .notification-history-container :deep(.ant-card-body) {
    padding: 16px 12px;
  }
  
  .notification-history-container :deep(.ant-form) {
    flex-direction: column;
  }
  
  .notification-history-container :deep(.ant-form-item) {
    width: 100%;
    margin-bottom: 12px;
  }
  
  .notification-history-container :deep(.ant-input),
  .notification-history-container :deep(.ant-select),
  .notification-history-container :deep(.ant-picker) {
    width: 100% !important;
  }
  
  .notification-history-container :deep(.ant-table) {
    font-size: 12px;
  }
  
  .notification-history-container :deep(.ant-table-thead > tr > th) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .notification-history-container :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .notification-history-container :deep(.ant-table-scroll) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .notification-history-container :deep(.ant-btn) {
    height: 32px;
    padding: 0 12px;
    font-size: 12px;
  }
  
  .notification-history-container :deep(.ant-statistic-title) {
    font-size: 12px;
  }
  
  .notification-history-container :deep(.ant-statistic-content) {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .notification-history-container :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .notification-history-container :deep(.ant-card-body) {
    padding: 12px 8px;
  }
  
  .notification-history-container :deep(.ant-table-thead > tr > th),
  .notification-history-container :deep(.ant-table-tbody > tr > td) {
    padding: 6px 2px;
    font-size: 10px;
  }
}
</style>
