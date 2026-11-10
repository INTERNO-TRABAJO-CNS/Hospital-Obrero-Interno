# [T100-18][Nuxt+Java][Transversal] Base de la web del médico en Nuxt (Vue) y CORS en el backend

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Benjamín |
| Revisor (aprueba) | Alex |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-18-base-nuxt-web-medico` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 3 |
| Nivel del modelo en V | Diseño de arquitectura (se verifica en T100-31) |
| Casos de uso | transversal (CU-13, 14, 15, 21, 23, 24, 25 se muestran aquí) |
| Requisitos | RNF-07 · RNF-05 |
| Depende de / Bloquea | T100-00 / Bloquea: T100-21, T100-22, T100-23, T100-27, T100-28, T100-29, T100-30 |

## Objetivo
Crear el proyecto **Nuxt** (Vue 3) que usarán **solo los médicos** (general, pediatría, odontología y especialistas). El paciente, enfermería, el administrador y la TV siguen en Flutter. Dejar el cliente REST, la sesión temporal, el menú por tipo de médico y el CORS del backend.

## Reglas de negocio que debes cumplir
- Solo rol `MEDICO`; cualquier otro rol ve `/sin-acceso`.
- Menú según el médico: **General / Pediatría** → Agenda, Atención, Historial, Ticket interno, Cola de prioridad. **Odontología** → Agenda, Atención, Historial. **Especialista** → Agenda de fichas internas y, si opera, Cirugías.
- Mientras no exista Spring Security (S3-08), la sesión usa los headers temporales `X-Usuario-Id`, `X-Rol`, `X-Persona-Id` igual que `api_client.dart`. Cuando T100-05 publique `POST /api/v1/auth/login`, el login de Nuxt lo usa.
- Errores del backend `{codigo, mensaje}` → `ApiError` y se muestran tal cual.
- Diseño para computadora (consultorio) y que no se rompa en tablet.

## Diagramas y documentos de referencia (solo para leer)
- `03_des/02-arquitecturaGeneral/02_mvc.md`
- `04_imp/frontend/lib/core/api_client.dart` (mismo comportamiento)
- Figma `KgJK1swBix6WpC5wfDw2B6`, página COMPUTER

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/frontend-web/ (proyecto)` | `npx nuxi@3 init 04_imp/frontend-web`; `ssr: false` (aplicación interna); módulos `@pinia/nuxt`; dev: `vitest`, `@vue/test-utils`, `@nuxt/test-utils`, `happy-dom`. `package.json` con `dev`, `build`, `test` |
| `04_imp/frontend-web/nuxt.config.ts` | `runtimeConfig.public.apiUrl` (por defecto `http://localhost:8080`) |
| `04_imp/frontend-web/services/apiClient.ts` | `get/post/put` con headers temporales; error → `ApiError(codigo, mensaje)` |
| `04_imp/frontend-web/services/medicoService.ts + services/fake/fakeMedicoService.ts` | `yo()` → `GET /api/v1/medico/yo` |
| `04_imp/frontend-web/stores/sesion.ts` | usuario, médico actual (`puedeDerivar`, `operaEnQuirofano`), `entrar()`, `salir()` |
| `04_imp/frontend-web/models/medico.ts, models/especialidad.ts` | tipos y nombres en español de las 13 especialidades |
| `04_imp/frontend-web/middleware/auth.global.ts` | sin sesión → `/login`; rol ≠ MEDICO → `/sin-acceso` |
| `04_imp/frontend-web/layouts/default.vue, pages/index.vue, pages/login.vue, pages/sin-acceso.vue` | marco con menú lateral según el tipo de médico |
| `04_imp/frontend-web/tests/apiClient.test.ts, tests/sesion.test.ts` | Vitest |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/config/ConfiguracionCors.java` | permite el origen `cns.web.origen` (por defecto `http://localhost:3000`), métodos GET/POST/PUT/DELETE y headers `X-*` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/MedicoActualController.java + model/recursos/MedicoActualDTO.java` | `GET /api/v1/medico/yo` → `{medicoId, nombre, especialidad, puedeDerivar, operaEnQuirofano}` (usa `MedicoRepository` de T100-01) |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/controller/MedicoActualControllerTest.java` | MockMvc: 403 si no es MEDICO |
| `04_imp/backend/src/main/resources/application.properties` | `cns.web.origen=http://localhost:3000` |

## Contrato (copiar las firmas tal cual)
```ts
// services/apiClient.ts
export class ApiError extends Error { constructor(public codigo: string, mensaje: string, public status: number) { super(mensaje) } }
export function useApi(): {
  get<T>(ruta: string): Promise<T>
  post<T>(ruta: string, cuerpo?: unknown): Promise<T>
  put<T>(ruta: string, cuerpo?: unknown): Promise<T>
}
// models/medico.ts
export interface MedicoActual { medicoId: number; nombre: string; especialidad: Especialidad; puedeDerivar: boolean; operaEnQuirofano: boolean }
```

## Paso a paso
1. Rama `feature/T100-18-base-nuxt-web-medico`.
2. Crear el proyecto y la estructura `pages/ stores/ services/ models/ middleware/ layouts/ tests/`.
3. `apiClient`, `sesion`, `middleware`, `layout` y páginas base.
4. Backend: `ConfiguracionCors` y `GET /api/v1/medico/yo`.
5. Probar: `npm run dev` + backend local; entrar como médico general, pediatra, odontólogo y especialista y ver que el menú cambia.
6. Agregar a `04_imp/README.md` cómo levantar la web (`cd 04_imp/frontend-web && npm install && npm run dev`) — es parte del código, no documentación de diseño.
7. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `apiClient.test.ts` | envía los 3 headers; un 409 con `{codigo}` lanza `ApiError` con ese código |
| `sesion.test.ts` | menú de General incluye «Ticket interno»; Odontología no; Especialista ve «Cirugías» solo si opera |
| `MedicoActualControllerTest` | 200 para MEDICO; 403 para PACIENTE |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-18-base-nuxt-web-medico
# ... trabajar, commits pequeños: feat(T100-18): descripción
git push -u origin feature/T100-18-base-nuxt-web-medico
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
- Pantallas de cada caso de uso (tarjetas T100-21…23 y T100-27…30).
- Seguridad real (S3-08).

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` / `npm run test` si la tarjeta toca Flutter / Nuxt)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Tarjeta movida a **Hecho** en el tablero Kanban y resumen del PR enviado a Benjamín

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-18-base-nuxt-web-medico`
