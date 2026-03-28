<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-showcase glass-panel">
        <div class="showcase-top">
          <div class="eyebrow">
            Admin Authentication
          </div>
          <div class="brand-lockup">
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
            <div>
              <h1 class="editorial-title brand-title">
                {{ appConfigStore.lawFirmName }}
              </h1>
              <p class="brand-subtitle">
                {{ appConfigStore.appSlogan || '客户服务管理系统' }}
              </p>
            </div>
          </div>
        </div>

        <div class="showcase-copy">
          <h2 class="editorial-title showcase-title">
            统一的客户门户，需要同样克制而可靠的后台入口
          </h2>
          <p class="showcase-text">
            管理端负责项目、通知、文件与配置协同，因此登录页不再独立成另一种审美，而是回到同一套产品语言里。
          </p>
        </div>

        <div class="showcase-metrics">
          <div class="metric-chip">
            <span>Session</span>
            <strong>Protected</strong>
          </div>
          <div class="metric-chip">
            <span>Captcha</span>
            <strong>Enabled</strong>
          </div>
          <div class="metric-chip">
            <span>Workspace</span>
            <strong>Unified</strong>
          </div>
        </div>

        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-index">01</span>
            <div>
              <h3>安全认证</h3>
              <p>用户名密码、验证码与会话控制共同收敛到统一登录流程。</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-index">02</span>
            <div>
              <h3>运营效率</h3>
              <p>进入后台后可直接处理项目、通知与系统配置，不必跨系统切换。</p>
            </div>
          </div>
          <div class="feature-item">
            <span class="feature-index">03</span>
            <div>
              <h3>品牌一致</h3>
              <p>视觉规则与门户、后台 shell 对齐，减少“像三个产品”的割裂感。</p>
            </div>
          </div>
        </div>
      </section>

      <section class="login-panel glass-panel">
        <div class="form-header">
          <div class="eyebrow">
            Sign In
          </div>
          <h2 class="editorial-title">
            欢迎回来
          </h2>
          <p>请使用您的管理员账户继续操作。</p>
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
            <div class="captcha-row">
              <a-input
                v-model:value="form.captchaText"
                size="large"
                placeholder="请输入验证码"
                class="login-input captcha-input"
                @press-enter="handleLogin"
              />
              <div
                class="captcha-image"
                @click="refreshCaptcha"
              >
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  alt="验证码"
                >
                <span v-else>加载中...</span>
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
            © {{ currentYear }} {{ appConfigStore.lawFirmName }}
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAppConfigStore } from '@/stores/appConfig'
import { getCaptcha } from '@/api/auth'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import '@/styles/theme.css'

const router = useRouter()
const authStore = useAuthStore()
const appConfigStore = useAppConfigStore()

const currentYear = new Date().getFullYear()

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
  background: linear-gradient(180deg, #f6f8fb 0%, #eef2f6 100%);
}

.login-shell {
  min-height: calc(100vh - 48px);
  width: min(1360px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(380px, 0.85fr);
  gap: 24px;
}

.login-showcase,
.login-panel {
  border-radius: 18px;
  padding: 32px;
}

.login-showcase {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: #10273d;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.showcase-top {
  display: grid;
  gap: 26px;
}

.brand-lockup {
  display: flex;
  align-items: center;
  gap: 18px;
}

.logo {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  overflow: hidden;
  flex-shrink: 0;
}

.logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo svg {
  width: 38px;
  height: 38px;
  color: var(--accent-color);
}

.brand-title {
  margin: 0 0 6px;
  font-size: clamp(30px, 4vw, 52px);
  color: #fff;
}

.brand-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.68);
  line-height: 1.7;
}

.showcase-copy {
  max-width: 640px;
}

.showcase-title {
  margin: 0 0 18px;
  font-size: clamp(34px, 5vw, 60px);
  color: #fff;
  line-height: 0.98;
}

.showcase-text {
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.85;
  font-size: 16px;
}

.showcase-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 28px;
}

.metric-chip {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.metric-chip span {
  display: block;
  margin-bottom: 8px;
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.46);
}

.metric-chip strong {
  display: block;
  font-size: 20px;
  line-height: 1.1;
  color: #fff;
}

.feature-list {
  display: grid;
  gap: 14px;
}

.feature-item {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
  padding: 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.feature-index {
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 0.12em;
  color: rgba(255, 255, 255, 0.46);
}

.feature-item h3 {
  margin: 0 0 6px;
  color: #fff;
  font-size: 18px;
}

.feature-item p {
  margin: 0;
  color: rgba(255, 255, 255, 0.66);
  line-height: 1.7;
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
  border: 1px solid rgba(21, 33, 46, 0.08);
  box-shadow: var(--shadow-sm);
}

.form-header {
  margin-bottom: 28px;
}

.form-header h2 {
  margin: 18px 0 10px;
  font-size: clamp(30px, 4vw, 44px);
  color: var(--primary-color-dark);
  line-height: 1.02;
}

.form-header p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 18px;
}

.login-input :deep(.ant-input),
.login-input :deep(.ant-input-affix-wrapper) {
  min-height: 54px;
}

.input-icon {
  color: var(--text-tertiary);
}

.captcha-row {
  display: flex;
  gap: 12px;
}

.login-form :deep(.ant-btn-primary) {
  min-height: 52px;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 132px;
  height: 54px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  transition: border-color 0.2s ease, transform 0.2s ease;
  overflow: hidden;
}

.captcha-image:hover {
  border-color: rgba(37, 77, 119, 0.35);
  transform: translateY(-1px);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-image span {
  font-size: 12px;
  color: var(--text-tertiary);
}

.login-button {
  height: 54px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px !important;
}

.error-alert {
  margin-top: 12px;
}

.form-footer {
  margin-top: 28px;
  text-align: center;
  font-size: 13px;
  color: var(--text-tertiary);
}

.form-footer p {
  margin: 4px 0;
}

.icp-link a {
  color: inherit;
  text-decoration: none;
}

.icp-link a:hover {
  color: var(--primary-color);
}

@media (max-width: 1024px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .login-showcase {
    min-height: auto;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 12px;
  }

  .login-shell {
    min-height: calc(100vh - 24px);
  }

  .login-showcase,
  .login-panel {
    padding: 22px;
    border-radius: 14px;
  }

  .brand-lockup {
    align-items: flex-start;
  }

  .captcha-row {
    flex-direction: column;
  }

  .showcase-metrics {
    grid-template-columns: 1fr;
  }

  .captcha-image {
    width: 100%;
  }
}
</style>
