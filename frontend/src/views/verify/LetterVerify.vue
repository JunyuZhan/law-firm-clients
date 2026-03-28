<template>
  <div class="letter-verify-container">
    <a-layout class="verify-layout">
      <AppHeader variant="portal" />

      <a-layout-content class="content">
        <div class="verify-section">
          <div class="verify-content">
            <!-- 验证表单（仅在未验证时显示） -->
            <a-card
              v-if="!verifyResult"
              class="verify-card"
              :bordered="false"
            >
              <div class="card-header">
                <SafetyCertificateOutlined class="header-icon" />
                <a-typography-title
                  :level="2"
                  style="margin-bottom: 8px"
                >
                  电子函件真伪验证
                </a-typography-title>
                <a-typography-text type="secondary">
                  请输入函件编号和验证码进行真伪验证
                </a-typography-text>
              </div>

              <a-divider />

              <a-form
                :model="formData"
                :rules="rules"
                layout="vertical"
                @finish="handleVerify"
              >
                <a-form-item
                  label="函件编号"
                  name="applicationNo"
                >
                  <a-input
                    v-model:value="formData.applicationNo"
                    placeholder="请输入函件申请编号"
                    size="large"
                    allow-clear
                  >
                    <template #prefix>
                      <FileTextOutlined style="color: rgba(0,0,0,0.25)" />
                    </template>
                  </a-input>
                </a-form-item>

                <a-form-item
                  label="验证码"
                  name="code"
                >
                  <a-input
                    v-model:value="formData.code"
                    placeholder="请输入验证码"
                    size="large"
                    allow-clear
                  >
                    <template #prefix>
                      <KeyOutlined style="color: rgba(0,0,0,0.25)" />
                    </template>
                  </a-input>
                </a-form-item>

                <a-form-item>
                  <a-button
                    type="primary"
                    html-type="submit"
                    size="large"
                    block
                    :loading="loading"
                  >
                    <template #icon>
                      <SafetyCertificateOutlined />
                    </template>
                    立即验证
                  </a-button>
                </a-form-item>
              </a-form>
            </a-card>

            <!-- 验证结果 -->
            <div
              v-else
              class="result-section"
            >
              <!-- 验证状态卡片 -->
              <a-card
                class="status-card"
                :bordered="false"
                :class="statusCardClass"
              >
                <div class="status-content">
                  <div class="status-icon-wrapper">
                    <component
                      :is="statusIcon"
                      class="status-icon"
                    />
                  </div>
                  <a-typography-title
                    :level="2"
                    class="status-title"
                  >
                    {{ verifyResult.statusMessage }}
                  </a-typography-title>
                  <a-tag
                    :color="statusTagColor"
                    size="large"
                  >
                    {{ statusTagText }}
                  </a-tag>
                </div>
              </a-card>

              <!-- 函件详细信息 -->
              <a-card
                v-if="verifyResult.applicationNo"
                class="info-card"
                :bordered="false"
              >
                <template #title>
                  <FileTextOutlined style="margin-right: 8px" />
                  函件信息
                </template>

                <a-descriptions
                  :column="{ xs: 1, sm: 2, md: 2 }"
                  bordered
                  size="small"
                >
                  <a-descriptions-item
                    label="函件编号"
                    :span="2"
                  >
                    <a-typography-text copyable>
                      {{ verifyResult.applicationNo }}
                    </a-typography-text>
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.letterTypeName"
                    label="函件类型"
                  >
                    {{ verifyResult.letterTypeName }}
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.firmName"
                    label="出函律所"
                  >
                    {{ verifyResult.firmName }}
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.lawyerNames"
                    label="出函律师"
                    :span="2"
                  >
                    {{ verifyResult.lawyerNames }}
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.targetUnit"
                    label="接收单位"
                    :span="2"
                  >
                    {{ verifyResult.targetUnit }}
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.matterName"
                    label="关联项目"
                    :span="2"
                  >
                    {{ verifyResult.matterName }}
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.approvedAt"
                    label="审批时间"
                  >
                    {{ formatDateTime(verifyResult.approvedAt) }}
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.printedAt"
                    label="盖章时间"
                  >
                    {{ formatDateTime(verifyResult.printedAt) }}
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.validUntil"
                    label="有效期至"
                  >
                    <a-typography-text :type="isExpired ? 'danger' : undefined">
                      {{ formatDateTime(verifyResult.validUntil) }}
                      <span v-if="isExpired">（已过期）</span>
                    </a-typography-text>
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.verifyCount !== undefined"
                    label="验证次数"
                  >
                    {{ verifyResult.verifyCount }} 次
                  </a-descriptions-item>

                  <a-descriptions-item
                    v-if="verifyResult.remark"
                    label="备注"
                    :span="2"
                  >
                    {{ verifyResult.remark }}
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <a-button
                  size="large"
                  @click="handleReset"
                >
                  <template #icon>
                    <ReloadOutlined />
                  </template>
                  重新验证
                </a-button>
                <a-button
                  type="primary"
                  size="large"
                  @click="goToPortal"
                >
                  <template #icon>
                    <HomeOutlined />
                  </template>
                  返回首页
                </a-button>
              </div>
            </div>
          </div>
        </div>
      </a-layout-content>

      <!-- 页脚 -->
      <a-layout-footer class="footer">
        <div class="footer-content">
          <a-typography-paragraph style="color: rgba(255, 255, 255, 0.65); margin-bottom: 0;">
            © 2026 {{ lawFirmName }} · 电子函件验证服务
          </a-typography-paragraph>
        </div>
      </a-layout-footer>
      <!-- 移动端底部导航 -->
      <MobileBottomNav />
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  SafetyCertificateOutlined,
  FileTextOutlined,
  KeyOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  StopOutlined,
  QuestionCircleOutlined,
  ReloadOutlined,
  HomeOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { verifyLetter } from '@/api/letter-verification'
