<template>
  <div class="matter-detail-container">
    <a-layout>
      <!-- 统一的 Header 组件 -->
      <AppHeader
        variant="detail"
        :title="'项目详情'"
        :welcome-text="welcomeText"
        :show-back="true"
        :show-back-text="true"
        :show-mobile-menu="false"
        @back="goBack"
      />
      <a-layout-content class="content">
        <a-spin :spinning="loading">
          <div
            v-if="matterDetail"
            class="detail-card"
          >
            <!-- 动态渲染所有数据块 -->
            <template
              v-for="section in visibleSections"
              :key="section.key"
            >
              <!-- 项目基本信息 -->
              <a-card
                v-if="section.key === 'matterInfo'"
                :title="section.title"
                style="margin-bottom: 24px"
              >
                <a-descriptions
                  :column="{ xs: 1, sm: 2 }"
                  bordered
                  size="small"
                >
                  <a-descriptions-item
                    v-if="matterData.matterName"
                    label="项目名称"
                  >
                    {{ matterData.matterName }}
                  </a-descriptions-item>
                  <a-descriptions-item
                    v-if="matterData.matterNo"
                    label="项目编号"
                  >
                    {{ matterData.matterNo }}
                  </a-descriptions-item>
                  <a-descriptions-item label="客户名称">
                    {{ matterDetail.clientName }}
                  </a-descriptions-item>
                  <a-descriptions-item
                    v-if="matterData.matterTypeName || matterData.matterType"
                    label="项目类型"
                  >
                    {{ matterData.matterTypeName || matterData.matterType }}
                  </a-descriptions-item>
                  <a-descriptions-item label="项目状态">
                    <a-tag :color="getStatusColor(String(matterData.status || matterDetail.status))">
                      {{ matterData.statusName || matterData.status || matterDetail.status }}
                    </a-tag>
                  </a-descriptions-item>
                  <a-descriptions-item label="有效期至">
                    {{ formatDate(matterDetail.expiresAt) }}
                  </a-descriptions-item>
                  <a-descriptions-item
                    v-if="matterData.createDate"
                    label="委托日期"
                  >
                    {{ formatDate(String(matterData.createDate)) }}
                  </a-descriptions-item>
                  <a-descriptions-item label="创建时间">
                    {{ formatDate(matterDetail.createdAt) }}
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>

              <!-- 项目进度 -->
              <a-card
                v-else-if="section.key === 'progress'"
                :title="section.title"
                style="margin-bottom: 24px"
              >
                <a-descriptions
                  :column="1"
                  bordered
                  size="small"
                >
                  <a-descriptions-item
                    v-if="matterData.currentStageName || matterData.currentStage"
                    label="当前阶段"
                  >
                    <a-tag color="processing">
                      {{ matterData.currentStageName || matterData.currentStage }}
                    </a-tag>
                  </a-descriptions-item>
                  <a-descriptions-item
                    v-if="matterData.progress !== undefined && matterData.progress !== null"
                    label="整体进度"
                  >
                    <a-progress 
                      :percent="matterData.progress" 
                      :status="matterData.progress === 100 ? 'success' : 'active'" 
                      style="width: 100%; max-width: 400px"
                    />
                  </a-descriptions-item>
                  <a-descriptions-item
                    v-if="matterData.lastUpdateTime"
                    label="最后更新"
                  >
                    {{ formatDate(String(matterData.lastUpdateTime)) }}
                  </a-descriptions-item>
                </a-descriptions>
              </a-card>

              <!-- 承办律师 -->
              <a-card
                v-else-if="section.key === 'lawyers'"
                :title="section.title"
                style="margin-bottom: 24px"
              >
                <!-- 主办律师 -->
                <div
                  v-if="matterData.leadLawyerName"
                  class="lead-lawyer"
                >
                  <a-descriptions
                    :column="1"
                    size="small"
                  >
                    <a-descriptions-item label="主办律师">
                      <strong>{{ matterData.leadLawyerName }}</strong>
                      <span
                        v-if="matterData.leadLawyerContact"
                        style="margin-left: 16px; color: #666"
                      >
                        {{ matterData.leadLawyerContact }}
                      </span>
                    </a-descriptions-item>
                  </a-descriptions>
                </div>
                <!-- 团队成员列表 -->
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
                            v-if="item.role || item.roleName"
                            color="blue"
                            style="margin-left: 8px"
                          >
                            {{ item.roleName || item.role }}
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

              <!-- 待办任务 -->
              <a-card
                v-else-if="section.key === 'tasks'"
                :title="section.title"
                style="margin-bottom: 24px"
              >
                <template #extra>
                  <span
                    v-if="matterData.completedTaskCount !== undefined"
                    style="color: #666"
                  >
                    已完成 {{ matterData.completedTaskCount ?? 0 }}/{{ matterData.totalTaskCount ?? 0 }}
                  </span>
                </template>
                <a-list
                  :data-source="matterData.pendingTasks"
                  item-layout="horizontal"
                  size="small"
                >
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta>
                        <template #title>
                          {{ item.title }}
                          <a-tag
                            v-if="item.statusName || item.status"
                            :color="getTaskStatusColor(item.status)"
                            style="margin-left: 8px"
                          >
                            {{ item.statusName || item.status }}
                          </a-tag>
                        </template>
                        <template #description>
                          <a-space>
                            <span v-if="item.dueDate">
                              <ClockCircleOutlined /> 截止：{{ formatDate(item.dueDate) }}
                            </span>
                            <a-progress
                              v-if="item.progress !== undefined"
                              :percent="item.progress"
                              size="small"
                              style="width: 100px"
                            />
                          </a-space>
                        </template>
                      </a-list-item-meta>
                    </a-list-item>
                  </template>
                </a-list>
              </a-card>

              <!-- 关键期限 -->
              <a-card
                v-else-if="section.key === 'deadlines'"
                :title="section.title"
                style="margin-bottom: 24px"
              >
                <a-timeline>
                  <a-timeline-item
                    v-for="(deadline, index) in (matterData.deadlines as Array<{title?: string; type?: string; isOverdue?: boolean; deadline?: string}>)"
                    :key="index"
                    :color="deadline.isOverdue ? 'red' : (deadline.deadline && isDeadlineNear(deadline.deadline) ? 'orange' : 'blue')"
                  >
                    <div class="deadline-item">
                      <div class="deadline-title">
                        {{ deadline.title }}
                        <a-tag
                          v-if="deadline.type"
                          size="small"
                          style="margin-left: 8px"
                        >
                          {{ String(deadline.type) }}
                        </a-tag>
                        <a-tag
                          v-if="deadline.isOverdue"
                          color="red"
                          size="small"
                          style="margin-left: 8px"
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

              <!-- 费用信息 -->
              <a-card
                v-else-if="section.key === 'fees'"
                :title="section.title"
                style="margin-bottom: 24px"
              >
                <a-row :gutter="16">
                  <a-col
                    :xs="24"
                    :sm="8"
                  >
                    <a-statistic 
                      title="合同金额" 
                      :value="matterData.contractAmount" 
                      prefix="¥"
                      :precision="2"
                    />
                  </a-col>
                  <a-col
                    :xs="24"
                    :sm="8"
                  >
                    <a-statistic 
                      title="已收款" 
                      :value="matterData.receivedAmount" 
                      prefix="¥"
                      :precision="2"
                      :value-style="{ color: '#52c41a' }"
                    />
                  </a-col>
                  <a-col
                    :xs="24"
                    :sm="8"
                  >
                    <a-statistic 
                      title="待收款" 
                      :value="matterData.pendingAmount" 
                      prefix="¥"
                      :precision="2"
                      :value-style="{ color: '#faad14' }"
                    />
                  </a-col>
                </a-row>
              </a-card>

              <!-- 文档列表 -->
              <a-card
                v-else-if="section.key === 'documents'"
                :title="section.title"
                style="margin-bottom: 24px"
              >
                <template #extra>
                  <span style="color: #666">共 {{ (matterData.documentCount as number | undefined) || (matterData.documents as Array<any> | undefined)?.length || 0 }} 份</span>
                </template>
                <a-list
                  :data-source="matterData.documents as Array<any> || []"
                  item-layout="horizontal"
                  size="small"
                >
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <a-list-item-meta>
                        <template #avatar>
                          <FileTextOutlined style="font-size: 24px; color: #1890ff" />
                        </template>
                        <template #title>
                          {{ item.title }}
                        </template>
                        <template #description>
                          <a-space>
                            <a-tag
                              v-if="item.category"
                              size="small"
                            >
                              {{ item.category }}
                            </a-tag>
                            <span v-if="item.createdAt">{{ formatDate(item.createdAt) }}</span>
                          </a-space>
                        </template>
                      </a-list-item-meta>
                    </a-list-item>
                  </template>
                </a-list>
              </a-card>

              <!-- 文件管理 -->
              <a-card
                v-else-if="section.key === 'files'"
                :title="section.title"
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
                  :scroll="{ x: 'max-content' }"
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
            </template>
          </div>

          <a-empty
            v-else-if="!loading"
            description="项目不存在或已过期"
          />
        </a-spin>

        <!-- 文件预览对话框 -->
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

        <!-- 文件上传对话框 -->
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
      <!-- 移动端底部导航 -->
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
import { LAW_FIRM_NAME } from '@/config/app'
import { formatDate as formatDateTime } from '@/utils/date'
import { getMatterStatusColor, getTaskStatusColor as getTaskStatusColorUtil } from '@/utils/status'
import dayjs from 'dayjs'
import logger from '@/utils/logger'
import AppHeader from '@/components/AppHeader.vue'
import MobileBottomNav from '@/components/MobileBottomNav.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const fileLoading = ref(false)
const matterDetail = ref<ClientMatterDetail | null>(null)
const lawFirmName = LAW_FIRM_NAME
const fileList = ref<FileInfo[]>([])
const showUploadModal = ref(false)

