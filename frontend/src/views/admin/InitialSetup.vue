<template>
  <div class="initial-setup-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_INITIAL_SETUP_TEXTS.intro }}
        </p>
      </div>
      <div class="intro-actions">
        <a-button @click="goTo('/admin/system-info')">
          {{ ADMIN_INITIAL_SETUP_TEXTS.actions.systemInfo }}
        </a-button>
        <a-button @click="goTo('/admin/config')">
          {{ ADMIN_INITIAL_SETUP_TEXTS.actions.systemConfig }}
        </a-button>
        <a-button
          type="primary"
          :loading="saving"
          @click="saveSetup"
        >
          {{ ADMIN_INITIAL_SETUP_TEXTS.actions.saveSetup }}
        </a-button>
      </div>
    </section>

    <section class="status-strip config-card">
      <div
        class="status-strip-grid"
        aria-live="polite"
      >
        <a-alert
          :message="setupStatus.title"
          :type="setupStatus.type"
          show-icon
          :closable="false"
        />
        <div class="status-actions">
          <span class="status-label">{{ ADMIN_INITIAL_SETUP_TEXTS.status.pathLabel }}</span>
          <strong>{{ ADMIN_INITIAL_SETUP_TEXTS.status.pathValue }}</strong>
        </div>
      </div>
    </section>

    <section class="setup-grid">
      <article class="config-card">
        <div class="section-head">
          <div>
            <h3>{{ ADMIN_INITIAL_SETUP_TEXTS.sections.brandInit }}</h3>
          </div>
        </div>

        <a-form
          layout="vertical"
          class="setup-form"
        >
          <a-form-item label="系统全称">
            <a-input
              v-model:value="form.appName"
              maxlength="50"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.appName"
            />
          </a-form-item>
          <a-form-item label="系统简称（中文）">
            <a-input
              v-model:value="form.appShortName"
              maxlength="30"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.appShortName"
            />
          </a-form-item>
          <a-form-item label="系统简称（英文）">
            <a-input
              v-model:value="form.appShortNameEn"
              maxlength="50"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.appShortNameEn"
            />
          </a-form-item>
          <a-form-item label="Logo 地址">
            <a-input
              v-model:value="form.logoUrl"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.logoUrl"
            />
          </a-form-item>
          <a-form-item label="律所名称">
            <a-input
              v-model:value="form.lawFirmName"
              maxlength="50"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.lawFirmName"
            />
          </a-form-item>
          <a-form-item label="律所官网">
            <a-input
              v-model:value="form.lawFirmWebsite"
              maxlength="120"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.lawFirmWebsite"
            />
          </a-form-item>
          <a-form-item label="首页标语">
            <a-input
              v-model:value="form.appSlogan"
              maxlength="80"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.appSlogan"
            />
          </a-form-item>
          <a-form-item label="门户页英文眉标（可选）">
            <a-input
              v-model:value="form.portalEyebrowEn"
              maxlength="80"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.portalEyebrowEn"
            />
          </a-form-item>
          <a-form-item label="门户页客户说明（公开）">
            <a-textarea
              v-model:value="form.portalAccessNotice"
              :rows="3"
              maxlength="500"
              show-count
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.portalAccessNotice"
            />
          </a-form-item>
          <a-form-item label="系统管理入口">
            <a-input
              v-model:value="form.staffEntryLabel"
              maxlength="40"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.staffEntryLabel"
            />
          </a-form-item>
          <a-form-item label="ICP备案号">
            <a-input
              v-model:value="form.icpLicense"
              maxlength="80"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.icpLicense"
            />
          </a-form-item>
          <a-form-item label="版权说明">
            <a-input
              v-model:value="form.copyright"
              maxlength="120"
              :placeholder="ADMIN_INITIAL_SETUP_TEXTS.placeholders.copyright"
            />
          </a-form-item>
        </a-form>
      </article>

      <article class="config-card preview-card">
        <div class="section-head">
          <div>
            <h3>{{ ADMIN_INITIAL_SETUP_TEXTS.sections.preview }}</h3>
          </div>
        </div>

        <div class="preview-box">
          <div class="preview-brand">
            <div class="preview-logo-wrap">
              <img
                v-if="form.logoUrl"
                :src="form.logoUrl"
                alt="Logo 预览"
                class="preview-logo"
                width="40"
                height="40"
              >
              <span v-else>{{ ADMIN_INITIAL_SETUP_TEXTS.preview.logoEmpty }}</span>
            </div>
            <div>
              <div class="preview-name">
                {{ form.appName || defaultValues.appName }}
              </div>
              <div class="preview-subname">
                {{ form.appShortNameEn || defaultValues.appShortNameEn }}
              </div>
            </div>
          </div>

          <div class="preview-copy">
            <div class="preview-kicker">
              {{ form.lawFirmName || defaultValues.lawFirmName }}
            </div>
            <strong>{{ form.appSlogan || defaultValues.appSlogan }}</strong>
            <p class="preview-access">
              {{ form.portalAccessNotice ?? defaultValues.portalAccessNotice }}
            </p>
            <p>{{ ADMIN_INITIAL_SETUP_TEXTS.sections.previewUsage }}</p>
          </div>

          <div class="preview-footer">
            <span>{{ form.icpLicense || ADMIN_INITIAL_SETUP_TEXTS.preview.icpEmpty }}</span>
            <span>{{ form.copyright || defaultValues.copyright }}</span>
          </div>
        </div>

        <div class="next-step-card">
          <ul class="plain-list">
            <li
              v-for="item in ADMIN_INITIAL_SETUP_TEXTS.nextSteps"
              :key="item"
            >
              {{ item }}
            </li>
          </ul>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { getBrandConfig, getPortalConfig, saveConfig } from '@/api/config'
