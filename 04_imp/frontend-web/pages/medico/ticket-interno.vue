<script setup lang="ts">
import type { FichaAgenda } from '~/models/agenda'
import { DURACION_INTERNA, INTERNAS, nombreEspecialidad, type Especialidad } from '~/models/especialidad'
import {
  EDAD_ADULTO_MAYOR, MAX_MOTIVO, RECHAZOS_FICHA,
  type CitaOrigen, type Gravedad, type PacienteBuscado, type ResultadoFicha,
} from '~/models/fichaInterna'
import { fechaLarga, horaCorta } from '~/models/fechas'
import { mensajeDeError } from '~/services/apiClient'
import { useAgendaStore } from '~/stores/agenda'
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ middleware: 'tipo-medico' })

const route = useRoute()
const servicios = useServicios()
const sesion = useSesionStore()
const agenda = useAgendaStore()

const urgencia = ref(false)
const citaId = ref<number | null>(route.query.citaId ? Number(route.query.citaId) : null)
const paciente = ref<PacienteBuscado | null>(null)
const busqueda = ref('')
const errorBusqueda = ref<string | null>(null)
const especialidad = ref<Especialidad | null>(null)
const gravedad = ref<Gravedad>('MEDIA')
const motivo = ref('')
const enviando = ref(false)
const resultado = ref<ResultadoFicha | null>(null)
const error = ref<string | null>(null)

onMounted(async () => {
  if (!agenda.esHoy || !agenda.agenda) await agenda.volverAHoy()
  if (citaId.value) await elegirCita(citaId.value)
})

/** Citas de hoy desde las que se puede derivar (confirmadas o atendidas). */
const citasDerivables = computed(() => (agenda.agenda?.fichas ?? [])
  .filter(f => f.estado === 'ASISTENCIA_CONFIRMADA' || f.estado === 'ATENDIDA'))
const cita = computed<FichaAgenda | undefined>(() => agenda.agenda?.fichas.find(f => f.citaId === citaId.value))

async function elegirCita(id: number) {
  citaId.value = id
  paciente.value = null
  const f = agenda.agenda?.fichas.find(x => x.citaId === id)
  if (!f) return
  try {
    const h = await servicios.historial.historial(f.pacienteId)
    paciente.value = { pacienteId: f.pacienteId, nombre: f.nombrePaciente, numeroAfiliado: h.numeroAfiliado ?? '',
      edad: h.edad ?? 0 }
  } catch {
    paciente.value = { pacienteId: f.pacienteId, nombre: f.nombrePaciente, numeroAfiliado: '', edad: 0 }
  }
}

watch(urgencia, (u) => {
  resultado.value = null
  if (u) {
    paciente.value = null
  } else {
    if (especialidad.value === 'TERAPIA_INTENSIVA') especialidad.value = null
    if (citaId.value) elegirCita(citaId.value)
  }
})

async function buscar() {
  errorBusqueda.value = null
  paciente.value = await servicios.fichas.buscarPaciente(busqueda.value)
  if (!paciente.value) errorBusqueda.value = 'No se encontró un paciente con ese N° de afiliado.'
}

const datoCita = computed(() => {
  if (urgencia.value) return 'Sin cita (urgencia)'
  if (!cita.value) return ''
  return `Ficha N° ${cita.value.numeroFicha} · hoy ${horaCorta(cita.value.hora)} · ${nombreEspecialidad(sesion.medico?.especialidad)}`
})
const datoPaciente = computed(() => paciente.value
  ? [paciente.value.numeroAfiliado, paciente.value.nombre, `${paciente.value.edad} años`].filter(Boolean).join(' · ')
  : '')
const listo = computed(() => !!paciente.value && !!especialidad.value && motivo.value.trim().length > 0
  && motivo.value.trim().length <= MAX_MOTIVO && (urgencia.value || !!cita.value))

