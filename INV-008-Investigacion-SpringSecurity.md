# [INV-008] Investigar Spring Security y autenticación basada en roles #11

Este documento detalla la investigación, análisis y diseño propuesto para la implementación de seguridad en el sistema utilizando Spring Security, JWT y un control de acceso basado en roles (RBAC).

---

## 1. Spring Security
Spring Security es el framework estándar para la seguridad en aplicaciones del ecosistema Spring. Opera a través de una *cadena de filtros (Filter Chain)* que intercepta las peticiones HTTP antes de que lleguen a los controladores.
* *Autenticación:* Identifica la identidad del usuario (¿Quién eres?).
* *Autorización:* Determina los accesos y permisos del usuario (¿Qué puedes hacer?).

## 2. Autenticación mediante JWT (JSON Web Tokens)
Para arquitecturas orientadas a APIs REST sin estado (stateless), se adopta el estándar JWT.
* *Flujo:* El cliente envía credenciales a /auth/login, el servidor valida y retorna un token firmado. El cliente almacena este token y lo envía en el encabezado Authorization: Bearer <TOKEN> en cada petición.
* *Estructura:* Se compone de tres partes codificadas en Base64 y separadas por puntos: Header.Payload.Signature.

## 3. Autorización basada en Roles
Maneja el control de acceso utilizando GrantedAuthority.
* *Roles:* Representan perfiles globales. Por convención estricta en Spring Security, deben llevar el prefijo ROLE_ (ej. ROLE_ADMIN).
* *Privilegios / Permisos:* Acciones atómicas e individuales (ej. READ_PRIVILEGE).

## 4. Protección de Contraseñas
Las contraseñas nunca deben guardarse en texto plano.
* *Algoritmo:* Se utilizará *BCrypt* (BCryptPasswordEncoder).
* *Mecanismo:* Incluye automáticamente un valor aleatorio (salt) único por contraseña antes de aplicar el hash, lo que mitiga ataques de diccionario y rainbow tables.

## 5. Restricciones de Acceso a Endpoints
Se implementarán dos niveles de control en Spring Boot 3.x:
* *Centralizado:* Configurado en la clase SecurityFilterChain usando .requestMatchers().
* *Declarativo:* Mediante anotaciones @PreAuthorize("hasRole('...')") directamente sobre los métodos de los controladores.

## 6. Roles del Sistema
Se identifican tres roles fundamentales:
1. *ADMIN:* Control total del sistema, auditoría y administración de cuentas.
2. *MANAGER:* Permisos operativos avanzados (crear, modificar y analizar datos del negocio).
3. *USER:* Acceso restringido a consultas básicas y operaciones esenciales del perfil propio.

## 7. Matriz de Permisos

| Módulo / Recurso | Acción / Endpoint | Rol: USER | Rol: MANAGER | Rol: ADMIN |
| :--- | :--- | :---: | :---: | :---: |
| *Usuarios* | Ver perfil propio (GET) | ✅ Sí | ✅ Sí | ✅ Sí |
| *Usuarios* | Listar todos los usuarios (GET) | ❌ No | ❌ No | ✅ Sí |
| *Usuarios* | Modificar/Eliminar usuarios (PUT/DELETE) | ❌ No | ❌ No | ✅ Sí |
| *Reportes / Negocio* | Ver consultas y listados (GET) | ✅ Sí | ✅ Sí | ✅ Sí |
| *Reportes / Negocio* | Crear y editar registros (POST/PUT) | ❌ No | ✅ Sí | ✅ Sí |
| *Reportes / Negocio* | Eliminar registros (DELETE) | ❌ No | ❌ No | ✅ Sí |
| *Configuración* | Alterar parámetros del sistema (PATCH) | ❌ No | ❌ No | ✅ Sí |

## 8. Recomendaciones de Seguridad
* *Expiración corta:* Configurar el tiempo de vida (TTL) de los tokens de acceso entre 15 y 30 minutos.
* *Secreto Seguro:* Almacenar la clave de firma del JWT fuera del código utilizando variables de entorno.
* *Fuerza Bruta:* Forzar el uso de TLS/HTTPS en entornos de producción.
* *Menor Privilegio:* Denegar accesos por defecto (.anyRequest().authenticated()) y abrir únicamente los necesarios.
*
