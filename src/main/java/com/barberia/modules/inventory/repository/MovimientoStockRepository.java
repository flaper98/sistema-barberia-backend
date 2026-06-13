package com.barberia.modules.inventory.repository;

import com.barberia.modules.inventory.entity.MovimientoStock;
import com.barberia.modules.inventory.entity.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Integer> {

    Page<MovimientoStock> findByProductoId(Integer productoId, Pageable pageable);

    Page<MovimientoStock> findByTipoMovimiento(TipoMovimiento tipo, Pageable pageable);
}
