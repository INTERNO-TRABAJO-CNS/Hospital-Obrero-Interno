# [T100-25][Java+Flutter][Ajuste CU-19/20] Supervisión del administrador: las 20 tablas de fichas internas y los quirófanos por separado (solo lectura)

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Benjamín |
| Revisor (aprueba) | Matías |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-25-ajuste-cu19-cu20-supervision-fichas-quirofanos` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Nivel del modelo en V | Diseño detallado → unitarias |
| Casos de uso | CU-19, CU-20 |
| Requisitos | RF-19 · RF-20 · RNF-08 |
| Depende de / Bloquea | T100-17, T100-20, T100-23 / Bloquea: — |

## Objetivo
El administrador revisa cada tabla de fichas internas **por separado** (especialidad × general/pediátrica) y la ocupación de cada quirófano, para que no haya confusión con las citas comunes. Se actualiza en tiempo real.

## Reglas de negocio que debes cumplir
- Solo `ADMINISTRADOR`, solo lectura (`SOLO_LECTURA`, 403).
- Resumen del día por tabla: asignadas, en espera, atendidas, no se presentó, desplazadas.
- Indicadores: fichas por especialidad, % no se presentó, horas de quirófano usadas / horas abiertas, cantidad de desplazamientos.
- El administrador **no** ve la gravedad.
- Tiempo real: `FichaInternaEventosWs` publica en `/ws/fichas-internas`.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/01-diagramaCompor/CU-19_GenerarReportesCitas/CU-19_GenerarReportesCitas.puml` *(lo crea T100-16)*
- `03_des/01-diagramaCompor/CU-20_ConsultarIndicadoresGestion/CU-20_ConsultarIndicadoresGestion.puml` *(lo crea T100-16)*
- Tarjeta `T100-04` (mismo estilo)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/reportes/SupervisionFichasInternasService.java + ResumenTablaFichasDTO.java, OcupacionQuirofanoDTO.java` | `resumen(fecha)`, `tabla(especialidad, pediatrica, fecha)`, `quirofanos(fecha)`, `indicadores(desde, hasta)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/ws/FichaInternaEventosWs.java` | implementa `FichaInternaEventos` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/SupervisionFichasInternasController.java` | `GET /api/v1/admin/fichas-internas/resumen?fecha=`, `GET /api/v1/admin/fichas-internas/{especialidad}?pediatrica=&fecha=`, `GET /api/v1/admin/quirofanos/ocupacion?fecha=`, `GET /api/v1/admin/fichas-internas/indicadores?desde=&hasta=` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/reportes/SupervisionFichasInternasServiceTest.java` |  |
| `04_imp/frontend/lib/views/admin/fichas_internas_view.dart` | selector de especialidad + pestañas General / Pediatría; tabla de fichas del día |
| `04_imp/frontend/lib/views/admin/quirofanos_supervision_view.dart` | línea de tiempo por quirófano (cirugía en color, limpieza en gris) |
| `04_imp/frontend/lib/services/fichas_internas_admin_service.dart, controllers/fichas_internas_admin_controller.dart, models/fichas_internas/*.dart` | con *fake* y `WebSocketChannel` |

## Paso a paso
1. Rama `feature/T100-25-ajuste-cu19-cu20-supervision-fichas-quirofanos`.
2. Service + tests.
3. WebSocket.
4. Pantallas Flutter.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `resumen_porTablaSeparada` |  |
| `tabla_noExponeGravedad` |  |
| `quirofanos_horasUsadasIncluyenLimpieza` |  |
| `controller_rolMedico_403` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-25-ajuste-cu19-cu20-supervision-fichas-quirofanos
# ... trabajar, commits pequeños: feat(T100-25): descripción
git push -u origin feature/T100-25-ajuste-cu19-cu20-supervision-fichas-quirofanos
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

## Fuera de alcance
- Editar fichas o cirugías desde el administrador.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-25-ajuste-cu19-cu20-supervision-fichas-quirofanos`
