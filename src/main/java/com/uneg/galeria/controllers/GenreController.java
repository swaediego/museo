package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Genre;
import com.uneg.galeria.services.GenreService;
import com.uneg.galeria.repositories.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/genres")
@CrossOrigin(origins = "http://localhost:3000")
public class GenreController {

    @Autowired private GenreRepository genreRepository;

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
    public void delete(@PathVariable Long id) { genreRepository.deleteById(id); }
}