# Orden de subida — Construcción

**Nunca convertir `CON-XX` automáticamente en números `T-XX` sin comprobar la numeración previa del CNS.** Primero cerrar Elaboración y los contratos del sistema.

| Orden | Referencia | Nombre del issue | Milestone | Área | Responsable | Puntos | Depende de | #GitHub |
|---:|---|---|---|---|---|---:|---|---|
| 1 | CON-01 | Crear estructura base del backend Spring Boot | Sprint 3 | Java | Benjamín | 5 | ELA-02, ELA-05 | Pendiente |
| 2 | CON-02 | Implementar esquema PostgreSQL y migraciones | Sprint 3 | PostgreSQL | Alex | 5 | CON-01, ELA-07 | Pendiente |
| 3 | CON-03 | Implementar autenticación y permisos por rol | Sprint 3 | Java | Matías | 5 | CON-01, CON-02 | Pendiente |
| 4 | CON-04 | Crear estructura base de aplicación Flutter | Sprint 3 | Flutter | Abigal | 5 | ELA-02, ELA-06 | Pendiente |
| 5 | CON-05 | Implementar administración de usuarios, roles y permisos | Sprint 3 | Java | Matías | 3 | CON-03 | Pendiente |
| 6 | CON-06 | Implementar registro y consulta de pacientes | Sprint 3 | Java | Alejandro | 5 | CON-02, CON-03 | Pendiente |
| 7 | CON-07 | Crear pantalla de inicio de sesión y sesión en Flutter | Sprint 3 | Flutter | Abigal | 3 | CON-03, CON-04 | Pendiente |
| 8 | CON-08 | Crear pantallas Flutter de registro y consulta de pacientes | Sprint 3 | Flutter | Alex | 3 | CON-04, CON-06 | Pendiente |
| 9 | CON-09 | Configurar CI y pruebas base Java/Flutter | Sprint 3 | Pruebas | Benjamín | 3 | CON-01, CON-04 | Pendiente |
| 10 | CON-10 | Implementar catálogos de médicos, especialidades, recursos y agendas | Sprint 4 | Java | Alex | 5 | CON-02, CON-03 | Pendiente |
| 11 | CON-11 | Implementar validación de disponibilidad y no doble reserva | Sprint 4 | Java | Benjamín | 5 | CON-02, CON-10 | Pendiente |
| 12 | CON-12 | Implementar motor de asignación automática íntegramente en Java | Sprint 4 | Java | Benjamín | 5 | CON-11, ELA-04 | Pendiente |
| 13 | CON-13 | Implementar solicitud y confirmación de citas con transacción | Sprint 4 | Java | Alejandro | 5 | CON-06, CON-12 | Pendiente |
| 14 | CON-14 | Implementar consulta de citas del paciente | Sprint 4 | Java | Alejandro | 3 | CON-13 | Pendiente |
| 15 | CON-15 | Implementar consulta de citas para admisión | Sprint 4 | Java | Matías | 3 | CON-13 | Pendiente |
| 16 | CON-16 | Implementar reprogramación y cancelación de citas | Sprint 4 | Java | Matías | 5 | CON-11, CON-13 | Pendiente |
| 17 | CON-17 | Diseñar e implementar pantallas paciente para solicitar y consultar citas | Sprint 4 | Flutter | Abigal | 5 | CON-04, CON-13, CON-14 | Pendiente |
| 18 | CON-18 | Implementar vistas de admisión para citas, reprogramación y cancelación | Sprint 4 | Flutter | Abigal | 5 | CON-15, CON-16 | Pendiente |
| 19 | CON-19 | Implementar agenda médica y actualización de estado | Sprint 4 | Java | Alex | 5 | CON-10, CON-13 | Pendiente |
| 20 | CON-20 | Pruebas integradas de solicitud, asignación, reprogramación y cancelación | Sprint 4 | Pruebas | Benjamín | 5 | CON-13, CON-16 | Pendiente |
| 21 | CON-21 | Implementar reasignación de cupo liberado en Java | Sprint 5 | Java | Benjamín | 5 | CON-16, CON-20 | Pendiente |
| 22 | CON-22 | Emitir eventos de citas con WebSocket desde Spring Boot | Sprint 5 | WebSocket | Matías | 5 | CON-13, CON-16, CON-21 | Pendiente |
| 23 | CON-23 | Integrar notificaciones y refresco de citas en Flutter | Sprint 5 | Flutter | Abigal | 5 | CON-22, CON-17 | Pendiente |
| 24 | CON-24 | Generar notificaciones y recordatorios de cita | Sprint 5 | Java | Alejandro | 5 | CON-13, CON-21, CON-22 | Pendiente |
| 25 | CON-25 | Implementar consulta permitida de paciente y registro de atención | Sprint 5 | Java | Alex | 5 | CON-03, CON-19 | Pendiente |
| 26 | CON-26 | Implementar historial de cambios de citas | Sprint 5 | Java | Alejandro | 3 | CON-16, CON-19 | Pendiente |
| 27 | CON-27 | Implementar reportes de citas e indicadores de gestión | Sprint 5 | Java | Alex | 5 | CON-13, CON-19 | Pendiente |
| 28 | CON-28 | Crear vistas del médico para agenda y registro de atención | Sprint 5 | Flutter | Abigal | 5 | CON-19, CON-25 | Pendiente |
| 29 | CON-29 | Crear vistas de administración para historial, reportes e indicadores | Sprint 5 | Flutter | Abigal | 3 | CON-26, CON-27 | Pendiente |
| 30 | CON-30 | Desplegar ambiente de integración de Java, Flutter y PostgreSQL | Sprint 5 | Servidor | Matías | 3 | CON-09, CON-22 | Pendiente |
| 31 | CON-31 | Ejecutar pruebas extremo a extremo de los veinte casos de uso | Sprint 6 | Pruebas | Benjamín | 5 | CON-20, CON-23, CON-24, CON-25, CON-29, CON-30 | Pendiente |
| 32 | CON-32 | Medir concurrencia, rendimiento y robustez del núcleo Java | Sprint 6 | Pruebas | Alex | 5 | CON-21, CON-30 | Pendiente |
| 33 | CON-33 | Auditar permisos, privacidad y configuración de seguridad | Sprint 6 | Pruebas | Matías | 5 | CON-03, CON-25, CON-30 | Pendiente |
| 34 | CON-34 | Ajustar validaciones, accesibilidad y estabilidad de Flutter | Sprint 6 | Flutter | Abigal | 3 | CON-23, CON-28, CON-29 | Pendiente |
| 35 | CON-35 | Probar respaldo, restauración y recuperación del sistema | Sprint 6 | Servidor | Alex | 3 | CON-30 | Pendiente |
| 36 | CON-36 | Cerrar trazabilidad, checklist y versión de entrega | Sprint 6 | Pruebas | Alejandro | 3 | CON-31, CON-32, CON-33, CON-34, CON-35 | Pendiente |
