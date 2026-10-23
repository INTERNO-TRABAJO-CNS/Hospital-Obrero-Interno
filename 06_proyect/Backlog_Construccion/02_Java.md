# Backlog de Construcción — Java

[Volver al índice](README.md) · Responsable indicado **como propuesta**, validar carga al iniciar cada sprint.

### [CON-01][Java] Crear estructura base del backend Spring Boot

**Identificador:** `CON-01` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-02, ELA-05  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Crear proyecto Java MVC con arquitectura modular, perfiles y manejo de errores.

**Criterios de aceptación**
- [ ] Proyecto Spring Boot compila y ejecuta pruebas JUnit.
- [ ] Paquetes controladores, servicios, dominio, repositorios, DTO y configuración separados.
- [ ] REST y documentación OpenAPI inicial disponibles en desarrollo.

**Tareas**
- [ ] Crear el proyecto y configuración por ambiente.
- [ ] Agregar endpoint de salud, validación y manejador de errores.

**Caso de prueba sugerido:** Arranque limpio y respuesta homogénea ante error de validación.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-01-crear-estructura-base-del-backend-s` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-03][Java] Implementar autenticación y permisos por rol

**Identificador:** `CON-03` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Matías · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-01, RF-02  
**Casos de uso relacionados:** CU-01, CU-02  
**Depende de (referencias del plan):** CON-01, CON-02  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Proteger las API según los cuatro roles documentados.

**Criterios de aceptación**
- [ ] Acceso exige credenciales válidas y respeta permisos por recurso.
- [ ] El rechazo no revela si un paciente o cuenta existe.
- [ ] Pruebas cubren sesión válida, inválida, expirada y accesos prohibidos.

**Tareas**
- [ ] Implementar autenticación y autorización Spring Security.
- [ ] Definir respuestas 401/403 documentadas.

**Caso de prueba sugerido:** Paciente no puede consultar recursos administrativos.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-03-implementar-autenticacin-y-permisos` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-05][Java] Implementar administración de usuarios, roles y permisos

**Identificador:** `CON-05` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Matías · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-02  
**Casos de uso relacionados:** CU-02  
**Depende de (referencias del plan):** CON-03  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** CRUD administrativo de usuarios y asignación controlada de roles.

**Criterios de aceptación**
- [ ] Solo Administrador ejecuta altas y cambios de rol.
- [ ] Cambios se validan y se registra actor responsable.
- [ ] No se permiten permisos superiores mediante actualización arbitraria.

**Tareas**
- [ ] Crear controladores y servicios de usuarios.
- [ ] Agregar pruebas de autorización.

**Caso de prueba sugerido:** Cuenta de paciente intenta cambiar su rol y recibe 403.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-05-implementar-administracin-de-usuari` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-06][Java] Implementar registro y consulta de pacientes

**Identificador:** `CON-06` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-03  
**Casos de uso relacionados:** CU-03  
**Depende de (referencias del plan):** CON-02, CON-03  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Gestionar datos de pacientes siguiendo los campos autorizados en documentación.

**Criterios de aceptación**
- [ ] Registro y consulta aplican validaciones del caso de uso.
- [ ] No se crean pacientes duplicados bajo las claves definidas.
- [ ] Consultas están limitadas al rol y al paciente correspondiente.

**Tareas**
- [ ] Crear entidad, repositorio, servicio y endpoints.
- [ ] Agregar validación y pruebas.

**Caso de prueba sugerido:** Registro con identificador duplicado se rechaza sin corrupción.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-06-implementar-registro-y-consulta-de-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-10][Java] Implementar catálogos de médicos, especialidades, recursos y agendas

**Identificador:** `CON-10` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Alex · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-06, RF-13  
**Casos de uso relacionados:** CU-06, CU-13  
**Depende de (referencias del plan):** CON-02, CON-03  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Preparar disponibilidad a partir de profesionales, especialidades, horarios y recursos habilitados.

**Criterios de aceptación**
- [ ] Catálogos y agenda consultables por roles autorizados.
- [ ] No aparecen recursos inactivos como disponibles.
- [ ] Horarios usan zona horaria definida por la arquitectura.

**Tareas**
- [ ] Crear modelo y endpoints de consulta/mantenimiento permitidos.
- [ ] Probar cruces de horarios.

**Caso de prueba sugerido:** Agenda sin consultorio válido no habilita un cupo.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-10-implementar-catlogos-de-mdicos-espe` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-11][Java] Implementar validación de disponibilidad y no doble reserva

**Identificador:** `CON-11` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-06  
**Casos de uso relacionados:** CU-06  
**Depende de (referencias del plan):** CON-02, CON-10  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Implementar validación y exclusión de reservas concurrentes.

