# Issues T100-16 … T100-32 — Fichas internas, quirófanos y ajustes del equipo

Creado el 10/11/2026 a partir de la reunión en pizarra («Ticket interno», «hist web JS», «Tickets», «IDTick → fecha, hora, médico, especialidad»). Son **17 tarjetas** que **continúan la numeración** de `06_proyect/Issues_T100/` (T100-00 … T100-15) y se trabajan en la **misma rama de integración: `feature/T100-algoritmos-ejecucion`**.

> Nota: en el mensaje se nombró la rama como «feature/T100-algoritmos-integracion», pero esa rama no existe. La rama en la que trabaja el equipo es `feature/T100-algoritmos-ejecucion`, y es la que usan todas las tarjetas.

## 1. Qué es lo nuevo (resumen para todo el equipo)
1. **Ficha interna (derivación).** Durante la consulta de **Médico General** (`CONSULTORIO_FAMILIAR`) o de **Pediatría**, el médico ve algo grave (por ejemplo, una fractura) y genera una ficha para una especialidad. **El paciente no hace nada.** El sistema le asigna médico, día y hora. **Odontología no deriva.**
2. **Cola de prioridad** por especialidad: **urgencia > gravedad (ALTA > MEDIA > BAJA) > adulto mayor (≥ 60) > orden de llegada**. La gravedad **solo la ve el médico general o el pediatra**.
3. **Quirófanos.** Si el daño es grave, el especialista que atendió la ficha programa la cirugía. **Cero choques de horario**, con limpieza de **30 a 45 min** después de cada cirugía.
4. **Web del médico en Nuxt (Vue).** Todas las pantallas del médico pasan a `04_imp/frontend-web`. El paciente, enfermería, el administrador y la TV **siguen en Flutter**. El backend sigue siendo **Spring Boot**.
5. **Reglas que valen «en todo»:** Pediatría solo **menores de 10 años**; prioridad para **adultos mayores (60 años o más)** también en las citas comunes; ningún cruce de horario del paciente entre citas comunes, fichas internas y cirugías.
6. **Aviso al paciente:** notificación en la app **y** ticket impreso (por si no tiene internet), tanto para citas comunes como para fichas internas y cirugías.

## 2. Decisiones del 10/11/2026 (fuente para T100-16)
| Tema | Decisión |
|---|---|
| Especialidades internas y duración de cada ficha | Cardiología 20–40 min · Nefrología 20–40 · Neumología 20–30 · Endocrinología 20–40 · Gastroenterología 20–30 · Hematología 20–40 · Infectología 30–45 · Dermatología 15–30 · Medicina del Trabajo 20–40 · Terapia Intensiva 30–60 |
| Fichas por día de cada especialista | Igual que en la consulta general: de 5 a 7 |
| Tablas | **20 tablas** de fichas internas (10 especialidades × general y pediátrica), separadas de las citas comunes, para que el administrador vea cada una por separado |
| Confirmación de asistencia | **No existe** en fichas internas |
| Cancelación | Solo cuando el paciente **no se presentó** (estado CANCELADA). Ese horario **no** se reasigna |
| Sin cupo | La ficha queda **EN_ESPERA** y se vuelve a buscar al inicio de cada día hábil |
| Desplazamiento | Si no hay hueco, un caso de mayor prioridad toma el lugar de uno de menor prioridad. El desplazado pasa al siguiente hueco (o a EN_ESPERA) y se le notifica y reimprime el ticket |
| Urgencia | Llega por el médico general, que le asigna la ficha **de inmediato** (hoy) |
| Terapia Intensiva | Solo reanimación de pacientes a punto de morir. Entra **solo por urgencia**, de inmediato y sin agenda; dura de 30 a 60 min |
| Derivaciones por cita | **Una sola** |
| Quirófanos | **No se comparten** entre especialidades; **una sola tabla** de cirugías; la duración la estima el especialista según qué tan problemático es el caso; limpieza de 30 a 45 min; una urgencia puede desplazar |
| Cirugías por especialidad | Gastroenterología 30 min–2 h · Hematología 30 min–2 h · Dermatología 15 min–2 h. Infectología, Medicina del Trabajo y Terapia Intensiva **no operan**. Cardiología, Nefrología, Neumología y Endocrinología: **si hay tiempo** (pueden durar más de 12 h) |
| Automatización | «No muy automático»: el sistema calcula y propone; una persona confirma. Así no se crean telarañas de código y se respeta MVC y el modelo en V |
| Impresión | La hace el equipo (T100-30) |

### Supuestos que T100-16 registra como pendientes (validar con el equipo)
- **PV-19** Horario de quirófanos: 07:00–19:00 configurable por quirófano. Las urgencias pueden operarse fuera de horario.
- **PV-20** Cirugías de Cardiología, Nefrología, Neumología y Endocrinología: fuera de alcance por ahora.
- **PV-21** Un paciente solo puede tener **una** ficha interna activa a la vez.
- **PV-22** Qué parte del historial ve el especialista (por ahora, el mismo historial si tiene una ficha o cirugía con el paciente).
- Adulto mayor = **60 años o más** (Ley 369). Pediatría = edad **< 10**.

