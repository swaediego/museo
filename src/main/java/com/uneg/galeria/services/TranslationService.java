package com.uneg.galeria.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.HashMap;

@Service
public class TranslationService {

    private final RestTemplate restTemplate;
    private static final String MYMEMORY_API_URL = "https://api.mymemory.translated.net/get";

    public TranslationService() {
        this.restTemplate = new RestTemplate();
    }

    public String traducir(String texto, String fromLang, String toLang) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }

        try {
            String url = MYMEMORY_API_URL + "?q=" + encodeURIComponent(texto) + "&langpair=" + fromLang + "|" + toLang;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseData = response.getBody();
                Map<String, Object> responseData2 = (Map<String, Object>) responseData.get("responseData");
                if (responseData2 != null) {
                    String translatedText = (String) responseData2.get("translatedText");
                    if (translatedText != null && !translatedText.isBlank()) {
                        return translatedText;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error traduciendo '" + texto + "': " + e.getMessage());
        }

        return texto;
    }

    public String spanishToEnglish(String spanishText) {
        // =============================================================================
        // Quien/Nombre: Diego Torrelles
        // Que/descripcion: No traducir textos que parecen nombres propios de obras de arte.
        //   MyMemory destruye artículos como "la" en tahitiano/gallego, convirtiendo
        //   "la Orana Maria" → "orana maria". Si el texto tiene palabras que empiezan con
        //   mayúscula en posiciones intermedias (tipo título propio), se devuelve sin
        //   modificar para que la búsqueda en MET funcione correctamente.
        // Cuando/fecha: 2026-06-09
        // =============================================================================
        if (spanishText == null || spanishText.isBlank()) {
            return spanishText;
        }
        // Detectar título propio: si hay2+ palabras con mayúscula sostenida (no solo
        // la primera letra de la oración), es probable que sea un nombre de obra/artista.
        String trimmed = spanishText.trim();
        long capitalWords = trimmed.codePoints().filter(cp -> {
            if (Character.isUpperCase(cp)) {
                int idx = trimmed.indexOf(cp);
                return idx > 0 && Character.isLetter(trimmed.charAt(idx - 1));
            }
            return false;
        }).count();
        if (capitalWords >= 2) {
            System.out.println("[TranslationService] Nombre propio detectado, no traduzco: " + trimmed);
            return trimmed;
        }
        String translated = traducir(trimmed, "es", "en");
        // Si MyMemory eliminó artículos importantes ("la orana" → "orana"), devolver original
        if (trimmed.toLowerCase().startsWith("la ") && translated.toLowerCase().startsWith("la ")
            && trimmed.toLowerCase().contains("orana")) {
            // Ok, la traducción preservó "la"
        } else if (trimmed.toLowerCase().startsWith("la ") && !translated.toLowerCase().startsWith("la ")) {
            System.out.println("[TranslationService] MyMemory eliminó 'la', devuelvo original: " + trimmed);
            return trimmed;
        }
        return translated;
    }

    public String englishToSpanish(String englishText) {
        return traducir(englishText, "en", "es");
    }

    private String encodeURIComponent(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }
}