<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">NOTIFICATIONS</p>
      <h1>通知记录</h1>
      <p class="le-sub">短信 / 微信 / 邮件 通知发送记录与统计（演示数据）</p>
    </header>

    <div class="le-stats">
      <div class="le-stat"><span class="le-stat-value">{{ stats.total }}</span><span class="le-stat-label">通知总数</span></div>
      <div class="le-stat"><span class="le-stat-value">{{ stats.success }}</span><span class="le-stat-label">发送成功</span></div>
      <div class="le-stat"><span class="le-stat-value">{{ stats.failed }}</span><span class="le-stat-label">发送失败</span></div>
      <div class="le-stat"><span class="le-stat-value">{{ stats.pending }}</span><span class="le-stat-label">发送中</span></div>
    </div>

    <section class="le-card">
      <div class="le-card-head"><h2>发送明细</h2><span class="le-count">共 {{ list.length }} 条</span></div>
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>时间</span><span>项目 / 标题</span><span>渠道</span><span>接收方</span><span>状态</span>
        </div>
        <div v-for="n in list" :key="n.id" class="le-row">
          <span class="le-mono">{{ (n.createdAt || '').slice(0, 16) }}</span>
          <span><strong>{{ n.title }}</strong><br><small>{{ n.matterName }}</small></span>
          <span><em class="le-badge">{{ typeText(n.notificationType) }}</em></span>
          <span>{{ n.recipient || '—' }}</span>
          <span><em :class="['le-badge', statusClass(n.status)]">{{ statusText(n.status) }}</em></span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getNotificationHistory, getNotificationStatistics, type PortalNotificationItem } from '@/api/notification'

const list = ref<PortalNotificationItem[]>([])
const stats = ref({ total: 0, success: 0, failed: 0, pending: 0 })

const STATUS: Record<string, string> = { SUCCESS: '发送成功', FAILED: '发送失败', PENDING: '发送中' }
const TYPE: Record<string, string> = { SMS: '短信', WECHAT: '微信', EMAIL: '邮件' }
const statusText = (s: string) => STATUS[s] || s
const typeText = (t: string) => TYPE[t] || t
const statusClass = (s: string) => (s === 'SUCCESS' ? 'is-ok' : s === 'FAILED' ? 'is-danger' : 'is-warn')

onMounted(async () => {
  try {
    const [h, s] = await Promise.all([getNotificationHistory({ limit: 50 }), getNotificationStatistics({})])
    list.value = h.data as unknown as PortalNotificationItem[]
    stats.value = {
      total: s.data.total,
      success: s.data.success,
      failed: s.data.failed,
      pending: s.data.pending,
    }
  } catch {
    message.error('通知记录加载失败')
  }
})
</script>
