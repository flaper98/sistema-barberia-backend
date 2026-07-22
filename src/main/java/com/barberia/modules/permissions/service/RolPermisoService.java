package com.barberia.modules.permissions.service;

import com.barberia.modules.permissions.dto.PermisoFlags;
import com.barberia.modules.permissions.dto.RolPermisoItem;
import com.barberia.modules.permissions.entity.Modulo;
import com.barberia.modules.permissions.entity.RolPermiso;
import com.barberia.modules.permissions.repository.RolPermisoRepository;
import com.barberia.modules.users.entity.Rol;
import com.barberia.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ADMIN siempre tiene acceso total y no se guarda en la tabla: así un admin
 * nunca puede quedar bloqueado del sistema por un cambio en la matriz.
 */
@Service
@RequiredArgsConstructor
public class RolPermisoService {

    private static final List<Rol> ROLES_CONFIGURABLES = List.of(Rol.BARBER, Rol.CASHIER, Rol.RECEPTION);

    private final RolPermisoRepository repository;

    @Transactional(readOnly = true)
    public Map<Rol, Map<Modulo, PermisoFlags>> obtenerMatriz() {
        Map<Rol, Map<Modulo, PermisoFlags>> matriz = new EnumMap<>(Rol.class);
        for (Rol rol : ROLES_CONFIGURABLES) {
            matriz.put(rol, obtenerParaRol(rol));
        }
        return matriz;
    }

    @Transactional(readOnly = true)
    public Map<Modulo, PermisoFlags> obtenerParaRol(Rol rol) {
        if (rol == Rol.ADMIN) {
            Map<Modulo, PermisoFlags> total = new EnumMap<>(Modulo.class);
            for (Modulo modulo : Modulo.values()) {
                total.put(modulo, PermisoFlags.ACCESO_TOTAL);
            }
            return total;
        }
        Map<Modulo, PermisoFlags> resultado = new EnumMap<>(Modulo.class);
        for (RolPermiso p : repository.findByRol(rol)) {
            resultado.put(p.getModulo(), new PermisoFlags(p.isPuedeVer(), p.isPuedeEditar(), p.isPuedeEliminar(), p.isPuedeVerDetalle()));
        }
        return resultado;
    }

    @Transactional
    public void actualizarMatriz(List<RolPermisoItem> items) {
        for (RolPermisoItem item : items) {
            if (item.rol() == Rol.ADMIN) continue;
            RolPermiso permiso = repository.findByRolAndModulo(item.rol(), item.modulo())
                    .orElse(RolPermiso.builder().rol(item.rol()).modulo(item.modulo()).build());
            boolean puedeVer = item.puedeVer() || item.puedeEditar() || item.puedeEliminar();
            permiso.setPuedeVer(puedeVer);
            permiso.setPuedeEditar(item.puedeEditar());
            permiso.setPuedeEliminar(item.puedeEliminar());
            permiso.setPuedeVerDetalle(item.puedeVerDetalle());
            repository.save(permiso);
        }
    }

    // Usado directo desde @PreAuthorize (SpEL "@rolPermisoService.puedeVerDetalleComisiones(...)")
    // en ComisionController -- a diferencia del resto de la matriz (que hoy
    // solo gobierna que muestra el Angular), esta es la primera vez que la
    // matriz se aplica tambien del lado del backend: el detalle por item de
    // ventas/comisiones de un barbero (cuanto vendio de cada servicio/
    // producto, con fecha) es informacion sensible -- ADMIN siempre puede,
    // un BARBER solo puede ver el suyo propio, y CASHIER/RECEPTION necesitan
    // el permiso explicito (arranca en false para los dos, ver seeder).
    @Transactional(readOnly = true)
    public boolean puedeVerDetalleComisiones(CustomUserDetails principal, Integer barberoId) {
        if (principal == null) return false;
        Rol rol = principal.getUsuario().getRol();
        if (rol == Rol.ADMIN) return true;
        if (rol == Rol.BARBER) {
            Integer miBarberoId = principal.getUsuario().getBarberoId();
            return miBarberoId != null && miBarberoId.equals(barberoId);
        }
        return obtenerParaRol(rol).getOrDefault(Modulo.COMISIONES, PermisoFlags.SIN_ACCESO).puedeVerDetalle();
    }
}
