<template>
  <div class="api-key-management-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_API_KEY_TEXTS.intro }}
        </p>
      </div>
      <a-space>
        <a-button
          type="primary"
          @click="showCreateModal = true"
        >
          <template #icon>
            <PlusOutlined />
          </template>
          {{ ADMIN_API_KEY_TEXTS.actions.create }}
        </a-button>
        <a-button @click="loadData">
          <template #icon>
            <ReloadOutlined />
          </template>
          {{ UI_TEXTS.refresh }}
        </a-button>
      </a-space>
    </section>

    <section class="guide-grid dashboard-guide-grid">
      <article class="guide-card guide-card--wide dashboard-guide-card dashboard-guide-card--wide">
        <div class="guide-card__head dashboard-guide-head">
          <div>
              <h3>{{ ADMIN_API_KEY_TEXTS.guide.title }}</h3>
          </div>
          <a-tag color="processing">
              {{ ADMIN_API_KEY_TEXTS.guide.tag }}
          </a-tag>
        </div>
        <a-descriptions
          :column="1"
          size="small"
          bordered
        >
          <a-descriptions-item label="API 地址">
            <a-typography-text
              v-if="apiBaseUrl"
              copyable
              :content="apiBaseUrl"
            >
              <code>{{ apiBaseUrl }}</code>
            </a-typography-text>
            <span
              v-else
              class="muted-text"
            >
              {{ ADMIN_API_KEY_TEXTS.guide.apiAddressMissing }}
            </span>
            <div class="helper-text">
              {{ ADMIN_API_KEY_TEXTS.guide.apiAddressHint }}
            </div>
          </a-descriptions-item>
          <a-descriptions-item :label="ADMIN_API_KEY_TEXTS.guide.authLabel">
            <div><code>X-API-Key: {API密钥}</code></div>
            <div><code>X-API-Secret: {API密钥Secret}</code></div>
          </a-descriptions-item>
          <a-descriptions-item :label="ADMIN_API_KEY_TEXTS.guide.usageLabel">
            <ol class="guide-list">
              <li
                v-for="item in ADMIN_API_KEY_TEXTS.guide.usageItems"
                :key="item"
              >
                {{ item }}
              </li>
            </ol>
          </a-descriptions-item>
        </a-descriptions>
        <div class="guide-footer">
          <router-link to="/admin/config">
            <a-button type="default">
              {{ ADMIN_API_KEY_TEXTS.actions.goToConfig }}
            </a-button>
          </router-link>
        </div>
      </article>
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">{{ ADMIN_API_KEY_TEXTS.stats.total }}</span>
        <strong>{{ keyStats.total }}</strong>
        <p>{{ ADMIN_API_KEY_TEXTS.stats.totalHint }}</p>
      </div>
      <div class="stats-card success">
        <span class="stats-label">{{ ADMIN_API_KEY_TEXTS.stats.enabled }}</span>
        <strong>{{ keyStats.enabled }}</strong>
        <p>{{ ADMIN_API_KEY_TEXTS.stats.enabledHint }}</p>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">{{ ADMIN_API_KEY_TEXTS.stats.disabled }}</span>
        <strong>{{ keyStats.disabled }}</strong>
        <p>{{ ADMIN_API_KEY_TEXTS.stats.disabledHint }}</p>
      </div>
      <div class="stats-card info">
        <span class="stats-label">{{ ADMIN_API_KEY_TEXTS.stats.expired }}</span>
        <strong>{{ keyStats.expired }}</strong>
        <p>{{ ADMIN_API_KEY_TEXTS.stats.expiredHint }}</p>
      </div>
    </section>

    <section class="filter-panel">
      <div class="panel-head dashboard-panel-head">
        <div>
          <h3>{{ ADMIN_API_KEY_TEXTS.filter.title }}</h3>
        </div>
        <p>{{ ADMIN_API_KEY_TEXTS.filter.desc }}</p>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="key-filter-form"
        @finish="handleSearch"
      >
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.enabled"
            :placeholder="ADMIN_API_KEY_TEXTS.filter.statusPlaceholder"
            allow-clear
            style="width: 140px"
          >
            <a-select-option :value="true">
              启用
            </a-select-option>
            <a-select-option :value="false">
              禁用
            </a-select-option>
          </a-select>
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
        <h3>{{ ADMIN_API_KEY_TEXTS.table.title }}</h3>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 'max-content' }"
        row-key="id"
        size="small"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'apiKey'">
            <a-typography-text :copyable="{ text: record.apiKey }">
              {{ record.apiKey }}
            </a-typography-text>
          </template>
          <template v-else-if="column.key === 'apiSecret'">
            <a-typography-text
              v-if="record.apiSecret"
              :copyable="{ text: record.apiSecret }"
            >
              {{ maskSecret(record.apiSecret) }}
            </a-typography-text>
            <span class="muted-text">-</span>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              @change="(checked: boolean) => handleToggleEnabled(record, checked)"
            />
          </template>
          <template v-else-if="column.key === 'expiresAt'">
            <span :class="{ expired: isExpired(record.expiresAt) }">
              {{ formatApiKeyDate(record.expiresAt) }}
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleEdit(record)"
              >
                编辑
              </a-button>
              <a-popconfirm
                :title="UI_CONFIRM_TEXTS.removeApiKey"
                @confirm="handleDelete(record)"
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
      v-model:open="showCreateModal"
      :title="editingRecord ? ADMIN_API_KEY_TEXTS.actions.editTitle : ADMIN_API_KEY_TEXTS.actions.createTitle"
      :width="modalWidth"
      wrap-class-name="apikey-modal"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        :model="formData"
        layout="vertical"
      >
        <a-form-item
          label="密钥名称"
          required
        >
          <a-input
            v-model:value="formData.keyName"
            :placeholder="ADMIN_API_KEY_TEXTS.modal.keyNamePlaceholder"
          />
        </a-form-item>
        <a-form-item
          v-if="editingRecord"
          label="API 密钥 (Key)"
        >
          <a-typography-text
            copyable
            :content="editingRecord?.apiKey"
          >
            <code>{{ editingRecord?.apiKey }}</code>
          </a-typography-text>
        </a-form-item>
        <a-form-item
          v-if="editingRecord"
          label="API 密钥 (Secret)"
        >
          <a-alert
            type="warning"
            show-icon
          >
            <template #message>
              <div>
                <div class="modal-title-note">
                  <strong>{{ ADMIN_API_KEY_TEXTS.modal.secretWarnTitle }}</strong>
                </div>
                <div class="modal-text-note">
                  {{ ADMIN_API_KEY_TEXTS.modal.secretWarnBody1 }}
                </div>
                <div class="modal-text-note">
                  {{ ADMIN_API_KEY_TEXTS.modal.secretWarnBody2 }}
                </div>
              </div>
            </template>
          </a-alert>
        </a-form-item>
        <a-form-item label="过期时间">
          <a-date-picker
            v-model:value="formData.expiresAt"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            :placeholder="ADMIN_API_KEY_TEXTS.modal.expiresPlaceholder"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item
          v-if="editingRecord"
          label="是否启用"
        >
          <a-switch v-model:checked="formData.enabled" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="showSuccessModal"
      :title="ADMIN_API_KEY_TEXTS.actions.successTitle"
      :footer="null"
      :closable="true"
      :mask-closable="false"
      :width="modalWidth"
      wrap-class-name="apikey-success-modal"
    >
      <a-alert
        type="warning"
        show-icon
        class="success-alert"
      >
        <template #message>
          {{ ADMIN_API_KEY_TEXTS.modal.successAlert }}
        </template>
      </a-alert>
      <a-descriptions
        :column="1"
        bordered
      >
        <a-descriptions-item label="API 密钥 (Key)">
          <a-typography-text
            copyable
            :content="createdApiKey.apiKey"
          >
            <code>{{ createdApiKey.apiKey }}</code>
          </a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="API 密钥 (Secret)">
          <a-typography-text
            copyable
            :content="createdApiKey.apiSecret"
          >
            <code>{{ createdApiKey.apiSecret }}</code>
          </a-typography-text>
        </a-descriptions-item>
      </a-descriptions>
      <div class="success-footer">
        <a-button
          type="primary"
          @click="showSuccessModal = false"
        >
          {{ ADMIN_API_KEY_TEXTS.actions.successClose }}
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'
import {
  getApiKeyList,
  createApiKey,
  updateApiKey,
  deleteApiKey,
  type ApiKeyInfo,
  type CreateApiKeyRequest,
  type UpdateApiKeyRequest,
} from '@/api/apiKey'
import { getConfigList } from '@/api/config'
import { formatDate } from '@/utils/date'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import logger from '@/utils/logger'
import { UI_CONFIRM_TEXTS, UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_API_KEY_TEXTS } from '@/constants/adminTexts'

