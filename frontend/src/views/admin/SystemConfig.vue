<template>
  <div class="system-config-container">
    <a-tabs
      v-model:active-key="activeTab"
      type="card"
    >
      <!-- 品牌配置 Tab -->
      <a-tab-pane
        key="brand"
        tab="品牌配置"
      >
        <a-row :gutter="[16, 16]">
          <a-col
            :xs="24"
            :lg="12"
          >
            <a-card
              title="系统名称"
              :bordered="false"
              class="config-card"
            >
              <a-form layout="vertical">
                <a-form-item label="系统全称（浏览器标题）">
                  <a-input
                    v-model:value="brandConfig.appName"
                    placeholder="如：XX律师事务所客户服务系统"
                    @blur="saveBrandConfig('system.app-name', brandConfig.appName)"
                  />
                  <div class="field-hint">
                    显示在浏览器标签页标题
                  </div>
                </a-form-item>
                <a-form-item label="系统简称（中文）">
                  <a-input
                    v-model:value="brandConfig.appShortName"
                    placeholder="如：XX律所客服"
                    @blur="saveBrandConfig('system.app-short-name', brandConfig.appShortName)"
                  />
                  <div class="field-hint">
                    显示在管理后台侧边栏
                  </div>
                </a-form-item>
                <a-form-item label="系统简称（英文）">
                  <a-input
                    v-model:value="brandConfig.appShortNameEn"
                    placeholder="如：XX Law Service"
                    @blur="saveBrandConfig('system.app-short-name-en', brandConfig.appShortNameEn)"
                  />
                  <div class="field-hint">
                    显示在管理后台侧边栏下方
                  </div>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
          <a-col
            :xs="24"
            :lg="12"
          >
            <a-card
              title="Logo 设置"
              :bordered="false"
              class="config-card"
            >
              <a-form layout="vertical">
                <a-form-item label="Logo 图片地址">
                  <a-input
                    v-model:value="brandConfig.logoUrl"
                    placeholder="如：/logo.png 或 https://..."
                    @blur="saveBrandConfig('system.logo-url', brandConfig.logoUrl)"
                  />
                  <div class="field-hint">
                    支持相对路径或完整 URL
                  </div>
                </a-form-item>
                <a-form-item label="Logo 预览">
                  <div class="logo-preview">
                    <img
                      v-if="brandConfig.logoUrl"
                      :src="brandConfig.logoUrl"
                      alt="Logo预览"
                      @error="handleLogoError"
                    >
                    <div
                      v-else
                      class="logo-placeholder"
                    >
                      暂未设置 Logo
                    </div>
                  </div>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>

      <!-- 门户配置 Tab -->
      <a-tab-pane
        key="portal"
        tab="门户配置"
      >
        <a-row :gutter="[16, 16]">
          <a-col
            :xs="24"
            :lg="12"
          >
            <a-card
              title="律所信息"
              :bordered="false"
              class="config-card"
            >
              <a-form layout="vertical">
                <a-form-item label="律所名称">
                  <a-input
                    v-model:value="portalConfig.lawFirmName"
                    placeholder="如：XX律师事务所"
                    @blur="savePortalConfig('system.law-firm-name', portalConfig.lawFirmName)"
                  />
                  <div class="field-hint">
                    显示在门户页面头部和底部
                  </div>
                </a-form-item>
                <a-form-item label="律所官网">
                  <a-input
                    v-model:value="portalConfig.lawFirmWebsite"
                    placeholder="如：https://www.example.com"
                    @blur="savePortalConfig('system.law-firm-website', portalConfig.lawFirmWebsite)"
                  />
                  <div class="field-hint">
                    点击律所名称时跳转的链接
                  </div>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
          <a-col
            :xs="24"
            :lg="12"
          >
            <a-card
              title="页面内容"
              :bordered="false"
              class="config-card"
            >
              <a-form layout="vertical">
                <a-form-item label="首页标语">
                  <a-input
                    v-model:value="portalConfig.appSlogan"
                    placeholder="如：专业 · 诚信 · 高效"
                    @blur="savePortalConfig('system.app-slogan', portalConfig.appSlogan)"
                  />
                  <div class="field-hint">
                    显示在门户欢迎区域
                  </div>
                </a-form-item>
                <a-form-item label="ICP 备案号">
                  <a-input
                    v-model:value="portalConfig.icpLicense"
                    placeholder="如：京ICP备XXXXXXXX号"
                    @blur="savePortalConfig('system.icp-license', portalConfig.icpLicense)"
                  />
                  <div class="field-hint">
                    显示在页面底部，可选
                  </div>
                </a-form-item>
                <a-form-item label="版权信息">
                  <a-input
                    v-model:value="portalConfig.copyright"
                    placeholder="如：© 2024 XX律师事务所"
                    @blur="savePortalConfig('system.copyright', portalConfig.copyright)"
                  />
                  <div class="field-hint">
                    显示在页面底部
                  </div>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>

      <!-- 系统配置 Tab -->
      <a-tab-pane
        key="system"
        tab="系统配置"
      >
        <a-row :gutter="[16, 16]">
          <a-col
            :xs="24"
            :lg="12"
          >
            <a-card
              title="系统地址"
              :bordered="false"
              class="config-card"
            >
              <a-form layout="vertical">
                <a-form-item label="系统基础 URL（必填）">
                  <a-input
                    v-model:value="systemConfig.baseUrl"
                    placeholder="如：https://client.yourfirm.com"
                    @blur="saveSystemConfig('system.base-url', systemConfig.baseUrl)"
                  />
                  <div class="field-hint">
                    客户服务系统的外网访问地址，用于生成客户访问链接
                    <br>
                    <span class="hint-example">示例：https://client.yourfirm.com 或 http://192.168.1.100:8080</span>
                  </div>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
          <a-col
            :xs="24"
            :lg="12"
          >
            <a-card
              title="回调配置"
              :bordered="false"
              class="config-card"
            >
              <a-form layout="vertical">
                <a-form-item label="启用回调">
                  <a-switch
                    v-model:checked="systemConfig.callbackEnabled"
                    checked-children="开"
                    un-checked-children="关"
                    @change="saveSystemConfig('callback.enabled', systemConfig.callbackEnabled ? 'true' : 'false')"
                  />
                  <div class="field-hint">
                    当客户访问项目或下载文件时，通知律所系统记录日志
                  </div>
                </a-form-item>
                <a-form-item
                  v-if="systemConfig.callbackEnabled"
                  label="律所系统地址"
                >
                  <a-input
                    v-model:value="systemConfig.callbackUrl"
                    placeholder="如：http://192.168.1.100/api"
                    @blur="saveSystemConfig('callback.law-firm-url', systemConfig.callbackUrl)"
                  />
                </a-form-item>
                <a-form-item
                  v-if="systemConfig.callbackEnabled"
                  label="回调密钥"
                >
                  <a-input-password
                    v-model:value="systemConfig.callbackApiKey"
                    placeholder="与律所系统约定的密钥"
                    @blur="saveSystemConfig('callback.api-key', systemConfig.callbackApiKey)"
                  />
                </a-form-item>
                <a-form-item
                  v-if="systemConfig.callbackEnabled"
                  label="允许内网回调"
                >
                  <a-switch
                    v-model:checked="systemConfig.callbackAllowInternal"
                    checked-children="是"
                    un-checked-children="否"
                    @change="saveSystemConfig('callback.allow-internal', systemConfig.callbackAllowInternal ? 'true' : 'false')"
                  />
                  <div class="field-hint">
                    <span v-if="systemConfig.callbackAllowInternal">
                      当前允许回调到内网地址（如 192.168.x.x、localhost），适用于企业内网部署
                    </span>
                    <span v-else>
                      当前禁止回调到内网地址，仅允许公网地址，用于防止 SSRF 攻击
                    </span>
                  </div>
                </a-form-item>
              </a-form>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>

      <!-- 高级配置 Tab -->
      <a-tab-pane
        key="advanced"
        tab="高级配置"
      >
        <a-card :bordered="false">
          <template #title>
            <span>所有配置项</span>
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
                新增配置
              </a-button>
              <a-button @click="loadData">
                <template #icon>
                  <ReloadOutlined />
                </template>
                刷新
              </a-button>
            </a-space>
          </template>

          <a-form
            :model="searchForm"
            layout="inline"
            style="margin-bottom: 16px"
            @finish="handleSearch"
          >
            <a-form-item label="配置键">
              <a-input
                v-model:value="searchForm.configKey"
                placeholder="请输入配置键"
                allow-clear
                style="width: 200px"
              />
            </a-form-item>
            <a-form-item label="配置类型">
              <a-select
                v-model:value="searchForm.configType"
                placeholder="请选择配置类型"
                allow-clear
                style="width: 150px"
              >
                <a-select-option value="STRING">
                  字符串
                </a-select-option>
                <a-select-option value="NUMBER">
                  数字
                </a-select-option>
                <a-select-option value="BOOLEAN">
                  布尔值
                </a-select-option>
                <a-select-option value="JSON">
                  JSON
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
              <template v-if="column.key === 'configValue'">
                <template v-if="record.configType === 'BOOLEAN'">
                  <a-tag :color="record.configValue === 'true' ? 'green' : 'red'">
                    {{ record.configValue === 'true' ? '是' : '否' }}
                  </a-tag>
                </template>
                <template v-else-if="record.configType === 'JSON'">
                  <a-typography-paragraph
                    :content="record.configValue"
                    :ellipsis="{ rows: 1 }"
                    code
                    style="margin: 0; max-width: 200px"
                  />
                </template>
                <template v-else>
                  <span style="word-break: break-all">{{ record.configValue }}</span>
                </template>
              </template>
              <template v-else-if="column.key === 'configType'">
                <a-tag>{{ getConfigTypeName(record.configType) }}</a-tag>
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
                    title="确定要删除这个配置吗？"
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
      </a-tab-pane>
    </a-tabs>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="showCreateModal"
      :title="editingRecord ? '编辑配置' : '新增配置'"
      :width="modalWidth"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        :model="formData"
        layout="vertical"
      >
        <a-form-item
          label="配置键"
          required
        >
          <a-input
            v-model:value="formData.configKey"
            placeholder="请输入配置键"
            :disabled="!!editingRecord"
          />
        </a-form-item>
        <a-form-item
          label="配置值"
          required
        >
          <a-textarea
            v-if="formData.configType === 'JSON'"
            v-model:value="formData.configValue"
            placeholder="请输入 JSON 格式的配置值"
            :rows="4"
          />
          <a-select
            v-else-if="formData.configType === 'BOOLEAN'"
            v-model:value="formData.configValue"
            placeholder="请选择"
          >
            <a-select-option value="true">
              是
            </a-select-option>
            <a-select-option value="false">
              否
            </a-select-option>
          </a-select>
          <a-input-number
            v-else-if="formData.configType === 'NUMBER'"
            v-model:value="formData.configValue"
            placeholder="请输入数字"
            style="width: 100%"
          />
          <a-input
            v-else
            v-model:value="formData.configValue"
            placeholder="请输入配置值"
          />
        </a-form-item>
        <a-form-item
          label="配置类型"
          required
        >
          <a-select
            v-model:value="formData.configType"
            placeholder="请选择配置类型"
          >
            <a-select-option value="STRING">
              字符串
            </a-select-option>
            <a-select-option value="NUMBER">
              数字
            </a-select-option>
            <a-select-option value="BOOLEAN">
              布尔值
            </a-select-option>
            <a-select-option value="JSON">
              JSON
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入配置描述"
            :rows="2"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 保存状态提示 -->
    <div
      v-if="saveStatus"
      class="save-status"
      :class="saveStatus"
    >
      {{ saveStatus === 'saving' ? '保存中...' : saveStatus === 'saved' ? '已保存' : '保存失败' }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import logger from '@/utils/logger'
import type { TablePaginationConfig } from 'ant-design-vue'
import { ReloadOutlined, PlusOutlined } from '@ant-design/icons-vue'

// 定时器引用（组件卸载时清理，防止内存泄漏）
let saveStatusTimer: number | undefined
import {
  getConfigList,
  saveConfig,
  updateConfig,
  deleteConfig,
  getPortalConfig,
  getBrandConfig,
  type SysConfigInfo,
  type SaveConfigRequest,
  type UpdateConfigRequest,
} from '@/api/config'

const activeTab = ref('brand')
const loading = ref(false)
const dataSource = ref<SysConfigInfo[]>([])
const showCreateModal = ref(false)
const editingRecord = ref<SysConfigInfo | null>(null)
const saveStatus = ref<'saving' | 'saved' | 'error' | ''>('')

// 响应式宽度
const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => windowWidth.value < 768 ? '95%' : '600px')

