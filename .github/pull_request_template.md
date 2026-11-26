## Qué hace este PR

<!-- Una o dos líneas. -->

- Issue: #
- Rama base: `feature/T100-algoritmos-ejecucion` (no `develop` ni `main`, salvo que se indique)
- Casos de uso / requisitos: CU-__ · RF-__

## Cómo se probó

- [ ] `./mvnw test` en verde (pegar el resumen o captura)
- [ ] `flutter test` y `flutter analyze` en verde (si hay Flutter)
- [ ] Pantallas revisadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] El check **CI** de GitHub está en verde

## Checklist del revisor (reglas de código)

- [ ] El Controller no tiene SQL ni reglas de negocio; el usuario sale del token (`ResolutorUsuario`/`Permisos`), nunca del body.
- [ ] La Entidad no sale del modelo; los DTO son `record`.
- [ ] Repository = interfaz en `model`; implementación `Pg*` en `infrastructure/pg`.
- [ ] `PreparedStatement` con `?`, `try-with-resources` y logger (nada de `System.out` ni `printStackTrace`).
- [ ] Un caso de uso = un método de Service; cada flujo alternativo `[Na]` del diagrama tiene su resultado/excepción y su test.
- [ ] Service probado con `Fake*` (sin BD); `Pg*` contra PostgreSQL (IT); Controller con MockMvc.
- [ ] No se cambiaron contratos de `model/compartido` sin avisar a Benjamín.
- [ ] Solo código y migraciones: sin `.md` de entrega, capturas, secretos ni datos reales de pacientes.

## Notas para quien integra

<!-- Migraciones nuevas, cambios de configuración, pasos manuales... -->
