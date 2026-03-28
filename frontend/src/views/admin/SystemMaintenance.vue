<template>
  <div class="system-maintenance">
    <a-page-header
      title="系统维护"
      sub-title="备份、状态监控"
    >
      <template #extra>
        <a-button
          type="primary"
          :loading="loading"
          @click="refreshStatus"
        >
          <ReloadOutlined /> 刷新状态
        </a-button>
      </template>
    </a-page-header>

    <!-- 系统状态卡片 -->
    <a-row
      :gutter="[16, 16]"
      style="margin-bottom: 24px"
      class="status-row"
    >
      <a-col
        :xs="24"
        :sm="24"
        :md="8"
      >
        <a-card
          title="数据库状态"
          :loading="loading"
        >
          <template #extra>
            <a-tag :color="status.database?.connected ? 'green' : 'red'">
              {{ status.database?.connected ? '正常' : '异常' }}
            </a-tag>
          </template>
          <a-descriptions
            :column="1"
            size="small"
          >
            <a-descriptions-item label="数据库大小">
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
              <span class="stat-value">{{ count }} 条</span>
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
          title="文件存储"
          :loading="loading"
        >
          <template #extra>
            <a-tag :color="status.storage?.available ? 'green' : 'red'">
              {{ status.storage?.available ? '正常' : '异常' }}
            </a-tag>
          </template>
          <a-descriptions
            :column="1"
            size="small"
          >
            <a-descriptions-item label="文件数量">
              {{ status.storage?.fileCount || 0 }} 个
            </a-descriptions-item>
            <a-descriptions-item label="占用空间">
              {{ status.storage?.totalSizeFormatted || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="存储路径">
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
          title="服务器信息"
          :loading="loading"
        >
          <a-descriptions
            :column="1"
            size="small"
          >
            <a-descriptions-item label="Java 版本">
              {{ status.system?.javaVersion || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="操作系统">
              {{ status.system?.osName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="CPU 核心">
              {{ status.system?.availableProcessors || '-' }} 核
            </a-descriptions-item>
            <a-descriptions-item label="可用内存">
              {{ status.system?.freeMemoryMB || '-' }} / {{ status.system?.maxMemoryMB || '-' }} MB
            </a-descriptions-item>
            <a-descriptions-item label="服务器时间">
              {{ status.system?.serverTime || '-' }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <!-- 系统升级 -->
    <a-card
      title="系统升级"
      style="margin-bottom: 24px"
    >
      <template #extra>
        <a-button
          :loading="gitLoading"
          @click="refreshGitInfo"
        >
          <ReloadOutlined /> 检查更新
        </a-button>
      </template>

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
        message="发现新版本"
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
          >
            查看更新日志
          </a-button>
        </template>
      </a-alert>
      <a-alert
        v-else-if="gitInfo.currentVersion && gitInfo.remoteVersion"
        :message="gitInfo.updateMessage || '已是最新版本'"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <a-alert
        v-if="gitInfo.error"
        message="检查失败"
        :description="gitInfo.error"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-divider />

      <div class="upgrade-actions">
        <a-button
          type="primary"
          size="large"
          @click="showUpgradeGuide"
        >
          <RocketOutlined /> 查看升级指南
        </a-button>
        <span
          v-if="!gitInfo.hasUpdate && gitInfo.currentVersion"
          class="upgrade-status-text"
        >
          当前已是最新版本
        </span>
      </div>

      <a-divider />

      <a-alert
        type="info"
        show-icon
      >
        <template #message>
          升级说明
        </template>
        <template #description>
          <ul style="padding-left: 20px; margin: 8px 0 0">
            <li>容器化部署需要在服务器上手动执行升级命令</li>
            <li>点击"查看升级指南"获取具体操作步骤</li>
            <li>升级过程中服务可能会短暂中断</li>
            <li>建议在低峰期执行升级操作</li>
          </ul>
        </template>
      </a-alert>
    </a-card>

    <!-- 升级指南弹窗 -->
    <a-modal
      v-model:open="upgradeGuideVisible"
      title="升级指南"
      :footer="null"
      :width="modalWidth"
      wrap-class-name="upgrade-modal"
    >
      <a-alert
        message="请在服务器上执行以下命令完成升级"
        :description="upgradeGuideData.message"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      
      <div class="upgrade-command">
        <pre class="code-block">{{ upgradeGuideData.commands }}</pre>
        <a-alert
          v-if="copySuccess"
          message="命令已自动复制到剪贴板"
          description="您可以直接在终端中粘贴执行（记得修改路径为实际项目目录）"
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
          <CopyOutlined /> {{ copySuccess ? '重新复制' : '复制命令' }}
        </a-button>
      </div>
      
      <a-divider />
      
      <a-typography-text type="secondary">
        <p><strong>操作步骤：</strong></p>
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
        >
          查看 GitHub Releases
        </a-button>
        <a-button
          type="primary"
          @click="upgradeGuideVisible = false"
        >
          知道了
        </a-button>
      </div>
    </a-modal>

    <!-- 数据备份 -->
    <a-card
      title="数据备份"
      style="margin-bottom: 24px"
    >
      <template #extra>
        <a-button
          type="primary"
          :loading="backupLoading"
          @click="createBackup"
        >
          <DownloadOutlined /> 创建备份
        </a-button>
      </template>

      <a-alert
        message="备份说明"
        description="点击「创建备份」会导出数据库中的所有数据为 SQL 文件。建议定期备份，特别是在升级系统前。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-table
        :columns="backupColumns"
        :data-source="backups"
        :loading="backupListLoading"
        :pagination="false"
        row-key="filename"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="downloadBackup(record)"
              >
                <DownloadOutlined /> 下载
              </a-button>
              <a-popconfirm
                title="确定删除此备份？"
                @confirm="deleteBackup(record)"
              >
                <a-button
                  type="link"
                  danger
                  size="small"
                >
                  <DeleteOutlined /> 删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 日志管理 -->
    <a-card
      title="日志管理"
      style="margin-bottom: 24px"
    >
      <a-tabs v-model:active-key="logActiveTab">
        <!-- 登录日志 -->
        <a-tab-pane
          key="login"
          tab="登录日志"
        >
          <a-form
            layout="inline"
            :model="loginLogFilter"
            style="margin-bottom: 16px"
            class="log-filter-form"
          >
            <a-form-item label="开始时间">
              <a-date-picker
                v-model:value="loginLogFilter.startTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择开始时间"
                style="width: 200px"
                class="log-date-picker"
              />
            </a-form-item>
            <a-form-item label="结束时间">
              <a-date-picker
                v-model:value="loginLogFilter.endTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择结束时间"
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
                  查询
                </a-button>
                <a-button @click="resetLoginLogFilter">
                  重置
                </a-button>
                <a-button
                  :loading="exportLoading"
                  @click="exportLoginLogs"
                >
                  <DownloadOutlined /> <span class="btn-text">导出 CSV</span>
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
          
          <a-table
            :columns="loginLogColumns"
            :data-source="loginLogs"
            :loading="loginLogLoading"
            :pagination="loginLogPagination"
            :scroll="{ x: 'max-content' }"
            @change="handleLoginLogTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'success'">
                <a-tag :color="record.success ? 'green' : 'red'">
                  {{ record.success ? '成功' : '失败' }}
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
          tab="下载日志"
        >
          <a-form
            layout="inline"
            :model="downloadLogFilter"
            style="margin-bottom: 16px"
            class="log-filter-form"
          >
            <a-form-item label="项目ID">
              <a-input
                v-model:value="downloadLogFilter.matterId"
                placeholder="输入项目ID"
                style="width: 150px"
                allow-clear
                class="log-input"
              />
            </a-form-item>
            <a-form-item label="开始时间">
              <a-date-picker
                v-model:value="downloadLogFilter.startTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择开始时间"
                style="width: 200px"
                class="log-date-picker"
              />
            </a-form-item>
            <a-form-item label="结束时间">
              <a-date-picker
                v-model:value="downloadLogFilter.endTime"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择结束时间"
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
                  查询
                </a-button>
                <a-button @click="resetDownloadLogFilter">
                  重置
                </a-button>
                <a-button
                  :loading="exportLoading"
                  @click="exportDownloadLogs"
                >
                  <DownloadOutlined /> <span class="btn-text">导出 CSV</span>
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
          
          <a-table
            :columns="downloadLogColumns"
            :data-source="downloadLogs"
            :loading="downloadLogLoading"
            :pagination="downloadLogPagination"
            :scroll="{ x: 'max-content' }"
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
    <a-card title="命令行操作（高级）">
      <a-collapse>
        <a-collapse-panel
          key="1"
          header="一键升级"
        >
          <a-typography-paragraph>
            <pre class="code-block">cd client-service
./deploy.sh --upgrade</pre>
          </a-typography-paragraph>
          <a-typography-text type="secondary">
            自动执行：备份 → 拉取代码 → 重建镜像 → 重启服务
          </a-typography-text>
        </a-collapse-panel>
        <a-collapse-panel
          key="2"
          header="命令行备份"
        >
          <a-typography-paragraph>
            <pre class="code-block">./deploy.sh --backup</pre>
          </a-typography-paragraph>
          <a-typography-text type="secondary">
            备份数据库 + 文件 + 配置到 backups/ 目录
          </a-typography-text>
        </a-collapse-panel>
        <a-collapse-panel
          key="3"
          header="查看日志"
        >
          <a-typography-paragraph>
            <pre class="code-block">./deploy.sh --logs</pre>
          </a-typography-paragraph>
        </a-collapse-panel>
        <a-collapse-panel
          key="4"
          header="重启服务"
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
      throw new Error('请求超时，请稍后重试')
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
  { title: '文件名', dataIndex: 'filename', key: 'filename' },
  { title: '大小', dataIndex: 'sizeFormatted', key: 'size', width: 100 },
  { title: '创建时间', dataIndex: 'lastModified', key: 'lastModified', width: 200 },
  { title: '操作', key: 'action', width: 150 },
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
  showTotal: (total: number) => `共 ${total} 条`,
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
  showTotal: (total: number) => `共 ${total} 条`,
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
    message.error('获取系统状态失败')
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
    title: '确认创建数据库备份',
    content: '备份操作可能需要一定时间，期间请勿进行其他数据库操作。确定要继续吗？',
    okText: '确认备份',
    cancelText: '取消',
    onOk: async () => {
      backupLoading.value = true
      try {
        const res = await request.post('/api/admin/system/backup/database')
        if (res.success) {
          message.success(`备份创建成功：${(res.data as { filename?: string }).filename || ''}`)
          loadBackups()
        } else {
          message.error(res.message || '备份失败')
        }
      } catch (e) {
        message.error('创建备份失败')
      } finally {
        backupLoading.value = false
      }
    },
  })
}

// 下载备份
const downloadBackup = async (record: BackupInfo) => {
  if (!record?.filename) {
    message.error('无法获取备份文件名')
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
      throw new Error('下载失败')
    }
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = record.filename
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    message.error('下载失败')
  }
}

// 删除备份
const deleteBackup = async (record: BackupInfo) => {
  if (!record?.filename) {
    message.error('无法获取备份文件名')
    return
  }
  try {
    const res = await request.delete(`/api/admin/system/backup/${record.filename}`)
    if (res.success) {
      message.success('删除成功')
      loadBackups()
    } else {
      message.error(res.message || '删除失败')
    }
  } catch (e) {
    message.error('删除失败')
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
        message.success('发现新版本可用')
      } else if (data.currentVersion && data.remoteVersion) {
        message.info('当前已是最新版本')
      }
    }
  } catch (e) {
    message.error('获取版本信息失败')
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
      message.error(res.message || '获取升级指南失败')
    }
  } catch (e: unknown) {
    message.error(getErrorMessage(e, '获取升级指南失败'))
  }
}

