<script setup lang="ts">
// PENDIENTE T100-21: generar la ficha interna (derivacion) desde la cita. Aqui solo se deja la ruta.
const route = useRoute()
</script>

<template>
  <section class="tarjeta pendiente">
    <h1>Ticket interno</h1>
    <p class="suave">La derivación a especialistas (ficha interna) se habilita con la tarjeta T100-21.</p>
    <p v-if="route.query.citaId" class="suave">Cita de origen: {{ route.query.citaId }}</p>
    <NuxtLink to="/medico/agenda" class="boton">Volver a la agenda</NuxtLink>
  </section>
</template>

<style scoped>
.pendiente { max-width: 560px; display: flex; flex-direction: column; gap: 10px; align-items: flex-start; }
.pendiente p { margin: 0; }
</style>
