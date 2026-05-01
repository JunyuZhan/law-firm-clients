<template>
  <div class="system-maintenance">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.intro }}
        </p>
      </div>
      <a-button
        type="primary"
        :loading="loading"
        @click="refreshStatus"
      >
        <template #icon>
          <ReloadOutlined />
        </template>
        {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.actions.refreshStatus }}
      </a-button>
    </section>

    <section class="ops-overview">
      <article class="ops-metric">
        <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.database }}</span>
        <strong>{{ status.database?.connected ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.normal : ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.waiting }}</strong>
      </article>
      <article class="ops-metric">
        <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.storage }}</span>
        <strong>{{ status.storage?.available ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.available : ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.waiting }}</strong>
      </article>
      <article class="ops-metric">
        <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.backups }}</span>
        <strong>{{ backups.length }}</strong>
      </article>
      <article class="ops-metric">
        <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.version }}</span>
        <strong>{{ gitInfo.hasUpdate ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.upgradeable : gitInfo.currentVersion ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.latest : ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.unchecked }}</strong>
      </article>
    </section>

    <a-row
      :gutter="[16, 16]"
      class="status-row equal-height-cards"
    >
      <a-col
        :xs="24"
        :sm="24"
        :md="8"
      >
        <a-card
          :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.databaseTitle"
          :loading="loading"
          class="maintenance-card"
        >
          <template #extra>
            <a-tag :color="status.database?.connected ? 'green' : 'red'">
              {{ status.database?.connected ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.normal : ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.abnormal }}
            </a-tag>
          </template>
          <a-descriptions
            :column="1"
            size="small"
          >
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.databaseSizeLabel">
              {{ status.database?.sizeFormatted || '-' }}
            </a-descriptions-item>
          </a-descriptions>
          <a-divider style="margin: 12px 0" />
          <div class="table-stats">
            <div
              v-for="(count, name) in status.database?.tables"
              :key="name"
              class="stat-item"
            >
              <span class="stat-label">{{ name }}</span>
              <span class="stat-value">{{ count }} {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.recordsSuffix }}</span>
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col
        :xs="24"
        :sm="24"
        :md="8"
      >
        <a-card
          :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.storageTitle"
          :loading="loading"
          class="maintenance-card"
        >
          <template #extra>
            <a-tag :color="status.storage?.available ? 'green' : 'red'">
              {{ status.storage?.available ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.normal : ADMIN_SYSTEM_MAINTENANCE_TEXTS.overview.abnormal }}
            </a-tag>
          </template>
          <a-descriptions
            :column="1"
            size="small"
          >
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.fileCountLabel">
              {{ status.storage?.fileCount || 0 }} {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.filesSuffix }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.storageUsageLabel">
              {{ status.storage?.totalSizeFormatted || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.storagePathLabel">
              <a-typography-text
                code
                style="font-size: 12px"
              >
                {{ status.storage?.path || '-' }}
              </a-typography-text>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>

      <a-col
        :xs="24"
        :sm="24"
        :md="8"
      >
        <a-card
          :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.serverTitle"
          :loading="loading"
          class="maintenance-card"
        >
          <a-descriptions
            :column="1"
            size="small"
          >
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.javaVersionLabel">
              {{ status.system?.javaVersion || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.osLabel">
              {{ status.system?.osName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.cpuLabel">
              {{ status.system?.availableProcessors || '-' }} {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.cpuSuffix }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.memoryLabel">
              {{ status.system?.freeMemoryMB || '-' }} / {{ status.system?.maxMemoryMB || '-' }} {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.memorySuffix }}
            </a-descriptions-item>
            <a-descriptions-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cards.serverTimeLabel">
              {{ status.system?.serverTime || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <!-- 系统升级 -->
    <a-card
      :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.systemUpgrade"
      class="maintenance-card section-card"
    >
      <template #extra>
        <a-button
          :loading="gitLoading"
          @click="refreshGitInfo"
        >
          <ReloadOutlined /> {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.checkUpdates }}
        </a-button>
      </template>

      <div class="section-head dashboard-section-head">
        <div>
          <h3>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.upgradeCheckTitle }}</h3>
        </div>
        <p>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.upgradeCheckDesc }}</p>
      </div>

      <div class="upgrade-summary">
        <div class="upgrade-summary-item">
          <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.currentVersion }}</span>
          <strong>{{ gitInfo.currentVersion || '-' }}</strong>
        </div>
        <div class="upgrade-summary-item">
          <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.remoteVersion }}</span>
          <strong>{{ gitInfo.remoteVersion || '-' }}</strong>
        </div>
        <div class="upgrade-summary-item">
          <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.decision }}</span>
          <strong>{{ gitInfo.hasUpdate ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.suggestUpgrade : gitInfo.currentVersion ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.alreadyLatest : ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.waitingCheck }}</strong>
        </div>
      </div>

      <!-- 版本信息 -->
      <a-descriptions
        :column="{ xs: 1, sm: 1, md: 2 }"
        bordered
        size="small"
        style="margin-bottom: 16px"
        class="git-info-desc"
      >
        <a-descriptions-item label="当前版本">
          <a-tag color="blue">
            {{ gitInfo.currentVersion || '-' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="最新版本">
          <a-tag :color="gitInfo.hasUpdate ? 'green' : 'default'">
            {{ gitInfo.remoteVersion || '-' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="GitHub 仓库">
          <a
            v-if="gitInfo.githubRepo"
            :href="'https://github.com/' + gitInfo.githubRepo"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ gitInfo.githubRepo }}
          </a>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="最后检查">
          {{ gitInfo.lastCheckTime || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <!-- 状态提示 -->
      <a-alert
        v-if="gitInfo.hasUpdate"
        :message="ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.newVersion"
        :description="gitInfo.updateMessage"
        type="success"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #action>
          <a-button
            v-if="gitInfo.releaseUrl"
            size="small"
            type="link"
            :href="gitInfo.releaseUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.releaseNotes }}
          </a-button>
        </template>
      </a-alert>
      <a-alert
        v-else-if="gitInfo.currentVersion && gitInfo.remoteVersion"
        :message="gitInfo.updateMessage || ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.latestVersionMessage"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <div aria-live="polite">
        <a-alert
          v-if="gitInfo.error"
          :message="ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.failedTitle"
          :description="gitInfo.error"
          type="warning"
          show-icon
          style="margin-bottom: 16px"
        />
      </div>

      <a-divider />

      <div class="upgrade-actions">
        <a-button
          type="primary"
          size="large"
          @click="showUpgradeGuide"
        >
          <RocketOutlined /> {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.guideButton }}
        </a-button>
        <span
          v-if="!gitInfo.hasUpdate && gitInfo.currentVersion"
          class="upgrade-status-text"
        >
          {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.alreadyLatest }}
        </span>
      </div>

      <a-divider />

      <a-alert
        type="info"
        show-icon
      >
        <template #message>
          {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.guideTitle }}
        </template>
        <template #description>
          <ul style="padding-left: 20px; margin: 8px 0 0">
            <li
              v-for="item in ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.guideItems"
              :key="item"
            >
              {{ item }}
            </li>
          </ul>
        </template>
      </a-alert>
    </a-card>

    <!-- 升级指南弹窗 -->
    <a-modal
      v-model:open="upgradeGuideVisible"
      :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.guideModalTitle"
      :footer="null"
      :width="modalWidth"
      wrap-class-name="upgrade-modal"
    >
      <a-alert
        :message="ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.guideAlert"
        :description="upgradeGuideData.message"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      
      <div class="upgrade-command">
        <pre class="code-block">{{ upgradeGuideData.commands }}</pre>
        <a-alert
          v-if="copySuccess"
          :message="ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.copyAlertTitle"
          :description="ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.copyAlertDesc"
          type="success"
          show-icon
          style="margin-top: 12px"
        />
        <a-button
          type="primary"
          size="small"
          style="margin-top: 8px"
          @click="handleReCopy"
        >
          <CopyOutlined /> {{ copySuccess ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.copyAgain : ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.copyNow }}
        </a-button>
      </div>
      
      <a-divider />
      
      <a-typography-text type="secondary">
        <p><strong>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.stepsTitle }}</strong></p>
        <ol>
          <li
            v-for="(step, index) in (upgradeGuideData.steps || [])"
            :key="index"
          >
            {{ step }}
          </li>
        </ol>
      </a-typography-text>
      
      <div style="margin-top: 16px; display: flex; gap: 8px; flex-wrap: wrap">
        <a-button
          v-if="gitInfo.releaseUrl"
          type="link"
          :href="gitInfo.releaseUrl"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.githubReleases }}
        </a-button>
        <a-button
          type="primary"
          @click="upgradeGuideVisible = false"
        >
          {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.upgrade.acknowledged }}
        </a-button>
      </div>
    </a-modal>

    <!-- 数据备份 -->
    <a-card
      :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.backupTitle"
      class="maintenance-card section-card"
    >
      <template #extra>
        <a-button
          type="primary"
          :loading="backupLoading"
          @click="createBackup"
        >
          <DownloadOutlined /> {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.create }}
        </a-button>
      </template>

      <div class="section-head dashboard-section-head">
        <div>
          <h3>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.backupManageTitle }}</h3>
        </div>
        <p>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.backupManageDesc }}</p>
      </div>

      <div class="compact-info-row">
        <a-alert
          :message="ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.noteTitle"
          :description="ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.noteDesc"
          type="info"
          show-icon
        />
      </div>

      <div class="backup-summary">
        <div class="backup-summary-item">
          <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.count }}</span>
          <strong>{{ backups.length }}</strong>
        </div>
        <div class="backup-summary-item">
          <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.suggestion }}</span>
          <strong>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.suggestionValue }}</strong>
        </div>
      </div>

      <a-table
        :columns="backupColumns"
        :data-source="backups"
        :loading="backupListLoading"
        :pagination="false"
        row-key="filename"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="downloadBackup(record)"
              >
                <DownloadOutlined /> {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.actions.download }}
              </a-button>
              <a-popconfirm
                :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.confirmDelete"
                @confirm="deleteBackup(record)"
              >
                <a-button
                  type="link"
                  danger
                  size="small"
                >
                  <DeleteOutlined /> {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.actions.remove }}
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 日志管理 -->
    <a-card
      :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.logsTitle"
      class="maintenance-card section-card"
    >
      <div class="section-head dashboard-section-head">
        <div>
          <h3>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.logsTitle }}</h3>
        </div>
        <p>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.logsDesc }}</p>
      </div>

      <div class="compact-info-row compact-info-row--subtle">
        <span>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.hint }}</span>
      </div>

      <a-tabs v-model:active-key="logActiveTab">
        <!-- 登录日志 -->
        <a-tab-pane
          key="login"
          :tab="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.loginTab"
        >
          <div class="log-filter-card">
            <a-form
              layout="inline"
              :model="loginLogFilter"
              style="margin-bottom: 0"
              class="log-filter-form"
            >
              <a-form-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.startLabel">
                <a-date-picker
                  v-model:value="loginLogFilter.startTime"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.startPlaceholder"
                  style="width: 200px"
                  class="log-date-picker"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.endLabel">
                <a-date-picker
                  v-model:value="loginLogFilter.endTime"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.endPlaceholder"
                  style="width: 200px"
                  class="log-date-picker"
                />
              </a-form-item>
              <a-form-item>
                <a-space class="log-action-buttons">
                  <a-button
                    type="primary"
                    @click="loadLoginLogs"
                  >
                    {{ UI_TEXTS.search }}
                  </a-button>
                  <a-button @click="resetLoginLogFilter">
                    {{ UI_TEXTS.reset }}
                  </a-button>
                  <a-button
                    :loading="exportLoading"
                    @click="exportLoginLogs"
                  >
                    <DownloadOutlined /> <span class="btn-text">{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.exportCsv }}</span>
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>

          <a-table
            :columns="loginLogColumns"
            :data-source="loginLogs"
            :loading="loginLogLoading"
            :pagination="loginLogPagination"
            :scroll="{ x: 'max-content' }"
            size="small"
            @change="handleLoginLogTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'success'">
                <a-tag :color="record.success ? 'green' : 'red'">
                  {{ record.success ? ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.successTag : ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.failureTag }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'loginTime'">
                {{ formatDateTime(record.loginTime) }}
              </template>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 下载日志 -->
        <a-tab-pane
          key="download"
          :tab="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.downloadTab"
        >
          <div class="log-filter-card">
            <a-form
              layout="inline"
              :model="downloadLogFilter"
              style="margin-bottom: 0"
              class="log-filter-form"
            >
              <a-form-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.matterIdLabel">
                <a-input
                  v-model:value="downloadLogFilter.matterId"
                  :placeholder="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.matterIdPlaceholder"
                  style="width: 150px"
                  allow-clear
                  class="log-input"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.startLabel">
                <a-date-picker
                  v-model:value="downloadLogFilter.startTime"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.startPlaceholder"
                  style="width: 200px"
                  class="log-date-picker"
                />
              </a-form-item>
              <a-form-item :label="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.endLabel">
                <a-date-picker
                  v-model:value="downloadLogFilter.endTime"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.endPlaceholder"
                  style="width: 200px"
                  class="log-date-picker"
                />
              </a-form-item>
              <a-form-item>
                <a-space class="log-action-buttons">
                  <a-button
                    type="primary"
                    @click="loadDownloadLogs"
                  >
                    {{ UI_TEXTS.search }}
                  </a-button>
                  <a-button @click="resetDownloadLogFilter">
                    {{ UI_TEXTS.reset }}
                  </a-button>
                  <a-button
                    :loading="exportLoading"
                    @click="exportDownloadLogs"
                  >
                    <DownloadOutlined /> <span class="btn-text">{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.exportCsv }}</span>
                  </a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </div>

          <a-table
            :columns="downloadLogColumns"
            :data-source="downloadLogs"
            :loading="downloadLogLoading"
            :pagination="downloadLogPagination"
            :scroll="{ x: 'max-content' }"
            size="small"
            @change="handleDownloadLogTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'downloadTime'">
                {{ formatDateTime(record.downloadTime) }}
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 快捷操作指南 -->
    <a-card
      :title="ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.cliTitle"
      class="maintenance-card section-card"
    >
      <div class="section-head dashboard-section-head">
        <div>
          <h3>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.cliManageTitle }}</h3>
        </div>
        <p>{{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.sections.cliManageDesc }}</p>
      </div>

      <a-collapse>
        <a-collapse-panel
          key="1"
          :header="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cli.upgrade"
        >
          <a-typography-paragraph>
            <pre class="code-block">cd client-service
