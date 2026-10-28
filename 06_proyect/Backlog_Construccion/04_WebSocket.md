# Backlog de Construcción — WebSocket | CNS Hospital Obrero

[Índice general](README.md) · [Orden de creación](00_Indice_de_subida.md) · [Trazabilidad](08_Cobertura_y_trazabilidad.md) · [Flujos](09_Flujos_y_dependencias.md) · [Pruebas](12_Casos_de_prueba.md)

**Total de issues propuestos:** 1. **Código:** pendiente de crear en GitHub; son especificaciones del backlog y no evidencias de implementación.
**Responsables propuestos:** Matías. Los responsables y puntos se revisan en [07_Estimacion.md](07_Estimacion.md).

Este archivo toma el comportamiento oficial de [CU-01 a CU-20](13_Comportamiento_CU.md) y la [matriz de requisitos](08_Cobertura_y_trazabilidad.md). No sustituye la aprobación de arquitectura del Sprint 2.

## Distribución por Sprint

| Sprint | Issues |
|---|---|
| S5 (30/11–11/12/2026) | [CON-22](04_WebSocket.md#con-22) |

---

<a id="con-22"></a>
### [CON-22][WebSocket] Emitir eventos de citas con WebSocket desde Spring Boot

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Matías. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-22` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-13](02_Java.md#con-13) · [CON-16](02_Java.md#con-16)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:websocket`  
**Rama de trabajo (base `develop`):** `feature/con-22-emitir-eventos-de-citas-con-websocket-desde-spri` (crear solo cuando se abra el issue).

**Descripción:** Emitir eventos de citas con WebSocket desde Spring Boot. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.
**RNF aplicables:** RNF-01, RNF-02, RNF-03.

**Criterios de aceptación**
- [ ] Spring Boot publica por WebSocket cambios de cita o disponibilidad únicamente después de que la transacción REST quede confirmada.
- [ ] Clientes con sesión no válida o rol no autorizado no acceden a eventos de otro paciente o médico (RNF-01/RNF-02).
- [ ] Los mensajes y destinos siguen el contrato ELA-05; no se difunden datos clínicos ni personales innecesarios.
- [ ] Una falla WebSocket no revierte una cita ya confirmada ni genera reservas duplicadas; los clientes pueden reconectar y consultar REST.
- [ ] La emisión no depende de CON-21 si la reasignación de cupos no ha sido aprobada.

**Tareas**
- [ ] Implementar canal de eventos autorizado y publicación tras commit.
- [ ] Añadir pruebas de entrega, desconexión/reconexión y aislamiento entre roles.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-22A](12_Casos_de_prueba.md#cp-cns-22a) — Confirmar cita emite actualización a cliente autorizado, no a otro paciente.
- [ ] [CP-CNS-22B](12_Casos_de_prueba.md#cp-cns-22b) — Cortar WebSocket y completar cancelación por REST conserva el cambio; reconexión refresca datos.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.
