package com.uneg.galeria.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
public class MetMuseumService {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://collectionapi.metmuseum.org/public/collection/v1";

    public MetMuseumService() {
        this.restTemplate = new RestTemplate();
    }

    public List<Long> search(String query) {
        try {
            String url = BASE_URL + "/search?q=" + encodeURIComponent(query) + "&hasImages=true";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Long> objectIds = (List<Long>) response.getBody().get("objectIDs");
                return objectIds;
            }
        } catch (Exception e) {
            System.err.println("Error buscando en MET: " + e.getMessage());
        }
        return List.of();
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