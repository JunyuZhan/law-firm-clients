<template>
  <div class="notification-template-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.intro }}
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
          {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.actions.create }}
        </a-button>
        <a-button @click="loadData">
          <template #icon>
            <ReloadOutlined />
          </template>
          {{ UI_TEXTS.refresh }}
        </a-button>
      </a-space>
    </section>

    <section class="guide-panel">
      <div class="guide-grid dashboard-guide-grid">
        <article class="guide-card guide-card--wide dashboard-guide-card dashboard-guide-card--wide">
          <div class="guide-card__head dashboard-guide-head">
            <div>
              <h3>{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.guide.title }}</h3>
            </div>
            <a-tag color="processing">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.guide.tag }}
            </a-tag>
          </div>
          <ul class="guide-list">
            <li
              v-for="item in ADMIN_NOTIFICATION_TEMPLATE_TEXTS.guide.items"
              :key="item"
            >
              {{ item }}
            </li>
          </ul>
          <div class="token-cloud">
            <code>${matterName}</code>
            <code>${accessUrl}</code>
            <code>${clientName}</code>
            <code>${validDays}</code>
          </div>
        </article>
      </div>
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.stats.total }}</span>
        <strong>{{ templateStats.total }}</strong>
      </div>
      <div class="stats-card success">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.stats.enabled }}</span>
        <strong>{{ templateStats.enabled }}</strong>
      </div>
      <div class="stats-card info">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.stats.channelMix }}</span>
        <strong>{{ templateStats.sms }}/{{ templateStats.wechat }}/{{ templateStats.email }}</strong>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.stats.providerMapping }}</span>
        <strong>{{ templateStats.providers }}</strong>
      </div>
    </section>

    <section class="filter-panel">
      <div class="panel-head dashboard-panel-head">
        <h3>{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.title }}</h3>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="template-filter-form"
        @finish="handleSearch"
      >
        <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.typeLabel">
          <a-select
            v-model:value="searchForm.templateType"
            :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.typePlaceholder"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="SMS">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.sms }}
            </a-select-option>
            <a-select-option value="WECHAT">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.wechat }}
            </a-select-option>
            <a-select-option value="EMAIL">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.email }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.providerLabel">
          <a-select
            v-model:value="searchForm.provider"
            :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.providerPlaceholder"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="aliyun">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.aliyun }}
            </a-select-option>
            <a-select-option value="tencent">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.tencent }}
            </a-select-option>
            <a-select-option value="wechat">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.wechatProvider }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.statusLabel">
          <a-select
            v-model:value="searchForm.enabled"
            :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.statusPlaceholder"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="true">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.enabled }}
            </a-select-option>
            <a-select-option :value="false">
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.disabled }}
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
        <h3>{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.title }}</h3>
      </div>

      <div class="table-summary dashboard-table-summary">
        <span>{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.totalPrefix }}{{ dataSource.length }}{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.totalSuffix }}</span>
        <span v-if="templateStats.enabled">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.enabledPrefix }}{{ templateStats.enabled }}{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.enabledSuffix }}</span>
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
          <template v-if="column.key === 'templateType'">
            <a-tag :color="getTemplateTypeColor(record.templateType)">
              {{ getTemplateTypeName(record.templateType) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'provider'">
            <a-tag>{{ getProviderName(record.provider) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              @change="(checked: boolean) => handleToggleEnabled(record, checked)"
            />
          </template>
          <template v-else-if="column.key === 'templateContent'">
            <span class="content-preview">
              {{ record.templateContent || '-' }}
            </span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="handleEdit(record)"
              >
                {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.actions.edit }}
              </a-button>
              <a-popconfirm
                :title="UI_CONFIRM_TEXTS.removeTemplate"
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
      :title="editingRecord ? ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.editTitle : ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.createTitle"
      :width="modalWidth"
      wrap-class-name="template-modal"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        :model="formData"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item
              :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.nameLabel"
              required
            >
              <a-input
                v-model:value="formData.templateName"
                :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.namePlaceholder"
              />
            </a-form-item>
          </a-col>
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item
              :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.typeLabel"
              required
            >
              <a-select
                v-model:value="formData.templateType"
                :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.typePlaceholder"
                :disabled="!!editingRecord"
                @change="handleTemplateTypeChange"
              >
                <a-select-option value="SMS">
                  {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.sms }}
                </a-select-option>
                <a-select-option value="WECHAT">
                  {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.wechat }}
                </a-select-option>
                <a-select-option value="EMAIL">
                  {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.email }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.codeLabel">
              <a-input
                v-model:value="formData.templateCode"
                :placeholder="getTemplateCodePlaceholder()"
                :disabled="!!editingRecord"
              />
              <div class="field-note">
                <span v-if="formData.templateType === 'SMS'">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.notes.smsAliyunCode }}</span>
                <span v-else-if="formData.templateType === 'WECHAT'">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.notes.wechatTemplateId }}</span>
                <span v-else-if="formData.templateType === 'EMAIL'">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.notes.emailNoCode }}</span>
              </div>
            </a-form-item>
          </a-col>
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.providerLabel">
              <a-select
                v-model:value="formData.provider"
                :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.providerPlaceholder"
                :disabled="!!editingRecord"
                @change="handleProviderChange"
              >
                <a-select-option value="aliyun">
                  {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.aliyun }}
                </a-select-option>
                <a-select-option value="tencent">
                  {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.tencent }}
                </a-select-option>
                <a-select-option value="wechat">
                  {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.wechatProvider }}
                </a-select-option>
              </a-select>
              <div class="field-note">
                <span v-if="formData.templateType === 'SMS' && !formData.provider">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.notes.smsProviderRequired }}</span>
                <span v-else-if="formData.templateType === 'WECHAT' && formData.provider !== 'wechat'">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.notes.wechatProviderSuggested }}</span>
                <span v-else-if="formData.templateType === 'EMAIL'">{{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.notes.emailNoProvider }}</span>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item
          v-if="formData.templateType === 'SMS'"
          :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.signNameLabel"
        >
          <a-input
            v-model:value="formData.signName"
            :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.signNamePlaceholder"
            :disabled="!!editingRecord"
          />
          <div class="field-note">
            {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.notes.smsSignHint }}
          </div>
        </a-form-item>
        <a-form-item
          :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.contentLabel"
          required
        >
          <a-textarea
            v-model:value="formData.templateContent"
            :placeholder="getTemplateContentPlaceholder()"
            :rows="6"
          />
          <div class="template-hints">
            <a-alert
              :message="getTemplateFormatHint()"
              type="info"
              show-icon
              :closable="false"
              class="hint-alert"
            />
            <a-alert
              :message="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.contentBusinessHint"
              type="warning"
              show-icon
              :closable="false"
              class="hint-alert"
            />
            <a-button
              type="link"
              size="small"
              class="example-button"
              @click="loadExample"
            >
              <template #icon>
                <FileTextOutlined />
              </template>
              {{ ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.loadExample }}
            </a-button>
          </div>
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.variablesLabel">
          <a-textarea
            v-model:value="formData.templateVariables"
            :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.variablesPlaceholder"
            :rows="3"
          />
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.statusLabel">
          <a-switch
            v-model:checked="formData.enabled"
            :checked-children="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.enabled"
            :un-checked-children="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.disabled"
          />
        </a-form-item>
        <a-form-item :label="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.descriptionLabel">
          <a-textarea
            v-model:value="formData.description"
            :placeholder="ADMIN_NOTIFICATION_TEMPLATE_TEXTS.modal.descriptionPlaceholder"
            :rows="2"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined, PlusOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import {
  getTemplateList,
  createTemplate,
  updateTemplate,
  deleteTemplate,
  type NotificationTemplateInfo,
  type CreateTemplateRequest,
  type UpdateTemplateRequest,
} from '@/api/notificationTemplate'
import { UI_CONFIRM_TEXTS, UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_NOTIFICATION_TEMPLATE_TEXTS } from '@/constants/adminTexts'

