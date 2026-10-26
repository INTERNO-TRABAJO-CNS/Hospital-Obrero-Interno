# Guía para publicar y no duplicar Issues — Sprint 2 CNS

## No generar tareas automáticamente
- En GitHub ya existen **#28 Backlog de Elaboración** y **#29 Backlog de Construcción** cerrados. No crearlos de nuevo.
- **#10 Investigación sobre reasignación** se encuentra cerrado. No duplicar la investigación: si falta el trámite formal del cambio, abrir un issue de **control de cambios**, enlazando #10.
- Los códigos S2-M01…M07 son internos de un paquete de propuestas, NO números reales GitHub.
- Antes de publicar los cinco submodelos, comprobar los otros Issues ya cerrados en el Sprint 2 y sus PR/entregables.

## Campos recomendados al publicar pendientes reales
- **Milestone:** `Sprint 2 - Arquitectura MVC` (la captura actual indica vencimiento **28/10/2026**, mientras el contexto anterior menciona 29/10; respetar la fecha vigente del repositorio).
- **Tipo:** `Task` para documentos de diseño; NO Feature para simples actividades técnicas si el equipo dispone del tipo Task.
- **Labels:** usar las **existentes** en GitHub, no inventar etiquetas que no estén configuradas. Las capturas muestran `Elaboración` y `develop`; comprobar disponibilidad de etiquetas de `documentacion`, `arquitectura` o similares en repositorio.
- **Assignee:** Alejandro(M01), Alex(M02), Benjamín(M03), Matías(M04), Abigal(M05); Benjamín(M06).
- **Dependencies:** enlazar Issues reales una vez creados, no referencias inexistentes.
- **Estimación:** el esfuerzo en GitHub debe ser razonado por entregable y capacidad del equipo; no fijar valores sin consenso.
- **Criterio Done:** `.md` y `.puml` revisados, coherentes con DER existente, PR a `develop` fusionado y evidencia/estado actualizado.

## Flujo por PR
1. Abrir issue SOLO por brecha real de arquitectura.
2. Crear nueva rama desde `develop` con nombre único.
3. Entregar documento/diagrama asignados.
4. Push → Pull Request a `develop` → revisión cruzada.
5. Benjamín integra modelos ya fusionados en una rama de integración, realiza validación final y PR a `develop`.
6. El issue se cierra **después** de que su trabajo y PR sean aceptados.

## Plantillas editadas
Se adjuntan siete textos propuestos dentro de `ISSUES_PROPUESTOS_REVISADOS/`; el M06 quedó corregido a **Benjamín** y M07 se encuadra como trámite de cambio, no investigación repetida. Son material de referencia y NO publicaciones en GitHub.
