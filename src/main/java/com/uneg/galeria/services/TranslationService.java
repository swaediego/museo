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
        return traducir(spanishText, "es", "en");
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