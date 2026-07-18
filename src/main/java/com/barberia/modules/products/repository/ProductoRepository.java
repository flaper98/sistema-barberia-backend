package com.barberia.modules.products.repository;

import com.barberia.modules.products.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>, JpaSpecificationExecutor<Producto> {

    Optional<Producto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Integer id);

    Page<Producto> findByEstadoTrue(Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.estado = true AND p.stockActual <= p.stockMinimo " +
           "ORDER BY (p.stockMinimo - p.stockActual) DESC")
    Page<Producto> findProductosConStockBajo(Pageable pageable);

    @Modifying
    @Query("UPDATE Producto p SET p.estado = :estado WHERE p.id = :id")
    void updateEstado(@Param("id") Integer id, @Param("estado") Boolean estado);

    @Modifying
    @Query("UPDATE Producto p SET p.categoria = :nuevoNombre WHERE p.categoria = :nombreActual")
    void renombrarCategoriaEnProductos(@Param("nombreActual") String nombreActual, @Param("nuevoNombre") String nuevoNombre);

    boolean existsByCategoriaIgnoreCase(String categoria);
}
