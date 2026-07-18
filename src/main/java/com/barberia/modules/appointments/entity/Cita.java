package com.barberia.modules.appointments.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "citas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Column(name = "barbero_id")
    private Integer barberoId;

    @Column(name = "barbero_nombre", nullable = false, length = 200)
    private String barberoNombre;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @Column(name = "total_estimado", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalEstimado = BigDecimal.ZERO;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "appointment_code", unique = true, length = 20)
    private String appointmentCode;

    @Column(name = "appointment_source", nullable = false, length = 30)
    @Builder.Default
    private String appointmentSource = "INTERNAL";

    @Column(name = "venta_id")
    private Integer ventaId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cita", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CitaServicio> servicios = new ArrayList<>();
}
