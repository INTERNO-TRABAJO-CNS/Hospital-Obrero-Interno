# [S2-M01][Arquitectura MVC] Modelo de identidad, roles y pacientes

**Issue de diseño / Elaboración (no programar).**

| Campo | Valor |
|---|---|
| Milestone | `Sprint 2 - Arquitectura MVC` (vence 29/10/2026) |
| Responsable propuesto | Alejandro |
| Rama (desde `develop`) | `feature/S2-M01-identidad-pacientes` |
| Labels sugeridos | `fase:elaboracion` · `tipo:tarea` · `tec:arquitectura` · `capa:modelo` |
| CU | CU-01, CU-02, CU-03 |
| RF | RF-01, RF-02, RF-03 |
| HU | HU-01, HU-02, HU-03 |
| RNF | RNF-01, RNF-02, RNF-05 |
| Dependencias | Sin bloqueo inicial; coordinar interfaces con S2-M03 y S2-M05. |

**Lectura obligatoria antes de usar IA:** [Contexto compartido](CONTEXTO_COMPARTIDO.md) y los archivos fuente del Sprint 1 allí citados. Los códigos `S2-M01` identifican **esta propuesta**, no números de Issues GitHub creados.

Definir el **submodelo de identidad y pacientes**, que será consumido por las demás piezas del CNS. No desarrollar pantallas ni backend en este sprint.

### Cobertura que debe analizarse
- **CU-01**: iniciar sesión; credenciales válidas/incorrectas y funciones de acuerdo al rol.
- **CU-02**: el administrador gestiona usuarios, roles y permisos; rechazo de cambios no autorizados o inválidos.
- **CU-03**: admisión/enfermería consulta o registra pacientes sin duplicar identificación.

### Actividades
- [ ] Leer las fichas completas, escenario principal y extensiones de CU-01 a CU-03 y sus HU/RF.
- [ ] Definir las entidades candidatas **Usuario, Rol, Permiso, Paciente/Persona** con nombres, atributos esenciales, identificadores y relaciones; no asumir que Paciente y Usuario siempre sean el mismo objeto.
- [ ] Registrar reglas de acceso y visibilidad de datos por paciente, admisión, médico y administrador; diferenciar ver una pantalla de autorizar el endpoint.
- [ ] Acordar con S2-M03 la **referencia estable** de paciente y con S2-M05 la información mínima que pueden mostrar las consultas.
- [ ] Listar operaciones necesarias (autenticar, consultar permisos, buscar paciente, registrar paciente), errores y límites, **sin escoger todavía una forma de autenticación no aprobada**.
- [ ] Preparar modelo de clases parcial y casos de verificación de permisos y duplicidad.

### Criterios de aceptación
- [ ] Cada CU tiene flujo normal y al menos su alternativa documentada con referencias verificables.
- [ ] Está definida la propiedad de los datos de identidad/paciente y cómo la consultan otros módulos, sin duplicarlos.
- [ ] La propuesta impide que usuario no autorizado vea datos ajenos y contempla identificación duplicada.
- [ ] Se entrega `03_des/02-arquitecturaGeneral/modelo/MDL-01_identidad_pacientes.md` y `diagramas/MDL-01_identidad_pacientes.puml`.
- [ ] Las decisiones no documentadas de autenticación están marcadas **PENDIENTE DE VALIDACIÓN**.

### Verificación de diseño
- [ ] Caso A: un usuario con rol Paciente intenta administrar roles y se rechaza.
- [ ] Caso B: admisión intenta registrar por segunda vez la misma identificación y no aparece duplicado.

**Trabajo coordinado (leer para evitar contradicciones):** [S2-M03](S2-M03_nucleo-citas.md) · [S2-M05](S2-M05_consultas-comunicacion-ui.md) · [Mapa de interfaces](MAPA_INTERFACES.md)

### Integración y entrega
- [ ] Referenciar el issue real en el Pull Request y solicitar revisión cruzada de otro integrante.
- [ ] Hacer PR `feature/S2-M01-identidad-pacientes` → `develop` con documentación y diagramas (sin código de producción).
- [ ] Actualizar el índice con el número `#...` **real** después de crear la tarea en GitHub.
