# MDL-03 — Modelo central de solicitudes y asignación de citas

> **Proyecto:** Sistema de Gestión y Control de Citas Médicas — Hospital Obrero CNS  
> **Responsable:** Benjamín  
> **Issue propuesta:** `[S2-M03][Arquitectura MVC] Modelo central Java de solicitudes y asignación`  
> **Fase / milestone:** Elaboración — Sprint 2, Arquitectura MVC (29/10/2026)  
> **Rama prevista:** `feature/S2-M03-nucleo-citas` (desde `develop`)  
> **Estado:** Diseño elaborado para revisión cruzada; **no equivale a aprobación institucional ni implementación**.  
> **Tecnologías previstas:** Java / Spring Boot, PostgreSQL; frontend Flutter; arquitectura MVC.  
> **Alcance:** diseño del **Modelo**; no contiene código de producción, endpoints ni DDL.

## 1. Fuentes revisadas y criterio de interpretación

1. `Especificacion_de_Casos_de_Uso_CNS(1)(2).md` (Sprint 1), apartados **CU-04, CU-05, CU-06 y CU-07**, incluidos escenarios alternativos; la especificación indica **estado de aprobación: borrador**.
2. `Matriz_de_Requisitos_y_Trazabilidad_CNS(1)(2).md` (Sprint 1), **RF/HU-04, 05, 06 y 07; RNF-03, 04 y 06**; también RF-12, RF-16 y RF-18 para integración. La matriz señala requisitos por validar.
3. Borradores compartidos de Sprint 2: `MDL-01_identidad_pacientes.md` (Alejandro), `MDL-02_recursos_agenda.md` (Alex), `MDL-03_solicitud_asignacion_cita.md` (Benjamín), `MDL-04_operaciones_historial.md` (Matías) y `MDL-05_consultas_comunicacion_reportes.md` (Abigal).

**Criterio:** se conserva lo descrito en Sprint 1. Los borradores MDL sirven para coordinar interfaces, pero las decisiones no aprobadas se marcan **PROPUESTA** o **PENDIENTE**. No se han modificado los CU, RF, HU ni RNF originales. No se ha cotejado este modelo con el DER final aprobado: la correspondencia exacta de nombres, tipos y cardinalidades físicas queda pendiente.

## 2. Objetivo, alcance y frontera del módulo

### 2.1 Objetivo

Diseñar el núcleo del negocio en Java para que el sistema reciba una solicitud por **especialidad**, obtenga una combinación válida de recursos médicos, revalide su disponibilidad y registre **una cita confirmada sin conflictos**, notificando la confirmación solo después de una transacción exitosa.

### 2.2 Dentro de M03

- **Propiedad exclusiva del dominio y la persistencia lógica** de `SolicitudCita` y `CitaMedica` (sin duplicaciones en M02, M04 ni M05).
- Orquestación del proceso CU-04 → CU-05 → CU-06 (servicio de M02) → CU-07.
- Reglas de validación de solicitud, coordinación de búsqueda y confirmación transaccional.
- Contratos de consulta y cambios atómicos para consumidores autorizados.
- Publicación conceptual del **hecho de cita confirmada**, posterior al *commit*, para que M05 genere la notificación.

### 2.3 Fuera de M03

| Función / datos | Responsable | Límite explícito |
| --- | --- | --- |
| `Persona`, `Paciente`, `Usuario`, `Rol`, `Permiso` | **M01 — Alejandro** | M03 recibe una referencia válida y consulta autorización; no administra afiliaciones ni credenciales. |
| `Medico`, `Especialidad`, `Consultorio`, `AgendaMedica`, `FranjaDisponible` | **M02 — Alex** | M02 localiza/valida combinaciones; **no confirma ni crea `CitaMedica`**. |
| `RegistroLlegada`, `CambioCita`, `DatosAtencion`, historial | **M04 — Matías** | M04 ejecuta su lógica operativa y pide que M03 aplique el cambio sobre `CitaMedica`, con nueva validación de M02 si corresponde. |
| `Notificacion`, `Recordatorio`, consultas y reportes | **M05 — Abigal** | M05 consume el evento confirmado y usa lecturas autorizadas; no publica resultados de solicitudes no confirmadas. |
| Formulario / controlador HTTP REST, pantallas Flutter y WebSocket | Vista/controlador e integración | Son canales de entrada/salida; **no deciden el recurso ni sustituyen las invariantes del Modelo**. |

