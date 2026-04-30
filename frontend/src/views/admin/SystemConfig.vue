<template>
  <div class="system-config-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_SYSTEM_CONFIG_TEXTS.intro }}
        </p>
      </div>

      <div class="intro-side">
        <div class="mini-stat">
          <span>{{ ADMIN_SYSTEM_CONFIG_TEXTS.miniStats.brandFields }}</span>
          <strong>{{ brandCompletion }}/4</strong>
        </div>
        <div class="mini-stat">
          <span>{{ ADMIN_SYSTEM_CONFIG_TEXTS.miniStats.portalFields }}</span>
          <strong>{{ portalCompletion }}/8</strong>
        </div>
        <div class="mini-stat">
          <span>{{ ADMIN_SYSTEM_CONFIG_TEXTS.miniStats.systemConfigItems }}</span>
          <strong>{{ dataSource.length }}</strong>
        </div>
      </div>
    </section>

    <section class="completion-strip config-card">
      <div class="completion-head">
        <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.completionTitle }}</h3>
      </div>
      <div class="completion-grid">
        <div class="completion-item">
          <span>{{ ADMIN_SYSTEM_CONFIG_TEXTS.completion.brand }}</span>
          <strong>{{ brandCompletionRate }}</strong>
          <p>{{ brandCompletion }}/4</p>
        </div>
        <div class="completion-item">
          <span>{{ ADMIN_SYSTEM_CONFIG_TEXTS.completion.portal }}</span>
          <strong>{{ portalCompletionRate }}</strong>
          <p>{{ portalCompletion }}/8</p>
        </div>
        <div class="completion-item">
          <span>{{ ADMIN_SYSTEM_CONFIG_TEXTS.completion.system }}</span>
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
        :tab="ADMIN_SYSTEM_CONFIG_TEXTS.tabs.brand"
      >
        <div class="config-grid">
          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.brand.systemNameTitle }}</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.brand.appNameLabel">
                <a-input
                  v-model:value="brandConfig.appName"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.brand.appNamePlaceholder"
                  @blur="saveBrandConfig('system.app-name', brandConfig.appName)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.brand.appNameHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.brand.shortNameLabel">
                <a-input
                  v-model:value="brandConfig.appShortName"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.brand.shortNamePlaceholder"
                  @blur="saveBrandConfig('system.app-short-name', brandConfig.appShortName)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.brand.shortNameHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.brand.shortNameEnLabel">
                <a-input
                  v-model:value="brandConfig.appShortNameEn"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.brand.shortNameEnPlaceholder"
                  @blur="saveBrandConfig('system.app-short-name-en', brandConfig.appShortNameEn)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.brand.shortNameEnHint }}
                </div>
              </a-form-item>
            </a-form>
          </section>

          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.brand.logoTitle }}</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.brand.logoUrlLabel">
                <a-input
                  v-model:value="brandConfig.logoUrl"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.brand.logoPlaceholder"
                  @blur="saveBrandConfig('system.logo-url', brandConfig.logoUrl)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.brand.logoHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.brand.logoPreviewLabel">
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
                    {{ ADMIN_SYSTEM_CONFIG_TEXTS.brand.logoEmpty }}
                  </div>
                </div>
              </a-form-item>
            </a-form>
          </section>
        </div>
      </a-tab-pane>

      <a-tab-pane
        key="portal"
        :tab="ADMIN_SYSTEM_CONFIG_TEXTS.tabs.portal"
      >
        <div class="config-grid">
          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.lawFirmTitle }}</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.lawFirmNameLabel">
                <a-input
                  v-model:value="portalConfig.lawFirmName"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.lawFirmNamePlaceholder"
                  @blur="savePortalConfig('system.law-firm-name', portalConfig.lawFirmName)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.lawFirmNameHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.websiteLabel">
                <a-input
                  v-model:value="portalConfig.lawFirmWebsite"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.websitePlaceholder"
                  @blur="savePortalConfig('system.law-firm-website', portalConfig.lawFirmWebsite)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.websiteHint }}
                </div>
              </a-form-item>
            </a-form>
          </section>

          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.pageContentTitle }}</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.sloganLabel">
                <a-input
                  v-model:value="portalConfig.appSlogan"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.sloganPlaceholder"
                  @blur="savePortalConfig('system.app-slogan', portalConfig.appSlogan)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.sloganHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.eyebrowLabel">
                <a-input
                  v-model:value="portalConfig.portalEyebrowEn"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.eyebrowPlaceholder"
                  @blur="savePortalConfig('system.portal-eyebrow-en', portalConfig.portalEyebrowEn)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.eyebrowHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.accessNoticeLabel">
                <a-textarea
                  v-model:value="portalConfig.portalAccessNotice"
                  :rows="4"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.accessNoticePlaceholder"
                  @blur="savePortalConfig('system.portal-access-notice', portalConfig.portalAccessNotice)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.accessNoticeHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.staffEntryLabelText">
                <a-input
                  v-model:value="portalConfig.staffEntryLabel"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.staffEntryPlaceholder"
                  @blur="savePortalConfig('system.staff-entry-label', portalConfig.staffEntryLabel)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.staffEntryHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.icpLabel">
                <a-input
                  v-model:value="portalConfig.icpLicense"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.icpPlaceholder"
                  @blur="savePortalConfig('system.icp-license', portalConfig.icpLicense)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.icpHint }}
                </div>
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.portal.copyrightLabel">
                <a-input
                  v-model:value="portalConfig.copyright"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.portal.copyrightPlaceholder"
                  @blur="savePortalConfig('system.copyright', portalConfig.copyright)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.portal.copyrightHint }}
                </div>
              </a-form-item>
            </a-form>
          </section>
        </div>
      </a-tab-pane>

      <a-tab-pane
        key="system"
        :tab="ADMIN_SYSTEM_CONFIG_TEXTS.tabs.system"
      >
        <div class="config-grid">
          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.system.systemUrlTitle }}</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.baseUrlLabel">
                <a-input
                  v-model:value="systemConfig.baseUrl"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.baseUrlPlaceholder"
                  @blur="saveSystemConfig('system.base-url', systemConfig.baseUrl)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.baseUrlHint }}
                  <br>
                  <span class="hint-example">{{ ADMIN_SYSTEM_CONFIG_TEXTS.system.baseUrlExample }}</span>
                </div>
              </a-form-item>
            </a-form>
          </section>

          <section class="config-card">
            <div class="card-heading">
              <div>
                <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackTitle }}</h3>
              </div>
            </div>
            <a-form layout="vertical">
              <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackEnabledLabel">
                <a-switch
                  v-model:checked="systemConfig.callbackEnabled"
                  checked-children="开"
                  un-checked-children="关"
                  @change="saveSystemConfig('callback.enabled', systemConfig.callbackEnabled ? 'true' : 'false')"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackEnabledHint }}
                </div>
              </a-form-item>
              <a-form-item
                v-if="systemConfig.callbackEnabled"
                :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackUrlLabel"
              >
                <a-input
                  v-model:value="systemConfig.callbackUrl"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackUrlPlaceholder"
                  @blur="saveSystemConfig('callback.law-firm-url', systemConfig.callbackUrl)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackUrlHint }}
                </div>
              </a-form-item>
              <a-form-item
                v-if="systemConfig.callbackEnabled"
                :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackSecretLabel"
              >
                <a-input-password
                  v-model:value="systemConfig.callbackApiKey"
                  :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackSecretPlaceholder"
                  @blur="saveSystemConfig('callback.api-key', systemConfig.callbackApiKey)"
                />
                <div class="field-hint">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackSecretHint }}
                </div>
              </a-form-item>
              <a-form-item
                v-if="systemConfig.callbackEnabled"
                :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackAllowInternalLabel"
              >
                <a-switch
                  v-model:checked="systemConfig.callbackAllowInternal"
                  checked-children="是"
                  un-checked-children="否"
                  @change="saveSystemConfig('callback.allow-internal', systemConfig.callbackAllowInternal ? 'true' : 'false')"
                />
                <div class="field-hint">
                  <span v-if="systemConfig.callbackAllowInternal">{{ ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackAllowInternalYes }}</span>
                  <span v-else>{{ ADMIN_SYSTEM_CONFIG_TEXTS.system.callbackAllowInternalNo }}</span>
                </div>
              </a-form-item>
            </a-form>
          </section>
        </div>
      </a-tab-pane>

      <a-tab-pane
        key="advanced"
        :tab="ADMIN_SYSTEM_CONFIG_TEXTS.tabs.advanced"
      >
        <section class="advanced-panel">
          <div class="advanced-header">
            <div>
              <h3>{{ ADMIN_SYSTEM_CONFIG_TEXTS.system.advancedTitle }}</h3>
              <p>{{ ADMIN_SYSTEM_CONFIG_TEXTS.system.advancedDesc }}</p>
            </div>
            <a-space>
              <a-button
                type="primary"
                @click="showCreateModal = true"
              >
                <template #icon>
                  <PlusOutlined />
                </template>
                {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.addConfig }}
              </a-button>
              <a-button @click="loadData">
                <template #icon>
                  <ReloadOutlined />
                </template>
                {{ UI_TEXTS.refresh }}
              </a-button>
            </a-space>
          </div>

          <a-form
            :model="searchForm"
            layout="inline"
            class="advanced-filter-form"
            @finish="handleSearch"
          >
            <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.searchKeyLabel">
              <a-input
                v-model:value="searchForm.configKey"
                :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.searchKeyPlaceholder"
                allow-clear
                style="width: 220px"
              />
            </a-form-item>
            <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.searchTypeLabel">
              <a-select
                v-model:value="searchForm.configType"
                :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.searchTypePlaceholder"
                allow-clear
                style="width: 150px"
              >
                <a-select-option value="STRING">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.stringOption }}
                </a-select-option>
                <a-select-option value="NUMBER">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.numberOption }}
                </a-select-option>
                <a-select-option value="BOOLEAN">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.booleanOption }}
                </a-select-option>
                <a-select-option value="JSON">
                  {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.jsonOption }}
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
                    {{ record.configValue === 'true' ? ADMIN_SYSTEM_CONFIG_TEXTS.system.booleanTrue : ADMIN_SYSTEM_CONFIG_TEXTS.system.booleanFalse }}
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
                    {{ ADMIN_SYSTEM_CONFIG_TEXTS.table.edit }}
                  </a-button>
                  <a-popconfirm
                    :title="UI_CONFIRM_TEXTS.removeConfig"
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
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:open="showCreateModal"
      :title="editingRecord ? ADMIN_SYSTEM_CONFIG_TEXTS.system.modalEditTitle : ADMIN_SYSTEM_CONFIG_TEXTS.system.modalCreateTitle"
      :width="modalWidth"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        :model="formData"
        layout="vertical"
      >
        <a-form-item
          :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.keyLabel"
          required
        >
          <a-input
            v-model:value="formData.configKey"
            :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.searchKeyPlaceholder"
            :disabled="!!editingRecord"
          />
        </a-form-item>
        <a-form-item
          :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.valueLabel"
          required
        >
          <a-textarea
            v-if="formData.configType === 'JSON'"
            v-model:value="formData.configValue"
            :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.valueJsonPlaceholder"
            :rows="4"
          />
          <a-select
            v-else-if="formData.configType === 'BOOLEAN'"
            v-model:value="formData.configValue"
            :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.valueBooleanPlaceholder"
          >
            <a-select-option value="true">
              {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.booleanTrue }}
            </a-select-option>
            <a-select-option value="false">
              {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.booleanFalse }}
            </a-select-option>
          </a-select>
          <a-input-number
            v-else-if="formData.configType === 'NUMBER'"
            v-model:value="formData.configValue"
            :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.valueNumberPlaceholder"
            style="width: 100%"
          />
          <a-input
            v-else
            v-model:value="formData.configValue"
            :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.valueDefaultPlaceholder"
          />
        </a-form-item>
        <a-form-item
          :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.typeLabel"
          required
        >
          <a-select
            v-model:value="formData.configType"
            :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.searchTypePlaceholder"
          >
            <a-select-option value="STRING">
              {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.stringOption }}
            </a-select-option>
            <a-select-option value="NUMBER">
              {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.numberOption }}
            </a-select-option>
            <a-select-option value="BOOLEAN">
              {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.booleanOption }}
            </a-select-option>
            <a-select-option value="JSON">
              {{ ADMIN_SYSTEM_CONFIG_TEXTS.system.jsonOption }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="ADMIN_SYSTEM_CONFIG_TEXTS.system.descriptionLabel">
          <a-textarea
            v-model:value="formData.description"
            :placeholder="ADMIN_SYSTEM_CONFIG_TEXTS.system.descriptionPlaceholder"
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
      {{ saveStatus === 'saving' ? ADMIN_SYSTEM_CONFIG_TEXTS.system.saveSaving : saveStatus === 'saved' ? ADMIN_SYSTEM_CONFIG_TEXTS.system.saveSaved : ADMIN_SYSTEM_CONFIG_TEXTS.system.saveFailed }}
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
import { UI_CONFIRM_TEXTS, UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_SYSTEM_CONFIG_TEXTS } from '@/constants/adminTexts'

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
  showTotal: (total: number) => `${ADMIN_SYSTEM_CONFIG_TEXTS.system.totalPrefix}${total}${ADMIN_SYSTEM_CONFIG_TEXTS.system.totalSuffix}`,
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
  { title: ADMIN_SYSTEM_CONFIG_TEXTS.table.configKey, key: 'configKey', dataIndex: 'configKey', ellipsis: true, width: 180 },
  { title: ADMIN_SYSTEM_CONFIG_TEXTS.table.configValue, key: 'configValue', ellipsis: true, width: 200 },
  { title: ADMIN_SYSTEM_CONFIG_TEXTS.table.type, key: 'configType', width: 80 },
  { title: ADMIN_SYSTEM_CONFIG_TEXTS.table.description, key: 'description', dataIndex: 'description', ellipsis: true, width: 150 },
  { title: ADMIN_SYSTEM_CONFIG_TEXTS.table.action, key: 'action', width: 120, fixed: 'right' as const },
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
    logger.error(
      ADMIN_SYSTEM_CONFIG_TEXTS.feedback.loadConfigFailed,
      error instanceof Error ? error.message : ADMIN_SYSTEM_CONFIG_TEXTS.feedback.unknownError,
    )
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
    const errorMessage = error instanceof Error ? error.message : UI_FEEDBACK_TEXTS.configSaveFailed
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
    const errorMessage = error instanceof Error ? error.message : UI_FEEDBACK_TEXTS.configLoadFailed
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
    message.success(UI_FEEDBACK_TEXTS.configDeleted)
    await loadData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_SYSTEM_CONFIG_TEXTS.feedback.deleteFailed
    message.error(errorMessage)
  }
}

