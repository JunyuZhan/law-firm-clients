<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">FILE CENTER</p>
      <h1>文件中心</h1>
      <p class="le-sub">系统推送的法律文书与您上传的补充材料（演示数据）</p>
    </header>

    <section class="le-card">
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>文件名</span><span>来源</span><span>大小</span><span>上传时间</span><span>操作</span>
        </div>
        <div v-for="f in list" :key="f.id" class="le-row">
          <span><strong>{{ f.fileName }}</strong><br><small>{{ f.description || '—' }}</small></span>
          <span><em class="le-badge">{{ f.fileSource === 'CLIENT_UPLOAD' ? '我上传的' : '律所推送' }}</em></span>
          <span>{{ fmtSize(f.fileSize) }}</span>
          <span>{{ (f.uploadedAt || '').slice(0, 16) }}</span>
          <span><a class="le-link" @click="download(f)">下载</a></span>
        </div>
      </div>
    </section>

    <footer class="le-foot">© 汇衡律师事务所 · 客户服务门户（Demo）</footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getFileList, downloadFile, type FileInfo } from '@/api/file'

const DEMO_TOKEN = 'demo-portal-token-2026'
const list = ref<FileInfo[]>([])

function fmtSize(bytes: number) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return `${(bytes / Math.pow(1024, i)).toFixed(i ? 1 : 0)} ${units[i]}`
}

async function download(f: FileInfo) {
  try {
    await downloadFile(f.id, f.matterId, DEMO_TOKEN, f.fileName)
    message.success('已开始下载（演示文件）')
  } catch {
    message.error('下载失败')
  }
}

onMounted(async () => {
  try {
    const res = await getFileList('M2026001', DEMO_TOKEN)
    list.value = res.data || []
  } catch {
    message.error('文件列表加载失败')
  }
})
</script>
