CNS - HOSPITAL OBRERO | ARQUITECTURA Y MODELOS PARA EL EQUIPO
================================================================

Este ZIP contiene 11 diagramas basados en los 11 PUML recibidos, cada uno
con imagen PNG, imagen vectorial SVG y copia del PUML original. También
incluye una guía visual adicional (00_Mapa_de_Responsabilidades).

ESTRUCTURA
- 00_Mapa_de_Responsabilidades.png / .svg (guía extra)
- ARQ-01_Componentes/             PNG + SVG + PUML
- ARQ-02_CapasMVC/                PNG + SVG + PUML
- ARQ-03_ModeloDominio/           PNG + SVG + PUML
- ARQ-04_SecuenciaAsignacion/     PNG + SVG + PUML
- ARQ-05_EstadosCita/             PNG + SVG + PUML
- ARQ-06_SecuenciaReasignacion_PENDIENTE/    PNG + SVG + PUML
- MDL-01_Identidad/              PNG + SVG + PUML
- MDL-02_Recursos/               PNG + SVG + PUML
- MDL-03_Citas/                  PNG + SVG + PUML
- MDL-04_Operaciones/            PNG + SVG + PUML
- MDL-05_Comunicacion/           PNG + SVG + PUML

GUIA DE LECTURA
ARQ-01: Quién usa cada componente y cómo se comunican.
ARQ-02: Responsabilidades de Flutter, REST, servicios Java, repositorios y DB.
ARQ-03: Clases por área y responsable; asociaciones y cardinalidades.
ARQ-04: Mensajes durante una asignación de cita.
ARQ-05: Cambios de estado de una cita.
ARQ-06: PROPUESTA PENDIENTE de ofertas y reasignación (NO APROBADA).
MDL-01..05: Modelos pequeños para comprender cada área sin mezclar temas.

REPARTO SEGUN ARQ-03 (04/11/2026)
Benjamín: CU-02, 04, 05, 06, 07 (recursos / cita / motor).
Matías: CU-01, 03, 10 (identidad y reprogramación).
Alejandro: CU-09, 12, 13 (operaciones, asistencia, agenda).
Alex: CU-11, 14, 18 (cancelación, historial y cambios).
Abigail: CU-15, 16, 17 (receta/atención, notificaciones, recordatorios).
El PUML no asigna explícitamente CU-08, CU-19 y CU-20 a una persona.
Este reparto corresponde literalmente al archivo ARQ-03: corroborarlo
con el reparto de trabajo del equipo antes de utilizarlo como decisión final.

ASPECTOS A VALIDAR (NO SE CORRIGEN DE FORMA SILENCIOSA)
1) ARQ-03 indica que el modelo conceptual no es un DER validado.
2) ARQ-06 es un cambio NO APROBADO en requisitos o casos de uso.
3) ARQ-05 afirma que NO hay reasignación automática por ahora.
4) MDL-03 enumera tres estados de cita; ARQ-05 suma ASISTENCIA_CONFIRMADA.
5) MDL-04 contiene DatosAtencion; ARQ-03 declara que se elimina.
6) ARQ-01 deja sin confirmar algunos canales de acceso.

NOTA SOBRE LOS DIAGRAMAS
Se trata de representaciones visuales explicadas, diseñadas a partir del
PUML para facilitar el trabajo por módulo. No son imágenes generadas por el
motor oficial de PlantUML ni reemplazan al código original, conservado en
cada carpeta. Los diagramas no son todos de comportamiento: incluyen
componentes, capas, clases, secuencias y estados.
