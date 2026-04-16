<template>
  <div class="notification-settings-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.intro }}
        </p>
      </div>
      <a-button @click="loadData">
        <template #icon>
          <ReloadOutlined />
        </template>
        {{ UI_TEXTS.refresh }}
      </a-button>
    </section>

    <section class="overview-grid">
      <article class="config-card overview-card overview-card--wide">
        <div class="overview-card__head">
          <div>
            <h3>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.overviewTitle }}</h3>
          </div>
          <a-tag color="processing">
            {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.overviewTag }}
          </a-tag>
        </div>
        <p>
          {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.overviewDescPrefix }} <code>application.yml</code>。
          <router-link to="/admin/notification-templates">
            {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.overviewLink }}
          </router-link>
        </p>
      </article>
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.enabledChannels }}</span>
        <strong>{{ enabledChannelCount }}/3</strong>
      </div>
      <div class="stats-card success">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.email }}</span>
        <strong>{{ emailForm.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.on : ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.off }}</strong>
      </div>
      <div class="stats-card info">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.sms }}</span>
        <strong>{{ smsForm.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.on : ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.off }}</strong>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.wechat }}</span>
        <strong>{{ wechatForm.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.on : ADMIN_NOTIFICATION_SETTINGS_TEXTS.stats.off }}</strong>
      </div>
    </section>

    <a-spin :spinning="loading">
      <section class="section-shell-card">
        <div class="section-head dashboard-section-head">
          <h3>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.channelSectionTitle }}</h3>
        </div>
      </section>

      <section class="channel-grid">
        <article class="channel-card channel-card--email">
          <div class="channel-header">
            <div>
              <div class="channel-kicker">
                Email
              </div>
              <h3>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.title }}</h3>
              <p class="channel-description">
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.desc }}
              </p>
            </div>
            <div class="channel-switch">
              <a-switch
                v-model:checked="emailForm.enabled"
                @change="handleEmailEnabledChange"
              />
              <span>{{ emailForm.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.channel.enabled : ADMIN_NOTIFICATION_SETTINGS_TEXTS.channel.disabled }}</span>
            </div>
          </div>

          <div class="channel-meta">
            <div class="channel-meta-item">
              <span>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.accessStatusLabel }}</span>
              <strong>{{ emailForm.smtpHost ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.statusConfigured : ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.statusPending }}</strong>
            </div>
            <div class="channel-meta-item">
              <span>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.senderIdentityLabel }}</span>
              <strong>{{ emailForm.fromName || emailForm.from || ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.identityUnset }}</strong>
            </div>
          </div>

          <a-form
            :model="emailForm"
            layout="vertical"
            class="channel-form"
          >
            <template v-if="emailForm.enabled">
              <div class="subsection-title">
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpTitle }}
              </div>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpHostLabel">
                <a-input
                  v-model:value="emailForm.smtpHost"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpHostPlaceholder"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpPortLabel">
                <a-input-number
                  v-model:value="emailForm.smtpPort"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpPortPlaceholder"
                  :min="1"
                  :max="65535"
                  style="width: 100%"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpUserLabel">
                <a-input
                  v-model:value="emailForm.smtpUsername"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpUserPlaceholder"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpPasswordLabel">
                <a-input-password
                  v-model:value="emailForm.smtpPassword"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpPasswordPlaceholder"
                />
              </a-form-item>
              <a-alert
                :message="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.smtpAlert"
                type="info"
                show-icon
                class="inline-alert"
              />

              <div class="subsection-title">
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.senderTitle }}
              </div>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.senderEmailLabel">
                <a-input
                  v-model:value="emailForm.from"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.senderEmailPlaceholder"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.senderNameLabel">
                <a-input
                  v-model:value="emailForm.fromName"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.senderNamePlaceholder"
                />
              </a-form-item>
            </template>
            <a-form-item>
              <a-button
                type="primary"
                :loading="saving"
                @click="handleSaveEmailConfig"
              >
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.saveButton }}
              </a-button>
            </a-form-item>
          </a-form>
        </article>

        <article class="channel-card channel-card--sms">
          <div class="channel-header">
            <div>
              <div class="channel-kicker">
                SMS
              </div>
              <h3>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.title }}</h3>
              <p class="channel-description">
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.desc }}
              </p>
            </div>
            <div class="channel-switch">
              <a-switch
                v-model:checked="smsForm.enabled"
                @change="handleSmsEnabledChange"
              />
              <span>{{ smsForm.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.channel.enabled : ADMIN_NOTIFICATION_SETTINGS_TEXTS.channel.disabled }}</span>
            </div>
          </div>

          <div class="channel-meta">
            <div class="channel-meta-item">
              <span>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.providerMetaLabel }}</span>
              <strong>{{ smsForm.provider === 'tencent' ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.tencentLabel : ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.aliyunLabel }}</strong>
            </div>
            <div class="channel-meta-item">
              <span>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.templateMetaLabel }}</span>
              <strong>{{ smsForm.provider === 'tencent' ? (smsForm.tencent.templateId || ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.unset) : (smsForm.aliyun.templateCode || ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.unset) }}</strong>
            </div>
          </div>

          <a-form
            :model="smsForm"
            layout="vertical"
            class="channel-form"
          >
            <template v-if="smsForm.enabled">
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.providerLabel">
                <a-select
                  v-model:value="smsForm.provider"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.providerPlaceholder"
                >
                  <a-select-option value="aliyun">
                    {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.aliyunLabel }}
                  </a-select-option>
                  <a-select-option value="tencent">
                    {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.tencentLabel }}
                  </a-select-option>
                </a-select>
              </a-form-item>

              <template v-if="smsForm.provider === 'aliyun'">
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.accessKeyIdLabel">
                  <a-input
                    v-model:value="smsForm.aliyun.accessKeyId"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.accessKeyIdPlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.accessKeySecretLabel">
                  <a-input-password
                    v-model:value="smsForm.aliyun.accessKeySecret"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.accessKeySecretPlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.signNameLabel">
                  <a-input
                    v-model:value="smsForm.aliyun.signName"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.signNamePlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.templateCodeLabel">
                  <a-input
                    v-model:value="smsForm.aliyun.templateCode"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.templateCodePlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.endpointLabel">
                  <a-input
                    v-model:value="smsForm.aliyun.endpoint"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.endpointPlaceholder"
                  />
                  <div class="field-note">
                    {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.optionalHint }}
                  </div>
                </a-form-item>
              </template>

              <template v-if="smsForm.provider === 'tencent'">
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.secretIdLabel">
                  <a-input
                    v-model:value="smsForm.tencent.secretId"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.secretIdPlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.secretKeyLabel">
                  <a-input-password
                    v-model:value="smsForm.tencent.secretKey"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.secretKeyPlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.appIdLabel">
                  <a-input
                    v-model:value="smsForm.tencent.appId"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.appIdPlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.signNameLabel">
                  <a-input
                    v-model:value="smsForm.tencent.signName"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.signNamePlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.templateIdLabel">
                  <a-input
                    v-model:value="smsForm.tencent.templateId"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.templateIdPlaceholder"
                  />
                </a-form-item>
                <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.regionLabel">
                  <a-input
                    v-model:value="smsForm.tencent.region"
                    :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.regionPlaceholder"
                  />
                  <div class="field-note">
                    {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.optionalHint }}
                  </div>
                </a-form-item>
              </template>
            </template>
            <a-form-item>
              <a-button
                type="primary"
                :loading="saving"
                @click="handleSaveSmsConfig"
              >
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.saveButton }}
              </a-button>
            </a-form-item>
          </a-form>
        </article>

        <article class="channel-card channel-card--wechat">
          <div class="channel-header">
            <div>
              <div class="channel-kicker">
                WeChat
              </div>
              <h3>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.title }}</h3>
              <p class="channel-description">
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.desc }}
              </p>
            </div>
            <div class="channel-switch">
              <a-switch
                v-model:checked="wechatForm.enabled"
                @change="handleWechatEnabledChange"
              />
              <span>{{ wechatForm.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.channel.enabled : ADMIN_NOTIFICATION_SETTINGS_TEXTS.channel.disabled }}</span>
            </div>
          </div>

          <div class="channel-meta">
            <div class="channel-meta-item">
              <span>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.credentialLabel }}</span>
              <strong>{{ wechatForm.appId ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.appIdFilled : ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.credentialPending }}</strong>
            </div>
            <div class="channel-meta-item">
              <span>{{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.defaultTemplateLabel }}</span>
              <strong>{{ wechatForm.templateId || ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.templateUnset }}</strong>
            </div>
          </div>

          <a-form
            :model="wechatForm"
            layout="vertical"
            class="channel-form"
          >
            <template v-if="wechatForm.enabled">
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.appIdLabel">
                <a-input
                  v-model:value="wechatForm.appId"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.appIdPlaceholder"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.appSecretLabel">
                <a-input-password
                  v-model:value="wechatForm.appSecret"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.appSecretPlaceholder"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.templateLabel">
                <a-input
                  v-model:value="wechatForm.templateId"
                  :placeholder="ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.templatePlaceholder"
                />
                <div class="field-note">
                  {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.templateHint }}
                </div>
              </a-form-item>
            </template>
            <a-form-item>
              <a-button
                type="primary"
                :loading="saving"
                @click="handleSaveWechatConfig"
              >
                {{ ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.saveButton }}
              </a-button>
            </a-form-item>
          </a-form>
        </article>
      </section>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { getConfigList, saveConfig, type SysConfigInfo } from '@/api/config'
import { UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_NOTIFICATION_SETTINGS_TEXTS } from '@/constants/adminTexts'

const loading = ref(false)
const saving = ref(false)

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback
}

const emailForm = ref({
  enabled: false,
  smtpHost: '',
  smtpPort: 587,
  smtpUsername: '',
  smtpPassword: '',
  from: '',
  fromName: '',
})

const smsForm = ref({
  enabled: false,
  provider: 'aliyun',
  aliyun: {
    accessKeyId: '',
    accessKeySecret: '',
    signName: '',
    templateCode: '',
    endpoint: '',
  },
  tencent: {
    secretId: '',
    secretKey: '',
    appId: '',
    signName: '',
    templateId: '',
    region: '',
  },
})

const wechatForm = ref({
  enabled: false,
  appId: '',
  appSecret: '',
  templateId: '',
})

const enabledChannelCount = computed(() =>
  [emailForm.value.enabled, smsForm.value.enabled, wechatForm.value.enabled].filter(Boolean).length,
)

function findConfigValue(configs: SysConfigInfo[], key: string): string | null {
  const config = configs.find(c => c.configKey === key)
  return config ? config.configValue : null
}

async function loadData() {
  loading.value = true
  try {
    const res = await getConfigList({ limit: 200 })
    const configs = res.data || []

    emailForm.value.enabled = findConfigValue(configs, 'client-service.notification.email.enabled') === 'true' || findConfigValue(configs, 'notification.email.enabled') === 'true'
    emailForm.value.smtpHost = findConfigValue(configs, 'spring.mail.host') || ''
    emailForm.value.smtpPort = parseInt(findConfigValue(configs, 'spring.mail.port') || '587', 10)
    emailForm.value.smtpUsername = findConfigValue(configs, 'spring.mail.username') || ''
    emailForm.value.smtpPassword = findConfigValue(configs, 'spring.mail.password') || ''
    emailForm.value.from = findConfigValue(configs, 'client-service.notification.email.from') || findConfigValue(configs, 'notification.email.from') || ''
    emailForm.value.fromName = findConfigValue(configs, 'client-service.notification.email.from-name') || findConfigValue(configs, 'notification.email.from-name') || ''

    smsForm.value.enabled = findConfigValue(configs, 'client-service.notification.sms.enabled') === 'true' || findConfigValue(configs, 'notification.sms.enabled') === 'true'
    smsForm.value.provider = findConfigValue(configs, 'client-service.notification.sms.provider') || findConfigValue(configs, 'notification.sms.provider') || 'aliyun'
    smsForm.value.aliyun.accessKeyId = findConfigValue(configs, 'client-service.notification.sms.aliyun.access-key-id') || ''
    smsForm.value.aliyun.accessKeySecret = findConfigValue(configs, 'client-service.notification.sms.aliyun.access-key-secret') || ''
    smsForm.value.aliyun.signName = findConfigValue(configs, 'client-service.notification.sms.aliyun.sign-name') || ''
    smsForm.value.aliyun.templateCode = findConfigValue(configs, 'client-service.notification.sms.aliyun.template-code') || ''
    smsForm.value.aliyun.endpoint = findConfigValue(configs, 'client-service.notification.sms.aliyun.endpoint') || ''
    smsForm.value.tencent.secretId = findConfigValue(configs, 'client-service.notification.sms.tencent.secret-id') || ''
    smsForm.value.tencent.secretKey = findConfigValue(configs, 'client-service.notification.sms.tencent.secret-key') || ''
    smsForm.value.tencent.appId = findConfigValue(configs, 'client-service.notification.sms.tencent.app-id') || ''
    smsForm.value.tencent.signName = findConfigValue(configs, 'client-service.notification.sms.tencent.sign-name') || ''
    smsForm.value.tencent.templateId = findConfigValue(configs, 'client-service.notification.sms.tencent.template-id') || ''
    smsForm.value.tencent.region = findConfigValue(configs, 'client-service.notification.sms.tencent.region') || ''

    wechatForm.value.enabled = findConfigValue(configs, 'client-service.notification.wechat.enabled') === 'true' || findConfigValue(configs, 'notification.wechat.enabled') === 'true'
    wechatForm.value.appId = findConfigValue(configs, 'client-service.notification.wechat.app-id') || ''
    wechatForm.value.appSecret = findConfigValue(configs, 'client-service.notification.wechat.app-secret') || ''
    wechatForm.value.templateId = findConfigValue(configs, 'client-service.notification.wechat.template-id') || findConfigValue(configs, 'notification.wechat.template-id') || ''
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.configLoadFailed))
  } finally {
    loading.value = false
  }
}

