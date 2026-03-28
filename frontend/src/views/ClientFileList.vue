<template>
  <div class="file-list-container">
    <AppHeader
      variant="portal"
      title="我的文件"
    />
    <a-layout-content class="content">
      <a-spin :spinning="loading">
        <!-- 骨架屏 Loading -->
        <div
          v-if="loading"
          class="skeleton-list"
        >
          <div
            v-for="i in 5"
            :key="i"
            class="skeleton-item"
          >
            <a-skeleton
              :active="true"
              :paragraph="{ rows: 1 }"
            />
          </div>
        </div>
        <div
          v-else-if="!matterId || !token"
          class="empty-state"
        >
          <FileOutlined class="empty-icon" />
          <p>请先访问项目</p>
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
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #avatar>
                  <FileOutlined class="file-icon" />
                </template>
                <template #title>
                  {{ item.fileName }}
                </template>
                <template #description>
                  {{ formatDate(item.uploadedAt) }} · {{ formatSize(item.fileSize) }}
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
    </a-layout-content>
    <MobileBottomNav />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { FileOutlined, EyeOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { getFileList, downloadFile, previewFile, type FileInfo } from '@/api/file'

const route = useRoute()
const loading = ref(false)
const files = ref<FileInfo[]>([])
const matterId = ref(route.query.matterId as string || '')
const token = ref(route.query.token as string || '')

async function loadFiles() {
  if (!matterId.value || !token.value) return
  
  loading.value = true
  try {
    const res = await getFileList(matterId.value, token.value)
    if (res.success && res.data) {
      files.value = res.data
    }
  } catch (error) {
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

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.file-list-container {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.content {
  padding: 16px;
}

.file-icon {
  font-size: 24px;
  color: #1890ff;
}

.skeleton-list {
  padding: 16px;
}

.skeleton-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.empty-state {
  text-align: center;
  padding: 48px 0;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  color: #ddd;
  margin-bottom: 16px;
}

.empty-hint {
  font-size: 12px;
  color: #bbb;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .file-list-container {
    padding-bottom: 70px;
  }
}
</style>
