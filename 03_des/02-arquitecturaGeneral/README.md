# Arquitectura del Sistema de Gestión y Control de Citas Médicas — CNS

Este documento presenta la organización de la arquitectura del **Sistema de Gestión y Control de Citas Médicas del Hospital Obrero de la Caja Nacional de Salud (CNS)**. Explica los componentes del sistema, su distribución mediante el patrón **Modelo-Vista-Controlador (MVC)**, los objetos de dominio y las reglas de negocio que deben cumplirse durante la gestión de citas médicas.

La arquitectura sirve como referencia para implementar el sistema de forma ordenada y mantener la relación entre **requisitos, casos de uso, historias de usuario, diseño y código**. También permite dividir el trabajo del equipo en tareas técnicas e issues de GitHub.

> **Estado:** documentación de arquitectura en elaboración — actualización del **20/10/2026**.
>
> **Organización:** cada tema de arquitectura tendrá su propio archivo Markdown. Los nombres de la tabla siguiente describen la **estructura prevista**; los enlaces funcionarán cuando esos archivos y carpetas se creen.

## Contenido

| Archivo o carpeta | Qué contiene | Lo consulta principalmente |
| --- | --- | --- |
| [01_vision_general.md](01_vision_general.md) | Objetivos técnicos, alcance, restricciones, componentes del sistema y comunicación entre ellos. | Todo el equipo |
| [02_mvc.md](02_mvc.md) | Aplicación del patrón MVC: responsabilidades de Vista, Controlador y Modelo; separación de capas y flujo de solicitudes. | Flutter, Java/Spring Boot |
| [03_modelo_dominio.md](03_modelo_dominio.md) | Entidades y objetos del negocio, relaciones, atributos, estados y responsabilidades. | Backend, base de datos |
| [04_reglas/](04_reglas/README.md) | Reglas de negocio organizadas por caso de uso: validaciones, condiciones, excepciones y resultados. | Todo el equipo, según cada CU |
| [05_contratos_api.md](05_contratos_api.md) | Contratos REST, solicitudes y respuestas, errores, autenticación e intercambio de eventos por WebSocket. | Flutter, Spring Boot |
| [06_base_de_datos.md](06_base_de_datos.md) | Diseño de persistencia en PostgreSQL, tablas, relaciones, restricciones e integridad de datos. | Backend, base de datos |
| [07_seguridad_privacidad.md](07_seguridad_privacidad.md) | Autenticación, autorización por roles, protección de datos personales y trazabilidad de operaciones. | Backend, todo el equipo |
| [08_decisiones_parametros.md](08_decisiones_parametros.md) | Decisiones técnicas, parámetros configurables y puntos que requieren validación institucional. | Todo el equipo |
| [diagramas/](diagramas/README.md) | Diagramas de arquitectura, componentes, despliegue y secuencia en PlantUML. | Todo el equipo |

Los **diagramas de casos de uso** se gestionan por separado en la carpeta [DiagCU](../DiagCU/), incluyendo el **CU-00 (diagrama general)** y los diagramas específicos de **CU-01 a CU-20**, si se conserva la estructura actual del repositorio.

La relación de los requisitos con los casos de uso y sus criterios se documenta en la [Matriz de Requisitos y Trazabilidad CNS](../../02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md).

## Propósito

- Definir la responsabilidad de cada componente y evitar mezclar interfaz, lógica de negocio y acceso a datos.
- Especificar las reglas que rigen el registro, solicitud, asignación, confirmación, modificación y cancelación de citas médicas.
- Establecer los objetos de dominio y las validaciones que les corresponden.
- Asegurar que la asignación de citas considere la disponibilidad de **especialidades, médicos, horarios y consultorios**.
- Mantener trazabilidad desde los requisitos hasta el diseño, la implementación y las pruebas.
- Dar al equipo una referencia común antes de crear issues de construcción y distribuir tareas entre las ramas `feature/*`.

## Alcance

La arquitectura toma como base los **20 casos de uso (CU-01 a CU-20)** definidos en la especificación de requisitos del proyecto y los requisitos funcionales **RF-01 a RF-20**. Considera los procesos siguientes:

- Inicio de sesión y gestión de usuarios, roles y permisos.
- Registro y consulta de pacientes.
- Solicitud de citas médicas según la especialidad o atención requerida.
- **Asignación automática** de médico, fecha, hora y consultorio según disponibilidad; el paciente no selecciona manualmente estos recursos.
- Validación de disponibilidad para evitar superposiciones y duplicaciones.
- Registro, confirmación, consulta, modificación, cancelación y reprogramación de citas.
- Gestión de agendas y seguimiento de la atención médica.
- Notificaciones, recordatorios, trazabilidad e indicadores de gestión.

**Regla importante en revisión:** al cancelar una cita debe liberarse el cupo correspondiente. La política exacta de reasignación automática —por ejemplo, a qué solicitud en espera se asignará, con qué prioridad y qué ocurre si la persona no acepta— se documentará en `04_reglas/` y `08_decisiones_parametros.md` antes de considerarse aprobada.

Las integraciones externas y funcionalidades no confirmadas en la Visión no se consideran decisiones definitivas de esta arquitectura.

## Arquitectura tecnológica

| Componente | Tecnología | Responsabilidad |
| --- | --- | --- |
| Aplicación móvil (Vista) | **Flutter / Dart** | Presentar pantallas, capturar datos, validar formularios y mostrar el estado de las citas. |
| Backend (Controladores y servicios) | **Java + Spring Boot** | Exponer endpoints, aplicar autenticación y autorización, coordinar los casos de uso y ejecutar reglas de negocio. |
| Comunicación cliente-servidor | **API REST** | Intercambiar información entre la aplicación móvil y el backend. |
| Actualizaciones en tiempo real | **WebSocket** | Comunicar cambios relevantes del estado de las citas cuando el diseño del proceso lo requiera. |
| Persistencia | **PostgreSQL** | Almacenar pacientes, usuarios, citas, disponibilidad, estados y registros de auditoría. |
| Modelado | **MVC** | Separar presentación, coordinación de solicitudes y lógica/datos del dominio. |

**Flujo general:** `Flutter (Vista) → API REST / WebSocket → Spring Boot (Controlador) → Servicios y Modelo de dominio → PostgreSQL`.

Las reglas de asignación, disponibilidad y cancelación **no deben depender solamente de validaciones de la aplicación móvil**: se aplicarán en el backend para conservar la consistencia de la información.

## Reglas generales del negocio

Las reglas detalladas se organizarán en `04_reglas/` y se identificarán por caso de uso. Entre las condiciones fundamentales están:

1. Registrar una solicitud no significa que exista una cita confirmada; la confirmación depende de una asignación válida.
2. Para asignar una cita, deben comprobarse la especialidad solicitada y la disponibilidad real del médico, horario y consultorio.
3. Un médico y un consultorio no pueden quedar reservados simultáneamente en citas incompatibles.
4. La cancelación debe actualizar el estado de la cita, liberar los recursos asociados y conservar la trazabilidad de la operación.
5. La reasignación de cupos liberados debe seguir una política explícita y verificable, sin asignaciones duplicadas.
6. Cada operación debe respetar los permisos del rol correspondiente y registrar la información necesaria para su seguimiento.

Las condiciones específicas, prioridades de atención, tiempos límite, estados definitivos y manejo de excepciones deberán contrastarse con los casos de uso y validarse cuando corresponda.

## Referencias

| Documento | Ubicación | Aporte a la arquitectura |
| --- | --- | --- |
| Documento de Visión CNS | [Documento_de_Vision_CNS.md](../../02_req/archivos-md/Documento_de_Vision_CNS.md) | Alcance, actores, objetivos y restricciones. |
| Especificación de Casos de Uso CNS | [Especificacion_de_Casos_de_Uso_CNS.md](../../02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md) | Flujos principales, alternativas, precondiciones y postcondiciones de CU-01 a CU-20. |
| Matriz de Requisitos y Trazabilidad | [Matriz_de_Requisitos_y_Trazabilidad_CNS.md](../../02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md) | Relaciones entre requisitos, casos de uso y criterios de aceptación. |
| Historias de Usuario CNS | Por incorporar o vincular según su ubicación en `02_req/`. | Necesidades de los actores y criterios de aceptación. |
| Modelo del Negocio CNS | Documento base del proyecto. | Procesos de atención, actores y problema de gestión de citas. |
| Diagramas de Casos de Uso | [DiagCU](../DiagCU/) | Vista general CU-00 y diagramas individuales de casos de uso. |