async function saveConfigValue(configKey: string, configValue: string, configType: string = 'STRING', description?: string) {
  await saveConfig({
    configKey,
    configValue,
    configType,
    description,
  })
}

async function handleSaveEmailConfig() {
  saving.value = true
  try {
    await saveConfigValue('client-service.notification.email.enabled', String(emailForm.value.enabled), 'BOOLEAN', '是否启用邮件通知')
    if (emailForm.value.enabled) {
      if (emailForm.value.smtpHost) await saveConfigValue('spring.mail.host', emailForm.value.smtpHost, 'STRING', 'SMTP 服务器地址')
      if (emailForm.value.smtpPort) await saveConfigValue('spring.mail.port', String(emailForm.value.smtpPort), 'NUMBER', 'SMTP 服务器端口')
      if (emailForm.value.smtpUsername) await saveConfigValue('spring.mail.username', emailForm.value.smtpUsername, 'STRING', 'SMTP 用户名')
      if (emailForm.value.smtpPassword) await saveConfigValue('spring.mail.password', emailForm.value.smtpPassword, 'STRING', 'SMTP 密码')
      if (emailForm.value.from) await saveConfigValue('client-service.notification.email.from', emailForm.value.from, 'STRING', '邮件发件人邮箱')
      if (emailForm.value.fromName) await saveConfigValue('client-service.notification.email.from-name', emailForm.value.fromName, 'STRING', '邮件发件人名称')
    }
    message.success(ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.saveSuccess)
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.configSaveFailed))
  } finally {
    saving.value = false
  }
}

