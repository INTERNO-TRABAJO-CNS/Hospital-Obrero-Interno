# Backlog de Construcción — Flutter

[Volver al índice](README.md) · Responsable indicado **como propuesta**, validar carga al iniciar cada sprint.

### [CON-04][Flutter] Crear estructura base de aplicación Flutter

**Identificador:** `CON-04` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-01, RF-03, RF-04, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-03, CU-04, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** ELA-02, ELA-06  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Preparar aplicación Flutter con navegación por roles y cliente API centralizado.

**Criterios de aceptación**
- [ ] App compila en Android y cuenta con navegación principal por rol.
- [ ] Pantallas no acceden directamente a PostgreSQL ni ejecutan reglas de negocio.
- [ ] Cliente HTTP configura ambientes y manejo consistente de errores.

**Tareas**
- [ ] Crear proyecto Flutter y carpetas UI/estado/red.
- [ ] Implementar rutas y componentes reutilizables.

**Caso de prueba sugerido:** Prueba widget de navegación y error de conectividad.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-04-crear-estructura-base-de-aplicacin-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-07][Flutter] Crear pantalla de inicio de sesión y sesión en Flutter

**Identificador:** `CON-07` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-01  
**Casos de uso relacionados:** CU-01  
**Depende de (referencias del plan):** CON-03, CON-04  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Conectar pantalla y flujo de inicio de sesión con la API protegida.

**Criterios de aceptación**
- [ ] Inicio correcto abre navegación permitida al rol.
- [ ] Credenciales inválidas muestran error y no desbloquean pantallas.
- [ ] Sesión se almacena de forma segura según el mecanismo aprobado.

**Tareas**
- [ ] Crear vista y controlador de autenticación.
- [ ] Probar navegación protegida y cierre de sesión.

**Caso de prueba sugerido:** Inicio inválido no permite entrar a la agenda.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-07-crear-pantalla-de-inicio-de-sesin-y` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-08][Flutter] Crear pantallas Flutter de registro y consulta de pacientes

**Identificador:** `CON-08` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Alex · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-03  
**Casos de uso relacionados:** CU-03  
**Depende de (referencias del plan):** CON-04, CON-06  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Conectar formularios de pacientes con el backend usando datos de prueba.

**Criterios de aceptación**
- [ ] Formulario valida obligatorios y presenta los errores del backend.
- [ ] Lista o ficha solo muestra datos autorizados por rol.
- [ ] No se almacena información de pacientes en logs del dispositivo.

**Tareas**
- [ ] Crear formularios y vistas.
- [ ] Probar cliente REST con respuestas simuladas y reales.

**Caso de prueba sugerido:** Buscar paciente inexistente ofrece resultado controlado, sin cierre de app.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-08-crear-pantallas-flutter-de-registro` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-17][Flutter] Diseñar e implementar pantallas paciente para solicitar y consultar citas

**Identificador:** `CON-17` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-04, RF-08  
**Casos de uso relacionados:** CU-04, CU-08  
**Depende de (referencias del plan):** CON-04, CON-13, CON-14  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Implementar flujo del paciente desde especialidad hasta confirmación y consulta.

**Criterios de aceptación**
- [ ] Pantallas siguen prototipo v1 y contratos REST.
- [ ] El usuario recibe estado claro ante falta de cupos o error.
- [ ] No promete cita hasta confirmación del backend.

**Tareas**
- [ ] Construir formularios y vista de resultado.
- [ ] Conectar API y pruebas widget.

