<template>
  <div class="notification-template-container">
    <!-- 使用提示 -->
    <a-alert
      type="info"
      show-icon
      closable
      style="margin-bottom: 16px"
    >
      <template #message>
        <div>
          <strong>📋 通知模板使用说明</strong>
          <ul style="margin: 8px 0 0 20px; padding: 0">
            <li><strong>重要提示</strong>：<code>模板内容</code>字段就是<strong>客户收到的消息内容结构</strong>，系统会根据模板内容替换变量后发送给客户</li>
            <li><strong>短信模板</strong>：需要在阿里云/腾讯云平台申请模板，模板代码和变量名必须与平台一致。模板内容格式需符合服务商要求（阿里云JSON格式，腾讯云数组格式）</li>
            <li><strong>微信模板</strong>：需要在微信公众平台申请模板，使用JSON格式。模板内容就是发送给微信的数据结构</li>
            <li><strong>邮件模板</strong>：支持HTML格式，可直接使用变量替换。模板内容就是客户收到的邮件HTML</li>
            <li><strong>支持变量</strong>：<code>${matterName}</code>（项目名称）、<code>${accessUrl}</code>（访问链接）、<code>${clientName}</code>（客户名称）、<code>${validDays}</code>（有效期天数）</li>
          </ul>
        </div>
      </template>
    </a-alert>

    <a-card>
      <template #title>
        <span>通知模板管理</span>
      </template>
      <template #extra>
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
      </template>

      <!-- 搜索表单 -->
      <a-form
        :model="searchForm"
        layout="inline"
        style="margin-bottom: 16px"
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
            <span style="word-break: break-all; max-width: 300px; display: inline-block">
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
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑对话框 -->
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
              <div style="margin-top: 4px; font-size: 12px; color: #999">
                <span v-if="formData.templateType === 'SMS'">
                  💡 模板代码需在服务商平台申请，阿里云格式如：SMS_123456789，腾讯云格式如：123456
                </span>
                <span v-else-if="formData.templateType === 'WECHAT'">
                  💡 模板ID需在微信公众平台申请，格式如：abc123def456
                </span>
                <span v-else-if="formData.templateType === 'EMAIL'">
                  💡 邮件模板无需填写模板代码
                </span>
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
              <div style="margin-top: 4px; font-size: 12px; color: #999">
                <span v-if="formData.templateType === 'SMS' && !formData.provider">
                  💡 短信模板需要选择服务商（阿里云或腾讯云）
                </span>
                <span v-else-if="formData.templateType === 'WECHAT' && formData.provider !== 'wechat'">
                  💡 微信模板的服务商应选择"微信"
                </span>
                <span v-else-if="formData.templateType === 'EMAIL'">
                  💡 邮件模板无需选择服务商
                </span>
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
          <div style="margin-top: 4px; font-size: 12px; color: #999">
            💡 短信模板必须填写签名名称，需在服务商平台申请
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
          <!-- 格式说明和示例 -->
          <div style="margin-top: 8px">
            <a-alert
              :message="getTemplateFormatHint()"
              type="info"
              show-icon
              :closable="false"
              style="margin-bottom: 8px"
            />
            <a-alert
              message="💡 提示：模板内容就是客户收到的消息内容结构，系统会自动替换变量（如 ${matterName}、${accessUrl} 等）后发送给客户"
              type="warning"
              show-icon
              :closable="false"
              style="margin-bottom: 8px"
            />
            <a-button
              type="link"
              size="small"
              style="padding: 0"
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
            placeholder="请输入模板变量说明（JSON格式，如：{&quot;name&quot;: &quot;姓名&quot;, &quot;code&quot;: &quot;验证码&quot;}）"
            :rows="3"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col
            :xs="24"
            :sm="12"
          >
            <a-form-item label="状态">
              <a-switch
                v-model:checked="formData.enabled"
                checked-children="启用"
                un-checked-children="禁用"
              />
            </a-form-item>
          </a-col>
        </a-row>
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

// 响应式宽度
const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => {
  return windowWidth.value < 768 ? '95%' : '800px'
})

// 监听窗口大小变化
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