import { useAppConfigStore } from '@/stores/appConfig'
import { UI_FEEDBACK_TEXTS } from '@/constants/uiTexts'
import { ADMIN_INITIAL_SETUP_TEXTS } from '@/constants/adminTexts'

const appConfigStore = useAppConfigStore()
const router = useRouter()
const saving = ref(false)

const defaultValues = {
  appName: '客户服务系统',
  appShortName: '客户服务系统',
  appShortNameEn: 'Law Firm Clients',
  lawFirmName: '律师事务所',
  appSlogan: '专业事项，一个清晰的客户入口',
  portalEyebrowEn: '',
  portalAccessNotice:
    '本系统由律师事务所为客户提供专项服务。查看项目进展与材料，请使用承办律师向您发送的专属访问链接。',
  staffEntryLabel: '系统管理入口',
  copyright: '© 2026 律师事务所',
}

const form = reactive({
  appName: '',
  appShortName: '',
  appShortNameEn: '',
  logoUrl: '',
  lawFirmName: '',
  lawFirmWebsite: '',
  appSlogan: '',
  portalEyebrowEn: '',
  portalAccessNotice: '',
  staffEntryLabel: '',
  icpLicense: '',
  copyright: '',
})

const setupStatus = computed(() => {
  const initialized = !!form.appName && form.appName !== defaultValues.appName
  return initialized
    ? {
      type: 'success' as const,
      title: ADMIN_INITIAL_SETUP_TEXTS.status.initialized,
    }
    : {
      type: 'warning' as const,
      title: ADMIN_INITIAL_SETUP_TEXTS.status.pending,
    }
})

async function loadSetupData() {
  try {
    const [brandRes, portalRes] = await Promise.all([
      getBrandConfig(),
      getPortalConfig(),
    ])

    if (brandRes.success && brandRes.data) {
      form.appName = brandRes.data.appName || defaultValues.appName
      form.appShortName = brandRes.data.appShortName || defaultValues.appShortName
      form.appShortNameEn = brandRes.data.appShortNameEn || defaultValues.appShortNameEn
      form.logoUrl = brandRes.data.logoUrl || ''
    }

    if (portalRes.success && portalRes.data) {
      form.lawFirmName = portalRes.data.lawFirmName || defaultValues.lawFirmName
      form.lawFirmWebsite = portalRes.data.lawFirmWebsite || ''
      form.appSlogan = portalRes.data.appSlogan || defaultValues.appSlogan
      form.portalEyebrowEn = portalRes.data.portalEyebrowEn ?? ''
      form.portalAccessNotice = portalRes.data.portalAccessNotice ?? defaultValues.portalAccessNotice
      form.staffEntryLabel = portalRes.data.staffEntryLabel ?? defaultValues.staffEntryLabel
      form.icpLicense = portalRes.data.icpLicense || ''
      form.copyright = portalRes.data.copyright || defaultValues.copyright
      if (!form.logoUrl && portalRes.data.logoUrl) {
        form.logoUrl = portalRes.data.logoUrl
      }
    }
  } catch {
    message.error(UI_FEEDBACK_TEXTS.initLoadFailed)
  }
}

