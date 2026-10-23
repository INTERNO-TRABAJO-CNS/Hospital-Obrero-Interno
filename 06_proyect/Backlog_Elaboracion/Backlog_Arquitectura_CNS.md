# Issues de Elaboración — Arquitectura MVC

### [ELA-01][Arquitectura] Consolidar RF/CU/HU y arquitectura pendiente

**Identificador:** `ELA-01` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** Ninguna  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Revisar los documentos existentes del sprint documental sin duplicarlos ni renumerar sus identificadores.

**Criterios de aceptación**
- [ ] Matriz RF-01 a RF-20 y CU-01 a CU-20 cotejada con los documentos oficiales.
- [ ] Se identifican y registran ambigüedades y decisiones pendientes, sin atribuirles reglas inventadas.
- [ ] Se conservan ramas, PR e issues anteriores con enlaces.

**Tareas**
- [ ] Revisar documentación y marcar artefactos ya terminados.
- [ ] Registrar dudas concretas en decisiones pendientes.

**Caso de prueba sugerido:** Comparar una muestra de RF/CU/HU con su documento y archivo del repositorio.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-01-consolidar-rfcuhu-y-arquitectura-pe` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-02][Arquitectura] Definir arquitectura general y reparto MVC

**Identificador:** `ELA-02` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-01  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Delimitar vista Flutter, controladores REST, modelo y servicios Java, repositorios PostgreSQL y comunicación de eventos.

**Criterios de aceptación**
- [ ] Documento `02_mvc.md` distingue Vista, Controlador, Modelo y persistencia.
- [ ] Java concentra el motor de asignación de citas; no se crea servicio C++.
- [ ] Diagrama de componentes y capas con responsabilidades y dependencias verificables.

**Tareas**
- [ ] Escribir visión general de componentes.
- [ ] Definir paquetes, capas y flujo completo desde Flutter al backend.

**Caso de prueba sugerido:** Revisar el flujo de solicitud de cita e identificar el dueño de cada validación.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-02-definir-arquitectura-general-y-repa` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-03][Arquitectura] Diseñar modelo de dominio y estados de cita

**Identificador:** `ELA-03` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Alex · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-01  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Identificar entidades y relaciones del CNS a partir de CU, sin asumir que cada pantalla sea un modelo.

**Criterios de aceptación**
- [ ] Se documentan Paciente, Usuario, Médico, Agenda, Recurso, Solicitud y Cita cuando correspondan a RF/CU.
- [ ] Estados y transiciones de la cita se validan contra reglas del negocio.
- [ ] Diagrama PlantUML editable y exportación visual.

**Tareas**
- [ ] Especificar atributos y asociaciones.
- [ ] Documentar restricciones, estados y operaciones del dominio.

**Caso de prueba sugerido:** Caminar dos escenarios: cita confirmada y cancelada, verificando consistencia de estados.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-03-disear-modelo-de-dominio-y-estados-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-04][Arquitectura] Definir reglas del núcleo Java de asignación y reasignación

**Identificador:** `ELA-04` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-04, RF-05, RF-06, RF-07, RF-10, RF-11  
**Casos de uso relacionados:** CU-04, CU-05, CU-06, CU-07, CU-10, CU-11  
**Depende de (referencias del plan):** ELA-03  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Especificar el orden de evaluación y el manejo de concurrencia para que no existan dobles reservas.

**Criterios de aceptación**
- [ ] Reglas de elección de cupo y elegibilidad vinculadas a RF/CU; vacíos quedan como decisiones por aprobar.
- [ ] Se documenta qué pasa al cancelar, al no hallar cupo y ante dos solicitudes simultáneas.
- [ ] Se define necesidad de transacción, restricción única y estrategia de bloqueo en Java/PostgreSQL.

**Tareas**
- [ ] Identificar precondiciones y resultados de cada operación.
- [ ] Redactar pseudoflujos y escenarios de carrera para las pruebas.

