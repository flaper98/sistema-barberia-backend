package com.barberia.modules.workers.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.workers.dto.BarberoRequest;
import com.barberia.modules.workers.dto.BarberoResponse;
import com.barberia.modules.workers.dto.BarberoSummaryResponse;
import com.barberia.modules.workers.service.BarberoService;
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
@RequestMapping("/api/workers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Barberos / Trabajadores", description = "Gestión de barberos y sus estadísticas")
public class BarberoController {

    private final BarberoService barberoService;

    @Operation(summary = "Listar barberos activos")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<PageResponse<BarberoResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("apellido", "nombre"));
        return ResponseEntity.ok(ApiResponse.ok(barberoService.listarBarberos(search, pageable)));
    }

    @Operation(summary = "Obtener barbero por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<BarberoResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(barberoService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear barbero", description = "Solo ADMIN")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BarberoResponse>> crear(
            @Valid @RequestBody BarberoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(barberoService.crear(request)));
    }

    @Operation(summary = "Actualizar barbero", description = "Solo ADMIN")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BarberoResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody BarberoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Barbero actualizado",
                barberoService.actualizar(id, request)));
    }

    @Operation(summary = "Cambiar estado del barbero")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {
        Boolean estado = body.get("estado");
        if (estado == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El campo 'estado' es obligatorio"));
        }
        barberoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado correctamente"));
    }

    @Operation(summary = "Resumen de ingresos y comisiones por barbero",
               description = "Filtra por rango de fechas. Por defecto el mes actual.")
    @GetMapping("/{id}/summary")
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('BARBER') and #id == authentication.principal.usuario.id)")
    public ResponseEntity<ApiResponse<BarberoSummaryResponse>> resumen(
            @PathVariable Integer id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde == null) desde = LocalDate.now().withDayOfMonth(1);
        if (hasta == null) hasta = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(barberoService.obtenerResumen(id, desde, hasta)));
    }
}
