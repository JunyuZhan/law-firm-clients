<template>
  <div class="notification-template-container">
    <section class="page-intro">
      <div>
        <h2 class="editorial-title intro-title">
          通知模板管理
        </h2>
        <p class="intro-text">
          集中维护短信、微信、邮件模板，统一模板结构、变量定义和服务商映射。
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
          新增模板
        </a-button>
        <a-button @click="loadData">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </a-space>
    </section>

    <section class="guide-panel">
      <div class="guide-grid dashboard-guide-grid">
        <article class="guide-card guide-card--wide dashboard-guide-card dashboard-guide-card--wide">
          <div class="guide-card__head dashboard-guide-head">
            <div>
              <h3>模板使用说明</h3>
            </div>
            <a-tag color="processing">
              统一管理
            </a-tag>
          </div>
          <ul class="guide-list">
            <li><strong>模板内容</strong> 就是客户最终收到的消息结构，发送前会自动替换变量。</li>
            <li><strong>短信模板</strong> 需与阿里云/腾讯云平台的模板代码和变量命名保持一致。</li>
            <li><strong>微信模板</strong> 使用 JSON 结构，需符合微信模板消息字段格式。</li>
            <li><strong>邮件模板</strong> 支持 HTML，可直接组合品牌化邮件内容。</li>
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
        <span class="stats-label">模板总数</span>
        <strong>{{ templateStats.total }}</strong>
        <p>当前模板库中的全部模板。</p>
      </div>
      <div class="stats-card success">
        <span class="stats-label">启用中</span>
        <strong>{{ templateStats.enabled }}</strong>
        <p>会参与实际消息发送的模板。</p>
      </div>
      <div class="stats-card info">
        <span class="stats-label">短信 / 微信 / 邮件</span>
        <strong>{{ templateStats.sms }}/{{ templateStats.wechat }}/{{ templateStats.email }}</strong>
        <p>按渠道拆分的模板库存量。</p>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">服务商映射</span>
        <strong>{{ templateStats.providers }}</strong>
        <p>已配置服务商的模板数量。</p>
      </div>
    </section>

    <section class="filter-panel">
      <div class="panel-head dashboard-panel-head">
        <div>
          <h3>筛选模板库</h3>
        </div>
        <p>按渠道、服务商和启用状态定位模板，降低误改风险。</p>
      </div>

      <a-form
        :model="searchForm"
        layout="inline"
        class="template-filter-form"
        @finish="handleSearch"
      >
        <a-form-item label="模板类型">
          <a-select
            v-model:value="searchForm.templateType"
            placeholder="请选择模板类型"
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
        <a-form-item label="服务商">
          <a-select
            v-model:value="searchForm.provider"
            placeholder="请选择服务商"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="aliyun">
              阿里云
            </a-select-option>
            <a-select-option value="tencent">
              腾讯云
            </a-select-option>
            <a-select-option value="wechat">
              微信
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.enabled"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
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
      <div class="panel-head panel-head--table dashboard-panel-head dashboard-panel-head--table">
        <div>
          <h3>模板库清单</h3>
        </div>
        <p>表格保留治理动作，内容预览做截断展示，避免长文本打乱阅读节奏。</p>
      </div>

      <div class="table-summary dashboard-table-summary">
        <span>当前模板 {{ dataSource.length }} 条</span>
        <span>启用模板 {{ templateStats.enabled }} 条</span>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1080 }"
        row-key="id"
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
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除这个模板吗？"
                @confirm="handleDelete(record)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                >
                  移除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>

    <a-modal
      v-model:open="showCreateModal"
      :title="editingRecord ? '编辑模板' : '新增模板'"
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
              label="模板名称"
              required
            >
              <a-input
                v-model:value="formData.templateName"
                placeholder="请输入模板名称"
              />
            </a-form-item>
          </a-col>
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item
              label="模板类型"
              required
            >
              <a-select
                v-model:value="formData.templateType"
                placeholder="请选择模板类型"
                :disabled="!!editingRecord"
                @change="handleTemplateTypeChange"
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
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item label="模板代码">
              <a-input
                v-model:value="formData.templateCode"
                :placeholder="getTemplateCodePlaceholder()"
                :disabled="!!editingRecord"
              />
              <div class="field-note">
                <span v-if="formData.templateType === 'SMS'">模板代码需与服务商平台模板保持一致。</span>
                <span v-else-if="formData.templateType === 'WECHAT'">模板 ID 需在微信公众平台申请。</span>
                <span v-else-if="formData.templateType === 'EMAIL'">邮件模板无需填写模板代码。</span>
              </div>
            </a-form-item>
          </a-col>
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item label="服务商">
              <a-select
                v-model:value="formData.provider"
                placeholder="请选择服务商"
                :disabled="!!editingRecord"
                @change="handleProviderChange"
              >
                <a-select-option value="aliyun">
                  阿里云
                </a-select-option>
                <a-select-option value="tencent">
                  腾讯云
                </a-select-option>
                <a-select-option value="wechat">
                  微信
                </a-select-option>
              </a-select>
              <div class="field-note">
                <span v-if="formData.templateType === 'SMS' && !formData.provider">短信模板需选择服务商。</span>
                <span v-else-if="formData.templateType === 'WECHAT' && formData.provider !== 'wechat'">微信模板建议固定使用“微信”服务商。</span>
                <span v-else-if="formData.templateType === 'EMAIL'">邮件模板无需选择服务商。</span>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item
          v-if="formData.templateType === 'SMS'"
          label="签名名称"
        >
          <a-input
            v-model:value="formData.signName"
            placeholder="请输入签名名称（在服务商平台申请的签名）"
            :disabled="!!editingRecord"
          />
          <div class="field-note">
            短信模板通常需要匹配服务商平台上的签名。
          </div>
        </a-form-item>
        <a-form-item
          label="模板内容"
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
              message="模板内容就是客户收到的消息结构，系统会自动替换变量后发送。"
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
              点击加载示例模板
            </a-button>
          </div>
        </a-form-item>
        <a-form-item label="模板变量">
          <a-textarea
            v-model:value="formData.templateVariables"
            placeholder="请输入模板变量说明（JSON格式）"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch
            v-model:checked="formData.enabled"
            checked-children="启用"
            un-checked-children="禁用"
          />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入模板描述"
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
  showTotal: (total: number) => `共 ${total} 条`,
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
  { title: 'ID', key: 'id', dataIndex: 'id', width: 72, align: 'center' },
  { title: '模板名称', key: 'templateName', dataIndex: 'templateName', ellipsis: true, width: 140, align: 'center' },
  { title: '模板类型', key: 'templateType', width: 100, align: 'center' },
  { title: '模板代码', key: 'templateCode', dataIndex: 'templateCode', ellipsis: true, width: 140, align: 'center' },
  { title: '服务商', key: 'provider', width: 100, align: 'center' },
  { title: '签名名称', key: 'signName', dataIndex: 'signName', ellipsis: true, width: 110, align: 'center' },
  { title: '模板内容', key: 'templateContent', ellipsis: true, width: 180, align: 'center' },
  { title: '状态', key: 'enabled', width: 76, align: 'center' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt', width: 160, align: 'center' },
  { title: '操作', key: 'action', width: 116, align: 'center' },
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
    const errorMessage = error instanceof Error ? error.message : '加载模板列表失败'
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
    message.success('模板已删除')
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '删除失败'
    message.error(errorMessage)
  }
}

