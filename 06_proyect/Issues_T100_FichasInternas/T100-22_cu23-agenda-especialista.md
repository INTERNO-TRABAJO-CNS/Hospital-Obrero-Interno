# [T100-22][Java+Nuxt][CU-23] Agenda del especialista: fichas internas del día, «Atendida» y «No se presentó»

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Abigail |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-22-cu23-agenda-especialista` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-23 |
| Requisitos | RF-23 |
| Depende de / Bloquea | T100-17, T100-18, T100-20 / Bloquea: T100-23 |

## Objetivo
El especialista ve sus fichas internas del día (de la tabla general y de la pediátrica) y sus cirugías, y registra si el paciente fue atendido o no se presentó.

## Reglas de negocio que debes cumplir
- Solo el médico asignado a la ficha. **No hay confirmación de asistencia** en fichas internas.
- Atendida: ASIGNADA → ATENDIDA, con fecha ≤ hoy; observaciones opcionales (≤ 2000).
- No se presentó: ASIGNADA → **CANCELADA**, solo después de la hora de la ficha. Ese horario **no** se reasigna.
- El especialista **no** ve la gravedad (solo la ve el médico general); sí ve si fue urgencia y el motivo.
- Cada cambio: `CambioFichaRepository` + `FichaInternaEventos`.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-23_AtenderFichaInterna/CU-23_AtenderFichaInterna.puml` *(lo crea T100-16)*
- `03_des/diagramasEstado/ED-08_FichaInterna/ED-08_FichaInterna.puml`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/fichasinternas/AtencionFichaInternaService.java + AgendaEspecialistaDTO.java` | `agenda(medicoId, fecha)`, `marcarAtendida(fichaId, medicoId, observaciones)`, `marcarNoSePresento(fichaId, medicoId)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/EspecialistaController.java` | `GET /api/v1/especialista/agenda?fecha=`, `POST /api/v1/especialista/fichas-internas/{id}/atendida`, `POST …/{id}/no-se-presento` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/fichasinternas/AtencionFichaInternaServiceTest.java` | con Fakes y `RelojFijo` |
| `04_imp/frontend-web/pages/especialista/agenda.vue` | lista (hora · paciente · General/Pediatría · urgencia) y botones; botón «Programar cirugía» si `operaEnQuirofano` y la ficha está ATENDIDA (abre T100-23) |
| `04_imp/frontend-web/stores/agendaEspecialista.ts, services/especialistaService.ts + fake, models/agendaEspecialista.ts` |  |
| `04_imp/frontend-web/tests/agendaEspecialista.test.ts` | Vitest |

## Paso a paso
1. Rama `feature/T100-22-cu23-agenda-especialista`.
2. Service + tests.
3. Controller.
4. Pantalla Nuxt.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `agenda_juntaGeneralYPediatrica_ordenHora` |  |
| `noSePresento_antesDeLaHora_rechaza` |  |
| `noSePresento_noReasignaHorario` | el hueco queda libre sin buscar otro paciente |
| `marcarAtendida_otroMedico_403` |  |
| `agenda_noExponeGravedad` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-22-cu23-agenda-especialista
# ... trabajar, commits pequeños: feat(T100-22): descripción
git push -u origin feature/T100-22-cu23-agenda-especialista
```
Abrir el Pull Request con **base = `feature/T100-algoritmos-ejecucion`** (no `develop`, no `main`). Solo Benjamín integra; nadie integra su propio PR.

## Reglas de código (las revisa quien aprueba el PR)
- Controller sin SQL ni reglas; la Entidad no sale del modelo; DTO = `record`; Repository = interfaz en `model`, implementación `Pg*` en `infrastructure/pg`.
- `PreparedStatement` con `?`, `try-with-resources`, logger (nada de `System.out` ni `printStackTrace`).
- Un caso de uso = un método de Service. Cada flujo alternativo `[Na]` del diagrama = un resultado/excepción + un test.
- Service probado con `Fake*` (sin BD); `Pg*` contra PostgreSQL; Controller con MockMvc.
- **En el PR solo código y migraciones** (nada de `.md`, capturas ni documentación). Cambios fuera de las carpetas de la tabla: pedirlos antes a Benjamín.
- Si necesitas cambiar un contrato de `model/compartido`, **no lo cambies**: abre un comentario en este issue y avisa a Benjamín.
- Nada «demasiado automático»: el sistema calcula y propone; las acciones las dispara una persona (médico, especialista, administrador). El único proceso programado nuevo es el de fichas EN_ESPERA (T100-20).
- **MVC también en la web:** `pages/` = vista, `stores/` (Pinia) = controlador de interfaz, `services/` = llamadas REST, `models/` = tipos. Una página **nunca** llama `fetch` directo: usa su *store* y el *store* usa el *service*.
- Sin reglas de negocio en la web: el backend decide; la web muestra el resultado o el mensaje del `codigo` de error.
- Cada *service* tiene su `Fake*` en `services/fake/` para trabajar sin backend (igual que `FakeCatalogoService` en Flutter).
- Pruebas con Vitest (`npm run test`) para *stores* y *services*.
- La URL del backend sale de `NUXT_PUBLIC_API_URL`; nada de usuarios ni claves escritos en el código.

## Fuera de alcance
- Receta e historial del especialista (`// PENDIENTE PV-22`).
- Cirugías (T100-23).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-22-cu23-agenda-especialista`
