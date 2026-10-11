<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">HELP CENTER</p>
      <h1>帮助中心</h1>
      <p class="le-sub">常见问题与使用指引（演示内容）</p>
    </header>

    <section class="le-card">
      <div class="le-faq" v-for="(q, i) in faqs" :key="i">
        <h3>{{ q.q }}</h3>
        <p>{{ q.a }}</p>
      </div>
    </section>

    <footer class="le-foot">© 汇衡律师事务所 · 客户服务门户（Demo）</footer>
  </div>
</template>

<script setup lang="ts">
const faqs = [
  { q: '如何查看我的项目进展？', a: '进入「我的项目」，点击任意项目即可查看当前阶段、进度与关键节点。' },
  { q: '文件下载安全吗？', a: '所有文件通过专属访问令牌校验，令牌失效后链接立即不可用。' },
  { q: '如何验证律师函真伪？', a: '进入「函件验证」，输入函件编号与验证码即可核验。' },
  { q: '这是一套真实系统吗？', a: '当前为体验 Demo，数据均为演示数据，可自由浏览各项功能。' },
]
</script>
