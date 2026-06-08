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
@Table("eventos_por_tipo_severidad")
public class EventoPorTipoSeveridad {

    @PrimaryKeyColumn(name = "tipo_evento", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String tipoEvento;

    @PrimaryKeyColumn(name = "severidad", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String severidad;

    @PrimaryKeyColumn(name = "fecha_evento", ordinal = 2)
    private Instant fechaEvento;

    @PrimaryKeyColumn(name = "id_entidad", ordinal = 3)
    private Integer idEntidad;

    @Column("detalle_json")
    private String detalleJson;

    @Column("usuario_origen")
    private String usuarioOrigen;
}
