# Backlog de Construcción — Flutter | CNS Hospital Obrero

[Índice general](README.md) · [Orden de creación](00_Indice_de_subida.md) · [Trazabilidad](08_Cobertura_y_trazabilidad.md) · [Flujos](09_Flujos_y_dependencias.md) · [Pruebas](12_Casos_de_prueba.md)

**Total de issues propuestos:** 9. **Código:** pendiente de crear en GitHub; son especificaciones del backlog y no evidencias de implementación.
**Responsables propuestos:** Abigal, Alex. Los responsables y puntos se revisan en [07_Estimacion.md](07_Estimacion.md).

Este archivo toma el comportamiento oficial de [CU-01 a CU-20](13_Comportamiento_CU.md) y la [matriz de requisitos](08_Cobertura_y_trazabilidad.md). No sustituye la aprobación de arquitectura del Sprint 2.

## Distribución por Sprint

| Sprint | Issues |
|---|---|
| S3 (02/11–13/11/2026) | [CON-04](01_Flutter.md#con-04) · [CON-07](01_Flutter.md#con-07) · [CON-08](01_Flutter.md#con-08) |
| S4 (16/11–27/11/2026) | [CON-17](01_Flutter.md#con-17) · [CON-18](01_Flutter.md#con-18) |
| S5 (30/11–11/12/2026) | [CON-23](01_Flutter.md#con-23) · [CON-28](01_Flutter.md#con-28) · [CON-29](01_Flutter.md#con-29) |
| S6 (14/12–23/12/2026) | [CON-34](01_Flutter.md#con-34) |

---

<a id="con-04"></a>
### [CON-04][Flutter] Crear estructura base de aplicación Flutter

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-04` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [ELA-02](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md) · [ELA-06](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`  
**Rama de trabajo (base `develop`):** `feature/con-04-crear-estructura-base-de-aplicacion-flutter` (crear solo cuando se abra el issue).

**Descripción:** Crear estructura base de aplicación Flutter. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Criterios de aceptación**
- [ ] La aplicación Flutter compila en Android y separa vistas/presentación, controladores de pantalla, cliente REST y gestión de estado siguiendo ELA-02 y ELA-06.
- [ ] Las pantallas no contienen validación definitiva de cupos ni acceso directo a PostgreSQL; esas decisiones pertenecen al backend Java.
- [ ] Una configuración por ambiente selecciona servidor sin modificar el código fuente y la interfaz presenta mensajes en español.
- [ ] La navegación inicial contempla los cuatro roles; las pantallas finales se derivan del diseño UI v1 aprobado, no de suposiciones.

**Tareas**
- [ ] Crear el proyecto, directorios MVC del frontend y gestión de estado acordada.
- [ ] Configurar cliente HTTP central, errores y ambientes de desarrollo/pruebas.
- [ ] Crear navegación inicial y pruebas de widgets.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-04A](12_Casos_de_prueba.md#cp-cns-04a) — Compilar y ejecutar pruebas de widgets desde una instalación limpia.
- [ ] [CP-CNS-04B](12_Casos_de_prueba.md#cp-cns-04b) — Simular backend no disponible y confirmar que la app muestra un error controlado sin cerrarse.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-07"></a>
### [CON-07][Flutter] Crear pantalla de inicio de sesión y sesión en Flutter

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-07` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-03](02_Java.md#con-03) · [CON-04](01_Flutter.md#con-04)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:flutter` · `caso-uso:CU-01`  
**Rama de trabajo (base `develop`):** `feature/con-07-crear-pantalla-de-inicio-de-sesion-y-sesion-en-f` (crear solo cuando se abra el issue).

**Descripción:** Crear pantalla de inicio de sesión y sesión en Flutter. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-01 / CU-01 — Iniciar Sesión](13_Comportamiento_CU.md#cu-01).
**Requisitos funcionales asociados:** RF-01.
**RNF aplicables:** RNF-01, RNF-05.

**Criterios de aceptación**
- [ ] La vista de inicio de sesión captura credenciales conforme al mecanismo aprobado en ELA-05 y no almacena secretos en texto plano.
- [ ] Tras autenticar, Flutter presenta solo la navegación del rol correspondiente (CU-01, RNF-05).
- [ ] Credenciales inválidas muestran rechazo sin permitir acceso ni mostrar datos de sesión anterior.
- [ ] Los intentos de acceder a funciones prohibidas desde enlaces/navegación quedan bloqueados en la vista y por el backend.

**Tareas**
- [ ] Implementar vista y controlador de sesión con cliente REST.
- [ ] Configurar navegación según rol y pruebas con API simulada.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-07A](12_Casos_de_prueba.md#cp-cns-07a) — Ingreso de médico muestra solo pantallas permitidas para ese rol.
- [ ] [CP-CNS-07B](12_Casos_de_prueba.md#cp-cns-07b) — Credenciales rechazadas mantienen la sesión cerrada y las opciones protegidas ocultas.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-08"></a>
### [CON-08][Flutter] Crear pantallas Flutter de registro y consulta de pacientes

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-08` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-04](01_Flutter.md#con-04) · [CON-06](02_Java.md#con-06)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:flutter` · `caso-uso:CU-03`  
**Rama de trabajo (base `develop`):** `feature/con-08-crear-pantallas-flutter-de-registro-y-consulta-d` (crear solo cuando se abra el issue).

**Descripción:** Crear pantallas Flutter de registro y consulta de pacientes. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-03 / CU-03 — Registrar y consultar pacientes](13_Comportamiento_CU.md#cu-03).
**Requisitos funcionales asociados:** RF-03.
**RNF aplicables:** RNF-01, RNF-02, RNF-05.

**Criterios de aceptación**
- [ ] Admisión/enfermería dispone de pantalla para buscar paciente por identificación y ver datos autorizados (CU-03).
- [ ] La interfaz permite completar el registro cuando no hay coincidencias; si existe paciente, muestra el registro existente sin duplicarlo.
- [ ] Errores de campos o conexión no generan registros parciales ni exponen información restringida.
- [ ] El diseño se basa en el UI v1 de ELA-06 y consume exclusivamente API Java.

**Tareas**
- [ ] Implementar pantallas de búsqueda, consulta y registro y controladores.
- [ ] Conectar servicio REST y probar errores de validación/duplicidad.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-08A](12_Casos_de_prueba.md#cp-cns-08a) — Buscar paciente existente abre su ficha sin crear una nueva.
- [ ] [CP-CNS-08B](12_Casos_de_prueba.md#cp-cns-08b) — Registrar paciente con campos faltantes muestra validación y no genera alta.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-17"></a>
### [CON-17][Flutter] Diseñar e implementar pantallas paciente para solicitar y consultar citas

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-17` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-04](01_Flutter.md#con-04) · [CON-13](02_Java.md#con-13) · [CON-14](02_Java.md#con-14)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:flutter` · `caso-uso:CU-04` · `caso-uso:CU-08`  
**Rama de trabajo (base `develop`):** `feature/con-17-disenar-e-implementar-pantallas-paciente-para-so` (crear solo cuando se abra el issue).

**Descripción:** Diseñar e implementar pantallas paciente para solicitar y consultar citas. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-04 / CU-04 — Solicitar cita médica](13_Comportamiento_CU.md#cu-04) · [HU-08 / CU-08 — Consultar cita paciente](13_Comportamiento_CU.md#cu-08).
**Requisitos funcionales asociados:** RF-04, RF-08.
**RNF aplicables:** RNF-03, RNF-05.

**Criterios de aceptación**
- [ ] El paciente selecciona especialidad o atención y envía CU-04 sin elegir manualmente médico, horario ni consultorio.
- [ ] Si se asigna una cita, muestra médico, fecha, hora, consultorio y estado; si no hay disponibilidad, informa el resultado real sin mostrar una confirmación inexistente.
- [ ] La vista Mis citas (CU-08) solo presenta las del paciente autenticado y permite ver su detalle sin editarlo.
- [ ] La navegación sigue el prototipo aprobado en ELA-06 y usa REST como fuente de verdad.

**Tareas**
- [ ] Implementar vista Solicitar cita y controlador con validación de especialidad.
- [ ] Implementar Mis citas/detalle y comunicación API.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-17A](12_Casos_de_prueba.md#cp-cns-17a) — Una solicitud válida solo pide especialidad y muestra la cita que asignó el servidor.
- [ ] [CP-CNS-17B](12_Casos_de_prueba.md#cp-cns-17b) — Paciente sin citas observa pantalla vacía; al intentar consultar otra cita, no recibe sus datos.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-18"></a>
### [CON-18][Flutter] Implementar vistas de admisión para citas, reprogramación y cancelación

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 4 - Gestion de citas (16/11–27/11/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-18` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-15](02_Java.md#con-15) · [CON-16](02_Java.md#con-16)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:flutter` · `caso-uso:CU-09` · `caso-uso:CU-10` · `caso-uso:CU-11`  
**Rama de trabajo (base `develop`):** `feature/con-18-implementar-vistas-de-admision-para-citas-reprog` (crear solo cuando se abra el issue).

**Descripción:** Implementar vistas de admisión para citas, reprogramación y cancelación. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-09 / CU-09 — Consultar citas registradas](13_Comportamiento_CU.md#cu-09) · [HU-10 / CU-10 — Reprogramar cita](13_Comportamiento_CU.md#cu-10) · [HU-11 / CU-11 — Cancelar cita](13_Comportamiento_CU.md#cu-11).
**Requisitos funcionales asociados:** RF-09, RF-10, RF-11.
**RNF aplicables:** RNF-03, RNF-05.

**Criterios de aceptación**
- [ ] Admisión/enfermería puede localizar citas por los filtros de CU-09 desde la interfaz autorizada.
- [ ] La vista de reprogramación presenta resultado de validación vigente; ante conflicto conserva los datos originales.
- [ ] La cancelación exige motivo y, tras confirmarse, presenta estado cancelada sin borrar historial.
- [ ] Todos los cambios usan REST de Java y respetan roles y contrato ELA-05.

**Tareas**
- [ ] Implementar búsqueda/listado y detalle para personal de admisión.
- [ ] Implementar flujos reprogramar/cancelar y manejo de errores.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-18A](12_Casos_de_prueba.md#cp-cns-18a) — Admisión cancela con motivo y la vista muestra el nuevo estado.
- [ ] [CP-CNS-18B](12_Casos_de_prueba.md#cp-cns-18b) — Ante reprogramación sin disponibilidad, la vista conserva la cita anterior y muestra rechazo.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-23"></a>
### [CON-23][Flutter] Integrar notificaciones y refresco de citas en Flutter

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-23` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-22](04_WebSocket.md#con-22) · [CON-17](01_Flutter.md#con-17)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:flutter` · `caso-uso:CU-08` · `caso-uso:CU-16`  
**Rama de trabajo (base `develop`):** `feature/con-23-integrar-notificaciones-y-refresco-de-citas-en-f` (crear solo cuando se abra el issue).

**Descripción:** Integrar notificaciones y refresco de citas en Flutter. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-08 / CU-08 — Consultar cita paciente](13_Comportamiento_CU.md#cu-08) · [HU-16 / CU-16 — Generar notificación](13_Comportamiento_CU.md#cu-16).
**Requisitos funcionales asociados:** RF-08, RF-16.
**RNF aplicables:** RNF-02, RNF-05.

**Criterios de aceptación**
- [ ] Flutter recibe eventos WebSocket para refrescar sus vistas de citas/agenda según el rol y el contrato vigente.
- [ ] El cliente restablece conexión y recupera el estado actual por REST; los eventos no reemplazan la consulta oficial.
- [ ] Solo se presentan notificaciones y datos del usuario autorizado; un evento no autoriza consultar otra cita.
- [ ] Si WebSocket falla, el usuario puede seguir utilizando las funciones REST y visualiza información consistente al consultar.

**Tareas**
- [ ] Crear servicio de WebSocket y controlador de actualización de pantallas.
- [ ] Implementar reconexión y pruebas de consumo de mensajes autorizados.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-23A](12_Casos_de_prueba.md#cp-cns-23a) — Una reprogramación confirmada en admisión actualiza Mis citas del paciente conectado sin refresco manual.
- [ ] [CP-CNS-23B](12_Casos_de_prueba.md#cp-cns-23b) — App desconectada reconecta y recarga citas desde REST sin duplicar registros en pantalla.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-28"></a>
### [CON-28][Flutter] Crear vistas del médico para agenda y registro de atención

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-28` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-19](02_Java.md#con-19) · [CON-25](02_Java.md#con-25)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:flutter` · `caso-uso:CU-12` · `caso-uso:CU-13` · `caso-uso:CU-14` · `caso-uso:CU-15`  
**Rama de trabajo (base `develop`):** `feature/con-28-crear-vistas-del-medico-para-agenda-y-registro-d` (crear solo cuando se abra el issue).

**Descripción:** Crear vistas del médico para agenda y registro de atención. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-12 / CU-12 — Actualizar estado de cita](13_Comportamiento_CU.md#cu-12) · [HU-13 / CU-13 — Consultar agenda médica](13_Comportamiento_CU.md#cu-13) · [HU-14 / CU-14 — Consultar información permitida del paciente](13_Comportamiento_CU.md#cu-14) · [HU-15 / CU-15 — Registrar datos de atención](13_Comportamiento_CU.md#cu-15).
**Requisitos funcionales asociados:** RF-12, RF-13, RF-14, RF-15.
**RNF aplicables:** RNF-01, RNF-02, RNF-05.

**Criterios de aceptación**
- [ ] Médico puede consultar sus citas diarias y semanales y actualizar estados autorizados (CU-12, CU-13).
- [ ] Vista de paciente asignado (CU-14) no muestra campos no autorizados y solo permite acceder a citas propias del médico.
- [ ] Registro de atención (CU-15) captura asistencia, espera y duración válidas y presenta los errores del backend.
- [ ] Las pantallas utilizan diseño aprobado ELA-06 y no calculan estados definitivos ni reglas de acceso por su cuenta.

**Tareas**
- [ ] Implementar agenda diaria/semanal y controladores de rol médico.
- [ ] Implementar detalle autorizado y formulario de atención.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-28A](12_Casos_de_prueba.md#cp-cns-28a) — Médico A consulta su agenda y no ve pacientes de la agenda del médico B.
- [ ] [CP-CNS-28B](12_Casos_de_prueba.md#cp-cns-28b) — Registrar atención con tiempos inválidos muestra rechazo sin borrar datos previos.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-29"></a>
### [CON-29][Flutter] Crear vistas de administración para historial, reportes e indicadores

**Tipo:** Historia de usuario / implementación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-29` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-26](02_Java.md#con-26) · [CON-27](02_Java.md#con-27)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:historia-usuario` · `tec:flutter` · `caso-uso:CU-18` · `caso-uso:CU-19` · `caso-uso:CU-20`  
**Rama de trabajo (base `develop`):** `feature/con-29-crear-vistas-de-administracion-para-historial-re` (crear solo cuando se abra el issue).

**Descripción:** Crear vistas de administración para historial, reportes e indicadores. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Historias y casos de uso cubiertos:** [HU-18 / CU-18 — Consultar historial de cambios](13_Comportamiento_CU.md#cu-18) · [HU-19 / CU-19 — Generar reportes de citas](13_Comportamiento_CU.md#cu-19) · [HU-20 / CU-20 — Consultar indicadores de gestión](13_Comportamiento_CU.md#cu-20).
**Requisitos funcionales asociados:** RF-18, RF-19, RF-20.
**RNF aplicables:** RNF-01, RNF-02, RNF-05.

**Criterios de aceptación**
- [ ] Administrador accede a historial permitido, reportes e indicadores correspondientes a CU-18, CU-19 y CU-20.
- [ ] Las consultas con conjuntos vacíos presentan 'sin información' y no cifras calculadas arbitrariamente.
- [ ] Usuario ajeno a administración no accede a reportes ni indicadores, y las vistas no muestran información personal innecesaria.
- [ ] Los filtros y gráficos siguen ELA-06 y fórmulas aprobadas, no métricas inventadas.

**Tareas**
- [ ] Crear pantallas y controladores de historial, reportes e indicadores.
- [ ] Conectar API y probar roles/vacíos.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-29A](12_Casos_de_prueba.md#cp-cns-29a) — Administrador obtiene reporte por médico filtrado con datos coherentes.
- [ ] [CP-CNS-29B](12_Casos_de_prueba.md#cp-cns-29b) — Paciente intenta acceder a indicadores y no visualiza el módulo ni sus resultados.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-34"></a>
### [CON-34][Flutter] Ajustar validaciones, accesibilidad y estabilidad de Flutter

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 6 - Validacion y entrega (14/12–23/12/2026).
**Responsable sugerido:** Abigal. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-34` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-23](01_Flutter.md#con-23) · [CON-28](01_Flutter.md#con-28) · [CON-29](01_Flutter.md#con-29)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`  
**Rama de trabajo (base `develop`):** `feature/con-34-ajustar-validaciones-accesibilidad-y-estabilidad` (crear solo cuando se abra el issue).

**Descripción:** Ajustar validaciones, accesibilidad y estabilidad de Flutter. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.
**RNF aplicables:** RNF-05.

**Criterios de aceptación**
- [ ] Flutter muestra errores de validación coherentes con el backend y mantiene campos introducidos al recibir un rechazo.
- [ ] Navegación, accesibilidad básica, etiquetas y legibilidad son revisadas en las vistas de los cuatro roles.
- [ ] La app no falla al perder conexión REST o WebSocket y no muestra citas confirmadas si Java rechazó la operación.
- [ ] Pruebas de widgets para principales pantallas ejecutan con un solo comando.

**Tareas**
- [ ] Corregir errores y mejorar componentes compartidos de formularios y navegación.
- [ ] Ejecutar pruebas de accesibilidad/estabilidad con fallos simulados.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-34A](12_Casos_de_prueba.md#cp-cns-34a) — Red caída durante solicitud muestra error sin confirmar cita inexistente.
- [ ] [CP-CNS-34B](12_Casos_de_prueba.md#cp-cns-34b) — Pruebas de widgets verifican lectura, navegación y mensajes de error de vistas principales.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.
