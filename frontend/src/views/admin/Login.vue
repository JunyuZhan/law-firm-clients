<template>
  <div class="login-page">
    <section class="login-card">
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

      <div class="brand-copy">
        <p class="brand-label">
          {{ organizationName || '律师事务所' }}
        </p>
        <h1 class="form-title">{{ UI_TEXTS.loginTitle }}</h1>
        <p class="form-subtitle">{{ UI_TEXTS.loginSubtitle }}</p>
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
        <p v-if="appConfigStore.copyright">
          {{ appConfigStore.copyright }}
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
    </section>
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
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at top, rgba(27, 59, 95, 0.16), transparent 42%),
    linear-gradient(180deg, #f5f7fa 0%, #edf2f7 100%);
}

.login-card {
  width: min(400px, 100%);
  padding: 48px 40px;
  border-radius: 20px;
  border: 1px solid rgba(232, 236, 239, 0.92);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 48px rgba(16, 42, 67, 0.12);
  display: grid;
  gap: 24px;
}

.logo-shell {
  width: 56px;
  height: 56px;
  margin: 0 auto;
}

.logo-image,
.logo-fallback {
  width: 100%;
  height: 100%;
}

.brand-copy {
  display: grid;
  gap: 8px;
  text-align: center;
}

.brand-label {
  margin: 0;
  font-size: 13px;
  color: #627d98;
  letter-spacing: 0.08em;
}

.form-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.3;
  color: #102a43;
}

.form-subtitle {
  margin: 0;
  font-size: 14px;
  color: #627d98;
}

.login-form {
  display: grid;
  gap: 4px;
}

.login-input {
  min-height: 44px;
}

.input-icon {
  color: #999999;
}

.captcha-row {
  display: flex;
  align-items: stretch;
  gap: 5%;
}

.captcha-input {
  flex: 0 0 65%;
  min-width: 0;
}

.captcha-image {
  flex: 0 0 30%;
  min-height: 44px;
  padding: 0 10px;
  border: 1px solid #d9e2ec;
  border-radius: 4px;
  background: linear-gradient(180deg, #fff 0%, #f8fafc 100%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-button {
  height: 44px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
}

.error-alert {
  margin-top: -8px;
}

.form-footer {
  display: grid;
  gap: 6px;
  text-align: center;
}

.form-footer p {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 12px;
}

.icp-link a {
  color: inherit;
  text-decoration: none;
}

@media (max-width: 480px) {
  .login-page {
    padding: 16px;
  }

  .login-card {
    padding: 40px 20px;
  }

  .captcha-row {
    gap: 12px;
    flex-wrap: wrap;
  }

  .captcha-image {
    flex: 1 1 100%;
  }
}
</style>
