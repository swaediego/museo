package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.BitacoraPorTipo;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BitacoraPorTipoRepository extends CassandraRepository<BitacoraPorTipo, String> {

    @Query("SELECT * FROM bitacora_por_tipo WHERE periodo_dia = ?0 AND tipo_evento = ?1")
    List<BitacoraPorTipo> findByPeriodoDiaAndTipoEvento(String periodoDia, String tipoEvento);

    @Query("SELECT * FROM bitacora_por_tipo WHERE periodo_dia = ?0 AND tipo_evento = ?1 AND timestamp_evento >= ?2 AND timestamp_evento <= ?3")
    List<BitacoraPorTipo> findByPeriodoDiaAndTipoEventoAndTimestampBetween(
            String periodoDia, String tipoEvento, Instant desde, Instant hasta);
}
