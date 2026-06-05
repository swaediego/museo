package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.HistorialPrecio;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialPrecioRepository extends CassandraRepository<HistorialPrecio, String> {

    @Query("SELECT * FROM historial_precio_por_obra WHERE id_relacional = ?0")
    List<HistorialPrecio> findByIdRelacional(Integer idRelacional);
}