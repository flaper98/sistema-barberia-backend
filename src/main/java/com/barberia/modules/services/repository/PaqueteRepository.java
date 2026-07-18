package com.barberia.modules.services.repository;

import com.barberia.modules.services.entity.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, Integer> {

    Page<Paquete> findByEstadoTrue(Pageable pageable);

    List<Paquete> findByEstadoTrueOrderByPrecio();

    @Modifying
    @Query("UPDATE Paquete p SET p.estado = :estado WHERE p.id = :id")
    void updateEstado(@Param("id") Integer id, @Param("estado") Boolean estado);
}
