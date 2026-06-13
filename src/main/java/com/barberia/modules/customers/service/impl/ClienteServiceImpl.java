package com.barberia.modules.customers.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.customers.dto.ClienteRequest;
import com.barberia.modules.customers.dto.ClienteResponse;
import com.barberia.modules.customers.entity.Cliente;
import com.barberia.modules.customers.mapper.ClienteMapper;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.customers.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final AuditPublisher auditPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> listarClientes(String search, Pageable pageable) {
        Page<Cliente> page = StringUtils.hasText(search)
                ? clienteRepository.searchActivos(search.trim(), pageable)
                : clienteRepository.findByEstadoTrue(pageable);
        return PageResponse.from(page.map(clienteMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Integer id) {
        return clienteMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (StringUtils.hasText(request.getCorreo())
                && clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new ConflictException("Ya existe un cliente con el correo: " + request.getCorreo());
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .correo(request.getCorreo())
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .notas(request.getNotas())
                .estado(true)
                .cantidadSellos(0)
                .recompensasDisponibles(0)
                .build();

        cliente = clienteRepository.save(cliente);
        auditPublisher.publish(AuditAction.CLIENT_CREATE, "SISTEMA",
                "CLIENTE", cliente.getId().toString(),
                "Cliente creado: " + cliente.getNombreCompleto());
        log.info("Cliente creado ID={}", cliente.getId());
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Integer id, ClienteRequest request) {
        Cliente cliente = findOrThrow(id);

        if (StringUtils.hasText(request.getCorreo())
                && clienteRepository.existsByCorreoAndIdNot(request.getCorreo(), id)) {
            throw new ConflictException("Ya existe otro cliente con el correo: " + request.getCorreo());
        }

        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setCorreo(request.getCorreo());
        cliente.setTelefono(request.getTelefono());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setNotas(request.getNotas());

        cliente = clienteRepository.save(cliente);
        auditPublisher.publish(AuditAction.CLIENT_UPDATE, "SISTEMA",
                "CLIENTE", cliente.getId().toString(), "Cliente actualizado");
        log.info("Cliente actualizado ID={}", cliente.getId());
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        findOrThrow(id);
        clienteRepository.updateEstado(id, false);
        auditPublisher.publish(AuditAction.CLIENT_DELETE, "SISTEMA",
                "CLIENTE", id.toString(), "Cliente deshabilitado (soft delete)");
        log.info("Cliente deshabilitado ID={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClienteResponse> listarConRecompensas(Pageable pageable) {
        Page<Cliente> page = clienteRepository.findClientesConRecompensas(pageable);
        return PageResponse.from(page.map(clienteMapper::toResponse));
    }

    private Cliente findOrThrow(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
