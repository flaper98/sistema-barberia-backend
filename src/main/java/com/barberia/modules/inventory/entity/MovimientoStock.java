package com.barberia.modules.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "producto_nombre", nullable = false, length = 150)
    private String productoNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "usuario_nombre", length = 200)
    private String usuarioNombre;

    @Column(name = "venta_id")
    private Integer ventaId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
