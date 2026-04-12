<template>
  <div class="portal-page">
    <AppHeader variant="portal" />

    <a-layout-content
      id="main-content"
      class="content"
      tabindex="-1"
    >
      <section class="portal-panel section-shell">
        <p
          v-if="cardEyebrow"
          class="portal-eyebrow"
        >
          {{ cardEyebrow }}
        </p>
        <p class="portal-slogan">
          {{ slogan }}
        </p>
        <p
          v-if="accessNoticeDisplay"
          class="portal-note"
        >
          {{ accessNoticeDisplay }}
        </p>
      </section>

      <footer
        v-if="footerText || staffEntryLabel"
        class="portal-footer section-shell"
      >
        <p v-if="footerText">
          {{ footerText }}
        </p>
        <p
          v-if="staffEntryLabel"
          class="portal-staff-line"
        >
          <router-link
            to="/admin/login"
            class="portal-staff-link"
          >
            {{ staffEntryLabel }}
          </router-link>
        </p>
      </footer>
    </a-layout-content>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAppConfigStore } from '@/stores/appConfig'
import { APP_SLOGAN } from '@/config/app'

const appConfigStore = useAppConfigStore()

const slogan = computed(() => appConfigStore.appSlogan?.trim() || APP_SLOGAN)

const cardEyebrow = computed(() => {
  const custom = appConfigStore.portalEyebrowEn?.trim()
  if (custom) return custom
  return appConfigStore.appShortNameEn?.trim() || ''
})

const accessNoticeDisplay = computed(() => appConfigStore.portalAccessNotice?.trim() || '')

const staffEntryLabel = computed(() => appConfigStore.staffEntryLabel?.trim() || '')

const footerText = computed(() => {
  const icp = appConfigStore.icpLicense?.trim()
  const copy = appConfigStore.copyright?.trim()
  if (icp && copy) return `${copy} · ${icp}`
  return icp || copy || ''
})

</script>

<style scoped>
.portal-page {
  min-height: 100vh;
  background: transparent;
}

.content {
  display: grid;
  gap: 16px;
}

.portal-panel {
  padding: 24px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--lex-surface-strong);
  box-shadow: var(--shadow-sm);
}

.portal-eyebrow {
  margin: 0 0 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--lex-accent-strong);
}

.portal-slogan {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.45;
  color: var(--lex-primary);
}

.portal-note {
  margin: 0;
  font-size: 14px;
  line-height: 1.75;
  color: var(--text-secondary);
}

.portal-footer p {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  text-align: center;
  color: var(--text-tertiary);
}

.portal-staff-line {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
}

.portal-staff-link {
  color: var(--text-tertiary);
  text-decoration: none;
}

.portal-staff-link:hover {
  color: var(--lex-primary-soft);
  text-decoration: underline;
}

@media (max-width: 768px) {
  .portal-panel {
    padding: 16px;
  }

  .portal-slogan {
    font-size: 15px;
  }
}
</style>
