package com.uneg.galeria.services;
// =============================================================================
// CREADO por Diego Torrelles (2026-06-10)
// Servicio: Wikimedia Commons como fuente补充aria de obras de arte.
// API REST pública (100% libre, sin autenticación).
// Endpoint búsqueda: https://commons.wikimedia.org/w/api.php
// Documentación: https://www.mediawiki.org/wiki/API:Search
// Esta fuente es útil para obras famosas que no están en MET ni Art Institute
// (ej: "The Starry Night" de Van Gogh — solo existe en Wikimedia Commons).
// =============================================================================
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class WikimediaCommonsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://commons.wikimedia.org/w/api.php";

    public WikimediaCommonsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // =============================================================================
        // MODIFICADO por Diego Torrelles (2026-06-10)
        // Cambio: Agregados timeouts de conexión y lectura para evitar bloqueos.
        // =============================================================================
        this.restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory());
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
            .setConnectTimeout(java.time.Duration.ofSeconds(10));
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
            .setReadTimeout(java.time.Duration.ofSeconds(15));
    }

    /**
     * Busca imágenes en Wikimedia Commons por título de obra.
     * Solo retorna resultados que sean imágenes (namespace6) con URL válida.
     */
    public List<WikimediaResult> search(String query, String artistName) {
        List<WikimediaResult> results = new ArrayList<>();
        try {
            // 1. Buscar imágenes por título
            String searchUrl = buildSearchUrl(query, artistName);
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "GaleriaApp/1.0 (museum-art-catalog; https://github.com/diego/galeria-art)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            JsonNode root = objectMapper.readTree(
                restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class).getBody()
            );
            JsonNode searchResults = root.path("query").path("search");

            if (!searchResults.isArray()) return results;

            for (JsonNode item : searchResults) {
                String title = item.path("title").asText(""); // "File:Nombre.jpg"
                if (title.isBlank()) continue;

                // Extraer nombre de archivo limpio
                String filename = title.replace("File:", "");

                // 2. Obtener URL de imagen
                String imageUrl = getImageUrl(filename);
                if (imageUrl.isBlank()) continue;

                // 3. Extraer artista del nombre de archivo si no se proporcionó
                String artista = artistName != null && !artistName.isBlank()
                    ? artistName
                    : extractArtistFromFilename(filename);

                results.add(new WikimediaResult(
                    filename,
                    artista,
                    imageUrl,
                    filename,
                    "Pintura"
                ));
            }
        } catch (Exception e) {
            System.err.println("Error buscando en Wikimedia Commons: " + e.getMessage());
        }
        return results;
    }

    private String buildSearchUrl(String query, String artistName) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?action=query");
        url.append("&list=search");
        url.append("&srsearch=").append(encodeURIComponent(query));
        url.append("&srnamespace=6"); // namespace6 = File
        url.append("&srlimit=10");
        url.append("&format=json");
        return url.toString();
    }

    private String getImageUrl(String filename) {
        try {
            // NO codificar "File:" — el : debe ir literal. Wikimedia lo rechaza si está encodeado.
            String imageUrlApi = BASE_URL + "?action=query"
                + "&titles=File:" + encodeURIComponent(filename)
                + "&prop=imageinfo"
                + "&iiprop=url"
                + "&format=json";

            HttpHeaders headersImg = new HttpHeaders();
            headersImg.set("User-Agent", "GaleriaApp/1.0 (museum-art-catalog; https://github.com/diego/galeria-art)");
            HttpEntity<String> entityImg = new HttpEntity<>(headersImg);
            String response = restTemplate.exchange(imageUrlApi, HttpMethod.GET, entityImg, String.class).getBody();
            JsonNode root = objectMapper.readTree(response);
            JsonNode pages = root.path("query").path("pages");

            for (java.util.Iterator<java.util.Map.Entry<String, JsonNode>> it = pages.fields(); it.hasNext(); ) {
                java.util.Map.Entry<String, JsonNode> entry = it.next();
                JsonNode imageinfo = entry.getValue().path("imageinfo");
                if (imageinfo.isArray() && !imageinfo.isEmpty()) {
                    return imageinfo.get(0).path("url").asText("");
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo URL de imagen Wikimedia: " + e.getMessage());
        }
        return "";
    }

    /**
     * Intenta extraer el artista del nombre del archivo.
     * Ej: "Vincent_van_Gogh_-_Starry_Night_-_Google_Art_Project.jpg" → "Vincent van Gogh"
     */
    private String extractArtistFromFilename(String filename) {
        String name = filename.replace("_", " ").replace("-", " ");
        // Patrón común: "Artista - Título - ..." o "Artista - Título.jpg"
        if (name.contains("van Gogh")) return "Vincent van Gogh";
        if (name.contains("Picasso")) return "Pablo Picasso";
        if (name.contains("Monet")) return "Claude Monet";
        if (name.contains("Da Vinci") || name.contains("da Vinci")) return "Leonardo da Vinci";
        if (name.contains("Kandinsky")) return "Wassily Kandinsky";
        if (name.contains("Warhol")) return "Andy Warhol";
        if (name.contains("Klimt")) return "Gustav Klimt";
        if (name.contains("Rembrandt")) return "Rembrandt";
        if (name.contains("Dali") || name.contains("Dalí")) return "Salvador Dalí";
        if (name.contains("Frieda") || name.contains("Frida")) return "Frida Kahlo";
        if (name.contains("OKeeffe") || name.contains("O'Keeffe")) return "Georgia O'Keeffe";
        return "Artista Desconocido";
    }

    private String encodeURIComponent(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8")
                .replace("%2C", ",")       // Wikimedia acepta comas literales
                .replace("%20", " ")       // ya viene encoding de espacios
                .replace("%22", "\"")      // Wikimedia acepta comillas literales
                .replace("%27", "'")       // comillas simples literales
                .replace("%28", "(")       // paréntesis literales
                .replace("%29", ")")       // paréntesis literales
                .replace("%2F", "/");      // slashes literales
        } catch (Exception e) {
            return text;
        }
    }

    public static class WikimediaResult {
        private String titulo;
        private String artista;
        private String imagenUrl;
        private String id;
        private String clasificacion;

        public WikimediaResult(String titulo, String artista, String imagenUrl,
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
