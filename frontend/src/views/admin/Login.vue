<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-frame">
        <aside class="frame-aside">
          <div class="aside-top">
            <p class="aside-kicker">Admin Console</p>

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
                <p class="brand-caption">
                  {{ organizationName || 'Law Firm Clients' }}
                </p>
                <h1 class="brand-title">
                  {{ systemName }}
                </h1>
              </div>
            </div>

            <p class="aside-lead">
              统一管理客户项目、通知分发、文件资料与门户品牌配置。
            </p>
          </div>

          <div class="signal-grid">
            <article class="signal-card">
              <span>Project Control</span>
              <p>集中维护项目状态、访问有效期与客户可见范围。</p>
            </article>
            <article class="signal-card">
              <span>Operational Flow</span>
              <p>统一管理通知触达、文件交付与协作记录。</p>
            </article>
            <article class="signal-card">
              <span>Brand Source</span>
              <p>系统名称、Logo 与门户文案优先读取后台配置。</p>
            </article>
          </div>

          <div class="aside-footer">
            <div class="status-chip">
              <span class="status-dot" />
              <span>Secure administrative workspace</span>
            </div>
          </div>
        </aside>

        <section class="frame-form">
          <div class="form-panel">
            <div class="form-header">
              <p class="form-kicker">Management Login</p>
              <h2 class="form-title">进入管理后台</h2>
              <p class="form-hint">
                使用管理员账号登录系统工作台。
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
    radial-gradient(circle at 12% 16%, rgba(30, 64, 175, 0.18), transparent 24%),
    radial-gradient(circle at 88% 18%, rgba(245, 158, 11, 0.09), transparent 22%),
    linear-gradient(180deg, #020617 0%, #0f172a 56%, #111827 100%);
}

.login-shell {
  width: min(1200px, 100%);
  min-height: calc(100vh - 48px);
  margin: 0 auto;
  display: grid;
  place-items: center;
}

.login-frame {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(380px, 460px);
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(15, 23, 42, 0.8);
  box-shadow: 0 32px 90px rgba(2, 6, 23, 0.45);
  backdrop-filter: blur(18px);
}

.frame-aside {
  display: grid;
  align-content: space-between;
  gap: 30px;
  padding: 38px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.18), transparent 28%),
    linear-gradient(180deg, rgba(15, 23, 42, 0.95) 0%, rgba(15, 23, 42, 0.88) 100%);
  color: #f8fafc;
}

.aside-top {
  display: grid;
  gap: 22px;
}

.aside-kicker,
.form-kicker,
.signal-card span {
  margin: 0;
  color: rgba(245, 158, 11, 0.88);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
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
  gap: 8px;
}

.brand-caption {
  margin: 0;
  color: rgba(226, 232, 240, 0.68);
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-title {
  margin: 0;
  color: #f8fafc;
  font-family: var(--font-heading);
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.02;
}

.aside-lead {
  max-width: 34rem;
  margin: 0;
  color: rgba(226, 232, 240, 0.82);
  font-size: 18px;
  line-height: 1.9;
}

.signal-grid {
  display: grid;
  gap: 14px;
}

.signal-card {
  display: grid;
  gap: 8px;
  padding: 18px 18px 16px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
}

.signal-card p {
  margin: 0;
  color: rgba(226, 232, 240, 0.72);
  font-size: 14px;
  line-height: 1.8;
}

.aside-footer {
  display: flex;
  justify-content: flex-start;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(226, 232, 240, 0.78);
  font-size: 13px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #22c55e;
  box-shadow: 0 0 0 6px rgba(34, 197, 94, 0.14);
}

.frame-form {
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    linear-gradient(180deg, rgba(248, 250, 252, 0.98) 0%, rgba(241, 245, 249, 0.98) 100%);
}

.form-panel {
  width: min(100%, 380px);
  display: grid;
  gap: 18px;
}

.form-header {
  display: grid;
  gap: 10px;
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
  font-size: 15px;
  line-height: 1.75;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.login-input :deep(.ant-input),
.login-input :deep(.ant-input-affix-wrapper) {
  min-height: 50px;
  border-radius: 10px;
  border-color: rgba(148, 163, 184, 0.24);
  background: rgba(255, 255, 255, 0.96);
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
  min-width: 128px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 10px;
  background: #fff;
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
  margin-top: 6px;
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.75;
}

.config-note {
  color: var(--text-secondary);
}

.icp-link a {
  color: inherit;
}

@media (max-width: 1024px) {
  .login-frame {
    grid-template-columns: 1fr;
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
    min-height: calc(100vh - 32px);
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
