<template>
  <div class="matter-detail-container">
    <a-layout>
      <AppHeader
        variant="detail"
        :title="'项目详情'"
        :welcome-text="welcomeText"
        :show-back="true"
        :show-back-text="true"
        :show-mobile-menu="false"
        @back="goBack"
      />
      <a-layout-content
        id="main-content"
        class="content"
        tabindex="-1"
      >
        <a-spin :spinning="loading">
          <div
            v-if="matterDetail"
            class="detail-shell"
          >
            <section class="hero-panel glass-panel">
              <div class="hero-copy">
                <h1 class="hero-title">
                  {{ matterData.matterName || `项目 ${matterId}` }}
                </h1>
                <p class="hero-subtitle">
                  {{ matterDetail.clientName }} · 进度、材料与节点见下方各区块。
                </p>

                <div class="hero-meta">
                  <article class="hero-meta-item">
                    <strong>项目编号</strong>
                    <span>{{ matterData.matterNo || '未同步' }}</span>
                  </article>
                  <article class="hero-meta-item">
                    <strong>项目类型</strong>
                    <span>{{ matterData.matterTypeName || matterData.matterType || '未分类' }}</span>
                  </article>
                  <article class="hero-meta-item">
                    <strong>有效期至</strong>
                    <span>{{ formatDate(matterDetail.expiresAt) }}</span>
                  </article>
                </div>
              </div>

              <div class="hero-side">
                <a-tag
                  class="hero-status"
                  :color="getStatusColor(displayStatusRaw)"
                >
                  {{ displayStatus }}
                </a-tag>

                <div class="hero-progress-card">
                  <span class="hero-progress-label">当前阶段</span>
                  <strong>{{ currentStageLabel }}</strong>
                  <a-progress
                    class="progress-bar"
                    :percent="normalizedProgress"
                    :status="normalizedProgress === 100 ? 'success' : 'active'"
                  />
                  <span class="hero-progress-foot">
                    最后更新 {{ formatDate(displayLastUpdate) }}
                  </span>
                </div>

                <div class="hero-action-card">
                  <span class="hero-progress-label">建议</span>
                  <strong>{{ heroActionTitle }}</strong>
                  <p>{{ heroActionDescription }}</p>
                  <a-button
                    type="primary"
                    block
                    class="hero-action-btn"
                    @click="showUploadModal = true"
                  >
                    上传补充文件
                  </a-button>
                </div>
              </div>
            </section>

            <section class="detail-grid">
              <div class="detail-main">
                <a-card
                  title="项目信息"
                  class="detail-block"
                >
                  <a-descriptions
                    :column="{ xs: 1, sm: 2 }"
                    bordered
                    size="small"
                  >
                    <a-descriptions-item label="项目名称">
                      {{ matterData.matterName || '-' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="项目编号">
                      {{ matterData.matterNo || '-' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="客户名称">
                      {{ matterDetail.clientName }}
                    </a-descriptions-item>
                    <a-descriptions-item label="项目类型">
                      {{ matterData.matterTypeName || matterData.matterType || '-' }}
                    </a-descriptions-item>
                    <a-descriptions-item label="项目状态">
                      <a-tag :color="getStatusColor(displayStatusRaw)">
                        {{ displayStatus }}
                      </a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="有效期至">
                      {{ formatDate(matterDetail.expiresAt) }}
                    </a-descriptions-item>
                    <a-descriptions-item label="委托日期">
                      {{ formatDate(String(matterData.createDate || '')) }}
                    </a-descriptions-item>
                    <a-descriptions-item label="创建时间">
                      {{ formatDate(matterDetail.createdAt) }}
                    </a-descriptions-item>
                  </a-descriptions>
                </a-card>

                <a-card
                  v-if="showProgressSection"
                  title="项目进度"
                  class="detail-block"
                >
                  <div class="progress-panel">
                    <div class="progress-item">
                      <span class="label">当前阶段</span>
                      <a-tag color="processing">
                        {{ currentStageLabel }}
                      </a-tag>
                    </div>
                    <div class="progress-item progress-item-bar">
                      <span class="label">整体进度</span>
                      <a-progress
                        class="progress-bar"
                        :percent="normalizedProgress"
                        :status="normalizedProgress === 100 ? 'success' : 'active'"
                      />
                    </div>
                    <div class="progress-item">
                      <span class="label">最后更新</span>
                      <span>{{ formatDate(displayLastUpdate) }}</span>
                    </div>
                  </div>
                </a-card>

                <a-card
                  v-if="recentActivityList.length > 0"
                  title="最近动态"
                  class="detail-block"
                >
                  <div class="activity-list">
                    <article
                      v-for="activity in recentActivityList"
                      :key="activity.key"
                      class="activity-item"
                    >
                      <div class="activity-dot" />
                      <div class="activity-copy">
                        <strong>{{ activity.title }}</strong>
                        <p>{{ activity.description }}</p>
                        <span>{{ activity.meta }}</span>
                      </div>
                    </article>
                  </div>
                </a-card>

                <a-card
                  v-if="showTasksSection"
                  title="待办任务"
                  class="detail-block"
                >
                  <template #extra>
                    <span class="panel-extra">{{ completedTasksLabel }}</span>
                  </template>
                  <a-list
                    :data-source="pendingTasksList"
                    item-layout="horizontal"
                    size="small"
                  >
                    <template #renderItem="{ item }">
                      <a-list-item>
                        <a-list-item-meta>
                          <template #title>
                            <span class="task-title">{{ item.title }}</span>
                            <a-tag
                              v-if="item.statusName || item.status"
                              :color="getTaskStatusColor(item.status)"
                              class="inline-tag"
                            >
                              {{ item.statusName || item.status }}
                            </a-tag>
                          </template>
                          <template #description>
                            <a-space wrap>
                              <span v-if="item.dueDate">
                                <ClockCircleOutlined /> 截止：{{ formatDate(item.dueDate) }}
                              </span>
                              <a-progress
                                v-if="item.progress !== undefined"
                                :percent="item.progress"
                                size="small"
                                style="width: 120px"
                              />
                            </a-space>
                          </template>
                        </a-list-item-meta>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-card>

                <a-card
                  v-if="showDeadlinesSection"
                  title="关键期限"
                  class="detail-block"
                >
                  <a-timeline>
                    <a-timeline-item
                      v-for="(deadline, index) in deadlinesList"
                      :key="index"
                      :color="deadline.isOverdue ? 'red' : (deadline.deadline && isDeadlineNear(deadline.deadline) ? 'orange' : 'blue')"
                    >
                      <div class="deadline-item">
                        <div class="deadline-title">
                          {{ deadline.title }}
                          <a-tag
                            v-if="deadline.type"
                            size="small"
                            class="inline-tag"
                          >
                            {{ String(deadline.type) }}
                          </a-tag>
                          <a-tag
                            v-if="deadline.isOverdue"
                            color="red"
                            size="small"
                            class="inline-tag"
                          >
                            已逾期
                          </a-tag>
                        </div>
                        <div class="deadline-date">
                          <CalendarOutlined /> {{ formatDate(deadline.deadline) }}
                        </div>
                      </div>
                    </a-timeline-item>
                  </a-timeline>
                </a-card>

                <a-card
                  v-if="showDocumentsSection"
                  title="文档列表"
                  class="detail-block"
                >
                  <template #extra>
                    <span class="panel-extra">共 {{ documentCount }} 份</span>
                  </template>
                  <a-list
                    :data-source="documentsList"
                    item-layout="horizontal"
                    size="small"
                  >
                    <template #renderItem="{ item }">
                      <a-list-item>
                        <a-list-item-meta>
                          <template #avatar>
                            <FileTextOutlined class="doc-icon" />
                          </template>
                          <template #title>
                            {{ item.title || item.name }}
                          </template>
                          <template #description>
                            <a-space wrap>
                              <a-tag
                                v-if="item.category || item.type"
                                size="small"
                              >
                                {{ item.category || item.type }}
                              </a-tag>
                              <span v-if="item.createdAt || item.uploadTime">
                                {{ formatDate(item.createdAt || item.uploadTime) }}
                              </span>
                            </a-space>
                          </template>
                        </a-list-item-meta>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-card>
              </div>

              <aside class="detail-side">
                <a-card
                  v-if="showLawyersSection"
                  title="承办律师"
                  class="detail-block"
                >
                  <div
                    v-if="matterData.leadLawyerName"
                    class="lead-lawyer"
                  >
                    <div class="side-kicker">
                      主办律师
                    </div>
                    <strong>{{ matterData.leadLawyerName }}</strong>
                    <p v-if="matterData.leadLawyerContact">
                      {{ matterData.leadLawyerContact }}
                    </p>
                  </div>
                  <a-list
                    v-if="lawyerList.length > 0"
                    :data-source="lawyerList"
                    item-layout="horizontal"
                    size="small"
                  >
                    <template #renderItem="{ item }">
                      <a-list-item>
                        <a-list-item-meta>
                          <template #title>
                            {{ item.name }}
                            <a-tag
                              v-if="item.role || item.roleName || item.title"
                              color="blue"
                              class="inline-tag"
                            >
                              {{ item.roleName || item.role || item.title }}
                            </a-tag>
                          </template>
                          <template #description>
                            <a-space wrap>
                              <span v-if="item.phone || item.contact">
                                <PhoneOutlined /> {{ item.phone || item.contact }}
                              </span>
                              <span v-if="item.email">
                                <MailOutlined /> {{ item.email }}
                              </span>
                            </a-space>
                          </template>
                        </a-list-item-meta>
                      </a-list-item>
                    </template>
                  </a-list>
                </a-card>

                <a-card
                  v-if="showFeesSection"
                  title="费用信息"
                  class="detail-block"
                >
                  <div class="fee-stack">
                    <div class="fee-item">
                      <span>合同金额</span>
                      <strong>¥{{ formatCurrency(matterData.contractAmount) }}</strong>
                    </div>
                    <div class="fee-item fee-item-success">
                      <span>已收款</span>
                      <strong>¥{{ formatCurrency(matterData.receivedAmount) }}</strong>
                    </div>
                    <div class="fee-item fee-item-warning">
                      <span>待收款</span>
                      <strong>¥{{ formatCurrency(matterData.pendingAmount) }}</strong>
                    </div>
                  </div>
                </a-card>

                <a-card
                  title="访问说明"
                  class="detail-block"
                >
                  <div class="note-list">
                    <div class="note-item">
                      <strong>访问有效期</strong>
                      <p>{{ formatDate(matterDetail.expiresAt) }}</p>
                    </div>
                    <div class="note-item">
                      <strong>文件协作</strong>
                      <p>可查看律所推送文件，也可上传客户补充材料。</p>
                    </div>
                    <div class="note-item">
                      <strong>问题处理</strong>
                      <p>如信息异常，请联系承办律师核对项目数据。</p>
                    </div>
                  </div>
                </a-card>

                <a-card
                  v-if="upcomingSnapshot.length > 0"
                  title="关键快照"
                  class="detail-block"
                >
                  <div class="snapshot-list">
                    <article
                      v-for="snapshot in upcomingSnapshot"
                      :key="snapshot.key"
                      class="snapshot-item"
                    >
                      <span class="snapshot-label">{{ snapshot.label }}</span>
                      <strong>{{ snapshot.value }}</strong>
                      <p>{{ snapshot.description }}</p>
                    </article>
                  </div>
                </a-card>
              </aside>
            </section>

            <a-card
              title="文件管理"
              class="detail-block files-block"
            >
              <template #extra>
                <a-button
                  type="primary"
                  class="upload-btn"
                  @click="showUploadModal = true"
                >
                  <template #icon>
                    <UploadOutlined />
                  </template>
                  <span class="upload-btn-text">上传文件</span>
                </a-button>
              </template>

              <a-table
                :columns="fileColumns"
                :data-source="fileList"
                :loading="fileLoading"
                :pagination="false"
                :scroll="{ x: 920 }"
                row-key="id"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'fileName'">
                    <a @click="handlePreview(record)">{{ record.fileName }}</a>
                  </template>
                  <template v-else-if="column.key === 'fileSource'">
                    <a-tag :color="record.fileSource === FILE_SOURCE_PUSHED ? 'blue' : 'green'">
                      {{ record.fileSource === FILE_SOURCE_PUSHED ? '律所' : (isMobile ? '我的' : '我上传的') }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'fileSize'">
                    {{ formatFileSize(record.fileSize) }}
                  </template>
                  <template v-else-if="column.key === 'uploadTime'">
                    {{ formatDate(record.uploadedAt) }}
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space
                      class="file-actions"
                      :size="4"
                    >
                      <a-tooltip title="预览">
                        <a-button
                          type="text"
                          size="small"
                          class="action-btn"
                          @click="handlePreview(record)"
                        >
                          <template #icon>
                            <EyeOutlined />
                          </template>
                          <span class="action-text">预览</span>
                        </a-button>
                      </a-tooltip>
                      <a-tooltip title="下载">
                        <a-button
                          type="text"
                          size="small"
                          class="action-btn"
                          @click="handleDownload(record)"
                        >
                          <template #icon>
                            <DownloadOutlined />
                          </template>
                          <span class="action-text">下载</span>
                        </a-button>
                      </a-tooltip>
                      <a-popconfirm
                        title="确定要删除这个文件吗？"
                        @confirm="handleDelete(record)"
                      >
                        <a-tooltip title="删除">
                          <a-button
                            type="text"
                            size="small"
                            danger
                            class="action-btn"
                          >
                            <template #icon>
                              <DeleteOutlined />
                            </template>
                            <span class="action-text">删除</span>
                          </a-button>
                        </a-tooltip>
                      </a-popconfirm>
                    </a-space>
                  </template>
                </template>
              </a-table>
            </a-card>
          </div>

          <a-empty
            v-else-if="!loading"
            description="项目不存在或已过期"
          />
        </a-spin>

        <a-modal
          v-model:open="showPreviewModal"
          :title="previewFileInfo?.fileName || '文件预览'"
          :width="previewWidth"
          :wrap-class-name="'preview-modal-wrapper'"
          @cancel="showPreviewModal = false"
        >
          <template #footer>
            <a-space
              class="preview-modal-footer"
              wrap
            >
              <a-button @click="showPreviewModal = false">
                关闭
              </a-button>
              <a-button
                v-if="previewFileInfo"
                type="primary"
                @click="handleDownload(previewFileInfo)"
              >
                <template #icon>
                  <DownloadOutlined />
                </template>
                下载文件
              </a-button>
            </a-space>
          </template>
          <div
            v-if="previewFileInfo"
            style="text-align: center; min-height: 400px"
          >
            <img
              v-if="isImageFile(previewFileInfo)"
              :src="previewUrl"
              style="max-width: 100%; max-height: 70vh"
              alt="预览"
            >
            <iframe
              v-else-if="isPdfFile(previewFileInfo)"
              :src="previewUrl"
              style="width: 100%; height: 70vh; border: none"
            />
            <div
              v-else-if="isTextFile(previewFileInfo)"
              style="text-align: left; padding: 16px"
            >
              <pre style="white-space: pre-wrap; word-wrap: break-word">{{ previewTextContent }}</pre>
            </div>
            <a-result
              v-else
              status="info"
              title="不支持预览此文件类型"
              sub-title="请下载后查看"
            />
          </div>
        </a-modal>

        <a-modal
          v-model:open="showUploadModal"
          title="上传文件"
          :width="previewWidth"
          wrap-class-name="upload-modal-wrapper"
          @ok="handleUpload"
          @cancel="resetUploadForm"
        >
          <a-form
            :model="uploadForm"
            layout="vertical"
          >
            <a-form-item
              label="文件"
              required
            >
              <a-upload
                v-model:file-list="uploadFileList"
                :before-upload="beforeUpload"
                :max-count="1"
                :disabled="uploading"
              >
                <a-button :disabled="uploading">
                  <template #icon>
                    <UploadOutlined />
                  </template>
                  选择文件
                </a-button>
              </a-upload>
              <a-progress
                v-if="uploading"
                :percent="uploadProgress"
                :status="uploadProgress === 100 ? 'success' : 'active'"
                style="margin-top: 8px"
              />
            </a-form-item>
            <a-form-item label="文件分类">
              <a-select v-model:value="uploadForm.category">
                <a-select-option value="CONTRACT">
                  合同文件
                </a-select-option>
                <a-select-option value="EVIDENCE">
                  证据材料
                </a-select-option>
                <a-select-option value="DOCUMENT">
                  法律文书
                </a-select-option>
                <a-select-option value="OTHER">
                  其他
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="文件说明">
              <a-textarea
                v-model:value="uploadForm.description"
                :rows="3"
                placeholder="请输入文件说明（可选）"
              />
            </a-form-item>
          </a-form>
        </a-modal>
      </a-layout-content>
      <MobileBottomNav />
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { UploadFile } from 'ant-design-vue'
import {
  UploadOutlined,
  PhoneOutlined,
  MailOutlined,
  ClockCircleOutlined,
  CalendarOutlined,
  FileTextOutlined,
  DownloadOutlined,
  EyeOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { getClientMatterDetail, type ClientMatterDetail, type MatterData } from '@/api/matter'
import {
  getFileList,
  uploadFile,
  downloadFile,
  deleteFile,
  previewFile,
  type FileInfo,
  FILE_SOURCE_PUSHED
} from '@/api/file'
import { formatDate as formatDateTime } from '@/utils/date'
import { getMatterStatusColor, getTaskStatusColor as getTaskStatusColorUtil } from '@/utils/status'
import dayjs from 'dayjs'
import logger from '@/utils/logger'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'
import { usePortalVisitorStore } from '@/stores/portalVisitor'

const route = useRoute()
const router = useRouter()
const portalVisitorStore = usePortalVisitorStore()

const loading = ref(false)
const fileLoading = ref(false)
const matterDetail = ref<ClientMatterDetail | null>(null)
const fileList = ref<FileInfo[]>([])
const showUploadModal = ref(false)

const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1024)
const isMobile = computed(() => windowWidth.value <= 768)
const uploadForm = ref({
  file: null as File | null,
  category: 'OTHER',
  description: '',
})
const uploadFileList = ref<UploadFile[]>([])
const uploadProgress = ref(0)
const uploading = ref(false)
const showPreviewModal = ref(false)
const previewFileInfo = ref<FileInfo | null>(null)
const previewUrl = ref('')
const previewTextContent = ref('')
const previewWidth = ref(800)

const matterId = computed(() => route.params.id as string)
const token = computed(() => route.query.token as string)
const matterData = computed<MatterData>(() => matterDetail.value?.matterData || {})

const welcomeText = computed(() => matterDetail.value?.clientName || '')

const lawyerList = computed(() => {
  const data = matterData.value
  const lawyers = data.lawyers || data.lawyerList || data.teamMembers || []
  return Array.isArray(lawyers) ? lawyers : []
})

const currentStageLabel = computed(() => String(matterData.value.currentStageName || matterData.value.currentStage || '待更新'))
const displayStatusRaw = computed(() => String(matterData.value.status || matterDetail.value?.status || 'UNKNOWN'))
const displayStatus = computed(() => String(matterData.value.statusName || matterData.value.status || matterDetail.value?.status || '未知状态'))
const normalizedProgress = computed(() => {
  const raw = Number(matterData.value.progress ?? 0)
  if (Number.isNaN(raw)) return 0
  return Math.max(0, Math.min(100, raw))
})
const displayLastUpdate = computed(() => String(matterData.value.lastUpdateTime || matterDetail.value?.createdAt || ''))
const pendingTasksList = computed(() => Array.isArray(matterData.value.pendingTasks) ? matterData.value.pendingTasks : [])
const deadlinesList = computed(() => Array.isArray(matterData.value.deadlines) ? matterData.value.deadlines : [])
const documentsList = computed(() => Array.isArray(matterData.value.documents) ? matterData.value.documents : [])
const pendingTaskCount = computed(() => pendingTasksList.value.length)
const completedTasksLabel = computed(() => {
  if (matterData.value.completedTaskCount !== undefined || matterData.value.totalTaskCount !== undefined) {
    return `已完成 ${matterData.value.completedTaskCount ?? 0}/${matterData.value.totalTaskCount ?? 0}`
  }
  return '等待任务同步'
})
const documentCount = computed(() => Number(matterData.value.documentCount ?? documentsList.value.length ?? 0))
const heroActionTitle = computed(() => {
  if (pendingTaskCount.value > 0) return '补齐待办相关材料'
  if (fileList.value.length === 0) return '上传首批项目文件'
  return '继续查看最新进展'
})
const heroActionDescription = computed(() => {
  if (pendingTaskCount.value > 0) return `待办 ${pendingTaskCount.value} 项，请按截止日补充材料。`
  if (fileList.value.length === 0) return '暂无文件时可上传补充材料，或等待律所上传。'
  return '进展与材料见下方。'
})
const recentActivityList = computed(() => {
  const activities: Array<{ key: string; title: string; description: string; meta: string }> = []

  if (displayLastUpdate.value) {
    activities.push({
      key: 'last-update',
      title: '进度已更新',
      description: `${currentStageLabel.value} · ${normalizedProgress.value}%`,
      meta: formatDate(displayLastUpdate.value),
    })
  }

  pendingTasksList.value.slice(0, 2).forEach((task, index) => {
    activities.push({
      key: `task-${index}`,
      title: task.title || '待办事项',
      description: `任务状态：${String(task.status || '待处理')}`,
      meta: task.dueDate ? `截止于 ${formatDate(task.dueDate)}` : '等待时间同步',
    })
  })

  documentsList.value.slice(0, 2).forEach((doc, index) => {
    const extendedDoc = doc as {
      title?: string
      name?: string
      category?: string
      type?: string
      createdAt?: string
      uploadTime?: string
    }
    activities.push({
      key: `doc-${index}`,
      title: extendedDoc.title || extendedDoc.name || '文件已同步',
      description: `文档类型：${String(extendedDoc.category || extendedDoc.type || '未分类')}`,
      meta: extendedDoc.createdAt || extendedDoc.uploadTime ? formatDate(String(extendedDoc.createdAt || extendedDoc.uploadTime || '')) : '等待时间同步',
    })
  })

  return activities.slice(0, 4)
})
const upcomingSnapshot = computed(() => {
  const snapshots: Array<{ key: string; label: string; value: string; description: string }> = [
    {
      key: 'expiry',
      label: '访问有效期',
      value: formatDate(matterDetail.value?.expiresAt || ''),
      description: '超过有效期后需要由律所重新发送访问链接。',
    },
    {
      key: 'documents',
      label: '文件快照',
      value: `${fileList.value.length} 份`,
      description: '包含律所推送文件与客户补充材料。',
    },
    {
      key: 'progress',
      label: '项目进度',
      value: `${normalizedProgress.value}%`,
      description: currentStageLabel.value,
    },
  ]

  return snapshots.filter(item => item.value && item.value !== 'Invalid Date')
})
const showProgressSection = computed(() => {
  const data = matterData.value
  return !!(data.currentStage || data.currentStageName || data.progress !== undefined || data.lastUpdateTime)
})
const showLawyersSection = computed(() => {
  const data = matterData.value
  return !!(data.leadLawyerName || lawyerList.value.length > 0)
})
const showTasksSection = computed(() => pendingTasksList.value.length > 0)
const showDeadlinesSection = computed(() => deadlinesList.value.length > 0)
const showFeesSection = computed(() => {
  const data = matterData.value
  return data.contractAmount !== undefined || data.receivedAmount !== undefined || data.pendingAmount !== undefined
})
const showDocumentsSection = computed(() => documentsList.value.length > 0)

const fileColumns = computed(() => {
  if (isMobile.value) {
    return [
      { title: '文件名', key: 'fileName', dataIndex: 'fileName', ellipsis: true },
      { title: '来源', key: 'fileSource', dataIndex: 'fileSource', width: 70 },
      { title: '操作', key: 'action', width: 100 },
    ]
  }
  return [
    { title: '文件名', key: 'fileName', dataIndex: 'fileName', ellipsis: true },
    { title: '来源', key: 'fileSource', dataIndex: 'fileSource', width: 100 },
    { title: '大小', key: 'fileSize', dataIndex: 'fileSize', width: 100 },
    { title: '分类', key: 'fileCategory', dataIndex: 'fileCategory', width: 100 },
    { title: '上传时间', key: 'uploadTime', dataIndex: 'uploadedAt', width: 160 },
    { title: '操作', key: 'action', width: 150 },
  ]
})

async function loadMatterDetail() {
  const maskedToken = token.value ? token.value.substring(0, 8) + '****' : 'null'
  logger.debug('[MatterDetail] 路由信息:', {
    path: route.path,
    params: route.params,
    query: route.query,
    matterId: matterId.value,
    token: maskedToken
  })

  if (!matterId.value || !token.value) {
    logger.error('[MatterDetail] 缺少必要参数:', { matterId: matterId.value, token: maskedToken })
    message.error('缺少必要参数')
    router.push('/portal')
    return
  }

  loading.value = true
  try {
    const res = await getClientMatterDetail(matterId.value, token.value)
    matterDetail.value = res.data
    portalVisitorStore.saveProfile({
      clientId: res.data.clientId ?? null,
      clientName: res.data.clientName || '',
      lastMatterId: res.data.id || matterId.value,
      lastMatterToken: token.value || '',
    })
    await loadFileList()
    logger.debug('[MatterDetail] 数据加载成功')
  } catch (error: unknown) {
    logger.error('[MatterDetail] 加载失败:', error)
    const errorMessage = error instanceof Error ? error.message : '加载项目详情失败'
    message.error(errorMessage)
    router.push('/portal')
  } finally {
    loading.value = false
  }
}

async function loadFileList() {
  if (!matterId.value || !token.value) return

  fileLoading.value = true
  try {
    const res = await getFileList(matterId.value, token.value)
    fileList.value = res.data || []
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '加载文件列表失败'
    message.error(errorMessage)
  } finally {
    fileLoading.value = false
  }
}

const ALLOWED_FILE_TYPES = [
  '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
  '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.txt', '.zip', '.rar'
]
const MAX_FILE_SIZE = 10 * 1024 * 1024

function beforeUpload(file: File) {
  if (file.size > MAX_FILE_SIZE) {
    message.error(`文件大小不能超过 ${MAX_FILE_SIZE / 1024 / 1024}MB`)
    return false
  }

  const fileName = file.name.toLowerCase()
  const ext = fileName.substring(fileName.lastIndexOf('.'))
  if (!ALLOWED_FILE_TYPES.includes(ext)) {
    message.error(`不支持的文件类型：${ext}，允许的类型：${ALLOWED_FILE_TYPES.join(', ')}`)
    return false
  }

  uploadForm.value.file = file
  return false
}

async function handleUpload() {
  if (!uploadForm.value.file) {
    message.warning('请选择文件')
    return
  }

  if (!matterId.value || !token.value || !matterDetail.value) {
    message.error('缺少必要参数')
    return
  }

  uploading.value = true
  uploadProgress.value = 0

  try {
    await uploadFile(
      matterId.value,
      matterDetail.value.clientId,
      token.value,
      uploadForm.value.file,
      uploadForm.value.category,
      uploadForm.value.description,
      (progress) => {
        uploadProgress.value = progress
      }
    )
    message.success('文件上传成功')
    showUploadModal.value = false
    resetUploadForm()
    await loadFileList()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '文件上传失败'
    message.error(errorMessage)
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

function resetUploadForm() {
  uploadForm.value = {
    file: null,
    category: 'OTHER',
    description: '',
  }
  uploadFileList.value = []
  uploadProgress.value = 0
  uploading.value = false
}

async function handlePreview(file: FileInfo) {
  if (!file || !file.id || !matterId.value || !token.value) return

  previewFileInfo.value = file
  previewUrl.value = previewFile(file.id, matterId.value, token.value)

  const mobile = window.innerWidth <= 768
  if (isImageFile(file)) {
    previewWidth.value = mobile ? window.innerWidth - 32 : 900
  } else if (isPdfFile(file)) {
    previewWidth.value = mobile ? window.innerWidth - 32 : 1000
  } else {
    previewWidth.value = mobile ? window.innerWidth - 32 : 800
  }

  if (isTextFile(file)) {
    try {
      const response = await fetch(previewUrl.value)
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      previewTextContent.value = await response.text()
    } catch (error) {
      logger.warn('文件预览加载失败:', { fileId: file.id, fileName: file.fileName, error })
      previewTextContent.value = '无法加载文件内容'
    }
  }

  showPreviewModal.value = true
}

function isImageFile(file: FileInfo): boolean {
  const imageTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  return imageTypes.includes(file.fileType || '') ||
    /\.(jpg|jpeg|png|gif|webp|bmp)$/i.test(file.fileName || '')
}

function isPdfFile(file: FileInfo): boolean {
  return file.fileType === 'application/pdf' || /\.pdf$/i.test(file.fileName || '')
}

function isTextFile(file: FileInfo): boolean {
  const textTypes = ['text/plain', 'text/html', 'text/css', 'text/javascript', 'application/json', 'text/xml']
  return textTypes.includes(file.fileType || '') ||
    /\.(txt|html|css|js|json|xml|md)$/i.test(file.fileName || '')
}

async function handleDownload(file: FileInfo | null) {
  if (!file || !matterId.value || !token.value) return

  try {
    await downloadFile(file.id, matterId.value, token.value, file.fileName)
    message.success('文件下载成功')
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '文件下载失败'
    message.error(errorMessage)
  }
}

async function handleDelete(file: FileInfo) {
  if (!file || !file.id || !matterId.value || !token.value) return

  try {
    await deleteFile(file.id, matterId.value, token.value)
    message.success('文件删除成功')
    await loadFileList()
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '文件删除失败'
    message.error(errorMessage)
  }
}

function formatDate(date: string | undefined) {
  return formatDateTime(date, 'YYYY-MM-DD HH:mm')
}

function formatFileSize(bytes: number) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

function formatCurrency(value: unknown) {
  const amount = Number(value ?? 0)
  if (Number.isNaN(amount)) return '0.00'
  return amount.toFixed(2)
}

const getStatusColor = getMatterStatusColor
const getTaskStatusColor = getTaskStatusColorUtil

function isDeadlineNear(deadline: string) {
  if (!deadline) return false
  const days = dayjs(deadline).diff(dayjs(), 'day')
  return days >= 0 && days <= 7
}

function goBack() {
  router.push('/portal')
}

let resizeHandler: (() => void) | null = null

onMounted(() => {
  loadMatterDetail()

  resizeHandler = () => {
    windowWidth.value = window.innerWidth
  }
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
})
</script>

<style scoped>
.matter-detail-container {
  min-height: 100vh;
  background: transparent;
}

.content {
  width: min(var(--shell-max-width), calc(100vw - 2 * var(--shell-gutter)));
  margin: 0 auto;
  padding: 24px 0 110px;
}

.detail-shell {
  display: grid;
  gap: 18px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.8fr);
  gap: 20px;
  padding: 28px;
}

.hero-copy {
  display: grid;
  gap: 16px;
}

.hero-title {
  margin: 0;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.02;
}

.hero-subtitle {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.8;
  max-width: 720px;
}

.hero-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.hero-meta-item {
  display: grid;
  gap: 6px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.04);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.hero-meta-item strong {
  font-size: 12px;
  color: var(--text-tertiary);
}

.hero-meta-item span {
  color: var(--text-primary);
  font-weight: 600;
}

.hero-side {
  display: grid;
  align-content: start;
  gap: 14px;
}

.hero-status {
  justify-self: start;
  padding-inline: 12px !important;
}

.hero-progress-card {
  display: grid;
  gap: 10px;
  padding: 20px;
  border-radius: 8px;
  background: var(--lex-surface);
  border: 1px solid var(--lex-outline);
}

.hero-action-card {
  display: grid;
  gap: 10px;
  padding: 20px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--lex-surface-dark) 0%, var(--lex-surface-dark-muted) 100%);
  color: rgba(255, 255, 255, 0.82);
  box-shadow: var(--shadow-sm);
}

.hero-action-card strong {
  color: #fff;
  font-size: 22px;
  line-height: 1.2;
}

.hero-action-card p {
  margin: 0;
  line-height: 1.8;
}

.hero-action-btn {
  margin-top: 4px;
}

.hero-progress-label,
.panel-extra,
.side-kicker {
  font-size: 12px;
  color: var(--text-tertiary);
}

.hero-progress-card strong {
  font-size: 24px;
  line-height: 1.1;
  color: var(--text-primary);
}

.hero-progress-foot {
  color: var(--text-secondary);
  font-size: 13px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.12fr) minmax(280px, 0.88fr);
  gap: 18px;
  align-items: start;
}

.detail-main,
.detail-side {
  display: grid;
  gap: 18px;
}

.detail-block {
  margin: 0;
}

.progress-panel,
.fee-stack,
.note-list,
.activity-list,
.snapshot-list {
  display: grid;
  gap: 14px;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-item-bar {
  align-items: flex-start;
}

.label {
  min-width: 84px;
  color: var(--text-secondary);
}

.progress-bar {
  width: 100%;
}

.lead-lawyer {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.1);
}

.lead-lawyer strong {
  display: block;
  margin: 6px 0 4px;
  color: var(--text-primary);
}

.lead-lawyer p {
  margin: 0;
  color: var(--text-secondary);
}

.inline-tag {
  margin-left: 8px;
}

.task-title {
  color: var(--text-primary);
  font-weight: 600;
}

.deadline-item {
  display: grid;
  gap: 4px;
}

.deadline-title {
  font-weight: 600;
  color: var(--text-primary);
}

.deadline-date {
  color: var(--text-secondary);
  font-size: 13px;
}

.activity-item,
.snapshot-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.activity-dot {
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border-radius: 999px;
  background: var(--accent-color);
  box-shadow: 0 0 0 6px var(--lex-accent-soft);
}

.activity-copy {
  display: grid;
  gap: 4px;
}

.activity-copy strong,
.snapshot-item strong {
  color: var(--lex-primary);
}

.activity-copy p,
.snapshot-item p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
}

.activity-copy span,
.snapshot-label {
  color: var(--text-tertiary);
  font-size: 12px;
}

.doc-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.fee-item,
.note-item {
  display: grid;
  gap: 6px;
  padding: 16px;
  border-radius: 8px;
  background: var(--lex-bg-muted);
  border: 1px solid var(--lex-outline);
}

.fee-item span,
.note-item strong {
  color: var(--text-secondary);
}

.fee-item strong {
  font-size: 24px;
  line-height: 1.1;
  color: var(--text-primary);
}

.fee-item-success strong {
  color: var(--success-color);
}

.fee-item-warning strong {
  color: var(--warning-color);
}

.note-item p {
  margin: 0;
  color: var(--text-primary);
  line-height: 1.7;
}

.files-block {
  margin-top: 4px;
}

.file-actions {
  flex-wrap: nowrap;
}

.action-btn {
  padding: 4px 8px;
  height: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.action-btn :deep(.anticon) {
  font-size: 14px;
}

.upload-btn-text,
.action-text {
  display: inline-block;
}

.preview-modal-footer {
  width: 100%;
  justify-content: flex-end;
}

@media (max-width: 1024px) {
  .hero-panel,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .hero-meta {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .content {
    padding: 18px 0 110px;
  }

  .hero-panel {
    padding: 22px;
  }

  .hero-meta {
    grid-template-columns: 1fr;
  }

  .activity-item,
  .snapshot-item {
    grid-template-columns: 1fr;
  }

  .progress-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .label {
    min-width: auto;
  }

  .file-actions {
    gap: 2px !important;
  }

  .action-btn {
    padding: 2px 4px !important;
    min-width: 28px !important;
    height: 28px !important;
  }

  .action-text {
    display: none;
  }

  .upload-btn {
    width: 100%;
  }
}
</style>
