<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">CLIENT PORTAL</p>
      <h1>我的项目</h1>
      <p class="le-sub">您名下的委托项目列表（演示数据）</p>
    </header>

    <section class="le-card">
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>项目名称</span><span>状态</span><span>有效期至</span><span>操作</span>
        </div>
        <div v-for="m in list" :key="m.id" class="le-row">
          <span><strong>{{ m.matterName }}</strong><br><small>编号 {{ m.id }}</small></span>
          <span><em :class="['le-badge', m.status === 'ACTIVE' ? 'is-ok' : 'is-warn']">{{ m.statusName || m.status }}</em></span>
          <span>{{ (m.expiresAt || '').slice(0, 10) }}</span>
          <span><a class="le-link" @click="go(m.id)">查看</a></span>
        </div>
      </div>
    </section>

    <footer class="le-foot">© 汇衡律师事务所 · 客户服务门户（Demo）</footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getPortalMatterList, type PortalMatterListItem } from '@/api/matter'

const router = useRouter()
const DEMO_TOKEN = 'demo-portal-token-2026'
const list = ref<PortalMatterListItem[]>([])

function go(id: string) {
  router.push(`/matters/${id}`)
}

onMounted(async () => {
  try {
    const res = await getPortalMatterList(DEMO_TOKEN)
    list.value = res.data || []
  } catch {
    message.error('项目列表加载失败')
  }
})
</script>