./deploy.sh --upgrade</pre>
          </a-typography-paragraph>
          <a-typography-text type="secondary">
            {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.cli.upgradeDesc }}
          </a-typography-text>
        </a-collapse-panel>
        <a-collapse-panel
          key="2"
          :header="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cli.backup"
        >
          <a-typography-paragraph>
            <pre class="code-block">./deploy.sh --backup</pre>
          </a-typography-paragraph>
          <a-typography-text type="secondary">
            {{ ADMIN_SYSTEM_MAINTENANCE_TEXTS.cli.backupDesc }}
          </a-typography-text>
        </a-collapse-panel>
        <a-collapse-panel
          key="3"
          :header="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cli.logs"
        >
          <a-typography-paragraph>
            <pre class="code-block">./deploy.sh --logs</pre>
          </a-typography-paragraph>
        </a-collapse-panel>
        <a-collapse-panel
          key="4"
          :header="ADMIN_SYSTEM_MAINTENANCE_TEXTS.cli.restart"
        >
          <a-typography-paragraph>
            <pre class="code-block">./deploy.sh --restart</pre>
          </a-typography-paragraph>
        </a-collapse-panel>
      </a-collapse>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import {
  ReloadOutlined,
  DownloadOutlined,
  DeleteOutlined,
  RocketOutlined,
  CopyOutlined,
} from '@ant-design/icons-vue'
import request from '@/api/request'
import logger from '@/utils/logger'
import { useAuthStore } from '@/stores/auth'
import { UI_CONFIRM_TEXTS, UI_FEEDBACK_TEXTS, UI_TEXTS } from '@/constants/uiTexts'
import { ADMIN_SYSTEM_MAINTENANCE_TEXTS } from '@/constants/adminTexts'

