<template>
  <div class="portal-container">
    <a-layout class="portal-layout">
      <!-- 统一的 Header 组件 -->
      <AppHeader
        variant="portal"
        :show-admin-button="true"
        @admin-click="goToAdmin"
      />

      <!-- 主要内容区域 -->
      <a-layout-content class="content">
        <div class="hero-section">
          <div class="hero-content">
            <!-- 主标题区域 -->
            <div class="hero-header fade-in">
              <a-typography-title
                :level="1"
                class="main-title"
              >
                {{ appSlogan?.replace(/[、,，]/g, ' · ') }}
              </a-typography-title>
              <a-typography-paragraph class="main-subtitle">
                <span class="system-name">欢迎使用客户服务系统</span>
              </a-typography-paragraph>
              <a-divider class="title-divider" />
            </div>

            <!-- 访问卡片 -->
            <a-card
              class="access-card fade-in"
              :bordered="false"
              hoverable
              style="animation-delay: 0.1s"
            >
              <a-row
                :gutter="[24, 24]"
                align="middle"
              >
                <a-col
                  :xs="24"
                  :md="10"
                  :lg="8"
                  class="access-card-left"
                >
                  <div class="icon-wrapper">
                    <div class="icon-bg">
                      <LinkOutlined class="icon" />
                    </div>
                  </div>
                  <div class="text-content">
                    <a-typography-title
                      :level="3"
                      style="margin-bottom: 8px"
                    >
                      项目访问入口
                    </a-typography-title>
                    <a-typography-text type="secondary">
                      请输入您的项目访问链接，快速查看项目详情和相关文件
                    </a-typography-text>
                  </div>
                </a-col>
                <a-col
                  :xs="24"
                  :md="14"
                  :lg="16"
                >
                  <div class="input-wrapper">
                    <a-input-search
                      v-model:value="matterUrl"
                      placeholder="请输入完整的项目访问链接，例如：https://example.com/matter/123?token=xxx"
                      enter-button="立即访问"
                      size="large"
                      @search="handleAccess"
                      @input="checkClipboardAndUpdateButton"
                    >
                      <template #prefix>
                        <LinkOutlined style="color: rgba(0,0,0,0.25)" />
                      </template>
                    </a-input-search>
                    <div
                      v-if="showPasteButton"
                      class="paste-tip"
                    >
                      <a-button 
                        type="link" 
                        size="small" 
                        :disabled="!canPaste"
                        @mousedown.prevent
                        @click="handlePaste"
                      >
                        {{ pasteButtonText }}
                      </a-button>
                    </div>
                  </div>
                </a-col>
              </a-row>
            </a-card>

            <!-- 功能特性展示 -->
            <div
              class="features-section fade-in"
              style="animation-delay: 0.2s"
            >
              <div class="section-header">
                <a-typography-title :level="2">
                  核心功能
                </a-typography-title>
                <a-typography-text type="secondary">
                  为您提供全方位的法律服务支持
                </a-typography-text>
              </div>
              <a-row :gutter="[24, 24]">
                <a-col
                  v-for="feature in features"
                  :key="feature.title"
                  :xs="24"
                  :sm="12"
                  :md="8"
                >
                  <a-card
                    hoverable
                    :bordered="false"
                    class="feature-card"
                  >
                    <a-card-meta
                      :title="feature.title"
                      :description="feature.description"
                    >
                      <template #avatar>
                        <div class="feature-icon-box">
                          <component :is="feature.icon" />
                        </div>
                      </template>
                    </a-card-meta>
                  </a-card>
                </a-col>
              </a-row>
            </div>
          </div>
        </div>
      </a-layout-content>

      <!-- 页脚 -->
      <a-layout-footer class="footer">
        <div class="footer-content">
          <a-row :gutter="[32, 32]">
            <a-col
              :xs="24"
              :md="8"
            >
              <div class="footer-section">
                <a-typography-title
                  :level="4"
                  style="color: #fff; margin-bottom: 16px;"
                >
                  关于我们
                </a-typography-title>
                <a-typography-paragraph style="color: rgba(255, 255, 255, 0.65);">
                  <a
                    v-if="lawFirmWebsite"
                    :href="lawFirmWebsite"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="law-firm-link"
                  >
                    {{ lawFirmName }} 官网
                  </a>
                  <span v-else>{{ lawFirmName }}</span>
                </a-typography-paragraph>
              </div>
            </a-col>
            <a-col
              :xs="24"
              :md="8"
            >
              <div class="footer-section">
                <a-typography-title
                  :level="4"
                  style="color: #fff; margin-bottom: 16px;"
                >
                  联系方式
                </a-typography-title>
                <a-typography-paragraph style="color: rgba(255, 255, 255, 0.65);">
                  如有疑问，请联系您的专属律师
                </a-typography-paragraph>
              </div>
            </a-col>
            <a-col
              :xs="24"
              :md="8"
            >
              <div class="footer-section">
                <a-typography-title
                  :level="4"
                  style="color: #fff; margin-bottom: 16px;"
                >
                  安全保障
                </a-typography-title>
                <a-typography-paragraph style="color: rgba(255, 255, 255, 0.65);">
                  采用银行级加密技术，保障信息安全
                </a-typography-paragraph>
              </div>
            </a-col>
          </a-row>
          
          <a-divider style="border-color: rgba(255, 255, 255, 0.15)" />
          
          <div class="footer-bottom">
            <a-typography-paragraph style="color: rgba(255, 255, 255, 0.45); margin-bottom: 4px;">
              © 2026 {{ lawFirmName }}
            </a-typography-paragraph>
            <a-typography-paragraph
              v-if="icpLicense"
              class="icp-info"
              style="color: rgba(255, 255, 255, 0.45);"
            >
              <a
                href="https://beian.miit.gov.cn"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ icpLicense }}
              </a>
            </a-typography-paragraph>
          </div>
        </div>
      </a-layout-footer>
      <!-- 移动端底部导航 -->
      <MobileBottomNav />
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  LinkOutlined,
  FileTextOutlined,
  FolderOutlined,
  SafetyOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  LockOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import logger from '@/utils/logger'