**Caso de prueba sugerido:** Simular dos pacientes solicitando el mismo último cupo y una cancelación posterior.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-04-definir-reglas-del-ncleo-java-de-as` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-05][Arquitectura] Diseñar contratos REST y eventos WebSocket

**Identificador:** `ELA-05` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Matías · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-02, ELA-04  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Especificar interfaces independientes de la implementación para permitir trabajo paralelo Flutter/Java.

**Criterios de aceptación**
- [ ] Cada operación necesita ruta, método, entrada, respuesta y error documentados.
- [ ] WebSocket se usa para avisos/cambios de estado sin reemplazar las operaciones transaccionales REST.
- [ ] Autorización por rol y criterios de no divulgación de datos privados incluidos.

**Tareas**
- [ ] Definir versión inicial de API y ejemplos JSON no reales.
- [ ] Documentar eventos y estrategia de reconexión.

**Caso de prueba sugerido:** Validar que la petición Flutter de citas puede implementarse solo usando el contrato.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-05-disear-contratos-rest-y-eventos-web` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-06][Arquitectura] Diseñar prototipo de pantallas Flutter versión 1

**Identificador:** `ELA-06` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-01  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Prototipar navegación de paciente, enfermería/admisión, médico y administrador según permisos.

**Criterios de aceptación**
- [ ] Inventario de pantallas por rol y su vínculo al CU correspondiente.
- [ ] Flujos de inicio de sesión, solicitud, consulta, gestión, agenda e indicadores representados.
- [ ] No se incluyen datos reales de pacientes.

**Tareas**
- [ ] Elaborar wireframes/maquetas versionadas.
- [ ] Revisar navegación y formularios contra los 20 CU.

**Caso de prueba sugerido:** Demostración navegable en papel o prototipo del flujo solicitar y cancelar cita.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-06-disear-prototipo-de-pantallas-flutt` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-07][Arquitectura] Diseñar esquema PostgreSQL y migraciones iniciales

**Identificador:** `ELA-07` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Alex · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-18  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-18  
**Depende de (referencias del plan):** ELA-03, ELA-04  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Definir tablas, índices y restricciones consistentes con el dominio antes de programar.

**Criterios de aceptación**
- [ ] Diagrama entidad-relación y diccionario de datos registrados.
- [ ] Restricciones de integridad y prevención de doble reserva diseñadas.
- [ ] Datos sensibles y política de acceso lógico identificados.

**Tareas**
- [ ] Proponer claves, relaciones, índices y columnas de auditoría.
- [ ] Anotar la estrategia de migraciones y ambiente de pruebas.

**Caso de prueba sugerido:** Revisar que una cita no pueda referir a recursos inexistentes.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-07-disear-esquema-postgresql-y-migraci` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-08][Arquitectura] Validar diagramas UML y secuencias de los CU

**Identificador:** `ELA-08` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-02, ELA-03, ELA-06  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Reutilizar los diagramas ya trabajados del CNS; corregir solo incoherencias y huecos detectados.

**Criterios de aceptación**
- [ ] Diagramas de CU-01 a CU-20 revisados sin duplicar trabajo aprobado.
- [ ] Diagrama secuencial de solicitud-asignación-confirmación/cancelación revisado.
- [ ] Enlaces relativos del repositorio funcionan.

**Tareas**
- [ ] Inspeccionar la carpeta de diagramas existente.
- [ ] Corregir diferencias y registrar cambios.

**Caso de prueba sugerido:** Seguir CU-04 a CU-07 desde el caso de uso hasta las capas de arquitectura.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-08-validar-diagramas-uml-y-secuencias-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [ELA-09][Arquitectura] Revisar seguridad, estrategia de pruebas y aprobar arquitectura

**Identificador:** `ELA-09` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 2 - Arquitectura MVC  
**Responsable sugerido:** Matías · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Arquitectura  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-04, ELA-05, ELA-06, ELA-07, ELA-08  
**Etiquetas sugeridas:** `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura`

**Descripción:** Cerrar la Elaboración con una arquitectura verificable antes de comenzar Construcción.

**Criterios de aceptación**
- [ ] Matriz RF/CU → componente → contrato → prueba preparada.
- [ ] Roles, control de acceso, cifrado en tránsito, logs sin datos sensibles y respaldos definidos a nivel de diseño.
- [ ] Acta de pendientes y aprobación de versión 1 de arquitectura.

**Tareas**
- [ ] Preparar criterios verificables y Definition of Done.
- [ ] Hacer revisión cruzada de PRs con el equipo.

**Caso de prueba sugerido:** Revisión conjunta de un CU de paciente y uno de administrador.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-ela-09-revisar-seguridad-estrategia-de-pru` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).
