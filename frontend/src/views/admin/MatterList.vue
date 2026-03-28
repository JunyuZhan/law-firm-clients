<template>
  <div class="matter-list-container">
    <a-card>
      <template #title>
        <span>项目列表</span>
      </template>
      <template #extra>
        <a-button @click="loadData">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </template>

      <!-- 搜索表单 -->
      <a-form
        :model="searchForm"
        layout="inline"
        style="margin-bottom: 16px"
        @finish="handleSearch"
      >
        <a-form-item label="客户ID">
          <a-input-number
            v-model:value="searchForm.clientId"
            placeholder="请输入客户ID"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
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
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusName(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'accessUrl'">
            <a
              :href="record.accessUrl"
              target="_blank"
              style="word-break: break-all"
            >
              {{ record.accessUrl }}
            </a>
          </template>
          <template v-else-if="column.key === 'expiresAt'">
            <span :style="{ color: isExpired(record.expiresAt) ? '#cf1322' : '' }">
              {{ formatDate(record.expiresAt) }}
            </span>
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
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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

// 表格列定义
const columns = [
  { title: '项目ID', key: 'id', dataIndex: 'id', ellipsis: true, width: 180, align: 'center' },
  { title: '律所项目ID', key: 'lawFirmMatterId', dataIndex: 'lawFirmMatterId', width: 130, align: 'center' },
  { title: '客户ID', key: 'clientId', dataIndex: 'clientId', width: 100, align: 'center' },
  { title: '客户名称', key: 'clientName', dataIndex: 'clientName', ellipsis: true, width: 150, align: 'center' },
  { title: '状态', key: 'status', width: 100, align: 'center' },
  { title: '有效期（天）', key: 'validDays', dataIndex: 'validDays', width: 110, align: 'center' },
  { title: '过期时间', key: 'expiresAt', width: 180, align: 'center' },
  { title: '访问链接', key: 'accessUrl', ellipsis: true, width: 300, align: 'center' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt', width: 180, align: 'center' },
  { title: '操作', key: 'action', width: 120, fixed: 'right', align: 'center' },
]

// 加载数据
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

// 搜索
function handleSearch() {
  pagination.value.current = 1
  loadData()
}

// 重置
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

// 查看项目详情
function handleView(record: MatterListItem) {
  router.push(`/admin/matters/${record.id}`)
}

// 撤销项目
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

// 获取状态名称
// 使用统一的状态工具函数
const getStatusName = getMatterStatusText
const getStatusColor = getMatterStatusColor

// isExpired 和 formatDate 已从 @/utils/date 导入

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.matter-list-container {
  padding: 0;
}

.matter-list-container :deep(.ant-card) {
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.matter-list-container :deep(.ant-card-head) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.matter-list-container :deep(.ant-card-body) {
  padding: 20px;
}

.matter-list-container :deep(.ant-form) {
  margin-bottom: 16px;
}

.matter-list-container :deep(.ant-form-item) {
  margin-bottom: 12px;
}

.matter-list-container :deep(.ant-table) {
  font-size: 14px;
}

/* 固定列背景色 */
/* 表体固定列 */
.matter-list-container :deep(.ant-table-cell-fix-right),
.matter-list-container :deep(.ant-table-cell-fix-left),
.matter-list-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-right),
.matter-list-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-left),
.matter-list-container :deep([class*="ant-table-cell-fix-right"]),
.matter-list-container :deep([class*="ant-table-cell-fix-left"]) {
  background: #fff !important;
  background-color: #fff !important;
  background-image: none !important;
}

/* 表头固定列 */
.matter-list-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-right),
.matter-list-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-left) {
  background: #fafafa !important;
  background-color: #fafafa !important;
  background-image: none !important;
}

/* 表体固定列 hover */
.matter-list-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-right),
.matter-list-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-left),
.matter-list-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-right"]),
.matter-list-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-left"]),
.matter-list-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
.matter-list-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
.matter-list-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-right),
.matter-list-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-left),
.matter-list-container :deep(tr:hover td.ant-table-cell-fix-right),
.matter-list-container :deep(tr:hover td.ant-table-cell-fix-left),
.matter-list-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-right"]),
.matter-list-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-left"]),
.matter-list-container :deep(tr:hover [class*="ant-table-cell-fix-right"]),
.matter-list-container :deep(tr:hover [class*="ant-table-cell-fix-left"]) {
  background: var(--accent-color-lighter, #fffbf0) !important;
  background-color: var(--accent-color-lighter, #fffbf0) !important;
  background-image: none !important;
}

.matter-list-container :deep(.ant-table-thead > tr > th) {
  background: var(--bg-tertiary);
  font-weight: 600;
  padding: 12px 8px;
}

.matter-list-container :deep(.ant-table-tbody > tr > td) {
  padding: 12px 8px;
}

.matter-list-container :deep(.ant-btn) {
  height: 32px;
  padding: 0 12px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .matter-list-container {
    padding: 0;
  }
  
  .matter-list-container :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .matter-list-container :deep(.ant-card-body) {
    padding: 16px 12px;
  }
  
  .matter-list-container :deep(.ant-form) {
    flex-direction: column;
  }
  
  .matter-list-container :deep(.ant-form-item) {
    margin-bottom: 12px;
    width: 100%;
  }
  
  .matter-list-container :deep(.ant-form-item-label) {
    padding-bottom: 4px;
  }
  
  .matter-list-container :deep(.ant-input-number),
  .matter-list-container :deep(.ant-select),
  .matter-list-container :deep(.ant-picker) {
    width: 100% !important;
  }
  
  .matter-list-container :deep(.ant-table) {
    font-size: 12px;
  }
  
  .matter-list-container :deep(.ant-table-thead > tr > th) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .matter-list-container :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .matter-list-container :deep(.ant-table-scroll) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .matter-list-container :deep(.ant-btn) {
    height: 28px;
    padding: 0 8px;
    font-size: 12px;
  }
  
  .matter-list-container :deep(.ant-tag) {
    font-size: 11px;
    padding: 2px 6px;
  }
  
  .matter-list-container :deep(.ant-space) {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .matter-list-container :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .matter-list-container :deep(.ant-card-body) {
    padding: 12px 8px;
  }
  
  .matter-list-container :deep(.ant-table-thead > tr > th),
  .matter-list-container :deep(.ant-table-tbody > tr > td) {
    padding: 6px 2px;
    font-size: 10px;
  }
}
</style>
