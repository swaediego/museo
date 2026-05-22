package com.uneg.galeria.services.impl;

import com.uneg.galeria.documents.ArtCatalogDocument;
import com.uneg.galeria.repositories.ArtCatalogRepository;
import com.uneg.galeria.services.CatalogService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final ArtCatalogRepository repository;
    private final MongoTemplate mongoTemplate;

    @Override
    public void save(ArtCatalogDocument document) {
        repository.save(document);
    }

    @Override
    public void saveAll(List<ArtCatalogDocument> documents) {
        repository.saveAll(documents);
    }

    @Override
    public List<ArtCatalogDocument> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ArtCatalogDocument> findByIdRelacional(Long idRelacional) {
        return repository.findByIdRelacional(idRelacional);
    }

    @Override
    public List<ArtCatalogDocument> findByGenero(String genero) {
        return repository.findByGenero(genero);
    }

    @Override
    public List<ArtCatalogDocument> findByEstatus(String estatus) {
        return repository.findByEstatus(estatus);
    }

    @Override
    public List<ArtCatalogDocument> findByPrecioRange(Double min, Double max) {
        return repository.findByPrecioBetween(min, max);
    }

    @Override
public List<ArtCatalogDocument> filterByPrecioGeneroEstatus(Double precioMin, Double precioMax, String genero, String estatus, String sortBy) {
        List<AggregationOperation> operations = new ArrayList<>();

        if (precioMin != null && precioMax != null) {
            operations.add(match(where("precio").gte(precioMin).lte(precioMax)));
        } else if (precioMin != null) {
            operations.add(match(where("precio").gte(precioMin)));
        } else if (precioMax != null) {
            operations.add(match(where("precio").lte(precioMax)));
        }

        if (genero != null && !genero.isEmpty()) {
            operations.add(match(where("genero").is(genero)));
        }

        if (estatus != null && !estatus.isEmpty()) {
            operations.add(match(where("estatus").is(estatus)));
        } else {
            operations.add(match(where("estatus").nin("Vendida", "Vendido")));
        }

        if ("precioAsc".equalsIgnoreCase(sortBy)) {
            operations.add(Aggregation.sort(Sort.Direction.ASC, "precio"));
        } else if ("precioDesc".equalsIgnoreCase(sortBy)) {
            operations.add(Aggregation.sort(Sort.Direction.DESC, "precio"));
        }

        Aggregation aggregation = newAggregation(operations);
        AggregationResults<ArtCatalogDocument> results = mongoTemplate.aggregate(
            aggregation, "art_catalog", ArtCatalogDocument.class);
        return results.getMappedResults();
    }

    @Override
    public List<ArtCatalogDocument> aggregateByGeneroCount() {
        Aggregation aggregation = newAggregation(
            group("genero").count().as("count"),
            project("count").and("_id").as("genero")
        );
        AggregationResults<ArtCatalogDocument> results = mongoTemplate.aggregate(
            aggregation, "art_catalog", ArtCatalogDocument.class);
        return results.getMappedResults();
    }

    @Override
    public List<ArtCatalogDocument> aggregateByArtistaNacionalidad() {
        Aggregation aggregation = newAggregation(
            group("artista.nacionalidad").count().as("count"),
            project("count").and("_id").as("nacionalidad")
        );
        AggregationResults<ArtCatalogDocument> results = mongoTemplate.aggregate(
            aggregation, "art_catalog", ArtCatalogDocument.class);
        return results.getMappedResults();
    }

    @Override
    public long countByGenero(String genero) {
        return repository.findByGenero(genero).size();
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public void deleteByIdRelacional(Long idRelacional) {
        repository.deleteByIdRelacional(idRelacional);
    }
}