package com.barberia.modules.workers.repository;

import com.barberia.modules.workers.entity.Barbero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberoRepository extends JpaRepository<Barbero, Integer> {

    Page<Barbero> findByEstadoTrue(Pageable pageable);

    List<Barbero> findByEstadoTrue();

    @Query("SELECT b FROM Barbero b WHERE b.estado = true AND " +
           "(:search IS NULL OR LOWER(b.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(b.apellido) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Barbero> searchActivos(@Param("search") String search, Pageable pageable);

    @Modifying
    @Query("UPDATE Barbero b SET b.estado = :estado WHERE b.id = :id")
    void updateEstado(@Param("id") Integer id, @Param("estado") Boolean estado);
}
