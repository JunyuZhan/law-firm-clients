<template>
  <div class="notification-settings-container">
    <a-card>
      <template #title>
        <span>通知配置</span>
      </template>
      <template #extra>
        <a-button @click="loadData">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </template>

      <a-alert
        message="配置说明"
        description="在此页面配置邮件、短信、微信通知的相关参数。配置保存后，系统会优先使用数据库中的配置，如果数据库中没有配置，则使用 application.yml 中的默认配置。"
        type="info"
        show-icon
        style="margin-bottom: 24px"
      />

      <a-spin :spinning="loading">
        <!-- 邮件通知配置 -->
        <a-card
          title="邮件通知"
          style="margin-bottom: 24px"
        >
          <a-form
            :model="emailForm"
            layout="vertical"
          >
            <a-form-item label="启用邮件通知">
              <a-switch
                v-model:checked="emailForm.enabled"
                @change="handleEmailEnabledChange"
              />
              <span style="margin-left: 8px; color: #999">
                {{ emailForm.enabled ? '已启用' : '已禁用' }}
              </span>
            </a-form-item>
            <template v-if="emailForm.enabled">
              <a-divider
                orientation="left"
                style="margin: 16px 0"
              >
                SMTP 服务器配置
              </a-divider>
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
                message="配置说明"
                description="SMTP 配置保存后立即生效，无需重启服务。如果使用环境变量 SPRING_MAIL_* 配置，则环境变量优先级更高。"
                type="info"
                show-icon
                style="margin-bottom: 16px"
              />
              <a-divider
                orientation="left"
                style="margin: 16px 0"
              >
                发件人信息
              </a-divider>
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
        </a-card>

        <!-- 短信通知配置 -->
        <a-card
          title="短信通知"
          style="margin-bottom: 24px"
        >
          <a-form
            :model="smsForm"
            layout="vertical"
          >
            <a-form-item label="启用短信通知">
              <a-switch
                v-model:checked="smsForm.enabled"
                @change="handleSmsEnabledChange"
              />
              <span style="margin-left: 8px; color: #999">
                {{ smsForm.enabled ? '已启用' : '已禁用' }}
              </span>
            </a-form-item>
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
                  <div style="margin-top: 4px; font-size: 12px; color: #999">
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
                  <div style="margin-top: 4px; font-size: 12px; color: #999">
                    可选，留空使用默认值（如：ap-beijing, ap-shanghai）
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
        </a-card>

        <!-- 微信通知配置 -->
        <a-card title="微信通知">
          <a-form
            :model="wechatForm"
            layout="vertical"
          >
            <a-form-item label="启用微信通知">
              <a-switch
                v-model:checked="wechatForm.enabled"
                @change="handleWechatEnabledChange"
              />
              <span style="margin-left: 8px; color: #999">
                {{ wechatForm.enabled ? '已启用' : '已禁用' }}
              </span>
            </a-form-item>
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
                <div style="margin-top: 4px; font-size: 12px; color: #999">
                  提示：可以在「通知模板管理」页面配置更详细的模板内容
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
        </a-card>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { getConfigList, saveConfig, type SysConfigInfo } from '@/api/config'

const loading = ref(false)
const saving = ref(false)

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback
}

// 邮件配置表单
const emailForm = ref({
  enabled: false,
  smtpHost: '',
  smtpPort: 587,
  smtpUsername: '',
  smtpPassword: '',
  from: '',
  fromName: '',
})

// 短信配置表单
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

// 微信配置表单
const wechatForm = ref({
  enabled: false,
  appId: '',
  appSecret: '',
  templateId: '',
})

// 从配置列表中查找配置值
function findConfigValue(configs: SysConfigInfo[], key: string): string | null {
  const config = configs.find(c => c.configKey === key)
  return config ? config.configValue : null
}

