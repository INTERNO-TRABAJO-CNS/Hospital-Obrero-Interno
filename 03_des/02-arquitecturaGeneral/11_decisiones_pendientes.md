# ARQ-11 — Registro de decisiones, diferencias y riesgos

## Confirmado por el equipo el 26/10/2026
| ID | Decisión | Impacto |
|---|---|---|
| DC-01 | Sprint 2 diseña MVC; sin código de producción | Todos |
| DC-02 | Java Spring Boot realiza toda la lógica y asignación; sin C++ | M03/M04 |
| DC-03 | Flutter/Dart, PostgreSQL, REST y WebSocket son tecnologías del proyecto | Capas |
| DC-04 | Elegir **fecha y hora compatibles más próximas**; pasar a siguientes días disponibles/hábiles | M02/M03 |
| DC-05 | Estado persistido de cita: programada, atendida, cancelada | M03/M04 |
| DC-06 | Número de asegurado CNS indispensable; CI solo identificación secundaria | M01 |
| DC-07 | Paciente puede crear cuenta propia | M01/UI |
| DC-08 | Secretaría/enfermería verifica llegada y puede cancelar por inasistencia pasados 10 minutos | M04 |
| DC-09 | Administrador supervisa citas; no administra agendas por defecto | M01/M05 |
| DC-10 | Cinco modelos, uno por persona; **Benjamín** integra y revisa | Sprint 2 |
| DC-11 | La reasignación está **decidida como intención de alcance**, pero su aprobación documental/reglas permanecen pendientes | Control de cambios |
| DC-12 | `develop` es base de features e integración mediante PR | Git |

## Pendiente de validación (NO decidir arbitrariamente)
| ID | Pregunta sin cerrar | Bloquea |
|---|---|---|
| PV-01 | ¿Qué roles utilizarán exactamente Flutter y cuáles otro canal? Respuesta de punto 16 contradictoria. | UI/plataforma |
| PV-02 | ¿Quién crea/modifica agendas, turnos, consultorios y feriados? Enfermería tiene confirmada **llegada**, no alta de agenda. | M02 |
| PV-03 | ¿Cuál es duración por cita y ventana máxima de búsqueda hacia días futuros? | M02/M03 |
| PV-04 | ¿Qué pasa con múltiples médicos/recursos que tienen misma hora más cercana? | M03 |
| PV-05 | ¿Qué significa «día hábil» (fin de semana, feriados y funcionamiento del hospital)? | M02 |
| PV-06 | ¿Se permite al paciente cancelar directamente en Flutter? | M04/UI |
| PV-07 | ¿Cómo valida el sistema el número de afiliado sin integración CNS confirmada? | M01 |
| PV-08 | ¿La cita solo puede pasar de programada a atendida/cancelada? ¿Hay reversión autorizada? | M04 |
| PV-09 | ¿Se registra la llegada antes de la hora programada? ¿Exactamente cuándo vence +10min? ¿Tiempo de espera vs tolerancia? | M04 |
| PV-10 | ¿Cuál es el ERD/diagrama de clases REAL y cuáles sus nombres exactos? Ya diseñado, pero no adjunto en el material disponible. | Integración |
| PV-11 | ¿Qué esquema de credenciales/auth y despliegue/hosting usarán? | Seguridad |
| PV-12 | Para reasignación: prioridad, vigencia oferta, rango de fechas, reglas de elegibilidad y rechazos, formalización de cambio a RU/RF/HU/CU | Futuro cambio |

## Tensiones documentales (no borrar silenciosamente)
- RF-12 original lista «inexistente» y menciona inasistencia; equipo define ahora **tres estados**. Tramitar ajuste controlado de trazabilidad, manteniendo los CU originales intactos por ahora.
- RF-03/CU-03 asignan registro de paciente a Enfermería; la decisión de **autoregistro** necesita trazabilidad/cambio de actor, sin reenumerar RF/CU.
- Documento de visión prevé mecanismos institucionales por definir; el equipo eligió PostgreSQL posteriormente: actualizar versión de arquitectura, no alterar la versión histórica del documento.
- Reasignación: aparece investigación #10 cerrada, pero **no equivale a aprobación formal del cambio**. Evitar duplicar esa investigación.
- Capturas muestran Issues de backlog **#28 y #29 cerrados**; no abrir duplicados. Se desconoce cobertura de otros issues cerrados y su PR.

## Registro de resolución
Al validar un PV, añadir: fecha, responsable, evidencia, RF/CU impactado, enlace a issue/PR real y nuevo estado. **No generar IDs reales de GitHub por suposición.**
