package com.barberia.modules.permissions.repository;

import com.barberia.modules.permissions.entity.Modulo;
import com.barberia.modules.permissions.entity.RolPermiso;
import com.barberia.modules.users.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Integer> {
    List<RolPermiso> findByRol(Rol rol);

    Optional<RolPermiso> findByRolAndModulo(Rol rol, Modulo modulo);
}