// 响应式窗口宽度
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

// 提取 matterData（使用MatterData接口提供类型安全）
const matterData = computed<MatterData>(() => matterDetail.value?.matterData || {})

// 提取欢迎文本
const welcomeText = computed(() => {
  if (!matterDetail.value?.clientName) return ''
  return `尊敬的${matterDetail.value.clientName}：欢迎使用${lawFirmName}客户服务系统。`
})

// 提取律师信息
const lawyerList = computed(() => {
  const data = matterData.value
  const lawyers = data.lawyers || data.lawyerList || data.teamMembers || []
  return Array.isArray(lawyers) ? lawyers : []
})

// 定义所有可能的数据块
interface Section {
  key: string
  title: string
  visible: () => boolean
}

const allSections: Section[] = [
  {
    key: 'matterInfo',
    title: '项目信息',
    visible: () => true, // 始终显示
  },
  {
    key: 'progress',
    title: '项目进度',
    visible: () => {
      const data = matterData.value
      return !!(data.currentStage || data.currentStageName || data.progress !== undefined || data.lastUpdateTime)
    },
  },
  {
    key: 'lawyers',
    title: '承办律师',
    visible: () => {
      const data = matterData.value
      return !!(data.leadLawyerName || lawyerList.value.length > 0)
    },
  },
  {
    key: 'tasks',
    title: '待办任务',
    visible: () => {
      const data = matterData.value
      return Array.isArray(data.pendingTasks) && data.pendingTasks.length > 0
    },
  },
  {
    key: 'deadlines',
    title: '关键期限',
    visible: () => {
      const data = matterData.value
      return Array.isArray(data.deadlines) && data.deadlines.length > 0
    },
  },
  {
    key: 'fees',
    title: '费用信息',
    visible: () => {
      const data = matterData.value
      return data.contractAmount !== undefined || data.receivedAmount !== undefined || data.pendingAmount !== undefined
    },
  },
  {
    key: 'documents',
    title: '文档列表',
    visible: () => {
      const data = matterData.value
      return Array.isArray(data.documents) && data.documents.length > 0
    },
  },
  {
    key: 'files',
    title: '文件管理',
    visible: () => true, // 始终显示（客户可以上传文件）
  },
]

