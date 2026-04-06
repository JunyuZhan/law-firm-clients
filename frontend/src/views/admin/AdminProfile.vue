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

    <section class="spotlight-grid dashboard-spotlight-grid">
      <article class="spotlight-card dashboard-spotlight-card">
        <span class="section-kicker">Account Desk</span>
        <h3>先确认身份信息，再做密码轮换</h3>
        <p>个人中心的重点是确认账号身份、邮箱可用性和安全动作是否就绪，而不是在这里承载更多业务入口。</p>
      </article>
      <article class="spotlight-card spotlight-card--metric dashboard-spotlight-card dashboard-spotlight-card--metric">
        <span class="spotlight-label dashboard-spotlight-label">资料完整度</span>
        <strong>{{ profileReadiness }}</strong>
        <p>用户名、真实姓名和邮箱当前补全程度。</p>
      </article>
      <article class="spotlight-card spotlight-card--metric dashboard-spotlight-card dashboard-spotlight-card--metric">
        <span class="spotlight-label dashboard-spotlight-label">安全动作</span>
        <strong>2 项</strong>
        <p>密码修改与退出登录都在同一页面内完成。</p>
      </article>
    </section>

    <section class="guide-grid dashboard-guide-grid">
      <article class="guide-card guide-card--wide dashboard-guide-card dashboard-guide-card--wide">
        <div class="guide-card__head dashboard-guide-head">
          <div>
            <div class="section-kicker">
              Admin Identity
            </div>
            <h3>账户视图</h3>
          </div>
        </div>
        <div class="guide-points">
          <div class="guide-point">
            <span>01</span>
            <strong>当前账号拥有后台治理权限，建议保持唯一责任人使用。</strong>
          </div>
          <div class="guide-point">
            <span>02</span>
            <strong>邮箱建议保持可用，便于接收系统通知和安全提醒。</strong>
          </div>
          <div class="guide-point">
            <span>03</span>
            <strong>密码变更后会立即退出当前会话，避免旧凭证继续使用。</strong>
          </div>
        </div>
      </article>

      <article class="guide-card dashboard-guide-card">
        <div class="guide-card__head dashboard-guide-head">
          <div>
            <div class="section-kicker">
              Security Rules
            </div>
            <h3>安全建议</h3>
          </div>
        </div>
        <ul class="security-list">
          <li>密码至少 8 位，并避免与其他系统复用。</li>
          <li>修改密码后建议重新确认后台登录状态是否正常。</li>
          <li>如发现异常登录，请立即更换密码并检查登录日志。</li>
        </ul>
      </article>
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
const profileReadiness = computed(() => {
  const fields = [userInfo.value.username, userInfo.value.realName, userInfo.value.email].filter(Boolean).length
  return `${fields}/3`
})
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

.guide-card,
.profile-card {
  background: rgba(252, 251, 248, 0.82);
  border: 1px solid rgba(0, 9, 24, 0.05);
  border-radius: 8px;
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
  background: rgba(179, 138, 61, 0.12);
  border: 1px solid rgba(179, 138, 61, 0.14);
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
