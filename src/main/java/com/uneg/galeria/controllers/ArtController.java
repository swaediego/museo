package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Art;
import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.repositories.BuyerRepository;
import com.uneg.galeria.services.ArtService;
import com.uneg.galeria.repositories.ArtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/arts")
@CrossOrigin(origins = "http://localhost:3000")
public  class ArtController {

    @Autowired
    private ArtService artService;

    @Autowired
    private ArtRepository artRepository;

    @Autowired
    private BuyerRepository buyerRepository;

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

    //6. Reservar una Obra
    @PostMapping("/{id}/reservar/{compradorId}")
    public ResponseEntity<?> reservarObra(@PathVariable Long id, @PathVariable Long compradorId) {
        try {
            artService.reservarObra(id, compradorId);
            return ResponseEntity.ok().body("Obra reservada correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Endpoint para cancelar la reserva de una obra
    @PostMapping("/{id}/cancelar-reserva")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id) {
        try {
            Art art = artService.cancelarReserva(id);
            return ResponseEntity.ok(art);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //8. Modificar una obra existente
    @PutMapping("/{id}")
    public ResponseEntity<Art> update(@PathVariable Long id, @RequestBody Art artDetails) {
        return artService.obtenerPorId(id)
                .map(art -> {
                    art.setNombre(artDetails.getNombre());
                    art.setPrecioBase(artDetails.getPrecioBase());
                    art.setImagenUrl(artDetails.getImagenUrl());
                    art.setEstatus(artDetails.getEstatus());
                    art.setArtista(artDetails.getArtista());
                    art.setGenero(artDetails.getGenero());

                    return ResponseEntity.ok(artService.guardarObra(art));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //9. Borrar una obra
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (artService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        artService.eliminarObra(id);
        return ResponseEntity.noContent().build();
    }

    //10. Filtrar por estatus
    @GetMapping("/status/{estatus}")
    public List<Art> getByEstatus(@PathVariable String estatus) {
        return artRepository.findByEstatus(estatus);
    }

    //11. Filtrar por artista
    // En ArtController.java
    @GetMapping("/artist/{artistaId}")
    public ResponseEntity<List<Art>> getByArtist(@PathVariable Long artistaId) {
        return ResponseEntity.ok(artService.buscarPorArtista(artistaId));
    }
}