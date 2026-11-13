<script setup lang="ts">
import { nombreEspecialidad } from '~/models/especialidad'
import { fechaCorta, fechaTicket, horaActual, hoyISO } from '~/models/fechas'

definePageMeta({ layout: 'impresion' })

/**
 * Ticket de 80 mm (T100-30): mismo formato para fichas internas y cirugias. Nunca se imprime la gravedad.
 * La barra superior no sale en la impresion (@media print).
 */
interface Ticket {
  tipo: string
  numero: number
  filas: [string, string][]
  indicaciones: string
}

const route = useRoute()
const router = useRouter()
const servicios = useServicios()
const ticket = ref<Ticket | null>(null)
const noEncontrado = ref(false)
const emitido = `${fechaCorta(hoyISO())} ${horaActual()}`

onMounted(async () => {
  if (route.query.ficha) {
    const f = await servicios.fichas.porTicket(Number(route.query.ficha))
    if (f) {
      const espera = f.estado === 'EN_ESPERA'
      ticket.value = {
        tipo: espera ? 'FICHA INTERNA · EN ESPERA' : 'FICHA INTERNA',
        numero: f.ticket,
        filas: [['Paciente', f.nombrePaciente], ['N° afiliado', f.numeroAfiliado],
          ['Especialidad', nombreEspecialidad(f.especialidad)], ['Profesional', f.medico ?? 'Por asignar'],
          ['Lugar', f.consultorio ? `Consultorio ${f.consultorio}` : 'Por asignar'],
          ['Fecha', f.fecha ? fechaTicket(f.fecha) : 'Sin fecha todavía'], ['Hora', f.hora ?? '—']],
        indicaciones: espera
          ? 'Su ficha está en espera. Le avisaremos en la app cuando tenga fecha y hora; puede pedir que le reimpriman el ticket.'
          : 'Derivado por su médico. No necesita confirmar asistencia. Si no se presenta, pierde este horario.',
      }
    }
  } else if (route.query.cirugia) {
    const c = await servicios.cirugias.porNumero(Number(route.query.cirugia))
    if (c) {
      ticket.value = {
        tipo: 'CIRUGÍA',
        numero: c.numero,
        filas: [['Paciente', c.nombrePaciente], ['N° afiliado', c.numeroAfiliado || '—'],
          ['Especialidad', nombreEspecialidad(c.especialidad)], ['Profesional', c.cirujano],
          ['Lugar', `Quirófano ${c.quirofano}`], ['Fecha', fechaTicket(c.fecha)], ['Hora', c.inicio]],
        indicaciones: 'Ayuno de 8 horas. Llegue 1 hora antes con su carnet y un acompañante.',
      }
    }
  }
  noEncontrado.value = !ticket.value
})

function imprimir() {
  window.print()
}

function volver() {
  if (window.history.length > 1) router.back()
  else navigateTo('/medico/agenda')
}
</script>

<template>
  <div class="vista">
    <header class="barra no-imprimir">
      <h1>Vista de impresión · ticket de 80 mm</h1>
      <div class="acciones">
        <button class="boton mediano" type="button" :disabled="!ticket" @click="imprimir">Imprimir</button>
        <button class="boton mediano secundario" type="button" @click="volver">Volver</button>
      </div>
    </header>

    <p v-if="noEncontrado" class="aviso no-imprimir">No se encontró el ticket pedido.</p>

    <article v-if="ticket" class="ticket">
      <img src="/logo-cns.jpg" alt="Logo de la CNS" class="logo">
      <p class="institucion">CAJA NACIONAL DE SALUD<br>Hospital Obrero N° 1</p>
      <hr>
      <p class="tipo">{{ ticket.tipo }}</p>
      <p class="numero">N° {{ ticket.numero }}</p>
      <hr>
      <dl>
        <div v-for="[r, v] in ticket.filas" :key="r" class="fila"><dt>{{ r }}</dt><dd>{{ v }}</dd></div>
      </dl>
      <p class="rotulo">Indicaciones</p>
      <p class="indicaciones">{{ ticket.indicaciones }}</p>
      <hr>
      <p class="pie">Emitido: {{ emitido }}</p>
      <p class="pie">Si no tiene internet, este ticket es su comprobante.</p>
    </article>

    <p class="nota no-imprimir">Mismo formato para citas comunes, fichas internas y cirugías. Nunca se imprime la
      gravedad. La barra superior no sale en la impresión.</p>
  </div>
</template>

<style scoped>
.vista { display: flex; flex-direction: column; align-items: center; gap: 24px; padding-bottom: 32px; }
.barra {
  align-self: stretch;
  background: var(--blanco);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
  flex-wrap: wrap;
}
.barra h1 { font-size: 18px; font-weight: 800; }
.acciones { display: flex; gap: 10px; }
.aviso { background: var(--blanco); padding: 12px 16px; border-radius: 10px; }
.ticket {
  width: 80mm;
  max-width: 100%;
  background: var(--blanco);
  border: 1px solid #ccc;
  padding: 14px 16px;
  font-size: 10px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 6px;
}
.logo { width: 36px; height: 36px; object-fit: cover; align-self: center; }
.institucion { text-align: center; font-weight: 700; font-size: 9px; }
hr { border: 0; border-top: 1px dashed #999; margin: 4px 0; width: 100%; }
.tipo { text-align: center; font-weight: 800; font-size: 11px; }
.numero { text-align: center; font-weight: 800; font-size: 28px; }
dl { margin: 0; display: flex; flex-direction: column; gap: 5px; }
.fila { display: flex; justify-content: space-between; gap: 8px; }
.fila dt { color: var(--texto-suave); }
.fila dd { margin: 0; font-weight: 700; text-align: right; }
.rotulo { font-weight: 800; margin-top: 4px; }
.indicaciones { line-height: 1.35; }
.pie { text-align: center; color: var(--texto-suave); font-size: 8.5px; }
.nota { font-size: 12px; color: var(--texto-nota); text-align: center; padding: 0 16px; }

@media print {
  .no-imprimir { display: none !important; }
  .vista { padding: 0; }
  .ticket { border: 0; }
  @page { size: 80mm auto; margin: 4mm; }
}
</style>
