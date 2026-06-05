package com.uneg.galeria.history.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("exhibiciones_temporales")
public class ExhibicionTemporal {

    @PrimaryKeyColumn(name = "id_sala", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Integer idSala;

    @PrimaryKeyColumn(name = "fecha_inicio", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private Instant fechaInicio;

    @PrimaryKeyColumn(name = "id_exhibicion", ordinal = 2)
    private UUID idExhibicion;

    @Column("nombre")
    private String nombre;

    @Column("descripcion")
    private String descripcion;

    @Column("fecha_fin")
    private Instant fechaFin;

    @Column("obras_incluidas")
    private List<Integer> obrasIncluidas;

    @Column("curador")
    private String curador;
}
