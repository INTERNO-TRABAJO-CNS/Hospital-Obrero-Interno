# 01 · Guía MVC del proyecto (según lo visto en clase de Talleres de Sistemas de Información)

> **Fuente:** pizarrón de la clase (View → Controller → Service/Model → Repository → JDBC) y el ejemplo `almacen` (Producto, ProductoDTO, ProductoRepository, ProductoService, `infrastructure/pg/PgProductoRepository`). Esta guía está leída **de fotos**; si el docente corrige algo, se corrige aquí primero y después en los issues.
> **Alcance:** esto es el MVC **a nivel de clases** (lo que se programa). El MVC **a nivel de sistema** (Flutter = Vista, Spring = Controlador, Java = Modelo) ya está en `03_des/02-arquitecturaGeneral/02_mvc.md` y sigue vigente: esta guía lo baja a paquetes y clases.

## 1. La idea en una imagen

```
  Vista (Flutter)
     │ JSON (DTO) ▲ JSON (DTO)
     ▼            │
  Controller      recibe DTO · llama al Service · devuelve DTO + código HTTP      → controller/
     │ DTO        ▲ DTO / resultado
═════╪════════════╪═══════════════ frontera del MODELO (Java puro) ═══════════════
     ▼            │
  Service         el caso de uso: orquesta; NO sabe de SQL ni de HTTP             → model/<módulo>/
     ├──► Entidad       las reglas del negocio (isValid(), transiciones)          → model/<módulo>/
     ├──► DTO           datos que cruzan capas y resultados de lectura (record)   → model/<módulo>/
     └──► Repository    INTERFAZ: lo que el modelo necesita (store, getById…)     → model/<módulo>/
═════╪══════════════════════ frontera de INFRAESTRUCTURA ═════════════════════════
     ▼ implements
  PgXRepository   JDBC + PreparedStatement; traduce Entidad/DTO ⇄ filas           → infrastructure/pg/
     ▼
  PostgreSQL      un schema por módulo (identidad.paciente, recursos.medico, …)
```

La flecha clave del pizarrón: **el Repository es una interfaz que pertenece al Modelo y la implementación vive afuera**. Por eso el Service se puede probar sin base de datos y por eso la base se puede cambiar sin tocar las reglas.

## 2. Qué clase es qué (ejemplo de clase → nuestro proyecto)

| Capa | En clase (`almacen`) | En el CNS (ejemplo CU-03) | Hace | NO hace |
|---|---|---|---|---|
| Controller | `HomeController` | `PacienteController` | Recibe/valida forma del DTO, llama al Service, elige el código HTTP | SQL, reglas de negocio, crear entidades |
| Service | `ProductoService` | `PacienteService` | Un método por caso de uso: pide id al repo, arma la entidad, valida, guarda, devuelve resultado | Conocer HTTP, JDBC, JSON |
| Entidad | `Producto` (`isValid()`) | `Paciente` | Guarda y protege sus reglas | Salir del modelo hacia el Controller/JSON |
| DTO | `ProductoDTO` | `PacienteDTO` (`record`) | Transporta datos entre capas; resultado de `search` | Tener lógica |
| Repository (interfaz) | `ProductoRepository` | `PacienteRepository` | Declara `nextIdentity`, `store`, `getById`, `search(filtro)` | Tener implementación |
| Repository (impl.) | `PgProductoRepository` | `PgPacienteRepository` | JDBC con `PreparedStatement`, upsert (`on conflict … do update`) | Reglas de negocio |
| Vista | `view/` | Flutter (`04_imp/frontend`) | Pantallas | Hablar con la BD o decidir cupos |

## 3. Paquetes del backend

```
04_imp/backend/src/main/java/com/hospital/cns/
├── CnsApplication.java
├── controller/                 PacienteController, AuthController, …      (REST)
├── model/
│   ├── identidad/              Paciente, PacienteDTO, PacienteRepository, PacienteService, Usuario, Rol, …   (MDL-01)
│   ├── recursos/               Especialidad, Medico, Consultorio, AgendaMedica, …                           (MDL-02)
│   ├── citas/                  SolicitudCita, CitaMedica, CitaService, …                                    (MDL-03)
│   ├── operaciones/            CambioCita, DatosAtencion, OperacionCitaService, …                           (MDL-04)
│   └── comunicacion/           Notificacion, Recordatorio, ReporteService, …                                (MDL-05)
└── infrastructure/
    └── pg/                     PgPacienteRepository, PgUsuarioRepository, …   (JDBC)
```

