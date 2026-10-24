# Decisiones pendientes antes de programar

[Índice general](README.md) · [Fuentes](11_Fuentes_y_diagramas.md) · [Elaboración](../Backlog_Elaboracion/README.md)

**No son reglas aprobadas**: estos puntos se identificaron contrastando Visión 0.1, matriz RF, especificación de CU y el material de arquitectura/UI recibido. No deben transformarse silenciosamente en nuevos RF, condiciones o endpoints.

| Decisión | Pendiente / fundamento | Quién la valida | Issues afectados |
|---|---|---|---|
| <a id="d-01"></a> **D-01: Reasignación automática al cancelar** | RF-11 y CU-11 obligan a cambiar estado y registrar motivo, no a reasignar inmediatamente a otra persona. Confirmar existencia de lista de espera, prioridad, aceptación y destino del cupo. | ELA-04 / aprobación CNS | CON-21 (condicional); CON-16 obligatorio |
| <a id="d-02"></a> **D-02: Criterio de selección entre múltiples recursos** | CU-05 documenta elegir combinación disponible pero no su prioridad ni reglas de turno/desempate. | ELA-04 | CON-12, CON-13 |
| <a id="d-03"></a> **D-03: Estado inexistente vs inasistencia** | RF-12 menciona programada, atendida, cancelada e inexistente; HU-12 y CU-12 se refieren a inasistencia. Se debe resolver estado real y transiciones. | ELA-03 | CON-19, CON-28, CON-27 |
| <a id="d-04"></a> **D-04: Credenciales y tiempo de sesión** | CU-01 no define proveedor de identidad ni expiración. No imponer Google, OTP o JWT antes de decisión. | ELA-05 / ELA-09 | CON-03, CON-07 |
| <a id="d-05"></a> **D-05: Datos mínimos e identificación de pacientes** | CU-03 no define el tipo de documento ni servicios institucionales de afiliación. Validar campos y acceso. | ELA-03 / ELA-07 | CON-02, CON-06, CON-08 |
| <a id="d-06"></a> **D-06: Contrato REST y WS** | El README arquitectónico refiere 05_contratos_api.md todavía no entregado. Fijar rutas, errores, autenticación, destinos de eventos y recuperación. | ELA-05 | CON-01, CON-13, CON-22, CON-23 |
| <a id="d-07"></a> **D-07: Campos médicos permitidos** | CU-14 no detalla campos de paciente/historial accesibles para médico. No expandir a historia clínica avanzada. | ELA-03 / ELA-09 | CON-25, CON-28, CON-33 |
| <a id="d-08"></a> **D-08: Medición de tiempos de atención** | CU-15 obliga registrar espera y atención, pero no define origen ni cálculo de tiempos. | ELA-03 | CON-25, CON-27 |
| <a id="d-09"></a> **D-09: Canal y antelación de notificaciones** | CU-16/17 no definen SMS, correo, push, ni tiempo previo del recordatorio. Solo WebSocket/in-app si se aprueba como canal suficiente. | ELA-05 / CNS | CON-23, CON-24 |
| <a id="d-10"></a> **D-10: Fórmulas, períodos y metas de indicadores** | CU-20 identifica categorías, pero no fija fórmula ni umbrales. | ELA-04 / CNS | CON-27, CON-29 |
| <a id="d-11"></a> **D-11: Prototipo de pantallas** | El ZIP diseñoUI contiene solo README, sin maquetas concretas. ELA-06 debe aprobar UI v1. | ELA-06 | CON-04, CON-07, CON-08, CON-17, CON-18, CON-28, CON-29 |
| <a id="d-12"></a> **D-12: Esquema PostgreSQL y servidor** | PostgreSQL es una decisión de planificación técnica posterior a Visión 0.1. No confundir con tecnología confirmada en Visión; proveedor servidor aún no fijado. | ELA-07 / ELA-09 | CON-02, CON-30, CON-35 |
| <a id="d-13"></a> **D-13: Artefactos de arquitectura aún faltantes** | El ZIP 02-arquitecturaGeneral incluye un README que describe archivos futuros; no contiene las reglas por CU, contratos ni modelo desplegado. | ELA-02 a ELA-09 | Todos los CON que dependen del diseño |

## Bloqueos y reglas de trabajo

- Los Issues que implementan políticas no definidas pueden preparar pruebas y contratos *simulados*, pero no fijar la política definitiva sin aprobación.
- **CON-21** no debe bloquear emisiones WebSocket, generación de notificaciones ni las pruebas obligatorias. Si no se aprueba la reasignación automática, se cierra como fuera de alcance o se deja en Backlog con decisión explícita; el cierre de RF-11 exige cancelar/liberar, no reasignar a otra persona.
- En el momento que ELA-02 a ELA-09 definan los archivos reales (`02_mvc.md`, `04_reglas`, `05_contratos_api.md`, etc.), reemplazar estas referencias pendientes por enlaces relativos reales y actualizar criterios del issue correspondiente.
- El código Java garantiza los invariantes de disponibilidad y transacciones: Flutter no reserva cupos directamente ni WebSocket reemplaza REST.
