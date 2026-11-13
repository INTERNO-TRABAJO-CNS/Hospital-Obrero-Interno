<template>
  <div class="centro">
    <slot />
  </div>
</template>

<style scoped>
.centro { min-height: 100vh; display: grid; place-items: center; padding: 24px 16px; background: var(--verde-fondo); }
</style>
