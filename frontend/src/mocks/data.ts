/**
 * Demo Mock 假数据集中定义。
 * 所有返回体均遵循后端统一响应格式：{ code: 200, message: 'success', data, success: true }
 */

export const DEMO_USER = {
  id: 1,
  username: 'demo',
  realName: '演示管理员',
  email: 'demo@lawfirm.example.com',
  lastLoginAt: new Date().toISOString(),
  // 体验 Demo 默认给超管身份，方便完整浏览管理后台全部菜单
  superAdmin: true,
}

export const DEMO_TOKEN = 'demo-token-lex-elite-2026'
export const DEMO_CSRF = 'demo-csrf-token-lex-elite-2026'

/** 门户访问 token（体验门户页用） */
export const DEMO_PORTAL_TOKEN = 'demo-portal-token-2026'

export const MATTERS = [
  {
    id: 'M2026001',
    lawFirmMatterId: 100001,
    clientId: 5001,
    clientName: '华远控股集团有限公司',
    matterName: '华远控股并购重组专项法律服务',
    status: 'ACTIVE',
    statusName: '进行中',
    validDays: 180,
    expiresAt: '2026-12-31T23:59:59',
    accessUrl: '/portal/matter/M2026001?token=demo-portal-token-2026',
    accessToken: DEMO_PORTAL_TOKEN,
    createdAt: '2026-03-01T09:20:00',
    updatedAt: '2026-09-28T16:05:00',
  },
  {
    id: 'M2026002',
    lawFirmMatterId: 100002,
    clientId: 5001,
    clientName: '华远控股集团有限公司',
    matterName: '华远控股知识产权维权系列案件',
    status: 'ACTIVE',
    statusName: '进行中',
    validDays: 120,
    expiresAt: '2026-11-30T23:59:59',
    accessUrl: '/portal/matter/M2026002?token=demo-portal-token-2026',
    accessToken: DEMO_PORTAL_TOKEN,
    createdAt: '2026-05-12T10:00:00',
    updatedAt: '2026-09-30T11:30:00',
  },
  {
    id: 'M2026003',
    lawFirmMatterId: 100003,
    clientId: 5001,
    clientName: '华远控股集团有限公司',
    matterName: '常年法律顾问服务（2026年度）',
    status: 'ACTIVE',
    statusName: '进行中',
    validDays: 365,
    expiresAt: '2027-01-31T23:59:59',
    accessUrl: '/portal/matter/M2026003?token=demo-portal-token-2026',
    accessToken: DEMO_PORTAL_TOKEN,
    createdAt: '2026-01-05T08:30:00',
    updatedAt: '2026-10-08T09:15:00',
  },
  {
    id: 'M2025009',
    lawFirmMatterId: 100090,
    clientId: 5001,
    clientName: '华远控股集团有限公司',
    matterName: '劳动争议仲裁代理（已结案）',
    status: 'EXPIRED',
    statusName: '已过期',
    validDays: 90,
    expiresAt: '2026-06-30T23:59:59',
    accessUrl: '/portal/matter/M2025009?token=demo-portal-token-2026',
    accessToken: DEMO_PORTAL_TOKEN,
    createdAt: '2025-12-01T09:00:00',
    updatedAt: '2026-07-01T00:00:00',
  },
]

