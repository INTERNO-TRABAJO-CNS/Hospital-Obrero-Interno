# Especificación de Casos de Uso

## Datos del proyecto

| Campo | Detalle |
| --- | --- |
| Nombre del proyecto | Sistema de Gestión y Control de Citas Médicas — Hospital Obrero de la CNS |
| Documento | Especificación de Casos de Uso |
| Organización académica | Universidad Católica Boliviana «San Pablo», Carrera de Ingeniería de Sistemas |
| Asignaturas | Taller de Sistema de Información (SIS-227) e Ingeniería de Software (SIS-213) |
| Equipo de trabajo | Alejandro Álvaro Davila Albornoz, Abigal Noelia Farfan Chaves, Alex Sandro Ledezma Patiño, Benjamín Alejandro Sirpa Calizaya y Matias Alejandro García Suarez. |
| Lugar y fecha de portada | Tarija, Bolivia — septiembre de 2026 |
| Estado de los casos de uso | Borrador (según fichas individuales); CU-02 a CU-20 actualizados el 04/11/2026 (ver `06_proyect/CAMBIOS_04-11-2026.md`) |

## Índice de casos de uso

- [CU-01 — Iniciar Sesión](#cu-01)
- [CU-02 — Gestionar usuarios roles y permisos](#cu-02)
- [CU-03 — Registrar y consultar pacientes](#cu-03)
- [CU-04 — Solicitar cita médica](#cu-04)
- [CU-05 — Asignar cita automáticamente](#cu-05)
- [CU-06 — Validar disponibilidad de Recursos](#cu-06)
- [CU-07 — Registrar cita confirmada](#cu-07)
- [CU-08 — Consultar cita paciente](#cu-08)
- [CU-09 — Consultar citas registradas](#cu-09)
- [CU-10 — Reprogramar cita](#cu-10)
- [CU-11 — Cancelar cita](#cu-11)
- [CU-12 — Actualizar estado de cita](#cu-12)
- [CU-13 — Consultar agenda médica](#cu-13)
- [CU-14 — Consultar información permitida del paciente](#cu-14)
- [CU-15 — Registrar datos de atención](#cu-15)
- [CU-16 — Generar notificación](#cu-16)
- [CU-17 — Generar recordatorio de cita](#cu-17)
- [CU-18 — Consultar historial de cambios](#cu-18)
- [CU-19 — Generar reportes de citas](#cu-19)
- [CU-20 — Consultar indicadores de gestión](#cu-20)

## Matriz de trazabilidad de casos de uso

Relación de los **20 casos de uso** con sus requisitos funcionales y actores principales, según el documento original.

| CASOS DE USO | NOMBRE | REQUISITO | ACTOR PRINCIPAL |
| --- | --- | --- | --- |
| CU-01 | Iniciar Sesión | RF-01 | Paciente / Enfermería-Admisión / Médico / Administrador |
| CU-02 | Gestionar usuarios roles y permisos | RF-02 | Administrador |
| CU-03 | Registrar y consultar pacientes | RF-03 | Asegurado titular / Personal de Enfermería / Admisión |
| CU-04 | Solicitar cita médica | RF-04 | Paciente |
| CU-05 | Asignar cita automáticamente | RF-05 | Sistema (proceso automático de asignación) |
| CU-06 | Validar disponibilidad de Recursos | RF-06 | Sistema (proceso de validación) |
| CU-07 | Registrar cita confirmada | RF-07 | Sistema (proceso automático) |
| CU-08 | Consultar cita paciente | RF-08 | Paciente |
| CU-09 | Consultar citas registradas | RF-09 | Personal de enfermería / Admisión; pantalla TV (solo lectura) |
| CU-10 | Reprogramar cita | RF-10 | Paciente |
| CU-11 | Cancelar cita | RF-11 | Paciente / Sistema (cancelación automática) |
| CU-12 | Actualizar estado de cita | RF-12 | Personal de enfermería / Médico / Sistema |
| CU-13 | Consultar agenda médica | RF-13 | Médico |
| CU-14 | Consultar información permitida del paciente | RF-14 | Médico |
| CU-15 | Registrar datos de atención | RF-15 | Médico |
| CU-16 | Generar notificación | RF-16 | Sistema (proceso automático) |
| CU-17 | Generar recordatorio de cita | RF-17 | Sistema (proceso automático) |
| CU-18 | Consultar historial de cambios | RF-18 | Personal de Enfermería / Administrador |
| CU-19 | Generar reportes de citas | RF-19 | Administrador |
| CU-20 | Consultar indicadores de gestión | RF-20 | Administrador |

## Especificación detallada de los casos de uso

> **Observación:** los apartados «Modelo de contexto visual» y «Diagrama de comportamiento UML» se conservan, pero no incluyen gráficos en el Word recibido. Se identifican como pendientes, sin inventar diagramas.

<a id="cu-01"></a>

## CU-01 — Iniciar Sesión

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-01 |
| Nombre del caso de uso: | Iniciar sesión |
| Proyecto: | Sistema de gestión y control de citas médicos – Hospital Obrero CNS |
| Fecha de versión: | 01/10/2026 |
| Autor de artefacto: | Equipo de proyecto de CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-01: Iniciar Sesión |
| Actor Principal (Quién): | Paciente / Enfermería-Admisión / Médico / Administrador |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El usuario solicita ingresar al sistema e introduce sus credenciales. |
| Precondiciones (Cuándo): | • El usuario posee credenciales registradas.<br>• El sistema se encuentra disponible para autenticar al usuario. |
| Postcondiciones (Éxito): | • El usuario queda autenticado.<br>• El sistema identifica su rol y habilita únicamente las funciones autorizadas. |
| Garantía Mínima (Fallo): | Si las credenciales no son válidas o el usuario no posee el permiso requerido, el acceso se rechaza y no se habilitan funciones restringidas. |
| Actores Secundarios: | No se identifican servicios externos de autenticación en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite autenticar a los usuarios del sistema y determinar las funciones e información disponibles de acuerdo con el rol asignado. Derivado de RF-01 y de su historia de usuario. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Todos los usuarios | Acceder únicamente a las funciones necesarias para cumplir su rol. |
| Administración del Hospital | Garantizar que el acceso al sistema se encuentre controlado por roles y permisos. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El usuario solicita iniciar sesión e ingresa sus credenciales. | El sistema recibe las credenciales y comienza la validación. |
| 2 | El usuario confirma el ingreso. | El sistema valida la identidad del usuario y determina su rol. |
| 3 | — | El sistema aplica los permisos asociados al rol autenticado. |
| 4 | El usuario accede al sistema. | El sistema muestra únicamente las funciones e información autorizadas para ese rol. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | Las credenciales no son válidas. | El sistema rechaza el acceso e informa que no fue posible autenticar al usuario. El caso de uso finaliza sin iniciar sesión. |
| 3a | El usuario intenta acceder a una función para la que no posee permiso. | El sistema impide el acceso a la función restringida y mantiene únicamente las opciones autorizadas. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** Debe cumplirse RNF-01: autenticación y autorización basada en roles.

- **Confidencialidad:** Debe respetarse RNF-02, evitando acceso a información no autorizada.

- **Usabilidad:** Debe cumplirse RNF-05, mostrando a cada rol solo las operaciones necesarias.

### Variaciones de tecnología y datos

- El documento base no define el mecanismo técnico de autenticación; esta decisión queda pendiente de diseño.

- Los roles contemplados en el documento son paciente, enfermería/admisión, médico y administrador.

<a id="cu-02"></a>

## CU-02 — Gestionar usuarios roles y permisos

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-02 |
| Nombre del caso de uso: | Gestionar Usuarios, Roles y Permisos |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-02: Gestionar Usuarios, Roles y Permisos |
| Actor Principal (Quién): | Administrador |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El administrador accede a la gestión de usuarios y roles para registrar o modificar permisos. |
| Precondiciones (Cuándo): | • El administrador se encuentra autenticado.<br>• El usuario posee permisos de administración. |
| Postcondiciones (Éxito): | • Los cambios de usuario, rol o permisos quedan guardados.<br>• Los permisos actualizados se aplican a las funciones disponibles para el usuario afectado. |
| Garantía Mínima (Fallo): | Si el administrador no posee autorización o los datos no pueden validarse, no se aplican cambios a roles ni permisos. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al administrador gestionar usuarios, roles y permisos y mantener los catálogos de operación (médicos, especialidades, consultorios, horarios de atención, disponibilidad, fichas y cupos, días no hábiles y pacientes) mediante crear, consultar, modificar y eliminar (CRUD). Derivado de RF-02, RU-11 y RU-13. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Administrador | Mantener control sobre usuarios, roles y permisos de acceso. |
| Usuarios del sistema | Disponer de los permisos correspondientes a sus responsabilidades. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El administrador abre la gestión de usuarios, roles y catálogos. | El sistema muestra las opciones de administración disponibles. |
| 2 | El administrador selecciona un usuario, rol o catálogo (médico, especialidad, consultorio, horario, cupos, días no hábiles, paciente). | El sistema presenta la información y permisos actuales. |
| 3 | El administrador crea, modifica o elimina el registro. | El sistema valida el cambio (por ejemplo: cupo diario entre 5 y 7 fichas por médico y duración dentro del rango de la especialidad). |
| 4 | El administrador confirma la operación. | El sistema guarda los cambios, aplica permisos o disponibilidad y los deja disponibles para la asignación de citas. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | El usuario que intenta gestionar no posee rol de administrador. | El sistema impide el acceso a la función de administración. |
| 3a | La configuración solicitada no puede validarse. | El sistema no guarda los cambios y solicita corregir la información antes de confirmar. |
| 3b | El cambio afectaría citas ya programadas (por ejemplo, eliminar un horario con fichas asignadas). | El sistema no aplica el cambio mientras existan citas programadas afectadas. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** Solo usuarios con permisos administrativos pueden gestionar usuarios, roles y permisos.

- **Consistencia:** Los cambios guardados deben reflejarse en las funciones disponibles para el rol afectado.

- **Usabilidad:** La gestión debe presentar únicamente opciones administrativas autorizadas.

### Variaciones de tecnología y datos

- Las cuentas de enfermería/secretaría (aprox. 15 a 20 personas) tendrán usuario y contraseña individuales; se implementa en una etapa posterior.

- El administrador NO modifica citas asignadas: solo las supervisa (ver CU-19).

<a id="cu-03"></a>

## CU-03 — Registrar y consultar pacientes

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-03 |
| Nombre del caso de uso: | Registrar y Consultar Pacientes |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-03: Registrar y Consultar Pacientes |
| Actor Principal (Quién): | Asegurado titular (crea su cuenta) / Personal de Enfermería / Admisión (localiza o registra pacientes y crea el historial clínico en la primera atención) |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El asegurado titular desea crear su cuenta, o el personal necesita localizar o registrar a un paciente (titular o beneficiario) para gestionar sus citas. |
| Precondiciones (Cuándo): | • Para la cuenta: el titular dispone de su número de afiliado.<br>• Para el beneficiario: se dispone de la afiliación del titular y del CI del beneficiario.<br>• El personal se encuentra autenticado y autorizado. |
| Postcondiciones (Éxito): | • La cuenta del titular queda creada solo si su afiliado existe en la tabla interna de afiliados.<br>• Si el paciente ya existe, se recupera su registro; si no, queda registrado sin duplicar información.<br>• En la primera atención se crea su historial clínico. |
| Garantía Mínima (Fallo): | Si se detecta un paciente ya registrado, el sistema evita crear un registro duplicado y conserva el registro existente. |
| Actores Secundarios: | No se identifican sistemas externos de afiliación o identificación en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al titular crear su cuenta validando su número de afiliado, y al personal de enfermería/admisión localizar o registrar titulares y beneficiarios (por afiliación del titular y CI del beneficiario) sin duplicados. Los beneficiarios no son afiliados ni tienen cuenta propia. Derivado de RF-03 y RU-05. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Asegurado titular | Crear su cuenta de forma sencilla con su número de afiliado. |
| Enfermería / Admisión | Localizar o registrar pacientes de forma rápida y sin duplicaciones. |
| Paciente / Beneficiario | Mantener un único registro e historial clínico asociado a sus citas. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El titular ingresa su número de afiliado para crear su cuenta, o el personal ingresa la afiliación del titular. | El sistema valida el afiliado contra la tabla interna de afiliados y busca si existe el paciente. |
| 2 | Para un beneficiario, el personal ingresa su CI. | El sistema busca al beneficiario asociado a la afiliación del titular. |
| 3 | Si no existe, el personal solicita registrar al paciente y completa los datos requeridos. | El sistema valida nuevamente que no exista duplicidad. |
| 4 | — | El sistema registra al paciente, crea su historial clínico si es la primera atención y confirma la operación. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | El paciente ya se encuentra registrado. | El sistema muestra el registro existente y evita crear un duplicado. El flujo continúa con la consulta del paciente. |
| 1b | El número de afiliado no figura en la tabla interna de afiliados. | El sistema no crea la cuenta ni el registro e informa que el afiliado no fue encontrado. |
| 3a | La información necesaria está incompleta o no puede validarse. | El sistema no registra al paciente y solicita completar o corregir la información. |
| 2a | El beneficiario no está registrado bajo la afiliación del titular. | El personal registra al beneficiario con su CI bajo esa afiliación y se crea su historial en la primera atención. |

### Requisitos especiales (no funcionales específicos)

- **Confidencialidad:** La información del paciente debe mostrarse únicamente a usuarios autorizados, conforme RNF-02.

- **Seguridad:** El acceso a la función debe depender del rol autorizado, conforme RNF-01.

- **Integridad:** La identificación debe utilizarse para evitar registros duplicados.

### Variaciones de tecnología y datos

- Solo los asegurados titulares crean cuenta; los beneficiarios (por ejemplo, la esposa o los hijos) se atienden con la afiliación del titular y su CI.

- La validación del afiliado es interna al prototipo (tabla interna de afiliados); no hay integración con sistemas institucionales de la CNS.

<a id="cu-04"></a>

## CU-04 — Solicitar cita médica

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-04 |
| Nombre del caso de uso: | Solicitar Cita Médica |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-04: Solicitar Cita Médica |
| Actor Principal (Quién): | Paciente |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El paciente requiere atención médica y solicita una cita indicando la especialidad y el día en que desea ser atendido. |
| Precondiciones (Cuándo): | • El paciente ha iniciado sesión con su cuenta.<br>• El paciente no tiene otra cita programada en el día solicitado.<br>• El día solicitado es un día hábil dentro de los próximos 15 días hábiles. |
| Postcondiciones (Éxito): | • La solicitud queda registrada en la cola de solicitudes con su orden de llegada.<br>• El paciente selecciona solo especialidad y día; no selecciona médico, hora ni consultorio.<br>• La solicitud queda disponible para la asignación automática (CU-05). |
| Garantía Mínima (Fallo): | Si no se puede registrar la solicitud, no se confirma ninguna cita ni se asignan recursos. |
| Actores Secundarios: | CU-05 Asignar Cita Automáticamente, como proceso relacionado posterior a la solicitud. |
| Descripción Breve (Qué/Para qué): | Permite al paciente solicitar una cita indicando la especialidad (Consultorio Familiar / Medicina General, Odontología o Pediatría) y el día de atención, sin elegir médico, hora ni consultorio. Si no hay cupos muestra «NO HAY CUPOS» y la solicitud pasa a lista de espera. Derivado de RF-04 y RU-01. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Solicitar atención médica sin tener que buscar manualmente horarios o profesionales. |
| Hospital Obrero CNS | Recibir solicitudes estructuradas que puedan procesarse de acuerdo con disponibilidad real. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El paciente accede a la opción de solicitar cita. | El sistema muestra las especialidades disponibles. |
| 2 | El paciente selecciona la especialidad. | El sistema registra la selección. |
| 3 | El paciente selecciona el día de atención. | El sistema valida que sea día hábil, que esté dentro de los 15 días hábiles y que el paciente no tenga otra cita ese día. |
| 4 | El paciente confirma la solicitud. | El sistema registra la solicitud (ID de afiliado, especialidad, día y orden de llegada) en la cola. |
| 5 | — | El sistema deriva la solicitud al proceso de asignación automática (CU-05). |
| 6 | — | El sistema muestra al paciente la cita asignada (ficha, médico, especialidad, fecha, hora y consultorio) o el mensaje «NO HAY CUPOS» con la solicitud en espera. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El paciente no selecciona una especialidad válida. | El sistema no permite confirmar la solicitud y solicita completar la selección. |
| 3a | El día no es hábil, está fuera de los 15 días hábiles o el paciente ya tiene una cita ese día. | El sistema no permite confirmar y solicita elegir otro día. |
| 5a | No existen cupos para el día solicitado. | El sistema informa «NO HAY CUPOS», deja la solicitud EN_ESPERA para el siguiente día hábil (hasta 15 días hábiles) y no confirma ninguna cita; la solicitud sigue pendiente hasta encontrar disponibilidad. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** Solo el paciente autenticado debe poder registrar solicitudes a su nombre.

- **Usabilidad:** La solicitud exige únicamente especialidad y día; nunca médico, hora ni consultorio.

- **Consistencia:** La posterior asignación debe utilizar disponibilidad vigente, conforme RNF-03.

- **Portabilidad:** La pantalla debe funcionar en móvil y computadora, conforme RNF-07.

### Variaciones de tecnología y datos

- La prioridad de las solicitudes y la lista de espera se definen en CU-05.

- Cambio 04/11/2026: se agrega la selección del día (antes el paciente solo indicaba la especialidad).

<a id="cu-05"></a>

## CU-05 — Asignar cita automáticamente

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-05 |
| Nombre del caso de uso: | Asignar Cita Automáticamente |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-05: Asignar Cita Automáticamente |
| Actor Principal (Quién): | Sistema (proceso automático de asignación) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Existe una solicitud de cita válida en la cola de solicitudes. |
| Precondiciones (Cuándo): | • Existe al menos una solicitud registrada en la cola.<br>• Se dispone de información vigente de médicos, horarios, consultorios y fichas. |
| Postcondiciones (Éxito): | • Se asigna un médico de la especialidad, una hora y un consultorio dentro del día solicitado y se genera el número de ficha.<br>• Si no hay cupo, la solicitud queda EN_ESPERA para el siguiente día hábil (hasta 15 días hábiles).<br>• La asignación queda preparada para su registro (CU-07). |
| Garantía Mínima (Fallo): | Si no existen recursos compatibles disponibles, el sistema no confirma una asignación inválida ni duplica recursos. |
| Actores Secundarios: | CU-06 Validar Disponibilidad de Recursos y CU-07 Registrar Cita Confirmada. |
| Descripción Breve (Qué/Para qué): | Procesa las solicitudes de la cola en orden de llegada y asigna automáticamente médico, hora y consultorio con cupo. Las solicitudes que provienen de cancelaciones del paciente se atienden con menor prioridad que las de pacientes que no cancelaron. Derivado de RF-05 y RU-02. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Recibir una cita asignada de acuerdo con la disponibilidad existente. |
| Enfermería / Admisión | Trabajar con asignaciones válidas sin buscar manualmente combinaciones de recursos. |
| Hospital Obrero CNS | Aprovechar la disponibilidad real y reducir conflictos de agenda. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema toma la siguiente solicitud de la cola según orden de llegada y prioridad. |
| 2 | — | El sistema consulta la disponibilidad vigente de los médicos de la especialidad para el día solicitado (máximo 7 fichas por médico y día). |
| 3 | — | El sistema valida la combinación de recursos mediante CU-06. |
| 4 | — | El sistema asigna un médico con cupo y una hora según la duración de la especialidad; varios médicos pueden atender a la misma hora porque cada uno tiene su consultorio. |
| 5 | — | El sistema envía la asignación validada al registro de cita confirmada (CU-07). |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | No existen cupos para el día solicitado. | El sistema informa «NO HAY CUPOS», coloca la solicitud EN_ESPERA para el siguiente día hábil y repite la búsqueda ese día. |
| 2b | Se agotaron los 15 días hábiles sin cupo. | La solicitud queda en estado SIN_CUPO, no se crea cita y se informa al paciente. |
| 3a | Un recurso deja de estar disponible durante el procesamiento. | El sistema descarta esa combinación y continúa con otra compatible, sin duplicar el recurso ocupado. |
| 3b | Dos solicitudes simultáneas compiten por el mismo cupo. | PostgreSQL permite que solo una obtenga el cupo; la otra continúa con la siguiente opción o pasa a espera. |

### Requisitos especiales (no funcionales específicos)

- **Consistencia / Rendimiento:** Debe utilizarse el estado vigente de médicos, horarios y consultorios, conforme RNF-03.

- **Fiabilidad / Integridad:** Ante solicitudes concurrentes, un mismo recurso no puede confirmarse simultáneamente para más de una cita, conforme RNF-04.

- **Escalabilidad:** El proceso debe mantener las mismas reglas fundamentales al crecer el volumen de solicitudes, conforme RNF-06.

### Variaciones de tecnología y datos

- Criterio de desempate entre médicos con cupo: PENDIENTE de validar (propuesta por defecto: el médico con menos fichas ese día).

- La reasignación automática de cupos liberados por cancelaciones NO se implementa por ahora (decisión 04/11/2026).

<a id="cu-06"></a>

## CU-06 — Validar disponibilidad de Recursos

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-06 |
| Nombre del caso de uso: | Validar Disponibilidad de Recursos |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-06: Validar Disponibilidad de Recursos |
| Actor Principal (Quién): | Sistema (proceso de validación) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Una solicitud o confirmación requiere verificar médico, horario, consultorio y fichas del día. |
| Precondiciones (Cuándo): | • Existe una combinación candidata de médico, fecha, hora y consultorio.<br>• La información de disponibilidad se encuentra accesible para validación. |
| Postcondiciones (Éxito): | • La combinación queda marcada como disponible o no disponible.<br>• No se permite confirmar una combinación con recursos ocupados o solapados. |
| Garantía Mínima (Fallo): | Ante incertidumbre o conflicto de disponibilidad, el sistema no confirma la combinación como válida. |
| Actores Secundarios: | CU-05 Asignar Cita Automáticamente y CU-07 Registrar Cita Confirmada utilizan esta validación. |
| Descripción Breve (Qué/Para qué): | Verifica la disponibilidad del médico, horario y consultorio antes de confirmar una cita, impidiendo solapamientos y respetando el máximo de 7 fichas por médico y día y la duración de la especialidad. Derivado de RF-06 y RU-07. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Enfermería / Admisión | Evitar citas duplicadas o superpuestas. |
| Paciente | Recibir una cita realmente disponible. |
| Médico | Mantener una agenda coherente con sus horarios asignados. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema recibe una combinación candidata de médico, fecha, hora y consultorio. |
| 2 | — | El sistema consulta el estado vigente del médico y su horario de atención (incluidos días no hábiles). |
| 3 | — | El sistema consulta el estado vigente del consultorio. |
| 4 | — | El sistema verifica que el médico tenga menos de 7 fichas ese día, que la duración quepa en su horario y que el paciente no tenga otra cita ese día. |
| 5 | — | El sistema verifica que no exista otra cita incompatible o solapada. |
| 6 | — | El sistema devuelve el resultado de disponibilidad al proceso que solicitó la validación. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El médico o el horario ya se encuentra ocupado. | El sistema marca la combinación como no disponible y evita su confirmación. |
| 3a | El consultorio ya se encuentra ocupado. | El sistema marca la combinación como no disponible y evita su confirmación. |
| 4a | El médico ya tiene 7 fichas ese día o el paciente ya tiene una cita ese día. | El sistema marca la combinación como no disponible. |
| 5a | Dos solicitudes intentan confirmar el mismo recurso y horario. | El sistema permite que únicamente una confirme el recurso; la otra debe continuar sin generar doble asignación. |

### Requisitos especiales (no funcionales específicos)

- **Consistencia:** Debe utilizarse la disponibilidad vigente, conforme RNF-03.

- **Integridad:** Debe impedirse la doble asignación en solicitudes concurrentes, conforme RNF-04.

- **Seguridad:** La validación debe formar parte únicamente de operaciones autorizadas.

### Variaciones de tecnología y datos

- Concurrencia: se garantiza en PostgreSQL con SELECT … FOR UPDATE SKIP LOCKED sobre la cola y una restricción UNIQUE (médico, fecha, hora) como respaldo (RNF-04).

- Duración por cita (rango aceptado): Medicina General 30–45 min, Odontología 45–60 min, Pediatría 30–45 min. Cada médico tiene una duración fija configurable dentro del rango de su especialidad.

- Cada médico debe atender entre 5 (meta de cumplimiento, supervisada por el administrador) y 7 fichas por día (cupo máximo que bloquea la asignación).

<a id="cu-07"></a>

## CU-07 — Registrar cita confirmada

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-07 |
| Nombre del caso de uso: | Registrar Cita Confirmada |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-07: Registrar Cita Confirmada |
| Actor Principal (Quién): | Sistema (proceso automático) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Existe una asignación de cita validada y se solicita su confirmación. |
| Precondiciones (Cuándo): | • La asignación de recursos fue validada como disponible.<br>• Se cuenta con paciente, especialidad, médico, fecha, hora y consultorio. |
| Postcondiciones (Éxito): | • La cita queda almacenada con paciente, especialidad, médico, fecha, hora, consultorio, número de ficha y estado inicial PROGRAMADA.<br>• La solicitud de origen pasa a ASIGNADA.<br>• La cita queda disponible para consulta y seguimiento. |
| Garantía Mínima (Fallo): | Si la asignación deja de ser válida antes de confirmar, la cita no se registra con recursos en conflicto. |
| Actores Secundarios: | CU-06 Validar Disponibilidad de Recursos y CU-16 Generar Notificación de Cita. |
| Descripción Breve (Qué/Para qué): | Registra de forma definitiva una cita previamente validada, generando su número de ficha y asociando todos los datos necesarios para su seguimiento. Derivado de RF-07. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Contar con una cita registrada con todos sus datos de atención. |
| Enfermería / Admisión | Disponer de un registro completo para seguimiento. |
| Médico | Recibir citas coherentes en su agenda. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema recibe una asignación validada con todos los datos requeridos. |
| 2 | — | El sistema verifica, dentro de una misma transacción, que la asignación siga siendo válida antes de guardar. |
| 3 | — | El sistema genera el número de ficha y registra paciente, especialidad, médico, fecha, hora, consultorio y estado PROGRAMADA. |
| 4 | — | El sistema confirma el registro, marca la solicitud como ASIGNADA y deja la cita disponible para seguimiento. |
| 5 | — | El sistema genera la notificación (CU-16) y emite la actualización en tiempo real (WebSocket) a las pantallas afectadas. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | Algún recurso dejó de estar disponible antes de guardar. | El sistema no registra la cita con esa combinación y devuelve el proceso a una nueva validación/asignación. |
| 3a | Falta uno de los datos requeridos de la cita. | El sistema no confirma el registro hasta contar con la información necesaria. |

### Requisitos especiales (no funcionales específicos)

- **Integridad:** La cita no debe persistir con recursos duplicados o incompatibles, conforme RNF-04.

- **Consistencia:** La disponibilidad utilizada para confirmar debe ser vigente, conforme RNF-03.

- **Seguridad:** Solo operaciones autorizadas deben poder confirmar una cita.

### Variaciones de tecnología y datos

- El estado inicial es PROGRAMADA. El número de ficha es secuencial por médico y día (propuesta pendiente de validar).

- Cambio 04/11/2026: la confirmación es automática; ya no la realiza el personal de enfermería.

<a id="cu-08"></a>

## CU-08 — Consultar cita paciente

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-08 |
| Nombre del caso de uso: | Consultar Citas del Paciente |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-08: Consultar Citas del Paciente |
| Actor Principal (Quién): | Paciente |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El paciente accede a la consulta de las citas registradas a su nombre. |
| Precondiciones (Cuándo): | • El paciente se encuentra autenticado.<br>• Existen o pueden existir citas asociadas a su identidad. |
| Postcondiciones (Éxito): | • El paciente visualiza únicamente sus propias citas.<br>• La consulta no modifica los datos de las citas. |
| Garantía Mínima (Fallo): | Si no existen citas, el sistema informa que no hay registros para mostrar sin exponer información de otros pacientes. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al paciente consultar cuándo, dónde y con quién será atendido (número de ficha, médico, especialidad, fecha, hora, consultorio y estado), mostrando únicamente sus citas, y desde allí cancelar (CU-11) o reprogramar (CU-10). Derivado de RF-08 y RU-03. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Conocer médico, especialidad, fecha, hora, consultorio y estado de sus citas. |
| Hospital Obrero CNS | Proteger la confidencialidad de la información de otros pacientes. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El paciente abre la opción de consultar sus citas. | El sistema identifica al paciente autenticado. |
| 2 | — | El sistema busca únicamente las citas asociadas a ese paciente. |
| 3 | El paciente revisa la lista de citas. | El sistema muestra las citas con ficha, médico, especialidad, fecha, hora, consultorio y estado. |
| 4 | El paciente selecciona una cita. | El sistema muestra el detalle permitido y las acciones disponibles: cancelar (CU-11) o reprogramar (CU-10) si está PROGRAMADA. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El paciente no tiene citas registradas. | El sistema informa que no existen citas para mostrar. |
| 4a | El paciente intenta acceder a información que no corresponde a sus propias citas. | El sistema impide el acceso a la información no autorizada. |

### Requisitos especiales (no funcionales específicos)

- **Confidencialidad:** Debe cumplirse RNF-02: el paciente solo puede consultar su propia información.

- **Seguridad:** Debe cumplirse RNF-01 para controlar el acceso por rol.

- **Usabilidad:** La interfaz debe presentar únicamente información y operaciones necesarias para el paciente, conforme RNF-05.

### Variaciones de tecnología y datos

- El documento base no define formato de presentación, filtros adicionales ni canal de acceso; estos elementos quedan para diseño.

<a id="cu-09"></a>

## CU-09 — Consultar citas registradas

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-09 |
| Nombre del caso de uso: | Consultar Citas Registradas |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-09: Consultar Citas Registradas |
| Actor Principal (Quién): | Personal de Enfermería / Admisión (y pantalla de sala de espera TV, solo lectura) |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El personal necesita localizar fichas del día actual o una pantalla de sala de espera debe mostrar el listado de fichas del día. |
| Precondiciones (Cuándo): | • El usuario se encuentra autenticado y autorizado.<br>• Existen citas registradas o criterios de búsqueda válidos. |
| Postcondiciones (Éxito): | • Se presentan las fichas del día actual coincidentes con el criterio seleccionado.<br>• La consulta no modifica los registros. |
| Garantía Mínima (Fallo): | Si no existen coincidencias, el sistema informa que no se encontraron citas sin alterar la información almacenada. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al personal consultar las fichas del día actual por paciente, médico, estado o consultorio. Una pantalla de TV en sala de espera muestra, solo lectura, el listado de fichas del día. No permite consultar ni modificar fechas posteriores. Derivado de RF-09 y RU-06. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Enfermería / Admisión | Localizar rápidamente citas para su gestión operativa. |
| Paciente | Asegurar que su información solo sea consultada por personal autorizado. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal abre la opción de consulta de fichas del día. | El sistema muestra los criterios de búsqueda habilitados (fecha fija: día actual). |
| 2 | El personal selecciona un criterio: paciente, médico, estado o consultorio. | El sistema recibe el criterio y el valor de búsqueda. |
| 3 | El personal ejecuta la consulta. | El sistema busca las fichas del día coincidentes. |
| 4 | El personal revisa los resultados. | El sistema presenta las fichas que cumplen el criterio. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen fichas que coincidan con el criterio. | El sistema informa que no se encontraron resultados. |
| 2a | El criterio de búsqueda está incompleto o no es válido. | El sistema solicita corregir el criterio antes de ejecutar la consulta. |
| 2b | Se intenta consultar una fecha distinta del día actual. | El sistema no lo permite para este rol. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** Solo personal autorizado puede consultar citas mediante esta función.

- **Confidencialidad:** La información mostrada debe limitarse a la permitida para el rol, conforme RNF-02.

- **Usabilidad:** La interfaz debe mostrar solo los criterios y operaciones correspondientes al rol, conforme RNF-05.

### Variaciones de tecnología y datos

- Panel de TV (solo lectura): muestra número de ficha, especialidad, médico, hora y estado de las fichas del día; no muestra datos clínicos. La autenticación de la pantalla queda pendiente.

- Los criterios son paciente, médico, estado y consultorio; la fecha siempre es el día actual (cambio 04/11/2026).

<a id="cu-10"></a>

## CU-10 — Reprogramar cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-10 |
| Nombre del caso de uso: | Reprogramar Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-10: Reprogramar Cita |
| Actor Principal (Quién): | Paciente |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El paciente necesita cambiar el día de una cita programada. |
| Precondiciones (Cuándo): | • El paciente está autenticado.<br>• Existe una cita programada del paciente. |
| Postcondiciones (Éxito): | • La cita vigente queda CANCELADA (CU-11) y su cupo libre.<br>• Se registra una nueva solicitud para el siguiente día hábil con menor prioridad que las solicitudes de pacientes que no cancelaron. |
| Garantía Mínima (Fallo): | El paciente es informado de que reprogramar implica cancelar la cita vigente antes de confirmar; si desiste, la cita se conserva. |
| Actores Secundarios: | CU-11 Cancelar Cita, CU-04 Solicitar Cita Médica, CU-05 Asignar Cita Automáticamente, CU-16 Generar Notificación de Cita y CU-18 Consultar Historial de Cambios. |
| Descripción Breve (Qué/Para qué): | Reprogramar equivale a cancelar la cita vigente (CU-11) y enviar una nueva solicitud (CU-04) que se atiende con menor prioridad para el siguiente día hábil con cupo. El personal de enfermería no reprograma citas. Derivado de RF-10 y RU-03. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Cambiar su cita sabiendo que se cancelará la actual. |
| Hospital Obrero CNS | Liberar el cupo y dar prioridad a pacientes que no cancelaron. |
| Médico | Mantener una agenda actualizada. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El paciente selecciona su cita programada y solicita reprogramarla. | El sistema muestra los datos de la cita y advierte que se cancelará. |
| 2 | El paciente confirma. | El sistema cancela la cita mediante CU-11 y libera el cupo. |
| 3 | — | El sistema registra una nueva solicitud para el siguiente día hábil con prioridad menor (CU-04/CU-05). |
| 4 | — | El sistema procesa la solicitud (CU-05), genera la notificación (CU-16) y conserva el registro (CU-18). |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El paciente desiste de reprogramar. | El sistema no cancela la cita y la conserva PROGRAMADA. |
| 3a | No existen cupos en el siguiente día hábil. | La solicitud queda EN_ESPERA y avanza al siguiente día hábil, hasta 15 días hábiles. |
| 1a | La cita no está PROGRAMADA (ya ATENDIDA, CANCELADA o con asistencia confirmada). | El sistema no permite reprogramarla. |

### Requisitos especiales (no funcionales específicos)

- **Prioridad:** las solicitudes de pacientes que no cancelaron se atienden antes que las que provienen de una reprogramación.

- **Confidencialidad:** solo el paciente titular de la cita puede reprogramarla.

- **Trazabilidad:** la cancelación y la nueva solicitud quedan registradas (CU-18).

### Variaciones de tecnología y datos

- Cambio 04/11/2026: el actor pasa de enfermería a paciente y desaparece la reprogramación directa por el personal.

<a id="cu-11"></a>

## CU-11 — Cancelar cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-11 |
| Nombre del caso de uso: | Cancelar Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-11: Cancelar Cita |
| Actor Principal (Quién): | Paciente / Sistema (cancelación automática) |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El paciente decide cancelar su cita, o faltan 30 minutos para la hora de una cita cuya asistencia no fue confirmada por enfermería. |
| Precondiciones (Cuándo): | • Existe una cita en estado PROGRAMADA.<br>• Para la cancelación por el paciente: el paciente está autenticado. |
| Postcondiciones (Éxito): | • La cita queda CANCELADA y el registro de la cita se conserva (no se borra).<br>• Queda registrado el motivo (cancelada por el paciente / no confirmó asistencia).<br>• Se genera la notificación y el registro de historial. |
| Garantía Mínima (Fallo): | Si la cita no puede cancelarse (ya ATENDIDA, CANCELADA o con asistencia confirmada), el sistema no modifica su estado. |
| Actores Secundarios: | CU-16 Generar Notificación de Cita y CU-18 Consultar Historial de Cambios. |
| Descripción Breve (Qué/Para qué): | Permite al paciente cancelar su cita y define la cancelación automática: si enfermería no confirma la asistencia presencial hasta 30 minutos antes de la hora, el sistema cancela la cita. El cupo liberado no se reasigna automáticamente por ahora. Derivado de RF-11 y RU-03. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Cancelar su cita cuando no pueda asistir y ser avisado oportunamente. |
| Enfermería / Admisión | Contar con fichas del día depuradas, sin intervenir en cancelaciones. |
| Administración | Mantener trazabilidad de cancelaciones para control y análisis. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El paciente selecciona su cita programada y solicita cancelarla. | El sistema muestra la información de la cita. |
| 2 | El paciente confirma la cancelación. | El sistema registra el motivo «Cancelada por el paciente» y cambia el estado a CANCELADA. |
| 3 | — | El sistema conserva la cita, registra la operación (CU-18) y genera la notificación (CU-16). |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| A1 | Faltan 30 minutos para la hora de la cita y enfermería no confirmó la asistencia. | El sistema cancela automáticamente la cita (motivo «No confirmó asistencia», usuario responsable «Sistema»), la conserva y notifica al paciente. |
| 1a | La cita no existe o no está PROGRAMADA. | El sistema informa que la cancelación no puede ejecutarse sobre ese registro. |
| 2a | La cita ya tiene la asistencia confirmada o ya fue atendida. | El sistema no permite cancelarla. |

### Requisitos especiales (no funcionales específicos)

- **Trazabilidad:** la cancelación conserva el motivo, el usuario (o «Sistema») y el momento.

- **Seguridad:** solo el paciente titular de la cita puede cancelarla; la cancelación automática la ejecuta el sistema, no el personal.

- **Prioridad:** quien cancela y vuelve a solicitar lo hace con menor prioridad (CU-10).

### Variaciones de tecnología y datos

- Motivos definidos por el sistema: CANCELADA_POR_PACIENTE y NO_CONFIRMO_ASISTENCIA (se retira el catálogo libre de motivos).

- Cambio 04/11/2026: se retira la cancelación manual por enfermería/admisión; se agrega la cancelación automática.

<a id="cu-12"></a>

## CU-12 — Actualizar estado de cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-12 |
| Nombre del caso de uso: | Actualizar Estado de Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-12: Actualizar Estado de Cita |
| Actor Principal (Quién): | Personal de Enfermería (confirma asistencia) / Médico (finaliza la atención) / Sistema (cancelación automática) |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El paciente se presenta a confirmar su asistencia, el médico finaliza la atención o el sistema cancela por falta de confirmación. |
| Precondiciones (Cuándo): | • El usuario se encuentra autenticado y autorizado.<br>• La cita existe y puede ser actualizada por el rol correspondiente. |
| Postcondiciones (Éxito): | • La cita refleja el estado ASISTENCIA_CONFIRMADA, ATENDIDA o CANCELADA según el evento.<br>• El cambio queda registrado para seguimiento. |
| Garantía Mínima (Fallo): | Si el usuario no está autorizado o el cambio no corresponde al estado de la cita, el sistema no aplica la actualización. |
| Actores Secundarios: | CU-11 Cancelar Cita, CU-15 Registrar Datos de Atención y CU-18 Consultar Historial de Cambios. |
| Descripción Breve (Qué/Para qué): | Actualiza el estado de la cita: PROGRAMADA → ASISTENCIA_CONFIRMADA (enfermería, presencial, hasta 30 minutos antes de la hora) → ATENDIDA (el médico registra el fin; no registra inicio); o CANCELADA. Enfermería solo opera sobre fichas del día actual. Derivado de RF-12. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Médico / Enfermería | Mantener actualizado el resultado operativo de la cita. |
| Administración | Contar con estados confiables para seguimiento y reportes. |
| Paciente | Disponer de información coherente sobre la situación de su cita. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | Enfermería o médico selecciona una cita del día. | El sistema muestra el estado actual y las acciones permitidas para su rol. |
| 2 | Enfermería confirma que el paciente se presentó (hasta 30 min antes de la hora). | El sistema valida rol, fecha del día y plazo, y cambia la cita a ASISTENCIA_CONFIRMADA. |
| 3 | El médico registra el fin de la atención. | El sistema valida que la cita estaba ASISTENCIA_CONFIRMADA y la cambia a ATENDIDA (con CU-15). |
| 4 | — | El sistema conserva la operación para trazabilidad e informes posteriores. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El usuario no posee permiso para actualizar el estado. | El sistema rechaza la operación y mantiene el estado existente. |
| 2b | El cambio no corresponde al estado actual (por ejemplo, ATENDIDA sin asistencia confirmada). | El sistema no modifica la cita e informa el motivo. |
| 2c | La cita no es del día actual. | El sistema impide a enfermería modificarla. |
| 2d | Ya pasó el plazo de 30 minutos antes de la hora. | La cita ya fue cancelada automáticamente; enfermería no puede confirmarla. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** La actualización debe depender del rol y permisos, conforme RNF-01.

- **Integridad:** La cita debe conservar un estado coherente y trazable.

- **Usabilidad:** Cada rol debe visualizar únicamente las acciones de estado que le correspondan, conforme RNF-05.

### Variaciones de tecnología y datos

- Estados definidos el 04/11/2026: PROGRAMADA, ASISTENCIA_CONFIRMADA, ATENDIDA y CANCELADA. «Inexistente» e «inasistencia» ya no son estados (la inasistencia es una cancelación automática por falta de confirmación).

<a id="cu-13"></a>

## CU-13 — Consultar agenda médica

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-13 |
| Nombre del caso de uso: | Consultar Agenda Médica |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-13: Consultar Agenda Médica |
| Actor Principal (Quién): | Médico |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El médico necesita revisar las citas que tiene programadas. |
| Precondiciones (Cuándo): | • El médico se encuentra autenticado.<br>• El sistema puede identificar las citas asignadas al médico. |
| Postcondiciones (Éxito): | • El médico visualiza las citas del día actual (cantidad, número de ficha y hora).<br>• Puede consultar otras fechas con un botón/selector.<br>• La consulta no modifica las citas. |
| Garantía Mínima (Fallo): | Si no existen citas en el período seleccionado, el sistema informa que no hay citas programadas sin mostrar información de otros médicos. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al médico consultar las citas del día actual y, mediante un botón, las de otras fechas. No se muestran todas las fechas a la vez. Derivado de RF-13 y RU-08. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Médico | Conocer las citas programadas dentro del período de trabajo. |
| Paciente | Asegurar que la cita asignada aparezca en la agenda del médico correspondiente. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El médico abre la opción de agenda. | El sistema identifica al médico autenticado y consulta sus citas del día actual. |
| 2 | — | El sistema calcula la cantidad de citas del día. |
| 3 | El médico revisa la cantidad de citas, el número de ficha y la hora de cada paciente. | El sistema muestra las citas del día. |
| 4 | El médico pulsa «otras fechas» y elige una fecha (opcional). | El sistema muestra las citas de la fecha elegida. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen citas asignadas en el período. | El sistema informa que no hay citas para mostrar en la agenda seleccionada. |
| 1a | El usuario no posee rol de médico para esta función. | El sistema impide el acceso a la agenda médica restringida. |

### Requisitos especiales (no funcionales específicos)

- **Confidencialidad:** El médico debe ver únicamente las citas e información permitida para su rol.

- **Usabilidad:** Vista del día actual por defecto y selector de otras fechas, conforme RF-13.

- **Seguridad:** El acceso debe controlarse por rol, conforme RNF-01.

- **Tiempo real:** La lista del día se actualiza por WebSocket, conforme RNF-08.

### Variaciones de tecnología y datos

- Cambio 04/11/2026: se elimina la vista semanal; se agrega el selector de otras fechas.

<a id="cu-14"></a>

## CU-14 — Consultar información permitida del paciente

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-14 |
| Nombre del caso de uso: | Consultar Información Permitida del Paciente |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-14: Consultar Información Permitida del Paciente |
| Actor Principal (Quién): | Médico |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El médico necesita consultar información del paciente asociado a una cita que le fue asignada. |
| Precondiciones (Cuándo): | • El médico se encuentra autenticado.<br>• El paciente consultado tiene una cita en la agenda del médico. |
| Postcondiciones (Éxito): | • El médico visualiza el historial del paciente: nombre, citas pasadas con fecha y hora, atenciones y recetas anteriores.<br>• La consulta no modifica los datos (la receta nueva se agrega en CU-15). |
| Garantía Mínima (Fallo): | Si el paciente no está asociado a una cita asignada al médico o la información no está permitida, el sistema impide la consulta. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al médico consultar el historial del paciente (nombre, citas pasadas con fecha y hora, atenciones y recetas anteriores) solo de pacientes con cita en su agenda. Derivado de RF-14 y RU-09. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Médico | Disponer de la información necesaria durante la atención. |
| Paciente | Proteger su información y limitarla al profesional autorizado. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El médico selecciona una cita de su agenda. | El sistema verifica que la cita esté asignada al médico autenticado. |
| 2 | El médico solicita consultar el historial del paciente. | El sistema identifica la información permitida para el rol. |
| 3 | — | El sistema recupera el nombre, las citas pasadas con fecha y hora, las atenciones y las recetas anteriores del paciente. |
| 4 | El médico revisa la información. | El sistema muestra los datos permitidos sin exponer información restringida. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | La cita no está asignada al médico autenticado. | El sistema impide acceder a la información del paciente. |
| 3a | El dato solicitado no se encuentra autorizado para el rol. | El sistema no muestra el dato restringido. |

### Requisitos especiales (no funcionales específicos)

- **Confidencialidad:** Debe cumplirse RNF-02, limitando la información al rol y relación correspondiente.

- **Seguridad:** Debe verificarse la autenticación y autorización antes de mostrar datos.

- **Usabilidad:** Solo deben presentarse los datos necesarios para la función médica, conforme RNF-05.

### Variaciones de tecnología y datos

- Historial permitido (definido el 04/11/2026): nombre del paciente, citas pasadas, atenciones con fecha y hora y recetas anteriores; solo de pacientes con cita en la agenda del médico.

- Se distingue historial de citas, historial de atenciones e historial clínico; la receta nueva se agrega en CU-15.

<a id="cu-15"></a>

## CU-15 — Registrar datos de atención

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-15 |
| Nombre del caso de uso: | Registrar Datos de Atención |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-15: Registrar Datos de Atención |
| Actor Principal (Quién): | Médico |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El médico atiende al paciente y registra la medicación o receta y el fin de la atención. |
| Precondiciones (Cuándo): | • La cita está en estado ASISTENCIA_CONFIRMADA.<br>• El médico está autenticado y la cita pertenece a su agenda. |
| Postcondiciones (Éxito): | • La medicación o receta queda registrada en el historial clínico del paciente.<br>• El sistema completa ID de afiliado, número de ficha, fecha y hora de la atención e identificación de la consulta.<br>• La cita pasa a ATENDIDA (CU-12) al registrar el fin. |
| Garantía Mínima (Fallo): | Si la cita no está en ASISTENCIA_CONFIRMADA o no pertenece al médico, el sistema no registra la receta ni finaliza la cita. |
| Actores Secundarios: | CU-12 Actualizar Estado de Cita puede utilizarse para reflejar que la atención fue realizada. |
| Descripción Breve (Qué/Para qué): | Permite al médico registrar la medicación o receta y finalizar la cita. El sistema completa automáticamente ID de afiliado, número de ficha, fecha/hora e identificación de la consulta; el médico solo ingresa la medicación. Conserva la hora de confirmación y la de fin. Derivado de RF-15 y RU-10. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Médico | Registrar la receta sin llenar manualmente datos que el sistema ya conoce. |
| Paciente | Contar con su historial clínico actualizado. |
| Administración | Disponer de horas de confirmación y fin para indicadores. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El médico selecciona la cita en atención. | El sistema muestra los datos autocompletados (afiliado, ficha, fecha/hora y consulta). |
| 2 | El médico registra la medicación o receta. | El sistema valida los datos de la receta. |
| 3 | — | El sistema agrega la receta al historial clínico del paciente. |
| 4 | El médico registra el fin de la atención. | El sistema guarda la hora de fin y cambia la cita a ATENDIDA mediante CU-12. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | La cita no existe, no es de la agenda del médico o no está en ASISTENCIA_CONFIRMADA. | El sistema impide registrar la receta y finalizar la cita. |
| 2a | La medicación está vacía o no puede validarse. | El sistema no confirma el registro hasta corregir la información. |

### Requisitos especiales (no funcionales específicos)

- **Integridad:** La receta debe quedar asociada a la cita y al afiliado correctos.

- **Confidencialidad:** La información del paciente debe mantenerse protegida conforme RNF-02.

- **Seguridad:** Solo el médico titular de la agenda puede registrar la receta.

### Variaciones de tecnología y datos

- El médico no registra el inicio de la atención, solo el fin. El tiempo de espera y de atención por separado no es medible con este flujo (solo confirmación → fin); pendiente de validar frente a RF-20.

- Cambio 04/11/2026: se incorpora el registro de receta/medicación y se retira el registro de asistencia (pasa a CU-12, por enfermería).

<a id="cu-16"></a>

## CU-16 — Generar notificación

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-16 |
| Nombre del caso de uso: | Generar Notificación de Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-16: Generar Notificación de Cita |
| Actor Principal (Quién): | Sistema (proceso automático) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Una cita es registrada o cancelada (por el paciente o automáticamente), o una solicitud en espera recibe cita, y la operación queda confirmada. |
| Precondiciones (Cuándo): | • Existe una cita asociada a un paciente.<br>• La operación de registro o cancelación fue confirmada. |
| Postcondiciones (Éxito): | • El sistema genera la notificación correspondiente para el paciente. |
| Garantía Mínima (Fallo): | Si la notificación no puede generarse, la información confirmada de la cita no debe transformarse en una notificación contradictoria o incompleta. |
| Actores Secundarios: | El documento base no define un servicio o canal externo de notificaciones. |
| Descripción Breve (Qué/Para qué): | Genera información para el paciente cuando una cita se registra (incluida una solicitud en espera que recibe cita) o se cancela (por el paciente o automáticamente). Derivado de RF-16 y RU-04. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Recibir información cuando su cita sea creada o cambie. |
| Enfermería / Admisión | Reducir incertidumbre sobre cambios ya confirmados. |
| Hospital Obrero CNS | Mantener comunicación coherente con el estado registrado de las citas. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema detecta que una cita fue registrada o cancelada. |
| 2 | — | El sistema identifica al paciente asociado y el tipo de operación confirmada. |
| 3 | — | El sistema genera el contenido de la notificación correspondiente. |
| 4 | El paciente recibe o consulta la notificación en la aplicación. | El sistema deja disponible la notificación y la envía en tiempo real (WebSocket). |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | La operación sobre la cita no llegó a confirmarse. | El sistema no genera una notificación que indique un cambio inexistente. |
| 2a | No se dispone de la información necesaria para generar la notificación. | El sistema no genera contenido incompleto; la situación requiere revisión antes de notificar. |

### Requisitos especiales (no funcionales específicos)

- **Confidencialidad:** La notificación debe contener únicamente información correspondiente al paciente y su cita.

- **Consistencia:** El contenido debe corresponder al estado confirmado de la cita.

- **Usabilidad:** La información generada debe permitir identificar claramente el cambio ocurrido.

### Variaciones de tecnología y datos

- Canal definido el 04/11/2026: notificación dentro de la aplicación y actualización por WebSocket.

- Tipos: cita asignada, solicitud en espera que recibe cita, cita cancelada por el paciente y cita cancelada automáticamente.

<a id="cu-17"></a>

## CU-17 — Generar recordatorio de cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-17 |
| Nombre del caso de uso: | Generar Recordatorio de Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-17: Generar Recordatorio de Cita |
| Actor Principal (Quién): | Sistema (proceso automático) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Faltan 45 minutos para la hora de una cita programada cuya asistencia no ha sido confirmada. |
| Precondiciones (Cuándo): | • Existe una cita PROGRAMADA.<br>• La asistencia aún no fue confirmada por enfermería. |
| Postcondiciones (Éxito): | • El paciente recibe el aviso de que perderá su cita si no confirma su asistencia en el hospital hasta 30 minutos antes de la hora. |
| Garantía Mínima (Fallo): | Si la cita ya fue cancelada o confirmada antes del momento del aviso, no se genera un aviso que contradiga su estado vigente. |
| Actores Secundarios: | El documento base no define un servicio o canal externo de recordatorios. |
| Descripción Breve (Qué/Para qué): | Genera un aviso 45 minutos antes de la hora de la cita indicando que, si no confirma su asistencia presencial hasta 30 minutos antes, la cita se cancelará. Derivado de RF-17 y RU-04. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Paciente | Recordar oportunamente la fecha de su cita. |
| Hospital Obrero CNS | Reducir la posibilidad de inasistencias relacionadas con el olvido de citas. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | — | El sistema identifica una cita PROGRAMADA a 45 minutos de su hora. |
| 2 | — | El sistema verifica que la cita siga vigente y sin asistencia confirmada. |
| 3 | — | El sistema genera el aviso con la información de la cita y el plazo de confirmación. |
| 4 | El paciente recibe o consulta el aviso en la aplicación. | El sistema deja disponible el aviso y lo envía en tiempo real. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | La cita fue cancelada o la asistencia ya fue confirmada. | El sistema no genera el aviso. |
| 1a | Aún no faltan 45 minutos para la hora de la cita. | El sistema no genera el aviso en ese momento. |

### Requisitos especiales (no funcionales específicos)

- **Consistencia:** El recordatorio debe basarse en los datos vigentes de la cita.

- **Confidencialidad:** Solo debe incluir información correspondiente al paciente y su cita.

- **Escalabilidad:** La generación de recordatorios debe mantener las reglas del proceso al aumentar el volumen de citas, conforme RNF-06.

### Variaciones de tecnología y datos

- Anticipación definida el 04/11/2026: 45 minutos antes de la cita. Canal: aplicación y WebSocket.

<a id="cu-18"></a>

## CU-18 — Consultar historial de cambios

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-18 |
| Nombre del caso de uso: | Consultar Historial de Cambios |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-18: Consultar Historial de Cambios |
| Actor Principal (Quién): | Personal de Enfermería / Administrador |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Un usuario autorizado necesita conocer qué se modificó en una cita, quién realizó el cambio y cuándo. |
| Precondiciones (Cuándo): | • El usuario se encuentra autenticado y autorizado.<br>• Existe una cita con operaciones registradas o historial disponible. |
| Postcondiciones (Éxito): | • El usuario visualiza las operaciones históricas permitidas de la cita.<br>• La consulta no modifica el historial. |
| Garantía Mínima (Fallo): | Si el usuario no posee permisos o la cita no existe, el sistema no expone información del historial. |
| Actores Secundarios: | Las cancelaciones (por el paciente o automáticas, con usuario «Sistema»), las confirmaciones de asistencia y la finalización de la atención alimentan el historial. |
| Descripción Breve (Qué/Para qué): | Permite consultar el historial de operaciones realizadas sobre una cita, identificando la modificación, el usuario responsable y la fecha/hora correspondiente. Derivado de RF-18. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Enfermería / Admisión | Conocer cambios realizados durante la gestión operativa de una cita. |
| Administrador | Disponer de trazabilidad para supervisión y control. |
| Paciente | Beneficiarse de un proceso de citas auditable y consistente. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El usuario autorizado localiza una cita. | El sistema muestra la información general permitida de la cita. |
| 2 | El usuario solicita consultar el historial de cambios. | El sistema verifica sus permisos para acceder al historial. |
| 3 | — | El sistema recupera las operaciones conservadas para la cita. |
| 4 | El usuario revisa el historial. | El sistema presenta el cambio realizado, usuario responsable y fecha/hora correspondiente para cada operación disponible. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El usuario no posee permiso para consultar el historial. | El sistema impide el acceso a la información de trazabilidad. |
| 3a | La cita no tiene cambios registrados. | El sistema informa que no existen operaciones históricas adicionales para mostrar. |

### Requisitos especiales (no funcionales específicos)

- **Trazabilidad:** Las cancelaciones, confirmaciones de asistencia y finalizaciones de atención deben conservar la acción, usuario responsable (o «Sistema») y momento de ejecución.

- **Seguridad:** El historial solo debe ser accesible por roles autorizados.

- **Confidencialidad:** La consulta debe limitar la información a lo permitido por el rol.

### Variaciones de tecnología y datos

- El documento base no define el período de retención, mecanismo de auditoría ni formato de exportación del historial.

<a id="cu-19"></a>

## CU-19 — Generar reportes de citas

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-19 |
| Nombre del caso de uso: | Generar Reportes de Citas |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-19: Generar Reportes de Citas |
| Actor Principal (Quién): | Administrador |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El administrador solicita un reporte para supervisar el funcionamiento del proceso de atención. |
| Precondiciones (Cuándo): | • El administrador se encuentra autenticado y autorizado.<br>• Existen datos de citas registrados para el criterio solicitado. |
| Postcondiciones (Éxito): | • El sistema presenta información agrupada según el reporte seleccionado o la agenda de supervisión (solo lectura, tiempo real).<br>• La generación no modifica los registros de citas. |
| Garantía Mínima (Fallo): | Si no existen datos para el criterio seleccionado, el sistema informa que no hay información para el reporte solicitado. |
| Actores Secundarios: | No se identifican herramientas externas de reportes en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite generar reportes de citas programadas, atendidas y canceladas, por médico, especialidad y consultorio, y mostrar al administrador una agenda de supervisión (resumen por especialidad y médico; detalle por médico) en solo lectura y tiempo real. Derivado de RF-19 y RU-12. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Administrador | Supervisar el funcionamiento del proceso de citas y atención. |
| Hospital Obrero CNS | Disponer de información consolidada para control administrativo. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El administrador accede al módulo de reportes y supervisión. | El sistema muestra los reportes y la agenda de supervisión. |
| 2 | El administrador abre la agenda de supervisión o selecciona un reporte. | El sistema muestra el resumen por especialidad (Medicina General, Odontología, Pediatría) con médico, horario y cantidad de fichas; los horarios no laborales aparecen sombreados. |
| 3 | El administrador selecciona un médico. | El sistema muestra sus citas programadas con número de ficha, horario, estado y cumplimiento (mínimo 5 y máximo 7 fichas por día). |
| 4 | El administrador revisa el resultado. | El sistema actualiza la información en tiempo real (WebSocket). |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen datos para el criterio seleccionado. | El sistema informa que no hay información disponible con ese criterio. |
| 1a | El usuario no posee permiso administrativo. | El sistema impide el acceso. |
| 3b | El administrador intenta modificar una cita desde la agenda. | El sistema no lo permite: la agenda de supervisión es de solo lectura. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** La función debe restringirse a usuarios autorizados.

- **Solo lectura:** El administrador observa y verifica el cumplimiento de citas y horarios; no modifica citas.

- **Tiempo real:** La agenda se actualiza por WebSocket, conforme RNF-08.

- **Escalabilidad:** El crecimiento del volumen de citas no debe alterar las reglas fundamentales de consolidación, conforme RNF-06.

### Variaciones de tecnología y datos

- Los reportes incluyen citas programadas, atendidas, canceladas y agrupaciones por médico, especialidad y consultorio.

- Cambio 04/11/2026: se agrega la agenda de supervisión del administrador.

<a id="cu-20"></a>

## CU-20 — Consultar indicadores de gestión

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-20 |
| Nombre del caso de uso: | Consultar Indicadores de Gestión |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 04/11/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador (modificado 04/11/2026) |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-20: Consultar Indicadores de Gestión |
| Actor Principal (Quién): | Administrador |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El administrador necesita analizar información consolidada para apoyar la toma de decisiones del hospital. |
| Precondiciones (Cuándo): | • El administrador se encuentra autenticado y autorizado.<br>• Existen datos consolidados de citas y atención. |
| Postcondiciones (Éxito): | • El sistema muestra los indicadores disponibles calculados a partir de los registros almacenados.<br>• La consulta no modifica los datos fuente. |
| Garantía Mínima (Fallo): | Si no existe información suficiente para un indicador, el sistema no debe presentar un valor como si estuviera respaldado por datos inexistentes. |
| Actores Secundarios: | No se identifican plataformas analíticas externas en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite analizar indicadores relacionados con demanda, cancelaciones, inasistencias, tiempos de espera, tiempos de atención y carga de trabajo para apoyar la toma de decisiones. Derivado de RF-20 y RU-12. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

> **Nota sobre la fuente:** este apartado repite la ficha del caso de uso en el documento original; no hay tabla de intereses de stakeholders en esta sección.

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-20: Consultar Indicadores de Gestión |
| Actor Principal (Quién): | Administrador |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El administrador necesita analizar información consolidada para apoyar la toma dedecisiones del hospital. |
| Precondiciones (Cuándo): | • El administrador se encuentra autenticado y autorizado.<br>• Existen datos consolidados de citas y atención. |
| Postcondiciones (Éxito): | • El sistema muestra los indicadores disponibles calculados a partir de los registros almacenados.<br>• La consulta no modifica los datos fuente. |
| Garantía Mínima (Fallo): | Si no existe información suficiente para un indicador, el sistema no debe presentar un valor como si estuviera respaldado por datos inexistentes. |
| Actores Secundarios: | No se identifican plataformas analíticas externas en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite analizar indicadores relacionados con demanda, cancelaciones, inasistencias, tiempos de espera, tiempos de atención y carga de trabajo para apoyar la toma de decisiones. Derivado de RF-20 y RU-12. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El administrador accede al componente de indicadores. | El sistema muestra los indicadores disponibles para consulta. |
| 2 | El administrador selecciona el indicador o conjunto de indicadores que desea analizar. | El sistema identifica los datos registrados necesarios para el cálculo. |
| 3 | — | El sistema consolida la información y calcula los indicadores disponibles. |
| 4 | El administrador revisa los resultados. | El sistema presenta demanda, cancelaciones, inasistencias, tiempos de espera, tiempos de atención o carga de trabajo según la consulta. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existe información suficiente para calcular un indicador solicitado. | El sistema informa la ausencia o insuficiencia de datos para ese indicador. |
| 1a | El usuario no posee permiso administrativo. | El sistema impide el acceso al componente analítico. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** La consulta de indicadores debe limitarse a usuarios autorizados.

- **Confidencialidad:** La información consolidada debe respetar la protección de datos de pacientes, conforme RNF-02.

- **Escalabilidad:** El componente debe poder mantener las reglas de cálculo al crecer el volumen de registros, conforme RNF-06.

### Variaciones de tecnología y datos

- El documento base identifica los indicadores a analizar, pero no define fórmulas, metas, períodos ni visualizaciones; esos elementos requieren validación posterior.

- Cambio 04/11/2026: se agrega el cumplimiento del mínimo de 5 fichas por médico y día; las inasistencias se miden como cancelaciones automáticas por falta de confirmación; los tiempos de espera y atención quedan limitados al intervalo confirmación → fin (ver CU-15).

