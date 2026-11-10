# [T100-32][Pruebas][Modelo V] Pruebas de sistema y aceptación de punta a punta (web Nuxt + app Flutter) — todo el equipo

| Campo | Valor |
|---|---|
| Milestone | Sprint 6 - Validacion y entrega (vence 2026-11-28) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alex |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `docs/T100-32-pruebas-aceptacion-sistema` (desde `develop`) |
| PR hacia | `develop` |
| Puntos | 3 |
| Nivel del modelo en V | Derecha: sistema y aceptación (verifica los requisitos RF-21…25 de T100-16) |
| Casos de uso | CU-21…CU-25 y CU modificados |
| Requisitos | RF-21…RF-25 · RF-04 · RF-05 · RF-06 · RF-16 · RF-19 |
| Depende de / Bloquea | T100-31 / Bloquea: entrega final |

## Objetivo
Probar el sistema completo como lo usaría el hospital, contra cada criterio Dado/Cuando/Entonces de T100-16. **Cada integrante prueba lo que ajustó** y Benjamín consolida.

## Reglas de negocio que debes cumplir
- Datos de prueba: un médico general, un pediatra, un odontólogo, un especialista por especialidad que opera, un médico de Terapia Intensiva, un quirófano por especialidad que opera, un paciente de 8 años, uno de 35 y uno de 65.
- Evidencia: capturas y resultado (OK / falla + issue) en `05_pro/pruebas_aceptacion_fichas_internas.md` (documentación: va por `develop`, no por la rama T100).

## Diagramas y documentos de referencia (solo para leer)
- `06_proyect/MATRIZ_MODELO_V.md` y criterios de RF-21…25 *(T100-16)*

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `05_pro/pruebas_aceptacion_fichas_internas.md` | tabla: caso · pasos · resultado esperado · resultado obtenido · responsable · evidencia |

## Paso a paso
1. **Benjamín** — derivación normal, urgencia, Terapia Intensiva, desplazamiento, EN_ESPERA → asignada al siguiente día hábil, cirugía sin choques (incluida la limpieza), urgencia que desplaza una cirugía, supervisión del administrador.
2. **Matías** — paciente de 10 años no puede pedir Pediatría; el paciente no ve las especialidades internas; registro con fecha de nacimiento.
3. **Alejandro** — agenda del médico en Nuxt; botón Ticket interno solo para General/Pediatría; enfermería sigue confirmando en Flutter.
4. **Alex** — historial en Nuxt con fichas internas y cirugías; el especialista no ve la gravedad.
5. **Abigail** — atención en Nuxt; notificación en la app y ticket impreso de cita común, ficha interna y cirugía; el ticket no muestra la gravedad.
6. Benjamín consolida los resultados y abre los bugs.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `1 caso por criterio de aceptación de RF-21…25` | todos OK o con bug abierto y aceptado |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch develop
git pull --ff-only origin develop
git switch -c docs/T100-32-pruebas-aceptacion-sistema
# ... trabajar, commits pequeños: docs(T100-32): descripción
git push -u origin docs/T100-32-pruebas-aceptacion-sistema
```
Abrir el Pull Request con **base = `develop`**. La documentación **no** entra a la rama T100 (regla 4 de `Issues_T100/LEEME.md`: en T100 va solo código). Solo Benjamín integra.

## Fuera de alcance
- Pruebas de rendimiento y seguridad avanzada.

## Definition of Done
- [ ] Todos los casos ejecutados por su responsable
- [ ] Evidencia publicada
- [ ] Bugs críticos corregidos
- [ ] Tarjeta en **Hecho**

## Registro
Issue `#__` · PR `#__` · Rama `docs/T100-32-pruebas-aceptacion-sistema`