## 3. Trazabilidad funcional

| Paso | Fuente Sprint 1 | Regla que debe cumplir M03 | Verificación de diseño |
| --- | --- | --- | --- |
| Registrar solicitud | **CU-04 / RF-04 / HU-04** | Paciente autenticado/habilitado solicita por especialidad; **no** selecciona médico, fecha, hora ni consultorio. | Solicitud válida queda registrada; especialidad inválida no permite confirmar. |
| Proponer asignación | **CU-05 / RF-05 / HU-05** | Selección automática de combinación compatible. | Sin recursos no se confirma cita; recurso perdido se descarta. |
| Validar combinación | **CU-06 / RF-06 / HU-06**, provistos por M02 | Médico/horario/consultorio vigentes y sin solapamiento. | Una combinación ocupada se rechaza. |
| Registrar confirmación | **CU-07 / RF-07 / HU-07** | Almacenar paciente, especialidad, médico, fecha, hora, consultorio y estado inicial. | Ante datos insuficientes o conflicto, no persistir cita inválida. |
| Consistencia | **RNF-03** | Usar disponibilidad vigente al confirmar. | No confiar en una disponibilidad obtenida antes de un cambio. |
| Integridad concurrente | **RNF-04** | No confirmar el mismo recurso incompatible en dos solicitudes simultáneas. | Último cupo: una confirmación, la otra no. |
| Escalabilidad de reglas | **RNF-06** | Aumentar volumen sin alterar reglas centrales. | El criterio de validez es idéntico con varias solicitudes simultáneas. |
| Eventos tras confirmar | **RF-16 / HU-16; CU-16** | Comunicar el hecho de confirmación a M05. | Ninguna notificación de cita confirmada si hubo rollback. |

**Nota sobre roles:** CU-07 menciona `Sistema / Personal de Enfermería-Admisión` y señala que el personal confirma **cuando corresponda**. No se afirma que todo caso requiera aprobación manual ni que todo registro la omita; esa regla de intervención queda pendiente de concretar entre M01/M03 y los CU de Sprint 1.

## 4. Modelo de dominio propuesto

### 4.1 Entidad propia: `SolicitudCita`

Representa **la intención registrada de recibir atención**. Su existencia **no reserva un recurso** y **no equivale a una `CitaMedica` confirmada**.

| Atributo conceptual | Tipo conceptual | Uso | Respaldo / observación |
| --- | --- | --- | --- |
| `solicitudId` | Identificador | Identificar de forma única la solicitud. | **PROPUESTA técnica** necesaria para trazabilidad; tipo concreto por definir. |
| `pacienteId` | Referencia a M01 | Paciente que solicita. | Requerido por CU-04 y contrato M01. No duplicar ficha del paciente. |
| `especialidadId` | Referencia a M02 | Especialidad o atención solicitada. | Requerido por CU-04. |
| `fechaSolicitud` | Fecha/hora | Permitir registro temporal del evento. | **PROPUESTA técnica** para seguimiento; precisión/tipo por definir. |

**No se define por ahora** un `estadoSolicitud` persistido, una prioridad, una posición en cola ni un orden de desempate: **ninguno está aprobado en las fuentes**. La distinción entre solicitud registrada, candidata validada y cita confirmada se expresa como **resultado de operaciones**, no como catálogo inventado de estados. Si se necesitan estados de proceso para trazabilidad o reintentos, abrir validación/cambio antes de implementarlos.

### 4.2 Entidad propia: `CitaMedica`

Representa **una asignación efectiva que superó validaciones y fue confirmada**. Es la fuente autoritativa de la cita para los demás módulos.

