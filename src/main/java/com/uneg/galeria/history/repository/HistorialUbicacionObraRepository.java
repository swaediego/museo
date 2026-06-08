package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.HistorialUbicacionObra;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialUbicacionObraRepository extends CassandraRepository<HistorialUbicacionObra, Integer> {

    @Query("SELECT * FROM historial_ubicacion_obras WHERE id_relacional = ?0")
    List<HistorialUbicacionObra> findByIdRelacional(Integer idRelacional);
}