const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<ApiKeyInfo[]>([])
const showCreateModal = ref(false)
const showSuccessModal = ref(false)
const editingRecord = ref<ApiKeyInfo | null>(null)

const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => {
  return windowWidth.value < 768 ? '95%' : '600px'
})

function handleResize() {
  windowWidth.value = window.innerWidth
}

function maskSecret(secret: string): string {
  if (!secret || secret.length <= 8) return '••••••••'
  return secret.substring(0, 4) + '••••••' + secret.substring(secret.length - 4)
}

const apiBaseUrl = ref('')
const createdApiKey = ref({
  apiKey: '',
  apiSecret: '',
})

const searchForm = ref({
  enabled: undefined as boolean | undefined,
  limit: 100,
})

const formData = ref<{
  keyName: string
  expiresAt?: Dayjs | string
  enabled?: boolean
}>({
  keyName: '',
  expiresAt: undefined,
  enabled: true,
})

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const keyStats = computed(() => ({
  total: dataSource.value.length,
  enabled: dataSource.value.filter(item => item.enabled).length,
  disabled: dataSource.value.filter(item => !item.enabled).length,
  expired: dataSource.value.filter(item => isExpired(item.expiresAt)).length,
}))
const columns = [
  { title: 'ID', key: 'id', dataIndex: 'id', width: 80, align: 'center' },
  { title: '密钥名称', key: 'keyName', dataIndex: 'keyName', ellipsis: true, width: 150, align: 'center' },
  { title: 'API密钥', key: 'apiKey', ellipsis: true, width: 280, align: 'center' },
  { title: 'API密钥Secret', key: 'apiSecret', ellipsis: true, width: 280, align: 'center' },
  { title: '状态', key: 'enabled', width: 80, align: 'center' },
  { title: '过期时间', key: 'expiresAt', width: 160, align: 'center' },
  { title: '操作', key: 'action', width: 150, align: 'center', fixed: 'right' },
]

