# ARQ-05 — Contratos entre submodelos (interfaces conceptuales v1)

**Estado:** borrador de integración de Benjamín. Los métodos son **nombres ilustrativos**, no API Java existente. Los identificadores compartidos no crean clases duplicadas entre dominios.

## Propiedad y dependencias
| Proveedor / dueño | Consumidor | Contrato conceptual | Respuestas importantes | CU |
|---|---|---|---|---|
| M01 Identidad | M03 Citas | `obtenerPacienteHabilitado(numeroAseguradoCNS, actorId)` | pacienteId / AFILIACION_NO_VALIDA / NO_AUTORIZADO | 01,03,04 |
| M01 Identidad | M05 Consultas | `autorizarLecturaCitas(actorId, pacienteId)` | permitido/denegado | 08,09,14 |
| M02 Recursos | M03 Citas | `buscarPrimeraCombinacionDisponible(especialidadId, desde, limites)` | recurso candidato / SIN_DISPONIBILIDAD | 05,06 |
| M02 Recursos | M03 Citas | `validarCombinacionVigente(candidato)` | validación condicionada / CONFLICTO_RECURSO | 06,07 |
| M03 Citas | M04 Operaciones | `obtenerCitaParaOperacion(citaId, actorId)` | proyección + versión / no existe/denegado | 10,11,12,15 |
| M04 Operaciones | M03 Citas | `aplicarCambioAtomico(citaId, cambio, expectedVersion)` | cita confirmada actualizada / CONFLICTO_VERSION | 10,11,12 |
| M03/04 Citas/Operaciones | M05 Comunicación | `publicarEventoConfirmado(eventoId, tipo, citaId, timestamp)` | evento idempotente / reintento | 16,17,18 |
| M02/03/04 | M05 Consultas | `obtenerProyeccionAutorizada(filtros, actorId)` | DTO filtrado/denegado | 08,09,13,19,20 |

**Acuerdo importante:** `M02.validarCombinacionVigente` por sí sola no garantiza unicidad. La **transacción de M03** debe aplicar bloqueo/constraint del esquema PostgreSQL cuando guarda la cita.

## DTOs de contrato sugeridos
- `PacienteRef {pacienteId, numeroAseguradoCNS}` (nunca exponer CI innecesariamente).
- `RecursoCandidato {medicoId, especialidadId, consultorioId, inicio, fin, agendaId, versionDisponibilidad}`.
- `CitaResumen {citaId, pacienteId, medicoId, especialidadId, consultorioId, inicio, fin, estado, version}`.
- `EventoCita {eventoId, citaId, tipo, instante, actorId, versionCita}`.

Datos exactos, nullabilidad y tipos **pendientes de validar contra el ERD actual**.

## Garantías de integración
1. El proveedor define el contrato y su dueño; el consumidor depende de él sin replicar persistencia.
2. Autorización se comprueba en backend incluso si Flutter oculta botones.
3. La operación de cambio es idempotente o rechaza reintentos con claves/versión, nunca genera citas duplicadas.
4. El evento se emite después de confirmar; la falla de WS no revierte la transacción de negocio.
5. El catálogo de errores se comparte; no se modelan decisiones clínicas o institucionales desconocidas.
6. Revisar [MAPA_INTERFACES previo](../../06_proyect/Backlog_Elaboracion/Sprint_2_Modelo_MVC/MAPA_INTERFACES.md) en su ruta real del repositorio antes de añadir enlaces relativos.
