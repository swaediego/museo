package com.uneg.galeria.repositories;

import com.uneg.galeria.models.Art;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface ArtRepository extends JpaRepository<Art, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"artista", "genero", "artista.biografia"})
    List<Art> findAll();

    List<Art> findAllByIdIn(List<Long> ids);

    // 1. Buscar obras por género (usando el nombre del género)
    List<Art> findByGeneroNombreIgnoreCase(String nombreGenero);

    // 2. Buscar obras por artista (usando el ID del artista para el link) [cite: 10, 13]
    List<Art> findByArtistaId(Long artistId);

    // 3. Buscar obras disponibles filtradas por precio (Menor a Mayor) [cite: 14, 21]
    // Solo mostramos las que tienen estatus 'Disponible'
    List<Art> findByEstatusOrderByPrecioBaseAsc(String estatus);

    // 4. Búsqueda combinada: Por género y que estén disponibles [cite: 12, 21]
    List<Art> findByGeneroNombreIgnoreCaseAndEstatus(String nombreGenero, String estatus);

    List<Art> findByEstatus(String estatus);

    // 5. Buscar obras reservadas por un comprador específico
    List<Art> findByCompradorReservaId(Long compradorReservaId);

    // 6. Buscar obras reservadas por un comprador específico y estatus
    List<Art> findByCompradorReservaIdAndEstatus(Long compradorReservaId, String estatus);

    boolean existsByMetObjectId(Long metObjectId);

    Optional<Art> findByMetObjectId(Long metObjectId);
}