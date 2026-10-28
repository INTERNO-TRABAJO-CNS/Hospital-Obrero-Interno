# MDL-05 — Consultas, notificaciones, recordatorios, reportes e indicadores (S2-M05)

**Cobertura:** CU-08, CU-09, CU-16, CU-17, CU-19, CU-20 ↔ RF/HU-08,09,16,17,19,20 · RNF-02, RNF-05, RNF-06.

**Alcance de este submodelo:** lectura/comunicación. **No es dueño** de `CitaMedica`, `Paciente` ni `Agenda`; consume proyecciones y eventos confirmados de MDL-01/02/03/04.

---

## 1) Propiedad de datos y tipos de modelo

### 1.1 Entidades persistentes de MDL-05
- `Notificacion`
  - Finalidad: avisar al destinatario cuando una cita queda **confirmada, reprogramada o cancelada**.
  - Datos mínimos: `notificacionId`, `citaId`, `pacienteIdDestinatario`, `tipoEvento`, `mensajeResumen`, `fechaGeneracion`, `estadoLectura`.
- `Recordatorio`
  - Finalidad: registrar avisos previos de citas futuras vigentes.
  - Datos mínimos: `recordatorioId`, `citaId`, `pacienteIdDestinatario`, `fechaProgramadaEnvio`, `estadoEnvio`.

### 1.2 DTO/proyecciones de lectura (no dueños de persistencia de cita)
- `ConsultaDeCitasDTO`: vista filtrada para CU-08/CU-09 (médico, especialidad, fecha, hora, consultorio, estado, datos permitidos por rol).
- `ReporteCitasDTO`: agregaciones de CU-19 (por estado, médico, especialidad, consultorio).
- `IndicadorGestionDTO`: resultados de CU-20 (demanda, cancelación, inasistencia, tiempos, carga laboral) calculados desde registros consolidados.

### 1.3 Cálculos y límites
- Reportes e indicadores son **lecturas derivadas** (no reemplazan entidades transaccionales de M03/M04).
- No se fijan fórmulas nuevas de indicadores en este documento; quedan como **pendiente de validación** cuando no estén aprobadas en la base documental.

---

## 2) Eventos confirmados que originan comunicación (M03/M04 → M05)

Se procesan **solo eventos posteriores a commit**:
- `cita.confirmada` (M03)
- `cita.reprogramada` (M04)
- `cita.cancelada` (M04)

Regla de consistencia:
1. Operación REST transaccional confirma el estado en backend.
2. Recién después se publica evento para aviso (WebSocket y/o bandeja).
3. Si no hay confirmación, **no** se genera notificación contradictoria.

Mensajes por rol (minimización):
- **Paciente:** recibe detalle de su propia cita.
- **Admisión/enfermería y médico:** solo cambios de citas bajo su contexto operativo/autorizado.
- **Administrador:** vista consolidada para supervisión/reportes, sin exponer datos innecesarios de identidad.

Privacidad (RNF-02):
- El backend filtra por `actorId + rol + relación con la cita`.
- La UI no sustituye autorización de backend.
- Caso A obligatorio: paciente A no puede consultar detalle de paciente B.

---

## 3) REST vs WebSocket (diferencia obligatoria)

### REST (fuente de verdad transaccional)
- Crear/consultar/confirmar cambios de cita.
- Respuestas con éxito/error de negocio (`NO_AUTORIZADO`, `SIN_RESULTADOS`, `CONFLICTO_VERSION`, etc.).
- Garantiza idempotencia por control de versión/clave de operación en backend.

### WebSocket (aviso de cambios ya confirmados)
- Solo informa que hay novedad (`eventoId`, `citaId`, `version`, `timestamp`).
- No crea ni cancela citas por sí mismo.
- Si hay caída/reconexión, el cliente vuelve a consultar por REST para sincronizar estado.

Caso B obligatorio:
- Una caída de WebSocket **no elimina ni duplica** una cita ya confirmada en backend.

---

## 4) Cobertura funcional por CU

- **CU-08 (paciente):** consulta únicamente sus citas; sin registros ajenos; si no hay citas, respuesta vacía controlada.
- **CU-09 (admisión/enfermería):** filtros por paciente, médico, fecha, estado y consultorio; validación de criterio.
- **CU-16:** avisos tras confirmación, cambio o cancelación confirmados.
- **CU-17:** recordatorios de citas futuras vigentes según configuración aprobada.
- **CU-19:** reportes por estado, médico, especialidad y consultorio.
- **CU-20:** indicadores de demanda/cancelación/inasistencia/tiempos/carga con datos consolidados y sin inventar fórmulas.

---

## 5) Errores esperados y manejo de exposición

- `NO_AUTORIZADO`: no mostrar datos de terceros.
- `SIN_RESULTADOS`: informar ausencia de coincidencias (CU-08/CU-09/CU-19).
- `DATOS_INSUFICIENTES`: no publicar indicador no respaldado (CU-20).
- `EVENTO_DESCARTADO`: evento no confirmado o duplicado por `eventoId` ya procesado.

---

## 6) Controladores conceptuales (MVC)

- `ConsultaCitasController` → CU-08, CU-09
- `NotificacionController` → CU-16
- `RecordatorioController` → CU-17
- `ReporteController` → CU-19
- `IndicadorController` → CU-20

La Vista Flutter v1 solo presenta estado y dispara solicitudes; no ejecuta reglas de negocio ni recalcula estados oficiales.

---

## 7) Pendientes explícitos (sin inventar requisitos)

- Tiempo exacto de anticipación para recordatorios (CU-17): **pendiente de validación**.
- Canales externos definitivos de notificación (push/SMS/correo): **pendiente de validación**.
- Fórmulas exactas, período y metas de indicadores (CU-20): **pendiente de validación**.
- `CONTEXTO_COMPARTIDO.md` y `MAPA_INTERFACES.md` fueron referenciados por los issues coordinados, pero no están en la ruta esperada de esta copia; se trabajó con fuentes disponibles del Sprint 1 y documentos ARQ de `03_des/02-arquitecturaGeneral`.
