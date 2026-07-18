package com.barberia.modules.permissions.service;

import com.barberia.modules.permissions.entity.Modulo;
import com.barberia.modules.permissions.entity.RolPermiso;
import com.barberia.modules.permissions.repository.RolPermisoRepository;
import com.barberia.modules.users.entity.Rol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Carga la matriz de permisos por defecto la primera vez que arranca el sistema,
 * calcada del comportamiento real que ya tenían los endpoints (no de lo que mostraba
 * el sidebar, que en algunos casos estaba desalineado). A partir de ahí el ADMIN
 * la administra desde /users -> Roles y permisos y esto no se vuelve a tocar.
 * "Eliminar" arranca en false para todos los roles configurables: hoy esas acciones
 * eran ADMIN-only en el código, así que el default no le otorga nada nuevo a nadie.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RolPermisoSeeder implements ApplicationRunner {

    private final RolPermisoRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        log.info("Sembrando matriz de permisos por rol (primer arranque)");
        repository.saveAll(defaults());
    }

    private List<RolPermiso> defaults() {
        List<RolPermiso> lista = new ArrayList<>();
        // rol, modulo, puedeVer, puedeEditar, puedeEliminar
        add(lista, Rol.CASHIER, Modulo.DASHBOARD, true, true, false);
        add(lista, Rol.CASHIER, Modulo.VENTAS, true, true, false);
        add(lista, Rol.CASHIER, Modulo.CITAS, true, true, false);
        add(lista, Rol.CASHIER, Modulo.CLIENTES, true, true, false);
        add(lista, Rol.CASHIER, Modulo.BARBEROS, true, false, false);
        add(lista, Rol.CASHIER, Modulo.SERVICIOS, true, false, false);
        add(lista, Rol.CASHIER, Modulo.PRODUCTOS, true, false, false);
        add(lista, Rol.CASHIER, Modulo.INVENTARIO, true, true, false);
        add(lista, Rol.CASHIER, Modulo.FIDELIZACION, true, true, false);
        add(lista, Rol.CASHIER, Modulo.FINANZAS, false, false, false);
        add(lista, Rol.CASHIER, Modulo.REPORTES, false, false, false);
        add(lista, Rol.CASHIER, Modulo.CONFIGURACION, false, false, false);

        add(lista, Rol.BARBER, Modulo.DASHBOARD, true, true, false);
        add(lista, Rol.BARBER, Modulo.VENTAS, true, true, false);
        add(lista, Rol.BARBER, Modulo.CITAS, true, true, false);
        add(lista, Rol.BARBER, Modulo.CLIENTES, true, false, false);
        add(lista, Rol.BARBER, Modulo.BARBEROS, true, false, false);
        add(lista, Rol.BARBER, Modulo.SERVICIOS, true, false, false);
        add(lista, Rol.BARBER, Modulo.PRODUCTOS, true, false, false);
        add(lista, Rol.BARBER, Modulo.INVENTARIO, false, false, false);
        add(lista, Rol.BARBER, Modulo.FIDELIZACION, true, true, false);
        add(lista, Rol.BARBER, Modulo.FINANZAS, false, false, false);
        add(lista, Rol.BARBER, Modulo.REPORTES, false, false, false);
        add(lista, Rol.BARBER, Modulo.CONFIGURACION, false, false, false);

        add(lista, Rol.RECEPTION, Modulo.DASHBOARD, false, false, false);
        add(lista, Rol.RECEPTION, Modulo.VENTAS, false, false, false);
        add(lista, Rol.RECEPTION, Modulo.CITAS, true, true, false);
        add(lista, Rol.RECEPTION, Modulo.CLIENTES, true, true, false);
        add(lista, Rol.RECEPTION, Modulo.BARBEROS, true, false, false);
        add(lista, Rol.RECEPTION, Modulo.SERVICIOS, true, false, false);
        add(lista, Rol.RECEPTION, Modulo.PRODUCTOS, true, false, false);
        add(lista, Rol.RECEPTION, Modulo.INVENTARIO, false, false, false);
        add(lista, Rol.RECEPTION, Modulo.FIDELIZACION, true, true, false);
        add(lista, Rol.RECEPTION, Modulo.FINANZAS, false, false, false);
        add(lista, Rol.RECEPTION, Modulo.REPORTES, false, false, false);
        add(lista, Rol.RECEPTION, Modulo.CONFIGURACION, false, false, false);
        return lista;
    }

    private void add(List<RolPermiso> lista, Rol rol, Modulo modulo, boolean ver, boolean editar, boolean eliminar) {
        lista.add(RolPermiso.builder().rol(rol).modulo(modulo).puedeVer(ver).puedeEditar(editar).puedeEliminar(eliminar).build());
    }
}
