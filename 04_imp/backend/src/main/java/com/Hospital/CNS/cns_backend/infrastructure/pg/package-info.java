/**
 * Implementaciones PostgreSQL (JDBC) de las interfaces de {@code model.compartido}.
 *
 * <p>Aqui van los {@code PgCitaRepository}, {@code PgSolicitudRepository}, etc. (tarjetas de cada
 * modulo). Reglas: implementan la interfaz del model, usan {@code PreparedStatement} con
 * try-with-resources y obtienen la conexion SOLO con {@code lib.DB.getConnection()}.</p>
 */
package com.Hospital.CNS.cns_backend.infrastructure.pg;
