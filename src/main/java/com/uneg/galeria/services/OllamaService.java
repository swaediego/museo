package com.uneg.galeria.services;

import com.uneg.galeria.dto.OllamaArtResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3";

    private static final String SYSTEM_PROMPT = "Eres un experto en curaduria de arte y extraccion de datos tecnicos. Tu tarea es analizar la descripcion de una obra del MET y clasificarla en uno de los siguientes generos: Painting, Sculpture, Orphebrery, Photograph, Ceramic. Debes devolver estrictamente un objeto JSON con la siguiente estructura: { \"genre\": \"[Nombre del Genero]\", \"attributes\": { \"campo\": \"valor\" } } Los atributos deben corresponder exactamente a los campos definidos para cada genero.\n\nMapeo de atributos por genero:\n- Painting: tecnica, estilo, fechaCreacion\n- Sculpture: material, peso, largo, ancho, profundidad, fechaCreacion\n- Orphebrery: purezaMetal, metalBase, peso, fechaCreacion\n- Photograph: tipoImpresion, papel, edicion, fechaCreacion\n- Ceramic: tipoArcilla, temperaturaCoccion, fechaCreacion";

    public OllamaService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
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
            ResponseEntity<String> response = restTemplate.postForEntity(OLLAMA_URL, entity, String.class);

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