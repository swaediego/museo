package com.uneg.galeria.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "art")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Art {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "precio_base", nullable = false)
    private Double precioBase;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(length = 20)
    private String estatus = "Disponible"; // Puede ser: Disponible, Reservada, Vendida

    @Column(name = "imagen_url", nullable = false)
    private String imagenUrl;

    // Relación con el artista (Muchos a Uno)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artista", nullable = false)
    private Artist artista;

    // Relación con el género (Muchos a Uno)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_genero", nullable = false)
    private Genre genero;

    @ManyToOne
    @JoinColumn(name = "id_comprador_reserva")
    private Buyer compradorReserva;
}