// 加载配置数据
async function loadData() {
  loading.value = true
  try {
    const res = await getConfigList({ limit: 200 })
    const configs = res.data || []

    // 加载邮件配置（兼容两种键名格式：带前缀和不带前缀）
    emailForm.value.enabled = findConfigValue(configs, 'client-service.notification.email.enabled') === 'true' || findConfigValue(configs, 'notification.email.enabled') === 'true'
    emailForm.value.smtpHost = findConfigValue(configs, 'spring.mail.host') || ''
    emailForm.value.smtpPort = parseInt(findConfigValue(configs, 'spring.mail.port') || '587', 10)
    emailForm.value.smtpUsername = findConfigValue(configs, 'spring.mail.username') || ''
    emailForm.value.smtpPassword = findConfigValue(configs, 'spring.mail.password') || ''
    emailForm.value.from = findConfigValue(configs, 'client-service.notification.email.from') || findConfigValue(configs, 'notification.email.from') || ''
    emailForm.value.fromName = findConfigValue(configs, 'client-service.notification.email.from-name') || findConfigValue(configs, 'notification.email.from-name') || ''

    // 加载短信配置（兼容两种键名格式：带前缀和不带前缀）
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

    // 加载微信配置（兼容两种键名格式：带前缀和不带前缀）
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

// 保存配置的通用方法
async function saveConfigValue(
  configKey: string,
  configValue: string,
  configType: string = 'STRING',
  description?: string
) {
  await saveConfig({
    configKey,
    configValue,
    configType,
    description,
  })
}

// 保存邮件配置
async function handleSaveEmailConfig() {
  saving.value = true
  try {
    await saveConfigValue(
      'client-service.notification.email.enabled',
      String(emailForm.value.enabled),
      'BOOLEAN',
      '是否启用邮件通知'
    )
    if (emailForm.value.enabled) {
      // 保存 SMTP 服务器配置
      if (emailForm.value.smtpHost) {
        await saveConfigValue(
          'spring.mail.host',
          emailForm.value.smtpHost,
          'STRING',
          'SMTP 服务器地址'
        )
      }
      if (emailForm.value.smtpPort) {
        await saveConfigValue(
          'spring.mail.port',
          String(emailForm.value.smtpPort),
          'NUMBER',
          'SMTP 服务器端口'
        )
      }
      if (emailForm.value.smtpUsername) {
        await saveConfigValue(
          'spring.mail.username',
          emailForm.value.smtpUsername,
          'STRING',
          'SMTP 用户名'
        )
      }
      if (emailForm.value.smtpPassword) {
        await saveConfigValue(
          'spring.mail.password',
          emailForm.value.smtpPassword,
          'STRING',
          'SMTP 密码'
        )
      }
      // 保存发件人信息
      if (emailForm.value.from) {
        await saveConfigValue(
          'client-service.notification.email.from',
          emailForm.value.from,
          'STRING',
          '邮件发件人邮箱'
        )
      }
      if (emailForm.value.fromName) {
        await saveConfigValue(
          'client-service.notification.email.from-name',
          emailForm.value.fromName,
          'STRING',
          '邮件发件人名称'
        )
      }
    }
    message.success('邮件配置已保存，SMTP 配置已立即生效')
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '保存邮件配置失败'))
  } finally {
    saving.value = false
  }
}

// 保存短信配置
async function handleSaveSmsConfig() {
  saving.value = true
  try {
    await saveConfigValue(
      'client-service.notification.sms.enabled',
      String(smsForm.value.enabled),
      'BOOLEAN',
      '是否启用短信通知'
    )
    if (smsForm.value.enabled) {
      await saveConfigValue(
        'client-service.notification.sms.provider',
        smsForm.value.provider,
        'STRING',
        '短信服务商（aliyun/tencent）'
      )
      if (smsForm.value.provider === 'aliyun') {
        if (smsForm.value.aliyun.accessKeyId) {
          await saveConfigValue(
            'client-service.notification.sms.aliyun.access-key-id',
            smsForm.value.aliyun.accessKeyId,
            'STRING',
            '阿里云 Access Key ID'
          )
        }
        if (smsForm.value.aliyun.accessKeySecret) {
          await saveConfigValue(
            'client-service.notification.sms.aliyun.access-key-secret',
            smsForm.value.aliyun.accessKeySecret,
            'STRING',
            '阿里云 Access Key Secret'
          )
        }
        if (smsForm.value.aliyun.signName) {
          await saveConfigValue(
            'client-service.notification.sms.aliyun.sign-name',
            smsForm.value.aliyun.signName,
            'STRING',
            '阿里云短信签名名称'
          )
        }
        if (smsForm.value.aliyun.templateCode) {
          await saveConfigValue(
            'client-service.notification.sms.aliyun.template-code',
            smsForm.value.aliyun.templateCode,
            'STRING',
            '阿里云短信模板代码'
          )
        }
        if (smsForm.value.aliyun.endpoint) {
          await saveConfigValue(
            'client-service.notification.sms.aliyun.endpoint',
            smsForm.value.aliyun.endpoint,
            'STRING',
            '阿里云短信 API 端点'
          )
        }
      } else if (smsForm.value.provider === 'tencent') {
        if (smsForm.value.tencent.secretId) {
          await saveConfigValue(
            'client-service.notification.sms.tencent.secret-id',
            smsForm.value.tencent.secretId,
            'STRING',
            '腾讯云 Secret ID'
          )
        }
        if (smsForm.value.tencent.secretKey) {
          await saveConfigValue(
            'client-service.notification.sms.tencent.secret-key',
            smsForm.value.tencent.secretKey,
            'STRING',
            '腾讯云 Secret Key'
          )
        }
        if (smsForm.value.tencent.appId) {
          await saveConfigValue(
            'client-service.notification.sms.tencent.app-id',
            smsForm.value.tencent.appId,
            'STRING',
            '腾讯云短信应用ID'
          )
        }
        if (smsForm.value.tencent.signName) {
          await saveConfigValue(
            'client-service.notification.sms.tencent.sign-name',
            smsForm.value.tencent.signName,
            'STRING',
            '腾讯云短信签名名称'
          )
        }
        if (smsForm.value.tencent.templateId) {
          await saveConfigValue(
            'client-service.notification.sms.tencent.template-id',
            smsForm.value.tencent.templateId,
            'STRING',
            '腾讯云短信模板ID'
          )
        }
        if (smsForm.value.tencent.region) {
          await saveConfigValue(
            'client-service.notification.sms.tencent.region',
            smsForm.value.tencent.region,
            'STRING',
            '腾讯云短信区域'
          )
        }
      }
    }
    message.success('短信配置已保存，配置更新后立即生效')
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '保存短信配置失败'))
  } finally {
    saving.value = false
  }
}

