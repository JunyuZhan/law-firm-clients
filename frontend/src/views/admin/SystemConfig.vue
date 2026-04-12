<template>
  <div class="system-config-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          品牌、门户与系统参数；高级项在对应页签。
        </p>
      </div>

      <div class="intro-side">
        <div class="mini-stat">
          <span>品牌字段</span>
          <strong>{{ brandCompletion }}/4</strong>
        </div>
        <div class="mini-stat">
          <span>门户字段</span>
          <strong>{{ portalCompletion }}/8</strong>
        </div>
        <div class="mini-stat">
          <span>系统配置项</span>
          <strong>{{ dataSource.length }}</strong>
        </div>
      </div>
    </section>

    <section class="completion-strip config-card">
      <div class="completion-head">
        <h3>配置完整度</h3>
      </div>
      <div class="completion-grid">
        <div class="completion-item">
          <span>品牌配置</span>
          <strong>{{ brandCompletionRate }}</strong>
          <p>{{ brandCompletion }}/4</p>
        </div>
        <div class="completion-item">
          <span>门户配置</span>
          <strong>{{ portalCompletionRate }}</strong>
          <p>{{ portalCompletion }}/8</p>
        </div>
        <div class="completion-item">
          <span>系统配置项</span>
          <strong>{{ dataSource.length }}</strong>
        </div>
      </div>
    </section>

    <a-tabs
      v-model:active-key="activeTab"
      type="card"
      class="config-tabs"
    >
      <a-tab-pane
        key="brand"
        tab="品牌配置"
      >
        <div class="config-grid">
          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>系统名称</h3>
              </div>
            </div>
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
                  placeholder="如：XX Law Service（可选）"
                  @blur="saveBrandConfig('system.app-short-name-en', brandConfig.appShortNameEn)"
                />
                <div class="field-hint">
                  可选字段；如需双语品牌展示，可显示在管理后台侧边栏下方
                </div>
              </a-form-item>
            </a-form>
          </section>

          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>Logo 设置</h3>
              </div>
            </div>
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
          </section>
        </div>
      </a-tab-pane>

      <a-tab-pane
        key="portal"
        tab="门户配置"
      >
        <div class="config-grid">
          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>律所信息</h3>
              </div>
            </div>
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
          </section>

          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>页面内容</h3>
              </div>
            </div>
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
              <a-form-item label="门户页英文眉标（可选）">
                <a-input
                  v-model:value="portalConfig.portalEyebrowEn"
                  placeholder="留空则使用品牌配置中的「系统英文简称」"
                  @blur="savePortalConfig('system.portal-eyebrow-en', portalConfig.portalEyebrowEn)"
                />
                <div class="field-hint">
                  显示在公开门户页主标题上方
                </div>
              </a-form-item>
              <a-form-item label="门户页客户说明（公开页主文案）">
                <a-textarea
                  v-model:value="portalConfig.portalAccessNotice"
                  :rows="4"
                  placeholder="说明客户如何通过律师发送的链接使用本系统"
                  @blur="savePortalConfig('system.portal-access-notice', portalConfig.portalAccessNotice)"
                />
                <div class="field-hint">
                  显示在公开门户页中央；留空则不显示该段落
                </div>
              </a-form-item>
              <a-form-item label="工作人员入口（页脚小字链）">
                <a-input
                  v-model:value="portalConfig.staffEntryLabel"
                  placeholder="如：工作人员入口；留空则不显示"
                  @blur="savePortalConfig('system.staff-entry-label', portalConfig.staffEntryLabel)"
                />
                <div class="field-hint">
                  显示在公开门户页底部，链向管理后台登录；勿写敏感信息
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
          </section>
        </div>
      </a-tab-pane>

      <a-tab-pane
        key="system"
        tab="系统配置"
      >
        <div class="config-grid">
          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>系统地址</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item label="系统基础 URL（必填）">
                <a-input
                  v-model:value="systemConfig.baseUrl"
                  placeholder="如：https://client.yourfirm.com"
                  @blur="saveSystemConfig('system.base-url', systemConfig.baseUrl)"
                />
                <div class="field-hint">
                  客户服务系统的外网访问地址，用于生成客户访问链接。
                  <br>
                  <span class="hint-example">示例：https://client.yourfirm.com 或 http://192.168.1.100:8080</span>
                </div>
              </a-form-item>
            </a-form>
          </section>

          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>回调配置</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item label="启用回调">
                <a-switch
                  v-model:checked="systemConfig.callbackEnabled"
                  checked-children="开"
                  un-checked-children="关"
                  @change="saveSystemConfig('callback.enabled', systemConfig.callbackEnabled ? 'true' : 'false')"
                />
                <div class="field-hint">
                  当客户访问项目或下载文件时，回调管理系统记录访问日志。
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
                <div class="field-hint">
                  新主链路下通常无需手工填写。管理系统推送项目时会自动把自己的回调地址绑定到该项目；这里只作为历史项目或特殊覆盖场景兜底。
                </div>
              </a-form-item>
              <a-form-item
                v-if="systemConfig.callbackEnabled"
                label="回调密钥"
              >
                <a-input-password
                  v-model:value="systemConfig.callbackApiKey"
                  placeholder="与管理系统约定的回调校验密钥"
                  @blur="saveSystemConfig('callback.api-key', systemConfig.callbackApiKey)"
                />
                <div class="field-hint">
                  新主链路下通常优先复用当前项目来源 API key 的 Secret 作为回调校验；这里只作为旧链路或手工覆盖兜底。固定公网 IP 场景如需额外白名单控制，请在网关或反向代理侧配置。
                </div>
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
                  <span v-if="systemConfig.callbackAllowInternal">当前允许回调到内网地址，适用于企业内网部署。</span>
                  <span v-else>当前禁止回调到内网地址，仅允许公网地址。</span>
                </div>
              </a-form-item>
            </a-form>
          </section>
        </div>
      </a-tab-pane>

      <a-tab-pane
        key="advanced"
        tab="高级配置"
      >
        <section class="advanced-panel">
          <div class="advanced-header">
            <div>
              <h3>所有配置项</h3>
              <p>保留底层配置入口，便于管理未在可视化卡片中暴露的参数。</p>
            </div>
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
          </div>

          <a-form
            :model="searchForm"
            layout="inline"
            class="advanced-filter-form"
            @finish="handleSearch"
          >
            <a-form-item label="配置键">
              <a-input
                v-model:value="searchForm.configKey"
                placeholder="请输入配置键"
                allow-clear
                style="width: 220px"
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
                    class="json-preview"
                  />
                </template>
                <template v-else>
                  <span class="config-value">{{ record.configValue }}</span>
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
        </section>
      </a-tab-pane>
    </a-tabs>

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

