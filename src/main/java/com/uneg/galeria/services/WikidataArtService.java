package com.uneg.galeria.services;
// =============================================================================
// CREADO por Diego Torrelles (2026-06-10)
// Nuevo servicio: Wikidata como fuente de respaldo para importar obras de arte.
// Usa el SPARQL endpoint público de Wikidata (100% libre, sin API key).
// Endpoint: https://query.wikidata.org/sparql
// Wikidata tiene datos de ~100M entidades incluyendo artistas, obras y movimientos.
// =============================================================================
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Service
public class WikidataArtService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String SPARQL_ENDPOINT = "https://query.wikidata.org/sparql";

    public WikidataArtService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // =============================================================================
        // MODIFICADO por Diego Torrelles (2026-06-10)
        // Cambio: Agregados timeouts para evitar bloqueos en llamadas SPARQL.
        // =============================================================================
        this.restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory());
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
            .setConnectTimeout(java.time.Duration.ofSeconds(10));
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
            .setReadTimeout(java.time.Duration.ofSeconds(20));
    }

    public List<WikidataResult> search(String query, String artistName) {
        List<WikidataResult> results = new ArrayList<>();
        try {
            String sparql = buildSparqlQuery(query, artistName);
            ResponseEntity<String> response = executeSparql(sparql);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode bindings = root.path("results").path("bindings");

                if (bindings.isArray()) {
                    for (JsonNode item : bindings) {
                        String title = item.path("itemLabel").path("value").asText("");
                        String maker = item.path("creatorLabel").path("value").asText("Desconocido");
                        String imageUrl = item.path("image").path("value").asText("");
                        String classification = item.path("classificationLabel").path("value").asText("");

                        if (!title.isBlank() && !imageUrl.isBlank()) {
                            results.add(new WikidataResult(
                                title,
                                maker,
                                imageUrl,
                                item.path("item").path("value").asText(""),
                                classification
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error buscando en Wikidata: " + e.getMessage());
        }
        return results;
    }

    private ResponseEntity<String> executeSparql(String sparql) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/sparql-results+json");
        headers.set("User-Agent", "GaleriaApp/1.0 (museum-art-catalog-project)");

        HttpEntity<String> entity = new HttpEntity<>("query=" + encodeURIComponent(sparql), headers);
        return restTemplate.postForEntity(SPARQL_ENDPOINT, entity, String.class);
    }

    private String buildSparqlQuery(String query, String artistName) {
        StringBuilder q = new StringBuilder();
        q.append("PREFIX wdt: <http://www.wikidata.org/prop/direct/> ");
        q.append("PREFIX wd: <http://www.wikidata.org/entity/> ");
        q.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
        q.append("SELECT ?item ?itemLabel ?creatorLabel ?image ?classificationLabel WHERE { ");

        // Buscar obras que sean instancias de pintura, escultura o fotografía
        q.append("?item wdt:P31/wdt:P279* ?type . ");
        q.append("?type rdfs:label ?classificationLabel . ");
        q.append("FILTER(LANG(?classificationLabel) = \"en\") . ");
        q.append("FILTER(?type IN (wd:Q3304963, wd:Q860861, wd:Q125191)) . "); // Painting, Sculpture, Photograph

        // Filtro por título
        q.append("?item rdfs:label ?itemLabel . ");
        q.append("FILTER(LANG(?itemLabel) = \"en\") . ");
        q.append("FILTER(CONTAINS(LCASE(?itemLabel), LCASE(\"" + escapeSparql(query) + "\"))) . ");

        // Artista (opcional)
        if (artistName != null && !artistName.isBlank()) {
            q.append("?item wdt:P170 ?creator . ");
            q.append("?creator rdfs:label ?creatorLabel . ");
            q.append("FILTER(LANG(?creatorLabel) = \"en\") . ");
            q.append("FILTER(CONTAINS(LCASE(?creatorLabel), LCASE(\"" + escapeSparql(artistName) + "\"))) . ");
        } else {
            q.append("OPTIONAL { ?item wdt:P170 ?creator . ?creator rdfs:label ?creatorLabel . FILTER(LANG(?creatorLabel) = \"en\") } . ");
        }

        // Imagen
        q.append("OPTIONAL { ?item wdt:P18 ?image } . ");

        q.append("} LIMIT 10");

        return q.toString();
    }

    private String escapeSparql(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String encodeURIComponent(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }

    public static class WikidataResult {
        private String titulo;
        private String artista;
        private String imagenUrl;
        private String id;
        private String clasificacion;

        public WikidataResult(String titulo, String artista, String imagenUrl,
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
