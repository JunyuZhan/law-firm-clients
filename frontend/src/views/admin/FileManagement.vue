<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">FILE CENTER</p>
      <h1>文件管理</h1>
      <p class="le-sub">系统推送与客户上传的文件统一管理（演示数据）</p>
    </header>

    <section class="le-card">
      <div class="le-card-head"><h2>文件列表</h2><span class="le-count">共 {{ files.length }} 个文件</span></div>
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>文件名</span><span>所属项目</span><span>来源</span><span>大小</span><span>上传时间</span>
        </div>
        <div v-for="f in files" :key="f.id" class="le-row">
          <span><strong>{{ f.fileName }}</strong><br><small>{{ f.description || '—' }}</small></span>
          <span class="le-mono">{{ f.matterId }}</span>
          <span><em class="le-badge">{{ f.fileSource === 'CLIENT_UPLOAD' ? '客户上传' : '系统推送' }}</em></span>
          <span>{{ fmtSize(f.fileSize) }}</span>
          <span>{{ (f.uploadedAt || '').slice(0, 16) }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import request from '@/api/request'
import type { ApiResponse } from '@/api/request'
import type { FileInfo } from '@/api/file'

const files = ref<FileInfo[]>([])

function fmtSize(bytes: number) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return `${(bytes / Math.pow(1024, i)).toFixed(i ? 1 : 0)} ${units[i]}`
}

onMounted(async () => {
  try {
    const res = await request.get('/api/client/files') as ApiResponse<FileInfo[]>
    files.value = res.data
  } catch {
    message.error('文件列表加载失败')
  }
})
</script>
