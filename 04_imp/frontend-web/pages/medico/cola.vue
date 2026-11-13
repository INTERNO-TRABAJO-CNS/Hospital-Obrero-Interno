<script setup lang="ts">
import { INTERNAS, nombreEspecialidad, type Especialidad } from '~/models/especialidad'
import { EDAD_ADULTO_MAYOR, type FichaInterna } from '~/models/fichaInterna'
import { fechaMes, hoyISO } from '~/models/fechas'
import { mismoNombre } from '~/models/texto'
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ middleware: 'tipo-medico' })

const servicios = useServicios()
const sesion = useSesionStore()
const especialidad = ref<Especialidad>('GASTROENTEROLOGIA')
const fichas = ref<FichaInterna[]>([])
const cargando = ref(false)
let reloj: ReturnType<typeof setInterval> | undefined

/** El medico general ve la tabla general; el pediatra, la pediatrica. */
const tabla = computed(() => sesion.medico?.especialidad === 'PEDIATRIA' ? 'PEDIATRIA' : 'GENERAL')

async function cargar() {
  cargando.value = true
  try {
    fichas.value = await servicios.fichas.cola(especialidad.value, tabla.value)
  } finally {
    cargando.value = false
  }
}

onMounted(() => {
  cargar()
  reloj = setInterval(cargar, 15_000)
})
onBeforeUnmount(() => clearInterval(reloj))
watch(especialidad, cargar)

/** Se resalta la ultima ficha que derivo este medico (como en el Figma). */
const ultimaMia = computed(() => fichas.value
  .filter(f => mismoNombre(f.derivadoPor, sesion.medico?.nombre))
  .sort((a, b) => b.llegada.localeCompare(a.llegada))[0]?.ticket)

function asignada(f: FichaInterna) {
  if (!f.fecha) return ''
  const dia = f.fecha === hoyISO() ? 'Hoy' : fechaMes(f.fecha, false)
  return `${dia} ${f.hora} · ${f.medico}`
}

function llegada(f: FichaInterna) {
  const d = new Date(f.llegada)
  return `${fechaMes(hoyISO(d), false)} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Cola de prioridad" />

    <div class="opciones" role="tablist" aria-label="Especialidad">
      <button v-for="e in INTERNAS" :key="e" type="button" role="tab" class="opcion blanca"
              :class="{ activa: e === especialidad }" :aria-selected="e === especialidad" @click="especialidad = e">
        {{ nombreEspecialidad(e) }}
      </button>
    </div>

    <div class="informacion">
      <span class="en-vivo"><img src="/punto-en-vivo.svg" width="8" height="8" alt="">Se actualiza cada 15 s</span>
      <ChipEstado :valor="tabla === 'PEDIATRIA' ? 'PEDIATRIA' : 'GENERAL'"
                  :texto="tabla === 'PEDIATRIA' ? 'TABLA PEDIÁTRICA' : 'TABLA GENERAL'" />
      <p class="nota">
        {{ tabla === 'PEDIATRIA'
          ? 'Ves la tabla pediátrica porque derivas desde Pediatría. El médico general ve la tabla general.'
          : 'Ves la tabla general porque derivas desde Consultorio Familiar. El pediatra ve la tabla pediátrica.' }}
      </p>
    </div>

    <div class="tabla-contenedor">
      <table class="tabla">
        <thead>
          <tr>
            <th>Pos.</th><th>Paciente</th><th>Urgencia</th><th>Gravedad</th><th>60 o más</th>
            <th>Fecha / hora asignada</th><th>Llegada</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!fichas.length"><td colspan="7" class="vacio">{{ cargando ? 'Cargando…' : 'No hay fichas en espera ni asignadas.' }}</td></tr>
          <tr v-for="(f, i) in fichas" :key="f.ticket" :class="{ destacada: f.ticket === ultimaMia }">
            <td class="posicion">{{ i + 1 }}</td>
            <td>{{ f.nombrePaciente }}</td>
            <td><ChipEstado v-if="f.urgencia" valor="URGENCIA" /><template v-else>—</template></td>
            <td><ChipEstado :valor="f.gravedad" /></td>
            <td>{{ f.edad >= EDAD_ADULTO_MAYOR ? 'Sí' : 'No' }}</td>
            <td>
              <template v-if="f.estado === 'EN_ESPERA'">
                <ChipEstado valor="EN_ESPERA" /> <span class="detalle">sin cupo en 15 días hábiles</span>
              </template>
              <template v-else>{{ asignada(f) }}</template>
            </td>
            <td>{{ llegada(f) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="nota">Solo lectura. Orden: urgencia › gravedad (ALTA › MEDIA › BAJA) › 60 años o más › orden de llegada.
      Las fichas EN ESPERA se vuelven a buscar cada día hábil a las 06:00.</p>
  </section>
</template>

<style scoped>
.opcion.blanca { background: var(--blanco); }
.opcion.blanca.activa { background: var(--amarillo); }
.informacion { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.en-vivo {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--blanco);
  border-radius: 20px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
}
.informacion .nota { flex: 1; }
.posicion { font-weight: 800; }
</style>
