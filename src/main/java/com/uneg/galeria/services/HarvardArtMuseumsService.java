package com.uneg.galeria.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class HarvardArtMuseumsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://api.harvardartmuseums.org/object";
    private static final String API_KEY = System.getenv("HARVARD_API_KEY");

    public HarvardArtMuseumsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<HarvardResult> search(String query, String artistName) {
        List<HarvardResult> results = new ArrayList<>();
        try {
            String url = buildSearchUrl(query, artistName);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode records = root.path("records");
                
                if (records.isArray()) {
                    for (JsonNode record : records) {
                        String title = record.path("title").asText("");
                        String maker = record.path("people").path(0).path("name").asText("Desconocido");
                        String imageUrl = record.path("primaryimageurl").asText("");
                        String classification = record.path("classification").path("name").asText("");
                        String id = record.path("id").asText("");

                        if (!imageUrl.isBlank() && !title.isBlank()) {
                            results.add(new HarvardResult(
                                title,
                                maker,
                                imageUrl,
                                id,
                                classification
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error buscando en Harvard: " + e.getMessage());
        }
        return results;
    }

    private String buildSearchUrl(String query, String artistName) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?apikey=").append(API_KEY != null ? API_KEY : "demo");
        url.append("&q=").append(encodeURIComponent(query));
        url.append("&size=20");
        url.append("&hasimage=1");
        
        if (artistName != null && !artistName.isBlank()) {
            url.append("&person=").append(encodeURIComponent(artistName));
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

    public static class HarvardResult {
        private String titulo;
        private String artista;
        private String imagenUrl;
        private String id;
        private String clasificacion;

        public HarvardResult(String titulo, String artista, String imagenUrl,
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