// 获取token的统一方法
const authStore = useAuthStore()
const getAuthToken = () => authStore.getToken()

/** 带超时的 fetch 封装 */
async function fetchWithTimeout(url: string, options: RequestInit = {}, timeoutMs = 60000): Promise<Response> {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs)
  try {
    return await fetch(url, { ...options, signal: controller.signal })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error(ADMIN_SYSTEM_MAINTENANCE_TEXTS.feedback.requestTimeout)
    }
    throw error
  } finally {
    clearTimeout(timeoutId)
  }
}

// 响应式宽度
const modalWidth = computed(() => {
  if (typeof window !== 'undefined' && window.innerWidth < 768) {
    return '95%'
  }
  return 600
})

interface SystemStatus {
  database?: {
    connected?: boolean
    sizeFormatted?: string
    tables?: Record<string, number>
  }
  storage?: {
    available?: boolean
    fileCount?: number
    totalSizeFormatted?: string
    path?: string
  }
  system?: {
    javaVersion?: string
    osName?: string
    availableProcessors?: number
    freeMemoryMB?: number
    maxMemoryMB?: number
    serverTime?: string
  }
}

interface GitInfo {
  currentVersion?: string
  remoteVersion?: string
  hasUpdate?: boolean
  updateMessage?: string
  releaseUrl?: string
  githubRepo?: string
  lastCheckTime?: string
  error?: string
}

