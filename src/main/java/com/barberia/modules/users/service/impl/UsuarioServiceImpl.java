package com.barberia.modules.users.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.users.dto.UsuarioRequest;
import com.barberia.modules.users.dto.UsuarioResponse;
import com.barberia.modules.users.entity.Rol;
import com.barberia.modules.users.entity.Usuario;
import com.barberia.modules.users.mapper.UsuarioMapper;
import com.barberia.modules.users.repository.UsuarioRepository;
import com.barberia.modules.users.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditPublisher auditPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listarUsuarios(String search, Pageable pageable) {
        Page<Usuario> page = StringUtils.hasText(search)
                ? usuarioRepository.searchUsuarios(search.trim(), pageable)
                : usuarioRepository.findAll(pageable);
        return PageResponse.from(page.map(usuarioMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        return usuarioMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Ya existe un usuario con el email: " + request.getEmail());
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("La contraseña es obligatoria al crear un usuario");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .estado(true)
                .avatar(request.getAvatar())
                .build();

        usuario = usuarioRepository.save(usuario);
        auditPublisher.publish(AuditAction.USER_CREATE, "ADMIN",
                "USUARIO", usuario.getId().toString(),
                "Usuario creado: " + usuario.getEmail() + " rol=" + usuario.getRol());
        log.info("Usuario creado ID={} email={}", usuario.getId(), usuario.getEmail());
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioRequest request) {
        Usuario usuario = findOrThrow(id);

        if (usuarioRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ConflictException("Ya existe otro usuario con el email: " + request.getEmail());
        }

        Rol rolAnterior = usuario.getRol();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());
        usuario.setAvatar(request.getAvatar());

        if (StringUtils.hasText(request.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        usuario = usuarioRepository.save(usuario);

        AuditAction action = rolAnterior != request.getRol() ? AuditAction.ROLE_CHANGE : AuditAction.USER_UPDATE;
        auditPublisher.publish(action, "ADMIN",
                "USUARIO", usuario.getId().toString(),
                "Usuario actualizado. Rol: " + rolAnterior + " → " + usuario.getRol());
        log.info("Usuario actualizado ID={}", usuario.getId());
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, Boolean estado) {
        findOrThrow(id);
        usuarioRepository.updateEstado(id, estado);
        auditPublisher.publish(AuditAction.USER_STATUS_CHANGE, "ADMIN",
                "USUARIO", id.toString(), "Estado cambiado a: " + estado);
        log.info("Estado de usuario ID={} cambiado a {}", id, estado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        findOrThrow(id);
        usuarioRepository.updateEstado(id, false);
        auditPublisher.publish(AuditAction.USER_DELETE, "ADMIN",
                "USUARIO", id.toString(), "Usuario deshabilitado (soft delete)");
        log.info("Usuario deshabilitado ID={}", id);
    }

    private Usuario findOrThrow(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