import type { LetterVerificationResult } from '@/api/letter-verification'

const route = useRoute()
const router = useRouter()
const appConfigStore = useAppConfigStore()

const lawFirmName = computed(() => appConfigStore.lawFirmName || '律师事务所')

const loading = ref(false)
const verifyResult = ref<LetterVerificationResult | null>(null)

const formData = ref({
  applicationNo: '',
  code: '',
})

const rules = {
  applicationNo: [
    { required: true, message: '请输入函件编号', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
}

const statusIcon = computed(() => {
  if (!verifyResult.value) return QuestionCircleOutlined
  switch (verifyResult.value.verifyStatus) {
    case 'VALID':
      return CheckCircleOutlined
    case 'EXPIRED':
      return ExclamationCircleOutlined
    case 'REVOKED':
      return StopOutlined
    case 'INVALID':
    case 'NOT_FOUND':
    default:
      return CloseCircleOutlined
  }
})

const statusCardClass = computed(() => {
  if (!verifyResult.value) return ''
  switch (verifyResult.value.verifyStatus) {
    case 'VALID':
      return 'status-valid'
    case 'EXPIRED':
      return 'status-expired'
    case 'REVOKED':
      return 'status-revoked'
    default:
      return 'status-invalid'
  }
})

const statusTagColor = computed(() => {
  if (!verifyResult.value) return 'default'
  switch (verifyResult.value.verifyStatus) {
    case 'VALID':
      return 'success'
    case 'EXPIRED':
      return 'warning'
    case 'REVOKED':
      return 'error'
    default:
      return 'error'
  }
})

const statusTagText = computed(() => {
  if (!verifyResult.value) return '未知'
  switch (verifyResult.value.verifyStatus) {
    case 'VALID':
      return '验证通过'
    case 'EXPIRED':
      return '已过期'
    case 'REVOKED':
      return '已撤销'
    case 'INVALID':
      return '验证失败'
    case 'NOT_FOUND':
      return '未找到'
    default:
      return '未知'
  }
})

const isExpired = computed(() => {
  if (!verifyResult.value?.validUntil) return false
  return new Date(verifyResult.value.validUntil) < new Date()
})

function formatDateTime(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function handleVerify() {
  if (!formData.value.applicationNo || !formData.value.code) {
    message.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const response = await verifyLetter(formData.value.applicationNo, formData.value.code)
    verifyResult.value = response.data
  } catch (error) {
    message.error('验证请求失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  verifyResult.value = null
  formData.value = { applicationNo: '', code: '' }
}

function goToPortal() {
  router.push('/portal')
}

onMounted(() => {
  // 支持两种 URL 参数格式：
  // 1. /verify/letter?no=xxx&code=yyy (二维码扫描，管理系统生成)
  // 2. /verify/letter?applicationNo=xxx&code=yyy (兼容旧版本)
  const no = route.query.no as string
  const applicationNo = route.query.applicationNo as string
  const code = route.query.code as string
  
  const letterNo = no || applicationNo
  
  if (letterNo && code) {
    formData.value.applicationNo = letterNo
    formData.value.code = code
    handleVerify()
  }
})
</script>

<style scoped>
.letter-verify-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f0f8 50%, #f0f4f8 100%);
  background-attachment: fixed;
}

.verify-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.verify-section {
  max-width: 600px;
  width: 100%;
  margin-top: 24px;
}

.verify-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 16px;
}

.card-header {
  text-align: center;
  margin-bottom: 16px;
}

.header-icon {
  font-size: 48px;
  color: var(--primary-color);
  margin-bottom: 16px;
}

/* 验证结果样式 */
.result-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  text-align: center;
  padding: 32px 16px;
}

.status-card.status-valid {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
}

.status-card.status-expired {
  background: linear-gradient(135deg, #fffbe6 0%, #ffe58f 100%);
}

.status-card.status-revoked,
.status-card.status-invalid {
  background: linear-gradient(135deg, #fff2f0 0%, #ffccc7 100%);
}

.status-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.status-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
}

.status-valid .status-icon {
  font-size: 48px;
  color: #52c41a;
}

.status-expired .status-icon {
  font-size: 48px;
  color: #faad14;
}

.status-revoked .status-icon,
.status-invalid .status-icon {
  font-size: 48px;
  color: #ff4d4f;
}

.status-title {
  margin: 0 !important;
}

.info-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 16px;
}

/* 页脚 */
.footer {
  background: #001529;
  padding: 24px;
  text-align: center;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .content {
    padding: 16px;
  }

  .verify-section {
    margin-top: 16px;
  }

  .verify-card {
    padding: 8px;
  }

  .header-icon {
    font-size: 40px;
  }

  .status-icon-wrapper {
    width: 64px;
    height: 64px;
  }

  .status-valid .status-icon,
  .status-expired .status-icon,
  .status-revoked .status-icon,
  .status-invalid .status-icon {
    font-size: 36px;
  }

  .action-buttons {
    flex-direction: column;
  }

  /* 为底部导航留出空间 */
  .verify-layout {
    padding-bottom: 70px;
  }
}
</style>
