# Documento de Visión — Sistema de Gestión y Control de Citas Médicas

**Plantilla de requisitos de alto nivel — Estándar RUP**

## Datos del proyecto

| Campo | Detalle |
| --- | --- |
| Proyecto: | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero de la Caja Nacional de Salud (CNS), Tarija |
| Versión de Visión: | 0.1 - Borrador Inicial |
| Fecha de Creación: | 07/10/2026 |
| Autor(es): | Equipo del Proyecto CNS |
| Organización: | Universidad Católica Boliviana "San Pablo" - Unidad Académica Regional Tarija |

## Tabla de contenidos

- [1. Introducción](#seccion-1)
- [2. Posicionamiento](#seccion-2)
- [3. Descripción de stakeholders y usuarios](#seccion-3)
- [4. Descripción global del producto](#seccion-4)
- [5. Restricciones](#seccion-5)
- [6. Otros requisitos del producto (no funcionales)](#seccion-6)
- [7. Requisitos de documentación y empaque](#seccion-7)

## Contenido del documento

## Control de revisiones

| Fecha | Versión | Descripción de Modificación | Autor |
| --- | --- | --- | --- |
| 07/10/2026 | 0.1 | Elaboración preliminar del Documento de Visión: planteamiento del problema, alcance, stakeholders, arquitectura de alto nivel y características principales. | Equipo del Proyecto CNS |

<a id="seccion-1"></a>

## 1. Introducción

Esta introducción proporciona una visión general del Documento de Visión del Sistema de Gestión y Control de Citas Médicas para el Hospital Obrero de la Caja Nacional de Salud (CNS) en Tarija. Establece el propósito, el alcance inicial, los términos principales y las referencias utilizadas para definir la solución a un nivel de visión.

### 1.1 Propósito

Este documento tiene como finalidad definir la visión de alto nivel del Sistema de Gestión y Control de Citas Médicas del Hospital Obrero de la CNS. Describe el problema que se busca resolver, los usuarios y stakeholders involucrados, las capacidades principales del producto y las restricciones tecnológicas inicialmente acordadas. Servirá como base para la posterior especificación de requisitos, casos de uso, historias de usuario, arquitectura y diseño del sistema.

### 1.2 Alcance

El sistema permitirá digitalizar y centralizar el proceso de solicitud, asignación, consulta y control de citas médicas del Hospital Obrero de la CNS. En esta primera versión de la visión se considera el siguiente alcance:

- Registro y validación de pacientes/asegurados para la gestión de citas médicas.

- Solicitud de cita por especialidad y asignación automática de fecha, hora y profesional según disponibilidad y reglas definidas por la institución.

- Gestión de disponibilidad, agendas, modificaciones, cancelaciones y reprogramaciones, con actualización de información en tiempo real.

- Consulta de citas, notificaciones al usuario y generación de reportes administrativos básicos para el control del servicio.

Quedan fuera del alcance de esta versión:

- Historia clínica avanzada y gestión integral del expediente médico.

- Módulos de laboratorio, farmacia, facturación y otros procesos clínico-administrativos ajenos a la gestión de citas.

- Tecnologías de base de datos, infraestructura definitiva, servicios externos de mensajería e integraciones con otros sistemas institucionales, que serán definidas en iteraciones posteriores.

### 1.3 Definiciones, Acrónimos y Abreviaciones

Esta sección define los términos del negocio y los conceptos técnicos mínimos necesarios para interpretar el Documento de Visión de manera uniforme.

| Término / Acrónimo | Definición |
| --- | --- |
| RUP | Rational Unified Process. Enfoque de desarrollo iterativo que utiliza artefactos como el Documento de Visión para definir el producto a alto nivel. |
| CNS | Caja Nacional de Salud. |
| SUD | System Under Design. Sistema de software que se encuentra bajo análisis y diseño. |
| Flutter | Framework utilizado para desarrollar la aplicación móvil multiplataforma del sistema. |
| Java | Lenguaje de programación principal definido para el backend del sistema. |
| Spring Boot | Framework de Java utilizado para construir el backend y exponer los servicios del sistema. |
| API | Application Programming Interface. Interfaz que permite la comunicación entre la aplicación cliente y los servicios del backend. |
| REST API | Interfaz de servicios basada en REST utilizada para registrar, consultar y actualizar información del sistema. |
| WebSocket | Mecanismo de comunicación bidireccional persistente utilizado para actualizaciones en tiempo real. |
| Cita médica | Asignación programada de atención entre un paciente/asegurado y un profesional médico, asociada a una especialidad, fecha y horario. |

### 1.4 Referencias

Las referencias corresponden a la documentación previa del proyecto de citas médicas, al modelo de Documento de Visión RUP utilizado como guía y al ejemplo académico de ChuraRuta empleado como estructura de referencia.

| Título del Documento | Fecha | Organización / Fuente |
| --- | --- | --- |
| Sistema de Control y Registro de Citas del Hospital Obrero CNS - Documento de requerimientos y modelado | 2026 | Universidad Católica Boliviana "San Pablo" - Tarija |
| Sistema de Gestión de Citas Médicas - Marco práctico y análisis para soporte a decisiones | 2026 | Universidad Católica Boliviana "San Pablo" - Tarija |
| Documento de visión - Esquema de referencia RUP/IBM | 28/11/2025 | IBM Engineering Lifecycle Management |
| ChuraRuta - Documento de Visión (RUP), versión 0.1 | 02/09/2026 | Plantilla académica de referencia |

<a id="seccion-2"></a>

## 2. Posicionamiento

### 2.1 Oportunidad de Negocio

La oportunidad de mejora institucional surge de la necesidad de modernizar la gestión de citas médicas del Hospital Obrero de la CNS. La gestión manual o no integrada de pacientes, agendas y citas dificulta la organización, puede generar duplicaciones o conflictos de horario y limita la disponibilidad de información actualizada para pacientes y personal hospitalario. Digitalizar el proceso permitirá centralizar la información, reducir tareas repetitivas y utilizar de mejor manera los horarios disponibles.

El sistema busca mejorar la experiencia del asegurado y apoyar al personal de admisión, enfermería y medicina mediante una asignación de citas basada en disponibilidad real. El paciente no seleccionará libremente un horario; realizará la solicitud correspondiente y el sistema asignará una opción válida de acuerdo con la especialidad, la agenda médica, los turnos y las reglas institucionales definidas.

### 2.2 Sentencia del Problema (Problem Statement)

Este marco estandariza la definición del problema principal que el proyecto busca resolver en el Hospital Obrero de la CNS:

| Campo | Detalle |
| --- | --- |
| El problema de... | la gestión manual o no integrada de citas médicas, la falta de información actualizada sobre disponibilidad y la posibilidad de conflictos o duplicaciones en la asignación de horarios dentro del Hospital Obrero de la CNS. |
| Afecta a... | los pacientes/asegurados que necesitan acceder oportunamente a una cita, al personal de admisión y enfermería que organiza el proceso, a los médicos que requieren una agenda clara y a la administración responsable de supervisar el servicio. |
| El impacto de lo cual es... | mayores tiempos de espera, necesidad de realizar gestiones presenciales o repetitivas, riesgo de errores de asignación, cancelaciones o reprogramaciones difíciles de coordinar y menor capacidad de control sobre la demanda y el uso de los horarios médicos. |
| Una solución exitosa sería... | un sistema móvil centralizado que permita solicitar citas, asignar automáticamente fecha y hora según especialidad y disponibilidad, actualizar agendas en tiempo real, gestionar cambios y cancelaciones, notificar al paciente y brindar información de control al personal autorizado. |

### 2.3 Sentencia de Posicionamiento de Producto

Esta declaración resume el valor que el sistema pretende aportar al Hospital Obrero, a sus pacientes/asegurados y al personal que participa en la gestión de citas:

| Campo | Detalle |
| --- | --- |
| Para... | los pacientes/asegurados del Hospital Obrero de la CNS y el personal autorizado que participa en la gestión de citas médicas. |
| Quien... | necesita solicitar, asignar, consultar y controlar citas de manera organizada, reduciendo esperas, errores de horario y falta de información sobre disponibilidad. |
| El (nombre de producto)... | Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS |
| Es un (tipo de producto)... | sistema de información con aplicación móvil multiplataforma y servicios backend para la gestión centralizada de citas médicas. |
| Que... | permite registrar solicitudes, asignar automáticamente horarios disponibles, consultar agendas, comunicar cambios en tiempo real y mantener trazabilidad de las operaciones relacionadas con las citas. |
| A diferencia de... | la gestión manual o dispersa del proceso, la solución centraliza la información, aplica reglas uniformes de disponibilidad y mantiene actualizados a pacientes y personal hospitalario desde una única plataforma. |

<a id="seccion-3"></a>

## 3. Descripción de Stakeholders y Usuarios

Para definir una solución que responda al proceso real de atención, se identifican los principales stakeholders y usuarios que participan directa o indirectamente en la gestión de citas médicas del Hospital Obrero de la CNS.

### 3.1 Resumen de Stakeholders

Los stakeholders son las personas, áreas u organizaciones que se ven afectadas por el sistema, establecen reglas del proceso, participan en su operación o tienen responsabilidad sobre su implementación y resultados.

| Nombre del Rol | Descripción / Responsabilidad | Expectativa Clave |
| --- | --- | --- |
| Dirección / Administración del Hospital Obrero CNS | Define lineamientos institucionales, supervisa el servicio y utiliza información para evaluar la gestión de citas y el uso de recursos. | Mejorar la atención, reducir problemas operativos y disponer de información confiable para la toma de decisiones. |
| Área de Sistemas / TI | Responsable de apoyar la implementación, operación, mantenimiento y evolución técnica del sistema. | Contar con una solución mantenible, segura, documentada y compatible con las decisiones tecnológicas institucionales. |
| Admisión / Recepción y Enfermería | Participa directamente en el registro, control, modificación, cancelación y seguimiento de citas dentro del proceso operativo. | Reducir tareas manuales, evitar conflictos de horario y disponer de información actualizada. |
| Personal Médico | Atiende a los pacientes asignados y requiere conocer su agenda y la información básica necesaria para la atención programada. | Disponer de una agenda clara, actualizada y coherente con sus turnos y disponibilidad. |
| Pacientes / Asegurados | Solicitan atención médica y reciben la cita asignada por el sistema según disponibilidad y reglas institucionales. | Obtener una cita de forma más clara y oportuna, conocer su fecha/hora y recibir información sobre cambios. |

### 3.2 Resumen de Usuarios

Los usuarios son quienes interactuarán directamente con el sistema. Su identificación permite delimitar responsabilidades, permisos y necesidades de interacción dentro del proceso de citas médicas.

| Tipo de Usuario | Descripción de Actividades | Stakeholder Asociado |
| --- | --- | --- |
| Paciente / Asegurado | Solicita una cita por especialidad, consulta la cita asignada, recibe actualizaciones y puede solicitar cancelación o reprogramación cuando corresponda. No selecciona libremente el horario: el sistema lo asigna según disponibilidad. | Pacientes / Asegurados |
| Personal de Admisión / Enfermería | Registra o valida pacientes, gestiona citas y excepciones operativas, consulta disponibilidad, realiza modificaciones o cancelaciones autorizadas y da seguimiento al proceso. | Admisión / Recepción y Enfermería |
| Médico | Consulta su agenda diaria o semanal, visualiza pacientes asignados y actualiza los estados del proceso que le correspondan. | Personal Médico |
| Administrador del Sistema | Gestiona usuarios, roles, parámetros operativos, catálogos y configuraciones necesarias para mantener el sistema disponible. | Área de Sistemas / TI |

<a id="seccion-4"></a>

## 4. Descripción Global del Producto

### 4.1 Perspectiva del Producto

El Sistema de Gestión y Control de Citas Médicas será una solución cliente-servidor orientada a dispositivos móviles. Para esta versión del Documento de Visión se establecen únicamente las siguientes tecnologías base; la persistencia de datos, infraestructura definitiva, servicios externos y demás componentes serán definidos posteriormente:

- Frontend móvil multiplataforma desarrollado en Flutter para dispositivos Android/iOS.

- Backend desarrollado en Java utilizando Spring Boot para implementar la lógica de negocio, la gestión de citas, reglas de disponibilidad y servicios del sistema.

- REST API para las operaciones de consulta, registro, actualización y comunicación estándar entre la aplicación Flutter y el backend.

- WebSocket para comunicar cambios de estado, disponibilidad y actualizaciones relevantes en tiempo real sin requerir una actualización manual constante de la aplicación.

### 4.2 Resumen de Características

Mapeo de las capacidades funcionales de alto nivel del sistema con el beneficio institucional o de usuario que cada una busca proporcionar:

| Característica Clave | Descripción Detallada | Beneficio Institucional / de Usuario |
| --- | --- | --- |
| Solicitud y Asignación Automática de Citas | El paciente solicita una cita indicando la especialidad o servicio requerido. El sistema valida las reglas y asigna automáticamente un médico, fecha, hora y, cuando corresponda, consultorio disponible. | Reduce la asignación manual, evita choques de horario y permite aprovechar la disponibilidad real. |
| Gestión de Disponibilidad y Agenda en Tiempo Real | Mantiene actualizada la disponibilidad de médicos, turnos y citas. Los cambios relevantes se comunican a los clientes conectados mediante WebSocket. | Permite que pacientes y personal trabajen con información vigente y disminuye inconsistencias. |
| Registro y Gestión de Pacientes | Permite registrar, validar y consultar la información básica necesaria del paciente/asegurado para asociarla correctamente a sus citas. | Centraliza la información necesaria y reduce errores de identificación en el proceso de atención. |
| Modificación, Cancelación y Reprogramación | Permite gestionar cambios autorizados sobre una cita, conservando el motivo y la trazabilidad de la operación. | Facilita reorganizar la agenda sin perder control sobre lo ocurrido ni generar duplicaciones. |
| Notificaciones y Actualizaciones de Estado | Informa al usuario sobre la asignación de la cita y los cambios relevantes de fecha, hora o estado mediante los mecanismos que se definan para el proyecto; dentro de la aplicación, las actualizaciones podrán propagarse en tiempo real. | Reduce incertidumbre y mejora la comunicación entre el hospital y el paciente. |
| Gestión de Roles y Accesos | Diferencia funciones para paciente/asegurado, personal de admisión o enfermería, médico y administrador, limitando acciones según responsabilidades. | Protege la información y evita operaciones no autorizadas. |
| Reportes y Trazabilidad Administrativa | Permite consultar información consolidada de citas programadas, atendidas, canceladas o pendientes, además del historial de cambios asociado al proceso. | Apoya el control operativo, la auditoría y la toma de decisiones sobre demanda y uso de recursos. |

### 4.3 Suposiciones y Dependencias

El funcionamiento del sistema dependerá de que la información de médicos, especialidades, turnos y disponibilidad se mantenga actualizada; de que las reglas institucionales para asignar citas sean definidas y validadas por el Hospital Obrero; de que los usuarios autorizados dispongan de conectividad suficiente; y de que pacientes y personal utilicen correctamente el sistema. Las integraciones con otros sistemas o canales externos serán consideradas cuando la CNS defina su disponibilidad y condiciones de acceso.

### 4.4 Costo y Precio

En esta versión no se establece un costo definitivo de implementación. La estimación dependerá de la infraestructura de despliegue, capacidad requerida, servicios de comunicación, soporte, mantenimiento e integraciones que la CNS decida incorporar.

El sistema se plantea como una solución institucional y no como un producto comercial de suscripción. Los costos y recursos necesarios deberán definirse en una etapa posterior cuando se consoliden la arquitectura, el volumen de usuarios y el entorno de operación.

<a id="seccion-5"></a>

## 5. Restricciones

Las siguientes restricciones delimitan el diseño inicial del sistema y deben mantenerse mientras no exista una decisión formal de actualización tecnológica o funcional:

- **Restricción de Plataforma:** El frontend debe desarrollarse en Flutter; el backend debe desarrollarse en Java con Spring Boot; la comunicación cliente-servidor utilizará REST API y las actualizaciones en tiempo real utilizarán WebSocket.

- **Restricción de Idioma:** La interfaz de usuario y los mensajes principales del sistema deben estar disponibles en español.

- **Restricción Operativa y de Privacidad:** El sistema debe respetar las reglas institucionales de asignación de citas, proteger la confidencialidad de la información de pacientes y limitar el acceso según el rol autorizado.

<a id="seccion-6"></a>

## 6. Otros Requisitos del Producto (No Funcionales)

### 6.1 Requisitos de Sistema (Hardware/Software)

La aplicación cliente será desarrollada en Flutter para dispositivos móviles Android/iOS. El servidor de aplicaciones deberá soportar Java y Spring Boot. La comunicación funcional se realizará mediante REST API y WebSocket. La tecnología de base de datos, el proveedor de infraestructura y otros servicios complementarios quedan pendientes de definición para una siguiente versión del documento.

### 6.2 Requisitos de Desempeño (Rendimiento)

Como objetivo inicial, las operaciones comunes de consulta y gestión de citas deberán responder en un tiempo máximo aproximado de 2 segundos bajo condiciones normales de uso. Los cambios de disponibilidad, asignación o estado que requieran actualización inmediata deberán reflejarse mediante WebSocket sin que el usuario tenga que recargar manualmente la información. Los valores de concurrencia y capacidad serán definidos cuando se conozca el volumen real de usuarios.

### 6.3 Requisitos de Entorno y Seguridad

Seguridad de datos y comunicación:

- La comunicación entre la aplicación Flutter y el backend Spring Boot deberá realizarse mediante canales seguros, tanto para las operaciones REST como para la comunicación en tiempo real.

- La información personal y de citas de los pacientes deberá manejarse de forma confidencial y mostrarse únicamente a usuarios autorizados según sus funciones.

- El sistema deberá conservar la integridad de los datos y evitar registros duplicados o asignaciones incompatibles de médico, horario o consultorio.

Autenticación y autorización:

- El acceso deberá estar controlado por cuentas y roles diferenciados para paciente/asegurado, personal de admisión o enfermería, médico y administrador del sistema. El mecanismo técnico específico de autenticación se definirá posteriormente.

Integridad y trazabilidad del proceso de citas:

- Las modificaciones, cancelaciones y reprogramaciones deberán conservar información suficiente para identificar el cambio realizado, su fecha y el usuario responsable.

Disponibilidad operativa:

- El sistema deberá estar disponible para la consulta y gestión de citas durante los periodos definidos por la institución, procurando continuidad del servicio y recuperación ante interrupciones.

- Las fallas de comunicación en tiempo real no deberán provocar pérdida o duplicación de citas; las operaciones confirmadas deberán mantener consistencia en el backend.

Cumplimiento y privacidad:

- El tratamiento de datos personales y médicos deberá respetar las políticas internas de la CNS y la normativa nacional aplicable en materia de confidencialidad y protección de información.

- Las funciones de auditoría y consulta de historial de cambios deberán limitarse a usuarios autorizados y utilizarse para control administrativo y trazabilidad.

<a id="seccion-7"></a>

## 7. Requisitos de Documentación y Empaque

### 7.1 Manuales y Ayuda

La solución deberá contar, al menos, con un Manual de Usuario para pacientes y personal operativo, una guía breve de administración y una Guía de Instalación/Despliegue que se completarán cuando la arquitectura técnica quede consolidada.

### 7.2 Etiquetado y Empaque

La aplicación y la documentación deberán utilizar una identificación visual coherente con el proyecto y, cuando corresponda, con la identidad institucional de la Caja Nacional de Salud, respetando las autorizaciones de uso de logotipos, nombres e iconografía.

---

> **Nota de fidelidad:** este documento conserva las decisiones de su versión 0.1 (07/10/2026). En esa versión, la base de datos, la infraestructura definitiva y los servicios externos de mensajería aún se declaran pendientes de definición.

