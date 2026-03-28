<template>
  <div class="admin-profile">
    <section class="page-intro">
      <div>
        <div class="eyebrow">
          Profile & Security
        </div>
        <h2 class="editorial-title intro-title">
          个人中心
        </h2>
        <p class="intro-text">
          集中查看管理员资料，并在统一的安全面板中完成密码更新与会话退出。
        </p>
      </div>
      <a-space>
        <a-button @click="router.back()">
          返回上一页
        </a-button>
        <a-button
          type="primary"
          @click="handleLogout"
        >
          <template #icon>
            <LogoutOutlined />
          </template>
          退出登录
        </a-button>
      </a-space>
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">账户身份</span>
        <strong>{{ userInfo.realName || userInfo.username || '管理员' }}</strong>
        <p>当前登录管理员的展示名称。</p>
      </div>
      <div class="stats-card success">
        <span class="stats-label">用户名</span>
        <strong>{{ userInfo.username || '-' }}</strong>
        <p>用于后台登录与审计日志识别。</p>
      </div>
      <div class="stats-card info">
        <span class="stats-label">邮箱状态</span>
        <strong>{{ userInfo.email ? '已绑定' : '未绑定' }}</strong>
        <p>{{ userInfo.email || '建议补充邮箱用于通知和审计' }}</p>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">密码策略</span>
        <strong>定期轮换</strong>
        <p>避免重复使用旧密码，降低账户风险。</p>
      </div>
    </section>

    <section class="profile-grid">
      <article class="profile-card identity-card">
        <div class="identity-hero">
          <a-avatar
            :size="88"
            class="profile-avatar"
          >
            <template #icon>
              <UserOutlined />
            </template>
          </a-avatar>
          <div class="identity-copy">
            <div class="identity-kicker">
              Administrator
            </div>
            <h3>{{ userInfo.realName || userInfo.username || '管理员' }}</h3>
            <p>账号用于管理项目、通知、文件和系统配置。</p>
          </div>
        </div>

        <div class="meta-list">
          <div class="meta-item">
            <span>用户名</span>
            <strong>{{ userInfo.username || '-' }}</strong>
          </div>
          <div class="meta-item">
            <span>真实姓名</span>
            <strong>{{ userInfo.realName || '-' }}</strong>
          </div>
          <div class="meta-item">
            <span>邮箱</span>
            <strong>{{ userInfo.email || '-' }}</strong>
          </div>
          <div class="meta-item">
            <span>最后登录</span>
            <strong>{{ userInfo.lastLoginAt ? formatDate(userInfo.lastLoginAt) : '-' }}</strong>
          </div>
        </div>
      </article>

      <article class="profile-card security-card">
        <div class="section-header">
          <div>
            <div class="section-kicker">
              Security
            </div>
            <h3>安全设置</h3>
          </div>
          <div class="section-note">
            建议定期轮换密码并避免重复使用旧密码。
          </div>
        </div>

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

              <div class="password-actions">
                <a-button
                  type="primary"
                  html-type="submit"
                  :loading="changingPassword"
                >
                  修改密码
                </a-button>
              </div>
            </a-form>
          </a-tab-pane>
        </a-tabs>
      </article>
    </section>
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
  CheckOutlined,
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
  confirmPassword: '',
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
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少为8位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handlePasswordChange = async () => {
  changingPassword.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })

    message.success('密码修改成功，请重新登录')
    authStore.logout()
    router.push('/admin/login')
  } catch (error: unknown) {
    logger.error('修改密码失败', error)
    message.error(error instanceof Error ? error.message : '修改密码失败')
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
  display: grid;
  gap: 18px;
}

.profile-card {
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--border-color);
  border-radius: 24px;
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(12px);
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 18px;
}

.profile-card {
  padding: 24px;
}

.identity-card {
  display: grid;
  gap: 24px;
}

.identity-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(21, 33, 46, 0.08);
}

.profile-avatar {
  flex-shrink: 0;
  color: #fff;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-color-light));
  box-shadow: var(--shadow-sm);
}

.identity-kicker,
.section-kicker {
  margin-bottom: 8px;
  color: var(--text-tertiary);
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.identity-copy h3,
.section-header h3 {
  margin: 0;
  font-size: 28px;
}

.identity-copy p,
.section-note {
  margin-top: 8px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.meta-list {
  display: grid;
  gap: 12px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(21, 33, 46, 0.08);
}

.meta-item span {
  color: var(--text-secondary);
}

.meta-item strong {
  color: var(--text-primary);
  font-weight: 600;
  text-align: right;
}

.security-card {
  display: grid;
  gap: 18px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
}

.password-form {
  max-width: 520px;
  padding-top: 12px;
}

.password-actions {
  padding-top: 8px;
}

.security-card :deep(.ant-tabs-nav::before) {
  border-bottom-color: rgba(21, 33, 46, 0.08);
}

@media (max-width: 960px) {
  .section-header,
  .identity-hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .profile-card {
    padding: 20px;
  }

  .meta-item {
    flex-direction: column;
  }

  .meta-item strong {
    text-align: left;
  }
}
</style>
