# [S2-M05][Arquitectura MVC] Modelo de consultas, notificaciones e indicadores y vistas v1

**Issue de diseño / Elaboración (no programar).**

| Campo | Valor |
|---|---|
| Milestone | `Sprint 2 - Arquitectura MVC` (vence 29/10/2026) |
| Responsable propuesto | Abigal |
| Rama (desde `develop`) | `feature/S2-M05-consultas-comunicacion-ui` |
| Labels sugeridos | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| CU | CU-08, CU-09, CU-16, CU-17, CU-19, CU-20 |
| RF | RF-08, RF-09, RF-16, RF-17, RF-19, RF-20 |
| HU | HU-08, HU-09, HU-16, HU-17, HU-19, HU-20 |
| RNF | RNF-02, RNF-05, RNF-06 |
| Dependencias | Se inicia paralelo; revisa salidas de S2-M01–M04. |

**Lectura obligatoria antes de usar IA:** [Contexto compartido](CONTEXTO_COMPARTIDO.md) y los archivos fuente del Sprint 1 allí citados. Los códigos `S2-M05` identifican **esta propuesta**, no números de Issues GitHub creados.

Diseñar el submodelo de **lectura, comunicación y reportes** y el **inventario de Vistas Flutter v1**. Este componente consulta datos de otros dominios y **no es dueño de CitaMedica, Paciente o Agenda**.

### Cobertura que debe analizarse
- **CU-08:** paciente consulta **solo sus citas**.
- **CU-09:** admisión filtra citas por paciente, médico, fecha, estado y consultorio.
- **CU-16:** el sistema genera avisos tras confirmación/cambio/cancelación.
- **CU-17:** recordatorios de citas futuras.
- **CU-19:** reportes por estados, médico, especialidad y consultorio.
- **CU-20:** indicadores de demanda, cancelación, inasistencia, tiempos y carga laboral, sin inventar fórmulas no aprobadas.

### Actividades
- [ ] Definir los modelos de **Notificacion, Recordatorio, ConsultaDeCitas, Reporte/Indicador** o vistas de lectura; identificar cuáles son entidades persistentes y cuáles cálculos/DTO.
- [ ] Describir qué eventos confirmados desde S2-M03/S2-M04 originan avisos, qué mensaje recibe cada rol y cómo evitar exposición de datos.
- [ ] Definir la diferencia: **REST** crea/consulta/confirma cambios; **WebSocket** informa cambios ya confirmados y el cliente vuelve a consultar si pierde conexión.
- [ ] Preparar **inventario de pantallas Flutter v1 por rol** (paciente, admisión/enfermería, médico, administrador), asociar pantalla → CU → operación → respuesta/error, sin crear una pantalla para cada subfunción automática.
- [ ] Diseñar interfaz funcional mínima de acceso, solicitudes, mis citas, gestión, agenda y reportes; reutilizar prototipos existentes si ya fueron cerrados en GitHub.
- [ ] Coordinar con S2-M01–M04 los datos autorizados y los nombres de estados que se muestran; no introducir lógica de negocio en la Vista.

### Criterios de aceptación
- [ ] Los seis CU están cubiertos, con sus consultas/errores y privacidad por rol.
- [ ] Está documentada la diferencia entre aviso WebSocket y transacción REST.
- [ ] Existe mapa Vista → CU → RF/HU → controlador (diseño conceptual) para todos los flujos principales del CNS.
- [ ] Se entrega `03_des/02-arquitecturaGeneral/modelo/MDL-05_consultas_notificaciones_reportes.md` y `diagramas/MDL-05_consultas_notificaciones_reportes.puml`, más `03_des/diseñoUI/00_Indice_Pantallas_v1.md` (ajustar mayúsculas a la ruta real).
- [ ] Los tiempos de recordatorio, canales externos y fórmulas faltantes se identifican como pendientes.

### Verificación de diseño
- [ ] Caso A: paciente A nunca puede ver el detalle de citas del paciente B.
- [ ] Caso B: caída del WebSocket no elimina ni duplica una cita confirmada en el backend.

**Trabajo coordinado (leer para evitar contradicciones):** [S2-M01](S2-M01_identidad-pacientes.md) · [S2-M02](S2-M02_recursos-agenda.md) · [S2-M03](S2-M03_nucleo-citas.md) · [S2-M04](S2-M04_ciclo-vida-citas.md) · [Mapa de interfaces](MAPA_INTERFACES.md)

### Integración y entrega
- [ ] Referenciar el issue real en el Pull Request y solicitar revisión cruzada de otro integrante.
- [ ] Hacer PR `feature/S2-M05-consultas-comunicacion-ui` → `develop` con documentación y diagramas (sin código de producción).
- [ ] Actualizar el índice con el número `#...` **real** después de crear la tarea en GitHub.
