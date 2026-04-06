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
      <section class="help-hero">
        <div class="hero-copy">
          <div class="eyebrow">
            Support Desk
          </div>
          <h2 class="editorial-title">
            常见问题与操作说明
          </h2>
          <p class="help-summary">
            这里把访问说明、异常处理建议和联系路径整理成一套更清楚的支持入口，避免客户在不同页面之间来回找答案。
          </p>
        </div>
        <div class="hero-side">
          <div class="hero-side-label">
            Quick Guidance
          </div>
          <strong>先判断链接、再判断有效期、最后联系承办律师。</strong>
          <p>帮助页负责把最常见的问题变成可执行步骤，而不是只堆一组问答。</p>
        </div>
      </section>

      <section class="quick-grid">
        <article class="quick-card">
          <span class="quick-label">当前访客</span>
          <strong>{{ visitorName }}</strong>
          <p>如果您刚访问过具体项目，系统会在这里同步最近一次识别到的客户名称。</p>
        </article>
        <article class="quick-card">
          <span class="quick-label">服务机构</span>
          <strong>{{ lawFirmName }}</strong>
          <p>如需人工协助，请优先联系承办律师，或通过律所官网查询公开联系方式。</p>
        </article>
        <article class="quick-card">
          <span class="quick-label">处理建议</span>
          <strong>{{ appSlogan }}</strong>
          <p>遇到访问异常时，优先确认链接完整性、项目有效期和消息通知中的最新提醒。</p>
        </article>
      </section>

      <section class="support-path">
        <article class="path-card">
          <span class="panel-kicker">Support Path</span>
          <h3>建议顺序：先看链接，再看有效期，最后联系承办律师</h3>
          <p>帮助中心优先解决最常见的访问问题，只有在自助排查仍失败时，才进入人工协助路径。</p>
        </article>
      </section>

      <section class="help-card">
        <div class="section-head">
          <div>
            <div class="eyebrow eyebrow--muted">
              FAQ
            </div>
            <h3>高频问题</h3>
          </div>
          <p>先覆盖最常见的访问问题，让客户能在当前页面直接自助解决。</p>
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

      <section class="guide-grid">
        <article class="guide-card">
          <div class="guide-head">
            <div class="eyebrow eyebrow--muted">
              Access Failure
            </div>
            <h3>遇到链接失效</h3>
          </div>
          <ol class="guide-list">
            <li>确认访问链接是否完整，尤其是 `token` 参数没有被截断。</li>
            <li>检查项目是否已过期，或访问权限是否已被律所撤销。</li>
            <li>如仍无法访问，请联系承办律师重新发送链接。</li>
          </ol>
        </article>
        <article class="guide-card guide-card--steps">
          <div class="guide-head">
            <div class="eyebrow eyebrow--muted">
              Contact
            </div>
            <h3>需要更多帮助</h3>
          </div>
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
  background: transparent;
}

.content {
  display: grid;
  gap: 16px;
}

.help-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.8fr);
  gap: 16px;
  margin-bottom: 16px;
}

.hero-copy,
.hero-side,
.quick-card,
.help-card,
.guide-card {
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: #ffffff;
  box-shadow: var(--shadow-sm);
}

.hero-copy,
.hero-side {
  padding: 20px;
}

.hero-side {
  display: grid;
  align-content: start;
  gap: 8px;
  background: #fafafa;
}

.hero-side-label,
.eyebrow--muted {
  color: var(--text-tertiary);
  font-size: 12px;
  letter-spacing: 0;
  text-transform: none;
}

.hero-side strong {
  color: var(--text-primary);
  font-size: 18px;
  line-height: 1.5;
}

.hero-side p {
  margin: 0;
  line-height: 1.75;
  color: var(--text-secondary);
}

.help-summary {
  margin: 8px 0 0;
  line-height: 1.7;
  color: var(--text-secondary);
}

.support-path {
  display: grid;
  margin-bottom: 16px;
}

.path-card {
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  background: rgba(248, 244, 237, 0.74);
  box-shadow: var(--shadow-xs);
}

.path-card h3 {
  margin: 8px 0 10px;
  font-size: 22px;
  color: var(--text-primary);
}

.path-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.8;
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
  margin-bottom: 8px;
  color: var(--text-primary);
  font-size: 18px;
}

.quick-card p,
.contact-copy {
  margin: 0;
  line-height: 1.7;
  color: var(--text-secondary);
}

.guide-card--steps {
  background: rgba(252, 251, 248, 0.92);
}

.section-head,
.guide-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h3,
.guide-head h3 {
  margin: 4px 0 0;
  font-size: 18px;
  color: var(--text-primary);
}

.section-head p {
  margin: 0;
  max-width: 360px;
  line-height: 1.7;
  color: var(--text-secondary);
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
  .help-hero,
  .quick-grid,
  .guide-grid {
    grid-template-columns: 1fr;
  }

  .hero-copy,
  .hero-side,
  .quick-card,
  .path-card,
  .help-card,
  .guide-card {
    padding: 16px;
    border-radius: 8px;
  }

  .section-head,
  .guide-head {
    display: grid;
  }
}
</style>
