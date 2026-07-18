package com.barberia.modules.finance.repository;

import com.barberia.modules.finance.entity.CategoriaFinanza;
import com.barberia.modules.finance.entity.Finanza;
import com.barberia.modules.finance.entity.TipoFinanza;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FinanzaRepository extends JpaRepository<Finanza, Integer> {

    @Query("SELECT f FROM Finanza f WHERE " +
           "(:tipo IS NULL OR f.tipo = :tipo) AND " +
           "(:categoria IS NULL OR f.categoria = :categoria) AND " +
           "(:desde IS NULL OR f.fecha >= :desde) AND " +
           "(:hasta IS NULL OR f.fecha <= :hasta) " +
           "ORDER BY f.fecha DESC")
    Page<Finanza> findWithFilters(
            @Param("tipo") TipoFinanza tipo,
            @Param("categoria") CategoriaFinanza categoria,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(f.monto), 0) FROM Finanza f " +
           "WHERE f.tipo = :tipo AND f.fecha BETWEEN :desde AND :hasta")
    BigDecimal sumByTipoAndFecha(
            @Param("tipo") TipoFinanza tipo,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    void deleteByVentaId(Integer ventaId);

    Optional<Finanza> findByVentaId(Integer ventaId);
}
