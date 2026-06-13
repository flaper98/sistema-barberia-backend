package com.barberia.modules.publicbooking.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.publicbooking.dto.PublicAvailabilityResponse;
import com.barberia.modules.publicbooking.dto.PublicBookingRequest;
import com.barberia.modules.publicbooking.dto.PublicBookingResponse;
import com.barberia.modules.publicbooking.service.PublicBookingService;
import com.barberia.modules.services.entity.Servicio;
import com.barberia.modules.workers.entity.Barbero;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/booking")
@RequiredArgsConstructor
public class PublicBookingController {

    private final PublicBookingService bookingService;

    /** GET /api/public/booking/services — Servicios activos para reserva */
    @GetMapping("/services")
    public ApiResponse<List<Map<String, Object>>> getServices() {
        List<Map<String, Object>> data = bookingService.getActiveServices().stream()
                .map(this::toServiceMap)
                .toList();
        return ApiResponse.ok(data);
    }

    /** GET /api/public/booking/workers — Barberos activos para reserva */
    @GetMapping("/workers")
    public ApiResponse<List<Map<String, Object>>> getWorkers() {
        List<Map<String, Object>> data = bookingService.getActiveWorkers().stream()
                .map(this::toWorkerMap)
                .toList();
        return ApiResponse.ok(data);
    }

    /** GET /api/public/booking/availability — Horarios disponibles */
    @GetMapping("/availability")
    public ApiResponse<PublicAvailabilityResponse> getAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String serviceIds,
            @RequestParam(required = false) Integer workerId) {

        List<Integer> ids = Arrays.stream(serviceIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .toList();

        return ApiResponse.ok(bookingService.getAvailability(date, ids, workerId));
    }

    /** POST /api/public/booking/appointments — Crear cita pública */
    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PublicBookingResponse> createAppointment(
            @Valid @RequestBody PublicBookingRequest request) {
        return ApiResponse.ok("Cita creada exitosamente", bookingService.createAppointment(request));
    }

    /** GET /api/public/booking/appointments/{code} — Consultar cita por código */
    @GetMapping("/appointments/{code}")
    public ApiResponse<PublicBookingResponse> getByCode(@PathVariable String code) {
        return ApiResponse.ok(bookingService.getByCode(code));
    }

    // ── Mappers seguros — exponen solo campos públicos ──────────────────────

    private Map<String, Object> toServiceMap(Servicio s) {
        return Map.of(
                "id",              s.getId(),
                "nombre",          s.getNombre(),
                "categoria",       s.getCategoria().name(),
                "precio",          s.getPrecio(),
                "duracionMinutos", s.getDuracionMinutos(),
                "descripcion",     s.getDescripcion() != null ? s.getDescripcion() : "",
                "estado",          s.getEstado()
        );
    }

    private Map<String, Object> toWorkerMap(Barbero b) {
        return Map.of(
                "id",          b.getId(),
                "nombre",      b.getNombre(),
                "apellido",    b.getApellido(),
                "especialidad",b.getEspecialidad() != null ? b.getEspecialidad() : "",
                "avatar",      b.getAvatar()      != null ? b.getAvatar() : "",
                "estado",      b.getEstado()
        );
    }
}
