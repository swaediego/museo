package com.uneg.galeria.dto;

import lombok.Data;

@Data
public class OllamaArtResponse {
    private String genre;
    private Attributes attributes;

    @Data
    public static class Attributes {
        private String tecnica;
        private String estilo;
        private String material;
        private Double peso;
        private Double largo;
        private Double ancho;
        private Double profundidad;
        private String purezaMetal;
        private String metalBase;
        private String tipoImpresion;
        private String papel;
        private String edicion;
        private String tipoArcilla;
        private Double temperaturaCoccion;
        private Integer fechaCreacion;
    }
}