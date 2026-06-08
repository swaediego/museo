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

    @Transient
    public void setIdCargo(Long idCargo) {
        if (idCargo != null) {
            if (this.cargo == null) {
                this.cargo = new Cargo();
            }
            this.cargo.setId(idCargo);
        }
    }

    @Transient
    public void setCargo(String cargoNombre) {
        if (cargoNombre != null) {
            if (this.cargo == null) {
                this.cargo = new Cargo();
            }
            this.cargo.setNombre(cargoNombre);
        }
    }

    @Transient
    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }
}