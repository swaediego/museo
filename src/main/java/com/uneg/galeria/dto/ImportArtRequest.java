package com.uneg.galeria.dto;

import lombok.Data;

@Data
public class ImportArtRequest {
    private String busqueda;
    private Long objectId;       // Para MET
    private String tituloEspanol;
    private String artista;       // Para filtrar búsqueda
    private String fuente;        // "MET", "Rijksmuseum", "Harvard"
    private String fuenteId;      // ID de fuente alternativa
}