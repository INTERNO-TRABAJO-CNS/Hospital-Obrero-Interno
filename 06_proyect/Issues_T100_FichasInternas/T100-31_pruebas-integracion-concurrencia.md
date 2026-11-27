# [T100-31][Pruebas][Modelo V] Pruebas de integración y concurrencia de fichas internas y quirófanos

| Campo | Valor |
|---|---|
| Milestone | Sprint 6 - Validacion y entrega (vence 2026-11-28) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-31-pruebas-integracion-concurrencia` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Derecha: integración (verifica el diseño de arquitectura de T100-16/T100-17) |
| Casos de uso | CU-21…CU-25, CU-05, CU-06 |
| Requisitos | RNF-04 · RNF-09 · RF-21…RF-24 |
| Depende de / Bloquea | T100-17…T100-25 integradas / Bloquea: T100-32 |

## Objetivo
Comprobar contra PostgreSQL real que las piezas funcionan juntas y que **nunca** hay choques ni fichas duplicadas, aunque haya pedidos simultáneos.

## Reglas de negocio que debes cumplir
- Cada prueba prepara y limpia sus datos (no depende del orden).
- Los fallos se registran como issue `tipo:bug` enlazado a esta tarjeta.

## Diagramas y documentos de referencia (solo para leer)
- `06_proyect/MATRIZ_MODELO_V.md` *(T100-16)*
- `03_des/02-arquitecturaGeneral/07_base_de_datos.md`

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/integracion/FichasInternasIT.java` | flujo completo: derivar → asignar → atender; desplazamiento; EN_ESPERA → procesar al día hábil |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/integracion/QuirofanoConcurrenciaIT.java` | dos hilos confirman cirugía en el mismo quirófano y horario → una OK y la otra `CHOQUE_DE_HORARIO` |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/integracion/FichasInternasConcurrenciaIT.java` | 10 derivaciones simultáneas para 3 huecos → exactamente 3 ASIGNADAS y 7 EN_ESPERA o desplazadas según prioridad, sin duplicados |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/integracion/OcupacionCruzadaIT.java` | cita común + ficha interna + cirugía del mismo paciente: ningún cruce |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/integracion/ControladoresRolesIT.java` | cada endpoint nuevo con cada rol (200/403) |

## Paso a paso
1. Rama `feature/T100-31-pruebas-integracion-concurrencia`.
2. Escribir las IT.
3. Correr `./mvnw verify` con la BD local.
4. Abrir bugs y coordinar su corrección.
5. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `las 5 clases IT` | en verde 3 corridas seguidas |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-31-pruebas-integracion-concurrencia
# ... trabajar, commits pequeños: feat(T100-31): descripción
git push -u origin feature/T100-31-pruebas-integracion-concurrencia
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
- Pruebas de carga (rendimiento).

## Definition of Done
- [ ] Todas las IT en verde
- [ ] Bugs encontrados registrados y corregidos o aceptados por el equipo
- [ ] PR integrado
- [ ] Tarjeta en **Hecho**

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-31-pruebas-integracion-concurrencia`
