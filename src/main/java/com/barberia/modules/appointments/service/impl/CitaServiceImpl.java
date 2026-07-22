package com.barberia.modules.appointments.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.appointments.dto.CitaRequest;
import com.barberia.modules.appointments.dto.CitaResponse;
import com.barberia.modules.appointments.entity.Cita;
import com.barberia.modules.appointments.entity.CitaServicio;
import com.barberia.modules.appointments.entity.EstadoCita;
import com.barberia.modules.appointments.mapper.CitaMapper;
import com.barberia.modules.appointments.repository.CitaRepository;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.notifications.entity.TipoNotificacion;
import com.barberia.modules.notifications.service.NotificacionService;
import com.barberia.modules.workers.repository.BarberoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaServiceImpl {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final BarberoRepository barberoRepository;
    private final CitaMapper citaMapper;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public PageResponse<CitaResponse> listar(LocalDate fecha, EstadoCita estado, Pageable pageable) {
        return PageResponse.from(
                citaRepository.findWithFilters(fecha, estado, pageable).map(citaMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Integer id) {
        return citaMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<CitaResponse> listarPorBarbero(Integer barberoId, Pageable pageable) {
        return PageResponse.from(
                citaRepository.findByBarberoIdOrderByFechaAscHoraAsc(barberoId, pageable)
                        .map(citaMapper::toResponse));
    }

    @Transactional
    public CitaResponse crear(CitaRequest request) {
        validarBarbero(request.getBarberoId());
        String clienteNombre = resolverClienteNombre(request);
        String barberoNombre = barberoRepository.findById(request.getBarberoId())
                .map(b -> b.getNombreCompleto())
                .orElseThrow();

        verificarDisponibilidad(request.getFecha(), request.getBarberoId(), request.getHora(), null);

        BigDecimal totalEstimado = request.getTotalEstimado() != null
                ? request.getTotalEstimado() : BigDecimal.ZERO;

        if (!CollectionUtils.isEmpty(request.getServicios())) {
            totalEstimado = request.getServicios().stream()
                    .map(CitaRequest.CitaServicioItemRequest::getPrecio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Cita cita = Cita.builder()
                .clienteId(request.getClienteId())
                .clienteNombre(clienteNombre)
                .barberoId(request.getBarberoId())
                .barberoNombre(barberoNombre)
                .fecha(request.getFecha())
                .hora(request.getHora())
                .estado(EstadoCita.PENDIENTE)
                .totalEstimado(totalEstimado)
                .notas(request.getNotas())
                .build();

        if (!CollectionUtils.isEmpty(request.getServicios())) {
            request.getServicios().forEach(item -> {
                CitaServicio cs = CitaServicio.builder()
                        .cita(cita)
                        .servicioId(item.getServicioId())
                        .servicioNombre(item.getServicioNombre())
                        .precio(item.getPrecio())
                        .duracionMinutos(item.getDuracionMinutos())
                        .build();
                cita.getServicios().add(cs);
            });
        }

        Cita saved = citaRepository.save(cita);
        log.info("Cita creada ID={} barbero={} fecha={}", saved.getId(), barberoNombre, request.getFecha());

        notificacionService.notificarBarbero(saved.getBarberoId(), TipoNotificacion.CITA_ASIGNADA,
                "Nueva cita: " + clienteNombre + " el " + request.getFecha().format(FORMATO_FECHA)
                        + " a las " + request.getHora().format(FORMATO_HORA),
                "CITA", saved.getId());

        return citaMapper.toResponse(saved);
    }

    @Transactional
    public CitaResponse actualizar(Integer id, CitaRequest request) {
        Cita cita = findOrThrow(id);

        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.ATENDIDA) {
            throw new BusinessException("No se puede editar una cita en estado: " + cita.getEstado());
        }

        verificarDisponibilidad(request.getFecha(), request.getBarberoId(), request.getHora(), id);

        Integer barberoAnterior = cita.getBarberoId();

        cita.setBarberoId(request.getBarberoId());
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setNotas(request.getNotas());
        if (request.getTotalEstimado() != null) {
            cita.setTotalEstimado(request.getTotalEstimado());
        }

        if (!CollectionUtils.isEmpty(request.getServicios())) {
            cita.getServicios().clear();
            request.getServicios().forEach(item -> {
                CitaServicio cs = CitaServicio.builder()
                        .cita(cita)
                        .servicioId(item.getServicioId())
                        .servicioNombre(item.getServicioNombre())
                        .precio(item.getPrecio())
                        .duracionMinutos(item.getDuracionMinutos())
                        .build();
                cita.getServicios().add(cs);
            });
        }

        Cita saved = citaRepository.save(cita);
        log.info("Cita actualizada ID={}", id);

        // Solo se notifica si el barbero asignado realmente cambio (incluye
        // el caso "sin asignar -> asignado") -- si se guarda sin tocar el
        // barbero no tiene sentido volver a avisarle.
        if (saved.getBarberoId() != null && !saved.getBarberoId().equals(barberoAnterior)) {
            notificacionService.notificarBarbero(saved.getBarberoId(), TipoNotificacion.CITA_ASIGNADA,
                    "Se te asignó una cita: " + saved.getClienteNombre() + " el "
                            + saved.getFecha().format(FORMATO_FECHA) + " a las " + saved.getHora().format(FORMATO_HORA),
                    "CITA", saved.getId());
        }

        return citaMapper.toResponse(saved);
    }

    @Transactional
    public CitaResponse cambiarEstado(Integer id, EstadoCita nuevoEstado) {
        Cita cita = findOrThrow(id);

        if (cita.getEstado() == EstadoCita.ATENDIDA && nuevoEstado != EstadoCita.ATENDIDA) {
            throw new BusinessException("Una cita atendida no puede cambiar de estado");
        }
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new BusinessException("Una cita cancelada no puede cambiar de estado");
        }

        cita.setEstado(nuevoEstado);
        Cita saved = citaRepository.save(cita);
        log.info("Estado de cita ID={} cambiado a {}", id, nuevoEstado);
        return citaMapper.toResponse(saved);
    }

    private void verificarDisponibilidad(java.time.LocalDate fecha, Integer barberoId,
                                         java.time.LocalTime hora, Integer excludeId) {
        List<Cita> solapadas = excludeId == null
                ? citaRepository.findCitasSolapadas(fecha, barberoId, hora)
                : citaRepository.findCitasSolapadasExcluyendo(fecha, barberoId, hora, excludeId);

        if (!solapadas.isEmpty()) {
            throw new ConflictException("El barbero ya tiene una cita asignada en ese horario");
        }
    }

    private void validarBarbero(Integer barberoId) {
        var barbero = barberoRepository.findById(barberoId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero", barberoId));
        if (!Boolean.TRUE.equals(barbero.getEstado())) {
            throw new BusinessException("El barbero seleccionado no está activo");
        }
    }

    private String resolverClienteNombre(CitaRequest request) {
        if (request.getClienteId() != null) {
            return clienteRepository.findById(request.getClienteId())
                    .map(c -> c.getNombreCompleto())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
        }
        return "Cliente sin registrar";
    }

    private Cita findOrThrow(Integer id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", id));
    }
}
