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
          <p class="intro-text">
            按时间查看提醒；未读条目带标记。
          </p>
        </div>
        <div class="stats-grid">
          <article class="stats-card">
            <span class="stats-label">全部</span>
            <strong>{{ notifications.length }}</strong>
          </article>
          <article class="stats-card">
            <span class="stats-label">未读</span>
            <strong>{{ unreadCount }}</strong>
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
            v-else-if="notifications.length === 0"
            class="empty-state"
          >
            <BellOutlined class="empty-icon" />
            <p>暂无消息</p>
            <p class="empty-hint">
              有新消息时将会通知您
            </p>
          </div>
          <div
            v-else
            class="notification-list"
          >
            <article
              v-for="item in notifications"
              :key="item.id"
              class="notification-card"
              :class="{ 'notification-card--unread': !item.read }"
            >
              <div class="notification-card__head">
                <a-badge
                  :dot="!item.read"
                  :offset="[-4, 4]"
                >
                  <div class="notification-card__icon">
                    <BellOutlined class="notif-icon" />
                  </div>
                </a-badge>
                <div class="notification-card__title-wrap">
                  <div class="notif-title-row">
                    <span class="notif-title">{{ item.title }}</span>
                    <span
                      v-if="!item.read"
                      class="notif-state"
                    >未读</span>
                  </div>
                  <span class="notif-time">{{ formatTime(item.createdAt) }}</span>
                </div>
              </div>
              <p class="notification-card__content">
                {{ item.content }}
              </p>
            </article>
          </div>
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
  notifications.value = portalVisitorStore.profile.lastMatterId
    ? [
        {
          id: 'matter-entry',
          title: '已同步最近访问的项目',
          content: '可从「文件中心」或项目详情继续查看材料。',
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

.notification-list :deep(.ant-list-items) {
  display: grid;
  gap: 12px;
}

.notification-list {
  display: grid;
  gap: 12px;
}

.notification-card {
  display: grid;
  gap: 14px;
  padding: 18px 16px;
  border-radius: 20px;
  border: 1px solid rgba(27, 59, 95, 0.08);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(246, 249, 252, 0.94));
  box-shadow: 0 14px 30px rgba(16, 42, 67, 0.08);
}

.notification-card--unread {
  border-color: rgba(27, 59, 95, 0.18);
  box-shadow: 0 18px 36px rgba(27, 59, 95, 0.12);
}

.notification-card__head {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: start;
  gap: 12px;
}

.notification-card__icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  background: rgba(27, 59, 95, 0.08);
}

.notification-card__title-wrap {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.notification-card__content {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
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
  background: rgba(30, 64, 175, 0.1);
  border: 1px solid rgba(30, 64, 175, 0.2);
  color: var(--lex-primary-soft);
  font-size: 11px;
  font-weight: 600;
}

.notif-time {
  font-size: 12px;
  color: var(--text-tertiary);
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

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
