<script setup lang="ts">
import { nombreEspecialidad } from '~/models/especialidad'
import { useSesionStore } from '~/stores/sesion'

const sesion = useSesionStore()
const route = useRoute()

function salir() {
  sesion.salir()
  navigateTo('/login')
}
</script>

<template>
  <div class="marco">
    <aside class="lateral">
      <div class="marca">
        <strong>CNS</strong>
        <span>Web del médico</span>
      </div>
      <nav>
        <template v-for="op in sesion.menu" :key="op.ruta">
          <span v-if="op.pendiente" class="opcion deshabilitada" title="Disponible en una próxima versión">
            {{ op.texto }}
          </span>
          <NuxtLink v-else :to="op.ruta" class="opcion" :class="{ activa: route.path.startsWith(op.ruta) }">
            {{ op.texto }}
          </NuxtLink>
        </template>
      </nav>
      <div v-if="sesion.medico" class="medico">
        <strong>{{ sesion.medico.nombre }}</strong>
        <span>{{ nombreEspecialidad(sesion.medico.especialidad) }}</span>
        <button class="boton chico" type="button" @click="salir">Salir</button>
      </div>
    </aside>
    <main class="contenido">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.marco { display: grid; grid-template-columns: 240px 1fr; min-height: 100vh; }
.lateral {
  background: var(--superficie);
  border-right: 1px solid var(--borde);
  display: flex;
  flex-direction: column;
  padding: 20px 14px;
  gap: 20px;
}
.marca { display: flex; flex-direction: column; padding: 0 8px; }
.marca strong { font-size: 1.3rem; color: var(--primario); letter-spacing: .04em; }
.marca span { color: var(--texto-suave); font-size: .85rem; }
nav { display: flex; flex-direction: column; gap: 4px; flex: 1; }
.opcion {
  padding: 9px 12px;
  border-radius: 8px;
  color: var(--texto);
  text-decoration: none;
  font-weight: 500;
}
.opcion:hover { background: var(--superficie-2); }
.opcion.activa { background: var(--primario-suave); color: var(--primario); font-weight: 600; }
.opcion.deshabilitada { color: var(--texto-suave); opacity: .6; cursor: default; }
.medico { display: flex; flex-direction: column; gap: 2px; padding: 12px 8px 0; border-top: 1px solid var(--borde); }
.medico span { color: var(--texto-suave); font-size: .85rem; margin-bottom: 8px; }
.contenido { padding: 28px 32px; min-width: 0; }

@media (max-width: 860px) {
  .marco { grid-template-columns: 1fr; }
  .lateral { border-right: 0; border-bottom: 1px solid var(--borde); padding: 12px 16px; gap: 10px; }
  nav { flex-direction: row; flex-wrap: wrap; }
  .medico { flex-direction: row; align-items: center; gap: 10px; flex-wrap: wrap; padding-top: 10px; }
  .medico span { margin: 0; }
  .contenido { padding: 20px 16px; }
}
</style>
