package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Genre;
import com.uneg.galeria.repositories.GenreRepository;
import com.uneg.galeria.services.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/genres")
@CrossOrigin(origins = "http://localhost:3000")
public class GenreController {

    @Autowired private GenreRepository genreRepository;
    @Autowired private GenreService genreService;

    @GetMapping
    public List<Genre> getAll() { return genreRepository.findAll(); }

    @PostMapping
    public Genre create(@RequestBody Genre genre) { return genreRepository.save(genre); }

    @PutMapping("/{id}")
    public Genre update(@PathVariable Long id, @RequestBody Genre genre) {
        genre.setId(id);
        return genreRepository.save(genre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            genreService.deleteGenre(id); // Asegúrate de tener este método en tu Service
            return ResponseEntity.noContent().build();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body("No se puede borrar el género porque existen obras asociadas a él.");
        }
    }
}