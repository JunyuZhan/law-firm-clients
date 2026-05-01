<template>
  <div class="login-page">
    <!-- Left: illustration (desktop) -->
    <aside
      class="login-hero"
      aria-hidden="false"
    >
      <div class="hero-top">
        <div class="hero-brand">
          <div class="hero-brand-mark">
            <img
              v-if="appConfigStore.logoUrl"
              :src="appConfigStore.logoUrl"
              alt=""
              width="40"
              height="40"
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
          <div class="hero-brand-text">
            <span class="brand-name brand-label">{{ appConfigStore.lawFirmName || ADMIN_LOGIN_TEXTS.headings.fallbackLawFirm }}</span>
            <p class="hero-eyebrow">
              {{ appConfigStore.appSlogan || UI_TEXTS.loginSubtitle }}
            </p>
          </div>
        </div>
      </div>

      <div class="hero-mascots">
        <AdminLoginMascots
          :is-typing="isTyping"
          :password-length="form.password.length"
          :show-password="showPassword"
        />
      </div>

      <footer class="hero-footer">
        <a
          href="#"
          class="hero-link"
          @click.prevent
        >{{ ADMIN_LOGIN_TEXTS.footer.privacy }}</a>
        <span
          class="hero-dot"
          aria-hidden="true"
        >·</span>
        <a
          href="#"
          class="hero-link"
          @click.prevent
        >{{ ADMIN_LOGIN_TEXTS.footer.terms }}</a>
      </footer>

      <div
        class="hero-grid"
        aria-hidden="true"
      />
      <div
        class="hero-glow hero-glow-a"
        aria-hidden="true"
      />
      <div
        class="hero-glow hero-glow-b"
        aria-hidden="true"
      />
    </aside>

    <!-- Right: form -->
    <main class="login-main">
      <div class="login-card">
        <div class="mobile-brand">
          <div class="hero-brand hero-brand--mobile">
            <div class="hero-brand-mark">
              <img
                v-if="appConfigStore.logoUrl"
                :src="appConfigStore.logoUrl"
                alt=""
                width="36"
                height="36"
                class="logo-image"
              >
              <svg
                v-else
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                aria-hidden="true"
                class="logo-fallback logo-fallback--accent"
              >
                <path d="M12 3L20 7.5V16.5L12 21L4 16.5V7.5L12 3Z" />
                <path d="M12 12L20 7.5" />
                <path d="M12 12V21" />
                <path d="M12 12L4 7.5" />
              </svg>
            </div>
            <div class="hero-brand-text">
              <span class="brand-name brand-label">{{ appConfigStore.lawFirmName || ADMIN_LOGIN_TEXTS.headings.fallbackLawFirm }}</span>
              <p class="hero-eyebrow hero-eyebrow--mobile">
                {{ appConfigStore.appSlogan || UI_TEXTS.loginSubtitle }}
              </p>
            </div>
          </div>
        </div>

        <header class="form-heading-block">
          <h1 class="page-title">
            {{ organizationName || ADMIN_LOGIN_TEXTS.headings.fallbackLawFirm }}
          </h1>
          <p class="page-subtitle">{{ ADMIN_LOGIN_TEXTS.headings.subtitle }}</p>
        </header>

        <a-form
          :model="form"
          :rules="rules"
          layout="vertical"
          class="login-form"
          @finish="handleLogin"
        >
          <a-form-item name="username">
            <div class="login-input-shell">
              <a-input
                v-model:value="form.username"
                size="large"
                name="username"
                autocomplete="username"
                :spellcheck="false"
                :aria-label="ADMIN_LOGIN_TEXTS.placeholders.username"
                :placeholder="ADMIN_LOGIN_TEXTS.placeholders.username"
                @focus="isTyping = true"
                @blur="isTyping = false"
              >
                <template #prefix>
                  <UserOutlined
                    class="input-icon"
                    aria-hidden="true"
                  />
                </template>
              </a-input>
            </div>
          </a-form-item>

          <a-form-item name="password">
            <div class="login-input-shell">
              <a-input
                v-model:value="form.password"
                size="large"
                name="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                :aria-label="ADMIN_LOGIN_TEXTS.placeholders.password"
                :placeholder="ADMIN_LOGIN_TEXTS.placeholders.password"
                @press-enter="handleLogin"
              >
                <template #prefix>
                  <LockOutlined
                    class="input-icon"
                    aria-hidden="true"
                  />
                </template>
                <template #suffix>
                  <button
                    type="button"
                    class="password-suffix-btn"
                    tabindex="-1"
                    :aria-label="showPassword ? ADMIN_LOGIN_TEXTS.a11y.hidePassword : ADMIN_LOGIN_TEXTS.a11y.showPassword"
                    @click.prevent.stop="showPassword = !showPassword"
                  >
                    <EyeInvisibleOutlined
                      v-if="showPassword"
                      class="password-suffix-icon"
                    />
                    <EyeOutlined
                      v-else
                      class="password-suffix-icon"
                    />
                  </button>
                </template>
              </a-input>
            </div>
          </a-form-item>

          <a-form-item name="captchaText">
            <div class="captcha-wrapper">
              <div class="login-input-shell captcha-input-grow">
                <a-input
                  v-model:value="form.captchaText"
                  size="large"
                  name="captcha"
                  autocomplete="off"
                  :spellcheck="false"
                  :aria-label="ADMIN_LOGIN_TEXTS.placeholders.captcha"
                  :placeholder="ADMIN_LOGIN_TEXTS.placeholders.captcha"
                  @press-enter="handleLogin"
                >
                  <template #prefix>
                    <SafetyCertificateOutlined
                      class="input-icon"
                      aria-hidden="true"
                    />
                  </template>
                </a-input>
              </div>
              <button
                type="button"
                class="captcha-img-container"
                :aria-label="ADMIN_LOGIN_TEXTS.a11y.refreshCaptcha"
                @click="refreshCaptcha"
              >
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  :alt="ADMIN_LOGIN_TEXTS.a11y.captchaImage"
                  width="120"
                  height="48"
                >
                <span
                  v-else
                  class="captcha-loading"
                >{{ ADMIN_LOGIN_TEXTS.placeholders.captchaLoading }}</span>
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
              class="submit-btn"
            >
              {{ UI_TEXTS.loginButton }}
            </a-button>
          </a-form-item>
        </a-form>

        <div aria-live="polite">
          <a-alert
            v-if="errorMessage"
            :message="errorMessage"
            type="error"
            show-icon
            class="error-alert"
          />
        </div>

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
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import {
  EyeInvisibleOutlined,
  EyeOutlined,
  LockOutlined,
  UserOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons-vue'
import { getCaptcha } from '@/api/auth'
import AdminLoginMascots from '@/components/admin/login/AdminLoginMascots.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { useAuthStore } from '@/stores/auth'
import { UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_LOGIN_TEXTS } from '@/constants/adminTexts'

const router = useRouter()
const authStore = useAuthStore()
const appConfigStore = useAppConfigStore()

const showPassword = ref(false)
const isTyping = ref(false)

const currentYear = new Date().getFullYear()
const organizationName = computed(() => appConfigStore.appName || appConfigStore.lawFirmName || appConfigStore.displayName)
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
/* Layout tokens aligned with frontend/css.md (Tailwind): lg:grid-cols-2, left p-12, right p-8, form max-w-[420px], space-y-5, h-12 */
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr;
  font-family:
    'Inter',
    'Helvetica Neue',
    Helvetica,
    -apple-system,
    Arial,
    sans-serif;
  background: #ffffff;
}

