package com.uneg.galeria.projections;

public interface BuyerProjection {
    Long getId();
    String getLogin();
    String getNombre();
    String getApellido();
    String getEmail();
    Boolean getActivo();
    Boolean getMembresiaPaga();
    String getDireccionEnvio();
}