const router = useRouter()
const route = useRoute()
const matterUrl = ref('')
const loading = ref(false)
const showPasteButton = ref(false)
const canPaste = ref(false)
const pasteButtonText = ref('粘贴')

// 从全局配置 store 获取配置
const appConfigStore = useAppConfigStore()
const appSlogan = computed(() => appConfigStore.appSlogan)
const icpLicense = computed(() => appConfigStore.icpLicense)
const lawFirmName = computed(() => appConfigStore.lawFirmName)
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)

// 功能特性数据
const features = [
  {
    icon: FileTextOutlined,
    title: '项目详情',
    description: '实时查看项目基本信息、承办律师、案件进展等详细信息'
  },
  {
    icon: FolderOutlined,
    title: '文件管理',
    description: '安全上传、下载、预览项目相关文件，支持多种文件格式'
  },
  {
    icon: TeamOutlined,
    title: '律师团队',
    description: '了解承办律师信息，随时与专业团队保持沟通'
  },
  {
    icon: LockOutlined,
    title: '安全可靠',
    description: '采用银行级加密技术，多重安全验证，保障您的信息安全'
  },
  {
    icon: ClockCircleOutlined,
    title: '实时更新',
    description: '项目信息实时同步，第一时间获取最新进展和文件'
  },
  {
    icon: SafetyOutlined,
    title: '隐私保护',
    description: '严格遵守法律法规，保护客户隐私，信息仅限授权访问'
  }
]

// 从URL中提取项目ID和token（带安全验证）
function parseMatterUrl(url: string): { matterId: string; token: string } | null {
  try {
    if (!url || typeof url !== 'string') return null
    
    const urlObj = new URL(url)
    if (!['http:', 'https:'].includes(urlObj.protocol)) return null
    
    const pathParts = urlObj.pathname.split('/')
    const matterId = pathParts[pathParts.length - 1]
    const token = urlObj.searchParams.get('token') || ''
    
    if (!matterId || !/^[\w-]+$/.test(matterId)) return null
    if (!token || !/^[\w.-]+$/.test(token)) return null
    
    return { matterId, token }
  } catch {
    return null
  }
}

// 检查剪贴板内容并更新按钮状态
async function checkClipboardAndUpdateButton() {
  if (matterUrl.value.trim()) {
    canPaste.value = false
    pasteButtonText.value = '请复制链接，再输入'
    showPasteButton.value = false
    return
  }
  
  if (!window.isSecureContext || !navigator.clipboard) {
    canPaste.value = false
    pasteButtonText.value = '请复制链接，再输入'
    return
  }
  
  try {
    const text = await navigator.clipboard.readText()
    if (text && text.trim()) {
      canPaste.value = true
      pasteButtonText.value = '粘贴链接'
      showPasteButton.value = true
    } else {
      canPaste.value = false
      showPasteButton.value = false
    }
  } catch (error) {
    logger.debug('剪贴板读取失败:', error)
    canPaste.value = false
    showPasteButton.value = false
  }
}

