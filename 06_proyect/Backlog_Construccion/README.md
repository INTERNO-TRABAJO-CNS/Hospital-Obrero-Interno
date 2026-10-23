# Backlog de Construcción — CNS Hospital Obrero

Los issues propuestos se derivan de la arquitectura MVC que debe aprobarse en el Sprint 2, y de los 20 casos de uso del CNS. **El núcleo del sistema es Java con Spring Boot: no hay motor C++, wrapper por pipes, rutas vehiculares, WhatsApp/LLM, Mapbox ni simulador de tráfico.**

| Archivo | Área | Issues |
|---|---|---:|
| [01_Flutter.md](01_Flutter.md) | Flutter | 9 |
| [02_Java.md](02_Java.md) | Java | 17 |
| [03_PostgreSQL.md](03_PostgreSQL.md) | PostgreSQL | 1 |
| [04_WebSocket.md](04_WebSocket.md) | WebSocket | 1 |
| [05_Pruebas.md](05_Pruebas.md) | Pruebas | 6 |
| [06_Servidor.md](06_Servidor.md) | Servidor | 2 |
| **Total** | | **36** |

El orden de creación y dependencias está en [00_Indice_de_subida.md](00_Indice_de_subida.md), y la carga está en [07_Estimacion.md](07_Estimacion.md).

## Fechas
| Sprint | Fechas | Objetivo |
|---|---|---|
| Sprint 3 | 02/11–13/11/2026 | Base Java/Flutter, PostgreSQL, seguridad, pacientes. |
| Sprint 4 | 16/11–27/11/2026 | Gestión y validación de citas, disponibilidad, asignación, agendas. |
| Sprint 5 | 30/11–11/12/2026 | Reasignación, eventos en tiempo real, notificaciones, reportes. |
| Sprint 6 | 14/12–23/12/2026 | Pruebas, seguridad, recuperación y versión de entrega (sprint reducido sugerido). |

### Reglas de implementación

- Una pantalla es una **Vista**; no crear un Modelo por pantalla. En Java viven el dominio y las reglas del negocio.
- Flutter presenta datos y manda solicitudes; Java valida, autoriza, decide y persiste.
- WebSocket informa cambios ya confirmados; REST sigue siendo fuente de verdad y vía de las operaciones.
- El algoritmo de asignación, la cancelación y la reasignación corren en Java y deben prevenir duplicidad de cupos mediante transacciones/restricciones.
- Si el contrato o una regla cambia, actualizar el diseño en el mismo PR.
- Las reglas aún no aprobadas son **pendientes de decisión**, no requisitos inventados.
- Referencias `CON-XX` identifican este plan; para GitHub revisar números libres `T-XX`, `HU-XX` y `CP-XX` antes de emplearlos.

### Definición de terminado (DoD)

Un issue se cierra solo tras cumplir los criterios, pasar pruebas, tener PR aprobado y fusionado en `develop`, actualizar la trazabilidad y cerrar manualmente el issue cuando proceda. Un commit no basta para demostrar avance.
