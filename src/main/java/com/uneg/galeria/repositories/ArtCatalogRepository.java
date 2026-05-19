package com.uneg.galeria.repositories;

import com.uneg.galeria.documents.ArtCatalogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtCatalogRepository extends MongoRepository<ArtCatalogDocument, String> {
    Optional<ArtCatalogDocument> findByIdRelacional(Long idRelacional);
    List<ArtCatalogDocument> findByGenero(String genero);
    List<ArtCatalogDocument> findByEstatus(String estatus);
    List<ArtCatalogDocument> findByPrecioBetween(Double min, Double max);
    List<ArtCatalogDocument> findByGeneroAndEstatus(String genero, String estatus);
    void deleteByIdRelacional(Long idRelacional);
}