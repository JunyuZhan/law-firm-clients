<template>
  <div class="login-page">
    <!-- Left: brand panel (desktop) -->
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
              width="44"
              height="44"
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
      <div
        class="hero-seal"
        aria-hidden="true"
      >
        <span>LEX&nbsp;ELITE</span>
      </div>
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
          <span class="form-kicker eyebrow-text">Client Service Console</span>
          <h1 class="page-title">
            <template v-if="isInitMode">
              系统初始化
            </template>
            <template v-else>
              {{ organizationName || ADMIN_LOGIN_TEXTS.headings.fallbackLawFirm }}
            </template>
          </h1>
          <div
            class="title-rule"
            aria-hidden="true"
          />
          <p class="page-subtitle">
            <template v-if="isInitMode">
              欢迎使用客户服务系统，请设置超级管理员 (admin) 的初始密码
            </template>
            <template v-else>
              {{ ADMIN_LOGIN_TEXTS.headings.subtitle }}
            </template>
          </p>
        </header>

        <a-form
          :model="form"
          :rules="rules"
          layout="vertical"
          class="login-form"
          @finish="handleLogin"
        >
          <a-form-item
            v-if="!isInitMode"
            name="username"
          >
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
                :autocomplete="isInitMode ? 'new-password' : 'current-password'"
                :aria-label="isInitMode ? '请设置初始密码' : ADMIN_LOGIN_TEXTS.placeholders.password"
                :placeholder="isInitMode ? '请设置初始密码' : ADMIN_LOGIN_TEXTS.placeholders.password"
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
            <div
              v-if="isInitMode"
              class="password-requirements"
            >
              <ul>
                <li>至少 8 个字符</li>
                <li>包含大小写字母、数字和特殊字符</li>
              </ul>
            </div>
          </a-form-item>

          <a-form-item
            v-if="isInitMode"
            name="confirmPassword"
          >
            <div class="login-input-shell">
              <a-input
                v-model:value="form.confirmPassword"
                size="large"
                name="confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                aria-label="请再次确认密码"
                placeholder="请再次确认密码"
                @press-enter="handleLogin"
              >
                <template #prefix>
                  <LockOutlined
                    class="input-icon"
                    aria-hidden="true"
                  />
                </template>
              </a-input>
            </div>
          </a-form-item>

          <a-form-item
            v-if="!isInitMode"
            name="captchaText"
          >
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
              {{ isInitMode ? '完成初始化' : UI_TEXTS.loginButton }}
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
import { checkSystemInit, setupSystemInit, getCaptcha } from '@/api/auth'
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
const isInitMode = ref(false)

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
  confirmPassword: '',
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

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (!isInitMode.value) return Promise.resolve()
  if (value === '') {
    return Promise.reject('请再次输入密码')
  } else if (value !== form.value.password) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
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
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
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

  if (isInitMode.value) {
    loading.value = true
    try {
      await setupSystemInit(form.value.password)
      message.success('系统初始化成功，请使用新密码登录')
      isInitMode.value = false
      form.value.password = ''
      form.value.confirmPassword = ''
      await loadCaptcha()
    } catch (error: unknown) {
      errorMessage.value = getErrorMessage(error, '初始化失败')
    } finally {
      loading.value = false
    }
    return
  }

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

onMounted(async () => {
  if (!appConfigStore.loaded && !appConfigStore.loading) {
    appConfigStore.loadConfig()
  }

  try {
    const needsInit = await checkSystemInit()
    if (needsInit) {
      isInitMode.value = true
    } else {
      await loadCaptcha()
    }
  } catch (error) {
    console.error('Failed to check init status:', error)
    await loadCaptcha()
  }
})
</script>

<style scoped>
/* ===== Lex Elite 登录页 =====
   色值全部来自 src/styles/lex-elite.css（--le-*）。
   深藏青主panel + 金色点缀 + 衬线标题；金色细线分隔。
   布局保留：lg 双栏、左 p-12、右 p-8、表单 max-w-[420px]、h-12 输入壳。 */
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr;
  font-family: var(--le-font-sans);
  background: var(--le-paper);
}

@media (min-width: 1024px) {
  .login-page {
    grid-template-columns: 1fr 1fr;
  }
}

/* --- Hero：深藏青底 + 金色顶线，与 Lex Elite 样板一致 --- */
.login-hero {
  display: none;
  position: relative;
  flex-direction: column;
  justify-content: space-between;
  padding: var(--le-space-2xl); /* 48px */
  color: #fff;
  border-top: 3px solid var(--le-gold);
  background: var(--le-hero-gradient);
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
  gap: var(--le-space-md);
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
  font-size: 13px;
  line-height: 1.45;
  color: rgba(224, 196, 133, 0.92); /* gold-soft 上深底 */
  letter-spacing: 0.08em;
}

