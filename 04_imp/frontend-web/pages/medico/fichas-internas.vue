<script setup lang="ts">
import { DURACION_CIRUGIA, nombreEspecialidad } from '~/models/especialidad'
import { MAX_MOTIVO, type FichaInterna } from '~/models/fichaInterna'
import { fechaTitulo, horaActual, hoyISO } from '~/models/fechas'
import { mensajeDeError } from '~/services/apiClient'
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ middleware: 'tipo-medico' })

const servicios = useServicios()
const sesion = useSesionStore()
const fecha = ref(hoyISO())
const fichas = ref<FichaInterna[]>([])
const elegida = ref<FichaInterna | null>(null)
const observaciones = ref('')
const error = ref<string | null>(null)
const cargando = ref(false)

async function cargar() {
  if (!sesion.medico) return
  cargando.value = true
  try {
    fichas.value = await servicios.fichas.agendaEspecialista(sesion.medico, fecha.value)
    elegida.value = fichas.value.find(f => f.ticket === elegida.value?.ticket)
      ?? fichas.value.find(f => f.estado === 'ASIGNADA') ?? null
  } finally {
    cargando.value = false
  }
}
onMounted(cargar)
watch(fecha, cargar)

const consultorio = computed(() => fichas.value.find(f => f.consultorio)?.consultorio)
const resumen = computed(() => {
  const n = fichas.value.length
  const partes = [fechaTitulo(fecha.value), `${n} ${n === 1 ? 'ficha interna' : 'fichas internas'}`]
  if (consultorio.value) partes.push(`Consultorio ${consultorio.value}`)
  return partes.join(' · ')
})
const opera = computed(() => !!sesion.medico && !!DURACION_CIRUGIA[sesion.medico.especialidad])

/** «No se presentó» se habilita despues de la hora de la ficha. */
function yaPasoLaHora(f: FichaInterna) {
  if (!f.fecha || !f.hora) return false
  const hoy = hoyISO()
  return f.fecha < hoy || (f.fecha === hoy && horaActual() >= f.hora)
}

async function atendida(f: FichaInterna) {
  error.value = null
  try {
    await servicios.fichas.marcarAtendida(f.ticket, f.ticket === elegida.value?.ticket ? observaciones.value : '')
    observaciones.value = ''
    await cargar()
  } catch (e) {
    error.value = mensajeDeError(e)
  }
}

async function noSePresento(f: FichaInterna) {
  error.value = null
  try {
    await servicios.fichas.noSePresento(f.ticket)
    await cargar()
  } catch (e) {
    error.value = mensajeDeError(e)
  }
}
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Fichas internas de hoy" />

    <div class="fila-fecha">
      <p class="resumen">{{ resumen }}</p>
      <input v-model="fecha" class="selector-fecha" type="date" aria-label="Elegir fecha">
      <button class="boton mediano" type="button" @click="fecha = hoyISO()">Hoy</button>
    </div>

    <p v-if="error" class="alerta error" role="alert">{{ error }}</p>

    <div class="tabla-contenedor">
      <table class="tabla">
        <thead>
          <tr><th>Hora</th><th>Paciente</th><th>Tabla</th><th>Urgencia</th><th>Motivo</th><th>Estado</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          <tr v-if="!fichas.length"><td colspan="7" class="vacio">{{ cargando ? 'Cargando…' : 'No tienes fichas internas este día.' }}</td></tr>
          <tr v-for="f in fichas" :key="f.ticket" class="seleccionable"
              :class="{ destacada: elegida?.ticket === f.ticket }" @click="elegida = f">
            <td>{{ f.hora }}</td>
            <td>{{ f.nombrePaciente }}</td>
            <td><ChipEstado :valor="f.tabla" :texto="f.tabla === 'PEDIATRIA' ? 'PEDIATRÍA' : 'GENERAL'" /></td>
            <td><ChipEstado v-if="f.urgencia" valor="URGENCIA" /><template v-else>—</template></td>
            <td>{{ f.motivo }}</td>
            <td><ChipEstado :valor="f.estado" /></td>
            <td>
              <div class="acciones">
                <NuxtLink v-if="f.estado === 'ATENDIDA' && opera" :to="`/medico/cirugias/programar?ficha=${f.ticket}`"
                          class="boton chico" @click.stop>
                  Programar cirugía
                </NuxtLink>
                <template v-if="f.estado === 'ASIGNADA'">
                  <button class="boton chico" type="button" @click.stop="atendida(f)">Atendida</button>
                  <button class="boton chico gris" type="button" :disabled="!yaPasoLaHora(f)" @click.stop="noSePresento(f)">
                    No se presentó
                  </button>
                </template>
                <span v-if="f.estado === 'CANCELADA'" class="detalle">No se presentó</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="columnas">
      <div v-if="elegida" class="tarjeta principal">
        <h2 class="tarjeta-titulo">Ficha interna N° {{ elegida.ticket }} · {{ elegida.nombrePaciente }} · {{ elegida.edad }} años</h2>
        <div class="dato">
          <span>Derivado por</span><span>{{ elegida.derivadoPor }} · {{ nombreEspecialidad(elegida.especialidadOrigen) }}</span>
        </div>
        <div class="dato"><span>Motivo</span><span>{{ elegida.motivo }}</span></div>
        <template v-if="elegida.estado === 'ASIGNADA'">
          <div class="campo">
            <label for="observaciones">Observaciones (opcional)</label>
            <textarea id="observaciones" v-model="observaciones" class="entrada" :maxlength="2000"
                      placeholder="Escribe tus observaciones…" />
            <span class="ayuda">Hasta 2000 caracteres.</span>
          </div>
          <div class="botones">
            <button class="boton" type="button" @click="atendida(elegida)">Marcar atendida</button>
            <button class="boton gris" type="button" :disabled="!yaPasoLaHora(elegida)" @click="noSePresento(elegida)">
              No se presentó
            </button>
          </div>
          <p class="ayuda">«No se presentó» se habilita después de las {{ elegida.hora }}.</p>
        </template>
        <p v-else-if="elegida.observaciones" class="ayuda">Observaciones: {{ elegida.observaciones }}</p>
      </div>
      <aside class="tarjeta lateral">
        <h2 class="tarjeta-titulo">Para tener en cuenta</h2>
        <ul class="lista-puntos">
          <li>No ves la gravedad: solo la ve el médico que derivó. Sí ves si fue urgencia y el motivo.</li>
          <li>En fichas internas no hay confirmación de asistencia.</li>
          <li>«No se presentó» deja la ficha CANCELADA y ese horario no se reasigna.</li>
          <li>«Programar cirugía» aparece cuando la ficha está ATENDIDA y tu especialidad opera.</li>
        </ul>
        <p class="ayuda">Motivo de hasta {{ MAX_MOTIVO }} caracteres, escrito por el médico que derivó.</p>
      </aside>
    </div>
  </section>
</template>