async function handleSaveSmsConfig() {
  saving.value = true
  try {
    await saveConfigValue('client-service.notification.sms.enabled', String(smsForm.value.enabled), 'BOOLEAN', '是否启用短信通知')
    if (smsForm.value.enabled) {
      await saveConfigValue('client-service.notification.sms.provider', smsForm.value.provider, 'STRING', '短信服务商（aliyun/tencent）')
      if (smsForm.value.provider === 'aliyun') {
        if (smsForm.value.aliyun.accessKeyId) await saveConfigValue('client-service.notification.sms.aliyun.access-key-id', smsForm.value.aliyun.accessKeyId, 'STRING', '阿里云 Access Key ID')
        if (smsForm.value.aliyun.accessKeySecret) await saveConfigValue('client-service.notification.sms.aliyun.access-key-secret', smsForm.value.aliyun.accessKeySecret, 'STRING', '阿里云 Access Key Secret')
        if (smsForm.value.aliyun.signName) await saveConfigValue('client-service.notification.sms.aliyun.sign-name', smsForm.value.aliyun.signName, 'STRING', '阿里云短信签名名称')
        if (smsForm.value.aliyun.templateCode) await saveConfigValue('client-service.notification.sms.aliyun.template-code', smsForm.value.aliyun.templateCode, 'STRING', '阿里云短信模板代码')
        if (smsForm.value.aliyun.endpoint) await saveConfigValue('client-service.notification.sms.aliyun.endpoint', smsForm.value.aliyun.endpoint, 'STRING', '阿里云短信 API 端点')
      } else if (smsForm.value.provider === 'tencent') {
        if (smsForm.value.tencent.secretId) await saveConfigValue('client-service.notification.sms.tencent.secret-id', smsForm.value.tencent.secretId, 'STRING', '腾讯云 Secret ID')
        if (smsForm.value.tencent.secretKey) await saveConfigValue('client-service.notification.sms.tencent.secret-key', smsForm.value.tencent.secretKey, 'STRING', '腾讯云 Secret Key')
        if (smsForm.value.tencent.appId) await saveConfigValue('client-service.notification.sms.tencent.app-id', smsForm.value.tencent.appId, 'STRING', '腾讯云短信应用ID')
        if (smsForm.value.tencent.signName) await saveConfigValue('client-service.notification.sms.tencent.sign-name', smsForm.value.tencent.signName, 'STRING', '腾讯云短信签名名称')
        if (smsForm.value.tencent.templateId) await saveConfigValue('client-service.notification.sms.tencent.template-id', smsForm.value.tencent.templateId, 'STRING', '腾讯云短信模板ID')
        if (smsForm.value.tencent.region) await saveConfigValue('client-service.notification.sms.tencent.region', smsForm.value.tencent.region, 'STRING', '腾讯云短信区域')
      }
    }
    message.success(ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.saveSuccess)
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.configSaveFailed))
  } finally {
    saving.value = false
  }
}

