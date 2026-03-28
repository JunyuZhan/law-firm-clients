<template>
  <div class="matter-detail-container">
    <a-card>
      <template #title>
        <a-space>
          <a-button
            class="back-btn"
            @click="goBack"
          >
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            <span class="back-btn-text">返回</span>
          </a-button>
          <span>项目详情</span>
        </a-space>
      </template>

      <a-spin :spinning="loading">
        <div v-if="matterDetail">
          <!-- 基本信息 -->
          <a-card
            title="基本信息"
            style="margin-bottom: 24px"
          >
            <a-descriptions
              :column="{ xs: 1, sm: 2 }"
              bordered
            >
              <a-descriptions-item label="项目ID">
                {{ matterDetail.id }}
              </a-descriptions-item>
              <a-descriptions-item label="律所项目ID">
                {{ matterDetail.lawFirmMatterId }}
              </a-descriptions-item>
              <a-descriptions-item label="客户ID">
                {{ matterDetail.clientId }}
              </a-descriptions-item>
              <a-descriptions-item label="客户名称">
                {{ matterDetail.clientName }}
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="getStatusColor(matterDetail.status)">
                  {{ getStatusName(matterDetail.status) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="有效期（天）">
                {{ matterDetail.validDays }}
              </a-descriptions-item>
              <a-descriptions-item label="过期时间">
                <span :style="{ color: isExpired(matterDetail.expiresAt) ? '#cf1322' : '' }">
                  {{ formatDate(matterDetail.expiresAt) }}
                </span>
              </a-descriptions-item>
              <a-descriptions-item label="权限范围">
                {{ matterDetail.scopes }}
              </a-descriptions-item>
              <a-descriptions-item label="创建时间">
                {{ formatDate(matterDetail.createdAt) }}
              </a-descriptions-item>
              <a-descriptions-item label="更新时间">
                {{ formatDate(matterDetail.updatedAt) }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <!-- 访问信息 -->
          <a-card
            title="访问信息"
            style="margin-bottom: 24px"
          >
            <a-descriptions
              :column="1"
              bordered
            >
              <a-descriptions-item label="访问链接">
                <div class="access-url-wrapper">
                  <a
                    :href="matterDetail.accessUrl"
                    target="_blank"
                    class="access-url"
                  >
                    {{ matterDetail.accessUrl }}
                  </a>
                  <a-button
                    type="link"
                    size="small"
                    class="copy-btn"
                    @click="copyToClipboard(matterDetail.accessUrl)"
                  >
                    复制
                  </a-button>
                </div>
              </a-descriptions-item>
              <a-descriptions-item label="访问令牌">
                <div class="token-display">
                  <a-typography-text :copyable="{ text: matterDetail.accessToken }">
                    {{ showToken ? matterDetail.accessToken : maskToken(matterDetail.accessToken) }}
                  </a-typography-text>
                  <a-button
                    type="link"
                    size="small"
                    @click="showToken = !showToken"
                  >
                    {{ showToken ? '隐藏' : '显示' }}
                  </a-button>
                </div>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>

          <!-- 项目数据 -->
          <a-card
            title="项目数据"
            style="margin-bottom: 24px"
          >
            <a-typography-paragraph>
              <pre class="matter-data-pre">{{ formatMatterData(matterDetail.matterData) }}</pre>
            </a-typography-paragraph>
          </a-card>

          <!-- 操作按钮 -->
          <a-card>
            <a-space
              class="action-buttons"
              :size="16"
            >
              <a-button
                v-if="matterDetail.status === 'ACTIVE'"
                type="primary"
                danger
                @click="handleRevoke"
              >
                撤销访问
              </a-button>
              <a-button @click="goBack">
                返回列表
              </a-button>
            </a-space>
          </a-card>
        </div>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import { getMatterDetail, revokeMatter, type MatterDetailInfo } from '@/api/matter'
import { formatDate, isExpired } from '@/utils/date'
import { getMatterStatusColor, getMatterStatusText } from '@/utils/status'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const matterDetail = ref<MatterDetailInfo | null>(null)
const showToken = ref(false)

// 隐藏Token显示（只显示前6位和后4位）
function maskToken(token: string): string {
  if (!token || token.length <= 10) return '••••••••••'
  return token.slice(0, 6) + '••••••••' + token.slice(-4)
}

// 加载项目详情
async function loadData() {
  const id = route.params.id as string
  // 验证 ID 格式（仅允许字母、数字、连字符、下划线）
  if (!id || !/^[\w-]+$/.test(id)) {
    message.error('项目ID无效')
    goBack()
    return
  }

  loading.value = true
  try {
    const res = await getMatterDetail(id)
    matterDetail.value = res.data
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '加载项目详情失败'
    message.error(errorMessage)
    goBack()
  } finally {
    loading.value = false
  }
}

// 返回列表
function goBack() {
  router.push('/admin/matters')
}

// 撤销项目
async function handleRevoke() {
  if (!matterDetail.value) return

  try {
    await revokeMatter(matterDetail.value.id)
    message.success('项目访问已撤销')
    await loadData() // 重新加载数据
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '撤销项目失败'
    message.error(errorMessage)
  }
}

// 复制到剪贴板（兼容 HTTP 环境）
function copyToClipboard(text: string) {
  // 优先使用现代 API
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      message.success('已复制到剪贴板')
    }).catch(() => {
      fallbackCopy(text)
    })
  } else {
    fallbackCopy(text)
  }
}

