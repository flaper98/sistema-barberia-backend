package com.barberia.modules.permissions.entity;

import com.barberia.modules.users.entity.Rol;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol_permisos", uniqueConstraints = @UniqueConstraint(columnNames = {"rol", "modulo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo", nullable = false, length = 30)
    private Modulo modulo;

    @Column(name = "puede_ver", nullable = false)
    private boolean puedeVer;

    @Column(name = "puede_editar", nullable = false)
    private boolean puedeEditar;

    @Column(name = "puede_eliminar", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private boolean puedeEliminar;

    // Solo tiene sentido para el modulo COMISIONES (ver el detalle por item
    // de las ventas/comisiones de un barbero, con fecha) -- en el resto de
    // los modulos queda en false sin usarse.
    @Column(name = "puede_ver_detalle", nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private boolean puedeVerDetalle;
}
