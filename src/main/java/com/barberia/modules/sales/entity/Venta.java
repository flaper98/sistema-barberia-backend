package com.barberia.modules.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "cliente_nombre", length = 200)
    private String clienteNombre;

    @Column(name = "barbero_id", nullable = false)
    private Integer barberoId;

    @Column(name = "barbero_nombre", nullable = false, length = 200)
    private String barberoNombre;

    @Column(name = "usuario_registro_id")
    private Integer usuarioRegistroId;

    @Column(name = "usuario_registro_nombre", length = 200)
    private String usuarioRegistroNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta", nullable = false, length = 10)
    private TipoVenta tipoVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    @Builder.Default
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @Column(name = "cita_id")
    private Integer citaId;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VentaItem> items = new ArrayList<>();
}
