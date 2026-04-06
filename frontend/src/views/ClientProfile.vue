<template>
  <div class="profile-container">
    <AppHeader
      variant="portal"
      title="个人中心"
    />
    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="profile-hero section-shell">
        <div class="profile-header">
          <a-avatar
            :size="84"
            class="profile-avatar"
          >
            <template #icon>
              <UserOutlined />
            </template>
          </a-avatar>
          <div class="profile-info">
            <div class="eyebrow">
              Client Identity
            </div>
            <h1>{{ clientName || '当前访客' }}</h1>
            <p>{{ clientIdText }}</p>
          </div>
          <div class="profile-badge">
            <span>当前状态</span>
            <strong>{{ clientName ? '已同步访客信息' : '访客模式' }}</strong>
          </div>
        </div>
      </section>

      <section class="profile-summary section-shell">
        <article class="summary-card">
          <span class="summary-label">最近访问</span>
          <strong>{{ portalVisitorStore.profile.lastMatterId || '-' }}</strong>
          <p>最近同步的项目编号。</p>
        </article>
        <article class="summary-card">
          <span class="summary-label">材料入口</span>
          <strong>{{ portalVisitorStore.profile.lastMatterToken ? '已具备' : '待进入项目' }}</strong>
          <p>是否已具备最近项目的访问上下文。</p>
        </article>
      </section>

      <section class="table-panel section-shell">
        <div class="action-panel-head">
          <div>
            <span class="panel-kicker">Quick Access</span>
            <h2>常用入口</h2>
          </div>
          <p>从这里继续进入文件、通知与帮助，不需要重新寻找主入口。</p>
        </div>
        <a-list>
          <a-list-item @click="router.push('/files')">
            <template #actions>
              <ArrowRightOutlined />
            </template>
            <a-list-item-meta>
              <template #title>
                文件中心
              </template>
              <template #description>
                查看最近访问项目的材料并进行预览或下载
              </template>
            </a-list-item-meta>
          </a-list-item>
          <a-list-item @click="router.push('/notifications')">
            <template #actions>
              <ArrowRightOutlined />
            </template>
            <a-list-item-meta>
              <template #title>
                消息通知
              </template>
              <template #description>
                查看最近提醒、动态和系统通知
              </template>
            </a-list-item-meta>
          </a-list-item>
          <a-list-item @click="router.push('/help')">
            <template #actions>
              <ArrowRightOutlined />
            </template>
            <a-list-item-meta>
              <template #title>
                帮助中心
              </template>
              <template #description>
                查看常见问题、使用说明和协助入口
              </template>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </section>
    </a-layout-content>
    <MobileBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, ArrowRightOutlined } from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { usePortalVisitorStore } from '@/stores/portalVisitor'

const router = useRouter()
const portalVisitorStore = usePortalVisitorStore()

const clientName = computed(() => portalVisitorStore.displayName)
const clientIdText = computed(() => {
  if (portalVisitorStore.displayId !== null && portalVisitorStore.displayId !== undefined) {
    return `客户编号: ${portalVisitorStore.displayId}`
  }
  return '暂未同步客户编号'
})
</script>

<style scoped>
.content {
  display: grid;
  gap: 20px;
}

.profile-hero {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color-light);
  background: var(--lex-surface);
  box-shadow: var(--shadow-sm);
}

.profile-header {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.profile-avatar {
  background: rgba(0, 33, 64, 0.08);
  color: var(--primary-color);
  border: 1px solid rgba(0, 33, 64, 0.12);
  flex-shrink: 0;
}

.profile-info {
  min-width: 0;
}

.profile-info h1 {
  margin: 8px 0 4px;
  color: var(--text-primary);
  font-size: 22px;
  line-height: 1.3;
  word-break: break-word;
}

.profile-info p {
  margin: 0;
  color: var(--text-secondary);
}

.profile-badge {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 8px;
  background: rgba(248, 244, 237, 0.72);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.profile-badge span {
  color: var(--text-tertiary);
  font-size: 12px;
}

.profile-badge strong {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.6;
}

.profile-summary {
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

.action-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.action-panel-head h2 {
  margin: 6px 0 0;
  color: var(--text-primary);
  font-size: 22px;
}

.action-panel-head p {
  margin: 0;
  max-width: 320px;
  color: var(--text-secondary);
  line-height: 1.75;
}

:deep(.ant-list-item) {
  cursor: pointer;
}

:deep(.ant-list-item:hover) {
  background: #fafafa;
}

@media (max-width: 768px) {
  .profile-hero {
    padding: 16px;
  }

  .profile-header,
  .profile-summary,
  .action-panel-head {
    grid-template-columns: 1fr;
    display: grid;
    align-items: flex-start;
  }

  .profile-badge {
    width: 100%;
  }
}
</style>
