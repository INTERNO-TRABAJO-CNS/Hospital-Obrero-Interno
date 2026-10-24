# Catálogo de casos de prueba de referencia — CNS

[Volver al índice](README.md) · [Cobertura](08_Cobertura_y_trazabilidad.md) · [Comportamiento](13_Comportamiento_CU.md)

**Identificadores locales propuestos:** `CP-CNS-XXA/B` sirven para enlazar borradores sin colisionar con casos de prueba ya existentes en GitHub/05_pro. Al registrarlos oficialmente, anotar el CP definitivo y la evidencia; no reemplazar sin comprobar numeración previa.

| Código local | Issue de origen | Escenario de prueba | CP oficial / evidencia | Resultado |
|---|---|---|---|---|
| <a id="cp-cns-01a"></a> `CP-CNS-01A` | [CON-01](02_Java.md#con-01) | Arrancar el backend con configuración de pruebas y comprobar endpoint de salud y pruebas JUnit. | Pendiente | No ejecutado |
| <a id="cp-cns-01b"></a> `CP-CNS-01B` | [CON-01](02_Java.md#con-01) | Enviar datos inválidos y verificar error definido sin fuga de datos. | Pendiente | No ejecutado |
| <a id="cp-cns-02a"></a> `CP-CNS-02A` | [CON-02](03_PostgreSQL.md#con-02) | Ejecutar migraciones sobre una base vacía y comprobar todas las claves referenciales. | Pendiente | No ejecutado |
| <a id="cp-cns-02b"></a> `CP-CNS-02B` | [CON-02](03_PostgreSQL.md#con-02) | Intentar registrar una identificación de paciente duplicada y verificar que BD no crea un segundo registro. | Pendiente | No ejecutado |
| <a id="cp-cns-03a"></a> `CP-CNS-03A` | [CON-03](02_Java.md#con-03) | Iniciar sesión con cuenta válida y comprobar las funciones autorizadas. | Pendiente | No ejecutado |
| <a id="cp-cns-03b"></a> `CP-CNS-03B` | [CON-03](02_Java.md#con-03) | Usar credenciales inválidas y tratar de consultar datos de otro rol; ambas operaciones deben rechazarse. | Pendiente | No ejecutado |
| <a id="cp-cns-04a"></a> `CP-CNS-04A` | [CON-04](01_Flutter.md#con-04) | Compilar y ejecutar pruebas de widgets desde una instalación limpia. | Pendiente | No ejecutado |
| <a id="cp-cns-04b"></a> `CP-CNS-04B` | [CON-04](01_Flutter.md#con-04) | Simular backend no disponible y confirmar que la app muestra un error controlado sin cerrarse. | Pendiente | No ejecutado |
| <a id="cp-cns-05a"></a> `CP-CNS-05A` | [CON-05](02_Java.md#con-05) | Administrador modifica el rol de un usuario; las funciones visibles y permisos efectivos se actualizan. | Pendiente | No ejecutado |
| <a id="cp-cns-05b"></a> `CP-CNS-05B` | [CON-05](02_Java.md#con-05) | Paciente intenta cambiar su rol y el backend rechaza sin modificar nada. | Pendiente | No ejecutado |
| <a id="cp-cns-06a"></a> `CP-CNS-06A` | [CON-06](02_Java.md#con-06) | Buscar paciente existente devuelve su registro y no incrementa la cantidad de pacientes. | Pendiente | No ejecutado |
| <a id="cp-cns-06b"></a> `CP-CNS-06B` | [CON-06](02_Java.md#con-06) | Dos registros con la misma identificación no pueden crear duplicados, incluso si se envían simultáneamente. | Pendiente | No ejecutado |
| <a id="cp-cns-07a"></a> `CP-CNS-07A` | [CON-07](01_Flutter.md#con-07) | Ingreso de médico muestra solo pantallas permitidas para ese rol. | Pendiente | No ejecutado |
| <a id="cp-cns-07b"></a> `CP-CNS-07B` | [CON-07](01_Flutter.md#con-07) | Credenciales rechazadas mantienen la sesión cerrada y las opciones protegidas ocultas. | Pendiente | No ejecutado |
| <a id="cp-cns-08a"></a> `CP-CNS-08A` | [CON-08](01_Flutter.md#con-08) | Buscar paciente existente abre su ficha sin crear una nueva. | Pendiente | No ejecutado |
| <a id="cp-cns-08b"></a> `CP-CNS-08B` | [CON-08](01_Flutter.md#con-08) | Registrar paciente con campos faltantes muestra validación y no genera alta. | Pendiente | No ejecutado |
| <a id="cp-cns-09a"></a> `CP-CNS-09A` | [CON-09](05_Pruebas.md#con-09) | Crear PR temporal con test que falla y comprobar que el check queda fallido. | Pendiente | No ejecutado |
| <a id="cp-cns-09b"></a> `CP-CNS-09B` | [CON-09](05_Pruebas.md#con-09) | Ejecutar pipeline exitoso con ejemplos JUnit y widget test. | Pendiente | No ejecutado |
| <a id="cp-cns-10a"></a> `CP-CNS-10A` | [CON-10](02_Java.md#con-10) | Consultar especialidad devuelve solamente médicos y franjas compatibles cargadas. | Pendiente | No ejecutado |
| <a id="cp-cns-10b"></a> `CP-CNS-10B` | [CON-10](02_Java.md#con-10) | Una agenda sin recursos vigentes no produce falsos cupos disponibles. | Pendiente | No ejecutado |
| <a id="cp-cns-11a"></a> `CP-CNS-11A` | [CON-11](02_Java.md#con-11) | Un consultorio ocupado no puede asignarse a otra cita solapada. | Pendiente | No ejecutado |
| <a id="cp-cns-11b"></a> `CP-CNS-11B` | [CON-11](02_Java.md#con-11) | Lanzar dos solicitudes concurrentes por el mismo médico y horario: como máximo una confirma la combinación. | Pendiente | No ejecutado |
| <a id="cp-cns-12a"></a> `CP-CNS-12A` | [CON-12](02_Java.md#con-12) | Para una especialidad con recursos libres, el servicio produce combinación válida sin intervención del paciente. | Pendiente | No ejecutado |
| <a id="cp-cns-12b"></a> `CP-CNS-12B` | [CON-12](02_Java.md#con-12) | Al bloquear concurrentemente la combinación inicialmente elegida, el algoritmo no confirma doble reserva y responde según recursos restantes. | Pendiente | No ejecutado |
| <a id="cp-cns-13a"></a> `CP-CNS-13A` | [CON-13](02_Java.md#con-13) | Paciente solicita especialidad válida y recibe cita confirmada con asignación automática disponible. | Pendiente | No ejecutado |
| <a id="cp-cns-13b"></a> `CP-CNS-13B` | [CON-13](02_Java.md#con-13) | Sin disponibilidad o con conflicto de último instante no se guarda cita confirmada ni se reserva un recurso inexistente. | Pendiente | No ejecutado |
| <a id="cp-cns-14a"></a> `CP-CNS-14A` | [CON-14](02_Java.md#con-14) | Paciente A consulta sus citas y no recibe ninguna cita de Paciente B. | Pendiente | No ejecutado |
| <a id="cp-cns-14b"></a> `CP-CNS-14B` | [CON-14](02_Java.md#con-14) | Paciente sin citas recibe resultado vacío válido sin modificar BD. | Pendiente | No ejecutado |
| <a id="cp-cns-15a"></a> `CP-CNS-15A` | [CON-15](02_Java.md#con-15) | Filtrar por médico y fecha devuelve solo citas coincidentes. | Pendiente | No ejecutado |
| <a id="cp-cns-15b"></a> `CP-CNS-15B` | [CON-15](02_Java.md#con-15) | Una búsqueda con criterio inválido devuelve rechazo controlado sin alterar citas. | Pendiente | No ejecutado |
| <a id="cp-cns-16a"></a> `CP-CNS-16A` | [CON-16](02_Java.md#con-16) | Intentar reprogramar a médico/consultorio ocupados conserva cita original y no duplica recursos. | Pendiente | No ejecutado |
| <a id="cp-cns-16b"></a> `CP-CNS-16B` | [CON-16](02_Java.md#con-16) | Cancelar sin motivo es rechazado; con motivo guarda estado cancelada y traza el responsable. | Pendiente | No ejecutado |
| <a id="cp-cns-17a"></a> `CP-CNS-17A` | [CON-17](01_Flutter.md#con-17) | Una solicitud válida solo pide especialidad y muestra la cita que asignó el servidor. | Pendiente | No ejecutado |
| <a id="cp-cns-17b"></a> `CP-CNS-17B` | [CON-17](01_Flutter.md#con-17) | Paciente sin citas observa pantalla vacía; al intentar consultar otra cita, no recibe sus datos. | Pendiente | No ejecutado |
| <a id="cp-cns-18a"></a> `CP-CNS-18A` | [CON-18](01_Flutter.md#con-18) | Admisión cancela con motivo y la vista muestra el nuevo estado. | Pendiente | No ejecutado |
| <a id="cp-cns-18b"></a> `CP-CNS-18B` | [CON-18](01_Flutter.md#con-18) | Ante reprogramación sin disponibilidad, la vista conserva la cita anterior y muestra rechazo. | Pendiente | No ejecutado |
| <a id="cp-cns-19a"></a> `CP-CNS-19A` | [CON-19](02_Java.md#con-19) | Médico A consulta semana y no recibe citas de médico B. | Pendiente | No ejecutado |
| <a id="cp-cns-19b"></a> `CP-CNS-19B` | [CON-19](02_Java.md#con-19) | Intentar actualizar una cita inexistente o estado no aprobado no modifica registros. | Pendiente | No ejecutado |
| <a id="cp-cns-20a"></a> `CP-CNS-20A` | [CON-20](05_Pruebas.md#con-20) | Solicitud + asignación + confirmación + reprogramación + cancelación mantienen historial y consistencia. | Pendiente | No ejecutado |
| <a id="cp-cns-20b"></a> `CP-CNS-20B` | [CON-20](05_Pruebas.md#con-20) | Ejecutar peticiones concurrentes sobre un solo cupo y comprobar que no se duplican reservas. | Pendiente | No ejecutado |
| <a id="cp-cns-21a"></a> `CP-CNS-21A` | [CON-21](02_Java.md#con-21) | Con política aprobada, cancelar libera recurso y no asigna a pacientes que no cumplen sus condiciones. | Pendiente | No ejecutado |
| <a id="cp-cns-21b"></a> `CP-CNS-21B` | [CON-21](02_Java.md#con-21) | Dos reasignaciones simultáneas nunca adjudican el mismo cupo a dos citas. | Pendiente | No ejecutado |
| <a id="cp-cns-22a"></a> `CP-CNS-22A` | [CON-22](04_WebSocket.md#con-22) | Confirmar cita emite actualización a cliente autorizado, no a otro paciente. | Pendiente | No ejecutado |
| <a id="cp-cns-22b"></a> `CP-CNS-22B` | [CON-22](04_WebSocket.md#con-22) | Cortar WebSocket y completar cancelación por REST conserva el cambio; reconexión refresca datos. | Pendiente | No ejecutado |
| <a id="cp-cns-23a"></a> `CP-CNS-23A` | [CON-23](01_Flutter.md#con-23) | Una reprogramación confirmada en admisión actualiza Mis citas del paciente conectado sin refresco manual. | Pendiente | No ejecutado |
| <a id="cp-cns-23b"></a> `CP-CNS-23B` | [CON-23](01_Flutter.md#con-23) | App desconectada reconecta y recarga citas desde REST sin duplicar registros en pantalla. | Pendiente | No ejecutado |
| <a id="cp-cns-24a"></a> `CP-CNS-24A` | [CON-24](02_Java.md#con-24) | Cita cancelada antes de recordatorio no genera recordatorio antiguo. | Pendiente | No ejecutado |
| <a id="cp-cns-24b"></a> `CP-CNS-24B` | [CON-24](02_Java.md#con-24) | Una operación fallida no genera notificación de cambio inexistente. | Pendiente | No ejecutado |
| <a id="cp-cns-25a"></a> `CP-CNS-25A` | [CON-25](02_Java.md#con-25) | Médico ajeno a cita no puede ver los datos de ese paciente. | Pendiente | No ejecutado |
| <a id="cp-cns-25b"></a> `CP-CNS-25B` | [CON-25](02_Java.md#con-25) | Registrar tiempos operativos inválidos no altera la atención, y datos válidos quedan asociados a la cita. | Pendiente | No ejecutado |
| <a id="cp-cns-26a"></a> `CP-CNS-26A` | [CON-26](02_Java.md#con-26) | Reprogramar y cancelar genera registros diferenciados con actor y momento. | Pendiente | No ejecutado |
| <a id="cp-cns-26b"></a> `CP-CNS-26B` | [CON-26](02_Java.md#con-26) | Usuario sin permiso no puede recuperar historial de cambios. | Pendiente | No ejecutado |
| <a id="cp-cns-27a"></a> `CP-CNS-27A` | [CON-27](02_Java.md#con-27) | Reporte por especialidad concuerda con número de citas del conjunto de prueba. | Pendiente | No ejecutado |
| <a id="cp-cns-27b"></a> `CP-CNS-27B` | [CON-27](02_Java.md#con-27) | Indicador sin datos o usuario no administrador obtiene ausencia de información o acceso denegado según corresponda. | Pendiente | No ejecutado |
| <a id="cp-cns-28a"></a> `CP-CNS-28A` | [CON-28](01_Flutter.md#con-28) | Médico A consulta su agenda y no ve pacientes de la agenda del médico B. | Pendiente | No ejecutado |
| <a id="cp-cns-28b"></a> `CP-CNS-28B` | [CON-28](01_Flutter.md#con-28) | Registrar atención con tiempos inválidos muestra rechazo sin borrar datos previos. | Pendiente | No ejecutado |
| <a id="cp-cns-29a"></a> `CP-CNS-29A` | [CON-29](01_Flutter.md#con-29) | Administrador obtiene reporte por médico filtrado con datos coherentes. | Pendiente | No ejecutado |
| <a id="cp-cns-29b"></a> `CP-CNS-29B` | [CON-29](01_Flutter.md#con-29) | Paciente intenta acceder a indicadores y no visualiza el módulo ni sus resultados. | Pendiente | No ejecutado |
| <a id="cp-cns-30a"></a> `CP-CNS-30A` | [CON-30](06_Servidor.md#con-30) | Desde un cliente de pruebas se autentica, consulta cita y reconecta WebSocket. | Pendiente | No ejecutado |
| <a id="cp-cns-30b"></a> `CP-CNS-30B` | [CON-30](06_Servidor.md#con-30) | Desplegar y revertir versión conserva esquema y citas de prueba según procedimiento. | Pendiente | No ejecutado |
| <a id="cp-cns-31a"></a> `CP-CNS-31A` | [CON-31](05_Pruebas.md#con-31) | Ejecutar flujo completo paciente solicita → Java asigna → registra → médico consulta → administración reprograma. | Pendiente | No ejecutado |
| <a id="cp-cns-31b"></a> `CP-CNS-31B` | [CON-31](05_Pruebas.md#con-31) | Ejecutar por lo menos un rechazo documentado por cada rol o caso de uso según la especificación. | Pendiente | No ejecutado |
| <a id="cp-cns-32a"></a> `CP-CNS-32A` | [CON-32](05_Pruebas.md#con-32) | Cien solicitudes simultáneas de pruebas por cupos limitados nunca generan doble asignación. | Pendiente | No ejecutado |
| <a id="cp-cns-32b"></a> `CP-CNS-32B` | [CON-32](05_Pruebas.md#con-32) | Medir latencia de consulta/asignación en entorno descrito y contrastar con objetivo aproximado de 2 segundos. | Pendiente | No ejecutado |
| <a id="cp-cns-33a"></a> `CP-CNS-33A` | [CON-33](05_Pruebas.md#con-33) | Paciente A intenta consultar cita de paciente B por API y WS y ambos accesos quedan bloqueados. | Pendiente | No ejecutado |
| <a id="cp-cns-33b"></a> `CP-CNS-33B` | [CON-33](05_Pruebas.md#con-33) | Acceso administrativo con cuenta médica falla sin revelar indicadores ni datos de pacientes ajenos. | Pendiente | No ejecutado |
| <a id="cp-cns-34a"></a> `CP-CNS-34A` | [CON-34](01_Flutter.md#con-34) | Red caída durante solicitud muestra error sin confirmar cita inexistente. | Pendiente | No ejecutado |
| <a id="cp-cns-34b"></a> `CP-CNS-34B` | [CON-34](01_Flutter.md#con-34) | Pruebas de widgets verifican lectura, navegación y mensajes de error de vistas principales. | Pendiente | No ejecutado |
| <a id="cp-cns-35a"></a> `CP-CNS-35A` | [CON-35](06_Servidor.md#con-35) | Restaurar base con citas, usuarios y audit trail preserva referencias y relaciones. | Pendiente | No ejecutado |
| <a id="cp-cns-35b"></a> `CP-CNS-35B` | [CON-35](06_Servidor.md#con-35) | Después de restaurar, no hay cita duplicada para la misma combinación incompatible de recursos. | Pendiente | No ejecutado |
| <a id="cp-cns-36a"></a> `CP-CNS-36A` | [CON-36](05_Pruebas.md#con-36) | Auditoría final encuentra enlaces y evidencias de los 20 CU o explicación explícita de bloqueos. | Pendiente | No ejecutado |
| <a id="cp-cns-36b"></a> `CP-CNS-36B` | [CON-36](05_Pruebas.md#con-36) | Instalar versión de entrega sobre entorno limpio siguiendo README y ejecutar smoke test de cita. | Pendiente | No ejecutado |

## Pruebas integrales obligatorias

- Pruebas del flujo `CU-04 → CU-05 → CU-06 → CU-07` y sus fallos sin cupo/por concurrencia.
- Pruebas de roles y acceso a información de pacientes CU-01/02/03/08/14/18.
- Pruebas de modificación, cancelación, historial, notificaciones y WebSocket CU-10/11/16/18.
- Pruebas de agenda y atención CU-12/13/15; reportes e indicadores CU-19/20.
- Cada resultado definitivo debe indicar datos de prueba, pasos, resultado esperado, obtenido, evidencia y PR correctivo si falla.
