package com.barberia.modules.loyalty.repository;

import com.barberia.modules.loyalty.entity.FidelizacionMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FidelizacionMovimientoRepository extends JpaRepository<FidelizacionMovimiento, Integer> {

    Page<FidelizacionMovimiento> findByClienteIdOrderByFechaDesc(Integer clienteId, Pageable pageable);
}
