# MDL-01: Submodelo de Identidad, Roles y Pacientes

- **Milestone:** Sprint 2 - Arquitectura MVC (Vence: 29/10/2026)
- **Responsable:** Alejandro
- **Rama Fuente:** `feature/S2-M01-identidad-pacientes`
- **Labels:** `fase:elaboracion`, `tipo:tarea`, `tec:arquitectura`, `capa:modelo`
- **Trazabilidad:** CU-01, CU-02, CU-03 | RF-01, RF-02, RF-03 | HU-01, HU-02, HU-03 | RNF-01, RNF-02, RNF-05

## 1. Definición de Entidades y Relaciones

Para evitar el acoplamiento y permitir escenarios de negocio donde un paciente no necesariamente tenga credenciales de acceso inmediatas, se separa el concepto de Identidad de Acceso (Usuario) del de Identidad Civil/Clínica (Persona/Paciente).

### Entidades Candidatas y Atributos

*   **Persona** (Estructura Base Civil)
    *   `id_persona` (UUID, PK): Identificador único global de la persona física.
    *   `tipo_documento` (Enum: DNI, RUN, Pasaporte): Tipo de identificación oficial.
    *   `numero_documento` (String, Unique): Número de documento de identidad.
    *   `nombres` (String)
    *   `apellidos` (String)
    *   `fecha_nacimiento` (Date)
    *   `genero` (String)
*   **Usuario** (Capa de Identidad / Autenticación)
    *   `id_usuario` (UUID, PK)
    *   `id_persona` (UUID, FK -> Persona, Unique, Nullable): Permite usuarios que no son pacientes ni médicos (ej. Administradores externos de TI).
    *   `correo_electronico` (String, Unique)
    *   `password_hash` (String)
    *   `estado_cuenta` (Enum: Activo, Suspendido, Bloqueado)
*   **Rol** (Control de Acceso)
    *   `id_rol` (UUID, PK)
    *   `nombre_rol` (String, Unique): Administrador, Admision, Enfermeria, Medico, Paciente.
*   **Permiso** (Granularidad de Acceso)
    *   `id_permiso` (UUID, PK)
    *   `codigo_permiso` (String, Unique): Ej. `paciente:crear`, `paciente:buscar`, `roles:escribir`.
*   **Paciente** (Capa de Dominio Clínico)
    *   `id_paciente` (UUID, PK): **Referencia estable para S2-M03**.
    *   `id_persona` (UUID, FK -> Persona, Unique): Enlace directo con su identidad civil.
    *   `numero_historia_clinica` (String, Unique): Código interno de expediente clínico.

### Tabla de Relaciones y Cardinalidad
*   `Persona` (1) ── (0..1) `Usuario` (Una persona puede o no tener cuenta de acceso al sistema).
*   `Persona` (1) ── (0..1) `Paciente` (Una persona registrada en admisión puede o no tener la condición de paciente activo).
*   `Usuario` (*) ── (*) `Rol` (A través de tabla asociativa `Usuario_Rol`).
*   `Rol` (*) ── (*) `Permiso` (A través de tabla asociativa `Rol_Permiso`).

---

## 2. Operaciones del Submodelo (Contratos Lógicos)

### Autenticación y Autorización (CU-01, CU-02)
*   `autenticarCredenciales(correo, password_plana) -> ResultadoAutenticacion`
    *   *Errores:* `CredencialesIncorrectasException`, `CuentaSuspendidaException`.
*   `obtenerPermisosUsuario(id_usuario) -> Set<CodigoPermiso>`
*   `validarAccesoAccion(id_usuario, codigo_permiso) -> Boolean`

### Gestión de Pacientes y Personas (CU-03)
*   `buscarPersonaPorDocumento(tipo_documento, numero_documento) -> Persona | Null`
*   `registrarPersonaYPaciente(datosPersona, datosPaciente) -> Paciente`
    *   *Límites/Errores:* `DocumentoDuplicadoException` (Lanzado si ya existe el tipo/número de documento para evitar registros duplicados).

---

## 3. Matriz de Control de Acceso y Visibilidad (RBAC)

El acceso se evalúa a nivel de Endpoint (Controlador) mediante tokens y permisos asignados:

| Rol | Permiso Asociado | Acción Autorizada (Endpoint) | Restricción de Visibilidad sobre el Paciente |
| :--- | :--- | :--- | :--- |
| **Administrador** | `roles:escribir`, `usuarios:gestionar` | Modificar roles, suspender usuarios. | **No tiene acceso** a datos clínicos ni de identificación del Paciente. |
| **Admisión / Enf.** | `paciente:crear`, `paciente:buscar` | Buscar por documento, registrar altas. | Datos demográficos y administrativos mínimos (**S2-M05**). No ve evoluciones médicas. |
| **Médico** | `paciente:buscar`, `historia:escribir` | Consultar paciente, añadir registros médicos. | Acceso completo a la ficha médica e historial clínico. |
| **Paciente** | `paciente:auto_consulta` | Consultar sus propios datos. | **Filtrado estricto:** El Modelo rechaza la petición si el `id_persona` del token no coincide con el recurso solicitado. |

---

## 4. Interfaces y Coordinación Inter-Módulos

*   **Con S2-M03 (Gestión de Historias/Consultas):** El submodelo MDL-01 expone el `id_paciente` (UUID) como una clave prima e inmutable. S2-M03 almacenará este `id_paciente` como llave foránea en sus registros clínicos, garantizando que cambios en los datos demográficos no rompan la integridad clínica.
*   **Con S2-M05 (Consultas / Reportes):** Se acuerda que para consultas globales, MDL-01 compartirá un Data Transfer Object (DTO) mínimo denominado `PacienteMinimoDTO` conteniendo únicamente: `id_paciente`, `nombres`, `apellidos`, `tipo_documento` y `numero_documento`. Se omiten datos sensibles como teléfonos, direcciones o correos sin autorización explícita.

---

## 5. Casos de Verificación de Diseño

### Caso A: Intento de Escalamiento de Privilegios (Rol Paciente)
1. Un usuario autenticado con Rol `Paciente` intercepta o genera una petición hacia el endpoint del controlador `UsuarioController@asignarRol`.
2. El middleware de seguridad invoca a `MDL-01::validarAccesoAccion(id_usuario, 'roles:escribir')`.
3. El submodelo evalúa los permisos del Rol del usuario. Al no encontrar el código de permiso requerido, retorna `False`.
4. El controlador detiene la ejecución inmediatamente y responde con un código de estado HTTP `403 Forbidden` (Rechazo de cambio no autorizado).

### Caso B: Registro en Admisión con Identificación Duplicada
1. El personal de Admisión completa el formulario para ingresar a una persona con DNI `12345678`.
2. El controlador invoca a `MDL-01::buscarPersonaPorDocumento('DNI', '12345678')`.
3. El submodelo ejecuta la consulta de existencia y localiza un registro previo asignado a la entidad `Persona`.
4. En lugar de proceder a la inserción, el modelo aborta la operación y eleva una excepción `DocumentoDuplicadoException`.
5. El sistema captura la excepción y redirige el flujo para mostrar en pantalla el perfil de la persona ya existente, impidiendo la duplicidad de identidades civiles en la base de datos.

---

## 6. Pendientes de Validación

> [!WARNING]
> **PENDIENTE DE VALIDACIÓN:** No se ha aprobado el mecanismo de persistencia de sesión ni la estrategia de hashing de contraseñas. Queda delegada a los siguientes Sprints la definición de si se utilizarán tokens JWT (State-less) o Sesiones en Servidor (Stateful), así como el algoritmo criptográfico exacto (Argon2id o bcrypt).