**Caso de prueba sugerido:** Una solicitud sin cupo muestra mensaje de disponibilidad, no éxito.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-17-disear-e-implementar-pantallas-paci` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-18][Flutter] Implementar vistas de admisión para citas, reprogramación y cancelación

**Identificador:** `CON-18` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 4 - Gestion de citas  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-09, RF-10, RF-11  
**Casos de uso relacionados:** CU-09, CU-10, CU-11  
**Depende de (referencias del plan):** CON-15, CON-16  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Crear vistas del personal de admisión con permisos y confirmaciones antes de modificar citas.

**Criterios de aceptación**
- [ ] Consultas y cambios visibles solo a rol autorizado.
- [ ] Reprogramar y cancelar actualizan estado al obtener respuesta REST.
- [ ] Errores de conflicto se presentan sin pérdida de datos del formulario.

**Tareas**
- [ ] Crear tabla/lista, filtros y formularios.
- [ ] Crear pruebas para conflictos de disponibilidad.

**Caso de prueba sugerido:** Cambio de cita con cupo ya ocupado informa conflicto.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-18-implementar-vistas-de-admisin-para-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-23][Flutter] Integrar notificaciones y refresco de citas en Flutter

**Identificador:** `CON-23` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-07, RF-08, RF-10, RF-11, RF-16, RF-17  
**Casos de uso relacionados:** CU-07, CU-08, CU-10, CU-11, CU-16, CU-17  
**Depende de (referencias del plan):** CON-22, CON-17  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Actualizar vistas cuando llegan eventos y consultar REST para recuperar el estado si hubo desconexión.

**Criterios de aceptación**
- [ ] Vista actualiza confirmación, cambio y cancelación sin recargar manualmente cuando hay conexión.
- [ ] Reconexión obtiene estado real por API.
- [ ] No muestra datos de otros usuarios.

**Tareas**
- [ ] Crear cliente WebSocket/estado UI.
- [ ] Probar pérdida de red y reconexión.

**Caso de prueba sugerido:** Cambiar cita mientras app estaba desconectada se refleja al reconectar.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-23-integrar-notificaciones-y-refresco-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-28][Flutter] Crear vistas del médico para agenda y registro de atención

**Identificador:** `CON-28` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-12, RF-13, RF-14, RF-15  
**Casos de uso relacionados:** CU-12, CU-13, CU-14, CU-15  
**Depende de (referencias del plan):** CON-19, CON-25  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Completar flujo médico de consulta de agenda y atención.

**Criterios de aceptación**
- [ ] Pantallas respetan diseño y roles.
- [ ] Información permitida se muestra únicamente al médico autorizado.
- [ ] No se almacena contenido clínico sensible en logs.

**Tareas**
- [ ] Crear vistas de agenda y ficha permitida.
- [ ] Conectar actualización de atención.

**Caso de prueba sugerido:** Médico no autorizado no abre ficha por URL interna.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-28-crear-vistas-del-mdico-para-agenda-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-29][Flutter] Crear vistas de administración para historial, reportes e indicadores

**Identificador:** `CON-29` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** CON-26, CON-27  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Habilitar consulta administrativa de historial y métricas.

**Criterios de aceptación**
- [ ] Vistas muestran filtros, valores y estados de error.
- [ ] Solo rol autorizado accede.
- [ ] Cifras coinciden con API.

**Tareas**
- [ ] Crear panel con tablas/indicadores.
- [ ] Agregar pruebas visuales del estado sin datos.

**Caso de prueba sugerido:** Indicador se actualiza con resultado nuevo de la API.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-29-crear-vistas-de-administracin-para-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-34][Flutter] Ajustar validaciones, accesibilidad y estabilidad de Flutter

**Identificador:** `CON-34` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 6 - Validacion y entrega  
**Responsable sugerido:** Abigal · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Flutter  
**Requisitos relacionados:** RF-01, RF-03, RF-04, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-14, RF-15, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-03, CU-04, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-14, CU-15, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** CON-23, CON-28, CON-29  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:flutter`

**Descripción:** Corregir problemas detectados en la demo de distintos roles.

**Criterios de aceptación**
- [ ] Formularios muestran mensajes y estados claros.
- [ ] Rutas de navegación y errores no bloquean la app.
- [ ] Pruebas de widget y smoke test Android pasan.

**Tareas**
- [ ] Corregir errores y revisar componentes UI.
- [ ] Ejecutar smoke test por perfil.

**Caso de prueba sugerido:** Pérdida temporal de conexión no cierra la aplicación.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-34-ajustar-validaciones-accesibilidad-` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).
