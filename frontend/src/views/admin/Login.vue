<template>
  <div class="login-page">
    <!-- 左侧装饰区域 -->
    <div class="login-left">
      <div class="left-content">
        <!-- 装饰几何图形 -->
        <div class="decorations">
          <div class="circle circle-1" />
          <div class="circle circle-2" />
          <div class="circle circle-3" />
          <div class="line line-1" />
          <div class="line line-2" />
          <div class="dot dot-1" />
          <div class="dot dot-2" />
          <div class="dot dot-3" />
        </div>
        
        <!-- Logo 和标题 -->
        <div class="brand">
          <div class="logo-wrapper">
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
          </div>
          <h1 class="brand-title">
            {{ appConfigStore.lawFirmName }}
          </h1>
          <p class="brand-subtitle">
            {{ appConfigStore.appSlogan || '客户服务管理系统' }}
          </p>
        </div>
        
        <!-- 特性说明 -->
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M9 12l2 2 4-4" />
                <circle
                  cx="12"
                  cy="12"
                  r="10"
                />
              </svg>
            </div>
            <div class="feature-text">
              <h3>安全可靠</h3>
              <p>企业级数据加密保护</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle
                  cx="9"
                  cy="7"
                  r="4"
                />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </div>
            <div class="feature-text">
              <h3>高效协作</h3>
              <p>团队案件协同管理</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <rect
                  x="3"
                  y="4"
                  width="18"
                  height="18"
                  rx="2"
                  ry="2"
                />
                <line
                  x1="16"
                  y1="2"
                  x2="16"
                  y2="6"
                />
                <line
                  x1="8"
                  y1="2"
                  x2="8"
                  y2="6"
                />
                <line
                  x1="3"
                  y1="10"
                  x2="21"
                  y2="10"
                />
              </svg>
            </div>
            <div class="feature-text">
              <h3>智能日程</h3>
              <p>案件进度实时追踪</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户以继续</p>
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
              登录
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
      </div>
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
      form.value.captchaText
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
  display: flex;
}

/* 左侧装饰区域 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #0f2438 0%, #1a3a5c 50%, #2c5282 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.left-content {
  position: relative;
  z-index: 2;
  padding: 40px;
}

/* 装饰元素 */
.decorations {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -50px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 10%;
  left: -50px;
  animation: float 6s ease-in-out infinite reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 40%;
  right: 20%;
  border-color: rgba(255, 255, 255, 0.1);
  animation: float 10s ease-in-out infinite;
}

.line {
  position: absolute;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(212, 175, 55, 0.3), transparent);
}

.line-1 {
  width: 200px;
  top: 30%;
  left: 10%;
  transform: rotate(-15deg);
}

.line-2 {
  width: 150px;
  bottom: 25%;
  right: 15%;
  transform: rotate(20deg);
}

.dot {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(212, 175, 55, 0.5);
}

.dot-1 {
  top: 20%;
  left: 30%;
  animation: pulse 2s ease-in-out infinite;
}

.dot-2 {
  top: 60%;
  right: 25%;
  animation: pulse 2s ease-in-out infinite 0.5s;
}

.dot-3 {
  bottom: 30%;
  left: 20%;
  animation: pulse 2s ease-in-out infinite 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.5); }
}

/* 品牌区域 */
.brand {
  text-align: center;
  margin-bottom: 60px;
}

.logo-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.logo {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  overflow: hidden;
}

.logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo svg {
  width: 40px;
  height: 40px;
  color: #d4af37;
}

.brand-title {
  font-family: var(--font-heading);
  font-size: 32px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  letter-spacing: 4px;
}

/* 特性列表 */
.features {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(212, 175, 55, 0.2);
  transform: translateX(8px);
}

.feature-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(212, 175, 55, 0.1));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-icon svg {
  width: 22px;
  height: 22px;
  color: #d4af37;
}

.feature-text h3 {
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 4px 0;
}

.feature-text p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 右侧登录表单 */
.login-right {
  width: 480px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-family: var(--font-heading);
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.form-header p {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}

/* 表单样式 */
.login-form :deep(.ant-form-item) {
  margin-bottom: 20px;
}

.login-input :deep(.ant-input) {
  background: var(--bg-secondary);
  border-color: var(--border-color);
  transition: all 0.3s ease;
}

.login-input :deep(.ant-input:hover) {
  border-color: var(--primary-color-lighter);
}

.login-input :deep(.ant-input:focus),
.login-input :deep(.ant-input-affix-wrapper-focused) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(26, 58, 92, 0.1);
}

.input-icon {
  color: var(--text-tertiary);
}

/* 验证码行 */
.captcha-row {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  transition: all 0.3s ease;
  overflow: hidden;
}

.captcha-image:hover {
  border-color: var(--primary-color-lighter);
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

/* 登录按钮 */
.login-button {
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-color-light) 100%);
  border: none;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.login-button:hover {
  background: linear-gradient(135deg, var(--primary-color-light) 0%, var(--primary-color-lighter) 100%);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

/* 错误提示 */
.error-alert {
  margin-top: 16px;
}

/* 底部 */
.form-footer {
  margin-top: 40px;
  text-align: center;
  font-size: 13px;
  color: var(--text-tertiary);
}

.form-footer p {
  margin: 4px 0;
}

.icp-link a {
  color: var(--text-tertiary);
  text-decoration: none;
  transition: color 0.2s ease;
}

.icp-link a:hover {
  color: var(--primary-color);
}

/* 响应式 */
@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 24px;
  }
  
  .form-header h2 {
    font-size: 24px;
  }
}
</style>
