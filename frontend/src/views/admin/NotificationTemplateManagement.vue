<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">NOTIFICATION TEMPLATES</p>
      <h1>通知模板管理</h1>
      <p class="le-sub">短信 / 微信 / 邮件 通知文案模板（演示数据）</p>
    </header>

    <section class="le-card">
      <div class="le-card-head"><h2>模板列表</h2></div>
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>模板名称</span><span>类型</span><span>模板编码</span><span>服务商</span><span>状态</span>
        </div>
        <div v-for="t in list" :key="t.id" class="le-row">
          <span><strong>{{ t.templateName }}</strong><br><small>{{ t.description || '—' }}</small></span>
          <span><em class="le-badge">{{ typeText(t.templateType) }}</em></span>
          <span class="le-mono">{{ t.templateCode }}</span>
          <span>{{ t.provider || '—' }}</span>
          <span><em :class="['le-badge', t.enabled ? 'is-ok' : 'is-warn']">{{ t.enabled ? '已启用' : '已停用' }}</em></span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getTemplateList, type NotificationTemplateInfo } from '@/api/notificationTemplate'

const list = ref<NotificationTemplateInfo[]>([])
const TYPE: Record<string, string> = { SMS: '短信', WECHAT: '微信', EMAIL: '邮件' }
const typeText = (t: string) => TYPE[t] || t

onMounted(async () => {
  try {
    const res = await getTemplateList({ limit: 50 })
    list.value = res.data || []
  } catch {
    message.error('模板列表加载失败')
  }
})
</script>
