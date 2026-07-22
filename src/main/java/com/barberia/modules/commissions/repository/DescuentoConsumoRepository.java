package com.barberia.modules.commissions.repository;

import com.barberia.modules.commissions.entity.DescuentoConsumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DescuentoConsumoRepository extends JpaRepository<DescuentoConsumo, Long> {

    Page<DescuentoConsumo> findByBarberoIdOrderByFechaDesc(Integer barberoId, Pageable pageable);

    List<DescuentoConsumo> findByBarberoIdAndCorteIdIsNullAndFechaBetween(Integer barberoId, LocalDate desde, LocalDate hasta);

    @Modifying
    @Query("UPDATE DescuentoConsumo d SET d.corteId = :corteId WHERE d.id IN :ids")
    void asignarCorte(@Param("ids") List<Long> ids, @Param("corteId") Long corteId);
}
