package com.uneg.galeria.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;



@Entity
@Table(name = "buyer")
@PrimaryKeyJoinColumn(name = "id_usuario")
@Data
@EqualsAndHashCode(callSuper = true)

public class Buyer extends User {

    @Column(name = "datos_tarjeta_mask", nullable = false)
    private String datosTarjetaMask; // Para cumplir con el registro de tarjeta

    @Column(name = "membresia_paga")
    private Boolean membresiaPaga = false;

    @Column(name = "direccion_envio", nullable = false)
    private String direccionEnvio;

    @Column(name = "codigo_seguridad", length = 10)
    private String codigoSeguridad; // Generado aleatoriamente por el sistema
}
