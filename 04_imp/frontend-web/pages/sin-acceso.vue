<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ layout: 'vacio' })

const sesion = useSesionStore()

function volver() {
  sesion.salir()
  navigateTo('/login')
}
</script>

<template>
  <div class="tarjeta aviso">
    <h1>Sin acceso</h1>
    <p class="suave">Esta web es solo para médicos. Pacientes, enfermería y administración usan la aplicación del
      hospital.</p>
    <button class="boton primario" type="button" @click="volver">Volver al inicio de sesión</button>
  </div>
</template>

<style scoped>
.aviso { width: min(420px, 100%); display: flex; flex-direction: column; gap: 12px; }
.aviso p { margin: 0; }
</style>