// 保存微信配置
async function handleSaveWechatConfig() {
  saving.value = true
  try {
    await saveConfigValue(
      'client-service.notification.wechat.enabled',
      String(wechatForm.value.enabled),
      'BOOLEAN',
      '是否启用微信通知'
    )
    if (wechatForm.value.enabled) {
      if (wechatForm.value.appId) {
        await saveConfigValue(
          'client-service.notification.wechat.app-id',
          wechatForm.value.appId,
          'STRING',
          '微信公众号 AppID'
        )
      }
      if (wechatForm.value.appSecret) {
        await saveConfigValue(
          'client-service.notification.wechat.app-secret',
          wechatForm.value.appSecret,
          'STRING',
          '微信公众号 AppSecret'
        )
      }
      if (wechatForm.value.templateId) {
        await saveConfigValue(
          'client-service.notification.wechat.template-id',
          wechatForm.value.templateId,
          'STRING',
          '微信模板消息ID'
        )
      }
    }
    message.success('微信配置已保存')
  } catch (error: unknown) {
    message.error(getErrorMessage(error, '保存微信配置失败'))
  } finally {
    saving.value = false
  }
}

// 启用状态变化处理（自动保存）
async function handleEmailEnabledChange() {
  try {
    await saveConfigValue(
      'client-service.notification.email.enabled',
      String(emailForm.value.enabled),
      'BOOLEAN',
      '是否启用邮件通知'
    )
    message.success(emailForm.value.enabled ? '邮件通知已启用' : '邮件通知已禁用')
    if (!emailForm.value.enabled) {
      // 禁用时清空本地配置
      emailForm.value.from = ''
      emailForm.value.fromName = ''
    }
  } catch (error: unknown) {
    // 恢复开关状态
    emailForm.value.enabled = !emailForm.value.enabled
    message.error(getErrorMessage(error, '保存失败'))
  }
}

async function handleSmsEnabledChange() {
  try {
    await saveConfigValue(
      'client-service.notification.sms.enabled',
      String(smsForm.value.enabled),
      'BOOLEAN',
      '是否启用短信通知'
    )
    message.success(smsForm.value.enabled ? '短信通知已启用' : '短信通知已禁用')
    if (!smsForm.value.enabled) {
      // 禁用时清空本地配置
      smsForm.value.aliyun = {
        accessKeyId: '',
        accessKeySecret: '',
        signName: '',
        templateCode: '',
        endpoint: '',
      }
      smsForm.value.tencent = {
        secretId: '',
        secretKey: '',
        appId: '',
        signName: '',
        templateId: '',
        region: '',
      }
    }
  } catch (error: unknown) {
    // 恢复开关状态
    smsForm.value.enabled = !smsForm.value.enabled
    message.error(getErrorMessage(error, '保存失败'))
  }
}