Un subpaquete de `model` por submodelo `MDL-0X` = **cada persona trabaja en su carpeta** y casi no hay conflictos de merge. (El ejemplo de clase tenía todo plano en `model/` porque era una sola app pequeña; aquí agrupamos por módulo.)

Flutter (misma idea en el cliente): `lib/models` (DTO) · `lib/services` (llamadas REST que devuelven DTO) · `lib/controllers` (estado y validación de formato) · `lib/views` (widgets, sin HTTP).

## 4. Las 10 reglas que revisa quien aprueba el PR

1. El **Controller** no contiene SQL, ni reglas de negocio, ni crea entidades: recibe DTO → llama al Service → devuelve DTO + HTTP.
2. La **Entidad nunca sale del modelo** (ni al Controller ni al JSON).
3. Entidad, DTO y Repository **sin anotaciones** de JPA/Jackson/Spring (el Service puede llevar `@Service`). Los DTO son `record`.
4. Una regla de negocio vive en la **entidad** (`isValid()`, transiciones de estado) o en el **Service**. Nunca en Controller ni en `Pg*`.
5. El **Repository es una interfaz en `model`**; la implementación `Pg*` está en `infrastructure/pg`. El Service recibe la interfaz por constructor.
6. En `Pg*`: `PreparedStatement` con `?` (jamás concatenar valores), `try-with-resources`, logger (no `System.out`/`printStackTrace`), sin reglas. Las lecturas (`search`, reportes) devuelven **DTO directo** (`select`/`count`).
7. El Service **no conoce HTTP**: devuelve un resultado del dominio (enum/DTO) o lanza una excepción de dominio; el `@RestControllerAdvice` la traduce al código HTTP.
8. **Un caso de uso = un método de Service.** El flujo principal es el camino feliz; **cada `[Na]` del diagrama de comportamiento = un resultado + un test.**
9. Pruebas: Service con `Fake*Repository` en memoria (sin BD) · `Pg*` contra PostgreSQL real · Controller con `MockMvc` y Service simulado.
10. Unicidad y concurrencia se garantizan **en PostgreSQL** (`UNIQUE`, `EXCLUDE`, transacción), no solo con un `if` en Java (RNF-04).

## 5. Del Caso de Uso a las capas (cómo leer el diagrama de comportamiento)

Los diagramas están en `03_des/01-diagramaCompor/CU-XX_*.puml` (actividad con carriles; las extensiones llevan etiqueta `[1a]`, `[2a]`, `[3b]`…).

| En el diagrama | Va a… |
|---|---|
| Carril «Aplicación móvil / interfaz / Flutter» | Vista + controller/service Flutter |
| Carril «Sistema CNS (Java)» | `Controller` (entrada/salida) y `Service` (decisiones) |
| Carriles de datos («Registro de pacientes», «Agenda», «Consultorios/citas», «Historial») | `Repository` (interfaz) y `Pg*` |
| Rombo `¿Datos válidos?` | `Entidad.isValid()` o una rama del Service |
| Rombo `¿Rol autorizado?` | Seguridad (Spring Security) + verificación en el Service (RNF-01) |
| Flecha `[Na]` (extensión) | un valor de resultado/excepción del Service + un código HTTP en el Controller + **un test** |
| Texto «Los elementos marcados como pendientes NO son reglas implementadas» | No inventar la regla: dejar `// PENDIENTE PV-xx` y avisar en el PR |

**Criterio de aceptación (HU/CA)** = lo que la persona que revisa verifica en la demo; **cada CA se convierte en un caso de prueba** (ver V más abajo).

## 6. Dónde vamos un paso más allá del ejemplo de clase