| Atributo conceptual | Tipo conceptual | Uso | Respaldo / observación |
| --- | --- | --- | --- |
| `citaId` | Identificador | Identificar una cita confirmada. | Identificador técnico propuesto; formato pendiente. |
| `pacienteId` | Referencia a M01 | Titular de la cita. | CU-07 / RF-07. |
| `especialidadId` | Referencia a M02 | Especialidad asignada. | CU-07 / RF-07. |
| `medicoId` | Referencia a M02 | Médico asignado. | CU-07 / RF-07. |
| `agendaId` | Referencia a M02 | Agenda bajo la que se habilitó el turno. | **PROPUESTA de integración** con M02; verificar DER. |
| `consultorioId` | Referencia a M02 | Consultorio asignado. | CU-07 / RF-07. |
| `fecha`, `hora` | Fecha / hora | Momento de la cita. | CU-07 / RF-07. |
| `franjaAsignada` | Intervalo o referencia a `FranjaDisponible` | Permite estudiar superposición de recursos. | **PROPUESTA conceptual**: M02 debe pactar inicio/fin o duración; no se fija duración arbitraria. |
| `estado` | Estado de cita | Inicialmente `PROGRAMADA`. | CU-07 y MDL-04 proponen `PROGRAMADA`; ciclo posterior en M04. |
| `solicitudId` | Referencia a solicitud | Rastrear solicitud origen. | **PROPUESTA de trazabilidad**; cardinalidad/obligatoriedad pendiente por atención registrada por personal. |

No se incorporan `Consulta`, `Receta` ni `Evaluacion_Medica` clínica en este alcance. Los tiempos y datos operativos de atención corresponden a M04.

### 4.3 Referencias, relaciones y cardinalidades

| Relación conceptual | Cardinalidad utilizable en análisis | Fundamento / reserva |
| --- | --- | --- |
| `Paciente` → `SolicitudCita` | Un paciente puede originar múltiples solicitudes; cada solicitud pertenece a **un** paciente. | Deriva de la autoría de la solicitud; cotejar con DER físico de M01. |
| `Especialidad` → `SolicitudCita` | Una especialidad se puede solicitar múltiples veces; cada solicitud especifica **una** especialidad. | CU-04. |
| `SolicitudCita` → `CitaMedica` | **PROPUESTA:** una solicitud origina **0..1** cita confirmada. | No hay cita si no existe disponibilidad; verificar tratamiento de reprocesos, citas presenciales y cardinalidad en DER. |
| `Paciente` → `CitaMedica` | Cada cita pertenece a **un** paciente. | CU-07. |
| `Medico` / `Consultorio` / `Especialidad` → `CitaMedica` | Cada cita confirmada hace referencia a **uno** de cada uno. | CU-07. |
| `AgendaMedica` / `FranjaDisponible` → `CitaMedica` | Referencia técnica **por pactar con M02**. | La fuente exige horario válido, no fija aún FKs concretas. |

**Fuente autoritativa:** relaciones M01/M02 son **referencias** de M03, no entidades clonadas. El archivo UML muestra las asociaciones como vista de integración y marca las asociaciones pendientes.

### 4.4 Diferencia central: solicitud vs cita

| Característica | `SolicitudCita` | `CitaMedica` |
| --- | --- | --- |
| Cuándo existe | Al guardar una solicitud válida del paciente. | Únicamente tras validar y confirmar recursos. |
| Datos indispensables | Paciente y especialidad. | Paciente, especialidad, médico, fecha, hora, consultorio, estado. |
| ¿Reserva médico/consultorio? | **No.** | **Sí**, bajo las reglas de no solapamiento. |
| Resultado sin disponibilidad | La solicitud puede estar registrada; se informa que **no hay cita confirmada**. | **No se crea.** |
| ¿Se notifica como cita creada? | **No.** | Solo después del *commit* y vía M05. |

## 5. Reglas e invariantes del dominio