function handleResize() {
  windowWidth.value = window.innerWidth
}

// 品牌配置
const brandConfig = reactive({
  appName: '',
  appShortName: '',
  appShortNameEn: '',
  logoUrl: '',
})

// 门户配置
const portalConfig = reactive({
  lawFirmName: '',
  lawFirmWebsite: '',
  appSlogan: '',
  icpLicense: '',
  copyright: '',
})

// 系统配置
const systemConfig = reactive({
  baseUrl: '',
  callbackEnabled: false,
  callbackUrl: '',
  callbackApiKey: '',
  callbackAllowInternal: true,
})

// 搜索表单
const searchForm = ref({
  configKey: '',
  configType: '',
  limit: 100,
})

// 表单数据
const formData = ref<SaveConfigRequest & UpdateConfigRequest & { configType?: string }>({
  configKey: '',
  configValue: '',
  configType: 'STRING',
  description: '',
})

// 分页
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

// 表格列定义
const columns = [
  { title: '配置键', key: 'configKey', dataIndex: 'configKey', ellipsis: true, width: 180 },
  { title: '配置值', key: 'configValue', ellipsis: true, width: 200 },
  { title: '类型', key: 'configType', width: 80 },
  { title: '描述', key: 'description', dataIndex: 'description', ellipsis: true, width: 150 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const },
]

