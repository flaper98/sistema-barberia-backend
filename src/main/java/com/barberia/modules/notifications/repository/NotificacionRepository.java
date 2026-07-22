package com.barberia.modules.notifications.repository;

import com.barberia.modules.notifications.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    Page<Notificacion> findByUsuarioIdOrderByFechaDesc(Integer usuarioId, Pageable pageable);

    Page<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaDesc(Integer usuarioId, Pageable pageable);

    long countByUsuarioIdAndLeidaFalse(Integer usuarioId);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuarioId = :usuarioId AND n.leida = false")
    void marcarTodasLeidas(@Param("usuarioId") Integer usuarioId);
}
