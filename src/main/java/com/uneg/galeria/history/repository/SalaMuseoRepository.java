package com.uneg.galeria.history.repository;

import com.uneg.galeria.history.domain.SalaMuseo;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaMuseoRepository extends CassandraRepository<SalaMuseo, Integer> {
}
