<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="login-left-content">
          <div class="logo-container">
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
            <span class="brand-name">{{ organizationName || '律师事务所' }}</span>
          </div>
          
          <div class="hero-text">
            <h1>{{ UI_TEXTS.loginTitle }}</h1>
            <p>专业、高效的客户服务系统，聚焦客户协作、事项流转与系统配置。</p>
          </div>

          <div class="feature-list">
            <div
              v-for="item in adminHighlights"
              :key="item.title"
              class="feature-item"
            >
              <div class="feature-icon">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <div class="feature-text">
                <h3>{{ item.title }}</h3>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="login-form-container">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>请使用管理员账户登录系统</p>
          </div>

          <a-form
            :model="form"
            :rules="rules"
            layout="vertical"
            class="login-form"
            @finish="handleLogin"
          >
            <a-form-item name="username">
              <a-input
                v-model:value="form.username"
                size="large"
                :placeholder="ADMIN_LOGIN_TEXTS.placeholders.username"
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
                @press-enter="handleLogin"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item name="captchaText">
              <div class="captcha-wrapper">
                <a-input
                  v-model:value="form.captchaText"
                  size="large"
                  :placeholder="ADMIN_LOGIN_TEXTS.placeholders.captcha"
                  @press-enter="handleLogin"
                />
                <div
                  class="captcha-img-container"
                  @click="refreshCaptcha"
                >
                  <img
                    v-if="captchaImage"
                    :src="captchaImage"
                    alt="验证码"
                  >
                  <span
                    v-else
                    class="captcha-loading"
                  >{{ ADMIN_LOGIN_TEXTS.placeholders.captchaLoading }}</span>
                </div>
              </div>
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                html-type="submit"
                size="large"
                :loading="loading"
                block
                class="submit-btn"
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
              >{{ appConfigStore.icpLicense }}</a>
            </p>
          </div>
        </div>
      </div>
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
  min-height: 100vh;
  display: flex;
  background-color: #f3f4f6;
}

.login-container {
  display: flex;
  flex: 1;
  width: 100%;
}

.login-left {
  display: none;
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  color: white;
  position: relative;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .login-left {
    display: flex;
    flex: 1;
    max-width: 50%;
  }
}

.login-left-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  padding: 64px;
  width: 100%;
  height: 100%;
  justify-content: space-between;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-image, .logo-fallback {
  width: 40px;
  height: 40px;
  color: white;
}

.brand-name {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.hero-text {
  margin-top: 60px;
  margin-bottom: auto;
}

.hero-text h1 {
  font-size: 3rem;
  font-weight: 700;
  color: white;
  line-height: 1.2;
  margin-bottom: 24px;
}

.hero-text p {
  font-size: 1.125rem;
  color: #bfdbfe;
  max-width: 480px;
  line-height: 1.6;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 48px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: #93c5fd;
  flex-shrink: 0;
}

.feature-icon svg {
  width: 20px;
  height: 20px;
}

.feature-text h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: white;
  margin: 0 0 4px;
}

.feature-text p {
  font-size: 0.875rem;
  color: #bfdbfe;
  margin: 0;
}

.login-right {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background-color: white;
}

.login-form-container {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 1rem;
  color: #6b7280;
  margin: 0;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input) {
  border-radius: 8px;
  padding-top: 8px;
  padding-bottom: 8px;
}

.input-icon {
  color: #9ca3af;
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
}

.captcha-wrapper .ant-input {
  flex: 1;
}

.captcha-img-container {
  width: 120px;
  height: 40px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9fafb;
}

.captcha-img-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  font-size: 0.75rem;
  color: #6b7280;
}

.submit-btn {
  height: 44px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  background-color: #1d4ed8;
  border-color: #1d4ed8;
}

.submit-btn:hover {
  background-color: #1e40af !important;
  border-color: #1e40af !important;
}

.error-alert {
  margin-bottom: 24px;
  border-radius: 8px;
}

.form-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 0.875rem;
  color: #6b7280;
}

.form-footer p {
  margin: 4px 0;
}

.icp-link a {
  color: #6b7280;
  text-decoration: none;
  transition: color 0.2s;
}

.icp-link a:hover {
  color: #111827;
}
</style>