const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<NotificationTemplateInfo[]>([])
const showCreateModal = ref(false)
const editingRecord = ref<NotificationTemplateInfo | null>(null)

const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => {
  return windowWidth.value < 768 ? '95%' : '800px'
})

function handleResize() {
  windowWidth.value = window.innerWidth
}

const searchForm = ref({
  templateType: '',
  provider: '',
  enabled: undefined as boolean | undefined,
  limit: 100,
})

const formData = ref<CreateTemplateRequest & { enabled?: boolean }>({
  templateName: '',
  templateType: '',
  templateCode: '',
  templateContent: '',
  templateVariables: '',
  provider: '',
  signName: '',
  enabled: true,
  description: '',
})

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `${ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.totalPrefix}${total}${ADMIN_NOTIFICATION_TEMPLATE_TEXTS.filter.totalSuffix}`,
})

const templateStats = computed(() => ({
  total: dataSource.value.length,
  enabled: dataSource.value.filter(item => item.enabled).length,
  sms: dataSource.value.filter(item => item.templateType === 'SMS').length,
  wechat: dataSource.value.filter(item => item.templateType === 'WECHAT').length,
  email: dataSource.value.filter(item => item.templateType === 'EMAIL').length,
  providers: dataSource.value.filter(item => !!item.provider).length,
}))
const columns = [
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.id, key: 'id', dataIndex: 'id', width: 72, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.name, key: 'templateName', dataIndex: 'templateName', ellipsis: true, width: 140, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.type, key: 'templateType', width: 100, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.code, key: 'templateCode', dataIndex: 'templateCode', ellipsis: true, width: 140, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.provider, key: 'provider', width: 100, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.signName, key: 'signName', dataIndex: 'signName', ellipsis: true, width: 110, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.content, key: 'templateContent', ellipsis: true, width: 180, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.status, key: 'enabled', width: 76, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.createdAt, key: 'createdAt', dataIndex: 'createdAt', width: 160, align: 'center' },
  { title: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.table.action, key: 'action', width: 116, align: 'center' },
]

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, string | number | boolean> = {}
    if (searchForm.value.templateType) params.templateType = searchForm.value.templateType
    if (searchForm.value.provider) params.provider = searchForm.value.provider
    if (searchForm.value.enabled !== undefined) params.enabled = searchForm.value.enabled
    if (searchForm.value.limit) params.limit = searchForm.value.limit

    const res = await getTemplateList(params)
    dataSource.value = res.data || []
    pagination.value.total = dataSource.value.length
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_NOTIFICATION_TEMPLATE_TEXTS.feedback.loadFailed
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
    templateType: '',
    provider: '',
    enabled: undefined,
    limit: 100,
  }
  handleSearch()
}