interface BackupInfo {
  filename?: string
  sizeFormatted?: string
  lastModified?: string
}

interface UpgradeGuideData {
  message?: string
  commands?: string
  steps?: string[]
}

interface LoginLog {
  id?: number
  username?: string
  ipAddress?: string
  loginTime?: string
  success?: boolean
  failureReason?: string
}

interface DownloadLog {
  id?: number
  matterId?: string
  clientId?: number
  fileId?: string
  fileName?: string
  ipAddress?: string
  downloadTime?: string
}

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback
}

// 状态数据
const loading = ref(false)
const status = ref<SystemStatus>({})
// Git 信息与升级相关
const gitLoading = ref(false)
const gitInfo = ref<GitInfo>({})

// 备份相关
const backupLoading = ref(false)
const backupListLoading = ref(false)
const backups = ref<BackupInfo[]>([])

const backupColumns = [
  { title: ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.tableFilename, dataIndex: 'filename', key: 'filename' },
  { title: ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.tableSize, dataIndex: 'sizeFormatted', key: 'size', width: 100 },
  { title: ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.tableCreatedAt, dataIndex: 'lastModified', key: 'lastModified', width: 200 },
  { title: ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.tableAction, key: 'action', width: 150 },
]

// 日志管理相关
const logActiveTab = ref('login')
const exportLoading = ref(false)

// 登录日志
const loginLogLoading = ref(false)
const loginLogs = ref<LoginLog[]>([])
const loginLogFilter = ref<{
  userId?: number
  startTime?: Dayjs
  endTime?: Dayjs
}>({})
const loginLogPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `${ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.totalPrefix}${total}${ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.totalSuffix}`,
})

const loginLogColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
  { title: 'IP地址', dataIndex: 'ipAddress', key: 'ipAddress', width: 150 },
  { title: '登录时间', key: 'loginTime', width: 180 },
  { title: '状态', key: 'success', width: 100 },
  { title: '失败原因', dataIndex: 'failureReason', key: 'failureReason', ellipsis: true },
]

// 下载日志
const downloadLogLoading = ref(false)
const downloadLogs = ref<DownloadLog[]>([])
const downloadLogFilter = ref<{
  matterId?: string
  clientId?: number
  startTime?: Dayjs
  endTime?: Dayjs
}>({})
const downloadLogPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `${ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.totalPrefix}${total}${ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.totalSuffix}`,
})

const downloadLogColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '项目ID', dataIndex: 'matterId', key: 'matterId', width: 120 },
  { title: '客户ID', dataIndex: 'clientId', key: 'clientId', width: 100 },
  { title: '文件ID', dataIndex: 'fileId', key: 'fileId', width: 120 },
  { title: '文件名', dataIndex: 'fileName', key: 'fileName', ellipsis: true },
  { title: 'IP地址', dataIndex: 'ipAddress', key: 'ipAddress', width: 150 },
  { title: '下载时间', key: 'downloadTime', width: 180 },
]

// 获取系统状态
const refreshStatus = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/system/status')
    if (res.success) {
      status.value = res.data as SystemStatus
    }
  } catch (e) {
    message.error(UI_FEEDBACK_TEXTS.systemInfoLoadFailed)
  } finally {
    loading.value = false
  }
}

// 获取备份列表
const loadBackups = async () => {
  backupListLoading.value = true
  try {
    const res = await request.get('/api/admin/system/backup/list')
    if (res.success) {
      backups.value = (res.data as BackupInfo[]) || []
    }
  } catch (e) {
    logger.error('获取备份列表失败', e)
  } finally {
    backupListLoading.value = false
  }
}