1. **R-M03-01 — Autorización.** M03 exige sesión y permiso válido en M01 para solicitar en nombre del `pacienteId`; no confía solo en lo enviado por Flutter o REST.
2. **R-M03-02 — Única selección del paciente.** CU-04 recibe la especialidad/atención; no acepta como criterio elegido por el paciente médico, fecha, hora ni consultorio.
3. **R-M03-03 — Solicitud no reserva.** Registrar una `SolicitudCita` no ocupa franja ni crea cita.
4. **R-M03-04 — Compatibilidad.** Una candidata debe responder a la especialidad solicitada y a recursos habilitados según M02 (agenda, médico, consultorio, fecha/hora).
5. **R-M03-05 — Disponibilidad vigente.** Antes de confirmar, M03 solicita a M02 nueva validación de disponibilidad, sin confiar en una comprobación anterior.
6. **R-M03-06 — No solapamiento.** Dos citas vigentes no pueden utilizar simultáneamente el mismo médico u otro recurso exclusivo incompatible; la regla de ocupación exacta y estados que liberan cupos se pacta con M02/M04.
7. **R-M03-07 — Atomicidad.** Confirmar disponibilidad y persistir `CitaMedica` es una sola unidad lógica transaccional; ante conflicto/rollback, no se crea una cita confirmada.
8. **R-M03-08 — Datos completos.** No se persiste `CitaMedica` si faltan paciente, especialidad, médico, fecha/hora o consultorio.
9. **R-M03-09 — Notificación posterior.** M05 solo recibe un hecho de **cita confirmada** después de transacción exitosa; ningún evento de confirmación ante rollback.
10. **R-M03-10 — Cambios posteriores controlados.** Reprogramaciones/cancelaciones autorizadas pasan por un contrato transaccional M03, con lógica de proceso de M04; no hay un segundo repositorio de `CitaMedica`.

## 6. Flujo integrado CU-04 → CU-05 → CU-06 → CU-07

### 6.1 Camino principal

1. **Entrada (CU-04).** Flutter envía al controlador REST la especialidad requerida. El backend resuelve el paciente/actor autenticado por M01; **no** toma como válida una identidad o elección de recurso arbitraria del cliente.
2. **Validación y registro de solicitud.** M03 verifica habilitación/autorización mediante M01, verifica la especialidad mediante referencia M02 y registra `SolicitudCita`.
3. **Búsqueda (CU-05).** M03 invoca a M02 para obtener una combinación candidata compatible de **médico + agenda/franja + consultorio**, según disponibilidad actual.
4. **Validación (CU-06, responsabilidad M02).** Se comprueba especialidad, horario, médico, consultorio y ausencia de solapamientos; la candidata se devuelve como **preliminar**, no como cupo reservado irrevocablemente.
5. **Confirmación (CU-07).** M03 vuelve a validar la candidata **en la misma operación de confirmación** y realiza registro atómico bajo protección contra colisiones en PostgreSQL. Inicializa la cita como `PROGRAMADA`.
6. **Resultado.** Tras *commit*, M03 devuelve los datos confirmados. Publica el evento conceptual `CitaConfirmada` hacia M05 para notificación (CU-16). La lectura posterior y agenda consumen esta `CitaMedica` autoritativa.

### 6.2 Alternativas y fallos documentados

| Origen | Condición | Comportamiento exigido | ¿Crear `CitaMedica`? |
| --- | --- | --- | --- |
| CU-04, paso 2a | Especialidad omitida/inválida | Rechazar avance y solicitar especialidad válida; no generar asignación. | **No** |
| CU-04 / M01 | Paciente sin autorización o habilitación | No aceptar solicitud de cita a nombre ajeno. | **No** |
| CU-05, paso 2a | Ningún recurso compatible | Informar falta de disponibilidad; **sin prioridad, lista de espera ni cola inventadas**. | **No** |
| CU-05, paso 3a | Recurso deja de estar disponible | Descartar candidata y evaluar otra compatible de acuerdo con M02. | **No** para la candidata ocupada |
| CU-06 | Médico/consultorio/horario solapado | Marcar candidata no válida y no confirmar. | **No** |
| CU-07, paso 2a | Disponibilidad cambió antes de guardar | Revalidar y regresar a búsqueda; no ocupar recurso en conflicto. | **No** para la candidata fallida |
| CU-07, paso 3a | Faltan datos necesarios | Rechazar confirmación hasta contar con los datos obligatorios. | **No** |
| RNF-04 | Dos solicitudes compiten por el último cupo | Confirmación transaccional garantiza **solo un ganador**. La otra evalúa otra candidata o termina sin cita. | **Una como máximo** |
| Transacción | Fallo de persistencia/rollback | No ofrecer éxito, ni producir evento de cita confirmada. | **No** |