@media (min-width: 1024px) {
  .login-page {
    grid-template-columns: 1fr 1fr;
  }
}

/* --- Hero：底色与门户页 Portal.vue（--portal-bg）一致，网格/光斑观感统一 --- */
.login-hero {
  display: none;
  position: relative;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px; /* p-12 */
  color: #fff;
  background: #1e293b;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .login-hero {
    display: flex;
  }
}

.hero-top {
  position: relative;
  z-index: 2;
}

.hero-brand {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.hero-brand-mark {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-brand-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.hero-brand--mobile {
  margin-inline: auto;
  width: fit-content;
  max-width: 100%;
}

.hero-eyebrow {
  margin: 0;
  font-size: 14px;
  line-height: 1.45;
  color: rgba(255, 255, 255, 0.75);
  letter-spacing: 0.04em;
}

.hero-eyebrow--mobile {
  color: #6b7280;
}

.hero-mascots {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-height: 0;
}

.hero-footer {
  position: relative;
  z-index: 2;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 24px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.hero-link {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: color 0.2s ease;
}

.hero-link:hover {
  color: #fff;
}

.hero-dot {
  opacity: 0.5;
  display: none;
}

.hero-muted {
  color: rgba(255, 255, 255, 0.5);
}

.hero-grid {
  pointer-events: none;
  position: absolute;
  inset: 0;
  opacity: 0.05;
  background-image:
    linear-gradient(rgba(255, 255, 255, 1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 1) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 0;
}

.hero-glow {
  pointer-events: none;
  position: absolute;
  border-radius: 999px;
  filter: blur(64px);
  z-index: 0;
}

/* 与 Portal.vue .hero-glow-a / b 同参，左右分栏时装饰一致 */
.hero-glow-a {
  top: 15%;
  right: 15%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
}

.hero-glow-b {
  bottom: 15%;
  left: 15%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.05);
}

.login-hero .hero-brand-mark .logo-image,
.login-hero .hero-brand-mark .logo-fallback {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  color: #fff;
}

.mobile-brand .hero-brand-mark .logo-image,
.mobile-brand .hero-brand-mark .logo-fallback {
  width: 36px;
  height: 36px;
  border-radius: 8px;
}

.logo-fallback--accent {
  color: #4f46e5;
}

.brand-name {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.02em;
  line-height: 1.35;
}

.login-hero .brand-name {
  color: rgba(255, 255, 255, 0.95);
}

.mobile-brand .brand-name {
  color: #111827;
}

/* --- Main --- */
.login-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px; /* p-8，与 css.md 右侧一致 */
  background: #ffffff; /* bg-background（浅色主题常用白底） */
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.mobile-brand {
  margin-bottom: 48px; /* mb-12，与 css.md 移动端 Logo 区一致 */
}

@media (min-width: 1024px) {
  .mobile-brand {
    display: none;
  }
}

.form-heading-block {
  text-align: center;
  margin-bottom: 40px; /* mb-10 */
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 30px; /* text-3xl */
  line-height: 1.2;
  font-weight: 700;
  letter-spacing: -0.025em; /* tracking-tight */
  color: #000;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280; /* text-muted-foreground */
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 20px; /* space-y-5 */
}

.login-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

/* 三框统一外高：固定 48px 外壳，避免 min-height 与 AD 内边距导致「看起来」高低不一 */
.login-form {
  --login-input-h: 48px;
}

.login-input-shell {
  width: 100%;
  height: var(--login-input-h);
  box-sizing: border-box;
}

.login-form .login-input-shell :deep(.ant-input-affix-wrapper) {
  height: 100%;
  min-height: 100%;
  max-height: 100%;
  width: 100%;
  box-sizing: border-box;
  display: inline-flex;
  align-items: center;
  border-radius: 8px;
  border-color: rgba(229, 231, 235, 0.85);
  box-shadow: none;
  background-color: #fff;
}

.login-form .login-input-shell :deep(.ant-input-affix-wrapper .ant-input) {
  box-sizing: border-box;
}

.login-form .login-input-shell :deep(.ant-input-affix-wrapper:hover),
.login-form .login-input-shell :deep(.ant-input-affix-wrapper-focused),
.login-form .login-input-shell :deep(.ant-input-affix-wrapper:focus-within) {
  border-color: #000;
  box-shadow: none;
}

/* 密码可见性：使用 suffix 槽位，与 Ant Design 边框同一盒模型，避免绝对定位错位 */
.password-suffix-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 4px;
  border: none;
  background: transparent;
  color: rgba(107, 114, 128, 0.95);
  cursor: pointer;
  border-radius: 6px;
  line-height: 0;
  transition:
    color 0.15s ease,
    background 0.15s ease;
}

.password-suffix-btn:hover {
  color: #000; /* hover:text-foreground */
}

.password-suffix-icon {
  font-size: 16px;
}

.input-icon {
  color: #9ca3af;
}

/* 验证码：无 prefix/suffix 时为原生 input（非 affix），高度需显式与上方 h-12 一致 */
.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: stretch;
}

.captcha-input-grow {
  flex: 1;
  min-width: 0;
}

.captcha-img-container {
  flex-shrink: 0;
  width: 120px;
  height: 48px;
  border: 1px solid #e5e7eb;
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
  font-size: 12px;
  color: #6b7280;
}

.submit-btn {
  height: 48px; /* h-12 */
  border-radius: 8px;
  font-weight: 500;
  font-size: 16px; /* text-base */
  background-color: #000; /* Primary button color */
  border-color: #000;
  margin-top: 4px;
}

.submit-btn:hover {
  background-color: #333 !important;
  border-color: #333 !important;
}

.error-alert {
  margin-bottom: 16px;
  border-radius: 8px;
}

.form-footer {
  margin-top: 32px; /* 接近 css.md 底部链接 mt-8 */
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.form-footer p {
  margin: 4px 0;
}

.icp-link a {
  color: #111827; /* text-foreground */
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s;
}

.icp-link a:hover {
  text-decoration: underline;
}
</style>
