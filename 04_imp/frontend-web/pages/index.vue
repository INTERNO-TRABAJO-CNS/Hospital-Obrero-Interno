<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

const sesion = useSesionStore()
const primera = sesion.menu.find(o => !o.pendiente)
await navigateTo(primera?.ruta ?? '/medico/agenda', { replace: true })
</script>

<template>
  <div />
</template>
