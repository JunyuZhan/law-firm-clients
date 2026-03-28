<template>
  <div class="notifications-container">
    <AppHeader
      variant="portal"
      title="消息通知"
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
        >
          <template #renderItem="{ item }">
            <a-list-item>
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
                  {{ item.title }}
                </template>
                <template #description>
                  {{ item.content }}
                </template>
              </a-list-item-meta>
              <template #extra>
                <span class="notif-time">{{ formatTime(item.createdAt) }}</span>
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
import { BellOutlined } from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'

interface NotificationItem {
  id: string
  title: string
  content: string
  createdAt: string
  read: boolean
}

const loading = ref(false)
const notifications = ref<NotificationItem[]>([])

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
  notifications.value = []
})
</script>

<style scoped>
.notifications-container {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.content {
  padding: 16px;
}

.notif-icon {
  font-size: 20px;
  color: #1890ff;
}

.notif-time {
  font-size: 12px;
  color: #999;
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
  .notifications-container {
    padding-bottom: 70px;
  }
}
</style>
