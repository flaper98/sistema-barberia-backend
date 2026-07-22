package com.barberia.modules.permissions.dto;

// puedeVerDetalle solo tiene sentido para el modulo COMISIONES (ver el
// detalle por item de las ventas/comisiones de un barbero) -- en el resto
// de los modulos siempre viene en false, sin usarse.
public record PermisoFlags(boolean puedeVer, boolean puedeEditar, boolean puedeEliminar, boolean puedeVerDetalle) {
    public static final PermisoFlags SIN_ACCESO = new PermisoFlags(false, false, false, false);
    public static final PermisoFlags ACCESO_TOTAL = new PermisoFlags(true, true, true, true);
}
