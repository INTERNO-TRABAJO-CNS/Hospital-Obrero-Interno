04\_imp - Implementación (Implementation)



Aquí se almacena el código fuente y los componentes desarrollados para el Sistema de Gestión y Control de Citas Médicas del Hospital Obrero de la Caja Nacional de Salud (CNS).



¿Qué archivos deben subirse aquí?



* El código fuente de la aplicación móvil multiplataforma desarrollada en Flutter.
* El código fuente del backend desarrollado en Java con Spring Boot.
* La implementación de los servicios y endpoints de la REST API.
* La implementación de WebSocket para las actualizaciones de citas y disponibilidad en tiempo real.
* Los archivos de configuración necesarios para la ejecución del sistema.
* Los scripts, componentes y módulos desarrollados durante la implementación.




## Cómo levantar cada parte

* **Backend (Spring Boot):** `cd 04_imp/backend && ./mvnw spring-boot:run` (puerto 8080).
  * Datos de la base: copia `src/main/resources/application-local.properties.example` como `application-local.properties` (está en `.gitignore`, **nunca** se sube) o usa las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`.
  * Flyway crea y actualiza las tablas al arrancar (`src/main/resources/db/migration`: V1 catálogos, V2 identidad/afiliados, V3 historial y recetas, V4 especialidades internas, V5 citas, V6 sesiones).
  * Administrador inicial: si no hay ningún administrador activo, se crea al arrancar con `CNS_ADMIN_USUARIO` (por defecto `admin`) y `CNS_ADMIN_CLAVE` (sin clave no se crea). Las demás cuentas del personal las crea el administrador (`POST /api/v1/admin/usuarios` o la sección «Cuentas» de la app).
  * Login: `POST /api/v1/auth/login` devuelve un `token`; todas las demás peticiones lo mandan como `Authorization: Bearer <token>` (vence a las 8 h, `CNS_SESION_HORAS`). `POST /api/v1/auth/logout` lo invalida. Tras 5 intentos fallidos se bloquea 15 min (429).
  * Datos de prueba del personal (médicos, consultorios, horarios, pacientes y la agenda de hoy): `psql -d <tu_base> -f datos-prueba/datos_prueba_personal.sql` (no es una migración; se puede correr varias veces).
* **App Flutter (paciente, enfermería, administrador, TV):** `cd 04_imp/frontend && flutter run`.
  * Sin backend: `--dart-define=AUTH_FAKE=true --dart-define=CATALOGO_FAKE=true` (usuarios de prueba en `lib/services/auth_service.dart`).
  * Enfermería, historial de cambios, quirófanos, supervisión y reportes todavía usan datos de prueba (el backend no tiene esos endpoints); la pantalla lo avisa.
  * Imprimir el ticket funciona en la versión web (`flutter build web`); en escritorio y celular falta un plugin de impresión.
* **Web del médico (Nuxt):** `cd 04_imp/frontend-web && npm install && npm run dev` (http://localhost:3000). URL del backend en `NUXT_PUBLIC_API_URL`; sin backend: `NUXT_PUBLIC_USAR_FAKES=true`. Pruebas: `npm run test`.
