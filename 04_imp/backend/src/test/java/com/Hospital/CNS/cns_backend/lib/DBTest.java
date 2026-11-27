package com.Hospital.CNS.cns_backend.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

class DBTest {
    @Test
    void usaElEntornoPrimero() {
        Properties local = new Properties();
        local.setProperty("spring.datasource.password", "delArchivo");
        String clave = DB.valor(Map.of("DB_PASSWORD", "delEntorno")::get, local,
                "DB_PASSWORD", "spring.datasource.password", DB.CLAVE_DEFECTO);
        assertEquals("delEntorno", clave);
    }

    @Test
    void usaElArchivoLocalSiNoHayEntorno() {
        Properties local = new Properties();
        local.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/CNS_prueba1");
        String url = DB.valor(Map.<String, String>of()::get, local,
                "DB_URL", "spring.datasource.url", DB.URL_DEFECTO);
        assertEquals("jdbc:postgresql://localhost:5432/CNS_prueba1", url);
    }

    @Test
    void usaElDefectoSiNoHayNada() {
        String usuario = DB.valor(Map.<String, String>of()::get, new Properties(),
                "DB_USER", "spring.datasource.username", DB.USUARIO_DEFECTO);
        assertEquals("postgres", usuario);
    }

    @Test
    void ignoraValoresVacios() {
        Properties local = new Properties();
        local.setProperty("spring.datasource.username", "   ");
        String usuario = DB.valor(Map.of("DB_USER", "")::get, local,
                "DB_USER", "spring.datasource.username", DB.USUARIO_DEFECTO);
        assertEquals("postgres", usuario);
    }

    @Test
    void ejecutaAccionesPosterioresSoloDespuesDelCommit() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        AtomicBoolean ejecutada = new AtomicBoolean();
        DB.usar(dataSource);
        try {
            DB.enTransaccion(() -> {
                DB.despuesDeCommit(() -> ejecutada.set(true));
                assertFalse(ejecutada.get());
                return null;
            });
            assertTrue(ejecutada.get());
            verify(connection).commit();
        } finally {
            DB.usar(null);
        }
    }

    @Test
    void noEjecutaAccionesPosterioresSiLaTransaccionFalla() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        AtomicBoolean ejecutada = new AtomicBoolean();
        DB.usar(dataSource);
        try {
            assertThrows(IllegalStateException.class, () -> DB.enTransaccion(() -> {
                DB.despuesDeCommit(() -> ejecutada.set(true));
                throw new IllegalStateException("rollback");
            }));
            assertFalse(ejecutada.get());
            verify(connection).rollback();
        } finally {
            DB.usar(null);
        }
    }
}