export function matterDetail(id: string) {
  const base = MATTERS.find(m => m.id === id) || MATTERS[0]
  return {
    id: base.id,
    lawFirmMatterId: base.lawFirmMatterId,
    clientId: base.clientId,
    clientName: base.clientName,
    status: base.status,
    validDays: base.validDays,
    expiresAt: base.expiresAt,
    accessToken: DEMO_PORTAL_TOKEN,
    accessUrl: base.accessUrl,
    createdAt: base.createdAt,
    updatedAt: base.updatedAt,
    scopes: 'matter:read,file:read,file:write',
    matterData: {
      matterName: base.matterName,
      matterNo: base.id,
      matterType: 'LITIGATION',
      matterTypeName: '诉讼/非诉专项',
      status: base.status,
      statusName: base.statusName,
      createDate: base.createdAt?.slice(0, 10),
      currentStage: 'EVIDENCE',
      currentStageName: '证据整理与庭前准备',
      progress: 62,
      lastUpdateTime: base.updatedAt,
      leadLawyerName: '沈明远',
      leadLawyerContact: '138****6688',
      lawyers: [
        { name: '沈明远', title: '高级合伙人', contact: '138****6688' },
        { name: '林清和', title: '资深律师', contact: '139****2233' },
        { name: '苏哲', title: '律师助理', contact: '137****9911' },
      ],
      teamMembers: [
        { name: '沈明远', title: '高级合伙人' },
        { name: '林清和', title: '资深律师' },
      ],
      completedTaskCount: 18,
      totalTaskCount: 29,
      pendingTasks: [
        { title: '补充提交第三组证据清单', dueDate: '2026-10-18', priority: 'HIGH', status: 'PENDING' },
        { title: '完成对方证据质证意见', dueDate: '2026-10-22', priority: 'MEDIUM', status: 'PENDING' },
      ],
      deadlines: [
        { title: '开庭审理', type: 'COURT', isOverdue: false, deadline: '2026-11-06' },
        { title: '举证期限届满', type: 'EVIDENCE', isOverdue: false, deadline: '2026-10-25' },
      ],
      contractAmount: 680000,
      receivedAmount: 408000,
      pendingAmount: 272000,
      documentCount: 24,
      documents: [
        { name: '委托代理协议.pdf', uploadTime: '2026-03-02 10:12', type: 'PDF' },
        { name: '证据目录（第一组）.xlsx', uploadTime: '2026-08-15 14:40', type: 'XLSX' },
        { name: '法律意见书.docx', uploadTime: '2026-09-20 09:05', type: 'DOCX' },
      ],
    },
  }
}

export const FILES = [
  {
    id: 'F1001',
    matterId: 'M2026001',
    clientId: 5001,
    fileName: '委托代理协议（签署版）.pdf',
    fileSize: 486912,
    fileType: 'application/pdf',
    fileCategory: 'CONTRACT',
    description: '双方签署盖章的正式委托代理协议',
    fileSource: 'PUSHED',
    uploadedAt: '2026-03-02 10:12:00',
    status: 'AVAILABLE',
  },
  {
    id: 'F1002',
    matterId: 'M2026001',
    clientId: 5001,
    fileName: '案件进展报告-2026年9月.pdf',
    fileSize: 233472,
    fileType: 'application/pdf',
    fileCategory: 'REPORT',
    description: '月度进展汇报',
    fileSource: 'PUSHED',
    uploadedAt: '2026-09-30 17:20:00',
    status: 'AVAILABLE',
  },
  {
    id: 'F1003',
    matterId: 'M2026001',
    clientId: 5001,
    fileName: '客户补充材料-财务凭证.zip',
    fileSize: 5242880,
    fileType: 'application/zip',
    fileCategory: 'EVIDENCE',
    description: '客户上传的补充证据材料',
    fileSource: 'CLIENT_UPLOAD',
    uploadedAt: '2026-10-05 11:02:00',
    status: 'AVAILABLE',
  },
  {
    id: 'F1004',
    matterId: 'M2026002',
    clientId: 5001,
    fileName: '知识产权维权方案.docx',
    fileSize: 128000,
    fileType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    fileCategory: 'DOCUMENT',
    description: '维权策略与行动方案',
    fileSource: 'PUSHED',
    uploadedAt: '2026-06-01 09:40:00',
    status: 'AVAILABLE',
  },
]

export const NOTIFICATIONS = [
  {
    id: 9001,
    matterId: 'M2026001',
    clientId: 5001,
    matterName: '华远控股并购重组专项法律服务',
    title: '案件进展提醒',
    content: '您的并购重组专项法律服务已完成证据整理，下一阶段进入庭前准备。',
    notificationType: 'SMS',
    recipient: '138****6688',
    status: 'SUCCESS',
    sentAt: '2026-09-28 16:05:00',
    createdAt: '2026-09-28 16:05:00',
  },
  {
    id: 9002,
    matterId: 'M2026001',
    clientId: 5001,
    matterName: '华远控股并购重组专项法律服务',
    title: '开庭时间通知',
    content: '本案定于 2026-11-06 上午 9:30 于市中级人民法院第三法庭开庭，请知悉。',
    notificationType: 'WECHAT',
    recipient: '华远控股法务部',
    status: 'SUCCESS',
    sentAt: '2026-10-08 09:15:00',
    createdAt: '2026-10-08 09:15:00',
  },
  {
    id: 9003,
    matterId: 'M2026003',
    clientId: 5001,
    matterName: '常年法律顾问服务（2026年度）',
    title: '合同续签提醒',
    content: '您的常年法律顾问服务将于 2027-01-31 到期，建议提前安排续签。',
    notificationType: 'EMAIL',
    recipient: 'legal@huayuan.example.com',
    status: 'PENDING',
    sentAt: '',
    createdAt: '2026-10-09 10:00:00',
  },
  {
    id: 9004,
    matterId: 'M2025009',
    clientId: 5001,
    matterName: '劳动争议仲裁代理（已结案）',
    title: '结案通知',
    content: '您委托的劳动争议仲裁代理事项已结案，相关文书已归档。',
    notificationType: 'SMS',
    recipient: '138****6688',
    status: 'FAILED',
    sentAt: '2026-07-01 08:00:00',
    createdAt: '2026-07-01 08:00:00',
  },
]