// 计算可见的数据块
const visibleSections = computed(() => {
  return allSections.filter(section => section.visible())
})

// 文件表格列（响应式）
const fileColumns = computed(() => {
  // 移动端精简列
  if (isMobile.value) {
    return [
      { title: '文件名', key: 'fileName', dataIndex: 'fileName', ellipsis: true },
      { title: '来源', key: 'fileSource', dataIndex: 'fileSource', width: 70 },
      { title: '操作', key: 'action', width: 100 },
    ]
  }
  // 桌面端完整列
  return [
    { title: '文件名', key: 'fileName', dataIndex: 'fileName', ellipsis: true },
    { title: '来源', key: 'fileSource', dataIndex: 'fileSource', width: 100 },
    { title: '大小', key: 'fileSize', dataIndex: 'fileSize', width: 100 },
    { title: '分类', key: 'fileCategory', dataIndex: 'fileCategory', width: 100 },
    { title: '上传时间', key: 'uploadTime', dataIndex: 'uploadedAt', width: 160 },
    { title: '操作', key: 'action', width: 150 },
  ]
})

// 加载项目详情
async function loadMatterDetail() {
  // 调试信息：验证参数是否正确获取（安全修复：不记录完整 token）
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

// 加载文件列表
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

// 允许的文件类型和大小限制
const ALLOWED_FILE_TYPES = [
  '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
  '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.txt', '.zip', '.rar'
]
const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB

// 上传前检查：文件类型和大小验证
function beforeUpload(file: File) {
  // 文件大小检查
  if (file.size > MAX_FILE_SIZE) {
    message.error(`文件大小不能超过 ${MAX_FILE_SIZE / 1024 / 1024}MB`)
    return false
  }

  // 文件类型检查
  const fileName = file.name.toLowerCase()
  const ext = fileName.substring(fileName.lastIndexOf('.'))
  if (!ALLOWED_FILE_TYPES.includes(ext)) {
    message.error(`不支持的文件类型：${ext}，允许的类型：${ALLOWED_FILE_TYPES.join(', ')}`)
    return false
  }

  uploadForm.value.file = file
  return false
}

// 上传文件
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

// 重置上传表单
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

// 预览文件
async function handlePreview(file: FileInfo) {
  if (!file || !file.id || !matterId.value || !token.value) return

  previewFileInfo.value = file
  previewUrl.value = previewFile(file.id, matterId.value, token.value)

  // 根据屏幕宽度和设备类型设置预览宽度
  const isMobile = window.innerWidth <= 768
  if (isImageFile(file)) {
    previewWidth.value = isMobile ? window.innerWidth - 32 : 900
  } else if (isPdfFile(file)) {
    previewWidth.value = isMobile ? window.innerWidth - 32 : 1000
  } else {
    previewWidth.value = isMobile ? window.innerWidth - 32 : 800
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

// 客户门户日期格式（不显示秒）
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

// 使用统一的状态工具函数
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

// 窗口大小变化监听
let resizeHandler: (() => void) | null = null

onMounted(() => {
  loadMatterDetail()
  
  // 监听窗口大小变化
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
  background: var(--bg-secondary);
}





















.content {
  padding: 32px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-card {
  background: transparent;
  padding: 0;
}

.detail-card :deep(.ant-card) {
  border-radius: var(--radius-md, 8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border-color, #e8e8e8);
  transition: all 0.3s ease;
}

.detail-card :deep(.ant-card:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.detail-card :deep(.ant-card-head) {
  background: linear-gradient(90deg, var(--bg-primary, #fff) 0%, var(--bg-secondary, #f5f5f5) 100%);
  border-bottom: 2px solid var(--primary-color-light, #69c0ff);
  padding: 12px 24px;
}

.detail-card :deep(.ant-card-head-title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #333);
}

/* 进度区域 */
.progress-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-item .label {
  color: #666;
  min-width: 80px;
}

/* 期限项 */
.deadline-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.deadline-title {
  font-weight: 500;
}

.deadline-date {
  color: #666;
  font-size: 13px;
}

/* 主办律师 */
.lead-lawyer {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e8e8e8;
}

/* 文件操作按钮 */
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

.action-text {
  display: inline;
}

/* 上传按钮 */
.upload-btn-text {
  display: inline-block;
}

/* 进度条 */
.progress-bar {
  max-width: 100%;
  width: 100%;
}

@media (max-width: 768px) {
  .progress-bar {
    max-width: 100%;
  }
}

/* 预览模态框 */
:deep(.preview-modal-wrapper) {
  .ant-modal {
    margin: 0;
    max-width: 100vw;
    top: 0;
    padding-bottom: 0;
  }
  
  .ant-modal-content {
    height: 100vh;
    display: flex;
    flex-direction: column;
  }
  
  .ant-modal-body {
    flex: 1;
    overflow: auto;
    padding: 16px;
  }
  
  .ant-modal-footer {
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
  }
}

@media (min-width: 769px) {
  :deep(.preview-modal-wrapper .ant-modal) {
    margin: 0 auto;
    top: 50px;
    padding-bottom: 24px;
  }
  
  :deep(.preview-modal-wrapper .ant-modal-content) {
    height: auto;
  }
  
  :deep(.preview-modal-wrapper .ant-modal-body) {
    padding: 24px;
  }
}

/* 上传模态框移动端优化 */
:deep(.upload-modal-wrapper .ant-modal) {
  margin: 0;
  max-width: 100vw;
  top: 0;
  padding-bottom: 0;
}

:deep(.upload-modal-wrapper .ant-modal-content) {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

:deep(.upload-modal-wrapper .ant-modal-body) {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

:deep(.upload-modal-wrapper .ant-modal-footer) {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

@media (min-width: 769px) {
  :deep(.upload-modal-wrapper .ant-modal) {
    margin: 0 auto;
    top: 50px;
    padding-bottom: 24px;
  }
  
  :deep(.upload-modal-wrapper .ant-modal-content) {
    height: auto;
  }
  
  :deep(.upload-modal-wrapper .ant-modal-body) {
    padding: 24px;
  }
  
  :deep(.upload-modal-wrapper .ant-modal-footer) {
    padding: 10px 16px;
  }
}

.preview-modal-footer {
  width: 100%;
  justify-content: flex-end;
}

.preview-modal-footer :deep(.ant-space-item) {
  flex: 1;
}

.preview-modal-footer :deep(.ant-btn) {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  
  .content {
    padding: 16px 12px;
  }
  
  .detail-card :deep(.ant-card) {
    margin-bottom: 16px;
  }
  
  .detail-card :deep(.ant-card-head) {
    padding: 12px 16px;
  }
  
  .detail-card :deep(.ant-card-head-title) {
    font-size: 14px;
  }
  
  .detail-card :deep(.ant-card-body) {
    padding: 12px;
  }
  
  /* 描述列表移动端优化 - 防止标签被截断 */
  .detail-card :deep(.ant-descriptions) {
    font-size: 13px;
  }
  
  .detail-card :deep(.ant-descriptions-item-label) {
    padding: 8px 8px !important;
    min-width: 70px;
    white-space: nowrap;
  }
  
  .detail-card :deep(.ant-descriptions-item-content) {
    padding: 8px 8px !important;
    word-break: break-all;
  }
  
  .progress-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .progress-item .label {
    min-width: auto;
  }
  
  /* 文件表格移动端优化 */
  .detail-card :deep(.ant-table) {
    font-size: 12px;
  }
  
  .detail-card :deep(.ant-table-thead > tr > th) {
    padding: 8px 6px;
    font-size: 12px;
  }
  
  .detail-card :deep(.ant-table-tbody > tr > td) {
    padding: 8px 6px;
  }
  
  /* 文件来源标签移动端 */
  .detail-card :deep(.ant-tag) {
    margin: 0;
    padding: 0 4px;
    font-size: 11px;
  }
  
  /* 文件操作按钮移动端 - 只显示图标 */
  .file-actions {
    flex-wrap: nowrap;
    gap: 2px !important;
  }
  
  .action-btn {
    padding: 2px 4px !important;
    min-width: auto !important;
    height: 28px !important;
  }
  
  .action-btn :deep(.anticon) {
    font-size: 14px;
  }
  
  .action-text {
    display: none;
  }
  
  /* 上传按钮移动端 */
  .upload-btn {
    width: 100%;
  }
  
  .upload-btn-text {
    display: inline;
  }
  
  /* 费用统计移动端 */
  .detail-card :deep(.ant-statistic-title) {
    font-size: 12px;
  }
  
  .detail-card :deep(.ant-statistic-content) {
    font-size: 20px;
  }
}

@media (max-width: 480px) {

  
  .back-btn {
    padding: 4px 8px;
  }
  
  .content {
    padding: 12px 8px;
  }
  
  .detail-card :deep(.ant-card-head) {
    padding: 10px 12px;
  }
  
  .detail-card :deep(.ant-card-body) {
    padding: 12px;
  }
  
  /* 文件表格超小屏 */
  .detail-card :deep(.ant-table-thead > tr > th),
  .detail-card :deep(.ant-table-tbody > tr > td) {
    padding: 6px 2px;
    font-size: 11px;
  }
  
  /* 预览模态框超小屏 */
  :deep(.preview-modal-wrapper .ant-modal-body) {
    padding: 12px;
  }
  
  :deep(.preview-modal-wrapper .ant-modal-footer) {
    padding: 8px 12px;
  }
  
  .preview-modal-footer :deep(.ant-btn) {
    font-size: 14px;
    height: 36px;
  }

  /* 为底部导航留出空间 */
  .matter-detail-container {
    padding-bottom: 70px;
  }
}
</style>
