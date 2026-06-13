package com.uneg.galeria.services;
// =============================================================================
// MODIFICADO por Diego Torrelles (2026-06-10)
// Cambio: Reemplazado Rijksmuseum (requiere API key) por Art Institute of Chicago
// API (100% libre, sin autenticación). El API endpoint es:
// https://api.artic.edu/api/v1/artworks/search
// Documentación: https://api.artic.edu/docs
// =============================================================================
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArtInstituteChicagoService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    // API libre de Art Institute of Chicago — no necesita key
    private static final String BASE_URL = "https://api.artic.edu/api/v1/artworks/search";

    public ArtInstituteChicagoService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // =============================================================================
        // MODIFICADO por Diego Torrelles (2026-06-10)
        // Cambio: Agregados timeouts para evitar bloqueos en llamadas lentas.
        // =============================================================================
        this.restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory());
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
            .setConnectTimeout(java.time.Duration.ofSeconds(10));
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
            .setReadTimeout(java.time.Duration.ofSeconds(15));
    }

    public List<ArtInstituteResult> search(String query, String artistName) {
        List<ArtInstituteResult> results = new ArrayList<>();
        try {
            String url = buildSearchUrl(query, artistName);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data");
                JsonNode config = root.path("config");
                JsonNode iiifUrl = config.path("iiif_url");

                if (data.isArray()) {
                    for (JsonNode art : data) {
                        String title = art.path("title").asText("");
                        String maker = art.path("artist_title").asText("Desconocido");
                        String classification = art.path("classification_title").asText("");
                        String imageId = art.path("image_id").asText("");

                        // Construir URL de imagen IIIF: {iiif_url}/{image_id}/full/843,/0/default.jpg
                        String imageUrl = "";
                        if (!imageId.isBlank() && !iiifUrl.asText().isBlank()) {
                            imageUrl = iiifUrl.asText() + "/" + imageId + "/full/843,/0/default.jpg";
                        }

                        if (!title.isBlank()) {
                            results.add(new ArtInstituteResult(
                                title,
                                maker,
                                imageUrl,
                                art.path("id").asText(""),
                                classification
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error buscando en Art Institute of Chicago: " + e.getMessage());
        }
        return results;
    }

    private String buildSearchUrl(String query, String artistName) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?q=").append(encodeURIComponent(query));
        url.append("&fields=id,title,artist_title,classification_title,image_id");
        url.append("&limit=10");

        if (artistName != null && !artistName.isBlank()) {
            url.append("&query[term][artist_title]=").append(encodeURIComponent(artistName));
        }

        return url.toString();
    }

    private String encodeURIComponent(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }

    public static class ArtInstituteResult {
        private String titulo;
        private String artista;
        private String imagenUrl;
        private String id;
        private String clasificacion;

        public ArtInstituteResult(String titulo, String artista, String imagenUrl,
                                  String id, String clasificacion) {
            this.titulo = titulo;
            this.artista = artista;
            this.imagenUrl = imagenUrl;
            this.id = id;
            this.clasificacion = clasificacion;
        }

        public String getTitulo() { return titulo; }
        public String getArtista() { return artista; }
        public String getImagenUrl() { return imagenUrl; }
        public String getId() { return id; }
        public String getClasificacion() { return clasificacion; }
    }
}
