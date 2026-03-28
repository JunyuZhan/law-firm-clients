<template>
  <div class="letter-verification-container">
    <a-card>
      <template #title>
        <span>函件验证记录</span>
      </template>
      <template #extra>
        <a-space>
          <a-button @click="loadStatistics">
            <template #icon>
              <BarChartOutlined />
            </template>
            统计
          </a-button>
          <a-button @click="loadData">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </template>

      <!-- 统计卡片 -->
      <a-row
        v-if="showStatistics"
        :gutter="16"
        style="margin-bottom: 16px"
      >
        <a-col
          :xs="12"
          :sm="6"
        >
          <a-statistic
            title="总记录数"
            :value="statistics.total"
          />
        </a-col>
        <a-col
          :xs="12"
          :sm="6"
        >
          <a-statistic
            title="有效"
            :value="statistics.active"
            value-style="color: #52c41a"
          />
        </a-col>
        <a-col
          :xs="12"
          :sm="6"
        >
          <a-statistic
            title="已过期"
            :value="statistics.expired"
            value-style="color: #faad14"
          />
        </a-col>
        <a-col
          :xs="12"
          :sm="6"
        >
          <a-statistic
            title="已撤销"
            :value="statistics.revoked"
            value-style="color: #ff4d4f"
          />
        </a-col>
      </a-row>

      <!-- 搜索表单 -->
      <a-form
        :model="searchForm"
        layout="inline"
        style="margin-bottom: 16px"
        @finish="handleSearch"
      >
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="ACTIVE">
              有效
            </a-select-option>
            <a-select-option value="REVOKED">
              已撤销
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="编号/律所/律师/接收单位"
            allow-clear
            style="width: 200px"
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
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record)">
              {{ getStatusName(record) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'validUntil'">
            <span :style="{ color: isExpired(record.validUntil) ? '#cf1322' : '' }">
              {{ formatDateTime(record.validUntil) }}
            </span>
          </template>
          <template v-else-if="column.key === 'verifyCount'">
            <a-badge
              :count="record.verifyCount"
              :number-style="{ backgroundColor: '#52c41a' }"
              show-zero
            />
          </template>
          <template v-else-if="column.key === 'lastVerifyAt'">
            <span v-if="record.lastVerifyAt">
              {{ formatDateTime(record.lastVerifyAt) }}
              <a-tooltip
                v-if="record.lastVerifyIp"
                :title="'IP: ' + record.lastVerifyIp"
              >
                <InfoCircleOutlined style="margin-left: 4px; color: #999" />
              </a-tooltip>
            </span>
            <span
              v-else
              style="color: #999"
            >-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleView(record)"
              >
                详情
              </a-button>
              <a-popconfirm
                v-if="record.status === 'ACTIVE' && !isExpired(record.validUntil)"
                title="确定要撤销这个函件验证吗？"
                @confirm="handleRevoke(record)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                >
                  撤销
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情抽屉 -->
    <a-drawer
      v-model:open="detailVisible"
      title="函件验证详情"
      :width="500"
      placement="right"
    >
      <a-descriptions
        v-if="currentRecord"
        :column="1"
        bordered
        size="small"
      >
        <a-descriptions-item label="函件编号">
          <a-typography-text copyable>
            {{ currentRecord.applicationNo }}
          </a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentRecord)">
            {{ getStatusName(currentRecord) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="函件类型">
          {{ currentRecord.letterTypeName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="律师事务所">
          {{ currentRecord.firmName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="出函律师">
          {{ currentRecord.lawyerNames || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="接收单位">
          {{ currentRecord.targetUnit || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="关联项目">
          {{ currentRecord.matterName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="审批时间">
          {{ formatDateTime(currentRecord.approvedAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="盖章时间">
          {{ formatDateTime(currentRecord.printedAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="有效期至">
          <span :style="{ color: isExpired(currentRecord.validUntil) ? '#cf1322' : '' }">
            {{ formatDateTime(currentRecord.validUntil) }}
            {{ isExpired(currentRecord.validUntil) ? '（已过期）' : '' }}
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="验证次数">
          <a-badge
            :count="currentRecord.verifyCount"
            :number-style="{ backgroundColor: '#52c41a' }"
            show-zero
          />
        </a-descriptions-item>
        <a-descriptions-item label="最后验证时间">
          {{ formatDateTime(currentRecord.lastVerifyAt) || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="最后验证IP">
          {{ currentRecord.lastVerifyIp || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="备注">
          {{ currentRecord.remark || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatDateTime(currentRecord.createdAt) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined, BarChartOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import { getVerificationList, revokeVerification, getVerificationStatistics } from '@/api/letter-verification'
import type { LetterVerificationRecord } from '@/api/letter-verification'

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

const searchForm = ref({
  status: undefined as string | undefined,
  keyword: '',
})

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: '函件编号', key: 'applicationNo', dataIndex: 'applicationNo', width: 150, align: 'center' },
  { title: '函件类型', key: 'letterTypeName', dataIndex: 'letterTypeName', width: 100, align: 'center', ellipsis: true },
  { title: '律师事务所', key: 'firmName', dataIndex: 'firmName', width: 150, align: 'center', ellipsis: true },
  { title: '出函律师', key: 'lawyerNames', dataIndex: 'lawyerNames', width: 120, align: 'center', ellipsis: true },
  { title: '接收单位', key: 'targetUnit', dataIndex: 'targetUnit', width: 150, align: 'center', ellipsis: true },
  { title: '状态', key: 'status', width: 80, align: 'center' },
  { title: '有效期至', key: 'validUntil', width: 160, align: 'center' },
  { title: '验证次数', key: 'verifyCount', width: 90, align: 'center' },
  { title: '最后验证', key: 'lastVerifyAt', width: 180, align: 'center' },
  { title: '操作', key: 'action', width: 120, fixed: 'right', align: 'center' },
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
    const errorMessage = error instanceof Error ? error.message : '加载数据失败'
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
    const errorMessage = error instanceof Error ? error.message : '加载统计数据失败'
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
    message.success('已撤销')
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '撤销失败'
    message.error(errorMessage)
  }
}

function getStatusName(record: LetterVerificationRecord): string {
  if (record.status === 'REVOKED') return '已撤销'
  if (record.status === 'ACTIVE' && isExpired(record.validUntil)) return '已过期'
  if (record.status === 'ACTIVE') return '有效'
  return '未知'
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
  padding: 0;
}

.letter-verification-container :deep(.ant-card) {
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.letter-verification-container :deep(.ant-card-head) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.letter-verification-container :deep(.ant-card-body) {
  padding: 20px;
}

.letter-verification-container :deep(.ant-statistic-title) {
  font-size: 13px;
  color: var(--text-secondary);
}

.letter-verification-container :deep(.ant-statistic-content-value) {
  font-size: 24px;
}

.letter-verification-container :deep(.ant-table-thead > tr > th) {
  background: var(--bg-tertiary);
  font-weight: 600;
  padding: 12px 8px;
}

.letter-verification-container :deep(.ant-table-tbody > tr > td) {
  padding: 12px 8px;
}

/* 固定列背景色 */
.letter-verification-container :deep(.ant-table-cell-fix-right),
.letter-verification-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-right) {
  background: #fff !important;
}

.letter-verification-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-right) {
  background: #fafafa !important;
}

.letter-verification-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-right) {
  background: var(--accent-color-lighter, #fffbf0) !important;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .letter-verification-container :deep(.ant-card-head) {
    padding: 12px 16px;
  }

  .letter-verification-container :deep(.ant-card-body) {
    padding: 16px 12px;
  }

  .letter-verification-container :deep(.ant-form) {
    flex-direction: column;
  }

  .letter-verification-container :deep(.ant-form-item) {
    margin-bottom: 12px;
    width: 100%;
  }

  .letter-verification-container :deep(.ant-select),
  .letter-verification-container :deep(.ant-input) {
    width: 100% !important;
  }

  .letter-verification-container :deep(.ant-statistic-content-value) {
    font-size: 20px;
  }
}
</style>