async function handleToggleEnabled(record: NotificationTemplateInfo, checked: boolean) {
  try {
    await updateTemplate(record.id, { enabled: checked })
    message.success(checked ? '模板已启用' : '模板已禁用')
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '操作失败'
    message.error(errorMessage)
    await loadData()
  }
}

async function handleSubmit() {
  if (!formData.value.templateName) {
    message.warning('请填写模板名称')
    return
  }
  if (!formData.value.templateType) {
    message.warning('请选择模板类型')
    return
  }
  if (!formData.value.templateContent) {
    message.warning('请填写模板内容')
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
      message.success('模板已更新')
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
      message.success('模板已创建')
    }
    handleCancel()
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '保存失败'
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
    SMS: '短信',
    WECHAT: '微信',
    EMAIL: '邮件',
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
    aliyun: '阿里云',
    tencent: '腾讯云',
    wechat: '微信',
  }
  return map[provider] || provider
}

function getTemplateContentPlaceholder(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider

  if (type === 'SMS') {
    if (provider === 'aliyun') return '请输入JSON格式，如：{"code":"${matterName}"}'
    if (provider === 'tencent') return '请输入数组格式，如：["${matterName}","${accessUrl}"]'
    return '请输入模板内容（JSON格式）'
  } else if (type === 'WECHAT') {
    return '请输入JSON格式，符合微信模板消息字段结构'
  } else if (type === 'EMAIL') {
    return '请输入HTML格式，支持变量：${matterName}、${accessUrl}、${clientName}'
  }
  return '请输入模板内容'
}

function getTemplateFormatHint(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider

  if (type === 'SMS') {
    if (provider === 'aliyun') return '格式：JSON对象，变量名必须与阿里云平台模板变量一致。'
    if (provider === 'tencent') return '格式：JSON数组，数组顺序必须与腾讯云模板变量顺序一致。'
    return '请选择服务商后查看格式说明'
  } else if (type === 'WECHAT') {
    return '格式：JSON对象，需符合微信模板消息的字段结构。'
  } else if (type === 'EMAIL') {
    return '格式：HTML格式，可直接组合完整邮件内容。'
  }
  return '请先选择模板类型'
}

function getTemplateCodePlaceholder(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider

  if (type === 'SMS') {
    if (provider === 'aliyun') return '请输入模板代码（如：SMS_123456789）'
    if (provider === 'tencent') return '请输入模板ID（如：123456）'
    return '请先选择服务商'
  } else if (type === 'WECHAT') {
    return '请输入模板ID（如：abc123def456）'
  }
  return '请输入模板代码'
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
      message.info('已加载阿里云短信模板示例')
    } else if (provider === 'tencent') {
      formData.value.templateContent = '["${matterName}","${accessUrl}"]'
      formData.value.templateCode = formData.value.templateCode || '123456'
      formData.value.signName = formData.value.signName || '律师事务所'
      message.info('已加载腾讯云短信模板示例')
    } else {
      message.warning('请先选择服务商（阿里云或腾讯云）')
    }
  } else if (type === 'WECHAT') {
    formData.value.templateContent = JSON.stringify({
      first: { value: '项目信息已更新', color: '#173177' },
      keyword1: { value: '${matterName}', color: '#173177' },
      keyword2: { value: '${accessUrl}', color: '#173177' },
      remark: { value: '点击查看详情', color: '#173177' },
    }, null, 2)
    formData.value.templateCode = formData.value.templateCode || 'abc123def456'
    message.info('已加载微信模板示例')
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
    message.info('已加载邮件模板示例')
  } else {
    message.warning('请先选择模板类型')
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