Cuando exista una contradicción entre documentos, debe registrarse la diferencia como issue y resolverse tomando como referencia la versión aprobada más reciente; no se modifica una regla de negocio sin dejar constancia del cambio.

## Definiciones

| Término | Significado en este documento |
| --- | --- |
| **Vista** | Interfaz con la que interactúan pacientes y personal autorizado; se implementa principalmente con Flutter. |
| **Controlador** | Componente que recibe solicitudes, valida entradas y permisos y coordina los servicios correspondientes. |
| **Modelo** | Objetos y reglas del dominio que representan pacientes, citas, agendas y recursos médicos. |
| **Objeto de dominio** | Concepto del negocio que contiene información y participa en reglas del proceso (por ejemplo, Cita Médica o Agenda Médica). |
| **Regla de negocio** | Condición que siempre debe cumplirse en la gestión de citas, independientemente de la pantalla que origine la operación. |
| **Agenda médica** | Organización de franjas horarias y disponibilidad de atención de los profesionales. |
| **Cupo disponible** | Espacio de atención que puede asignarse a una solicitud sin generar conflicto de recursos. |
| **Asignación automática** | Proceso por el que el sistema selecciona recursos compatibles para una solicitud de cita. |
| **Trazabilidad** | Registro que permite conocer cambios, responsables y momentos de las operaciones. |
| **API REST** | Interfaz de comunicación entre Flutter y el backend mediante peticiones HTTP. |
| **WebSocket** | Canal de comunicación para informar cambios en tiempo real cuando sea necesario. |

## Cómo leer los códigos

| Código | Significado | Referencia |
| --- | --- | --- |
| `RF-04` | Requisito funcional número 4, relacionado con la solicitud de cita médica. | Matriz de requisitos y casos de uso. |
| `CU-04` | Caso de uso 4: **Solicitar Cita Médica**. | Especificación de casos de uso. |
| `CU-05` | Caso de uso 5: **Asignar Cita Automáticamente**. | Especificación de casos de uso. |
| `RN-04.1` | **Ejemplo de codificación:** primera regla de negocio vinculada al CU-04. | Archivo correspondiente en `04_reglas/`, una vez creado. |
| `D-01` | **Ejemplo de codificación:** primera decisión registrada de arquitectura. | `08_decisiones_parametros.md`, una vez creado. |
| `P-01` | **Ejemplo de codificación:** primer parámetro configurable documentado. | `08_decisiones_parametros.md`, una vez creado. |
| `HU-XX / CA-X` | Identificador de historia de usuario y de su criterio de aceptación, conforme a la nomenclatura aprobada. | Documento de historias de usuario. |

> Los códigos `RN`, `D` y `P` son una **convención propuesta**, no un inventario de reglas o decisiones ya aprobadas.

## Cómo mantener esta carpeta

- Crear cada archivo conforme se complete el análisis, manteniendo los nombres de la tabla de contenido o actualizando sus enlaces si se acuerdan otros.
- Registrar las reglas junto al caso de uso que las origina e indicar sus excepciones.
- Identificar como **pendiente de validación** cualquier política que el equipo todavía no haya confirmado con el Hospital Obrero CNS.
- Mantener los diagramas `.puml` como fuente editable y actualizar sus imágenes cuando cambie el diseño.
- Trabajar los cambios en una rama `feature/*`, abrir un pull request y fusionar a `develop` después de la revisión correspondiente.

**Objetivo final:** disponer de una arquitectura consistente, verificable y preparada para guiar la implementación del sistema CNS.