function handleTableChange(pag: TablePaginationConfig) {
  if (pag.current) pagination.value.current = pag.current
  if (pag.pageSize) pagination.value.pageSize = pag.pageSize
}

function handleEdit(record: NotificationTemplateInfo) {
  editingRecord.value = record
  formData.value = {
    templateName: record.templateName,
    templateType: record.templateType,
    templateCode: record.templateCode,
    templateContent: record.templateContent || '',
    templateVariables: record.templateVariables || '',
    provider: record.provider || '',
    signName: record.signName || '',
    enabled: record.enabled,
    description: record.description || '',
  }
  showCreateModal.value = true
}

async function handleDelete(record: NotificationTemplateInfo) {
  try {
    await deleteTemplate(record.id)
    message.success(UI_FEEDBACK_TEXTS.templateDeleted)
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_NOTIFICATION_TEMPLATE_TEXTS.feedback.deleteFailed
    message.error(errorMessage)
  }
}

async function handleToggleEnabled(record: NotificationTemplateInfo, checked: boolean) {
  try {
    await updateTemplate(record.id, { enabled: checked })
    message.success(checked ? ADMIN_NOTIFICATION_TEMPLATE_TEXTS.feedback.enabledSuccess : ADMIN_NOTIFICATION_TEMPLATE_TEXTS.feedback.disabledSuccess)
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_NOTIFICATION_TEMPLATE_TEXTS.feedback.toggleFailed
    message.error(errorMessage)
    await loadData()
  }
}

