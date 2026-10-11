<template>
  <section class="page-container">
    <slot />
  </section>
</template>

<style scoped>
/* Lex Elite：内容页容器 —— 外层 content-wrapper 已提供白底与边框，
   此处仅保留内边距，避免双层边框/阴影叠加。 */
.page-container {
  width: 100%;
  padding: 8px 4px;
  background: transparent;
  border: 0;
  box-shadow: none;
}

@media (max-width: 768px) {
  .page-container {
    padding: 4px 0;
  }
}
</style>
