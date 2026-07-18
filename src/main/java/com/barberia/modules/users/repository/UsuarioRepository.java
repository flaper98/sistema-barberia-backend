package com.barberia.modules.users.repository;

import com.barberia.modules.users.entity.Rol;
import com.barberia.modules.users.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    boolean existsByBarberoId(Integer barberoId);

    boolean existsByBarberoIdAndIdNot(Integer barberoId, Integer id);

    Page<Usuario> findByEstadoTrue(Pageable pageable);

    Page<Usuario> findByRol(Rol rol, Pageable pageable);

    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoAcceso = :fechaHora WHERE u.id = :id")
    void updateUltimoAcceso(@Param("id") Integer id, @Param("fechaHora") LocalDateTime fechaHora);

    @Modifying
    @Query("UPDATE Usuario u SET u.estado = :estado WHERE u.id = :id")
    void updateEstado(@Param("id") Integer id, @Param("estado") Boolean estado);
}
