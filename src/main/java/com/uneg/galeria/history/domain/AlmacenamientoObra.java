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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("almacenamiento_obras")
public class AlmacenamientoObra {

    @PrimaryKeyColumn(name = "id_zona_almacen", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Integer idZonaAlmacen;

    @PrimaryKeyColumn(name = "id_relacional", ordinal = 1)
    private Integer idRelacional;

    @Column("nombre_obra")
    private String nombreObra;

    @Column("categoria")
    private String categoria;

    @Column("fecha_ingreso")
    private Instant fechaIngreso;

    @Column("condiciones")
    private String condiciones;

    @Column("posicion")
    private String posicion;
}
