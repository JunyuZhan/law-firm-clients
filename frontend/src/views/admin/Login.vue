<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-panel glass-panel">
        <div class="login-brand">
          <div class="logo">
            <img
              v-if="appConfigStore.logoUrl"
              :src="appConfigStore.logoUrl"
              alt="Logo"
            >
            <svg
              v-else
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path d="M12 3L20 7.5V16.5L12 21L4 16.5V7.5L12 3Z" />
              <path d="M12 12L20 7.5" />
              <path d="M12 12V21" />
              <path d="M12 12L4 7.5" />
            </svg>
          </div>
          <div class="brand-copy">
            <p class="brand-kicker">
              管理员登录
            </p>
            <h1 class="editorial-title brand-title">
              {{ appConfigStore.displayName }}
            </h1>
          </div>
        </div>

        <a-divider class="brand-divider" />

        <div class="form-header">
          <p class="form-header-hint">
            使用管理员账号登录。
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
              placeholder="请输入用户名"
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
              placeholder="请输入密码"
              class="login-input"
              @press-enter="handleLogin"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item name="captchaText">
            <label class="captcha-label">验证码</label>
            <div class="captcha-row">
              <a-input
                v-model:value="form.captchaText"
                size="large"
                placeholder="请输入验证码"
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
                <span v-else>加载中...</span>
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
              登录后台
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
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppConfigStore } from '@/stores/appConfig'
import { getCaptcha } from '@/api/auth'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

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
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度为 3-32 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只允许字母、数字和下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 64, message: '密码长度为 8-64 个字符', trigger: 'blur' },
  ],
  captchaText: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度为 4-6 个字符', trigger: 'blur' },
  ],
}

async function loadCaptcha() {
  try {
    const response = await getCaptcha(captchaId.value)
    if (response && response.captchaId && response.captchaImage) {
      captchaId.value = response.captchaId
      captchaImage.value = response.captchaImage
    } else {
      message.error('验证码响应格式错误，请刷新页面重试')
    }
  } catch (error: unknown) {
    const errorMsg = getErrorMessage(error, '获取验证码失败')
    message.error(errorMsg + '，请刷新页面重试')
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
    errorMessage.value = '请输入验证码'
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
    message.success('登录成功')
    router.push('/admin/matters')
  } catch (error: unknown) {
    errorMessage.value = getErrorMessage(error, '登录失败，请检查用户名和密码')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  appConfigStore.loadConfig()
  loadCaptcha()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(245, 158, 11, 0.08), transparent 28%),
    linear-gradient(180deg, var(--lex-bg) 0%, var(--lex-bg-muted) 100%);
}

.login-shell {
  min-height: calc(100vh - 48px);
  width: min(640px, 100%);
  margin: 0 auto;
  display: grid;
  place-items: center;
}

.login-panel {
  width: min(520px, 100%);
  padding: 32px;
  border-radius: 8px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.98);
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.logo {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo svg {
  width: 32px;
  height: 32px;
  color: var(--primary-color);
}

.brand-copy {
  display: grid;
  gap: 2px;
}

.brand-kicker {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 10px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  font-weight: 700;
}

.brand-title {
  margin: 0;
  font-size: clamp(20px, 3vw, 26px);
  color: var(--lex-primary);
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-divider {
  margin: 0 0 18px !important;
  border-color: rgba(15, 23, 42, 0.06);
}

.form-header {
  margin-bottom: 18px;
}

.form-header-hint {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 15px;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 14px;
}

.login-input :deep(.ant-input),
.login-input :deep(.ant-input-affix-wrapper) {
  min-height: 48px;
  border-radius: 8px;
  border-color: rgba(15, 23, 42, 0.1);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: none;
}

.login-input :deep(.ant-input-affix-wrapper:hover),
.login-input :deep(.ant-input-affix-wrapper:focus),
.login-input :deep(.ant-input-affix-wrapper-focused),
.login-input :deep(.ant-input:hover),
.login-input :deep(.ant-input:focus) {
  border-color: var(--lex-accent-strong);
  box-shadow: 0 0 0 3px var(--lex-accent-soft);
}

.input-icon {
  color: var(--text-tertiary);
}

.captcha-label {
  display: inline-block;
  margin-bottom: 6px;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-image {
  min-width: 128px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: var(--lex-surface-strong);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.captcha-image:hover {
  border-color: color-mix(in srgb, var(--lex-accent) 40%, var(--lex-outline));
  box-shadow: 0 0 0 3px var(--accent-color-lighter);
}

.captcha-image img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-form :deep(.ant-btn-primary) {
  min-height: 48px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 0.02em;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

.error-alert {
  margin-top: 8px;
  border-radius: 8px;
}

.form-footer {
  margin-top: 22px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.8;
}

.icp-link a {
  color: inherit;
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    min-height: calc(100vh - 32px);
    width: 100%;
    place-items: start center;
  }

  .login-panel {
    width: 100%;
    padding: 20px;
  }

  .login-brand {
    margin-bottom: 10px;
  }

  .brand-divider {
    margin-bottom: 16px !important;
  }

  .logo {
    width: 40px;
    height: 40px;
  }

  .captcha-row {
    flex-direction: column;
  }

  .captcha-image {
    min-height: 52px;
  }
}
</style>
