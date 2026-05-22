package com.uneg.galeria.services;

import com.uneg.galeria.dto.OllamaArtResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import jakarta.annotation.PostConstruct;

@Service
public class OllamaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static String OLLAMA_URL_BASE = "http://localhost:11434";
    private static final String MODEL = "llama3";

    private static final String SYSTEM_PROMPT = "Eres un experto en curaduría de arte y extracción de datos técnicos. Tu tarea es analizar la descripción de una obra del MET y clasificarla en uno de los siguientes géneros: Painting, Sculpture, Orfebrery, Photograph, Ceramic. Debes devolver estrictamente un objeto JSON con la siguiente estructura: { \"genre\": \"[Nombre del Género]\", \"attributes\": { \"campo\": \"valor\" } } Los atributos deben corresponder exactamente a los campos definidos para cada género.\n\nMapeo de atributos por género:\n- Painting: tecnica, estilo, fechaCreacion\n- Sculpture: material, peso, largo, ancho, profundidad, fechaCreacion\n- Orfebrery: purezaMetal, metalBase, peso, fechaCreacion\n- Photograph: tipoImpresion, papel, edicion, fechaCreacion\n- Ceramic: tipoArcilla, temperaturaCoccion, fechaCreacion";

    public OllamaService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);   // 15 segundos
        factory.setReadTimeout(30000);    // 30 segundos para generación de LLMs
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        String envUrl = System.getenv("OLLAMA_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            // Extraer la base: http://host:port de http://host:port/api/generate
            OLLAMA_URL_BASE = envUrl.replace("/api/generate", "").replace("/api/chat", "");
        }
    }

    public OllamaArtResponse analizarObra(String titulo, String artista, String medium,
                                          String dimensions, String objectDate, String classification) {
        String userPrompt = buildUserPrompt(titulo, artista, medium, dimensions, objectDate, classification);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("prompt", userPrompt);
            requestBody.put("system", SYSTEM_PROMPT);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String fullUrl = OLLAMA_URL_BASE + "/api/generate";
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseResponse(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error al llamar a Ollama: " + e.getMessage());
        }
        return null;
    }

    private String buildUserPrompt(String titulo, String artista, String medium,
                                   String dimensions, String objectDate, String classification) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analiza la siguiente obra de arte y clasifcala:\n\n");
        sb.append("Titulo: ").append(titulo != null ? titulo : "Desconocido").append("\n");
        sb.append("Artista: ").append(artista != null ? artista : "Desconocido").append("\n");
        sb.append("Tecnica/Medium: ").append(medium != null ? medium : "Desconocido").append("\n");
        sb.append("Dimensiones: ").append(dimensions != null ? dimensions : "Desconocido").append("\n");
        sb.append("Fecha: ").append(objectDate != null ? objectDate : "Desconocido").append("\n");
        sb.append("Clasificacion MET: ").append(classification != null ? classification : "Desconocido").append("\n");
        sb.append("\nResponde SOLO con el JSON sin texto adicional.");
        return sb.toString();
    }

    private OllamaArtResponse parseResponse(String jsonResponse) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);
            String fullResponse = (String) responseMap.get("response");

            if (fullResponse != null) {
                fullResponse = fullResponse.trim();
                int jsonStart = fullResponse.indexOf('{');
                int jsonEnd = fullResponse.lastIndexOf('}');
                if (jsonStart >= 0 && jsonEnd >= 0) {
                    String jsonPart = fullResponse.substring(jsonStart, jsonEnd + 1);
                    return objectMapper.readValue(jsonPart, OllamaArtResponse.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al parsear respuesta de Ollama: " + e.getMessage());
        }
        return null;
    }
}