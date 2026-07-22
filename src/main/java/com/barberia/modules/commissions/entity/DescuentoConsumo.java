package com.barberia.modules.commissions.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Descuento por consumo personal del barbero (ej. una gaseosa del stock).
// Se crea junto con un MovimientoStock tipo CONSUMO (ver
// ComisionServiceImpl.registrarConsumo), asi el inventario y la comision
// del barbero quedan sincronizados en un solo paso.
@Entity
@Table(name = "descuentos_consumo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescuentoConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barbero_id", nullable = false)
    private Integer barberoId;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "producto_nombre", nullable = false, length = 150)
    private String productoNombre;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "usuario_registro_id")
    private Integer usuarioRegistroId;

    @Column(name = "usuario_registro_nombre", length = 200)
    private String usuarioRegistroNombre;

    @Column(name = "corte_id")
    private Long corteId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
