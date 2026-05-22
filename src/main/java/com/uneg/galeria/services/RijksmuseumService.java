package com.uneg.galeria.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class RijksmuseumService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://www.rijksmuseum.nl/api/en/search";
    private static final String API_KEY = System.getenv("RIJKSMUSEUM_API_KEY");

    public RijksmuseumService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<RijksmuseumResult> search(String query, String artistName) {
        List<RijksmuseumResult> results = new ArrayList<>();
        try {
            String url = buildSearchUrl(query, artistName);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode artObjects = root.path("artObjects");
                
                if (artObjects.isArray()) {
                    for (JsonNode art : artObjects) {
                        String title = art.path("title").asText("");
                        String maker = art.path("principalMakers").path(0).path("name").asText("Desconocido");
                        String imageUrl = art.path("webImage").path("url").asText("");
                        String objectNumber = art.path("objectNumber").asText("");
                        String classification = art.path("classification").path("sub").asText("");

                        if (!imageUrl.isBlank()) {
                            results.add(new RijksmuseumResult(
                                title,
                                maker,
                                imageUrl,
                                objectNumber,
                                classification
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error buscando en Rijksmuseum: " + e.getMessage());
        }
        return results;
    }

    private String buildSearchUrl(String query, String artistName) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?q=").append(encodeURIComponent(query));
        url.append("&format=json");
        url.append("&type=painting,photo, sculpture");
        url.append("&hasImage=true");
        
        if (artistName != null && !artistName.isBlank()) {
            url.append("&maker=").append(encodeURIComponent(artistName));
        }
        
        if (API_KEY != null && !API_KEY.isBlank()) {
            url.append("&key=").append(API_KEY);
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

    public static class RijksmuseumResult {
        private String titulo;
        private String artista;
        private String imagenUrl;
        private String objectNumber;
        private String clasificacion;

        public RijksmuseumResult(String titulo, String artista, String imagenUrl, 
                                 String objectNumber, String clasificacion) {
            this.titulo = titulo;
            this.artista = artista;
            this.imagenUrl = imagenUrl;
            this.objectNumber = objectNumber;
            this.clasificacion = clasificacion;
        }

        public String getTitulo() { return titulo; }
        public String getArtista() { return artista; }
        public String getImagenUrl() { return imagenUrl; }
        public String getObjectNumber() { return objectNumber; }
        public String getClasificacion() { return clasificacion; }
    }
}