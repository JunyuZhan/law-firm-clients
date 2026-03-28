<template>
  <div class="file-management">
    <!-- 统计卡片 -->
    <a-row
      :gutter="16"
      style="margin-bottom: 16px"
    >
      <a-col
        :xs="12"
        :sm="6"
      >
        <a-card size="small">
          <a-statistic
            title="总文件数"
            :value="statistics.totalCount"
            :loading="statsLoading"
          >
            <template #prefix>
              <FileOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col
        :xs="12"
        :sm="6"
      >
        <a-card size="small">
          <a-statistic
            title="活跃文件"
            :value="statistics.activeCount"
            :loading="statsLoading"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col
        :xs="12"
        :sm="6"
      >
        <a-card size="small">
          <a-statistic
            title="已删除"
            :value="statistics.deletedCount"
            :loading="statsLoading"
            :value-style="{ color: '#ff4d4f' }"
          />
        </a-card>
      </a-col>
      <a-col
        :xs="12"
        :sm="6"
      >
        <a-card size="small">
          <a-statistic
            title="存储空间"
            :value="statistics.activeSizeFormatted"
            :loading="statsLoading"
          >
            <template #prefix>
              <CloudServerOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 文件列表 -->
    <a-card size="small">
      <template #title>
        <span>文件列表</span>
      </template>
      <template #extra>
        <a-space
          class="action-buttons"
          :size="8"
        >
          <a-button 
            danger 
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            <template #icon>
              <DeleteOutlined />
            </template>
            <span class="btn-text">批量删除</span>
          </a-button>
          <a-button @click="handleCleanup">
            <template #icon>
              <ClearOutlined />
            </template>
            <span class="btn-text">清理过期文件</span>
          </a-button>
        </a-space>
      </template>

      <!-- 筛选条件 -->
      <a-form
        layout="inline"
        class="filter-form"
        style="margin-bottom: 16px"
      >
        <a-form-item label="项目ID">
          <a-input
            v-model:value="filters.matterId"
            placeholder="输入项目ID"
            allow-clear
            style="width: 160px"
            @press-enter="loadFiles"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="filters.status"
            placeholder="选择状态"
            allow-clear
            style="width: 120px"
            @change="loadFiles"
          >
            <a-select-option value="ACTIVE">
              活跃
            </a-select-option>
            <a-select-option value="DELETED">
              已删除
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="类别">
          <a-select
            v-model:value="filters.fileCategory"
            placeholder="选择类别"
            allow-clear
            style="width: 120px"
            @change="loadFiles"
          >
            <a-select-option value="EVIDENCE">
              证据材料
            </a-select-option>
            <a-select-option value="CONTRACT">
              合同文件
            </a-select-option>
            <a-select-option value="ID_CARD">
              身份证件
            </a-select-option>
            <a-select-option value="OTHER">
              其他
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键字">
          <a-input
            v-model:value="filters.keyword"
            placeholder="文件名/描述"
            allow-clear
            style="width: 160px"
            @press-enter="loadFiles"
          />
        </a-form-item>
        <a-form-item>
          <a-button
            type="primary"
            @click="loadFiles"
          >
            <template #icon>
              <SearchOutlined />
            </template>
            搜索
          </a-button>
        </a-form-item>
      </a-form>

      <!-- 文件表格 -->
      <a-table
        :columns="columns"
        :data-source="files"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        :scroll="{ x: 'max-content' }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'fileName'">
            <a-tooltip :title="record.fileName">
              <span class="file-name">{{ record.fileName }}</span>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'fileSize'">
            {{ formatFileSize(record.fileSize) }}
          </template>
          <template v-else-if="column.key === 'fileCategory'">
            <a-tag :color="getCategoryColor(record.fileCategory)">
              {{ getCategoryLabel(record.fileCategory) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
              {{ record.status === 'ACTIVE' ? '活跃' : '已删除' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'uploadedAt'">
            {{ formatDate(record.uploadedAt) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space
              class="table-action-buttons"
              :size="8"
            >
              <a-button 
                type="link" 
                size="small"
                @click="showDetail(record)"
              >
                详情
              </a-button>
              <a-popconfirm
                title="确定删除此文件？"
                @confirm="handleDelete(record.id)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                >
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 文件详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="文件详情"
      :footer="null"
      :width="modalWidth"
      wrap-class-name="file-detail-modal"
    >
      <a-descriptions
        v-if="currentFile"
        :column="1"
        bordered
        size="small"
      >
        <a-descriptions-item label="文件ID">
          {{ currentFile.id }}
        </a-descriptions-item>
        <a-descriptions-item label="文件名">
          {{ currentFile.fileName }}
        </a-descriptions-item>
        <a-descriptions-item label="项目ID">
          {{ currentFile.matterId }}
        </a-descriptions-item>
        <a-descriptions-item label="文件大小">
          {{ formatFileSize(currentFile.fileSize) }}
        </a-descriptions-item>
        <a-descriptions-item label="文件类型">
          {{ currentFile.fileType }}
        </a-descriptions-item>
        <a-descriptions-item label="文件类别">
          <a-tag :color="getCategoryColor(currentFile.fileCategory)">
            {{ getCategoryLabel(currentFile.fileCategory) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="存储路径">
          <code>{{ currentFile.storagePath }}</code>
        </a-descriptions-item>
        <a-descriptions-item label="描述">
          {{ currentFile.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="上传时间">
          {{ formatDate(currentFile.uploadedAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentFile.status === 'ACTIVE' ? 'green' : 'red'">
            {{ currentFile.status === 'ACTIVE' ? '活跃' : '已删除' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 清理确认弹窗 -->
    <a-modal
      v-model:open="cleanupVisible"
      title="清理过期文件"
      :confirm-loading="cleanupLoading"
      :width="modalWidth"
      wrap-class-name="cleanup-modal"
      @ok="confirmCleanup"
    >
      <a-form layout="vertical">
        <a-form-item label="清理范围">
          <a-input-number
            v-model:value="cleanupDays"
            :min="1"
            :max="365"
            style="width: 100%"
          />
          <div style="margin-top: 4px; color: #999; font-size: 12px">
            清理 {{ cleanupDays }} 天前标记为删除的文件（物理删除）
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  FileOutlined,
  CloudServerOutlined,
  DeleteOutlined,
  ClearOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import request from '@/api/request'
import type { TableProps } from 'ant-design-vue'
import logger from '@/utils/logger'

interface FileItem {
  id: string
  matterId: string
  clientId: number
  fileName: string
  fileSize: number
  fileType: string
  fileCategory: string
  description: string
  storagePath: string
  uploadedAt: string
  status: string
}

interface Statistics {
  totalCount: number
  activeCount: number
  deletedCount: number
  totalSize: number
  totalSizeFormatted: string
  activeSize: number
  activeSizeFormatted: string
  matterCount: number
  categoryStats: Record<string, number>
}

// 统计数据
const statistics = reactive<Statistics>({
  totalCount: 0,
  activeCount: 0,
  deletedCount: 0,
  totalSize: 0,
  totalSizeFormatted: '0 B',
  activeSize: 0,
  activeSizeFormatted: '0 B',
  matterCount: 0,
  categoryStats: {},
})
const statsLoading = ref(false)

// 文件列表
const files = ref<FileItem[]>([])
const loading = ref(false)
const filters = reactive({
  matterId: '',
  status: undefined as string | undefined,
  fileCategory: undefined as string | undefined,
  keyword: '',
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 选择
const selectedRowKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

// 详情弹窗
const detailVisible = ref(false)
const currentFile = ref<FileItem | null>(null)

// 清理弹窗
const cleanupVisible = ref(false)
const cleanupDays = ref(30)
const cleanupLoading = ref(false)

// 响应式宽度
const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => {
  return windowWidth.value < 768 ? '95%' : '600px'
})

// 监听窗口大小变化
function handleResize() {
  windowWidth.value = window.innerWidth
}

// 表格列
const columns: TableProps['columns'] = [
  { title: '文件名', dataIndex: 'fileName', key: 'fileName', ellipsis: true },
  { title: '项目ID', dataIndex: 'matterId', key: 'matterId', width: 140, ellipsis: true },
  { title: '大小', dataIndex: 'fileSize', key: 'fileSize', width: 100 },
  { title: '类别', dataIndex: 'fileCategory', key: 'fileCategory', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '上传时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 160 },
  { title: '操作', key: 'action', width: 220, fixed: 'right', align: 'center' },
]

// 加载统计数据
const loadStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/api/admin/files/statistics')
    Object.assign(statistics, res.data as Partial<Statistics>)
  } catch (error) {
    logger.error('加载统计数据失败', error)
  } finally {
    statsLoading.value = false
  }
}

// 加载文件列表
const loadFiles = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/files', {
      params: {
        matterId: filters.matterId || undefined,
        status: filters.status,
        fileCategory: filters.fileCategory,
        keyword: filters.keyword || undefined,
        page: pagination.current,
        pageSize: pagination.pageSize,
      },
    })
    files.value = (res.data as { list?: FileItem[]; total?: number }).list || []
    pagination.total = (res.data as { list?: FileItem[]; total?: number }).total || 0
  } catch (error) {
    logger.error('加载文件列表失败', error)
  } finally {
    loading.value = false
  }
}

// 表格变化
const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 20
  loadFiles()
}

// 显示详情
const showDetail = (record: FileItem) => {
  currentFile.value = record
  detailVisible.value = true
}

// 删除文件
const handleDelete = async (fileId: string) => {
  try {
    await request.delete(`/api/admin/files/${fileId}`)
    message.success('删除成功')
    loadFiles()
    loadStatistics()
  } catch (error) {
    message.error('删除失败')
  }
}

// 批量删除（带二次确认）
const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个文件吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: doBatchDelete,
  })
}

const doBatchDelete = async () => {
  try {
    const res = await request.delete('/api/admin/files/batch', {
      data: selectedRowKeys.value,
    })
    message.success(`成功删除 ${(res.data as { successCount?: number }).successCount || 0} 个文件`)
    selectedRowKeys.value = []
    loadFiles()
    loadStatistics()
  } catch (error) {
    message.error('批量删除失败')
  }
}

// 清理过期文件
const handleCleanup = () => {
  cleanupVisible.value = true
}

const confirmCleanup = async () => {
  cleanupLoading.value = true
  try {
    const res = await request.post('/api/admin/files/cleanup', null, {
      params: { days: cleanupDays.value },
    })
    message.success(`成功清理 ${(res.data as { cleanedCount?: number }).cleanedCount || 0} 个文件`)
    cleanupVisible.value = false
    loadFiles()
    loadStatistics()
  } catch (error) {
    message.error('清理失败')
  } finally {
    cleanupLoading.value = false
  }
}

// 工具函数
const formatFileSize = (size: number) => {
  if (!size) return '0 B'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const getCategoryLabel = (category: string) => {
  const labels: Record<string, string> = {
    EVIDENCE: '证据材料',
    CONTRACT: '合同文件',
    ID_CARD: '身份证件',
    OTHER: '其他',
  }
  return labels[category] || category
}

const getCategoryColor = (category: string) => {
  const colors: Record<string, string> = {
    EVIDENCE: 'blue',
    CONTRACT: 'green',
    ID_CARD: 'orange',
    OTHER: 'default',
  }
  return colors[category] || 'default'
}

onMounted(() => {
  loadStatistics()
  loadFiles()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.file-management {
  padding: 0;
}

/* 确保操作列按钮可见 - 操作列是固定列 */
.file-management :deep(.ant-table-cell-fix-right) {
  min-width: 150px !important;
}

.file-management :deep(.ant-table-cell-fix-right .ant-space) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  flex-wrap: nowrap !important;
  gap: 8px !important;
}

.file-management :deep(.ant-table-cell-fix-right .ant-btn) {
  display: inline-block !important;
  visibility: visible !important;
  opacity: 1 !important;
  flex-shrink: 0 !important;
  white-space: nowrap !important;
}

.file-name {
  max-width: 200px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-text {
  margin-left: 4px;
}

.action-buttons {
  flex-wrap: wrap;
}

.table-action-buttons {
  flex-wrap: nowrap;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .file-management {
    padding: 0;
  }
  
  .file-management :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .file-management :deep(.ant-card-head-title) {
    font-size: 14px;
  }
  
  .file-management :deep(.ant-card-body) {
    padding: 16px;
  }
  
  .file-management :deep(.ant-card-extra) {
    margin-left: 0;
    margin-top: 8px;
  }
  
  .action-buttons {
    flex-direction: column;
    width: 100%;
  }
  
  .action-buttons :deep(.ant-space-item) {
    width: 100%;
  }
  
  .action-buttons :deep(.ant-btn) {
    width: 100%;
    justify-content: center;
  }
  
  .btn-text {
    display: inline;
  }
  
  .filter-form {
    flex-direction: column;
  }
  
  .filter-form :deep(.ant-form-item) {
    margin-bottom: 12px;
    width: 100%;
  }
  
  .filter-form :deep(.ant-form-item-control) {
    width: 100%;
  }
  
  .filter-form :deep(.ant-input),
  .filter-form :deep(.ant-select) {
    width: 100% !important;
  }
  
  .file-management :deep(.ant-table) {
    font-size: 12px;
  }
  
  .file-management :deep(.ant-table-thead > tr > th) {
    padding: 8px 4px;
    font-size: 12px;
  }
  
  .file-management :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
  }
  
  .file-management :deep(.ant-statistic-title) {
    font-size: 12px;
  }
  
  .file-management :deep(.ant-statistic-content) {
    font-size: 18px;
  }
  
  .file-management :deep(.ant-row) {
    margin-left: -8px !important;
    margin-right: -8px !important;
  }
  
  .file-management :deep(.ant-col) {
    padding-left: 8px !important;
    padding-right: 8px !important;
  }
}

@media (max-width: 480px) {
  .file-management :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .file-management :deep(.ant-card-body) {
    padding: 12px;
  }
  
  .file-management :deep(.ant-table-thead > tr > th),
  .file-management :deep(.ant-table-tbody > tr > td) {
    padding: 6px 2px;
    font-size: 11px;
  }
  
  .file-management :deep(.ant-statistic-content) {
    font-size: 16px;
  }
  
  .btn-text {
    display: none; /* 超小屏隐藏按钮文字 */
  }
  
  .table-action-buttons {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .table-action-buttons :deep(.ant-space-item) {
    width: 100%;
  }
  
  .table-action-buttons :deep(.ant-btn) {
    width: 100%;
    text-align: left;
    padding-left: 0;
  }
}

/* 文件详情弹窗移动端优化 */
:deep(.file-detail-modal .ant-modal) {
  margin: 0;
  max-width: 100vw;
  top: 0;
  padding-bottom: 0;
}

:deep(.file-detail-modal .ant-modal-content) {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

:deep(.file-detail-modal .ant-modal-body) {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

@media (min-width: 769px) {
  :deep(.file-detail-modal .ant-modal) {
    margin: 0 auto;
    top: 50px;
    padding-bottom: 24px;
  }
  
  :deep(.file-detail-modal .ant-modal-content) {
    height: auto;
  }
  
  :deep(.file-detail-modal .ant-modal-body) {
    padding: 24px;
  }
}

/* 清理弹窗移动端优化 */
:deep(.cleanup-modal .ant-modal) {
  margin: 0;
  max-width: 100vw;
  top: 0;
  padding-bottom: 0;
}

:deep(.cleanup-modal .ant-modal-content) {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

:deep(.cleanup-modal .ant-modal-body) {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

:deep(.cleanup-modal .ant-modal-footer) {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

@media (min-width: 769px) {
  :deep(.cleanup-modal .ant-modal) {
    margin: 0 auto;
    top: 50px;
    padding-bottom: 24px;
  }
  
  :deep(.cleanup-modal .ant-modal-content) {
    height: auto;
  }
  
  :deep(.cleanup-modal .ant-modal-body) {
    padding: 24px;
  }
  
  :deep(.cleanup-modal .ant-modal-footer) {
    padding: 10px 16px;
  }
}

/* 固定列背景色 - 最强覆盖 */
.file-management :deep(.ant-table-cell-fix-right),
.file-management :deep(.ant-table-cell-fix-left),
.file-management :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-right),
.file-management :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-left),
.file-management :deep(td.ant-table-cell-fix-right),
.file-management :deep(td.ant-table-cell-fix-left),
.file-management :deep([class*="ant-table-cell-fix-right"]),
.file-management :deep([class*="ant-table-cell-fix-left"]) {
  background: #fff !important;
  background-color: #fff !important;
  background-image: none !important;
}

.file-management :deep(.ant-table-thead > tr > th.ant-table-cell-fix-right),
.file-management :deep(.ant-table-thead > tr > th.ant-table-cell-fix-left),
.file-management :deep(th.ant-table-cell-fix-right),
.file-management :deep(th.ant-table-cell-fix-left) {
  background: #fafafa !important;
  background-color: #fafafa !important;
  background-image: none !important;
}

.file-management :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-right),
.file-management :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-left),
.file-management :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-right"]),
.file-management :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-left"]),
.file-management :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
.file-management :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
.file-management :deep(tr.ant-table-row:hover td.ant-table-cell-fix-right),
.file-management :deep(tr.ant-table-row:hover td.ant-table-cell-fix-left),
.file-management :deep(tr:hover td.ant-table-cell-fix-right),
.file-management :deep(tr:hover td.ant-table-cell-fix-left),
.file-management :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-right"]),
.file-management :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-left"]),
.file-management :deep(tr:hover [class*="ant-table-cell-fix-right"]),
.file-management :deep(tr:hover [class*="ant-table-cell-fix-left"]) {
  background: var(--accent-color-lighter, #fffbf0) !important;
  background-color: var(--accent-color-lighter, #fffbf0) !important;
  background-image: none !important;
}
</style>