// 复制升级命令
const copyUpgradeCommands = async () => {
  const commands = upgradeGuideData.value.commands || ''
  if (!commands) {
    message.warning('没有可复制的命令')
    return false
  }
  
  // 优先使用现代 API
  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(commands)
      message.success('命令已复制到剪贴板，可直接粘贴到终端执行')
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
    message.success('命令已复制到剪贴板，可直接粘贴到终端执行')
    document.body.removeChild(textarea)
    return true
  } catch {
    message.error('复制失败，请手动复制')
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
    message.error('加载登录日志失败')
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
      throw new Error('导出失败')
    }
    
    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `login_logs_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
    link.click()
    window.URL.revokeObjectURL(downloadUrl)
    
    message.success('导出成功')
  } catch (e) {
    message.error('导出失败')
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
    message.error('加载下载日志失败')
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
      throw new Error('导出失败')
    }
    
    const blob = await response.blob()
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = `download_logs_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
    link.click()
    window.URL.revokeObjectURL(downloadUrl)
    
    message.success('导出成功')
  } catch (e) {
    message.error('导出失败')
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
  padding: 24px;
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
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
}

.stat-label {
  color: #666;
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
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
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
  color: #666;
  font-size: 14px;
}

.version-tag {
  font-size: 14px;
}