// 表格列定义
const columns = [
  { title: 'ID', key: 'id', dataIndex: 'id', width: 80, align: 'center' },
  { title: '模板名称', key: 'templateName', dataIndex: 'templateName', ellipsis: true, width: 150, align: 'center' },
  { title: '模板类型', key: 'templateType', width: 110, align: 'center' },
  { title: '模板代码', key: 'templateCode', dataIndex: 'templateCode', ellipsis: true, width: 150, align: 'center' },
  { title: '服务商', key: 'provider', width: 110, align: 'center' },
  { title: '签名名称', key: 'signName', dataIndex: 'signName', ellipsis: true, width: 120, align: 'center' },
  { title: '模板内容', key: 'templateContent', ellipsis: true, width: 200, align: 'center' },
  { title: '状态', key: 'enabled', width: 80, align: 'center' },
  { title: '创建时间', key: 'createdAt', dataIndex: 'createdAt', width: 180, align: 'center' },
  { title: '操作', key: 'action', width: 150, align: 'center', fixed: 'right' },
]

// 加载数据
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

// 搜索
function handleSearch() {
  pagination.value.current = 1
  loadData()
}

// 重置
function handleReset() {
  searchForm.value = {
    templateType: '',
    provider: '',
    enabled: undefined,
    limit: 100,
  }
  handleSearch()
}

// 表格变化
function handleTableChange(pag: TablePaginationConfig) {
  if (pag.current) pagination.value.current = pag.current
  if (pag.pageSize) pagination.value.pageSize = pag.pageSize
}

// 编辑
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

// 删除
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

// 切换启用状态
async function handleToggleEnabled(record: NotificationTemplateInfo, checked: boolean) {
  try {
    await updateTemplate(record.id, { enabled: checked })
    message.success(checked ? '模板已启用' : '模板已禁用')
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '操作失败'
    message.error(errorMessage)
    // 恢复原状态
    await loadData()
  }
}

// 提交表单
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
      // 更新
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
      // 创建
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

// 取消
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

// 获取模板类型名称
function getTemplateTypeName(type: string): string {
  const map: Record<string, string> = {
    SMS: '短信',
    WECHAT: '微信',
    EMAIL: '邮件',
  }
  return map[type] || type
}

// 获取模板类型颜色
function getTemplateTypeColor(type: string): string {
  const map: Record<string, string> = {
    SMS: 'blue',
    WECHAT: 'green',
    EMAIL: 'orange',
  }
  return map[type] || 'default'
}

// 获取服务商名称
function getProviderName(provider?: string): string {
  if (!provider) return '-'
  const map: Record<string, string> = {
    aliyun: '阿里云',
    tencent: '腾讯云',
    wechat: '微信',
  }
  return map[provider] || provider
}

// 获取模板内容占位符
function getTemplateContentPlaceholder(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider
  
  if (type === 'SMS') {
    if (provider === 'aliyun') {
      return '请输入JSON格式，如：{"code":"${matterName}"}（变量名需与阿里云平台一致）'
    } else if (provider === 'tencent') {
      return '请输入数组格式，如：["${matterName}","${accessUrl}"]（顺序需与腾讯云平台一致）'
    }
    return '请输入模板内容（JSON格式）'
  } else if (type === 'WECHAT') {
    return '请输入JSON格式，如：{"first":{"value":"项目信息已更新"},"keyword1":{"value":"${matterName}"},"remark":{"value":"点击查看详情"}}'
  } else if (type === 'EMAIL') {
    return '请输入HTML格式，支持变量：${matterName}、${accessUrl}、${clientName}'
  }
  return '请输入模板内容'
}

// 获取模板格式提示
function getTemplateFormatHint(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider
  
  if (type === 'SMS') {
    if (provider === 'aliyun') {
      return '格式：JSON对象，变量名必须与阿里云平台申请的模板变量名一致。示例：{"code":"${matterName}","url":"${accessUrl}"}'
    } else if (provider === 'tencent') {
      return '格式：JSON数组，数组顺序必须与腾讯云平台申请的模板变量顺序一致。示例：["${matterName}","${accessUrl}"]'
    }
    return '请选择服务商后查看格式说明'
  } else if (type === 'WECHAT') {
    return '格式：JSON对象，必须符合微信模板消息格式。包含first、keyword1-3、remark等字段。'
  } else if (type === 'EMAIL') {
    return '格式：HTML格式，支持完整的HTML标签。变量使用 ${变量名} 格式。'
  }
  return '请先选择模板类型'
}

