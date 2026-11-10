<script setup lang="ts">
import { NOMBRE_ESTADO, type EstadoCita } from '~/models/agenda'
import { nombreEspecialidad } from '~/models/especialidad'
import { fechaCorta, horaCorta } from '~/models/fechas'
import { useHistorialStore } from '~/stores/historial'

const route = useRoute()
const router = useRouter()
const historial = useHistorialStore()
const pacienteId = computed(() => Number(route.params.pacienteId))

onMounted(() => historial.cargar(pacienteId.value))

function nombreEstado(e: string | null): string {
  return e ? NOMBRE_ESTADO[e as EstadoCita] ?? e : ''
}
</script>

<template>
  <section class="historial">
    <button class="boton chico volver" type="button" @click="router.back()">← Volver</button>

    <p v-if="historial.cargando" class="vacio">Cargando el historial…</p>

    <div v-else-if="historial.error" class="alerta error" role="alert">
      <strong>{{ historial.sinPermiso ? 'No puede ver este historial' : 'No se pudo cargar el historial' }}</strong>
      <p>{{ historial.error }}</p>
    </div>

    <template v-else-if="historial.historial">
      <header class="cabecera">
        <p class="suave">Historial clínico · solo lectura</p>
        <h1>{{ historial.historial.nombrePaciente || `Paciente ${historial.historial.pacienteId}` }}</h1>
        <div class="resumen">
          <span><strong>{{ historial.historial.citasPasadas.length }}</strong> citas pasadas</span>
          <span><strong>{{ historial.historial.atenciones.length }}</strong> atenciones</span>
          <span><strong>{{ historial.totalRecetas }}</strong> recetas</span>
        </div>
      </header>

      <p v-if="historial.lineaDeTiempo.length === 0" class="tarjeta vacio">El paciente todavía no tiene citas pasadas
        ni atenciones registradas.</p>

      <ol v-else class="linea">
        <li v-for="e in historial.lineaDeTiempo" :key="e.clave" class="tarjeta evento">
          <div class="cuando">
            <strong>{{ fechaCorta(e.fecha) }}</strong>
            <span class="suave">{{ horaCorta(e.hora) }}</span>
          </div>
          <div class="detalle">
            <div class="titulo">
              <strong>{{ nombreEspecialidad(e.especialidad) }}</strong>
              <span v-if="e.medico" class="suave">· {{ e.medico }}</span>
              <span v-if="e.estado" class="etiqueta" :class="e.estado">{{ nombreEstado(e.estado) }}</span>
            </div>
            <div v-if="e.recetas.length" class="recetas">
              <span class="suave rotulo">Receta</span>
              <p v-for="(r, i) in e.recetas" :key="i">{{ r }}</p>
            </div>
          </div>
        </li>
      </ol>
      <!-- PENDIENTE T100-28: fichas internas y cirugias del paciente (necesita T100-17). -->
    </template>
  </section>
</template>

<style scoped>
.historial { display: flex; flex-direction: column; gap: 18px; max-width: 900px; }
.volver { align-self: flex-start; }
.cabecera p { margin: 0 0 2px; }
.resumen { display: flex; gap: 18px; flex-wrap: wrap; margin-top: 10px; color: var(--texto-suave); }
.resumen strong { color: var(--texto); }
.linea { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.evento { display: grid; grid-template-columns: 110px 1fr; gap: 16px; padding: 16px 18px; }
.cuando { display: flex; flex-direction: column; font-variant-numeric: tabular-nums; }
.titulo { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.recetas { margin-top: 10px; padding: 10px 12px; background: var(--superficie-2); border-radius: 8px; }
.recetas p { margin: 2px 0 0; white-space: pre-wrap; }
.rotulo { font-size: .75rem; text-transform: uppercase; letter-spacing: .05em; font-weight: 600; }
.alerta p { margin: 4px 0 0; }

@media (max-width: 600px) {
  .evento { grid-template-columns: 1fr; gap: 6px; }
  .cuando { flex-direction: row; gap: 8px; }
}
</style>
