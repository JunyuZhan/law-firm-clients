<template>
  <div class="notifications-container">
    <AppHeader
      variant="portal"
      title="消息通知"
    />
    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="page-intro section-shell">
        <div>
          <div class="eyebrow">
            Updates
          </div>
          <h1 class="intro-title">
            项目提醒与动态
          </h1>
          <p class="intro-text">
            通知页只负责一件事：按时间顺序展示最近提醒。未读状态、正文和时间放在同一视线区域，避免原本的信息断层。
          </p>
        </div>
        <div class="stats-grid">
          <article class="stats-card">
            <span class="stats-label">通知总数</span>
            <strong>{{ notifications.length }}</strong>
            <p>最近消息</p>
          </article>
          <article class="stats-card">
            <span class="stats-label">未读</span>
            <strong>{{ unreadCount }}</strong>
            <p>待处理提醒</p>
          </article>
        </div>
      </section>

      <section class="notification-spotlight section-shell">
        <article class="spotlight-card">
          <span class="panel-kicker">Inbox View</span>
          <h2>项目提醒与动态</h2>
          <p>通知页只负责时间顺序、未读状态与正文摘要，不把复杂筛选和额外业务设定强行塞进来。</p>
        </article>
        <article class="spotlight-card spotlight-card--metric">
          <span class="spotlight-label">最近 7 天</span>
          <strong>{{ recentCount }}</strong>
          <p>统计最近进入通知流的消息数量。</p>
        </article>
        <article class="spotlight-card spotlight-card--metric">
          <span class="spotlight-label">已读率</span>
          <strong>{{ readRate }}</strong>
          <p>帮助快速判断是否存在积压提醒。</p>
        </article>
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
            v-else-if="notifications.length === 0"
            class="empty-state"
          >
            <BellOutlined class="empty-icon" />
            <p>暂无消息</p>
            <p class="empty-hint">
              有新消息时将会通知您
            </p>
          </div>
          <a-list
            v-else
            :data-source="notifications"
            class="notification-list"
          >
            <template #renderItem="{ item }">
              <a-list-item class="notification-item">
                <a-list-item-meta>
                  <template #avatar>
                    <a-badge
                      :dot="!item.read"
                      :offset="[-4, 4]"
                    >
                      <BellOutlined class="notif-icon" />
                    </a-badge>
                  </template>
                  <template #title>
                    <div class="notif-title-row">
                      <span class="notif-title">{{ item.title }}</span>
                      <span
                        v-if="!item.read"
                        class="notif-state"
                      >未读</span>
                    </div>
                  </template>
                  <template #description>
                    <div class="notif-content">
                      <p>{{ item.content }}</p>
                      <span class="notif-time">{{ formatTime(item.createdAt) }}</span>
                    </div>
                  </template>
                </a-list-item-meta>
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
import { computed, ref, onMounted } from 'vue'
import { BellOutlined } from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { usePortalVisitorStore } from '@/stores/portalVisitor'

interface NotificationItem {
  id: string
  title: string
  content: string
  createdAt: string
  read: boolean
}

const portalVisitorStore = usePortalVisitorStore()
const loading = ref(false)
const notifications = ref<NotificationItem[]>([])

const unreadCount = computed(() => notifications.value.filter(item => !item.read).length)
const recentCount = computed(() => notifications.value.filter(item => {
  const time = new Date(item.createdAt).getTime()
  if (Number.isNaN(time)) return false
  return Date.now() - time <= 7 * 24 * 60 * 60 * 1000
}).length)
const readRate = computed(() => {
  if (notifications.value.length === 0) return '0%'
  const read = notifications.value.length - unreadCount.value
  return `${Math.round((read / notifications.value.length) * 100)}%`
})

function formatTime(dateStr?: string): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return dateStr.split('T')[0]
}

onMounted(() => {
  const visitorName = portalVisitorStore.displayName || '当前访客'
  notifications.value = portalVisitorStore.profile.lastMatterId
    ? [
        {
          id: 'matter-entry',
          title: '最近访问项目已同步',
          content: `${visitorName} 可继续从文件中心或项目详情查看最近访问项目的最新材料与动态。`,
          createdAt: new Date().toISOString(),
          read: false,
        },
      ]
    : []
})
</script>

<style scoped>
.content {
  display: grid;
  gap: 20px;
}

.notification-spotlight {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) repeat(2, minmax(0, 1fr));
  gap: 18px;
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

.notification-list :deep(.ant-list-items) {
  display: grid;
  gap: 12px;
}

.notif-icon {
  font-size: 20px;
  color: var(--lex-primary-soft);
}

.notif-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.notif-title {
  font-weight: 600;
  color: var(--text-primary);
}

.notif-state {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  background: rgba(179, 138, 61, 0.12);
  border: 1px solid rgba(179, 138, 61, 0.16);
  color: var(--accent-color-deep);
  font-size: 11px;
  font-weight: 600;
}

.notif-content {
  display: grid;
  gap: 8px;
}

.notif-content p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.notif-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.notification-item:hover {
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

@media (max-width: 768px) {
  .notification-spotlight,
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
