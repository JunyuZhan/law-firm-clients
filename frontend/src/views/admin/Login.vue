<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-panel">
        <aside class="panel-intro">
          <p class="panel-kicker">Admin Console</p>
          <div class="brand-head">
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
                aria-hidden="true"
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
              <p
                v-if="organizationName"
                class="brand-meta"
              >
                {{ organizationName }}
              </p>
            </div>
          </div>

          <p class="panel-lead">
            统一管理项目、通知、文件资料与门户展示配置。
          </p>

          <div class="intro-grid">
            <article class="intro-card">
              <span>项目管理</span>
              <p>维护项目状态、有效期和客户访问范围。</p>
            </article>
            <article class="intro-card">
              <span>通知协作</span>
              <p>集中处理通知发送记录和协作过程追踪。</p>
            </article>
            <article class="intro-card">
              <span>品牌配置</span>
              <p>系统名称、Logo 与门户文案优先读取后台配置。</p>
            </article>
          </div>
        </aside>

        <section class="panel-form">
          <div class="form-header">
            <p class="panel-kicker">管理后台</p>
            <h2 class="form-title">登录系统</h2>
            <p class="form-header-hint">
              使用管理员账号进入工作台。
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
            <p class="config-note">
              品牌展示优先读取后台配置，未配置时回退到默认值。
            </p>
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
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(30, 64, 175, 0.1), transparent 28%),
    radial-gradient(circle at bottom right, rgba(245, 158, 11, 0.08), transparent 24%),
    linear-gradient(180deg, var(--lex-bg) 0%, var(--lex-bg-muted) 100%);
}

.login-shell {
  width: min(1100px, 100%);
  min-height: calc(100vh - 48px);
  margin: 0 auto;
  display: grid;
  place-items: center;
}

.login-panel {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 440px);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color-light);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.panel-intro,
.panel-form {
  padding: 32px;
}

.panel-intro {
  display: grid;
  align-content: start;
  gap: 20px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.94) 0%, rgba(241, 245, 249, 0.98) 100%);
  border-right: 1px solid var(--border-color-light);
}

.panel-kicker,
.intro-card span {
  margin: 0;
  color: var(--lex-accent-strong);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.brand-head {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color-light);
  background: var(--lex-surface-strong);
}

.logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo svg {
  width: 30px;
  height: 30px;
  color: var(--lex-primary-soft);
}

.brand-copy {
  display: grid;
  gap: 4px;
}

.brand-title,
.form-title {
  margin: 0;
  color: var(--lex-primary);
  font-family: var(--font-heading);
}

.brand-title {
  font-size: clamp(24px, 3vw, 30px);
}

.brand-meta,
.panel-lead,
.form-header-hint,
.intro-card p,
.form-footer {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.brand-meta {
  font-size: 13px;
}

.panel-lead {
  max-width: 34rem;
  font-size: 18px;
}

.intro-grid {
  display: grid;
  gap: 12px;
}

.intro-card {
  display: grid;
  gap: 8px;
  padding: 16px 18px;
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-xs);
}

.panel-form {
  display: grid;
  align-content: center;
}

.form-header {
  margin-bottom: 22px;
}

.form-title {
  margin-top: 8px;
  font-size: 28px;
  line-height: 1.1;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.login-input :deep(.ant-input),
.login-input :deep(.ant-input-affix-wrapper) {
  min-height: 48px;
  border-radius: var(--radius-sm);
  border-color: var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: none;
}

.login-input :deep(.ant-input-affix-wrapper:hover),
.login-input :deep(.ant-input-affix-wrapper-focused),
.login-input :deep(.ant-input:hover),
.login-input :deep(.ant-input:focus) {
  border-color: var(--lex-primary-soft);
  box-shadow: 0 0 0 2px rgba(30, 64, 175, 0.12);
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
  min-width: 128px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--lex-surface-strong);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.captcha-image:hover {
  border-color: rgba(30, 64, 175, 0.35);
  box-shadow: 0 0 0 2px rgba(30, 64, 175, 0.12);
}

.captcha-image img {
  display: block;
  width: 100%;
  height: 46px;
  object-fit: cover;
}

.captcha-image span {
  display: grid;
  place-items: center;
  width: 100%;
  height: 46px;
  color: var(--text-tertiary);
  font-size: 13px;
}

.login-button {
  min-height: 48px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  box-shadow: var(--shadow-sm);
}

.error-alert {
  margin-top: 8px;
  border-radius: var(--radius-md);
}

.form-footer {
  display: grid;
  gap: 4px;
  margin-top: 18px;
  font-size: 12px;
}

.config-note {
  color: var(--text-secondary);
}

.icp-link a {
  color: inherit;
}

@media (max-width: 960px) {
  .login-panel {
    grid-template-columns: 1fr;
  }

  .panel-intro {
    border-right: 0;
    border-bottom: 1px solid var(--border-color-light);
  }
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    min-height: calc(100vh - 32px);
  }

  .panel-intro,
  .panel-form {
    padding: 20px;
  }

  .panel-lead {
    font-size: 16px;
  }

  .form-title {
    font-size: 24px;
  }

  .captcha-row {
    flex-direction: column;
  }

  .captcha-image {
    min-width: 0;
  }
}
</style>