async function handleSaveWechatConfig() {
  saving.value = true
  try {
    await saveConfigValue('client-service.notification.wechat.enabled', String(wechatForm.value.enabled), 'BOOLEAN', '是否启用微信通知')
    if (wechatForm.value.enabled) {
      if (wechatForm.value.appId) await saveConfigValue('client-service.notification.wechat.app-id', wechatForm.value.appId, 'STRING', '微信公众号 AppID')
      if (wechatForm.value.appSecret) await saveConfigValue('client-service.notification.wechat.app-secret', wechatForm.value.appSecret, 'STRING', '微信公众号 AppSecret')
      if (wechatForm.value.templateId) await saveConfigValue('client-service.notification.wechat.template-id', wechatForm.value.templateId, 'STRING', '微信模板消息ID')
    }
    message.success(ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.saveSuccess)
  } catch (error: unknown) {
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.configSaveFailed))
  } finally {
    saving.value = false
  }
}

async function handleEmailEnabledChange() {
  try {
    await saveConfigValue('client-service.notification.email.enabled', String(emailForm.value.enabled), 'BOOLEAN', '是否启用邮件通知')
    message.success(emailForm.value.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.enabledSuccess : ADMIN_NOTIFICATION_SETTINGS_TEXTS.email.disabledSuccess)
    if (!emailForm.value.enabled) {
      emailForm.value.from = ''
      emailForm.value.fromName = ''
    }
  } catch (error: unknown) {
    emailForm.value.enabled = !emailForm.value.enabled
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.configSaveFailed))
  }
}

