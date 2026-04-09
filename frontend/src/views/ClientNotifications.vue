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
  background: var(--lex-bg-muted);
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
