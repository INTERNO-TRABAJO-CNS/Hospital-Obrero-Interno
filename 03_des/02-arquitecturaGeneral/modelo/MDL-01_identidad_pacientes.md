# MDL-01 — Identidad, roles y pacientes (Alejandro)
CU-01/02/03 ↔ RF/HU-01/02/03 · RNF-01/02/05. **Borrador de dominio; no código.**

## Entidades propias propuestas
`Persona` (id, ci opcional, identificación), `Paciente` (id, numeroAseguradoCNS obligatorio), `Usuario` (id, personaId, credencial), `Rol`, `Permiso`. Verificar nombres y cardinalidades reales contra DER existente. CI NO reemplaza afiliación.

## Contratos provistos
- Autenticar rol y autorizaciones; el mecanismo técnico permanece sin elegir.
- Consultar/registrar paciente sin duplicar número de asegurado.
- Verificar que actor puede solicitar/consultar cita para su propia afiliación.
- Autoregistro de cuenta por paciente: aprobado por equipo como comportamiento, pero verificar afiliación/identidad es pendiente documental.

## Consumidores
M03 exige `pacienteId` válido. M05 solicita proyección/autorización para lectura. Nadie más mantiene una segunda tabla Paciente.

## Riesgos/pruebas
Duplicado de afiliación; paciente intenta ver información ajena; cuentas registradas sin verificar; médico intenta gestionar roles. No ofrecer integración de afiliación externa sin confirmación.

## Entregables y cotejos
Completar atributos, asociaciones y UML parcial `diagramas/MDL-01_Identidad.puml`; documentar diferencia con CU-03 original (solo admisión) y autorización de auto-registro. Revisar contrato con Benjamín.
