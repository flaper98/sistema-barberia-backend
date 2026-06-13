package com.barberia.modules.workers.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "barberos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "especialidad", length = 200)
    private String especialidad;

    @Column(name = "porcentaje_comision", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeComision = BigDecimal.valueOf(40.00);

    @Column(name = "total_generado", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalGenerado = BigDecimal.ZERO;

    @Column(name = "servicios_realizados", nullable = false)
    @Builder.Default
    private Integer serviciosRealizados = 0;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
