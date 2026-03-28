<template>
  <div class="notification-settings-container">
    <section class="page-intro">
      <div>
        <div class="eyebrow">
          Channel Configuration
        </div>
        <h2 class="editorial-title intro-title">
          通知配置
        </h2>
        <p class="intro-text">
          在同一页面管理邮件、短信、微信三个渠道的启用状态与发送参数，保存后立即生效。
        </p>
      </div>
      <a-button @click="loadData">
        <template #icon>
          <ReloadOutlined />
        </template>
        刷新
      </a-button>
    </section>

    <section class="info-panel">
      <a-alert
        message="配置说明"
        description="系统优先使用数据库中的通知配置；当数据库中不存在对应键时，才会回退到 application.yml 中的默认值。"
        type="info"
        show-icon
      />
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">启用渠道</span>
        <strong>{{ enabledChannelCount }}/3</strong>
        <p>当前已启用的通知渠道数量。</p>
      </div>
      <div class="stats-card success">
        <span class="stats-label">邮件</span>
        <strong>{{ emailForm.enabled ? 'ON' : 'OFF' }}</strong>
        <p>{{ emailForm.smtpHost ? 'SMTP 已配置' : 'SMTP 待配置' }}</p>
      </div>
      <div class="stats-card info">
        <span class="stats-label">短信</span>
        <strong>{{ smsForm.enabled ? 'ON' : 'OFF' }}</strong>
        <p>{{ smsForm.provider === 'tencent' ? '腾讯云通道' : '阿里云通道' }}</p>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">微信</span>
        <strong>{{ wechatForm.enabled ? 'ON' : 'OFF' }}</strong>
        <p>{{ wechatForm.templateId ? '模板已配置' : '模板待配置' }}</p>
      </div>
    </section>

    <a-spin :spinning="loading">
      <section class="channel-grid">
        <article class="channel-card">
          <div class="channel-header">
            <div>
              <div class="channel-kicker">
                Email
              </div>
              <h3>邮件通知</h3>
            </div>
            <div class="channel-switch">
              <a-switch
                v-model:checked="emailForm.enabled"
                @change="handleEmailEnabledChange"
              />
              <span>{{ emailForm.enabled ? '已启用' : '已禁用' }}</span>
            </div>
          </div>

          <a-form
            :model="emailForm"
            layout="vertical"
            class="channel-form"
          >
            <template v-if="emailForm.enabled">
              <div class="subsection-title">
                SMTP 服务器配置
              </div>
              <a-form-item label="SMTP 服务器地址">
                <a-input
                  v-model:value="emailForm.smtpHost"
                  placeholder="请输入 SMTP 服务器地址（如：smtp.example.com）"
                />
              </a-form-item>
              <a-form-item label="SMTP 端口">
                <a-input-number
                  v-model:value="emailForm.smtpPort"
                  placeholder="请输入 SMTP 端口（如：587）"
                  :min="1"
                  :max="65535"
                  style="width: 100%"
                />
              </a-form-item>
              <a-form-item label="SMTP 用户名">
                <a-input
                  v-model:value="emailForm.smtpUsername"
                  placeholder="请输入 SMTP 用户名（通常是邮箱地址）"
                />
              </a-form-item>
              <a-form-item label="SMTP 密码">
                <a-input-password
                  v-model:value="emailForm.smtpPassword"
                  placeholder="请输入 SMTP 密码（或授权码）"
                />
              </a-form-item>
              <a-alert
                message="SMTP 配置保存后立即生效，无需重启服务。若通过环境变量 SPRING_MAIL_* 覆盖，则环境变量优先。"
                type="info"
                show-icon
                class="inline-alert"
              />

              <div class="subsection-title">
                发件人信息
              </div>
              <a-form-item label="发件人邮箱">
                <a-input
                  v-model:value="emailForm.from"
                  placeholder="请输入发件人邮箱（如：noreply@example.com）"
                />
              </a-form-item>
              <a-form-item label="发件人名称">
                <a-input
                  v-model:value="emailForm.fromName"
                  placeholder="请输入发件人名称（如：客户服务系统）"
                />
              </a-form-item>
            </template>
            <a-form-item>
              <a-button
                type="primary"
                :loading="saving"
                @click="handleSaveEmailConfig"
              >
                保存邮件配置
              </a-button>
            </a-form-item>
          </a-form>
        </article>

        <article class="channel-card">
          <div class="channel-header">
            <div>
              <div class="channel-kicker">
                SMS
              </div>
              <h3>短信通知</h3>
            </div>
            <div class="channel-switch">
              <a-switch
                v-model:checked="smsForm.enabled"
                @change="handleSmsEnabledChange"
              />
              <span>{{ smsForm.enabled ? '已启用' : '已禁用' }}</span>
            </div>
          </div>

          <a-form
            :model="smsForm"
            layout="vertical"
            class="channel-form"
          >
            <template v-if="smsForm.enabled">
              <a-form-item label="服务商">
                <a-select
                  v-model:value="smsForm.provider"
                  placeholder="请选择短信服务商"
                >
                  <a-select-option value="aliyun">
                    阿里云
                  </a-select-option>
                  <a-select-option value="tencent">
                    腾讯云
                  </a-select-option>
                </a-select>
              </a-form-item>

              <template v-if="smsForm.provider === 'aliyun'">
                <a-form-item label="Access Key ID">
                  <a-input
                    v-model:value="smsForm.aliyun.accessKeyId"
                    placeholder="请输入阿里云 Access Key ID"
                  />
                </a-form-item>
                <a-form-item label="Access Key Secret">
                  <a-input-password
                    v-model:value="smsForm.aliyun.accessKeySecret"
                    placeholder="请输入阿里云 Access Key Secret"
                  />
                </a-form-item>
                <a-form-item label="签名名称">
                  <a-input
                    v-model:value="smsForm.aliyun.signName"
                    placeholder="请输入短信签名名称"
                  />
                </a-form-item>
                <a-form-item label="模板代码">
                  <a-input
                    v-model:value="smsForm.aliyun.templateCode"
                    placeholder="请输入短信模板代码"
                  />
                </a-form-item>
                <a-form-item label="API 端点">
                  <a-input
                    v-model:value="smsForm.aliyun.endpoint"
                    placeholder="请输入 API 端点（默认：dysmsapi.aliyuncs.com）"
                  />
                  <div class="field-note">
                    可选，留空使用默认值
                  </div>
                </a-form-item>
              </template>

              <template v-if="smsForm.provider === 'tencent'">
                <a-form-item label="Secret ID">
                  <a-input
                    v-model:value="smsForm.tencent.secretId"
                    placeholder="请输入腾讯云 Secret ID"
                  />
                </a-form-item>
                <a-form-item label="Secret Key">
                  <a-input-password
                    v-model:value="smsForm.tencent.secretKey"
                    placeholder="请输入腾讯云 Secret Key"
                  />
                </a-form-item>
                <a-form-item label="应用ID">
                  <a-input
                    v-model:value="smsForm.tencent.appId"
                    placeholder="请输入腾讯云短信应用ID"
                  />
                </a-form-item>
                <a-form-item label="签名名称">
                  <a-input
                    v-model:value="smsForm.tencent.signName"
                    placeholder="请输入短信签名名称"
                  />
                </a-form-item>
                <a-form-item label="模板ID">
                  <a-input
                    v-model:value="smsForm.tencent.templateId"
                    placeholder="请输入短信模板ID"
                  />
                </a-form-item>
                <a-form-item label="区域">
                  <a-input
                    v-model:value="smsForm.tencent.region"
                    placeholder="请输入区域（默认：ap-beijing）"
                  />
                  <div class="field-note">
                    可选，留空使用默认值
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
                保存短信配置
              </a-button>
            </a-form-item>
          </a-form>
        </article>

        <article class="channel-card">
          <div class="channel-header">
            <div>
              <div class="channel-kicker">
                WeChat
              </div>
              <h3>微信通知</h3>
            </div>
            <div class="channel-switch">
              <a-switch
                v-model:checked="wechatForm.enabled"
                @change="handleWechatEnabledChange"
              />
              <span>{{ wechatForm.enabled ? '已启用' : '已禁用' }}</span>
            </div>
          </div>

          <a-form
            :model="wechatForm"
            layout="vertical"
            class="channel-form"
          >
            <template v-if="wechatForm.enabled">
              <a-form-item label="AppID">
                <a-input
                  v-model:value="wechatForm.appId"
                  placeholder="请输入微信公众号 AppID"
                />
              </a-form-item>
              <a-form-item label="AppSecret">
                <a-input-password
                  v-model:value="wechatForm.appSecret"
                  placeholder="请输入微信公众号 AppSecret"
                />
              </a-form-item>
              <a-form-item label="模板ID">
                <a-input
                  v-model:value="wechatForm.templateId"
                  placeholder="请输入微信模板消息ID（可选，也可在通知模板管理中配置）"
                />
                <div class="field-note">
                  可以在通知模板管理页面配置更细的模板内容。
                </div>
              </a-form-item>
            </template>
            <a-form-item>
              <a-button
                type="primary"
                :loading="saving"
                @click="handleSaveWechatConfig"
              >
                保存微信配置
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
    message.error(getErrorMessage(error, '加载配置失败'))
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
    message.success('邮件配置已保存，SMTP 配置已立即生效')
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '保存邮件配置失败'))
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
    message.success('短信配置已保存，配置更新后立即生效')
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '保存短信配置失败'))
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
    message.success('微信配置已保存')
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '保存微信配置失败'))
  } finally {
    saving.value = false
  }
}

