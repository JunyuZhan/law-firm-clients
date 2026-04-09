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
            当前项目的文件，可预览或下载。需先从项目页进入以携带有效访问权限。
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
          <a-list
            v-else
            :data-source="files"
            class="file-list"
          >
            <template #renderItem="{ item }">
              <a-list-item class="file-item">
                <a-list-item-meta>
                  <template #avatar>
                    <FileOutlined class="file-icon" />
                  </template>
                  <template #title>
                    <span class="file-name">{{ item.fileName }}</span>
                  </template>
                  <template #description>
                    <div class="file-meta">
                      <span>{{ formatDate(item.uploadedAt) }}</span>
                      <span>{{ formatSize(item.fileSize) }}</span>
                    </div>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-space>
                    <a-button
                      type="text"
                      size="small"
                      @click="handlePreview(item)"
                    >
                      <EyeOutlined /> 查看
                    </a-button>
                    <a-button
                      type="text"
                      size="small"
                      @click="handleDownload(item)"
                    >
                      <DownloadOutlined /> 下载
                    </a-button>
                  </a-space>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </a-spin>
      </section>
    </a-layout-content>
    <MobileBottomNav />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { FileOutlined, EyeOutlined, DownloadOutlined } from '@ant-design/icons-vue'
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

.file-list :deep(.ant-list-items) {
  display: grid;
  gap: 12px;
}

.file-icon {
  font-size: 24px;
  color: var(--lex-primary-soft);
}

.file-name {
  font-weight: 600;
  color: var(--text-primary);
}

.file-meta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  color: var(--text-secondary);
}

.file-item:hover {
  background: var(--lex-bg-muted);
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

.empty-actions {
  justify-content: center;
  margin-top: 16px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
