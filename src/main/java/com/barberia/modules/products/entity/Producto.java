package com.barberia.modules.products.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "categoria", nullable = false, length = 80)
    @Builder.Default
    private String categoria = "Otro";

    @Column(name = "precio_compra", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioCompra = BigDecimal.ZERO;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock_actual", nullable = false)
    @Builder.Default
    private Integer stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    @Builder.Default
    private Integer stockMinimo = 5;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "codigo_barras", unique = true, length = 50)
    private String codigoBarras;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isStockBajo() {
        return stockActual <= stockMinimo;
    }
}