**Criterios de aceptación**
- [ ] Consulta disponible considera médico, horario, recurso y ocupación.
- [ ] Dos solicitudes concurrentes no confirman el mismo cupo.
- [ ] Se registran resultados de prueba de concurrencia.

**Tareas**
- [ ] Implementar consultas de elegibilidad y bloqueos/índices.
- [ ] Crear pruebas con solicitudes simultáneas.

**Caso de prueba sugerido:** Dos clientes compiten por el último cupo: solo una confirmación.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-11-implementar-validacin-de-disponibil` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-12][Java] Implementar motor de asignación automática íntegramente en Java

**Identificador:** `CON-12` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-05, RF-06  
**Casos de uso relacionados:** CU-05, CU-06  
**Depende de (referencias del plan):** CON-11, ELA-04  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Elegir cupo mediante las reglas aprobadas y devolver una decisión determinista.

**Criterios de aceptación**
- [ ] La lógica se ejecuta en servicios Java, sin núcleo C++.
- [ ] Se cumplen las prioridades y filtros aprobados; decisiones faltantes bloquean solo la regla afectada.
- [ ] Si no hay disponibilidad, se devuelve un estado explícito sin reservar recursos.

**Tareas**
- [ ] Implementar estrategia de selección de cupo.
- [ ] Probar casos borde y orden de prioridad.

**Caso de prueba sugerido:** Para datos idénticos y reglas estables devuelve la misma asignación.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-12-implementar-motor-de-asignacin-auto` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-13][Java] Implementar solicitud y confirmación de citas con transacción

**Identificador:** `CON-13` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-04, RF-07  
**Casos de uso relacionados:** CU-04, CU-07  
**Depende de (referencias del plan):** CON-06, CON-12  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Recibir solicitud, ejecutar asignación y guardar cita confirmada de forma consistente.

**Criterios de aceptación**
- [ ] Operación de solicitud/confirmación sigue CU-04 y CU-07.
- [ ] No queda cita confirmada si la transacción falla a mitad.
- [ ] Se registran trazas sin datos personales innecesarios.

**Tareas**
- [ ] Crear endpoints y orquestación en servicios.
- [ ] Implementar rollback y tests.

**Caso de prueba sugerido:** Fallo entre reserva y confirmación no deja cupo ocupado.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-13-implementar-solicitud-y-confirmacin` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-14][Java] Implementar consulta de citas del paciente

**Identificador:** `CON-14` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-08  
**Casos de uso relacionados:** CU-08  
**Depende de (referencias del plan):** CON-13  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Permitir al paciente revisar sus citas y sus estados.

**Criterios de aceptación**
- [ ] Paciente ve únicamente sus citas autorizadas.
- [ ] Estados y fecha/hora coinciden con base de datos.
- [ ] Se define paginación cuando proceda.

**Tareas**
- [ ] Crear consulta por paciente autenticado.
- [ ] Agregar pruebas de acceso cruzado.

**Caso de prueba sugerido:** Paciente A no puede consultar la cita de paciente B.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-14-implementar-consulta-de-citas-del-p` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-15][Java] Implementar consulta de citas para admisión

**Identificador:** `CON-15` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Matías · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-09  
**Casos de uso relacionados:** CU-09  
**Depende de (referencias del plan):** CON-13  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Consultar agenda y citas registradas para personal autorizado de Enfermería/Admisión.

**Criterios de aceptación**
- [ ] Filtro por fechas y otros criterios aprobados funciona.
- [ ] Acceso denegado a quien carezca de rol.
- [ ] Respuestas no exponen información que no corresponde.

**Tareas**
- [ ] Crear endpoint de consulta administrativa.
- [ ] Agregar pruebas por rol.

**Caso de prueba sugerido:** Paciente no ve consulta general de todas las citas.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-15-implementar-consulta-de-citas-para-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-16][Java] Implementar reprogramación y cancelación de citas

**Identificador:** `CON-16` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Matías · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-10, RF-11  
**Casos de uso relacionados:** CU-10, CU-11  
**Depende de (referencias del plan):** CON-11, CON-13  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Gestionar cambios de cita bajo reglas de transición aprobadas.

**Criterios de aceptación**
- [ ] Operaciones verifican estado, permisos y disponibilidad.
- [ ] Cancelación libera el cupo; no reasigna todavía hasta issue CON-21.
- [ ] Operaciones dejan registro del motivo/actor si exige documento.

**Tareas**
- [ ] Crear endpoints y servicio transaccional.
- [ ] Registrar cambio de estado e historial mínimo.