async function handleWechatEnabledChange() {
  try {
    await saveConfigValue(
      'client-service.notification.wechat.enabled',
      String(wechatForm.value.enabled),
      'BOOLEAN',
      '是否启用微信通知'
    )
    message.success(wechatForm.value.enabled ? '微信通知已启用' : '微信通知已禁用')
    if (!wechatForm.value.enabled) {
      // 禁用时清空本地配置
      wechatForm.value.appId = ''
      wechatForm.value.appSecret = ''
      wechatForm.value.templateId = ''
    }
  } catch (error: unknown) {
    // 恢复开关状态
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
  padding: 0;
}

.notification-settings-container :deep(.ant-card) {
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.notification-settings-container :deep(.ant-card-head) {
  padding: 16px 20px;
}

.notification-settings-container :deep(.ant-card-body) {
  padding: 20px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .notification-settings-container {
    padding: 0;
  }
  
  .notification-settings-container :deep(.ant-card) {
    margin-bottom: 16px;
    border-radius: 8px;
  }
  
  .notification-settings-container :deep(.ant-card-head) {
    padding: 12px 16px;
    min-height: auto;
    flex-wrap: wrap;
  }
  
  .notification-settings-container :deep(.ant-card-head-wrapper) {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .notification-settings-container :deep(.ant-card-head-title) {
    font-size: 14px;
    flex: 1 1 auto;
  }
  
  .notification-settings-container :deep(.ant-card-extra) {
    margin-left: 0 !important;
  }
  
  .notification-settings-container :deep(.ant-card-extra .ant-btn) {
    height: 32px;
    padding: 0 12px;
    font-size: 12px;
  }
  
  .notification-settings-container :deep(.ant-card-body) {
    padding: 16px 12px;
  }
  
  .notification-settings-container :deep(.ant-alert) {
    margin-bottom: 16px;
    font-size: 12px;
  }
  
  .notification-settings-container :deep(.ant-alert-message) {
    font-size: 13px;
    margin-bottom: 4px;
  }
  
  .notification-settings-container :deep(.ant-alert-description) {
    font-size: 12px;
    line-height: 1.5;
  }
  
  .notification-settings-container :deep(.ant-divider) {
    margin: 12px 0;
  }
  
  .notification-settings-container :deep(.ant-divider-horizontal.ant-divider-with-text-left) {
    font-size: 13px;
  }
  
  .notification-settings-container :deep(.ant-form-item) {
    margin-bottom: 16px;
  }
  
  .notification-settings-container :deep(.ant-form-item-label) {
    padding-bottom: 4px;
  }
  
  .notification-settings-container :deep(.ant-form-item-label > label) {
    font-size: 13px;
    height: auto;
  }
  
  .notification-settings-container :deep(.ant-input),
  .notification-settings-container :deep(.ant-input-number),
  .notification-settings-container :deep(.ant-input-password),
  .notification-settings-container :deep(.ant-select) {
    font-size: 14px;
    height: 36px;
  }
  
  .notification-settings-container :deep(.ant-input-number) {
    width: 100% !important;
  }
  
  .notification-settings-container :deep(.ant-btn) {
    height: 36px;
    padding: 0 16px;
    font-size: 13px;
    width: 100%;
  }
  
  .notification-settings-container :deep(.ant-switch) {
    min-width: 44px;
    height: 22px;
  }
  
  .notification-settings-container :deep(.ant-switch + span) {
    font-size: 12px;
    margin-left: 8px;
  }
  
  .notification-settings-container :deep(div[style*="margin-top: 4px"]) {
    font-size: 11px !important;
    margin-top: 4px !important;
    line-height: 1.4;
  }
}

@media (max-width: 480px) {
  .notification-settings-container :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .notification-settings-container :deep(.ant-card-head-title) {
    font-size: 13px;
  }
  
  .notification-settings-container :deep(.ant-card-body) {
    padding: 12px 8px;
  }
  
  .notification-settings-container :deep(.ant-alert) {
    margin-bottom: 12px;
    padding: 8px 12px;
  }
  
  .notification-settings-container :deep(.ant-alert-message) {
    font-size: 12px;
  }
  
  .notification-settings-container :deep(.ant-alert-description) {
    font-size: 11px;
  }
  
  .notification-settings-container :deep(.ant-form-item) {
    margin-bottom: 12px;
  }
  
  .notification-settings-container :deep(.ant-form-item-label > label) {
    font-size: 12px;
  }
  
  .notification-settings-container :deep(.ant-input),
  .notification-settings-container :deep(.ant-input-number),
  .notification-settings-container :deep(.ant-input-password),
  .notification-settings-container :deep(.ant-select) {
    font-size: 14px;
    height: 36px;
  }
  
  .notification-settings-container :deep(.ant-btn) {
    height: 36px;
    font-size: 13px;
  }
  
  .notification-settings-container :deep(.ant-switch + span) {
    font-size: 11px;
    margin-left: 6px;
  }
  
  .notification-settings-container :deep(div[style*="margin-top: 4px"]) {
    font-size: 10px !important;
  }
}
</style>
