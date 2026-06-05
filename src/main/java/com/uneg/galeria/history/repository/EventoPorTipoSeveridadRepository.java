package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.EventoPorTipoSeveridad;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventoPorTipoSeveridadRepository extends CassandraRepository<EventoPorTipoSeveridad, String> {

    @Query("SELECT * FROM eventos_por_tipo_severidad WHERE tipo_evento = ?0 AND severidad = ?1")
    List<EventoPorTipoSeveridad> findByTipoEventoAndSeveridad(String tipoEvento, String severidad);

    @Query("SELECT * FROM eventos_por_tipo_severidad WHERE tipo_evento = ?0 AND severidad = ?1 AND fecha_evento >= ?2 AND fecha_evento <= ?3")
    List<EventoPorTipoSeveridad> findByTipoEventoAndSeveridadAndFechaEventoBetween(
            String tipoEvento, String severidad, Instant desde, Instant hasta);
}
