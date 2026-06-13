package com.barberia.modules.services.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.services.dto.ServicioRequest;
import com.barberia.modules.services.dto.ServicioResponse;
import com.barberia.modules.services.entity.Servicio;
import com.barberia.modules.services.mapper.ServicioMapper;
import com.barberia.modules.services.repository.ServicioRepository;
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
public class ServicioServiceImpl {

    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;

    @Transactional(readOnly = true)
    public PageResponse<ServicioResponse> listar(String search, Pageable pageable) {
        Page<Servicio> page = StringUtils.hasText(search)
                ? servicioRepository.searchActivos(search.trim(), pageable)
                : servicioRepository.findByEstadoTrue(pageable);
        return PageResponse.from(page.map(servicioMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtenerPorId(Integer id) {
        return servicioMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Servicio servicio = Servicio.builder()
                .nombre(request.getNombre())
                .categoria(request.getCategoria())
                .precio(request.getPrecio())
                .duracionMinutos(request.getDuracionMinutos())
                .descripcion(request.getDescripcion())
                .estado(true)
                .build();

        servicio = servicioRepository.save(servicio);
        log.info("Servicio creado ID={}", servicio.getId());
        return servicioMapper.toResponse(servicio);
    }

    @Transactional
    public ServicioResponse actualizar(Integer id, ServicioRequest request) {
        Servicio servicio = findOrThrow(id);

        servicio.setNombre(request.getNombre());
        servicio.setCategoria(request.getCategoria());
        servicio.setPrecio(request.getPrecio());
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setDescripcion(request.getDescripcion());

        servicio = servicioRepository.save(servicio);
        log.info("Servicio actualizado ID={}", servicio.getId());
        return servicioMapper.toResponse(servicio);
    }

    @Transactional
    public void cambiarEstado(Integer id, Boolean estado) {
        findOrThrow(id);
        servicioRepository.updateEstado(id, estado);
        log.info("Estado de servicio ID={} cambiado a {}", id, estado);
    }

    private Servicio findOrThrow(Integer id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", id));
    }
}