async function saveSetup() {
  if (!form.appName.trim()) {
    message.warning(ADMIN_INITIAL_SETUP_TEXTS.validation.appNameRequired)
    return
  }

  saving.value = true
  try {
    const entries = [
      ['system.app-name', form.appName],
      ['system.app-short-name', form.appShortName || form.appName],
      ['system.app-short-name-en', form.appShortNameEn || defaultValues.appShortNameEn],
      ['system.logo-url', form.logoUrl],
      ['system.law-firm-name', form.lawFirmName || defaultValues.lawFirmName],
      ['system.law-firm-website', form.lawFirmWebsite],
      ['system.app-slogan', form.appSlogan || defaultValues.appSlogan],
      ['system.portal-eyebrow-en', form.portalEyebrowEn],
      ['system.portal-access-notice', form.portalAccessNotice || defaultValues.portalAccessNotice],
      ['system.staff-entry-label', form.staffEntryLabel || defaultValues.staffEntryLabel],
      ['system.icp-license', form.icpLicense],
      ['system.copyright', form.copyright || defaultValues.copyright],
    ] as const

    await Promise.all(entries.map(([configKey, configValue]) => saveConfig({
      configKey,
      configValue,
      configType: 'STRING',
      description: ADMIN_INITIAL_SETUP_TEXTS.saveDescription,
    })))

    await appConfigStore.loadConfig()
    message.success(UI_FEEDBACK_TEXTS.initSaved)
  } catch {
    message.error(UI_FEEDBACK_TEXTS.initSaveFailed)
  } finally {
    saving.value = false
  }
}

function goTo(path: string) {
  router.push(path)
}

onMounted(() => {
  loadSetupData()
})
</script>

<style scoped>
.initial-setup-container {
  display: grid;
  gap: 18px;
}

.intro-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.status-strip {
  display: grid;
}

.status-strip-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(260px, 0.85fr);
  gap: 12px;
  align-items: stretch;
}

.status-actions {
  display: grid;
  gap: 4px;
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--lex-bg-muted);
  border: 1px solid color-mix(in srgb, var(--lex-accent) 22%, var(--border-color));
}

.status-label {
  color: var(--text-tertiary);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.status-actions strong {
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.6;
}

.setup-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(300px, 0.88fr);
  gap: 16px;
  align-items: start;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.panel-kicker {
  color: var(--text-tertiary);
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.setup-form {
  max-width: 760px;
}

.preview-card {
  position: sticky;
  top: 88px;
}

.preview-box {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
  background:
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.08), transparent 28%),
    rgba(255, 255, 255, 0.72);
  border: 1px solid var(--border-color-light);
}

.preview-brand {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-logo-wrap {
  width: 68px;
  height: 68px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--border-color-light);
  color: var(--text-tertiary);
  flex: none;
}

.preview-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 10px;
}

.preview-name {
  font-size: 22px;
  font-family: var(--font-heading);
  color: var(--text-primary);
}

.preview-subname {
  margin-top: 6px;
  color: var(--text-tertiary);
}

.preview-copy {
  display: grid;
  gap: 8px;
}

.preview-kicker {
  color: var(--primary-color);
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.preview-copy strong {
  color: var(--text-primary);
  font-size: 20px;
  line-height: 1.45;
}

.preview-copy p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.75;
}

.preview-access {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.preview-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
  color: var(--text-tertiary);
  font-size: 12px;
}

.next-step-card {
  display: grid;
  gap: 6px;
  margin-top: 14px;
  padding: 14px;
  border-radius: 16px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--border-color-light);
}

.plain-list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.9;
}

@media (max-width: 960px) {
  .status-strip-grid,
  .setup-grid {
    grid-template-columns: 1fr;
  }

  .preview-card {
    position: static;
  }
}
</style>
