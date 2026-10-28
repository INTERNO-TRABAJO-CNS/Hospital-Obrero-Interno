> **ACTUALIZACIÓN 26/10/2026:** La investigación sobre reasignación ya figura como issue #10 CERRADO. Este asunto S2-M07 debe limitarse al **registro formal de cambio y pendientes de aprobación**, sin repetir investigación ni incorporar todavía RF-21/CU-21.

# [S2-M07][Arquitectura MVC] Registrar cambio solicitado: ofertas y reasignación de cupos

**Issue de diseño / Elaboración (no programar).**

| Campo | Valor |
|---|---|
| Milestone | `Sprint 2 - Arquitectura MVC` (vence 29/10/2026) |
| Responsable propuesto | Matías (registro), revisión Benjamín y equipo |
| Rama (desde `develop`) | `feature/S2-M07-cambio-reasignacion` |
| Labels sugeridos | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `tipo:cambio` |
| CU | Impacto potencial en CU-06, CU-07, CU-10, CU-11, CU-12, CU-13, CU-16, CU-18 |
| RF | Impacto potencial en RF-06, RF-07, RF-10, RF-11, RF-12, RF-16, RF-18 |
| HU | Impacto potencial en HU equivalentes |
| RNF | RNF-03, RNF-04, RNF-02 |
| Dependencias | Independiente; registro para consideración de S2-M06. |

**Lectura obligatoria antes de usar IA:** [Contexto compartido](CONTEXTO_COMPARTIDO.md) y los archivos fuente del Sprint 1 allí citados. Los códigos `S2-M07` identifican **esta propuesta**, no números de Issues GitHub creados.

**Solicitud de cambio documental, no de construcción ni de modificación inmediata de requisitos cerrados.** Preservar las versiones de Sprint 1; documentar impacto conforme al Modelo en V, para que todo el equipo y futuras IAs entiendan qué habría que actualizar **antes** de implementar.

### Contexto exacto aportado por el equipo
- Un cupo se libera **solo** por cancelación válida o por inasistencia del paciente tras **10 minutos** desde su hora programada. Secretaría/enfermería registra el hecho, **no elige al sustituto**.
- **No se mueven automáticamente las otras citas del día actual.** El sistema considera citas futuras comenzando por el **día siguiente**; por ejemplo, si el cupo libre es lunes, candidatos del martes, y si se adelanta una cita del martes se puede generar otro cupo para candidatos del miércoles.
- Dentro del día candidato, los pacientes **prioritarios** se consideran primero. Se les envían ofertas simultáneas al **buzón de la aplicación**; la primera aceptación **confirmada de forma atómica por Java/PostgreSQL** gana. El resto de ofertas del mismo cupo se invalida/retira.
- Si no se concreta con prioritarios, se pasa a pacientes normales elegibles; se propuso invitar a los del día, con regla de desempate/alcance aún pendiente. **Nadie es movido sin aceptar**, ni pueden quedar dos pacientes en un cupo.
- Médico: vista de confirmación **SÍ/NO**; no opera la reasignación ni registra la inasistencia (según el cambio propuesto). Secretaría registra cancelación/inasistencia. 
- Núcleo Java/Spring Boot, PostgreSQL para atomicidad, Flutter buzón, WebSocket solo informa el estado. **Sin código en esta tarea.**

### Actividades
- [ ] Redactar la solicitud `CR-01_reasignacion_cupos.md` con estado **PENDIENTE DE APROBACIÓN** y motivo del cambio.
- [ ] Crear matriz de impacto: documento de requisitos, matriz RF/HU, fichas CU, comportamiento `.puml`, diagramas CU, Vista Flutter, Modelo Java, BD, REST, WebSocket, privacidad y pruebas.
- [ ] Identificar especialmente CU-11/12/16 y el futuro nuevo CU de reasignación **solo como propuesta**, sin renumerar ni editar fuentes aún.
- [ ] Registrar decisiones abiertas: quién concede prioridad; validez de oferta; regla de normales (primero cronológico vs oferta simultánea); límite de días; hora útil tras 10 min; si cancelación de paciente está permitida y cómo; qué ocurre si nadie acepta.
- [ ] Explicar aceptación concurrente: **una sola confirmación**, demás ofertas pasan a no vigentes; la cita previa del que adelanta no se pierde ante error.
- [ ] Referenciar esta solicitud desde `08_decisiones_parametros.md` o su registro de pendientes, **sin cambiar el alcance vigente silenciosamente**.

### Criterios de aceptación
- [ ] Contexto de la petición está completo, sin afirmar que ya está implementada.
- [ ] Lista de documentos/diagramas/Issues futuros afectados y decisiones sin resolver.
- [ ] Ningún requisito, HU, CU o diagrama ya cerrado del Sprint 1 fue modificado por esta tarea.
- [ ] Se establece que cambios posteriores requieren revisión de trazabilidad, diseño, pruebas y aceptación del equipo.

### Verificación documental
- [ ] Comprobar que alguien ajeno al equipo puede explicar quién marca inasistencia, quién decide el ofrecimiento y por qué no hay doble reserva.
- [ ] Comprobar que el registro no trata RF-21/CU-21 como identificadores ya aprobados.

**Trabajo coordinado (leer para evitar contradicciones):** [S2-M03](S2-M03_nucleo-citas.md) · [S2-M04](S2-M04_ciclo-vida-citas.md) · [S2-M05](S2-M05_consultas-comunicacion-ui.md) · [S2-M06](S2-M06_integracion-mvc.md) · [Mapa de interfaces](MAPA_INTERFACES.md)

### Integración y entrega
- [ ] Referenciar el issue real en el Pull Request y solicitar revisión cruzada de otro integrante.
- [ ] Hacer PR `feature/S2-M07-cambio-reasignacion` → `develop` con documentación y diagramas (sin código de producción).
- [ ] Actualizar el índice con el número `#...` **real** después de crear la tarea en GitHub.
