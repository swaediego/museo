package com.uneg.galeria.services;

import com.uneg.galeria.documents.ArtCatalogDocument;
import java.util.List;
import java.util.Optional;

public interface CatalogService {
    void save(ArtCatalogDocument document);
    void saveAll(List<ArtCatalogDocument> documents);
    List<ArtCatalogDocument> findAll();
    Optional<ArtCatalogDocument> findByIdRelacional(Long idRelacional);
    List<ArtCatalogDocument> findByGenero(String genero);
    List<ArtCatalogDocument> findByEstatus(String estatus);
    List<ArtCatalogDocument> findByPrecioRange(Double min, Double max);
    List<ArtCatalogDocument> filterByPrecioGeneroEstatus(Double precioMin, Double precioMax, String genero, String estatus, String sortBy);
    List<ArtCatalogDocument> aggregateByGeneroCount();
    List<ArtCatalogDocument> aggregateByArtistaNacionalidad();
    long countByGenero(String genero);
    void deleteAll();
    void deleteByIdRelacional(Long idRelacional);
}