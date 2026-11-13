<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

definePageMeta({ layout: 'vacio' })

const sesion = useSesionStore()
const usuario = ref('')
const clave = ref('')

async function entrar() {
  if (!(await sesion.entrar(usuario.value, clave.value))) return
  await navigateTo(sesion.esMedico ? sesion.inicio : '/sin-acceso')
}
</script>

<template>
  <div class="login">
    <img class="logo" src="/logo-cns.jpg" alt="Logo de la CNS">
    <form class="tarjeta inicio" @submit.prevent="entrar">
      <h1>Iniciar sesión</h1>
      <div class="campo">
        <label for="usuario" class="etiqueta-campo">Usuario</label>
        <input id="usuario" v-model="usuario" class="entrada" autocomplete="username" placeholder="Ingresar usuario"
               required>
      </div>
      <div class="campo">
        <label for="clave" class="etiqueta-campo">Contraseña</label>
        <input id="clave" v-model="clave" class="entrada" type="password" autocomplete="current-password"
               placeholder="Ingresar contraseña" required>
      </div>
      <p v-if="sesion.error" class="alerta error" role="alert">{{ sesion.error }}</p>
      <button class="boton ancho" type="submit" :disabled="sesion.cargando">
        {{ sesion.cargando ? 'Ingresando…' : 'Iniciar Sesión' }}
      </button>
      <p class="ayuda-registro">¿Aún no tienes cuenta? Si eres asegurado titular activo, regístrate desde la
        aplicación.</p>
    </form>
  </div>
</template>

<style scoped>
.login { display: flex; flex-direction: column; align-items: center; gap: 24px; width: min(420px, 100%); }
.logo { width: 120px; height: 120px; border-radius: 20px; object-fit: cover; background: var(--blanco); }
.inicio { width: 100%; gap: 14px; }
h1 { font-size: 24px; font-weight: 800; }
.campo { gap: 6px; }
.etiqueta-campo { font-size: 12px !important; font-weight: 500 !important; }
.ayuda-registro { font-size: 12px; color: var(--texto-suave); }
</style>
