# Contexto del proyecto — CNS Hospital Obrero

**Propósito:** gestionar y controlar citas médicas del Hospital Obrero de la CNS.

**Actores documentados:** Paciente, Enfermería/Admisión, Médico y Administrador. También hay procesos automáticos del sistema.

**Alcance funcional documental:** 20 requisitos funcionales RF-01 a RF-20, 20 casos de uso CU-01 a CU-20: autenticación, roles, pacientes, solicitudes, asignación y disponibilidad, confirmación, consulta, reprogramación/cancelación, estado/agenda, datos permitidos y atención, notificaciones, recordatorios, historial, reportes e indicadores.

**Arquitectura seleccionada:** MVC; Flutter/Dart (Vista y estado de interfaz), Java/Spring Boot (controladores REST, modelo de dominio, servicios, lógica de asignación y reasignación), PostgreSQL (persistencia), REST API y WebSocket (actualizaciones). **No C++.**

**Etapas:** Sprint 1 documentación finalizado; Sprint 2 arquitectura vigente; construcción en Sprints 3 a 5; Sprint 6 validación/entrega propuesto. Modelo en V y seguimiento Kanban.

**Pendientes que no se deben inventar:** reglas exactas de prioridad al reasignar, esquema de autenticación específico, proveedor de notificaciones, plataforma concreta de servidor, umbrales de rendimiento y correspondencia HU exacta (ver documentos originales).

**Ramas:** `main` (entregas estables), `develop` (integración), `feature/...` para tareas. El trabajo entra a `develop` por Pull Request; mantener ambas ramas principales protegidas.
