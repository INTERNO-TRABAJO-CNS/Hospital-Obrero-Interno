<script setup lang="ts">
import { horaCorta } from '~/models/fechas'
import { useAgendaStore } from '~/stores/agenda'

// «Atención» del menu: abre al primer paciente de hoy con asistencia confirmada; si no hay, lo explica.
const agenda = useAgendaStore()

onMounted(async () => {
  await agenda.volverAHoy()
  const siguiente = agenda.esHoy ? agenda.agenda?.fichas.find(f => agenda.puedeAtender(f)) : undefined
  if (siguiente) await navigateTo(`/medico/atencion/${siguiente.citaId}`, { replace: true })
})

const confirmadas = computed(() => agenda.agenda?.fichas.filter(f => agenda.puedeAtender(f)) ?? [])
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Atención" />
    <div class="tarjeta aviso">
      <p v-if="agenda.cargando">Buscando al siguiente paciente…</p>
      <template v-else-if="!confirmadas.length">
        <h2 class="tarjeta-titulo">No hay pacientes con la asistencia confirmada</h2>
        <p class="suave">«Atender» se habilita cuando enfermería confirma la asistencia. Revisa la agenda del día.</p>
        <NuxtLink to="/medico/agenda" class="boton">Ir a la agenda</NuxtLink>
      </template>
      <template v-else>
        <h2 class="tarjeta-titulo">Pacientes listos para atender</h2>
        <NuxtLink v-for="f in confirmadas" :key="f.citaId" :to="`/medico/atencion/${f.citaId}`" class="boton secundario">
          Ficha {{ f.numeroFicha }} · {{ horaCorta(f.hora) }} · {{ f.nombrePaciente }}
        </NuxtLink>
      </template>
    </div>
  </section>
</template>

<style scoped>
.aviso { max-width: 560px; align-items: flex-start; }
</style>
