# Trazabilidad inicial — CNS Hospital Obrero

La tabla se deriva de la **matriz de 20 casos de uso** del documento CNS. Los nombres de CU y RF-01..RF-20 proceden de esa matriz. **No asigna automáticamente HU-XX ni CP-XX**, porque no se comprobó su numeración en el repositorio. Esos campos se completan con documentos oficiales y los IDs reales al cargar los issues. Los responsables de requisito se conservan como referencia de la distribución indicada anteriormente (no equivalen necesariamente al autor del código).

| Requisito | CU | Nombre | Referente RF (planificación) | Issues propuestos (no números GitHub) | Casos de prueba | Estado de evidencia |
|---|---|---|---|---|---|---|
| RF-01 | CU-01 | Iniciar sesión | Alejandro | CON-03 / CON-07 / CON-09 / CON-30 | CP por definir | No verificado |
| RF-02 | CU-02 | Gestionar usuarios, roles y permisos | Alejandro | CON-03 / CON-05 / CON-09 | CP por definir | No verificado |
| RF-03 | CU-03 | Registrar y consultar pacientes | Alejandro | CON-06 / CON-08 / CON-09 | CP por definir | No verificado |
| RF-04 | CU-04 | Solicitar cita médica | Alejandro | CON-13 / CON-17 / CON-20 / CON-30 / CON-32 | CP por definir | No verificado |
| RF-05 | CU-05 | Asignar cita automáticamente | Alejandro | CON-12 / CON-20 / CON-21 / CON-30 / CON-32 | CP por definir | No verificado |
| RF-06 | CU-06 | Validar disponibilidad de recursos | Alejandro | CON-10 / CON-11 / CON-12 / CON-20 / CON-21 / CON-32 | CP por definir | No verificado |
| RF-07 | CU-07 | Registrar cita confirmada | Alejandro | CON-13 / CON-20 / CON-23 / CON-30 / CON-32 | CP por definir | No verificado |
| RF-08 | CU-08 | Consultar cita del paciente | Alex | CON-14 / CON-17 / CON-23 | CP por definir | No verificado |
| RF-09 | CU-09 | Consultar citas registradas | Alex | CON-15 / CON-18 | CP por definir | No verificado |
| RF-10 | CU-10 | Reprogramar cita | Alex | CON-16 / CON-18 / CON-20 / CON-21 / CON-23 / CON-32 | CP por definir | No verificado |
| RF-11 | CU-11 | Cancelar cita | Alex | CON-16 / CON-18 / CON-20 / CON-21 / CON-23 / CON-32 | CP por definir | No verificado |
| RF-12 | CU-12 | Actualizar estado de cita | Alex | CON-19 / CON-28 | CP por definir | No verificado |
| RF-13 | CU-13 | Consultar agenda médica | Benjamín | CON-10 / CON-19 / CON-28 | CP por definir | No verificado |
| RF-14 | CU-14 | Consultar información permitida del paciente | Benjamín | CON-25 / CON-28 | CP por definir | No verificado |
| RF-15 | CU-15 | Registrar datos de atención | Benjamín | CON-25 / CON-28 | CP por definir | No verificado |
| RF-16 | CU-16 | Generar notificación | Matías | CON-23 / CON-24 / CON-30 | CP por definir | No verificado |
| RF-17 | CU-17 | Generar recordatorio de cita | Abigal | CON-23 / CON-24 / CON-30 | CP por definir | No verificado |
| RF-18 | CU-18 | Consultar historial de cambios | Alex | CON-26 / CON-29 | CP por definir | No verificado |
| RF-19 | CU-19 | Generar reportes de citas | Alejandro | CON-27 / CON-29 | CP por definir | No verificado |
| RF-20 | CU-20 | Consultar indicadores de gestión | Benjamín | CON-27 / CON-29 | CP por definir | No verificado |

## Actualizar tras crear issues

Agregar número GitHub `#N`, rama real `feature/TXX-...`, HU aprobada, CP numerada, evidencia de pruebas, estado real y PR. Un CU no está completo por tener un prototipo de pantalla: deben estar integrados backend + frontend + pruebas relevantes.
