<template>
  <section class="page-container">
    <slot />
  </section>
</template>

<style scoped>
.page-container {
  width: 100%;
  padding: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(232, 236, 239, 0.92);
  box-shadow: 0 2px 12px rgba(16, 42, 67, 0.04);
  backdrop-filter: blur(10px);
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
    border-radius: 12px;
  }
}
</style>
