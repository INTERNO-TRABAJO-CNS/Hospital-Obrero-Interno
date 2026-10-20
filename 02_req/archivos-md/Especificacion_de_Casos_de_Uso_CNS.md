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
| Estado de los casos de uso | Borrador (según fichas individuales) |

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
| CU-03 | Registrar y consultar pacientes | RF-03 | Personal de Enfermería / Admisión |
| CU-04 | Solicitar cita médica | RF-04 | Paciente |
| CU-05 | Asignar cita automáticamente | RF-05 | Sistema (proceso automático de asignación) |
| CU-06 | Validar disponibilidad de Recursos | RF-06 | Sistema (proceso de validación) |
| CU-07 | Registrar cita confirmada | RF-07 | Sistema, personal de Enfermería-Admisión |
| CU-08 | Consultar cita paciente | RF-08 | Paciente |
| CU-09 | Consultar citas registradas | RF-09 | Personal de enfermería / Admisión |
| CU-10 | Reprogramar cita | RF-10 | Personal de enfermería / Admisión |
| CU-11 | Cancelar cita | RF-11 | Personal de enfermería / Admisión |
| CU-12 | Actualizar estado de cita | RF-12 | Médico / Personal de enfermería autorizado |
| CU-13 | Consultar agenda médica | RF-13 | Médico |
| CU-14 | Consultar información permitida del paciente | RF-14 | Médico |
| CU-15 | Registrar datos de atención | RF-15 | Médico / Personal de Enfermería autorizado |
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
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

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
| Descripción Breve (Qué/Para qué): | Permite al administrador gestionar usuarios, roles y permisos de acceso para controlar quién puede utilizar cada función del sistema. Derivado de RF-02. |

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
| 1 | El administrador abre la gestión de usuarios y roles. | El sistema muestra los usuarios y roles disponibles para administración. |
| 2 | El administrador selecciona un usuario o la opción de gestión correspondiente. | El sistema presenta la información y permisos actuales asociados. |
| 3 | El administrador registra o modifica el rol y los permisos. | El sistema valida los cambios solicitados. |
| 4 | El administrador confirma la operación. | El sistema guarda los cambios y aplica los permisos correspondientes. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | El usuario que intenta gestionar permisos no posee rol de administrador. | El sistema impide el acceso a la función de administración. |
| 3a | La configuración solicitada no puede validarse. | El sistema no guarda los cambios y solicita corregir la información antes de confirmar. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** Solo usuarios con permisos administrativos pueden gestionar usuarios, roles y permisos.

- **Consistencia:** Los cambios guardados deben reflejarse en las funciones disponibles para el rol afectado.

- **Usabilidad:** La gestión debe presentar únicamente opciones administrativas autorizadas.

### Variaciones de tecnología y datos

- El documento base no especifica una jerarquía interna de permisos más allá de los roles definidos; su detalle deberá establecerse posteriormente.

<a id="cu-03"></a>

## CU-03 — Registrar y consultar pacientes

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-03 |
| Nombre del caso de uso: | Registrar y Consultar Pacientes |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-03: Registrar y Consultar Pacientes |
| Actor Principal (Quién): | Personal de Enfermería / Admisión |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El personal autorizado necesita localizar o registrar a un paciente para gestionar sus citas. |
| Precondiciones (Cuándo): | • El personal se encuentra autenticado y autorizado.<br>• Se dispone de la identificación del paciente. |
| Postcondiciones (Éxito): | • Si el paciente ya existe, se recupera su registro.<br>• Si no existe, el paciente queda registrado sin duplicar información. |
| Garantía Mínima (Fallo): | Si se detecta un paciente ya registrado, el sistema evita crear un registro duplicado y conserva el registro existente. |
| Actores Secundarios: | No se identifican sistemas externos de afiliación o identificación en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al personal de enfermería/admisión registrar o localizar pacientes mediante su identificación, verificando previamente que no exista un registro duplicado. Derivado de RF-03 y RU-05. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Enfermería / Admisión | Localizar o registrar pacientes de forma rápida y sin duplicaciones. |
| Paciente | Mantener un único registro válido asociado a sus citas. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal ingresa la identificación del paciente. | El sistema busca si existe un paciente con esa identificación. |
| 2 | Si no existe, el personal solicita registrar al paciente. | El sistema habilita el registro de la información necesaria. |
| 3 | El personal completa la información requerida y confirma. | El sistema valida nuevamente que no exista duplicidad. |
| 4 | — | El sistema registra al paciente y confirma la operación. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1ª | El paciente ya se encuentra registrado. | El sistema muestra el registro existente y evita crear un duplicado. El flujo continúa con la consulta del paciente. |
| 3a | La información necesaria para el registro está incompleta o no puede validarse. | El sistema no registra al paciente y solicita completar o corregir la información. |

### Requisitos especiales (no funcionales específicos)

- **Confidencialidad:** La información del paciente debe mostrarse únicamente a usuarios autorizados, conforme RNF-02.