async function handleSmsEnabledChange() {
  try {
    await saveConfigValue('client-service.notification.sms.enabled', String(smsForm.value.enabled), 'BOOLEAN', '是否启用短信通知')
    message.success(smsForm.value.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.enabledSuccess : ADMIN_NOTIFICATION_SETTINGS_TEXTS.sms.disabledSuccess)
    if (!smsForm.value.enabled) {
      smsForm.value.aliyun = { accessKeyId: '', accessKeySecret: '', signName: '', templateCode: '', endpoint: '' }
      smsForm.value.tencent = { secretId: '', secretKey: '', appId: '', signName: '', templateId: '', region: '' }
    }
  } catch (error: unknown) {
    smsForm.value.enabled = !smsForm.value.enabled
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.configSaveFailed))
  }
}

async function handleWechatEnabledChange() {
  try {
    await saveConfigValue('client-service.notification.wechat.enabled', String(wechatForm.value.enabled), 'BOOLEAN', '是否启用微信通知')
    message.success(wechatForm.value.enabled ? ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.enabledSuccess : ADMIN_NOTIFICATION_SETTINGS_TEXTS.wechat.disabledSuccess)
    if (!wechatForm.value.enabled) {
      wechatForm.value.appId = ''
      wechatForm.value.appSecret = ''
      wechatForm.value.templateId = ''
    }
  } catch (error: unknown) {
    wechatForm.value.enabled = !wechatForm.value.enabled
    message.error(getErrorMessage(error, UI_FEEDBACK_TEXTS.configSaveFailed))
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notification-settings-container {
  display: grid;
  gap: 18px;
}

.overview-grid,
.channel-grid {
  display: grid;
  gap: 16px;
}

.overview-grid {
  grid-template-columns: 1.5fr 1fr;
}

.overview-card {
  display: grid;
  gap: 18px;
}

.overview-card__head,
.channel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.overview-card h3,
.channel-header h3 {
  margin: 6px 0 0;
  font-size: 22px;
  color: var(--text-primary);
}

.overview-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.overview-card--wide {
  background: linear-gradient(180deg, var(--lex-surface-strong), var(--lex-surface));
}

.channel-kicker {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--lex-accent-strong);
  font-weight: 700;
}


.section-shell-card,
.channel-card {
  padding: 22px;
  background: var(--lex-surface-strong);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}

.channel-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.channel-card {
  display: grid;
  gap: 18px;
}

.channel-card--email {
  background: linear-gradient(180deg, rgba(30, 64, 175, 0.08), var(--lex-surface) 28%), var(--lex-surface);
}

.channel-card--sms {
  background: linear-gradient(180deg, var(--accent-color-lighter), var(--lex-surface) 28%), var(--lex-surface);
}

.channel-card--wechat {
  background: linear-gradient(180deg, rgba(21, 128, 61, 0.1), var(--lex-surface) 28%), var(--lex-surface);
}

.channel-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

.channel-description {
  margin: 10px 0 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.channel-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.channel-meta-item {
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--border-color-light);
}

.channel-meta-item span {
  display: block;
  color: var(--text-tertiary);
  font-size: 12px;
  margin-bottom: 8px;
}

.channel-meta-item strong {
  color: var(--text-primary);
  line-height: 1.6;
}

.channel-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.subsection-title {
  margin: 4px 0 16px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-tertiary);
}

.field-note {
  margin-top: 6px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.7;
}

.inline-alert {
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .channel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1100px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .section-shell-card,
  .channel-card {
    padding: 16px;
    border-radius: 22px;
  }

  .overview-card__head,
  .channel-header {
    flex-direction: column;
  }

  .channel-meta {
    grid-template-columns: 1fr;
  }
}
</style>
