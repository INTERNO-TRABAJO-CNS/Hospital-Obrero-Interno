# Issues T100 — rama `feature/T100-algoritmos-ejecucion`

Creado el 04/11/2026. Son 16 tarjetas (T100-00 … T100-15) con **qué hacer paso a paso, en qué carpeta de GitHub va cada archivo y qué firmas de código deben existir**.

## Cómo se trabaja
1. **Primero T100-00 (Benjamín)**: sincroniza la rama T100 con `develop`, crea los contratos compartidos (`model/compartido`) y la base Flutter. Hasta que esté integrada, los demás pueden leer su issue y preparar su entorno, pero no deben crear su rama.
2. Cada persona crea `feature/T100-NN-tema` **desde** `feature/T100-algoritmos-ejecucion` y abre el PR **hacia** esa rama (nunca hacia `develop` ni `main`).
3. Solo Benjamín integra. Nadie integra su propio PR. El revisor de cada tarjeta aprueba; Benjamín hace el *merge*.
4. En la rama T100 va **solo código funcional** (Java, SQL de migración, Dart). La documentación no se sube ahí.
5. Para no depender de otra persona, cada tarjeta se programa contra **interfaces** y se prueba con `Fake*` en memoria (están en T100-00).

## Reparto
| Persona | CU | Tarjetas |
|---|---|---|
| Benjamín | 02, 05, 06, 07, 19, 20 | T100-00, 01, 02, 03, 04 |
| Matías | 01, 03, 04, 08, 10 | T100-05, 06, 07 |
| Alejandro | 09 (+TV), 12, 13 | T100-08, 09, 10 |
| Alex | 11, 14, 18 | T100-11, 12, 13 |
| Abigail | 15, 16, 17 | T100-14, 15 |

## Calendario
| Sprint | Fechas | Tarjetas |
|---|---|---|
| 4 | 6–12 nov | 00, 01 · 05 · 08 · 12 · 14 |
| 5 | 13–20 nov | 02, 03 · 06 · 09, 10 · 11 · 15 |
| 6 | 21–28 nov | 04 · 07 · 13 + pruebas de extremo a extremo |

## Cómo entregar para la revisión «bien / mal»
Al terminar, enviar a Benjamín: el enlace del PR (o el `git diff` de la rama contra T100) y la salida de `./mvnw test` / `flutter test`. Benjamín lo pasa a Claude, que contrasta contra la tarjeta (reglas, rutas, firmas y pruebas) y responde qué está bien y qué hay que corregir.

## Crear los issues en GitHub
```bash
bash 06_proyect/Issues_T100/crear_issues.sh            # los 16
bash 06_proyect/Issues_T100/crear_issues.sh Matías     # solo los de una persona
DRY=1 bash 06_proyect/Issues_T100/crear_issues.sh      # simulación
```
Antes: `gh auth login`, y completar `USR_BENJAMIN` y `USR_ALEX` en el script.
