# Contribución y seguimiento — CNS Hospital Obrero

## Reglas del repositorio

- **Modelo en V** para vincular requisitos, diseño, implementación y pruebas; **Kanban** para observar avances, sin llamar Scrum a una metodología que no se ha aprobado.
- **`main`**: versión estable / entrega. **`develop`**: integración revisada. **`feature/TXX-descripcion-corta`**: rama de un issue concreto nacida desde `develop`.
- **No hacer push directo a `main` ni `develop`**. Todo trabajo se fusiona por PR dirigido a `develop` y revisado por otra persona.
- Antes de abrir un issue, buscar duplicados; preservar números ya usados T-XX/HU-XX/CP-XX y la numeración RF-XX/CU-XX.
- Todas las tareas de implementación deben estar conectadas con al menos un RF/CU o identificarse expresamente como transversales.

## Tablero y Milestone

`Backlog → Por hacer → En progreso → En revisión (PR) → Hecho`

Todo issue nuevo tiene Milestone, Assignee real, labels, dependencias, criterios y caso de prueba. Los porcentajes de Milestones dependen de issues/PR cerrados: los commits o checks de una lista no suman automáticamente progreso al Milestone.

## Formato del issue

**Título:** `[TXX][Java][CU-05] Implementar asignación automática` (ejemplo, X solo tras confirmar número libre).

**Cuerpo:** objetivo, descripción, RF/CU, tareas `- [ ]`, criterios `- [ ]`, casos de prueba, dependencias, responsable, milestone, rama, evidencia.

## Pull Requests

1. `git fetch origin && git switch develop && git pull --ff-only origin develop`.
2. `git switch -c feature/TXX-titulo-corto` (no recrear rama ya existente).
3. Trabajar y probar; `git add ...`; `git commit -m "feat(citas): validar cupos" -m "Refs #NUMERO_REAL"`.
4. `git push -u origin feature/TXX-titulo-corto`.
5. Abrir PR hacia `develop`, asociar issue en **Development**, registrar evidencias y pedir revisión.
6. Después del merge aprobado, actualizar TRAZABILIDAD, poner Hecho y **cerrar issue manualmente** si `develop` no es la rama por defecto. `Closes #N` automático funciona al fusionar en la rama por defecto, no en `develop` cuando default es `main`.

## Definition of Done

Criterios cumplidos; pruebas superadas; cambios arquitectónicos/documentales actualizados en el mismo PR; revisión aprobada; sin secretos ni datos reales de pacientes; issue cerrado; trazabilidad actualizada. Si falla un caso de prueba, crear issue `tipo:bug`.
