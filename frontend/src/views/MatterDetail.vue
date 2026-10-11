<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">MATTER DETAIL</p>
      <h1>{{ matter?.matterData?.matterName || '项目详情' }}</h1>
      <p class="le-sub">{{ matter?.clientName }} · 编号 {{ matter?.id }}</p>
    </header>

    <section class="le-grid-2">
      <div class="le-card">
        <div class="le-card-head"><h2>进展概况</h2></div>
        <dl class="le-dl">
          <div><dt>当前阶段</dt><dd>{{ matter?.matterData?.currentStageName }}</dd></div>
          <div><dt>整体进度</dt><dd>{{ matter?.matterData?.progress }}%</dd></div>
          <div><dt>主办律师</dt><dd>{{ matter?.matterData?.leadLawyerName }}</dd></div>
          <div><dt>最近更新</dt><dd>{{ matter?.matterData?.lastUpdateTime }}</dd></div>
        </dl>
      </div>
      <div class="le-card">
        <div class="le-card-head"><h2>团队</h2></div>
        <ul class="le-people">
          <li v-for="l in matter?.matterData?.lawyers || []" :key="l.name">
            <strong>{{ l.name }}</strong><small>{{ l.title }}</small>
          </li>
        </ul>
      </div>
    </section>

    <section class="le-card">
      <div class="le-card-head"><h2>关键节点</h2></div>
      <ul class="le-timeline">
        <li v-for="d in matter?.matterData?.deadlines || []" :key="d.title">
          <span class="le-dot" />
          <div><strong>{{ d.title }}</strong><small>{{ d.deadline }}</small></div>
        </li>
      </ul>
    </section>

    <footer class="le-foot">© 汇衡律师事务所 · 客户服务门户（Demo）</footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getClientMatterDetail, type ClientMatterDetail } from '@/api/matter'

const route = useRoute()
const DEMO_TOKEN = 'demo-portal-token-2026'
const matter = ref<ClientMatterDetail | null>(null)

onMounted(async () => {
  try {
    const res = await getClientMatterDetail(String(route.params.id), DEMO_TOKEN)
    matter.value = res.data
  } catch {
    message.error('项目详情加载失败')
  }
})
</script>
