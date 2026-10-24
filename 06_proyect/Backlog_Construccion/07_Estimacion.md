# Estimación, responsables y balance de carga

[Índice](README.md) · [Orden de creación](00_Indice_de_subida.md) · [Flujos](09_Flujos_y_dependencias.md)

Los puntos son **propuestas iniciales**, no horas ni capacidad real de cada integrante. Preservamos las 36 tareas del plan aportado para no romper enlaces `CON-XX`. La asignación del trabajo de construcción no cambia la autoría de RF o la revisión del Sprint 1.

| Talla | Puntos | Interpretación |
|---|---:|---|
| XS | 1 | Muy pequeña |
| S | 2 | Pequeña |
| M | 3 | Media |
| L | 5 | Grande; requiere revisión de carga |
| XL | 8 | Dividir antes de tomar en un Sprint |

## Estimación por issue

| Issue | Sprint | Responsable propuesto | Puntos | Criterio / situación |
|---|---|---|---:|---|
| [CON-01](02_Java.md#con-01) | S3 | Benjamín | 5 | Ver criterios y CP enlazados |
| [CON-02](03_PostgreSQL.md#con-02) | S3 | Alex | 5 | Ver criterios y CP enlazados |
| [CON-03](02_Java.md#con-03) | S3 | Matías | 5 | Ver criterios y CP enlazados |
| [CON-04](01_Flutter.md#con-04) | S3 | Abigal | 5 | Ver criterios y CP enlazados |
| [CON-05](02_Java.md#con-05) | S3 | Matías | 3 | Ver criterios y CP enlazados |
| [CON-06](02_Java.md#con-06) | S3 | Alejandro | 5 | Ver criterios y CP enlazados |
| [CON-07](01_Flutter.md#con-07) | S3 | Abigal | 3 | Ver criterios y CP enlazados |
| [CON-08](01_Flutter.md#con-08) | S3 | Alex | 3 | Ver criterios y CP enlazados |
| [CON-09](05_Pruebas.md#con-09) | S3 | Benjamín | 3 | Ver criterios y CP enlazados |
| [CON-10](02_Java.md#con-10) | S4 | Alex | 5 | Ver criterios y CP enlazados |
| [CON-11](02_Java.md#con-11) | S4 | Benjamín | 5 | Ver criterios y CP enlazados |
| [CON-12](02_Java.md#con-12) | S4 | Benjamín | 5 | Ver criterios y CP enlazados |
| [CON-13](02_Java.md#con-13) | S4 | Alejandro | 5 | Ver criterios y CP enlazados |
| [CON-14](02_Java.md#con-14) | S4 | Alejandro | 3 | Ver criterios y CP enlazados |
| [CON-15](02_Java.md#con-15) | S4 | Matías | 3 | Ver criterios y CP enlazados |
| [CON-16](02_Java.md#con-16) | S4 | Matías | 5 | Ver criterios y CP enlazados |
| [CON-17](01_Flutter.md#con-17) | S4 | Abigal | 5 | Ver criterios y CP enlazados |
| [CON-18](01_Flutter.md#con-18) | S4 | Abigal | 5 | Ver criterios y CP enlazados |
| [CON-19](02_Java.md#con-19) | S4 | Alex | 5 | Ver criterios y CP enlazados |
| [CON-20](05_Pruebas.md#con-20) | S4 | Benjamín | 5 | Ver criterios y CP enlazados |
| [CON-21](02_Java.md#con-21) | S5 | Benjamín | 5 | Condicional: aprobación CNS pendiente |
| [CON-22](04_WebSocket.md#con-22) | S5 | Matías | 5 | Ver criterios y CP enlazados |
| [CON-23](01_Flutter.md#con-23) | S5 | Abigal | 5 | Ver criterios y CP enlazados |
| [CON-24](02_Java.md#con-24) | S5 | Alejandro | 5 | Ver criterios y CP enlazados |
| [CON-25](02_Java.md#con-25) | S5 | Alex | 5 | Ver criterios y CP enlazados |
| [CON-26](02_Java.md#con-26) | S5 | Alejandro | 3 | Ver criterios y CP enlazados |
| [CON-27](02_Java.md#con-27) | S5 | Alex | 5 | Ver criterios y CP enlazados |
| [CON-28](01_Flutter.md#con-28) | S5 | Abigal | 5 | Ver criterios y CP enlazados |
| [CON-29](01_Flutter.md#con-29) | S5 | Abigal | 3 | Ver criterios y CP enlazados |
| [CON-30](06_Servidor.md#con-30) | S5 | Matías | 3 | Ver criterios y CP enlazados |
| [CON-31](05_Pruebas.md#con-31) | S6 | Benjamín | 5 | Ver criterios y CP enlazados |
| [CON-32](05_Pruebas.md#con-32) | S6 | Alex | 5 | Ver criterios y CP enlazados |
| [CON-33](05_Pruebas.md#con-33) | S6 | Matías | 5 | Ver criterios y CP enlazados |
| [CON-34](01_Flutter.md#con-34) | S6 | Abigal | 3 | Ver criterios y CP enlazados |
| [CON-35](06_Servidor.md#con-35) | S6 | Alex | 3 | Ver criterios y CP enlazados |
| [CON-36](05_Pruebas.md#con-36) | S6 | Alejandro | 3 | Ver criterios y CP enlazados |

## Carga sugerida por persona y Sprint

| Persona | Sprint 3 | Sprint 4 | Sprint 5 | Sprint 6 | Total |
|---|---:|---:|---:|---:|---:|
| Abigal | 8 | 10 | 13 | 3 | 34 |
| Alejandro | 5 | 8 | 8 | 3 | 24 |
| Alex | 8 | 10 | 10 | 8 | 36 |
| Benjamín | 8 | 15 | 5 | 5 | 33 |
| Matías | 8 | 8 | 8 | 5 | 29 |
| **Total** | 37 | 51 | 44 | 24 | **156** |

**Riesgo de carga:** revisar especialmente quienes tengan tareas múltiples de tamaño L en el mismo sprint; redistribuir o dividir por PR antes de comprometer fechas.
**Condicional CON-21:** sus puntos son parte del escenario propuesto, **no del mínimo obligatorio** mientras no se apruebe D-01.
**Ningún usuario de GitHub/Assignee se infiere del nombre:** asignar en el repositorio después de comprobar las cuentas reales.
