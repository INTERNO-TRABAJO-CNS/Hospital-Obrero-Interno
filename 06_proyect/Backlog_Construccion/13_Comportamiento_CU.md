# Comportamiento oficial de CU-01 a CU-20 — referencia para los issues

[Volver al backlog](README.md) · [Cobertura](08_Cobertura_y_trazabilidad.md) · [Diagramas](11_Fuentes_y_diagramas.md)

Este archivo **reproduce únicamente las tablas de escenarios y alternativas** de la *Especificación de Casos de Uso CNS* suministrada (borrador). Se usa como puente de consulta; no reemplaza el documento original de `02_req`. Las decisiones no incluidas en esas fichas permanecen pendientes.

Un caso de uso puede ser proceso interno del sistema sin requerir pantalla propia (por ejemplo, CU-05, CU-06, CU-07, CU-16 y CU-17).


<a id="cu-01"></a>
## CU-01 — Iniciar Sesión

**Actores en la especificación:** Paciente / Enfermería-Admisión / Médico / Administrador. **Correspondencia documental:** RF-01, HU-01 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-03](02_Java.md#con-03) · [CON-07](01_Flutter.md#con-07) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-01).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El usuario solicita iniciar sesión e ingresa sus credenciales. | El sistema recibe las credenciales y comienza la validación. |
| 2 | El usuario confirma el ingreso. | El sistema valida la identidad del usuario y determina su rol. |
| 3 | — | El sistema aplica los permisos asociados al rol autenticado. |
| 4 | El usuario accede al sistema. | El sistema muestra únicamente las funciones e información autorizadas para ese rol. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | Las credenciales no son válidas. | El sistema rechaza el acceso e informa que no fue posible autenticar al usuario. El caso de uso finaliza sin iniciar sesión. |
| 3a | El usuario intenta acceder a una función para la que no posee permiso. | El sistema impide el acceso a la función restringida y mantiene únicamente las opciones autorizadas. |


<a id="cu-02"></a>
## CU-02 — Gestionar usuarios roles y permisos

**Actores en la especificación:** Administrador. **Correspondencia documental:** RF-02, HU-02 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-05](02_Java.md#con-05) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-02).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El administrador abre la gestión de usuarios y roles. | El sistema muestra los usuarios y roles disponibles para administración. |
| 2 | El administrador selecciona un usuario o la opción de gestión correspondiente. | El sistema presenta la información y permisos actuales asociados. |
| 3 | El administrador registra o modifica el rol y los permisos. | El sistema valida los cambios solicitados. |
| 4 | El administrador confirma la operación. | El sistema guarda los cambios y aplica los permisos correspondientes. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | El usuario que intenta gestionar permisos no posee rol de administrador. | El sistema impide el acceso a la función de administración. |
| 3a | La configuración solicitada no puede validarse. | El sistema no guarda los cambios y solicita corregir la información antes de confirmar. |


<a id="cu-03"></a>
## CU-03 — Registrar y consultar pacientes

**Actores en la especificación:** Personal de Enfermería / Admisión. **Correspondencia documental:** RF-03, HU-03 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-06](02_Java.md#con-06) · [CON-08](01_Flutter.md#con-08) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-03).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal ingresa la identificación del paciente. | El sistema busca si existe un paciente con esa identificación. |
| 2 | Si no existe, el personal solicita registrar al paciente. | El sistema habilita el registro de la información necesaria. |
| 3 | El personal completa la información requerida y confirma. | El sistema valida nuevamente que no exista duplicidad. |
| 4 | — | El sistema registra al paciente y confirma la operación. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1ª | El paciente ya se encuentra registrado. | El sistema muestra el registro existente y evita crear un duplicado. El flujo continúa con la consulta del paciente. |
| 3a | La información necesaria para el registro está incompleta o no puede validarse. | El sistema no registra al paciente y solicita completar o corregir la información. |


<a id="cu-04"></a>
## CU-04 — Solicitar cita médica

**Actores en la especificación:** Paciente. **Correspondencia documental:** RF-04, HU-04 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-13](02_Java.md#con-13) · [CON-17](01_Flutter.md#con-17) · [CON-20](05_Pruebas.md#con-20) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-04).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El paciente accede a la opción de solicitar cita. | El sistema muestra las especialidades o atenciones disponibles para solicitud. |
| 2 | El paciente selecciona la especialidad o atención requerida. | El sistema registra la selección para la solicitud. |
| 3 | El paciente confirma la solicitud. | El sistema registra la solicitud sin exigir seleccionar médico, fecha, hora o consultorio. |
| 4 | — | El sistema deriva la solicitud al proceso de asignación automática de cita (CU-05). |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El paciente no selecciona una especialidad o atención válida. | El sistema no permite confirmar la solicitud y solicita completar la selección. |
| 4a | No existe disponibilidad que permita una asignación inmediata. | El sistema no confirma una cita con recursos inexistentes u ocupados e informa que no fue posible completar la asignación. |


<a id="cu-05"></a>
## CU-05 — Asignar cita automáticamente

**Actores en la especificación:** Sistema (proceso automático de asignación). **Correspondencia documental:** RF-05, HU-05 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-10](02_Java.md#con-10) · [CON-12](02_Java.md#con-12) · [CON-20](05_Pruebas.md#con-20) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-05).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema recibe una solicitud válida con la especialidad requerida. |
| 2 | — | El sistema consulta la disponibilidad vigente de médicos, horarios y consultorios. |
| 3 | — | El sistema valida la combinación de recursos mediante CU-06. |
| 4 | — | El sistema selecciona una combinación disponible de médico, fecha, hora y consultorio. |
| 5 | — | El sistema envía la asignación validada al registro de cita confirmada (CU-07). |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | No existen recursos disponibles para la especialidad requerida. | El sistema no confirma una cita y comunica que no existe una asignación disponible con la información vigente. |
| 3a | Un recurso deja de estar disponible durante el procesamiento. | El sistema descarta esa combinación y continúa evaluando otra disponibilidad compatible, sin duplicar el recurso ocupado. |


<a id="cu-06"></a>
## CU-06 — Validar disponibilidad de Recursos

**Actores en la especificación:** Sistema (proceso de validación). **Correspondencia documental:** RF-06, HU-06 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-10](02_Java.md#con-10) · [CON-11](02_Java.md#con-11) · [CON-20](05_Pruebas.md#con-20) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-06).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema recibe una combinación candidata de médico, fecha, hora y consultorio. |
| 2 | — | El sistema consulta el estado vigente del médico y horario. |
| 3 | — | El sistema consulta el estado vigente del consultorio. |
| 4 | — | El sistema verifica que no exista otra cita incompatible o solapada. |
| 5 | — | El sistema devuelve el resultado de disponibilidad al proceso que solicitó la validación. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El médico o el horario ya se encuentra ocupado. | El sistema marca la combinación como no disponible y evita su confirmación. |
| 3a | El consultorio ya se encuentra ocupado. | El sistema marca la combinación como no disponible y evita su confirmación. |
| 4a | Dos solicitudes intentan confirmar el mismo recurso y horario. | El sistema permite que únicamente una confirme el recurso; la otra debe continuar sin generar doble asignación. |


<a id="cu-07"></a>
## CU-07 — Registrar cita confirmada

**Actores en la especificación:** Sistema, personal de Enfermería-Admisión. **Correspondencia documental:** RF-07, HU-07 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-13](02_Java.md#con-13) · [CON-20](05_Pruebas.md#con-20) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-07).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema recibe una asignación validada con todos los datos requeridos. |
| 2 | El personal confirma la cita cuando corresponda. | El sistema verifica que la asignación siga siendo válida antes de guardar. |
| 3 | — | El sistema registra paciente, especialidad, médico, fecha, hora, consultorio y estado inicial. |
| 4 | — | El sistema confirma el registro de la cita y la deja disponible para seguimiento. |
| 5 | — | El sistema genera la notificación correspondiente mediante CU-16. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | Algún recurso dejó de estar disponible antes de guardar. | El sistema no registra la cita con esa combinación y devuelve el proceso a una nueva validación/asignación. |
| 3a | Falta uno de los datos requeridos de la cita. | El sistema no confirma el registro hasta contar con la información necesaria. |


<a id="cu-08"></a>
## CU-08 — Consultar cita paciente

**Actores en la especificación:** Paciente. **Correspondencia documental:** RF-08, HU-08 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-14](02_Java.md#con-14) · [CON-17](01_Flutter.md#con-17) · [CON-23](01_Flutter.md#con-23) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-08).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El paciente abre la opción de consultar sus citas. | El sistema identifica al paciente autenticado. |
| 2 | — | El sistema busca únicamente las citas asociadas a ese paciente. |
| 3 | El paciente revisa la lista de citas. | El sistema muestra las citas encontradas con médico, especialidad, fecha, hora, consultorio y estado. |
| 4 | El paciente selecciona una cita. | El sistema muestra el detalle permitido de la cita seleccionada. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El paciente no tiene citas registradas. | El sistema informa que no existen citas para mostrar. |
| 4a | El paciente intenta acceder a información que no corresponde a sus propias citas. | El sistema impide el acceso a la información no autorizada. |


<a id="cu-09"></a>
## CU-09 — Consultar citas registradas

**Actores en la especificación:** Personal de enfermería / Admisión. **Correspondencia documental:** RF-09, HU-09 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-15](02_Java.md#con-15) · [CON-18](01_Flutter.md#con-18) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-09).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal abre la opción de consulta de citas. | El sistema muestra los criterios de búsqueda habilitados. |
| 2 | El personal selecciona un criterio: paciente, médico, fecha, estado o consultorio. | El sistema recibe el criterio y el valor de búsqueda. |
| 3 | El personal ejecuta la consulta. | El sistema busca las citas coincidentes. |
| 4 | El personal revisa los resultados. | El sistema presenta las citas que cumplen el criterio seleccionado. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen citas que coincidan con el criterio. | El sistema informa que no se encontraron resultados. |
| 2a | El criterio de búsqueda está incompleto o no es válido. | El sistema solicita corregir el criterio antes de ejecutar la consulta. |


<a id="cu-10"></a>
## CU-10 — Reprogramar cita

**Actores en la especificación:** Personal de enfermería / Admisión. **Correspondencia documental:** RF-10, HU-10 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-16](02_Java.md#con-16) · [CON-18](01_Flutter.md#con-18) · [CON-20](05_Pruebas.md#con-20) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-10).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal localiza y selecciona la cita a reprogramar. | El sistema muestra los datos actuales de la cita. |
| 2 | El personal solicita la reprogramación. | El sistema habilito la nueva asignación de recursos. |
| 3 | El personal confirma la opción propuesta o seleccionada según el proceso autorizado. | El sistema valida nuevamente médico, horario y consultorio mediante CU-06. |
| 4 | El personal confirma el cambio. | El sistema guarda la nueva asignación y conserva el cambio para trazabilidad. |
| 5 | — | El sistema genera la notificación correspondiente al paciente mediante CU-16. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | La nueva combinación no está disponible. | El sistema no guarda los cambios y solicita utilizar otra disponibilidad. |
| 3b | Un recurso se ocupa de forma concurrente durante la reprogramación. | El sistema impide la doble asignación y obliga a validar otra combinación. |


<a id="cu-11"></a>
## CU-11 — Cancelar cita

**Actores en la especificación:** Personal de enfermería / Admisión. **Correspondencia documental:** RF-11, HU-11 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-16](02_Java.md#con-16) · [CON-18](01_Flutter.md#con-18) · [CON-20](05_Pruebas.md#con-20) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-11).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal localiza y selecciona la cita activa. | El sistema muestra la información de la cita. |
| 2 | El personal solicita cancelar la cita. | El sistema exige registrar el motivo de cancelación. |
| 3 | El personal ingresa el motivo y confirma. | El sistema valida que exista un motivo. |
| 4 | — | El sistema cambia el estado de la cita a cancelada y conserva el motivo. |
| 5 | — | El sistema registra la operación para trazabilidad y genera la notificación correspondiente mediante CU-16. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | El personal intenta confirmar sin registrar un motivo. | El sistema no cancela la cita y solicita ingresar el motivo requerido. |
| 1a | La cita no existe o no está disponible como cita activa. | El sistema informa que la cancelación no puede ejecutarse sobre ese registro. |


<a id="cu-12"></a>
## CU-12 — Actualizar estado de cita

**Actores en la especificación:** Médico / Personal de enfermería autorizado. **Correspondencia documental:** RF-12, HU-12 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-19](02_Java.md#con-19) · [CON-28](01_Flutter.md#con-28) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-12).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El usuario autorizado selecciona una cita. | El sistema muestra el estado actual de la cita. |
| 2 | El usuario selecciona el nuevo estado conforme a lo ocurrido. | El sistema valida que el usuario tenga permiso para actualizarlo. |
| 3 | El usuario confirma el cambio. | El sistema actualiza el estado de la cita. |
| 4 | — | El sistema conserva la operación para trazabilidad e informes posteriores. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El usuario no posee permiso para actualizar el estado. | El sistema rechaza la operación y mantiene el estado existente. |
| 2b | El cambio solicitado no puede validarse para la situación actual. | El sistema no modifica la cita e informa que el estado no puede actualizarse con esa operación. |


<a id="cu-13"></a>
## CU-13 — Consultar agenda médica

**Actores en la especificación:** Médico. **Correspondencia documental:** RF-13, HU-13 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-10](02_Java.md#con-10) · [CON-19](02_Java.md#con-19) · [CON-28](01_Flutter.md#con-28) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-13).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El médico abre la opción de agenda. | El sistema identifica al médico autenticado. |
| 2 | El médico selecciona vista diaria o semanal. | El sistema determina el período correspondiente. |
| 3 | — | El sistema consulta las citas asignadas al médico dentro del período. |
| 4 | El médico revisa la agenda. | El sistema muestra las citas comprendidas en la vista seleccionada. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen citas asignadas en el período. | El sistema informa que no hay citas para mostrar en la agenda seleccionada. |
| 1a | El usuario no posee rol de médico para esta función. | El sistema impide el acceso a la agenda médica restringida. |


<a id="cu-14"></a>
## CU-14 — Consultar información permitida del paciente

**Actores en la especificación:** Médico. **Correspondencia documental:** RF-14, HU-14 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-25](02_Java.md#con-25) · [CON-28](01_Flutter.md#con-28) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-14).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El médico selecciona una cita de su agenda. | El sistema verifica que la cita esté asignada al médico autenticado. |
| 2 | El médico solicita consultar los datos del paciente. | El sistema identifica la información permitida para el rol. |
| 3 | — | El sistema recupera únicamente la información básica y el historial autorizado asociado al paciente. |
| 4 | El médico revisa la información. | El sistema muestra los datos permitidos sin exponer información restringida. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | La cita no está asignada al médico autenticado. | El sistema impide acceder a la información del paciente. |
| 3a | El dato solicitado no se encuentra autorizado para el rol. | El sistema no muestra el dato restringido. |


<a id="cu-15"></a>
## CU-15 — Registrar datos de atención

**Actores en la especificación:** Médico / Personal de Enfermería autorizado. **Correspondencia documental:** RF-15, HU-15 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-25](02_Java.md#con-25) · [CON-28](01_Flutter.md#con-28) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-15).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El usuario autorizado selecciona la cita que está siendo atendida. | El sistema muestra la información operativa disponible de la cita. |
| 2 | El usuario registra la asistencia del paciente. | El sistema asocia el dato de asistencia a la cita. |
| 3 | El usuario registra el tiempo de espera y el tiempo de atención. | El sistema valida y asocia los tiempos a la cita. |
| 4 | El usuario confirma el registro. | El sistema guarda los datos operativos de atención. |
| 5 | — | Cuando corresponde, el sistema permite actualizar el estado de la cita mediante CU-12. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | La cita no existe o no corresponde a una atención gestionable por el usuario. | El sistema impide registrar datos de atención. |
| 3a | Los datos operativos requeridos no pueden validarse. | El sistema no confirma el registro hasta corregir la información. |


<a id="cu-16"></a>
## CU-16 — Generar notificación

**Actores en la especificación:** Sistema (proceso automático). **Correspondencia documental:** RF-16, HU-16 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-23](01_Flutter.md#con-23) · [CON-24](02_Java.md#con-24) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-16).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema detecta que una cita fue registrada, modificada o cancelada. |
| 2 | — | El sistema identifica al paciente asociado y el tipo de operación confirmada. |
| 3 | — | El sistema genera el contenido de la notificación correspondiente a la situación de la cita. |
| 4 | El paciente recibe o consulta la información según el mecanismo que se defina. | El sistema deja disponible la notificación generada para el paciente. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | La operación sobre la cita no llegó a confirmarse. | El sistema no genera una notificación que indique un cambio inexistente. |
| 2a | No se dispone de la información necesaria para generar la notificación. | El sistema no genera contenido incompleto; la situación requiere revisión antes de notificar. |


<a id="cu-17"></a>
## CU-17 — Generar recordatorio de cita

**Actores en la especificación:** Sistema (proceso automático). **Correspondencia documental:** RF-17, HU-17 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-24](02_Java.md#con-24) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-17).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema identifica una cita programada cuya fecha se aproxima según la configuración. |
| 2 | — | El sistema verifica que la cita siga vigente y asociada al paciente. |
| 3 | — | El sistema genera el recordatorio con la información necesaria de la cita. |
| 4 | El paciente recibe o consulta el recordatorio según el mecanismo que se defina. | El sistema deja disponible el recordatorio generado. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | La cita fue cancelada o modificada antes del momento del recordatorio. | El sistema no genera un recordatorio con información desactualizada; debe usar el estado vigente. |
| 1a | La cita aún no se encuentra dentro del período configurado para recordatorio. | El sistema no genera el recordatorio en ese momento. |


<a id="cu-18"></a>
## CU-18 — Consultar historial de cambios

**Actores en la especificación:** Personal de Enfermería / Administrador. **Correspondencia documental:** RF-18, HU-18 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-26](02_Java.md#con-26) · [CON-29](01_Flutter.md#con-29) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-18).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El usuario autorizado localiza una cita. | El sistema muestra la información general permitida de la cita. |
| 2 | El usuario solicita consultar el historial de cambios. | El sistema verifica sus permisos para acceder al historial. |
| 3 | — | El sistema recupera las operaciones conservadas para la cita. |
| 4 | El usuario revisa el historial. | El sistema presenta el cambio realizado, usuario responsable y fecha/hora correspondiente para cada operación disponible. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El usuario no posee permiso para consultar el historial. | El sistema impide el acceso a la información de trazabilidad. |
| 3a | La cita no tiene cambios registrados. | El sistema informa que no existen operaciones históricas adicionales para mostrar. |


<a id="cu-19"></a>
## CU-19 — Generar reportes de citas

**Actores en la especificación:** Administrador. **Correspondencia documental:** RF-19, HU-19 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-27](02_Java.md#con-27) · [CON-29](01_Flutter.md#con-29) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-19).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El administrador accede al módulo de reportes. | El sistema muestra los tipos de reportes disponibles. |
| 2 | El administrador selecciona el reporte o criterio requerido. | El sistema recibe el tipo de reporte y los criterios aplicables. |
| 3 | El administrador solicita generar el reporte. | El sistema agrupa los datos de citas conforme al criterio seleccionado. |
| 4 | El administrador revisa el resultado. | El sistema presenta el reporte solicitado. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen datos para el criterio seleccionado. | El sistema informa que no hay información disponible para generar resultados con ese criterio. |
| 1a | El usuario no posee permiso administrativo. | El sistema impide el acceso a la función de reportes. |


<a id="cu-20"></a>
## CU-20 — Consultar indicadores de gestión

**Actores en la especificación:** Administrador. **Correspondencia documental:** RF-20, HU-20 (identificadores HU creados en la matriz Markdown).
**Issues vinculados:** [CON-27](02_Java.md#con-27) · [CON-29](01_Flutter.md#con-29) · [CON-31](05_Pruebas.md#con-31).
**Fuente original:** `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` (ruta declarada por la arquitectura, por comprobar); [archivo PlantUML correspondiente](11_Fuentes_y_diagramas.md#cu-20).

### Escenario principal de éxito

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El administrador accede al componente de indicadores. | El sistema muestra los indicadores disponibles para consulta. |
| 2 | El administrador selecciona el indicador o conjunto de indicadores que desea analizar. | El sistema identifica los datos registrados necesarios para el cálculo. |
| 3 | — | El sistema consolida la información y calcula los indicadores disponibles. |
| 4 | El administrador revisa los resultados. | El sistema presenta demanda, cancelaciones, inasistencias, tiempos de espera, tiempos de atención o carga de trabajo según la consulta. |

### Extensiones y flujos alternativos

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existe información suficiente para calcular un indicador solicitado. | El sistema informa la ausencia o insuficiencia de datos para ese indicador. |
| 1a | El usuario no posee permiso administrativo. | El sistema impide el acceso al componente analítico. |

