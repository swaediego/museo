package com.uneg.galeria.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class SearchAssistanceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String OLLAMA_URL_BASE;

    static {
        String envUrl = System.getenv("OLLAMA_URL");
        OLLAMA_URL_BASE = (envUrl != null && !envUrl.isBlank()) 
            ? envUrl.replace("/api/generate", "") 
            : "http://localhost:11434";
    }

    public SearchAssistanceService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        // Configurar timeouts
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(180000);
        this.restTemplate.setRequestFactory(factory);
    }

    /**
     * Analiza una búsqueda del usuario y genera términos optimizados usando IA
     */
    public SearchAnalysis analyzeSearch(String userQuery, String userArtistHint) {
        SearchAnalysis analysis = new SearchAnalysis();
        analysis.setOriginalQuery(userQuery);
        analysis.setArtistHint(userArtistHint);

        try {
            // Pedir a Ollama que analice y optimice la búsqueda
            String prompt = buildAnalysisPrompt(userQuery, userArtistHint);
            String rawResponse = callOllama(prompt);

            // Parsear respuesta de la IA
            parseAnalysis(rawResponse, analysis);

        } catch (Exception e) {
            System.err.println("Error en análisis de IA: " + e.getMessage());
            // Fallback: usar búsqueda directa con traducción simple
            analysis.setSearchTerms(userQuery);
            analysis.setFallback(true);
        }

        return analysis;
    }

    private String buildAnalysisPrompt(String query, String artistHint) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Eres un asistente experto en bellas artes y búsqueda en museos de arte.\n");
        prompt.append("El usuario quiere encontrar: \"").append(query).append("\"\n");
        if (artistHint != null && !artistHint.isBlank()) {
            prompt.append("Artista mencionado: \"").append(artistHint).append("\"\n");
        }
        prompt.append("\nAnaliza la búsqueda y devuelve SOLO un JSON válido con:\n");
        prompt.append("{\n");
        prompt.append("  \"searchTerms\": \"términos de búsqueda en inglés optimizados para API de museo\",\n");
        prompt.append("  \"artist\": \"nombre del artista en inglés o null\",\n");
        prompt.append("  \"yearFrom\": \"año inicial o null\",\n");
        prompt.append("  \"yearTo\": \"año final o null\",\n");
        prompt.append("  \"medium\": \"tipo de medio (painting, sculpture, photograph, etc.) o null\",\n");
        prompt.append("  \"confidence\": \"high, medium o low\"\n");
        prompt.append("}\n");
        prompt.append("\nEjemplos:\n");
        prompt.append("- Usuario: \"La noche estrellada\" → {\"searchTerms\": \"Starry Night\", \"artist\": \"Vincent van Gogh\", \"yearFrom\": 1880, \"yearTo\": 1890, \"medium\": \"paintings\", \"confidence\": \"high\"}\n");
        prompt.append("- Usuario: \"David de Miguel Ángel\" → {\"searchTerms\": \"David\", \"artist\": \"Michelangelo\", \"yearFrom\": 1500, \"yearTo\": 1520, \"medium\": \"sculpture\", \"confidence\": \"high\"}\n");
        prompt.append("- Usuario: \"Mona Lisa\" → {\"searchTerms\": \"Mona Lisa\", \"artist\": \"Leonardo da Vinci\", \"yearFrom\": 1500, \"yearTo\": 1520, \"medium\": \"paintings\", \"confidence\": \"high\"}\n");
        prompt.append("- Usuario: \"escultura de un pensador\" → {\"searchTerms\": \"thinker sculpture\", \"artist\": null, \"yearFrom\": 1800, \"yearTo\": 2000, \"medium\": \"sculpture\", \"confidence\": \"medium\"}\n");
        prompt.append("\nResponde SOLO con el JSON, sin texto adicional:");
        return prompt.toString();
    }

    private String callOllama(String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama3");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("options", Map.of("temperature", 0.3)); // Baja temperatura para respuestas más consistentes

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        String fullUrl = OLLAMA_URL_BASE + "/api/generate";

        // Usar restTemplate exchange para mejor control
        ResponseEntity<String> response = restTemplate.exchange(
            fullUrl,
            HttpMethod.POST,
            entity,
            String.class
        );

        if (response.getBody() != null) {
            // Parsear respuesta de Ollama (tiene formato {"response": "...", ...})
            var root = objectMapper.readTree(response.getBody());
            return root.path("response").asText();
        }
        return "";
    }

    private void parseAnalysis(String rawResponse, SearchAnalysis analysis) {
        try {
            // La respuesta ya debería ser JSON limpio
            String jsonStr = rawResponse.trim();
            // Limpiar markdown si hay
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }
            jsonStr = jsonStr.trim();

            var analysisNode = objectMapper.readTree(jsonStr);

            analysis.setSearchTerms(analysisNode.path("searchTerms").asText(analysis.getOriginalQuery()));
            analysis.setArtist(analysisNode.path("artist").isMissingNode() ? null : analysisNode.path("artist").asText(null));
            analysis.setYearFrom(analysisNode.path("yearFrom").isMissingNode() ? null : analysisNode.path("yearFrom").asInt());
            analysis.setYearTo(analysisNode.path("yearTo").isMissingNode() ? null : analysisNode.path("yearTo").asInt());
            analysis.setMedium(analysisNode.path("medium").isMissingNode() ? null : analysisNode.path("medium").asText(null));
            analysis.setConfidence(analysisNode.path("confidence").asText("medium"));

        } catch (Exception e) {
            System.err.println("Error parseando análisis de IA: " + e.getMessage());
            analysis.setSearchTerms(analysis.getOriginalQuery());
            analysis.setFallback(true);
        }
    }

    /**
     * Combina y rankea resultados de múltiples fuentes
     */
    public List<RankedSearchResult> rankResults(List<?> metResults, List<?> rijksResults, List<?> harvardResults,
                                                SearchAnalysis analysis) {
        List<RankedSearchResult> ranked = new ArrayList<>();

        // Procesar resultados MET
        if (metResults != null) {
            for (Object r : metResults) {
                RankedSearchResult rsr = new RankedSearchResult();
                rsr.setSource("MET");
                rsr.setScore(calculateRelevanceScore(r, analysis));
                ranked.add(rsr);
            }
        }

        // Ordenar por score descendente
        ranked.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return ranked;
    }

    private double calculateRelevanceScore(Object result, SearchAnalysis analysis) {
        double score = 50.0; // Score base

        // Factores que aumentan score:
        if (analysis.getArtist() != null && result.toString().contains(analysis.getArtist())) {
            score += 30;
        }

        return score;
    }

    // === DTOs ===

    public static class SearchAnalysis {
        private String originalQuery;
        private String searchTerms;
        private String artist;
        private Integer yearFrom;
        private Integer yearTo;
        private String medium;
        private String confidence;
        private String artistHint;
        private boolean fallback;

        public String getOriginalQuery() { return originalQuery; }
        public void setOriginalQuery(String originalQuery) { this.originalQuery = originalQuery; }
        public String getSearchTerms() { return searchTerms; }
        public void setSearchTerms(String searchTerms) { this.searchTerms = searchTerms; }
        public String getArtist() { return artist; }
        public void setArtist(String artist) { this.artist = artist; }
        public Integer getYearFrom() { return yearFrom; }
        public void setYearFrom(Integer yearFrom) { this.yearFrom = yearFrom; }
        public Integer getYearTo() { return yearTo; }
        public void setYearTo(Integer yearTo) { this.yearTo = yearTo; }
        public String getMedium() { return medium; }
        public void setMedium(String medium) { this.medium = medium; }
        public String getConfidence() { return confidence; }
        public void setConfidence(String confidence) { this.confidence = confidence; }
        public String getArtistHint() { return artistHint; }
        public void setArtistHint(String artistHint) { this.artistHint = artistHint; }
        public boolean isFallback() { return fallback; }
        public void setFallback(boolean fallback) { this.fallback = fallback; }
    }

    public static class RankedSearchResult {
        private String source;
        private double score;
        private Object rawResult;

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public Object getRawResult() { return rawResult; }
        public void setRawResult(Object rawResult) { this.rawResult = rawResult; }
    }
}