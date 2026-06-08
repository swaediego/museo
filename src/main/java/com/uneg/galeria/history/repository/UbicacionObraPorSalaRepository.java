package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.UbicacionObraPorSala;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbicacionObraPorSalaRepository extends CassandraRepository<UbicacionObraPorSala, Integer> {

    @Query("SELECT * FROM ubicacion_obras_por_sala WHERE id_sala = ?0")
    List<UbicacionObraPorSala> findByIdSala(Integer idSala);

    @Query("SELECT * FROM ubicacion_obras_por_sala WHERE id_sala = ?0 AND id_relacional = ?1")
    List<UbicacionObraPorSala> findByIdSalaAndIdRelacional(Integer idSala, Integer idRelacional);
}