**Qué no está aprobado:** cantidad máxima de búsquedas, horizonte de fechas, duración de turnos, calendario de feriados, orden de desempate entre médicos/horarios, colas y políticas especiales. El borrador M02 sugiere avanzar al siguiente día hábil y el borrador M03 menciona proximidad de fecha/hora; estas son **hipótesis de diseño a validar**, no una prioridad clínica confirmada por Sprint 1.

### 6.3 Diagrama de comportamiento

Ver [`diagramas/MDL-03_flujo_solicitud_asignacion.puml`](diagramas/MDL-03_flujo_solicitud_asignacion.puml). El diagrama representa los caminos de solicitud inválida, falta de recursos, conflicto concurrente, reintento con otra candidata, confirmación y evento posterior al *commit*.

## 7. Confirmación transaccional, concurrencia e integridad

### 7.1 Responsabilidad de Java y PostgreSQL

- **Modelo Java / servicio de dominio:** valida reglas y autorizaciones, coordina selección con M02, solicita revalidación, abre una unidad transaccional y decide si la operación terminó con cita o sin cita.
- **Persistencia PostgreSQL:** debe proveer una **protección efectiva ante confirmaciones concurrentes** (estrategia a decidir entre el equipo: bloqueo transaccional sobre cupo/franja, protección por exclusión de intervalos o mecanismo equivalente). **Una consulta `SELECT disponible` seguida de un `INSERT` sin protección no basta**.
- **Integración:** la decisión de compatibilidad (M02) y la reserva definitiva (M03) necesitan una estrategia común que evite el lapso entre «estaba libre» y «se guardó». Para intervalos variables debe protegerse el **solapamiento**, no solo la igualdad exacta de hora inicial.
- **Fallo:** si otra transacción confirma primero, la confirmación perdedora hace rollback o rechaza la candidata. Nunca devuelve una cita ficticia.
- **Evento:** el hecho de confirmación se entrega solo después de confirmar la transacción. **PROPUESTA de confiabilidad:** registrar un evento pendiente de entrega en la misma transacción (*outbox*) y publicarlo después; se define en integración, sin imponer tablas ahora.
- **M05:** debe hacer idempotente el procesamiento para no duplicar notificaciones ante entregas repetidas; M03 no posee la entidad `Notificacion`.

**Pendiente técnico:** definir con M02 el identificador único de la franja y sus límites temporales; con M04, estados que bloquean/liberan ocupación; y con responsables de datos, el mecanismo PostgreSQL elegido. No se implementan sentencias SQL en este Sprint.

### 7.2 Caso de concurrencia: último cupo

| Momento | Solicitud A | Solicitud B | Garantía |
| --- | --- | --- | --- |
| 1 | Recibe candidata X | Recibe candidata X | Una candidata **no es confirmación**. |
| 2 | Entra a confirmación protegida | Intenta confirmación protegida | Ambas revalidan bajo el mecanismo acordado. |
| 3 | Confirma X y hace *commit* | Encuentra X ya ocupada / conflicto | **A = 1 cita; B = 0 citas para X**. |
| 4 | Se genera evento de A | Busca otra candidata o informa que no hay cupos | Nunca existen dos citas incompatibles para X. |

El orden de «quién gana» depende de la ejecución transaccional, **no** de una política de prioridad inventada. El modelo no garantiza equidad entre solicitudes concurrentes sin una regla institucional adicional.

## 8. Estados y transiciones: no agregar categorías no confirmadas

- **Estado inicial de una cita confirmada:** `PROGRAMADA`, según CU-07 y borrador M04.
- **Transiciones posteriores reflejadas en el borrador M04:** `PROGRAMADA → ATENDIDA`, `PROGRAMADA → CANCELADA`; la reprogramación mantiene `PROGRAMADA` modificando recursos de manera validada.
- **La solicitud no tiene un catálogo de estados persistentes confirmado.** «Registrada», «candidata», «confirmada» son hitos del flujo, no valores de un campo `estadoSolicitud`.
- **Aclaración de Sprint 1:** RF-12 enumera «programada, atendida, cancelada e inexistente» y su criterio menciona **inasistencia**. «Inexistente» no se modela automáticamente como fila/estado persistido, y «inasistencia» **no se agrega como cuarto estado** por decisión del borrador M04; esta diferencia debe resolverse por revisión del equipo antes de cerrar el catálogo.
- **Reasignación de cupos liberados:** no forma parte de M03 actual. La propuesta de adelantos y ofertas tras cancelación/inasistencia está pendiente de control formal de cambios; no incluir «buzón de adelantos» como comportamiento vigente.

