INV-006: INVESTIGACIÓN Y PROPUESTA DE COMUNICACIÓN EN TIEMPO REAL

Sistema de Gestión y Control de Citas Médicas — Hospital Obrero (CNS)

## 1. Objetivo

Investigar WebSocket y evaluar su uso junto con Spring Boot y Flutter para comunicar cambios relevantes sobre la disponibilidad y el estado de las citas médicas sin depender de consultas manuales constantes. Este documento es una propuesta técnica; no implica implementar la solución.

## 2. ¿Qué es WebSocket?

WebSocket es un protocolo que permite establecer una conexión persistente y bidireccional entre un cliente (Flutter) y un servidor (Spring Boot). Una vez establecida, cualquiera de los extremos puede enviar mensajes sin iniciar una nueva petición HTTP para cada actualización. Es útil para notificaciones y datos que cambian con frecuencia.

WebSocket transporta mensajes, pero no define por sí solo el significado del contenido. Por eso, la aplicación debe acordar el formato de los mensajes y puede utilizar un protocolo de mensajería como STOMP.

## 3. WebSocket frente a REST API

Recomendación: mantener REST como mecanismo principal para consultar y modificar citas, y añadir WebSocket como canal de notificaciones. WebSocket no debe reemplazar la API REST ni ser la única fuente de verdad del estado de las citas.

## 4. WebSocket y STOMP en Spring Boot

Spring Boot ofrece integración WebSocket mediante spring-boot-starter-websocket. En una aplicación Spring MVC, una opción común es habilitar STOMP sobre WebSocket y configurar un endpoint, destinos de aplicación y destinos de suscripción.

- Configuración WebSocket: registra el endpoint de conexión, por ejemplo /ws-citas.

- Broker de mensajes: para una primera versión puede evaluarse el broker simple de Spring; si la carga crece, valorar un broker externo.

- Servicio de citas: aplica reglas de negocio y confirma los cambios en la base de datos.

- Publicador de eventos: después de confirmar una operación, publica el evento correspondiente a los clientes suscritos.

- Control de acceso: restringe las suscripciones y mensajes según la identidad y los permisos del usuario.

STOMP (Simple Text Oriented Messaging Protocol) es un protocolo de mensajería que funciona sobre WebSocket. Define comandos como CONNECT, SUBSCRIBE y SEND, y utiliza destinos para publicar o recibir mensajes. Facilita organizar la comunicación sin diseñar un protocolo propio desde cero.

Destinos ilustrativos: /topic/citas/disponibilidad para cambios generales de disponibilidad y /user/queue/citas para notificaciones privadas. Son propuestas que el equipo debe confirmar.

## 5. Recepción de eventos en Flutter

Flutter puede conectarse mediante el paquete web_socket_channel. Si el backend usa STOMP, se recomienda evaluar stomp_dart_client, que permite conectarse, suscribirse a destinos, recibir mensajes y configurar opciones de reconexión.

1. Abrir una conexión segura mediante wss:// en el entorno desplegado.

1. Esperar la confirmación de conexión antes de suscribirse.

1. Suscribirse únicamente a destinos autorizados para el usuario.

1. Validar el formato del mensaje y actualizar la interfaz o mostrar una notificación.

1. Al cerrar sesión o terminar el servicio, cancelar las suscripciones y cerrar la conexión.

Los eventos no sustituyen la autorización. El servidor debe validar identidad y permisos en la conexión, las suscripciones y las operaciones. Los mensajes deben incluir solo los datos necesarios, evitando información clínica sensible en temas compartidos.

## 6. Diagrama de comunicación propuesto

Flutter                         Spring Boot                         Base de datos
   |                                |                                      |
   |--- REST/HTTPS: consultar ------>|                                      |
   |<-- respuesta ------------------|                                      |
   |                                |--- validar y guardar ---------------->|
   |                                |<-- operación confirmada -------------|
   |<==== evento por WSS/STOMP =====|                                      |
   |  actualizar pantalla           |                                      |

Ejemplo: se cancela una cita → el servidor valida y guarda la cancelación → publica CITA_CANCELADA y/o
DISPONIBILIDAD_ACTUALIZADA → clientes autorizados reciben el evento → Flutter actualiza la información visible.

## 7. Eventos propuestos

Los nombres y campos son una propuesta, no una definición existente del proyecto. Evitar publicar nombres completos, números de documento, diagnósticos u otra información clínica en temas generales.

## 8. Ejemplo ilustrativo de mensaje

{
  "eventId": "evt-uuid",
  "type": "CITA_CANCELADA",
  "occurredAt": "2026-10-16T10:30:00-04:00",
  "appointmentId": "cita-123",
  "status": "CANCELADA",
  "version": 4
}

Ejemplo conceptual. Los identificadores, formato de fecha, versionado y campos definitivos deben ajustarse al modelo de datos y a las reglas del equipo.

## 9. Desconexiones y reconexión

La conexión puede interrumpirse por pérdida de internet, cambio de red, suspensión de la aplicación o reinicio del servidor. WebSocket no garantiza que un cliente desconectado reciba todos los eventos que ocurrieron durante la interrupción.

- Detectar errores y cierre de conexión y mostrar un estado como “Reconectando”.

- Aplicar reconexión automática con espera incremental (backoff exponencial) y una pequeña variación aleatoria.

