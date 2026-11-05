# [T100-15][Java+Flutter][CU-16/17] Notificaciones en la app (WebSocket) y aviso previo a T-45

| Campo | Valor |
|---|---|
| Milestone | Sprint 5 - Integracion y seguimiento (vence 2026-11-20) |
| Responsable | Abigail |
| Revisor (aprueba) | Matías |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-15-cu16-cu17-notificaciones-aviso` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | CU-16, CU-17 |
| Requisitos | RF-16 · RF-17 · RNF-08 |
| Depende de / Bloquea | T100-00 / Bloquea: — |

## Objetivo
El paciente recibe en la app una notificación cuando su cita se registra o se cancela, y un aviso **45 minutos antes** de la cita si aún no se confirmó su asistencia.

## Reglas de negocio que debes cumplir
- Notificar solo después de que la operación quedó confirmada (RNF-03).
- Aviso T-45: citas PROGRAMADAS cuya hora − ahora ≤ 45 min y sin aviso previo; mensaje: «Su cita será cancelada si no confirma su asistencia en el hospital hasta 30 min antes».
- No repetir el aviso (marcar `avisoEnviado`).
- Canal: aplicación + WebSocket `/ws/notificaciones`.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-16_GenerarNotificacionCita.puml`, `03_des/01-diagramaCompor/CU-17_GenerarRecordatorioCita.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `KgJK1swBix6WpC5wfDw2B6` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/comunicacion/NotificacionService.java` | implementa `NotificacionPublicador`: guarda y difunde |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/comunicacion/AvisoPrevioService.java` | `generarAvisosT45()` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/comunicacion/NotificacionRepository.java` | interfaz: `store`, `delPaciente`, `marcarAvisoEnviado(citaId)`, `avisoYaEnviado(citaId)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgNotificacionRepository.java` |  |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/ws/NotificacionesWs.java` | WebSocket por paciente |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/planificador/AvisoT45.java` | `@Scheduled(fixedDelay = 60000)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/NotificacionController.java` | GET `/api/v1/notificaciones` (las del paciente autenticado) |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__notificacion.sql` |  |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/comunicacion/NotificacionServiceTest.java, AvisoPrevioServiceTest.java` | con `RelojFijo` |
| `04_imp/frontend/lib/views/paciente/notificaciones_view.dart` | celular: lista + insignia en el menú |
| `04_imp/frontend/lib/services/notificaciones_service.dart, controllers/notificaciones_controller.dart` | `WebSocketChannel`; mostrar *snackbar* al llegar |

## Contrato (copiar las firmas tal cual)
```java
public class NotificacionService implements NotificacionPublicador {
    public NotificacionService(NotificacionRepository r, NotificacionesWs ws) {}
    @Override public void publicar(NotificacionDTO n);
}
public class AvisoPrevioService {
    public AvisoPrevioService(CitaRepository c, NotificacionPublicador n, NotificacionRepository r, Reloj reloj) {}
    public int generarAvisosT45();
}
```

## Paso a paso
1. Rama `feature/T100-15-cu16-cu17`.
2. Services + tests (`[1a]`, `[2a]` de `CU-16_*.puml` y `CU-17_*.puml`).
3. Repositorio Pg, migración, WebSocket, planificador.
4. Pantalla Flutter con `FakeNotificacionesService`.
5. Parte CU-17 puede cerrarse en Sprint 6 si falta tiempo (avisar a Benjamín).
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `publicar_guardaYDifunde` |  |
| `aviso_aT45_genera` |  |
| `aviso_aT46_noGenera` |  |
| `aviso_noSeRepite` |  |
| `aviso_citaConfirmada_noGenera` |  |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-15-cu16-cu17-notificaciones-aviso
# ... trabajar, commits pequeños: feat(T100-15): descripción
git push -u origin feature/T100-15-cu16-cu17-notificaciones-aviso
```
Abrir el Pull Request con **base = `feature/T100-algoritmos-ejecucion`** (no `develop`, no `main`). Solo Benjamín integra; nadie integra su propio PR.

## Reglas de código (las revisa quien aprueba el PR)
- Controller sin SQL ni reglas; la Entidad no sale del modelo; DTO = `record`; Repository = interfaz en `model`, implementación `Pg*` en `infrastructure/pg`.
- `PreparedStatement` con `?`, `try-with-resources`, logger (nada de `System.out` ni `printStackTrace`).
- Un caso de uso = un método de Service. Cada flujo alternativo `[Na]` del diagrama = un resultado/excepción + un test.
- Service probado con `Fake*` (sin BD); `Pg*` contra PostgreSQL; Controller con MockMvc.
- **En el PR solo código y migraciones** (nada de `.md`, capturas ni documentación). Cambios fuera de las carpetas de la tabla: pedirlos antes a Benjamín.
- Si necesitas cambiar un contrato de `model/compartido`, **no lo cambies**: abre un comentario en este issue y avisa a Benjamín.

## Fuera de alcance
- Correo electrónico, SMS o push externo.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-15-cu16-cu17-notificaciones-aviso`
