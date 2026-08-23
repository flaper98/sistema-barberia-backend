package com.barberia.modules.services.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Producto de cortesía asociado a un servicio (ej. una cerveza gratis con un
 * corte premium) -- configuración del administrador. Cuando se completa una
 * venta que incluye el servicio, se descuenta {@code cantidad} del stock del
 * producto automáticamente (ver VentaServiceImpl.procesarCortesias).
 */
@Entity
@Table(name = "servicio_cortesias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioCortesia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "servicio_id", nullable = false)
    private Integer servicioId;

    @Column(name = "producto_id", nullable = false)
    private Integer productoId;

    @Column(name = "cantidad", nullable = false)
    @Builder.Default
    private Integer cantidad = 1;
}
