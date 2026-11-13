<script setup lang="ts">
import type { PacienteDeAgenda } from '~/models/historial'
import { mensajeDeError } from '~/services/apiClient'

/** «Buscar por N° de afiliado…» del historial: solo encuentra pacientes de la agenda del medico. */
const servicios = useServicios()
const texto = ref('')
const resultados = ref<PacienteDeAgenda[] | null>(null)
const error = ref<string | null>(null)

async function buscar() {
  error.value = null
  if (!texto.value.trim()) return
  try {
    resultados.value = await servicios.historial.buscar(texto.value.trim())
    if (resultados.value.length === 1) await navigateTo(`/medico/pacientes/${resultados.value[0].pacienteId}/historial`)
  } catch (e) {
    error.value = mensajeDeError(e)
  }
}
</script>

<template>
  <form class="buscador" role="search" @submit.prevent="buscar">
    <input v-model="texto" class="entrada" placeholder="Buscar por N° de afiliado…" aria-label="Buscar por N° de afiliado">
    <div v-if="resultados && resultados.length !== 1 || error" class="resultados">
      <p v-if="error" class="alerta error">{{ error }}</p>
      <p v-else-if="!resultados?.length" class="suave">Ningún paciente de tu agenda con ese N° de afiliado.</p>
      <NuxtLink v-for="p in resultados ?? []" :key="p.pacienteId" :to="`/medico/pacientes/${p.pacienteId}/historial`"
                class="resultado">
        <strong>{{ p.nombre }}</strong> · {{ p.titular ? 'Titular' : 'Beneficiario' }} · {{ p.edad }} años
      </NuxtLink>
    </div>
  </form>
</template>

<style scoped>
.buscador { position: relative; width: 280px; max-width: 100%; }
.buscador .entrada { padding: 9px 12px; }
.resultados {
  position: absolute;
  z-index: 5;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  background: var(--blanco);
  border: 1px solid #9a9a9a;
  border-radius: 10px;
  box-shadow: var(--sombra-menu);
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}
.resultado { padding: 6px 8px; border-radius: 6px; text-decoration: none; }
.resultado:hover { background: var(--amarillo); }
</style>
