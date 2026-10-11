<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">CLIENT PORTAL · DEMO</p>
      <h1>{{ config.lawFirmName }}</h1>
      <p class="le-sub">{{ config.portalAccessNotice || '客户服务门户 · 体验演示' }}</p>
      <div class="le-actions">
        <a class="le-btn le-btn--primary" @click="go('/matters')">我的项目</a>
        <a class="le-btn" @click="go('/files')">文件中心</a>
        <a class="le-btn" @click="go('/notifications')">消息通知</a>
        <a class="le-btn" @click="go('/matters/M2026001')">查看项目详情</a>
      </div>
    </header>

    <section class="le-cards">
      <div class="le-mini">
        <h3>项目信息查询</h3>
        <p>实时查看委托项目的进展阶段、关键节点与负责团队。</p>
      </div>
      <div class="le-mini">
        <h3>文件安全共享</h3>
        <p>系统推送法律文书，客户可上传补充材料，全程留痕。</p>
      </div>
      <div class="le-mini">
        <h3>消息即时通知</h3>
        <p>短信、微信、邮件多渠道触达，重要节点不遗漏。</p>
      </div>
      <div class="le-mini">
        <h3>函件真伪验证</h3>
        <p>扫码/输入编号即可核验律师函与法律意见书真伪。</p>
      </div>
    </section>

    <section class="le-card">
      <div class="le-card-head"><h2>功能入口（体验）</h2></div>
      <div class="le-actions le-actions--wrap">
        <a class="le-btn" @click="go('/help')">帮助中心</a>
        <a class="le-btn" @click="go('/verify/letter')">函件验证</a>
        <a class="le-btn" @click="go('/admin/login')">工作人员入口</a>
      </div>
    </section>

    <footer class="le-foot">{{ config.copyright }} · {{ config.icpLicense }}</footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { getPortalConfig, type PortalConfig } from '@/api/config'

const router = useRouter()
const config = reactive<Partial<PortalConfig>>({
  lawFirmName: '律师事务所',
  portalAccessNotice: '',
  copyright: '',
  icpLicense: '',
})

function go(path: string) {
  router.push(path)
}

onMounted(async () => {
  try {
    const res = await getPortalConfig()
    Object.assign(config, res.data)
  } catch {
    /* 演示环境静默失败，保留默认文案 */
  }
})
</script>
