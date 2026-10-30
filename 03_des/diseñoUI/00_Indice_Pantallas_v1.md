# 00 — Índice de Pantallas v1 (inventario funcional/conceptual)

**Estado:** inventario de diseño v1. **No constituye evidencia de implementación Flutter ejecutable.**  
**Fuente base:** `03_des/diseñoUI/README.md`, CU/RF/HU vigentes y contratos de arquitectura S2.

## 1) Alcance del inventario

- Organiza pantallas por rol: paciente, admisión/enfermería, médico y administrador.
- Incluye flujos de acceso/sesión, solicitud y consulta de citas, avisos/recordatorios, gestión operativa y reportes/indicadores según permisos.
- Los procesos automáticos (asignación, validación, confirmación interna, generación de avisos y recordatorios) se ejecutan en backend; **no requieren una pantalla por cada proceso**.
- Cuando una vista no tiene prototipo validado en el repositorio, se marca como **Propuesta**.

## 2) Inventario por rol

### 2.1 Paciente

| ID pantalla | Nombre | Rol | Propósito | CU | RF/HU | Controlador conceptual | Operación REST / lectura | Evento WS (si aplica) | Éxito esperado | Errores esperados | Datos visibles y privacidad |
|---|---|---|---|---|---|---|---|---|---|---|---|
| UI-PAC-01 | Acceso y sesión paciente *(Propuesta)* | Paciente | Autenticarse para consultar su información | CU-01, CU-08 | RF/HU-01,08 | `SesionController` | `POST /auth/login` (propuesto) | N/A | Sesión válida y contexto de paciente | credenciales inválidas, no autorizado | Sin datos de terceros; no exponer tokens en pantalla. |
| UI-PAC-02 | Solicitud de cita por especialidad *(Propuesta)* | Paciente | Registrar solicitud sin elegir médico/fecha/hora/consultorio | CU-04 | RF/HU-04 | `SolicitudCitaController` | `POST /solicitudes-cita` | `cita.confirmada` (solo informativo posterior) | Solicitud aceptada; posible cita asignada | sin disponibilidad, validación | Mostrar resultado propio del paciente. |
| UI-PAC-03 | Mis citas *(Propuesta)* | Paciente | Consultar listado y detalle permitido de sus citas | CU-08 | RF/HU-08 | `ConsultasPacienteController` | `GET /citas/mias` | `cita.reprogramada`, `cita.cancelada`, `cita.estado_actualizado` | Lista de citas propias y detalle permitido | no autenticado, acceso denegado a cita ajena | Solo sus citas; nunca mostrar datos de otro paciente. |
| UI-PAC-04 | Buzón de avisos y recordatorios *(Propuesta)* | Paciente | Ver notificaciones de cambios confirmados y recordatorios | CU-16, CU-17 | RF/HU-16,17 | `NotificacionesController` | `GET /notificaciones/mias` | `notificacion.disponible` | Buzón actualizado | error de conexión, sesión expirada | Contenido mínimo: evento, cita, fecha/hora; sin datos sensibles extra. |

### 2.2 Admisión / Enfermería

| ID pantalla | Nombre | Rol | Propósito | CU | RF/HU | Controlador conceptual | Operación REST / lectura | Evento WS (si aplica) | Éxito esperado | Errores esperados | Datos visibles y privacidad |
|---|---|---|---|---|---|---|---|---|---|---|---|
| UI-ADM-01 | Acceso y sesión operativa *(Propuesta)* | Admisión/Enfermería | Ingreso al módulo de operación de citas | CU-01, CU-09 | RF/HU-01,09 | `SesionController` | `POST /auth/login` | N/A | Sesión autorizada | credenciales inválidas, rol sin permiso | Mostrar solo menú permitido por rol. |
| UI-ADM-02 | Consulta/Gestión de citas filtradas *(Propuesta)* | Admisión/Enfermería | Buscar citas por paciente, médico, fecha, estado y consultorio | CU-09 | RF/HU-09 | `ConsultasCitasController` | `GET /citas` con filtros autorizados | `cita.confirmada`, `cita.reprogramada`, `cita.cancelada` | Resultado filtrado autorizado | filtros inválidos, sin permisos, sin resultados | Minimizar campos sensibles; datos operativos únicamente. |
| UI-ADM-03 | Reprogramación / cancelación confirmada *(Propuesta)* | Admisión/Enfermería | Ejecutar cambios autorizados sobre cita existente | CU-10, CU-11, CU-12 | RF/HU-10,11,12 | `OperacionesCitasController` | `PATCH /citas/{id}/reprogramacion`, `POST /citas/{id}/cancelacion`, `PATCH /citas/{id}/estado` | `cita.reprogramada`, `cita.cancelada`, `cita.estado_actualizado` | Cambio persistido y auditado | conflicto de versión, transición inválida, no autorizado | Mostrar motivo/estado y trazabilidad mínima. |
| UI-ADM-04 | Atención operativa / llegada *(Propuesta)* | Admisión/Enfermería | Registrar llegada y datos operativos permitidos | CU-12, CU-15 | RF/HU-12,15 | `AtencionOperativaController` | `POST /citas/{id}/llegada`, `POST /citas/{id}/atencion` | `cita.estado_actualizado` | Registro operativo confirmado | fuera de ventana, datos inválidos, sin permiso | No mostrar ni capturar historia clínica fuera de alcance. |

