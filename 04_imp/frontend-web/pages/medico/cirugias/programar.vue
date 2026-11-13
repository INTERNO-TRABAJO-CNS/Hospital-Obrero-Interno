<script setup lang="ts">
import { RECHAZOS_CIRUGIA, type Cirugia, type PropuestaCirugia } from '~/models/cirugia'
import { DURACION_CIRUGIA, nombreEspecialidad } from '~/models/especialidad'
import type { FichaInterna, Gravedad } from '~/models/fichaInterna'
import { fechaLarga, fechaMes, hoyISO, minutosDe } from '~/models/fechas'
import { ApiError, mensajeDeError } from '~/services/apiClient'
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ middleware: 'tipo-medico' })

const route = useRoute()
const servicios = useServicios()
const sesion = useSesionStore()

const ficha = ref<FichaInterna | null>(null)
const duracion = ref<number | null>(null)
const gravedad = ref<Gravedad>('ALTA')
const urgente = ref(false)
const propuesta = ref<PropuestaCirugia | null>(null)
const rechazo = ref<{ codigo: string; mensaje: string } | null>(null)
const confirmada = ref<Cirugia | null>(null)
const trabajando = ref(false)

const rango = computed(() => sesion.medico ? DURACION_CIRUGIA[sesion.medico.especialidad] : undefined)

onMounted(async () => {
  ficha.value = await servicios.fichas.porTicket(Number(route.query.ficha))
  if (rango.value) duracion.value = Math.round((rango.value[0] + rango.value[1]) / 2 / 10) * 10
})

function datos() {
  return { fichaTicket: ficha.value!.ticket, duracionMin: Number(duracion.value), gravedad: gravedad.value,
    urgente: urgente.value }
}

async function proponer() {
  if (!ficha.value || !sesion.medico) return
  trabajando.value = true
  rechazo.value = null
  propuesta.value = null
  try {
    propuesta.value = await servicios.cirugias.proponer(datos(), sesion.medico)
  } catch (e) {
    rechazo.value = { codigo: e instanceof ApiError ? e.codigo : 'ERROR', mensaje: mensajeDeError(e) }
  } finally {
    trabajando.value = false
  }
}

async function confirmar() {
  if (!propuesta.value || !sesion.medico) return
  trabajando.value = true
  try {
    confirmada.value = await servicios.cirugias.confirmar(datos(), propuesta.value, sesion.medico)
  } catch (e) {
    rechazo.value = { codigo: e instanceof ApiError ? e.codigo : 'ERROR', mensaje: mensajeDeError(e) }
  } finally {
    trabajando.value = false
  }
}

const choque = computed(() => rechazo.value?.codigo === 'CHOQUE_DE_HORARIO')
const capitalizada = (s: string) => s.charAt(0).toUpperCase() + s.slice(1)
function inicioTexto(p: PropuestaCirugia) {
  const dia = fechaLarga(p.fecha).replace(/ de \d{4}$/, '')
  return p.fecha === hoyISO() ? `Hoy, ${dia} · ${p.inicio}` : `${capitalizada(dia)} · ${p.inicio}`
}
/** Barra de la propuesta: cirugia + limpieza proporcionales. */
const barra = computed(() => {
  const p = propuesta.value
  if (!p) return null
  const total = minutosDe(p.limpiezaHasta) - minutosDe(p.inicio)
  return { cirugia: ((minutosDe(p.fin) - minutosDe(p.inicio)) / total) * 100 }
})
</script>

