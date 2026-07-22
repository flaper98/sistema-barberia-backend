package com.barberia.modules.notifications.service;

import com.barberia.common.PageResponse;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.notifications.dto.NotificacionResponse;
import com.barberia.modules.notifications.entity.Notificacion;
import com.barberia.modules.notifications.entity.TipoNotificacion;
import com.barberia.modules.notifications.repository.NotificacionRepository;
import com.barberia.modules.users.entity.Rol;
import com.barberia.modules.users.entity.Usuario;
import com.barberia.modules.users.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Notificaciones internas simples: se guardan en la base y el frontend las
 * consulta por polling (GET /unread-count cada cierto tiempo) -- no hay
 * WebSocket/push en este proyecto todavia, y para el volumen de una sola
 * barberia no hace falta. Una fila por usuario destinatario (nunca por rol),
 * asi "leida" es siempre individual.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void notificarBarbero(Integer barberoId, TipoNotificacion tipo, String mensaje,
                                  String entidadTipo, Integer entidadId) {
        if (barberoId == null) return;
        usuarioRepository.findByBarberoIdAndEstadoTrue(barberoId).ifPresentOrElse(
                usuario -> crear(usuario.getId(), tipo, mensaje, entidadTipo, entidadId),
                () -> log.debug("No hay usuario activo vinculado al barbero {} -- no se notifica", barberoId));
    }

    @Transactional
    public void notificarRoles(List<Rol> roles, TipoNotificacion tipo, String mensaje,
                                String entidadTipo, Integer entidadId) {
        List<Usuario> usuarios = usuarioRepository.findByRolInAndEstadoTrue(roles);
        usuarios.forEach(u -> crear(u.getId(), tipo, mensaje, entidadTipo, entidadId));
    }

    private void crear(Integer usuarioId, TipoNotificacion tipo, String mensaje,
                        String entidadTipo, Integer entidadId) {
        notificacionRepository.save(Notificacion.builder()
                .usuarioId(usuarioId)
                .tipo(tipo)
                .mensaje(mensaje)
                .entidadTipo(entidadTipo)
                .entidadId(entidadId)
                .leida(false)
                .build());
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificacionResponse> listar(Integer usuarioId, boolean soloNoLeidas, Pageable pageable) {
        var page = soloNoLeidas
                ? notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId, pageable)
                : notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Integer usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public void marcarLeida(Integer usuarioId, Long id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", id));
        if (!n.getUsuarioId().equals(usuarioId)) {
            throw new AccessDeniedException("Esta notificación no te pertenece");
        }
        if (!n.isLeida()) {
            n.setLeida(true);
            notificacionRepository.save(n);
        }
    }

    @Transactional
    public void marcarTodasLeidas(Integer usuarioId) {
        notificacionRepository.marcarTodasLeidas(usuarioId);
    }

    private NotificacionResponse toResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .tipo(n.getTipo())
                .mensaje(n.getMensaje())
                .entidadTipo(n.getEntidadTipo())
                .entidadId(n.getEntidadId())
                .leida(n.isLeida())
                .fecha(n.getFecha())
                .build();
    }
}
