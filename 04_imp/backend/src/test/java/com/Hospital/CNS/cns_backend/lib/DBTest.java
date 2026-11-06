package com.Hospital.CNS.cns_backend.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Properties;

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
}
