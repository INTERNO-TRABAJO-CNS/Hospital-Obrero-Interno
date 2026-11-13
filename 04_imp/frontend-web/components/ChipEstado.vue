<script setup lang="ts">
/** Chip de color del Figma (ATENDIDA, PROGRAMADA, URGENCIA...). {@code valor} define el color; {@code texto} lo que se lee. */
const props = defineProps<{ valor: string; texto?: string }>()
const clase = computed(() => props.valor.normalize('NFD').replace(/[̀-ͯ]/g, '').toUpperCase())
</script>

<template>
  <span class="chip" :class="clase">{{ texto ?? valor }}</span>
</template>
