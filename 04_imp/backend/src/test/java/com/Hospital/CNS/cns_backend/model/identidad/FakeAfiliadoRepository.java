package com.Hospital.CNS.cns_backend.model.identidad;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Tabla interna de afiliados en memoria. {@code afiliados} es publica para preparar datos. */
public class FakeAfiliadoRepository implements AfiliadoRepository {

    public final Map<String, AfiliadoDTO> afiliados = new LinkedHashMap<>();

    public FakeAfiliadoRepository con(AfiliadoDTO a) {
        afiliados.put(a.numeroAfiliado(), a);
        return this;
    }

    @Override
    public boolean existePorNumero(String numeroAfiliado) {
        return titularPorNumero(numeroAfiliado).isPresent();
    }

    @Override
    public Optional<AfiliadoDTO> titularPorNumero(String numeroAfiliado) {
        return Optional.ofNullable(afiliados.get(numeroAfiliado)).filter(AfiliadoDTO::activo);
    }
}