- **Seguridad:** El acceso a la función debe depender del rol autorizado, conforme RNF-01.

- **Integridad:** La identificación debe utilizarse para evitar registros duplicados.

### Variaciones de tecnología y datos

- El documento base no define el tipo de identificador ni la integración con sistemas externos de afiliación; estos aspectos quedan pendientes de definición.

<a id="cu-04"></a>

## CU-04 — Solicitar cita médica

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-04 |
| Nombre del caso de uso: | Solicitar Cita Médica |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-04: Solicitar Cita Médica |
| Actor Principal (Quién): | Paciente |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El paciente requiere atención médica y solicita una cita según la especialidad o atención requerida. |
| Precondiciones (Cuándo): | • El paciente se encuentra habilitado para solicitar atención.<br>• El paciente ha iniciado sesión con un rol autorizado. |
| Postcondiciones (Éxito): | • La solicitud de cita queda registrada.<br>• El paciente no selecciona manualmente médico, fecha, hora ni consultorio.<br>• La solicitud queda disponible para el proceso de asignación automática. |
| Garantía Mínima (Fallo): | Si no se puede registrar la solicitud, no se confirma ninguna cita ni se asignan recursos. |
| Actores Secundarios: | CU-05 Asignar Cita Automáticamente, como proceso relacionado posterior a la solicitud. |
| Descripción Breve (Qué/Para qué): | Permite al paciente solicitar una cita indicando la especialidad o atención requerida, sin elegir manualmente médico, fecha, hora o consultorio. Derivado de RF-04 y RU-01. |

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
| 1 | El paciente accede a la opción de solicitar cita. | El sistema muestra las especialidades o atenciones disponibles para solicitud. |
| 2 | El paciente selecciona la especialidad o atención requerida. | El sistema registra la selección para la solicitud. |
| 3 | El paciente confirma la solicitud. | El sistema registra la solicitud sin exigir seleccionar médico, fecha, hora o consultorio. |
| 4 | — | El sistema deriva la solicitud al proceso de asignación automática de cita (CU-05). |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El paciente no selecciona una especialidad o atención válida. | El sistema no permite confirmar la solicitud y solicita completar la selección. |
| 4a | No existe disponibilidad que permita una asignación inmediata. | El sistema no confirma una cita con recursos inexistentes u ocupados e informa que no fue posible completar la asignación. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** Solo el paciente autenticado debe poder registrar solicitudes a su nombre.

- **Usabilidad:** La solicitud no debe exigir al paciente seleccionar manualmente médico, fecha, hora o consultorio.

- **Consistencia:** La posterior asignación debe utilizar disponibilidad vigente, conforme RNF-03.

### Variaciones de tecnología y datos

- El documento base no define reglas de preferencia, prioridad o cola para solicitudes sin disponibilidad; deben validarse con la institución.

<a id="cu-05"></a>

## CU-05 — Asignar cita automáticamente

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-05 |
| Nombre del caso de uso: | Asignar Cita Automáticamente |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-05: Asignar Cita Automáticamente |
| Actor Principal (Quién): | Sistema (proceso automático de asignación) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Existe una solicitud de cita válida que debe ser procesada según la especialidad requerida. |
| Precondiciones (Cuándo): | • Existe una solicitud de cita registrada.<br>• Se dispone de información vigente de médicos, horarios y consultorios. |
| Postcondiciones (Éxito): | • Se asigna un médico de la especialidad requerida, una fecha, una hora y un consultorio disponibles.<br>• La asignación queda preparada para su registro y confirmación. |
| Garantía Mínima (Fallo): | Si no existen recursos compatibles disponibles, el sistema no confirma una asignación inválida ni duplica recursos. |
| Actores Secundarios: | CU-06 Validar Disponibilidad de Recursos y CU-07 Registrar Cita Confirmada. |
| Descripción Breve (Qué/Para qué): | Procesa una solicitud de cita y selecciona automáticamente recursos compatibles disponibles, evitando que el paciente tenga que buscar manualmente médicos y horarios. Derivado de RF-05 y RU-02. |

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
| 1 | — | El sistema recibe una solicitud válida con la especialidad requerida. |
| 2 | — | El sistema consulta la disponibilidad vigente de médicos, horarios y consultorios. |
| 3 | — | El sistema valida la combinación de recursos mediante CU-06. |
| 4 | — | El sistema selecciona una combinación disponible de médico, fecha, hora y consultorio. |
| 5 | — | El sistema envía la asignación validada al registro de cita confirmada (CU-07). |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | No existen recursos disponibles para la especialidad requerida. | El sistema no confirma una cita y comunica que no existe una asignación disponible con la información vigente. |
| 3a | Un recurso deja de estar disponible durante el procesamiento. | El sistema descarta esa combinación y continúa evaluando otra disponibilidad compatible, sin duplicar el recurso ocupado. |

### Requisitos especiales (no funcionales específicos)

