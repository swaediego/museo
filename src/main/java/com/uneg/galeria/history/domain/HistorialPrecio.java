package com.uneg.galeria.history.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("historial_precio_por_obra")
public class HistorialPrecio {

    @PrimaryKeyColumn(name = "id_relacional", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Integer idRelacional;

    @PrimaryKeyColumn(name = "fecha_cambio", ordinal = 1)
    private Instant fechaCambio;

    @PrimaryKeyColumn(name = "id_evento", ordinal = 2)
    private UUID idEvento;

    @Column("precio_anterior")
    private BigDecimal precioAnterior;

    @Column("precio_nuevo")
    private BigDecimal precioNuevo;

    @Column("motivo")
    private String motivo;

    @Column("usuario_admin")
    private String usuarioAdmin;
}