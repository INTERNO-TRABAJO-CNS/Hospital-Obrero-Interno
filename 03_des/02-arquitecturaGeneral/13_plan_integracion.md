# ARQ-13 — Integración por Benjamín

## Distribución de revisión (no reasigna cuestiones funcionales)
1. **Alejandro**: entrega MDL-01, UML y contratos de paciente/roles.
2. **Alex**: entrega MDL-02, UML y reglas de agenda/recursos.
3. **Benjamín**: entrega MDL-03 y **únicamente él** consolida `03_modelo_dominio.md`, `05_contratos_entre_modulos.md` y diagramas ARQ integrados tras revisión.
4. **Matías**: entrega MDL-04 y requisitos para recepción, cancelación y auditoría.
5. **Abigal**: entrega MDL-05 y vistas/notificaciones/indicadores.

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
