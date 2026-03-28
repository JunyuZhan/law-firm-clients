<template>
  <div class="letter-verification-container">
    <section class="page-intro">
      <div>
        <div class="eyebrow">
          Verification Ledger
        </div>
        <h2 class="editorial-title intro-title">
          函件验证记录
        </h2>
        <p class="intro-text">
          统一查看律师函验证状态、访问次数与过期情况，便于后台快速判断函件是否仍可被第三方核验。
        </p>
      </div>
      <a-space>
        <a-button @click="loadStatistics">
          <template #icon>
            <BarChartOutlined />
          </template>
          {{ showStatistics ? '收起统计' : '查看统计' }}
        </a-button>
        <a-button @click="loadData">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </a-space>
    </section>

    <section
      v-if="showStatistics"
      class="stats-grid"
    >
      <article class="stat-card">
        <span>总记录数</span>
        <strong>{{ statistics.total }}</strong>
        <p>当前全部可检索的函件验证记录。</p>
      </article>
      <article class="stat-card success">
        <span>有效</span>
        <strong>{{ statistics.active }}</strong>
        <p>仍可被第三方继续核验。</p>
      </article>
      <article class="stat-card warning">
        <span>已过期</span>
        <strong>{{ statistics.expired }}</strong>
        <p>超过有效期，建议重新核查状态。</p>
      </article>
      <article class="stat-card danger">
        <span>已撤销</span>
        <strong>{{ statistics.revoked }}</strong>
        <p>已被主动撤销，不应继续使用。</p>
      </article>
    </section>

    <section class="filter-panel">
      <div class="panel-head">
        <div>
          <span class="panel-kicker">Search Ledger</span>
          <h3>验证检索</h3>
        </div>
        <p>按状态与关键词快速定位异常函件，再进入详情与撤销操作。</p>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="verification-filter-form"
        @finish="handleSearch"
      >
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 140px"
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
            style="width: 240px"
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

    <section class="table-panel">
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
                :title="'IP: ' + record.lastVerifyIp"
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
    </section>

    <a-drawer
      v-model:open="detailVisible"
      :width="520"
      placement="right"
      class="verification-drawer"
    >
      <template #title>
        <div class="drawer-title-block">
          <div class="drawer-kicker">
            Verification Detail
          </div>
          <div class="drawer-title">
            函件验证详情
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
              函件编号
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
              <span>函件类型</span>
              <strong>{{ currentRecord.letterTypeName || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>律师事务所</span>
              <strong>{{ currentRecord.firmName || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>出函律师</span>
              <strong>{{ currentRecord.lawyerNames || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>接收单位</span>
              <strong>{{ currentRecord.targetUnit || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>关联项目</span>
              <strong>{{ currentRecord.matterName || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>验证次数</span>
              <strong>{{ currentRecord.verifyCount }}</strong>
            </div>
            <div class="detail-item">
              <span>审批时间</span>
              <strong>{{ formatDateTime(currentRecord.approvedAt) }}</strong>
            </div>
            <div class="detail-item">
              <span>盖章时间</span>
              <strong>{{ formatDateTime(currentRecord.printedAt) }}</strong>
            </div>
            <div class="detail-item">
              <span>有效期至</span>
              <strong :class="{ expired: isExpired(currentRecord.validUntil) }">
                {{ formatDateTime(currentRecord.validUntil) }}
                {{ isExpired(currentRecord.validUntil) ? '（已过期）' : '' }}
              </strong>
            </div>
            <div class="detail-item">
              <span>最后验证时间</span>
              <strong>{{ formatDateTime(currentRecord.lastVerifyAt) || '-' }}</strong>
            </div>
            <div class="detail-item">
              <span>最后验证IP</span>
              <strong>{{ currentRecord.lastVerifyIp || '-' }}</strong>
            </div>
            <div class="detail-item full">
              <span>备注</span>
              <strong>{{ currentRecord.remark || '-' }}</strong>
            </div>
            <div class="detail-item full">
              <span>创建时间</span>
              <strong>{{ formatDateTime(currentRecord.createdAt) }}</strong>
            </div>
          </div>
        </section>
      </div>
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
  { title: '验证次数', key: 'verifyCount', width: 100, align: 'center' },
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
  display: grid;
  gap: 18px;
}

.stat-card,
.detail-hero,
.detail-panel {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px 22px;
}

.stat-card span {
  display: block;
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-card strong {
  display: block;
  margin-top: 12px;
  font-size: 32px;
  font-family: var(--font-heading);
}

.stat-card p {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.stat-card.success {
  background: linear-gradient(180deg, rgba(82, 196, 26, 0.12), rgba(255, 255, 255, 0.72));
}

.stat-card.warning {
  background: linear-gradient(180deg, rgba(250, 173, 20, 0.12), rgba(255, 255, 255, 0.72));
}

.stat-card.danger {
  background: linear-gradient(180deg, rgba(255, 77, 79, 0.1), rgba(255, 255, 255, 0.72));
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
  color: #b42318;
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
  background: rgba(82, 196, 26, 0.14);
  color: #226d17;
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
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(21, 33, 46, 0.06);
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

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .panel-head {
    display: grid;
  }

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

  .stats-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-hero {
    flex-direction: column;
  }
}
</style>
