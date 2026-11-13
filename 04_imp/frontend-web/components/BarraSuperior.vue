<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

/** Titulo de la pagina y pildora del medico («Médico General · Carlos Ramos»), como en el Figma. */
defineProps<{ titulo: string }>()
const sesion = useSesionStore()
</script>

<template>
  <header class="barra-superior">
    <h1 class="titulo-pagina">{{ titulo }}</h1>
    <span v-if="sesion.etiqueta" class="pildora">{{ sesion.etiqueta }}</span>
  </header>
</template>
