# Índice de Pantallas Flutter v1 — CNS (S2-M05)

**Objetivo:** inventario mínimo de vistas por rol para CU-08, CU-09, CU-16, CU-17, CU-19, CU-20 y flujos principales CNS, sin crear una pantalla por cada subfunción automática.

**Convenciones:**
- Pantalla = vista funcional de usuario.
- CU automáticos (CU-16, CU-17) se reflejan como bandeja/estado en vistas existentes; no requieren vista independiente por motor interno.
- Controlador conceptual (MVC) = punto de entrada lógico del backend.

---

## 1) Paciente

| Pantalla v1 | CU | Operación principal | Respuesta/Error esperada | Controlador conceptual |
|---|---|---|---|---|
| Acceso y autenticación | CU-01 | Iniciar sesión y cargar rol | Éxito: sesión válida / Error: credencial inválida | `AuthController` (M01) |
| Solicitar cita por especialidad | CU-04 | Enviar solicitud sin elegir médico/fecha/hora | Éxito: solicitud recibida / Error: afiliación no válida o sin disponibilidad posterior | `SolicitudCitaController` (M03) |
| Mis citas | CU-08 | Consultar citas del paciente autenticado | Éxito: lista propia / Error: `NO_AUTORIZADO` o sin citas | `ConsultaCitasController` (M05) |
| Detalle de cita propia | CU-08 | Ver datos permitidos de una cita listada | Éxito: detalle propio / Error: intento de acceso a cita ajena | `ConsultaCitasController` (M05) |
| Bandeja de avisos/recordatorios | CU-16, CU-17 | Consultar notificaciones y recordatorios de sus citas | Éxito: avisos visibles / Error: sin avisos o evento descartado | `NotificacionController` y `RecordatorioController` (M05) |

**Privacidad obligatoria:** paciente A no visualiza detalle de citas de paciente B.

---

## 2) Admisión / Enfermería

| Pantalla v1 | CU | Operación principal | Respuesta/Error esperada | Controlador conceptual |
|---|---|---|---|---|
| Buscar/gestionar citas | CU-09 | Filtrar por paciente, médico, fecha, estado y consultorio | Éxito: coincidencias / Error: criterio inválido o sin resultados | `ConsultaCitasController` (M05) |
| Reprogramar/cancelar cita | CU-10, CU-11 | Aplicar cambio confirmado sobre cita existente | Éxito: cambio persistido y aviso posterior / Error: conflicto de recurso o no autorizado | `OperacionCitaController` (M04 + contrato M03) |
| Confirmar llegada / registrar atención administrativa | CU-12, CU-15 | Registrar estado y tiempos permitidos | Éxito: registro guardado / Error: transición inválida | `OperacionCitaController` (M04) |

---

## 3) Médico

| Pantalla v1 | CU | Operación principal | Respuesta/Error esperada | Controlador conceptual |
|---|---|---|---|---|
| Agenda diaria/semanal | CU-13 | Consultar agenda propia autorizada | Éxito: citas de su agenda / Error: sin agenda disponible | `AgendaController` (M02) |
| Paciente asociado a cita asignada | CU-14 | Ver información permitida del paciente de su cita | Éxito: datos permitidos / Error: acceso denegado a paciente ajeno | `AgendaController` (M02) |

---

## 4) Administrador

| Pantalla v1 | CU | Operación principal | Respuesta/Error esperada | Controlador conceptual |
|---|---|---|---|---|
| Reportes de citas | CU-19 | Generar reportes por estado, médico, especialidad y consultorio | Éxito: reporte agrupado / Error: sin datos para criterio | `ReporteController` (M05) |
| Indicadores de gestión | CU-20 | Consultar demanda, cancelación, inasistencia, tiempos y carga laboral | Éxito: indicadores disponibles / Error: datos insuficientes | `IndicadorController` (M05) |
| Administración de usuarios/roles (fuera de MDL-05, pero parte del flujo CNS) | CU-02 | Gestionar cuentas/roles autorizados | Éxito: operación permitida / Error: restricción de permisos | `AdminUsuariosController` (M01) |

---

## 5) Mapa mínimo Vista → CU → RF/HU → Controlador

| Vista | CU | RF/HU | Controlador |
|---|---|---|---|
| Mis citas / detalle propio | CU-08 | RF/HU-08 | `ConsultaCitasController` |
| Buscar/gestionar citas (admisión) | CU-09 | RF/HU-09 | `ConsultaCitasController` |
| Bandeja de avisos | CU-16 | RF/HU-16 | `NotificacionController` |
| Bandeja de recordatorios | CU-17 | RF/HU-17 | `RecordatorioController` |
| Reportes administrativos | CU-19 | RF/HU-19 | `ReporteController` |
| Indicadores administrativos | CU-20 | RF/HU-20 | `IndicadorController` |

---

## 6) REST vs WebSocket en la UI

- **REST:** crea/consulta/confirma operaciones (fuente de verdad).
- **WebSocket:** solo avisa cambios ya confirmados para refrescar vista.
- Si se pierde WebSocket, la app reconsulta por REST al reconectar.
- La UI no decide estados de negocio ni reemplaza controles de autorización.

---

## 7) Pendientes explícitos de diseño

- Tiempo exacto de recordatorios (CU-17): pendiente de validación.
- Canales externos definitivos (push/SMS/correo): pendiente de validación.
- Fórmulas de indicadores (CU-20): pendiente de validación.
- `MAPA_INTERFACES.md` y `CONTEXTO_COMPARTIDO.md` no se localizaron en la ruta citada por los issues coordinados; este inventario se alinea con los documentos disponibles en `02_req/archivos-md` y `03_des/02-arquitecturaGeneral`.
