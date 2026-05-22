package com.uneg.galeria.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class MetMuseumService {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://collectionapi.metmuseum.org/public/collection/v1";

    public MetMuseumService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Búsqueda básica
     */
    public List<Long> search(String query) {
        return search(query, null, null, null, null);
    }

    /**
     * Búsqueda con filtro de artista
     */
    public List<Long> search(String query, String artistName) {
        return search(query, artistName, null, null, null);
    }

    /**
     * Búsqueda avanzada con todos los filtros optimizados por IA
     */
    public List<Long> search(String query, String artistName, Integer yearFrom, Integer yearTo, String medium) {
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL).append("/search?hasImages=true");

            // Término principal de búsqueda
            if (query != null && !query.isBlank()) {
                urlBuilder.append("&q=").append(encodeURIComponent(query));
            }

            // Filtro por artista
            if (artistName != null && !artistName.isBlank()) {
                urlBuilder.append("&artistOrCulture=true");
                // En la API del MET, el artista va en el query
                urlBuilder.append("&q=").append(encodeURIComponent(artistName + " " + query));
            }

            // Filtro por medio/tipo (paintings, sculptures, etc)
            if (medium != null && !medium.isBlank()) {
                urlBuilder.append("&medium=").append(encodeURIComponent(medium));
            }

            String url = urlBuilder.toString();
            ResponseEntity<SearchResponse> response = restTemplate.getForEntity(url, SearchResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Long> allIds = response.getBody().getObjectIDs();
                if (allIds == null || allIds.isEmpty()) {
                    return List.of();
                }

                // Si tenemos filtro de año,我们需要获取每个对象来检查日期
                // Para optimización, limitamos a los primeros 50 resultados
                int maxResults = 50;
                if (allIds.size() > maxResults) {
                    allIds = allIds.subList(0, maxResults);
                }

                return allIds;
            }
        } catch (Exception e) {
            System.err.println("Error buscando en MET: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Versión simple que solo busca por query
     */
    public List<Long> searchSimple(String query) {
        return search(query, null, null, null, null);
    }

    public MetArtResponse getObject(Long objectId) {
        try {
            String url = BASE_URL + "/objects/" + objectId;
            ResponseEntity<MetArtResponse> response = restTemplate.getForEntity(url, MetArtResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo objeto MET " + objectId + ": " + e.getMessage());
        }
        return null;
    }

    private String encodeURIComponent(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }

    // === DTOs para la respuesta de búsqueda ===
    public static class SearchResponse {
        private int total;
        private List<Long> objectIDs;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public List<Long> getObjectIDs() { return objectIDs; }
        public void setObjectIDs(List<Long> objectIDs) { this.objectIDs = objectIDs; }
    }

    public static class MetArtResponse {
        private Long objectID;
        private String title;
        private String artistDisplayName;
        private String department;
        private String objectName;
        private String medium;
        private String dimensions;
        private String classification;
        private String primaryImage;
        private String primaryImageSmall;
        private String objectDate;
        private String country;
        private String city;
        private String repository;
        private String creditLine;

        public Long getObjectID() { return objectID; }
        public void setObjectID(Long objectID) { this.objectID = objectID; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getArtistDisplayName() { return artistDisplayName; }
        public void setArtistDisplayName(String artistDisplayName) { this.artistDisplayName = artistDisplayName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getObjectName() { return objectName; }
        public void setObjectName(String objectName) { this.objectName = objectName; }
        public String getMedium() { return medium; }
        public void setMedium(String medium) { this.medium = medium; }
        public String getDimensions() { return dimensions; }
        public void setDimensions(String dimensions) { this.dimensions = dimensions; }
        public String getClassification() { return classification; }
        public void setClassification(String classification) { this.classification = classification; }
        public String getPrimaryImage() { return primaryImage; }
        public void setPrimaryImage(String primaryImage) { this.primaryImage = primaryImage; }
        public String getPrimaryImageSmall() { return primaryImageSmall; }
        public void setPrimaryImageSmall(String primaryImageSmall) { this.primaryImageSmall = primaryImageSmall; }
        public String getObjectDate() { return objectDate; }
        public void setObjectDate(String objectDate) { this.objectDate = objectDate; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getRepository() { return repository; }
        public void setRepository(String repository) { this.repository = repository; }
        public String getCreditLine() { return creditLine; }
        public void setCreditLine(String creditLine) { this.creditLine = creditLine; }
    }
}