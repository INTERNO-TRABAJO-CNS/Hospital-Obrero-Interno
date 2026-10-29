# [S3-02][PostgreSQL] Esquema PostgreSQL de identidad con migraciones Flyway y datos semilla

| Campo | Valor |
|---|---|
| Milestone | Sprint 3 - Bases e identidad (vence 2026-11-13) |
| Responsable | Alex (usuario de GitHub por confirmar) |
| Revisor | Alejandro (no se mergea el PR propio) |
| Rama (desde `develop`) | `feature/S3-02-bd-identidad` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:postgresql` |
| Puntos | 5 (= CON-02) |
| Depende de | S2-IMP-01 (Flyway + docker-compose) · S2-DOC-02 (el DER real en el repo) |
| Bloquea | S3-05, S3-08, S3-09 |

> Códigos `S2-…`/`S3-…` son **internos de este paquete**, no números de GitHub. Al crear el issue, anotar el `#` real en «Registro» y en `06_proyect/TRAZABILIDAD.md`.

## Objetivo
Crear las tablas que necesitan CU-01, CU-02 y CU-03 **tal como están en el DER real** y dejar roles y usuarios de prueba para que el resto del equipo pueda iniciar sesión y registrar pacientes en su máquina.

## Trazabilidad
| RF | HU | CU | RNF | Backlog | Diagrama de comportamiento |
|---|---|---|---|---|---|
| — | — | transversal | RNF-01, RNF-02 | CON-02 | — |

**RNF aplicables:**

- **RNF-01** — Dado que un usuario no se encuentra autenticado o no posee el permiso requerido, cuando intenta acceder a una función restringida, entonces el sistema deberá impedir el acceso.
- **RNF-02** — Dado que existe información asociada a un paciente, cuando un usuario intenta consultarla, entonces el sistema deberá mostrar únicamente la información permitida para el rol y la relación correspondiente

## Criterios de aceptación de esta tarjeta

- [ ] Los nombres de tablas y columnas son **los del DER subido en S2-DOC-02**. Si el DER no existe, esta tarjeta se detiene y se avisa: no se diseña a escondidas.
- [ ] Schema `identidad` con: paciente, usuario, rol, permiso y sus relaciones (según el DER; MDL-01 propone también `persona`).
- [ ] Restricciones: `UNIQUE(numero_asegurado_cns)` y `NOT NULL` en el paciente (CI es dato secundario, DC-06); `UNIQUE(nombre_usuario)`; la clave se guarda **solo como hash** (`password_hash`), nunca en claro (RNF-01/02).
- [ ] Una secuencia por tabla para que `nextIdentity()` funcione (`identidad.paciente_id_seq`, …).
- [ ] Migraciones `V2__…`, `V3__…` (`V1` es de S2-IMP-01), idempotentes sobre base vacía; semilla de los 4 roles de los documentos: **PACIENTE, ENFERMERIA (admisión), MEDICO, ADMINISTRADOR**.
- [ ] Usuarios y pacientes **sintéticos** solo para dev, en `db/seed-dev/` (versiones `V9xx`), activados únicamente con el perfil `dev`. Nunca datos reales.

## Tareas

- [ ] Escribir migraciones y semillas.
- [ ] Documentar en `04_imp/README.md` cómo recrear la BD de cero.
- [ ] Prueba que migra una BD vacía y comprueba claves foráneas y únicas (decidir con Benjamín: docker-compose o Testcontainers).
- [ ] Prueba: dos inserts con el mismo `numero_asegurado_cns` → la BD rechaza el segundo.

## Pruebas (lado derecho del V)

Casos del catálogo existente (`06_proyect/Backlog_Construccion/12_Casos_de_prueba.md`) que esta tarjeta debe dejar **ejecutables**:

- [ ] `CP-CNS-02A` — Ejecutar migraciones sobre una base vacía y comprobar todas las claves referenciales.
- [ ] `CP-CNS-02B` — Intentar registrar una identificación de paciente duplicada y verificar que BD no crea un segundo registro.

## Fuera de alcance (no hacer en esta tarjeta)

- Tablas de citas, agendas y notificaciones (Sprint 4–5).
- Procedimientos almacenados con reglas de negocio: las reglas viven en Java (DC-02).

## Definition of Done

- [ ] Cumple cada criterio de la HU/CU citados arriba (incluidos los flujos alternativos del diagrama).
- [ ] Respeta las reglas de capa de `06_proyect/Issues_MVC/01_GUIA_MVC.md` (el revisor las marca una por una).
- [ ] Pruebas escritas y en verde (`./mvnw test` o `flutter test` según corresponda); el PR muestra la salida o captura.
- [ ] PR hacia `develop` (nunca a `main`), enlazado al issue en **Development**, revisado y aprobado por otra persona (no se mergea el PR propio).
- [ ] Sin secretos, sin datos reales de pacientes, sin `System.out.println` (usar logger).
- [ ] Se actualiza `06_proyect/TRAZABILIDAD.md` (columna issue/rama/PR/CP) en el mismo PR y se mueve la tarjeta en el tablero.

## Registro
Issue `#____` · PR `#____` · CP oficial `____` · evidencia `05_pro/____`
