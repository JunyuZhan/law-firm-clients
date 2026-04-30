<template>
  <div class="file-list-container">
    <AppHeader
      variant="portal"
      title="文件中心"
    />
    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="section-shell portal-panel portal-panel--intro">
        <div>
          <span class="portal-kicker">文件协作</span>
          <h2 class="portal-heading">
            查看当前事项已授权的文件资料
          </h2>
          <p class="intro-text">
            文件中心只承接当前事项下的材料查看与下载。打开文件后可继续预览、下载或复制预览链接。
          </p>
        </div>
        <div class="stats-grid">
          <article class="stats-card">
            <span class="stats-label">文件数</span>
            <strong>{{ files.length }}</strong>
          </article>
          <article class="stats-card">
            <span class="stats-label">最近更新</span>
            <strong>{{ latestFileDate }}</strong>
          </article>
        </div>
      </section>

      <section class="section-shell portal-panel">
        <div
          v-if="hasStoredContext || hasAccessContext"
          class="section-actions"
        >
          <span class="section-actions__hint">
            {{ sourceText }}
          </span>
          <a-button
            type="default"
            size="small"
            @click="loadFiles"
          >
            刷新文件
          </a-button>
        </div>

        <a-spin :spinning="loading">
          <div
            v-if="loading"
            class="skeleton-list"
          >
            <div
              v-for="i in 4"
              :key="i"
              class="skeleton-item"
            >
              <a-skeleton
                :active="true"
                :paragraph="{ rows: 2 }"
              />
            </div>
          </div>
          <PortalStatePanel
            v-else-if="errorState === 'missing-token'"
            title="暂无可用访问上下文"
            description="请先通过律师发送的专属链接进入一个事项，再查看对应的文件资料。"
          >
            <template #icon>
              <FileOutlined />
            </template>
            <template #actions>
              <a-button
                type="primary"
                @click="router.push('/portal')"
              >
                返回首页
              </a-button>
            </template>
          </PortalStatePanel>
          <PortalStatePanel
            v-else-if="errorState === 'invalid-token'"
            title="访问链接已失效"
            description="当前保存的文件访问凭据不可用，可能已过期或被撤销。请重新通过律师发送的专属链接进入事项。"
          >
            <template #icon>
              <FileOutlined />
            </template>
            <template #actions>
              <a-button
                type="primary"
                @click="router.push('/portal')"
              >
                返回首页
              </a-button>
              <a-button @click="clearAccessContext">
                清除本地记录
              </a-button>
            </template>
          </PortalStatePanel>
          <PortalStatePanel
            v-else-if="files.length === 0"
            title="暂无文件"
            description="律所将为您上传相关文件。"
          >
            <template #icon>
              <FileOutlined />
            </template>
          </PortalStatePanel>
          <div
            v-else
            class="file-card-grid"
          >
            <article
              v-for="item in files"
              :key="item.id"
              class="file-card"
              @click="openFileActions(item)"
            >
              <div class="file-card__icon">
                <Icon
                  :icon="getFileVisual(item).icon"
                  :style="{ color: getFileVisual(item).color }"
                />
              </div>
              <div class="file-card__copy">
                <strong class="file-name">{{ item.fileName }}</strong>
                <div class="file-meta">
                  <span>{{ formatDate(item.uploadedAt) }}</span>
                  <span>{{ formatSize(item.fileSize) }}</span>
                </div>
                <p class="file-note">
                  点击后可继续预览、下载或分享当前文件。
                </p>
              </div>
            </article>
          </div>
        </a-spin>
      </section>
    </a-layout-content>
    <MobileBottomNav />

    <van-action-sheet
      v-model:show="actionSheetOpen"
      :actions="fileActions"
      cancel-text="取消"
      close-on-click-action
      @select="handleFileAction"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { FileOutlined } from '@ant-design/icons-vue'
