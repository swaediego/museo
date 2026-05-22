package com.uneg.galeria.repositories;

import com.uneg.galeria.models.Artist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // Buscar artista por nombre
    Optional<Artist> findByNombre(String nombre);

    // Buscar artista por nombre (insensible a mayúsculas)
    Optional<Artist> findByNombreIgnoreCase(String nombre);
}