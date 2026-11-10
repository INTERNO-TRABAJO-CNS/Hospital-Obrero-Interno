# [T100-16][Docs][Modelo V] Requisitos y diseño de fichas internas, quirófanos y ajustes (RF-21…25, CU-21…25)

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `docs/T100-16-fichas-internas-modelo-v` (desde `develop`) |
| PR hacia | `develop` |
| Puntos | 3 |
| Nivel del modelo en V | Izquierda: requisitos + diseño de arquitectura (se verifican en T100-31 y T100-32) |
| Casos de uso | CU-21, CU-22, CU-23, CU-24, CU-25 (nuevos) · CU-02, 04, 05, 06, 13, 14, 15, 16, 19, 20 (modificados) |
| Requisitos | RF-21…RF-25 (nuevos) · RF-02, 04, 05, 06, 13, 16, 19, 20 · RNF-07, RNF-09 · RU-14 |
| Depende de / Bloquea | Ninguna / Bloquea: T100-31, T100-32 (las pruebas se escriben contra estos criterios) |

## Objetivo
Dejar por escrito, **antes de cerrar el Sprint 4**, todo lo acordado el 10/11/2026 para que el código (lado derecho del modelo en V) tenga contra qué verificarse: requisitos, casos de uso, reglas, estados, clases, base de datos y la matriz requisito ↔ prueba.

## Reglas de negocio que debes cumplir
- Usar exactamente las decisiones de la sección «Decisiones del 10/11/2026» de `Issues_T100_FichasInternas/LEEME.md`; no inventar reglas nuevas. Lo no definido va como `PV-19…PV-22`.
- Estado de cada requisito nuevo o cambiado: «Nuevo / Modificado 10/11/2026 — por validar».
- No renumerar RF/CU existentes; los nuevos continúan en RF-21 y CU-21.
- Cada RF nuevo tiene HU y criterio Dado/Cuando/Entonces: de ahí salen las pruebas de aceptación de T100-32.

