<script setup lang="ts">
import type { Gravedad } from '~/models/fichaInterna'

/** ALTA / MEDIA / BAJA en tres botones (Figma). */
defineProps<{ modelValue: Gravedad; deshabilitado?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [Gravedad] }>()
const OPCIONES: Gravedad[] = ['ALTA', 'MEDIA', 'BAJA']
</script>

<template>
  <div class="gravedad" role="radiogroup" aria-label="Gravedad">
    <button v-for="g in OPCIONES" :key="g" type="button" role="radio" :aria-checked="g === modelValue"
            class="nivel" :class="{ activo: g === modelValue }" :disabled="deshabilitado"
            @click="emit('update:modelValue', g)">
      {{ g }}
    </button>
  </div>
</template>

<style scoped>
.gravedad { display: flex; gap: 8px; }
.nivel {
  flex: 1;
  background: var(--blanco);
  border: 1px solid #bbb;
  border-radius: 8px;
  padding: 9px 12px;
  font-size: 13px;
  cursor: pointer;
}
.nivel.activo { background: var(--amarillo); border-color: var(--verde-menu); font-weight: 800; }
.nivel:disabled { cursor: not-allowed; }
</style>
