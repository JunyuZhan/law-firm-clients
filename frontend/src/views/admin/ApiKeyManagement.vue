<template>
  <div class="api-key-management-container">
    <a-row
      :gutter="16"
      class="equal-height-row"
    >
      <!-- 左侧：API 对接信息 -->
      <a-col
        :xs="24"
        :lg="12"
        class="equal-height-col"
      >
        <a-card
          size="small"
          class="equal-height-card"
        >
          <template #title>
            <span>API 对接信息（律所系统 → 客服系统）</span>
          </template>
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
                style="color: #999"
              >
                未配置（请在「系统配置」中设置 system.base-url）
              </span>
              <div style="color: #999; font-size: 12px; margin-top: 4px">
                注：管理系统配置时直接使用此地址，无需添加 /api
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="认证方式">
              <div><code>X-API-Key: {API密钥}</code></div>
              <div><code>X-API-Secret: {API密钥Secret}</code></div>
            </a-descriptions-item>
            <a-descriptions-item label="使用说明">
              <ol style="margin: 0; padding-left: 20px">
                <li>在下方创建 API 密钥</li>
                <li>将 <b>API 地址</b> 和 <b>API 密钥</b> 提供给律所系统管理员</li>
                <li>律所系统在「外部系统集成 → 客户服务系统」中配置</li>
              </ol>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>

      <!-- 右侧：回调配置指引 -->
      <a-col
        :xs="24"
        :lg="12"
        class="equal-height-col"
      >
        <a-card
          size="small"
          class="equal-height-card"
        >
          <template #title>
            <span>回调配置（客服系统 → 律所系统）</span>
          </template>
          <a-descriptions
            :column="1"
            size="small"
            bordered
          >
            <a-descriptions-item label="功能说明">
              客户访问项目或下载文件时，自动通知律所系统记录日志
            </a-descriptions-item>
            <a-descriptions-item label="配置项">
              <ul style="margin: 0; padding-left: 20px; line-height: 1.8">
                <li><b>启用回调</b>：开启/关闭回调功能</li>
                <li><b>律所系统地址</b>：律所系统提供的回调接收地址</li>
                <li><b>回调密钥</b>：需与律所系统配置一致</li>
              </ul>
            </a-descriptions-item>
          </a-descriptions>
          <div style="margin-top: 12px">
            <router-link to="/admin/config">
              <a-button type="default">
                <template #icon>
                  <SettingOutlined />
                </template>
                前往「系统配置」页面配置
              </a-button>
            </router-link>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-card>
      <template #title>
        <span>API密钥管理</span>
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
            新增密钥
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
            <span
              v-else
              style="color: #999"
            >-</span>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              @change="(checked: boolean) => handleToggleEnabled(record, checked)"
            />
          </template>
          <template v-else-if="column.key === 'expiresAt'">
            <span :style="{ color: isExpired(record.expiresAt) ? '#cf1322' : '' }">
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
                title="确定要删除这个API密钥吗？"
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
      :title="editingRecord ? '编辑API密钥' : '新增API密钥'"
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
            placeholder="请输入密钥名称"
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
                <div style="margin-bottom: 8px">
                  <strong>Secret 仅在创建时显示一次完整值</strong>
                </div>
                <div style="font-size: 12px; color: #666; margin-bottom: 8px">
                  出于安全考虑，编辑时无法查看完整 Secret。
                </div>
                <div style="font-size: 12px; color: #666">
                  <strong>管理系统对接说明：</strong>
                </div>
                <ol style="margin: 8px 0 0 20px; font-size: 12px; color: #666">
                  <li>创建密钥时，在成功弹窗中会显示完整的 Key 和 Secret</li>
                  <li>请立即复制保存，关闭弹窗后将无法再次查看</li>
                  <li>如果忘记 Secret，需要删除当前密钥并重新创建</li>
                </ol>
              </div>
            </template>
          </a-alert>
        </a-form-item>
        <a-form-item label="过期时间">
          <a-date-picker
            v-model:value="formData.expiresAt"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择过期时间（留空表示永不过期）"
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

    <!-- 创建成功弹窗 -->
    <a-modal
      v-model:open="showSuccessModal"
      title="API 密钥创建成功"
      :footer="null"
      :closable="true"
      :mask-closable="false"
      :width="modalWidth"
      wrap-class-name="apikey-success-modal"
    >
      <a-alert
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #message>
          请立即复制保存以下密钥信息，关闭后将无法再次查看 Secret！
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
      <div style="margin-top: 16px; text-align: center">
        <a-button
          type="primary"
          @click="showSuccessModal = false"
        >
          我已保存，关闭
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined, PlusOutlined, SettingOutlined } from '@ant-design/icons-vue'
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

