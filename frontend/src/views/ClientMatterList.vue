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
            以移动端卡片方式展示案件，点击即可展开案件事务概览。
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

      <section class="portal-card-list section-shell">
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
          <div
            v-else
            class="matter-card-grid"
          >
            <article
              v-for="item in matters"
              :key="item.id"
              class="matter-card"
              @click="openMatterPanel(item)"
            >
              <div class="matter-card__head">
                <div>
                  <p class="matter-card__eyebrow">案件编号 {{ item.id }}</p>
                  <h3 class="matter-card__title">{{ item.matterName }}</h3>
                </div>
                <van-tag
                  plain
                  :type="getStatusTone(item.status)"
                >
                  {{ getStatusName(item.status) }}
                </van-tag>
              </div>
              <div class="matter-card__meta">
                <span>承办人 {{ item.counsel || '待同步' }}</span>
                <span>有效期至 {{ formatDate(item.expiresAt) }}</span>
              </div>
            </article>
          </div>
        </a-spin>
      </section>
    </a-layout-content>
    <MobileBottomNav />

    <van-popup
      v-model:show="detailOpen"
      round
      position="bottom"
      :style="{ height: '85vh' }"
      class="matter-detail-popup"
    >
      <div
        v-if="selectedMatter"
        class="matter-sheet"
      >
        <div class="matter-sheet__handle" />
        <div class="matter-sheet__header">
          <div>
            <p class="matter-sheet__eyebrow">案件事务概览</p>
            <h2>{{ selectedMatter.matterName }}</h2>
          </div>
          <van-tag plain :type="getStatusTone(selectedMatter.status)">
            {{ getStatusName(selectedMatter.status) }}
          </van-tag>
        </div>
        <div class="matter-sheet__panel">
          <div class="matter-sheet__row">
            <span>案件编号</span>
            <strong>{{ selectedMatter.id }}</strong>
          </div>
          <div class="matter-sheet__row">
            <span>承办律师</span>
            <strong>{{ selectedMatter.counsel || '待同步' }}</strong>
          </div>
          <div class="matter-sheet__row">
            <span>访问有效期</span>
            <strong>{{ formatDate(selectedMatter.expiresAt) }}</strong>
          </div>
        </div>
        <van-button
          type="primary"
          block
          class="matter-sheet__button"
          @click="goToMatter(selectedMatter)"
        >
          进入案件动态
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { FileTextOutlined } from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'

interface MatterItem {
  id: string
  matterName: string
  status: string
  expiresAt: string
  accessToken: string
  counsel?: string
}

const router = useRouter()
const loading = ref(false)
const matters = ref<MatterItem[]>([])
const detailOpen = ref(false)
const selectedMatter = ref<MatterItem | null>(null)

const activeCount = computed(() => matters.value.filter(item => item.status === 'ACTIVE').length)

function getStatusName(status: string): string {
  const names: Record<string, string> = {
    ACTIVE: '有效',
    EXPIRED: '已过期',
    REVOKED: '已撤销',
  }
  return names[status] || status
}

function getStatusTone(status: string): 'primary' | 'success' | 'warning' | 'danger' {
  const tones: Record<string, 'primary' | 'success' | 'warning' | 'danger'> = {
    ACTIVE: 'success',
    EXPIRED: 'warning',
    REVOKED: 'danger',
  }
  return tones[status] || 'primary'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  return dateStr.split('T')[0]
}

function goToMatter(item: MatterItem) {
  detailOpen.value = false
  router.push(`/matter/${item.id}?token=${item.accessToken}`)
}

function openMatterPanel(item: MatterItem) {
  selectedMatter.value = item
  detailOpen.value = true
}
</script>

<style scoped>
.content {
  display: grid;
  gap: 20px;
}

.portal-card-list {
  display: grid;
  gap: 12px;
}

.matter-card-grid {
  display: grid;
  gap: 12px;
}

.matter-card {
  padding: 18px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(217, 226, 236, 0.92);
  box-shadow: 0 10px 24px rgba(16, 42, 67, 0.08);
}

.matter-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.matter-card__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.08em;
  color: var(--text-tertiary);
}

.matter-card__title {
  margin: 0;
  color: var(--lex-primary);
  font-size: 18px;
}

.matter-card__meta {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--text-secondary);
}

.matter-sheet {
  padding: 16px 16px 24px;
  display: grid;
  gap: 20px;
}

.matter-sheet__handle {
  width: 44px;
  height: 4px;
  border-radius: 999px;
  background: rgba(98, 125, 152, 0.36);
  margin: 0 auto;
}

.matter-sheet__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.matter-sheet__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  color: var(--text-tertiary);
  letter-spacing: 0.08em;
}

.matter-sheet__header h2 {
  margin: 0;
  color: var(--lex-primary);
  font-size: 22px;
}

.matter-sheet__panel {
  display: grid;
  gap: 12px;
  padding: 18px 16px;
  border-radius: 14px;
  background: var(--lex-bg-muted);
}

.matter-sheet__row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-secondary);
}

.matter-sheet__row strong {
  color: var(--text-primary);
}

.matter-sheet__button {
  height: 44px;
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