export const DASHBOARD_STATS = {
  matterCount: 12,
  activeMatterCount: 9,
  expiredMatterCount: 3,
  notificationCount: 46,
  notificationSuccessCount: 41,
  notificationFailedCount: 3,
  fileCount: 87,
  totalFileSize: 268435456,
  verificationCount: 15,
  activeVerificationCount: 12,
  apiKeyCount: 2,
  accessLogCount: 1280,
  todayAccessCount: 37,
}

export const NOTIFICATION_STATS = {
  total: 46,
  success: 41,
  failed: 3,
  pending: 2,
  sms: { total: 22, success: 20, failed: 1, pending: 1 },
  wechat: { total: 14, success: 13, failed: 1, pending: 0 },
  email: { total: 10, success: 8, failed: 1, pending: 1 },
}

export const API_KEYS = [
  {
    id: 1,
    keyName: '律所业务系统对接',
    apiKey: 'lf_demo_5f3a9c1e7b2d4a86',
    apiSecret: '****（演示已隐藏）',
    enabled: true,
    expiresAt: '2027-01-01 00:00:00',
    lastUsedAt: '2026-10-10 15:22:31',
    createdAt: '2026-01-10 09:00:00',
    updatedAt: '2026-09-01 09:00:00',
  },
  {
    id: 2,
    keyName: '测试环境密钥',
    apiKey: 'lf_demo_test_1a2b3c4d5e6f7788',
    apiSecret: '****（演示已隐藏）',
    enabled: false,
    expiresAt: '2026-12-31 00:00:00',
    lastUsedAt: '2026-06-18 11:03:00',
    createdAt: '2026-02-20 14:00:00',
    updatedAt: '2026-06-18 11:03:00',
  },
]

export const CONFIGS = [
  { id: 1, configKey: 'app.name', configValue: '律师事务所客户服务系统', configType: 'STRING', description: '系统名称（浏览器标题）', createdAt: '2026-01-01 00:00:00', updatedAt: '2026-09-01 00:00:00' },
  { id: 2, configKey: 'law.firm.name', configValue: '汇衡律师事务所', configType: 'STRING', description: '律所名称', createdAt: '2026-01-01 00:00:00', updatedAt: '2026-09-01 00:00:00' },
  { id: 3, configKey: 'notification.sms.enabled', configValue: 'true', configType: 'BOOLEAN', description: '是否启用短信通知', createdAt: '2026-01-01 00:00:00', updatedAt: '2026-09-01 00:00:00' },
]

export const TEMPLATES = [
  { id: 1, templateName: '案件进展提醒', templateType: 'SMS', templateCode: 'SMS_PROGRESS', templateContent: '【${firm}】您的项目「${matter}」有新进展：${content}', templateVariables: 'firm,matter,content', provider: 'aliyun', signName: '汇衡律所', enabled: true, description: '项目进展短信模板', createdAt: '2026-01-05 00:00:00', updatedAt: '2026-08-01 00:00:00' },
  { id: 2, templateName: '开庭通知', templateType: 'WECHAT', templateCode: 'WX_COURT', templateContent: '您委托的案件将于 ${date} 开庭，请准时参加。', templateVariables: 'date', provider: 'wechat', signName: '', enabled: true, description: '开庭微信模板', createdAt: '2026-01-05 00:00:00', updatedAt: '2026-08-01 00:00:00' },
  { id: 3, templateName: '合同到期提醒', templateType: 'EMAIL', templateCode: 'EMAIL_EXPIRE', templateContent: '尊敬的客户，您的服务将于 ${date} 到期。', templateVariables: 'date', provider: 'aliyun', signName: '汇衡律所', enabled: false, description: '到期邮件模板', createdAt: '2026-01-05 00:00:00', updatedAt: '2026-08-01 00:00:00' },
]

