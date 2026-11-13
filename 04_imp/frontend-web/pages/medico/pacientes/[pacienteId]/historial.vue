<script setup lang="ts">
import { fechaMes } from '~/models/fechas'
import { type FiltroHistorial, type TipoEvento, useHistorialStore } from '~/stores/historial'

definePageMeta({ menu: '/medico/historial' })

const route = useRoute()
const historial = useHistorialStore()
const pacienteId = computed(() => Number(route.params.pacienteId))

onMounted(() => historial.cargar(pacienteId.value))
watch(pacienteId, id => historial.cargar(id))

const FILTROS: { valor: FiltroHistorial; texto: string }[] = [
  { valor: 'TODO', texto: 'Todo' },
  { valor: 'ATENCIONES', texto: 'Atenciones y recetas' },
  { valor: 'FICHAS', texto: 'Fichas internas' },
  { valor: 'CIRUGIAS', texto: 'Cirugías' },
]

const ETIQUETA: Record<TipoEvento, string> = {
  ATENCION: 'ATENCIÓN', CITA: 'CITA', FICHA_INTERNA: 'FICHA INTERNA', CIRUGIA: 'CIRUGÍA',
}

const subtitulo = computed(() => {
  const h = historial.historial
  if (!h) return ''
  const partes = [h.numeroAfiliado ? `Afiliado N° ${h.numeroAfiliado}` : null,
    h.titular === true ? 'Titular' : h.titular === false ? 'Beneficiario' : null,
    h.edad != null ? `${h.edad} años` : null, 'Solo ves pacientes de tu agenda']
  return partes.filter(Boolean).join(' · ')
})
</script>

<template>
  <section class="pagina">
    <BarraSuperior titulo="Historial del paciente" />

    <div class="tarjeta fila-paciente">
      <div class="datos">
        <p class="nombre">{{ historial.historial?.nombrePaciente || (historial.cargando ? 'Cargando…' : `Paciente ${pacienteId}`) }}</p>
        <p class="ayuda">{{ subtitulo }}</p>
      </div>
      <BuscadorPaciente />
    </div>

    <div v-if="historial.error" class="tarjeta">
      <p class="alerta error" role="alert">
        <strong>{{ historial.sinPermiso ? 'No puede ver este historial' : 'No se pudo cargar el historial' }}.</strong>
        {{ historial.error }}
      </p>
    </div>

    <template v-else-if="historial.historial">
      <div class="opciones" role="tablist" aria-label="Filtrar el historial">
        <button v-for="f in FILTROS" :key="f.valor" class="opcion" :class="{ activa: historial.filtro === f.valor }"
                type="button" role="tab" :aria-selected="historial.filtro === f.valor" @click="historial.filtro = f.valor">
          {{ f.texto }}
        </button>
      </div>

      <p v-if="!historial.eventosFiltrados.length" class="tarjeta vacio">Sin registros para este filtro.</p>
      <ol v-else class="linea">
        <li v-for="e in historial.eventosFiltrados" :key="e.clave" class="evento">
          <p class="cuando">{{ fechaMes(e.fecha) }}</p>
          <div class="detalle-evento">
            <div class="titulo-evento">
              <span class="chip" :class="`tipo-${e.tipo}`">{{ ETIQUETA[e.tipo] }}</span>
              <strong>{{ e.titulo }}</strong>
              <ChipEstado v-if="e.estado" :valor="e.estado" />
            </div>
            <p v-for="(l, i) in e.lineas" :key="i">{{ l }}</p>
          </div>
        </li>
      </ol>
    </template>
  </section>
</template>

<style scoped>
.fila-paciente { flex-direction: row; align-items: center; justify-content: space-between; flex-wrap: wrap; padding: 16px 20px; }
.nombre { font-size: 20px; font-weight: 800; }
.opciones .opcion { background: var(--blanco); }
.opciones .opcion.activa { background: var(--amarillo); }
.linea { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.evento { display: flex; gap: 14px; align-items: flex-start; }
.cuando { width: 110px; flex-shrink: 0; font-size: 14px; font-weight: 800; padding-top: 4px; }
.detalle-evento {
  flex: 1;
  background: var(--blanco);
  border-radius: 16px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 13px;
  min-width: 0;
}
.titulo-evento { display: flex; align-items: center; gap: 8px; }
.titulo-evento strong { flex: 1; font-size: 14px; font-weight: 800; }
.chip.tipo-ATENCION { background: #cfe8cf; }
.chip.tipo-CITA { background: var(--gris-chip); }
.chip.tipo-FICHA_INTERNA { background: var(--estado-interna); }
.chip.tipo-CIRUGIA { background: #f7d6a8; }

@media (max-width: 640px) {
  .evento { flex-direction: column; gap: 4px; }
}
</style>
