# MDL-05 — Consultas, notificaciones, recordatorios, reportes e indicadores (S2-M05)

**Tipo:** diseño/documentación (sin código de producción)  
**Cobertura:** CU-08, CU-09, CU-16, CU-17, CU-19, CU-20 · RF/HU-08,09,16,17,19,20 · RNF-01,02,05,06

## 1) Propósito y límites del submodelo M05

M05 define el submodelo de **lectura y comunicación** para citas ya gestionadas por otros dominios.

- **M05 consume** datos de M01/M02/M03/M04.
- **M05 no es dueño** de `CitaMedica`, `Paciente` ni `Agenda`.
- **M05 no crea** reglas finales de asignación, cancelación o transición de estados.
- **M05 no fija** fórmulas definitivas de indicadores ni estructuras físicas de BD no aprobadas.

## 2) Propiedad y separación de conceptos

### 2.1 Entidades persistentes vs proyecciones y cálculos

| Elemento M05 | Tipo conceptual en este diseño | Alcance / regla |
|---|---|---|
| `Notificacion` | Entidad de comunicación del buzón (persistencia por confirmar con DER) | Se registra tras eventos confirmados; no se genera por operaciones rechazadas. |
| `Recordatorio` | Puede persistirse o emitirse bajo demanda (pendiente de implementación real) | Siempre deduplicable por `eventoId`/clave conceptual; solo para citas vigentes `PROGRAMADA`. |
| `ConsultaDeCitas` | Proyección de lectura / DTO | No es entidad dueña de la cita; representa vista filtrada por permisos. |
| `Reporte` | Proyección agregada | Agrupa por estado, médico, especialidad y consultorio. |
| `Indicador` | Cálculo derivado | Sin fórmulas, denominadores, períodos ni metas definitivas en este documento. |

### 2.2 Ciclo y deduplicación de avisos

- Un evento confirmado de cita (`cita.confirmada`, `cita.reprogramada`, `cita.cancelada`, `cita.estado_actualizado` o equivalente autorizado) genera como máximo una unidad de aviso por destinatario y tipo.
- Reintentos de entrega deben ser **idempotentes** o **deduplicables** con `eventoId` (o clave equivalente compuesta por `tipoEvento + citaId + versión`).
- Si falla el canal WebSocket, el backend no revierte la transacción confirmada: el cliente reconecta y relee por REST.

## 3) Contratos conceptuales consumidos desde M01–M04

> Los nombres siguientes son de diseño y deben alinearse con `05_contratos_entre_modulos.md`; **no se declaran APIs Java finales**.

- **M01 → M05**
  - `autorizarLecturaCitas(actorId, pacienteId)`
  - Resultado: permitido/denegado; base para CU-08 y CU-09.
- **M02/M03/M04 → M05**
  - `obtenerProyeccionAutorizada(filtros, actorId)` para consultas/reportes autorizados.
- **M03/M04 → M05**
  - `publicarEventoConfirmado(eventoId, tipo, citaId, timestamp)` para avisos/recordatorios/reportes derivados.

## 4) Reglas funcionales por caso de uso

### 4.1 CU-08 / RF-08 / HU-08 — Paciente consulta sus citas

- Filtro obligatorio por **identidad autenticada**.
- Caso de seguridad: paciente A nunca accede al detalle de paciente B.
- Datos mínimos visibles: médico, especialidad, fecha/hora, consultorio, estado (`PROGRAMADA`, `ATENDIDA`, `CANCELADA`) y datos permitidos del flujo.
- Errores esperados: no autenticado/no autorizado; intento de acceso a cita ajena.
- Respuesta válida sin citas: lista vacía (no error funcional).

### 4.2 CU-09 / RF-09 / HU-09 — Admisión/enfermería consulta con filtros

- Filtros permitidos: paciente, médico, fecha, estado y consultorio.
- Validar formato/coherencia de filtros antes de consultar.
- Errores: filtro inválido, usuario sin permiso, combinación no autorizada.
- Si no hay coincidencias, devolver lista vacía.
- Minimizar campos sensibles: mostrar solo atributos operativos autorizados.

### 4.3 CU-16 / RF-16 / HU-16 — Avisos por cambios confirmados

- Eventos origen: cambios **confirmados** en M03/M04 (`cita.confirmada`, `cita.reprogramada`, `cita.cancelada`, `cita.estado_actualizado` o equivalente autorizado).
- No emitir avisos por intentos rechazados o transacciones fallidas.
- Destinatarios por rol:
  - Paciente propietario: aviso de su cita.
  - Personal autorizado (admisión/enfermería/médico): solo en ámbito de agenda/permisos.
- Carga mínima por rol: `eventoId`, referencia de cita, tipo de cambio, fecha/hora vigente, estado.

### 4.4 CU-17 / RF-17 / HU-17 — Recordatorios

- Solo para citas vigentes y programadas.
- No generar recordatorio de citas canceladas o ya atendidas.
- **Pendiente explícito:** antelación exacta del recordatorio y canal externo/interno.

