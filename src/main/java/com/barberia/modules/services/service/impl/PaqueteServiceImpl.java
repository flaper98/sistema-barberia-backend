package com.barberia.modules.services.service.impl;

import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.services.dto.PaqueteRequest;
import com.barberia.modules.services.dto.PaqueteResponse;
import com.barberia.modules.services.entity.Paquete;
import com.barberia.modules.services.repository.PaqueteRepository;
import com.barberia.modules.services.service.PaqueteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaqueteServiceImpl implements PaqueteService {

    private final PaqueteRepository paqueteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PaqueteResponse> listarActivos() {
        return paqueteRepository.findByEstadoTrueOrderByPrecio().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaqueteResponse> listarTodos() {
        return paqueteRepository.findAll().stream()
                .sorted((a, b) -> a.getPrecio().compareTo(b.getPrecio()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaqueteResponse crear(PaqueteRequest request) {
        Paquete paquete = Paquete.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion().trim())
                .precio(request.getPrecio())
                .estado(true)
                .build();
        paquete = paqueteRepository.save(paquete);
        log.info("Paquete creado ID={} nombre={}", paquete.getId(), paquete.getNombre());
        return toResponse(paquete);
    }

    @Override
    @Transactional
    public PaqueteResponse actualizar(Integer id, PaqueteRequest request) {
        Paquete paquete = findOrThrow(id);
        paquete.setNombre(request.getNombre().trim());
        paquete.setDescripcion(request.getDescripcion().trim());
        paquete.setPrecio(request.getPrecio());
        paquete = paqueteRepository.save(paquete);
        log.info("Paquete actualizado ID={}", paquete.getId());
        return toResponse(paquete);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, Boolean estado) {
        findOrThrow(id);
        paqueteRepository.updateEstado(id, estado);
        log.info("Estado de paquete ID={} cambiado a {}", id, estado);
    }

    private Paquete findOrThrow(Integer id) {
        return paqueteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete", id));
    }

    private PaqueteResponse toResponse(Paquete paquete) {
        return PaqueteResponse.builder()
                .id(paquete.getId())
                .nombre(paquete.getNombre())
                .descripcion(paquete.getDescripcion())
                .precio(paquete.getPrecio())
                .estado(paquete.getEstado())
                .createdAt(paquete.getCreatedAt())
                .updatedAt(paquete.getUpdatedAt())
                .build();
    }
}
