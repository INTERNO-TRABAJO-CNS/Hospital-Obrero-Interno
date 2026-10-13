Informe de Revisión y Consolidación de Requisitos

- Tipo: Documentation[cite: 1]
- Prioridad: P0 – Crítica[cite: 1]
- Rama de Trabajo: `feature/Documentación`[cite: 1]
- Proyecto: Sistema de Gestión y Control de Citas Médicas - Hospital Obrero CNS (Tarija, Bolivia)[cite: 1, 2]



1. Tareas Realizadas y Criterios de Aceptación
Para dar cumplimiento a los requerimientos de la tarea, se ejecutó la revisión sistemática de los siguientes artefactos:

- Revisión del Documento de Visión: Se validó el alcance del sistema (móvil Flutter, backend Spring Boot, REST API y WebSockets), los stakeholders (Dirección, TI, Admisión/Enfermería, Médicos y Pacientes) y las restricciones operativas y de privacidad[cite: 2].
- Revisión de Casos de Uso: Se auditaron los 20 casos de uso estructurados bajo el formato Fully Dressed (CU-01 al CU-20)[cite: 1].
- Revisión de Historias de Usuario (HU): Se comprobó la correspondencia directa de las acciones de los usuarios con los flujos principales y alternativos definidos en los casos de uso[cite: 1].
- Revisión de Requisitos Funcionales (RF) y No Funcionales (RNF): Se validó que la seguridad, confidencialidad, consistencia e integridad estén reflejadas en los requerimientos del sistema[cite: 1, 2].



2. Matriz de Trazabilidad Consolidada

| Código | Requisito Funcional (RF) / Historia de Usuario | Caso de Uso Asociado | Actor Principal / Responsable | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **RF-01** | Autenticación y control de acceso | CU-01: Iniciar Sesión[cite: 1] | Paciente / Enfermería / Médico / Administrador[cite: 1] | Conforme |
| **RF-02** | Gestión de usuarios, roles y permisos | CU-02: Gestionar Usuarios, Roles y Permisos[cite: 1] | Administrador[cite: 1] | Conforme |
| **RF-03** | Registro y consulta de pacientes | CU-03: Registrar y Consultar Pacientes[cite: 1] | Personal de Enfermería / Admisión[cite: 1] | Conforme |
| **RF-04** | Solicitud de cita médica sin selección manual | CU-04: Solicitar Cita Médica[cite: 1] | Paciente[cite: 1] | Conforme |
| **RF-05** | Asignación automática de citas | CU-05: Asignar Cita Automáticamente[cite: 1] | Sistema (Proceso automático)[cite: 1] | Conforme |
| **RF-06** | Validación de disponibilidad de recursos | CU-06: Validar Disponibilidad de Recursos[cite: 1] | Sistema (Proceso de validación)[cite: 1] | Conforme |
| **RF-07** | Registro de cita confirmada | CU-07: Registrar Cita Confirmada[cite: 1] | Sistema / Enfermería-Admisión[cite: 1] | Conforme |
| **RF-08** | Consulta de citas del paciente | CU-08: Consultar Citas del Paciente[cite: 1] | Paciente[cite: 1] | Conforme |
| **RF-09** | Consulta de citas registradas (filtros operativos) | CU-09: Consultar Citas Registradas[cite: 1] | Personal de Enfermería / Admisión[cite: 1] | Conforme |
| **RF-10** | Reprogramación de citas | CU-10: Reprogramar Cita[cite: 1] | Personal de Enfermería / Admisión[cite: 1] | Conforme |
| **RF-11** | Cancelación de citas con motivo obligatorio | CU-11: Cancelar Cita[cite: 1] | Personal de Enfermería / Admisión[cite: 1] | Conforme |
| **RF-12** | Actualización de estado de la cita | CU-12: Actualizar Estado de Cita[cite: 1] | Médico / Enfermería autorizada[cite: 1] | Observación |
| **RF-13** | Consulta de agenda médica (diaria/semanal) | CU-13: Consultar Agenda Médica[cite: 1] | Médico[cite: 1] | Conforme |
| **RF-14** | Consulta de información permitida del paciente | CU-14: Consultar Información Permitida del Paciente[cite: 1] | Médico[cite: 1] | Observación |
| **RF-15** | Registro de datos de atención (tiempos y asistencia) | CU-15: Registrar Datos de Atención[cite: 1] | Médico / Enfermería autorizada[cite: 1] | Conforme |
| **RF-16** | Generación de notificaciones de cambios | CU-16: Generar Notificación de Cita[cite: 1] | Sistema (Proceso automático)[cite: 1] | Conforme |
| **RF-17** | Generación de recordatorios de citas | CU-17: Generar Recordatorio de Cita[cite: 1] | Sistema (Proceso automático)[cite: 1] | Conforme |
| **RF-18** | Consulta de historial de cambios y trazabilidad | CU-18: Consultar Historial de Cambios[cite: 1] | Personal de Enfermería / Administrador[cite: 1] | Conforme |
| **RF-19** | Generación de reportes administrativos | CU-19: Generar Reportes de Citas[cite: 1] | Administrador[cite: 1] | Conforme |
| **RF-20** | Consulta de indicadores de gestión | CU-20: Consultar Indicadores de Gestión[cite: 1] | Administrador[cite: 1] | Observación |


3. Registro de Observaciones y Correcciones Necesarias
Durante la revisión cruzada de la documentación del proyecto, se identificaron los siguientes puntos que requieren corrección o validación con la institución:

1. Inconsistencia Terminológica en Estados de Cita (RF-12 / CU-12):**
   - Hallazgo: El documento de requisitos funcionales lista el estado `"inexistente"`, mientras que la historia de usuario asociada menciona `"inasistencia"`[cite: 1].
   - Corrección sugerida: Estandarizar el término a `inasistencia` en la base de datos y en las especificaciones del caso de uso `CU-12`[cite: 1].
2. Definición de Catálogos y Datos Restringidos (RF-14 / CU-14 y RF-11 / CU-11):**
   - Hallazgo: No se especifican explícitamente los campos exactos de la "información básica" permitida para el médico[cite: 1], ni se cuenta con un catálogo estructurado de motivos de cancelación.
   - Corrección sugerida: Definir estos catálogos en iteraciones posteriores junto al personal de la CNS.
3. Fórmulas de Indicadores (RF-20 / CU-20):
   - Hallazgo: Los indicadores de gestión están mencionados a nivel de visión, pero faltan las fórmulas de cálculo exactas[cite: 1].



4. Requisitos No Funcionales (RNF) Validados
- RNF-01 (Seguridad): Control estricto de accesos según roles (Paciente, Admisión/Enfermería, Médico, Administrador)[cite: 1, 2].
- RNF-02 (Confidencialidad): Protección y limitación de datos médicos según los permisos del rol[cite: 1, 2].
- RNF-03 y RNF-04 (Consistencia e Integridad):** Mecanismos para evitar la duplicación o el solapamiento de recursos médicos (horario, médico, consultorio) ante peticiones concurrentes[cite: 1, 2].
- RNF-06 (Rendimiento): Uso de WebSockets para la actualización en tiempo real de agendas y estados[cite: 1, 2].


Entregable documentado y listo para integración en la rama `feature/Documentación`[cite: 1].
