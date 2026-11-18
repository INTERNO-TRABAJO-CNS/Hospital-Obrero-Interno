package com.Hospital.CNS.cns_backend.model.fichasinternas;

/** Auditoria de fichas internas y cirugias (tabla fichas_internas.cambio). */
public interface CambioFichaRepository {

    /**
     * @param objeto  "FICHA" o "CIRUGIA"
     * @param accion  GENERADA, ASIGNADA, DESPLAZADA, EN_ESPERA, ATENDIDA, NO_SE_PRESENTO...
     * @param usuario quien lo hizo ("Medico 19", "Sistema")
     */
    void registrar(String objeto, long objetoId, String accion, String usuario, String motivo);
}