## 9. Contratos conceptuales entre módulos (no firmas Java definitivas)

| Contrato propuesto | Emisor → Receptor | Entradas conceptuales | Salida / garantía |
| --- | --- | --- | --- |
| `verificarPacienteAutorizado` | M03 → M01 | Actor autenticado, `pacienteId`, acción | Autorizado / denegado; nunca confiar en rol solo por UI. |
| `verificarEspecialidad` | M03 → M02 | `especialidadId` | Especialidad válida / inválida. |
| `buscarCandidataCompatible` | M03 → M02 | Especialidad y reglas aprobadas | Candidata (médico, agenda/franja, consultorio, fecha/hora) o sin disponibilidad. |
| `validarDisponibilidadVigente` | M03 → M02 | Candidata y contexto | Compatible / no compatible; **no reserva por sí mismo**. |
| `solicitar` | REST/integración → M03 | Actor/paciente autorizado + `especialidadId` | Solicitud registrada o error de validación/autorización. |
| `asignarProxima` | Orquestador M03 → dominio M03/M02 | `solicitudId` | Candidata validable o sin disponibilidad. Nombre **propuesto**: no presupone prioridad aprobada. |
| `confirmarAtomico` | Orquestador → M03 | Solicitud + candidata, actor habilitado | `CitaMedica(PROGRAMADA)` **solo tras commit**, o conflicto/error. |
| `obtenerCita` | M04/M05/REST → M03 | `citaId`, contexto/autorización | Cita o denegación; M05 no accede a datos ajenos. |
| `aplicarCambioAtomico` | M04 → M03 | Cita, operación validada, actor, recursos propuestos si aplica | Cambio persistido sin conflicto o rechazo; historial corresponde a M04. |
| `CitaConfirmada` (evento) | M03 → M05 | Identificador de cita confirmada y referencia segura | M05 crea notificación después del *commit*. Datos personales minimizados. |

**Convención de resultado** (no enum definitivo): `solicitud registrada`, `cita confirmada`, `sin disponibilidad`, `datos inválidos`, `no autorizado`, `conflicto concurrente` o `fallo transaccional`. El formato REST exacto y el catálogo técnico de errores se acuerdan al pasar a construcción.

## 10. Arquitectura MVC y flujo de dependencias

- **Vista:** Flutter presenta especialidades disponibles, inicia solicitud y muestra respuesta/consulta. No permite seleccionar recursos del CU-04.
- **Controlador:** capa REST autentica/encamina peticiones y devuelve respuestas; no realiza selección ni define reglas de prioridad.
- **Modelo:** orquestación de M03 + reglas de dominio + referencias M01/M02 + repositorios de `SolicitudCita`/`CitaMedica` + coordinación transaccional.
- **Comunicación:** M05 se responsabiliza de notificaciones/WebSocket; la UI consulta el estado autoritativo por REST si pierde una actualización.

Ver [`diagramas/MDL-03_solicitud_asignacion_cita.puml`](diagramas/MDL-03_solicitud_asignacion_cita.puml).

## 11. Pruebas de diseño y resultados esperados

