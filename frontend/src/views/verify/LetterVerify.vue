<template>
  <div class="letter-verify-container">
    <a-layout class="verify-layout">
      <AppHeader variant="portal" />

      <a-layout-content
        id="main-content"
        class="content"
        tabindex="-1"
      >
        <section class="verify-hero section-shell">
          <div class="hero-grid">
            <div class="hero-copy glass-panel">
              <div class="eyebrow">
                Verification Gateway
              </div>
              <h1 class="editorial-title hero-title">
                电子函件真伪验证
              </h1>
              <p class="hero-summary">
                这里用于核验电子函件是否真实有效。输入函件编号与验证码后，系统会返回当前状态、签发信息与有效期说明。
              </p>

              <div class="trust-grid">
                <article class="trust-card">
                  <span>01</span>
                  <strong>统一验证入口</strong>
                  <p>外部接收方无需进入后台，也不需要掌握系统结构。</p>
                </article>
                <article class="trust-card">
                  <span>02</span>
                  <strong>结果解释清楚</strong>
                  <p>有效、过期、撤销和异常状态都用同一套结构表达。</p>
                </article>
                <article class="trust-card">
                  <span>03</span>
                  <strong>信息可追溯</strong>
                  <p>可查看函件编号、律师、律所、接收单位与验证次数。</p>
                </article>
              </div>
            </div>

            <div class="hero-panel glass-panel">
              <div class="hero-panel__head">
                <div class="panel-kicker">
                  Verify Now
                </div>
                <h2>填写验证信息</h2>
                <p>支持手动输入，也支持扫描二维码后自动带入参数。</p>
              </div>

              <div class="hero-panel__rules">
                <div class="hero-rule">
                  <span>函件编号</span>
                  <strong>用于定位唯一函件记录</strong>
                </div>
                <div class="hero-rule">
                  <span>验证码</span>
                  <strong>用于确认函件校验凭证</strong>
                </div>
              </div>

              <div class="hero-panel__metrics">
                <article>
                  <strong>即时反馈</strong>
                  <span>验证后直接返回当前状态说明</span>
                </article>
                <article>
                  <strong>留痕核验</strong>
                  <span>可查看验证次数与签发信息</span>
                </article>
              </div>
            </div>
          </div>
        </section>

        <div class="verify-section section-shell">
          <div class="verify-content">
            <!-- 验证表单（仅在未验证时显示） -->
            <section
              v-if="!verifyResult"
              class="verify-card"
            >
              <div class="card-header">
                <SafetyCertificateOutlined class="header-icon" />
                <div>
                  <h2 class="card-title">开始验证</h2>
                  <p class="card-subtitle">请输入函件编号和验证码，系统会返回当前核验状态。</p>
                </div>
              </div>

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
                  <div>
                    <div class="panel-kicker">
                      Letter Detail
                    </div>
                    <h3>函件信息</h3>
                  </div>
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
                  <span class="summary-label">验证状态</span>
                  <strong>{{ statusTagText }}</strong>
                  <p>{{ verifyResult.statusMessage }}</p>
                </article>
                <article class="summary-card">
                  <span class="summary-label">验证次数</span>
                  <strong>{{ verifyResult.verifyCount ?? 0 }} 次</strong>
                  <p>用于辅助判断函件是否被多次核验。</p>
                </article>
                <article class="summary-card">
                  <span class="summary-label">有效期</span>
                  <strong>{{ verifyResult.validUntil ? formatDateTime(verifyResult.validUntil) : '未提供' }}</strong>
                  <p>{{ isExpired ? '当前函件已过期，请联系出函方确认。' : '如需继续使用，请留意有效期时间。' }}</p>
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
          <a-typography-paragraph style="color: rgba(255, 255, 255, 0.65); margin-bottom: 0;">
            © 2026 {{ lawFirmName }} · 电子函件验证服务
          </a-typography-paragraph>
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

.verify-hero {
  padding-top: 0;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 16px;
}

.hero-copy,
.hero-panel {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: #ffffff;
  box-shadow: var(--shadow-sm);
}