async function handleSubmit() {
  if (!formData.value.templateName) {
    message.warning(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.validation.nameRequired)
    return
  }
  if (!formData.value.templateType) {
    message.warning(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.validation.typeRequired)
    return
  }
  if (!formData.value.templateContent) {
    message.warning(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.validation.contentRequired)
    return
  }

  submitLoading.value = true
  try {
    if (editingRecord.value) {
      const updateData: UpdateTemplateRequest = {
        templateName: formData.value.templateName,
        templateContent: formData.value.templateContent,
        templateVariables: formData.value.templateVariables,
        enabled: formData.value.enabled,
        description: formData.value.description,
      }

      await updateTemplate(editingRecord.value.id, updateData)
      message.success(UI_FEEDBACK_TEXTS.templateUpdated)
    } else {
      const createData: CreateTemplateRequest = {
        templateName: formData.value.templateName!,
        templateType: formData.value.templateType!,
        templateCode: formData.value.templateCode,
        templateContent: formData.value.templateContent!,
        templateVariables: formData.value.templateVariables,
        provider: formData.value.provider,
        signName: formData.value.signName,
        enabled: formData.value.enabled !== false,
        description: formData.value.description,
      }

      await createTemplate(createData)
      message.success(UI_FEEDBACK_TEXTS.templateCreated)
    }
    handleCancel()
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : UI_FEEDBACK_TEXTS.configSaveFailed
    message.error(errorMessage)
  } finally {
    submitLoading.value = false
  }
}

function handleCancel() {
  showCreateModal.value = false
  editingRecord.value = null
  formData.value = {
    templateName: '',
    templateType: '',
    templateCode: '',
    templateContent: '',
    templateVariables: '',
    provider: '',
    signName: '',
    enabled: true,
    description: '',
  }
}

function getTemplateTypeName(type: string): string {
  const map: Record<string, string> = {
    SMS: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.sms,
    WECHAT: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.wechat,
    EMAIL: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.email,
  }
  return map[type] || type
}

function getTemplateTypeColor(type: string): string {
  const map: Record<string, string> = {
    SMS: 'blue',
    WECHAT: 'green',
    EMAIL: 'orange',
  }
  return map[type] || 'default'
}

function getProviderName(provider?: string): string {
  if (!provider) return '-'
  const map: Record<string, string> = {
    aliyun: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.aliyun,
    tencent: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.tencent,
    wechat: ADMIN_NOTIFICATION_TEMPLATE_TEXTS.options.wechatProvider,
  }
  return map[provider] || provider
}

function getTemplateContentPlaceholder(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider

  if (type === 'SMS') {
    if (provider === 'aliyun') return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.smsAliyunContent
    if (provider === 'tencent') return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.smsTencentContent
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.smsDefaultContent
  } else if (type === 'WECHAT') {
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.wechatContent
  } else if (type === 'EMAIL') {
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.emailContent
  }
  return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.defaultContent
}

function getTemplateFormatHint(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider

  if (type === 'SMS') {
    if (provider === 'aliyun') return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.formatHints.smsAliyun
    if (provider === 'tencent') return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.formatHints.smsTencent
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.formatHints.chooseProvider
  } else if (type === 'WECHAT') {
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.formatHints.wechat
  } else if (type === 'EMAIL') {
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.formatHints.email
  }
  return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.formatHints.chooseType
}

function getTemplateCodePlaceholder(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider

  if (type === 'SMS') {
    if (provider === 'aliyun') return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.aliyunCode
    if (provider === 'tencent') return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.tencentCode
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.providerFirst
  } else if (type === 'WECHAT') {
    return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.wechatCode
  }
  return ADMIN_NOTIFICATION_TEMPLATE_TEXTS.placeholders.defaultCode
}

