package com.barberia.modules.services.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paquetes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Boolean estado = true;

    // % que se le paga de comision al barbero por cada venta de este
    // paquete -- lo define el administrador desde Comisiones -> Tasas de
    // comision.
    @Column(name = "porcentaje_comision", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeComision = BigDecimal.ZERO;

    // Si una venta de este paquete suma sello de fidelizacion al cliente --
    // lo define el administrador desde Fidelizacion -> Que aplica sello.
    @Column(name = "aplica_fidelizacion", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 1")
    @Builder.Default
    private Boolean aplicaFidelizacion = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
