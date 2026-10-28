# 00 · Índice y orden de creación de Issues — Construcción CNS

[Volver al backlog](README.md) · [Estimación](07_Estimacion.md) · [Cobertura](08_Cobertura_y_trazabilidad.md) · [Flujos](09_Flujos_y_dependencias.md)

Este archivo indica el **orden de registro propuesto**, con los dependientes referenciados por `CON-XX` dentro de los `.md`. `CON-XX` NO es el número `#` de GitHub ni una clave `T-XX` ya asignada. No se crearon issues ni ramas. Confirmar la numeración libre en el repositorio antes de publicarlos.

**Previos:** cerrar/aprobar las tareas ELA-02 a ELA-09 pertinentes del [backlog de Elaboración](../Backlog_Elaboracion/README.md). Los archivos reales de arquitectura todavía se encuentran pendientes según material recibido.

| Orden | Referencia y enlace | Título propuesto | Milestone | Área | Responsable | Puntos | Depende de | GitHub |
|---:|---|---|---|---|---|---:|---|---|
| 1 | [CON-01](02_Java.md#con-01) | Crear estructura base del backend Spring Boot | Sprint 3 - Bases e identidad | Java | Benjamín | 5 | [ELA-02](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) · [ELA-05](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) | Pendiente |
| 2 | [CON-02](03_PostgreSQL.md#con-02) | Implementar esquema PostgreSQL y migraciones | Sprint 3 - Bases e identidad | PostgreSQL | Alex | 5 | [CON-01](02_Java.md#con-01) · [ELA-07](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) | Pendiente |
| 3 | [CON-03](02_Java.md#con-03) | Implementar autenticación y permisos por rol | Sprint 3 - Bases e identidad | Java | Matías | 5 | [CON-01](02_Java.md#con-01) · [CON-02](03_PostgreSQL.md#con-02) | Pendiente |
| 4 | [CON-04](01_Flutter.md#con-04) | Crear estructura base de aplicación Flutter | Sprint 3 - Bases e identidad | Flutter | Abigal | 5 | [ELA-02](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) · [ELA-06](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) | Pendiente |
| 5 | [CON-05](02_Java.md#con-05) | Implementar administración de usuarios, roles y permisos | Sprint 3 - Bases e identidad | Java | Matías | 3 | [CON-03](02_Java.md#con-03) | Pendiente |
| 6 | [CON-06](02_Java.md#con-06) | Implementar registro y consulta de pacientes | Sprint 3 - Bases e identidad | Java | Alejandro | 5 | [CON-02](03_PostgreSQL.md#con-02) · [CON-03](02_Java.md#con-03) | Pendiente |
| 7 | [CON-07](01_Flutter.md#con-07) | Crear pantalla de inicio de sesión y sesión en Flutter | Sprint 3 - Bases e identidad | Flutter | Abigal | 3 | [CON-03](02_Java.md#con-03) · [CON-04](01_Flutter.md#con-04) | Pendiente |
| 8 | [CON-08](01_Flutter.md#con-08) | Crear pantallas Flutter de registro y consulta de pacientes | Sprint 3 - Bases e identidad | Flutter | Alex | 3 | [CON-04](01_Flutter.md#con-04) · [CON-06](02_Java.md#con-06) | Pendiente |
| 9 | [CON-09](05_Pruebas.md#con-09) | Configurar CI y pruebas base Java/Flutter | Sprint 3 - Bases e identidad | Pruebas | Benjamín | 3 | [CON-01](02_Java.md#con-01) · [CON-04](01_Flutter.md#con-04) | Pendiente |
| 10 | [CON-10](02_Java.md#con-10) | Implementar catálogos de médicos, especialidades, recursos y agendas | Sprint 4 - Gestion de citas | Java | Alex | 5 | [CON-02](03_PostgreSQL.md#con-02) · [CON-03](02_Java.md#con-03) | Pendiente |
| 11 | [CON-11](02_Java.md#con-11) | Implementar validación de disponibilidad y no doble reserva | Sprint 4 - Gestion de citas | Java | Benjamín | 5 | [CON-02](03_PostgreSQL.md#con-02) · [CON-10](02_Java.md#con-10) | Pendiente |
| 12 | [CON-12](02_Java.md#con-12) | Implementar motor de asignación automática íntegramente en Java | Sprint 4 - Gestion de citas | Java | Benjamín | 5 | [CON-11](02_Java.md#con-11) · [ELA-04](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) | Pendiente |
| 13 | [CON-13](02_Java.md#con-13) | Implementar solicitud y confirmación de citas con transacción | Sprint 4 - Gestion de citas | Java | Alejandro | 5 | [CON-06](02_Java.md#con-06) · [CON-12](02_Java.md#con-12) | Pendiente |
| 14 | [CON-14](02_Java.md#con-14) | Implementar consulta de citas del paciente | Sprint 4 - Gestion de citas | Java | Alejandro | 3 | [CON-13](02_Java.md#con-13) | Pendiente |
| 15 | [CON-15](02_Java.md#con-15) | Implementar consulta de citas para admisión | Sprint 4 - Gestion de citas | Java | Matías | 3 | [CON-13](02_Java.md#con-13) | Pendiente |
| 16 | [CON-16](02_Java.md#con-16) | Implementar reprogramación y cancelación de citas | Sprint 4 - Gestion de citas | Java | Matías | 5 | [CON-11](02_Java.md#con-11) · [CON-13](02_Java.md#con-13) | Pendiente |
| 17 | [CON-17](01_Flutter.md#con-17) | Diseñar e implementar pantallas paciente para solicitar y consultar citas | Sprint 4 - Gestion de citas | Flutter | Abigal | 5 | [CON-04](01_Flutter.md#con-04) · [CON-13](02_Java.md#con-13) · [CON-14](02_Java.md#con-14) | Pendiente |
| 18 | [CON-18](01_Flutter.md#con-18) | Implementar vistas de admisión para citas, reprogramación y cancelación | Sprint 4 - Gestion de citas | Flutter | Abigal | 5 | [CON-15](02_Java.md#con-15) · [CON-16](02_Java.md#con-16) | Pendiente |
| 19 | [CON-19](02_Java.md#con-19) | Implementar agenda médica y actualización de estado | Sprint 4 - Gestion de citas | Java | Alex | 5 | [CON-10](02_Java.md#con-10) · [CON-13](02_Java.md#con-13) | Pendiente |
| 20 | [CON-20](05_Pruebas.md#con-20) | Pruebas integradas de solicitud, asignación, reprogramación y cancelación | Sprint 4 - Gestion de citas | Pruebas | Benjamín | 5 | [CON-13](02_Java.md#con-13) · [CON-16](02_Java.md#con-16) | Pendiente |
| 21 | [CON-21](02_Java.md#con-21) | Implementar reasignación de cupo liberado en Java | Sprint 5 - Integracion y seguimiento | Java | Benjamín | 5 | [CON-16](02_Java.md#con-16) · [CON-20](05_Pruebas.md#con-20) · [ELA-04](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) | Pendiente |
| 22 | [CON-22](04_WebSocket.md#con-22) | Emitir eventos de citas con WebSocket desde Spring Boot | Sprint 5 - Integracion y seguimiento | WebSocket | Matías | 5 | [CON-13](02_Java.md#con-13) · [CON-16](02_Java.md#con-16) | Pendiente |
| 23 | [CON-23](01_Flutter.md#con-23) | Integrar notificaciones y refresco de citas en Flutter | Sprint 5 - Integracion y seguimiento | Flutter | Abigal | 5 | [CON-22](04_WebSocket.md#con-22) · [CON-17](01_Flutter.md#con-17) | Pendiente |
| 24 | [CON-24](02_Java.md#con-24) | Generar notificaciones y recordatorios de cita | Sprint 5 - Integracion y seguimiento | Java | Alejandro | 5 | [CON-13](02_Java.md#con-13) · [CON-22](04_WebSocket.md#con-22) | Pendiente |
| 25 | [CON-25](02_Java.md#con-25) | Implementar consulta permitida de paciente y registro de atención | Sprint 5 - Integracion y seguimiento | Java | Alex | 5 | [CON-03](02_Java.md#con-03) · [CON-19](02_Java.md#con-19) | Pendiente |
| 26 | [CON-26](02_Java.md#con-26) | Implementar historial de cambios de citas | Sprint 5 - Integracion y seguimiento | Java | Alejandro | 3 | [CON-16](02_Java.md#con-16) · [CON-19](02_Java.md#con-19) | Pendiente |
| 27 | [CON-27](02_Java.md#con-27) | Implementar reportes de citas e indicadores de gestión | Sprint 5 - Integracion y seguimiento | Java | Alex | 5 | [CON-13](02_Java.md#con-13) · [CON-19](02_Java.md#con-19) | Pendiente |
| 28 | [CON-28](01_Flutter.md#con-28) | Crear vistas del médico para agenda y registro de atención | Sprint 5 - Integracion y seguimiento | Flutter | Abigal | 5 | [CON-19](02_Java.md#con-19) · [CON-25](02_Java.md#con-25) | Pendiente |
| 29 | [CON-29](01_Flutter.md#con-29) | Crear vistas de administración para historial, reportes e indicadores | Sprint 5 - Integracion y seguimiento | Flutter | Abigal | 3 | [CON-26](02_Java.md#con-26) · [CON-27](02_Java.md#con-27) | Pendiente |
| 30 | [CON-30](06_Servidor.md#con-30) | Desplegar ambiente de integración de Java, Flutter y PostgreSQL | Sprint 5 - Integracion y seguimiento | Servidor | Matías | 3 | [CON-09](05_Pruebas.md#con-09) · [CON-22](04_WebSocket.md#con-22) | Pendiente |
| 31 | [CON-31](05_Pruebas.md#con-31) | Ejecutar pruebas extremo a extremo de los veinte casos de uso | Sprint 6 - Validacion y entrega | Pruebas | Benjamín | 5 | [CON-20](05_Pruebas.md#con-20) · [CON-23](01_Flutter.md#con-23) · [CON-24](02_Java.md#con-24) · [CON-25](02_Java.md#con-25) · [CON-29](01_Flutter.md#con-29) · [CON-30](06_Servidor.md#con-30) | Pendiente |
| 32 | [CON-32](05_Pruebas.md#con-32) | Medir concurrencia, rendimiento y robustez del núcleo Java | Sprint 6 - Validacion y entrega | Pruebas | Alex | 5 | [CON-30](06_Servidor.md#con-30) | Pendiente |
| 33 | [CON-33](05_Pruebas.md#con-33) | Auditar permisos, privacidad y configuración de seguridad | Sprint 6 - Validacion y entrega | Pruebas | Matías | 5 | [CON-03](02_Java.md#con-03) · [CON-25](02_Java.md#con-25) · [CON-30](06_Servidor.md#con-30) | Pendiente |
| 34 | [CON-34](01_Flutter.md#con-34) | Ajustar validaciones, accesibilidad y estabilidad de Flutter | Sprint 6 - Validacion y entrega | Flutter | Abigal | 3 | [CON-23](01_Flutter.md#con-23) · [CON-28](01_Flutter.md#con-28) · [CON-29](01_Flutter.md#con-29) | Pendiente |
| 35 | [CON-35](06_Servidor.md#con-35) | Probar respaldo, restauración y recuperación del sistema | Sprint 6 - Validacion y entrega | Servidor | Alex | 3 | [CON-30](06_Servidor.md#con-30) | Pendiente |
| 36 | [CON-36](05_Pruebas.md#con-36) | Cerrar trazabilidad, checklist y versión de entrega | Sprint 6 - Validacion y entrega | Pruebas | Alejandro | 3 | [CON-31](05_Pruebas.md#con-31) · [CON-32](05_Pruebas.md#con-32) · [CON-33](05_Pruebas.md#con-33) · [CON-34](01_Flutter.md#con-34) · [CON-35](06_Servidor.md#con-35) | Pendiente |

## Pasos para crear en GitHub (manual, sin `gh`)

1. Revisar Issues existentes y numeraciones históricas `T-XX`, `HU-XX`, `CP-XX` de CNS; evitar duplicados.
2. Confirmar Milestones exactos del [plan](../PLAN_MILESTONES.md): **Sprint 3 - Bases e identidad**, **Sprint 4 - Gestion de citas**, **Sprint 5 - Integracion y seguimiento**, **Sprint 6 - Validacion y entrega**.
3. Crear las etiquetas listadas en el cuerpo de cada issue si no existen. No crear `tec:cpp` (CNS tiene núcleo Java).
4. Seguir el orden de la tabla; copiar el encabezado del issue como título y el contenido hasta el próximo separador como descripción.
5. En GitHub, asignar responsable, etiquetas y Milestone, y agregar vínculos a los `#` de las dependencias una vez creadas.
6. Completar referencias `#`, ramas y evidencia en [TRAZABILIDAD.md](../TRAZABILIDAD.md). Comprobar que el caso de uso conserva la relación RF/HU.
7. Las ramas nuevas parten siempre de `develop`; abrir PR contra `develop` y cerrar manualmente los issues cuando el PR se haya fusionado, según la configuración del repositorio.

**Nota:** el issue [CON-21](02_Java.md#con-21) es condicional y no está en el camino obligatorio de ninguna otra entrega. [D-01](10_Decisiones_pendientes.md#d-01).
