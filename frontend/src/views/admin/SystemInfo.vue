<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">SYSTEM INFO</p>
      <h1>系统信息</h1>
      <p class="le-sub">运行版本与依赖健康状态（演示数据）</p>
    </header>

    <section class="le-card">
      <div class="le-card-head"><h2>运行信息</h2></div>
      <dl class="le-dl">
        <div><dt>应用名称</dt><dd>{{ info.applicationName }}</dd></div>
        <div><dt>产品版本</dt><dd>{{ info.productVersion }}</dd></div>
        <div><dt>后端版本</dt><dd>{{ info.backendVersion }}</dd></div>
        <div><dt>构建时间</dt><dd>{{ info.buildTime }}</dd></div>
        <div><dt>运行模式</dt><dd><em class="le-badge is-ok">{{ info.recommendedMode }}</em></dd></div>
      </dl>
    </section>

    <section class="le-card">
      <div class="le-card-head"><h2>依赖状态</h2></div>
      <dl class="le-dl">
        <div v-for="i in dep.items" :key="i.key">
          <dt>{{ i.label }}</dt>
          <dd><em :class="['le-badge', i.status === 'UP' ? 'is-ok' : 'is-warn']">{{ i.status }}</em></dd>
        </div>
      </dl>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getSystemRuntimeInfo, getSystemDependencyStatus, type SystemRuntimeInfo, type SystemDependencyStatus } from '@/api/config'

const info = ref<SystemRuntimeInfo>({
  applicationName: '—', productVersion: '—', backendVersion: '—',
  commitSha: '', buildTime: '', recommendedMode: '—', serverTime: '',
})
const dep = ref<SystemDependencyStatus>({ overallStatus: '—', items: [] })

onMounted(async () => {
  try {
    const [r, d] = await Promise.all([getSystemRuntimeInfo(), getSystemDependencyStatus()])
    info.value = r.data
    dep.value = d.data
  } catch {
    message.error('系统信息加载失败')
  }
})
</script>
