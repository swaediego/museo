package com.uneg.galeria.dto;

import lombok.Data;
import java.util.List;

@Data
public class SyncResponse {
    private List<Long> processed;
    private List<Long> notFound;
    private String message;

    public SyncResponse() {}

    public SyncResponse(List<Long> processed, List<Long> notFound, String message) {
        this.processed = processed;
        this.notFound = notFound;
        this.message = message;
    }
}