- **Consistencia / Rendimiento:** Debe utilizarse el estado vigente de médicos, horarios y consultorios, conforme RNF-03.

- **Fiabilidad / Integridad:** Ante solicitudes concurrentes, un mismo recurso no puede confirmarse simultáneamente para más de una cita, conforme RNF-04.

- **Escalabilidad:** El proceso debe mantener las mismas reglas fundamentales al crecer el volumen de solicitudes, conforme RNF-06.

### Variaciones de tecnología y datos

- El documento base no define el criterio de prioridad para elegir entre varias combinaciones disponibles; este criterio debe acordarse con la institución.

<a id="cu-06"></a>

## CU-06 — Validar disponibilidad de Recursos

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-06 |
| Nombre del caso de uso: | Validar Disponibilidad de Recursos |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-06: Validar Disponibilidad de Recursos |
| Actor Principal (Quién): | Sistema (proceso de validación) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Una solicitud, confirmación o reprogramación requiere verificar médico, horario y consultorio. |
| Precondiciones (Cuándo): | • Existe una combinación candidata de médico, fecha, hora y consultorio.<br>• La información de disponibilidad se encuentra accesible para validación. |
| Postcondiciones (Éxito): | • La combinación queda marcada como disponible o no disponible.<br>• No se permite confirmar una combinación con recursos ocupados o solapados. |
| Garantía Mínima (Fallo): | Ante incertidumbre o conflicto de disponibilidad, el sistema no confirma la combinación como válida. |
| Actores Secundarios: | CU-05 Asignar Cita Automáticamente, CU-07 Registrar Cita Confirmada y CU-10 Reprogramar Cita utilizan esta validación. |
| Descripción Breve (Qué/Para qué): | Verifica la disponibilidad del médico, horario y consultorio antes de confirmar o modificar una cita, impidiendo solapamientos. Derivado de RF-06 y RU-07. |

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
| 2 | — | El sistema consulta el estado vigente del médico y horario. |
| 3 | — | El sistema consulta el estado vigente del consultorio. |
| 4 | — | El sistema verifica que no exista otra cita incompatible o solapada. |
| 5 | — | El sistema devuelve el resultado de disponibilidad al proceso que solicitó la validación. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El médico o el horario ya se encuentra ocupado. | El sistema marca la combinación como no disponible y evita su confirmación. |
| 3a | El consultorio ya se encuentra ocupado. | El sistema marca la combinación como no disponible y evita su confirmación. |
| 4a | Dos solicitudes intentan confirmar el mismo recurso y horario. | El sistema permite que únicamente una confirme el recurso; la otra debe continuar sin generar doble asignación. |

### Requisitos especiales (no funcionales específicos)

- **Consistencia:** Debe utilizarse la disponibilidad vigente, conforme RNF-03.

- **Integridad:** Debe impedirse la doble asignación en solicitudes concurrentes, conforme RNF-04.

- **Seguridad:** La validación debe formar parte únicamente de operaciones autorizadas.

### Variaciones de tecnología y datos

- El documento base no define límites de capacidad adicionales ni reglas específicas por especialidad; deberán documentarse si la CNS las establece.

<a id="cu-07"></a>

## CU-07 — Registrar cita confirmada

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-07 |
| Nombre del caso de uso: | Registrar Cita Confirmada |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-07: Registrar Cita Confirmada |
| Actor Principal (Quién): | Sistema / Personal de Enfermería-Admisión |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Existe una asignación de cita validada y se solicita su confirmación. |
| Precondiciones (Cuándo): | • La asignación de recursos fue validada como disponible.<br>• Se cuenta con paciente, especialidad, médico, fecha, hora y consultorio. |
| Postcondiciones (Éxito): | • La cita queda almacenada con paciente, especialidad, médico, fecha, hora, consultorio y estado inicial.<br>• La cita queda disponible para consulta y seguimiento. |
| Garantía Mínima (Fallo): | Si la asignación deja de ser válida antes de confirmar, la cita no se registra con recursos en conflicto. |
| Actores Secundarios: | CU-06 Validar Disponibilidad de Recursos y CU-16 Generar Notificación de Cita. |
| Descripción Breve (Qué/Para qué): | Registra de forma definitiva una cita previamente validada, asociando todos los datos necesarios para su seguimiento. Derivado de RF-07. |

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
| 2 | El personal confirma la cita cuando corresponda. | El sistema verifica que la asignación siga siendo válida antes de guardar. |
| 3 | — | El sistema registra paciente, especialidad, médico, fecha, hora, consultorio y estado inicial. |
| 4 | — | El sistema confirma el registro de la cita y la deja disponible para seguimiento. |
| 5 | — | El sistema genera la notificación correspondiente mediante CU-16. |

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

- El documento base contempla “programada” como estado de una cita; se utiliza como referencia para el estado inicial del registro.

<a id="cu-08"></a>

