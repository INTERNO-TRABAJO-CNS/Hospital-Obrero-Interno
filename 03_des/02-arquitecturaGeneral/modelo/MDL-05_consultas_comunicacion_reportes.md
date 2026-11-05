# MDL-05 — Consultas, notificaciones, reportes e indicadores (Abigal)
CU-08/09/16/17/19/20 ↔ RF/HU-08/09/16/17/19/20 · RNF-01/02/05/06. **Borrador, no código.**

## Objetos propios propuestos
`Notificacion`, `Recordatorio` y proyecciones de consultas/reportes. Indicadores pueden calcularse sin entidades almacenadas si el modelo real lo soporta; decisión posterior.

## Consumidores/contratos
- De M01 recibe permisos y propietario de paciente.
- De M03 y M04 recibe eventos de citas **confirmadas**, nunca borradores.
- De M02 recibe agenda autorizada.
- A Flutter entrega citas propias/búsqueda autorizada, buzón y reportes (si ese actor usa Flutter).

## Reglas
No filtrar solo en cliente. Notificar tras commit; recordatorios parametrizados (horas antes pendiente); administrador ve informes, no edita por defecto horarios. WebSocket actualiza UI y la vista debe recuperar estado por REST si perdió conexión.

## Verificación
Paciente A no obtiene citas B; ante rollback de cancelación no existe evento; dos entregas de un evento no duplican notificación; reporte no filtra datos confidenciales ajenos.

## Cambio futuro
Buzón de ofertas de adelanto pendiente de aprobación: no introducirlo como capacidad oficial actual.
