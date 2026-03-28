<template>
  <div class="admin-profile">
    <a-page-header
      title="个人中心"
      sub-title="管理个人信息和安全设置"
      @back="() => router.back()"
    >
      <template #extra>
        <a-button
          type="primary"
          @click="handleLogout"
        >
          <template #icon>
            <LogoutOutlined />
          </template>
          退出登录
        </a-button>
      </template>
    </a-page-header>

    <div class="profile-content">
      <a-row :gutter="24">
        <!-- 左侧：个人信息卡片 -->
        <a-col
          :xs="24"
          :lg="8"
        >
          <a-card
            :bordered="false"
            class="profile-card"
          >
            <div class="profile-header">
              <a-avatar
                :size="80"
                class="profile-avatar"
              >
                <template #icon>
                  <UserOutlined />
                </template>
              </a-avatar>
              <h2 class="profile-name">
                {{ userInfo.realName || userInfo.username }}
              </h2>
              <p class="profile-role">
                管理员
              </p>
            </div>
            
            <a-descriptions
              :column="1"
              layout="horizontal"
              class="profile-details"
            >
              <a-descriptions-item label="用户名">
                {{ userInfo.username || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="真实姓名">
                {{ userInfo.realName || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="邮箱">
                {{ userInfo.email || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="最后登录">
                {{ userInfo.lastLoginAt ? formatDate(userInfo.lastLoginAt) : '-' }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <!-- 右侧：修改密码 -->
        <a-col
          :xs="24"
          :lg="16"
        >
          <a-card
            title="安全设置"
            :bordered="false"
          >
            <a-tabs v-model:active-key="activeTab">
              <a-tab-pane
                key="password"
                tab="修改密码"
              >
                <a-form
                  ref="passwordFormRef"
                  :model="passwordForm"
                  :rules="passwordRules"
                  layout="vertical"
                  class="password-form"
                  @finish="handlePasswordChange"
                >
                  <a-form-item
                    label="当前密码"
                    name="oldPassword"
                  >
                    <a-input-password 
                      v-model:value="passwordForm.oldPassword" 
                      placeholder="请输入当前使用的密码"
                    >
                      <template #prefix>
                        <LockOutlined />
                      </template>
                    </a-input-password>
                  </a-form-item>

                  <a-form-item
                    label="新密码"
                    name="newPassword"
                  >
                    <a-input-password 
                      v-model:value="passwordForm.newPassword" 
                      placeholder="请输入新密码（至少8位）"
                    >
                      <template #prefix>
                        <KeyOutlined />
                      </template>
                    </a-input-password>
                  </a-form-item>

                  <a-form-item
                    label="确认新密码"
                    name="confirmPassword"
                  >
                    <a-input-password 
                      v-model:value="passwordForm.confirmPassword" 
                      placeholder="请再次输入新密码"
                    >
                      <template #prefix>
                        <CheckOutlined />
                      </template>
                    </a-input-password>
                  </a-form-item>

                  <a-form-item>
                    <a-button
                      type="primary"
                      html-type="submit"
                      :loading="changingPassword"
                    >
                      修改密码
                    </a-button>
                  </a-form-item>
                </a-form>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  UserOutlined, 
  LogoutOutlined, 
  LockOutlined, 
  KeyOutlined, 
  CheckOutlined 
} from '@ant-design/icons-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { useAuthStore } from '@/stores/auth'
import { changePassword } from '@/api/auth'
import type { UserInfo } from '@/api/auth'
import { formatDate } from '@/utils/date'
import logger from '@/utils/logger'

const router = useRouter()
const authStore = useAuthStore()

const userInfo = computed<Partial<UserInfo>>(() => authStore.user || {})
const activeTab = ref('password')
const passwordFormRef = ref<FormInstance>()
const changingPassword = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = async (_rule: Rule, value: string) => {
  if (value === '') {
    return Promise.reject('请再次输入新密码')
  }
  if (value !== passwordForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const passwordRules: Record<string, Rule[]> = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少为8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handlePasswordChange = async () => {
  changingPassword.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    message.success('密码修改成功，请重新登录')
    authStore.logout()
    router.push('/admin/login')
  } catch (error: any) {
    logger.error('修改密码失败', error)
    message.error(error.message || '修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-profile {
  padding: 24px;
}

.profile-content {
  margin-top: 24px;
}

.profile-card {
  text-align: center;
  margin-bottom: 24px;
}

.profile-header {
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 24px;
}

.profile-avatar {
  background-color: #1890ff;
  margin-bottom: 16px;
}

.profile-name {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

.profile-role {
  margin: 0;
  color: rgba(0, 0, 0, 0.45);
}

.profile-details :deep(.ant-descriptions-item-label) {
  color: rgba(0, 0, 0, 0.45);
  width: 100px;
}

.profile-details :deep(.ant-descriptions-item-content) {
  color: rgba(0, 0, 0, 0.85);
}

.password-form {
  max-width: 400px;
  padding: 24px 0;
}
</style>
