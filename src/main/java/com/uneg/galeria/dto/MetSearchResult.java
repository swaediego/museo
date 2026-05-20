package com.uneg.galeria.dto;

import lombok.Data;
import java.util.List;

@Data
public class MetSearchResult {
    private Long objectId;
    private String titulo;
    private String tituloEspanol;
    private String artista;
    private String imagenUrl;
    private String clasificacion;

    public static MetSearchResult from(Long objectId, String titulo, String artista, String imagenUrl, String clasificacion) {
        MetSearchResult r = new MetSearchResult();
        r.objectId = objectId;
        r.titulo = titulo;
        r.artista = artista;
        r.imagenUrl = imagenUrl;
        r.clasificacion = clasificacion;
        return r;
    }
}