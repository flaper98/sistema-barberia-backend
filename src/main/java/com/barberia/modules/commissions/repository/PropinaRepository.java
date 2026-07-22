package com.barberia.modules.commissions.repository;

import com.barberia.modules.commissions.entity.Propina;
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
public interface PropinaRepository extends JpaRepository<Propina, Long> {

    Page<Propina> findByBarberoIdOrderByFechaDesc(Integer barberoId, Pageable pageable);

    List<Propina> findByBarberoIdAndCorteIdIsNullAndFechaBetween(Integer barberoId, LocalDate desde, LocalDate hasta);

    @Modifying
    @Query("UPDATE Propina p SET p.corteId = :corteId WHERE p.id IN :ids")
    void asignarCorte(@Param("ids") List<Long> ids, @Param("corteId") Long corteId);
}