## CU-08 — Consultar cita paciente

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-08 |
| Nombre del caso de uso: | Consultar Citas del Paciente |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

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
| Descripción Breve (Qué/Para qué): | Permite al paciente consultar cuándo, dónde y con quién será atendido, mostrando únicamente las citas que le pertenecen. Derivado de RF-08 y RU-03. |

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
| 3 | El paciente revisa la lista de citas. | El sistema muestra las citas encontradas con médico, especialidad, fecha, hora, consultorio y estado. |
| 4 | El paciente selecciona una cita. | El sistema muestra el detalle permitido de la cita seleccionada. |

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
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-09: Consultar Citas Registradas |
| Actor Principal (Quién): | Personal de Enfermería / Admisión |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El personal autorizado necesita localizar una o más citas registradas. |
| Precondiciones (Cuándo): | • El usuario se encuentra autenticado y autorizado.<br>• Existen citas registradas o criterios de búsqueda válidos. |
| Postcondiciones (Éxito): | • Se presentan las citas coincidentes con el criterio seleccionado.<br>• La consulta no modifica los registros. |
| Garantía Mínima (Fallo): | Si no existen coincidencias, el sistema informa que no se encontraron citas sin alterar la información almacenada. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al personal autorizado consultar citas por paciente, médico, fecha, estado o consultorio para localizar rápidamente la información necesaria. Derivado de RF-09 y RU-06. |

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
| 1 | El personal abre la opción de consulta de citas. | El sistema muestra los criterios de búsqueda habilitados. |
| 2 | El personal selecciona un criterio: paciente, médico, fecha, estado o consultorio. | El sistema recibe el criterio y el valor de búsqueda. |
| 3 | El personal ejecuta la consulta. | El sistema busca las citas coincidentes. |
| 4 | El personal revisa los resultados. | El sistema presenta las citas que cumplen el criterio seleccionado. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen citas que coincidan con el criterio. | El sistema informa que no se encontraron resultados. |
| 2a | El criterio de búsqueda está incompleto o no es válido. | El sistema solicita corregir el criterio antes de ejecutar la consulta. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** Solo personal autorizado puede consultar citas mediante esta función.

- **Confidencialidad:** La información mostrada debe limitarse a la permitida para el rol, conforme RNF-02.

- **Usabilidad:** La interfaz debe mostrar solo los criterios y operaciones correspondientes al rol, conforme RNF-05.

### Variaciones de tecnología y datos

- Los criterios definidos en el documento base son paciente, médico, fecha, estado y consultorio.

<a id="cu-10"></a>

## CU-10 — Reprogramar cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-10 |
| Nombre del caso de uso: | Reprogramar Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-10: Reprogramar Cita |
| Actor Principal (Quién): | Personal de Enfermería / Admisión |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Existe una cita registrada que requiere modificación o reprogramación. |
| Precondiciones (Cuándo): | • El personal se encuentra autenticado y autorizado.<br>• La cita a reprogramar existe. |
| Postcondiciones (Éxito): | • La cita queda actualizada únicamente si la nueva combinación de recursos es válida.<br>• El cambio queda disponible para historial y notificación. |
| Garantía Mínima (Fallo): | Si no existe disponibilidad compatible, la cita no se actualiza con una combinación en conflicto. |
| Actores Secundarios: | CU-06 Validar Disponibilidad de Recursos, CU-16 Generar Notificación de Cita y CU-18 Consultar Historial de Cambios. |
| Descripción Breve (Qué/Para qué): | Permite al personal autorizado modificar o reprogramar una cita, validando nuevamente médico, horario y consultorio antes de guardar los cambios. Derivado de RF-10 y RU-06. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Enfermería / Admisión | Ajustar citas cuando sea necesario sin provocar conflictos de horario. |
| Paciente | Recibir información correcta sobre su cita reprogramada. |
| Médico | Mantener una agenda actualizada y sin solapamientos. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal localiza y selecciona la cita a reprogramar. | El sistema muestra los datos actuales de la cita. |
| 2 | El personal solicita la reprogramación. | El sistema habilito la nueva asignación de recursos. |
| 3 | El personal confirma la opción propuesta o seleccionada según el proceso autorizado. | El sistema valida nuevamente médico, horario y consultorio mediante CU-06. |
| 4 | El personal confirma el cambio. | El sistema guarda la nueva asignación y conserva el cambio para trazabilidad. |
| 5 | — | El sistema genera la notificación correspondiente al paciente mediante CU-16. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | La nueva combinación no está disponible. | El sistema no guarda los cambios y solicita utilizar otra disponibilidad. |
| 3b | Un recurso se ocupa de forma concurrente durante la reprogramación. | El sistema impide la doble asignación y obliga a validar otra combinación. |

### Requisitos especiales (no funcionales específicos)

- **Consistencia:** Debe validarse disponibilidad vigente, conforme RNF-03.

- **Integridad:** Debe impedirse la doble asignación en operaciones concurrentes, conforme RNF-04.

