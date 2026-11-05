# ARQ-09 — Contrato de Vistas v1 / Flutter

**Atención:** el equipo indicó que no todos los actores necesariamente usan Flutter, aunque también dijo «sí ... por todos». Hasta precisar canales reales, este documento enumera **vistas necesarias por rol**, sin afirmar que todas serán móviles. Los diseños UI existentes deben conservarse.

| Rol | Vista/conjunto de vistas sugerido | CU | Servicio backend | Regla UX |
|---|---|---|---|---|
| Paciente | Ingreso/autoregistro y verificación de afiliación | 01,03 | M01 | Número de asegurado obligatorio |
| Paciente | Solicitar cita por especialidad | 04 | M03 | No mostrar selector de médico/fecha/hora/consultorio |
| Paciente | Mis citas y estado | 08 | M05 | Solo citas propias |
| Paciente | Buzón de avisos y recordatorios | 16,17 | M05 | Avisar solo al confirmar cambios |
| Enfermería/Secretaría | Registro/consulta paciente, búsqueda de citas | 03,09 | M01/M05 | Filtros autorizados |
| Enfermería/Secretaría | Confirmación de llegada; cancelación/reprogramación | 10,11,12,15 | M04 | Registro de motivo y control de +10min |
| Médico | Agenda día/semana, paciente permitido, atención | 13,14,15 | M02/M04 | No adjudicar cupo ni marcar inasistencia del cambio propuesto |
| Administrador | Cuentas y roles | 02 | M01 | Autorización de backend |
| Administrador | Reportes e indicadores | 19,20 | M05 | No adjudicar manualmente recursos por defecto |

**Subfunciones que NO requieren pantalla propia:** CU-05 asignación, CU-06 validación, CU-07 confirmación persistente, CU-16/17 generación en backend. Una pantalla no equivale a un submodelo.

**Vista futura, no vigente:** buzón de ofertas de adelanto y aceptación voluntaria, tras aprobación del cambio.

### Revisión UI v1
Cada diseñador debe vincular la pantalla real existente (archivo Figma/Flutter/imagen si lo hubiera) a sus CU y contrato; declarar las pantallas no disponibles, no inventar que están implementadas.
