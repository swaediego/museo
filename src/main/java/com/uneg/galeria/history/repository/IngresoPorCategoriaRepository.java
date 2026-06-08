package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.IngresoPorCategoria;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngresoPorCategoriaRepository extends CassandraRepository<IngresoPorCategoria, String> {

    @Query("SELECT * FROM ingresos_por_categoria WHERE categoria = ?0 AND periodo = ?1")
    List<IngresoPorCategoria> findByCategoriaAndPeriodo(String categoria, String periodo);
}
