<template>
  <div class="help-center-container">
    <AppHeader
      variant="portal"
      title="帮助中心"
      :show-back="true"
      @back="goBack"
    />
    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="help-hero section-shell">
        <p class="help-summary">
          常见问题与自助排查。
        </p>
      </section>

      <section class="quick-grid section-shell">
        <article class="quick-card">
          <span class="quick-label">访客</span>
          <strong>{{ visitorName }}</strong>
        </article>
        <article class="quick-card">
          <span class="quick-label">服务机构</span>
          <strong>{{ lawFirmName }}</strong>
        </article>
        <article class="quick-card">
          <span class="quick-label">服务说明</span>
          <strong class="quick-slogan">{{ appSlogan }}</strong>
        </article>
      </section>

      <section class="help-card section-shell">
        <div class="section-head">
          <h3>常见问题</h3>
        </div>
        <a-collapse ghost>
          <a-collapse-panel
            v-for="item in faqItems"
            :key="item.key"
            :header="item.title"
          >
            <p>{{ item.content }}</p>
          </a-collapse-panel>
        </a-collapse>
      </section>

      <section class="guide-grid section-shell">
        <article class="guide-card">
          <h3>链接打不开</h3>
          <ol class="guide-list">
            <li>确认链接完整（勿缺参数）。</li>
            <li>确认项目未过期且仍有权限。</li>
            <li>仍不行则请律师重新发送链接。</li>
          </ol>
        </article>
        <article class="guide-card guide-card--steps">
          <h3>联系律所</h3>
          <p class="contact-copy">
            请优先联系承办律师。
          </p>
          <a
            v-if="lawFirmWebsite"
            :href="lawFirmWebsite"
            target="_blank"
            rel="noopener noreferrer"
            class="contact-link"
          >
            {{ lawFirmName }} 官网
          </a>
          <p
            v-else
            class="contact-copy"
          >
            未配置官网链接时，请直接联系律所。
          </p>
        </article>
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
    title: '如何进入项目？',
    content: '打开律师发来的链接；或在首页粘贴完整访问链接；已登录可从「我的项目」进入。',
  },
  {
    key: 'file-access',
    title: '文件在哪里看？',
    content: '进入项目后查看材料，或使用「文件中心」。',
  },
  {
    key: 'notification-access',
    title: '消息在哪里看？',
    content: '在「消息通知」查看提醒与更新。',
  },
  {
    key: 'expired-link',
    title: '链接失效怎么办？',
    content: '请联系承办律师重新发送链接。',
  },
]

function goBack() {
  router.back()
}
</script>

<style scoped>
.help-center-container {
  min-height: 100vh;
  background: transparent;
}

.content {
  display: grid;
  gap: 16px;
}

.help-hero {
  padding: 20px 24px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
}

.help-summary {
  margin: 8px 0 0;
  line-height: 1.6;
  color: var(--text-secondary);
}

.quick-card,
.help-card,
.guide-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
}

.help-card {
  padding: 20px;
  margin-bottom: 16px;
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
  padding: 20px;
}

.quick-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.quick-card strong {
  display: block;
  margin-bottom: 0;
  color: var(--text-primary);
  font-size: 18px;
}

.quick-slogan {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.45;
}

.quick-card p,
.contact-copy {
  margin: 0;
  line-height: 1.7;
  color: var(--text-secondary);
}

.guide-card--steps {
  background: var(--lex-bg-muted);
}

.section-head {
  margin-bottom: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 18px;
  color: var(--lex-primary);
}

.guide-card h3 {
  margin: 0 0 12px;
  font-size: 17px;
  color: var(--lex-primary);
}

.guide-list {
  margin: 0;
  padding-left: 18px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.contact-link {
  display: inline-flex;
  margin-top: 12px;
}

@media (max-width: 768px) {
  .quick-grid,
  .guide-grid {
    grid-template-columns: 1fr;
  }

  .help-hero,
  .quick-card,
  .help-card,
  .guide-card {
    padding: 16px;
  }
}
</style>
