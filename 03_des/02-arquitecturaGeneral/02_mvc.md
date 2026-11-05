# ARQ-02 — Responsabilidades de MVC

| Sección | Contenido propio | NO debe realizar |
|---|---|---|
| **Vista Flutter** | Formularios, pantallas, mensajes de error, presentación de agenda/citas/notificaciones, accesibilidad | Elegir o bloquear recurso médico, saltarse permisos, declarar que una reserva está confirmada antes de respuesta REST |
| **Controlador/estado Flutter** | Captura eventos de interacción, validación de formato, llamadas al cliente HTTP, gestión de estado visual | Reglas definitivas del negocio, escritura directa a BD, confirmación por WebSocket |
| **Controlador REST Spring** | Autenticación de solicitud, autorización, validación estructural, llamada a servicio, DTOs y códigos HTTP | Consultas SQL dispersas o lógica de asignación incrustada en controller |
| **Modelo/servicios Java** | Solicitud, validación, agenda, asignación, cancelación, atención, notificación, auditoría | Renderizar interfaz o depender de widgets Flutter |
| **Persistencia JPA/PostgreSQL** | Repositorios, claves, transacciones, restricciones y concurrencia | Reglas clínicas no validadas |
| **Eventos/WS** | Distribución de cambios confirmados | Confirmar o reservar citas, sustituir REST |

**Decisión de diseño propuesta:** un backend monolítico modular por dominio en Spring Boot permite transacciones consistentes; separar paquetes y contratos internos sin introducir microservicios no solicitados. Propuesta, no decisión institucional.

Módulos lógicos: `identidad`, `recursos`, `citas`, `operaciones`, `comunicacion` y servicios transversales `seguridad`, `auditoria` y `reportes`. El paquete Java definitivo requiere aceptación del equipo.
