package com.uneg.galeria.history.event;

import java.time.Instant;

public record ObraEliminadaEvent(
    Integer idRelacional,
    String nombreObra,
    String usuario,
    Instant fecha
) {}