- **Confidencialidad:** Solo usuarios autorizados pueden modificar una cita.

### Variaciones de tecnología y datos

- El documento base no define si el paciente puede solicitar directamente una reprogramación; la función se asigna al personal de enfermería/admisión según RF-10 y su historia de usuario.

<a id="cu-11"></a>

## CU-11 — Cancelar cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-11 |
| Nombre del caso de uso: | Cancelar Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-11: Cancelar Cita |
| Actor Principal (Quién): | Personal de Enfermería / Admisión |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El personal autorizado solicita cancelar una cita activa. |
| Precondiciones (Cuándo): | • El personal se encuentra autenticado y autorizado.<br>• Existe una cita activa que puede ser gestionada. |
| Postcondiciones (Éxito): | • La cita queda con estado cancelado.<br>• El motivo de cancelación queda registrado.<br>• La operación queda disponible para historial y notificación. |
| Garantía Mínima (Fallo): | Si no se registra el motivo o la cita no puede ser cancelada, el sistema no confirma la cancelación. |
| Actores Secundarios: | CU-16 Generar Notificación de Cita y CU-18 Consultar Historial de Cambios. |
| Descripción Breve (Qué/Para qué): | Permite cancelar una cita registrada exigiendo el motivo de cancelación y conservando el registro de lo ocurrido. Derivado de RF-11 y RU-06. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Enfermería / Admisión | Cancelar citas de forma controlada y con motivo registrado. |
| Paciente | Conocer oportunamente la cancelación de una cita. |
| Administración | Mantener trazabilidad de cancelaciones para control y análisis. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El personal localiza y selecciona la cita activa. | El sistema muestra la información de la cita. |
| 2 | El personal solicita cancelar la cita. | El sistema exige registrar el motivo de cancelación. |
| 3 | El personal ingresa el motivo y confirma. | El sistema valida que exista un motivo. |
| 4 | — | El sistema cambia el estado de la cita a cancelada y conserva el motivo. |
| 5 | — | El sistema registra la operación para trazabilidad y genera la notificación correspondiente mediante CU-16. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | El personal intenta confirmar sin registrar un motivo. | El sistema no cancela la cita y solicita ingresar el motivo requerido. |
| 1a | La cita no existe o no está disponible como cita activa. | El sistema informa que la cancelación no puede ejecutarse sobre ese registro. |

### Requisitos especiales (no funcionales específicos)

- **Trazabilidad:** La cancelación debe conservar el motivo y la operación realizada.

- **Seguridad:** Solo personal autorizado puede cancelar citas.

- **Confidencialidad:** La información de la cita debe mantenerse protegida conforme al rol.

### Variaciones de tecnología y datos

- El documento base no define un catálogo de motivos de cancelación; puede modelarse posteriormente si la institución lo requiere.

<a id="cu-12"></a>

## CU-12 — Actualizar estado de cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-12 |
| Nombre del caso de uso: | Actualizar Estado de Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-12: Actualizar Estado de Cita |
| Actor Principal (Quién): | Médico / Personal de Enfermería autorizado |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Ocurre un evento de atención, cancelación u otra situación que requiere reflejar el estado actual de la cita. |
| Precondiciones (Cuándo): | • El usuario se encuentra autenticado y autorizado.<br>• La cita existe y puede ser actualizada por el rol correspondiente. |
| Postcondiciones (Éxito): | • La cita refleja el estado correspondiente al evento ocurrido.<br>• El cambio queda registrado para seguimiento. |
| Garantía Mínima (Fallo): | Si el usuario no está autorizado o el cambio no corresponde al estado de la cita, el sistema no aplica la actualización. |
| Actores Secundarios: | CU-18 Consultar Historial de Cambios. |
| Descripción Breve (Qué/Para qué): | Permite actualizar el estado de una cita para reflejar lo ocurrido durante el proceso de atención. Derivado de RF-12 y de su historia de usuario. |

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
| 1 | El usuario autorizado selecciona una cita. | El sistema muestra el estado actual de la cita. |
| 2 | El usuario selecciona el nuevo estado conforme a lo ocurrido. | El sistema valida que el usuario tenga permiso para actualizarlo. |
| 3 | El usuario confirma el cambio. | El sistema actualiza el estado de la cita. |
| 4 | — | El sistema conserva la operación para trazabilidad e informes posteriores. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | El usuario no posee permiso para actualizar el estado. | El sistema rechaza la operación y mantiene el estado existente. |
| 2b | El cambio solicitado no puede validarse para la situación actual. | El sistema no modifica la cita e informa que el estado no puede actualizarse con esa operación. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** La actualización debe depender del rol y permisos, conforme RNF-01.

- **Integridad:** La cita debe conservar un estado coherente y trazable.

- **Usabilidad:** Cada rol debe visualizar únicamente las acciones de estado que le correspondan, conforme RNF-05.

### Variaciones de tecnología y datos

