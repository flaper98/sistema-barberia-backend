package com.barberia.modules.permissions.dto;

import com.barberia.modules.permissions.entity.Modulo;
import com.barberia.modules.users.entity.Rol;
import jakarta.validation.constraints.NotNull;

public record RolPermisoItem(
        @NotNull Rol rol,
        @NotNull Modulo modulo,
        boolean puedeVer,
        boolean puedeEditar,
        boolean puedeEliminar,
        boolean puedeVerDetalle
) {
}
