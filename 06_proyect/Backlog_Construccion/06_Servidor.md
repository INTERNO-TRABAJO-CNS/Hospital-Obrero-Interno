# Backlog de Construcción — Servidor

[Volver al índice](README.md) · Responsable indicado **como propuesta**, validar carga al iniciar cada sprint.

### [CON-30][Servidor] Desplegar ambiente de integración de Java, Flutter y PostgreSQL

**Identificador:** `CON-30` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 5 - Integracion y seguimiento  
**Responsable sugerido:** Matías · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Servidor  
**Requisitos relacionados:** RF-01, RF-04, RF-05, RF-07, RF-16, RF-17  
**Casos de uso relacionados:** CU-01, CU-04, CU-05, CU-07, CU-16, CU-17  
**Depende de (referencias del plan):** CON-09, CON-22  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:servidor`

**Descripción:** Proporcionar entorno de pruebas seguro, sin infraestructura específica impuesta antes de decidirla.

**Criterios de aceptación**
- [ ] Backend accesible de manera cifrada al ambiente de pruebas.
- [ ] Base de prueba separada de datos reales.
- [ ] Se documenta despliegue, variables y rollback sin publicar secretos.

**Tareas**
- [ ] Configurar despliegue reproducible.
- [ ] Validar API/WS desde móvil o emulador.

**Caso de prueba sugerido:** Una nueva versión se despliega y revierte con guía.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-30-desplegar-ambiente-de-integracin-de` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).

---

### [CON-35][Servidor] Probar respaldo, restauración y recuperación del sistema

**Identificador:** `CON-35` (referencia temporal de planificación; comprobar T-XX libre antes de crear en GitHub).  
**Milestone:** Sprint 6 - Validacion y entrega  
**Responsable sugerido:** Alex · **Tamaño inicial:** 3 puntos  
**Tecnología/área:** Servidor  
**Requisitos relacionados:** RF-01, RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-08, RF-09, RF-10, RF-11, RF-12, RF-13, RF-18, RF-19, RF-20  
**Casos de uso relacionados:** CU-01, CU-02, CU-03, CU-04, CU-05, CU-06, CU-07, CU-08, CU-09, CU-10, CU-11, CU-12, CU-13, CU-18, CU-19, CU-20  
**Depende de (referencias del plan):** CON-30  
**Etiquetas sugeridas:** `fase:construccion` · `tipo:tarea` · `tec:servidor`

**Descripción:** Validar recuperación de datos y operación ante caída del entorno.

**Criterios de aceptación**
- [ ] Respaldo de base de pruebas puede restaurarse en otro destino.
- [ ] Credenciales se administran fuera del repositorio.
- [ ] Procedimiento escrito incluye responsable y pasos reversibles.

**Tareas**
- [ ] Realizar respaldo/restauración.
- [ ] Documentar límites y recuperación.

**Caso de prueba sugerido:** Restauración recupera citas y sus referencias intactas.  
**Caso de prueba CP-XX:** numerar después de revisar los CP existentes.  
**Rama:** `feature/TXX-con-35-probar-respaldo-restauracin-y-recup` (sustituir `TXX` por número libre).  
**Issue GitHub:** `#PENDIENTE` (anotar después de crearlo).
