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
            <h1 class="profile-name">
              {{ clientName || '访客' }}
            </h1>
            <p class="profile-id">
              {{ clientIdText }}
            </p>
          </div>
          <div class="profile-badge">
            <span>状态</span>
            <strong>{{ clientName ? '已识别' : '未登录' }}</strong>
          </div>
        </div>
      </section>

      <section class="table-panel section-shell">
        <h2 class="panel-title">
          快捷入口
        </h2>
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
                预览、下载项目文件
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
                系统提醒与动态
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
                常见问题与访问说明
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
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
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
  background: rgba(30, 64, 175, 0.08);
  color: var(--lex-primary-soft);
  border: 1px solid rgba(30, 64, 175, 0.15);
  flex-shrink: 0;
}

.profile-info {
  min-width: 0;
}

.profile-name {
  margin: 0 0 4px;
  color: var(--lex-primary);
  font-size: 22px;
  line-height: 1.3;
  word-break: break-word;
}

.profile-id {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.profile-badge {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  background: var(--lex-bg-muted);
  border: 1px solid var(--border-color-light);
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

.panel-title {
  margin: 0 0 12px;
  font-size: 18px;
  color: var(--lex-primary);
}

:deep(.ant-list-item) {
  cursor: pointer;
}

:deep(.ant-list-item:hover) {
  background: var(--lex-bg-muted);
}

@media (max-width: 768px) {
  .profile-hero {
    padding: 16px;
  }

  .profile-header {
    grid-template-columns: 1fr;
    display: grid;
    align-items: flex-start;
  }

  .profile-badge {
    width: 100%;
  }
}
</style>