// 加载配置数据
async function loadConfigData() {
  try {
    // 加载品牌配置
    const brandRes = await getBrandConfig().catch(() => null)
    if (brandRes?.success && brandRes.data) {
      brandConfig.appName = brandRes.data.appName || ''
      brandConfig.appShortName = brandRes.data.appShortName || ''
      brandConfig.appShortNameEn = brandRes.data.appShortNameEn || ''
      brandConfig.logoUrl = brandRes.data.logoUrl || ''
    }

    // 加载门户配置
    const portalRes = await getPortalConfig().catch(() => null)
    if (portalRes?.success && portalRes.data) {
      portalConfig.lawFirmName = portalRes.data.lawFirmName || ''
      portalConfig.lawFirmWebsite = portalRes.data.lawFirmWebsite || ''
      portalConfig.appSlogan = portalRes.data.appSlogan || ''
      portalConfig.icpLicense = portalRes.data.icpLicense || ''
      portalConfig.copyright = portalRes.data.copyright || ''
    }

    // 加载系统配置（从配置列表中获取）
    const configRes = await getConfigList({ limit: 100 })
    if (configRes.data) {
      const configs = configRes.data
      const findConfig = (key: string) => configs.find(c => c.configKey === key)?.configValue || ''

      systemConfig.baseUrl = findConfig('system.base-url')
      systemConfig.callbackEnabled = findConfig('callback.enabled') === 'true'
      systemConfig.callbackUrl = findConfig('callback.law-firm-url')
      systemConfig.callbackApiKey = findConfig('callback.api-key')
      const allowInternalValue = findConfig('callback.allow-internal')
      systemConfig.callbackAllowInternal = allowInternalValue === '' || allowInternalValue === 'true'
    }
  } catch (error) {
    logger.error('加载配置失败:', error instanceof Error ? error.message : '未知错误')
  }
}

