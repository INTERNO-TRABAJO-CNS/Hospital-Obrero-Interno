> **ACTUALIZACIÓN 26/10/2026:** Integración a cargo de BENJAMÍN, no Alejandro. Antes de publicar verificar tareas duplicadas.

# [S2-M06][Arquitectura MVC] Integrar arquitectura MVC, contratos y trazabilidad del equipo

**Issue de diseño / Elaboración (no programar).**

| Campo | Valor |
|---|---|
| Milestone | `Sprint 2 - Arquitectura MVC` (vence 29/10/2026) |
| Responsable propuesto | **Benjamín (integración), con revisión de los cinco** |
| Rama (desde `develop`) | `feature/S2-M06-integracion-benjamin` |
| Labels sugeridos | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:integracion` |
| CU | CU-01 a CU-20 |
| RF | RF-01 a RF-20 |
| HU | HU-01 a HU-20 |
| RNF | RNF-01 a RNF-06 |
| Dependencias | S2-M01, S2-M02, S2-M03, S2-M04 y S2-M05. |

**Lectura obligatoria antes de usar IA:** [Contexto compartido](CONTEXTO_COMPARTIDO.md) y los archivos fuente del Sprint 1 allí citados. Los códigos `S2-M06` identifican **esta propuesta**, no números de Issues GitHub creados.

Integrar los cinco modelos parciales en **una sola arquitectura MVC coherente** y preparar el documento guía del Sprint 3. **No rehacer los trabajos ya cerrados del Milestone**: reutilizar lo que esté aprobado y completar únicamente brechas.

### Dependencias
S2-M01, S2-M02, S2-M03, S2-M04, S2-M05; consulta el registro S2-M07, pero **no necesita aprobar la solicitud de cambio** para consolidar la arquitectura vigente.

### Actividades
- [ ] Revisar propiedad exclusiva de entidades: S2-M01 identidad/pacientes; S2-M02 recursos; S2-M03 solicitudes/cita; S2-M04 cambios/historial; S2-M05 lecturas/notificaciones.
- [ ] Integrar `01_vision_general.md`, `02_mvc.md`, `03_modelo_dominio.md`, `05_contratos.md`, `06_base_de_datos.md`, `07_seguridad_privacidad.md`, `08_decisiones_parametros.md` **o completar equivalentes existentes sin sobrescribir trabajo aprobado**.
- [ ] Preparar diagramas editables `ARQ-01_Componentes.puml`, `ARQ-02_CapasMVC.puml`, `ARQ-03_ModeloDominio.puml`, con versiones visuales exportadas cuando sea posible.
- [ ] Acordar interfaces entre submodelos y contratos iniciales REST con operaciones, entrada/salida/errores; definir eventos WebSocket descriptivos y la persistencia PostgreSQL a nivel de diseño.
- [ ] Verificar que las cinco piezas enlazan a las 20 CU y HU/RF, y diseñar al menos una prueba por CU sensible, incluida concurrencia de recursos.
- [ ] Registrar decisiones no tomadas y solicitud de cambio de reasignación como **pendiente**, no introducir CU-21/RF-21 como aprobado.
- [ ] Convocar revisión cruzada con el equipo, checklist de seguridad y plan de tareas técnicas del Sprint 3.

### Criterios de aceptación
- [ ] Una sola explicación de Modelo, Vista y Controlador, sin reglas de negocio repartidas en Flutter.
- [ ] Los diagramas y documentos enlazados son consistentes entre sí; no duplican entidades.
- [ ] Cobertura completa CU-01 a CU-20 y al menos un flujo integral CU-04→05→06→07 documentado.
- [ ] La arquitectura indica cómo evitar doble reserva (RNF-04) y cómo proteger los datos (RNF-01/02).
- [ ] Cada integrante revisó al menos una parte ajena; los hallazgos quedaron reflejados en PR.
- [ ] Entregable fusionado a `develop`, con `README.md` de arquitectura actualizado y listado de decisiones abiertas.

### Verificación de diseño
- [ ] Seguir una solicitud médica completa desde la Vista Flutter hasta la confirmación PostgreSQL y su notificación, identificando el responsable de cada paso.
- [ ] Comprobar por matriz que los 20 CU tienen propietario de diseño e interfaces sin contradicciones.

**Trabajo coordinado (leer para evitar contradicciones):** [S2-M01](S2-M01_identidad-pacientes.md) · [S2-M02](S2-M02_recursos-agenda.md) · [S2-M03](S2-M03_nucleo-citas.md) · [S2-M04](S2-M04_ciclo-vida-citas.md) · [S2-M05](S2-M05_consultas-comunicacion-ui.md) · [S2-M07](S2-M07_cambio-reasignacion.md) · [Mapa de interfaces](MAPA_INTERFACES.md)

### Integración y entrega
- [ ] Referenciar el issue real en el Pull Request y solicitar revisión cruzada de otro integrante.
- [ ] Hacer PR `feature/S2-M06-integracion-benjamin` → `develop` con documentación y diagramas (sin código de producción).
- [ ] Actualizar el índice con el número `#...` **real** después de crear la tarea en GitHub.
