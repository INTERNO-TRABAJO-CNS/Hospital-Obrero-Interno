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
  * Datos de la base: crea `src/main/resources/application-local.properties` (está en `.gitignore`, **nunca** se sube) con `spring.datasource.url=jdbc:postgresql://localhost:5432/<tu_base>`, `spring.datasource.username` y `spring.datasource.password`, o usa las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`. Todos los datos del sistema se guardan en esa base PostgreSQL.
  * Flyway crea y actualiza las tablas al arrancar (`src/main/resources/db/migration`: V1 catálogos, V2 identidad/afiliados, V3 historial y recetas, V4 especialidades internas, V5 citas, V6 sesiones, V7 fichas internas en 20 tablas + avisos al paciente, V8 quirófanos y cirugías sin choques). V8 crea la extensión `btree_gist` (el usuario de la base necesita poder crearla; con `postgres` no hay problema).
  * Administrador inicial: si no hay ningún administrador activo, se crea al arrancar con `CNS_ADMIN_USUARIO` (por defecto `admin`) y `CNS_ADMIN_CLAVE` (sin clave no se crea). Las demás cuentas del personal las crea el administrador (`POST /api/v1/admin/usuarios` o la sección «Cuentas» de la app).
  * Login: `POST /api/v1/auth/login` devuelve un `token`; todas las demás peticiones lo mandan como `Authorization: Bearer <token>` (vence a las 8 h, `CNS_SESION_HORAS`). `POST /api/v1/auth/logout` lo invalida. Tras 5 intentos fallidos se bloquea 15 min (429).
  * Datos de prueba del personal (médicos, especialistas con su horario, consultorios, quirófanos G-1/G-2/H-1/D-1, pacientes y la agenda de hoy): `psql -d <tu_base> -f datos-prueba/datos_prueba_personal.sql` (no es una migración; se puede correr varias veces).
* **App Flutter (única app: paciente, médico, enfermería, administrador y TV):** `cd 04_imp/frontend`.
  * Navegador: `flutter run -d chrome --web-port 5050` (el backend acepta ese origen; para otro puerto, `CNS_WEB_ORIGEN`).
  * Celular o Android TV: ábrelo en Android Studio o usa `flutter run` con el equipo conectado. Desde el emulador el backend es `--dart-define=API_URL=http://10.0.2.2:8080`; desde un celular real, la IP de tu PC (`--dart-define=API_URL=http://192.168.x.x:8080`).
  * Computadora: `flutter run -d windows`.
  * Sin backend: `--dart-define=AUTH_FAKE=true --dart-define=CATALOGO_FAKE=true` (usuarios de prueba en `lib/services/auth_service.dart`; médicos: `medico`, `pediatra`, `odontologo` o `especialista` con la clave `medico123`).
  * Médico y especialista: agenda, atención, historial, ticket interno, cola de prioridad, fichas internas y cirugías usan el backend (todo queda en PostgreSQL). La base impide cualquier choque de quirófano (con su limpieza), cirujano o paciente.
  * Las fichas EN_ESPERA se vuelven a asignar solas cada día hábil a las 06:00 (hora del hospital).
  * Administrador: el catálogo de quirófanos usa el backend. Enfermería, historial de cambios, supervisión y reportes todavía usan datos de prueba (el backend no tiene esos endpoints); la pantalla lo avisa.
  * Imprimir el ticket funciona en la versión web (`flutter build web`); en escritorio y celular falta un plugin de impresión.
  * Pruebas: `flutter test`. Análisis: `dart analyze lib test`.
