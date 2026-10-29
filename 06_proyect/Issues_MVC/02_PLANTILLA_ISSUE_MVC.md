# 02 · Plantilla de issue MVC (copiar al crear un issue nuevo)

> Reemplaza el uso suelto de `06_proyect/Plantillas/ISSUE_CNS.md` para tareas de implementación. Una tarjeta **no pasa a «Por hacer»** si le falta alguna sección marcada (DoR).

```markdown
# [S?-NN][Java|Flutter|PostgreSQL|Pruebas][CU-XX] Verbo + qué + dónde

| Campo | Valor |
|---|---|
| Milestone | Sprint N - Nombre (vence AAAA-MM-DD) |
| Responsable | nombre (`usuario-github`) |
| Revisor | otra persona |
| Rama (desde `develop`) | `feature/S?-NN-cuXX-tema-corto` |
| Labels | `fase:construccion` · `tipo:tarea` · `tec:java` · `capa:model|infra|controller|flutter` · `caso-uso:CU-XX` |
| Puntos | N (de CON-XX = M) |
| Depende de / Bloquea | S?-NN / S?-NN |

## Objetivo
Una o dos frases: qué se puede hacer cuando esto termine.

## Trazabilidad  (obligatorio)
RF-XX · HU-XX · CU-XX · RNF-XX · CON-XX (backlog) · diagrama `03_des/01-diagramaCompor/CU-XX_*.puml`

## Criterio de aceptación de la HU  (obligatorio, copiado de la matriz)
> HU-XX: Como … quiero … para …
> CA: Dado que … cuando … entonces …

## Diagrama de comportamiento → dónde se implementa cada paso  (obligatorio)
| Paso del diagrama | Capa / clase donde se implementa | Test |
|---|---|---|
| Camino principal | `XService.metodo()` | `XServiceTest.camino_principal` |
| `[1a]` texto del diagrama | resultado `XXX` → HTTP `4xx` | `XServiceTest.alternativa_1a` |

## Contrato de clases  (obligatorio)
Firmas de las clases/métodos que deben existir (sin implementar). Paquetes según `01_GUIA_MVC.md`.

## Tareas
- [ ] Entidad / DTO / interfaz Repository
- [ ] Service + `Fake*Repository` + pruebas unitarias
- [ ] `Pg*Repository` + prueba de integración   (o Controller + MockMvc)
- [ ] Casos de prueba de aceptación escritos desde el CA (`CP-CNS-xx`)

## Fuera de alcance
Lo que NO se hace en esta tarjeta (evita crecer sin avisar).

## Definition of Done
- [ ] Cumple el CA y los flujos alternativos del diagrama
- [ ] Reglas de capa de la guía (revisor las marca)
- [ ] Pruebas en verde · PR a `develop` · aprobado por otra persona
- [ ] `TRAZABILIDAD.md` actualizada · tarjeta movida en el tablero

## Registro
Issue `#__` · PR `#__` · CP `__` · evidencia `05_pro/__`
```
