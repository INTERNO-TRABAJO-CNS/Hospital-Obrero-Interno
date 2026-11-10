<script setup lang="ts">
import { NOMBRE_ESTADO } from '~/models/agenda'
import { fechaLarga, horaCorta, hoyISO } from '~/models/fechas'
import { useAgendaStore } from '~/stores/agenda'
import { useSesionStore } from '~/stores/sesion'

const agenda = useAgendaStore()
const sesion = useSesionStore()
const fechaElegida = ref(hoyISO())
const mostrarSelector = ref(false)

onMounted(() => agenda.cargar())

function verFecha() {
  agenda.cargar(fechaElegida.value)
}

function volverAHoy() {
  fechaElegida.value = hoyISO()
  mostrarSelector.value = false
  agenda.cargar()
}
</script>

<template>
  <section class="agenda">
    <header class="cabecera">
      <div>
        <p class="suave">{{ agenda.agenda ? fechaLarga(agenda.agenda.fecha) : 'Cargando…' }}</p>
        <h1>{{ agenda.titulo }}</h1>
      </div>
      <div class="acciones">
        <button v-if="!agenda.esHoy && agenda.agenda" class="boton" type="button" @click="volverAHoy">Hoy</button>
        <button class="boton" type="button" @click="mostrarSelector = !mostrarSelector">Otras fechas</button>
      </div>
    </header>

    <form v-if="mostrarSelector" class="tarjeta selector" @submit.prevent="verFecha">
      <div class="campo">
        <label for="fecha">Fecha</label>
        <input id="fecha" v-model="fechaElegida" type="date" required>
      </div>
      <button class="boton primario" type="submit">Ver citas</button>
    </form>

    <p v-if="agenda.error" class="alerta error" role="alert">{{ agenda.error }}</p>

    <div class="tarjeta tabla-contenedor">
      <p v-if="agenda.cargando" class="vacio">Cargando la agenda…</p>
      <p v-else-if="agenda.agenda && agenda.agenda.cantidad === 0" class="vacio">No hay citas programadas para esta
        fecha.</p>
      <table v-else-if="agenda.agenda" class="tabla">
        <thead>
          <tr>
            <th>Ficha</th>
            <th>Hora</th>
            <th>Paciente</th>
            <th>Estado</th>
            <th class="derecha">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="f in agenda.agenda.fichas" :key="f.citaId" :class="{ cancelada: f.estado === 'CANCELADA' }">
            <td class="ficha">{{ f.numeroFicha }}</td>
            <td>{{ horaCorta(f.hora) }}</td>
            <td class="paciente">{{ f.nombrePaciente }}</td>
            <td><span class="etiqueta" :class="f.estado">{{ NOMBRE_ESTADO[f.estado] }}</span></td>
            <td class="derecha botones">
              <NuxtLink v-if="agenda.puedeAtender(f)" :to="`/medico/atencion/${f.citaId}`" class="boton primario chico">
                Atender
              </NuxtLink>
              <NuxtLink v-if="f.estado !== 'CANCELADA'" :to="`/medico/pacientes/${f.pacienteId}/historial`"
                        class="boton chico">
                Historial
              </NuxtLink>
              <NuxtLink v-if="agenda.puedeTicketInterno(f, sesion.medico)"
                        :to="`/medico/ticket-interno?citaId=${f.citaId}`" class="boton chico">
                Ticket interno
              </NuxtLink>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.agenda { display: flex; flex-direction: column; gap: 18px; max-width: 1040px; }
.cabecera { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; flex-wrap: wrap; }
.cabecera p { margin: 0 0 2px; text-transform: capitalize; }
.acciones { display: flex; gap: 8px; }
.selector { display: flex; align-items: flex-end; gap: 12px; flex-wrap: wrap; padding: 16px; }
.tabla-contenedor { padding: 0; overflow-x: auto; }
.tabla { width: 100%; border-collapse: collapse; }
.tabla th, .tabla td { padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--borde); }
.tabla th { font-size: .8rem; text-transform: uppercase; letter-spacing: .04em; color: var(--texto-suave); }
.tabla tbody tr:last-child td { border-bottom: 0; }
.ficha { font-weight: 700; font-variant-numeric: tabular-nums; }
.paciente { font-weight: 500; }
.cancelada .paciente, .cancelada td:nth-child(2) { text-decoration: line-through; color: var(--texto-suave); }
.derecha { text-align: right; }
.botones { display: flex; gap: 8px; justify-content: flex-end; }
.nota { margin: 0; font-size: .85rem; }
</style>
