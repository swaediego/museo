package com.uneg.galeria.controllers;

import com.uneg.galeria.dto.ImportArtRequest;
import com.uneg.galeria.dto.ImportArtResponse;
import com.uneg.galeria.dto.MetSearchResult;
import com.uneg.galeria.services.ArtImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/arts/import")
@CrossOrigin(origins = "http://localhost:3000")
public class ImportController {

    @Autowired
    private ArtImportService artImportService;

    @PostMapping("/buscar")
    public ResponseEntity<List<MetSearchResult>> buscarObras(@RequestBody Map<String, String> body) {
        String busqueda = body.get("busqueda");
        if (busqueda == null || busqueda.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        List<MetSearchResult> resultados = artImportService.buscarObras(busqueda);
        return ResponseEntity.ok(resultados);
    }

    @PostMapping
    public ResponseEntity<ImportArtResponse> importarObra(@RequestBody ImportArtRequest request) {
        if (request.getObjectId() == null) {
            return ResponseEntity.badRequest()
                .body(ImportArtResponse.error("objectId es requerido"));
        }
        try {
            ImportArtResponse response = artImportService.importarObra(request);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(502).body(
                ImportArtResponse.error("Error en la importacion: " + e.getMessage())
            );
        }
    }
}