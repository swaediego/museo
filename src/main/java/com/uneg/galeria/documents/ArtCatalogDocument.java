package com.uneg.galeria.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.Map;

@Document(collection = "art_catalog")
@Data
public class ArtCatalogDocument {

    @Id
    private String id;

    @Indexed
    private Long idRelacional;

    private String nombre;

    @Indexed
    private Double precio;

    private String estatus;

    private String genero;

    private String imagenUrl;

    private Integer fechaCreacion;

    private EmbeddedArtist artista;

    private Map<String, Object> detallesEspecificos;

    @Data
    public static class EmbeddedArtist {
        private Long idArtistaRelacional;
        private String nombre;
        private String nacionalidad;
        private String biografia;
    }
}