let saveStatusTimer: number | undefined
const activeTab = ref('brand')
const loading = ref(false)
const dataSource = ref<SysConfigInfo[]>([])
const showCreateModal = ref(false)
const editingRecord = ref<SysConfigInfo | null>(null)
const saveStatus = ref<'saving' | 'saved' | 'error' | ''>('')

const windowWidth = ref(window.innerWidth)
const modalWidth = computed(() => windowWidth.value < 768 ? '95%' : '600px')

function handleResize() {
  windowWidth.value = window.innerWidth
}

const brandConfig = reactive({
  appName: '',
  appShortName: '',
  appShortNameEn: '',
  logoUrl: '',
})

const portalConfig = reactive({
  lawFirmName: '',
  lawFirmWebsite: '',
  appSlogan: '',
  portalEyebrowEn: '',
  portalAccessNotice: '',
  staffEntryLabel: '',
  icpLicense: '',
  copyright: '',
})

const systemConfig = reactive({
  baseUrl: '',
  callbackEnabled: false,
  callbackUrl: '',
  callbackApiKey: '',
  callbackAllowInternal: true,
})

const searchForm = ref({
  configKey: '',
  configType: '',
  limit: 100,
})

const formData = ref<SaveConfigRequest & UpdateConfigRequest & { configType?: string }>({
  configKey: '',
  configValue: '',
  configType: 'STRING',
  description: '',
})

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const brandCompletion = computed(() =>
  [brandConfig.appName, brandConfig.appShortName, brandConfig.appShortNameEn, brandConfig.logoUrl].filter(Boolean).length,
)
const brandCompletionRate = computed(() => `${Math.round((brandCompletion.value / 4) * 100)}%`)

