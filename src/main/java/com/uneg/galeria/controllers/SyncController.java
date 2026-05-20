package com.uneg.galeria.controllers;

import com.uneg.galeria.dto.SyncRequest;
import com.uneg.galeria.dto.SyncResponse;
import com.uneg.galeria.services.ArtistService;
import com.uneg.galeria.services.ArtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SyncController {

    private final ArtService artService;
    private final ArtistService artistService;

    @PostMapping
    public ResponseEntity<SyncResponse> syncDeleted(@RequestBody SyncRequest request) {
        List<Long> processed = new ArrayList<>();
        List<Long> notFound = new ArrayList<>();

        if (request == null || request.getDeletedIds() == null) {
            return ResponseEntity.badRequest().body(new SyncResponse(List.of(), List.of(), "Invalid request"));
        }

        for (SyncRequest.DeletedItem item : request.getDeletedIds()) {
            try {
                if ("art".equalsIgnoreCase(item.getType())) {
                    artService.eliminarObra(item.getId());
                    processed.add(item.getId());
                } else if ("artist".equalsIgnoreCase(item.getType())) {
                    artistService.deleteArtist(item.getId());
                    processed.add(item.getId());
                }
            } catch (Exception e) {
                notFound.add(item.getId());
            }
        }

        String message = notFound.isEmpty()
            ? "Sincronización completada"
            : "Sincronización completada con errores";

        return ResponseEntity.ok(new SyncResponse(processed, notFound, message));
    }
}