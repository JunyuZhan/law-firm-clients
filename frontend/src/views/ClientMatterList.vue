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
      <section class="section-shell portal-panel portal-panel--intro">
        <div>
          <span class="portal-kicker">事项列表</span>
          <h2 class="portal-heading">
            查看当前可访问的事项入口
          </h2>
          <p class="intro-text">
            当前页面仅列出已授权访问的事项。进入详情后可继续查看进展、文件、期限与后续协作动作。
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

      <section class="section-shell portal-panel">
        <div
          v-if="hasStoredContext || routeToken"
          class="section-actions"
        >
          <span class="section-actions__hint">
            {{ activeTokenSourceText }}
          </span>
          <a-button
            type="default"
            size="small"
            @click="loadMatters"
          >
            刷新列表
          </a-button>
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
          <PortalStatePanel
            v-else-if="errorState === 'missing-token'"
            title="暂无可用访问上下文"
            description="请先通过律师发送的专属链接进入一个事项，再查看您的事项列表。"
          >
            <template #icon>
              <FileTextOutlined />
            </template>
            <template #actions>
              <a-button
                type="primary"
                @click="router.push('/portal')"
              >
                返回首页
              </a-button>
            </template>
          </PortalStatePanel>
          <PortalStatePanel
            v-else-if="errorState === 'invalid-token'"
            title="访问链接已失效"
            description="当前保存的访问凭据不可用，可能已过期或被撤销。请重新通过律师发送的专属链接进入事项。"
          >
            <template #icon>
              <FileTextOutlined />
            </template>
            <template #actions>
              <a-button
                type="primary"
                @click="router.push('/portal')"
              >
                返回首页
              </a-button>
              <a-button @click="clearStoredContext">
                清除本地记录
              </a-button>
            </template>
          </PortalStatePanel>
          <PortalStatePanel
            v-else-if="matters.length === 0"
            title="暂无项目"
            description="请联系律所为您开通项目访问权限。"
          >
            <template #icon>
              <FileTextOutlined />
            </template>
          </PortalStatePanel>
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
                  <p class="matter-card__eyebrow">
                    案件编号 {{ item.id }}
                  </p>
                  <h3 class="matter-card__title">
                    {{ item.matterName }}
                  </h3>
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
              <p class="matter-card__hint">
                点击查看事项概览并进入协作详情。
              </p>
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
            <p class="matter-sheet__eyebrow">
              案件事务概览
            </p>
            <h2>{{ selectedMatter.matterName }}</h2>
          </div>
          <van-tag
            plain
            :type="getStatusTone(selectedMatter.status)"
          >
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { FileTextOutlined } from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import PortalStatePanel from '@/components/PortalStatePanel.vue'
import { getPortalMatterList, type PortalMatterListItem } from '@/api/matter'
import { usePortalAccessContext } from '@/composables/usePortalAccessContext'
import {
  getPortalMatterStatusText,
  getPortalMatterStatusTone,
  resolvePortalAccessErrorState,
  type PortalAccessErrorState,
} from '@/utils/portalState'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const matters = ref<PortalMatterListItem[]>([])
const detailOpen = ref(false)
const selectedMatter = ref<PortalMatterListItem | null>(null)
const errorState = ref<PortalAccessErrorState>('none')
const { token, hasStoredContext, sourceText, clearAccessContext } = usePortalAccessContext()

const activeCount = computed(() => matters.value.filter(item => item.status === 'ACTIVE').length)
const routeToken = computed(() => (route.query.token as string) || '')
const activeTokenSourceText = computed(() => sourceText.value)

const getStatusName = getPortalMatterStatusText
const getStatusTone = getPortalMatterStatusTone

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  return dateStr.split('T')[0]
}

async function loadMatters() {
  const accessToken = token.value
  if (!accessToken) {
    matters.value = []
    errorState.value = 'missing-token'
    return
  }

  loading.value = true
  errorState.value = 'none'
  try {
    const res = await getPortalMatterList(accessToken, 20)
    matters.value = res.data || []
  } catch (error) {
    matters.value = []
    errorState.value = resolvePortalAccessErrorState(error)
  } finally {
    loading.value = false
  }
}

function clearStoredContext() {
  clearAccessContext()
  errorState.value = 'missing-token'
}

function goToMatter(item: PortalMatterListItem) {
  detailOpen.value = false
  router.push(`/matter/${item.id}?token=${item.accessToken}`)
}

function openMatterPanel(item: PortalMatterListItem) {
  selectedMatter.value = item
  detailOpen.value = true
}

onMounted(() => {
  loadMatters()
})

watch(
  () => route.query.token,
  () => {
    loadMatters()
  },
)
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
}

.matter-card-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}

.matter-card {
  padding: 20px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
}

.matter-card:hover {
  border-color: #1677ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.matter-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.matter-card__eyebrow {
  margin: 0 0 4px;
  font-size: 12px;
  color: #6b7280;
}

.matter-card__title {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
}

.matter-card__meta {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #4b5563;
}

.matter-card__hint {
  margin: 12px 0 0;
  color: #9ca3af;
  font-size: 12px;
}

.matter-sheet {
  padding: 16px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.matter-sheet__handle {
  width: 40px;
  height: 4px;
  border-radius: 2px;
  background: #d9d9d9;
  margin: 0 auto;
}

.matter-sheet__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.matter-sheet__eyebrow {
  margin: 0 0 4px;
  font-size: 12px;
  color: #6b7280;
}

.matter-sheet__header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.matter-sheet__panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.matter-sheet__row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
  color: #4b5563;
}

.matter-sheet__row strong {
  color: #1f2937;
  font-weight: 500;
}

.matter-sheet__button {
  height: 40px;
  border-radius: 6px;
}

@media (max-width: 768px) {
  .content {
    padding: 16px;
  }
  .matter-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
