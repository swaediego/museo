package com.uneg.galeria.dto;

import java.util.List;

public class MultiSourceSearchResult {

    private String fuente; // "MET", "Rijksmuseum", "Harvard"
    private String objectId; // ID de la fuente (puede ser String o Long según la fuente)
    private String titulo;
    private String artista;
    private String imagenUrl;
    private String clasificacion;
    private String detalles; // JSON adicional específico de cada fuente

    public MultiSourceSearchResult() {}

    public MultiSourceSearchResult(String fuente, String objectId, String titulo, 
                                    String artista, String imagenUrl, String clasificacion) {
        this.fuente = fuente;
        this.objectId = objectId;
        this.titulo = titulo;
        this.artista = artista;
        this.imagenUrl = imagenUrl;
        this.clasificacion = clasificacion;
    }

    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
}