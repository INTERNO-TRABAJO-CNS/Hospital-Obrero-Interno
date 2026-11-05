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

## Decisiones nuevas del 04/11/2026 (equipo)
| ID | Decisión | Reemplaza / impacto |
|---|---|---|
| DC-13 | El paciente elige especialidad y día; el sistema asigna médico, hora y consultorio | Ajusta DC-04 / CU-04 |
| DC-14 | Cola por orden de llegada; quien cancela o reprograma vuelve con prioridad menor | CU-04/05/10 |
| DC-15 | NO HAY CUPOS → EN_ESPERA al siguiente día hábil, hasta 15 días hábiles (luego SIN_CUPO) | Cierra PV-03; ajusta DC-04 |
| DC-16 | Estados de cita: PROGRAMADA, ASISTENCIA_CONFIRMADA, ATENDIDA, CANCELADA | **Reemplaza DC-05** |
| DC-17 | Confirmación por enfermería hasta T-30; cancelación automática a T-30; aviso a T-45; la cita no se borra | **Reemplaza DC-08** (10 min); cierra PV-09 |
| DC-18 | Máx. 7 fichas/médico/día, meta mínima 5; una cita por paciente por día | Cierra parte de PV-03 |
| DC-19 | Duración: General 30–45, Odontología 45–60, Pediatría 30–45 min | Cierra PV-03 |
| DC-20 | Solo el titular crea cuenta (valida tabla interna de afiliados); beneficiarios por afiliación del titular + CI | Cierra PV-07; ajusta DC-06/DC-07 |
| DC-21 | El administrador gestiona catálogos (CRUD) y ve agenda de supervisión de solo lectura en tiempo real | **Reemplaza DC-09** (no administrar agendas); cierra PV-02 |
| DC-22 | El médico solo registra el fin y la receta; consulta solo pacientes de su agenda | CU-13/14/15 |
| DC-23 | Reprogramar = cancelar + nueva solicitud con prioridad menor | CU-10 |
| DC-24 | El cupo liberado NO se reasigna automáticamente por ahora | Difiere DC-11 |
| DC-25 | TV de solo lectura: listado de fichas del día | CU-09 |
| DC-26 | App multiplataforma (celular, PC, TV) con Flutter; despliegue en AWS EC2 | PV-01, PV-11 |
| DC-27 | Concurrencia con PostgreSQL (`FOR UPDATE SKIP LOCKED` + `UNIQUE`) | RNF-04 |
| DC-28 | Rama de integración `feature/T100-algoritmos-ejecucion`; solo Benjamín integra en `develop`, `main` y T100 | Git; ajusta DC-12 |
| DC-29 | Sprints: S1 1–22 oct; S2 23–29 oct; S3 30 oct–5 nov; S4 6–12 nov; S5 13–20 nov; S6 21–28 nov | Plan |

## Pendiente de validación (NO decidir arbitrariamente)
> Actualizado 04/11/2026: PV-02, PV-03, PV-07, PV-09 quedan **resueltos** por DC-15…DC-21; PV-06 queda resuelto (el paciente cancela en la app, antes de ASISTENCIA_CONFIRMADA — supuesto por confirmar); PV-08 queda reemplazado por DC-16.

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

## Pendientes nuevos (04/11/2026)
| ID | Pregunta | Bloquea |
|---|---|---|
| PV-13 | Desempate entre dos médicos con la misma primera hora | CU-05 |
| PV-14 | Numeración de ficha (propuesta: correlativo por médico y día) | CU-07 |
| PV-15 | ¿El paciente puede cancelar tras ASISTENCIA_CONFIRMADA? (supuesto: no) | CU-11 |
| PV-16 | Autenticación de la pantalla TV | CU-09 |
| PV-17 | Tiempo de espera y de atención por separado (no medible hoy) | CU-19/20 |
| PV-18 | Usuario y contraseña por enfermera: se hará más adelante | CU-01/12 |

## Tensiones documentales (no borrar silenciosamente)
- RF-12 original lista «inexistente» y menciona inasistencia; equipo define ahora **tres estados**. Tramitar ajuste controlado de trazabilidad, manteniendo los CU originales intactos por ahora.
- RF-03/CU-03 asignan registro de paciente a Enfermería; la decisión de **autoregistro** necesita trazabilidad/cambio de actor, sin reenumerar RF/CU.
- Documento de visión prevé mecanismos institucionales por definir; el equipo eligió PostgreSQL posteriormente: actualizar versión de arquitectura, no alterar la versión histórica del documento.
- Reasignación: aparece investigación #10 cerrada, pero **no equivale a aprobación formal del cambio**. Evitar duplicar esa investigación.
- Capturas muestran Issues de backlog **#28 y #29 cerrados**; no abrir duplicados. Se desconoce cobertura de otros issues cerrados y su PR.

## Registro de resolución
Al validar un PV, añadir: fecha, responsable, evidencia, RF/CU impactado, enlace a issue/PR real y nuevo estado. **No generar IDs reales de GitHub por suposición.**
