package com.barberia.modules.permissions.dto;

public record PermisoFlags(boolean puedeVer, boolean puedeEditar, boolean puedeEliminar) {
    public static final PermisoFlags SIN_ACCESO = new PermisoFlags(false, false, false);
    public static final PermisoFlags ACCESO_TOTAL = new PermisoFlags(true, true, true);
}
