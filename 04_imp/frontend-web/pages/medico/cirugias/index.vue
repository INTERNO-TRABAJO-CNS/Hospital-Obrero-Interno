<script setup lang="ts">
import type { Cirugia } from '~/models/cirugia'
import { nombreEspecialidad } from '~/models/especialidad'
import { fechaTitulo, horaActual, hoyISO, minutosDe } from '~/models/fechas'
import { mismoNombre } from '~/models/texto'
import { mensajeDeError } from '~/services/apiClient'
import { FakeCirugiasService } from '~/services/fake/fakeCirugiasService'
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ middleware: 'tipo-medico' })

const servicios = useServicios()
const sesion = useSesionStore()
const fecha = ref(hoyISO())
const cirugias = ref<Cirugia[]>([])
const error = ref<string | null>(null)
const ahora = ref(horaActual())
let reloj: ReturnType<typeof setInterval> | undefined

const ABRE = 7 * 60
const CIERRA = 19 * 60
const HORAS = Array.from({ length: 13 }, (_, i) => `${String(7 + i).padStart(2, '0')}:00`)

async function cargar() {
  if (!sesion.medico) return
  error.value = null
  try {
    cirugias.value = await servicios.cirugias.delDia(sesion.medico, fecha.value)
  } catch (e) {
    error.value = mensajeDeError(e)
  }
}
onMounted(() => {
  cargar()
  reloj = setInterval(() => { ahora.value = horaActual() }, 60_000)
})
onBeforeUnmount(() => clearInterval(reloj))
watch(fecha, cargar)

const quirofanos = computed(() => {
  const s = servicios.cirugias
  const codigos = s instanceof FakeCirugiasService && sesion.medico
    ? s.quirofanosDe(sesion.medico.especialidad).map(q => q.codigo) : []
  return [...new Set([...codigos, ...cirugias.value.map(c => c.quirofano)])].sort()
})
const mias = computed(() => cirugias.value.filter(c => mismoNombre(c.cirujano, sesion.medico?.nombre)))
const esHoy = computed(() => fecha.value === hoyISO())

/** Posicion horizontal (%) de una hora en la linea 07:00-19:00. */
function x(hora: string) {
  const m = Math.min(Math.max(minutosDe(hora), ABRE), CIERRA)
  return ((m - ABRE) / (CIERRA - ABRE)) * 100
}
function bloque(inicio: string, fin: string) {
  return { left: `${x(inicio)}%`, width: `${Math.max(x(fin) - x(inicio), .5)}%` }
}