.version-arrow {
  color: #999;
  margin: 0 4px;
}

.version-actions {
  flex-shrink: 0;
}

.upgrade-options {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.option-item {
  margin-bottom: 12px;
}

.option-item:last-child {
  margin-bottom: 0;
}

.option-desc {
  margin-left: 24px;
  margin-top: 4px;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.upgrade-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upgrade-status-text {
  color: #1890ff;
}

.modal-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .system-maintenance {
    padding: 12px;
  }

  .system-maintenance :deep(.ant-page-header) {
    padding: 12px 0;
  }

  .system-maintenance :deep(.ant-page-header-heading) {
    flex-wrap: wrap;
    gap: 8px;
  }

  .system-maintenance :deep(.ant-page-header-heading-left) {
    flex: 1 1 100%;
  }

  .system-maintenance :deep(.ant-page-header-heading-extra) {
    flex: 1 1 100%;
    margin-left: 0 !important;
  }

  .system-maintenance :deep(.ant-page-header-heading-title) {
    font-size: 18px;
  }

  .system-maintenance :deep(.ant-page-header-heading-sub-title) {
    display: none;
  }

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
:deep(.upgrade-modal .ant-modal) {
  margin: 0;
  max-width: 100vw;
  top: 0;
  padding-bottom: 0;
}

:deep(.upgrade-modal .ant-modal-content) {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

:deep(.upgrade-modal .ant-modal-body) {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

@media (min-width: 769px) {
  :deep(.upgrade-modal .ant-modal) {
    margin: 0 auto;
    top: 50px;
    padding-bottom: 24px;
  }
  
  :deep(.upgrade-modal .ant-modal-content) {
    height: auto;
  }
  
  :deep(.upgrade-modal .ant-modal-body) {
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .system-maintenance {
    padding: 8px;
  }

  .system-maintenance :deep(.ant-page-header) {
    padding: 8px 0;
  }

  .system-maintenance :deep(.ant-page-header-heading-title) {
    font-size: 16px;
  }

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
