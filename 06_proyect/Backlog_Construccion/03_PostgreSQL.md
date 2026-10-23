# Backlog de Construcción — PostgreSQL

[Volver al índice](README.md) · Responsable indicado **como propuesta**, validar carga al iniciar cada sprint.

### [CON-02][PostgreSQL] Implementar esquema PostgreSQL y migraciones

**Identificador:** `CON-02` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 3 - Bases e identidad  
**Responsable sugerido:** Alex · **Tamaño inicial:** 5 puntos  
**Tecnología/área:** PostgreSQL  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-18  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-18  
**Depende de (referencias del plan):** CON-01, ELA-07  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:postgresql`

**Descripción:** Crear almacenamiento persistente versionado para usuarios, pacientes, médicos, agendas y citas.

**Criterios de aceptación**
- [ ] Las migraciones crean un esquema desde cero y no usan datos personales reales.
- [ ] Claves foráneas, índices y restricciones acordadas están activas.
- [ ] Se puede restablecer la base de pruebas automáticamente.

**Tareas**
- [ ] Configurar conexión y migraciones versionadas.
- [ ] Crear datos sintéticos para desarrollo.

**Caso de prueba sugerido:** Ejecutar migraciones dos veces y comprobar idempotencia de la herramienta.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-02-implementar-esquema-postgresql-y-mi` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).
