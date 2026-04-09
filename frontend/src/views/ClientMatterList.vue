<template>
  <div class="matter-list-container">
    <AppHeader
      variant="portal"
      title="我的项目"
    />
    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="page-intro section-shell">
        <div>
          <p class="intro-text">
            点击项目进入详情；「有效」即可访问。
          </p>
        </div>
        <div class="stats-grid">
          <article class="stats-card">
            <span class="stats-label">全部</span>
            <strong>{{ matters.length }}</strong>
          </article>
          <article class="stats-card">
            <span class="stats-label">有效</span>
            <strong>{{ activeCount }}</strong>
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
              v-for="i in 3"
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
                    <div class="matter-title-row">
                      <span class="matter-name">{{ item.matterName }}</span>
                      <a-tag :color="getStatusColor(item.status)">
                        {{ getStatusName(item.status) }}
                      </a-tag>
                    </div>
                  </template>
                  <template #description>
                    <div class="matter-meta">
                      <span>有效期至 {{ formatDate(item.expiresAt) }}</span>
                    </div>
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <span class="matter-action">进入</span>
                  <ArrowRightOutlined />
                </template>
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
import { computed, ref } from 'vue'
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

const activeCount = computed(() => matters.value.filter(item => item.status === 'ACTIVE').length)

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
.content {
  display: grid;
  gap: 20px;
}

.matter-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.matter-name {
  font-weight: 600;
  color: var(--text-primary);
}

.matter-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: var(--text-secondary);
}

.matter-action {
  color: var(--lex-primary-soft);
  font-size: 13px;
  font-weight: 600;
}

.matter-item {
  cursor: pointer;
}

.matter-item:hover {
  background: var(--lex-bg-muted);
}

.skeleton-list {
  display: grid;
  gap: 12px;
}

.skeleton-item {
  padding: 16px;
  border-radius: 8px;
  background: var(--lex-outline-soft);
  border: 1px solid var(--border-color-light);
}

@media (max-width: 768px) {
  .content {
    gap: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
