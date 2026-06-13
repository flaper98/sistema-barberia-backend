package com.barberia.modules.loyalty.repository;

import com.barberia.modules.loyalty.entity.FidelizacionCuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FidelizacionCuentaRepository extends JpaRepository<FidelizacionCuenta, Integer> {

    Optional<FidelizacionCuenta> findByClienteId(Integer clienteId);

    boolean existsByClienteId(Integer clienteId);

    Page<FidelizacionCuenta> findByRecompensasDisponiblesGreaterThan(Integer minimo, Pageable pageable);
}
