<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-frame">
        <aside class="frame-aside">
          <div class="aside-brand">
            <p class="brand-name">
              {{ organizationName || '律师事务所' }}
            </p>
            <div class="brand-lockup">
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
                <h1 class="brand-title">
                  {{ systemName }}
                </h1>
                <p class="brand-subtitle">
                  客户服务管理后台
                </p>
              </div>
            </div>

            <div class="brand-rule" />
            <p class="aside-lead">
              统一管理客户项目、通知与文件资料。
            </p>
          </div>
        </aside>

        <section class="frame-form">
          <div class="form-panel">
            <div class="form-header">
              <h2 class="form-title">管理后台登录</h2>
              <p class="form-hint">
                请输入账号和密码
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
          </div>
        </section>
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

const router = useRouter()
const authStore = useAuthStore()
const appConfigStore = useAppConfigStore()

const currentYear = new Date().getFullYear()
const organizationName = computed(() => appConfigStore.lawFirmName || appConfigStore.displayName)
const systemName = computed(() => appConfigStore.appShortName || appConfigStore.appName || '客户服务系统')

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
    router.push(authStore.getCurrentUserInfo()?.superAdmin ? '/admin/matters' : '/admin/notifications')
  } catch (error: unknown) {
    errorMessage.value = getErrorMessage(error, '登录失败，请检查用户名和密码')
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
  background: var(--lex-bg);
}

.login-shell {
  width: 100%;
  min-height: 100vh;
  margin: 0 auto;
  display: grid;
  place-items: center;
  padding: 24px;
}

.login-frame {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(320px, 0.82fr) minmax(420px, 1fr);
  max-width: 1180px;
  min-height: 720px;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.14);
  background: var(--lex-surface-strong);
  box-shadow: 0 32px 90px rgba(15, 23, 42, 0.14);
}

.frame-aside {
  display: grid;
  align-content: center;
  padding: 48px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.18), transparent 28%),
    linear-gradient(180deg, rgba(2, 6, 23, 0.98) 0%, rgba(15, 23, 42, 0.96) 100%);
  color: #f8fafc;
}

.aside-brand {
  display: grid;
  gap: 22px;
  max-width: 360px;
}

.brand-name {
  margin: 0;
  color: rgba(248, 250, 252, 0.92);
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 0.08em;
}

.brand-lockup {
  display: grid;
  gap: 18px;
}

.logo-shell {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.05);
  overflow: hidden;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-fallback {
  width: 36px;
  height: 36px;
  color: rgba(248, 250, 252, 0.92);
}

.brand-copy {
  display: grid;
  gap: 6px;
}

.brand-title {
  margin: 0;
  color: #f8fafc;
  font-family: var(--font-heading);
  font-size: clamp(30px, 4vw, 40px);
  line-height: 1.08;
}

.brand-subtitle {
  margin: 0;
  color: rgba(226, 232, 240, 0.68);
  font-size: 18px;
}

.brand-rule {
  width: 40px;
  height: 4px;
  border-radius: 999px;
  background: var(--lex-primary-soft);
}

.aside-lead {
  margin: 0;
  color: rgba(226, 232, 240, 0.82);
  font-size: 17px;
  line-height: 1.85;
}

.frame-form {
  display: grid;
  place-items: center;
  padding: 40px;
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.98) 100%);
}

.form-panel {
  width: min(100%, 420px);
  display: grid;
  gap: 20px;
}

.form-header {
  display: grid;
  gap: 8px;
}

.form-title {
  margin: 0;
  color: var(--lex-primary);
  font-family: var(--font-heading);
  font-size: 30px;
  line-height: 1.08;
}

.form-hint {
  margin: 0;
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.7;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 18px;
}

.login-input :deep(.ant-input),
.login-input :deep(.ant-input-affix-wrapper) {
  min-height: 50px;
  border-width: 0 0 2px;
  border-radius: 10px 10px 0 0;
  border-color: rgba(148, 163, 184, 0.3);
  background: var(--lex-bg-muted);
  box-shadow: none;
}

.login-input :deep(.ant-input-affix-wrapper:hover),
.login-input :deep(.ant-input-affix-wrapper-focused),
.login-input :deep(.ant-input:hover),
.login-input :deep(.ant-input:focus) {
  border-color: var(--lex-primary-soft);
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.12);
}

.input-icon {
  color: var(--text-tertiary);
}

.captcha-label {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-size: 13px;
}

.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  min-width: 132px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  background: var(--lex-surface);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.captcha-image:hover {
  border-color: rgba(30, 64, 175, 0.35);
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.12);
  transform: translateY(-1px);
}

.captcha-image img {
  display: block;
  width: 100%;
  height: 48px;
  object-fit: cover;
}

.captcha-image span {
  display: grid;
  place-items: center;
  width: 100%;
  height: 48px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.login-button {
  min-height: 50px;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 16px 32px rgba(30, 64, 175, 0.18);
}

.error-alert {
  margin-top: 4px;
  border-radius: 10px;
}

.form-footer {
  display: grid;
  gap: 4px;
  margin-top: 10px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.75;
}

.icp-link a {
  color: inherit;
}

@media (max-width: 1024px) {
  .login-frame {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .frame-aside {
    padding: 28px;
  }

  .frame-form {
    padding: 28px;
  }
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    padding: 16px;
  }

  .login-frame {
    border-radius: 16px;
  }

  .frame-aside,
  .frame-form {
    padding: 20px;
  }

  .aside-lead {
    font-size: 16px;
  }

  .form-title {
    font-size: 26px;
  }

  .captcha-row {
    flex-direction: column;
  }

  .captcha-image {
    min-width: 0;
  }
}
</style>