**Caso de prueba sugerido:** No se cancela dos veces como dos operaciones distintas.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-16-implementar-reprogramacin-y-cancela` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-19][Java] Implementar agenda médica y actualización de estado

**Identificador:** `CON-19` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Alex · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-12, RF-13  
**Casos de uso relacionados:** CU-12, CU-13  
**Depende de (referencias del plan):** CON-10, CON-13  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Exponer la agenda del médico y actualizar estado de una cita con transiciones controladas.

**Criterios de aceptación**
- [ ] Médico solo consulta agenda permitida.
- [ ] Estados se validan y quedan persistidos.
- [ ] Se conserva historial de transiciones relevantes.

**Tareas**
- [ ] Implementar servicios y endpoints.
- [ ] Probar transiciones válidas e inválidas.

**Caso de prueba sugerido:** Cita cancelada no puede pasar a atendida sin regla aprobada.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-19-implementar-agenda-mdica-y-actualiz` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-21][Java] Implementar reasignación de cupo liberado en Java

**Identificador:** `CON-21` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-05, RF-06, RF-10, RF-11  
**Casos de uso relacionados:** CU-05, CU-06, CU-10, CU-11  
**Depende de (referencias del plan):** CON-16, CON-20  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Procesar cupo liberado de manera consistente usando los criterios aprobados en arquitectura.

**Criterios de aceptación**
- [ ] Cupo liberado entra al flujo de reasignación permitido por los requisitos.
- [ ] No se asigna dos veces ni a persona no elegible.
- [ ] La operación genera evento auditable y aviso para consumidores.

**Tareas**
- [ ] Implementar servicio transaccional idempotente.
- [ ] Agregar casos simultáneos de cancelación y demanda.

**Caso de prueba sugerido:** Dos disparadores para el mismo cupo no producen dos citas.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-21-implementar-reasignacin-de-cupo-lib` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-24][Java] Generar notificaciones y recordatorios de cita

**Identificador:** `CON-24` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-16, RF-17  
**Casos de uso relacionados:** CU-16, CU-17  
**Depende de (referencias del plan):** CON-13, CON-21, CON-22  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Diseñar y ejecutar avisos de cita y recordatorios sin suponer proveedor externo no aprobado.

**Criterios de aceptación**
- [ ] Reglas de generación y momento del recordatorio siguen RF-16/17.
- [ ] El mecanismo de entrega queda documentado y probado según alcance aprobado.
- [ ] No hay avisos duplicados por reintento.

**Tareas**
- [ ] Crear servicios y agenda de tareas.
- [ ] Probar programación y deduplicación.

**Caso de prueba sugerido:** Dos ejecuciones programadas no duplican el mismo recordatorio.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-24-generar-notificaciones-y-recordator` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-25][Java] Implementar consulta permitida de paciente y registro de atención

**Identificador:** `CON-25` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Alex · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-14, RF-15  
**Casos de uso relacionados:** CU-14, CU-15  
**Depende de (referencias del plan):** CON-03, CON-19  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Permitir al personal autorizado consultar datos permitidos y registrar atención.

**Criterios de aceptación**
- [ ] Consulta y registro respetan rol, finalidad y mínimos de datos.
- [ ] Registro de atención conserva relación con cita.
- [ ] No se transforma en historia clínica avanzada fuera de alcance.

**Tareas**
- [ ] Diseñar DTO de datos permitidos.
- [ ] Crear endpoint de registro y pruebas de autorización.

**Caso de prueba sugerido:** Un usuario sin permiso no consulta datos de atención.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-25-implementar-consulta-permitida-de-p` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-26][Java] Implementar historial de cambios de citas

**Identificador:** `CON-26` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-18  
**Casos de uso relacionados:** CU-18  
**Depende de (referencias del plan):** CON-16, CON-19  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Exponer una trazabilidad de cambios con autor, operación y fecha de acuerdo a los permisos.

**Criterios de aceptación**
- [ ] Se registran alta y modificaciones relevantes.
- [ ] Historial no se altera al actualizar una cita.
- [ ] Consultas protegidas por permisos.

**Tareas**
- [ ] Implementar endpoint y filtros.
- [ ] Probar integridad del historial.

**Caso de prueba sugerido:** Reprogramación deja rastro del estado anterior y nuevo.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-26-implementar-historial-de-cambios-de` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-27][Java] Implementar reportes de citas e indicadores de gestión

**Identificador:** `CON-27` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Alex · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Java  
**Requisitos relacionados:** RF-19, RF-20  
**Casos de uso relacionados:** CU-19, CU-20  
**Depende de (referencias del plan):** CON-13, CON-19  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`

**Descripción:** Agregar agregaciones administrativas desde datos de citas.

**Criterios de aceptación**
- [ ] Reportes responden a RF-19/20 y tienen alcance temporal explícito.
- [ ] Consultas se ejecutan con permisos administrativos.
- [ ] Se prueban totales contra datos conocidos.

**Tareas**
- [ ] Definir consultas y filtros autorizados.
- [ ] Documentar indicadores concretos derivados de requisitos.

**Caso de prueba sugerido:** Con un juego sintético pequeño los totales coinciden.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-27-implementar-reportes-de-citas-e-ind` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).
