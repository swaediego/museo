package com.uneg.galeria.history.event;

import java.math.BigDecimal;
import java.time.Instant;

public record VentaRegistradaEvent(
    Integer idFactura,
    Integer idObra,
    Integer idComprador,
    BigDecimal monto,
    BigDecimal impuesto,
    String metodoPago,
    String estatusFactura,
    Instant fechaVenta
) {}