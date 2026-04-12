<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-frame">
        <aside class="brand-panel">
          <div class="brand-header">
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
              <p class="brand-kicker">Admin Console</p>
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

          <div class="brand-body">
            <p class="brand-lead">
              用统一后台管理项目进展、文件资料、通知分发与门户配置。
            </p>

            <div class="brand-points">
              <article class="brand-point">
                <span>项目与访问</span>
                <p>集中维护项目状态、有效期与客户访问链接。</p>
              </article>
              <article class="brand-point">
                <span>资料与通知</span>
                <p>管理文件、通知记录与协作过程中的关键触达。</p>
              </article>
              <article class="brand-point">
                <span>品牌与门户</span>
                <p>系统名称、Logo、律所名称等展示信息均可从后台配置获取。</p>
              </article>
            </div>
          </div>
        </aside>

        <section class="login-panel">
          <div class="form-header">
            <p class="form-header-kicker">管理后台</p>
            <h2 class="form-title">登录系统</h2>
            <p class="form-header-hint">
              使用管理员账号进入后台工作台。
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
              品牌展示信息来源：后台系统配置；未配置时回退到环境变量默认值。
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
    radial-gradient(circle at top left, rgba(30, 64, 175, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(245, 158, 11, 0.09), transparent 22%),
    linear-gradient(180deg, var(--lex-bg) 0%, var(--lex-bg-muted) 100%);
}

.login-shell {
  min-height: calc(100vh - 48px);
  width: min(1100px, 100%);
  margin: 0 auto;
  display: grid;
  place-items: center;
}

.login-frame {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(320px, 1.05fr) minmax(360px, 0.92fr);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.1);
  background: var(--lex-surface-strong);
}

.brand-panel {
  display: grid;
  align-content: space-between;
  gap: 28px;
  min-height: 640px;
  padding: 36px 32px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.18), transparent 28%),
    linear-gradient(180deg, #0f172a 0%, #172554 100%);
  color: rgba(248, 250, 252, 0.92);
}

.brand-header {
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
  overflow: hidden;
  flex-shrink: 0;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo svg {
  width: 32px;
  height: 32px;
  color: rgba(255, 255, 255, 0.92);
}

.brand-copy {
  display: grid;
  gap: 4px;
}

.brand-kicker,
.form-header-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.brand-kicker {
  color: rgba(245, 158, 11, 0.88);
}

.brand-title {
  margin: 0;
  font-size: clamp(24px, 3vw, 30px);
  color: #f8fafc;
}

.brand-meta {
  margin: 0;
  color: rgba(226, 232, 240, 0.76);
  font-size: 13px;
  line-height: 1.6;
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 36px 32px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
}

.brand-body {
  display: grid;
  gap: 24px;
}

.brand-lead {
  max-width: 30ch;
  margin: 0;
  font-size: 26px;
  line-height: 1.35;
  color: #f8fafc;
}

.brand-points {
  display: grid;
  gap: 12px;
}

.brand-point {
  padding: 16px 18px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
}

.brand-point span {
  display: inline-block;
  margin-bottom: 8px;
  color: rgba(245, 158, 11, 0.9);
  font-size: 12px;
  font-weight: 700;
}

.brand-point p {
  margin: 0;
  color: rgba(226, 232, 240, 0.82);
  font-size: 13px;
  line-height: 1.7;
}

.form-header {
  margin-bottom: 22px;
}

.form-header-kicker {
  color: var(--lex-accent-strong);
}

.form-title {
  margin: 6px 0 10px;
  color: var(--lex-primary);
  font-size: 28px;
  line-height: 1.15;
}

.form-header-hint {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 15px;
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
.login-input :deep(.ant-input-affix-wrapper:focus),
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
  height: 100%;
  object-fit: cover;
}

.login-form :deep(.ant-btn-primary) {
  min-height: 48px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  letter-spacing: 0.02em;
  box-shadow: var(--shadow-sm);
}

.error-alert {
  margin-top: 8px;
  border-radius: var(--radius-md);
}

.form-footer {
  margin-top: 22px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.8;
}

.config-note {
  margin-bottom: 4px;
  color: var(--text-secondary);
}

.icp-link a {
  color: inherit;
}

@media (max-width: 1024px) {
  .login-frame {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    min-height: auto;
    padding: 28px 24px;
  }

  .login-panel {
    padding: 28px 24px;
  }
}

@media (max-width: 768px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    min-height: calc(100vh - 32px);
    width: 100%;
    place-items: center;
  }

  .logo {
    width: 40px;
    height: 40px;
  }

  .brand-panel,
  .login-panel {
    padding: 20px;
  }

  .brand-lead {
    font-size: 22px;
  }

  .form-title {
    font-size: 24px;
  }

  .captcha-row {
    flex-direction: column;
  }

  .captcha-image {
    min-height: 52px;
  }
}
</style>
