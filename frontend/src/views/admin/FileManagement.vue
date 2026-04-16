<template>
  <div class="file-management">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_FILE_MANAGEMENT_TEXTS.intro }}
        </p>
      </div>
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
          <span class="btn-text">{{ ADMIN_FILE_MANAGEMENT_TEXTS.actions.batchRemove }}</span>
        </a-button>
        <a-button @click="handleCleanup">
          <template #icon>
            <ClearOutlined />
          </template>
          <span class="btn-text">{{ ADMIN_FILE_MANAGEMENT_TEXTS.actions.cleanupExpired }}</span>
        </a-button>
      </a-space>
    </section>

    <section class="stats-grid">
      <article class="stats-card">
        <span class="stats-label">{{ ADMIN_FILE_MANAGEMENT_TEXTS.stats.total }}</span>
        <strong>{{ statistics.totalCount }}</strong>
      </article>
      <article class="stats-card success">
        <span class="stats-label">{{ ADMIN_FILE_MANAGEMENT_TEXTS.stats.active }}</span>
        <strong>{{ statistics.activeCount }}</strong>
      </article>
      <article class="stats-card danger">
        <span class="stats-label">{{ ADMIN_FILE_MANAGEMENT_TEXTS.stats.deleted }}</span>
        <strong>{{ statistics.deletedCount }}</strong>
      </article>
      <article class="stats-card info">
        <span class="stats-label">{{ ADMIN_FILE_MANAGEMENT_TEXTS.stats.storage }}</span>
        <strong>{{ statistics.activeSizeFormatted }}</strong>
      </article>
    </section>

    <section class="filter-panel">
      <div class="panel-head dashboard-panel-head">
        <h3>{{ ADMIN_FILE_MANAGEMENT_TEXTS.filter.title }}</h3>
      </div>

      <a-form
        layout="inline"
        class="filter-form"
      >
        <a-form-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.filter.matterIdLabel">
          <a-input
            v-model:value="filters.matterId"
            :placeholder="ADMIN_FILE_MANAGEMENT_TEXTS.filter.matterIdPlaceholder"
            allow-clear
            style="width: 180px"
            @press-enter="loadFiles"
          />
        </a-form-item>
        <a-form-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.filter.statusLabel">
          <a-select
            v-model:value="filters.status"
            :placeholder="ADMIN_FILE_MANAGEMENT_TEXTS.filter.statusPlaceholder"
            allow-clear
            style="width: 130px"
            @change="loadFiles"
          >
            <a-select-option value="ACTIVE">
              {{ ADMIN_FILE_MANAGEMENT_TEXTS.statusOptions.active }}
            </a-select-option>
            <a-select-option value="DELETED">
              {{ ADMIN_FILE_MANAGEMENT_TEXTS.statusOptions.deleted }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.filter.categoryLabel">
          <a-select
            v-model:value="filters.fileCategory"
            :placeholder="ADMIN_FILE_MANAGEMENT_TEXTS.filter.categoryPlaceholder"
            allow-clear
            style="width: 150px"
            @change="loadFiles"
          >
            <a-select-option value="EVIDENCE">
              {{ ADMIN_FILE_MANAGEMENT_TEXTS.categories.evidence }}
            </a-select-option>
            <a-select-option value="CONTRACT">
              {{ ADMIN_FILE_MANAGEMENT_TEXTS.categories.contract }}
            </a-select-option>
            <a-select-option value="ID_CARD">
              {{ ADMIN_FILE_MANAGEMENT_TEXTS.categories.idCard }}
            </a-select-option>
            <a-select-option value="OTHER">
              {{ ADMIN_FILE_MANAGEMENT_TEXTS.categories.other }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.filter.keywordLabel">
          <a-input
            v-model:value="filters.keyword"
            :placeholder="ADMIN_FILE_MANAGEMENT_TEXTS.filter.keywordPlaceholder"
            allow-clear
            style="width: 200px"
            @press-enter="loadFiles"
          />
        </a-form-item>
        <a-form-item class="filter-actions">
          <a-button
            type="primary"
            @click="loadFiles"
          >
            <template #icon>
              <SearchOutlined />
            </template>
            {{ UI_TEXTS.search }}
          </a-button>
        </a-form-item>
      </a-form>
    </section>

    <section class="table-panel">
      <div class="panel-head panel-head--table dashboard-panel-head dashboard-panel-head--table">
        <h3>{{ ADMIN_FILE_MANAGEMENT_TEXTS.table.title }}</h3>
      </div>

      <div class="table-summary dashboard-table-summary">
        <span>{{ ADMIN_FILE_MANAGEMENT_TEXTS.filter.totalPrefix }}{{ files.length }}{{ ADMIN_FILE_MANAGEMENT_TEXTS.filter.totalSuffix }}</span>
        <span v-if="selectedRowKeys.length">{{ ADMIN_FILE_MANAGEMENT_TEXTS.filter.selectedPrefix }}{{ selectedRowKeys.length }}{{ ADMIN_FILE_MANAGEMENT_TEXTS.filter.selectedSuffix }}</span>
      </div>

      <a-table
        :columns="columns"
        :data-source="files"
        :loading="loading || statsLoading"
        :pagination="pagination"
        :row-selection="rowSelection"
        :scroll="{ x: 920 }"
        row-key="id"
        size="small"
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
                {{ ADMIN_FILE_MANAGEMENT_TEXTS.actions.detail }}
              </a-button>
              <a-popconfirm
                :title="UI_CONFIRM_TEXTS.removeFile"
                @confirm="handleDelete(record.id)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                >
                  {{ UI_TEXTS.remove }}
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <a-modal
      v-model:open="detailVisible"
      :title="ADMIN_FILE_MANAGEMENT_TEXTS.detail.title"
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
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.fileId">
          {{ currentFile.id }}
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.fileName">
          {{ currentFile.fileName }}
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.matterId">
          {{ currentFile.matterId }}
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.fileSize">
          {{ formatFileSize(currentFile.fileSize) }}
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.fileType">
          {{ currentFile.fileType }}
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.fileCategory">
          <a-tag :color="getCategoryColor(currentFile.fileCategory)">
            {{ getCategoryLabel(currentFile.fileCategory) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.storagePath">
          <code>{{ currentFile.storagePath }}</code>
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.description">
          {{ currentFile.description || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.uploadedAt">
          {{ formatDate(currentFile.uploadedAt) }}
        </a-descriptions-item>
        <a-descriptions-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.detail.status">
          <a-tag :color="currentFile.status === 'ACTIVE' ? 'green' : 'red'">
            {{ currentFile.status === 'ACTIVE' ? '活跃' : '已删除' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <a-modal
      v-model:open="cleanupVisible"
      :title="ADMIN_FILE_MANAGEMENT_TEXTS.actions.cleanupTitle"
      :confirm-loading="cleanupLoading"
      :width="modalWidth"
      wrap-class-name="cleanup-modal"
      @ok="confirmCleanup"
    >
      <a-form layout="vertical">
        <a-form-item :label="ADMIN_FILE_MANAGEMENT_TEXTS.actions.cleanupRange">
          <a-input-number
            v-model:value="cleanupDays"
            :min="1"
            :max="365"
            style="width: 100%"
          />
          <div class="cleanup-help">
            {{ ADMIN_FILE_MANAGEMENT_TEXTS.actions.cleanupHelpPrefix }}{{ cleanupDays }}{{ ADMIN_FILE_MANAGEMENT_TEXTS.actions.cleanupHelpSuffix }}
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
  DeleteOutlined,
  ClearOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import request from '@/api/request'
import type { TableProps } from 'ant-design-vue'
import logger from '@/utils/logger'
import { UI_CONFIRM_TEXTS, UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_FILE_MANAGEMENT_TEXTS } from '@/constants/adminTexts'

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
const files = ref<FileItem[]>([])
const loading = ref(false)
const filters = reactive({
  matterId: '',
  status: undefined as string | undefined,
  fileCategory: undefined as string | undefined,
  keyword: '',
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `${ADMIN_FILE_MANAGEMENT_TEXTS.filter.totalPrefix}${total}${ADMIN_FILE_MANAGEMENT_TEXTS.filter.totalSuffix}`,
})

const selectedRowKeys = ref<string[]>([])
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

const detailVisible = ref(false)
const currentFile = ref<FileItem | null>(null)

const cleanupVisible = ref(false)
const cleanupDays = ref(30)
const cleanupLoading = ref(false)

const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => {
  return windowWidth.value < 768 ? '95%' : '600px'
})

function handleResize() {
  windowWidth.value = window.innerWidth
}

const columns: TableProps['columns'] = [
  { title: ADMIN_FILE_MANAGEMENT_TEXTS.table.fileName, dataIndex: 'fileName', key: 'fileName', ellipsis: true },
  { title: ADMIN_FILE_MANAGEMENT_TEXTS.table.matterId, dataIndex: 'matterId', key: 'matterId', width: 132, ellipsis: true },
  { title: ADMIN_FILE_MANAGEMENT_TEXTS.table.size, dataIndex: 'fileSize', key: 'fileSize', width: 96 },
  { title: ADMIN_FILE_MANAGEMENT_TEXTS.table.category, dataIndex: 'fileCategory', key: 'fileCategory', width: 96 },
  { title: ADMIN_FILE_MANAGEMENT_TEXTS.table.status, dataIndex: 'status', key: 'status', width: 84 },
  { title: ADMIN_FILE_MANAGEMENT_TEXTS.table.uploadedAt, dataIndex: 'uploadedAt', key: 'uploadedAt', width: 148 },
  { title: ADMIN_FILE_MANAGEMENT_TEXTS.table.action, key: 'action', width: 140, align: 'center' },
]

const loadStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await request.get('/api/admin/files/statistics')
    Object.assign(statistics, res.data as Partial<Statistics>)
  } catch (error) {
    logger.error(ADMIN_FILE_MANAGEMENT_TEXTS.feedback.statsLoadFailed, error)
  } finally {
    statsLoading.value = false
  }
}

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
    logger.error(ADMIN_FILE_MANAGEMENT_TEXTS.feedback.listLoadFailed, error)
  } finally {
    loading.value = false
  }
}

const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 20
  loadFiles()
}

const showDetail = (record: FileItem) => {
  currentFile.value = record
  detailVisible.value = true
}

const handleDelete = async (fileId: string) => {
  try {
    await request.delete(`/api/admin/files/${fileId}`)
    message.success(UI_FEEDBACK_TEXTS.fileRemoved)
    loadFiles()
    loadStatistics()
  } catch {
    message.error(UI_FEEDBACK_TEXTS.fileRemoveFailed)
  }
}

const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: UI_CONFIRM_TEXTS.batchRemoveFilesTitle,
    content: `${ADMIN_FILE_MANAGEMENT_TEXTS.confirm.batchRemoveContentPrefix}${selectedRowKeys.value.length}${ADMIN_FILE_MANAGEMENT_TEXTS.confirm.batchRemoveContentMiddle}`,
    okText: UI_TEXTS.remove,
    okType: 'danger',
    cancelText: ADMIN_FILE_MANAGEMENT_TEXTS.confirm.cancel,
    onOk: doBatchDelete,
  })
}

