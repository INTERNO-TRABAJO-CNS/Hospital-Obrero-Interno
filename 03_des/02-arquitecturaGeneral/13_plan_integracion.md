# ARQ-13 — Integración por Benjamín

> Modificado 04/11/2026: el trabajo de implementación se integra en `feature/T100-algoritmos-ejecucion` (solo código funcional). **Solo Benjamín integra** en esa rama, en `develop` y en `main`.

## Reparto vigente (04/11/2026)
1. **Benjamín**: CU-02, 05, 06, 07, 19, 20 — catálogos, asignación y reportes; integra.
2. **Matías**: CU-01, 03, 04, 08, 10 — login, pacientes, solicitud, mis citas, reprogramar.
3. **Alejandro**: CU-09 (+TV), 12, 13 — listado de fichas, confirmar asistencia, agenda médica.
4. **Alex**: CU-11, 14, 18 — cancelar (+automática), historial del paciente, historial de cambios.
5. **Abigail**: CU-15, 16, 17 — receta/fin de atención, notificaciones, aviso T-45.

## Criterio de entrega de cada integrante
- [ ] Casos CU/RF/HU correctos y enlazados; revisar escenario principal y alternativo del Word oficial.
- [ ] Entidades **solo del dominio asignado**, atributos esenciales e identificadores; referencias externas como IDs, no duplicación de clases.
- [ ] Operaciones provistas/consumidas, DTOs y errores conceptuales.
- [ ] Diagrama PlantUML parcial editable y tests de diseño (normales, error y permisos).
- [ ] Diferencias con DER real y pantallas UI v1 marcadas (no inventadas).
- [ ] PR desde feature basado en `develop` y revisión cruzada de otro integrante.

## Gate del integrador Benjamín
- [ ] Obtener y comparar **DER y diagramas ya elaborados** (no recibidos en el paquete).
- [ ] Confirmar clase única `CitaMedica`, relaciones médico/agenda/consultorio y FK reales.
- [ ] Conciliar propiedad de estados/llegada, solicitudes, atención y auditoría.
- [ ] Unificar contratos REST y WS sin reservar por WS.
- [ ] Conciliar vistas con roles realmente dotados de Flutter.
- [ ] Añadir evidencia de concurrencia (validación + guardado atómico en Java/PostgreSQL).
- [ ] Registrar diferencias RF-12/autoregistro y la reasignación en control de cambios.
- [ ] Revisar todos los archivos y diagramas `.puml` antes de abrir PR integrador.
- [ ] PR a `develop`, revisiones del equipo, no actualizar `main` automáticamente.

## Nombres propuestos de ramas (NO existentes por defecto)
`feature/S2-M01-identidad-pacientes`, `feature/S2-M02-recursos-agenda`, `feature/S2-M03-nucleo-citas`, `feature/S2-M04-ciclo-vida-citas`, `feature/S2-M05-consultas-comunicacion-ui`, `feature/S2-M06-integracion-benjamin`. Crear solo si no existen y cuando se vaya a trabajar ese issue; no reutilizar números de ramas eliminadas.

## Comandos por integrante (en terminal VS Code, desde raíz repo)
```bash
git status
git switch develop
git pull --ff-only origin develop
git switch -c feature/S2-M0X-tema
git add 03_des/02-arquitecturaGeneral/modelo/MDL-0X_*.md 03_des/02-arquitecturaGeneral/diagramas/MDL-0X_*.puml
git commit -m "docs(S2): definir submodelo MDL-0X"
git push -u origin feature/S2-M0X-tema
```
**Sustituir `0X` por 01–05 y el nombre de rama real**. Crear PR manual en GitHub `feature/...` → `develop`, solicitar revisión. Benjamín crea rama de integración desde `develop` **después de merges**, y hace commit solo de archivos transversales. Para evitar conflictos, los otros compañeros no modifican el mismo `03_modelo_dominio.md`.


## Flujo T100 (04/11/2026)
Cada issue `T100-NN` crea `feature/T100-NN-tema` desde `feature/T100-algoritmos-ejecucion`, abre PR hacia esa rama y lo integra solo Benjamín. Antes de empezar: `git fetch` y `git merge origin/develop` en T100. Nadie integra su propio PR.
