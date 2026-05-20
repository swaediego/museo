package com.uneg.galeria.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cargo")
@Data
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @Column
    private String descripcion;
}