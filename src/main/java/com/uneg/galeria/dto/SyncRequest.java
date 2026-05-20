package com.uneg.galeria.dto;

import lombok.Data;
import java.util.List;

@Data
public class SyncRequest {
    private List<DeletedItem> deletedIds;

    @Data
    public static class DeletedItem {
        private String type;
        private Long id;
    }
}