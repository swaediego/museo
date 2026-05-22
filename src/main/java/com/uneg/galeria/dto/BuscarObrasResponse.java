package com.uneg.galeria.dto;

import lombok.Data;
import java.util.List;

@Data
public class BuscarObrasResponse {
    private boolean success;
    private String message;
    private List<MetSearchResult> resultados;
    private List<MetSearchResult> sugerencias;

    public static BuscarObrasResponse exito(List<MetSearchResult> resultados) {
        BuscarObrasResponse r = new BuscarObrasResponse();
        r.success = true;
        r.resultados = resultados;
        return r;
    }

    public static BuscarObrasResponse noEncontrado(String busquedaOriginal, List<MetSearchResult> sugerencias) {
        BuscarObrasResponse r = new BuscarObrasResponse();
        r.success = false;
        r.message = "No se encontró la obra \"" + busquedaOriginal + "\"";
        r.resultados = List.of();
        r.sugerencias = sugerencias;
        return r;
    }
}
