<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ layout: 'vacio' })

const sesion = useSesionStore()

async function volver() {
  await sesion.salir()
  await navigateTo('/login')
}
</script>

<template>
  <div class="sin-acceso">
    <img class="logo" src="/logo-cns.jpg" alt="Logo de la CNS">
    <div class="tarjeta aviso">
      <h1>Sin acceso a la web del médico</h1>
      <p class="suave">Esta web es solo para médicos (general, pediatría, odontología y especialistas). Pacientes,
        enfermería y administración usan la aplicación del hospital.</p>
      <button class="boton ancho" type="button" @click="volver">Volver a iniciar sesión</button>
    </div>
  </div>
</template>

<style scoped>
.sin-acceso { display: flex; flex-direction: column; align-items: center; gap: 20px; width: min(460px, 100%); }
.logo { width: 96px; height: 96px; border-radius: 20px; object-fit: cover; background: var(--blanco); }
.aviso { width: 100%; padding: 24px 28px; }
h1 { font-size: 22px; font-weight: 800; }
</style>