function handleConfigLoadError(configName: string, error: unknown): void {
  const errorMessage = error instanceof Error ? error.message : String(error)
  logger.warn(`加载${configName}失败: ${errorMessage}`)
}

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback
}

async function loadApiBaseUrl() {
  try {
    const res = await getConfigList({ configKey: 'system.base-url', limit: 1 })
    const config = res.data?.find(c => c.configKey === 'system.base-url')
    if (config?.configValue) {
      apiBaseUrl.value = config.configValue.replace(/\/+$/, '')
    }
  } catch (error) {
    handleConfigLoadError('系统基础URL', error)
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, string | number | boolean> = {}
    if (searchForm.value.enabled !== undefined) params.enabled = searchForm.value.enabled
    if (searchForm.value.limit) params.limit = searchForm.value.limit

    const res = await getApiKeyList(params)
    dataSource.value = res.data || []
    pagination.value.total = dataSource.value.length
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.apiKeyListLoadFailed))
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
    enabled: undefined,
    limit: 100,
  }
  handleSearch()
}

function handleTableChange(pag: TablePaginationConfig) {
  if (pag.current) pagination.value.current = pag.current
  if (pag.pageSize) pagination.value.pageSize = pag.pageSize
}

function handleEdit(record: ApiKeyInfo) {
  editingRecord.value = record
  formData.value = {
    keyName: record.keyName,
    expiresAt: record.expiresAt ? dayjs(record.expiresAt) : undefined,
    enabled: record.enabled,
  }
  showCreateModal.value = true
}

async function handleToggleEnabled(record: ApiKeyInfo, enabled: boolean) {
  try {
    await updateApiKey(record.id, { enabled })
    message.success(enabled ? ADMIN_API_KEY_TEXTS.toggle.enabled : ADMIN_API_KEY_TEXTS.toggle.disabled)
    await loadData()
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.operationFailed))
    await loadData()
  }
}

