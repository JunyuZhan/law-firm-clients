<template>
  <div class="system-info-container">
    <section class="page-intro">
      <div>
        <p class="intro-text">
          版本与健康状态；可按需刷新。
        </p>
      </div>
      <div class="intro-actions">
        <a-button @click="loadRuntimeInfo">
          刷新版本
        </a-button>
        <a-button
          type="primary"
          @click="loadDependencyStatus"
        >
          刷新健康状态
        </a-button>
      </div>
    </section>

    <section class="stats-grid">
      <div class="stats-card">
        <span class="stats-label">产品版本</span>
        <strong>{{ runtimeInfo.productVersion || '-' }}</strong>
      </div>
      <div class="stats-card success">
        <span class="stats-label">依赖总状态</span>
        <strong>{{ formatDependencyStatus(dependencyStatus.overallStatus) }}</strong>
      </div>
      <div class="stats-card info">
        <span class="stats-label">推荐部署</span>
        <strong>{{ runtimeInfo.recommendedMode || 'Docker Compose' }}</strong>
      </div>
      <div class="stats-card danger">
        <span class="stats-label">构建时间</span>
        <strong>{{ runtimeInfo.buildTime || '-' }}</strong>
      </div>
    </section>

    <section class="info-grid">
      <article class="config-card">
        <div class="section-head">
          <div>
            <h3>版本与构建信息</h3>
          </div>
        </div>
        <a-descriptions
          :column="1"
          bordered
          size="small"
        >
          <a-descriptions-item label="应用名称">
            {{ runtimeInfo.applicationName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="产品版本">
            {{ runtimeInfo.productVersion || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="后端版本">
            {{ runtimeInfo.backendVersion || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="提交号">
            <code>{{ runtimeInfo.commitSha || '-' }}</code>
          </a-descriptions-item>
          <a-descriptions-item label="构建时间">
            {{ runtimeInfo.buildTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="服务器时间">
            {{ runtimeInfo.serverTime || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </article>

      <article class="config-card">
        <div class="section-head">
          <div>
            <h3>关键依赖状态</h3>
          </div>
        </div>
        <div class="dependency-summary">
          <span>总体状态</span>
          <a-tag :color="statusColorMap[dependencyStatus.overallStatus] || 'default'">
            {{ formatDependencyStatus(dependencyStatus.overallStatus) }}
          </a-tag>
        </div>
        <div class="dependency-list">
          <div
            v-for="item in dependencyStatus.items"
            :key="item.key"
            class="dependency-item"
          >
            <div class="dependency-row">
              <span class="dependency-name">{{ item.label }}</span>
              <a-tag :color="statusColorMap[item.status] || 'default'">
                {{ formatDependencyStatus(item.status) }}
              </a-tag>
            </div>
            <div class="dependency-meta">
              <span
                v-for="(value, detailKey) in normalizeDependencyDetails(item.details)"
                :key="detailKey"
                class="meta-item"
              >
                {{ detailKey }}: {{ value }}
              </span>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="info-grid">
      <article class="config-card">
        <div class="section-head">
          <div>
            <h3>运维边界</h3>
          </div>
        </div>
        <ul class="plain-list">
          <li>业务管理员负责系统配置、通知模板、API Key、文件治理和日常业务操作。</li>
          <li>运维管理员负责程序升级、镜像替换、容器重启、数据库迁移、回滚和部署目录维护。</li>
          <li>任何正式升级都应先备份，再执行冒烟测试，再决定是否继续发布或回滚。</li>
        </ul>
      </article>

      <article class="config-card">
        <div class="section-head">
          <div>
            <h3>发布前验收</h3>
          </div>
        </div>
        <ul class="plain-list">
          <li
            v-for="item in releaseChecklist"
            :key="item"
          >
            {{ item }}
          </li>
        </ul>
      </article>
    </section>

    <section class="config-card">
      <div class="section-head">
        <div>
          <h3>交付文档入口</h3>
        </div>
        <p>后台先给出统一文档位置，方便交付、升级和回归测试时快速取用。</p>
      </div>

      <div class="doc-summary">
        <div class="doc-summary-item">
          <span>文档数量</span>
          <strong>{{ deliveryDocs.length }}</strong>
        </div>
        <div class="doc-summary-item">
          <span>适用场景</span>
          <strong>交付、升级、回归测试</strong>
        </div>
      </div>

      <div class="doc-grid">
        <article
          v-for="doc in deliveryDocs"
          :key="doc.path"
          class="doc-item"
        >
          <div class="doc-head">
            <div>
              <div class="doc-title">
                {{ doc.title }}
              </div>
              <div class="doc-desc">
                {{ doc.description }}
              </div>
            </div>
            <a-button
              type="link"
              @click="copyDocPath(doc.path)"
            >
              复制路径
            </a-button>
          </div>
          <div class="doc-path">
            {{ doc.path }}
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  getSystemDependencyStatus,
  getSystemRuntimeInfo,
  type DependencyStatusItem,
} from '@/api/config'

const runtimeInfo = reactive({
  applicationName: '',
  productVersion: '',
  backendVersion: '',
  commitSha: '',
  buildTime: '',
  recommendedMode: 'Docker Compose',
  serverTime: '',
})

const dependencyStatus = reactive<{
  overallStatus: string
  items: DependencyStatusItem[]
}>({
  overallStatus: 'UNKNOWN',
  items: [],
})

const statusColorMap: Record<string, string> = {
  UP: 'success',
  DOWN: 'error',
  OUT_OF_SERVICE: 'error',
  DEGRADED: 'warning',
  UNKNOWN: 'default',
}

const releaseChecklist = [
  '确认产品版本、构建时间、提交号和部署清单一致',
  '确认数据库初始化脚本与环境变量已经核对',
  '确认升级前备份编号和回滚版本已记录',
  '确认快速 API 冒烟测试和前端构建校验已执行',
  '确认系统健康状态无关键依赖告警',
]

const deliveryDocs = [
  {
    title: '用户手册',
    description: '用于业务管理员和客户的日常使用说明。',
    path: '/Users/apple/Documents/Project/law-firm-clients/docs/guides/USER_MANUAL.md',
  },
  {
    title: '系统配置指南',
    description: '用于品牌、门户、回调和基础系统参数配置。',
    path: '/Users/apple/Documents/Project/law-firm-clients/docs/guides/SYSTEM_CONFIG_GUIDE.md',
  },
  {
    title: '部署与升级手册',
    description: '用于首次部署、升级、回滚和目录规范核查。',
    path: '/Users/apple/Documents/Project/law-firm-clients/docs/operations/DEPLOYMENT_UPGRADE_GUIDE.md',
  },
  {
    title: '部署后冒烟测试',
    description: '用于升级完成后快速验证后台、门户和函件验证主链路。',
    path: '/Users/apple/Documents/Project/law-firm-clients/docs/operations/DEPLOYMENT_SMOKE_TEST.md',
  },
  {
    title: '测试台账模板',
    description: '用于记录版本、环境、结果、缺陷和是否回滚。',
    path: '/Users/apple/Documents/Project/law-firm-clients/docs/operations/TEST_LEDGER_TEMPLATE.md',
  },
]

async function loadRuntimeInfo() {
  try {
    const res = await getSystemRuntimeInfo()
    if (res.success && res.data) {
      Object.assign(runtimeInfo, res.data)
    }
  } catch {
    message.error('加载系统运行时信息失败')
  }
}

async function loadDependencyStatus() {
  try {
    const res = await getSystemDependencyStatus()
    if (res.success && res.data) {
      dependencyStatus.overallStatus = res.data.overallStatus || 'UNKNOWN'
      dependencyStatus.items = Array.isArray(res.data.items) ? res.data.items : []
    }
  } catch {
    message.error('加载依赖状态失败')
  }
}

function formatDependencyStatus(status: string) {
  const mapping: Record<string, string> = {
    UP: '正常',
    DOWN: '异常',
    OUT_OF_SERVICE: '停服',
    DEGRADED: '部分异常',
    UNKNOWN: '未知',
  }
  return mapping[status] || status || '未知'
}

function normalizeDependencyDetails(details: Record<string, string | number | boolean>) {
  if (!details || Object.keys(details).length === 0) {
    return { message: '无额外信息' }
  }
  return Object.fromEntries(
    Object.entries(details).map(([key, value]) => [key, String(value)]),
  )
}

async function copyDocPath(path: string) {
  try {
    await navigator.clipboard.writeText(path)
    message.success('文档路径已复制')
  } catch {
    message.error('复制失败，请手动复制')
  }
}

onMounted(() => {
  loadRuntimeInfo()
  loadDependencyStatus()
})
</script>

<style scoped>
.system-info-container {
  display: grid;
  gap: 18px;
}

.intro-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h3 {
  margin: 6px 0 0;
  font-size: 24px;
  color: var(--text-primary);
}

.section-head p {
  margin: 0;
  max-width: 420px;
  color: var(--text-secondary);
  line-height: 1.75;
}

.dependency-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  color: var(--text-secondary);
}

.dependency-list {
  display: grid;
  gap: 12px;
}

.dependency-item,
.doc-item {
  padding: 14px 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.dependency-row,
.doc-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.dependency-name,
.doc-title {
  font-weight: 600;
  color: var(--text-primary);
}

.dependency-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-top: 10px;
}

.meta-item,
.doc-path {
  font-size: 12px;
  color: var(--text-tertiary);
  word-break: break-all;
}

.plain-list {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  line-height: 1.9;
}

.doc-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.doc-summary-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.doc-summary-item span {
  color: var(--text-tertiary);
  font-size: 12px;
}

.doc-summary-item strong {
  color: var(--text-primary);
  font-size: 16px;
  line-height: 1.6;
}

.doc-grid {
  display: grid;
  gap: 12px;
}

.doc-desc {
  margin-top: 4px;
  color: var(--text-secondary);
  line-height: 1.7;
}

@media (max-width: 900px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .intro-actions {
    width: 100%;
  }

  .quick-link-head,
  .section-head,
  .dependency-row,
  .doc-head,
  .doc-summary {
    display: grid;
  }
}
</style>
