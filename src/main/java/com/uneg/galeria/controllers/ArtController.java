package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Art;
import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.models.Sculpture;
import com.uneg.galeria.models.Painting;
import com.uneg.galeria.models.Photograph;
import com.uneg.galeria.models.Ceramic;
import com.uneg.galeria.models.Orphebrery;
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

    // 1b. Obtener TODAS las obras sin filtro (para admin)
    @GetMapping("/all")
    public ResponseEntity<List<Art>> getAll() {
        return ResponseEntity.ok(artService.obtenerTodas());
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
    public ResponseEntity<?> reservarObra(
            @PathVariable Long id, 
            @PathVariable Long compradorId, 
            @RequestParam String securityCode) {
        try {
            artService.reservarObra(id, compradorId, securityCode);
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
                    if (artDetails.getNombre() != null && !artDetails.getNombre().isBlank()) art.setNombre(artDetails.getNombre());
                    if (artDetails.getPrecioBase() != null) art.setPrecioBase(artDetails.getPrecioBase());
                    if (artDetails.getImagenUrl() != null && !artDetails.getImagenUrl().isBlank()) art.setImagenUrl(artDetails.getImagenUrl());
                    if (artDetails.getEstatus() != null && !artDetails.getEstatus().isBlank()) art.setEstatus(artDetails.getEstatus());
                    if (artDetails.getArtista() != null && artDetails.getArtista().getId() != null) art.setArtista(artDetails.getArtista());
                    if (artDetails.getGenero() != null && artDetails.getGenero().getId() != null) art.setGenero(artDetails.getGenero());

                    if (artDetails instanceof Sculpture && art instanceof Sculpture) {
                        Sculpture sculpture = (Sculpture) art;
                        Sculpture sculptureDetails = (Sculpture) artDetails;
                        if (sculptureDetails.getMaterial() != null) sculpture.setMaterial(sculptureDetails.getMaterial());
                        if (sculptureDetails.getPeso() != null) sculpture.setPeso(sculptureDetails.getPeso());
                        if (sculptureDetails.getLargo() != null) sculpture.setLargo(sculptureDetails.getLargo());
                        if (sculptureDetails.getAncho() != null) sculpture.setAncho(sculptureDetails.getAncho());
                        if (sculptureDetails.getProfundidad() != null) sculpture.setProfundidad(sculptureDetails.getProfundidad());
                    } else if (artDetails instanceof Painting && art instanceof Painting) {
                        Painting painting = (Painting) art;
                        Painting details = (Painting) artDetails;
                        if (details.getTecnica() != null) painting.setTecnica(details.getTecnica());
                        if (details.getEstilo() != null) painting.setEstilo(details.getEstilo());
                    } else if (artDetails instanceof Photograph && art instanceof Photograph) {
                        Photograph photo = (Photograph) art;
                        Photograph details = (Photograph) artDetails;
                        if (details.getTipoImpresion() != null) photo.setTipoImpresion(details.getTipoImpresion());
                        if (details.getPapel() != null) photo.setPapel(details.getPapel());
                        if (details.getEdicion() != null) photo.setEdicion(details.getEdicion());
                    } else if (artDetails instanceof Ceramic && art instanceof Ceramic) {
                        Ceramic ceramic = (Ceramic) art;
                        Ceramic details = (Ceramic) artDetails;
                        if (details.getTipoArcilla() != null) ceramic.setTipoArcilla(details.getTipoArcilla());
                        if (details.getTemperaturaCoccion() != null) ceramic.setTemperaturaCoccion(details.getTemperaturaCoccion());
                    } else if (artDetails instanceof Orphebrery && art instanceof Orphebrery) {
                        Orphebrery orph = (Orphebrery) art;
                        Orphebrery details = (Orphebrery) artDetails;
                        if (details.getPurezaMetal() != null) orph.setPurezaMetal(details.getPurezaMetal());
                        if (details.getPeso() != null) orph.setPeso(details.getPeso());
                        if (details.getMetalBase() != null) orph.setMetalBase(details.getMetalBase());
                    }

                    return ResponseEntity.ok(artService.guardarObra(art));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //9. Borrar una obra
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (artService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (artService.esReservada(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("No se puede eliminar una obra reservada. Cancele la reserva primero.");
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

    //12. Verificar si una obra está reservada
    @GetMapping("/{id}/reservada")
    public ResponseEntity<?> esReservada(@PathVariable Long id) {
        return ResponseEntity.ok(artService.esReservada(id));
    }
}