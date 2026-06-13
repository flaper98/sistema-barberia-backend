package com.barberia.modules.publicbooking.service;

import com.barberia.exception.BusinessException;
import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.appointments.entity.Cita;
import com.barberia.modules.appointments.entity.CitaServicio;
import com.barberia.modules.appointments.entity.EstadoCita;
import com.barberia.modules.appointments.repository.CitaRepository;
import com.barberia.modules.customers.entity.Cliente;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.publicbooking.dto.PublicAvailabilityResponse;
import com.barberia.modules.publicbooking.dto.PublicBookingRequest;
import com.barberia.modules.publicbooking.dto.PublicBookingResponse;
import com.barberia.modules.services.entity.Servicio;
import com.barberia.modules.services.repository.ServicioRepository;
import com.barberia.modules.workers.entity.Barbero;
import com.barberia.modules.workers.repository.BarberoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicBookingService {

    // Horario de atención por defecto — puede moverse a ConfiguracionBarberia
    private static final LocalTime OPEN  = LocalTime.of(8, 0);
    private static final LocalTime CLOSE = LocalTime.of(20, 0);
    private static final int SLOT_INTERVAL_MINUTES = 30;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaRepository      citaRepository;
    private final ClienteRepository   clienteRepository;
    private final BarberoRepository   barberoRepository;
    private final ServicioRepository  servicioRepository;

    // ── Servicios públicos ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Servicio> getActiveServices() {
        return servicioRepository.findByEstadoTrue();
    }

    @Transactional(readOnly = true)
    public List<Barbero> getActiveWorkers() {
        return barberoRepository.findByEstadoTrue();
    }

    // ── Disponibilidad ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PublicAvailabilityResponse getAvailability(
            LocalDate date,
            List<Integer> serviceIds,
            Integer workerId) {

        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException("No se puede consultar disponibilidad en fechas pasadas");
        }

        List<Servicio> servicios = resolveAndValidateServices(serviceIds);
        int totalDuration = servicios.stream().mapToInt(Servicio::getDuracionMinutos).sum();

        List<Barbero> barberos = workerId != null
                ? List.of(resolveActiveWorker(workerId))
                : barberoRepository.findByEstadoTrue();

        List<PublicAvailabilityResponse.WorkerSlots> workerSlots = new ArrayList<>();
        for (Barbero barbero : barberos) {
            List<Cita> existingCitas = citaRepository.findActivasByBarberoIdAndFecha(barbero.getId(), date);
            List<PublicAvailabilityResponse.TimeSlot> slots = computeSlots(existingCitas, totalDuration, date);
            workerSlots.add(PublicAvailabilityResponse.WorkerSlots.builder()
                    .workerId(barbero.getId())
                    .workerName(barbero.getNombreCompleto())
                    .slots(slots)
                    .build());
        }

        return PublicAvailabilityResponse.builder()
                .date(date.toString())
                .serviceDurationMinutes(totalDuration)
                .availableSlots(workerSlots)
                .build();
    }

    private List<PublicAvailabilityResponse.TimeSlot> computeSlots(
            List<Cita> existingCitas, int durationMinutes, LocalDate date) {

        List<PublicAvailabilityResponse.TimeSlot> result = new ArrayList<>();
        LocalTime slotStart = OPEN;
        boolean isToday = date.isEqual(LocalDate.now());

        while (!slotStart.plusMinutes(durationMinutes).isAfter(CLOSE)) {
            LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);

            // No ofrecer slots en el pasado si es hoy
            if (isToday && !slotStart.isAfter(LocalTime.now())) {
                slotStart = slotStart.plusMinutes(SLOT_INTERVAL_MINUTES);
                continue;
            }

            if (!hasOverlap(existingCitas, slotStart, slotEnd)) {
                result.add(PublicAvailabilityResponse.TimeSlot.builder()
                        .startTime(slotStart.format(TIME_FMT))
                        .endTime(slotEnd.format(TIME_FMT))
                        .build());
            }

            slotStart = slotStart.plusMinutes(SLOT_INTERVAL_MINUTES);
        }

        return result;
    }

    /**
     * Verifica si [candidateStart, candidateEnd) se solapa con alguna cita existente.
     * Usa la duración total de los servicios de cada cita para calcular su end time.
     */
    private boolean hasOverlap(List<Cita> existing, LocalTime candidateStart, LocalTime candidateEnd) {
        for (Cita c : existing) {
            int existingDuration = c.getServicios().stream()
                    .mapToInt(CitaServicio::getDuracionMinutos)
                    .sum();
            // Si la cita no tiene servicios, asumir 30 min como mínimo
            if (existingDuration <= 0) existingDuration = 30;

            LocalTime existingEnd = c.getHora().plusMinutes(existingDuration);

            // Overlap: existingStart < candidateEnd AND existingEnd > candidateStart
            if (c.getHora().isBefore(candidateEnd) && existingEnd.isAfter(candidateStart)) {
                return true;
            }
        }
        return false;
    }

    // ── Crear cita pública ──────────────────────────────────────────────────

    @Transactional
    public PublicBookingResponse createAppointment(PublicBookingRequest request) {
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BusinessException("No se pueden crear citas en fechas pasadas");
        }

        Barbero barbero = resolveActiveWorker(request.getWorkerId());
        List<Servicio> servicios = resolveAndValidateServices(request.getServiceIds());
        int totalDuration = servicios.stream().mapToInt(Servicio::getDuracionMinutos).sum();

        LocalTime startTime = request.getStartTime();
        LocalTime endTime   = startTime.plusMinutes(totalDuration);

        // Validar horario de atención
        if (startTime.isBefore(OPEN) || endTime.isAfter(CLOSE)) {
            throw new BusinessException(
                    "El horario seleccionado está fuera del horario de atención (%s - %s)"
                            .formatted(OPEN.format(TIME_FMT), CLOSE.format(TIME_FMT)));
        }

        // Verificar disponibilidad con detección de solapamiento por duración
        List<Cita> existingCitas = citaRepository
                .findActivasByBarberoIdAndFecha(barbero.getId(), request.getAppointmentDate());

        if (hasOverlap(existingCitas, startTime, endTime)) {
            throw new ConflictException(
                    "El horario %s ya no está disponible para %s. Por favor elige otro."
                            .formatted(startTime.format(TIME_FMT), barbero.getNombreCompleto()));
        }

        // Encontrar o crear cliente
        Cliente cliente = findOrCreateCliente(request.getCustomer());

        // Construir y guardar la cita
        BigDecimal totalEstimado = servicios.stream()
                .map(Servicio::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Cita cita = Cita.builder()
                .clienteId(cliente.getId())
                .clienteNombre(cliente.getNombreCompleto())
                .barberoId(barbero.getId())
                .barberoNombre(barbero.getNombreCompleto())
                .fecha(request.getAppointmentDate())
                .hora(startTime)
                .estado(EstadoCita.PENDIENTE)
                .totalEstimado(totalEstimado)
                .notas(sanitize(request.getNotes()))
                .appointmentSource("PUBLIC_WEB")
                .build();

        servicios.forEach(s -> {
            CitaServicio cs = CitaServicio.builder()
                    .cita(cita)
                    .servicioId(s.getId())
                    .servicioNombre(s.getNombre())
                    .precio(s.getPrecio())
                    .duracionMinutos(s.getDuracionMinutos())
                    .build();
            cita.getServicios().add(cs);
        });

        Cita saved = citaRepository.save(cita);

        // Generar código único basado en ID
        String code = "CITA-%06d".formatted(saved.getId());
        saved.setAppointmentCode(code);
        citaRepository.save(saved);

        log.info("Cita pública creada ID={} code={} cliente={} barbero={} fecha={} hora={}",
                saved.getId(), code, cliente.getNombreCompleto(),
                barbero.getNombreCompleto(), request.getAppointmentDate(), startTime);

        return toResponse(saved, servicios, barbero, endTime);
    }

    // ── Consulta por código ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PublicBookingResponse getByCode(String code) {
        Cita cita = citaRepository.findByAppointmentCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "código", code));

        int totalDuration = cita.getServicios().stream()
                .mapToInt(CitaServicio::getDuracionMinutos).sum();

        return PublicBookingResponse.builder()
                .appointmentId(cita.getId())
                .appointmentCode(cita.getAppointmentCode())
                .status(cita.getEstado().name())
                .customerName(cita.getClienteNombre())
                .workerName(cita.getBarberoNombre())
                .services(cita.getServicios().stream()
                        .map(cs -> PublicBookingResponse.ServiceItem.builder()
                                .id(cs.getServicioId())
                                .name(cs.getServicioNombre())
                                .price(cs.getPrecio())
                                .durationMinutes(cs.getDuracionMinutos())
                                .build())
                        .toList())
                .appointmentDate(cita.getFecha().toString())
                .startTime(cita.getHora().format(TIME_FMT))
                .endTime(cita.getHora().plusMinutes(totalDuration).format(TIME_FMT))
                .estimatedTotal(cita.getTotalEstimado())
                .message("Cita encontrada")
                .build();
    }

    // ── Helpers privados ────────────────────────────────────────────────────

    private Barbero resolveActiveWorker(Integer workerId) {
        Barbero barbero = barberoRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero", workerId));
        if (!Boolean.TRUE.equals(barbero.getEstado())) {
            throw new BusinessException("El barbero seleccionado no está activo");
        }
        return barbero;
    }

    private List<Servicio> resolveAndValidateServices(List<Integer> serviceIds) {
        List<Servicio> servicios = new ArrayList<>();
        for (Integer sid : serviceIds) {
            Servicio s = servicioRepository.findById(sid)
                    .orElseThrow(() -> new ResourceNotFoundException("Servicio", sid));
            if (!Boolean.TRUE.equals(s.getEstado())) {
                throw new BusinessException("El servicio '%s' no está disponible".formatted(s.getNombre()));
            }
            servicios.add(s);
        }
        return servicios;
    }

    private Cliente findOrCreateCliente(com.barberia.modules.publicbooking.dto.PublicCustomerRequest req) {
        String correo = (req.getCorreo() != null && !req.getCorreo().isBlank())
                ? req.getCorreo().trim().toLowerCase() : null;
        String telefono = req.getTelefono().trim();

        // 1. Buscar por correo (identificador único más confiable)
        if (correo != null) {
            var byCorreo = clienteRepository.findByCorreo(correo);
            if (byCorreo.isPresent()) return byCorreo.get();
        }

        // 2. Buscar por teléfono como segundo criterio de deduplicación
        var byTelefono = clienteRepository.findByTelefono(telefono);
        if (byTelefono.isPresent()) return byTelefono.get();

        // 3. Crear nuevo cliente
        Cliente nuevo = Cliente.builder()
                .nombre(req.getNombre().trim())
                .apellido(req.getApellido().trim())
                .telefono(telefono)
                .correo(correo)
                .estado(true)
                .build();

        return clienteRepository.save(nuevo);
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("[<>\"']", "");
    }

    private PublicBookingResponse toResponse(Cita saved, List<Servicio> servicios,
                                             Barbero barbero, LocalTime endTime) {
        return PublicBookingResponse.builder()
                .appointmentId(saved.getId())
                .appointmentCode(saved.getAppointmentCode())
                .status(saved.getEstado().name())
                .customerName(saved.getClienteNombre())
                .workerName(barbero.getNombreCompleto())
                .services(servicios.stream()
                        .map(s -> PublicBookingResponse.ServiceItem.builder()
                                .id(s.getId())
                                .name(s.getNombre())
                                .price(s.getPrecio())
                                .durationMinutes(s.getDuracionMinutos())
                                .build())
                        .toList())
                .appointmentDate(saved.getFecha().toString())
                .startTime(saved.getHora().format(TIME_FMT))
                .endTime(endTime.format(TIME_FMT))
                .estimatedTotal(saved.getTotalEstimado())
                .message("Tu cita fue registrada correctamente. Te esperamos el %s a las %s."
                        .formatted(saved.getFecha(), saved.getHora().format(TIME_FMT)))
                .build();
    }
}
