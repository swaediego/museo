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
@Table("historial_ubicacion_obras")
public class HistorialUbicacionObra {

    @PrimaryKeyColumn(name = "id_relacional", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Integer idRelacional;

    @PrimaryKeyColumn(name = "fecha_ingreso", ordinal = 1)
    private Instant fechaIngreso;

    @Column("id_sala")
    private Integer idSala;

    @Column("nombre_sala")
    private String nombreSala;

    @Column("posicion_estante")
    private String posicionEstante;
}
