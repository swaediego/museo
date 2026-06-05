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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ingresos_por_categoria")
public class IngresoPorCategoria {

    @PrimaryKeyColumn(name = "categoria", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String categoria;

    @PrimaryKeyColumn(name = "periodo", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String periodo;

    @PrimaryKeyColumn(name = "id_factura", ordinal = 2)
    private Integer idFactura;

    @Column("id_obra")
    private Integer idObra;

    @Column("monto")
    private BigDecimal monto;

    @Column("impuesto")
    private BigDecimal impuesto;
}
