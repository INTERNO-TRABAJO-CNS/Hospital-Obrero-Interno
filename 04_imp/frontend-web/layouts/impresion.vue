<template>
  <div class="impresion">
    <slot />
  </div>
</template>

<style scoped>
.impresion { min-height: 100vh; background: #e0e6e0; }
@media print {
  .impresion { background: #fff; }
}
</style>
