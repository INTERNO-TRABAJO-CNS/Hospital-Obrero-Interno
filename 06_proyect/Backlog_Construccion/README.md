# Backlog de Construcción — Sistema CNS Hospital Obrero

**Versión de trabajo:** 23/10/2026 · **Fase:** Construcción posterior a arquitectura MVC del Sprint 2 · **Estado:** planificación revisada, Issues aún no creados/verificados.

Este backlog se elabora exclusivamente para el **Sistema de Gestión y Control de Citas Médicas del Hospital Obrero de la CNS (Tarija)**. La lógica de asignación de citas vive en **Java + Spring Boot**; Flutter implementa las vistas; PostgreSQL conserva la información; REST realiza las operaciones y WebSocket transmite actualizaciones tras confirmarse cambios. No hay C++, motor de rutas, simulador de tráfico ni servicio de WhatsApp heredado de ChuroViaje.

## Navegación entre los Markdown

| Archivo | Función | Issues |
|---|---|---:|
| [01_Flutter.md](01_Flutter.md) | Trabajo de Flutter; criterios, tareas y pruebas verificables | 9 |
| [02_Java.md](02_Java.md) | Trabajo de Java; criterios, tareas y pruebas verificables | 17 |
| [03_PostgreSQL.md](03_PostgreSQL.md) | Trabajo de PostgreSQL; criterios, tareas y pruebas verificables | 1 |
| [04_WebSocket.md](04_WebSocket.md) | Trabajo de WebSocket; criterios, tareas y pruebas verificables | 1 |
| [05_Pruebas.md](05_Pruebas.md) | Trabajo de Pruebas; criterios, tareas y pruebas verificables | 6 |
| [06_Servidor.md](06_Servidor.md) | Trabajo de Servidor; criterios, tareas y pruebas verificables | 2 |
| [00_Indice_de_subida.md](00_Indice_de_subida.md) | Orden para registrar issues y Milestones | — |
| [07_Estimacion.md](07_Estimacion.md) | Responsables, puntos y carga por sprint | — |
| [08_Cobertura_y_trazabilidad.md](08_Cobertura_y_trazabilidad.md) | Cobertura de los 20 RF/CU/HU | — |
| [09_Flujos_y_dependencias.md](09_Flujos_y_dependencias.md) | Comunicación entre issues y ruta crítica | — |
| [10_Decisiones_pendientes.md](10_Decisiones_pendientes.md) | Vacíos reales de los documentos; no inventar reglas | — |
| [11_Fuentes_y_diagramas.md](11_Fuentes_y_diagramas.md) | Mapa de documentos y diagramas recibidos | — |
| [12_Casos_de_prueba.md](12_Casos_de_prueba.md) | Catálogo local de CP con enlaces al issue | — |
| [13_Comportamiento_CU.md](13_Comportamiento_CU.md) | Escenarios oficiales de éxito y flujos alternativos de CU-01 a CU-20 | — |
| **Total** | **36 propuestas (`CON-01` a `CON-36`, una condicional)** | **36** |

## Milestones y alcance

| Milestone | Fechas | Resultado esperado |
|---|---|---|
| Sprint 3 - Bases e identidad | 02/11–13/11/2026 | Backend y app base, BD, identidad, roles y pacientes |
| Sprint 4 - Gestion de citas | 16/11–27/11/2026 | Disponibilidad, asignación automática, citas y reprogramación/cancelación |
| Sprint 5 - Integracion y seguimiento | 30/11–11/12/2026 | Agenda, atención, eventos WS, notificaciones, historial, reportes, entorno de integración |
| Sprint 6 - Validacion y entrega | 14/12–23/12/2026 | Pruebas E2E, concurrencia, permisos, respaldo y versión entregable |

## Trazabilidad central

La documentación conecta **RF-N → HU-N → CU-N → comportamiento oficial (camino feliz y alternativas) → Issues por tecnología → dependencias → CP → PR en `develop`**. La relación HU-XX fue propuesta en la matriz Markdown compartida; NO se asignan números `#` de GitHub ficticios.

**Ejemplo nuclear:** [CU-04 solicitar](13_Comportamiento_CU.md#cu-04) → [CU-05 asignar](13_Comportamiento_CU.md#cu-05) → [CU-06 validar](13_Comportamiento_CU.md#cu-06) → [CU-07 registrar](13_Comportamiento_CU.md#cu-07), implementados en [CON-13](02_Java.md#con-13), [CON-12](02_Java.md#con-12) y [CON-11](02_Java.md#con-11). La Vista Flutter reside en [CON-17](01_Flutter.md#con-17).

**No todo CU es una pantalla.** CU-05, CU-06, CU-07, CU-16 y CU-17 son procesos automáticos o internos; sus decisiones se ejecutan en el backend Java.

## Reglas de reparto MVC

- **Vista Flutter:** presenta el diseño UI aprobado, recoge solicitudes, muestra resultados y actualizaciones; no consulta PostgreSQL ni confirma cupos por su cuenta.
- **Controladores REST Java:** autentican, validan entradas y permisos, invocan servicios y devuelven resultados claros.
- **Modelo Java:** representa paciente, cita, agenda, disponibilidad y aplica reglas de negocio y estados; las operaciones de confirmación son transaccionales.
- **PostgreSQL:** persiste objetos e integridad. **WebSocket:** avisa después del commit; REST es fuente de verdad ante desconexiones.
- **No inventar políticas:** el tipo de autenticación, cupo por cancelación, estados de inasistencia, fórmulas y canales externos requieren aprobación. Ver [decisiones pendientes](10_Decisiones_pendientes.md).

<a id="definicion-de-terminado"></a>
## Definición de terminado

- [ ] Cumple todos los criterios y pruebas del Issue (incluyendo el camino alternativo del CU).
- [ ] Se ejecutaron las pruebas y quedaron registradas en [12_Casos_de_prueba.md](12_Casos_de_prueba.md) y `05_pro/`.
- [ ] PR revisado y fusionado en `develop`; el número y enlace se actualizan en el [índice](00_Indice_de_subida.md).
- [ ] Si cambió el diseño, se actualiza arquitectura/contrato/regla correspondiente con el mismo PR.
- [ ] Se actualiza [TRAZABILIDAD.md](../TRAZABILIDAD.md) sin confundir Issue propuesto con Issue existente.
- [ ] Se comprueba autorización por rol y no exposición de información del paciente en cada funcionalidad pertinente.

**GitHub:** se trabaja con issues y PR manuales por ahora; la configuración de `gh` queda para más adelante. No subir scripts de sincronización como si ya hubieran sido ejecutados.
