<script setup lang="ts">
import { useSesionStore } from '~/stores/sesion'

const sesion = useSesionStore()
const route = useRoute()

/**
 * La opcion activa es la de la ruta mas larga que coincide (p. ej. /medico/atencion/3 -> Atención); una pagina
 * puede elegirla con definePageMeta({ menu: '/medico/historial' }).
 */
const activa = computed(() => {
  if (typeof route.meta.menu === 'string') return route.meta.menu
  const coinciden = sesion.menu.filter(o => route.path === o.ruta || route.path.startsWith(`${o.ruta}/`))
  return coinciden.sort((a, b) => b.ruta.length - a.ruta.length)[0]?.ruta
})

async function salir() {
  await sesion.salir()
  await navigateTo('/login')
}
</script>

<template>
  <div class="marco">
    <aside class="menu-lateral">
      <img class="logo" src="/logo-cns.jpg" alt="Logo de la CNS">
      <p class="hospital">Hospital Obrero<br>CNS</p>
      <nav aria-label="Menú principal">
        <NuxtLink v-for="op in sesion.menu" :key="op.ruta" :to="op.ruta" class="opcion-menu"
                  :class="{ activa: activa === op.ruta }" :aria-current="activa === op.ruta ? 'page' : undefined">
          {{ op.texto }}
        </NuxtLink>
      </nav>
      <button class="salir" type="button" @click="salir">Cerrar sesión</button>
    </aside>
    <main class="contenido">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.marco { display: grid; grid-template-columns: 240px 1fr; min-height: 100vh; }
.menu-lateral {
  background: var(--verde-menu);
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 28px 20px;
  position: sticky;
  top: 0;
  height: 100vh;
}
.logo { width: 72px; height: 72px; border-radius: 20px; object-fit: cover; background: var(--blanco); }
.hospital { color: var(--blanco); font-size: 16px; font-weight: 800; margin-bottom: 14px; }
nav { display: flex; flex-direction: column; gap: 10px; flex: 1; }
.opcion-menu {
  color: var(--blanco);
  text-decoration: none;
  font-size: 14px;
  padding: 11px 14px;
  border-radius: 10px;
}
.opcion-menu:hover { background: rgba(255, 255, 255, .12); }
.opcion-menu.activa { background: var(--amarillo); color: var(--texto); font-weight: 800; }
.salir {
  background: none;
  border: 0;
  color: var(--blanco);
  text-align: left;
  padding: 11px 14px;
  font-size: 13px;
  cursor: pointer;
  opacity: .85;
}
.salir:hover { opacity: 1; text-decoration: underline; }
.contenido { padding: 28px 32px; min-width: 0; }

@media (max-width: 900px) {
  .marco { grid-template-columns: 1fr; }
  .menu-lateral { position: static; height: auto; flex-direction: row; flex-wrap: wrap; align-items: center; padding: 12px 16px; }
  .logo { width: 44px; height: 44px; border-radius: 12px; }
  .hospital { margin: 0 12px 0 0; font-size: 14px; }
  nav { flex-direction: row; flex-wrap: wrap; flex: 1 1 100%; order: 3; }
  .contenido { padding: 20px 16px; }
}
@media print {
  .marco { display: block; }
  .menu-lateral { display: none; }
  .contenido { padding: 0; }
}
</style>
