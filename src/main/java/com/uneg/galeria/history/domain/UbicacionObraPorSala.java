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
@Table("ubicacion_obras_por_sala")
public class UbicacionObraPorSala {

    @PrimaryKeyColumn(name = "id_sala", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Integer idSala;

    @PrimaryKeyColumn(name = "id_relacional", ordinal = 1)
    private Integer idRelacional;

    @Column("nombre_obra")
    private String nombreObra;

    @Column("nombre_artista")
    private String nombreArtista;

    @Column("fecha_ingreso")
    private Instant fechaIngreso;

    @Column("fecha_salida")
    private Instant fechaSalida;

    @Column("posicion_estante")
    private String posicionEstante;
}
