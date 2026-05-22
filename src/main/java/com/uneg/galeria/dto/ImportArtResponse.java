package com.uneg.galeria.dto;

import lombok.Data;

@Data
public class ImportArtResponse {
    private boolean success;
    private String message;
    private Long obraId;
    private Long idRelacional;
    private String nombre;
    private String tipo;
    private String imagenUrl;
    private String clasificacionSugeridaIA;
    private java.util.Map<String, Object> detallesExtraidos;

    public static ImportArtResponse success(Long obraId, Long idRelacional, String nombre, String tipo, String imagenUrl) {
        ImportArtResponse r = new ImportArtResponse();
        r.success = true;
        r.message = "Obra importada exitosamente desde MET Museum";
        r.obraId = obraId;
        r.idRelacional = idRelacional;
        r.nombre = nombre;
        r.tipo = tipo;
        r.imagenUrl = imagenUrl;
        return r;
    }

    public static ImportArtResponse error(String message) {
        ImportArtResponse r = new ImportArtResponse();
        r.success = false;
        r.message = message;
        return r;
    }
}