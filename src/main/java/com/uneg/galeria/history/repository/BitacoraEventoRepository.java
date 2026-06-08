package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.BitacoraEvento;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BitacoraEventoRepository extends CassandraRepository<BitacoraEvento, String> {

    @Query("SELECT * FROM bitacora_eventos WHERE periodo_dia = ?0")
    List<BitacoraEvento> findByKeyPeriodoDia(String periodoDia);
}