const doBatchDelete = async () => {
  try {
    const res = await request.delete('/api/admin/files/batch', {
      data: selectedRowKeys.value,
    })
    message.success(`${ADMIN_FILE_MANAGEMENT_TEXTS.feedback.batchRemovedPrefix}${(res.data as { successCount?: number }).successCount || 0}${ADMIN_FILE_MANAGEMENT_TEXTS.feedback.batchRemovedSuffix}`)
    selectedRowKeys.value = []
    loadFiles()
    loadStatistics()
  } catch {
    message.error(UI_FEEDBACK_TEXTS.batchRemoveFailed)
  }
}

const handleCleanup = () => {
  cleanupVisible.value = true
}

const confirmCleanup = async () => {
  cleanupLoading.value = true
  try {
    const res = await request.post('/api/admin/files/cleanup', null, {
      params: { days: cleanupDays.value },
    })
    message.success(`${ADMIN_FILE_MANAGEMENT_TEXTS.feedback.cleanedPrefix}${(res.data as { cleanedCount?: number }).cleanedCount || 0}${ADMIN_FILE_MANAGEMENT_TEXTS.feedback.cleanedSuffix}`)
    cleanupVisible.value = false
    loadFiles()
    loadStatistics()
  } catch {
    message.error(UI_FEEDBACK_TEXTS.cleanupFailed)
  } finally {
    cleanupLoading.value = false
  }
}

