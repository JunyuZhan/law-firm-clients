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
          <div class="eyebrow">
            File Center
          </div>
          <h1 class="intro-title">
            当前项目文件
          </h1>
          <p class="intro-text">
            文件按查看和下载两个动作统一呈现。页面只保留必要信息，避免原先操作区和列表区相互打架。
          </p>
        </div>
        <div class="stats-grid">
          <article class="stats-card">
            <span class="stats-label">文件数量</span>
            <strong>{{ files.length }}</strong>
            <p>当前项目内文件</p>
          </article>
          <article class="stats-card">
            <span class="stats-label">最近更新</span>
            <strong>{{ latestFileDate }}</strong>
            <p>按最新上传时间显示</p>
          </article>
        </div>
      </section>

      <section class="file-spotlight section-shell">
        <article class="spotlight-card">
          <span class="panel-kicker">Snapshot</span>
          <h2>受控文件快照</h2>
          <p>页面只保留查看、下载与必要元信息，避免把项目页面的复杂操作搬到文件中心里。</p>
        </article>
        <article class="spotlight-card spotlight-card--metric">
          <span class="spotlight-label">可预览类型</span>
          <strong>{{ previewableCount }}</strong>
          <p>图片、PDF 或可直接打开的文件。</p>
        </article>
        <article class="spotlight-card spotlight-card--metric">
          <span class="spotlight-label">文件总体积</span>
          <strong>{{ totalSizeLabel }}</strong>
          <p>用于快速判断资料规模。</p>
        </article>
      </section>

      <section class="file-context section-shell">
        <article class="context-card">
          <span class="context-label">当前项目</span>
          <strong>{{ matterId || '-' }}</strong>
          <p>当前文件列表绑定的项目编号。</p>
        </article>
        <article class="context-card">
          <span class="context-label">访问上下文</span>
          <strong>{{ token ? '已就绪' : '待进入项目' }}</strong>
          <p>只有具备有效访问 token 才能查看或下载材料。</p>
        </article>
      </section>

      <section class="table-panel section-shell">
        <div class="list-head">
          <div>
            <span class="panel-kicker">File List</span>
            <h2>文件列表</h2>
          </div>
          <p>把文件名、时间、大小和操作收在同一层，避免列表和动作区相互打断。</p>
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
              <a-button type="primary" @click="router.push('/matters')">查看项目</a-button>
              <a-button @click="router.push('/portal')">返回首页</a-button>
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

const totalSize = computed(() => files.value.reduce((sum, item) => sum + (item.fileSize || 0), 0))
const totalSizeLabel = computed(() => formatSize(totalSize.value))
const previewableCount = computed(() => files.value.filter(item => {
  const name = (item.fileName || '').toLowerCase()
  return /\.(pdf|png|jpg|jpeg|gif|webp|txt|md)$/i.test(name)
}).length)
const latestFileDate = computed(() => {
  const dates = files.value
    .map(item => item.uploadedAt)
    .filter(Boolean)
    .sort()
  const latest = dates.length > 0 ? dates[dates.length - 1] : undefined
  return latest ? formatDate(latest) : '-'
})
const hasRouteContext = computed(() => Boolean(route.query.matterId && route.query.token))
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

.file-spotlight {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.file-context {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.context-card {
  display: grid;
  gap: 6px;
  padding: 18px 20px;
  border-radius: 8px;
  background: rgba(252, 251, 248, 0.82);
  border: 1px solid rgba(15, 23, 42, 0.05);
  box-shadow: var(--shadow-xs);
}

.context-label {
  color: var(--text-tertiary);
  font-size: 12px;
}

.context-card strong {
  color: var(--text-primary);
  font-size: 22px;
  line-height: 1.2;
}

.context-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.spotlight-card {
  display: grid;
  gap: 8px;
  padding: 20px;
  border-radius: 8px;
  background: rgba(252, 251, 248, 0.78);
  border: 1px solid rgba(0, 9, 24, 0.05);
  box-shadow: var(--shadow-xs);
}

.spotlight-card h2,
.spotlight-card p {
  margin: 0;
}

.spotlight-card h2 {
  font-size: 24px;
  color: var(--lex-primary);
  font-family: var(--font-heading);
}

.spotlight-card p {
  color: var(--text-secondary);
  line-height: 1.8;
}

.spotlight-card--metric {
  align-content: end;
}

.spotlight-label,
.panel-kicker {
  color: var(--lex-accent-strong);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-weight: 700;
}

.spotlight-card--metric strong {
  font-size: 32px;
  line-height: 1;
  color: var(--lex-primary);
}

.list-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.list-head h2 {
  margin: 6px 0 0;
  color: var(--text-primary);
  font-size: 22px;
}

.list-head p {
  margin: 0;
  max-width: 320px;
  color: var(--text-secondary);
  line-height: 1.75;
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
  background: rgba(252, 251, 248, 0.9);
}

.skeleton-list {
  display: grid;
  gap: 12px;
}

.skeleton-item {
  padding: 16px;
  border-radius: 8px;
  background: rgba(252, 251, 248, 0.82);
  border: 1px solid var(--border-color-light);
}

.empty-actions {
  justify-content: center;
  margin-top: 16px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .file-spotlight,
  .stats-grid,
  .file-context,
  .list-head {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
