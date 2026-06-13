package com.barberia.modules.appointments.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "citas_servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @Column(name = "servicio_id")
    private Integer servicioId;

    @Column(name = "servicio_nombre", nullable = false, length = 150)
    private String servicioNombre;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;
}