const loading = ref(false)
const submitLoading = ref(false)
const dataSource = ref<ApiKeyInfo[]>([])
const showCreateModal = ref(false)
const showSuccessModal = ref(false)
const editingRecord = ref<ApiKeyInfo | null>(null)

// 响应式宽度
const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => {
  return windowWidth.value < 768 ? '95%' : '600px'
})

// 监听窗口大小变化
function handleResize() {
  windowWidth.value = window.innerWidth
}

// API Secret 脱敏显示（仅展示前4位和后4位）
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

// 表格列定义
const columns = [
  { title: 'ID', key: 'id', dataIndex: 'id', width: 80, align: 'center' },
  { title: '密钥名称', key: 'keyName', dataIndex: 'keyName', ellipsis: true, width: 150, align: 'center' },
  { title: 'API密钥', key: 'apiKey', ellipsis: true, width: 280, align: 'center' },
  { title: 'API密钥Secret', key: 'apiSecret', ellipsis: true, width: 280, align: 'center' },
  { title: '状态', key: 'enabled', width: 80, align: 'center' },
  { title: '过期时间', key: 'expiresAt', width: 160, align: 'center' },
  { title: '操作', key: 'action', width: 150, align: 'center', fixed: 'right' },
]

/**
 * 统一的配置加载错误处理
 * 配置加载失败不阻塞页面，使用默认值并记录警告
 */
function handleConfigLoadError(configName: string, error: unknown): void {
  const errorMessage = error instanceof Error ? error.message : String(error)
  logger.warn(`加载${configName}失败: ${errorMessage}`)
}

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback
}

// 加载 API 基础地址
async function loadApiBaseUrl() {
  try {
    const res = await getConfigList({ configKey: 'system.base-url', limit: 1 })
    const config = res.data?.find(c => c.configKey === 'system.base-url')
    if (config?.configValue) {
      // 移除末尾的斜杠
      apiBaseUrl.value = config.configValue.replace(/\/+$/, '')
    }
  } catch (error) {
    handleConfigLoadError('系统基础URL', error)
  }
}

// 加载数据
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
    message.error(getErrorMessage(error, '加载API密钥列表失败'))
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
function handleEdit(record: ApiKeyInfo) {
  editingRecord.value = record
  formData.value = {
    keyName: record.keyName,
    expiresAt: record.expiresAt ? dayjs(record.expiresAt) : undefined,
    enabled: record.enabled,
  }
  showCreateModal.value = true
}

// 切换启用状态
async function handleToggleEnabled(record: ApiKeyInfo, enabled: boolean) {
  try {
    await updateApiKey(record.id, { enabled })
    message.success(enabled ? 'API密钥已启用' : 'API密钥已禁用')
    await loadData()
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '更新失败'))
    await loadData() // 刷新以恢复原状态
  }
}

// 删除
async function handleDelete(record: ApiKeyInfo) {
  try {
    await deleteApiKey(record.id)
    message.success('API密钥已删除')
    await loadData()
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '删除失败'))
  }
}

