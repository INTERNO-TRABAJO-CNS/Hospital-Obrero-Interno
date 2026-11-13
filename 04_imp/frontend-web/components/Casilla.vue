<script setup lang="ts">
/** Casilla verde del Figma (borde verde; marcada: fondo verde con un cuadrado amarillo). */
defineProps<{ modelValue: boolean; texto: string; deshabilitado?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [boolean] }>()
</script>

<template>
  <label class="casilla" :class="{ deshabilitada: deshabilitado }">
    <input type="checkbox" class="oculta" :checked="modelValue" :disabled="deshabilitado"
           @change="emit('update:modelValue', ($event.target as HTMLInputElement).checked)">
    <span class="caja" :class="{ marcada: modelValue }" aria-hidden="true"><span v-if="modelValue" class="marca" /></span>
    <span class="texto">{{ texto }}</span>
  </label>
</template>

<style scoped>
.casilla { display: flex; align-items: center; gap: 10px; cursor: pointer; font-size: 13px; font-weight: 700; }
.casilla.deshabilitada { cursor: not-allowed; opacity: .7; }
.oculta { position: absolute; opacity: 0; width: 1px; height: 1px; }
.caja {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  border: 2px solid var(--verde-menu);
  border-radius: 4px;
  background: var(--blanco);
  display: grid;
  place-items: center;
}
.caja.marcada { background: var(--verde-menu); }
.marca { width: 10px; height: 10px; border-radius: 2px; background: var(--amarillo); }
.oculta:focus-visible + .caja { outline: 2px solid var(--verde-menu); outline-offset: 2px; }
</style>
