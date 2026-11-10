<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ layout: 'vacio' })

const sesion = useSesionStore()
const usuario = ref('')
const clave = ref('')
const medicoId = ref<number | null>(null)
const modoTemporal = ref(false)

async function despues(ok: boolean) {
  if (!ok) return
  await navigateTo(sesion.esMedico ? '/medico/agenda' : '/sin-acceso')
}

async function entrar() {
  await despues(await sesion.entrar(usuario.value, clave.value))
}

async function entrarTemporal() {
  if (!medicoId.value) return
  await despues(await sesion.entrarTemporal(medicoId.value))
}
</script>

<template>
  <div class="tarjeta login">
    <div class="encabezado">
      <strong class="marca">CNS</strong>
      <h1>Web del médico</h1>
      <p class="suave">Agenda, atención e historial de sus pacientes.</p>
    </div>

    <form v-if="!modoTemporal" class="formulario" @submit.prevent="entrar">
      <div class="campo">
        <label for="usuario">Usuario</label>
        <input id="usuario" v-model="usuario" autocomplete="username" required>
      </div>
      <div class="campo">
        <label for="clave">Contraseña</label>
        <input id="clave" v-model="clave" type="password" autocomplete="current-password" required>
      </div>
      <button class="boton primario" type="submit" :disabled="sesion.cargando">
        {{ sesion.cargando ? 'Ingresando…' : 'Ingresar' }}
      </button>
    </form>

    <form v-else class="formulario" @submit.prevent="entrarTemporal">
      <p class="alerta info">Acceso temporal de pruebas: hasta que exista la seguridad real (S3-08) se entra con el id
        del médico.</p>
      <div class="campo">
        <label for="medico">Id del médico</label>
        <input id="medico" v-model.number="medicoId" type="number" min="1" required>
      </div>
      <button class="boton primario" type="submit" :disabled="sesion.cargando">Entrar</button>
    </form>

    <p v-if="sesion.error" class="alerta error" role="alert">{{ sesion.error }}</p>

    <button class="enlace" type="button" @click="modoTemporal = !modoTemporal">
      {{ modoTemporal ? 'Volver al ingreso con usuario y contraseña' : 'Acceso temporal con id de médico' }}
    </button>
  </div>
</template>

<style scoped>
.login { width: min(400px, 100%); display: flex; flex-direction: column; gap: 18px; padding: 28px; }
.encabezado { display: flex; flex-direction: column; gap: 4px; }
.encabezado p { margin: 0; }
.marca { color: var(--primario); letter-spacing: .06em; }
.formulario { display: flex; flex-direction: column; gap: 14px; }
.formulario .alerta { margin: 0; font-size: .9rem; }
.enlace { background: none; border: 0; color: var(--primario); cursor: pointer; font: inherit; font-size: .9rem; }
</style>
