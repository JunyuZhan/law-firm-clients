<template>
  <div class="login-page">
    <a-layout class="login-shell">
      <a-layout-content class="login-content">
        <a-row
          class="login-grid"
          :gutter="[0, 0]"
        >
          <a-col
            :xs="24"
            :lg="14"
            class="brand-panel"
          >
            <div class="brand-topline">
              <div class="logo-shell">
                <img
                  v-if="appConfigStore.logoUrl"
                  :src="appConfigStore.logoUrl"
                  alt="Logo"
                  class="logo-image"
                >
                <svg
                  v-else
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  aria-hidden="true"
                  class="logo-fallback"
                >
                  <path d="M12 3L20 7.5V16.5L12 21L4 16.5V7.5L12 3Z" />
                  <path d="M12 12L20 7.5" />
                  <path d="M12 12V21" />
                  <path d="M12 12L4 7.5" />
                </svg>
              </div>

              <div class="brand-lockup">
                <a-typography-text class="brand-label">
                  {{ organizationName || '律师事务所' }}
                </a-typography-text>
                <a-typography-text class="brand-heading">
                  {{ UI_TEXTS.loginTitle }}
                </a-typography-text>
              </div>
            </div>

            <div class="brand-copy">
              <a-typography-text class="brand-kicker">
                Administrator Access
              </a-typography-text>
              <a-typography-title
                :level="1"
                class="form-title"
              >
                {{ appSlogan }}
              </a-typography-title>
              <a-typography-paragraph class="form-subtitle">
                管理员通过本入口进入系统后台，统一处理客户协作、案件流转、通知记录与平台配置。
              </a-typography-paragraph>
            </div>

            <a-space
              direction="vertical"
              :size="18"
              class="brand-points"
            >
              <article class="brand-point">
                <div>
                  <a-typography-title
                    :level="4"
                    class="brand-point__title"
                  >
                    管理员受控入口
                  </a-typography-title>
                  <a-typography-paragraph class="brand-point__description">
                    仅向已授权的系统管理员开放，所有登录行为均纳入身份校验与权限边界控制。
                  </a-typography-paragraph>
                </div>
              </article>

              <article class="brand-point">
                <div>
                  <a-typography-title
                    :level="4"
                    class="brand-point__title"
                  >
                    统一资料治理
                  </a-typography-title>
                  <a-typography-paragraph class="brand-point__description">
                    案件、文书、通知与客户协作信息在统一后台中维护，保持数据口径与操作秩序一致。
                  </a-typography-paragraph>
                </div>
              </article>
            </a-space>
          </a-col>

          <a-col
            :xs="24"
            :lg="10"
            class="form-panel-wrap"
          >
            <a-card
              :bordered="false"
              class="form-panel"
            >
              <div class="form-panel-head">
                <a-typography-text class="form-eyebrow">
                  管理端登录
                </a-typography-text>
                <a-typography-title
                  :level="2"
                  class="form-panel-head__title"
                >
                  进入工作台
                </a-typography-title>
                <a-typography-paragraph class="form-panel-head__desc">
                  请使用已分配的账户信息完成身份校验后进入管理后台。
                </a-typography-paragraph>
              </div>

              <a-form
                :model="form"
                :rules="rules"
                class="login-form"
                @finish="handleLogin"
              >
                <a-form-item name="username">
                  <a-input
                    v-model:value="form.username"
                    size="large"
                    :placeholder="ADMIN_LOGIN_TEXTS.placeholders.username"
                    class="login-input"
                  >
                    <template #prefix>
                      <UserOutlined class="input-icon" />
                    </template>
                  </a-input>
                </a-form-item>

                <a-form-item name="password">
                  <a-input-password
                    v-model:value="form.password"
                    size="large"
                    :placeholder="ADMIN_LOGIN_TEXTS.placeholders.password"
                    class="login-input"
                    @press-enter="handleLogin"
                  >
                    <template #prefix>
                      <LockOutlined class="input-icon" />
                    </template>
                  </a-input-password>
                </a-form-item>

                <a-form-item name="captchaText">
                  <div class="captcha-row">
                    <a-input
                      v-model:value="form.captchaText"
                      size="large"
                      :placeholder="ADMIN_LOGIN_TEXTS.placeholders.captcha"
                      class="login-input captcha-input"
                      @press-enter="handleLogin"
                    />
                    <button
                      type="button"
                      class="captcha-image"
                      @click="refreshCaptcha"
                    >
                      <img
                        v-if="captchaImage"
                        :src="captchaImage"
                        alt="验证码"
                      >
                      <span v-else>{{ ADMIN_LOGIN_TEXTS.placeholders.captchaLoading }}</span>
                    </button>
                  </div>
                </a-form-item>

                <a-form-item>
                  <a-button
                    type="primary"
                    html-type="submit"
                    size="large"
                    :loading="loading"
                    block
                    class="login-button"
                  >
                    {{ UI_TEXTS.loginButton }}
                  </a-button>
                </a-form-item>
              </a-form>

              <a-alert
                v-if="errorMessage"
                :message="errorMessage"
                type="error"
                show-icon
                class="error-alert"
              />

              <div class="form-footer">
                <p v-if="footerCopyright">
                  {{ footerCopyright }}
                </p>
                <p v-else>
                  © {{ currentYear }} {{ organizationName }}
                </p>
                <p
                  v-if="appConfigStore.icpLicense"
                  class="icp-link"
                >
                  <a
                    href="https://beian.miit.gov.cn/"
                    target="_blank"
                    rel="noopener"
                  >
                    {{ appConfigStore.icpLicense }}
                  </a>
                </p>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { getCaptcha } from '@/api/auth'