Lo que se ve en pantalla en clase está a medio camino; para el proyecto exigimos además:
- Nada de `catch (Exception e) { System.out.println(...); e.printStackTrace(); }`: se registra con logger y se devuelve un resultado de error claro.
- `Connection`, `PreparedStatement` y `ResultSet` se cierran (`try-with-resources`).
- `nextIdentity()` implementado con una secuencia de PostgreSQL (no vacío).
- `store` no devuelve solo `boolean` si hay más de una razón de fallo: el Service distingue «duplicado» de «error técnico».

## 7. Antes / después

**Antes (código espagueti: todo en el controlador)**
```java
@PostMapping("/pacientes")
public String registrar(@RequestParam String num, @RequestParam String nombre) throws Exception {
    Connection c = DriverManager.getConnection(URL, "postgres", "1234");              // credencial en el código
    ResultSet rs = c.createStatement().executeQuery("select * from paciente where num='" + num + "'"); // SQL inyectable
    if (rs.next()) return "ya existe";                                                 // regla + SQL + HTTP mezclados
    c.createStatement().executeUpdate("insert into paciente values('" + num + "','" + nombre + "')");
    return "ok";
}
```

**Después (MVC)**
```java
// controller/PacienteController.java
@PostMapping("/api/v1/pacientes")
ResponseEntity<PacienteDTO> registrar(@RequestBody PacienteDTO datos) {
    RegistroPacienteResultado r = pacienteService.registrar(datos);   // solo traduce resultado → HTTP
    return switch (r.estado()) { case CREADO -> created(r.paciente()); case YA_EXISTIA -> ok(r.paciente()); case DATOS_INVALIDOS -> unprocessable(); };
}
// model/identidad/PacienteService.java  → regla del caso de uso; pide id al repo, arma Paciente, p.isValid(), repo.store(p)
// infrastructure/pg/PgPacienteRepository.java → insert ... on conflict ..., PreparedStatement con ?
```

## 8. V-model: cada nivel de diseño tiene su nivel de prueba

| Lado izquierdo (se escribe antes) | Lado derecho (se verifica después) | Dónde queda en el repo |
|---|---|---|
| Requisitos y HU/CA (`02_req`) | **Pruebas de aceptación**: cada CA «Dado/Cuando/Entonces» = 1+ caso de prueba | `05_pro/` (catálogo `CP-CNS-xx`) |
| Arquitectura / contratos (`03_des`) | **Pruebas de integración**: Controller + Service + `Pg*` + PostgreSQL | `src/test/.../*IT.java` |
| Diseño detallado (clases de esta guía) | **Pruebas unitarias**: entidad y Service con `Fake*Repository` | `src/test/.../*Test.java` |
| Implementación | — | `04_imp/` |

Regla del equipo: **quien implementa un CU no es quien escribe/ejecuta sus pruebas de aceptación** (independencia propia del modelo en V). Los códigos de prueba reutilizan el catálogo existente `06_proyect/Backlog_Construccion/12_Casos_de_prueba.md` (`CP-CNS-06A`, …).

## 9. Kanban: reglas del tablero

- Columnas (las del repo): `Backlog → Por hacer → En progreso → En revisión (PR) → Hecho`.
- **Límite de trabajo en curso: 2 tarjetas por persona** en «En progreso». Si estás bloqueado, etiqueta `estado:bloqueado` y avisa; no abras una tercera.
- **Definition of Ready** (una tarjeta no pasa a «Por hacer» sin esto): CU, RF/HU, criterio de aceptación, diagrama de comportamiento, contrato de clases, responsable, revisor, dependencias.
- **Tarjeta pequeña:** ≤ 2 días de trabajo y PR ≤ ~400 líneas. Si no cabe, se parte.
- Se mergea solo con un PR aprobado por otra persona; **nadie mergea su propio PR**.

## 10. Checklist del revisor (copiar en el comentario del PR)

- [ ] Las 10 reglas de la sección 4 se cumplen (Controller sin SQL, entidad no sale del modelo, repo = interfaz + `Pg*`, …).
- [ ] Cada flujo alternativo `[Na]` del diagrama tiene su resultado y su test.
- [ ] El criterio de aceptación de la HU se puede demostrar (ejecuté el endpoint/pantalla).
- [ ] Hay pruebas y pasan; no se borraron pruebas para pasar.
- [ ] Sin secretos ni datos reales; `TRAZABILIDAD.md` actualizado.
