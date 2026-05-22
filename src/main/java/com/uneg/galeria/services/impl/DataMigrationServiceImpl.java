package com.uneg.galeria.services.impl;

import com.uneg.galeria.documents.ArtCatalogDocument;
import com.uneg.galeria.documents.ArtCatalogDocument.EmbeddedArtist;
import com.uneg.galeria.models.*;
import com.uneg.galeria.repositories.ArtCatalogRepository;
import com.uneg.galeria.repositories.ArtRepository;
import com.uneg.galeria.services.CatalogService;
import com.uneg.galeria.services.DataMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataMigrationServiceImpl implements DataMigrationService {

    private final ArtRepository artRepository;
    private final CatalogService catalogService;
    private final ArtCatalogRepository catalogRepository;

    @Override
    @Transactional
    public void migrateAllArtToMongo() {
        List<Art> obras = artRepository.findAll();
        List<ArtCatalogDocument> documents = new ArrayList<>();

        for (Art obra : obras) {
            ArtCatalogDocument doc = mapArtToDocument(obra);
            documents.add(doc);
        }

        catalogService.deleteAll();
        catalogService.saveAll(documents);
    }

    @Override
    public void migrateArtById(Long id) {
        artRepository.findById(id).ifPresent(obra -> {
            ArtCatalogDocument doc = mapArtToDocument(obra);
            catalogRepository.findByIdRelacional(id).ifPresent(existing -> {
                doc.setId(existing.getId());
            });
            catalogService.save(doc);
        });
    }

    @Override
    public List<ArtCatalogDocument> getMigratedDocuments() {
        return catalogService.findAll();
    }

    private ArtCatalogDocument mapArtToDocument(Art obra) {
        ArtCatalogDocument doc = new ArtCatalogDocument();
        doc.setIdRelacional(obra.getId());
        doc.setNombre(obra.getNombre());
        doc.setPrecio(obra.getPrecioBase());
        doc.setEstatus(obra.getEstatus());
        doc.setImagenUrl(obra.getImagenUrl());
        doc.setFechaCreacion(obra.getFechaCreacion());

        EmbeddedArtist embeddedArtist = new EmbeddedArtist();
        embeddedArtist.setIdArtistaRelacional(obra.getArtista().getId());
        embeddedArtist.setNombre(obra.getArtista().getNombre());
        embeddedArtist.setNacionalidad(obra.getArtista().getNacionalidad());
        embeddedArtist.setBiografia(obra.getArtista().getBiografia());
        doc.setArtista(embeddedArtist);

        String genero = obra.getGenero().getNombre();
        doc.setGenero(genero);

        Map<String, Object> detalles = new HashMap<>();

        if (obra instanceof Painting painting) {
            detalles.put("tecnica", painting.getTecnica());
            detalles.put("estilo", painting.getEstilo());
        } else if (obra instanceof Sculpture sculpture) {
            detalles.put("material", sculpture.getMaterial());
            detalles.put("peso", sculpture.getPeso());
            Map<String, Object> dimensiones = new HashMap<>();
            dimensiones.put("largo", sculpture.getLargo());
            dimensiones.put("ancho", sculpture.getAncho());
            dimensiones.put("profundidad", sculpture.getProfundidad());
            detalles.put("dimensiones", dimensiones);
        } else if (obra instanceof Photograph photograph) {
            detalles.put("tipoImpresion", photograph.getTipoImpresion());
            detalles.put("papel", photograph.getPapel());
            detalles.put("edicion", photograph.getEdicion());
        } else if (obra instanceof Ceramic ceramic) {
            detalles.put("tipoArcilla", ceramic.getTipoArcilla());
            detalles.put("temperaturaCoccion", ceramic.getTemperaturaCoccion());
        } else if (obra instanceof Orphebrery orphebrery) {
            detalles.put("purezaMetal", orphebrery.getPurezaMetal());
            detalles.put("peso", orphebrery.getPeso());
            detalles.put("metalBase", orphebrery.getMetalBase());
        }

        doc.setDetallesEspecificos(detalles);
        return doc;
    }
}