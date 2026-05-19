package com.uneg.galeria.services;

import com.uneg.galeria.documents.ArtCatalogDocument;
import java.util.List;

public interface DataMigrationService {
    void migrateAllArtToMongo();
    void migrateArtById(Long id);
    List<ArtCatalogDocument> getMigratedDocuments();
}