<script setup lang="ts">
import { DURACION_INTERNA, INTERNAS, nombreEspecialidad, type Especialidad } from '~/models/especialidad'

/**
 * Lista desplegable de especialidades internas con su duracion de ficha (Figma). Terapia Intensiva solo se
 * habilita con urgencia.
 */
const props = defineProps<{ modelValue: Especialidad | null; urgencia: boolean; deshabilitado?: boolean; id?: string }>()
const emit = defineEmits<{ 'update:modelValue': [Especialidad] }>()
const abierta = ref(false)
const raiz = ref<HTMLElement | null>(null)

function habilitada(e: Especialidad) {
  return e !== 'TERAPIA_INTENSIVA' || props.urgencia
}

function detalle(e: Especialidad) {
  if (e === 'TERAPIA_INTENSIVA') return props.urgencia ? 'habilitada por urgencia' : 'solo con urgencia'
  const [min, max] = DURACION_INTERNA[e] ?? [0, 0]
  return `${min}–${max} min`
}

function elegir(e: Especialidad) {
  if (!habilitada(e)) return
  emit('update:modelValue', e)
  abierta.value = false
}

function alClicFuera(ev: MouseEvent) {
  if (raiz.value && !raiz.value.contains(ev.target as Node)) abierta.value = false
}
onMounted(() => document.addEventListener('click', alClicFuera))
onBeforeUnmount(() => document.removeEventListener('click', alClicFuera))
</script>

<template>
  <div ref="raiz" class="selector">
    <button :id="id" type="button" class="entrada boton-selector" :class="{ resaltada: urgencia }" :disabled="deshabilitado"
            aria-haspopup="listbox" :aria-expanded="abierta" @click="abierta = !abierta" @keydown.esc="abierta = false">
      <span>{{ modelValue ? nombreEspecialidad(modelValue) : 'Elegir especialidad' }}</span>
      <span class="flecha" aria-hidden="true">▾</span>
    </button>
    <ul v-if="abierta" class="lista" role="listbox" :aria-activedescendant="modelValue ?? undefined">
      <li v-for="e in INTERNAS" :id="e" :key="e" role="option" :aria-selected="e === modelValue"
          :aria-disabled="!habilitada(e)" class="opcion-lista"
          :class="{ elegida: e === modelValue, apagada: !habilitada(e) }" tabindex="0"
          @click="elegir(e)" @keydown.enter.prevent="elegir(e)">
        <span>{{ nombreEspecialidad(e) }}</span>
        <span class="duracion">{{ detalle(e) }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.selector { position: relative; }
.boton-selector { display: flex; justify-content: space-between; align-items: center; cursor: pointer; text-align: left; }
.boton-selector.resaltada { background: var(--blanco); border: 1px solid var(--verde-menu); }
.flecha { color: var(--texto-suave); }
.lista {
  position: absolute;
  z-index: 10;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  margin: 0;
  padding: 6px;
  list-style: none;
  background: var(--blanco);
  border: 1px solid #9a9a9a;
  border-radius: 10px;
  box-shadow: var(--sombra-menu);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.opcion-lista {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 7px 10px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.opcion-lista:hover, .opcion-lista:focus-visible { background: var(--fila-par); outline: none; }
.opcion-lista.elegida { background: var(--amarillo); font-weight: 800; }
.opcion-lista.apagada { color: var(--texto-deshabilitado); cursor: not-allowed; }
.duracion { color: var(--texto-suave); font-size: 11px; font-weight: 400; }
</style>