import { useAppConfigStore } from '@/stores/appConfig'
import { useAuthStore } from '@/stores/auth'
import { UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_LOGIN_TEXTS } from '@/constants/adminTexts'

const router = useRouter()
const authStore = useAuthStore()
const appConfigStore = useAppConfigStore()

const currentYear = new Date().getFullYear()
const organizationName = computed(() => appConfigStore.lawFirmName || appConfigStore.displayName)
const appSlogan = computed(() => appConfigStore.appSlogan?.trim() || '专业 · 诚信 · 高效')
const footerCopyright = computed(() => {
  const normalized = appConfigStore.copyright
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  if (normalized.length <= 1) {
    return normalized[0] ?? ''
  }

  const orgName = organizationName.value?.trim()
  if (!orgName) {
    return normalized.join(' ')
  }

  const dedupedLines = normalized.filter((line, index) => !(index === 0 && line === orgName))
  return dedupedLines.join(' ')
})

const form = ref({
  username: '',
  password: '',
  captchaText: '',
})

const loading = ref(false)
const errorMessage = ref('')
const captchaId = ref<string>('')
const captchaImage = ref<string>('')

function getErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null) {
    const axiosError = error as { response?: { data?: { message?: string } } }
    if (axiosError.response?.data?.message) return axiosError.response.data.message
  }
  return fallback
}

const rules: Record<string, Rule[]> = {
  username: [
    { required: true, message: ADMIN_LOGIN_TEXTS.validation.usernameRequired, trigger: 'blur' },
    { min: 3, max: 32, message: ADMIN_LOGIN_TEXTS.validation.usernameLength, trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: ADMIN_LOGIN_TEXTS.validation.usernamePattern, trigger: 'blur' },
  ],
  password: [
    { required: true, message: ADMIN_LOGIN_TEXTS.validation.passwordRequired, trigger: 'blur' },
    { min: 8, max: 64, message: ADMIN_LOGIN_TEXTS.validation.passwordLength, trigger: 'blur' },
  ],
  captchaText: [
    { required: true, message: ADMIN_LOGIN_TEXTS.validation.captchaRequired, trigger: 'blur' },
    { min: 4, max: 6, message: ADMIN_LOGIN_TEXTS.validation.captchaLength, trigger: 'blur' },
  ],
}

async function loadCaptcha() {
  try {
    const response = await getCaptcha(captchaId.value)
    if (response && response.captchaId && response.captchaImage) {
      captchaId.value = response.captchaId
      captchaImage.value = response.captchaImage
    } else {
      message.error(ADMIN_LOGIN_TEXTS.feedback.captchaFormatError)
    }
  } catch (error: unknown) {
    const errorMsg = getErrorMessage(error, ADMIN_LOGIN_TEXTS.feedback.captchaLoadFailed)
    message.error(errorMsg)
  }
}

function refreshCaptcha() {
  form.value.captchaText = ''
  loadCaptcha()
}

async function handleLogin() {
  if (loading.value) return

  errorMessage.value = ''

  if (!form.value.captchaText || !form.value.captchaText.trim()) {
    errorMessage.value = ADMIN_LOGIN_TEXTS.validation.captchaRequired
    return
  }

  loading.value = true

  try {
    await authStore.login(
      form.value.username,
      form.value.password,
      captchaId.value,
      form.value.captchaText,
    )
    message.success(ADMIN_LOGIN_TEXTS.feedback.loginSuccess)
    router.push(authStore.getCurrentUserInfo()?.superAdmin ? '/admin/matters' : '/admin/notifications')
  } catch (error: unknown) {
    errorMessage.value = getErrorMessage(error, ADMIN_LOGIN_TEXTS.feedback.loginFailed)
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!appConfigStore.loaded && !appConfigStore.loading) {
    appConfigStore.loadConfig()
  }
  loadCaptcha()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(27, 59, 95, 0.12), transparent 32%),
    linear-gradient(180deg, #f6f7f9 0%, #eef1f5 54%, #f7f9fc 100%);
}

.login-shell {
  min-height: calc(100vh - 64px);
  background: transparent;
}

