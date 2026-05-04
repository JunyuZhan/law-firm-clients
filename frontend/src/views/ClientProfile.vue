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
      <section class="section-shell portal-panel profile-hero">
        <header>
          <hgroup>
            <p class="portal-kicker">
              个人中心
            </p>
            <h2 class="portal-heading text-balance">
              查看当前访客识别信息与常用入口
            </h2>
          </hgroup>
          <p class="intro-text text-balance">
            个人中心不承接公开注册，仅显示当前识别到的访客信息，以及继续进入事项协作所需的常用入口。
          </p>
        </header>
        <div class="profile-header">
          <a-avatar
            :size="84"
            class="profile-avatar"
          >
            <template #icon>
              <UserOutlined aria-hidden="true" />
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

      <section class="section-shell portal-panel">
        <div class="section-head">
          <h3 class="panel-title">
            快捷入口
          </h3>
          <p>从这里继续进入文件、消息和帮助信息，不切换到无关页面。</p>
        </div>
        <van-cell-group
          inset
          class="profile-cell-group"
        >
          <van-cell
            title="文件中心"
            label="预览、下载项目文件"
            is-link
            center
            @click="router.push('/files')"
          >
            <template #icon>
              <span class="portal-cell-icon">档</span>
            </template>
          </van-cell>
          <van-cell
            title="消息通知"
            label="系统提醒与动态"
            is-link
            center
            @click="router.push('/notifications')"
          >
            <template #icon>
              <span class="portal-cell-icon">讯</span>
            </template>
          </van-cell>
          <van-cell
            title="帮助中心"
            label="常见问题与访问说明"
            is-link
            center
            @click="router.push('/help')"
          >
            <template #icon>
              <span class="portal-cell-icon">助</span>
            </template>
          </van-cell>
        </van-cell-group>
      </section>
    </a-layout-content>
    <MobileBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined } from '@ant-design/icons-vue'
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
  padding: 14px 16px;
  border-radius: 18px;
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
  margin: 0;
  font-size: 18px;
  color: var(--lex-primary);
}

.section-head {
  display: grid;
  gap: 8px;
}

.section-head p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.profile-cell-group {
  border-radius: 20px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 28px rgba(16, 42, 67, 0.08);
}

.portal-cell-icon {
  display: inline-grid;
  place-items: center;
  width: 28px;
  height: 28px;
  margin-right: 12px;
  border-radius: 10px;
  background: rgba(27, 59, 95, 0.1);
  color: var(--lex-primary);
  font-size: 13px;
  font-weight: 700;
}

.profile-cell-group :deep(.van-cell) {
  padding: 16px;
  background: transparent;
}

.profile-cell-group :deep(.van-cell__title) {
  font-weight: 600;
  color: var(--text-primary);
}

.profile-cell-group :deep(.van-cell__label) {
  margin-top: 4px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .portal-panel {
    padding: 18px;
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