function handleTemplateTypeChange() {
  formData.value.templateContent = ''
  formData.value.templateCode = ''
  formData.value.signName = ''

  if (formData.value.templateType === 'WECHAT') {
    formData.value.provider = 'wechat'
  } else if (formData.value.templateType === 'EMAIL') {
    formData.value.provider = ''
  }
}

function handleProviderChange() {
  if (formData.value.templateType === 'SMS') {
    formData.value.templateContent = ''
  }
}

function loadExample() {
  const type = formData.value.templateType
  const provider = formData.value.provider

  if (type === 'SMS') {
    if (provider === 'aliyun') {
      formData.value.templateContent = '{"code":"${matterName}","url":"${accessUrl}"}'
      formData.value.templateCode = formData.value.templateCode || 'SMS_123456789'
      formData.value.signName = formData.value.signName || '律师事务所'
      message.info(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.examples.aliyunLoaded)
    } else if (provider === 'tencent') {
      formData.value.templateContent = '["${matterName}","${accessUrl}"]'
      formData.value.templateCode = formData.value.templateCode || '123456'
      formData.value.signName = formData.value.signName || '律师事务所'
      message.info(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.examples.tencentLoaded)
    } else {
      message.warning(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.examples.providerRequired)
    }
  } else if (type === 'WECHAT') {
    formData.value.templateContent = JSON.stringify({
      first: { value: '项目信息已更新', color: '#173177' },
      keyword1: { value: '${matterName}', color: '#173177' },
      keyword2: { value: '${accessUrl}', color: '#173177' },
      remark: { value: '点击查看详情', color: '#173177' },
    }, null, 2)
    formData.value.templateCode = formData.value.templateCode || 'abc123def456'
    message.info(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.examples.wechatLoaded)
  } else if (type === 'EMAIL') {
    formData.value.templateContent = `<!DOCTYPE html>
<html>
<head><meta charset='UTF-8'></head>
<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
  <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
    <h2 style='color: #2c3e50;'>项目信息已更新</h2>
    <p>尊敬的${'${clientName}'}，</p>
    <p>您的项目 <strong>${'${matterName}'}</strong> 信息已更新。</p>
    <p><a href='${'${accessUrl}'}'>点击查看详情</a></p>
    <p style='color: #7f8c8d; font-size: 12px;'>此链接有效期为30天</p>
  </div>
</body>
</html>`
    message.info(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.examples.emailLoaded)
  } else {
    message.warning(ADMIN_NOTIFICATION_TEMPLATE_TEXTS.examples.typeRequired)
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.notification-template-container {
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

.guide-panel {
  background: transparent;
}

.guide-card {
  display: grid;
  gap: 18px;
  padding: 20px;
}

.guide-list {
  margin: 0 0 0 20px;
  padding: 0;
  color: var(--text-secondary);
  line-height: 1.8;
}

.token-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.token-cloud code {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(30, 64, 175, 0.08);
  border: 1px solid rgba(30, 64, 175, 0.12);
  color: var(--primary-color-dark);
  font-size: 13px;
}

.guide-note {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.template-filter-form {
  display: flex;
  gap: 12px 8px;
}

.template-filter-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  margin-left: auto;
}

.content-preview {
  word-break: break-all;
  max-width: 300px;
  display: inline-block;
  color: var(--text-secondary);
}

.template-hints {
  margin-top: 8px;
}

.field-note {
  margin-top: 6px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.7;
}

.hint-alert {
  margin-bottom: 8px;
}

.example-button {
  padding: 0;
}

@media (max-width: 768px) {
  .guide-card {
    padding: 16px;
  }

  .template-filter-form {
    display: grid;
  }

  .template-filter-form :deep(.ant-form-item) {
    width: 100%;
  }

  .template-filter-form :deep(.ant-input),
  .template-filter-form :deep(.ant-select),
  .template-filter-form :deep(.ant-input-number) {
    width: 100% !important;
  }

  .filter-actions {
    margin-left: 0;
  }
}
</style>
