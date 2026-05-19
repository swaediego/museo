package com.uneg.galeria.controllers;

import com.uneg.galeria.documents.ArtCatalogDocument;
import com.uneg.galeria.models.Art;
import com.uneg.galeria.repositories.ArtRepository;
import com.uneg.galeria.services.CatalogService;
import com.uneg.galeria.services.DataMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final DataMigrationService migrationService;
    private final CatalogService catalogService;
    private final ArtRepository artRepository;

    @PostMapping("/migrate-all")
    public ResponseEntity<Map<String, String>> migrateAll() {
        migrationService.migrateAllArtToMongo();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Migración completada exitosamente");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/migrate/{id}")
    public ResponseEntity<Map<String, String>> migrateById(@PathVariable Long id) {
        migrationService.migrateArtById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Obra con id " + id + " migrada exitosamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<ArtCatalogDocument>> getCatalog() {
        return ResponseEntity.ok(migrationService.getMigratedDocuments());
    }

    @DeleteMapping("/catalog")
    public ResponseEntity<Map<String, String>> clearCatalog() {
        catalogService.deleteAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Catálogo de MongoDB eliminado");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncCatalog() {
        List<ArtCatalogDocument> mongoDocs = catalogService.findAll();
        List<Art> postgresArts = artRepository.findAll();
        List<Long> postgresIds = postgresArts.stream().map(Art::getId).toList();

        int deleted = 0;
        for (ArtCatalogDocument doc : mongoDocs) {
            if (!postgresIds.contains(doc.getIdRelacional())) {
                catalogService.deleteByIdRelacional(doc.getIdRelacional());
                deleted++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sincronización completada");
        response.put("deletedOrphans", deleted);
        return ResponseEntity.ok(response);
    }
}