package com.barberia.modules.customers.repository;

import com.barberia.modules.customers.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByCorreo(String correo);

    Optional<Cliente> findByTelefono(String telefono);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Integer id);

    boolean existsByTelefono(String telefono);

    @Query("SELECT c FROM Cliente c WHERE c.estado = true AND " +
           "(:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.telefono LIKE CONCAT('%', :search, '%') " +
           "OR LOWER(c.correo) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Cliente> searchActivos(@Param("search") String search, Pageable pageable);

    Page<Cliente> findByEstadoTrue(Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.recompensasDisponibles > 0 AND c.estado = true")
    Page<Cliente> findClientesConRecompensas(Pageable pageable);

    @Modifying
    @Query("UPDATE Cliente c SET c.estado = :estado WHERE c.id = :id")
    void updateEstado(@Param("id") Integer id, @Param("estado") Boolean estado);
}
