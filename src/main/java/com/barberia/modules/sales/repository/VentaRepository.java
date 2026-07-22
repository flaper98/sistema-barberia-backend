package com.barberia.modules.sales.repository;

import com.barberia.modules.sales.entity.EstadoVenta;
import com.barberia.modules.sales.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    Page<Venta> findByBarberoId(Integer barberoId, Pageable pageable);

    Page<Venta> findByClienteId(Integer clienteId, Pageable pageable);

    // Para "Mis Ventas" (BARBER) -- el rango de fechas es opcional, por
    // defecto la pantalla lo manda siempre (hoy a hoy), pero la query
    // tolera que venga vacio igual que findWithFilters.
    @Query("SELECT v FROM Venta v WHERE v.barberoId = :barberoId AND " +
           "(:desde IS NULL OR v.fecha >= :desde) AND " +
           "(:hasta IS NULL OR v.fecha <= :hasta) " +
           "ORDER BY v.fecha DESC")
    Page<Venta> findByBarberoIdWithFilters(
            @Param("barberoId") Integer barberoId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    @Query("SELECT v FROM Venta v WHERE " +
           "(:estado IS NULL OR v.estado = :estado) AND " +
           "(:desde IS NULL OR v.fecha >= :desde) AND " +
           "(:hasta IS NULL OR v.fecha <= :hasta) " +
           "ORDER BY v.fecha DESC")
    Page<Venta> findWithFilters(
            @Param("estado") EstadoVenta estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable);

    boolean existsByCitaId(Integer citaId);
}
