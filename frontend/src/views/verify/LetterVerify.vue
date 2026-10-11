<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">LETTER VERIFICATION</p>
      <h1>函件真伪验证</h1>
      <p class="le-sub">输入函件申请编号与验证码核验真伪（演示：任意输入均返回有效）</p>
    </header>

    <section class="le-card">
      <div class="le-form">
        <label>函件申请编号
          <input v-model="no" placeholder="如 LF-2026-000128" />
        </label>
        <label>验证码
          <input v-model="code" placeholder="如 8F3K2M" />
        </label>
        <a class="le-btn le-btn--primary" @click="submit">立即验证</a>
      </div>
    </section>

    <section v-if="result" class="le-card">
      <div class="le-card-head">
        <h2>验证结果</h2>
        <em :class="['le-badge', result.valid ? 'is-ok' : 'is-danger']">{{ result.statusMessage }}</em>
      </div>
      <dl class="le-dl">
        <div><dt>申请编号</dt><dd class="le-mono">{{ result.applicationNo }}</dd></div>
        <div><dt>函件类型</dt><dd>{{ result.letterTypeName }}</dd></div>
        <div><dt>出具律所</dt><dd>{{ result.firmName }}</dd></div>
        <div><dt>承办律师</dt><dd>{{ result.lawyerNames }}</dd></div>
        <div><dt>目标单位</dt><dd>{{ result.targetUnit }}</dd></div>
        <div><dt>有效期至</dt><dd>{{ (result.validUntil || '').slice(0, 10) }}</dd></div>
      </dl>
    </section>

    <footer class="le-foot">© 汇衡律师事务所 · 函件验证（Demo）</footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { verifyLetter, type LetterVerificationResult } from '@/api/letter-verification'

const no = ref('')
const code = ref('')
const result = ref<LetterVerificationResult | null>(null)

async function submit() {
  if (!no.value.trim() || !code.value.trim()) {
    message.warning('请输入函件编号与验证码')
    return
  }
  try {
    const res = await verifyLetter(no.value.trim(), code.value.trim())
    result.value = res.data
  } catch {
    message.error('验证失败')
  }
}
</script>