### 4.5 CU-19 / RF-19 / HU-19 — Reportes de citas

- Agrupaciones permitidas: estado, médico, especialidad y consultorio.
- Si no existen datos para el criterio, informar sin inventar resultados.
- **Pendiente explícito:** formato de exportación/salida no aprobado.

### 4.6 CU-20 / RF-20 / HU-20 — Indicadores de gestión

Categorías documentadas (sin fórmulas cerradas):
- demanda,
- cancelación,
- inasistencia,
- tiempos de espera/atención,
- carga laboral.

No fijar en este diseño fórmulas, denominadores, períodos, metas ni umbrales sin aprobación.

## 5) REST vs WebSocket (diferencia obligatoria)

- **REST** es el canal autoritativo para crear/consultar/confirmar cambios y obtener datos.
- **WebSocket** solo difunde cambios ya confirmados.
- Si WebSocket cae:
  1. la transacción de backend ya confirmada **no se revierte**,
  2. no se duplica ni elimina una cita,
  3. el cliente reconecta y consulta REST como fuente de verdad.

## 6) Rol de MVC en M05

- **Vista (Flutter/UI):** presenta información y errores; no define reglas finales de negocio.
- **Controlador (UI/REST):** coordina solicitud/respuesta y estado visual.
- **Modelo/servicios M05:** construyen consultas, proyecciones y cálculos derivados autorizados.

## 7) Verificación de diseño (casos exigidos)

### Caso A — Privacidad entre pacientes
**Dado** paciente A autenticado, **cuando** consulta o intenta abrir cita de paciente B, **entonces** el backend rechaza el acceso y no expone detalle de B.

### Caso B — Caída de WebSocket
**Dado** una cita ya confirmada por REST, **cuando** se cae WebSocket durante la difusión, **entonces** la cita confirmada no se elimina ni duplica; al reconectar, el cliente refresca estado por REST.

## 8) Pendientes explícitos

1. Antelación exacta de recordatorio (RF-17).
2. Canal externo/interno para avisos y recordatorios.
3. Fórmulas y ventanas temporales de indicadores (RF-20).
4. Permisos/campos definitivos por rol para CU-08/09/19/20.
5. Prototipos UI cerrados no disponibles en esta carpeta (solo guía en `03_des/diseñoUI/README.md`).
6. Conciliación de identificadores (`numeroAseguradoCNS` vs `numero_historia_clinica`) si impacta DTOs de lectura.
7. Mecanismo técnico de autenticación/autorización aún abierto en arquitectura.
8. No se localizó `MAPA_INTERFACES.md` separado en el repositorio; se usa como mapa conceptual `05_contratos_entre_modulos.md`, `06_contratos_rest_api.md`, `08_seguridad_privacidad.md`, `09_interfaz_flutter_v1.md` y `10_websocket.md`.

## 9) Trazabilidad (CU/RF/HU/RNF y controlador conceptual)

| CU | RF/HU | RNF clave | Controlador conceptual (diseño) | Resultado esperado |
|---|---|---|---|---|
| CU-08 | RF/HU-08 | RNF-01, RNF-02 | `ConsultasPacienteController` | Solo citas propias; lista vacía permitida. |
| CU-09 | RF/HU-09 | RNF-01, RNF-02 | `ConsultasCitasController` | Filtros autorizados y validación de criterios. |
| CU-16 | RF/HU-16 | RNF-05, RNF-06 | `NotificacionesController` + servicio de eventos | Avisos solo tras cambio confirmado. |
| CU-17 | RF/HU-17 | RNF-05, RNF-06 | `RecordatoriosController` (interno) | Recordatorio de citas vigentes/programadas. |
| CU-19 | RF/HU-19 | RNF-02, RNF-06 | `ReportesCitasController` | Reportes por estado/médico/especialidad/consultorio. |
| CU-20 | RF/HU-20 | RNF-02, RNF-06 | `IndicadoresGestionController` | Indicadores derivados sin fórmulas inventadas. |

## 10) Criterios de aceptación cubiertos en este documento

- [x] M05 documentado como consumidor de M01–M04, sin duplicar dueño de `CitaMedica`, `Paciente` ni `Agenda`.
- [x] Separación clara entre persistencia/proyecciones/cálculos (`Notificacion`, `Recordatorio`, `ConsultaDeCitas`, `Reporte`, `Indicador`).
- [x] Reglas de privacidad y filtros para CU-08 y CU-09.
- [x] Eventos confirmados, destinatarios y payload mínimo para CU-16.
- [x] Recordatorios solo para citas vigentes/programadas con pendientes explícitos.
- [x] Reportes e indicadores sin inventar fórmulas no aprobadas.
- [x] Distinción REST/WS y resiliencia ante caída de WS.
- [x] Verificación de diseño casos A y B.
- [x] Trazabilidad completa para CU-08/09/16/17/19/20.
