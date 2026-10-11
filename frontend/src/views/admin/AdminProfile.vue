<template>
  <div class="le-page">
    <header class="le-hero">
      <p class="le-eyebrow">PROFILE</p>
      <h1>个人中心</h1>
      <p class="le-sub">当前登录管理员信息（演示数据）</p>
    </header>

    <section class="le-card">
      <div class="le-card-head"><h2>账号信息</h2></div>
      <dl class="le-dl">
        <div><dt>用户名</dt><dd class="le-mono">{{ user?.username || '—' }}</dd></div>
        <div><dt>姓名</dt><dd>{{ user?.realName || '—' }}</dd></div>
        <div><dt>邮箱</dt><dd>{{ user?.email || '—' }}</dd></div>
        <div><dt>角色</dt><dd><em class="le-badge is-ok">{{ user?.superAdmin ? '超级管理员' : '普通管理员' }}</em></dd></div>
        <div><dt>最近登录</dt><dd>{{ (user?.lastLoginAt || '—').slice(0, 19).replace('T', ' ') }}</dd></div>
      </dl>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { UserInfo } from '@/api/auth'

const authStore = useAuthStore()
const user = computed<UserInfo | null>(() => authStore.getCurrentUserInfo())
</script>
