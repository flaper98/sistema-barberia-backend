package com.barberia.modules.workers.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.workers.dto.BarberoRequest;
import com.barberia.modules.workers.dto.BarberoResponse;
import com.barberia.modules.workers.dto.BarberoSummaryResponse;
import com.barberia.modules.workers.entity.Barbero;
import com.barberia.modules.workers.mapper.BarberoMapper;
import com.barberia.modules.workers.repository.BarberoRepository;
import com.barberia.modules.workers.service.BarberoService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarberoServiceImpl implements BarberoService {

    private final BarberoRepository barberoRepository;
    private final BarberoMapper barberoMapper;
    private final EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BarberoResponse> listarBarberos(String search, Pageable pageable) {
        Page<Barbero> page = StringUtils.hasText(search)
                ? barberoRepository.searchActivos(search.trim(), pageable)
                : barberoRepository.findByEstadoTrue(pageable);
        return PageResponse.from(page.map(barberoMapper::toResponse));
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
                .porcentajeComision(request.getPorcentajeComision())
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
        barbero.setPorcentajeComision(request.getPorcentajeComision());
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
    @Transactional(readOnly = true)
    public BarberoSummaryResponse obtenerResumen(Integer id, LocalDate desde, LocalDate hasta) {
        Barbero barbero = findOrThrow(id);

        List<Object[]> resultado = em.createNativeQuery(
                "SELECT COUNT(v.id), COALESCE(SUM(v.total), 0), " +
                "COALESCE(SUM(CASE WHEN vi.tipo = 'SERVICIO' THEN vi.cantidad ELSE 0 END), 0) " +
                "FROM ventas v " +
                "LEFT JOIN venta_items vi ON vi.venta_id = v.id " +
                "WHERE v.barbero_id = :bid AND v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha) BETWEEN :desde AND :hasta")
                .setParameter("bid", id)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        Long totalVentas = 0L;
        BigDecimal ingresos = BigDecimal.ZERO;
        Long servicios = 0L;

        if (!resultado.isEmpty() && resultado.get(0) != null) {
            Object[] row = (Object[]) resultado.get(0);
            totalVentas = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            ingresos = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            servicios = row[2] != null ? ((Number) row[2]).longValue() : 0L;
        }

        BigDecimal comision = ingresos
                .multiply(barbero.getPorcentajeComision())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return BarberoSummaryResponse.builder()
                .barberoId(id)
                .nombreCompleto(barbero.getNombreCompleto())
                .porcentajeComision(barbero.getPorcentajeComision())
                .totalVentas(totalVentas)
                .ingresosGenerados(ingresos)
                .comisionCalculada(comision)
                .serviciosRealizados(servicios)
                .periodo(desde + " al " + hasta)
                .build();
    }

    private Barbero findOrThrow(Integer id) {
        return barberoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero", id));
    }
}