export const VERIFICATION_RECORDS = [
  {
    id: 1, letterId: 3001, applicationNo: 'LF-2026-000128', verificationCode: '8F3K2M', letterType: 'LEGAL_OPINION', letterTypeName: '法律意见书',
    targetUnit: '市中级人民法院', lawyerNames: '沈明远、林清和', firmName: '汇衡律师事务所', matterName: '华远控股并购重组专项法律服务',
    approvedAt: '2026-09-10 10:00:00', printedAt: '2026-09-11 09:00:00', validUntil: '2027-09-10 00:00:00',
    remark: '正式用章文件', verifyCount: 6, lastVerifyAt: '2026-10-08 14:22:00', lastVerifyIp: '120.24.***.***', status: 'VALID',
    createdAt: '2026-09-10 10:00:00', updatedAt: '2026-10-08 14:22:00',
  },
  {
    id: 2, letterId: 3002, applicationNo: 'LF-2026-000131', verificationCode: 'P7Q4W9', letterType: 'LAWYER_LETTER', letterTypeName: '律师函',
    targetUnit: '上海某贸易有限公司', lawyerNames: '林清和', firmName: '汇衡律师事务所', matterName: '知识产权维权系列案件',
    approvedAt: '2026-09-20 15:30:00', printedAt: '2026-09-21 08:40:00', validUntil: '2026-12-20 00:00:00',
    remark: '催告函', verifyCount: 3, lastVerifyAt: '2026-10-01 09:10:00', lastVerifyIp: '58.246.***.***', status: 'VALID',
    createdAt: '2026-09-20 15:30:00', updatedAt: '2026-10-01 09:10:00',
  },
]

export const VERIFY_RESULT = {
  valid: true,
  verifyStatus: 'VALID',
  statusMessage: '验证通过，该函件真实有效',
  applicationNo: 'LF-2026-000128',
  letterTypeName: '法律意见书',
  firmName: '汇衡律师事务所',
  lawyerNames: '沈明远、林清和',
  targetUnit: '市中级人民法院',
  matterName: '华远控股并购重组专项法律服务',
  approvedAt: '2026-09-10 10:00:00',
  printedAt: '2026-09-11 09:00:00',
  validUntil: '2027-09-10 00:00:00',
  verifyCount: 6,
  remark: '正式用章文件',
}

export const PORTAL_CONFIG = {
  lawFirmName: '汇衡律师事务所',
  lawFirmWebsite: 'https://www.example-lawfirm.com',
  appSlogan: '专业 · 诚信 · 高效',
  icpLicense: '沪ICP备00000000号',
  copyright: '© 2026 汇衡律师事务所 版权所有',
  logoUrl: '',
  portalEyebrowEn: 'CLIENT SERVICE PORTAL',
  portalAccessNotice: '请输入您的专属访问链接或令牌以查看项目信息。',
  staffEntryLabel: '工作人员入口',
}

export const BRAND_CONFIG = {
  appName: '律师事务所客户服务系统',
  appShortName: '律所客服系统',
  appShortNameEn: 'Law Firm Service',
  logoUrl: '',
}

export const SYSTEM_RUNTIME_INFO = {
  applicationName: 'client-service-portal',
  productVersion: '1.0.0-demo',
  backendVersion: 'mock-1.0.0',
  commitSha: 'demo0000',
  buildTime: '2026-10-11 19:55:00',
  recommendedMode: 'DEMO',
  serverTime: new Date().toISOString(),
}

export const DEPENDENCY_STATUS = {
  overallStatus: 'HEALTHY',
  items: [
    { key: 'postgres', label: 'PostgreSQL', status: 'UP', details: { url: 'mock://localhost:5432' } },
    { key: 'redis', label: 'Redis', status: 'UP', details: { url: 'mock://localhost:6379' } },
    { key: 'sms', label: '短信服务', status: 'MOCK', details: { provider: 'aliyun' } },
  ],
}
