package com.uneg.galeria.dto;

import lombok.Data;
import java.util.Objects;

@Data
public class MetSearchResult {
    private Long objectId;       // Para MET (Long)
    private String titulo;
    private String tituloEspanol;
    private String artista;
    private String imagenUrl;
    private String clasificacion;
    private String fuente;       // "MET", "Rijksmuseum", "Harvard", "Wikimedia"
    private String fuenteId;     // ID de la fuente alternativa (String)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetSearchResult that = (MetSearchResult) o;
        // Deduplicar por título + artista (case-insensitive)
        return Objects.equals(titulo, that.titulo) && 
               Objects.equals(artista, that.artista);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo, artista);
    }

    public static MetSearchResult from(Long objectId, String titulo, String artista, String imagenUrl, String clasificacion) {
        MetSearchResult r = new MetSearchResult();
        r.objectId = objectId;
        r.titulo = titulo;
        r.artista = artista;
        r.imagenUrl = imagenUrl;
        r.clasificacion = clasificacion;
        r.fuente = "MET";
        return r;
    }

    public static MetSearchResult fromRijksmuseum(String titulo, String artista, String imagenUrl,
                                                   String clasificacion, String objectNumber) {
        MetSearchResult r = new MetSearchResult();
        r.titulo = titulo;
        r.artista = artista;
        r.imagenUrl = imagenUrl;
        r.clasificacion = clasificacion;
        r.fuente = "Rijksmuseum";
        r.fuenteId = objectNumber;
        return r;
    }

    public static MetSearchResult fromHarvard(String titulo, String artista, String imagenUrl,
                                               String clasificacion, String id) {
        MetSearchResult r = new MetSearchResult();
        r.titulo = titulo;
        r.artista = artista;
        r.imagenUrl = imagenUrl;
        r.clasificacion = clasificacion;
        r.fuente = "Harvard";
        r.fuenteId = id;
        return r;
    }

    // =============================================================================
    // CREADO por Diego Torrelles (2026-06-10)
    // Fuente: Wikimedia Commons — API REST pública, sin autenticación.
    // Útil para obras famosas ausentes en MET / Art Institute (ej: "The Starry Night").
    // =============================================================================
    public static MetSearchResult fromWikimedia(String titulo, String artista, String imagenUrl,
                                                 String clasificacion, String id) {
        MetSearchResult r = new MetSearchResult();
        r.titulo = titulo;
        r.artista = artista;
        r.imagenUrl = imagenUrl;
        r.clasificacion = clasificacion;
        r.fuente = "Wikimedia";
        r.fuenteId = id;
        return r;
    }
}