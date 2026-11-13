<script setup lang="ts">
import { MAX_RECETA } from '~/models/atencion'
import { nombreEspecialidad } from '~/models/especialidad'
import { fechaDiaMes, fechaMes, horaCorta } from '~/models/fechas'
import { useAtencionStore } from '~/stores/atencion'
import { useHistorialStore } from '~/stores/historial'

const route = useRoute()
const atencion = useAtencionStore()
const historial = useHistorialStore()
const citaId = computed(() => Number(route.params.citaId))

onMounted(async () => {
  await atencion.cargar(citaId.value)
  if (atencion.datos) await historial.cargar(atencion.datos.pacienteId)
})

const d = computed(() => atencion.datos)
const afiliado = computed(() => {
  if (!d.value) return ''
  const tipo = d.value.titular === true ? ' · Titular' : d.value.titular === false ? ' · Beneficiario' : ''
  return `${d.value.numeroAfiliado}${tipo}`
})
const paciente = computed(() => {
  if (!d.value) return ''
  return d.value.edad != null ? `${d.value.nombrePaciente} · ${d.value.edad} años` : d.value.nombrePaciente
})
/** Las 3 atenciones o cirugias mas recientes, sin contar la de hoy. */
const recientes = computed(() => historial.lineaDeTiempo
  .filter(e => (e.tipo === 'ATENCION' && e.recetas.length) || e.tipo === 'CIRUGIA')
  .slice(0, 3))
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Atención" />

    <p v-if="atencion.cargando" class="tarjeta vacio">Cargando la cita…</p>
    <div v-else-if="!d && atencion.error" class="tarjeta">
      <p class="alerta error" role="alert">{{ atencion.error }}</p>
      <NuxtLink to="/medico/agenda" class="boton secundario">Volver a la agenda</NuxtLink>
    </div>

    <div v-else-if="d" class="columnas">
      <form class="tarjeta principal" @submit.prevent="atencion.finalizar()">
        <h2 class="tarjeta-titulo grande">Ficha N° {{ d.numeroFicha }} · {{ fechaDiaMes(d.fecha) }} · {{ horaCorta(d.hora) }}</h2>
        <div class="dato"><span>Afiliado</span><span>{{ afiliado }}</span></div>
        <div class="dato"><span>Paciente</span><span>{{ paciente }}</span></div>
        <div class="dato">
          <span>Consulta</span><span>{{ nombreEspecialidad(d.consulta) }} · {{ d.nombreConsultorio ?? `Consultorio ${d.consultorio}` }}</span>
        </div>
        <div class="dato"><span>Estado</span><ChipEstado :valor="d.estado ?? 'ASISTENCIA_CONFIRMADA'" /></div>

        <div v-if="!atencion.finalizada" class="campo">
          <label for="medicacion">Medicación / receta</label>
          <textarea id="medicacion" v-model="atencion.medicacion" class="entrada receta" rows="6"
                    :maxlength="MAX_RECETA + 200" placeholder="Medicamento, dosis, frecuencia y duración" />
          <span class="ayuda" :class="{ excedido: atencion.caracteres > MAX_RECETA }">
            Entre 1 y {{ MAX_RECETA }} caracteres. {{ atencion.caracteres ? `(${atencion.caracteres})` : '' }}
          </span>
        </div>
        <p v-else class="alerta ok" role="status">
          Atención finalizada: la receta quedó en el historial y la cita pasó a ATENDIDA.
        </p>

        <p v-if="atencion.error" class="alerta error" role="alert">{{ atencion.error }}</p>

        <div class="botones">
          <button v-if="!atencion.finalizada" class="boton" type="submit" :disabled="!atencion.puedeFinalizar">
            {{ atencion.enviando ? 'Guardando…' : 'Finalizar atención' }}
          </button>
          <NuxtLink v-else to="/medico/agenda" class="boton">Volver a la agenda</NuxtLink>
          <NuxtLink v-if="d.puedeDerivar" :to="`/medico/ticket-interno?citaId=${d.citaId}`" class="boton secundario">
            Generar ticket interno
          </NuxtLink>
        </div>
        <p class="ayuda nota-final">Al finalizar, la receta se agrega al historial y la cita pasa a ATENDIDA. El ticket
          interno se puede generar antes o después de finalizar, siempre el mismo día.</p>
      </form>

      <aside class="tarjeta lateral">
        <h2 class="tarjeta-titulo">Historial reciente</h2>
        <p v-if="historial.cargando" class="suave">Cargando…</p>
        <p v-else-if="!recientes.length" class="suave">Sin atenciones anteriores.</p>
        <div v-for="e in recientes" :key="e.clave" class="item-lista" :class="{ cirugia: e.tipo === 'CIRUGIA' }">
          <strong v-if="e.tipo === 'CIRUGIA'">{{ fechaMes(e.fecha, false) }} · Cirugía · {{ nombreEspecialidad(e.especialidad) }}</strong>
          <strong v-else>{{ fechaMes(e.fecha, false) }} · {{ e.titulo }}</strong>
          <span v-if="e.tipo === 'CIRUGIA'">{{ e.lineas[0]?.split(' · ')[0] }} · {{ e.estado }}</span>
          <span v-for="(r, i) in e.recetas" v-else :key="i">Receta: {{ r }}</span>
        </div>
        <NuxtLink :to="`/medico/pacientes/${d.pacienteId}/historial`" class="boton secundario mediano ancho">
          Ver historial completo
        </NuxtLink>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.receta { min-height: 120px; }
.excedido { color: var(--peligro); font-weight: 700; }
.nota-final { font-size: 12px; }
</style>
