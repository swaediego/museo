package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Artist;
import com.uneg.galeria.services.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@CrossOrigin(origins = "http://localhost:3000")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @GetMapping
    public List<Artist> getAll() {
        return artistService.getAllArtists();
    }

    // Nuevo: Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<Artist> getById(@PathVariable Long id) {
        Artist artist = artistService.getArtistById(id);
        return artist != null ? ResponseEntity.ok(artist) : ResponseEntity.notFound().build();
    }

    // Nuevo: Crear
    @PostMapping
    public Artist create(@RequestBody Artist artist) {
        return artistService.saveArtist(artist);
    }

    // Nuevo: Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<Artist> update(@PathVariable Long id, @RequestBody Artist artist) {
        Artist existing = artistService.getArtistById(id);
        if (existing == null) return ResponseEntity.notFound().build();

        artist.setId(id); // Aseguramos que el ID sea el correcto
        return ResponseEntity.ok(artistService.saveArtist(artist));
    }

    // Nuevo: Borrar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }
}