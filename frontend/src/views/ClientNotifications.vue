<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">NOTIFICATIONS</p>
      <h1>消息通知</h1>
      <p class="le-sub">来自律所的项目进展与重要提醒（演示数据）</p>
    </header>

    <section class="le-card">
      <ul class="le-msgs">
        <li v-for="n in list" :key="n.id">
          <div class="le-msg-head">
            <strong>{{ n.title }}</strong>
            <em :class="['le-badge', n.status === 'SUCCESS' ? 'is-ok' : 'is-warn']">{{ typeText(n.notificationType) }}</em>
          </div>
          <p>{{ n.content }}</p>
          <small>{{ n.matterName }} · {{ (n.createdAt || '').slice(0, 16) }}</small>
        </li>
      </ul>
    </section>

    <footer class="le-foot">© 汇衡律师事务所 · 客户服务门户（Demo）</footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getPortalNotifications, type PortalNotificationItem } from '@/api/notification'

const DEMO_TOKEN = 'demo-portal-token-2026'
const list = ref<PortalNotificationItem[]>([])
const TYPE: Record<string, string> = { SMS: '短信', WECHAT: '微信', EMAIL: '邮件' }
const typeText = (t: string) => TYPE[t] || t

onMounted(async () => {
  try {
    const res = await getPortalNotifications(DEMO_TOKEN)
    list.value = res.data || []
  } catch {
    message.error('通知加载失败')
  }
})
</script>