- Restablecer las suscripciones después de reconectar.

- Consultar de nuevo por REST las citas o disponibilidad para recuperar el estado correcto.

- Incluir un identificador único del evento y, si hace falta detectar huecos u ordenar cambios, una versión o secuencia.

- Hacer idempotente el procesamiento: recibir dos veces el mismo evento no debe duplicar acciones.

- Definir límites de reintento y cerrar conexiones al cerrar sesión.

- Utilizar heartbeat o detección de conexión inactiva cuando lo soporte la solución elegida.

La asignación de una cita debe validarse en el backend con controles de concurrencia. La interfaz no debe confiar en que recibió a tiempo una actualización para evitar reservas duplicadas.

## 10. Seguridad y privacidad

- Usar WSS en producción; no enviar credenciales ni datos de pacientes por conexiones sin cifrar.

- Validar autenticación y autorización en la conexión, suscripciones y mensajes.

- Impedir que un paciente se suscriba a eventos de otro paciente.

- Enviar solo los campos imprescindibles y evitar información clínica sensible en canales generales.

- No registrar tokens ni datos personales innecesarios en logs.

- Validar contenido y establecer límites de tamaño y frecuencia de mensajes.

## 11. Evaluación y recomendación

Se recomienda una arquitectura híbrida: REST para operaciones y consultas autoritativas, y WebSocket con STOMP para notificaciones en tiempo real. Así, la interfaz puede reflejar cambios con menor demora y reducir consultas repetitivas, mientras REST permite recuperar el estado vigente tras una desconexión.

Antes de implementar, el equipo debe validar:

- Volumen esperado de usuarios simultáneos y capacidad del servidor.

- Autenticación y autorización ya existentes.

- Destinos, permisos y contrato definitivo de eventos.

- Necesidad de un broker externo si se ejecutan varias instancias del servidor.

- Política de sincronización REST y tratamiento de eventos duplicados o perdidos.

- Comportamiento en segundo plano: WebSocket no necesariamente reemplaza las notificaciones push del sistema operativo.

## 12. Conclusión

WebSocket es apropiado para informar en tiempo real de cambios de disponibilidad y del estado de las citas. Spring Boot puede publicar eventos mediante STOMP sobre WebSocket y Flutter puede suscribirse con un cliente STOMP compatible. REST debe mantenerse para consultar el estado vigente y ejecutar las operaciones de negocio. La reconexión debe acompañarse de resincronización, porque no se debe asumir que todos los eventos se reciben durante una interrupción. La propuesta requiere validación del equipo antes de implementarse.

## 13. Referencias técnicas

- Spring Boot — WebSockets: https://docs.spring.io/spring-boot/reference/messaging/websockets.html

- Spring Framework — STOMP sobre WebSocket: https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html

- Spring Framework — descripción de STOMP: https://docs.spring.io/spring-framework/reference/web/websocket/stomp/overview.html

- Flutter — comunicación mediante WebSockets: https://docs.flutter.dev/cookbook/networking/web-sockets

- Dart package — web_socket_channel: https://pub.dev/packages/web_socket_channel

- Dart package — stomp_dart_client: https://pub.dev/packages/stomp_dart_client

## 14. Alcance

Este entregable es de investigación y propuesta. No se implementó código ni se modificó la arquitectura del sistema. Los destinos, eventos y payloads son sugerencias que deben validarse antes de considerarse contrato oficial.

| Aspecto | REST API (HTTP) | WebSocket |

| --- | --- | --- |

| Comunicación | Petición del cliente y respuesta del servidor. | Conexión persistente y bidireccional. |

| Actualizaciones | El cliente vuelve a consultar o utiliza otro mecanismo. | El servidor puede enviar un evento al cliente conectado cuando ocurre un cambio. |

| Uso recomendado | Consultar, crear, modificar o cancelar citas. | Avisar de cambios de disponibilidad, asignaciones y cancelaciones. |

| Complejidad | Más sencillo para operaciones puntuales. | Requiere gestionar conexión, suscripciones y reconexión. |

| Recuperación | El cliente vuelve a consultar el recurso. | Después de desconectarse, debe sincronizarse mediante REST para recuperar el estado vigente. |

| Evento | Cuándo se emite | Destinatario propuesto | Datos mínimos sugeridos |

| --- | --- | --- | --- |

| DISPONIBILIDAD_ACTUALIZADA | Cuando una asignación, cancelación o reprogramación cambia los horarios disponibles. | Usuarios autorizados que consultan esa agenda. | eventId, agenda/profesional, fecha de actualización, versión. |

| CITA_ASIGNADA | Después de confirmar y guardar una asignación. | Paciente afectado y personal autorizado. | eventId, citaId, estado, fecha/hora de la cita. |

| CITA_CANCELADA | Después de confirmar una cancelación. | Paciente afectado y personal autorizado. | eventId, citaId, estado, fecha de actualización. |

| CITA_REPROGRAMADA | Después de confirmar un cambio de fecha u hora. | Paciente afectado y personal autorizado. | eventId, citaId, nueva fecha/hora, estado. |

| CITA_ESTADO_ACTUALIZADO | Cuando cambia un estado relevante de la cita. | Solo usuarios que necesitan conocerlo. | eventId, citaId, estado nuevo, fecha de actualización. |
