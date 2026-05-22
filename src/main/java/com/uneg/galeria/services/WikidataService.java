package com.uneg.galeria.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
public class WikidataService {

    private final RestTemplate restTemplate;
    private static final String WIKIDATA_SPARQL_URL = "https://query.wikidata.org/sparql";

    public WikidataService() {
        this.restTemplate = new RestTemplate();
    }

    public String obtenerEstiloArtistico(String obraNombreIngles) {
        String sparqlQuery = "SELECT ?movementLabel WHERE {" +
            " ?painting rdfs:label \"" + obraNombreIngles + "\"@en ." +
            " ?painting wdt:P135 ?movement ." +
            " SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" }" +
            "} LIMIT 5";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "GaleriaArtMuseum/1.0 (contact@uneg.edu)");

            HttpEntity<String> request = new HttpEntity<>(sparqlQuery, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                WIKIDATA_SPARQL_URL,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parsearEstilo(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error consultando Wikidata para estilo: " + e.getMessage());
        }
        return null;
    }

    public String obtenerDescripcion(String obraNombreIngles) {
        String sparqlQuery = "SELECT ?description WHERE {" +
            " ?painting rdfs:label \"" + obraNombreIngles + "\"@en ." +
            " ?painting schema:description ?description ." +
            " FILTER(LANG(?description) = \"en\")" +
            "} LIMIT 1";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "GaleriaArtMuseum/1.0 (contact@uneg.edu)");

            HttpEntity<String> request = new HttpEntity<>(sparqlQuery, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                WIKIDATA_SPARQL_URL,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parsearDescripcion(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error consultando Wikidata para descripcion: " + e.getMessage());
        }
        return null;
    }

    public String obtenerMateriales(String obraNombreIngles) {
        String sparqlQuery = "SELECT ?materialLabel WHERE {" +
            " ?painting rdfs:label \"" + obraNombreIngles + "\"@en ." +
            " ?painting wdt:P186 ?material ." +
            " SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" }" +
            "} LIMIT 3";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "GaleriaArtMuseum/1.0 (contact@uneg.edu)");

            HttpEntity<String> request = new HttpEntity<>(sparqlQuery, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                WIKIDATA_SPARQL_URL,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parsearMateriales(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error consultando Wikidata para materiales: " + e.getMessage());
        }
        return null;
    }

    /**
     * Consulta Wikidata para obtener datos de un artista: nacionalidad, fecha de nacimiento y descripción.
     * @param nombreArtista Nombre del artista en inglés (ej: "Vincent van Gogh")
     * @return Mapa con claves "nationality", "birthDate", "description"; valores null si no se encuentran
     */
    public Map<String, String> obtenerDatosArtista(String nombreArtista) {
        Map<String, String> datos = new HashMap<>();
        datos.put("nationality", null);
        datos.put("birthDate", null);
        datos.put("description", null);

        if (nombreArtista == null || nombreArtista.isBlank()) {
            return datos;
        }

        String sparqlQuery = "SELECT ?nationality ?birthDate ?description WHERE {" +
            " ?artist rdfs:label \"" + nombreArtista + "\"@en ." +
            " ?artist wdt:P31 wd:Q5 ." +  // es humano
            " OPTIONAL { ?artist wdt:P27 ?nationality . }" +
            " OPTIONAL { ?artist wdt:P569 ?birthDate . }" +
            " OPTIONAL { ?artist schema:description ?description FILTER(LANG(?description)=\"en\") }" +
            "} LIMIT 1";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "GaleriaArtMuseum/1.0 (contact@uneg.edu)");

            HttpEntity<String> request = new HttpEntity<>(sparqlQuery, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                WIKIDATA_SPARQL_URL,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                parsearDatosArtista(response.getBody(), datos);
            }
        } catch (Exception e) {
            System.err.println("[WikidataService] Error consultando datos del artista: " + e.getMessage());
        }

        return datos;
    }

    private void parsearDatosArtista(String xmlResponse, Map<String, String> datos) {
        // Parsear nationality
        if (xmlResponse.contains("nationality")) {
            int idx = xmlResponse.indexOf("<nationality>");
            if (idx != -1) {
                int start = idx + "<nationality>".length();
                int end = xmlResponse.indexOf("</nationality>", start);
                if (end != -1) {
                    datos.put("nationality", xmlResponse.substring(start, end).trim());
                }
            }
        }

        // Parsear birthDate
        if (xmlResponse.contains("birthDate")) {
            int idx = xmlResponse.indexOf("<birthDate>");
            if (idx != -1) {
                int start = idx + "<birthDate>".length();
                int end = xmlResponse.indexOf("</birthDate>", start);
                if (end != -1) {
                    String date = xmlResponse.substring(start, end).trim();
                    // Wikidata devuelve fechas en formato YYYY-MM-DDTHH:MM:SSZ
                    // Solo nos interesa el año para guardar en LocalDate
                    if (date.length() >= 4) {
                        datos.put("birthDate", date.substring(0, 4));
                    }
                }
            }
        }

        // Parsear description
        if (xmlResponse.contains("description")) {
            int idx = xmlResponse.indexOf("<description>");
            if (idx != -1) {
                int start = idx + "<description>".length();
                int end = xmlResponse.indexOf("</description>", start);
                if (end != -1) {
                    datos.put("description", xmlResponse.substring(start, end).trim());
                }
            }
        }
    }

    private String parsearEstilo(String xmlResponse) {
        List<String> estilos = new ArrayList<>();
        String marker = "movementLabel";
        int idx = 0;

        while ((idx = xmlResponse.indexOf("<" + marker + ">", idx)) != -1) {
            int start = idx + marker.length() + 2;
            int end = xmlResponse.indexOf("</" + marker + ">", start);
            if (end != -1) {
                String valor = xmlResponse.substring(start, end).trim();
                if (!valor.isEmpty()) {
                    estilos.add(valor);
                }
            }
            idx = end;
        }

        if (!estilos.isEmpty()) {
            return estilos.get(0);
        }
        return null;
    }

    private String parsearDescripcion(String xmlResponse) {
        String marker = "description";
        int idx = xmlResponse.indexOf("<" + marker + ">");
        if (idx != -1) {
            int start = idx + marker.length() + 2;
            int end = xmlResponse.indexOf("</" + marker + ">", start);
            if (end != -1) {
                return xmlResponse.substring(start, end).trim();
            }
        }
        return null;
    }

    private String parsearMateriales(String xmlResponse) {
        List<String> materiales = new ArrayList<>();
        String marker = "materialLabel";
        int idx = 0;

        while ((idx = xmlResponse.indexOf("<" + marker + ">", idx)) != -1) {
            int start = idx + marker.length() + 2;
            int end = xmlResponse.indexOf("</" + marker + ">", start);
            if (end != -1) {
                String valor = xmlResponse.substring(start, end).trim();
                if (!valor.isEmpty()) {
                    materiales.add(valor);
                }
            }
            idx = end;
        }

        if (!materiales.isEmpty()) {
            return String.join(", ", materiales);
        }
        return null;
    }
}