<template>
  <div class="letter-verify-container">
    <a-layout class="verify-layout">
      <AppHeader variant="portal" />

      <a-layout-content
        id="main-content"
        class="content"
        tabindex="-1"
      >
        <div class="verify-section section-shell">
          <div class="verify-content">
            <section
              v-if="!verifyResult"
              class="verify-card"
            >
              <h1 class="verify-page-title text-balance">
                电子函件真伪验证
              </h1>
              <p class="verify-page-lead text-balance">
                输入函件编号与验证码，查询核验结果。
              </p>

              <a-form
                :model="formData"
                :rules="rules"
                layout="vertical"
                class="verify-form"
                @finish="handleVerify"
              >
                <a-form-item
                  label="函件编号"
                  name="applicationNo"
                >
                  <a-input
                    v-model:value="formData.applicationNo"
                    name="applicationNo"
                    autocomplete="off"
                    :spellcheck="false"
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
                    name="code"
                    autocomplete="off"
                    :spellcheck="false"
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
            </section>

            <!-- 验证结果 -->
            <div
              v-else
              class="result-section"
            >
              <!-- 验证状态卡片 -->
              <section
                class="status-card"
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
              </section>

              <!-- 函件详细信息 -->
              <section
                v-if="verifyResult.applicationNo"
                class="info-card"
              >
                <div class="info-head">
                  <h3>函件信息</h3>
                </div>

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
              </section>

              <section class="summary-grid">
                <article class="summary-card">
                  <span class="summary-label">状态</span>
                  <strong>{{ statusTagText }}</strong>
                  <p>{{ verifyResult.statusMessage }}</p>
                </article>
                <article class="summary-card">
                  <span class="summary-label">验证次数</span>
                  <strong>{{ verifyResult.verifyCount ?? 0 }} 次</strong>
                </article>
                <article class="summary-card">
                  <span class="summary-label">有效期至</span>
                  <strong>{{ verifyResult.validUntil ? formatDateTime(verifyResult.validUntil) : '—' }}</strong>
                  <p v-if="isExpired">
                    已过期，请联系出函方。
                  </p>
                </article>
              </section>

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
        <div class="footer-content section-shell">
          <p class="footer-text">
            © {{ new Date().getFullYear() }} {{ lawFirmName }}
          </p>
        </div>
      </a-layout-footer>
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
  background: transparent;
}

.verify-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: 28px 0 40px;
}

.verify-section {
  margin-top: 0;
}

.verify-card {
  max-width: 560px;
  margin: 0 auto;
  padding: 24px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
}

.verify-page-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: var(--lex-primary);
  font-family: var(--font-heading);
  text-align: center;
}

.verify-page-lead {
  margin: 0 0 24px;
  text-align: center;
  color: var(--text-secondary);
  line-height: 1.55;
  font-size: 14px;
}

.verify-form {
  max-width: 520px;
  margin: 0 auto;
}

/* 验证结果样式 */
.result-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  display: grid;
  gap: 8px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--lex-bg-muted);
  border: 1px solid var(--border-color-light);
}

.summary-card strong {
  color: var(--lex-primary);
}

.summary-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-tertiary);
}

.summary-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.status-card {
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  text-align: center;
  padding: 24px 20px;
  border: 1px solid var(--border-color);
}

.status-card.status-valid {
  background: color-mix(in srgb, var(--success-color) 10%, transparent);
}

.status-card.status-expired {
  background: color-mix(in srgb, var(--warning-color) 12%, transparent);
}

.status-card.status-revoked,
.status-card.status-invalid {
  background: color-mix(in srgb, var(--error-color) 10%, transparent);
}

.status-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.status-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--lex-surface-strong);
  border: 1px solid var(--lex-outline);
}

.status-valid .status-icon {
  font-size: 36px;
  color: var(--success-color);
}

.status-expired .status-icon {
  font-size: 36px;
  color: var(--warning-color);
}

.status-revoked .status-icon,
.status-invalid .status-icon {
  font-size: 36px;
  color: var(--error-color);
}

.status-title {
  margin: 0 !important;
}

.info-card {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
}

.info-head {
  margin-bottom: 16px;
}

.info-head h3 {
  margin: 0;
  font-size: 17px;
  color: var(--lex-primary);
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 16px;
}

.footer {
  background: rgba(255, 255, 255, 0.75);
  padding: 20px 0 28px;
  text-align: center;
  border-top: 1px solid var(--border-color-light);
}

.footer-text {
  margin: 0;
  font-size: 13px;
  color: var(--text-tertiary);
}

@media (max-width: 992px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .content {
    padding: 20px 0 32px;
  }

  .verify-card {
    padding: 16px;
  }

  .verify-page-title {
    font-size: 20px;
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

}
</style>