import { Icon } from '@iconify/vue'
import { message } from 'ant-design-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import PortalStatePanel from '@/components/PortalStatePanel.vue'
import { getFileList, downloadFile, previewFile, type FileInfo } from '@/api/file'
import { usePortalAccessContext } from '@/composables/usePortalAccessContext'
import {
  resolvePortalAccessErrorState,
  type PortalAccessErrorState,
} from '@/utils/portalState'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const files = ref<FileInfo[]>([])
const actionSheetOpen = ref(false)
const selectedFile = ref<FileInfo | null>(null)
const errorState = ref<PortalAccessErrorState>('none')
const { matterId, token, hasStoredContext, hasAccessContext, sourceText, clearAccessContext } = usePortalAccessContext()
const fileActions = [
  { name: '预览', key: 'preview' },
  { name: '下载', key: 'download' },
  { name: '分享', key: 'share' },
]

const latestFileDate = computed(() => {
  const dates = files.value
    .map(item => item.uploadedAt)
    .filter(Boolean)
    .sort()
  const latest = dates.length > 0 ? dates[dates.length - 1] : undefined
  return latest ? formatDate(latest) : '-'
})
async function loadFiles() {
  if (!matterId.value || !token.value) {
    files.value = []
    errorState.value = 'missing-token'
    return
  }

  loading.value = true
  errorState.value = 'none'
  try {
    const res = await getFileList(matterId.value, token.value)
    if (res.success && res.data) {
      files.value = res.data
    }
  } catch (error) {
    files.value = []
    errorState.value = resolvePortalAccessErrorState(error)
    message.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  return dateStr.split('T')[0]
}

function formatSize(size?: number): string {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

function handlePreview(item: FileInfo) {
  if (!matterId.value || !token.value) return
  const url = previewFile(item.id, matterId.value, token.value)
  window.open(url, '_blank')
}

function handleDownload(item: FileInfo) {
  if (!matterId.value || !token.value) return
  downloadFile(item.id, matterId.value, token.value)
}

function getFileVisual(item: FileInfo): { icon: string; color: string } {
  const ext = item.fileName.split('.').pop()?.toLowerCase() || ''
  if (ext === 'pdf') return { icon: 'mdi:file-pdf-box', color: '#b42318' }
  if (['doc', 'docx'].includes(ext)) return { icon: 'mdi:file-word-box', color: '#175cd3' }
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) return { icon: 'mdi:file-image', color: '#2d6a4f' }
  return { icon: 'mdi:file-document-outline', color: '#486581' }
}

function openFileActions(item: FileInfo) {
  selectedFile.value = item
  actionSheetOpen.value = true
}

async function handleFileAction(action: { key?: string; name: string }) {
  const item = selectedFile.value
  if (!item) return

  if (action.key === 'preview') {
    handlePreview(item)
    return
  }

  if (action.key === 'download') {
    handleDownload(item)
    return
  }

  if (action.key === 'share' && matterId.value && token.value) {
    const shareUrl = `${window.location.origin}${previewFile(item.id, matterId.value, token.value)}`
    try {
      if (navigator.share) {
        await navigator.share({ title: item.fileName, url: shareUrl })
      } else {
        await navigator.clipboard.writeText(shareUrl)
        message.success('预览链接已复制')
      }
    } catch {
      message.warning('分享未完成')
    }
  }
}

watch(
  () => [route.query.matterId, route.query.token],
  () => {
    loadFiles()
  },
)

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.content {
  display: grid;
  gap: 20px;
}

.file-card-grid {
  display: grid;
  gap: 12px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(16, 42, 67, 0.08);
}

.file-card:hover {
  border-color: rgba(16, 42, 67, 0.14);
  box-shadow: 0 14px 28px rgba(16, 42, 67, 0.08);
  transform: translateY(-1px);
}

.file-card__icon {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  background: var(--lex-bg-muted);
  font-size: 26px;
  flex-shrink: 0;
}

.file-card__copy {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.file-name {
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
}

.file-meta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  color: var(--text-secondary);
}

.file-note {
  margin: 2px 0 0;
  color: var(--text-tertiary);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 768px) {
}
</style>