.hero-eyebrow--mobile {
  color: var(--le-muted);
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
  gap: var(--le-space-lg);
  padding-top: var(--le-space-md);
  border-top: 1px solid rgba(224, 196, 133, 0.28);
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.hero-link {
  color: rgba(255, 255, 255, 0.62);
  text-decoration: none;
  transition: color 0.2s ease;
}

.hero-link:hover {
  color: var(--le-gold-soft);
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
  opacity: 0.06;
  background-image:
    linear-gradient(rgba(255, 255, 255, 1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 1) 1px, transparent 1px);
  background-size: 24px 24px;
  z-index: 0;
}

.hero-glow {
  pointer-events: none;
  position: absolute;
  border-radius: 999px;
  filter: blur(64px);
  z-index: 0;
}

.hero-glow-a {
  top: 12%;
  right: 12%;
  width: 320px;
  height: 320px;
  background: rgba(201, 169, 97, 0.18);
}

.hero-glow-b {
  bottom: 12%;
  left: 12%;
  width: 380px;
  height: 380px;
  background: rgba(255, 255, 255, 0.07);
}

/* 装饰印记：呼应 Lex Elite 样板右上角金色圆环 */
.hero-seal {
  pointer-events: none;
  position: absolute;
  right: -70px;
  top: -70px;
  width: 300px;
  height: 300px;
  border: 1px solid rgba(201, 169, 97, 0.3);
  border-radius: 50%;
  z-index: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.hero-seal span {
  transform: translateY(38px);
  font-family: var(--le-font-serif);
  font-size: 11px;
  letter-spacing: 0.4em;
  color: rgba(224, 196, 133, 0.55);
}

.login-hero .hero-brand-mark .logo-image,
.login-hero .hero-brand-mark .logo-fallback {
  width: 44px;
  height: 44px;
  border-radius: var(--le-radius-xs);
  color: var(--le-gold-soft);
  border: 1px solid rgba(201, 169, 97, 0.5);
  padding: 6px;
}

.mobile-brand .hero-brand-mark .logo-image,
.mobile-brand .hero-brand-mark .logo-fallback {
  width: 36px;
  height: 36px;
  border-radius: var(--le-radius-xs);
}

.logo-fallback--accent {
  color: var(--le-navy-800);
}

.brand-name {
  font-family: var(--le-font-serif);
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.06em;
  line-height: 1.35;
}

.login-hero .brand-name {
  color: rgba(255, 255, 255, 0.96);
}

.mobile-brand .brand-name {
  color: var(--le-navy-900);
}

/* --- Main --- */
.login-main {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--le-space-xl); /* 32px */
  background: var(--le-paper);
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.mobile-brand {
  margin-bottom: var(--le-space-2xl); /* 48px */
}

@media (min-width: 1024px) {
  .mobile-brand {
    display: none;
  }
}

.form-heading-block {
  text-align: center;
  margin-bottom: var(--le-space-xl); /* 32px */
}

.form-kicker {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--le-gold-deep);
}

.page-title {
  margin: 0 0 12px 0;
  font-family: var(--le-font-serif);
  font-size: 30px;
  line-height: 1.25;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--le-navy-800);
}

/* 标题下金色细线（Lex Elite 标志性分隔） */
.title-rule {
  width: 56px;
  height: 2px;
  margin: 0 auto 16px;
  background: var(--le-gold-rule);
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--le-muted);
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

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
  border-radius: var(--le-radius-sm);
  border-color: var(--le-line);
  box-shadow: none;
  background-color: #fff;
}

.login-form .login-input-shell :deep(.ant-input-affix-wrapper .ant-input) {
  box-sizing: border-box;
}

.login-form .login-input-shell :deep(.ant-input-affix-wrapper:hover),
.login-form .login-input-shell :deep(.ant-input-affix-wrapper-focused),
.login-form .login-input-shell :deep(.ant-input-affix-wrapper:focus-within) {
  border-color: var(--le-gold);
  box-shadow: 0 0 0 2px rgba(201, 169, 97, 0.16);
}

.password-suffix-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 4px;
  border: none;
  background: transparent;
  color: var(--le-muted);
  cursor: pointer;
  border-radius: var(--le-radius-sm);
  line-height: 0;
  transition:
    color 0.15s ease,
    background 0.15s ease;
}

.password-suffix-btn:hover {
  color: var(--le-navy-800);
}

.password-suffix-btn:focus-visible {
  outline: 2px solid var(--le-gold);
  outline-offset: 2px;
}

.password-suffix-icon {
  font-size: 16px;
}

.input-icon {
  color: var(--le-navy-300);
}

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
  border: 1px solid var(--le-line);
  border-radius: var(--le-radius-sm);
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f6f1;
}

.captcha-img-container:focus-visible {
  outline: 2px solid var(--le-gold);
  outline-offset: 2px;
}

.captcha-img-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  font-size: 12px;
  color: var(--le-muted);
}

/* 主按钮：深藏青底 + 金色描边（Lex Elite 庄重感） */
.submit-btn {
  height: 48px;
  border-radius: var(--le-radius-sm);
  font-weight: 600;
  font-size: 16px;
  letter-spacing: 0.06em;
  background-color: var(--le-navy-800);
  border: 1px solid var(--le-navy-800);
  margin-top: 4px;
}

.submit-btn:hover {
  background-color: var(--le-navy-700) !important;
  border-color: var(--le-gold) !important;
}

.error-alert {
  margin-bottom: 16px;
  border-radius: var(--le-radius-sm);
}

.form-footer {
  margin-top: var(--le-space-xl);
  text-align: center;
  font-size: 14px;
  color: var(--le-muted);
}

.form-footer p {
  margin: 4px 0;
}

.icp-link a {
  color: var(--le-navy-800);
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s;
}

.icp-link a:hover {
  color: var(--le-gold-deep);
  text-decoration: underline;
}

.password-requirements {
  margin-top: 8px;
  padding: 12px;
  background-color: #f8f6f1;
  border: 1px solid var(--le-line-soft);
  border-radius: var(--le-radius-sm);
  font-size: 13px;
  color: #4b5563;
}

.password-requirements ul {
  margin: 0;
  padding-left: 20px;
}

.password-requirements li {
  margin-bottom: 4px;
}

.password-requirements li:last-child {
  margin-bottom: 0;
}
</style>
