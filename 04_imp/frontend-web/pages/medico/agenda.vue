<script setup lang="ts">
import type { FichaAgenda } from '~/models/agenda'
import { nombreEspecialidad } from '~/models/especialidad'
import { horaCorta, hoyISO } from '~/models/fechas'
import { useAgendaStore } from '~/stores/agenda'
import { useSesionStore } from '~/stores/sesion'

const agenda = useAgendaStore()
const sesion = useSesionStore()
const fecha = ref(agenda.fechaElegida ?? hoyISO())

onMounted(() => agenda.cargar())
watch(() => agenda.agenda?.fecha, (f) => { if (f) fecha.value = f })

function cambiarFecha() {
  if (fecha.value) agenda.cargar(fecha.value)
}

function claseFila(f: FichaAgenda) {
  return { destacada: f.estado === 'ASISTENCIA_CONFIRMADA', tachada: f.estado === 'CANCELADA' }
}

function derivacion(f: FichaAgenda): string | null {
  const ficha = agenda.derivaciones[f.citaId]
  return ficha ? `Derivada a ${nombreEspecialidad(ficha.especialidad)} · Ticket N° ${ficha.ticket}` : null
}
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Agenda" />

    <div class="fila-fecha">
      <p class="resumen">{{ agenda.resumen }}</p>
      <input v-model="fecha" class="selector-fecha" type="date" aria-label="Elegir fecha" @change="cambiarFecha">
      <button class="boton mediano" type="button" @click="agenda.volverAHoy()">Hoy</button>
    </div>

    <p v-if="agenda.error" class="alerta error" role="alert">{{ agenda.error }}</p>

    <div class="tabla-contenedor">
      <table class="tabla">
        <thead>
          <tr>
            <th class="col-ficha">Ficha</th>
            <th class="col-hora">Hora</th>
            <th class="col-paciente">Paciente</th>
            <th class="col-estado">Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="agenda.cargando && !agenda.agenda"><td colspan="5" class="vacio">Cargando la agenda…</td></tr>
          <tr v-else-if="agenda.agenda && agenda.agenda.cantidad === 0">
            <td colspan="5" class="vacio">No hay citas para esta fecha.</td>
          </tr>
          <tr v-for="f in agenda.agenda?.fichas ?? []" :key="f.citaId" :class="claseFila(f)">
            <td class="tachable">{{ f.numeroFicha }}</td>
            <td class="tachable">{{ horaCorta(f.hora) }}</td>
            <td class="tachable">
              <NuxtLink v-if="f.estado !== 'CANCELADA'" :to="`/medico/pacientes/${f.pacienteId}/historial`"
                        class="enlace-paciente" :title="`Ver historial de ${f.nombrePaciente}`">
                {{ f.nombrePaciente }}
              </NuxtLink>
              <template v-else>{{ f.nombrePaciente }}</template>
            </td>
            <td><ChipEstado :valor="f.estado" /></td>
            <td>
              <div class="acciones">
                <NuxtLink v-if="agenda.puedeAtender(f)" :to="`/medico/atencion/${f.citaId}`" class="boton chico">
                  Atender
                </NuxtLink>
                <NuxtLink v-if="agenda.puedeTicketInterno(f, sesion.medico)"
                          :to="`/medico/ticket-interno?citaId=${f.citaId}`" class="boton chico secundario">
                  Ticket interno
                </NuxtLink>
                <span v-if="derivacion(f)" class="detalle">{{ derivacion(f) }}</span>
                <span v-else-if="f.estado === 'CANCELADA'" class="detalle">No confirmó su asistencia</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="nota">«Atender» solo con ASISTENCIA_CONFIRMADA. «Ticket interno» solo para citas de hoy confirmadas o
      atendidas (una derivación por cita). Las canceladas se muestran marcadas y cuentan en el total.</p>
  </section>
</template>

<style scoped>
.col-ficha { width: 60px; }
.col-hora { width: 70px; }
.col-paciente { width: 200px; }
.col-estado { width: 230px; }
.enlace-paciente { color: inherit; text-decoration: none; }
.enlace-paciente:hover { text-decoration: underline; }
</style>
