<template>
  <div class="login-page">
    <div class="login-shell fade-in">
      <section class="login-panel surface-card">
        <div class="login-panel__brand">
          <div class="login-brand-topline">
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
              <p class="brand-label">
                {{ organizationName || '律师事务所' }}
              </p>
              <h1 class="brand-system-name">
                {{ UI_TEXTS.loginTitle }}
              </h1>
            </div>
          </div>

          <div class="brand-copy">
            <span class="eyebrow">Administrator Access</span>
            <h2 class="brand-title">
              管理后台登录
            </h2>
            <p class="brand-subtitle">
              聚焦客户协作、事项流转与系统配置，仅向已授权管理员开放。
            </p>
          </div>

          <div class="brand-points">
            <article
              v-for="item in adminHighlights"
              :key="item.title"
              class="brand-point"
            >
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </article>
          </div>
        </div>

        <div class="login-panel__form">
          <div class="form-panel-head">
            <span class="form-eyebrow">管理端登录</span>
            <h2 class="form-panel-head__title">
              进入工作台
            </h2>
            <p class="form-panel-head__desc">
              请使用已分配的管理员账户完成校验后进入工作台。
            </p>
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
        </div>
      </section>
    </div>
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

const adminHighlights = [
  {
    title: '受控登录入口',
    description: '仅向已授权管理员开放，登录权限与访问边界统一控制。',
  },
  {
    title: '统一事务管理',
    description: '案件、文件与系统配置集中处理，减少后台切换成本。',
  },
]

const currentYear = new Date().getFullYear()
const organizationName = computed(() => appConfigStore.lawFirmName || appConfigStore.displayName)
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
  --login-accent: #b88a2b;
  --login-accent-soft: rgba(184, 138, 43, 0.12);
  --login-ink: #0f172a;
  --login-ink-soft: #334155;
  --login-border: rgba(15, 23, 42, 0.08);
  min-height: 100vh;
  padding: 28px;
  background:
    radial-gradient(circle at top left, rgba(15, 23, 42, 0.08), transparent 30%),
    radial-gradient(circle at top right, rgba(184, 138, 43, 0.08), transparent 22%),
    linear-gradient(180deg, #fafaf9 0%, #f8fafc 48%, #f1f5f9 100%);
}

.login-shell {
  min-height: calc(100vh - 56px);
  display: grid;
  place-items: center;
}

.login-panel {
  display: grid;
  grid-template-columns: minmax(0, 0.96fr) minmax(360px, 440px);
  width: min(1040px, 100%);
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 26px 56px rgba(15, 23, 42, 0.09);
}

.login-panel__brand {
  display: grid;
  gap: 28px;
  padding: 44px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.94)),
    radial-gradient(circle at top left, rgba(184, 138, 43, 0.08), transparent 36%);
  border-right: 1px solid var(--login-border);
}

.login-brand-topline {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-shell {
  display: grid;
  place-items: center;
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
  color: var(--login-accent);
}

.logo-image,
.logo-fallback {
  width: 100%;
  height: 100%;
}

.brand-lockup {
  min-width: 0;
}

.brand-label,
.brand-system-name,
.brand-title,
.brand-subtitle,
.form-panel-head__title,
.form-panel-head__desc,
.form-eyebrow {
  margin: 0;
}

.brand-label,
.form-eyebrow {
  color: var(--login-accent);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.brand-system-name {
  margin-top: 6px;
  color: var(--login-ink);
  font-size: 20px;
  line-height: 1.3;
}

.brand-copy {
  display: grid;
  gap: 12px;
  max-width: 460px;
}

.brand-title,
.form-panel-head__title {
  color: var(--login-ink);
  font-size: clamp(30px, 3.8vw, 40px);
  line-height: 1.12;
  letter-spacing: -0.035em;
}

.brand-subtitle,
.form-panel-head__desc,
.brand-point p,
.form-footer p {
  color: #475569;
  line-height: 1.75;
}

.brand-subtitle {
  font-size: 15px;
  max-width: 30em;
}

.brand-points {
  display: grid;
  gap: 14px;
  margin-top: auto;
}

.brand-point {
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.04);
}

.brand-point h3 {
  margin: 0;
  color: var(--login-ink);
  font-size: 16px;
}

.brand-point p {
  margin-top: 8px;
  font-size: 14px;
}

.login-panel__form {
  display: grid;
  align-content: center;
  gap: 26px;
  padding: 44px 38px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99), rgba(250, 250, 249, 0.96));
}

.form-panel-head {
  display: grid;
  gap: 10px;
}

.form-panel-head__title {
  font-size: 32px;
}

.form-panel-head__desc {
  font-size: 14px;
  max-width: 26em;
}

.login-form {
  margin-bottom: 2px;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input),
.captcha-image {
  border-radius: 14px;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input) {
  border-color: rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: none;
}

.login-form :deep(.ant-input-affix-wrapper:hover),
.login-form :deep(.ant-input:hover),
.captcha-image:hover {
  border-color: rgba(184, 138, 43, 0.3);
}

.login-form :deep(.ant-input-affix-wrapper-focused),
.login-form :deep(.ant-input:focus),
.captcha-image:focus-visible {
  border-color: var(--login-accent);
  box-shadow: 0 0 0 3px rgba(184, 138, 43, 0.14);
}

.login-input {
  min-height: 52px;
}

.input-icon {
  color: #64748b;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 13fr) minmax(0, 6fr);
  align-items: stretch;
  gap: 12px;
}

.captcha-input {
  min-width: 0;
}

.captcha-image {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 52px;
  padding: 4px 10px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: linear-gradient(180deg, #fff 0%, #f8fafc 100%);
  overflow: hidden;
  cursor: pointer;
}

.captcha-image img {
  display: block;
  width: 100%;
  height: auto;
  max-height: 40px;
  object-fit: contain;
}

.login-button {
  height: 52px !important;
  border-radius: 14px !important;
  font-size: 15px !important;
  font-weight: 700 !important;
  letter-spacing: 0.04em;
}

.error-alert {
  margin-top: -4px;
}

.form-footer {
  display: grid;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.form-footer p {
  font-size: 12px;
}

.icp-link a {
  color: inherit;
  text-decoration: none;
}

.icp-link a:hover {
  color: var(--login-ink);
}

@media (max-width: 960px) {
  .login-page {
    padding: 20px;
  }

  .login-shell {
    min-height: calc(100vh - 40px);
  }

  .login-panel {
    grid-template-columns: 1fr;
  }

  .login-panel__brand {
    gap: 22px;
    padding: 32px 28px 20px;
    border-right: 0;
    border-bottom: 1px solid var(--login-border);
  }

  .login-panel__form {
    padding: 32px 28px;
  }
}

@media (max-width: 560px) {
  .login-page {
    padding: 12px;
  }

  .login-shell {
    min-height: calc(100vh - 24px);
  }

  .login-panel {
    border-radius: 22px;
  }

  .login-panel__brand,
  .login-panel__form {
    padding-inline: 20px;
  }

  .login-brand-topline {
    align-items: flex-start;
  }

  .logo-shell {
    width: 52px;
    height: 52px;
    border-radius: 16px;
  }

  .brand-title,
  .form-panel-head__title {
    font-size: 28px;
  }

  .captcha-row {
    grid-template-columns: 1fr;
  }
}
</style>