- El RF-12 del documento base enumera los estados “programada, atendida, cancelada e inexistente”, mientras que la historia de usuario asociada menciona atención, cancelación o inasistencia. Esta diferencia debe validarse antes de aprobar la especificación definitiva.

<a id="cu-13"></a>

## CU-13 — Consultar agenda médica

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-13 |
| Nombre del caso de uso: | Consultar Agenda Médica |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 15/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-13: Consultar Agenda Médica |
| Actor Principal (Quién): | Médico |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El médico necesita revisar las citas que tiene programadas. |
| Precondiciones (Cuándo): | • El médico se encuentra autenticado.<br>• El sistema puede identificar las citas asignadas al médico. |
| Postcondiciones (Éxito): | • El médico visualiza su agenda diaria o semanal.<br>• La consulta no modifica las citas. |
| Garantía Mínima (Fallo): | Si no existen citas en el período seleccionado, el sistema informa que no hay citas programadas sin mostrar información de otros médicos. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al médico consultar su agenda diaria o semanal y conocer las citas que debe atender. Derivado de RF-13 y RU-08. |

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
| 1 | El médico abre la opción de agenda. | El sistema identifica al médico autenticado. |
| 2 | El médico selecciona vista diaria o semanal. | El sistema determina el período correspondiente. |
| 3 | — | El sistema consulta las citas asignadas al médico dentro del período. |
| 4 | El médico revisa la agenda. | El sistema muestra las citas comprendidas en la vista seleccionada. |

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

- **Usabilidad:** Debe disponer de vistas diaria y semanal, conforme RF-13.

- **Seguridad:** El acceso debe controlarse por rol, conforme RNF-01.

### Variaciones de tecnología y datos

- El documento base define únicamente las vistas diaria y semanal; otros períodos o filtros no están especificados.

<a id="cu-14"></a>

## CU-14 — Consultar información permitida del paciente

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-14 |
| Nombre del caso de uso: | Consultar Información Permitida del Paciente |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-14: Consultar Información Permitida del Paciente |
| Actor Principal (Quién): | Médico |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El médico necesita consultar información del paciente asociado a una cita que le fue asignada. |
| Precondiciones (Cuándo): | • El médico se encuentra autenticado.<br>• Existe una cita asignada al médico y asociada al paciente consultado. |
| Postcondiciones (Éxito): | • El médico visualiza únicamente la información básica y el historial permitido para su rol.<br>• La consulta no modifica los datos del paciente. |
| Garantía Mínima (Fallo): | Si el paciente no está asociado a una cita asignada al médico o la información no está permitida, el sistema impide la consulta. |
| Actores Secundarios: | No se identifican sistemas externos en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite al médico acceder a la información permitida del paciente correspondiente a una cita asignada, sin exponer datos que no correspondan a su rol. Derivado de RF-14 y RU-09. |

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
| 2 | El médico solicita consultar los datos del paciente. | El sistema identifica la información permitida para el rol. |
| 3 | — | El sistema recupera únicamente la información básica y el historial autorizado asociado al paciente. |
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

- El documento base no detalla qué campos forman la “información básica” ni qué parte del historial está permitida; esa definición debe validarse con la CNS.

<a id="cu-15"></a>

## CU-15 — Registrar datos de atención

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-15 |
| Nombre del caso de uso: | Registrar Datos de Atención |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-15: Registrar Datos de Atención |
| Actor Principal (Quién): | Médico / Personal de Enfermería autorizado |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Una cita llega al proceso de atención y se requiere registrar sus datos operativos. |
| Precondiciones (Cuándo): | • Existe una cita registrada.<br>• El usuario se encuentra autenticado y autorizado para registrar datos de atención. |
| Postcondiciones (Éxito): | • La asistencia del paciente queda asociada a la cita.<br>• Se registran el tiempo de espera y el tiempo de atención contemplados por el proyecto. |
| Garantía Mínima (Fallo): | Si la cita no existe o el usuario no está autorizado, el sistema no registra datos operativos sobre la cita. |
| Actores Secundarios: | CU-12 Actualizar Estado de Cita puede utilizarse para reflejar que la atención fue realizada. |
| Descripción Breve (Qué/Para qué): | Permite registrar la asistencia del paciente, el tiempo de espera y el tiempo de atención para conocer el comportamiento real del proceso de citas. Derivado de RF-15 y RU-10. |

### Modelo de contexto visual

_Apartado sin gráfico incorporado en el documento original._

### Partes interesadas y sus intereses

Identificación de quiénes se preocupan por este caso de uso y qué garantías exige el proceso de gestión de citas:

| Parte Interesada (Stakeholder) | Interés en este Caso de Uso (Garantía exigida) |
| --- | --- |
| Médico / Enfermería | Registrar correctamente los datos operativos de la atención. |
| Administración | Disponer de información para calcular tiempos e indicadores de gestión. |
| Paciente | Mantener datos de atención asociados a su cita correspondiente. |

### Escenario principal de éxito (camino feliz / Happy Path)

