# Trazabilidad inicial — CNS Hospital Obrero

La tabla se deriva de la **matriz de 20 casos de uso** del documento CNS. Los nombres de CU y RF-01..RF-20 proceden de esa matriz. **No asigna automáticamente HU-XX ni CP-XX**, porque no se comprobó su numeración en el repositorio. Esos campos se completan con documentos oficiales y los IDs reales al cargar los issues. **Modificado 04/11/2026:** los responsables corresponden a la nueva distribución (Benjamín CU-02,05,06,07,19,20; Matías CU-01,03,04,08,10; Alejandro CU-09,12,13; Alex CU-11,14,18; Abigail CU-15,16,17). Los issues `T100-NN` son los de la rama `feature/T100-algoritmos-ejecucion`; los `CON-xx` son los del plan anterior.

| Requisito | CU | Nombre | Referente RF (planificación) | Issues propuestos (no números GitHub) | Casos de prueba | Estado de evidencia |
|---|---|---|---|---|---|---|
| RF-01 | CU-01 | Iniciar sesión | Matías | CON-03 / CON-07 / CON-09 / CON-30 / T100-05 | CP por definir | No verificado |
| RF-02 | CU-02 | Gestionar usuarios, roles y catálogos | Benjamín | CON-03 / CON-05 / CON-09 / T100-01 | CP por definir | No verificado |
| RF-03 | CU-03 | Registrar y consultar pacientes | Matías | CON-06 / CON-08 / CON-09 / T100-05 | CP por definir | No verificado |
| RF-04 | CU-04 | Solicitar cita médica | Matías | CON-13 / CON-17 / CON-20 / CON-30 / CON-32 / T100-06 | CP por definir | No verificado |
| RF-05 | CU-05 | Asignar cita automáticamente | Benjamín | CON-12 / CON-20 / CON-21 / CON-30 / CON-32 / T100-03 | CP por definir | No verificado |
| RF-06 | CU-06 | Validar disponibilidad de recursos | Benjamín | CON-10 / CON-11 / CON-12 / CON-20 / CON-21 / CON-32 / T100-02 | CP por definir | No verificado |
| RF-07 | CU-07 | Registrar cita confirmada | Benjamín | CON-13 / CON-20 / CON-23 / CON-30 / CON-32 / T100-03 | CP por definir | No verificado |
| RF-08 | CU-08 | Consultar cita del paciente | Matías | CON-14 / CON-17 / CON-23 / T100-07 | CP por definir | No verificado |
| RF-09 | CU-09 | Consultar fichas del día (listado y TV) | Alejandro | CON-15 / CON-18 / T100-09 | CP por definir | No verificado |
| RF-10 | CU-10 | Reprogramar cita | Matías | CON-16 / CON-18 / CON-20 / CON-21 / CON-23 / CON-32 / T100-07 | CP por definir | No verificado |
| RF-11 | CU-11 | Cancelar cita | Alex | CON-16 / CON-18 / CON-20 / CON-21 / CON-23 / CON-32 / T100-11 | CP por definir | No verificado |
| RF-12 | CU-12 | Actualizar estado de cita | Alejandro | CON-19 / CON-28 / T100-10 | CP por definir | No verificado |
| RF-13 | CU-13 | Consultar agenda médica | Alejandro | CON-10 / CON-19 / CON-28 / T100-08 | CP por definir | No verificado |
| RF-14 | CU-14 | Consultar historial del paciente | Alex | CON-25 / CON-28 / T100-12 | CP por definir | No verificado |
| RF-15 | CU-15 | Registrar datos de atención (receta y fin) | Abigail | CON-25 / CON-28 / T100-14 | CP por definir | No verificado |
| RF-16 | CU-16 | Generar notificación | Abigail | CON-23 / CON-24 / CON-30 / T100-15 | CP por definir | No verificado |
| RF-17 | CU-17 | Generar aviso previo de cita (T-45) | Abigail | CON-23 / CON-24 / CON-30 / T100-15 | CP por definir | No verificado |
| RF-18 | CU-18 | Consultar historial de cambios | Alex | CON-26 / CON-29 / T100-13 | CP por definir | No verificado |
| RF-19 | CU-19 | Generar reportes y agenda de supervisión | Benjamín | CON-27 / CON-29 / T100-04 | CP por definir | No verificado |
| RF-20 | CU-20 | Consultar indicadores de gestión | Benjamín | CON-27 / CON-29 / T100-04 | CP por definir | No verificado |

## Actualizar tras crear issues

Agregar número GitHub `#N`, rama real `feature/TXX-...`, HU aprobada, CP numerada, evidencia de pruebas, estado real y PR. Un CU no está completo por tener un prototipo de pantalla: deben estar integrados backend + frontend + pruebas relevantes.