async function handleEmailEnabledChange() {
  try {
    await saveConfigValue('client-service.notification.email.enabled', String(emailForm.value.enabled), 'BOOLEAN', '是否启用邮件通知')
    message.success(emailForm.value.enabled ? '邮件通知已启用' : '邮件通知已禁用')
    if (!emailForm.value.enabled) {
      emailForm.value.from = ''
      emailForm.value.fromName = ''
    }
  } catch (error: unknown) {
    emailForm.value.enabled = !emailForm.value.enabled
    message.error(getErrorMessage(error, '保存失败'))
  }
}

async function handleSmsEnabledChange() {
  try {
    await saveConfigValue('client-service.notification.sms.enabled', String(smsForm.value.enabled), 'BOOLEAN', '是否启用短信通知')
    message.success(smsForm.value.enabled ? '短信通知已启用' : '短信通知已禁用')
    if (!smsForm.value.enabled) {
      smsForm.value.aliyun = { accessKeyId: '', accessKeySecret: '', signName: '', templateCode: '', endpoint: '' }
      smsForm.value.tencent = { secretId: '', secretKey: '', appId: '', signName: '', templateId: '', region: '' }
    }
  } catch (error: unknown) {
    smsForm.value.enabled = !smsForm.value.enabled
    message.error(getErrorMessage(error, '保存失败'))
  }
}