// 创建备份
const createBackup = () => {
  Modal.confirm({
    title: UI_CONFIRM_TEXTS.createBackupTitle,
    content: UI_CONFIRM_TEXTS.createBackupContent,
    okText: ADMIN_SYSTEM_MAINTENANCE_TEXTS.actions.confirmBackup,
    cancelText: ADMIN_SYSTEM_MAINTENANCE_TEXTS.actions.cancel,
    onOk: async () => {
      backupLoading.value = true
      try {
        const res = await request.post('/api/admin/system/backup/database')
        if (res.success) {
          message.success(`${ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.successPrefix}${(res.data as { filename?: string }).filename || ''}`)
          loadBackups()
        } else {
          message.error(res.message || ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.failed)
        }
      } catch (e) {
        message.error(UI_FEEDBACK_TEXTS.backupCreateFailed)
      } finally {
        backupLoading.value = false
      }
    },
  })
}

// 下载备份
const downloadBackup = async (record: BackupInfo) => {
  if (!record?.filename) {
    message.error(UI_FEEDBACK_TEXTS.operationFailed)
    return
  }
  try {
    const token = getAuthToken()
    const response = await fetchWithTimeout(`/api/admin/system/backup/download/${record.filename}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }, 120000)
    
    if (!response.ok) {
      throw new Error(UI_FEEDBACK_TEXTS.backupDownloadFailed)
    }
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = record.filename
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    message.error(UI_FEEDBACK_TEXTS.backupDownloadFailed)
  }
}

// 删除备份
const deleteBackup = async (record: BackupInfo) => {
  if (!record?.filename) {
    message.error(UI_FEEDBACK_TEXTS.operationFailed)
    return
  }
  try {
    const res = await request.delete(`/api/admin/system/backup/${record.filename}`)
    if (res.success) {
      message.success(ADMIN_SYSTEM_MAINTENANCE_TEXTS.backup.removed)
      loadBackups()
    } else {
      message.error(res.message || UI_FEEDBACK_TEXTS.backupDeleteFailed)
    }
  } catch (e) {
    message.error(UI_FEEDBACK_TEXTS.backupDeleteFailed)
  }
}

// 获取 Git 信息
const refreshGitInfo = async () => {
  gitLoading.value = true
  try {
    const res = await request.get('/api/admin/system/git/info')
    if (res.success && res.data) {
      const data = res.data as {
        currentVersion?: string
        remoteVersion?: string
        hasUpdate?: boolean
        updateMessage?: string
        releaseUrl?: string
        githubRepo?: string
        lastCheckTime?: string
      }
      gitInfo.value = data
      if (data.hasUpdate) {
        message.success(ADMIN_SYSTEM_MAINTENANCE_TEXTS.feedback.updateFound)
      } else if (data.currentVersion && data.remoteVersion) {
        message.info(ADMIN_SYSTEM_MAINTENANCE_TEXTS.feedback.alreadyLatest)
      }
    }
  } catch (e) {
    message.error(UI_FEEDBACK_TEXTS.systemInfoLoadFailed)
  } finally {
    gitLoading.value = false
  }
}

// 升级指南弹窗
const upgradeGuideVisible = ref(false)
const upgradeGuideData = ref<UpgradeGuideData>({})
const copySuccess = ref(false)

// 显示升级指南
const showUpgradeGuide = async () => {
  try {
    const res = await request.post('/api/admin/system/upgrade/start', null, {
      params: {
        noCache: false,
        skipRestart: false,
      }
    })
    
    if (res.success) {
      upgradeGuideData.value = res.data as UpgradeGuideData
      upgradeGuideVisible.value = true
      copySuccess.value = false
      // 自动复制命令到剪贴板
      const success = await copyUpgradeCommands()
      copySuccess.value = success
    } else {
      message.error(res.message || ADMIN_SYSTEM_MAINTENANCE_TEXTS.feedback.guideLoadFailed)
    }
  } catch (e: unknown) {
    message.error(getErrorMessage(e, ADMIN_SYSTEM_MAINTENANCE_TEXTS.feedback.guideLoadFailed))
  }
}

// 复制升级命令
const copyUpgradeCommands = async () => {
  const commands = upgradeGuideData.value.commands || ''
  if (!commands) {
    message.warning(ADMIN_SYSTEM_MAINTENANCE_TEXTS.feedback.noCommands)
    return false
  }
  
  // 优先使用现代 API
  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(commands)
      message.success(UI_FEEDBACK_TEXTS.copySuccess)
      return true
    } catch {
      return fallbackCopy(commands)
    }
  } else {
    return fallbackCopy(commands)
  }
}

// 降级复制方案（兼容非 HTTPS 环境）
const fallbackCopy = (text: string): boolean => {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    message.success(UI_FEEDBACK_TEXTS.copySuccess)
    document.body.removeChild(textarea)
    return true
  } catch {
    message.error(UI_FEEDBACK_TEXTS.copyFailed)
    document.body.removeChild(textarea)
    return false
  }
}

// 重新复制命令
const handleReCopy = async () => {
  const success = await copyUpgradeCommands()
  copySuccess.value = success
}

// 加载登录日志
const loadLoginLogs = async () => {
  loginLogLoading.value = true
  try {
    const params: Record<string, string | number> = {
      pageNum: loginLogPagination.value.current,
      pageSize: loginLogPagination.value.pageSize,
    }
    if (loginLogFilter.value.startTime) {
      params.startTime = loginLogFilter.value.startTime.format('YYYY-MM-DD HH:mm:ss')
    }
    if (loginLogFilter.value.endTime) {
      params.endTime = loginLogFilter.value.endTime.format('YYYY-MM-DD HH:mm:ss')
    }
    if (loginLogFilter.value.userId) {
      params.userId = loginLogFilter.value.userId
    }

    const res = await request.get('/api/admin/system/logs/login', { params })
    if (res.success && res.data) {
      const data = res.data as { records?: LoginLog[]; total?: number }
      loginLogs.value = data.records || []
      loginLogPagination.value.total = data.total || 0
    }
  } catch (e) {
    message.error(ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.loadLoginFailed)
  } finally {
    loginLogLoading.value = false
  }
}

// 重置登录日志筛选
const resetLoginLogFilter = () => {
  loginLogFilter.value = {}
  loginLogPagination.value.current = 1
  loadLoginLogs()
}

// 登录日志表格变化
const handleLoginLogTableChange = (pagination: TablePaginationConfig) => {
  if (pagination.current) loginLogPagination.value.current = pagination.current
  if (pagination.pageSize) loginLogPagination.value.pageSize = pagination.pageSize
  loadLoginLogs()
}

// 导出登录日志
const exportLoginLogs = async () => {
  exportLoading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (loginLogFilter.value.startTime) {
      params.startTime = loginLogFilter.value.startTime.format('YYYY-MM-DD HH:mm:ss')
    }
    if (loginLogFilter.value.endTime) {
      params.endTime = loginLogFilter.value.endTime.format('YYYY-MM-DD HH:mm:ss')
    }
    if (loginLogFilter.value.userId) {
      params.userId = loginLogFilter.value.userId
    }

    const token = getAuthToken()
    // 构建查询字符串，确保日期时间格式正确编码
    const queryParts: string[] = []
    Object.keys(params).forEach(key => {
      if (params[key] != null && params[key] !== '') {
        queryParts.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      }
    })
    const queryString = queryParts.join('&')
    const url = `/api/admin/system/logs/login/export${queryString ? '?' + queryString : ''}`
    
    const response = await fetchWithTimeout(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (!response.ok) {
      throw new Error(ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.exportFailed)
    }
    
    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `login_logs_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
    link.click()
    window.URL.revokeObjectURL(downloadUrl)
    
    message.success(UI_FEEDBACK_TEXTS.exportSuccess)
  } catch (e) {
    message.error(UI_FEEDBACK_TEXTS.exportFailed)
  } finally {
    exportLoading.value = false
  }
}

// 加载下载日志
const loadDownloadLogs = async () => {
  downloadLogLoading.value = true
  try {
    const params: Record<string, string | number> = {
      pageNum: downloadLogPagination.value.current,
      pageSize: downloadLogPagination.value.pageSize,
    }
    if (downloadLogFilter.value.matterId) {
      params.matterId = downloadLogFilter.value.matterId
    }
    if (downloadLogFilter.value.clientId) {
      params.clientId = downloadLogFilter.value.clientId
    }
    if (downloadLogFilter.value.startTime) {
      params.startTime = downloadLogFilter.value.startTime.format('YYYY-MM-DD HH:mm:ss')
    }
    if (downloadLogFilter.value.endTime) {
      params.endTime = downloadLogFilter.value.endTime.format('YYYY-MM-DD HH:mm:ss')
    }

    const res = await request.get('/api/admin/system/logs/download', { params })
    if (res.success && res.data) {
      const data = res.data as { records?: DownloadLog[]; total?: number }
      downloadLogs.value = data.records || []
      downloadLogPagination.value.total = data.total || 0
    }
  } catch (e) {
    message.error(ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.loadDownloadFailed)
  } finally {
    downloadLogLoading.value = false
  }
}

// 重置下载日志筛选
const resetDownloadLogFilter = () => {
  downloadLogFilter.value = {}
  downloadLogPagination.value.current = 1
  loadDownloadLogs()
}

// 下载日志表格变化
const handleDownloadLogTableChange = (pagination: TablePaginationConfig) => {
  if (pagination.current) downloadLogPagination.value.current = pagination.current
  if (pagination.pageSize) downloadLogPagination.value.pageSize = pagination.pageSize
  loadDownloadLogs()
}

// 导出下载日志
const exportDownloadLogs = async () => {
  exportLoading.value = true
  try {
    const params: Record<string, string | number> = {}
    if (downloadLogFilter.value.matterId) {
      params.matterId = downloadLogFilter.value.matterId
    }
    if (downloadLogFilter.value.clientId) {
      params.clientId = downloadLogFilter.value.clientId
    }
    if (downloadLogFilter.value.startTime) {
      params.startTime = downloadLogFilter.value.startTime.format('YYYY-MM-DD HH:mm:ss')
    }
    if (downloadLogFilter.value.endTime) {
      params.endTime = downloadLogFilter.value.endTime.format('YYYY-MM-DD HH:mm:ss')
    }

    const token = getAuthToken()
    // 构建查询字符串，确保日期时间格式正确编码
    const queryParts: string[] = []
    Object.keys(params).forEach(key => {
      if (params[key] != null && params[key] !== '') {
        queryParts.push(`${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      }
    })
    const queryString = queryParts.join('&')
    const url = `/api/admin/system/logs/download/export${queryString ? '?' + queryString : ''}`
    
    const response = await fetchWithTimeout(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (!response.ok) {
      throw new Error(ADMIN_SYSTEM_MAINTENANCE_TEXTS.logs.exportFailed)
    }
    
    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `download_logs_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
    link.click()
    window.URL.revokeObjectURL(downloadUrl)
    
    message.success(UI_FEEDBACK_TEXTS.exportSuccess)
  } catch (e) {
    message.error(UI_FEEDBACK_TEXTS.exportFailed)
  } finally {
    exportLoading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string | null | undefined) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  refreshStatus()
  loadBackups()
  refreshGitInfo() // 页面加载时自动获取 Git 信息
  loadLoginLogs() // 加载登录日志
})
</script>

<style scoped>
.system-maintenance {
  display: grid;
  gap: 18px;
}

.guide-card,
.ops-metric {
  background: var(--lex-surface);
  border: 1px solid var(--lex-outline);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.ops-overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.ops-metric {
  padding: 16px 18px;
}

.ops-metric span {
  display: block;
  color: var(--text-secondary);
  font-size: 13px;
}

.ops-metric strong {
  display: block;
  margin-top: 10px;
  font-size: 24px;
  font-family: var(--font-heading);
}

.ops-metric p {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.maintenance-card :deep(.ant-card-head) {
  min-height: 64px;
  border-bottom: 1px solid rgba(21, 33, 46, 0.08);
}

.maintenance-card :deep(.ant-card-head-title) {
  font-size: 18px;
  font-family: var(--font-heading);
}

.section-card {
  margin-top: 2px;
}

/* 横向排列的卡片高度一致 */
.status-row :deep(.ant-col) {
  display: flex;
}

.status-row :deep(.ant-card) {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.status-row :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 通用：横向排列的卡片高度一致（适用于所有使用 a-row 和 a-col 的卡片布局） */
.equal-height-cards :deep(.ant-col) {
  display: flex;
}

.equal-height-cards :deep(.ant-card) {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.equal-height-cards :deep(.ant-card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.table-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  width: calc(50% - 4px);
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  border: 1px solid rgba(21, 33, 46, 0.08);
  font-size: 12px;
}

.stat-label {
  color: var(--text-tertiary);
}

.stat-value {
  font-weight: 500;
}

.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px 16px;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  margin: 0;
}

.upgrade-command {
  background: rgba(255, 255, 255, 0.74);
  padding: 16px;
  border-radius: 8px;
  border: 1px solid rgba(21, 33, 46, 0.08);
  box-shadow: var(--shadow-xs);
}

.version-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.version-label {
  color: var(--text-tertiary);
  font-size: 14px;
}

.version-tag {
  font-size: 14px;
}

.version-arrow {
  color: var(--lex-muted-soft);
  margin: 0 4px;
}

.version-actions {
  flex-shrink: 0;
}

.upgrade-options {
  background: rgba(255, 255, 255, 0.7);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid rgba(21, 33, 46, 0.08);
}

.option-item {
  margin-bottom: 12px;
}

.option-item:last-child {
  margin-bottom: 0;
}

.system-maintenance :deep(.ant-table-thead > tr > th) {
  background: var(--lex-bg);
  font-weight: 700;
}

.system-maintenance :deep(.ant-tabs-nav::before) {
  border-bottom-color: rgba(21, 33, 46, 0.08);
}

.system-maintenance :deep(.ant-form-inline) {
  row-gap: 8px;
}

@media (max-width: 1100px) {
  .ops-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .ops-overview {
    grid-template-columns: 1fr;
  }

  .compact-info-row--subtle,
  .backup-summary,
  .upgrade-summary {
    grid-template-columns: 1fr;
    display: grid;
  }

  .log-filter-card {
    padding: 14px;
  }

}

.option-desc {
  margin-left: 24px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-tertiary);
  line-height: 1.5;
}

.compact-info-row {
  margin-bottom: 12px;
}

.compact-info-row--subtle {
  display: flex;
  align-items: center;
  min-height: 40px;
  padding: 0 12px;
  margin-bottom: 12px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
  color: var(--text-secondary);
  font-size: 13px;
}

.backup-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.backup-summary-item {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.backup-summary-item span {
  color: var(--text-tertiary);
  font-size: 12px;
}

.backup-summary-item strong {
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.5;
}

.upgrade-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.upgrade-summary-item {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.upgrade-summary-item span {
  color: var(--text-tertiary);
  font-size: 12px;
}

.upgrade-summary-item strong {
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.5;
}

.upgrade-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upgrade-status-text {
  color: var(--lex-primary-soft);
}

.modal-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .system-maintenance :deep(.ant-card) {
    border-radius: 8px;
  }

  .system-maintenance :deep(.ant-card-head) {
    padding: 12px 16px;
    min-height: auto;
    flex-wrap: wrap;
  }

  .system-maintenance :deep(.ant-card-head-wrapper) {
    flex-wrap: wrap;
    gap: 8px;
  }

  .system-maintenance :deep(.ant-card-head-title) {
    font-size: 14px;
    flex: 1 1 auto;
  }

  .system-maintenance :deep(.ant-card-extra) {
    margin-left: 0 !important;
  }

  .system-maintenance :deep(.ant-card-body) {
    padding: 12px 16px;
  }

  .system-maintenance :deep(.ant-descriptions-item-label) {
    font-size: 12px;
  }

  .system-maintenance :deep(.ant-descriptions-item-content) {
    font-size: 12px;
    word-break: break-all;
  }

  .git-info-desc :deep(.ant-descriptions-view) {
    overflow-x: auto;
  }

  .table-stats {
    gap: 6px;
  }

  .stat-item {
    width: 100%;
    font-size: 11px;
    padding: 6px 8px;
  }

  .upgrade-options {
    padding: 12px;
  }

  .option-desc {
    font-size: 11px;
    margin-left: 22px;
  }

  .upgrade-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .modal-actions {
    flex-direction: column;
    width: 100%;
  }

  .modal-actions :deep(.ant-btn) {
    width: 100%;
  }

  .system-maintenance :deep(.ant-btn) {
    height: 36px;
    padding: 0 12px;
    font-size: 13px;
  }

  .system-maintenance :deep(.ant-table-wrapper) {
    overflow-x: auto;
  }

  .system-maintenance :deep(.ant-table) {
    font-size: 12px;
    min-width: 400px;
  }

  .system-maintenance :deep(.ant-table-thead > tr > th) {
    padding: 8px;
    font-size: 12px;
    white-space: nowrap;
  }

  .system-maintenance :deep(.ant-table-tbody > tr > td) {
    padding: 8px;
    font-size: 12px;
  }

  .system-maintenance :deep(.ant-alert) {
    padding: 8px 12px;
  }

  .system-maintenance :deep(.ant-alert-message) {
    font-size: 13px;
  }

  .system-maintenance :deep(.ant-alert-description) {
    font-size: 12px;
  }

  .system-maintenance :deep(.ant-collapse-header) {
    padding: 12px 16px !important;
    font-size: 13px;
  }

  .code-block {
    font-size: 11px;
    padding: 10px 12px;
    overflow-x: auto;
  }

  .system-maintenance :deep(.ant-steps) {
    flex-wrap: wrap;
  }

  .system-maintenance :deep(.ant-steps-item) {
    flex: 1 1 auto;
  }

  .system-maintenance :deep(.ant-steps-item-title) {
    font-size: 12px;
  }
}

/* 升级指南弹窗移动端优化 */
@media (max-width: 480px) {
  .system-maintenance :deep(.ant-card-head) {
    padding: 10px 12px;
  }

  .system-maintenance :deep(.ant-card-body) {
    padding: 10px 12px;
  }

  .stat-item {
    font-size: 10px;
    padding: 4px 6px;
  }

  .system-maintenance :deep(.ant-btn) {
    height: 32px;
    padding: 0 10px;
    font-size: 12px;
  }

  .system-maintenance :deep(.ant-table-thead > tr > th),
  .system-maintenance :deep(.ant-table-tbody > tr > td) {
    padding: 6px;
    font-size: 11px;
  }

  .code-block {
    font-size: 10px;
    padding: 8px 10px;
  }

  .upgrade-options {
    padding: 10px;
  }

  .option-desc {
    font-size: 10px;
  }

  .system-maintenance :deep(.ant-steps) {
    font-size: 11px;
  }

  .system-maintenance :deep(.ant-steps-item-icon) {
    width: 24px;
    height: 24px;
    line-height: 24px;
    font-size: 12px;
  }
}

/* 日志筛选表单移动端优化 */
.log-filter-card {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
  padding: 14px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.log-filter-form {
  flex-wrap: wrap;
}

.log-action-buttons {
  flex-wrap: wrap;
}


@media (max-width: 768px) {
  .log-filter-form :deep(.ant-form-item) {
    width: 100%;
    margin-bottom: 12px;
  }

  .log-filter-form :deep(.ant-form-item-label) {
    width: 100%;
    text-align: left;
    padding-bottom: 4px;
  }

  .log-filter-form :deep(.ant-form-item-control) {
    width: 100%;
  }

  .log-filter-form :deep(.ant-picker),
  .log-date-picker {
    width: 100% !important;
  }

  .log-filter-form :deep(.ant-input),
  .log-input {
    width: 100% !important;
  }

  .log-action-buttons {
    width: 100%;
    flex-direction: column;
    gap: 8px;
  }

  .log-action-buttons :deep(.ant-btn) {
    width: 100%;
  }

  /* 日志表格移动端优化 */
  .system-maintenance :deep(.ant-tabs-content) {
    overflow-x: auto;
  }

  .system-maintenance :deep(.ant-tabs-tab) {
    padding: 8px 12px;
    font-size: 13px;
  }

  .system-maintenance :deep(.ant-table) {
    font-size: 11px;
  }

  .system-maintenance :deep(.ant-table-thead > tr > th) {
    padding: 6px 4px;
    font-size: 11px;
  }

  .system-maintenance :deep(.ant-table-tbody > tr > td) {
    padding: 6px 4px;
    font-size: 11px;
  }

  .system-maintenance :deep(.ant-tag) {
    font-size: 11px;
    padding: 2px 6px;
  }

  .system-maintenance :deep(.ant-pagination) {
    margin: 12px 0;
  }

  .system-maintenance :deep(.ant-pagination-item),
  .system-maintenance :deep(.ant-pagination-prev),
  .system-maintenance :deep(.ant-pagination-next) {
    min-width: 28px;
    height: 28px;
    line-height: 28px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .log-action-buttons .btn-text {
    display: none;
  }

  .system-maintenance :deep(.ant-table-thead > tr > th),
  .system-maintenance :deep(.ant-table-tbody > tr > td) {
    padding: 4px 2px;
    font-size: 10px;
  }

  .system-maintenance :deep(.ant-table) {
    font-size: 10px;
  }
}
</style>
