# Backlog de Construcción — Java | CNS Hospital Obrero

[Índice general](README.md) · [Orden de creación](00_Indice_de_subida.md) · [Trazabilidad](08_Cobertura_y_trazabilidad.md) · [Flujos](09_Flujos_y_dependencias.md) · [Pruebas](12_Casos_de_prueba.md)

**Total de issues propuestos:** 17. **Código:** pendiente de crear en GitHub; son especificaciones del backlog y no evidencias de implementación.
**Responsables propuestos:** Benjamín, Matías, Alejandro, Alex. Los responsables y puntos se revisan en [07_Estimacion.md](07_Estimacion.md).

Este archivo toma el comportamiento oficial de [CU-01 a CU-20](13_Comportamiento_CU.md) y la [matriz de requisitos](08_Cobertura_y_trazabilidad.md). No sustituye la aprobación de arquitectura del Sprint 2.

## Distribución por Sprint

| Sprint | Issues |
|---|---|
| S3 (02/11–13/11/2026) | [CON-01](02_Java.md#con-01) · [CON-03](02_Java.md#con-03) · [CON-05](02_Java.md#con-05) · [CON-06](02_Java.md#con-06) |
| S4 (16/11–27/11/2026) | [CON-10](02_Java.md#con-10) · [CON-11](02_Java.md#con-11) · [CON-12](02_Java.md#con-12) · [CON-13](02_Java.md#con-13) · [CON-14](02_Java.md#con-14) · [CON-15](02_Java.md#con-15) · [CON-16](02_Java.md#con-16) · [CON-19](02_Java.md#con-19) |
| S5 (30/11–11/12/2026) | [CON-21](02_Java.md#con-21) · [CON-24](02_Java.md#con-24) · [CON-25](02_Java.md#con-25) · [CON-26](02_Java.md#con-26) · [CON-27](02_Java.md#con-27) |

---

<a id="con-01"></a>
### [CON-01][Java] Crear estructura base del backend Spring Boot

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Benjamín. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-01` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [ELA-02](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) · [ELA-05](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java`  
**Rama de trabajo (base `develop`):** `feature/con-01-crear-estructura-base-del-backend-spring-boot` (crear solo cuando se abra el issue).

**Descripción:** Crear estructura base del backend Spring Boot. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Criterios de aceptación**
- [ ] El backend arranca con Java/Spring Boot y separa controladores REST, servicios de aplicación, modelo de dominio, repositorios y DTO, sin reglas de negocio en controladores.
- [ ] Los perfiles de desarrollo y pruebas cargan configuración externa; claves y contraseñas nunca están dentro del repositorio.
- [ ] Las respuestas de validación y errores no incluyen datos personales y siguen el contrato acordado en ELA-05; se publica OpenAPI en desarrollo.
- [ ] Una prueba de arranque automático y una petición incorrecta verifican que el servicio continúa operativo.

**Tareas**
- [ ] Crear proyecto, paquetes MVC y perfiles de configuración.
- [ ] Instalar validaciones, excepciones REST, OpenAPI y prueba básica de salud.
- [ ] Documentar compilación, ejecución y pruebas en README.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-01A](12_Casos_de_prueba.md#cp-cns-01a) — Arrancar el backend con configuración de pruebas y comprobar endpoint de salud y pruebas JUnit.
- [ ] [CP-CNS-01B](12_Casos_de_prueba.md#cp-cns-01b) — Enviar datos inválidos y verificar error definido sin fuga de datos.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-03"></a>
### [CON-03][Java] Implementar autenticación y permisos por rol

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Matías. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-03` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-01](02_Java.md#con-01) · [CON-02](03_PostgreSQL.md#con-02)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-01`  
**Rama de trabajo (base `develop`):** `feature/con-03-implementar-autenticacion-y-permisos-por-rol` (crear solo cuando se abra el issue).

**Descripción:** Implementar autenticación y permisos por rol. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-01 / CU-01 — Iniciar Sesión](13_Comportamiento_CU.md#cu-01).
**Requisitos funcionales asociados:** RF-01.
**RNF aplicables:** RNF-01, RNF-02, RNF-05.

**Criterios de aceptación**
- [ ] Una cuenta válida puede autenticarse y obtener acceso únicamente a las operaciones correspondientes a su rol: paciente, admisión/enfermería, médico o administrador (CU-01).
- [ ] Credenciales inválidas o sesión no válida impiden el ingreso y no exponen funciones restringidas (CU-01, alternativa 2a).
- [ ] Un usuario autenticado sin rol adecuado recibe rechazo autorizado desde el backend, aunque intente acceder a una API por fuera de Flutter (CU-01, alternativa 3a).
- [ ] El mecanismo de autenticación y duración de sesiones provienen de ELA-05/ELA-09; no se presupone login Google o JWT sin esa aprobación.

**Tareas**
- [ ] Configurar Spring Security, identidad, sesiones y reglas por rol.
- [ ] Implementar filtros de autorización de endpoints, manejo de errores y pruebas negativas.
- [ ] Registrar en la documentación las decisiones de autenticación aprobadas.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-03A](12_Casos_de_prueba.md#cp-cns-03a) — Iniciar sesión con cuenta válida y comprobar las funciones autorizadas.
- [ ] [CP-CNS-03B](12_Casos_de_prueba.md#cp-cns-03b) — Usar credenciales inválidas y tratar de consultar datos de otro rol; ambas operaciones deben rechazarse.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-05"></a>
### [CON-05][Java] Implementar administración de usuarios, roles y permisos

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Matías. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-05` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-03](02_Java.md#con-03)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-02`  
**Rama de trabajo (base `develop`):** `feature/con-05-implementar-administracion-de-usuarios-roles-y-p` (crear solo cuando se abra el issue).

**Descripción:** Implementar administración de usuarios, roles y permisos. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-02 / CU-02 — Gestionar usuarios roles y permisos](13_Comportamiento_CU.md#cu-02).
**Requisitos funcionales asociados:** RF-02.
**RNF aplicables:** RNF-01, RNF-05.

**Criterios de aceptación**
- [ ] Solo el administrador autenticado puede consultar y modificar usuarios, roles y permisos (CU-02).
- [ ] Cambiar un rol aplica los permisos nuevos en las operaciones posteriores; usuarios no autorizados no pueden elevar sus privilegios.
- [ ] Un cambio inválido no altera la configuración previa del usuario (CU-02, alternativa 3a).
- [ ] La granularidad de permisos se limita a lo autorizado por ELA-09; ninguna jerarquía adicional se inventa.

**Tareas**
- [ ] Implementar servicios y controladores de administración de usuarios y roles.
- [ ] Proteger operaciones y agregar pruebas de cambios aceptados y rechazados.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-05A](12_Casos_de_prueba.md#cp-cns-05a) — Administrador modifica el rol de un usuario; las funciones visibles y permisos efectivos se actualizan.
- [ ] [CP-CNS-05B](12_Casos_de_prueba.md#cp-cns-05b) — Paciente intenta cambiar su rol y el backend rechaza sin modificar nada.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-06"></a>
### [CON-06][Java] Implementar registro y consulta de pacientes

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Alejandro. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-06` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-02](03_PostgreSQL.md#con-02) · [CON-03](02_Java.md#con-03)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-03`  
**Rama de trabajo (base `develop`):** `feature/con-06-implementar-registro-y-consulta-de-pacientes` (crear solo cuando se abra el issue).

**Descripción:** Implementar registro y consulta de pacientes. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-03 / CU-03 — Registrar y consultar pacientes](13_Comportamiento_CU.md#cu-03).
**Requisitos funcionales asociados:** RF-03.
**RNF aplicables:** RNF-01, RNF-02.

**Criterios de aceptación**
- [ ] El personal de enfermería/admisión puede localizar al paciente usando su identificación (CU-03).
- [ ] Si el paciente existe, se presenta el registro existente y no se crea duplicado (CU-03, alternativa 1a).
- [ ] Si no existe, se registra con datos válidos y se vuelve a comprobar la duplicidad al confirmar; datos incompletos no se guardan (CU-03, alternativa 3a).
- [ ] La identificación y los campos autorizados se toman de ELA-03/ELA-07; no se establece integración externa de afiliación no documentada.

**Tareas**
- [ ] Crear entidad/servicio/repositorio y consultas restringidas.
- [ ] Implementar validación de campos e identificación única con pruebas de concurrencia.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-06A](12_Casos_de_prueba.md#cp-cns-06a) — Buscar paciente existente devuelve su registro y no incrementa la cantidad de pacientes.
- [ ] [CP-CNS-06B](12_Casos_de_prueba.md#cp-cns-06b) — Dos registros con la misma identificación no pueden crear duplicados, incluso si se envían simultáneamente.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-10"></a>
### [CON-10][Java] Implementar catálogos de médicos, especialidades, recursos y agendas

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-10` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-02](03_PostgreSQL.md#con-02) · [CON-03](02_Java.md#con-03)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-05` · `caso-uso:CU-06` · `caso-uso:CU-13`  
**Rama de trabajo (base `develop`):** `feature/con-10-implementar-catalogos-de-medicos-especialidades` (crear solo cuando se abra el issue).

**Descripción:** Implementar catálogos de médicos, especialidades, recursos y agendas. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-05 / CU-05 — Asignar cita automáticamente](13_Comportamiento_CU.md#cu-05) · [HU-06 / CU-06 — Validar disponibilidad de Recursos](13_Comportamiento_CU.md#cu-06) · [HU-13 / CU-13 — Consultar agenda médica](13_Comportamiento_CU.md#cu-13).
**Requisitos funcionales asociados:** RF-05, RF-06, RF-13.
**RNF aplicables:** RNF-03.

**Criterios de aceptación**
- [ ] Servicios Java permiten consultar especialidades, médicos, horarios y consultorios con disponibilidad de origen documentado para CU-05 y CU-06.
- [ ] La agenda refleja horarios vigentes autorizados; una especialidad no presenta médicos no relacionados.
- [ ] Catálogos y configuraciones dependen de ELA-03/ELA-07; no se inventan turnos ni reglas de capacidad institucional.
- [ ] Las consultas se restringen por rol y devuelven únicamente datos necesarios.

**Tareas**
- [ ] Implementar entidades, repositorios, servicios y endpoints de catálogos/agendas según diseño.
- [ ] Preparar datos sintéticos coherentes y pruebas de relaciones.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-10A](12_Casos_de_prueba.md#cp-cns-10a) — Consultar especialidad devuelve solamente médicos y franjas compatibles cargadas.
- [ ] [CP-CNS-10B](12_Casos_de_prueba.md#cp-cns-10b) — Una agenda sin recursos vigentes no produce falsos cupos disponibles.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-11"></a>
### [CON-11][Java] Implementar validación de disponibilidad y no doble reserva

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Benjamín. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-11` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-02](03_PostgreSQL.md#con-02) · [CON-10](02_Java.md#con-10)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-06`  
**Rama de trabajo (base `develop`):** `feature/con-11-implementar-validacion-de-disponibilidad-y-no-do` (crear solo cuando se abra el issue).

**Descripción:** Implementar validación de disponibilidad y no doble reserva. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-06 / CU-06 — Validar disponibilidad de Recursos](13_Comportamiento_CU.md#cu-06).
**Requisitos funcionales asociados:** RF-06.
**RNF aplicables:** RNF-03, RNF-04.

**Criterios de aceptación**
- [ ] CU-06 determina si una combinación de médico, fecha, horario y consultorio está libre antes de confirmar una cita.
- [ ] Una combinación ocupada, superpuesta o con consultorio ocupado se rechaza sin crear cita (CU-06, alternativas 2a y 3a).
- [ ] Dos solicitudes simultáneas que compiten por un mismo recurso no pueden reservarlo ambas (CU-06, alternativa 4a; RNF-04).
- [ ] Java ejecuta la validación junto con persistencia segura en transacción; las restricciones PostgreSQL refuerzan su integridad.

**Tareas**
- [ ] Implementar validador de recursos con intervalos y estados aprobados.
- [ ] Incorporar transacciones, control de concurrencia y pruebas reproducibles contra PostgreSQL.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-11A](12_Casos_de_prueba.md#cp-cns-11a) — Un consultorio ocupado no puede asignarse a otra cita solapada.
- [ ] [CP-CNS-11B](12_Casos_de_prueba.md#cp-cns-11b) — Lanzar dos solicitudes concurrentes por el mismo médico y horario: como máximo una confirma la combinación.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-12"></a>
### [CON-12][Java] Implementar motor de asignación automática íntegramente en Java

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Benjamín. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-12` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-11](02_Java.md#con-11) · [ELA-04](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-05`  
**Rama de trabajo (base `develop`):** `feature/con-12-implementar-motor-de-asignacion-automatica-integ` (crear solo cuando se abra el issue).

**Descripción:** Implementar motor de asignación automática íntegramente en Java. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-05 / CU-05 — Asignar cita automáticamente](13_Comportamiento_CU.md#cu-05).
**Requisitos funcionales asociados:** RF-05.
**RNF aplicables:** RNF-03, RNF-04.

**Criterios de aceptación**
- [ ] CU-05 recibe una solicitud válida por especialidad y busca médico, fecha, hora y consultorio compatibles usando CU-06.
- [ ] El paciente nunca elige manualmente esos recursos; la selección la realiza Java según los criterios que se aprueben en ELA-04.
- [ ] Si no existe disponibilidad se informa que no se pudo asignar, sin inventar una cita (CU-05, alternativa 2a).
- [ ] Si un recurso queda ocupado durante el proceso, descarta la combinación e intenta otra válida o termina sin confirmación (CU-05, alternativa 3a).
- [ ] El algoritmo reside íntegramente en Java Spring Boot, sin motor externo C++.

**Tareas**
- [ ] Implementar servicio Java de asignación basado en consultas y validación de disponibilidad.
- [ ] Incorporar selección según política institucional aprobada y pruebas para ausencia/conflicto.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-12A](12_Casos_de_prueba.md#cp-cns-12a) — Para una especialidad con recursos libres, el servicio produce combinación válida sin intervención del paciente.
- [ ] [CP-CNS-12B](12_Casos_de_prueba.md#cp-cns-12b) — Al bloquear concurrentemente la combinación inicialmente elegida, el algoritmo no confirma doble reserva y responde según recursos restantes.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-13"></a>
### [CON-13][Java] Implementar solicitud y confirmación de citas con transacción

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Alejandro. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-13` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-06](02_Java.md#con-06) · [CON-12](02_Java.md#con-12)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-04` · `caso-uso:CU-07`  
**Rama de trabajo (base `develop`):** `feature/con-13-implementar-solicitud-y-confirmacion-de-citas-co` (crear solo cuando se abra el issue).

**Descripción:** Implementar solicitud y confirmación de citas con transacción. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-04 / CU-04 — Solicitar cita médica](13_Comportamiento_CU.md#cu-04) · [HU-07 / CU-07 — Registrar cita confirmada](13_Comportamiento_CU.md#cu-07).
**Requisitos funcionales asociados:** RF-04, RF-07.
**RNF aplicables:** RNF-03, RNF-04.

> **Cobertura conjunta:** CU-04 (solicitud) y CU-07 (registro confirmado) comparten este issue. CU-05 y CU-06 se desarrollan en CON-12 y CON-11; no confundir solicitud con cita confirmada.

**Criterios de aceptación**
- [ ] CU-04 registra la solicitud del paciente con especialidad válida sin campos de médico, fecha, hora o consultorio seleccionados por él.
- [ ] CU-04 deriva el procesamiento al servicio CU-05, que verifica recursos con CU-06 y remite una asignación válida a CU-07.
- [ ] CU-07 confirma una cita solo después de verificar de nuevo la disponibilidad y guarda paciente, especialidad, médico, fecha, hora, consultorio y estado inicial aprobado.
- [ ] Si falla registro o confirmación, no queda una cita con asignación parcial; si faltan datos o se pierde disponibilidad, se rechaza sin conflicto (CU-04 2a/4a y CU-07 2a/3a).
- [ ] La generación de notificación CU-16 sucede únicamente tras una operación confirmada; su implementación corresponde a CON-24.

**Tareas**
- [ ] Implementar flujo de solicitud CU-04 y coordinación del dominio CU-05/CU-06/CU-07 con transacción.
- [ ] Crear servicios y endpoints REST según contrato ELA-05 y pruebas de rollback.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-13A](12_Casos_de_prueba.md#cp-cns-13a) — Paciente solicita especialidad válida y recibe cita confirmada con asignación automática disponible.
- [ ] [CP-CNS-13B](12_Casos_de_prueba.md#cp-cns-13b) — Sin disponibilidad o con conflicto de último instante no se guarda cita confirmada ni se reserva un recurso inexistente.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-14"></a>
### [CON-14][Java] Implementar consulta de citas del paciente

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Alejandro. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-14` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-13](02_Java.md#con-13)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-08`  
**Rama de trabajo (base `develop`):** `feature/con-14-implementar-consulta-de-citas-del-paciente` (crear solo cuando se abra el issue).

**Descripción:** Implementar consulta de citas del paciente. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-08 / CU-08 — Consultar cita paciente](13_Comportamiento_CU.md#cu-08).
**Requisitos funcionales asociados:** RF-08.
**RNF aplicables:** RNF-02.

**Criterios de aceptación**
- [ ] CU-08 devuelve al paciente autenticado únicamente sus citas y nunca las de otro paciente.
- [ ] Cada cita mostrada incorpora médico, especialidad, fecha, hora, consultorio y estado, según el registro confirmado.
- [ ] Si no hay citas, devuelve una lista vacía o respuesta de no registros según el contrato aprobado, sin exponer otros datos.
- [ ] La consulta no modifica las citas.

**Tareas**
- [ ] Crear consulta por identidad autenticada y controlador correspondiente.
- [ ] Escribir pruebas por propietario y sin resultados.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-14A](12_Casos_de_prueba.md#cp-cns-14a) — Paciente A consulta sus citas y no recibe ninguna cita de Paciente B.
- [ ] [CP-CNS-14B](12_Casos_de_prueba.md#cp-cns-14b) — Paciente sin citas recibe resultado vacío válido sin modificar BD.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-15"></a>
### [CON-15][Java] Implementar consulta de citas para admisión

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Matías. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-15` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-13](02_Java.md#con-13)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-09`  
**Rama de trabajo (base `develop`):** `feature/con-15-implementar-consulta-de-citas-para-admision` (crear solo cuando se abra el issue).

**Descripción:** Implementar consulta de citas para admisión. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-09 / CU-09 — Consultar citas registradas](13_Comportamiento_CU.md#cu-09).
**Requisitos funcionales asociados:** RF-09.
**RNF aplicables:** RNF-02.

**Criterios de aceptación**
- [ ] Admisión/enfermería puede filtrar citas registradas por paciente, médico, fecha, estado y consultorio (CU-09).
- [ ] Una consulta sin coincidencias informa correctamente que no hay resultados y no modifica citas.
- [ ] Criterios incompletos o inválidos generan solicitud de corrección, y el rol no autorizado queda bloqueado.
- [ ] La respuesta limita campos sensibles a los permitidos por el rol.

**Tareas**
- [ ] Implementar consultas filtradas, DTO y controlador con autorizaciones.
- [ ] Probar combinaciones válidas, sin resultados y filtros erróneos.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-15A](12_Casos_de_prueba.md#cp-cns-15a) — Filtrar por médico y fecha devuelve solo citas coincidentes.
- [ ] [CP-CNS-15B](12_Casos_de_prueba.md#cp-cns-15b) — Una búsqueda con criterio inválido devuelve rechazo controlado sin alterar citas.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-16"></a>
### [CON-16][Java] Implementar reprogramación y cancelación de citas

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Matías. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-16` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-11](02_Java.md#con-11) · [CON-13](02_Java.md#con-13)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-10` · `caso-uso:CU-11`  
**Rama de trabajo (base `develop`):** `feature/con-16-implementar-reprogramacion-y-cancelacion-de-cita` (crear solo cuando se abra el issue).

**Descripción:** Implementar reprogramación y cancelación de citas. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-10 / CU-10 — Reprogramar cita](13_Comportamiento_CU.md#cu-10) · [HU-11 / CU-11 — Cancelar cita](13_Comportamiento_CU.md#cu-11).
**Requisitos funcionales asociados:** RF-10, RF-11.
**RNF aplicables:** RNF-03, RNF-04.

**Criterios de aceptación**
- [ ] CU-10 permite reprogramar a personal autorizado tras validar nuevamente médico, horario y consultorio con CU-06.
- [ ] Si no hay disponibilidad o hay conflicto concurrente, conserva la asignación anterior sin crear otra cita ni perder trazabilidad (CU-10, alternativas 3a/3b).
- [ ] CU-11 exige motivo de cancelación y deja la cita cancelada con su registro histórico sin borrar su identificación.
- [ ] La cancelación deja el horario disponible para nuevas asignaciones válidas; una eventual asignación automática del cupo liberado es propuesta adicional, no aprobada en RF-11.
- [ ] Los cambios confirmados quedan listos para CU-18 y CU-16; no se emiten notificaciones de operaciones rechazadas.

**Tareas**
- [ ] Implementar reprogramación transaccional reutilizando CU-06 y registro de cambios.
- [ ] Implementar cancelación con motivo, actualización de disponibilidad y auditoría.
- [ ] Añadir pruebas de conflicto y cambios concurrentes.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-16A](12_Casos_de_prueba.md#cp-cns-16a) — Intentar reprogramar a médico/consultorio ocupados conserva cita original y no duplica recursos.
- [ ] [CP-CNS-16B](12_Casos_de_prueba.md#cp-cns-16b) — Cancelar sin motivo es rechazado; con motivo guarda estado cancelada y traza el responsable.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-19"></a>
### [CON-19][Java] Implementar agenda médica y actualización de estado

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-19` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-10](02_Java.md#con-10) · [CON-13](02_Java.md#con-13)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-12` · `caso-uso:CU-13`  
**Rama de trabajo (base `develop`):** `feature/con-19-implementar-agenda-medica-y-actualizacion-de-est` (crear solo cuando se abra el issue).

**Descripción:** Implementar agenda médica y actualización de estado. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-12 / CU-12 — Actualizar estado de cita](13_Comportamiento_CU.md#cu-12) · [HU-13 / CU-13 — Consultar agenda médica](13_Comportamiento_CU.md#cu-13).
**Requisitos funcionales asociados:** RF-12, RF-13.
**RNF aplicables:** RNF-01, RNF-02.

**Criterios de aceptación**
- [ ] CU-13 devuelve al médico sus citas en agenda diaria o semanal, sin asignar las de otros médicos.
- [ ] CU-12 permite a médico/personal autorizado modificar el estado de una cita existente conforme al catálogo de estados aprobado.
- [ ] Una cita inexistente o no perteneciente al ámbito permitido no permite cambiar estado.
- [ ] Los cambios confirmados quedan disponibles para historial y notificaciones cuando corresponda; se registra el actor.
- [ ] La discrepancia 'inexistente' frente a 'inasistencia' se resuelve en ELA-03 antes de definir estados definitivos.

**Tareas**
- [ ] Implementar consulta de agenda diaria/semanal filtrada por médico.
- [ ] Implementar servicio de transiciones de estado autorizadas y tests.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-19A](12_Casos_de_prueba.md#cp-cns-19a) — Médico A consulta semana y no recibe citas de médico B.
- [ ] [CP-CNS-19B](12_Casos_de_prueba.md#cp-cns-19b) — Intentar actualizar una cita inexistente o estado no aprobado no modifica registros.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-21"></a>
### [CON-21][Java] Implementar reasignación de cupo liberado en Java

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Benjamín. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-21` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-16](02_Java.md#con-16) · [CON-20](05_Pruebas.md#con-20) · [ELA-04](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:java` · `estado:bloqueado`  
**Rama de trabajo (base `develop`):** `feature/con-21-implementar-reasignacion-de-cupo-liberado-en-jav` (crear solo cuando se abra el issue).

**Descripción:** Implementar reasignación de cupo liberado en Java. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.
**RNF aplicables:** RNF-03, RNF-04.

> **NO APROBADO COMO RF-11:** la cancelación requiere motivo; la reasignación automática del cupo liberado no figura como obligación explícita. Solo empezar si ELA-04 y la CNS validan esa política. [Decisión pendiente](10_Decisiones_pendientes.md#d-01).

**Criterios de aceptación**
- [ ] Este Issue es CONDICIONAL: no entra en ejecución sin política de reasignación del cupo liberado validada explícitamente por la CNS en ELA-04 y decisión documentada.
- [ ] Si se aprueba, Java asigna un cupo liberado solo a una solicitud elegible conforme a la prioridad/cola aprobada y previa validación CU-06.
- [ ] La operación evita dobles reservas y registra la trazabilidad; si no existe persona elegible, el cupo queda libre sin asignar una cita arbitraria.
- [ ] No se presupone confirmación de paciente, tiempo de aceptación ni lista de espera hasta que la institución lo defina.

**Tareas**
- [ ] Registrar primero la decisión institucional y los escenarios de aceptación.
- [ ] Solo si se aprueba, implementar selección y asignación transaccional con pruebas.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-21A](12_Casos_de_prueba.md#cp-cns-21a) — Con política aprobada, cancelar libera recurso y no asigna a pacientes que no cumplen sus condiciones.
- [ ] [CP-CNS-21B](12_Casos_de_prueba.md#cp-cns-21b) — Dos reasignaciones simultáneas nunca adjudican el mismo cupo a dos citas.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-24"></a>
### [CON-24][Java] Generar notificaciones y recordatorios de cita

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Alejandro. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-24` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-13](02_Java.md#con-13) · [CON-22](04_WebSocket.md#con-22)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-16` · `caso-uso:CU-17`  
**Rama de trabajo (base `develop`):** `feature/con-24-generar-notificaciones-y-recordatorios-de-cita` (crear solo cuando se abra el issue).

**Descripción:** Generar notificaciones y recordatorios de cita. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-16 / CU-16 — Generar notificación](13_Comportamiento_CU.md#cu-16) · [HU-17 / CU-17 — Generar recordatorio de cita](13_Comportamiento_CU.md#cu-17).
**Requisitos funcionales asociados:** RF-16, RF-17.
**RNF aplicables:** RNF-02.

**Criterios de aceptación**
- [ ] CU-16 genera notificación únicamente cuando registro, modificación o cancelación de cita ha sido confirmada.
- [ ] CU-17 genera recordatorio de una cita programada al llegar la antelación configurada y aprobada; omite citas canceladas/desactualizadas.
- [ ] La notificación identifica al destinatario correcto y contiene solo los datos permitidos; ningún cambio rechazado desencadena aviso falso.
- [ ] El canal externo (SMS/WhatsApp/correo/push) NO se considera aprobado; la implementación usa canal interno solo si ELA-05 lo valida y documenta el medio final.

**Tareas**
- [ ] Implementar eventos de dominio tras confirmación y generación de notificaciones/recordatorios.
- [ ] Configurar mecanismo y anticipación aprobados y pruebas con reloj controlado.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-24A](12_Casos_de_prueba.md#cp-cns-24a) — Cita cancelada antes de recordatorio no genera recordatorio antiguo.
- [ ] [CP-CNS-24B](12_Casos_de_prueba.md#cp-cns-24b) — Una operación fallida no genera notificación de cambio inexistente.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-25"></a>
### [CON-25][Java] Implementar consulta permitida de paciente y registro de atención

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-25` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-03](02_Java.md#con-03) · [CON-19](02_Java.md#con-19)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-14` · `caso-uso:CU-15`  
**Rama de trabajo (base `develop`):** `feature/con-25-implementar-consulta-permitida-de-paciente-y-reg` (crear solo cuando se abra el issue).

**Descripción:** Implementar consulta permitida de paciente y registro de atención. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-14 / CU-14 — Consultar información permitida del paciente](13_Comportamiento_CU.md#cu-14) · [HU-15 / CU-15 — Registrar datos de atención](13_Comportamiento_CU.md#cu-15).
**Requisitos funcionales asociados:** RF-14, RF-15.
**RNF aplicables:** RNF-01, RNF-02.

**Criterios de aceptación**
- [ ] CU-14 permite al médico ver solo información básica e historial autorizado del paciente correspondiente a una cita asignada.
- [ ] Si el médico no está vinculado a la cita o solicita un campo no permitido, el backend rechaza/omite el dato sin revelarlo (RNF-02).
- [ ] CU-15 registra asistencia, tiempo de espera y tiempo de atención de la cita con controles de validez; no registra historia clínica integral.
- [ ] Datos incompletos o no válidos no se guardan y la operación queda asociada a cita gestionable.

**Tareas**
- [ ] Implementar consulta con proyección de campos autorizados y comprobación de cita asignada.
- [ ] Implementar servicio de registro operativo de atención y validación de tiempos.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-25A](12_Casos_de_prueba.md#cp-cns-25a) — Médico ajeno a cita no puede ver los datos de ese paciente.
- [ ] [CP-CNS-25B](12_Casos_de_prueba.md#cp-cns-25b) — Registrar tiempos operativos inválidos no altera la atención, y datos válidos quedan asociados a la cita.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-26"></a>
### [CON-26][Java] Implementar historial de cambios de citas

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Alejandro. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-26` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-16](02_Java.md#con-16) · [CON-19](02_Java.md#con-19)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-18`  
**Rama de trabajo (base `develop`):** `feature/con-26-implementar-historial-de-cambios-de-citas` (crear solo cuando se abra el issue).

**Descripción:** Implementar historial de cambios de citas. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-18 / CU-18 — Consultar historial de cambios](13_Comportamiento_CU.md#cu-18).
**Requisitos funcionales asociados:** RF-18.
**RNF aplicables:** RNF-02.

**Criterios de aceptación**
- [ ] CU-18 permite a admisión/administrador consultar cambios de una cita con acción, usuario responsable y fecha/hora.
- [ ] Las reprogramaciones y cancelaciones confirmadas conservan evidencia sin sobrescribir el historial previo.
- [ ] Usuario sin permiso no puede acceder al historial y una cita sin cambios retorna resultado informativo.
- [ ] La política de retención y nivel de detalle queda pendiente de ELA-09; no se inventa tiempo de conservación.

**Tareas**
- [ ] Implementar registro auditado integrado con operaciones confirmadas.
- [ ] Implementar consulta protegida y pruebas por rol.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-26A](12_Casos_de_prueba.md#cp-cns-26a) — Reprogramar y cancelar genera registros diferenciados con actor y momento.
- [ ] [CP-CNS-26B](12_Casos_de_prueba.md#cp-cns-26b) — Usuario sin permiso no puede recuperar historial de cambios.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-27"></a>
### [CON-27][Java] Implementar reportes de citas e indicadores de gestión

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-27` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-13](02_Java.md#con-13) · [CON-19](02_Java.md#con-19)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:java` · `caso-uso:CU-19` · `caso-uso:CU-20`  
**Rama de trabajo (base `develop`):** `feature/con-27-implementar-reportes-de-citas-e-indicadores-de-g` (crear solo cuando se abra el issue).

**Descripción:** Implementar reportes de citas e indicadores de gestión. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-19 / CU-19 — Generar reportes de citas](13_Comportamiento_CU.md#cu-19) · [HU-20 / CU-20 — Consultar indicadores de gestión](13_Comportamiento_CU.md#cu-20).
**Requisitos funcionales asociados:** RF-19, RF-20.
**RNF aplicables:** RNF-02.

**Criterios de aceptación**
- [ ] CU-19 genera reportes de citas programadas, atendidas y canceladas y agrupaciones por médico, especialidad y consultorio.
- [ ] CU-20 consulta indicadores de demanda, cancelaciones, inasistencias, espera, atención y carga desde datos almacenados y autorizados.
- [ ] Solo administrador puede acceder a reportes e indicadores; una consulta sin datos presenta resultado vacío/insuficiente sin fabricar cifras.
- [ ] Fórmulas, rangos y definiciones de indicadores se fijan antes de implementar según decisión institucional; no se supone cómo calcular tasa de inasistencia.

**Tareas**
- [ ] Implementar consultas agregadas, servicios y endpoints administrativos.
- [ ] Documentar fórmulas aprobadas y probar conjuntos de datos sintéticos controlados.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-27A](12_Casos_de_prueba.md#cp-cns-27a) — Reporte por especialidad concuerda con número de citas del conjunto de prueba.
- [ ] [CP-CNS-27B](12_Casos_de_prueba.md#cp-cns-27b) — Indicador sin datos o usuario no administrador obtiene ausencia de información o acceso denegado según corresponda.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.