### 2.3 Médico

| ID pantalla | Nombre | Rol | Propósito | CU | RF/HU | Controlador conceptual | Operación REST / lectura | Evento WS (si aplica) | Éxito esperado | Errores esperados | Datos visibles y privacidad |
|---|---|---|---|---|---|---|---|---|---|---|---|
| UI-MED-01 | Agenda médica diaria/semanal *(Propuesta)* | Médico | Consultar agenda y citas autorizadas | CU-13 | RF/HU-13 | `AgendaMedicaController` | `GET /medicos/me/agenda` | `cita.confirmada`, `cita.reprogramada`, `cita.cancelada` | Agenda actualizada | no autorizado, sin datos | Solo pacientes vinculados a citas autorizadas. |
| UI-MED-02 | Datos permitidos del paciente citado *(Propuesta)* | Médico | Ver información mínima del paciente asociado a cita | CU-14 | RF/HU-14 | `PacienteCitadoController` | `GET /citas/{id}/paciente-autorizado` | N/A | Datos mínimos visibles | acceso denegado a cita no vinculada | Sin exponer información de pacientes fuera de su agenda. |
| UI-MED-03 | Registro de atención operativa *(Propuesta)* | Médico | Registrar asistencia/espera/atención permitida | CU-15 | RF/HU-15 | `AtencionOperativaController` | `POST /citas/{id}/atencion` | `cita.estado_actualizado` | Registro confirmado | validación, transición inválida | Solo datos operativos del alcance aprobado. |

### 2.4 Administrador

| ID pantalla | Nombre | Rol | Propósito | CU | RF/HU | Controlador conceptual | Operación REST / lectura | Evento WS (si aplica) | Éxito esperado | Errores esperados | Datos visibles y privacidad |
|---|---|---|---|---|---|---|---|---|---|---|---|
| UI-ADMN-01 | Gestión de cuentas y roles *(Propuesta)* | Administrador | Administrar usuarios/roles aprobados | CU-02 | RF/HU-02 | `AdministracionUsuariosController` | `GET /usuarios`, `PATCH /usuarios/{id}/roles` | N/A | Cambios autorizados | 403, validación | Sin exposición de credenciales. |
| UI-ADMN-02 | Reportes de citas *(Propuesta)* | Administrador | Consultar reportes por estado, médico, especialidad y consultorio | CU-19 | RF/HU-19 | `ReportesCitasController` | `GET /reportes/citas` | `cita.estado_actualizado` (para refresco posterior) | Reporte consolidado | sin datos, sin permisos | Mostrar datos agregados según autorización. |
| UI-ADMN-03 | Indicadores de gestión *(Propuesta)* | Administrador | Consultar demanda, cancelación, inasistencia, tiempos y carga laboral | CU-20 | RF/HU-20 | `IndicadoresGestionController` | `GET /indicadores/gestion` | `cita.estado_actualizado` (refresco) | Indicadores disponibles | datos insuficientes, sin permisos | Sin fórmulas inventadas ni umbrales no aprobados. |

## 3) Navegación mínima v1

1. **Acceso/sesión** por rol.
2. **Paciente:** Solicitud de cita → Mis citas → Buzón de avisos/recordatorios.
3. **Admisión/enfermería:** Consulta/gestión de citas → Reprogramación/cancelación → Atención operativa/llegada.
4. **Médico:** Agenda → Datos permitidos de paciente citado → Registro de atención operativa.
5. **Administrador:** Gestión de usuarios/roles → Reportes → Indicadores.

## 4) Mapa Vista → CU → RF/HU → controlador conceptual

| Vista / conjunto | CU | RF/HU | Controlador conceptual |
|---|---|---|---|
| Mis citas paciente | CU-08 | RF/HU-08 | `ConsultasPacienteController` |
| Consulta de citas en admisión | CU-09 | RF/HU-09 | `ConsultasCitasController` |
| Buzón de avisos | CU-16 | RF/HU-16 | `NotificacionesController` |
| Recordatorios de citas futuras | CU-17 | RF/HU-17 | `RecordatoriosController` |
| Reportes de citas | CU-19 | RF/HU-19 | `ReportesCitasController` |
| Indicadores de gestión | CU-20 | RF/HU-20 | `IndicadoresGestionController` |

## 5) Pendientes UI/documentales

- Definir qué prototipos visuales están cerrados y cuáles siguen en propuesta (en esta carpeta solo existe `README.md`).
- Confirmar canal técnico final por rol (Flutter para todos o coexistencia con otros canales).
- Validar campos definitivos por pantalla según autorización final y políticas de privacidad.
- Confirmar identidad técnica/autenticación (token/sesión) en diseño de detalle.
- No existe `MAPA_INTERFACES.md` separado en el repositorio; se usa como referencia conceptual la combinación de contratos entre módulos, REST, seguridad, vistas y WebSocket.