async function handleWechatEnabledChange() {
  try {
    await saveConfigValue('client-service.notification.wechat.enabled', String(wechatForm.value.enabled), 'BOOLEAN', '是否启用微信通知')
    message.success(wechatForm.value.enabled ? '微信通知已启用' : '微信通知已禁用')
    if (!wechatForm.value.enabled) {
      wechatForm.value.appId = ''
      wechatForm.value.appSecret = ''
      wechatForm.value.templateId = ''
    }
  } catch (error: unknown) {
    wechatForm.value.enabled = !wechatForm.value.enabled
    message.error(getErrorMessage(error, '保存失败'))
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

.info-panel,
.channel-card {
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(12px);
}

.info-panel {
  padding: 18px;
}

.channel-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.channel-card {
  padding: 20px;
}

.channel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.channel-kicker {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-tertiary);
  margin-bottom: 6px;
}

.channel-header h3 {
  margin: 0;
  color: var(--primary-color-dark);
  font-size: 22px;
}

.channel-switch {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
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

.inline-alert {
  margin-bottom: 16px;
}

@media (max-width: 1100px) {
  .channel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .info-panel,
  .channel-card {
    padding: 16px;
    border-radius: 20px;
  }

  .channel-header {
    flex-direction: column;
  }
}
</style>
