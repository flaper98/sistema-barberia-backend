package com.barberia.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// Un tramo del pago de una venta -- una venta con un solo metodo tiene un
// solo VentaPago (por el total completo); una venta pagada con mas de un
// metodo (ej. mitad efectivo, mitad Yape) tiene uno por cada tramo, y la
// suma de todos siempre coincide con Venta.total.
@Entity
@Table(name = "venta_pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
}
