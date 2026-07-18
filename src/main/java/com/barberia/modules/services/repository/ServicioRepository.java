package com.barberia.modules.services.repository;

import com.barberia.modules.services.entity.CategoriaServicio;
import com.barberia.modules.services.entity.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer>, JpaSpecificationExecutor<Servicio> {

    Page<Servicio> findByEstadoTrue(Pageable pageable);

    List<Servicio> findByEstadoTrue();

    Page<Servicio> findByCategoria(CategoriaServicio categoria, Pageable pageable);

    @Modifying
    @Query("UPDATE Servicio s SET s.estado = :estado WHERE s.id = :id")
    void updateEstado(@Param("id") Integer id, @Param("estado") Boolean estado);
}