async function handleDelete(record: ApiKeyInfo) {
  try {
    await deleteApiKey(record.id)
    message.success(UI_FEEDBACK_TEXTS.apiKeyDeleted)
    await loadData()
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.operationFailed))
  }
}

async function handleSubmit() {
  if (!formData.value.keyName) {
    message.warning(ADMIN_API_KEY_TEXTS.validation.nameRequired)
    return
  }
  if (formData.value.keyName.length > 50) {
    message.warning(ADMIN_API_KEY_TEXTS.validation.nameTooLong)
    return
  }
  if (!/^[\u4e00-\u9fa5a-zA-Z0-9_\-\s]+$/.test(formData.value.keyName)) {
    message.warning(ADMIN_API_KEY_TEXTS.validation.nameInvalid)
    return
  }

  submitLoading.value = true
  try {
    if (editingRecord.value) {
      const updateData: UpdateApiKeyRequest = {}
      if (formData.value.keyName) updateData.keyName = formData.value.keyName
      if (formData.value.enabled !== undefined) updateData.enabled = formData.value.enabled
      if (formData.value.expiresAt !== undefined) {
        updateData.expiresAt = dayjs.isDayjs(formData.value.expiresAt)
          ? formData.value.expiresAt.toISOString()
          : formData.value.expiresAt
      }

      if (!editingRecord.value?.id) {
        message.error(ADMIN_API_KEY_TEXTS.validation.missingId)
        return
      }
      await updateApiKey(editingRecord.value.id, updateData)
      message.success(UI_FEEDBACK_TEXTS.apiKeyUpdated)
    } else {
      const createData: CreateApiKeyRequest = {
        keyName: formData.value.keyName,
      }
      if (formData.value.expiresAt) {
        createData.expiresAt = dayjs.isDayjs(formData.value.expiresAt)
          ? formData.value.expiresAt.toISOString()
          : formData.value.expiresAt
      }

      const res = await createApiKey(createData)
      if (res.data) {
        createdApiKey.value = {
          apiKey: res.data.apiKey || '',
          apiSecret: res.data.apiSecret || '',
        }
        showSuccessModal.value = true
      }
    }
    handleCancel()
    await loadData()
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.operationFailed))
  } finally {
    submitLoading.value = false
  }
}

function handleCancel() {
  showCreateModal.value = false
  editingRecord.value = null
  formData.value = {
    keyName: '',
    expiresAt: undefined,
    enabled: true,
  }
}

function isExpired(expiresAt?: string): boolean {
  if (!expiresAt) return false
  return dayjs(expiresAt).isBefore(dayjs())
}

function formatApiKeyDate(date?: string): string {
  if (!date) return ADMIN_API_KEY_TEXTS.table.neverExpires
  return formatDate(date)
}

onMounted(() => {
  loadData()
  loadApiBaseUrl()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.api-key-management-container {
  display: grid;
  gap: 18px;
}

.panel-kicker {
  display: inline-block;
  color: var(--lex-accent-strong);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-weight: 700;
}

.guide-card {
  padding: 20px;
}

.guide-card :deep(.ant-descriptions-bordered .ant-descriptions-item-label) {
  background: var(--lex-surface);
}

.guide-list {
  margin: 0;
  padding-left: 20px;
  line-height: 1.8;
}

.guide-footer {
  margin-top: 14px;
}

.helper-text,
.muted-text {
  color: var(--text-tertiary);
}

.helper-text {
  font-size: 12px;
  margin-top: 4px;
}

.key-filter-form {
  display: flex;
  gap: 12px 8px;
}

.key-filter-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  margin-left: auto;
}

.expired {
  color: var(--error-color);
}

.modal-title-note {
  margin-bottom: 8px;
}

.modal-text-note {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.success-alert {
  margin-bottom: 16px;
}

.success-footer {
  margin-top: 16px;
  text-align: center;
}

@media (max-width: 992px) {
}

@media (max-width: 768px) {
  .guide-card {
    padding: 16px;
    border-radius: 20px;
  }

  .key-filter-form {
    display: grid;
  }

  .key-filter-form :deep(.ant-form-item) {
    width: 100%;
  }

  .key-filter-form :deep(.ant-input),
  .key-filter-form :deep(.ant-select) {
    width: 100% !important;
  }

  .filter-actions {
    margin-left: 0;
  }
}
</style>
