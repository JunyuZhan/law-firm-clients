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
          <div class="eyebrow">
            Matter Access
          </div>
          <h1 class="intro-title">
            项目访问与状态总览
          </h1>
          <p class="intro-text">
            这里集中展示您当前可访问的项目。状态、有效期和进入路径都在同一层级里，不需要反复跳转确认。
          </p>
        </div>
        <div class="stats-grid">
          <article class="stats-card">
            <span class="stats-label">项目数量</span>
            <strong>{{ matters.length }}</strong>
            <p>当前可访问项目</p>
          </article>
          <article class="stats-card">
            <span class="stats-label">活跃项目</span>
            <strong>{{ activeCount }}</strong>
            <p>状态为有效</p>
          </article>
        </div>
      </section>

      <section class="matter-summary section-shell">
        <article class="summary-card">
          <span class="summary-label">最近状态</span>
          <strong>{{ activeCount > 0 ? '可继续处理' : '待开通访问' }}</strong>
          <p>根据当前有效项目数量自动判断。</p>
        </article>
        <article class="summary-card">
          <span class="summary-label">进入方式</span>
          <strong>点击项目进入详情</strong>
          <p>访问 token 会随项目入口一同带入。</p>
        </article>
      </section>

      <section class="table-panel section-shell">
        <div class="list-head">
          <div>
            <span class="panel-kicker">Matter List</span>
            <h2>项目入口</h2>
          </div>
          <p>把状态、有效期和进入动作收在同一张列表里，减少来回确认。</p>
        </div>
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
                      <span>点击进入项目详情</span>
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

.matter-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  display: grid;
  gap: 6px;
  padding: 18px 20px;
  border-radius: 8px;
  background: rgba(252, 251, 248, 0.82);
  border: 1px solid rgba(15, 23, 42, 0.05);
  box-shadow: var(--shadow-xs);
}

.summary-label {
  color: var(--text-tertiary);
  font-size: 12px;
}

.summary-card strong {
  color: var(--text-primary);
  font-size: 22px;
  line-height: 1.2;
}

.summary-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.list-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.list-head h2 {
  margin: 6px 0 0;
  color: var(--text-primary);
  font-size: 22px;
}

.list-head p {
  margin: 0;
  max-width: 320px;
  color: var(--text-secondary);
  line-height: 1.75;
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
  background: #fafafa;
}

.skeleton-list {
  display: grid;
  gap: 12px;
}

.skeleton-item {
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid var(--border-color-light);
}

@media (max-width: 768px) {
  .content {
    gap: 16px;
  }

  .stats-grid,
  .matter-summary,
  .list-head {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
