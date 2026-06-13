package com.barberia.modules.loyalty.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recompensas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 25)
    private TipoRecompensa tipo;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "sellos_requeridos", nullable = false)
    @Builder.Default
    private Integer sellosRequeridos = 5;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
