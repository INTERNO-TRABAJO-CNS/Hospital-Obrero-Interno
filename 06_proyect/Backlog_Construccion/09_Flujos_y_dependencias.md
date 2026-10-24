# Flujos funcionales y dependencias entre Issues

[Índice](README.md) · [Comportamiento de CU](13_Comportamiento_CU.md) · [Índice de subida](00_Indice_de_subida.md)

## Camino principal de asignación (no equivale a pantallas)

```text
Paciente (Flutter) CU-04 — Solicitar cita
  CON-17 (vista) → CON-13 (solicitud Java)
                          ↓
                CON-12 (CU-05, núcleo Java)
                          ↓
                CON-11 (CU-06, disponibilidad)
                          ↓
                CON-13 (CU-07, confirmación)
                          ↓ transacción confirmada
                CON-14/15 (consultas)
                CON-22 (evento WebSocket) → CON-23 (actualización Flutter)
                CON-24 (notificación, CU-16)
```

**Regla invariante:** el paciente **no elige** médico, fecha, hora o consultorio, y una solicitud nunca equivale por sí misma a cita confirmada. El control de disponibilidad se ejecuta en Java y PostgreSQL protege integridad bajo concurrencia.

## Otros flujos

| Flujo | Dependencias principales | Resultado esperado |
|---|---|---|
| Acceso por roles CU-01/02 | [CON-03](02_Java.md#con-03) → [CON-07](01_Flutter.md#con-07) y [CON-05](02_Java.md#con-05) | Paciente, admisión/enfermería, médico y administrador ven y ejecutan lo autorizado. |
| Registro de paciente CU-03 | [CON-02](03_PostgreSQL.md#con-02) → [CON-06](02_Java.md#con-06) → [CON-08](01_Flutter.md#con-08) | Paciente localizado o creado una vez, sin duplicaciones. |
| Reprogramación CU-10 | [CON-11](02_Java.md#con-11) + [CON-13](02_Java.md#con-13) → [CON-16](02_Java.md#con-16) → [CON-18](01_Flutter.md#con-18) | Nueva combinación validada, con historial. |
| Cancelación CU-11 | [CON-13](02_Java.md#con-13) → [CON-16](02_Java.md#con-16) → [CON-18](01_Flutter.md#con-18) | Estado cancelada y motivo, recursos liberados, historial. |
| Agenda y atención CU-12/13/14/15 | [CON-10](02_Java.md#con-10) → [CON-19](02_Java.md#con-19) → [CON-25](02_Java.md#con-25) → [CON-28](01_Flutter.md#con-28) | Agenda de médico, acceso permitido y registro operativo. |
| Notificación CU-16/17 | [CON-13](02_Java.md#con-13) + [CON-16](02_Java.md#con-16) → [CON-24](02_Java.md#con-24) → [CON-23](01_Flutter.md#con-23) | Notificación solo de cambio confirmado y recordatorio con vigencia. |
| Historial CU-18 | [CON-16](02_Java.md#con-16) → [CON-26](02_Java.md#con-26) → [CON-29](01_Flutter.md#con-29) | Cambios consultables por rol autorizado. |
| Reportes CU-19/20 | [CON-13](02_Java.md#con-13) + [CON-19](02_Java.md#con-19) → [CON-27](02_Java.md#con-27) → [CON-29](01_Flutter.md#con-29) | Reportes e indicadores de datos reales o sintéticos de prueba. |
| Reasignación tras cancelación (**no confirmada**) | [CON-16](02_Java.md#con-16) → [CON-21](02_Java.md#con-21) (**sujeto a D-01**) | No se ejecuta sin aprobación institucional. |

## Dependencias técnicas / ruta crítica

```text
ELA-02/05/07 arquitectura y contratos → CON-01 + CON-04
CON-01 → CON-02 → CON-03 → CON-06
                  └→ CON-10 → CON-11 → CON-12 → CON-13
                                      ↓         └→ CON-16
                                    CON-20           ↓
                                       └──────────── CON-31
CON-13/16 → CON-22 → CON-23 → CON-31
CON-09 + CON-22 → CON-30 → CON-31/32/33/35
```

**Paralelismo:** si el backend aún no está disponible, Flutter puede avanzar con respuestas de API simuladas conforme a los contratos aprobados ELA-05 y sustituirlas al fusionar el issue Java. Nunca sustituir el contrato por una suposición.

**WebSocket:** emite tras commit; REST guarda y consulta. La reasignación propuesta CON-21 no bloquea ni el flujo de citas requerido ni sus eventos.

## Checklist de traspaso entre equipos

- Backend y frontend deben compartir un contrato API versionado; cada cambio se confirma en PR.
- PostgreSQL debe exponer restricciones/procedimientos necesarios antes de pruebas de concurrencia.
- Las pruebas de cada CU cubren su camino feliz y los flujos alternativos documentados en [13_Comportamiento_CU.md](13_Comportamiento_CU.md).
- Finalizar con las condiciones de [Definition of Done del README](README.md#definicion-de-terminado).
