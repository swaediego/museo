package com.uneg.galeria.controllers;

import com.uneg.galeria.dto.BuscarObrasResponse;
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
    public ResponseEntity<?> buscarObras(@RequestBody Map<String, String> body) {
        String busqueda = body.get("busqueda");
        String artista = body.get("artista");
        if (busqueda == null || busqueda.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ImportArtResponse.error("Debe proporcionar un término de búsqueda"));
        }
        
        try {
            BuscarObrasResponse response = artImportService.buscarObrasConSugerencias(busqueda, artista);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ImportController] Error en búsqueda: " + e.getMessage());
            return ResponseEntity.ok(BuscarObrasResponse.noEncontrado(busqueda, List.of()));
        }
    }

    @PostMapping
    public ResponseEntity<ImportArtResponse> importarObra(@RequestBody ImportArtRequest request) {
        // Validaciones
        if (request.getObjectId() == null) {
            return ResponseEntity.badRequest()
                .body(ImportArtResponse.error("objectId es requerido para importar una obra"));
        }
        
        // Validar longitud máxima de campos para evitar inyecciones
        if (request.getTituloEspanol() != null && request.getTituloEspanol().length() > 255) {
            return ResponseEntity.badRequest()
                .body(ImportArtResponse.error("El título no puede exceder 255 caracteres"));
        }
        
        try {
            ImportArtResponse response = artImportService.importarObra(request);
            
            // Si la obra no se encontró en ninguna fuente, devolver mensaje amigable
            if (!response.isSuccess() && response.getMessage() != null 
                && response.getMessage().contains("No se pudo obtener")) {
                response.setMessage("La obra no está disponible para la venta en este momento");
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("[ImportController] Error en importación: " + e.getMessage());
            // Mensaje amigable para el usuario
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("No se pudo obtener")) {
                return ResponseEntity.ok(
                    ImportArtResponse.error("La obra no está disponible para la venta en este momento")
                );
            }
            return ResponseEntity.status(502).body(
                ImportArtResponse.error("Error en la importacion: " + mensaje)
            );
        }
    }
}