// 获取模板代码占位符
function getTemplateCodePlaceholder(): string {
  const type = formData.value.templateType
  const provider = formData.value.provider
  
  if (type === 'SMS') {
    if (provider === 'aliyun') {
      return '请输入模板代码（如：SMS_123456789）'
    } else if (provider === 'tencent') {
      return '请输入模板ID（如：123456）'
    }
    return '请先选择服务商'
  } else if (type === 'WECHAT') {
    return '请输入模板ID（如：abc123def456）'
  }
  return '请输入模板代码'
}

// 模板类型变化
function handleTemplateTypeChange() {
  // 清空相关内容
  formData.value.templateContent = ''
  formData.value.templateCode = ''
  formData.value.signName = ''
  
  // 根据类型设置默认服务商
  if (formData.value.templateType === 'WECHAT') {
    formData.value.provider = 'wechat'
  } else if (formData.value.templateType === 'EMAIL') {
    formData.value.provider = ''
  }
}

// 服务商变化
function handleProviderChange() {
  // 清空模板内容，让用户重新填写
  if (formData.value.templateType === 'SMS') {
    formData.value.templateContent = ''
  }
}

// 加载示例模板
function loadExample() {
  const type = formData.value.templateType
  const provider = formData.value.provider
  
  if (type === 'SMS') {
    if (provider === 'aliyun') {
      formData.value.templateContent = '{"code":"${matterName}","url":"${accessUrl}"}'
      formData.value.templateCode = formData.value.templateCode || 'SMS_123456789'
      formData.value.signName = formData.value.signName || '律师事务所'
      message.info('已加载阿里云短信模板示例，请根据实际模板修改变量名')
    } else if (provider === 'tencent') {
      formData.value.templateContent = '["${matterName}","${accessUrl}"]'
      formData.value.templateCode = formData.value.templateCode || '123456'
      formData.value.signName = formData.value.signName || '律师事务所'
      message.info('已加载腾讯云短信模板示例，请根据实际模板调整数组顺序')
    } else {
      message.warning('请先选择服务商（阿里云或腾讯云）')
    }
  } else if (type === 'WECHAT') {
    formData.value.templateContent = JSON.stringify({
      first: {
        value: '项目信息已更新',
        color: '#173177'
      },
      keyword1: {
        value: '${matterName}',
        color: '#173177'
      },
      keyword2: {
        value: '${accessUrl}',
        color: '#173177'
      },
      remark: {
        value: '点击查看详情',
        color: '#173177'
      }
    }, null, 2)
    formData.value.templateCode = formData.value.templateCode || 'abc123def456'
    message.info('已加载微信模板示例，请根据实际模板调整字段')
  } else if (type === 'EMAIL') {
    formData.value.templateContent = `<!DOCTYPE html>
<html>
<head><meta charset='UTF-8'></head>
<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
  <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
    <h2 style='color: #2c3e50;'>项目信息已更新</h2>
    <p>尊敬的${'${clientName}'}，</p>
    <p>您的项目 <strong>${'${matterName}'}</strong> 信息已更新。</p>
    <p>请点击以下链接查看详情：</p>
    <p style='text-align: center; margin: 30px 0;'>
      <a href='${'${accessUrl}'}' style='display: inline-block; padding: 12px 30px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px;'>查看详情</a>
    </p>
    <p style='color: #7f8c8d; font-size: 12px;'>此链接有效期为30天</p>
  </div>
</body>
</html>`
    message.info('已加载邮件模板示例，支持HTML格式和变量替换')
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
  padding: 0;
}

.notification-template-container :deep(.ant-card) {
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.notification-template-container :deep(.ant-card-head) {
  padding: 16px 20px;
}

.notification-template-container :deep(.ant-card-body) {
  padding: 20px;
}

.notification-template-container :deep(.ant-form) {
  margin-bottom: 16px;
}

.notification-template-container :deep(.ant-table) {
  font-size: 14px;
}

/* 确保操作列按钮可见 - 操作列是固定列 */
.notification-template-container :deep(.ant-table-cell-fix-right) {
  min-width: 150px !important;
}

.notification-template-container :deep(.ant-table-cell-fix-right .ant-space) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  flex-wrap: nowrap !important;
  gap: 8px !important;
}

.notification-template-container :deep(.ant-table-cell-fix-right .ant-btn) {
  display: inline-block !important;
  visibility: visible !important;
  opacity: 1 !important;
  flex-shrink: 0 !important;
  white-space: nowrap !important;
}

/* 固定列背景色 - 最强覆盖 */
.notification-template-container :deep(.ant-table-cell-fix-right),
.notification-template-container :deep(.ant-table-cell-fix-left),
.notification-template-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-right),
.notification-template-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-left),
.notification-template-container :deep(td.ant-table-cell-fix-right),
.notification-template-container :deep(td.ant-table-cell-fix-left),
.notification-template-container :deep([class*="ant-table-cell-fix-right"]),
.notification-template-container :deep([class*="ant-table-cell-fix-left"]) {
  background: #fff !important;
  background-color: #fff !important;
  background-image: none !important;
}