// 降级复制方案（兼容非 HTTPS 环境）
function fallbackCopy(text: string) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    message.success('已复制到剪贴板')
  } catch {
    message.error('复制失败，请手动复制')
  }
  document.body.removeChild(textarea)
}

// 使用统一的状态工具函数
const getStatusName = getMatterStatusText
const getStatusColor = getMatterStatusColor

// isExpired 和 formatDate 已从 @/utils/date 导入

// 格式化项目数据
function formatMatterData(data?: Record<string, unknown>): string {
  if (!data) return '无数据'
  return JSON.stringify(data, null, 2)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.matter-detail-container {
  padding: 24px;
}

.back-btn-text {
  margin-left: 4px;
}

.access-url-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.access-url {
  flex: 1;
  word-break: break-all;
  min-width: 0;
}

.copy-btn {
  flex-shrink: 0;
  white-space: nowrap;
}

.token-display {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.token-display :deep(.ant-typography) {
  font-family: monospace;
  word-break: break-all;
}

.matter-data-pre {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.6;
}

.action-buttons {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .matter-detail-container {
    padding: 16px 12px;
  }
  
  .back-btn-text {
    display: none; /* 移动端隐藏文字，只显示图标 */
  }
  
  .matter-detail-container :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .matter-detail-container :deep(.ant-card-head-title) {
    font-size: 14px;
  }
  
  .matter-detail-container :deep(.ant-card-body) {
    padding: 16px;
  }
  
  .access-url-wrapper {
    flex-direction: column;
    align-items: stretch;
  }
  
  .copy-btn {
    width: 100%;
    text-align: left;
    padding-left: 0;
  }
  
  .matter-data-pre {
    padding: 12px;
    font-size: 11px;
  }
  
  .action-buttons {
    flex-direction: column;
    width: 100%;
  }
  
  .action-buttons :deep(.ant-space-item) {
    width: 100%;
  }
  
  .action-buttons :deep(.ant-btn) {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .matter-detail-container {
    padding: 12px 8px;
  }
  
  .matter-detail-container :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .matter-detail-container :deep(.ant-card-body) {
    padding: 12px;
  }
  
  .matter-detail-container :deep(.ant-descriptions-item-label) {
    font-size: 12px;
  }
  
  .matter-detail-container :deep(.ant-descriptions-item-content) {
    font-size: 12px;
  }
  
  .matter-data-pre {
    padding: 8px;
    font-size: 10px;
  }
}
</style>
