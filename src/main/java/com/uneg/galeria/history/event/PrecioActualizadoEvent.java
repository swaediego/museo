package com.uneg.galeria.history.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PrecioActualizadoEvent(
    Integer idRelacional,
    BigDecimal precioAnterior,
    BigDecimal precioNuevo,
    String motivo,
    String usuario,
    Instant fecha
) {}