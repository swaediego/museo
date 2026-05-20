package com.uneg.galeria.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "admin")
@PrimaryKeyJoinColumn(name = "id_usuario")
@Data
@EqualsAndHashCode(callSuper = true)
public class Admin extends User {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cargo", nullable = false)
    private Cargo cargo;

    @Column
    private String rol;
}