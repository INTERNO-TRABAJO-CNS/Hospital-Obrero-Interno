# Backlog de Construcción — PostgreSQL | CNS Hospital Obrero

[Índice general](README.md) · [Orden de creación](00_Indice_de_subida.md) · [Trazabilidad](08_Cobertura_y_trazabilidad.md) · [Flujos](09_Flujos_y_dependencias.md) · [Pruebas](12_Casos_de_prueba.md)

**Total de issues propuestos:** 1. **Código:** pendiente de crear en GitHub; son especificaciones del backlog y no evidencias de implementación.
**Responsables propuestos:** Alex. Los responsables y puntos se revisan en [07_Estimacion.md](07_Estimacion.md).

Este archivo toma el comportamiento oficial de [CU-01 a CU-20](13_Comportamiento_CU.md) y la [matriz de requisitos](08_Cobertura_y_trazabilidad.md). No sustituye la aprobación de arquitectura del Sprint 2.

## Distribución por Sprint

| Sprint | Issues |
|---|---|
| S3 (02/11–13/11/2026) | [CON-02](03_PostgreSQL.md#con-02) |

---

<a id="con-02"></a>
### [CON-02][PostgreSQL] Implementar esquema PostgreSQL y migraciones

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 3 - Bases e identidad (02/11–13/11/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 5 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-02` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-01](02_Java.md#con-01) · [ELA-07](../Backlog_Elaboracion/Backlog_Arquitectura_CNS.md)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:postgresql`  
**Rama de trabajo (base `develop`):** `feature/con-02-implementar-esquema-postgresql-y-migraciones` (crear solo cuando se abra el issue).

**Descripción:** Implementar esquema PostgreSQL y migraciones. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Criterios de aceptación**
- [ ] Migraciones versionadas crean las tablas previstas por el modelo aprobado en ELA-07 (usuarios, roles, pacientes, especialidades, médicos, agendas, consultorios, solicitudes y citas).
- [ ] Relaciones, claves únicas y restricciones impiden identificadores duplicados y permiten vincular cita con recursos existentes.
- [ ] Las restricciones relacionadas con solapamientos complementan, y no sustituyen, la validación transaccional de Java (RNF-03 y RNF-04).
- [ ] El esquema puede recrearse desde cero en ambiente de pruebas con datos sintéticos, sin datos reales de pacientes.

**Tareas**
- [ ] Configurar migraciones y PostgreSQL para desarrollo y pruebas.
- [ ] Implementar tablas y restricciones definidas en ELA-07, sin anticipar campos pendientes.
- [ ] Verificar reconstrucción, índices y permisos de conexión.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-02A](12_Casos_de_prueba.md#cp-cns-02a) — Ejecutar migraciones sobre una base vacía y comprobar todas las claves referenciales.
- [ ] [CP-CNS-02B](12_Casos_de_prueba.md#cp-cns-02b) — Intentar registrar una identificación de paciente duplicada y verificar que BD no crea un segundo registro.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.
