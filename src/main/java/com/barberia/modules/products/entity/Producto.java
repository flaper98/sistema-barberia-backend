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

    // % que se le paga de comision al barbero por cada venta de este
    // producto -- lo define el administrador desde Comisiones -> Tasas de
    // comision (no todos los productos pagan lo mismo).
    @Column(name = "porcentaje_comision", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeComision = BigDecimal.ZERO;

    // Si una venta de este producto suma sello de fidelizacion al cliente --
    // lo define el administrador desde Fidelizacion -> Que aplica sello.
    // Arranca en false: hoy ningun producto solo suma sello, distinto de
    // servicios/paquetes (ver V14__fidelizacion_por_item.sql).
    @Column(name = "aplica_fidelizacion", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    @Builder.Default
    private Boolean aplicaFidelizacion = false;

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
