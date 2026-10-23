# Backlog de Construcción — WebSocket

[Volver al índice](README.md) · Responsable indicado **como propuesta**, validar carga al iniciar cada sprint.

### [CON-22][WebSocket] Emitir eventos de citas con WebSocket desde Spring Boot

**Identificador:** `CON-22` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Matías · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** WebSocket  
**Requisitos relacionados:** RF-05, RF-07, RF-10, RF-11, RF-12, RF-16, RF-17  
**Casos de uso relacionados:** CU-05, CU-07, CU-10, CU-11, CU-12, CU-16, CU-17  
**Depende de (referencias del plan):** CON-13, CON-16, CON-21  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:websocket`

**Descripción:** Notificar cambios relevantes desde backend tras confirmar transacciones.

**Criterios de aceptación**
- [ ] Conexiones se autentican y cada usuario recibe eventos autorizados.
- [ ] No se difunde información privada de otros pacientes.
- [ ] Reconexión no sustituye consulta REST de estado actual.

**Tareas**
- [ ] Configurar canal de eventos y seguridad.
- [ ] Emitir eventos solo tras commit exitoso.

**Caso de prueba sugerido:** Paciente A no recibe aviso de paciente B.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-22-emitir-eventos-de-citas-con-websock` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).
