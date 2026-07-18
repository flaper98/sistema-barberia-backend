package com.barberia.modules.services.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    private CategoriaServicio categoria;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "precio_variable", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    @Builder.Default
    private Boolean precioVariable = false;

    @Column(name = "duracion_minutos", nullable = false)
    @Builder.Default
    private Integer duracionMinutos = 30;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
