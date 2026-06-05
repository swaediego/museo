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
@Table("auditoria_ventas_por_periodo")
public class AuditoriaVenta {

    @PrimaryKeyColumn(name = "periodo", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String periodo;

    @PrimaryKeyColumn(name = "fecha_venta", ordinal = 1)
    private Instant fechaVenta;

    @PrimaryKeyColumn(name = "id_factura", ordinal = 2)
    private Integer idFactura;

    @Column("id_obra")
    private Integer idObra;

    @Column("id_comprador")
    private Integer idComprador;

    @Column("monto")
    private BigDecimal monto;

    @Column("impuesto")
    private BigDecimal impuesto;

    @Column("metodo_pago")
    private String metodoPago;

    @Column("estatus_factura")
    private String estatusFactura;
}