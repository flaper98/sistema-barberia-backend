package com.barberia.modules.commissions.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Propina que un cliente le da directamente al barbero. Queda suelta
// (corteId null) hasta que un CorteComision la agrupa, para no contarla
// dos veces en el siguiente corte.
@Entity
@Table(name = "propinas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Propina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barbero_id", nullable = false)
    private Integer barberoId;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "notas", length = 255)
    private String notas;

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
