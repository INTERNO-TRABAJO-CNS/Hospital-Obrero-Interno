<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

// La primera opcion del menu de cada tipo de medico (Agenda o Agenda de fichas internas).
const sesion = useSesionStore()
await navigateTo(sesion.inicio, { replace: true })
</script>

<template>
  <div />
</template>
