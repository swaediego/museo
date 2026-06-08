package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.VentaPorObra;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaPorObraRepository extends CassandraRepository<VentaPorObra, Integer> {

    @Query("SELECT * FROM ventas_por_obra WHERE id_relacional = ?0")
    List<VentaPorObra> findByIdRelacional(Integer idRelacional);
}
