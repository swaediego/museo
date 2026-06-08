package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.AuditoriaVenta;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaVentaRepository extends CassandraRepository<AuditoriaVenta, String> {

    @Query("SELECT * FROM auditoria_ventas_por_periodo WHERE periodo = ?0")
    List<AuditoriaVenta> findByKeyPeriodo(String periodo);
}