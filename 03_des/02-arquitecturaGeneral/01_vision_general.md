# ARQ-01 — Visión general (diseño)

**Problema:** filas, cancelaciones, fichas no disponibles y errores de asignación en el Hospital Obrero CNS (Tarija).

## Contexto
- Roles funcionales documentados: **Paciente, Enfermería/Admisión, Médico y Administrador**.
- El **paciente** solicita por especialidad; la fecha, hora, médico y consultorio son seleccionados por el sistema.
- **Enfermería/Secretaría** verifica llegada, gestiona citas autorizadas y registra cancelación por inasistencia cuando corresponda.
- **Médico** consulta agenda, ve datos permitidos y registra información operativa de atención; no reasigna pacientes ni marca inasistencia en el cambio propuesto.
- **Administrador** consulta reportes/indicadores de cumplimiento y, conforme al CU-02 vigente, gestiona usuarios/roles; su facultad de administrar agendas no se da por aprobada.

## Alcance técnico acordado
- Java + Spring Boot: reglas de negocio, transacciones, validación, seguridad, servicios y motor de asignación.
- PostgreSQL: persistencia y restricciones de integridad.
- Flutter/Dart: las interfaces **de los roles efectivamente habilitados para móvil**; qué roles disponen de Flutter está pendiente de confirmar por respuesta contradictoria.
- REST: operaciones con respuesta autoritativa del backend.
- WebSocket: informar cambios ya confirmados; jamás adjudicar un cupo.
- MVC: Vista Flutter; Controlador de interfaz/estado en Flutter + controladores REST Spring; Modelo en entidades/servicios/repositorios Java.

## Límites
Fuera del alcance descrito: historia clínica avanzada, facturación, farmacia y laboratorio. No se presume integración real con registros de afiliación institucional, proveedor SMS ni infraestructura en producción.

## Flujo vertical prioritario
Paciente → Vista → REST de solicitudes → Servicio de solicitudes Java → Agenda/Disponibilidad → Motor de asignación → Transacción de cita → PostgreSQL → evento después de confirmación → Notificación/WebSocket → Vista.

Véanse [componentes](diagramas/ARQ-01_Componentes.puml), [capas](diagramas/ARQ-02_CapasMVC.puml) y [secuencia](diagramas/ARQ-04_SecuenciaAsignacion.puml).