Secuencia paso a paso donde todas las validaciones son exitosas, en estilo de ping-pong (Acción del Actor frente a Reacción del Sistema).

| Paso | Acción del Actor | Respuesta del Sistema |
| --- | --- | --- |
| 1 | El usuario autorizado selecciona la cita que está siendo atendida. | El sistema muestra la información operativa disponible de la cita. |
| 2 | El usuario registra la asistencia del paciente. | El sistema asocia el dato de asistencia a la cita. |
| 3 | El usuario registra el tiempo de espera y el tiempo de atención. | El sistema valida y asocia los tiempos a la cita. |
| 4 | El usuario confirma el registro. | El sistema guarda los datos operativos de atención. |
| 5 | — | Cuando corresponde, el sistema permite actualizar el estado de la cita mediante CU-12. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 1a | La cita no existe o no corresponde a una atención gestionable por el usuario. | El sistema impide registrar datos de atención. |
| 3a | Los datos operativos requeridos no pueden validarse. | El sistema no confirma el registro hasta corregir la información. |

### Requisitos especiales (no funcionales específicos)

- **Integridad:** Los datos de atención deben quedar asociados a la cita correcta.

- **Confidencialidad:** La información del paciente debe mantenerse protegida conforme RNF-02.

- **Seguridad:** Solo roles autorizados pueden registrar datos operativos de atención.

### Variaciones de tecnología y datos

- El documento base no define el mecanismo de medición de los tiempos de espera y atención; se especifica únicamente que deben registrarse

<a id="cu-16"></a>

## CU-16 — Generar notificación

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-16 |
| Nombre del caso de uso: | Generar Notificación de Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-16: Generar Notificación de Cita |
| Actor Principal (Quién): | Sistema (proceso automático) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Una cita es registrada, modificada o cancelada y la operación queda confirmada. |
| Precondiciones (Cuándo): | • Existe una cita asociada a un paciente.<br>• La operación de registro, modificación o cancelación fue confirmada. |
| Postcondiciones (Éxito): | • El sistema genera la notificación correspondiente para el paciente. |
| Garantía Mínima (Fallo): | Si la notificación no puede generarse, la información confirmada de la cita no debe transformarse en una notificación contradictoria o incompleta. |
| Actores Secundarios: | El documento base no define un servicio o canal externo de notificaciones. |
| Descripción Breve (Qué/Para qué): | Genera información para el paciente cuando una cita se registra, modifica o cancela, permitiéndole conocer oportunamente su situación. Derivado de RF-16 y RU-04. |

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
| 1 | — | El sistema detecta que una cita fue registrada, modificada o cancelada. |
| 2 | — | El sistema identifica al paciente asociado y el tipo de operación confirmada. |
| 3 | — | El sistema genera el contenido de la notificación correspondiente a la situación de la cita. |
| 4 | El paciente recibe o consulta la información según el mecanismo que se defina. | El sistema deja disponible la notificación generada para el paciente. |

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

- El documento base no define el canal de notificación (aplicación, mensajería, correo u otro); la elección queda pendiente de diseño.

<a id="cu-17"></a>

## CU-17 — Generar recordatorio de cita

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-17 |
| Nombre del caso de uso: | Generar Recordatorio de Cita |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-17: Generar Recordatorio de Cita |
| Actor Principal (Quién): | Sistema (proceso automático) |
| Nivel de Abstracción: | Subfunction |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | Se aproxima la fecha de una cita programada conforme a la configuración establecida. |
| Precondiciones (Cuándo): | • Existe una cita programada.<br>• La cita continúa vigente antes del momento configurado para el recordatorio. |
| Postcondiciones (Éxito): | • El sistema genera el recordatorio correspondiente para el paciente. |
| Garantía Mínima (Fallo): | Si la cita deja de estar programada antes del momento del recordatorio, no se debe generar un recordatorio que contradiga su estado vigente. |
| Actores Secundarios: | El documento base no define un servicio o canal externo de recordatorios. |
| Descripción Breve (Qué/Para qué): | Genera un recordatorio previo a la fecha programada de una cita para reducir la posibilidad de olvido por parte del paciente. Derivado de RF-17 y RU-04. |

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
| 1 | — | El sistema identifica una cita programada cuya fecha se aproxima según la configuración. |
| 2 | — | El sistema verifica que la cita siga vigente y asociada al paciente. |
| 3 | — | El sistema genera el recordatorio con la información necesaria de la cita. |
| 4 | El paciente recibe o consulta el recordatorio según el mecanismo que se defina. | El sistema deja disponible el recordatorio generado. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 2a | La cita fue cancelada o modificada antes del momento del recordatorio. | El sistema no genera un recordatorio con información desactualizada; debe usar el estado vigente. |
| 1a | La cita aún no se encuentra dentro del período configurado para recordatorio. | El sistema no genera el recordatorio en ese momento. |

### Requisitos especiales (no funcionales específicos)

