# Fuentes oficiales, diagramas y ubicación de los enlaces

[Inicio](README.md) · [Comportamiento de CU](13_Comportamiento_CU.md) · [Cobertura](08_Cobertura_y_trazabilidad.md)

El contenido del backlog se contrastó con los archivos entregados. **No se modifican los documentos del Sprint 1 ni los diagramas de diseño**. Las rutas siguientes son las declaradas por el README de arquitectura y los nombres reales contenidos en los ZIP; conviene comprobar su ubicación en el repositorio antes de publicar el PR.

| Fuente | Ruta esperada desde la raíz del repositorio | Estado |
|---|---|---|
| Documento de Visión | `02_req/archivos-md/Documento_de_Vision_CNS.md` | Se recibió contenido Markdown; confirmar ruta en repo |
| Especificación de Casos de Uso | `02_req/archivos-md/Especificacion_de_Casos_de_Uso_CNS.md` | Se recibió contenido Markdown; confirmar ruta en repo |
| Matriz RF/HU/RNF | `02_req/archivos-md/Matriz_de_Requisitos_y_Trazabilidad_CNS.md` | Se recibió contenido Markdown; confirmar ruta en repo |
| Arquitectura MVC | `03_des/02-arquitecturaGeneral/README.md` **o** `03_des/02_Arquitectura/README.md` | El ZIP solo contiene un README con los futuros archivos previstos; todavía NO contiene `02_mvc.md`, `04_reglas/` ni contratos aprobados |
| Diagramas de comportamiento | `03_des/01-diagramaCompor/` | ZIP contiene README y PUML/PNG/SVG para CU-01 a CU-20 |
| Diagramas de casos de uso | `03_des/DiagCU/` (nombre mostrado en README de arquitectura; ZIP usa `diagramaCU/`) | ZIP contiene CU-00 y CU-01 a CU-20 en PUML/PNG/SVG; confirmar nombre real de carpeta |
| Diseño UI v1 | `03_des/diseñoUI/` o equivalente | ZIP contiene **solo README**, no diseños concretos |
| Backlog Elaboración | `06_proyect/Backlog_Elaboracion/Backlog_Arquitectura_CNS.md` | Nueve issues planeados, sin confirmación GitHub |

## Mapa por caso de uso

Cada fila se corresponde con el nombre exacto del archivo `.puml` recibido. El enlace a diagramas externos **no se fuerza** hasta confirmar si la carpeta es `DiagCU` o `diagramaCU`, para evitar referencias rotas. Los vínculos a comportamiento e issues dentro de `06_proyect` sí se incluyen.

