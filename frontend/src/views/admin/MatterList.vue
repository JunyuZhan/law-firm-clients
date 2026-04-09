<template>
  <div class="matter-list-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          查看状态、有效期与访问链接；支持筛选与刷新。
        </p>
      </div>
      <a-button @click="loadData">
        <template #icon>
          <ReloadOutlined />
        </template>
        刷新数据
      </a-button>
    </section>

    <section class="stats-grid">
      <article class="stats-card">
        <span class="stats-label">总项目数</span>
        <strong>{{ summaryStats.total }}</strong>
      </article>
      <article class="stats-card success">
        <span class="stats-label">有效访问</span>
        <strong>{{ summaryStats.active }}</strong>
      </article>
      <article class="stats-card warning">
        <span class="stats-label">已过期</span>
        <strong>{{ summaryStats.expired }}</strong>
      </article>
      <article class="stats-card danger">
        <span class="stats-label">已撤销</span>
        <strong>{{ summaryStats.revoked }}</strong>
      </article>
    </section>

    <section class="filter-panel">
      <div class="panel-head">
        <h3>筛选</h3>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="matter-filter-form"
        @finish="handleSearch"
      >
        <a-form-item label="客户ID">
          <a-input-number
            v-model:value="searchForm.clientId"
            placeholder="请输入客户ID"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="ACTIVE">
              有效
            </a-select-option>
            <a-select-option value="EXPIRED">
              已过期
            </a-select-option>
            <a-select-option value="REVOKED">
              已撤销
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

    <section class="table-panel">
      <div class="panel-head panel-head--table">
        <h3>项目列表</h3>
      </div>

      <div class="table-summary dashboard-table-summary">
        <span>共 {{ dataSource.length }} 条</span>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1080 }"
        row-key="id"
        size="small"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusName(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'accessUrl'">
            <a
              :href="record.accessUrl"
              target="_blank"
              class="matter-link"
            >
              {{ record.accessUrl }}
            </a>
          </template>
          <template v-else-if="column.key === 'expiresAt'">
            <span :class="{ expired: isExpired(record.expiresAt) }">
              {{ formatDate(record.expiresAt) }}
            </span>
          </template>
          <template v-else-if="column.key === 'clientName'">
            <span class="client-name">{{ record.clientName }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleView(record)"
              >
                查看
              </a-button>
              <a-popconfirm
                v-if="record.status === 'ACTIVE'"
                title="确定要撤销这个项目的访问吗？"
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { getMatterList, revokeMatter, type MatterListItem } from '@/api/matter'
import { formatDate, isExpired } from '@/utils/date'
import { getMatterStatusColor, getMatterStatusText } from '@/utils/status'
import type { Dayjs } from 'dayjs'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<MatterListItem[]>([])
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

const searchForm = ref({
  clientId: undefined as number | undefined,
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

const columns = [
  { title: '项目ID', key: 'id', dataIndex: 'id', ellipsis: true, width: 160, align: 'center' },
  { title: '客户名称', key: 'clientName', dataIndex: 'clientName', ellipsis: true, width: 140, align: 'center' },
  { title: '状态', key: 'status', width: 96, align: 'center' },
  { title: '有效期（天）', key: 'validDays', dataIndex: 'validDays', width: 104, align: 'center' },
  { title: '过期时间', key: 'expiresAt', width: 160, align: 'center' },
  { title: '访问链接', key: 'accessUrl', ellipsis: true, width: 260, align: 'center' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt', width: 160, align: 'center' },
  { title: '操作', key: 'action', width: 108, align: 'center' },
]

const summaryStats = computed(() => {
  const items = dataSource.value
  return {
    total: items.length,
    active: items.filter(item => item.status === 'ACTIVE').length,
    expired: items.filter(item => item.status === 'EXPIRED').length,
    revoked: items.filter(item => item.status === 'REVOKED').length,
  }
})

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (searchForm.value.clientId) params.clientId = searchForm.value.clientId
    if (searchForm.value.status) params.status = searchForm.value.status
    if (searchForm.value.startTime) params.startTime = searchForm.value.startTime
    if (searchForm.value.endTime) params.endTime = searchForm.value.endTime
    if (searchForm.value.limit) params.limit = searchForm.value.limit

    const res = await getMatterList(params)
    dataSource.value = res.data || []
    pagination.value.total = dataSource.value.length
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '加载项目列表失败'
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  searchForm.value = {
    clientId: undefined,
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

function handleView(record: MatterListItem) {
  router.push(`/admin/matters/${record.id}`)
}

async function handleRevoke(record: MatterListItem) {
  try {
    await revokeMatter(record.id)
    message.success('项目访问已撤销')
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '撤销项目失败'
    message.error(errorMessage)
  }
}

const getStatusName = getMatterStatusText
const getStatusColor = getMatterStatusColor

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.matter-list-container {
  display: grid;
  gap: 16px;
}

.stats-card.success strong {
  color: var(--success-color);
}

.stats-card.warning strong {
  color: var(--warning-color);
}

.stats-card.danger strong {
  color: var(--error-color);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
}

.panel-head h3 {
  margin: 0;
  font-size: 17px;
  color: var(--lex-primary);
}

.panel-head p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.panel-head--table {
  margin-bottom: 16px;
}

.matter-filter-form {
  display: flex;
  gap: 12px 8px;
}

.matter-filter-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  margin-left: auto;
}

.matter-link {
  display: inline-block;
  max-width: 100%;
  word-break: break-all;
}

.client-name {
  font-weight: 600;
  color: var(--text-primary);
}

.expired {
  color: var(--error-color);
}

@media (max-width: 768px) {
  .panel-head {
    display: grid;
  }

  .matter-filter-form {
    display: grid;
    gap: 12px;
  }

  .matter-filter-form :deep(.ant-form-item) {
    width: 100%;
  }

  .matter-filter-form :deep(.ant-input-number),
  .matter-filter-form :deep(.ant-select),
  .matter-filter-form :deep(.ant-picker) {
    width: 100% !important;
  }

  .filter-actions {
    margin-left: 0;
  }
}
</style>