<template>
  <section class="pagina">
    <BarraSuperior :titulo="urgente ? 'Programar cirugía · Urgente' : 'Programar cirugía'" />

    <p v-if="!ficha" class="tarjeta vacio">Elige una ficha ATENDIDA desde la agenda de fichas internas.</p>

    <div v-else class="columnas">
      <form class="tarjeta principal datos-cirugia" @submit.prevent="proponer">
        <h2 class="tarjeta-titulo grande">Datos de la cirugía</h2>
        <div class="dato"><span>Paciente</span><span>{{ ficha.nombrePaciente }} · {{ ficha.edad }} años</span></div>
        <div class="dato">
          <span>Ficha interna</span>
          <span>N° {{ ficha.ticket }} · {{ ficha.estado }} {{ ficha.fecha === hoyISO() ? 'hoy' : fechaMes(ficha.fecha ?? '', false) }} {{ ficha.hora }}</span>
        </div>
        <div class="dato"><span>Cirujano</span><span>{{ sesion.medico?.nombre }} (tú)</span></div>
        <div class="campo">
          <label for="duracion">Duración estimada (minutos)</label>
          <input id="duracion" v-model.number="duracion" class="entrada resaltada" type="number" min="1" :disabled="!!confirmada">
          <span v-if="rango" class="ayuda">{{ nombreEspecialidad(sesion.medico?.especialidad) }}: de {{ rango[0] }} a
            {{ rango[1] }} min, según qué tan problemático es el caso.</span>
        </div>
        <div class="campo">
          <span class="rotulo">Gravedad</span>
          <SelectorGravedad v-model="gravedad" :deshabilitado="!!confirmada" />
        </div>
        <Casilla v-model="urgente" texto="Urgente (se busca desde ahora, también fuera de horario)" :deshabilitado="!!confirmada" />
        <template v-if="!choque && !confirmada">
          <button class="boton ancho" type="submit" :disabled="trabajando || !duracion">Proponer horario</button>
          <p class="ayuda">Proponer no guarda nada: solo calcula el primer hueco sin choques.</p>
        </template>
      </form>

      <!-- Cirugia confirmada -->
      <div v-if="confirmada" class="tarjeta lateral">
        <div class="fila-titulo"><h2 class="tarjeta-titulo grande">Cirugía programada</h2><ChipEstado valor="PROGRAMADA" /></div>
        <p class="destacado">Cirugía N° {{ confirmada.numero }} · Quirófano {{ confirmada.quirofano }}</p>
        <div class="dato"><span>Inicio</span><span>{{ capitalizada(fechaLarga(confirmada.fecha)) }} · {{ confirmada.inicio }}</span></div>
        <div class="dato"><span>Fin</span><span>{{ confirmada.fin }}</span></div>
        <div class="dato"><span>Limpieza del quirófano</span><span>hasta {{ confirmada.limpiezaHasta }}</span></div>
        <div class="botones">
          <NuxtLink :to="`/medico/ticket-impreso?cirugia=${confirmada.numero}`" class="boton">Imprimir ticket</NuxtLink>
          <NuxtLink to="/medico/cirugias" class="boton secundario">Ver cirugías del día</NuxtLink>
        </div>
        <p class="ayuda">El paciente recibe «Cirugía programada» en la app.</p>
      </div>

      <!-- Rechazo (incluido el choque de horario) -->
      <div v-else-if="rechazo" class="tarjeta lateral">
        <div class="rechazo" role="alert">
          <strong>{{ rechazo.codigo }}</strong>
          <span>{{ rechazo.mensaje }}</span>
        </div>
        <template v-if="choque && propuesta">
          <h2 class="tarjeta-titulo anterior">Propuesta anterior (ya no disponible)</h2>
          <div class="dato"><span>Quirófano</span><span>{{ propuesta.quirofano }} · {{ nombreEspecialidad(propuesta.especialidad) }}</span></div>
          <div class="dato"><span>Inicio</span><span>{{ inicioTexto(propuesta) }}</span></div>
          <div class="dato"><span>Fin y limpieza</span><span>{{ propuesta.fin }} · limpieza hasta {{ propuesta.limpiezaHasta }}</span></div>
        </template>
        <div class="botones">
          <button class="boton gris" type="button" disabled>Confirmar</button>
          <button class="boton" type="button" :disabled="trabajando" @click="proponer">Proponer de nuevo</button>
        </div>
        <h3 class="tarjeta-titulo chico">Otros rechazos posibles</h3>
        <div v-for="r in RECHAZOS_CIRUGIA.filter(x => x.codigo !== rechazo!.codigo)" :key="r.codigo" class="item-lista">
          <strong>{{ r.codigo }}</strong><span>{{ r.descripcion }}</span>
        </div>
      </div>

      <!-- Propuesta -->
      <div v-else-if="propuesta" class="tarjeta lateral">
        <div class="fila-titulo">
          <h2 class="tarjeta-titulo grande">Propuesta del sistema</h2>
          <ChipEstado :valor="propuesta.desplaza ? 'MEDIA' : 'GENERAL'" :texto="propuesta.desplaza ? 'DESPLAZA 1' : 'NO DESPLAZA'" />
        </div>
        <p class="destacado">Quirófano {{ propuesta.quirofano }} · {{ nombreEspecialidad(propuesta.especialidad) }}</p>
        <div class="dato"><span>Inicio</span><span>{{ inicioTexto(propuesta) }}</span></div>
        <div class="dato"><span>Fin</span><span>{{ propuesta.fin }}</span></div>
        <div class="dato"><span>Limpieza del quirófano</span><span>hasta {{ propuesta.limpiezaHasta }} ({{ propuesta.limpiezaMin }} min)</span></div>
        <div class="dato"><span>Choques</span><span>Ninguno: quirófano, cirujano y paciente libres</span></div>
        <div v-if="barra" class="mini-linea">
          <div class="tramo" :class="urgente ? 'urgente' : 'normal'" :style="{ width: `${barra.cirugia}%` }" />
          <div class="tramo limpieza" :style="{ width: `${100 - barra.cirugia}%` }" />
          <span class="etiqueta-tramo">{{ propuesta.inicio }} – {{ propuesta.fin }} {{ urgente ? 'urgente' : 'cirugía' }}</span>
          <span class="etiqueta-tramo derecha">limpieza hasta {{ propuesta.limpiezaHasta }}</span>
        </div>
        <div v-if="propuesta.desplaza" class="desplaza">
          <strong>Esta cirugía desplaza a otra</strong>
          <span>Cirugía N° {{ propuesta.desplaza.numero }} · {{ propuesta.desplaza.nombrePaciente }} ·
            {{ propuesta.desplaza.inicio }} – {{ propuesta.desplaza.fin }} · no urgente y todavía no empieza. Al confirmar
            se reprograma al siguiente hueco libre ({{ fechaLarga(propuesta.desplaza.nuevaFecha).replace(/ de \d{4}$/, '') }}
            · {{ propuesta.desplaza.nuevoInicio }}) y se le avisa «Cirugía reprogramada».</span>
        </div>
        <div class="botones">
          <button class="boton" type="button" :disabled="trabajando" @click="confirmar">Confirmar</button>
          <button class="boton secundario" type="button" :disabled="trabajando" @click="proponer">Proponer de nuevo</button>
        </div>
        <p class="ayuda">Se guarda solo al confirmar. El paciente recibe «Cirugía programada» en la app y se puede
          imprimir el ticket.</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.datos-cirugia { max-width: 520px; }
.resaltada { background: var(--blanco); border: 1px solid var(--verde-menu); }
.fila-titulo { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.destacado { font-size: 22px; font-weight: 800; color: var(--verde-menu); }
.anterior { color: var(--texto-suave); }
.chico { font-size: 14px; }
.mini-linea {
  position: relative;
  display: flex;
  height: 54px;
  padding: 10px 12px 22px;
  background: var(--fila-par);
  border-radius: 8px;
}
.tramo { height: 22px; border-radius: 4px; }
.tramo.normal { background: var(--amarillo); }
.tramo.urgente { background: var(--estado-cancelada); }
.tramo.limpieza { background: #b5b5b5; }
.etiqueta-tramo { position: absolute; bottom: 5px; left: 12px; font-size: 10px; font-weight: 700; }
.etiqueta-tramo.derecha { left: auto; right: 12px; font-weight: 400; color: var(--texto-suave); }
.desplaza {
  background: #fdf0d8;
  border: 1px solid #f5c26b;
  border-radius: 10px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}
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
</style>
