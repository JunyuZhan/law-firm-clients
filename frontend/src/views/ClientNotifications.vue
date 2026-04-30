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
      <section class="section-shell portal-panel portal-panel--intro">
        <div>
          <span class="portal-kicker">通知提醒</span>
          <h2 class="portal-heading">
            查看当前客户的通知记录与发送状态
          </h2>
          <p class="intro-text">
            通知页展示当前客户已生成的事项通知记录，重点是通知内容、对应事项和发送状态，而不是伪造未读消息模型。
          </p>
        </div>
        <div class="stats-grid">
          <article class="stats-card">
            <span class="stats-label">全部</span>
            <strong>{{ filteredNotifications.length }}</strong>
          </article>
          <article class="stats-card">
            <span class="stats-label">成功发送</span>
            <strong>{{ successCount }}</strong>
          </article>
        </div>
      </section>

      <section class="section-shell portal-panel">
        <div
          v-if="hasStoredContext || hasAccessContext"
          class="filter-row"
        >
          <span class="filter-row__label">
            {{ sourceText }}
          </span>
          <a-segmented
            v-if="notifications.length > 0"
            v-model:value="statusFilter"
            :options="filterOptions"
            size="small"
          />
        </div>

        <div
          v-else-if="notifications.length > 0"
          class="filter-row"
        >
          <span class="filter-row__label">发送状态</span>
          <a-segmented
            v-model:value="statusFilter"
            :options="filterOptions"
            size="small"
          />
        </div>

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
          <PortalStatePanel
            v-else-if="errorState === 'missing-token'"
            title="暂无可用访问上下文"
            description="请先通过律师发送的专属链接进入一个事项，再查看当前客户的通知记录。"
          >
            <template #icon>
              <BellOutlined />
            </template>
          </PortalStatePanel>
          <PortalStatePanel
            v-else-if="errorState === 'invalid-token'"
            title="访问链接已失效"
            description="当前保存的通知访问凭据不可用，可能已过期或被撤销。请重新通过律师发送的专属链接进入事项。"
          >
            <template #icon>
              <BellOutlined />
            </template>
          </PortalStatePanel>
          <PortalStatePanel
            v-else-if="notifications.length === 0"
            title="暂无通知记录"
            description="当前客户尚未产生通知发送记录。"
          >
            <template #icon>
              <BellOutlined />
            </template>
          </PortalStatePanel>
          <PortalStatePanel
            v-else-if="filteredNotifications.length === 0"
            title="当前筛选条件下暂无记录"
            description="可以切换发送状态筛选，查看其他通知记录。"
          >
            <template #icon>
              <BellOutlined />
            </template>
          </PortalStatePanel>
          <div
            v-else
            class="notification-list"
          >
            <article
              v-for="item in filteredNotifications"
              :key="item.id"
              class="notification-card"
              :class="{ 'notification-card--attention': item.status !== 'SUCCESS' }"
            >
              <div class="notification-card__head">
                <div class="notification-card__icon">
                  <BellOutlined class="notif-icon" />
                </div>
                <div class="notification-card__title-wrap">
                  <div class="notif-title-row">
                    <span class="notif-title">{{ item.title }}</span>
                    <span class="notif-state">{{ getStatusText(item.status) }}</span>
                  </div>
                  <span class="notif-time">
                    {{ item.matterName || item.matterId }} · {{ formatTime(item.sentAt || item.createdAt) }}
                  </span>
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
import { computed, ref, watch } from 'vue'
import { BellOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import PortalStatePanel from '@/components/PortalStatePanel.vue'
import { getPortalNotifications, type PortalNotificationItem } from '@/api/notification'
import { usePortalAccessContext } from '@/composables/usePortalAccessContext'
import {
  getPortalNotificationStatusText,
  resolvePortalAccessErrorState,
  type PortalAccessErrorState,
} from '@/utils/portalState'

const loading = ref(false)
const notifications = ref<PortalNotificationItem[]>([])
const statusFilter = ref<'ALL' | 'SUCCESS' | 'PENDING' | 'FAILED'>('ALL')
const errorState = ref<PortalAccessErrorState>('none')
const { token, hasStoredContext, hasAccessContext, sourceText } = usePortalAccessContext()

const successCount = computed(() => notifications.value.filter(item => item.status === 'SUCCESS').length)
const filterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '成功', value: 'SUCCESS' },
  { label: '发送中', value: 'PENDING' },
  { label: '失败', value: 'FAILED' },
]
const filteredNotifications = computed(() => {
  if (statusFilter.value === 'ALL') {
    return notifications.value
  }
  return notifications.value.filter(item => item.status === statusFilter.value)
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

const getStatusText = getPortalNotificationStatusText

async function loadNotifications() {
  const accessToken = token.value
  if (!accessToken) {
    notifications.value = []
    errorState.value = 'missing-token'
    return
  }

  loading.value = true
  errorState.value = 'none'
  try {
    const res = await getPortalNotifications(accessToken, 20)
    notifications.value = res.data || []
  } catch (error) {
    notifications.value = []
    errorState.value = resolvePortalAccessErrorState(error)
    message.error('加载通知记录失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => token.value,
  () => {
    loadNotifications()
  },
  { immediate: true },
)
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
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(27, 59, 95, 0.08);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(246, 249, 252, 0.94));
}

.notification-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(16, 42, 67, 0.08);
}

.notification-card--attention {
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

@media (max-width: 768px) {
}
</style>