.login-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-grid {
  width: min(1120px, 100%);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 24px 60px rgba(16, 42, 67, 0.08);
  backdrop-filter: blur(18px);
}

.brand-panel {
  display: grid;
  align-content: start;
  gap: 40px;
  min-height: 680px;
  padding: 56px;
  background:
    radial-gradient(circle at top right, rgba(27, 59, 95, 0.06), transparent 36%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.72) 0%, rgba(255, 255, 255, 0.42) 100%);
  border-right: 1px solid rgba(16, 42, 67, 0.08);
}

.brand-topline {
  display: flex;
  align-items: center;
  gap: 20px;
}

.brand-lockup {
  display: grid;
  gap: 6px;
}

.logo-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  flex-shrink: 0;
  border-radius: 16px;
  border: 1px solid rgba(16, 42, 67, 0.12);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 24px rgba(16, 42, 67, 0.06);
}

.logo-image,
.logo-fallback {
  width: 100%;
  height: 100%;
}

.brand-copy {
  display: grid;
  gap: 16px;
  max-width: 520px;
}

.brand-label,
.brand-heading,
.brand-kicker {
  margin-bottom: 0;
}

.brand-label {
  color: #1b3b5f;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.brand-heading {
  color: #486581;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
}

.brand-kicker {
  color: rgba(72, 101, 129, 0.82);
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.form-title {
  margin-bottom: 0;
  font-family: 'EB Garamond', 'STSong', 'Songti SC', serif;
  font-size: clamp(54px, 6vw, 82px);
  font-weight: 600;
  line-height: 0.96;
  color: #102a43;
  letter-spacing: -0.05em;
}

.form-subtitle {
  margin-bottom: 0;
  max-width: 460px;
  font-size: 16px;
  color: #627d98;
  line-height: 1.9;
}

.brand-points {
  width: 100%;
  max-width: 520px;
  margin-top: auto;
}

.brand-point {
  padding-top: 18px;
  border-top: 1px solid rgba(16, 42, 67, 0.08);
}

.brand-point__title {
  margin-bottom: 8px;
  color: #102a43;
  font-size: 17px;
  font-weight: 600;
}

.brand-point__description {
  margin-bottom: 0;
  color: #627d98;
  font-size: 14px;
  line-height: 1.8;
}

.form-panel-wrap {
  display: flex;
}

.form-panel {
  width: 100%;
  border-radius: 0;
  background: rgba(255, 255, 255, 0.98);
}

.form-panel :deep(.ant-card-body) {
  display: grid;
  align-content: center;
  gap: 28px;
  min-height: 100%;
  padding: 56px 44px;
}

.form-panel-head {
  display: grid;
  gap: 10px;
}

.form-eyebrow {
  color: #486581;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.form-panel-head__title {
  margin-bottom: 0;
  color: #102a43;
  font-family: 'EB Garamond', 'STSong', 'Songti SC', serif;
  font-size: 34px;
  font-weight: 600;
  line-height: 1.2;
  letter-spacing: -0.03em;
}

.form-panel-head__desc {
  margin-bottom: 0;
  color: #627d98;
  font-size: 14px;
  line-height: 1.8;
}

.login-form {
  margin-bottom: 12px;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input) {
  border-radius: 4px;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.login-input {
  min-height: 48px;
}

.input-icon {
  color: #999999;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 13fr) minmax(0, 6fr);
  align-items: stretch;
  column-gap: 5%;
}

.captcha-input {
  min-width: 0;
}

.captcha-image {
  min-height: 48px;
  padding: 4px 8px;
  border: 1px solid #d9e2ec;
  border-radius: 4px;
  background: linear-gradient(180deg, #fff 0%, #f8fafc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
}

.captcha-image img {
  display: block;
  width: 100%;
  height: auto;
  max-height: 38px;
  object-fit: contain;
}

.login-button {
  height: 48px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.error-alert {
  margin-top: -4px;
}

.form-footer {
  display: grid;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px solid rgba(16, 42, 67, 0.08);
}

.form-footer p {
  margin: 0;
  color: #829ab1;
  font-size: 12px;
  line-height: 1.7;
}

.icp-link a {
  color: inherit;
  text-decoration: none;
}

@media (max-width: 991px) {
  .brand-panel {
    min-height: auto;
    padding: 40px 28px 32px;
    border-right: 0;
    border-bottom: 1px solid rgba(16, 42, 67, 0.08);
  }

  .form-panel :deep(.ant-card-body) {
    padding: 36px 28px 32px;
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }

  .brand-panel {
    padding-inline: 20px;
  }

  .form-panel :deep(.ant-card-body) {
    padding-inline: 20px;
  }

  .brand-topline {
    align-items: flex-start;
  }

  .logo-shell {
    width: 56px;
    height: 56px;
  }

  .form-title {
    font-size: clamp(40px, 16vw, 56px);
  }

  .captcha-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .captcha-image {
    min-height: 48px;
  }
}
</style>
