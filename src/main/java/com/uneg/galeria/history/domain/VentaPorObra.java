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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ventas_por_obra")
public class VentaPorObra {

    @PrimaryKeyColumn(name = "id_relacional", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Integer idRelacional;

    @PrimaryKeyColumn(name = "fecha_venta", ordinal = 1)
    private Instant fechaVenta;

    @PrimaryKeyColumn(name = "id_factura", ordinal = 2)
    private Integer idFactura;

    @Column("monto")
    private BigDecimal monto;

    @Column("impuesto")
    private BigDecimal impuesto;

    @Column("id_comprador")
    private Integer idComprador;
}
