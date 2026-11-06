package com.Hospital.CNS.cns_backend.lib;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Function;

/**
 * Unico punto de conexion a PostgreSQL (JDBC).
 *
 * <p>Regla de la arquitectura: SOLO las clases de {@code infrastructure.pg} (los
 * {@code Pg*Repository}) usan esta clase. {@code model}, {@code controller} y {@code view}
 * nunca la importan, asi la base de datos no genera dependencias con los demas componentes.</p>
 *
 * <p>Uso en un repositorio (cada llamada abre una conexion nueva y se cierra sola):</p>
 * <pre>
 * try (Connection conn = DB.getConnection();
 *      PreparedStatement stmt = conn.prepareStatement("SELECT id FROM cita WHERE id = ?")) {
 *     stmt.setLong(1, id);
 *     ...
 * }
 * </pre>
 *
 * <p>Los datos de conexion se toman, en este orden, de: variables de entorno
 * {@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD}; el archivo local
 * {@code application-local.properties} (ignorado por Git); y los valores por defecto.</p>
 */
public final class DB {

    static final String URL_DEFECTO = "jdbc:postgresql://localhost:5432/cns_db";
    static final String USUARIO_DEFECTO = "postgres";
    static final String CLAVE_DEFECTO = "postgres";

    private static final String ARCHIVO_LOCAL = "application-local.properties";

    private DB() {
    }

    /**
     * Abre una conexion nueva. Quien la pide es responsable de cerrarla (try-with-resources).
     *
     * @throws IllegalStateException si falta el driver o no se puede conectar (mensaje claro,
     *                               sin mostrar la contrasena)
     */
    public static Connection getConnection() {
        Properties local = cargarLocal();
        String url = valor(System::getenv, local, "DB_URL", "spring.datasource.url", URL_DEFECTO);
        String usuario = valor(System::getenv, local, "DB_USER", "spring.datasource.username", USUARIO_DEFECTO);
        String clave = valor(System::getenv, local, "DB_PASSWORD", "spring.datasource.password", CLAVE_DEFECTO);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontro el driver JDBC de PostgreSQL (org.postgresql.Driver).", e);
        }
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "No se pudo conectar a PostgreSQL (url=" + url + ", usuario=" + usuario + "): " + e.getMessage(), e);
        }
    }

    /** Prioridad: variable de entorno, luego archivo local, luego valor por defecto. */
    static String valor(Function<String, String> entorno, Properties local,
                        String variable, String clave, String defecto) {
        String delEntorno = entorno.apply(variable);
        if (delEntorno != null && !delEntorno.isBlank()) {
            return delEntorno;
        }
        String delArchivo = local.getProperty(clave);
        if (delArchivo != null && !delArchivo.isBlank()) {
            return delArchivo.trim();
        }
        return defecto;
    }

    private static Properties cargarLocal() {
        Properties propiedades = new Properties();
        try (InputStream in = DB.class.getClassLoader().getResourceAsStream(ARCHIVO_LOCAL)) {
            if (in != null) {
                propiedades.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO_LOCAL, e);
        }
        return propiedades;
    }
}