// 提交表单
async function handleSubmit() {
  if (!formData.value.keyName) {
    message.warning('请填写密钥名称')
    return
  }
  if (formData.value.keyName.length > 50) {
    message.warning('密钥名称不能超过50个字符')
    return
  }
  if (!/^[\u4e00-\u9fa5a-zA-Z0-9_\-\s]+$/.test(formData.value.keyName)) {
    message.warning('密钥名称只能包含中英文、数字、下划线和横杠')
    return
  }

  submitLoading.value = true
  try {
    if (editingRecord.value) {
      // 更新
      const updateData: UpdateApiKeyRequest = {}
      if (formData.value.keyName) updateData.keyName = formData.value.keyName
      if (formData.value.enabled !== undefined) updateData.enabled = formData.value.enabled
      if (formData.value.expiresAt !== undefined) {
        updateData.expiresAt = dayjs.isDayjs(formData.value.expiresAt)
          ? formData.value.expiresAt.toISOString()
          : formData.value.expiresAt
      }

      if (!editingRecord.value?.id) {
        message.error('无法获取密钥ID')
        return
      }
      await updateApiKey(editingRecord.value.id, updateData)
      message.success('API密钥已更新')
    } else {
      // 创建（不传备注信息，后端不存储）
      const createData: CreateApiKeyRequest = {
        keyName: formData.value.keyName!,
      }
      if (formData.value.expiresAt) {
        createData.expiresAt = dayjs.isDayjs(formData.value.expiresAt)
          ? formData.value.expiresAt.toISOString()
          : formData.value.expiresAt
      }

      const res = await createApiKey(createData)
      // 显示完整密钥弹窗（仅创建时）
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
    message.error(getErrorMessage(error, '操作失败'))
  } finally {
    submitLoading.value = false
  }
}

// 取消
function handleCancel() {
  showCreateModal.value = false
  editingRecord.value = null
  formData.value = {
    keyName: '',
    expiresAt: undefined,
    enabled: true,
  }
}

// 判断是否过期
function isExpired(expiresAt?: string): boolean {
  if (!expiresAt) return false
  return dayjs(expiresAt).isBefore(dayjs())
}

// 格式化日期（API密钥专用，空值显示"永不过期"）
function formatApiKeyDate(date?: string): string {
  if (!date) return '永不过期'
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
  padding: 0;
}

/* 等高卡片布局 */
.equal-height-row {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.equal-height-col {
  display: flex;
  margin-bottom: 16px;
}

.equal-height-card {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.equal-height-card :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.equal-height-card :deep(.ant-form) {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.api-key-management-container :deep(.ant-card) {
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.api-key-management-container :deep(.ant-card-head) {
  padding: 16px 20px;
}

.api-key-management-container :deep(.ant-card-body) {
  padding: 20px;
}

.api-key-management-container :deep(.ant-form) {
  margin-bottom: 16px;
}

.api-key-management-container :deep(.ant-table) {
  font-size: 14px;
}

/* 确保操作列按钮可见 - 操作列是固定列 */
.api-key-management-container :deep(.ant-table-cell-fix-right) {
  min-width: 150px !important;
}

.api-key-management-container :deep(.ant-table-cell-fix-right .ant-space) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  flex-wrap: nowrap !important;
  gap: 8px !important;
}

.api-key-management-container :deep(.ant-table-cell-fix-right .ant-btn) {
  display: inline-block !important;
  visibility: visible !important;
  opacity: 1 !important;
  flex-shrink: 0 !important;
  white-space: nowrap !important;
}

/* 固定列背景色 - 最强覆盖（包括内联样式） */
/* 表体固定列 */
.api-key-management-container :deep(.ant-table-cell-fix-right),
.api-key-management-container :deep(.ant-table-cell-fix-left),
.api-key-management-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-right),
.api-key-management-container :deep(.ant-table-tbody > tr > td.ant-table-cell-fix-left),
.api-key-management-container :deep(td.ant-table-cell-fix-right),
.api-key-management-container :deep(td.ant-table-cell-fix-left),
.api-key-management-container :deep([class*="ant-table-cell-fix-right"]),
.api-key-management-container :deep([class*="ant-table-cell-fix-left"]) {
  background: #fff !important;
  background-color: #fff !important;
  background-image: none !important;
}

/* 表头固定列 */
.api-key-management-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-right),
.api-key-management-container :deep(.ant-table-thead > tr > th.ant-table-cell-fix-left),
.api-key-management-container :deep(th.ant-table-cell-fix-right),
.api-key-management-container :deep(th.ant-table-cell-fix-left) {
  background: #fafafa !important;
  background-color: #fafafa !important;
  background-image: none !important;
}

/* 表体固定列 hover - 最高优先级 */
.api-key-management-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-right),
.api-key-management-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td.ant-table-cell-fix-left),
.api-key-management-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-right"]),
.api-key-management-container :deep(.ant-table-tbody > tr.ant-table-row:hover > td[class*="ant-table-cell-fix-left"]),
.api-key-management-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
.api-key-management-container :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
.api-key-management-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-right),
.api-key-management-container :deep(tr.ant-table-row:hover td.ant-table-cell-fix-left),
.api-key-management-container :deep(tr:hover td.ant-table-cell-fix-right),
.api-key-management-container :deep(tr:hover td.ant-table-cell-fix-left),
.api-key-management-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-right"]),
.api-key-management-container :deep(tr.ant-table-row:hover [class*="ant-table-cell-fix-left"]),
.api-key-management-container :deep(tr:hover [class*="ant-table-cell-fix-right"]),
.api-key-management-container :deep(tr:hover [class*="ant-table-cell-fix-left"]) {
  background: var(--accent-color-lighter, #fffbf0) !important;
  background-color: var(--accent-color-lighter, #fffbf0) !important;
  background-image: none !important;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .api-key-management-container {
    padding: 0;
  }
  
  .api-key-management-container :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .api-key-management-container :deep(.ant-card-body) {
    padding: 16px 12px;
  }
  
  .api-key-management-container :deep(.ant-form) {
    flex-direction: column;
  }
  
  .api-key-management-container :deep(.ant-form-item) {
    width: 100%;
    margin-bottom: 12px;
  }
  
  .api-key-management-container :deep(.ant-input),
  .api-key-management-container :deep(.ant-select) {
    width: 100% !important;
  }
  
  .api-key-management-container :deep(.ant-table) {
    font-size: 12px;
  }
  
  .api-key-management-container :deep(.ant-table-thead > tr > th) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .api-key-management-container :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
    font-size: 11px;
  }
  
  .api-key-management-container :deep(.ant-table-scroll) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .api-key-management-container :deep(.ant-btn) {
    height: 32px;
    padding: 0 12px;
    font-size: 12px;
  }
  
  .api-key-management-container :deep(.ant-modal) {
    max-width: 90%;
    margin: 20px auto;
  }
  
  .api-key-management-container :deep(.ant-modal-content) {
    padding: 16px;
  }
}

/* API密钥弹窗移动端优化 */
:deep(.apikey-modal .ant-modal),
:deep(.apikey-success-modal .ant-modal) {
  margin: 0;
  max-width: 100vw;
  top: 0;
  padding-bottom: 0;
}

:deep(.apikey-modal .ant-modal-content),
:deep(.apikey-success-modal .ant-modal-content) {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

:deep(.apikey-modal .ant-modal-body),
:deep(.apikey-success-modal .ant-modal-body) {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

:deep(.apikey-modal .ant-modal-footer) {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

@media (min-width: 769px) {
  :deep(.apikey-modal .ant-modal),
  :deep(.apikey-success-modal .ant-modal) {
    margin: 0 auto;
    top: 50px;
    padding-bottom: 24px;
  }
  
  :deep(.apikey-modal .ant-modal-content),
  :deep(.apikey-success-modal .ant-modal-content) {
    height: auto;
  }
  
  :deep(.apikey-modal .ant-modal-body),
  :deep(.apikey-success-modal .ant-modal-body) {
    padding: 24px;
  }
  
  :deep(.apikey-modal .ant-modal-footer) {
    padding: 10px 16px;
  }
}

@media (max-width: 480px) {
  .api-key-management-container :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .api-key-management-container :deep(.ant-card-body) {
    padding: 12px 8px;
  }
  
  .api-key-management-container :deep(.ant-table-thead > tr > th),
  .api-key-management-container :deep(.ant-table-tbody > tr > td) {
    padding: 6px 2px;
    font-size: 10px;
  }
}
</style>