async function generar() {
  if (!listo.value || !sesion.medico || !paciente.value || !especialidad.value) return
  enviando.value = true
  error.value = null
  const origen: CitaOrigen | null = !urgencia.value && cita.value
    ? { citaId: cita.value.citaId, numeroFicha: cita.value.numeroFicha, fecha: agenda.agenda!.fecha,
        hora: cita.value.hora, estado: cita.value.estado }
    : null
  try {
    resultado.value = await servicios.fichas.generar({
      citaOrigenId: origen?.citaId ?? null, pacienteId: paciente.value.pacienteId, nombrePaciente: paciente.value.nombre,
      numeroAfiliado: paciente.value.numeroAfiliado, edad: paciente.value.edad, especialidad: especialidad.value,
      urgencia: urgencia.value, gravedad: gravedad.value, motivo: motivo.value,
    }, sesion.medico, origen)
    if (resultado.value.resultado !== 'RECHAZADA') await agenda.cargar()
  } catch (e) {
    error.value = mensajeDeError(e)
  } finally {
    enviando.value = false
  }
}

const rechazo = computed(() => resultado.value?.resultado === 'RECHAZADA' ? resultado.value : null)
const ficha = computed(() => resultado.value && resultado.value.resultado !== 'RECHAZADA' ? resultado.value.ficha : null)
const titulo = computed(() => urgencia.value && !ficha.value ? 'Ticket interno · Urgencia' : 'Ticket interno')
const capitalizada = (s: string) => s.charAt(0).toUpperCase() + s.slice(1)
</script>

