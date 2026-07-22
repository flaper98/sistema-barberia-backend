package com.barberia.modules.workers.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.common.search.TextSearchSpecification;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.workers.dto.BarberoRequest;
import com.barberia.modules.workers.dto.BarberoResponse;
import com.barberia.modules.workers.dto.BarberoSelectorResponse;
import com.barberia.modules.workers.entity.Barbero;
import com.barberia.modules.workers.mapper.BarberoMapper;
import com.barberia.modules.workers.repository.BarberoRepository;
import com.barberia.modules.workers.service.BarberoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarberoServiceImpl implements BarberoService {

    private final BarberoRepository barberoRepository;
    private final BarberoMapper barberoMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BarberoResponse> listarBarberos(String search, Pageable pageable) {
        Specification<Barbero> spec = Specification.<Barbero>where((root, query, cb) -> cb.isTrue(root.get("estado")))
                .and(TextSearchSpecification.matches(search, "nombre", "apellido"));
        Page<Barbero> page = barberoRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(barberoMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BarberoSelectorResponse> listarParaSelector() {
        return barberoRepository.findByEstadoTrue().stream()
                .map(b -> BarberoSelectorResponse.builder()
                        .id(b.getId())
                        .nombre(b.getNombre())
                        .apellido(b.getApellido())
                        .especialidad(b.getEspecialidad())
                        .avatar(b.getAvatar())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BarberoResponse obtenerPorId(Integer id) {
        return barberoMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public BarberoResponse crear(BarberoRequest request) {
        Barbero barbero = Barbero.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .especialidad(request.getEspecialidad())
                .totalGenerado(BigDecimal.ZERO)
                .serviciosRealizados(0)
                .estado(true)
                .avatar(request.getAvatar())
                .build();

        barbero = barberoRepository.save(barbero);
        log.info("Barbero creado ID={}", barbero.getId());
        return barberoMapper.toResponse(barbero);
    }

    @Override
    @Transactional
    public BarberoResponse actualizar(Integer id, BarberoRequest request) {
        Barbero barbero = findOrThrow(id);

        barbero.setNombre(request.getNombre());
        barbero.setApellido(request.getApellido());
        barbero.setTelefono(request.getTelefono());
        barbero.setEspecialidad(request.getEspecialidad());
        barbero.setAvatar(request.getAvatar());

        barbero = barberoRepository.save(barbero);
        log.info("Barbero actualizado ID={}", barbero.getId());
        return barberoMapper.toResponse(barbero);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, Boolean estado) {
        findOrThrow(id);
        barberoRepository.updateEstado(id, estado);
        log.info("Estado de barbero ID={} cambiado a {}", id, estado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        findOrThrow(id);
        barberoRepository.updateEstado(id, false);
        log.info("Barbero deshabilitado (soft delete) ID={}", id);
    }

    private Barbero findOrThrow(Integer id) {
        return barberoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero", id));
    }
}
