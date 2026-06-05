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
@Table("bitacora_por_tipo")
public class BitacoraPorTipo {

    @PrimaryKeyColumn(name = "periodo_dia", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String periodoDia;

    @PrimaryKeyColumn(name = "tipo_evento", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String tipoEvento;

    @PrimaryKeyColumn(name = "timestamp_evento", ordinal = 2)
    private Instant timestampEvento;

    @Column("id_entidad")
    private Integer idEntidad;

    @Column("tipo_entidad")
    private String tipoEntidad;

    @Column("detalle_json")
    private String detalleJson;

    @Column("severidad")
    private String severidad;

    @Column("usuario_origen")
    private String usuarioOrigen;
}
