package com.barberia.modules.permissions.service;

import com.barberia.modules.permissions.dto.PermisoFlags;
import com.barberia.modules.permissions.dto.RolPermisoItem;
import com.barberia.modules.permissions.entity.Modulo;
import com.barberia.modules.permissions.entity.RolPermiso;
import com.barberia.modules.permissions.repository.RolPermisoRepository;
import com.barberia.modules.users.entity.Rol;
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
            resultado.put(p.getModulo(), new PermisoFlags(p.isPuedeVer(), p.isPuedeEditar(), p.isPuedeEliminar()));
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
            repository.save(permiso);
        }
    }
}
