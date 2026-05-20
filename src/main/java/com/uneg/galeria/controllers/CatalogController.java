package com.uneg.galeria.controllers;

import com.uneg.galeria.documents.ArtCatalogDocument;
import com.uneg.galeria.services.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<List<ArtCatalogDocument>> getAllArt() {
        return ResponseEntity.ok(catalogService.findAll());
    }

    @GetMapping("/{idRelacional}")
    public ResponseEntity<ArtCatalogDocument> getByIdRelacional(@PathVariable Long idRelacional) {
        return catalogService.findByIdRelacional(idRelacional)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/genero/{genero}")
    public ResponseEntity<List<ArtCatalogDocument>> getByGenero(@PathVariable String genero) {
        return ResponseEntity.ok(catalogService.findByGenero(genero));
    }

    @GetMapping("/estatus/{estatus}")
    public ResponseEntity<List<ArtCatalogDocument>> getByEstatus(@PathVariable String estatus) {
        return ResponseEntity.ok(catalogService.findByEstatus(estatus));
    }

    @GetMapping("/precio")
    public ResponseEntity<List<ArtCatalogDocument>> getByPrecioRange(
            @RequestParam Double min,
            @RequestParam Double max) {
        return ResponseEntity.ok(catalogService.findByPrecioRange(min, max));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<ArtCatalogDocument>> filterArt(
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String estatus,
            @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(catalogService.filterByPrecioGeneroEstatus(
                precioMin, precioMax, genero, estatus, sortBy));
    }

    @GetMapping("/stats/genero")
    public ResponseEntity<List<ArtCatalogDocument>> getStatsByGenero() {
        return ResponseEntity.ok(catalogService.aggregateByGeneroCount());
    }

    @GetMapping("/stats/nacionalidad")
    public ResponseEntity<List<ArtCatalogDocument>> getStatsByNacionalidad() {
        return ResponseEntity.ok(catalogService.aggregateByArtistaNacionalidad());
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> saveArt(@RequestBody ArtCatalogDocument document) {
        catalogService.save(document);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Documento guardado exitosamente");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, String>> saveAll(@RequestBody List<ArtCatalogDocument> documents) {
        catalogService.saveAll(documents);
        Map<String, String> response = new HashMap<>();
        response.put("message", documents.size() + " documentos guardados exitosamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteAll() {
        catalogService.deleteAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Coleccion limpiada");
        return ResponseEntity.ok(response);
    }
}