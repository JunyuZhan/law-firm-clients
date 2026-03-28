<template>
  <div class="help-center-container">
    <AppHeader
      variant="portal"
      title="帮助中心"
      :show-back="true"
      @back="goBack"
    />
    <a-layout-content class="content">
      <section class="help-hero">
        <div class="eyebrow">
          使用帮助
        </div>
        <h2 class="editorial-title">
          常见问题与操作说明
        </h2>
        <p class="help-summary">
          这里汇总客户最常用的访问说明、异常处理建议和联系入口，帮助您更快找到项目、文件和消息通知。
        </p>
      </section>

      <section class="quick-grid">
        <a-card class="quick-card">
          <span class="quick-label">当前访客</span>
          <strong>{{ visitorName }}</strong>
          <p>如果您刚访问过具体项目，系统会在这里同步最近一次识别到的客户名称。</p>
        </a-card>
        <a-card class="quick-card">
          <span class="quick-label">服务机构</span>
          <strong>{{ lawFirmName }}</strong>
          <p>如需人工协助，请优先联系承办律师，或通过律所官网查询公开联系方式。</p>
        </a-card>
        <a-card class="quick-card">
          <span class="quick-label">处理建议</span>
          <strong>{{ appSlogan }}</strong>
          <p>遇到访问异常时，优先确认链接完整性、项目有效期和消息通知中的最新提醒。</p>
        </a-card>
      </section>

      <a-card class="help-card">
        <a-collapse ghost>
          <a-collapse-panel
            v-for="item in faqItems"
            :key="item.key"
            :header="item.title"
          >
            <p>{{ item.content }}</p>
          </a-collapse-panel>
        </a-collapse>
      </a-card>

      <section class="guide-grid">
        <a-card
          class="guide-card"
          title="遇到链接失效"
        >
          <ol class="guide-list">
            <li>确认访问链接是否完整，尤其是 `token` 参数没有被截断。</li>
            <li>检查项目是否已过期，或访问权限是否已被律所撤销。</li>
            <li>如仍无法访问，请联系承办律师重新发送链接。</li>
          </ol>
        </a-card>
        <a-card
          class="guide-card"
          title="需要更多帮助"
        >
          <p class="contact-copy">
            如需进一步协助，请优先联系您的承办律师。
          </p>
          <a
            v-if="lawFirmWebsite"
            :href="lawFirmWebsite"
            target="_blank"
            rel="noopener noreferrer"
            class="contact-link"
          >
            前往 {{ lawFirmName }} 官网
          </a>
          <p
            v-else
            class="contact-copy"
          >
            当前未配置官网链接，请直接联系律所工作人员获取支持。
          </p>
        </a-card>
      </section>
    </a-layout-content>
    <MobileBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { usePortalVisitorStore } from '@/stores/portalVisitor'

const router = useRouter()
const appConfigStore = useAppConfigStore()
const portalVisitorStore = usePortalVisitorStore()

const lawFirmName = computed(() => appConfigStore.lawFirmName || '律师事务所')
const lawFirmWebsite = computed(() => appConfigStore.lawFirmWebsite)
const appSlogan = computed(() => appConfigStore.appSlogan || '专业 · 诚信 · 高效')
const visitorName = computed(() => portalVisitorStore.displayName || '当前访客')

const faqItems = [
  {
    key: 'matter-access',
    title: '如何进入我的项目？',
    content: '您可以在首页粘贴律所发送的项目访问链接，或从“我的项目”中直接进入已有访问权限的项目。',
  },
  {
    key: 'file-access',
    title: '在哪里查看和下载文件？',
    content: '进入具体项目后可查看文件详情，也可以通过“文件中心”集中浏览与下载当前项目相关文件。',
  },
  {
    key: 'notification-access',
    title: '消息通知在哪里查看？',
    content: '系统推送的最新动态会显示在“消息通知”页面，您可以在其中查看最近收到的提醒和状态更新。',
  },
  {
    key: 'expired-link',
    title: '如果访问链接失效怎么办？',
    content: '当项目访问链接过期或被撤销时，请联系承办律师重新生成新的访问链接或为您恢复访问权限。',
  },
]

function goBack() {
  router.back()
}
</script>

<style scoped>
.help-center-container {
  min-height: 100vh;
  background: var(--bg-secondary);
}

.content {
  padding: 16px;
}

.help-hero {
  margin-bottom: 16px;
}

.eyebrow {
  margin-bottom: 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--primary-color);
}

.help-summary {
  margin: 8px 0 0;
  line-height: 1.7;
  color: #666;
}

.help-card {
  border-radius: 20px;
}

.quick-grid,
.guide-grid {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
}

.quick-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.guide-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.quick-card,
.guide-card {
  border-radius: 20px;
}

.quick-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.quick-card strong {
  display: block;
  margin-bottom: 10px;
  color: var(--primary-color);
}

.quick-card p,
.contact-copy {
  margin: 0;
  line-height: 1.7;
  color: #666;
}

.guide-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.8;
  color: #666;
}

.contact-link {
  display: inline-flex;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .help-center-container {
    padding-bottom: 70px;
  }

  .quick-grid,
  .guide-grid {
    grid-template-columns: 1fr;
  }
}
</style>
