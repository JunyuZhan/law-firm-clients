<template>
  <div class="initial-setup-container">
    <section class="page-intro">
      <div>
        <h2 class="editorial-title intro-title">
          首次初始化
        </h2>
        <p class="intro-text">
          用于新客户首次交付后的基础配置初始化，先完成品牌名称、门户展示文案和页脚信息，再进入系统配置补充高级参数。
        </p>
      </div>
      <div class="intro-actions">
        <a-button @click="goTo('/admin/system-info')">
          查看系统信息
        </a-button>
        <a-button @click="goTo('/admin/config')">
          前往系统配置
        </a-button>
        <a-button
          type="primary"
          :loading="saving"
          @click="saveSetup"
        >
          保存初始化配置
        </a-button>
      </div>
    </section>

    <section class="status-strip config-card">
      <div class="status-strip-grid">
        <a-alert
          :message="setupStatus.title"
          :type="setupStatus.type"
          show-icon
          :closable="false"
        />
        <div class="status-actions">
          <span class="status-label">交付路径</span>
          <strong>首次初始化 → 系统信息 → 系统配置</strong>
        </div>
      </div>
    </section>

    <section class="setup-grid">
      <article class="config-card">
        <div class="section-head">
          <div>
            <h3>基础品牌初始化</h3>
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
              placeholder="例如：某某律师事务所客户服务系统"
            />
          </a-form-item>
          <a-form-item label="系统简称（中文）">
            <a-input
              v-model:value="form.appShortName"
              maxlength="30"
              placeholder="例如：某某律所客服"
            />
          </a-form-item>
          <a-form-item label="系统简称（英文）">
            <a-input
              v-model:value="form.appShortNameEn"
              maxlength="50"
              placeholder="例如：Law Firm Clients"
            />
          </a-form-item>
          <a-form-item label="Logo 地址">
            <a-input
              v-model:value="form.logoUrl"
              placeholder="可填写 /logo.png 或完整 https:// URL"
            />
          </a-form-item>
          <a-form-item label="律所名称">
            <a-input
              v-model:value="form.lawFirmName"
              maxlength="50"
              placeholder="例如：某某律师事务所"
            />
          </a-form-item>
          <a-form-item label="律所官网">
            <a-input
              v-model:value="form.lawFirmWebsite"
              maxlength="120"
              placeholder="例如：https://www.example.com"
            />
          </a-form-item>
          <a-form-item label="首页标语">
            <a-input
              v-model:value="form.appSlogan"
              maxlength="80"
              placeholder="例如：专业事项，一个清晰的客户入口"
            />
          </a-form-item>
          <a-form-item label="ICP备案号">
            <a-input
              v-model:value="form.icpLicense"
              maxlength="80"
              placeholder="例如：京ICP备12345678号-1"
            />
          </a-form-item>
          <a-form-item label="版权说明">
            <a-input
              v-model:value="form.copyright"
              maxlength="120"
              placeholder="例如：© 2026 某某律师事务所"
            />
          </a-form-item>
        </a-form>
      </article>

      <article class="config-card preview-card">
        <div class="section-head">
          <div>
            <h3>效果预览</h3>
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
              >
              <span v-else>未设置</span>
            </div>
            <div>
              <div class="preview-name">{{ form.appName || defaultValues.appName }}</div>
              <div class="preview-subname">{{ form.appShortNameEn || defaultValues.appShortNameEn }}</div>
            </div>
          </div>

          <div class="preview-copy">
            <div class="preview-kicker">{{ form.lawFirmName || defaultValues.lawFirmName }}</div>
            <strong>{{ form.appSlogan || defaultValues.appSlogan }}</strong>
            <p>用于门户首页、帮助页、登录页和后台导航中的统一品牌表达。</p>
          </div>

          <div class="preview-footer">
            <span>{{ form.icpLicense || '未设置ICP备案号' }}</span>
            <span>{{ form.copyright || defaultValues.copyright }}</span>
          </div>
        </div>

        <div class="next-step-card">
          <ul class="plain-list">
            <li>初始化完成后，可进入“系统信息”页确认版本和文档入口是否齐全。</li>
            <li>再进入“系统配置”补充基础 URL、回调和通知策略。</li>
            <li>正式交付前，建议执行一次快速 API 冒烟测试。</li>
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

const appConfigStore = useAppConfigStore()
const router = useRouter()
const saving = ref(false)

const defaultValues = {
  appName: '客户服务系统',
  appShortName: '客户服务系统',
  appShortNameEn: 'Law Firm Clients',
  lawFirmName: '律师事务所',
  appSlogan: '专业事项，一个清晰的客户入口',
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
  icpLicense: '',
  copyright: '',
})

const setupStatus = computed(() => {
  const initialized = !!form.appName && form.appName !== defaultValues.appName
  return initialized
    ? {
      type: 'success' as const,
      title: '当前站点已完成基础品牌初始化，可继续微调或重新保存。',
    }
    : {
      type: 'warning' as const,
      title: '当前仍接近默认品牌配置，建议在正式交付前完成初始化。',
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
      form.icpLicense = portalRes.data.icpLicense || ''
      form.copyright = portalRes.data.copyright || defaultValues.copyright
      if (!form.logoUrl && portalRes.data.logoUrl) {
        form.logoUrl = portalRes.data.logoUrl
      }
    }
  } catch {
    message.error('加载初始化配置失败')
  }
}

async function saveSetup() {
  if (!form.appName.trim()) {
    message.warning('请先填写系统全称')
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
      ['system.icp-license', form.icpLicense],
      ['system.copyright', form.copyright || defaultValues.copyright],
    ] as const

    await Promise.all(entries.map(([configKey, configValue]) => saveConfig({
      configKey,
      configValue,
      configType: 'STRING',
      description: '首次初始化向导配置',
    })))

    await appConfigStore.loadConfig()
    message.success('初始化配置保存成功')
  } catch {
    message.error('初始化配置保存失败')
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
  border-radius: 8px;
  background: rgba(248, 244, 237, 0.8);
  border: 1px solid rgba(179, 138, 61, 0.18);
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
  border-radius: 8px;
  background:
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.08), transparent 28%),
    rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.preview-brand {
  display: flex;
  align-items: center;
  gap: 16px;
}

.preview-logo-wrap {
  width: 68px;
  height: 68px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(15, 23, 42, 0.08);
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
  border-radius: 8px;
  background: rgba(248, 244, 237, 0.74);
  border: 1px solid rgba(15, 23, 42, 0.08);
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