const formatFileSize = (size: number) => {
  if (!size) return '0 B'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / (1024 * 1024)).toFixed(2)} MB`
  return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const getCategoryLabel = (category: string) => {
  const labels: Record<string, string> = {
    EVIDENCE: ADMIN_FILE_MANAGEMENT_TEXTS.categories.evidence,
    CONTRACT: ADMIN_FILE_MANAGEMENT_TEXTS.categories.contract,
    ID_CARD: ADMIN_FILE_MANAGEMENT_TEXTS.categories.idCard,
    OTHER: ADMIN_FILE_MANAGEMENT_TEXTS.categories.other,
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
  color: var(--lex-primary-soft);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  margin-left: auto;
}

.file-name {
  max-width: 220px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
  font-weight: 600;
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

.cleanup-help {
  margin-top: 4px;
  color: var(--text-tertiary);
  font-size: 12px;
}

@media (max-width: 768px) {
  .action-buttons {
    width: 100%;
  }

  .action-buttons :deep(.ant-space-item),
  .action-buttons :deep(.ant-btn) {
    width: 100%;
  }

  .filter-form {
    display: grid;
  }

  .filter-form :deep(.ant-form-item) {
    width: 100%;
  }

  .filter-form :deep(.ant-input),
  .filter-form :deep(.ant-select) {
    width: 100% !important;
  }

  .filter-actions {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .btn-text {
    display: none;
  }

  .table-action-buttons {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
