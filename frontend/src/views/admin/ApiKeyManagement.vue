<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">API KEYS</p>
      <h1>API 密钥管理</h1>
      <p class="le-sub">律所业务系统对接密钥（演示数据，密钥已脱敏）</p>
    </header>

    <section class="le-card">
      <div class="le-card-head"><h2>密钥列表</h2></div>
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>名称</span><span>API Key</span><span>状态</span><span>有效期至</span><span>最近使用</span>
        </div>
        <div v-for="k in list" :key="k.id" class="le-row">
          <span><strong>{{ k.keyName }}</strong></span>
          <span class="le-mono">{{ k.apiKey }}</span>
          <span><em :class="['le-badge', k.enabled ? 'is-ok' : 'is-warn']">{{ k.enabled ? '已启用' : '已停用' }}</em></span>
          <span>{{ (k.expiresAt || '').slice(0, 10) }}</span>
          <span>{{ (k.lastUsedAt || '—').slice(0, 16) }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getApiKeyList, type ApiKeyInfo } from '@/api/apiKey'

const list = ref<ApiKeyInfo[]>([])

onMounted(async () => {
  try {
    const res = await getApiKeyList({ limit: 50 })
    list.value = res.data || []
  } catch {
    message.error('密钥列表加载失败')
  }
})
</script>
