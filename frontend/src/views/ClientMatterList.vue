<template>
  <div class="matter-list-container">
    <AppHeader
      variant="portal"
      title="我的项目"
    />
    <a-layout-content class="content">
      <!-- 骨架屏 Loading -->
      <a-spin :spinning="loading">
        <div
          v-if="loading"
          class="skeleton-list"
        >
          <div
            v-for="i in 3"
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
          v-else-if="matters.length === 0"
          class="empty-state"
        >
          <FileTextOutlined class="empty-icon" />
          <p>暂无项目</p>
          <p class="empty-hint">
            请联系律所为您开通项目访问权限
          </p>
        </div>
        <a-list
          v-else
          :data-source="matters"
        >
          <template #renderItem="{ item }">
            <a-list-item
              class="matter-item"
              @click="goToMatter(item)"
            >
              <a-list-item-meta>
                <template #title>
                  <span>{{ item.matterName }}</span>
                </template>
                <template #description>
                  <a-space>
                    <a-tag :color="getStatusColor(item.status)">
                      {{ getStatusName(item.status) }}
                    </a-tag>
                    <span>有效期至 {{ formatDate(item.expiresAt) }}</span>
                  </a-space>
                </template>
              </a-list-item-meta>
              <template #actions>
                <ArrowRightOutlined />
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { FileTextOutlined, ArrowRightOutlined } from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'

interface MatterItem {
  id: string
  matterName: string
  status: string
  expiresAt: string
  accessToken: string
}

const router = useRouter()
const loading = ref(false)
const matters = ref<MatterItem[]>([])

function getStatusColor(status: string): string {
  const colors: Record<string, string> = {
    ACTIVE: 'green',
    EXPIRED: 'orange',
    REVOKED: 'red',
  }
  return colors[status] || 'default'
}

function getStatusName(status: string): string {
  const names: Record<string, string> = {
    ACTIVE: '有效',
    EXPIRED: '已过期',
    REVOKED: '已撤销',
  }
  return names[status] || status
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  return dateStr.split('T')[0]
}

function goToMatter(item: MatterItem) {
  router.push(`/matter/${item.id}?token=${item.accessToken}`)
}
</script>

<style scoped>
.matter-list-container {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.content {
  padding: 16px;
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

.matter-item {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.matter-item:active {
  background-color: #f5f5f5;
}

@media (max-width: 768px) {
  .matter-list-container {
    padding-bottom: 70px;
  }
}
</style>
