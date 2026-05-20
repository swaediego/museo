package com.uneg.galeria.dto;

import lombok.Data;

@Data
public class ImportArtRequest {
    private String busqueda;
    private Long objectId;
    private String tituloEspanol;
}