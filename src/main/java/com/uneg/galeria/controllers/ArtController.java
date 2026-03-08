package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Art;
import com.uneg.galeria.services.ArtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/arts")
@CrossOrigin(origins = "http://localhost:3000")
public  class ArtController {

    @Autowired
    private ArtService artService;

    // 1. Obtener todas las obras disponibles (Galería principal)
    @GetMapping
    public ResponseEntity<List<Art>> getAllAvailable() {
        List<Art> arts = artService.obtenerTodasDisponibles();
        return ResponseEntity.ok(arts);
    }

    // 2. Obtener una obra por ID (Para la página de detalles/indagar)
    @GetMapping("/{id}")
    public ResponseEntity<Art> getById(@PathVariable Long id) {
        return artService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Filtrar por Género (Ej: /api/arts/genre/Pintura)
    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<Art>> getByGenre(@PathVariable String genreName) {
        return ResponseEntity.ok(artService.buscarPorGenero(genreName));
    }

    // 4. Listar por precio de menor a mayor
    @GetMapping("/sort/price")
    public ResponseEntity<List<Art>> sortByPrice() {
        return ResponseEntity.ok(artService.listarPorPrecioMenorAMayor());
    }

    // 5. Guardar una nueva obra (Uso administrativo)
    @PostMapping
    public ResponseEntity<Art> create(@RequestBody Art art) {
        return ResponseEntity.ok(artService.guardarObra(art));
    }

    @PatchMapping("/{id}/reservar")
    public ResponseEntity<?> reservarObra(@PathVariable Long id) {
        return artService.obtenerPorId(id)
                .map(art -> {
                    if (!"Disponible".equalsIgnoreCase(art.getEstatus())) {
                        // Retornamos un error 409 Conflict con un mensaje
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("La obra no está disponible (Estatus: " + art.getEstatus() + ")");
                    }

                    art.setEstatus("Reservada");
                    artService.guardarObra(art);
                    return ResponseEntity.ok(art);
                })
                // El orElse debe devolver el mismo tipo: ResponseEntity<?>
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

