package com.barberia.modules.commissions.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Resumen cerrado de un periodo de pago para un barbero: comision de
// servicios + comision de productos + propinas - descuentos por consumo.
// Mientras esta PENDIENTE se puede revisar antes de pagar; al pasar a
// PAGADO queda fijo (no se recalcula si cambian las tasas de comision
// despues) y genera un egreso en Finanzas.
@Entity
@Table(name = "cortes_comision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteComision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barbero_id", nullable = false)
    private Integer barberoId;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDate fechaHasta;

    @Column(name = "base_servicios", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal baseServicios = BigDecimal.ZERO;

    @Column(name = "base_productos", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal baseProductos = BigDecimal.ZERO;

    @Column(name = "comision_servicios", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal comisionServicios = BigDecimal.ZERO;

    @Column(name = "comision_productos", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal comisionProductos = BigDecimal.ZERO;

    @Column(name = "total_propinas", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalPropinas = BigDecimal.ZERO;

    @Column(name = "total_descuentos", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalDescuentos = BigDecimal.ZERO;

    @Column(name = "total_a_pagar", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAPagar = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    @Builder.Default
    private EstadoCorte estado = EstadoCorte.PENDIENTE;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "notas", length = 500)
    private String notas;

    @Column(name = "usuario_registro_id")
    private Integer usuarioRegistroId;

    @Column(name = "usuario_registro_nombre", length = 200)
    private String usuarioRegistroNombre;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