// 保存单个配置
async function saveConfigItem(key: string, value: string, description?: string) {
  if (!value && value !== '') return // 空值时不保存

  saveStatus.value = 'saving'
  try {
    // 先查找是否存在
    const existing = dataSource.value.find(c => c.configKey === key)
    if (existing) {
      await updateConfig(existing.id, { configValue: value })
    } else {
      await saveConfig({
        configKey: key,
        configValue: value,
        configType: 'STRING',
        description: description || key,
      })
      // 重新加载列表以获取新记录
      await loadData()
    }
    saveStatus.value = 'saved'
    saveStatusTimer = window.setTimeout(() => { saveStatus.value = '' }, 2000)
  } catch (error) {
    saveStatus.value = 'error'
    const errorMessage = error instanceof Error ? error.message : '保存失败'
    message.error(errorMessage)
    saveStatusTimer = window.setTimeout(() => { saveStatus.value = '' }, 3000)
  }
}

// 保存品牌配置
function saveBrandConfig(key: string, value: string) {
  const descriptions: Record<string, string> = {
    'system.app-name': '系统名称（浏览器标题）',
    'system.app-short-name': '系统简称（侧边栏）',
    'system.app-short-name-en': '系统英文简称',
    'system.logo-url': 'Logo图片地址',
  }
  saveConfigItem(key, value, descriptions[key])
}

// 保存门户配置
function savePortalConfig(key: string, value: string) {
  const descriptions: Record<string, string> = {
    'system.law-firm-name': '律所名称',
    'system.law-firm-website': '律所官网',
    'system.app-slogan': '首页标语',
    'system.icp-license': 'ICP备案号',
    'system.copyright': '版权信息',
  }
  saveConfigItem(key, value, descriptions[key])
}

