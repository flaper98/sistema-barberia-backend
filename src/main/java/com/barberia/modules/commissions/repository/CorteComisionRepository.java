package com.barberia.modules.commissions.repository;

import com.barberia.modules.commissions.entity.CorteComision;
import com.barberia.modules.commissions.entity.EstadoCorte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CorteComisionRepository extends JpaRepository<CorteComision, Long> {

    @Query("SELECT c FROM CorteComision c WHERE " +
           "(:barberoId IS NULL OR c.barberoId = :barberoId) AND " +
           "(:estado IS NULL OR c.estado = :estado) " +
           "ORDER BY c.fechaHasta DESC, c.id DESC")
    Page<CorteComision> findWithFilters(
            @Param("barberoId") Integer barberoId,
            @Param("estado") EstadoCorte estado,
            Pageable pageable);

    // Evita generar dos cortes que se pisen en fechas para el mismo barbero
    // -- eso contaria la comision de las mismas ventas dos veces (a
    // diferencia de propinas/descuentos, las ventas no quedan "marcadas"
    // como ya incluidas en un corte, asi que la unica forma de no
    // duplicarlas es no permitir rangos superpuestos).
    @Query("SELECT COUNT(c) > 0 FROM CorteComision c WHERE c.barberoId = :barberoId " +
           "AND c.fechaDesde <= :hasta AND c.fechaHasta >= :desde")
    boolean existsOverlapping(
            @Param("barberoId") Integer barberoId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
