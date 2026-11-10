<script setup lang="ts">
import { MAX_RECETA } from '~/models/atencion'
import { nombreEspecialidad } from '~/models/especialidad'
import { fechaCorta, horaCorta } from '~/models/fechas'
import { useAtencionStore } from '~/stores/atencion'

const route = useRoute()
const atencion = useAtencionStore()
const citaId = computed(() => Number(route.params.citaId))

onMounted(() => atencion.cargar(citaId.value))
</script>

<template>
  <section class="atencion">
    <NuxtLink to="/medico/agenda" class="boton chico volver">← Agenda</NuxtLink>

    <p v-if="atencion.cargando" class="vacio">Cargando la cita…</p>
    <p v-else-if="!atencion.datos && atencion.error" class="alerta error" role="alert">{{ atencion.error }}</p>

    <template v-else-if="atencion.datos">
      <header>
        <p class="suave">Atención · Ficha {{ atencion.datos.numeroFicha }}</p>
        <h1>{{ atencion.datos.nombrePaciente }}</h1>
      </header>

      <dl class="tarjeta datos">
        <div><dt>Afiliado</dt><dd>{{ atencion.datos.numeroAfiliado }}</dd></div>
        <div><dt>Ficha</dt><dd>{{ atencion.datos.numeroFicha }}</dd></div>
        <div><dt>Fecha</dt><dd>{{ fechaCorta(atencion.datos.fecha) }}</dd></div>
        <div><dt>Hora</dt><dd>{{ horaCorta(atencion.datos.hora) }}</dd></div>
        <div>
          <dt>Consulta</dt>
          <dd>
            {{ nombreEspecialidad(atencion.datos.consulta) }}
            <span class="etiqueta">{{ atencion.datos.categoriaConsulta === 'INTERNA' ? 'Interna' : 'General' }}</span>
          </dd>
        </div>
        <div><dt>Consultorio</dt><dd>{{ atencion.datos.consultorio }}</dd></div>
      </dl>

      <div class="enlaces">
        <NuxtLink :to="`/medico/pacientes/${atencion.datos.pacienteId}/historial`" class="boton chico">
          Ver historial
        </NuxtLink>
        <NuxtLink v-if="atencion.datos.puedeDerivar" :to="`/medico/ticket-interno?citaId=${atencion.datos.citaId}`"
                  class="boton chico">
          Generar ticket interno
        </NuxtLink>
      </div>

      <div v-if="atencion.finalizada" class="alerta ok" role="status">
        Atención finalizada: la receta quedó en el historial y la cita pasó a «Atendida».
      </div>

      <form v-else class="tarjeta receta" @submit.prevent="atencion.finalizar()">
        <div class="campo">
          <label for="medicacion">Medicación</label>
          <textarea id="medicacion" v-model="atencion.medicacion" rows="7" :maxlength="MAX_RECETA + 200"
                    placeholder="Medicamento, dosis, frecuencia y duración"></textarea>
          <span class="contador" :class="{ excedido: atencion.caracteres > MAX_RECETA }">
            {{ atencion.caracteres }} / {{ MAX_RECETA }}
          </span>
        </div>
        <p v-if="atencion.error" class="alerta error" role="alert">{{ atencion.error }}</p>
        <div class="pie">
          <button class="boton primario" type="submit" :disabled="!atencion.puedeFinalizar">
            {{ atencion.enviando ? 'Guardando…' : 'Finalizar atención' }}
          </button>
        </div>
      </form>
    </template>
  </section>
</template>

<style scoped>
.atencion { display: flex; flex-direction: column; gap: 18px; max-width: 860px; }
.volver { align-self: flex-start; }
header p { margin: 0 0 2px; }
.datos { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px 24px; margin: 0; }
.datos dt { font-size: .8rem; text-transform: uppercase; letter-spacing: .04em; color: var(--texto-suave); }
.datos dd { margin: 2px 0 0; font-weight: 600; display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.enlaces { display: flex; gap: 8px; flex-wrap: wrap; }
.receta { display: flex; flex-direction: column; gap: 14px; }
.receta textarea { resize: vertical; min-height: 140px; }
.contador { align-self: flex-end; font-size: .8rem; color: var(--texto-suave); font-variant-numeric: tabular-nums; }
.contador.excedido { color: var(--peligro); font-weight: 600; }
.receta .alerta { margin: 0; }
.pie { display: flex; justify-content: flex-end; }

@media (max-width: 640px) {
  .datos { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
