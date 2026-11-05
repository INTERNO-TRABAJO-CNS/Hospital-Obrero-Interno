# [T100-05][Java+Flutter][CU-01/03] Iniciar sesión, crear cuenta del titular (valida afiliados) y primera pantalla del paciente

| Campo | Valor |
|---|---|
| Milestone | Sprint 4 - Gestion de citas (vence 2026-11-12) |
| Responsable | Matías |
| Revisor (aprueba) | Alejandro |
| Integra | Benjamín (único con permiso en la rama T100) |
| Rama de trabajo | `feature/T100-05-cu01-cu03-login-pacientes` (desde `feature/T100-algoritmos-ejecucion`) |
| PR hacia | `feature/T100-algoritmos-ejecucion` |
| Puntos | 5 |
| Casos de uso | CU-01, CU-03 |
| Requisitos | RF-01 · RF-03 · RU-03 · RU-04 |
| Depende de / Bloquea | T100-00 / Bloquea: T100-06 |

## Objetivo
Un asegurado **titular** crea su cuenta validando su número de afiliado contra la tabla interna; inicia sesión; el beneficiario se registra con la afiliación del titular + su CI; en el primer registro se crea el historial clínico. Pantalla inicial del paciente en celular.

## Reglas de negocio que debes cumplir
- Solo el titular crea cuenta. Si el afiliado no existe en `identidad.afiliado` → `AFILIADO_NO_ENCONTRADO`.
- Beneficiarios **no** son afiliados: se buscan por afiliación del titular + CI.
- No duplicar pacientes (UNIQUE en PostgreSQL).
- En el primer registro: `HistorialRepository.crearSiNoExiste(pacienteId)`.
- Usuario/contraseña por enfermera: se hará más adelante (`// PENDIENTE PV-18`).
- Reutilizar S3-04..S3-08 y S3-11/12 si ya están en `develop`; **no reescribir lo que ya existe**: adaptar.

## Diagramas de referencia (solo para leer)
- Comportamiento (cada `[Na]` = un test): `03_des/01-diagramaCompor/CU-01_IniciarSesion (1).puml`, `03_des/01-diagramaCompor/CU-03_RegistrarConsultarPacientes.puml`
- Estados: `03_des/diagramasEstado/ED-01_SolicitudCita.puml`, `ED-02_CitaMedica.puml`
- Clases: `03_des/02-arquitecturaGeneral/diagramas/ARQ-03_ModeloDominio.puml`
- Pantallas: Figma `KgJK1swBix6WpC5wfDw2B6` (páginas PHONE, COMPUTER, TV'S)

## Dónde va cada archivo (rutas desde la raíz del repositorio)
| Ruta | Qué debe contener |
|---|---|
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/identidad/Afiliado.java, AfiliadoRepository.java` | tabla interna de afiliados: `existePorNumero`, `titularPorNumero` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/identidad/PacienteService.java` | agregar `registrarTitular(RegistroTitularDTO)`, `registrarBeneficiario(String numeroAfiliadoTitular, String ci, PacienteDTO datos)`, `buscar(...)` |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/model/identidad/AuthService.java` | `login(String usuario, String clave)` (si S3-07 ya lo tiene, solo revisar) |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/infrastructure/pg/PgAfiliadoRepository.java, PgPacienteRepository.java, PgUsuarioRepository.java` |  |
| `04_imp/backend/src/main/java/com/Hospital/CNS/cns_backend/controller/AuthController.java, PacienteController.java` | POST `/api/v1/auth/login`, POST `/api/v1/pacientes/titular`, POST `/api/v1/pacientes/beneficiario`, GET `/api/v1/pacientes` |
| `04_imp/backend/src/main/resources/db/migration/V<siguiente>__afiliados_semilla.sql` | tabla `identidad.afiliado` con 20 afiliados de prueba |
| `04_imp/backend/src/test/java/com/Hospital/CNS/cns_backend/model/identidad/PacienteServiceTest.java, AuthServiceTest.java` | con Fakes |
| `04_imp/frontend/lib/views/auth/login_view.dart, registro_titular_view.dart` | celular y escritorio |
| `04_imp/frontend/lib/views/paciente/home_paciente_view.dart` | **primera pantalla del paciente** (celular): saludo, botón «Solicitar cita», «Mis citas», avisos |
| `04_imp/frontend/lib/services/auth_service.dart, services/paciente_service.dart, controllers/sesion_controller.dart, models/auth/*.dart` |  |

## Contrato (copiar las firmas tal cual)
```java
public class PacienteService {
    public PacienteService(PacienteRepository p, AfiliadoRepository a, HistorialRepository h) {}
    public PacienteDTO registrarTitular(RegistroTitularDTO d);                 // AFILIADO_NO_ENCONTRADO, DATOS_INVALIDOS, YA_REGISTRADO
    public PacienteDTO registrarBeneficiario(String afiliadoTitular, String ci, PacienteDTO d);
    public List<PacienteDTO> buscar(String afiliado, String ci);
}
```

## Paso a paso
1. Rama `feature/T100-05-cu01-cu03`.
2. `git merge origin/develop` en tu rama para traer lo ya hecho en S3 y ver qué falta.
3. Backend: afiliado → servicio → Pg → controller (en ese orden, con tests).
4. Flutter: `login_view`, `registro_titular_view`, `home_paciente_view` usando `FakeAuthService` primero.
5. Un test por `[Na]` de `CU-01_*.puml` y `CU-03_*.puml`.
6. PR a T100.

## Pruebas que deben existir
| Prueba | Qué verifica |
|---|---|
| `registrarTitular_afiliadoInexistente` | → AFILIADO_NO_ENCONTRADO |
| `registrarTitular_yaRegistrado` |  |
| `registrarBeneficiario_porAfiliacionYCI` |  |
| `primerRegistro_creaHistorial` | verifica `FakeHistorialRepository` |
| `login_credencialesInvalidas` |  |
| `widget_home_paciente_celular` | Flutter |

## Cómo crear tu rama y entregar
```bash
git fetch origin
git switch feature/T100-algoritmos-ejecucion
git pull --ff-only origin feature/T100-algoritmos-ejecucion
git switch -c feature/T100-05-cu01-cu03-login-pacientes
# ... trabajar, commits pequeños: feat(T100-05): descripción
git push -u origin feature/T100-05-cu01-cu03-login-pacientes
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
- Credenciales por enfermera.
- Recuperar contraseña.

## Definition of Done
- [ ] Cumple las reglas de negocio y todos los flujos alternativos listados
- [ ] `./mvnw test` en verde (y `flutter test` si hay Flutter)
- [ ] Pantallas probadas en el tamaño que corresponde (celular / escritorio / TV)
- [ ] PR hacia la rama T100 aprobado por el revisor e integrado por Benjamín
- [ ] Enviar a Benjamín el resumen del PR para la revisión de correcto/incorrecto

## Registro
Issue `#__` · PR `#__` · Rama `feature/T100-05-cu01-cu03-login-pacientes`
