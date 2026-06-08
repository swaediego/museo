package com.uneg.galeria.history.event;

import java.time.Instant;

public record EstatusCambiadoEvent(
    Integer idRelacional,
    String entidadTipo,
    String estatusAnterior,
    String estatusNuevo,
    String usuario,
    Instant fecha
) {}