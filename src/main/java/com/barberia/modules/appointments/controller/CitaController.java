package com.barberia.modules.appointments.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.appointments.dto.CitaRequest;
import com.barberia.modules.appointments.dto.CitaResponse;
import com.barberia.modules.appointments.entity.EstadoCita;
import com.barberia.modules.appointments.service.impl.CitaServiceImpl;
import com.barberia.modules.sales.dto.VentaRequest;
import com.barberia.modules.sales.dto.VentaResponse;
import com.barberia.modules.sales.service.impl.VentaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Citas", description = "Gestión de agenda de citas")
public class CitaController {

    private final CitaServiceImpl citaBiz;
    private final VentaServiceImpl ventaBiz;

    @Operation(summary = "Listar citas. Filtrar por fecha y/o estado.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<PageResponse<CitaResponse>>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(ApiResponse.ok(citaBiz.listar(fecha, estado, pageable)));
    }

    @Operation(summary = "Obtener cita por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<CitaResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(citaBiz.obtenerPorId(id)));
    }

    @Operation(summary = "Citas por barbero")
    @GetMapping("/by-worker/{workerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<PageResponse<CitaResponse>>> porBarbero(
            @PathVariable Integer workerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha", "hora"));
        return ResponseEntity.ok(ApiResponse.ok(citaBiz.listarPorBarbero(workerId, pageable)));
    }

    @Operation(summary = "Crear cita")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<CitaResponse>> crear(
            @Valid @RequestBody CitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(citaBiz.crear(request)));
    }

    @Operation(summary = "Actualizar cita")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<CitaResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CitaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cita actualizada", citaBiz.actualizar(id, request)));
    }

    @Operation(summary = "Convertir cita en venta",
               description = "Registra la venta a partir de los datos de la cita. Requiere el body de VentaRequest con los ítems y el método de pago.")
    @PostMapping("/{id}/convert-to-sale")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','BARBER')")
    public ResponseEntity<ApiResponse<VentaResponse>> convertirAVenta(
            @PathVariable Integer id,
            @Valid @RequestBody VentaRequest request) {
        request.setCitaId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ventaBiz.crear(request)));
    }

    @Operation(summary = "Cambiar estado de la cita",
               description = "Estados: PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, NO_ASISTIO")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<CitaResponse>> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        EstadoCita nuevoEstado;
        try {
            nuevoEstado = EstadoCita.valueOf(body.get("estado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Estado inválido: " + body.get("estado")));
        }
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", citaBiz.cambiarEstado(id, nuevoEstado)));
    }
}
