<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">MATTER MANAGEMENT</p>
      <h1>项目列表</h1>
      <p class="le-sub">汇衡律师事务所 · 客户服务项目与访问授权总览（演示数据）</p>
    </header>

    <div class="le-stats">
      <div v-for="s in stats" :key="s.label" class="le-stat">
        <span class="le-stat-value">{{ s.value }}</span>
        <span class="le-stat-label">{{ s.label }}</span>
      </div>
    </div>

    <section class="le-card">
      <div class="le-card-head">
        <h2>全部项目</h2>
        <span class="le-count">共 {{ matters.length }} 条</span>
      </div>
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>项目编号</span><span>客户 / 项目名称</span><span>状态</span><span>有效期至</span><span>操作</span>
        </div>
        <div v-for="m in matters" :key="m.id" class="le-row">
          <span class="le-mono">{{ m.id }}</span>
          <span><strong>{{ m.clientName }}</strong><br><small>{{ m.matterName }}</small></span>
          <span><em :class="['le-badge', m.status === 'ACTIVE' ? 'is-ok' : 'is-warn']">{{ m.statusName }}</em></span>
          <span>{{ (m.expiresAt || '').slice(0, 10) }}</span>
          <span><a class="le-link" @click="goDetail(m.id)">查看详情</a></span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getMatterList, type MatterListItem } from '@/api/matter'

const router = useRouter()
const matters = ref<MatterListItem[]>([])
const stats = ref([
  { label: '项目总数', value: '—' },
  { label: '进行中', value: '—' },
  { label: '已过期', value: '—' },
])

onMounted(async () => {
  try {
    const res = await getMatterList({ limit: 100 })
    matters.value = res.data || []
    stats.value = [
      { label: '项目总数', value: String(matters.value.length) },
      { label: '进行中', value: String(matters.value.filter(m => m.status === 'ACTIVE').length) },
      { label: '已过期', value: String(matters.value.filter(m => m.status !== 'ACTIVE').length) },
    ]
  } catch {
    message.error('项目列表加载失败')
  }
})

function goDetail(id: string) {
  router.push(`/admin/matters/${id}`)
}
</script>
