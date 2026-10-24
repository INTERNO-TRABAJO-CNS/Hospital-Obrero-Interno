# Backlog de Construcción — Servidor | CNS Hospital Obrero

[Índice general](README.md) · [Orden de creación](00_Indice_de_subida.md) · [Trazabilidad](08_Cobertura_y_trazabilidad.md) · [Flujos](09_Flujos_y_dependencias.md) · [Pruebas](12_Casos_de_prueba.md)

**Total de issues propuestos:** 2. **Código:** pendiente de crear en GitHub; son especificaciones del backlog y no evidencias de implementación.
**Responsables propuestos:** Matías, Alex. Los responsables y puntos se revisan en [07_Estimacion.md](07_Estimacion.md).

Este archivo toma el comportamiento oficial de [CU-01 a CU-20](13_Comportamiento_CU.md) y la [matriz de requisitos](08_Cobertura_y_trazabilidad.md). No sustituye la aprobación de arquitectura del Sprint 2.

## Distribución por Sprint

| Sprint | Issues |
|---|---|
| S5 (30/11–11/12/2026) | [CON-30](06_Servidor.md#con-30) |
| S6 (14/12–23/12/2026) | [CON-35](06_Servidor.md#con-35) |

---

<a id="con-30"></a>
### [CON-30][Servidor] Desplegar ambiente de integración de Java, Flutter y PostgreSQL

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 5 - Integracion y seguimiento (30/11–11/12/2026).
**Responsable sugerido:** Matías. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-30` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-09](05_Pruebas.md#con-09) · [CON-22](04_WebSocket.md#con-22)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:servidor`  
**Rama de trabajo (base `develop`):** `feature/con-30-desplegar-ambiente-de-integracion-de-java-flutte` (crear solo cuando se abra el issue).

**Descripción:** Desplegar ambiente de integración de Java, Flutter y PostgreSQL. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.

**Criterios de aceptación**
- [ ] Ambiente de integración ejecuta Java Spring Boot y PostgreSQL de pruebas, aislado de información real de la CNS.
- [ ] Desde Flutter de pruebas se consume REST y WebSocket mediante transporte cifrado y credenciales de prueba.
- [ ] Configuración, despliegue y reversión son reproducibles y documentados, sin credenciales publicadas.
- [ ] Infraestructura/proveedor definitivo se registra como pendiente de decisión; no se declara un proveedor no confirmado.

**Tareas**
- [ ] Preparar entorno, variables y scripts de despliegue reproducibles.
- [ ] Verificar acceso cifrado, migraciones y conectividad desde app.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-30A](12_Casos_de_prueba.md#cp-cns-30a) — Desde un cliente de pruebas se autentica, consulta cita y reconecta WebSocket.
- [ ] [CP-CNS-30B](12_Casos_de_prueba.md#cp-cns-30b) — Desplegar y revertir versión conserva esquema y citas de prueba según procedimiento.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.

---

<a id="con-35"></a>
### [CON-35][Servidor] Probar respaldo, restauración y recuperación del sistema

**Tipo:** Tarea técnica / validación. **Sprint:** Sprint 6 - Validacion y entrega (14/12–23/12/2026).
**Responsable sugerido:** Alex. **Talla inicial:** 3 puntos ([estimación](07_Estimacion.md)).
**Referencia del plan:** `CON-35` · **GitHub:** `#PENDIENTE` (no se ha verificado número existente).
**Dependencias:** [CON-30](06_Servidor.md#con-30)  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:prueba` · `tec:servidor`  
**Rama de trabajo (base `develop`):** `feature/con-35-probar-respaldo-restauracion-y-recuperacion-del` (crear solo cuando se abra el issue).

**Descripción:** Probar respaldo, restauración y recuperación del sistema. La implementación debe cumplir únicamente el alcance aprobado del CNS, separando Vista Flutter, control REST y Modelo Java según el diseño MVC.
**RNF aplicables:** RNF-03, RNF-04.

**Criterios de aceptación**
- [ ] Base de pruebas puede respaldarse y restaurarse en un entorno independiente con todas sus relaciones y cambios de citas.
- [ ] El procedimiento registra fecha, versión de esquema y pasos de recuperación; usa credenciales externas al repositorio.
- [ ] Se verifica que la restauración no produce pérdida de integridad ni reserva incompatible.
- [ ] No se prueba con información personal real sin autorización institucional.

**Tareas**
- [ ] Preparar y ejecutar respaldo y restauración de datos sintéticos.
- [ ] Documentar proceso, tiempo observado y verificación de consistencia.

**Casos de prueba de referencia (identificadores locales, no CP oficiales)**
- [ ] [CP-CNS-35A](12_Casos_de_prueba.md#cp-cns-35a) — Restaurar base con citas, usuarios y audit trail preserva referencias y relaciones.
- [ ] [CP-CNS-35B](12_Casos_de_prueba.md#cp-cns-35b) — Después de restaurar, no hay cita duplicada para la misma combinación incompatible de recursos.

**Fuentes y relaciones:** [Especificación de CU](13_Comportamiento_CU.md) · [Cobertura RF/HU](08_Cobertura_y_trazabilidad.md) · [Decisiones abiertas](10_Decisiones_pendientes.md) · [Flujo de entrega](09_Flujos_y_dependencias.md).
Si cambia un criterio o contrato, actualizar el documento aprobado de arquitectura y los vínculos de trazabilidad en el mismo Pull Request.

**Registro al crear en GitHub:** número `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`.
