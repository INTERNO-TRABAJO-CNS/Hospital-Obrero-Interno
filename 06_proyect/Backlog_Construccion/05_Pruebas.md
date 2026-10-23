# Backlog de Construcción — Pruebas

[Volver al índice](README.md) · Responsable indicado **como propuesta**, validar carga al iniciar cada sprint.

### [CON-09][Pruebas] Configurar CI y pruebas base Java/Flutter

**Identificador:** `CON-09` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Pruebas  
**Requisitos relacionados:** RF-01, RF-02, RF-03  
**Casos de uso relacionados:** CU-01, CU-02, CU-03  
**Depende de (referencias del plan):** CON-01, CON-04  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:pruebas`

**Descripción:** Crear comprobaciones automáticas mínimas en Pull Requests hacia develop.

**Criterios de aceptación**
- [ ] Pipeline valida compilación y pruebas automatizadas.
- [ ] No se imprimen secretos en logs.
- [ ] README explica ejecución local y dependencias.

**Tareas**
- [ ] Configurar GitHub Actions.
- [ ] Agregar ejemplo de pruebas por tecnología.

**Caso de prueba sugerido:** PR con prueba fallida muestra check fallido.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-09-configurar-ci-y-pruebas-base-javafl` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-20][Pruebas] Pruebas integradas de solicitud, asignación, reprogramación y cancelación

**Identificador:** `CON-20` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Pruebas  
**Requisitos relacionados:** RF-04, RF-05, RF-06, RF-07, RF-10, RF-11  
**Casos de uso relacionados:** CU-04, CU-05, CU-06, CU-07, CU-10, CU-11  
**Depende de (referencias del plan):** CON-13, CON-16  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:pruebas`

**Descripción:** Validar los flujos nucleares en backend y BD con datos sintéticos.

**Criterios de aceptación**
- [ ] Casos felices, errores y concurrencia tienen pruebas repetibles.
- [ ] Las pruebas se ejecutan sin datos productivos.
- [ ] Se registran defectos como issues separados.

**Tareas**
- [ ] Preparar integración con PostgreSQL de pruebas.
- [ ] Ejecutar y documentar escenarios.

**Caso de prueba sugerido:** Solicitud concurrente seguida de cancelación preserva integridad.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-20-pruebas-integradas-de-solicitud-asi` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-31][Pruebas] Ejecutar pruebas extremo a extremo de los veinte casos de uso

**Identificador:** `CON-31` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 6 - Validacion y entrega  
**Responsable sugerido:** Benjamín · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Pruebas  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** CON-20, CON-23, CON-24, CON-25, CON-29, CON-30  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`

**Descripción:** Validar la trazabilidad de CU-01 a CU-20 con evidencia de ejecución.

**Criterios de aceptación**
- [ ] Cada CU tiene prueba o justificación de pendiente.
- [ ] Bugs se abren como issues separados.
- [ ] Evidencia y resultado se guardan en 05_pro.

**Tareas**
- [ ] Preparar guiones por actor.
- [ ] Correr pruebas en ambiente aislado.

**Caso de prueba sugerido:** Cubrir flujo solicitud → asignación → cancelación → notificación.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-31-ejecutar-pruebas-extremo-a-extremo-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-32][Pruebas] Medir concurrencia, rendimiento y robustez del núcleo Java

**Identificador:** `CON-32` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 6 - Validacion y entrega  
**Responsable sugerido:** Alex · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Pruebas  
**Requisitos relacionados:** RF-04, RF-05, RF-06, RF-07, RF-10, RF-11  
**Casos de uso relacionados:** CU-04, CU-05, CU-06, CU-07, CU-10, CU-11  
**Depende de (referencias del plan):** CON-21, CON-30  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`

**Descripción:** Comprobar reserva de cupos bajo carga y degradación controlada.

**Criterios de aceptación**
- [ ] Se mide tasa de conflictos, duplicados y tiempo de respuesta con umbrales acordados.
- [ ] No existen reservas duplicadas al finalizar la ejecución.
- [ ] Resultados y condiciones de prueba se documentan.

**Tareas**
- [ ] Definir escenarios con peticiones concurrentes.
- [ ] Ejecutar mediciones y revisar cuellos de botella.

**Caso de prueba sugerido:** 100 solicitudes sintéticas compiten por cupos limitados sin inconsistencias.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-32-medir-concurrencia-rendimiento-y-ro` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-33][Pruebas] Auditar permisos, privacidad y configuración de seguridad

**Identificador:** `CON-33` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 6 - Validacion y entrega  
**Responsable sugerido:** Matías · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Pruebas  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-08, RF-09, RF-14, RF-15, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-08, CU-09, CU-14, CU-15, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** CON-03, CON-25, CON-30  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`

**Descripción:** Realizar pruebas negativas por rol y revisar exposición de datos.

**Criterios de aceptación**
- [ ] No hay tokens ni credenciales en Git o logs.
- [ ] Las API/WS exigen permisos coherentes.
- [ ] Informe breve de hallazgos y corrección asociada.

**Tareas**
- [ ] Probar accesos cruzados y sesión vencida.
- [ ] Revisar configuración y documentación de privacidad.

**Caso de prueba sugerido:** Paciente intenta obtener consulta administrativa y se rechaza.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-33-auditar-permisos-privacidad-y-confi` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-36][Pruebas] Cerrar trazabilidad, checklist y versión de entrega

**Identificador:** `CON-36` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 6 - Validacion y entrega  
**Responsable sugerido:** Alejandro · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Pruebas  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-16, RF-17, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-16, CU-17, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** CON-31, CON-32, CON-33, CON-34, CON-35  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:pruebas`

**Descripción:** Preparar release del CNS con evidencias y pendientes conocidos.

**Criterios de aceptación**
- [ ] Matriz RF-CU-issues-pruebas actualizada.
- [ ] Solo se declara completado lo efectivamente probado.
- [ ] Versión de entrega y manual de ejecución quedan registrados.

**Tareas**
- [ ] Revisar QA y PR fusionados.
- [ ] Preparar demo y release notes.

**Caso de prueba sugerido:** Trazabilidad completa sin enlaces rotos ni issues abiertos declarados terminados.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-36-cerrar-trazabilidad-checklist-y-ver` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).
