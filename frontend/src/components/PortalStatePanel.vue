<template>
  <section class="portal-state-panel">
    <div
      v-if="$slots.icon"
      class="portal-state-panel__icon"
      aria-hidden="true"
    >
      <slot name="icon" />
    </div>
    <hgroup class="portal-state-panel__body">
      <h3 class="portal-state-panel__title">
        {{ title }}
      </h3>
      <p
        v-if="description"
        class="portal-state-panel__description"
      >
        {{ description }}
      </p>
    </hgroup>
    <div
      v-if="$slots.actions"
      class="portal-state-panel__actions"
    >
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.portal-state-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 32px 24px;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
  background-color: #fafafa;
  text-align: center;
}

.portal-state-panel__icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #e6f4ff;
  color: #1677ff;
  font-size: 24px;
}

.portal-state-panel__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 480px;
}

.portal-state-panel__title {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
}

.portal-state-panel__description {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
}

.portal-state-panel__actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .portal-state-panel {
    padding: 24px 16px;
  }

  .portal-state-panel__actions {
    width: 100%;
  }

  .portal-state-panel__actions :deep(.ant-btn) {
    flex: 1 1 auto;
  }
}
</style>
