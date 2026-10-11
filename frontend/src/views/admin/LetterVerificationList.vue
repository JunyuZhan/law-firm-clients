<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">LETTER VERIFICATION</p>
      <h1>函件验证</h1>
      <p class="le-sub">律师函 / 法律意见书 真伪验证记录（演示数据）</p>
    </header>

    <div class="le-stats">
      <div class="le-stat"><span class="le-stat-value">{{ st.total }}</span><span class="le-stat-label">总记录</span></div>
      <div class="le-stat"><span class="le-stat-value">{{ st.active }}</span><span class="le-stat-label">有效</span></div>
      <div class="le-stat"><span class="le-stat-value">{{ st.expired }}</span><span class="le-stat-label">已过期</span></div>
      <div class="le-stat"><span class="le-stat-value">{{ st.revoked }}</span><span class="le-stat-label">已撤销</span></div>
    </div>

    <section class="le-card">
      <div class="le-card-head"><h2>验证记录</h2></div>
      <div class="le-table">
        <div class="le-row le-row--head">
          <span>申请编号</span><span>函件类型</span><span>承办律师</span><span>目标单位</span><span>有效期至</span><span>状态</span>
        </div>
        <div v-for="r in records" :key="r.id" class="le-row">
          <span class="le-mono">{{ r.applicationNo }}</span>
          <span>{{ r.letterTypeName }}</span>
          <span>{{ r.lawyerNames }}</span>
          <span>{{ r.targetUnit }}</span>
          <span>{{ (r.validUntil || '').slice(0, 10) }}</span>
          <span><em :class="['le-badge', r.status === 'VALID' ? 'is-ok' : 'is-warn']">{{ r.status === 'VALID' ? '有效' : r.status }}</em></span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getVerificationList, getVerificationStatistics, type LetterVerificationRecord } from '@/api/letter-verification'

const records = ref<LetterVerificationRecord[]>([])
const st = ref({ total: 0, active: 0, expired: 0, revoked: 0 })

onMounted(async () => {
  try {
    const [l, s] = await Promise.all([
      getVerificationList({ page: 1, pageSize: 20 }),
      getVerificationStatistics(),
    ])
    records.value = l.data.records || []
    st.value = { total: s.data.total, active: s.data.active, expired: s.data.expired, revoked: s.data.revoked }
  } catch {
    message.error('验证记录加载失败')
  }
})
</script>
