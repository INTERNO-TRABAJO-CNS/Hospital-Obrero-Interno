<script setup lang="ts">
import { horaCorta } from '~/models/fechas'
import { useAgendaStore } from '~/stores/agenda'

// «Historial» del menu: buscar al paciente (solo los de su agenda) o elegirlo de la agenda de hoy.
const agenda = useAgendaStore()
onMounted(() => { if (!agenda.agenda) agenda.cargar() })
const pacientes = computed(() => agenda.agenda?.fichas.filter(f => f.estado !== 'CANCELADA') ?? [])
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Historial del paciente" />
    <div class="tarjeta fila-paciente">
      <div class="datos">
        <p class="nombre">Elige un paciente</p>
        <p class="ayuda">Solo ves pacientes de tu agenda.</p>
      </div>
      <BuscadorPaciente />
    </div>
    <div class="tarjeta">
      <h2 class="tarjeta-titulo">Pacientes de la agenda {{ agenda.esHoy ? 'de hoy' : 'elegida' }}</h2>
      <p v-if="!pacientes.length" class="suave">No hay pacientes en la agenda.</p>
      <NuxtLink v-for="f in pacientes" :key="f.citaId" :to="`/medico/pacientes/${f.pacienteId}/historial`"
                class="item-lista enlace">
        <strong>{{ f.nombrePaciente }}</strong>
        <span>Ficha {{ f.numeroFicha }} · {{ horaCorta(f.hora) }}</span>
      </NuxtLink>
    </div>
  </section>
</template>

<style scoped>
.fila-paciente { flex-direction: row; align-items: center; justify-content: space-between; flex-wrap: wrap; padding: 16px 20px; }
.nombre { font-size: 20px; font-weight: 800; }
.enlace { text-decoration: none; }
.enlace:hover { outline: 2px solid var(--verde-menu); }
</style>
