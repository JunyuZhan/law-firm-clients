<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">MATTER DETAIL</p>
      <h1>{{ matter?.matterData?.matterName || '项目详情' }}</h1>
      <p class="le-sub">{{ matter?.clientName }} · 项目编号 {{ matter?.id }}</p>
    </header>

    <section class="le-grid-2">
      <div class="le-card">
        <div class="le-card-head"><h2>项目概况</h2></div>
        <dl class="le-dl">
          <div><dt>项目编号</dt><dd class="le-mono">{{ matter?.id }}</dd></div>
          <div><dt>当前阶段</dt><dd>{{ matter?.matterData?.currentStageName }}</dd></div>
          <div><dt>整体进度</dt><dd>{{ matter?.matterData?.progress }}%</dd></div>
          <div><dt>主办律师</dt><dd>{{ matter?.matterData?.leadLawyerName }}</dd></div>
          <div><dt>团队</dt><dd>{{ team }}</dd></div>
          <div><dt>最近更新</dt><dd>{{ matter?.matterData?.lastUpdateTime }}</dd></div>
        </dl>
      </div>

      <div class="le-card">
        <div class="le-card-head"><h2>费用概览</h2></div>
        <dl class="le-dl">
          <div><dt>合同金额</dt><dd>¥ {{ fmt(matter?.matterData?.contractAmount) }}</dd></div>
          <div><dt>已收款项</dt><dd>¥ {{ fmt(matter?.matterData?.receivedAmount) }}</dd></div>
          <div><dt>待收款项</dt><dd>¥ {{ fmt(matter?.matterData?.pendingAmount) }}</dd></div>
          <div><dt>任务进度</dt><dd>{{ matter?.matterData?.completedTaskCount }} / {{ matter?.matterData?.totalTaskCount }}</dd></div>
          <div><dt>文档数量</dt><dd>{{ matter?.matterData?.documentCount }} 份</dd></div>
        </dl>
      </div>
    </section>

    <section class="le-card">
      <div class="le-card-head"><h2>待办任务</h2></div>
      <ul class="le-timeline">
        <li v-for="t in matter?.matterData?.pendingTasks || []" :key="t.title">
          <span class="le-dot" />
          <div>
            <strong>{{ t.title }}</strong>
            <small>截止 {{ t.dueDate }} · 优先级 {{ t.priority }}</small>
          </div>
        </li>
      </ul>
    </section>

    <section class="le-card">
      <div class="le-card-head"><h2>关键节点</h2></div>
      <ul class="le-timeline">
        <li v-for="d in matter?.matterData?.deadlines || []" :key="d.title">
          <span class="le-dot" :class="{ 'is-warn': d.isOverdue }" />
          <div>
            <strong>{{ d.title }}</strong>
            <small>{{ d.deadline }}</small>
          </div>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { getMatterDetail, type MatterDetailInfo } from '@/api/matter'

const route = useRoute()
const matter = ref<MatterDetailInfo | null>(null)

const team = computed(() => (matter.value?.matterData?.lawyers || []).map(l => l.name).join('、') || '—')

function fmt(v?: number) {
  return (v ?? 0).toLocaleString('zh-CN')
}

onMounted(async () => {
  try {
    const res = await getMatterDetail(String(route.params.id))
    matter.value = res.data
  } catch {
    message.error('项目详情加载失败')
  }
})
</script>
