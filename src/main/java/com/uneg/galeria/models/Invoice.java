package com.uneg.galeria.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Data
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cada factura debe incluir solo una obra
    @OneToOne
    @JoinColumn(name = "id_obra", nullable = false, unique = true)
    private Art obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comprador", nullable = false)
    private Buyer comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin", nullable = false)
    private Admin administrador;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double iva; // Calculado sobre el subtotal

    @Column(name = "porcentaje_ganancia", nullable = false)
    private Double porcentajeGanancia; // Entre 5% y 10%

    @Column(name = "monto_ganancia", nullable = false)
    private Double montoGanancia;

    @Column(nullable = false)
    private Double total; // Subtotal + IVA

    @Column(name = "direccion_destino", nullable = false)
    private String direccionDestino;
}