.notification-template-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-right),
.notification-template-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-left),
.notification-template-container :deep(th.ant-table-cell-fix-right),
.notification-template-container :deep(th.ant-table-cell-fix-left) {
  background: #fafafa !important;
  background-color: #fafafa !important;
  background-image: none !important;
}

.notification-template-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-right),
.notification-template-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-left),
.notification-template-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-right"]),
.notification-template-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-left"]),
.notification-template-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
.notification-template-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
.notification-template-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-right),
.notification-template-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-left),
.notification-template-container :deep(tr:hover td.ant-table-cell-fix-right),
.notification-template-container :deep(tr:hover td.ant-table-cell-fix-left),
.notification-template-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-right"]),
.notification-template-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-left"]),
.notification-template-container :deep(tr:hover [class*="ant-table-cell-fix-right"]),
.notification-template-container :deep(tr:hover [class*="ant-table-cell-fix-left"]) {
  background: var(--accent-color-lighter, #fffbf0) !important;
  background-color: var(--accent-color-lighter, #fffbf0) !important;
  background-image: none !important;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .notification-template-container {
    padding: 0;
  }
  
  .notification-template-container :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .notification-template-container :deep(.ant-card-body) {
    padding: 16px 12px;
  }
  
  .notification-template-container :deep(.ant-form) {
    flex-direction: column;
  }
  
  .notification-template-container :deep(.ant-form-item) {
    width: 100%;
    margin-bottom: 12px;
  }
  
  .notification-template-container :deep(.ant-input),
  .notification-template-container :deep(.ant-select),
  .notification-template-container :deep(.ant-input-number) {
    width: 100% !important;
  }
  
  .notification-template-container :deep(.ant-table) {
    font-size: 12px;
  }
  
  .notification-template-container :deep(.ant-table-thead > tr > th) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .notification-template-container :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .notification-template-container :deep(.ant-table-scroll) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .notification-template-container :deep(.ant-btn) {
    height: 32px;
    padding: 0 12px;
    font-size: 12px;
  }
  
  .notification-template-container :deep(.ant-switch) {
    min-width: 44px;
    height: 24px;
  }
  
  .notification-template-container :deep(.ant-modal) {
    max-width: 90%;
    margin: 20px auto;
  }
  
  .notification-template-container :deep(.ant-modal-content) {
    padding: 16px;
  }
  
  .notification-template-container :deep(.ant-input-number) {
    width: 100% !important;
  }
}

/* 模板弹窗移动端优化 */
:deep(.template-modal .ant-modal) {
  margin: 0;
  max-width: 100vw;
  top: 0;
  padding-bottom: 0;
}

:deep(.template-modal .ant-modal-content) {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

:deep(.template-modal .ant-modal-body) {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

:deep(.template-modal .ant-modal-footer) {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

@media (min-width: 769px) {
  :deep(.template-modal .ant-modal) {
    margin: 0 auto;
    top: 50px;
    padding-bottom: 24px;
  }
  
  :deep(.template-modal .ant-modal-content) {
    height: auto;
  }
  
  :deep(.template-modal .ant-modal-body) {
    padding: 24px;
  }
  
  :deep(.template-modal .ant-modal-footer) {
    padding: 10px 16px;
  }
}

@media (max-width: 480px) {
  .notification-template-container :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .notification-template-container :deep(.ant-card-body) {
    padding: 12px 8px;
  }
  
  .notification-template-container :deep(.ant-table-thead > tr > th),
  .notification-template-container :deep(.ant-table-tbody > tr > td) {
    padding: 6px 2px;
    font-size: 10px;
  }
}
</style>
