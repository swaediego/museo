package com.uneg.galeria.config;

import com.uneg.galeria.services.CatalogService;
import com.uneg.galeria.services.DataMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupSyncConfig {

    @Bean
    public CommandLineRunner runStartupSync(DataMigrationService migrationService, CatalogService catalogService) {
        return args -> {
            if (catalogService.findAll().isEmpty()) {
                System.out.println("====== [Startup] La base de datos de MongoDB está vacía. Iniciando sincronización de semillas (PostgreSQL -> MongoDB) ======");
                migrationService.migrateAllArtToMongo();
                System.out.println("====== [Startup] Sincronización inicial completada con éxito. ======");
            } else {
                System.out.println("====== [Startup] MongoDB ya contiene datos. Omitiendo sincronización inicial. ======");
            }
        };
    }
}