// 处理粘贴
async function handlePaste() {
  if (!canPaste.value) return
  
  try {
    const text = await navigator.clipboard.readText()
    if (text && text.trim()) {
      matterUrl.value = text.trim()
      message.success('已粘贴链接')
      showPasteButton.value = false
    } else {
      message.warning('剪贴板中没有内容')
    }
  } catch (error) {
    message.error('无法读取剪贴板内容，请手动粘贴')
  }
}

// 处理访问
async function handleAccess() {
  if (!matterUrl.value) {
    message.warning('请输入项目访问链接')
    return
  }

  const parsed = parseMatterUrl(matterUrl.value)
  if (!parsed) {
    message.error('链接格式不正确，请检查链接是否包含项目ID和token')
    return
  }

  loading.value = true
  try {
    await router.push({
      name: 'MatterDetail',
      params: { id: parsed.matterId },
      query: { token: parsed.token },
    })
  } catch (error) {
    message.error('访问失败，请检查链接是否正确')
  } finally {
    loading.value = false
  }
}

// 跳转到管理员登录页
function goToAdmin() {
  router.push({ name: 'AdminLogin' }).catch(() => {
    window.location.href = '/admin/login'
  })
}

onBeforeUnmount(() => {
})

onMounted(() => {
  const urlParam = route.query.url as string
  if (urlParam) {
    const parsed = parseMatterUrl(urlParam)
    if (parsed) {
      router.push({
        name: 'MatterDetail',
        params: { id: parsed.matterId },
        query: { token: parsed.token },
      })
    }
  }
})
</script>

<style scoped>
.portal-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f0f8 50%, #f0f4f8 100%);
  background-attachment: fixed;
}

.portal-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 0;
}

.hero-section {
  position: relative;
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
}

.hero-content {
  max-width: 1200px;
  width: 100%;
}

/* 头部样式 */
.hero-header {
  text-align: center;
  margin-bottom: 48px;
}

.main-title {
  margin-bottom: 16px !important;
  color: var(--primary-color) !important;
  font-weight: 700 !important;
}

.main-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 24px !important;
}

.title-divider {
  width: 80px;
  min-width: 80px;
  margin: 0 auto !important;
  background-color: var(--accent-color);
  height: 3px;
  border-radius: 2px;
}

/* 访问卡片 */
.access-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  margin-bottom: 64px;
  overflow: hidden;
}

.access-card-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

@media (min-width: 768px) {
  .access-card-left {
    align-items: flex-start;
    text-align: left;
    border-right: 1px solid #f0f0f0;
    padding-right: 32px;
  }
}

.icon-wrapper {
  margin-bottom: 16px;
}

.icon-bg {
  width: 64px;
  height: 64px;
  background: rgba(24, 144, 255, 0.1);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon {
  font-size: 32px;
  color: var(--primary-color);
}

.input-wrapper {
  position: relative;
}

.paste-tip {
  position: absolute;
  right: 120px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}

/* 功能特性 */
.features-section {
  margin-bottom: 64px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.feature-card {
  border-radius: 12px;
  transition: all 0.3s;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.feature-icon-box {
  width: 48px;
  height: 48px;
  background: rgba(24, 144, 255, 0.05);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--primary-color);
  margin-bottom: 16px;
}

/* 统计数据 */
.stats-card {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.stat-col {
  display: flex;
  justify-content: center;
}

.divider-col {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 页脚 */
.footer {
  background: #001529;
  color: rgba(255, 255, 255, 0.65);
  padding: 48px 24px 24px;
}

.law-firm-link {
  color: rgba(255, 255, 255, 0.85);
  transition: color 0.3s;
}

.law-firm-link:hover {
  color: #1890ff;
}

.footer-bottom {
  text-align: center;
  padding-top: 24px;
}

.icp-info a {
  color: rgba(255, 255, 255, 0.45);
}

.icp-info a:hover {
  color: rgba(255, 255, 255, 0.85);
}

/* 动画 */
.fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .hero-section {
    padding: 32px 16px;
  }
  
  .main-title {
    font-size: 28px !important;
  }
  
  .main-subtitle {
    font-size: 14px;
  }
  
  .access-card-left {
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
    padding-right: 0;
    padding-bottom: 24px;
    margin-bottom: 24px;
  }
  
  .paste-tip {
    right: 80px;
    top: auto;
    bottom: -32px;
    transform: none;
  }

  .input-wrapper {
    margin-bottom: 24px;
  }
  
  .footer {
    padding: 24px 16px;
  }
  
  .footer-section {
    text-align: center;
    margin-bottom: 24px;
  }

  /* 为底部导航留出空间 */
  .portal-layout {
    padding-bottom: 70px;
  }
}
</style>