## Diagramas y documentos de referencia (solo para leer)
- `06_proyect/CAMBIOS_04-11-2026.md` (formato a imitar para el registro de cambios)
- `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md` y `Especificacion_de_Casos_de_Uso_CNS.md`
- `03_des/02-arquitecturaGeneral/04_reglas_negocio.md`, `07_base_de_datos.md`, `11_decisiones_pendientes.md`
- `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio/ARQ-03_ModeloDominio.puml`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md` | RF-21 Derivar a especialidad (ficha interna) · RF-22 Asignar ficha interna por prioridad (desplazamiento, EN_ESPERA, Terapia Intensiva) · RF-23 Atender ficha interna (especialista) · RF-24 Programar cirugía sin choques · RF-25 Consultar cola de prioridad. Modificar RF-04 (Pediatría < 10 años), RF-05 (≥ 60 años), RF-06 (cruce de horario con fichas internas y cirugías), RF-13 (agenda en web), RF-16 (ticket impreso), RF-19/20 (20 tablas + quirófanos). RNF-07 (web del médico en Nuxt) y **RNF-09 nuevo** (cero choques en quirófano garantizado por la BD). RU-14 nuevo. |
| `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` | CU-21 Generar ficha interna · CU-22 Asignar ficha interna por prioridad (Sistema) · CU-23 Atender ficha interna · CU-24 Programar cirugía en quirófano · CU-25 Consultar cola de prioridad. Actualizar CU-02, 04, 05, 06, 13, 14, 15, 16, 19, 20 y la tabla de actores (Médico General, Médico de Pediatría, Especialista). |
| `03_des/02-arquitecturaGeneral/04_reglas_negocio.md` | RN-17…RN-28 (ver lista de reglas en el LEEME) |
| `03_des/02-arquitecturaGeneral/11_decisiones_pendientes.md` | PV-19 horario de quirófanos · PV-20 cirugías de Cardiología, Nefrología, Neumología y Endocrinología (> 12 h, si hay tiempo) · PV-21 una sola ficha interna activa por paciente · PV-22 qué parte del historial ve el especialista |
| `03_des/02-arquitecturaGeneral/07_base_de_datos.md, 06_contratos_rest_api.md, 10_websocket.md` | esquemas `fichas_internas` (20 tablas + vista `v_todas`) y `quirofano`; endpoints y canal `/ws/fichas-internas` de las tarjetas T100-17…T100-30 |
| `03_des/diagramaCU/CU-00_DiagramaGeneral_CNS/ y CU-21…CU-25 (carpeta + .puml)` | actores Médico General/Pediatría y Especialista; CU-22 «include» desde CU-21 |
| `03_des/01-diagramaCompor/CU-21_GenerarFichaInterna/ … CU-25_ConsultarColaPrioridad/` | diagramas de comportamiento con flujos alternativos numerados `[Na]` (cada uno será un test) |
| `03_des/diagramasEstado/ED-08_FichaInterna/ED-08_FichaInterna.puml` | EN_ESPERA → ASIGNADA → ATENDIDA | CANCELADA (no se presentó); ASIGNADA → EN_ESPERA/ASIGNADA por desplazamiento |
| `03_des/diagramasEstado/ED-09_Cirugia/ED-09_Cirugia.puml` | PROGRAMADA → REALIZADA | CANCELADA; PROGRAMADA → PROGRAMADA (desplazada) |
| `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio/ARQ-03_ModeloDominio.puml` | clases `FichaInterna`, `PrioridadClinica`, `Quirofano`, `Cirugia`; `TipoEspecialidad` con categoría |
| `06_proyect/TRAZABILIDAD.md y 03_des/02-arquitecturaGeneral/12_trazabilidad.md` | filas CU-21…25 → T100-16…32 y responsables |
| `06_proyect/CAMBIOS_10-11-2026.md` | registro de cambios del día (mismo formato que el del 04/11) |
| `06_proyect/MATRIZ_MODELO_V.md` | tabla: nivel izquierdo (RF / arquitectura / contrato) ↔ prueba derecha (aceptación T100-32 / integración T100-31 / unitarias de cada tarjeta) |

## Paso a paso
1. Rama `docs/T100-16-fichas-internas-modelo-v` desde `develop`.
2. Requisitos (RF/RU/RNF) con HU y criterios de aceptación.
3. Especificación de CU-21…25 y cambios en los CU afectados.
4. Reglas RN-17…28 y pendientes PV-19…22.
5. Diagramas `.puml` (CU, comportamiento, estados ED-08/ED-09, ARQ-03). Las imágenes se regeneran si hay PlantUML.
6. Matriz del modelo en V y registro de cambios.
7. PR a `develop`; avisar al grupo para que lean sus diagramas antes de programar.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `Revisión cruzada` | el revisor comprueba que cada regla del LEEME aparece en un RF/RN y cada RF en un CU |
| `Matriz V completa` | cada RF-21…25 tiene al menos una prueba de aceptación asignada en T100-32 |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch develop
git pull --ff-only origin develop
git switch -c docs/T100-16-fichas-internas-modelo-v
# ... trabajar, commits pequeños: docs(T100-16): descripción
git push -u origin docs/T100-16-fichas-internas-modelo-v
```
Abrir el Pull Request con **base = `develop`**. La documentación **no** entra a la rama T100 (regla 4 de `Issues_T100/LEEME.md`: en T100 va solo código). Solo Benjamín integra.

## Fuera de alcance
- Código (va en T100-17…T100-30).
- Regenerar los `.docx` originales.

## Definition of Done
- [ ] RF, CU, RN, PV, diagramas y matriz V actualizados
- [ ] Revisión aprobada por Alejandro
- [ ] PR integrado en `develop`
- [ ] Tarjeta en **Hecho** en el tablero

## Registro
Issue `#__` · PR `#__` · Rama `docs/T100-16-fichas-internas-modelo-v`
