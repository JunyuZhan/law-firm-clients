<template>
  <section class="page-container">
    <slot />
  </section>
</template>

<style scoped>
.page-container {
  width: 100%;
  padding: 24px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
    border-radius: 6px;
  }
}
</style>
