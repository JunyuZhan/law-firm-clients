<template>
  <div class="profile-container">
    <AppHeader
      variant="portal"
      title="个人中心"
    />
    <a-layout-content class="content">
      <a-card>
        <div class="profile-header">
          <a-avatar
            :size="80"
            class="profile-avatar"
          >
            <template #icon>
              <UserOutlined />
            </template>
          </a-avatar>
          <div class="profile-info">
            <h2>{{ clientName }}</h2>
            <p>{{ clientIdText }}</p>
          </div>
        </div>
        
        <a-divider />
        
        <a-list>
          <a-list-item @click="router.push('/matters')">
            <template #actions>
              <ArrowRightOutlined />
            </template>
            <a-list-item-meta>
              <template #title>
                我的项目
              </template>
              <template #description>
                查看我的项目与访问状态
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
                查看消息通知与最近动态
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
                查看常见问题与使用说明
              </template>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </a-card>
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
.profile-container {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.content {
  padding: 16px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
}

.profile-avatar {
  background: var(--primary-color);
}

.profile-info h2 {
  margin: 0;
  font-size: 18px;
}

.profile-info p {
  margin: 4px 0 0;
  color: #999;
  font-size: 14px;
}

:deep(.ant-list-item) {
  cursor: pointer;
}

@media (max-width: 768px) {
  .profile-container {
    padding-bottom: 70px;
  }
}
</style>
