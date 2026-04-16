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
      <section class="page-intro section-shell">
        <div>
          <p class="intro-text">
            当前项目的文书与证据材料，以移动端动作面板方式进行预览、下载与分享。
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

      <section class="table-panel section-shell">
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
          <div
            v-else-if="!matterId || !token"
            class="empty-state"
          >
            <FileOutlined class="empty-icon" />
            <p>{{ hasStoredContext ? '最近访问项目已失效' : '请先进入项目' }}</p>
            <p class="empty-hint">
              {{ hasStoredContext ? '请重新进入项目详情页后再查看文件。' : '请先从首页或项目列表进入一个有效项目。' }}
            </p>
            <a-space class="empty-actions">
              <a-button
                type="primary"
                @click="router.push('/matters')"
              >
                查看项目
              </a-button>
              <a-button @click="router.push('/portal')">
                返回首页
              </a-button>
            </a-space>
          </div>
          <div
            v-else-if="files.length === 0"
            class="empty-state"
          >
            <FileOutlined class="empty-icon" />
            <p>暂无文件</p>
            <p class="empty-hint">
              律所将为您上传相关文件
            </p>
          </div>
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
import { getFileList, downloadFile, previewFile, type FileInfo } from '@/api/file'
import { usePortalVisitorStore } from '@/stores/portalVisitor'

const route = useRoute()
const router = useRouter()
const portalVisitorStore = usePortalVisitorStore()
const loading = ref(false)
const files = ref<FileInfo[]>([])
const matterId = ref('')
const token = ref('')
const actionSheetOpen = ref(false)
const selectedFile = ref<FileInfo | null>(null)
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
const hasStoredContext = computed(() => Boolean(
  portalVisitorStore.profile.lastMatterId && portalVisitorStore.profile.lastMatterToken,
))

function syncAccessContext() {
  const routeMatterId = (route.query.matterId as string) || ''
  const routeToken = (route.query.token as string) || ''

  if (routeMatterId && routeToken) {
    matterId.value = routeMatterId
    token.value = routeToken
    return
  }

  matterId.value = portalVisitorStore.profile.lastMatterId || ''
  token.value = portalVisitorStore.profile.lastMatterToken || ''
}

async function loadFiles() {
  if (!matterId.value || !token.value) return

  loading.value = true
  try {
    const res = await getFileList(matterId.value, token.value)
    if (res.success && res.data) {
      files.value = res.data
    }
  } catch {
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
    syncAccessContext()
    loadFiles()
  },
)

onMounted(() => {
  syncAccessContext()
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
  padding: 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(217, 226, 236, 0.92);
  box-shadow: 0 10px 24px rgba(16, 42, 67, 0.08);
}

.file-card__icon {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 14px;
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

.skeleton-list {
  display: grid;
  gap: 12px;
}

.skeleton-item {
  padding: 16px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--border-color-light);
}
</style>
