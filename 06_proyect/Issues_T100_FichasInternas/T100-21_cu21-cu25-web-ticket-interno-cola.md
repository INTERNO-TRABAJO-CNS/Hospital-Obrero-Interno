# [T100-21][Nuxt+Java][CU-21/25] Web del médico general: formulario «Ticket interno» y cola de prioridad por especialidad

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alex |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-21-cu21-cu25-web-ticket-interno-cola` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Nivel del modelo en V | Diseño detallado → unitarias (+ aceptación en T100-32) |
| Casos de uso | CU-21, CU-25 |
| Requisitos | RF-21 · RF-25 |
| Depende de / Bloquea | T100-18, T100-20 (se puede empezar con `fakeFichaInternaService`) / Bloquea: — |

## Objetivo
Implementar la pantalla de la pizarra «Ticket interno» (Afiliado · Especialidad · Prioridad · **Generar**) y la vista de la cola de prioridad que **solo** ve el médico general o el pediatra.

## Reglas de negocio que debes cumplir
- Desde la atención o la agenda se abre `/medico/ticket-interno?citaId=…` con el paciente ya cargado (número de afiliado y nombre, solo lectura).
- Con **Urgencia** marcada se puede buscar al paciente por número de afiliado sin cita (`GET /api/v1/pacientes?numeroAfiliado=` de T100-05).
- Especialidad: solo las 10 internas; **Terapia Intensiva** habilitada solo con Urgencia. Gravedad: ALTA / MEDIA / BAJA (obligatoria). Motivo: hasta 500 caracteres.
- Resultado: «Ticket N° … — fecha, hora, médico, consultorio» o «EN ESPERA: se le asignará fecha y se le notificará», con botón **Imprimir ticket** (página de T100-30). Si el backend rechaza, se muestra el mensaje del código.
- Cola (`/medico/cola`): elegir especialidad; el médico general ve las tablas **generales** y el pediatra las **pediátricas**. Columnas: posición, paciente, urgencia, gravedad, ≥ 60, fecha/hora o EN ESPERA, llegada. Solo lectura; se actualiza cada 15 s.
- Backend de la cola: `GET /api/v1/medico/fichas-internas/cola?especialidad=` — solo médicos que derivan (`MEDICO_NO_DERIVA`).

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-21_GenerarFichaInterna/CU-21_GenerarFichaInterna.puml` *(lo crea T100-16)*
- `03_des/01-diagramaCompor/CU-25_ConsultarColaPrioridad/CU-25_ConsultarColaPrioridad.puml` *(lo crea T100-16)*
- Fotos de la pizarra (10/11/2026): formulario IDAfil / Esp / Pri / Generar → JSON → «ID / no»

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/fichasinternas/ColaPrioridadService.java + ItemColaDTO.java` | `cola(long medicoId, TipoEspecialidad esp)`: ordenada con `ComparadorPrioridad` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/FichaInternaController.java` | agregar el `GET …/cola` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/fichasinternas/ColaPrioridadServiceTest.java` | orden y tabla correcta |
| `04_imp/frontend-web/pages/medico/ticket-interno.vue` | formulario + resultado |
| `04_imp/frontend-web/pages/medico/cola.vue` | tabla de la cola |
| `04_imp/frontend-web/stores/fichaInterna.ts, stores/cola.ts` | estado de la pantalla y mensajes por código |
| `04_imp/frontend-web/services/fichaInternaService.ts + services/fake/fakeFichaInternaService.ts` | `generar`, `ticket`, `cola`, `buscarPaciente` |
| `04_imp/frontend-web/models/fichaInterna.ts` | `Gravedad`, `SolicitudFichaInterna`, `TicketFichaInterna`, `ItemCola` |
| `04_imp/frontend-web/tests/fichaInterna.test.ts, tests/cola.test.ts` | Vitest |

## Paso a paso
1. Rama `feature/T100-21-cu21-cu25-web-ticket-interno-cola`.
2. Backend de la cola + test.
3. Formulario y resultado con el *fake*.
4. Cola.
5. Probar contra el backend real cuando T100-20 esté integrado.
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `fichaInterna.test: terapiaSinUrgencia_deshabilitada` | regla de la pantalla |
| `fichaInterna.test: error409_muestraMensaje` | «ID / no» |
| `cola.test: pediatraVeTablaPediatrica` | tabla correcta |
| `ColaPrioridadServiceTest: ordenUrgenciaGravedad60Llegada` | orden |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-21-cu21-cu25-web-ticket-interno-cola
# ... trabajar, commits pequeños: feat(T100-21): descripción
git push -u origin feature/T100-21-cu21-cu25-web-ticket-interno-cola
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
- Agenda del médico (T100-27).
- Página de impresión (T100-30).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-21-cu21-cu25-web-ticket-interno-cola`