async function handleSubmit() {
  if (!formData.value.configKey) {
    message.warning(ADMIN_SYSTEM_CONFIG_TEXTS.validation.configKeyRequired)
    return
  }
  if (!formData.value.configValue && formData.value.configValue !== '') {
    message.warning(ADMIN_SYSTEM_CONFIG_TEXTS.validation.configValueRequired)
    return
  }

  try {
    if (editingRecord.value) {
      await updateConfig(editingRecord.value.id, {
        configValue: String(formData.value.configValue),
        configType: formData.value.configType,
        description: formData.value.description,
      })
      message.success(UI_FEEDBACK_TEXTS.configUpdated)
    } else {
      await saveConfig({
        configKey: formData.value.configKey,
        configValue: String(formData.value.configValue),
        configType: formData.value.configType || 'STRING',
        description: formData.value.description,
      })
      message.success(UI_FEEDBACK_TEXTS.configCreated)
    }
    handleCancel()
    await loadData()
    await loadConfigData()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : ADMIN_SYSTEM_CONFIG_TEXTS.feedback.operationFailed
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
    STRING: ADMIN_SYSTEM_CONFIG_TEXTS.system.stringOption,
    NUMBER: ADMIN_SYSTEM_CONFIG_TEXTS.system.numberOption,
    BOOLEAN: ADMIN_SYSTEM_CONFIG_TEXTS.system.booleanOption,
    JSON: ADMIN_SYSTEM_CONFIG_TEXTS.system.jsonOption,
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
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.intro-side {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  min-width: min(420px, 100%);
}

.mini-stat {
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.mini-stat span {
  display: block;
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 13px;
}

.mini-stat strong {
  display: block;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
}

.config-card,
.advanced-panel {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

.completion-strip {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.completion-head h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.completion-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.completion-item {
  padding: 16px;
  border-radius: 6px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.completion-item span {
  color: #6b7280;
  font-size: 14px;
}

.completion-item strong {
  color: #1677ff;
  font-size: 24px;
  font-weight: 600;
}

.completion-item p {
  margin: 0;
  color: #9ca3af;
  font-size: 13px;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.card-heading {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.card-heading h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.config-card .ant-form {
  padding: 24px;
}

.field-hint {
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.5;
}

.hint-example {
  color: #9ca3af;
  font-family: monospace;
}

.logo-preview {
  margin-top: 8px;
  padding: 16px;
  border-radius: 6px;
  border: 1px dashed #d9d9d9;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
}

.logo-preview img {
  max-width: 100%;
  max-height: 80px;
  object-fit: contain;
}

.logo-placeholder {
  color: #9ca3af;
  font-size: 14px;
}

.advanced-panel {
  padding: 24px;
}

.advanced-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.advanced-header h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.advanced-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.advanced-filter-form {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.config-value {
  display: inline-block;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.json-preview {
  margin: 0 !important;
  max-width: 300px;
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

@media (max-width: 768px) {
  .intro-side {
    grid-template-columns: 1fr;
  }

  .config-grid {
    grid-template-columns: 1fr;
  }

  .advanced-header {
    flex-direction: column;
    gap: 16px;
  }

  .save-status {
    bottom: 16px;
    right: 16px;
    left: 16px;
    text-align: center;
  }
}
</style>