## 3. Reparto
| Persona | Tarjetas nuevas | Qué le cambia |
|---|---|---|
| **Benjamín** | 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 31, 32 | Toda la funcionalidad nueva (la toma él) + ajustes de sus CU (05, 06, 19, 20) + pruebas |
| **Matías** | 26 | Fecha de nacimiento del paciente; Pediatría < 10; el paciente solo pide las 3 básicas |
| **Alejandro** | 27 | Agenda del médico en Nuxt (el backend de T100-08 no cambia) |
| **Alex** | 28 | Historial del paciente en Nuxt, con fichas internas y cirugías |
| **Abigail** | 29, 30 | Atención en Nuxt; notificaciones nuevas y ticket impreso |

## 4. Calendario (Sprint 6 = solo pruebas)
| Sprint | Fechas | Tarjetas | Puntos |
|---|---|---|---|
| 4 | 6–12 nov | 16 (documentación modelo V), 17 (contratos + BD), 18 (base Nuxt) | 11 |
| 5 | 13–20 nov | 19, 20, 21, 22, 23, 24, 25 · 26 · 27 · 28 · 29, 30 | 51 |
| 6 | 21–28 nov | 31 (integración y concurrencia), 32 (sistema y aceptación, todo el equipo) | 6 |

**Riesgo de carga:** en el Sprint 5 Benjamín suma 35 puntos nuevos más T100-02 y T100-03 (11 puntos). Si alguien termina antes, lo primero que conviene pasarle es **T100-25** (supervisión del administrador) o **T100-22** (agenda del especialista).

## 5. Orden de trabajo y dependencias
```
T100-16 (docs, rama develop) ──────────────────────────────────────────────┐
T100-17 contratos + BD ─┬─> T100-19 catálogo ──> T100-23 quirófano ──┐     │
                        ├─> T100-20 ficha interna ─┬─> T100-21 web    ├─> T100-25 admin
                        │      (usa T100-02/03)    └─> T100-22 especialista ─┘
                        ├─> T100-24 ajuste CU-05/06 (después de T100-02, 03 y 26)
                        └─> T100-26 (Matías) · T100-28 (Alex) · T100-30 (Abigail)
T100-18 base Nuxt ──────> T100-21, 22, 23, 27, 28, 29, 30
Todo lo anterior ───────> T100-31 integración ──> T100-32 aceptación
```
Igual que en T100: **cada tarjeta se programa contra interfaces y se prueba con `Fake*`**, así nadie espera a nadie. Los Fakes nuevos vienen en T100-17.

## 6. Cómo se trabaja (igual que `Issues_T100/LEEME.md`)
1. Cada persona crea `feature/T100-NN-tema` **desde** `feature/T100-algoritmos-ejecucion` y abre el PR **hacia** esa rama.
2. Solo Benjamín integra. Nadie integra su propio PR; el revisor de la tarjeta aprueba.
3. En la rama T100 va **solo código** (Java, SQL, Dart y ahora también Vue/TypeScript en `04_imp/frontend-web`). La documentación (T100-16 y la evidencia de T100-32) va por `develop`.
4. **Modelo en V:** cada tarjeta indica su nivel. Izquierda = requisitos y diseño (T100-16, T100-17). Derecha = pruebas unitarias (en cada tarjeta), de integración (T100-31) y de aceptación (T100-32). La matriz completa queda en `06_proyect/MATRIZ_MODELO_V.md`.
5. **MVC:** backend `controller → model (Service, Entidad, Repository) → infrastructure/pg`. Web Nuxt: `pages` (vista) → `stores` (controlador de interfaz) → `services` (REST) → `models` (tipos).

## 7. Tablero Kanban
Agregar los 17 issues al GitHub Project (vista **Board**) con las mismas columnas del proyecto: `Backlog → Por hacer → En progreso → En revisión (PR) → Hecho`.
- **Límite:** como máximo **2 tarjetas «En progreso» por persona**.
- Una tarjeta pasa a **En revisión** solo con el PR abierto y las pruebas en verde.
- Pasa a **Hecho** cuando Benjamín integra el PR (cerrar el issue a mano: los PR a la rama T100 no lo cierran solos).
- Filtros útiles: `label:modulo:fichas-internas`, `label:modulo:quirofano`, `label:tipo:ajuste`.

## 8. Crear los issues en GitHub
```bash
DRY=1 bash 06_proyect/Issues_T100_FichasInternas/crear_issues.sh      # simulación
bash 06_proyect/Issues_T100_FichasInternas/crear_issues.sh            # los 17
bash 06_proyect/Issues_T100_FichasInternas/crear_issues.sh Matías     # solo los de una persona
```
Antes: `gh auth login`, y completar `USR_BENJAMIN` y `USR_ALEX` en el script. El script **no cambia** las fechas de los milestones (solo comprueba que existan) y omite los issues que ya existen.

## 9. Archivos de esta carpeta
- `T100-16_…md` … `T100-32_…md`: una tarjeta por issue. La primera línea es el título del issue y el resto es el cuerpo.
- `_meta.json`: responsable, revisor, milestone, etiquetas y puntos de cada tarjeta.
- `crear_issues.sh`: crea etiquetas e issues con `gh`.
