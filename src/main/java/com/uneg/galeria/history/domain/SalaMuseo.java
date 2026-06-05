package com.uneg.galeria.history.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("salas_museo")
public class SalaMuseo {

    @PrimaryKey("id_sala")
    private Integer idSala;

    @Column("nombre")
    private String nombre;

    @Column("piso")
    private Integer piso;

    @Column("ala")
    private String ala;

    @Column("capacidad_max")
    private Integer capacidadMax;

    @Column("latitud")
    private Double latitud;

    @Column("longitud")
    private Double longitud;

    @Column("descripcion")
    private String descripcion;
}
