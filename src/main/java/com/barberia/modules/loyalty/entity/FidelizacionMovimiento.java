package com.barberia.modules.loyalty.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fidelizacion_movimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FidelizacionMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cuenta_id", nullable = false)
    private Integer cuentaId;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoMovimientoFidelizacion tipo;

    @Column(name = "cantidad", nullable = false)
    @Builder.Default
    private Integer cantidad = 1;

    @Column(name = "motivo", length = 255)
    private String motivo;

    @Column(name = "venta_id")
    private Integer ventaId;

    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;
}