// 保存系统配置
function saveSystemConfig(key: string, value: string) {
  const descriptions: Record<string, string> = {
    'system.base-url': '系统基础URL',
    'callback.enabled': '是否启用回调',
    'callback.law-firm-url': '律所系统回调地址',
    'callback.api-key': '回调密钥',
    'callback.allow-internal': '允许回调到内网地址（企业内网部署设为true）',
  }
  saveConfigItem(key, value, descriptions[key])
}

// Logo 加载失败处理
function handleLogoError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

// 加载配置列表
async function loadData() {
  loading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (searchForm.value.configKey) params.configKey = searchForm.value.configKey
    if (searchForm.value.configType) params.configType = searchForm.value.configType
    if (searchForm.value.limit) params.limit = searchForm.value.limit

    const res = await getConfigList(params)
    dataSource.value = res.data || []
    pagination.value.total = dataSource.value.length
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '加载配置列表失败'
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
  searchForm.value = { configKey: '', configType: '', limit: 100 }
  handleSearch()
}

function handleTableChange(pag: TablePaginationConfig) {
  if (pag.current) pagination.value.current = pag.current
  if (pag.pageSize) pagination.value.pageSize = pag.pageSize
}

function handleEdit(record: SysConfigInfo) {
  editingRecord.value = record
  formData.value = {
    configKey: record.configKey,
    configValue: String(record.configValue),
    configType: record.configType,
    description: record.description,
  }
  showCreateModal.value = true
}

async function handleDelete(record: SysConfigInfo) {
  try {
    await deleteConfig(record.id)
    message.success('配置已删除')
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '删除失败'
    message.error(errorMessage)
  }
}

async function handleSubmit() {
  if (!formData.value.configKey) {
    message.warning('请填写配置键')
    return
  }
  if (!formData.value.configValue && formData.value.configValue !== '') {
    message.warning('请填写配置值')
    return
  }

  try {
    if (editingRecord.value) {
      await updateConfig(editingRecord.value.id, {
        configValue: String(formData.value.configValue),
        configType: formData.value.configType,
        description: formData.value.description,
      })
      message.success('配置已更新')
    } else {
      await saveConfig({
        configKey: formData.value.configKey,
        configValue: String(formData.value.configValue),
        configType: formData.value.configType || 'STRING',
        description: formData.value.description,
      })
      message.success('配置已创建')
    }
    handleCancel()
    await loadData()
    await loadConfigData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '操作失败'
    message.error(errorMessage)
  }
}

function handleCancel() {
  showCreateModal.value = false
  editingRecord.value = null
  formData.value = { configKey: '', configValue: '', configType: 'STRING', description: '' }
}

function getConfigTypeName(type: string): string {
  const typeNames: Record<string, string> = {
    STRING: '字符串',
    NUMBER: '数字',
    BOOLEAN: '布尔值',
    JSON: 'JSON',
  }
  return typeNames[type] || type
}

onMounted(() => {
  loadData()
  loadConfigData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (saveStatusTimer !== undefined) {
    clearTimeout(saveStatusTimer)
  }
})
</script>

<style scoped>
.system-config-container {
  padding: 16px;
  max-width: 1400px;
  margin: 0 auto;
}

.config-card {
  height: 100%;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.config-card :deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  min-height: 48px;
}

.config-card :deep(.ant-card-head-title) {
  font-weight: 600;
  color: #1890ff;
}

.config-card :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.config-card :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.field-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.5;
}

.hint-example {
  color: #1890ff;
}

.logo-preview {
  width: 100%;
  min-height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  padding: 16px;
}

.logo-preview img {
  max-width: 100%;
  max-height: 120px;
  object-fit: contain;
}

.logo-placeholder {
  color: #999;
  font-size: 14px;
}

.save-status {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.save-status.saving {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.save-status.saved {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.save-status.error {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .system-config-container {
    padding: 12px;
  }

  .config-card {
    margin-bottom: 12px;
  }

  :deep(.ant-tabs-nav) {
    margin-bottom: 12px;
  }

  :deep(.ant-tabs-tab) {
    padding: 8px 12px !important;
    font-size: 14px;
  }

  :deep(.ant-form-item-label) {
    padding-bottom: 4px;
  }

  :deep(.ant-table) {
    font-size: 12px;
  }

  .save-status {
    bottom: 16px;
    right: 16px;
    left: 16px;
    text-align: center;
  }
}

@media (max-width: 480px) {
  :deep(.ant-tabs-tab) {
    padding: 6px 8px !important;
    font-size: 13px;
  }
}
</style>