| ID | Preparación | Ejecución | Resultado verificable |
| --- | --- | --- | --- |
| **A — sin consultorio** | Médico/hora disponibles, consultorio ocupado. | Solicitar la especialidad. | M02 no valida combinación; **no existe nueva cita confirmada**. |
| **B — último cupo** | Única franja con recursos compatibles. | Solicitudes A y B concurrentes. | **Exactamente una** confirmación para ese cupo; otra busca alternativa o termina sin cita. |
| C — no hay ningún recurso | Agenda sin candidatos compatibles. | Solicitud válida. | Se distingue la solicitud registrada del hecho de **no haber cita**; no se crea cola. |
| D — especialidad inválida | Id especialidad ausente/inválido. | Enviar solicitud. | Rechazo de validación; sin cita. |
| E — cambio entre lectura/commit | Candidata que otro proceso ocupa antes de guardar. | Confirmar. | Revalidación rechaza conflicto; puede evaluar siguiente candidata. |
| F — datos incompletos | Falta médico, hora o consultorio al confirmar. | Confirmar. | No se persiste cita con datos faltantes. |
| G — actor ajeno | Paciente A intenta solicitar por paciente B sin permiso. | Enviar solicitud manipulada. | M01 deniega; M03 no registra a nombre de B. |
| H — rollback | Fallo de persistencia durante confirmación. | Confirmar. | Sin cita confirmada y **sin** evento `CitaConfirmada`. |
| I — reprogramación posterior | M04 solicita cambio de horario para cita programada. | M03 aplica cambio con revalidación M02. | No se solapan recursos y M04 mantiene historial del cambio. |
| J — evento repetido | Evento confirmado entregado dos veces. | M05 recibe duplicados. | M05 evita duplicar la notificación; M03 mantiene un solo hecho de confirmación. |

## 12. Pendientes explícitos para cierre de diseño

| Decisión | Motivo | Coordinación solicitada |
| --- | --- | --- |
| Prioridad entre candidatas y desempate | CU-05 no define orden de elección. | Equipo / CNS; M02 + M03. |
| Horizonte de búsqueda, siguiente día hábil y feriados | Aparece como idea en MDL-02, no como regla institucional cerrada. | M02 + responsable funcional. |
| Duración del turno, intervalos y capacidad de consultorios | Clave para evitar superposiciones reales. | M02 + modelo de datos. |
| Cardinalidad `SolicitudCita → CitaMedica` y trazabilidad de registros asistidos | La especificación distingue solicitud y confirmación, pero no formaliza relación física. | M01/M03 + revisión DER. |
| Esquema de bloqueo/restricción PostgreSQL | RNF-04 exige atomicidad; aún falta seleccionar diseño de persistencia. | M02/M03 + responsable de datos. |
| Confirmación por personal de Enfermería-Admisión «cuando corresponda» | CU-07 menciona actor humano condicionado. | Responsable de CU-07 / M01. |
| Catálogo definitivo de estados e inasistencia | RF-12 y borrador M04 necesitan conciliación. | M04 + equipo, sin modificar Sprint 1 sin gestión de cambios. |
| Entrega confiable de evento y duplicados | M05 exige post-commit e idempotencia. | M03/M05. |
| Política de reintentos/caducidad de solicitudes | No está autorizada lista de espera ni estados derivados. | Equipo / CNS. |

## 13. Criterios de aceptación y estado de evidencia

- [x] Se diferencian **solicitud registrada** y **cita confirmada**.
- [x] CU-04 no deja elegir al paciente médico, fecha, hora ni consultorio.
- [x] Se diseñaron `SolicitudCita` y `CitaMedica` como entidades **propias** de M03.
- [x] Se identificaron referencias a `Paciente` (M01) y recursos de M02 sin duplicarlos.
- [x] Se documentó CU-04 → CU-05 → CU-06 → CU-07 y sus alternativas.
- [x] Se exigió protección transaccional y revalidación para RNF-03/RNF-04.
- [x] Se representó qué ocurre sin recursos, sin inventar cola o prioridad.
- [x] Se establecieron contratos conceptuales con M01/M02/M04/M05.
- [x] Se separó publicación de confirmación a M05 de la persistencia de M03.
- [x] Se prepararon documentos y diagramas PlantUML sin código de producción.
- [ ] **Pendiente:** validación cruzada con responsables M01/M02/M04/M05 y cotejo final del DER.
- [ ] **Pendiente:** confirmar reglas institucionales y decisiones de la sección 12.
- [ ] **Pendiente GitHub:** crear o referenciar número real de Issue y abrir PR `feature/S2-M03-nucleo-citas` → `develop`.

**Nota de integración:** no confundir el código de propuesta `S2-M03` con un número real `#...` de GitHub. El PR deberá enlazar la Issue auténtica y solicitar revisión de otro integrante.
