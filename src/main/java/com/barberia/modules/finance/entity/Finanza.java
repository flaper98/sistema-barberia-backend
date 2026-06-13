package com.barberia.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "finanzas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Finanza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TipoFinanza tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    @Builder.Default
    private CategoriaFinanza categoria = CategoriaFinanza.OTRO;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "usuario_nombre", length = 200)
    private String usuarioNombre;

    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "venta_id")
    private Integer ventaId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