async function marcar(c: Cirugia, estado: 'REALIZADA' | 'CANCELADA') {
  try {
    await servicios.cirugias.marcar(c.numero, estado)
    await cargar()
  } catch (e) {
    error.value = mensajeDeError(e)
  }
}
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Cirugías" />

    <div class="fila-fecha">
      <p class="resumen">{{ fechaTitulo(fecha) }} · Quirófanos de {{ nombreEspecialidad(sesion.medico?.especialidad) }}</p>
      <input v-model="fecha" class="selector-fecha" type="date" aria-label="Elegir fecha">
      <button class="boton mediano" type="button" @click="fecha = hoyISO()">Hoy</button>
    </div>

    <p v-if="error" class="alerta error" role="alert">{{ error }}</p>

    <div class="tarjeta">
      <div class="encabezado-linea">
        <h2 class="tarjeta-titulo">Ocupación del día</h2>
        <div class="leyenda">
          <span><i class="muestra cirugia" />Cirugía</span>
          <span><i class="muestra urgente" />Urgente</span>
          <span><i class="muestra limpieza" />Limpieza (30–45 min)</span>
        </div>
      </div>
      <div class="linea-tiempo" role="img" :aria-label="`Ocupación de los quirófanos el ${fechaTitulo(fecha)}`">
        <div class="eje">
          <span v-for="h in HORAS" :key="h" :style="{ left: `${x(h)}%` }">{{ h }}</span>
        </div>
        <div v-for="q in quirofanos" :key="q" class="fila-quirofano">
          <span class="nombre-quirofano">Quirófano {{ q }}</span>
          <div class="pista">
            <template v-for="c in cirugias.filter(c => c.quirofano === q && c.estado !== 'CANCELADA')" :key="c.numero">
              <div class="bloque" :class="{ urgente: c.urgente }" :style="bloque(c.inicio, c.fin)"
                   :title="`${c.nombrePaciente} ${c.inicio}–${c.fin}`">
                <strong>{{ c.nombrePaciente }}</strong>
                <span>{{ c.urgente ? 'URGENTE · ' : '' }}{{ c.inicio }} – {{ c.fin }}</span>
              </div>
              <div class="bloque limpieza" :style="bloque(c.fin, c.limpiezaHasta)" />
            </template>
          </div>
        </div>
        <div v-if="esHoy && minutosDe(ahora) >= ABRE && minutosDe(ahora) <= CIERRA" class="ahora"
             :style="{ left: `calc(110px + (100% - 110px) * ${x(ahora) / 100})` }">
          <span>ahora {{ ahora }}</span>
        </div>
      </div>
    </div>

    <h2 class="subtitulo">Mis cirugías {{ esHoy ? 'de hoy' : 'del día' }}</h2>
    <div class="tabla-contenedor">
      <table class="tabla">
        <thead><tr><th>Hora</th><th>Paciente</th><th>Quirófano</th><th>Duración</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
          <tr v-if="!mias.length"><td colspan="6" class="vacio">No tienes cirugías este día.</td></tr>
          <tr v-for="c in mias" :key="c.numero" :class="{ tachada: c.estado === 'CANCELADA' }">
            <td class="tachable">{{ c.inicio }} – {{ c.fin }}</td>
            <td class="tachable">{{ c.nombrePaciente }}</td>
            <td>{{ c.quirofano }}</td>
            <td>{{ c.duracionMin }} min</td>
            <td>
              <div class="acciones">
                <ChipEstado :valor="c.estado" />
                <ChipEstado v-if="c.urgente" valor="URGENCIA" />
              </div>
            </td>
            <td>
              <div v-if="c.estado === 'PROGRAMADA'" class="acciones">
                <button class="boton chico" type="button" @click="marcar(c, 'REALIZADA')">Realizada</button>
                <button class="boton chico gris" type="button" @click="marcar(c, 'CANCELADA')">No se presentó</button>
                <NuxtLink :to="`/medico/ticket-impreso?cirugia=${c.numero}`" class="boton chico secundario">Ticket</NuxtLink>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p class="nota">Solo ves los quirófanos de tu especialidad (no se comparten). «No se presentó» deja la cirugía
      CANCELADA y el horario no se reasigna.</p>
  </section>
</template>

<style scoped>
.subtitulo { font-size: 16px; font-weight: 800; }
.encabezado-linea { display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.leyenda { display: flex; gap: 14px; font-size: 12px; align-items: center; }
.leyenda span { display: inline-flex; align-items: center; gap: 6px; }
.muestra { width: 14px; height: 10px; border-radius: 3px; display: inline-block; }
.muestra.cirugia { background: var(--amarillo); }
.muestra.urgente { background: var(--estado-cancelada); }
.muestra.limpieza { background: #b5b5b5; }
.linea-tiempo { position: relative; padding-bottom: 22px; overflow-x: auto; }
.eje { position: relative; height: 18px; margin-left: 110px; font-size: 10px; color: var(--texto-suave); }
.eje span { position: absolute; transform: translateX(-50%); }
.fila-quirofano { display: flex; align-items: center; margin-top: 10px; }
.nombre-quirofano { width: 110px; flex-shrink: 0; font-size: 12px; font-weight: 800; }
.pista { position: relative; flex: 1; height: 44px; background: var(--fila-par); border-radius: 6px; min-width: 520px; }
.bloque {
  position: absolute;
  top: 4px;
  bottom: 4px;
  background: var(--amarillo);
  border-radius: 4px;
  padding: 3px 5px;
  font-size: 9px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  white-space: nowrap;
}
.bloque strong { font-size: 10px; }
.bloque.urgente { background: var(--estado-cancelada); }
.bloque.limpieza { background: #b5b5b5; }
.ahora { position: absolute; top: 18px; bottom: 0; width: 2px; background: #c62828; }
.ahora span { position: absolute; bottom: -2px; left: 4px; font-size: 10px; color: #c62828; font-weight: 700; white-space: nowrap; }
</style>