| CU | Comportamiento del caso de uso | Archivo `.puml` del ZIP de CU | Issues principales |
|---|---|---|---|
| <a id="cu-01"></a> CU-01 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-01) | `CU-01_Iniciarsesion.puml` | [CON-03](02_Java.md#con-03) · [CON-07](01_Flutter.md#con-07) |
| <a id="cu-02"></a> CU-02 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-02) | `CU-02_GestionarUsuariosRolesyPermisos.puml` | [CON-05](02_Java.md#con-05) |
| <a id="cu-03"></a> CU-03 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-03) | `CU-03_RegistraryConsultarPacientes.puml` | [CON-06](02_Java.md#con-06) · [CON-08](01_Flutter.md#con-08) |
| <a id="cu-04"></a> CU-04 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-04) | `CU-04_SolicitarCitaMedica.puml` | [CON-13](02_Java.md#con-13) · [CON-17](01_Flutter.md#con-17) |
| <a id="cu-05"></a> CU-05 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-05) | `CU-05_AsignarCitaAutomaticamente.puml` | [CON-10](02_Java.md#con-10) · [CON-12](02_Java.md#con-12) |
| <a id="cu-06"></a> CU-06 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-06) | `CU-06_ValidarDisponibilidaddeRecursos.puml` | [CON-10](02_Java.md#con-10) · [CON-11](02_Java.md#con-11) |
| <a id="cu-07"></a> CU-07 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-07) | `CU-07_RegistrarCitaConfirmada.puml` | [CON-13](02_Java.md#con-13) |
| <a id="cu-08"></a> CU-08 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-08) | `CU-08_ConsultarCitasdelPaciente.puml` | [CON-14](02_Java.md#con-14) · [CON-17](01_Flutter.md#con-17) · [CON-23](01_Flutter.md#con-23) |
| <a id="cu-09"></a> CU-09 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-09) | `CU-09_ConsultarCitasRegistradas.puml` | [CON-15](02_Java.md#con-15) · [CON-18](01_Flutter.md#con-18) |
| <a id="cu-10"></a> CU-10 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-10) | `CU-10_ReprogramarCita.puml` | [CON-16](02_Java.md#con-16) · [CON-18](01_Flutter.md#con-18) |
| <a id="cu-11"></a> CU-11 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-11) | `CU-11_CancelarCita.puml` | [CON-16](02_Java.md#con-16) · [CON-18](01_Flutter.md#con-18) |
| <a id="cu-12"></a> CU-12 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-12) | `CU-12_ActualizarEstadodeCita.puml` | [CON-19](02_Java.md#con-19) · [CON-28](01_Flutter.md#con-28) |
| <a id="cu-13"></a> CU-13 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-13) | `CU-13_ConsultarAgendaMedica.puml` | [CON-10](02_Java.md#con-10) · [CON-19](02_Java.md#con-19) · [CON-28](01_Flutter.md#con-28) |
| <a id="cu-14"></a> CU-14 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-14) | `CU-14_ConsultarInformacionPermitidadelPaciente.puml` | [CON-25](02_Java.md#con-25) · [CON-28](01_Flutter.md#con-28) |
| <a id="cu-15"></a> CU-15 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-15) | `CU-15_RegistrarDatosdeAtencion.puml` | [CON-25](02_Java.md#con-25) · [CON-28](01_Flutter.md#con-28) |
| <a id="cu-16"></a> CU-16 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-16) | `CU-16_GenerarNotificaciondeCita.puml` | [CON-23](01_Flutter.md#con-23) · [CON-24](02_Java.md#con-24) |
| <a id="cu-17"></a> CU-17 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-17) | `CU-17_GenerarRecordatoriodeCita.puml` | [CON-24](02_Java.md#con-24) |
| <a id="cu-18"></a> CU-18 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-18) | `CU-18_ConsultarHistorialdeCambios.puml` | [CON-26](02_Java.md#con-26) · [CON-29](01_Flutter.md#con-29) |
| <a id="cu-19"></a> CU-19 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-19) | `CU-19_GenerarReportesdeCitas.puml` | [CON-27](02_Java.md#con-27) · [CON-29](01_Flutter.md#con-29) |
| <a id="cu-20"></a> CU-20 | [Flujos y alternativas](13_Comportamiento_CU.md#cu-20) | `CU-20_ConsultarIndicadoresdeGestion.puml` | [CON-27](02_Java.md#con-27) · [CON-29](01_Flutter.md#con-29) |

## Cómo corregir rutas externas si cambian los nombres

1. Verificar el nombre físico bajo `03_des` en la rama `develop`.
2. Ajustar la tabla de este documento y los enlaces a fuentes sin renombrar las carpetas oficiales del Sprint 1.
3. Los archivos `01_vision_general.md`, `02_mvc.md`, `03_modelo_dominio.md`, `04_reglas/`, `05_contratos_api.md`, `06_base_de_datos.md`, `07_seguridad_privacidad.md` y `08_decisiones_parametros.md` figuran como **previstos** en la arquitectura, pero no existen dentro del ZIP recibido. Las tareas del Sprint 2 deben elaborarlos y aprobarlos.
4. No tratar este backlog como definición de un endpoint/tabla/fórmula que todavía no existe en los documentos firmados.
