<template>
  <div class="admin-profile">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_PROFILE_TEXTS.intro }}
        </p>
      </div>
      <a-space>
        <a-button @click="router.back()">
          {{ UI_TEXTS.back }}
        </a-button>
        <a-button
          type="primary"
          @click="handleLogout"
        >
          <template #icon>
            <LogoutOutlined />
          </template>
          {{ UI_TEXTS.logout }}
        </a-button>
      </a-space>
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">{{ ADMIN_PROFILE_TEXTS.stats.identity }}</span>
        <strong>{{ userInfo.realName || userInfo.username || ADMIN_PROFILE_TEXTS.stats.adminFallback }}</strong>
      </div>
      <div class="stats-card success">
        <span class="stats-label">{{ ADMIN_PROFILE_TEXTS.stats.username }}</span>
        <strong>{{ userInfo.username || '-' }}</strong>
      </div>
      <div class="stats-card info">
        <span class="stats-label">{{ ADMIN_PROFILE_TEXTS.stats.email }}</span>
        <strong>{{ userInfo.email ? userInfo.email : ADMIN_PROFILE_TEXTS.stats.unbound }}</strong>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">{{ ADMIN_PROFILE_TEXTS.stats.password }}</span>
        <strong>{{ ADMIN_PROFILE_TEXTS.stats.passwordAdvice }}</strong>
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
            <h3>{{ userInfo.realName || userInfo.username || ADMIN_PROFILE_TEXTS.stats.adminFallback }}</h3>
            <p>{{ ADMIN_PROFILE_TEXTS.identity.roleDesc }}</p>
          </div>
        </div>

        <div class="meta-list">
          <div class="meta-item">
            <span>{{ ADMIN_PROFILE_TEXTS.identity.username }}</span>
            <strong>{{ userInfo.username || '-' }}</strong>
          </div>
          <div class="meta-item">
            <span>{{ ADMIN_PROFILE_TEXTS.identity.realName }}</span>
            <strong>{{ userInfo.realName || '-' }}</strong>
          </div>
          <div class="meta-item">
            <span>{{ ADMIN_PROFILE_TEXTS.identity.email }}</span>
            <strong>{{ userInfo.email || '-' }}</strong>
          </div>
          <div class="meta-item">
            <span>{{ ADMIN_PROFILE_TEXTS.identity.lastLoginAt }}</span>
            <strong>{{ userInfo.lastLoginAt ? formatDate(userInfo.lastLoginAt) : '-' }}</strong>
          </div>
        </div>
      </article>

      <article class="profile-card security-card">
        <div class="section-header">
          <div>
            <h3>{{ ADMIN_PROFILE_TEXTS.security.title }}</h3>
          </div>
          <div class="section-note">
            {{ ADMIN_PROFILE_TEXTS.security.note }}
          </div>
        </div>

        <a-tabs v-model:active-key="activeTab">
          <a-tab-pane
            key="password"
            :tab="ADMIN_PROFILE_TEXTS.security.passwordTab"
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
                :label="ADMIN_PROFILE_TEXTS.security.oldPasswordLabel"
                name="oldPassword"
              >
                <a-input-password
                  v-model:value="passwordForm.oldPassword"
                  :placeholder="ADMIN_PROFILE_TEXTS.security.oldPasswordPlaceholder"
                >
                  <template #prefix>
                    <LockOutlined />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item
                :label="ADMIN_PROFILE_TEXTS.security.newPasswordLabel"
                name="newPassword"
              >
                <a-input-password
                  v-model:value="passwordForm.newPassword"
                  :placeholder="ADMIN_PROFILE_TEXTS.security.newPasswordPlaceholder"
                >
                  <template #prefix>
                    <KeyOutlined />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item
                :label="ADMIN_PROFILE_TEXTS.security.confirmPasswordLabel"
                name="confirmPassword"
              >
                <a-input-password
                  v-model:value="passwordForm.confirmPassword"
                  :placeholder="ADMIN_PROFILE_TEXTS.security.confirmPasswordPlaceholder"
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
                  {{ ADMIN_PROFILE_TEXTS.security.submit }}
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
import { UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_PROFILE_TEXTS } from '@/constants/adminTexts'

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
    return Promise.reject(ADMIN_PROFILE_TEXTS.validation.confirmRequired)
  }
  if (value !== passwordForm.newPassword) {
    return Promise.reject(ADMIN_PROFILE_TEXTS.validation.confirmMismatch)
  }
  return Promise.resolve()
}

const passwordRules: Record<string, Rule[]> = {
  oldPassword: [
    { required: true, message: ADMIN_PROFILE_TEXTS.validation.oldPasswordRequired, trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: ADMIN_PROFILE_TEXTS.validation.newPasswordRequired, trigger: 'blur' },
    { min: 8, message: ADMIN_PROFILE_TEXTS.validation.passwordMinLength, trigger: 'blur' },
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

    message.success(UI_FEEDBACK_TEXTS.profilePasswordUpdated)
    authStore.logout()
    router.push('/admin/login')
  } catch (error: unknown) {
    logger.error('修改密码失败', error)
    message.error(error instanceof Error ? error.message : UI_FEEDBACK_TEXTS.operationFailed)
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

.guide-card,
.profile-card {
  background: var(--lex-surface-strong);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-sm);
}

.guide-card,
.profile-card {
  padding: 20px;
}

.guide-card {
  display: grid;
  gap: 18px;
}

.guide-points {
  display: grid;
  gap: 12px;
}

.guide-point {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px 14px;
  border-radius: 8px;
  background: rgba(0, 9, 24, 0.03);
  border: 1px solid var(--border-color-light);
}

.guide-point span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: var(--lex-accent-soft);
  border: 1px solid color-mix(in srgb, var(--lex-accent) 28%, var(--lex-outline));
  color: var(--accent-color-deep);
  font-size: 12px;
}

.guide-point strong {
  color: var(--text-primary);
  line-height: 1.7;
}

.security-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.9;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
  gap: 18px;
}

.identity-card {
  display: grid;
  gap: 16px;
}

.identity-hero {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.profile-avatar {
  flex-shrink: 0;
  color: var(--lex-primary-soft);
  background: rgba(0, 33, 64, 0.08);
  border: 1px solid rgba(0, 33, 64, 0.12);
}

.identity-kicker,
.section-kicker {
  margin-bottom: 6px;
  color: var(--text-tertiary);
  font-size: 12px;
}

.identity-copy h3,
.section-header h3 {
  margin: 0;
  font-size: 20px;
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
  padding: 12px 14px;
  border-radius: 8px;
  background: rgba(0, 9, 24, 0.03);
  border: 1px solid var(--border-color-light);
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
  align-items: flex-start;
}

.password-form {
  max-width: 520px;
  padding-top: 8px;
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
    padding: 16px;
  }

  .meta-item {
    flex-direction: column;
  }

  .meta-item strong {
    text-align: left;
  }
}
</style>
