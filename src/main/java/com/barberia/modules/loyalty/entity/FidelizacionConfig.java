package com.barberia.modules.loyalty.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fidelizacion_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FidelizacionConfig {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "sellos_necesarios", nullable = false)
    private Integer sellosNecesarios;

    @Column(name = "sellos_por_ganancia", nullable = false)
    private Integer sellosPorGanancia;

    @Column(name = "descripcion_recompensa", nullable = false, length = 255)
    private String descripcionRecompensa;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
