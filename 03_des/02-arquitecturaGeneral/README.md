# CNS — Sprint 2 · Arquitectura MVC · base de integración v1

**Fecha:** 26/10/2026 · **Estado:** diseño preliminar para revisión, NO aprobado · **Integración:** Benjamín · **Base Git:** `develop`.

> Este paquete agrega documentos faltantes sin sustituir ni sobrescribir el modelo de datos, diagramas o interfaces que el equipo ya elaboró. Hay documentos previos que no fueron suministrados: contrastar cualquier nombre de clase/tabla con el DER y los diagramas ya existentes ANTES de fusionar. No contiene código de producción.

## Fuente de verdad y precedencia
1. Documentos oficiales del Sprint 1: requisitos, matriz de trazabilidad, especificación de CU-01 a CU-20 y sus diagramas.
2. Decisiones explícitas del equipo confirmadas el 26/10/2026 (en `11_decisiones_pendientes.md`).
3. Modelos y diagramas del repositorio ya revisados por el equipo (aún pendientes de cotejar en esta copia).
4. Estos archivos de arquitectura son **propuestas**, no cambios aprobados de requisitos.

## Lectura guiada

| Archivo | Uso |
|---|---|
| [01_vision_general.md](01_vision_general.md) | Límites, actores, tecnologías |
| [02_mvc.md](02_mvc.md) | Capas y responsabilidades MVC |
| [03_modelo_dominio.md](03_modelo_dominio.md) | Clases, propietarios, relaciones y estados |
| [04_reglas_negocio.md](04_reglas_negocio.md) | Asignación y reglas confirmadas vs. pendientes |
| [05_contratos_entre_modulos.md](05_contratos_entre_modulos.md) | Acuerdos entre los cinco submodelos |
| [06_contratos_rest_api.md](06_contratos_rest_api.md) | Borrador de contratos API; sin implementar |
| [07_base_de_datos.md](07_base_de_datos.md) | Alineación con DER PostgreSQL existente |
| [08_seguridad_privacidad.md](08_seguridad_privacidad.md) | Controles y permisos |
| [09_interfaz_flutter_v1.md](09_interfaz_flutter_v1.md) | Mapa de vistas sin inventar pantallas para subfunciones |
| [10_websocket.md](10_websocket.md) | Eventos informativos después de commit |
| [11_decisiones_pendientes.md](11_decisiones_pendientes.md) | Decisiones confirmadas, supuestos y conflictos |
| [12_trazabilidad.md](12_trazabilidad.md) | RF/HU/CU y propiedad por integrante |
| [13_plan_integracion.md](13_plan_integracion.md) | Flujo de integración de Benjamín y checklist |
| [modelo/](modelo/) | Cinco contratos de submodelo para trabajo paralelo |
| [diagramas/](diagramas/) | PlantUML editable: componentes, capas, clases, secuencias, estados |

## Salidas verificables del Sprint 2
- Cinco submodelos (`MDL-01…05`) revisados por sus propietarios.
- Un modelo de dominio integrado, una única definición de CitaMedica y relaciones acordadas con el DER real.
- Contratos entre módulos, propuesta REST y eventos WS coherentes con casos de uso.
- Registro formal de aspectos pendientes (incluida reasignación por cambio no aprobado aún).
- Revisión cruzada y PR integrador a `develop`.

**Prohibido:** crear CU-21/RF-21 como aprobado, reescribir diagramas previos sin comparar, dar por implementada la funcionalidad o alterar el backlog cerrado solo para hacer coincidir estos documentos.
