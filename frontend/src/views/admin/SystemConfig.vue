<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">SYSTEM CONFIG</p>
      <h1>系统配置</h1>
      <p class="le-sub">系统级参数与开关配置（演示数据，修改不会生效）</p>
    </header>

    <section class="le-card">
      <div class="le-card-head"><h2>配置项</h2></div>
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>配置键</span><span>配置值</span><span>类型</span><span>说明</span>
        </div>
        <div v-for="c in list" :key="c.id" class="le-row">
          <span class="le-mono">{{ c.configKey }}</span>
          <span>{{ c.configValue }}</span>
          <span><em class="le-badge">{{ c.configType }}</em></span>
          <span>{{ c.description || '—' }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getConfigList, type SysConfigInfo } from '@/api/config'

const list = ref<SysConfigInfo[]>([])

onMounted(async () => {
  try {
    const res = await getConfigList({ limit: 50 })
    list.value = res.data || []
  } catch {
    message.error('配置加载失败')
  }
})
</script>