<template>
  <section class="pagina">
    <BarraSuperior :titulo="titulo" />

    <!-- Resultado: ASIGNADA o EN_ESPERA -->
    <div v-if="ficha" class="columnas">
      <div class="tarjeta principal resultado">
        <div class="fila-titulo">
          <ChipEstado :valor="ficha.estado" />
          <h2>{{ ficha.estado === 'ASIGNADA' ? 'Ficha interna generada' : 'Ficha interna en espera' }}</h2>
        </div>
        <p class="numero-ticket">Ticket N° {{ ficha.ticket }}</p>
        <div class="dato"><span>Paciente</span><span>{{ ficha.nombrePaciente }} · {{ ficha.numeroAfiliado }}</span></div>
        <div class="dato"><span>Especialidad</span><span>{{ nombreEspecialidad(ficha.especialidad) }}</span></div>
        <div class="dato"><span>Médico</span><span>{{ ficha.medico ?? 'Por asignar' }}</span></div>
        <template v-if="ficha.estado === 'ASIGNADA'">
          <div class="dato"><span>Consultorio</span><span>{{ ficha.consultorio }}</span></div>
          <div class="dato"><span>Fecha</span><span>{{ capitalizada(fechaLarga(ficha.fecha!)) }}</span></div>
          <div class="dato"><span>Hora</span><span>{{ ficha.hora }}</span></div>
          <div class="dato"><span>Duración aproximada</span><span>{{ ficha.duracionMin }} min</span></div>
        </template>
        <template v-else>
          <div class="dato"><span>Fecha y hora</span><span>Sin fecha todavía</span></div>
          <p class="ayuda">No hay cupo en los próximos 15 días hábiles y todas las fichas asignadas tienen mayor
            prioridad.</p>
        </template>
        <div class="botones">
          <NuxtLink :to="`/medico/ticket-impreso?ficha=${ficha.ticket}`" class="boton">
            {{ ficha.estado === 'ASIGNADA' ? 'Imprimir ticket' : 'Imprimir constancia' }}
          </NuxtLink>
          <NuxtLink to="/medico/agenda" class="boton secundario">Volver a la agenda</NuxtLink>
        </div>
      </div>
      <aside class="tarjeta lateral">
        <h2 class="tarjeta-titulo">Qué pasa ahora</h2>
        <ul v-if="ficha.estado === 'ASIGNADA'" class="lista-puntos">
          <li>El paciente recibe el aviso «Ficha interna asignada» en la app.</li>
          <li>Imprime el ticket por si el paciente no tiene internet. La gravedad no se imprime.</li>
          <li>No hay confirmación de asistencia: si no se presenta, el especialista la marca como «No se presentó» y
            ese horario no se reasigna.</li>
          <li>Si un caso más grave necesita este horario, el sistema lo mueve al siguiente hueco libre, avisa al
            paciente y se reimprime el ticket.</li>
        </ul>
        <ul v-else class="lista-puntos">
          <li>Cada día hábil a las 06:00 el sistema vuelve a buscar un hueco, en orden de prioridad y sin desplazar a
            nadie.</li>
          <li>Cuando lo encuentre, el paciente recibe el aviso «Ficha interna asignada» y se puede reimprimir el
            ticket.</li>
          <li>La ficha aparece como EN ESPERA en la cola de prioridad.</li>
        </ul>
      </aside>
    </div>

    <!-- Formulario (y rechazo) -->
    <div v-else class="columnas">
      <form class="tarjeta principal" @submit.prevent="generar">
        <div v-if="rechazo" class="rechazo" role="alert">
          <strong>No se pudo generar la ficha · {{ rechazo.codigo }}</strong>
          <span>{{ rechazo.mensaje }}</span>
        </div>
        <h2 v-else class="tarjeta-titulo grande">Nueva ficha interna (derivación)</h2>

        <div v-if="urgencia || cita" class="dato"><span>Cita de origen</span><span>{{ datoCita }}</span></div>
        <div v-else class="campo">
          <label for="cita">Cita de origen</label>
          <select id="cita" class="entrada" :value="citaId ?? ''" @change="elegirCita(Number(($event.target as HTMLSelectElement).value))">
            <option value="" disabled>Elegir una cita de hoy confirmada o atendida</option>
            <option v-for="f in citasDerivables" :key="f.citaId" :value="f.citaId">
              Ficha {{ f.numeroFicha }} · {{ horaCorta(f.hora) }} · {{ f.nombrePaciente }}
            </option>
          </select>
          <span v-if="!citasDerivables.length" class="ayuda">Hoy no hay citas confirmadas ni atendidas.</span>
        </div>

        <div v-if="!urgencia" class="campo">
          <span class="rotulo">Paciente</span>
          <div class="entrada solo-lectura">{{ datoPaciente || '—' }}</div>
          <span class="ayuda">Se carga desde la cita (solo lectura).</span>
        </div>

        <Casilla v-model="urgencia" texto="Urgencia (sin cita: se busca al paciente y se asigna hoy)"
                 :deshabilitado="!!rechazo" />

        <template v-if="urgencia">
          <div class="busqueda">
            <div class="campo crece">
              <label for="afiliado">Buscar paciente por N° de afiliado</label>
              <input id="afiliado" v-model="busqueda" class="entrada resaltada" :disabled="!!rechazo"
                     @keydown.enter.prevent="buscar">
            </div>
            <button class="boton" type="button" :disabled="!busqueda.trim() || !!rechazo" @click="buscar">Buscar</button>
          </div>
          <p v-if="paciente" class="encontrado">
            {{ paciente.nombre }} · {{ paciente.edad }} años{{ paciente.edad >= EDAD_ADULTO_MAYOR ? ' · adulto mayor' : '' }}
          </p>
          <p v-if="errorBusqueda" class="alerta error">{{ errorBusqueda }}</p>
        </template>

        <div class="campo">
          <label for="especialidad">Especialidad interna</label>
          <SelectorEspecialidad id="especialidad" v-model="especialidad" :urgencia="urgencia" :deshabilitado="!!rechazo" />
          <span v-if="!urgencia" class="ayuda">Terapia Intensiva solo se habilita con Urgencia.</span>
        </div>

        <div class="campo">
          <span class="rotulo">Gravedad</span>
          <SelectorGravedad v-model="gravedad" :deshabilitado="!!rechazo" />
        </div>

        <div class="campo">
          <label for="motivo">Motivo</label>
          <textarea id="motivo" v-model="motivo" class="entrada motivo" :maxlength="MAX_MOTIVO" :disabled="!!rechazo" />
          <span class="ayuda">{{ motivo.length }} / {{ MAX_MOTIVO }} caracteres</span>
        </div>

        <p v-if="error" class="alerta error" role="alert">{{ error }}</p>

        <NuxtLink v-if="rechazo" to="/medico/agenda" class="boton secundario ancho">Volver a la agenda</NuxtLink>
        <button v-else class="boton ancho" type="submit" :disabled="!listo || enviando">
          {{ enviando ? 'Generando…' : 'Generar' }}
        </button>
      </form>

      <aside v-if="rechazo" class="tarjeta lateral">
        <h2 class="tarjeta-titulo">Otros rechazos posibles</h2>
        <div v-for="r in RECHAZOS_FICHA.filter(x => x.codigo !== rechazo!.codigo)" :key="r.codigo" class="item-lista">
          <strong>{{ r.codigo }}</strong>
          <span>{{ r.descripcion }}</span>
        </div>
      </aside>
      <aside v-else class="tarjeta lateral reglas">
        <h2 class="tarjeta-titulo">Cómo se asigna</h2>
        <p class="regla">1. Urgencia</p>
        <p class="regla">2. Gravedad (ALTA &gt; MEDIA &gt; BAJA)</p>
        <p class="regla">3. Adulto mayor (60 años o más)</p>
        <p class="regla">4. Orden de llegada</p>
        <p v-if="urgencia" class="nota-gris">Con urgencia la ficha se asigna HOY desde ahora. Si no hay hueco, toma el
          lugar de la ficha de hoy de menor prioridad que todavía no empezó (al desplazado se le notifica y se le
          reimprime el ticket). Terapia Intensiva se asigna ya, al médico de TI que se libere antes.</p>
        <p v-else class="nota-gris">Sin urgencia se busca desde mañana, hasta 15 días hábiles, con el médico de la
          especialidad que tenga menos fichas. Si no hay hueco, la ficha queda EN ESPERA.</p>
        <p class="nota-roja">La gravedad solo la ves tú (médico general o pediatra). No sale en el ticket ni en la
          notificación.</p>
        <p class="regla">Especialidades internas · duración de la ficha</p>
        <div v-for="e in INTERNAS" :key="e" class="fila-duracion">
          <span>{{ nombreEspecialidad(e) }}</span>
          <span class="suave">{{ DURACION_INTERNA[e]![0] }}–{{ DURACION_INTERNA[e]![1] }} min{{ e === 'TERAPIA_INTENSIVA' ? ' · solo urgencia' : '' }}</span>
        </div>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.principal { max-width: 640px; gap: 14px; }
.solo-lectura { background: #efefef; }
.resaltada { background: var(--blanco); border: 1px solid var(--verde-menu); }
.motivo { min-height: 90px; }
.busqueda { display: flex; gap: 10px; align-items: flex-end; }
.crece { flex: 1; }
.encontrado { color: var(--verde-menu); font-weight: 700; font-size: 13px; }
.reglas { gap: 8px; }
.regla { font-size: 13px; font-weight: 700; }
.nota-gris { font-size: 12px; color: var(--texto-suave); }
.nota-roja { font-size: 12px; color: var(--peligro); }
.fila-duracion { display: flex; justify-content: space-between; font-size: 12px; }
.rechazo {
  background: #fde3e1;
  border: 1px solid var(--peligro);
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}
.rechazo strong { color: var(--peligro); font-size: 13px; }
.resultado { padding: 24px 28px; }
.fila-titulo { display: flex; align-items: center; gap: 10px; }
.fila-titulo h2 { font-size: 20px; font-weight: 800; }
.numero-ticket { font-size: 32px; font-weight: 800; color: var(--verde-menu); }
</style>
