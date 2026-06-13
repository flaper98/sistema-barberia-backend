package com.barberia.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoItem tipo;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "cantidad", nullable = false)
    @Builder.Default
    private Integer cantidad = 1;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
