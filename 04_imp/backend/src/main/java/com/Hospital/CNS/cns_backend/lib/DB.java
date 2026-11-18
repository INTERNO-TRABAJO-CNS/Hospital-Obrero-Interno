package com.Hospital.CNS.cns_backend.lib;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Unico punto de conexion a PostgreSQL (JDBC), con pool de conexiones y transacciones.
 *
 * <p>Regla de la arquitectura: SOLO las clases de {@code infrastructure.pg} (los {@code Pg*Repository} y
 * {@code PgTransaccion}) usan esta clase. {@code model}, {@code controller} y {@code view} nunca la importan.</p>
 *
 * <p>Uso en un repositorio (la conexion vuelve al pool al cerrarse):</p>
 * <pre>
 * try (Connection conn = DB.getConnection();
 *      PreparedStatement stmt = conn.prepareStatement("SELECT id FROM cita WHERE id = ?")) {
 *     stmt.setLong(1, id);
 *     ...
 * }
 * </pre>
 *
 * <p>Dentro de {@link #enTransaccion(Supplier)} todas las llamadas a {@code getConnection()} del mismo hilo
 * reciben la MISMA conexion (su {@code close()} no la cierra): o se guarda todo, o nada.</p>
 *
 * <p>Con Spring se usa el pool de Spring ({@link #usar(DataSource)}, lo llama ConfiguracionBaseDatos). Sin
 * Spring (pruebas de integracion) se crea un pool propio con, en este orden: variables de entorno
 * {@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD}; el archivo local {@code application-local.properties}
 * (ignorado por Git); y los valores por defecto.</p>
 */
public final class DB {

    static final String URL_DEFECTO = "jdbc:postgresql://localhost:5432/cns_db";
    static final String USUARIO_DEFECTO = "postgres";
    static final String CLAVE_DEFECTO = "postgres";

    private static final String ARCHIVO_LOCAL = "application-local.properties";
    private static final ThreadLocal<Connection> TRANSACCION = new ThreadLocal<>();

    private static volatile DataSource origen;

    private DB() {
    }

    /** Usa el pool de Spring. Lo llama ConfiguracionBaseDatos al arrancar. */
    public static void usar(DataSource dataSource) {
        origen = dataSource;
    }

    /**
     * Conexion del pool (o la de la transaccion en curso). Quien la pide la cierra (try-with-resources).
     *
     * @throws IllegalStateException si no se puede conectar (mensaje claro, sin mostrar la contrasena)
     */
    public static Connection getConnection() {
        Connection enCurso = TRANSACCION.get();
        if (enCurso != null) {
            return sinCerrar(enCurso);
        }
        try {
            return dataSource().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo conectar a PostgreSQL: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta {@code trabajo} en una transaccion: commit si termina bien, rollback si lanza una excepcion.
     * Si ya hay una transaccion en este hilo, se une a ella.
     */
    public static <T> T enTransaccion(Supplier<T> trabajo) {
        if (TRANSACCION.get() != null) {
            return trabajo.get();
        }
        Connection conn;
        try {
            conn = dataSource().getConnection();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo iniciar la transaccion: " + e.getMessage(), e);
        }
        TRANSACCION.set(conn);
        try {
            T resultado = trabajo.get();
            conn.commit();
            return resultado;
        } catch (RuntimeException | Error e) {
            deshacer(conn, e);
            throw e;
        } catch (SQLException e) {
            deshacer(conn, e);
            throw new IllegalStateException("No se pudo confirmar la transaccion: " + e.getMessage(), e);
        } finally {
            TRANSACCION.remove();
            cerrar(conn);
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

    private static DataSource dataSource() {
        DataSource ds = origen;
        if (ds == null) {
            synchronized (DB.class) {
                if (origen == null) {
                    origen = poolPropio();
                }
                ds = origen;
            }
        }
        return ds;
    }

    private static DataSource poolPropio() {
        Properties local = cargarLocal();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(valor(System::getenv, local, "DB_URL", "spring.datasource.url", URL_DEFECTO));
        config.setUsername(valor(System::getenv, local, "DB_USER", "spring.datasource.username", USUARIO_DEFECTO));
        config.setPassword(valor(System::getenv, local, "DB_PASSWORD", "spring.datasource.password", CLAVE_DEFECTO));
        config.setMaximumPoolSize(5);
        config.setPoolName("cns-db");
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }

    /** La conexion de la transaccion, pero con un close() que no la cierra (la cierra enTransaccion). */
    private static Connection sinCerrar(Connection real) {
        return (Connection) Proxy.newProxyInstance(DB.class.getClassLoader(), new Class<?>[] {Connection.class},
                (proxy, metodo, args) -> {
                    if ("close".equals(metodo.getName())) {
                        return null;
                    }
                    if ("isClosed".equals(metodo.getName())) {
                        return real.isClosed();
                    }
                    try {
                        return metodo.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    private static void deshacer(Connection conn, Throwable causa) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            causa.addSuppressed(e);
        }
    }

    private static void cerrar(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignorada) {
            // la conexion vuelve al pool igual; Hikari la reinicia
        }
        try {
            conn.close();
        } catch (SQLException ignorada) {
            // nada que hacer: ya termino la transaccion
        }
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