.hero-copy {
  display: grid;
  gap: 22px;
}

.hero-title {
  margin: 8px 0 0;
  font-size: 28px;
  max-width: none;
}

.hero-summary {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 14px;
}

.trust-grid {
  display: grid;
  gap: 14px;
}

.trust-card {
  display: grid;
  gap: 8px;
  padding: 14px;
  border-radius: 6px;
  background: #fafafa;
  border: 1px solid var(--border-color-light);
}

.trust-card span {
  color: var(--text-tertiary);
  font-size: 12px;
}

.trust-card strong {
  color: var(--text-primary);
}

.trust-card p {
  color: var(--text-secondary);
  line-height: 1.75;
}

.hero-panel {
  display: grid;
  align-content: start;
  gap: 14px;
}

.hero-panel__head h2 {
  margin: 6px 0;
  font-size: 20px;
}

.hero-panel__head p {
  color: var(--text-secondary);
  line-height: 1.8;
}

.panel-kicker {
  color: var(--text-tertiary);
  font-size: 12px;
}

.hero-panel__rules {
  display: grid;
  gap: 12px;
}

.hero-panel__metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.hero-panel__metrics article {
  display: grid;
  gap: 6px;
  padding: 14px;
  border-radius: 6px;
  background: rgba(0, 9, 24, 0.03);
}

.hero-panel__metrics strong {
  color: var(--lex-primary);
}

.hero-panel__metrics span {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 13px;
}

.hero-rule {
  padding: 12px 14px;
  border-radius: 6px;
  background: rgba(0, 9, 24, 0.03);
  border: 1px solid var(--border-color-light);
}

.hero-rule span {
  display: block;
  color: var(--text-tertiary);
  font-size: 12px;
  margin-bottom: 8px;
}

.hero-rule strong {
  color: var(--text-primary);
}

.verify-section {
  margin-top: 16px;
}

.verify-card {
  max-width: 680px;
  margin: 0 auto;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: #ffffff;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: grid;
  justify-items: center;
  gap: 12px;
  text-align: center;
  margin-bottom: 24px;
}

.header-icon {
  font-size: 36px;
  color: var(--primary-color);
}

.card-title {
  margin: 0;
  font-size: 22px;
}

.card-subtitle {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.8;
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
  padding: 18px;
  border-radius: 8px;
  background: rgba(252, 251, 248, 0.82);
  border: 1px solid rgba(0, 9, 24, 0.06);
}

.summary-card strong,
.summary-label {
  color: var(--lex-primary);
}

.summary-label {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--lex-accent-strong);
  font-weight: 700;
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
  background: rgba(63, 111, 77, 0.08);
}

.status-card.status-expired {
  background: rgba(162, 108, 23, 0.1);
}

.status-card.status-revoked,
.status-card.status-invalid {
  background: rgba(180, 69, 55, 0.08);
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
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.status-valid .status-icon {
  font-size: 36px;
  color: #52c41a;
}

.status-expired .status-icon {
  font-size: 36px;
  color: #faad14;
}

.status-revoked .status-icon,
.status-invalid .status-icon {
  font-size: 36px;
  color: #ff4d4f;
}

.status-title {
  margin: 0 !important;
}

.info-card {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: #ffffff;
  box-shadow: var(--shadow-sm);
}

.info-head {
  margin-bottom: 18px;
}

.info-head h3 {
  margin: 4px 0 0;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 16px;
}

/* 页脚 */
.footer {
  background: rgba(249, 247, 242, 0.66);
  padding: 24px 0 32px;
  text-align: center;
  border-top: 1px solid var(--border-color-light);
}

/* 移动端适配 */
@media (max-width: 992px) {
  .hero-grid {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    max-width: none;
  }
}

@media (max-width: 768px) {
  .content {
    padding: 20px 0 32px;
  }

  .hero-copy,
  .hero-panel,
  .verify-card {
    padding: 16px;
  }

  .hero-panel__metrics {
    grid-template-columns: 1fr;
  }

  .verify-section {
    margin-top: 20px;
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

}
</style>