const portalCompletion = computed(() =>
  [
    portalConfig.lawFirmName,
    portalConfig.lawFirmWebsite,
    portalConfig.appSlogan,
    portalConfig.portalEyebrowEn,
    portalConfig.portalAccessNotice,
    portalConfig.staffEntryLabel,
    portalConfig.icpLicense,
    portalConfig.copyright,
  ].filter(Boolean).length,
)
const portalCompletionRate = computed(() => `${Math.round((portalCompletion.value / 8) * 100)}%`)

const columns = [
  { title: '配置键', key: 'configKey', dataIndex: 'configKey', ellipsis: true, width: 180 },
  { title: '配置值', key: 'configValue', ellipsis: true, width: 200 },
  { title: '类型', key: 'configType', width: 80 },
  { title: '描述', key: 'description', dataIndex: 'description', ellipsis: true, width: 150 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const },
]

async function loadConfigData() {
  try {
    const brandRes = await getBrandConfig().catch(() => null)
    if (brandRes?.success && brandRes.data) {
      brandConfig.appName = brandRes.data.appName || ''
      brandConfig.appShortName = brandRes.data.appShortName || ''
      brandConfig.appShortNameEn = brandRes.data.appShortNameEn || ''
      brandConfig.logoUrl = brandRes.data.logoUrl || ''
    }

    const portalRes = await getPortalConfig().catch(() => null)
    if (portalRes?.success && portalRes.data) {
      portalConfig.lawFirmName = portalRes.data.lawFirmName || ''
      portalConfig.lawFirmWebsite = portalRes.data.lawFirmWebsite || ''
      portalConfig.appSlogan = portalRes.data.appSlogan || ''
      portalConfig.portalEyebrowEn = portalRes.data.portalEyebrowEn ?? ''
      portalConfig.portalAccessNotice = portalRes.data.portalAccessNotice ?? ''
      portalConfig.staffEntryLabel = portalRes.data.staffEntryLabel ?? ''
      portalConfig.icpLicense = portalRes.data.icpLicense || ''
      portalConfig.copyright = portalRes.data.copyright || ''
    }

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

async function saveConfigItem(key: string, value: string, description?: string) {
  if (!value && value !== '') return

  saveStatus.value = 'saving'
  try {
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

function saveBrandConfig(key: string, value: string) {
  const descriptions: Record<string, string> = {
    'system.app-name': '系统名称（浏览器标题）',
    'system.app-short-name': '系统简称（侧边栏）',
    'system.app-short-name-en': '系统英文简称',
    'system.logo-url': 'Logo图片地址',
  }
  saveConfigItem(key, value, descriptions[key])
}

function savePortalConfig(key: string, value: string) {
  const descriptions: Record<string, string> = {
    'system.law-firm-name': '律所名称',
    'system.law-firm-website': '律所官网',
    'system.app-slogan': '首页标语',
    'system.portal-eyebrow-en': '门户页英文眉标',
    'system.portal-access-notice': '门户页客户说明',
    'system.staff-entry-label': '工作人员入口（页脚链）',
    'system.icp-license': 'ICP备案号',
    'system.copyright': '版权信息',
  }
  saveConfigItem(key, value, descriptions[key])
}

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

function handleLogoError(e: Event) {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
}

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
  display: grid;
  gap: 18px;
}

.intro-side {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  min-width: min(420px, 100%);
}

.config-entry-strip {
  display: grid;
}

.entry-card {
  padding: 20px 22px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(30, 64, 175, 0.05), var(--lex-accent-soft)), var(--lex-surface);
  border: 1px solid var(--lex-outline);
}

.entry-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.entry-badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 9, 24, 0.06);
  color: var(--lex-primary-soft);
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.entry-card h3 {
  margin: 8px 0 10px;
  font-size: 22px;
  color: var(--text-primary);
}

.entry-card p {
  margin: 0 0 16px;
  max-width: 760px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.mini-stat {
  padding: 14px 16px;
  border-radius: 8px;
  background: rgba(0, 9, 24, 0.05);
  border: 1px solid rgba(0, 9, 24, 0.08);
}

.mini-stat span {
  display: block;
  margin-bottom: 8px;
  color: var(--text-tertiary);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.mini-stat strong {
  display: block;
  color: var(--text-primary);
  font-family: var(--font-heading);
  font-size: 24px;
  line-height: 1;
}

.config-card,
.advanced-panel {
  background: var(--lex-surface);
  border: 1px solid var(--lex-outline);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.completion-strip {
  display: grid;
  gap: 12px;
}

.completion-head h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.completion-head p {
  margin: 6px 0 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.completion-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.completion-item {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.completion-item span {
  color: var(--text-tertiary);
  font-size: 12px;
}

.completion-item strong {
  color: var(--text-primary);
  font-family: var(--font-heading);
  font-size: 20px;
  line-height: 1.1;
}

.completion-item p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.config-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 16px;
}

.config-tabs :deep(.ant-tabs-tab) {
  border-radius: 18px 18px 0 0 !important;
  padding-inline: 18px !important;
}

.config-tabs :deep(.ant-tabs-tab-active) {
  background: rgba(255, 255, 255, 0.82) !important;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.config-card {
  padding: 18px;
}

.card-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.card-heading h3,
.config-card h3,
.advanced-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 18px;
}

.field-hint {
  margin-top: 4px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.6;
}

.hint-example {
  color: var(--primary-color-light);
}

.logo-preview {
  width: 100%;
  min-height: 96px;
  border: 1px dashed var(--border-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.82);
  padding: 16px;
}

.logo-preview img {
  max-width: 100%;
  max-height: 120px;
  object-fit: contain;
}

.logo-placeholder {
  color: var(--text-tertiary);
}

.advanced-panel {
  padding: 22px;
}

.advanced-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.advanced-header p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.advanced-filter-form {
  display: flex;
  gap: 12px 8px;
  margin-bottom: 16px;
}

.advanced-filter-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.filter-actions {
  margin-left: auto;
}

.json-preview {
  margin: 0;
  max-width: 200px;
}

.config-value {
  word-break: break-all;
}

.config-tabs :deep(.ant-tabs-content-holder) {
  background: transparent;
}

.config-tabs :deep(.ant-tabs-tabpane) {
  padding-top: 4px;
}

.save-status {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 10px 16px;
  border-radius: 14px;
  font-size: 14px;
  z-index: 1000;
}

.save-status.saving {
  background: color-mix(in srgb, var(--lex-primary-soft) 12%, transparent);
  color: var(--lex-primary-soft);
  border: 1px solid color-mix(in srgb, var(--lex-primary-soft) 32%, var(--lex-outline));
}

.save-status.saved {
  background: color-mix(in srgb, var(--success-color) 10%, transparent);
  color: var(--success-color);
  border: 1px solid color-mix(in srgb, var(--success-color) 28%, var(--lex-outline));
}

.save-status.error {
  background: color-mix(in srgb, var(--error-color) 8%, transparent);
  color: var(--error-color);
  border: 1px solid color-mix(in srgb, var(--error-color) 24%, var(--lex-outline));
}

@media (max-width: 992px) {
  .intro-side,
  .completion-grid {
    grid-template-columns: 1fr;
    min-width: 0;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .entry-head,
  .card-heading,
  .advanced-header {
    flex-direction: column;
    align-items: stretch;
  }

  .config-card,
  .advanced-panel {
    padding: 16px;
    border-radius: 8px;
  }

  .advanced-header {
    flex-direction: column;
    align-items: stretch;
  }

  .advanced-filter-form {
    display: grid;
  }

  .advanced-filter-form :deep(.ant-form-item) {
    width: 100%;
  }

  .advanced-filter-form :deep(.ant-input),
  .advanced-filter-form :deep(.ant-select) {
    width: 100% !important;
  }

  .filter-actions {
    margin-left: 0;
  }

  .save-status {
    bottom: 16px;
    right: 16px;
    left: 16px;
    text-align: center;
  }
}
</style>