- **Consistencia:** El recordatorio debe basarse en los datos vigentes de la cita.

- **Confidencialidad:** Solo debe incluir información correspondiente al paciente y su cita.

- **Escalabilidad:** La generación de recordatorios debe mantener las reglas del proceso al aumentar el volumen de citas, conforme RNF-06.

### Variaciones de tecnología y datos

- El documento base no define con cuánta anticipación debe generarse el recordatorio ni el canal utilizado; ambos parámetros quedan pendientes de definición.

<a id="cu-18"></a>

## CU-18 — Consultar historial de cambios

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-18 |
| Nombre del caso de uso: | Consultar Historial de Cambios |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

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
| Actores Secundarios: | Las operaciones de modificación, reprogramación y cancelación alimentan el historial conservado por el sistema. |
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

- **Trazabilidad:** Las modificaciones, reprogramaciones y cancelaciones deben conservar la acción, usuario responsable y momento de ejecución.

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
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

### Ficha del caso de uso (¿quién?, ¿qué?, ¿cuándo?)

| Campo | Detalle |
| --- | --- |
| Nombre del Caso de Uso: | CU-19: Generar Reportes de Citas |
| Actor Principal (Quién): | Administrador |
| Nivel de Abstracción: | User Goal (Objetivo de usuario) |
| Alcance del Sistema: | SUD: Sistema de Gestión y Control de Citas Médicas |
| Evento Disparador (Trigger): | El administrador solicita un reporte para supervisar el funcionamiento del proceso de atención. |
| Precondiciones (Cuándo): | • El administrador se encuentra autenticado y autorizado.<br>• Existen datos de citas registrados para el criterio solicitado. |
| Postcondiciones (Éxito): | • El sistema presenta información agrupada según el reporte seleccionado.<br>• La generación del reporte no modifica los registros de citas. |
| Garantía Mínima (Fallo): | Si no existen datos para el criterio seleccionado, el sistema informa que no hay información para el reporte solicitado. |
| Actores Secundarios: | No se identifican herramientas externas de reportes en el documento base. |
| Descripción Breve (Qué/Para qué): | Permite generar reportes de citas programadas, atendidas y canceladas, así como reportes por médico, especialidad y consultorio. Derivado de RF-19 y RU-12. |

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
| 1 | El administrador accede al módulo de reportes. | El sistema muestra los tipos de reportes disponibles. |
| 2 | El administrador selecciona el reporte o criterio requerido. | El sistema recibe el tipo de reporte y los criterios aplicables. |
| 3 | El administrador solicita generar el reporte. | El sistema agrupa los datos de citas conforme al criterio seleccionado. |
| 4 | El administrador revisa el resultado. | El sistema presenta el reporte solicitado. |

### Diagrama de comportamiento UML

_Apartado sin gráfico incorporado en el documento original._

### Extensiones y flujos alternativos

Desviaciones del flujo principal debido a errores de validación, reglas del proceso o condiciones del entorno. Cada extensión referencia el paso donde se ramifica.

| Paso Ref. | Condición de Desvío | Acción de Recuperación / Salida |
| --- | --- | --- |
| 3a | No existen datos para el criterio seleccionado. | El sistema informa que no hay información disponible para generar resultados con ese criterio. |
| 1a | El usuario no posee permiso administrativo. | El sistema impide el acceso a la función de reportes. |

### Requisitos especiales (no funcionales específicos)

- **Seguridad:** La función debe restringirse a usuarios autorizados.

- **Confidencialidad:** Los reportes deben respetar la información permitida para administración.

- **Escalabilidad:** El crecimiento del volumen de citas no debe alterar las reglas fundamentales de consolidación, conforme RNF-06.

### Variaciones de tecnología y datos

- Los reportes definidos en el documento base incluyen citas programadas, atendidas, canceladas y agrupaciones por médico, especialidad y consultorio.

<a id="cu-20"></a>

## CU-20 — Consultar indicadores de gestión

### Identificación del caso de uso

| Campo | Detalle |
| --- | --- |
| Código de caso de uso: | CU-20 |
| Nombre del caso de uso: | Consultar Indicadores de Gestión |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Fecha de versión: | 08/10/2026 |
| Autor de artefacto: | Equipo del Proyecto CNS |
| Estado de aprobación: | Borrador |

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

---

## Observaciones para revisión del proyecto

- El documento fuente deja sin ilustraciones las secciones de contexto visual y diagramas de comportamiento UML; deberán incorporarse posteriormente si forman parte del entregable.
- En **CU-12**, el propio documento señala una diferencia entre los estados de cita de RF-12 y la referencia a inasistencia en su historia de usuario; la definición final debe validarse.
- En **CU-20**, el apartado 20.3 «Partes interesadas y sus intereses» contiene nuevamente la ficha del caso, no una tabla de stakeholders.
- El documento original no establece reglas detalladas de prioridad de asignación, parámetros de notificación o fórmulas de indicadores; se mantienen como pendientes de validación.

