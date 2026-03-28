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
            <p>客户编号: {{ clientId }}</p>
          </div>
        </div>
        
        <a-divider />
        
        <a-list>
          <a-list-item>
            <template #actions>
              <ArrowRightOutlined />
            </template>
            <a-list-item-meta>
              <template #title>
                我的项目
              </template>
              <template #description>
                查看所有项目
              </template>
            </a-list-item-meta>
          </a-list-item>
          <a-list-item>
            <template #actions>
              <ArrowRightOutlined />
            </template>
            <a-list-item-meta>
              <template #title>
                消息通知
              </template>
              <template #description>
                查看消息记录
              </template>
            </a-list-item-meta>
          </a-list-item>
          <a-list-item>
            <template #actions>
              <ArrowRightOutlined />
            </template>
            <a-list-item-meta>
              <template #title>
                帮助中心
              </template>
              <template #description>
                常见问题解答
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
import { ref } from 'vue'
import { UserOutlined, ArrowRightOutlined } from '@ant-design/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'

const clientName = ref('客户')
const clientId = ref('-')
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

@media (max-width: 768px) {
  .profile-container {
    padding-bottom: 70px;
  }
}
</style>
