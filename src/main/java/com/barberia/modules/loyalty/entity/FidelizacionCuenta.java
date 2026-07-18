package com.barberia.modules.loyalty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fidelizacion_cuentas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FidelizacionCuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private Integer clienteId;

    @Column(name = "sellos_actuales", nullable = false)
    @Builder.Default
    private Integer sellosActuales = 0;

    @Column(name = "sellos_necesarios", nullable = false)
    @Builder.Default
    private Integer sellosNecesarios = 5;

    @Column(name = "recompensas_disponibles", nullable = false)
    @Builder.Default
    private Integer recompensasDisponibles = 0;

    @Column(name = "recompensas_canjeadas", nullable = false)
    @Builder.Default
    private Integer recompensasCanjeadas = 0;

    @Column(name = "total_sellos_acumulados", nullable = false)
    @Builder.Default
    private Integer totalSellosAcumulados = 0;

    @Column(name = "descripcion_recompensa", length = 255)
    private String descripcionRecompensa;

    @Column(name = "ultimo_movimiento")
    private LocalDateTime ultimoMovimiento;
}
