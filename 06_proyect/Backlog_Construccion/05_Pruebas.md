# Backlog de Construcción — Pruebas | CNS Hospital Obrero

[Índice general](README.md) · [Orden de creación](00_Indice_de_subida.md) · [Trazabilidad](08_Cobertura_y_trazabilidad.md) · [Flujos](09_Flujos_y_dependencias.md) · [Pruebas](12_Casos_de_prueba.md)

**Total de issues propuestos:** 6. **Código:** pendiente de crear en GitHub; son especificaciones del backlog y no evidencias de implementación.
**Responsables propuestos:** Benjamín, Alex, Matías, Alejandro. Los responsables y puntos se revisan en [07_Estimacion.md](07_Estimacion.md).

Este archivo toma el comportamiento oficial de [CU-01 a CU-20](13_Comportamiento_CU.md) y la [matriz de requisitos](08_Cobertura_y_trazabilidad.md). No sustituye la aprobación de arquitectura del Sprint 2.

## Distribución por Sprint

| Sprint | Issues |
|---|---|
| S3 (02/11–13/11/2026) | [CON-09](05_Pruebas.md#con-09) |
| S4 (16/11–27/11/2026) | [CON-20](05_Pruebas.md#con-20) |
| S6 (14/12–23/12/2026) | [CON-31](05_Pruebas.md#con-31) · [CON-32](05_Pruebas.md#con-32) · [CON-33](05_Pruebas.md#con-33) · [CON-36](05_Pruebas.md#con-36) |

---

<a id="con-09"></a>
### [CON-09][Pruebas] Configurar CI y pruebas base Java/Flutter

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Benjamín. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-09` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-01](02_Java.md#con-01) · [CON-04](01_Flutter.md#con-04)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`  
**Rama de trabajo (base `develop`):** `feature/con-09-configurar-ci-y-pruebas-base-java-flutter` (crear solo cuando se abra el issue).

**Descripción:** Configurar CI y pruebas base Java/Flutter. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Criterios de aceptación**
- [ ] Un Pull Request hacia develop compila backend y frontend y ejecuta JUnit y pruebas Flutter automáticamente.
- [ ] Una prueba fallida impide dar el PR por validado; el flujo registra resultados consultables por el equipo.
- [ ] Variables y secretos se obtienen de configuración segura, sin publicarse en los logs.

**Tareas**
- [ ] Configurar GitHub Actions y comandos de test reproducibles.
- [ ] Documentar reglas de revisión y evidencia mínima por Issue.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-09A](12_Casos_de_prueba.md#cp-cns-09a) — Crear PR temporal con test que falla y comprobar que el check queda fallido.
- [ ] [CP-CNS-09B](12_Casos_de_prueba.md#cp-cns-09b) — Ejecutar pipeline exitoso con ejemplos JUnit y widget test.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-20"></a>
### [CON-20][Pruebas] Pruebas integradas de solicitud, asignación, reprogramación y cancelación

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Benjamín. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-20` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-13](02_Java.md#con-13) · [CON-16](02_Java.md#con-16)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`  
**Rama de trabajo (base `develop`):** `feature/con-20-pruebas-integradas-de-solicitud-asignacion-repro` (crear solo cuando se abra el issue).

**Descripción:** Pruebas integradas de solicitud, asignación, reprogramación y cancelación. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-04 / CU-04 — Solicitar cita médica](13_Comportamiento_CU.md#cu-04) · [HU-05 / CU-05 — Asignar cita automáticamente](13_Comportamiento_CU.md#cu-05) · [HU-06 / CU-06 — Validar disponibilidad de Recursos](13_Comportamiento_CU.md#cu-06) · [HU-07 / CU-07 — Registrar cita confirmada](13_Comportamiento_CU.md#cu-07) · [HU-10 / CU-10 — Reprogramar cita](13_Comportamiento_CU.md#cu-10) · [HU-11 / CU-11 — Cancelar cita](13_Comportamiento_CU.md#cu-11).
**Requisitos funcionales asociados:** RF-04, RF-05, RF-06, RF-07, RF-10, RF-11.
**RNF aplicables:** RNF-03, RNF-04.

**Criterios de aceptación**
- [ ] Pruebas contra PostgreSQL y API cubren CU-04→CU-05→CU-06→CU-07, CU-10 y CU-11.
- [ ] Pruebas felices y alternativas verifican ausencia de cupos, reprogramación conflictiva, cancelación sin motivo y simultaneidad de reservas.
- [ ] Ningún caso de prueba crea registros incompletos o dobles reservas tras un fallo.
- [ ] Evidencia de ejecución y defectos detectados queda vinculada a RF/CU en trazabilidad.

**Tareas**
- [ ] Preparar fixture con especialistas, médicos, consultorios y franjas sintéticas.
- [ ] Ejecutar pruebas JUnit/integración concurrentes e incorporar evidencias.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-20A](12_Casos_de_prueba.md#cp-cns-20a) — Solicitud + asignación + confirmación + reprogramación + cancelación mantienen historial y consistencia.
- [ ] [CP-CNS-20B](12_Casos_de_prueba.md#cp-cns-20b) — Ejecutar peticiones concurrentes sobre un solo cupo y comprobar que no se duplican reservas.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-31"></a>
### [CON-31][Pruebas] Ejecutar pruebas extremo a extremo de los veinte casos de uso

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 6 - Validacion y entrega (14/12–23/12/2026).
**Responsable sugerido:** Benjamín. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-31` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-20](05_Pruebas.md#con-20) · [CON-23](01_Flutter.md#con-23) · [CON-24](02_Java.md#con-24) · [CON-25](02_Java.md#con-25) · [CON-29](01_Flutter.md#con-29) · [CON-30](06_Servidor.md#con-30)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`  
**Rama de trabajo (base `develop`):** `feature/con-31-ejecutar-pruebas-extremo-a-extremo-de-los-veinte` (crear solo cuando se abra el issue).

**Descripción:** Ejecutar pruebas extremo a extremo de los veinte casos de uso. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-01 / CU-01 — Iniciar Sesión](13_Comportamiento_CU.md#cu-01) · [HU-02 / CU-02 — Gestionar usuarios roles y permisos](13_Comportamiento_CU.md#cu-02) · [HU-03 / CU-03 — Registrar y consultar pacientes](13_Comportamiento_CU.md#cu-03) · [HU-04 / CU-04 — Solicitar cita médica](13_Comportamiento_CU.md#cu-04) · [HU-05 / CU-05 — Asignar cita automáticamente](13_Comportamiento_CU.md#cu-05) · [HU-06 / CU-06 — Validar disponibilidad de Recursos](13_Comportamiento_CU.md#cu-06) · [HU-07 / CU-07 — Registrar cita confirmada](13_Comportamiento_CU.md#cu-07) · [HU-08 / CU-08 — Consultar cita paciente](13_Comportamiento_CU.md#cu-08) · [HU-09 / CU-09 — Consultar citas registradas](13_Comportamiento_CU.md#cu-09) · [HU-10 / CU-10 — Reprogramar cita](13_Comportamiento_CU.md#cu-10) · [HU-11 / CU-11 — Cancelar cita](13_Comportamiento_CU.md#cu-11) · [HU-12 / CU-12 — Actualizar estado de cita](13_Comportamiento_CU.md#cu-12) · [HU-13 / CU-13 — Consultar agenda médica](13_Comportamiento_CU.md#cu-13) · [HU-14 / CU-14 — Consultar información permitida del paciente](13_Comportamiento_CU.md#cu-14) · [HU-15 / CU-15 — Registrar datos de atención](13_Comportamiento_CU.md#cu-15) · [HU-16 / CU-16 — Generar notificación](13_Comportamiento_CU.md#cu-16) · [HU-17 / CU-17 — Generar recordatorio de cita](13_Comportamiento_CU.md#cu-17) · [HU-18 / CU-18 — Consultar historial de cambios](13_Comportamiento_CU.md#cu-18) · [HU-19 / CU-19 — Generar reportes de citas](13_Comportamiento_CU.md#cu-19) · [HU-20 / CU-20 — Consultar indicadores de gestión](13_Comportamiento_CU.md#cu-20).
**Requisitos funcionales asociados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20.
**RNF aplicables:** RNF-01, RNF-02, RNF-03, RNF-04, RNF-05, RNF-06.

**Criterios de aceptación**
- [ ] Existe evidencia de ejecución del camino feliz y al menos un flujo alternativo para CU-01 a CU-20, en el alcance aprobado.
- [ ] Los casos de uso automáticos CU-05, CU-06, CU-07, CU-16 y CU-17 se prueban como procesos del backend y no se consideran pantallas independientes obligatorias.
- [ ] Cada fallo tiene issue de bug y los resultados quedan en 05_pro/ con vínculos al requisito, caso de uso, issue de desarrollo y PR.
- [ ] La conclusión no presenta como 'aprobados' casos con reglas institucionales aún sin definir; quedan marcados bloqueados o pendientes.

**Tareas**
- [ ] Ejecutar matriz E2E por rol y los veinte casos de uso con datos sintéticos.
- [ ] Adjuntar evidencias y registrar defectos y pendientes.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-31A](12_Casos_de_prueba.md#cp-cns-31a) — Ejecutar flujo completo paciente solicita → Java asigna → registra → médico consulta → administración reprograma.
- [ ] [CP-CNS-31B](12_Casos_de_prueba.md#cp-cns-31b) — Ejecutar por lo menos un rechazo documentado por cada rol o caso de uso según la especificación.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-32"></a>
### [CON-32][Pruebas] Medir concurrencia, rendimiento y robustez del núcleo Java

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 6 - Validacion y entrega (14/12–23/12/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-32` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-30](06_Servidor.md#con-30)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`  
**Rama de trabajo (base `develop`):** `feature/con-32-medir-concurrencia-rendimiento-y-robustez-del-nu` (crear solo cuando se abra el issue).

**Descripción:** Medir concurrencia, rendimiento y robustez del núcleo Java. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.
**RNF aplicables:** RNF-03, RNF-04, RNF-06.

**Criterios de aceptación**
- [ ] Se miden tiempos de operaciones comunes con objetivo inicial aproximado de 2 segundos bajo uso normal, según la Visión; se documenta entorno y percentiles medidos.
- [ ] Escenarios simultáneos compiten por cupos limitados y ningún médico/consultorio queda doblemente reservado (RNF-03, RNF-04).
- [ ] Se verifican tiempos y ausencia de pérdida de integridad cuando WebSocket cae o cuando la API recibe solicitudes inválidas.
- [ ] Volumen de usuarios concurrentes y umbrales definitivos se acuerdan antes de declarar resultados como cumplimiento final.

**Tareas**
- [ ] Preparar carga sintética sobre API de Java y fixture controlado en PostgreSQL.
- [ ] Ejecutar escenarios base y creciente, recoger latencias e inconsistencias.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-32A](12_Casos_de_prueba.md#cp-cns-32a) — Cien solicitudes simultáneas de pruebas por cupos limitados nunca generan doble asignación.
- [ ] [CP-CNS-32B](12_Casos_de_prueba.md#cp-cns-32b) — Medir latencia de consulta/asignación en entorno descrito y contrastar con objetivo aproximado de 2 segundos.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-33"></a>
### [CON-33][Pruebas] Auditar permisos, privacidad y configuración de seguridad

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 6 - Validacion y entrega (14/12–23/12/2026).
**Responsable sugerido:** Matías. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-33` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-03](02_Java.md#con-03) · [CON-25](02_Java.md#con-25) · [CON-30](06_Servidor.md#con-30)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`  
**Rama de trabajo (base `develop`):** `feature/con-33-auditar-permisos-privacidad-y-configuracion-de-s` (crear solo cuando se abra el issue).

**Descripción:** Auditar permisos, privacidad y configuración de seguridad. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.
**RNF aplicables:** RNF-01, RNF-02.

**Criterios de aceptación**
- [ ] CU-01/CU-02 y RNF-01/RNF-02 aplican acceso estricto por identidad, rol y relación con paciente en REST y WS.
- [ ] No se exponen credenciales, tokens, detalles médicos o datos personales en registros de aplicaciones o respuestas no autorizadas.
- [ ] Se prueban cuatro perfiles de rol y sesiones inválidas con reporte de problemas reproducibles y seguimiento.
- [ ] Se documentan controles mínimos de protección y privacidad del entorno de pruebas; datos reales no se incorporan.

**Tareas**
- [ ] Ejecutar matriz de accesos cruzados, sesión inválida y reconexión WS.
- [ ] Revisar configuración, trazas y secretos; levantar issues de corrección.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-33A](12_Casos_de_prueba.md#cp-cns-33a) — Paciente A intenta consultar cita de paciente B por API y WS y ambos accesos quedan bloqueados.
- [ ] [CP-CNS-33B](12_Casos_de_prueba.md#cp-cns-33b) — Acceso administrativo con cuenta médica falla sin revelar indicadores ni datos de pacientes ajenos.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-36"></a>
### [CON-36][Pruebas] Cerrar trazabilidad, checklist y versión de entrega

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 6 - Validacion y entrega (14/12–23/12/2026).
**Responsable sugerido:** Alejandro. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-36` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-31](05_Pruebas.md#con-31) · [CON-32](05_Pruebas.md#con-32) · [CON-33](05_Pruebas.md#con-33) · [CON-34](01_Flutter.md#con-34) · [CON-35](06_Servidor.md#con-35)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`  
**Rama de trabajo (base `develop`):** `feature/con-36-cerrar-trazabilidad-checklist-y-version-de-entre` (crear solo cuando se abra el issue).

**Descripción:** Cerrar trazabilidad, checklist y versión de entrega. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Criterios de aceptación**
- [ ] La tabla de cobertura RF/CU/HU y casos de prueba presenta el estado real de los veinte casos y links a issues/PR.
- [ ] No se da por terminado un CU que carece de evidencia de sus procesos Java, Flutter y pruebas correspondientes.
- [ ] Documentación de arquitectura, API, configuración y README coincide con el código fusionado en develop.
- [ ] Las reglas pendientes de validación institucional se documentan como tales; no se declaran resueltas sin evidencia.

**Tareas**
- [ ] Revisar links, trazabilidad, dependencias, resultados, Milestones y estado de PR.
- [ ] Preparar checklist de entrega, versión y guía mínima de instalación/uso.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-36A](12_Casos_de_prueba.md#cp-cns-36a) — Auditoría final encuentra enlaces y evidencias de los 20 CU o explicación explícita de bloqueos.
- [ ] [CP-CNS-36B](12_Casos_de_prueba.md#cp-cns-36b) — Instalar versión de entrega sobre entorno limpio siguiendo README y ejecutar smoke test de cita.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.
