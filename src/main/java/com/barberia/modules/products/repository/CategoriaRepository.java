package com.barberia.modules.products.repository;

import com.barberia.modules.products.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findByEstadoTrueOrderByNombre();

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    @Modifying
    @Query("UPDATE Categoria c SET c.estado = :estado WHERE c.id = :id")
    void updateEstado(@Param("id") Integer id, @Param("estado") Boolean estado);
}
