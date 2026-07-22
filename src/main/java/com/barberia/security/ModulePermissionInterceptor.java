package com.barberia.security;

import com.barberia.modules.permissions.dto.PermisoFlags;
import com.barberia.modules.permissions.entity.Modulo;
import com.barberia.modules.permissions.service.RolPermisoService;
import com.barberia.modules.users.entity.Rol;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Segunda capa de autorización, encima de los @PreAuthorize de cada controller:
 * consulta la matriz configurable por el ADMIN (rol x módulo -> ver/editar) y
 * bloquea lo que esa matriz no permite. Los @PreAuthorize existentes siguen
 * siendo el piso mínimo (ver SecurityConfig/controllers); esta capa solo puede
 * restringir más, salvo en los módulos donde se relajó el @PreAuthorize a
 * isAuthenticated() para que la matriz sea la única autoridad real.
 * ADMIN y /api/users quedan siempre fuera de esta capa (no configurables,
 * para evitar que un admin se bloquee a sí mismo o pierda el control de altas).
 */
@Component
@RequiredArgsConstructor
public class ModulePermissionInterceptor implements HandlerInterceptor {

    private static final Map<Modulo, String> NOMBRE_MODULO = new EnumMap<>(Modulo.class);
    static {
        NOMBRE_MODULO.put(Modulo.DASHBOARD, "Dashboard");
        NOMBRE_MODULO.put(Modulo.VENTAS, "Ventas");
        NOMBRE_MODULO.put(Modulo.CITAS, "Citas");
        NOMBRE_MODULO.put(Modulo.CLIENTES, "Clientes");
        NOMBRE_MODULO.put(Modulo.BARBEROS, "Barberos");
        NOMBRE_MODULO.put(Modulo.SERVICIOS, "Servicios");
        NOMBRE_MODULO.put(Modulo.PRODUCTOS, "Productos");
        NOMBRE_MODULO.put(Modulo.INVENTARIO, "Inventario");
        NOMBRE_MODULO.put(Modulo.FIDELIZACION, "Fidelización");
        NOMBRE_MODULO.put(Modulo.FINANZAS, "Finanzas");
        NOMBRE_MODULO.put(Modulo.REPORTES, "Reportes");
        NOMBRE_MODULO.put(Modulo.CONFIGURACION, "Configuración");
        NOMBRE_MODULO.put(Modulo.COMISIONES, "Comisiones");
    }

    private final RolPermisoService rolPermisoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Modulo modulo = resolveModulo(request.getRequestURI());
        if (modulo == null) return true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth != null && auth.getPrincipal() instanceof CustomUserDetails principal)) return true;

        Rol rol = principal.getUsuario().getRol();
        if (rol == Rol.ADMIN) return true;

        PermisoFlags flags = rolPermisoService.obtenerParaRol(rol).getOrDefault(modulo, PermisoFlags.SIN_ACCESO);
        boolean esEliminar = HttpMethod.DELETE.matches(request.getMethod());
        boolean esEscritura = !esEliminar && !HttpMethod.GET.matches(request.getMethod()) && !HttpMethod.HEAD.matches(request.getMethod());
        boolean permitido = esEliminar ? flags.puedeEliminar() : esEscritura ? flags.puedeEditar() : flags.puedeVer();

        if (!permitido) {
            String accion = esEliminar ? "eliminar en" : esEscritura ? "editar en" : "ver";
            throw new AccessDeniedException(
                    "No tienes permiso para " + accion + " la sección " + NOMBRE_MODULO.get(modulo)
                            + ". Pídele a un administrador que ajuste tus permisos en Usuarios.");
        }
        return true;
    }

    private Modulo resolveModulo(String path) {
        if (path.startsWith("/api/reports/dashboard")) return Modulo.DASHBOARD;
        if (path.startsWith("/api/reports")) return Modulo.REPORTES;
        if (path.startsWith("/api/sales")) return Modulo.VENTAS;
        // Convertir una cita en venta es sustancialmente "crear una venta", no
        // "editar la cita" -- se rige por el permiso de Ventas (donde el
        // barbero ya puede generar sus solicitudes de cobro), no por el de
        // Citas (donde el barbero solo tiene acceso de lectura).
        if (path.matches("/api/appointments/\\d+/convert-to-sale")) return Modulo.VENTAS;
        if (path.startsWith("/api/appointments")) return Modulo.CITAS;
        if (path.startsWith("/api/customers/for-sale")) return null;
        if (path.startsWith("/api/customers")) return Modulo.CLIENTES;
        if (path.startsWith("/api/workers/for-sale")) return null;
        if (path.startsWith("/api/workers")) return Modulo.BARBEROS;
        if (path.startsWith("/api/services")) return Modulo.SERVICIOS;
        if (path.startsWith("/api/packages")) return Modulo.SERVICIOS;
        if (path.startsWith("/api/products")) return Modulo.PRODUCTOS;
        if (path.startsWith("/api/inventory")) return Modulo.INVENTARIO;
        if (path.startsWith("/api/loyalty")) return Modulo.FIDELIZACION;
        if (path.startsWith("/api/finance")) return Modulo.FINANZAS;
        if (path.startsWith("/api/config")) return Modulo.CONFIGURACION;
        if (path.startsWith("/api/commissions")) return Modulo.COMISIONES;
        return null;
    }
}
